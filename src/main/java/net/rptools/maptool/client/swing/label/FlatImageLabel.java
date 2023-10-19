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
package net.rptools.maptool.client.swing.label;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.swing.SwingUtilities;

public class FlatImageLabel {

  public enum Justification {
    Left,
    Right,
    Center
  }

  private final int padX;
  private final int padY;
  private final Color background;
  private final Color foreground;
  private final Font font;

  private final Justification justification;

  public FlatImageLabel(
      int padX,
      int padY,
      Color foreground,
      Color background,
      Font font,
      Justification justification) {
    this.padX = padX;
    this.padY = padY;
    this.foreground = foreground;
    this.background = background;
    this.font = font;
    this.justification = justification;
  }

  public Dimension getDimensions(Graphics2D graphics2D, String string) {
    var g2d = (Graphics2D) graphics2D.create();
    g2d.setFont(font);
    var fm = g2d.getFontMetrics();
    int strWidth = SwingUtilities.computeStringWidth(fm, string);
    int strHeight = fm.getHeight();
    return new Dimension(strWidth + padX * 2, strHeight + padY * 2);
  }

  public Rectangle render(Graphics2D graphics2D, int x, int y, String string) {
    var g2d = (Graphics2D) graphics2D.create();
    g2d.setFont(font);
    var fm = g2d.getFontMetrics();
    int strWidth = SwingUtilities.computeStringWidth(fm, string);
    int strHeight = fm.getHeight();

    int width = strWidth + padX * 2;
    int height = strHeight + padY * 2;

    var bounds = new Rectangle(x, y, width, height);

    y = y + strHeight;
    x =
        switch (justification) {
          case Left -> x + padY;
          case Right -> width - strWidth - padX;
          case Center -> x + padX + (width - strWidth) / 2 - padX;
        };

    g2d.setBackground(background);
    g2d.setColor(background);
    g2d.fill(bounds);
    g2d.setColor(foreground);
    g2d.setBackground(foreground);
    g2d.drawString(string, x, y);

    return bounds;
  }
}
