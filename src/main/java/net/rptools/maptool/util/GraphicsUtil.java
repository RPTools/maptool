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
package net.rptools.maptool.util;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.ImageLabel;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;

/** */
public class GraphicsUtil {
  public static final int BOX_PADDINGX = 10;
  public static final int BOX_PADDINGY = 2;

  // TODO: Make this configurable
  public static final ImageLabel GREY_LABEL =
      new ImageLabel(RessourceManager.getImage(Images.BOX_GRAY), 4, 4);
  public static final ImageLabel BLUE_LABEL =
      new ImageLabel(RessourceManager.getImage(Images.BOX_BLUE), 4, 4);
  public static final ImageLabel DARK_GREY_LABEL =
      new ImageLabel(RessourceManager.getImage(Images.BOX_DARK_GRAY), 4, 4);

  /**
   * A multiline text wrapping popup.
   *
   * @param g The graphics to draw into
   * @param string - the string to display in he popup
   * @param x the x position of the popup
   * @param y the y position of the popup
   * @param justification justification of the text
   * @param maxWidth - the max width in pixels before wrapping the text
   * @return the surrounding rectangle for the popup
   */
  public static Rectangle drawPopup(
      Graphics2D g, String string, int x, int y, int justification, int maxWidth) {
    return drawPopup(g, string, x, y, justification, Color.black, Color.white, maxWidth, 0.5f);
  }

  public static Rectangle drawPopup(
      Graphics2D g,
      String string,
      int x,
      int y,
      int justification,
      Color background,
      Color foreground,
      int maxWidth,
      float alpha) {
    if (string == null) {
      string = "";
    }
    // TODO: expand to work for variable width fonts.
    Font oldFont = g.getFont();
    Font fixedWidthFont = new Font("Courier New", 0, 12);
    g.setFont(fixedWidthFont);
    FontMetrics fm = g.getFontMetrics();

    StringBuilder sb = new StringBuilder();
    while (SwingUtilities.computeStringWidth(fm, sb.toString()) < maxWidth) {
      sb.append("0");
    }
    int maxChars = sb.length() - 1;

    string = StringUtil.wrapText(string, Math.min(maxChars, string.length()));

    String pattern = "\n";
    String[] stringByLine = string.split(pattern);
    int rows = stringByLine.length;

    String longestRow = "";
    for (String s : stringByLine) {
      if (longestRow.length() < s.length()) {
        longestRow = s;
      }
    }
    int strPixelHeight = fm.getHeight();
    int strPixelWidth = SwingUtilities.computeStringWidth(fm, longestRow);

    int width = strPixelWidth + BOX_PADDINGX * 2;
    int height = strPixelHeight * rows + BOX_PADDINGY * 2;

    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();

    y = Math.max(y - height, BOX_PADDINGY);
    switch (justification) {
      case SwingUtilities.CENTER:
        x = x - strPixelWidth / 2 - BOX_PADDINGX;
        break;
      case SwingUtilities.RIGHT:
        x = x - strPixelWidth - BOX_PADDINGX;
        break;
      case SwingUtilities.LEFT:
        break;
    }
    x = Math.max(x, BOX_PADDINGX);
    x = Math.min(x, renderer.getWidth() - width - BOX_PADDINGX);

    // Box
    Composite oldComposite = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

    Rectangle boxBounds = new Rectangle(x, y, width, height);
    g.setColor(background);
    g.fillRect(boxBounds.x, boxBounds.y, boxBounds.width, boxBounds.height);
    AppStyle.border.paintWithin(g, boxBounds);
    g.setComposite(oldComposite);

    // Renderer message
    g.setColor(foreground);

    for (int i = 0; i < stringByLine.length; i++) {
      int textX = x + BOX_PADDINGX;
      int textY = y + BOX_PADDINGY + fm.getAscent() + strPixelHeight * i;
      g.drawString(stringByLine[i], textX, textY);
    }
    g.setFont(oldFont);
    return boxBounds;
  }

  public static Rectangle drawBoxedString(Graphics2D g, String string, int centerX, int centerY) {
    return drawBoxedString(g, string, centerX, centerY, SwingUtilities.CENTER);
  }

  public static Rectangle drawBoxedString(
      Graphics2D g, String string, int x, int y, int justification) {
    return drawBoxedString(g, string, x, y, justification, GREY_LABEL, Color.black);
  }

