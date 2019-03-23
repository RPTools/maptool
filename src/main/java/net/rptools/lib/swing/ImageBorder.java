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
package net.rptools.lib.swing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.rptools.lib.image.ImageUtil;

/** @author trevor */
public class ImageBorder {
  public static final ImageBorder GRAY = new ImageBorder("net/rptools/lib/swing/image/border/gray");
  public static final ImageBorder RED = new ImageBorder("net/rptools/lib/swing/image/border/red");
  public static final ImageBorder BLUE = new ImageBorder("net/rptools/lib/swing/image/border/blue");

  private BufferedImage topRight;
  private BufferedImage top;
  private BufferedImage topLeft;
  private BufferedImage left;
  private BufferedImage bottomLeft;
  private BufferedImage bottom;
  private BufferedImage bottomRight;
  private BufferedImage right;

  private int topMargin;
  private int bottomMargin;
  private int leftMargin;
  private int rightMargin;

  public ImageBorder(String imagePath) {
    try {
      topRight = ImageUtil.getCompatibleImage(imagePath + "/tr.png");
      top = ImageUtil.getCompatibleImage(imagePath + "/top.png");
      topLeft = ImageUtil.getCompatibleImage(imagePath + "/tl.png");
      left = ImageUtil.getCompatibleImage(imagePath + "/left.png");
      bottomLeft = ImageUtil.getCompatibleImage(imagePath + "/bl.png");
      bottom = ImageUtil.getCompatibleImage(imagePath + "/bottom.png");
      bottomRight = ImageUtil.getCompatibleImage(imagePath + "/br.png");
      right = ImageUtil.getCompatibleImage(imagePath + "/right.png");

      topMargin = max(topRight.getHeight(), top.getHeight(), topLeft.getHeight());
      bottomMargin = max(bottomRight.getHeight(), bottom.getHeight(), bottomLeft.getHeight());
      rightMargin = max(topRight.getWidth(), right.getWidth(), bottomRight.getWidth());
      leftMargin = max(topLeft.getWidth(), left.getWidth(), bottomLeft.getWidth());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public int getTopMargin() {
    return topMargin;
  }

  public int getBottomMargin() {
    return bottomMargin;
  }

  public int getRightMargin() {
    return rightMargin;
  }

  public int getLeftMargin() {
    return leftMargin;
  }

  public void paintAround(Graphics2D g, Rectangle rect) {
    paintAround(g, rect.x, rect.y, rect.width, rect.height);
  }

  public void paintAround(Graphics2D g, int x, int y, int width, int height) {
    paintWithin(
        g,
        x - leftMargin,
        y - topMargin,
        width + leftMargin + rightMargin,
        height + topMargin + bottomMargin);
  }

  public void paintWithin(Graphics2D g, Rectangle rect) {
    paintWithin(g, rect.x, rect.y, rect.width, rect.height);
  }

  public void paintWithin(Graphics2D g, int x, int y, int width, int height) {
    Rectangle2D r = g.getClipBounds().createIntersection(new Rectangle(x, y, width, height));
    if (r.isEmpty()) return;
    Shape oldClip = g.getClip();

    // Draw Corners
    g.drawImage(
        topLeft, x + leftMargin - topLeft.getWidth(), y + topMargin - topLeft.getHeight(), null);
    g.drawImage(topRight, x + width - rightMargin, y + topMargin - topRight.getHeight(), null);
    g.drawImage(
        bottomLeft, x + leftMargin - bottomLeft.getWidth(), y + height - bottomMargin, null);
    g.drawImage(bottomRight, x + width - rightMargin, y + height - bottomMargin, null);

    // Draw top

    int i;
    int max = width - rightMargin;

    // Hopefully the compiler is doing subexpression optimization! ;-)
    Rectangle topEdge, botEdge, lftEdge, rgtEdge;
    topEdge =
        new Rectangle(
            x + leftMargin,
            y + topMargin - top.getHeight(),
            width - leftMargin - rightMargin,
            top.getHeight());
    botEdge =
        new Rectangle(
            x + leftMargin,
            y + height - bottomMargin,
            width - leftMargin - rightMargin,
            top.getHeight());
    lftEdge =
        new Rectangle(
            x + leftMargin - left.getWidth(),
            y + topMargin,
            left.getWidth(),
            height - topMargin - bottomMargin);
    rgtEdge =
        new Rectangle(
            x + width - rightMargin,
            y + topMargin,
            right.getWidth(),
            height - topMargin - bottomMargin);

    Rectangle.intersect(topEdge, r, topEdge);
    Rectangle.intersect(botEdge, r, botEdge);
    Rectangle.intersect(lftEdge, r, lftEdge);
    Rectangle.intersect(rgtEdge, r, rgtEdge);

    // Top
    if (!topEdge.isEmpty()) {
      g.setClip(topEdge);
      for (i = leftMargin; i < max; i += top.getWidth()) {
        g.drawImage(top, x + i, y + topMargin - top.getHeight(), null);
      }
    }

    // Bottom
    if (!botEdge.isEmpty()) {
      g.setClip(botEdge);
      for (i = leftMargin; i < max; i += bottom.getWidth()) {
        g.drawImage(bottom, x + i, y + height - bottomMargin, null);
      }
    }

    // Left
    if (!lftEdge.isEmpty()) {
      g.setClip(lftEdge);
      max = height - bottomMargin;
      for (i = topMargin; i < max; i += left.getHeight()) {
        g.drawImage(left, x + leftMargin - left.getWidth(), y + i, null);
      }
    }

    // Right
    if (!rgtEdge.isEmpty()) {
      g.setClip(rgtEdge);
      for (i = topMargin; i < max; i += right.getHeight()) {
        g.drawImage(right, x + width - rightMargin, y + i, null);
      }
    }
    g.setClip(oldClip);
  }

  private int max(int i1, int i2, int i3) {
    int bigger = i1 > i2 ? i1 : i2;
    return bigger > i3 ? bigger : i3;
  }
}
