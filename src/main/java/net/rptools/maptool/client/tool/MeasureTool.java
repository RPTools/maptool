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
package net.rptools.maptool.client.tool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.ImageCursor;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.GraphicsUtil;

/** */
public class MeasureTool extends DefaultTool implements ZoneOverlay {

  public static final String CURSOR_NAME = "Measure tool";

  private ZoneWalker walker;
  private Path<ZonePoint> gridlessPath;
  private static Cursor measureCursor;
  private static javafx.scene.Cursor measureCursorFX;

  public MeasureTool() {
    measureCursor =
        Toolkit.getDefaultToolkit()
            .createCustomCursor(
                RessourceManager.getImage(Images.MEASURE), new Point(2, 28), CURSOR_NAME);
    Platform.runLater(
        () ->
            measureCursorFX = new ImageCursor(RessourceManager.getFxImage(Images.MEASURE), 2, 28));
  }

  public static Cursor getMeasureCursor() {
    return measureCursor;
  }

  public static javafx.scene.Cursor getMeasureCursorFX() {
    return measureCursorFX;
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    renderer.setCursor(measureCursor);
    super.attachTo(renderer);
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    renderer.setCursor(Cursor.getDefaultCursor());
    super.detachFrom(renderer);
  }

  @Override
  public String getTooltip() {
    return "tool.measure.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.measure.instructions";
  }

  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (walker == null && gridlessPath == null) {
      return;
    }
    if (walker != null) {
      renderer.renderPath(g, walker.getPath(), renderer.getZone().getGrid().getDefaultFootprint());
      ScreenPoint sp = walker.getLastPoint().convertToScreen(renderer);

      int y = (int) sp.y - 10;
      int x = (int) sp.x + (int) (renderer.getScaledGridSize() / 2);
      GraphicsUtil.drawBoxedString(g, Double.toString(walker.getDistance()), x, y);
    } else {
      Object oldAA = SwingUtil.useAntiAliasing(g);
      g.setColor(Color.black);
      ScreenPoint lastPoint = null;
      for (ZonePoint zp : gridlessPath.getCellPath()) {
        if (lastPoint == null) {
          lastPoint = ScreenPoint.fromZonePoint(renderer, zp);
          continue;
        }
        ScreenPoint nextPoint = ScreenPoint.fromZonePoint(renderer, zp.x, zp.y);
        g.drawLine((int) lastPoint.x, (int) lastPoint.y, (int) nextPoint.x, (int) nextPoint.y);
        lastPoint = nextPoint;
      }

      // distance
      double c = 0;
      ZonePoint lastZP = null;
      for (ZonePoint zp : gridlessPath.getCellPath()) {
        if (lastZP == null) {
          lastZP = zp;
          continue;
        }
        int a = lastZP.x - zp.x;
        int b = lastZP.y - zp.y;
        c += Math.sqrt(a * a + b * b);
        lastZP = zp;
      }

      // int a = lastPoint.x - (set.offsetX + token.getX());
      // int b = lastPoint.y - (set.offsetY + token.getY());
      //
      // c += Math.sqrt(a*a + b*b)/zone.getUnitsPerCell();

      c /= renderer.getZone().getGrid().getSize();
      c *= renderer.getZone().getUnitsPerCell();

      String distance = NumberFormat.getInstance().format(c);
      ScreenPoint sp = ScreenPoint.fromZonePoint(renderer, lastZP.x, lastZP.y);
      GraphicsUtil.drawBoxedString(g, distance, (int) sp.x, (int) sp.y - 20);

      SwingUtil.restoreAntiAliasing(g, oldAA);
    }
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false),
        new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
            if (walker == null && gridlessPath == null) {
              return;
            }
            // Waypoint
            if (walker != null) {
              CellPoint cp =
                  renderer
                      .getZone()
                      .getGrid()
                      .convert(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
              walker.toggleWaypoint(cp);
            } else {
              gridlessPath.addWayPoint(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
              gridlessPath.addPathCell(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
            }
          }
        });
  }

  ////
  // MOUSE LISTENER
  @Override
  public void mousePressed(java.awt.event.MouseEvent e) {
    ZoneRenderer renderer = (ZoneRenderer) e.getSource();

    if (SwingUtilities.isLeftMouseButton(e)) {
      if (renderer.getZone().getGrid().getCapabilities().isPathingSupported()) {
        CellPoint cellPoint = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
        walker = renderer.getZone().getGrid().createZoneWalker();
        walker.addWaypoints(cellPoint, cellPoint);
      } else {
        gridlessPath = new Path<ZonePoint>();
        gridlessPath.addPathCell(new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer));

        // Add a second one that will be replaced as the mouse moves around the screen
        gridlessPath.addPathCell(new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer));
      }
      renderer.repaint();
      return;
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    ZoneRenderer renderer = (ZoneRenderer) e.getSource();

    if (SwingUtilities.isLeftMouseButton(e)) {
      walker = null;
      gridlessPath = null;
      renderer.repaint();
      return;
    }
    super.mouseReleased(e);
  }

  ////
  // MOUSE MOTION LISTENER
  @Override
  public void mouseDragged(MouseEvent e) {
    if (SwingUtilities.isRightMouseButton(e)) {
      super.mouseDragged(e);
      return;
    }
    ZoneRenderer renderer = (ZoneRenderer) e.getSource();
    if (walker != null && renderer.getZone().getGrid().getCapabilities().isPathingSupported()) {
      CellPoint cellPoint = renderer.getCellAt(new ScreenPoint(e.getX(), e.getY()));
      walker.replaceLastWaypoint(cellPoint);
    } else if (gridlessPath != null) {
      gridlessPath.replaceLastPoint(new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer));
    }
    renderer.repaint();
  }
}
