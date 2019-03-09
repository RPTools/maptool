/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.zone;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.vbl.AreaOcean;
import net.rptools.maptool.client.ui.zone.vbl.AreaTree;
import net.rptools.maptool.client.ui.zone.vbl.VisibleAreaSegment;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ExposedAreaMetaData;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.GridCapabilities;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FogUtil {
  private static final Logger log = LogManager.getLogger(FogUtil.class);

  public static Area calculateVisibility(int x, int y, Area vision, AreaTree topology) {
    CodeTimer timer = new CodeTimer("calculateVisibility");

    vision = new Area(vision);
    vision.transform(AffineTransform.getTranslateInstance(x, y));

    Point origin = new Point(x, y);

    AreaOcean ocean = topology.getOceanAt(origin);
    if (ocean == null) {
      return null;
    }
    int skippedAreas = 0;

    // Jamz: Updated comparison for VisibleAreaSegment, hopefully this fixes the exceptions
    // If exception still happens, this JVM option can be used as a temp fix:
    // -Djava.util.Arrays.useLegacyMergeSort=true
    // http://dertompson.com/2012/11/23/sort-algorithm-changes-in-java-7/
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7075600
    List<VisibleAreaSegment> segmentList =
        new ArrayList<VisibleAreaSegment>(ocean.getVisibleAreaSegments(origin));
    Collections.sort(segmentList);

    List<Area> clearedAreaList = new LinkedList<Area>();
    nextSegment:
    for (VisibleAreaSegment segment : segmentList) {
      Rectangle r = segment.getPath().getBounds();
      for (Area clearedArea : clearedAreaList) {
        if (clearedArea.contains(r)) {
          skippedAreas++;
          continue nextSegment;
        }
      }
      Area area = segment.getArea();

      timer.start("combine");
      Area intersectedArea = null;
      for (ListIterator<Area> iter = clearedAreaList.listIterator(); iter.hasNext(); ) {
        Area clearedArea = iter.next();
        if (clearedArea.intersects(r)) {
          clearedArea.add(area);
          iter.remove(); // we'll put it on the back of the list to prevent crazy growth at the
          // front
          intersectedArea = clearedArea;
          break;
        }
      }
      timer.stop("combine");
      clearedAreaList.add(intersectedArea != null ? intersectedArea : area);
    }

    while (clearedAreaList.size() > 1) {
      Area a1 = clearedAreaList.remove(0);
      Area a2 = clearedAreaList.remove(0);

      a1.add(a2);
      clearedAreaList.add(a1);
    }

    if (clearedAreaList.size() > 0) {
      vision.subtract(clearedAreaList.get(0));
    }

    // For simplicity, this catches some of the edge cases
    return vision;
  }

  public static void exposeVisibleArea(final ZoneRenderer renderer, Set<GUID> tokenSet) {
    exposeVisibleArea(renderer, tokenSet, false);
  }

  // Jamz: Added boolean exposeCurrentOnly
  // @SuppressWarnings("unchecked")
  @SuppressWarnings("unchecked")
  public static void exposeVisibleArea(
      final ZoneRenderer renderer, Set<GUID> tokenSet, boolean exposeCurrentOnly) {
    final Zone zone = renderer.getZone();

    for (GUID tokenGUID : tokenSet) {
      Token token = zone.getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      if (!token.getHasSight()) {
        continue;
      }
      if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
        continue;
      }

      if (zone.getWaypointExposureToggle() && !exposeCurrentOnly) {
        if (token.getLastPath() == null) return;

        List<CellPoint> wayPointList = (List<CellPoint>) token.getLastPath().getWayPointList();

        final Token tokenClone = token.clone();

        for (final Object cell : wayPointList) {
          ZonePoint zp = null;
          if (cell instanceof CellPoint) {
            zp = zone.getGrid().convert((CellPoint) cell);
          } else {
            zp = (ZonePoint) cell;
          }

          tokenClone.setX(zp.x);
          tokenClone.setY(zp.y);

          renderer.flush(tokenClone);
          Area tokenVision = renderer.getZoneView().getVisibleArea(tokenClone);
          if (tokenVision != null) {
            Set<GUID> filteredToks = new HashSet<GUID>();
            filteredToks.add(tokenClone.getId());
            zone.exposeArea(tokenVision, tokenClone);
            MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision, filteredToks);
          }
        }
        // System.out.println("2. Token: " + token.getGMName() + " - ID: " + token.getId());
        renderer.flush(token);
      } else {
        renderer.flush(token);
        Area tokenVision = renderer.getVisibleArea(token);
        if (tokenVision != null) {
          Set<GUID> filteredToks = new HashSet<GUID>();
          filteredToks.add(token.getId());
          zone.exposeArea(tokenVision, filteredToks);
          MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision, filteredToks);
        }
      }
    }
  }

  public static void exposeVisibleAreaAtWaypoint(
      final ZoneRenderer renderer, Set<GUID> tokenSet, ZonePoint zp) {
    final Zone zone = renderer.getZone();

    for (GUID tokenGUID : tokenSet) {
      Token token = zone.getToken(tokenGUID);
      if (token == null) {
        continue;
      }
      if (!token.getHasSight()) {
        continue;
      }
      if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
        continue;
      }

      ZonePoint zpStart = new ZonePoint(token.getX(), token.getY());
      token.setX(zp.x);
      token.setY(zp.y);
      renderer.flush(token);

      Area tokenVision = renderer.getZoneView().getVisibleArea(token);
      if (tokenVision != null) {
        Set<GUID> filteredToks = new HashSet<GUID>();
        filteredToks.add(token.getId());
        zone.exposeArea(tokenVision, token);
        MapTool.serverCommand().exposeFoW(zone.getId(), tokenVision, filteredToks);
      }

      token.setX(zpStart.x);
      token.setY(zpStart.y);
      renderer.flush(token);
    }
  }

  /**
   * This function is called by Meta-Shift-O, the token right-click, Expose -> only Currently
   * visible menu, from the Client/Server methods calls from
   * net.rptools.maptool.server.ServerMethodHandler.exposePCArea(GUID), and the macro
   * exposePCOnlyArea().
   *
   * @author updated Jamz
   * @since updated 1.4.0.1
   * @param renderer
   */
  public static void exposePCArea(ZoneRenderer renderer) {
    Set<GUID> tokenSet = new HashSet<GUID>();
    List<Token> tokList = renderer.getZone().getPlayerTokensWithSight();

    String playerName = MapTool.getPlayer().getName();
    boolean isGM = MapTool.getPlayer().getRole() == Role.GM;

    for (Token token : tokList) {
      boolean owner = token.isOwner(playerName) || isGM;

      if ((!MapTool.isPersonalServer() || MapTool.getServerPolicy().isUseIndividualViews())
          && !owner) {
        continue;
      }

      tokenSet.add(token.getId());
    }

    renderer.getZone().clearExposedArea(tokenSet);
    // this was .clearExposedArea(), changed to expose current area only vs last path
    exposeVisibleArea(renderer, tokenSet, true);
  }

  // Jamz: Expose not just PC tokens but also any NPC tokens the player owns
  /**
   * This function is called by Meta-Shift-F and the macro exposeAllOwnedArea()
   *
   * <p>Changed base function to select tokens now on ownership and based on TokenSelection menu
   * buttons.
   *
   * @author Jamz
   * @since 1.4.0.1
   * @param renderer
   */
  public static void exposeAllOwnedArea(ZoneRenderer renderer) {
    Set<GUID> tokenSet = new HashSet<GUID>();

    // Jamz: Possibly pass a variable to override buttons? Also, maybe add a return a list of ID's
    List<Token> tokList = renderer.getZone().getOwnedTokensWithSight(MapTool.getPlayer());

    for (Token token : tokList) tokenSet.add(token.getId());

    // System.out.println("tokList: " + tokList.toString());

    /*
     * TODO: Jamz: May need to add back the isUseIndividualViews() logic later after testing... String playerName = MapTool.getPlayer().getName(); boolean isGM = MapTool.getPlayer().getRole() ==
     * Role.GM;
     *
     * for (Token token : tokList) { boolean owner = token.isOwner(playerName) || isGM;
     *
     * //System.out.println("token: " + token.getName() + ", owner: " + owner);
     *
     * if ((!MapTool.isPersonalServer() || MapTool.getServerPolicy().isUseIndividualViews()) && !owner) { continue; } tokenSet.add(token.getId()); }
     */

    renderer.getZone().clearExposedArea(tokenSet);
    exposeVisibleArea(renderer, tokenSet, true);
  }

  public static void restoreFoW(final ZoneRenderer renderer) {
    // System.out.println("Zone ID: " + renderer.getZone().getId());

    renderer.getZone().clearExposedArea();
    renderer.flush();
    MapTool.serverCommand().clearExposedArea(renderer.getZone().getId());
  }

  public static void exposeLastPath(final ZoneRenderer renderer, final Set<GUID> tokenSet) {
    CodeTimer timer = new CodeTimer("exposeLastPath");

    final Zone zone = renderer.getZone();
    final Grid grid = zone.getGrid();
    GridCapabilities caps = grid.getCapabilities();

    if (!caps.isPathingSupported() || !caps.isSnapToGridSupported()) {
      return;
    }

    final Set<GUID> filteredToks = new HashSet<GUID>(2);

    for (final GUID tokenGUID : tokenSet) {
      final Token token = zone.getToken(tokenGUID);
      timer.start("exposeLastPath-" + token.getName());

      @SuppressWarnings("unchecked")
      Path<CellPoint> lastPath = (Path<CellPoint>) token.getLastPath();

      if (lastPath == null) return;

      Map<GUID, ExposedAreaMetaData> fullMeta = zone.getExposedAreaMetaData();
      GUID exposedGUID = token.getExposedAreaGUID();
      ExposedAreaMetaData meta = fullMeta.get(exposedGUID);

      if (meta == null) {
        meta = new ExposedAreaMetaData();
        fullMeta.put(exposedGUID, meta);
      }

      /*
       * Lee: this assumes that all tokens that pass through the checks above stored CellPoints. Well, they don't, not in the context of a snapped to grid follower following an unsnapped key
       * token. Commenting out and replacing... for (CellPoint cell : lastPath.getCellPath()) {
       */
      final ExposedAreaMetaData metaCopy = meta;
      final Token tokenClone = new Token(token);
      final ZoneView zoneView = renderer.getZoneView();
      Area visionArea = new Area();

      // Lee: get path according to zone's way point exposure toggle...
      List<CellPoint> processPath =
          zone.getWaypointExposureToggle() ? lastPath.getWayPointList() : lastPath.getCellPath();

      int stepCount = processPath.size();
      log.debug("Path size = " + stepCount);

      for (final Object cell : processPath) {
        if (cell instanceof CellPoint) {
          // timer.start("expose" + cell.toString());
          ZonePoint zp = grid.convert((CellPoint) cell);
          tokenClone.setX(zp.x);
          tokenClone.setY(zp.y);
          metaCopy.addToExposedAreaHistory(zoneView.getVisibleArea(tokenClone));
          // zoneView.flush(tokenClone);
          // timer.start("expose" + cell.toString());
        }

        Area currVisionArea = zoneView.getVisibleArea(tokenClone);
        if (currVisionArea != null) {
          visionArea.add(currVisionArea);
          meta.addToExposedAreaHistory(currVisionArea);
        }
        zoneView.flush(tokenClone);
      }

      timer.stop("exposeLastPath-" + token.getName());
      renderer.flush(tokenClone);
      renderer.flush(token); // calls ZoneView.flush() -- too bad, I'd like to eliminate it...

      zone.exposeArea(visionArea, token);

      filteredToks.clear();
      filteredToks.add(token.getId());
      zone.putToken(token);
      MapTool.serverCommand().exposeFoW(zone.getId(), visionArea, filteredToks);
      MapTool.serverCommand().updateExposedAreaMeta(zone.getId(), exposedGUID, metaCopy);
    }

    String results = timer.toString();
    MapTool.getProfilingNoteFrame().addText(results);
    // System.out.println(results);
    timer.clear();
  }

  /**
   * Find the center point of a vision TODO: This is a horrible horrible method. the API is just
   * plain disgusting. But it'll work to consolidate all the places this has to be done until we can
   * encapsulate it into the vision itself
   */
  public static Point calculateVisionCenter(Token token, Zone zone) {
    Grid grid = zone.getGrid();
    int x = 0, y = 0;

    Rectangle bounds = null;
    if (token.isSnapToGrid()) {
      bounds =
          token
              .getFootprint(grid)
              .getBounds(grid, grid.convert(new ZonePoint(token.getX(), token.getY())));
    } else {
      bounds = token.getBounds(zone);
    }

    x = bounds.x + bounds.width / 2;
    y = bounds.y + bounds.height / 2;

    return new Point(x, y);
  }

  public static void main(String[] args) {
    System.out.println("Creating topology");
    final int topSize = 20000;
    final Area topology = new Area();
    Random r = new Random(12345);
    for (int i = 0; i < 500; i++) {
      int x = r.nextInt(topSize);
      int y = r.nextInt(topSize);
      int w = r.nextInt(500) + 50;
      int h = r.nextInt(500) + 50;
      topology.add(new Area(new Rectangle(x, y, w, h)));
    }

    // Make sure the the center point is not contained inside the blocked area
    topology.subtract(new Area(new Rectangle(topSize / 2 - 200, topSize / 2 - 200, 400, 400)));

    final Area vision =
        new Area(
            new Rectangle(
                -Integer.MAX_VALUE / 2,
                -Integer.MAX_VALUE / 2,
                Integer.MAX_VALUE,
                Integer.MAX_VALUE));

    int pointCount = 0;
    for (PathIterator iter = topology.getPathIterator(null); !iter.isDone(); iter.next()) {
      pointCount++;
    }
    System.out.println("Starting test " + pointCount + " points");
    final AreaData data = new AreaData(topology);
    data.digest();
    final AreaTree tree = new AreaTree(topology);

    // Make sure all classes are loaded
    calculateVisibility(topSize / 2, topSize / 2, vision, tree);

    Area area1 = new Area();
    for (int i = 0; i < 1; i++) {
      // Return value isn't used except for debugging
      area1 = calculateVisibility(topSize / 2, topSize / 2, vision, tree);
    }
    area1.equals(null); // Eliminates the warning about "area1 never read locally" from Eclipse

    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setBounds(0, 0, 400, 200);
    f.setLayout(new GridLayout());
    f.add(
        new JPanel() {
          BufferedImage topImage = null;
          Area theArea = null;

          {
            addMouseMotionListener(
                new MouseMotionAdapter() {
                  @Override
                  public void mouseDragged(MouseEvent e) {
                    final long start = System.currentTimeMillis();
                    Dimension size = getSize();
                    int x = (int) ((e.getX() - (size.width / 2)) / (size.width / 2.0 / topSize));
                    int y = (int) (e.getY() / (size.height / 2.0 / topSize) / 2);
                    theArea = FogUtil.calculateVisibility(x, y, vision, tree);
                    System.out.println("Calc: " + (System.currentTimeMillis() - start));
                    repaint();
                  }
                });
            addMouseListener(
                new MouseAdapter() {
                  @Override
                  public void mousePressed(MouseEvent e) {
                    final long start = System.currentTimeMillis();
                    Dimension size = getSize();
                    int x = (int) ((e.getX() - (size.width / 2)) / (size.width / 2.0 / topSize));
                    int y = (int) (e.getY() / (size.height / 2.0 / topSize) / 2);
                    theArea = FogUtil.calculateVisibility(x, y, vision, tree);
                    System.out.println("Calc: " + (System.currentTimeMillis() - start));
                    repaint();
                  }
                });
          }

          @Override
          protected void paintComponent(Graphics g) {
            Dimension size = getSize();
            g.setColor(Color.white);
            g.fillRect(0, 0, size.width, size.height);

            Graphics2D g2d = (Graphics2D) g;

            AffineTransform at =
                AffineTransform.getScaleInstance(
                    (size.width / 2) / (double) topSize, (size.height) / (double) topSize);
            if (topImage == null) {
              Area top = topology.createTransformedArea(at);
              topImage = new BufferedImage(size.width / 2, size.height, BufferedImage.OPAQUE);

              Graphics2D g2 = topImage.createGraphics();
              g2.setColor(Color.white);
              g2.fillRect(0, 0, size.width / 2, size.height);

              g2.setColor(Color.green);
              g2.fill(top);
              g2.dispose();
            }
            g.setColor(Color.black);
            g.drawLine(size.width / 2, 0, size.width / 2, size.height);

            // g.setClip(new Rectangle(0, 0, size.width/2, size.height));
            // g.setColor(Color.green);
            // g2d.fill(top);
            //
            // g.setColor(Color.lightGray);
            // g2d.fill(a1.createTransformedArea(at));

            g.setClip(new Rectangle(size.width / 2, 0, size.width / 2, size.height));
            g2d.translate(200, 0);
            g.setColor(Color.green);
            g2d.drawImage(topImage, 0, 0, this);
            g.setColor(Color.gray);
            if (theArea != null) {
              g2d.fill(theArea.createTransformedArea(at));
            }
            for (AreaMeta areaMeta : data.getAreaList(new Point(0, 0))) {
              g.setColor(Color.red);
              g2d.draw(areaMeta.area.createTransformedArea(at));
            }
            // g.setColor(Color.red);
            // System.out.println("Size: " + data.metaList.size() + " - " + skippedAreaList.size());
            // for (Area area : skippedAreaList) {
            // g2d.fill(area.createTransformedArea(at));
            // }
            g2d.translate(-200, 0);
          }
        });
    f.setVisible(true);
  }
}