  public static Rectangle drawBoxedString(
      Graphics2D g,
      String string,
      int x,
      int y,
      int justification,
      ImageLabel background,
      Color foreground) {
    if (string == null) {
      string = "";
    }
    FontMetrics fm = g.getFontMetrics();
    int strWidth = SwingUtilities.computeStringWidth(fm, string);

    int width = strWidth + BOX_PADDINGX * 2;
    int height = fm.getHeight() + BOX_PADDINGY * 2;

    y = y - fm.getHeight() / 2 - BOX_PADDINGY;
    switch (justification) {
      case SwingUtilities.CENTER:
        x = x - strWidth / 2 - BOX_PADDINGX;
        break;
      case SwingUtilities.RIGHT:
        x = x - strWidth - BOX_PADDINGX;
        break;
      case SwingUtilities.LEFT:
        break;
    }
    // Box
    Rectangle boxBounds = new Rectangle(x, y, width, height);
    background.renderLabel(g, x, y, width, height);

    // Renderer message
    g.setColor(foreground);
    int textX = x + BOX_PADDINGX;
    int textY = y + BOX_PADDINGY + fm.getAscent();

    g.drawString(string, textX, textY);
    return boxBounds;
  }

  public static Point2D getProjectedPoint(Point2D origin, Point2D target, int distance) {
    double x1 = origin.getX();
    double x2 = target.getX();

    double y1 = origin.getY();
    double y2 = target.getY();

    double angle = Math.atan2(y2 - y1, x2 - x1);

    double newX = x1 + distance * Math.cos(angle);
    double newY = y1 + distance * Math.sin(angle);

    return new Point2D.Double(newX, newY);
  }

  /**
   * @param c the color to lighten up
   * @return a lighten color, as opposed to a brighter color as in Color.brighter(). This prevents
   *     light colors from getting bleached out.
   */
  public static Color lighten(Color c) {
    if (c == null) return null;
    else {
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();

      r += 64 * (255 - r) / 255;
      g += 64 * (255 - g) / 255;
      b += 64 * (255 - b) / 255;

      return new Color(r, g, b);
    }
  }

  public static Area createAreaBetween(Point a, Point b, int width) {
    // Find the angle that is perpendicular to the slope of the points
    double rise = b.y - a.y;
    double run = b.x - a.x;

    double theta1 = Math.atan2(rise, run) - Math.PI / 2;
    double theta2 = Math.atan2(rise, run) + Math.PI / 2;

    double ax1 = a.x + width * Math.cos(theta1);
    double ay1 = a.y + width * Math.sin(theta1);

    double ax2 = a.x + width * Math.cos(theta2);
    double ay2 = a.y + width * Math.sin(theta2);

    double bx1 = b.x + width * Math.cos(theta1);
    double by1 = b.y + width * Math.sin(theta1);

    double bx2 = b.x + width * Math.cos(theta2);
    double by2 = b.y + width * Math.sin(theta2);

    GeneralPath path = new GeneralPath();
    path.moveTo((float) ax1, (float) ay1);
    path.lineTo((float) ax2, (float) ay2);
    path.lineTo((float) bx2, (float) by2);
    path.lineTo((float) bx1, (float) by1);
    path.closePath();

    return new Area(path);
  }

  public static boolean intersects(Area lhs, Area rhs) {
    if (lhs == null || lhs.isEmpty() || rhs == null || rhs.isEmpty()) {
      return false;
    }
    if (!lhs.getBounds().intersects(rhs.getBounds())) {
      return false;
    }
    Area newArea = new Area(lhs);
    newArea.intersect(rhs);
    return !newArea.isEmpty();
  }

  /**
   * @param lhs the left hand side area
   * @param rhs the right hand side area
   * @return True if the lhs area totally contains the rhs area
   */
  public static boolean contains(Area lhs, Area rhs) {
    if (lhs == null || lhs.isEmpty() || rhs == null || rhs.isEmpty()) {
      return false;
    }
    if (!lhs.getBounds().intersects(rhs.getBounds())) {
      return false;
    }
    Area newArea = new Area(rhs);
    newArea.subtract(lhs);
    return newArea.isEmpty();
  }

  public static Area createLineSegmentEllipse(int x1, int y1, int x2, int y2, int steps) {
    return createLineSegmentEllipse(x1, y1, x2, (double) y2, steps);
  }

  public static Area createLineSegmentEllipse(
      double x1, double y1, double x2, double y2, int steps) {
    double x = Math.min(x1, x2);
    double y = Math.min(y1, y2);

    double w = Math.abs(x1 - x2);
    double h = Math.abs(y1 - y2);

    // Operate from the center of the ellipse
    x += w / 2;
    y += h / 2;

    // The Ellipse class uses curves, which doesn't work with the topology, so we have to create a
    // geometric ellipse
    // out of line segments
    GeneralPath path = new GeneralPath();

    double a = w / 2;
    double b = h / 2;

    boolean firstMove = true;
    for (double t = -Math.PI; t <= Math.PI; t += (2 * Math.PI / steps)) {
      int px = (int) Math.round(x + a * Math.cos(t));
      int py = (int) Math.round(y + b * Math.sin(t));

      if (firstMove) {
        path.moveTo(px, py);
        firstMove = false;
      } else {
        path.lineTo(px, py);
      }
    }

    path.closePath();
    return new Area(path);
  }

