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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.*;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.swing.ImageLabel;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;

/** */
public class GraphicsUtil {

  public static final int BOX_PADDINGX = 10;
  public static final int BOX_PADDINGY = 2;

  public static final ImageLabel GREY_LABEL =
      new ImageLabel(RessourceManager.getImage(Images.BOX_GRAY), 4, 4);
  public static final ImageLabel BLUE_LABEL =
      new ImageLabel(RessourceManager.getImage(Images.BOX_BLUE), 4, 4);
  public static final ImageLabel DARK_GREY_LABEL =
      new ImageLabel(RessourceManager.getImage(Images.BOX_DARK_GRAY), 4, 4);

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

  /**
   * For a given {@link Color}, determine whether black or white is best as a contrasting color and
   * return that color.
   *
   * @param c The color to contrast.
   * @return A black or white {@link Color}.
   * @see <a
   *     href="https://stackoverflow.com/questions/946544/good-text-foreground-color-for-a-given-background-color">https://stackoverflow.com/questions/946544/good-text-foreground-color-for-a-given-background-color</a>
   */
  public static Color contrast(Color c) {
    if (c == null) {
      return null;
    } else {
      int r = c.getRed();
      int g = c.getGreen();
      int b = c.getBlue();

      double brightness = (r * 0.299 + g * 0.587 + b * 0.114);
      return brightness > 186 ? new Color(0, 0, 0) : new Color(255, 255, 255);
    }
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

  public static Area createLineSegmentEllipse(
      double x1, double y1, double x2, double y2, int steps) {
    return new Area(createLineSegmentEllipsePath(x1, y1, x2, y2, steps));
  }

  public static Path2D createLineSegmentEllipsePath(
      double x1, double y1, double x2, double y2, int steps) {
    double x = Math.min(x1, x2);
    double y = Math.min(y1, y2);

    double w = Math.abs(x1 - x2);
    double h = Math.abs(y1 - y2);

    // Operate from the center of the ellipse
    x += w / 2;
    y += h / 2;

    // The Ellipse class uses curves, which doesn't work with the topology, so we have to create a
    // geometric ellipse out of line segments
    GeneralPath path = new GeneralPath();

    double a = w / 2;
    double b = h / 2;

    for (double t = -Math.PI; t <= Math.PI; t += (2 * Math.PI / steps)) {
      int px = (int) Math.round(x + a * Math.cos(t));
      int py = (int) Math.round(y + b * Math.sin(t));

      if (path.getCurrentPoint() == null) {
        path.moveTo(px, py);
      } else {
        path.lineTo(px, py);
      }
    }

    path.closePath();
    return path;
  }
}
