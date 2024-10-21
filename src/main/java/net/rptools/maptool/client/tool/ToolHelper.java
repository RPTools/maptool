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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.text.NumberFormat;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.util.GraphicsUtil;

/**
 * @author trevor
 */
public class ToolHelper {

  private static AbstractAction deleteTokenAction =
      new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
          // Check to see if this is the required action
          if (!MapTool.confirmTokenDelete()) {
            return;
          }

          ZoneRenderer renderer = (ZoneRenderer) e.getSource();
          AppActions.deleteTokens(renderer.getZone(), renderer.getSelectedTokenSet());
        }
      };

  public static void drawIsoRectangleMeasurement(
      ZoneRenderer renderer, Graphics2D g, ScreenPoint north, ScreenPoint west, ScreenPoint east) {
    if (g != null) {
      g.setColor(Color.white);
      g.setStroke(new BasicStroke(3));
      g.draw(new Line2D.Double(north.x, north.y - 20, north.x, north.y - 10));
      g.draw(new Line2D.Double(north.x, north.y - 15, east.x, east.y - 15));
      g.draw(new Line2D.Double(east.x, east.y - 20, east.x, east.y - 10));
      g.draw(new Line2D.Double(north.x, north.y - 15, west.x, west.y - 15));
      g.draw(new Line2D.Double(west.x, west.y - 20, west.x, west.y - 10));

      g.setColor(Color.black);
      g.setStroke(new BasicStroke(1));
      // Same points, but in thin black.
      g.draw(new Line2D.Double(north.x, north.y - 20, north.x, north.y - 10));
      g.draw(new Line2D.Double(north.x, north.y - 15, east.x, east.y - 15));
      g.draw(new Line2D.Double(east.x, east.y - 20, east.x, east.y - 10));
      g.draw(new Line2D.Double(north.x, north.y - 15, west.x, west.y - 15));
      g.draw(new Line2D.Double(west.x, west.y - 20, west.x, west.y - 10));

      String displayString =
          NumberFormat.getInstance().format(isometricDistance(renderer, north, east));
      GraphicsUtil.drawBoxedString(g, displayString, (int) (north.x + 25), (int) (north.y - 25));
      displayString = NumberFormat.getInstance().format(isometricDistance(renderer, north, west));
      GraphicsUtil.drawBoxedString(g, displayString, (int) (north.x - 25), (int) (north.y - 25));
    }
  }

  public static void drawBoxedMeasurement(
      ZoneRenderer renderer, Graphics2D g, ScreenPoint startPoint, ScreenPoint endPoint) {
    if (!MapTool.getFrame().isPaintDrawingMeasurement()) {
      return;
    }

    // Calculations
    int left = (int) Math.min(startPoint.x, endPoint.x);
    int top = (int) Math.min(startPoint.y, endPoint.y);
    int right = (int) Math.max(startPoint.x, endPoint.x);
    int bottom = (int) Math.max(startPoint.y, endPoint.y);

    // outline
    g.setColor(Color.white);
    g.setStroke(new BasicStroke(3));
    // HORIZONTAL Measure
    g.drawLine(left, top - 15, right, top - 15);
    g.drawLine(left, top - 20, left, top - 10);
    g.drawLine(right, top - 20, right, top - 10);
    // VETICAL Measure
    g.drawLine(right + 15, top, right + 15, bottom);
    g.drawLine(right + 10, top, right + 20, top);
    g.drawLine(right + 10, bottom, right + 20, bottom);
    // inner line
    g.setColor(Color.black);
    g.setStroke(new BasicStroke(1));
    // HORIZONTAL Measure
    g.drawLine(left, top - 15, right, top - 15);
    g.drawLine(left, top - 20, left, top - 10);
    g.drawLine(right, top - 20, right, top - 10);
    // VETICAL Measure
    g.drawLine(right + 15, top, right + 15, bottom);
    g.drawLine(right + 10, top, right + 20, top);
    g.drawLine(right + 10, bottom, right + 20, bottom);

    // Horizontal number
    String displayString =
        NumberFormat.getInstance()
            .format(
                euclideanDistance(
                    renderer, new ScreenPoint(left, top), new ScreenPoint(right, top)));
    GraphicsUtil.drawBoxedString(g, displayString, left + (right - left) / 2, top - 15);

    // Verical number
    displayString =
        NumberFormat.getInstance()
            .format(
                euclideanDistance(
                    renderer, new ScreenPoint(right, top), new ScreenPoint(right, bottom)));
    GraphicsUtil.drawBoxedString(g, displayString, right + 18, bottom + (top - bottom) / 2);
  }

  public static void drawMeasurement(
      ZoneRenderer renderer, Graphics2D g, ScreenPoint startPoint, ScreenPoint endPoint) {
    if (!MapTool.getFrame().isPaintDrawingMeasurement()) {
      return;
    }

    boolean dirLeft = startPoint.x > endPoint.x;
    boolean dirUp = startPoint.y < endPoint.y;

    String displayString =
        NumberFormat.getInstance().format(euclideanDistance(renderer, startPoint, endPoint));

    GraphicsUtil.drawBoxedString(
        g,
        displayString,
        (int) endPoint.x + (dirLeft ? -15 : 10),
        (int) endPoint.y + (dirUp ? 15 : -15),
        dirLeft ? SwingUtilities.LEFT : SwingUtilities.RIGHT);
  }

  /**
   * Draw a measurement on the passed graphics object.
   *
   * @param g Draw the measurement here.
   * @param distance The size of the measurement in feet
   * @param x The x location of the measurement
   * @param y The y location of the measurement
   */
  public static void drawMeasurement(Graphics2D g, double distance, int x, int y) {
    if (!MapTool.getFrame().isPaintDrawingMeasurement()) {
      return;
    }
    String radius = NumberFormat.getInstance().format(distance);
    GraphicsUtil.drawBoxedString(g, radius, x, y);
  }

  private static double euclideanDistance(ZoneRenderer renderer, ScreenPoint p1, ScreenPoint p2) {
    double a = p2.x - p1.x;
    double b = p2.y - p1.y;

    return Math.sqrt(a * a + b * b)
        * renderer.getZone().getUnitsPerCell()
        / renderer.getScaledGridSize();
  }

  private static double isometricDistance(ZoneRenderer renderer, ScreenPoint p1, ScreenPoint p2) {
    double b = p2.y - p1.y;
    return 2 * b * renderer.getZone().getUnitsPerCell() / renderer.getScaledGridSize();
  }

  protected static AbstractAction getDeleteTokenAction() {
    return deleteTokenAction;
  }
}
