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
package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class digesting a VBL area into an AreaOcean. */
public class AreaTree {
  private static final Logger log = LogManager.getLogger(AreaTree.class);

  /** The original area digested. */
  private AreaOcean theOcean;

  /** The original area, in case we want to return the original area undigested */
  private Area theArea;

  /**
   * Digest the area and store it in theOcean.
   *
   * @param area the area to digest.
   */
  public AreaTree(Area area) {
    digest(area);
  }

  /**
   * Gets the most nested ocean or island that contains `point`.
   *
   * @param point
   * @return
   */
  public AreaContainer getContainerAt(Point2D point) {
    return theOcean.getDeepestContainerAt(point);
  }

  public Area getArea() {
    return theArea;
  }

  private void digest(Area area) {
    if (area == null) {
      return;
    }

    theArea = area;

    List<AreaOcean> oceanList = new ArrayList<AreaOcean>();
    List<AreaIsland> islandList = new ArrayList<AreaIsland>();

    // Break the big area into independent areas
    double[] coords = new double[6];
    AreaMeta areaMeta = new AreaMeta();
    for (PathIterator iter = area.getPathIterator(null, 1e-2); !iter.isDone(); iter.next()) {
      int type = iter.currentSegment(coords);
      switch (type) {
        case PathIterator.SEG_CLOSE:
          areaMeta.close();

          // Holes are oceans, solids are islands
          if (!areaMeta.isEmpty()) {
            if (areaMeta.isHole()) {
              oceanList.add(new AreaOcean(areaMeta));
            } else {
              islandList.add(new AreaIsland(areaMeta));
            }
          }
          break;
        case PathIterator.SEG_LINETO:
          areaMeta.addPoint(coords[0], coords[1]);
          break;
        case PathIterator.SEG_MOVETO:
          areaMeta = new AreaMeta();
          areaMeta.addPoint(coords[0], coords[1]);
          break;
      }
    }
    // Create the hierarchy
    // Start by putting each ocean into the containing island
    // Every ocean should have a containing island. There is only one ocean that doesn't
    // have an explicit island and that's the global scope ocean container
    for (AreaOcean ocean : oceanList) {
      AreaIsland island = findSmallestContainer(ocean, islandList);
      if (island == null) {
        log.warn("Weird, I couldn't find an island for an ocean.  Bad/overlapping VBL?");
        continue;
      }
      island.addOcean(ocean);
      ocean.setParentIsland(island);
    }
    // Now put each island into the containing ocean
    List<AreaIsland> globalIslandList = new ArrayList<AreaIsland>();
    for (AreaIsland island : islandList) {
      AreaOcean ocean = findSmallestContainer(island, oceanList);
      if (ocean == null) {
        globalIslandList.add(island);
        continue;
      }
      ocean.addIsland(island);
      island.setParentOcean(ocean);
    }

    // Now we have our hierarchy, just hook up the global space
    theOcean = new AreaOcean(null);
    for (AreaIsland island : globalIslandList) {
      theOcean.addIsland(island);
      island.setParentOcean(theOcean);
    }
  }

  private <T extends AreaContainer> T findSmallestContainer(AreaContainer item, List<T> list) {
    T smallest = null;
    for (T container : list) {
      if (!GraphicsUtil.contains(container.getBounds(), item.getBounds())) {
        continue;
      }
      smallest = getSmallest(smallest, container);
    }
    return smallest;
  }

  private <T extends AreaContainer> T getSmallest(T left, T right) {
    // Something is smaller than nothing, for our purposes
    if (left == null) {
      return right;
    }
    if (right == null) {
      return left;
    }
    // Presumably the container with the smaller area will be the contained area
    double leftSize =
        left.getBounds().getBounds().getWidth() * left.getBounds().getBounds().getHeight();
    double rightSize =
        right.getBounds().getBounds().getWidth() * right.getBounds().getBounds().getHeight();

    return leftSize < rightSize ? left : right;
  }
}