  public static void renderSoftClipping(Graphics2D g, Shape shape, int width, double initialAlpha) {
    // Our method actually uses double the width, let's update internally
    width *= 2;

    // Make a copy so that we don't have to revert our changes
    Graphics2D g2 = (Graphics2D) g.create();

    Area newClip = new Area(g.getClip());
    newClip.intersect(new Area(shape));

    g2.setClip(newClip);
    g2.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_OFF); // Faster without antialiasing, and looks just as good

    // float alpha = (float)initialAlpha / width / 6;
    float alpha = .04f;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    for (int i = 1; i < width; i += 2) {
      // if (alpha * i < .2) {
      // // Too faded to see anyway, don't waste cycles on it
      // continue;
      // }
      g2.setStroke(new BasicStroke(i));
      g2.draw(shape);
    }
    g2.dispose();
  }

  public static Area createLine(int width, Point2D... points) {
    if (points.length < 2) {
      throw new IllegalArgumentException("Must supply at least two points");
    }
    List<Point2D> bottomList = new ArrayList<Point2D>(points.length);
    List<Point2D> topList = new ArrayList<Point2D>(points.length);

    for (int i = 0; i < points.length; i++) {
      double angle =
          i < points.length - 1
              ? GeometryUtil.getAngle(points[i], points[i + 1])
              : GeometryUtil.getAngle(points[i - 1], points[i]);
      double lastAngle =
          i > 0
              ? GeometryUtil.getAngle(points[i], points[i - 1])
              : GeometryUtil.getAngle(points[i], points[i + 1]);

      double delta =
          i > 0 && i < points.length - 1
              ? Math.abs(GeometryUtil.getAngleDelta(angle, lastAngle))
              : 180; // creates a 90-deg angle

      double bottomAngle = (angle + delta / 2) % 360;
      double topAngle = bottomAngle + 180;
      // System.out.println(angle + " - " + delta + " - " + bottomAngle + " - " + topAngle);

      bottomList.add(getPointAtVector(points[i], bottomAngle, width));
      topList.add(getPointAtVector(points[i], topAngle, width));
    }
    // System.out.println(bottomList);
    // System.out.println(topList);
    Collections.reverse(topList);

    GeneralPath path = new GeneralPath();
    Point2D initialPoint = bottomList.remove(0);
    path.moveTo((float) initialPoint.getX(), (float) initialPoint.getY());

    for (Point2D point : bottomList) {
      path.lineTo((float) point.getX(), (float) point.getY());
    }
    for (Point2D point : topList) {
      path.lineTo((float) point.getX(), (float) point.getY());
    }
    path.closePath();
    return new Area(path);
  }

  private static Point2D getPointAtVector(Point2D point, double angle, double length) {
    double x = point.getX() + length * Math.cos(Math.toRadians(angle));
    double y = point.getY() - length * Math.sin(Math.toRadians(angle));

    // System.out.println(point + " - " + angle + " - " + x + "x" + y + " - " +
    // Math.cos(Math.toRadians(angle)) + " - " + Math.sin(Math.toRadians(angle)) + " - " +
    // Math.toRadians(angle));
    return new Point2D.Double(x, y);
  }

  public static void main(String[] args) {
    final Point2D[] points =
        new Point2D[] {
          new Point(20, 20), new Point(50, 50), new Point(80, 20), new Point(100, 100)
        };
    // final Point2D[] points = new Point2D[]{new Point(50, 50), new Point(20, 20), new Point(20,
    // 100), new Point(50,75)};
    final Area line = createLine(10, points);

    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setBounds(10, 10, 200, 200);

    JPanel p =
        new JPanel() {
          @Override
          protected void paintComponent(Graphics g) {
            Dimension size = getSize();
            g.setColor(Color.white);
            g.fillRect(0, 0, size.width, size.height);

            g.setColor(Color.gray);
            ((Graphics2D) g).fill(line);

            g.setColor(Color.red);
            for (Point2D p : points) {
              g.fillRect((int) (p.getX() - 1), (int) (p.getY() - 1), 2, 2);
            }
          }
        };
    f.add(p);
    f.setVisible(true);
    // System.out.println(area.equals(area2));
  }
}