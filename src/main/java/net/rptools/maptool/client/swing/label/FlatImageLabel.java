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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import javax.swing.SwingUtilities;

/**
 * The FlatImageLabel class represents an image label with customizable properties such as padding,
 * colors, font, and justification. It can be used to create labels for images in various
 * containers.
 */
public class FlatImageLabel {

  /**
   * The Justification enum represents the different types of text justification. It can be used to
   * align text to the left, right, or center of a container.
   */
  public enum Justification {
    /** The text is aligned to the left of the container. */
    Left,
    /** The text is aligned to the right of the container. */
    Right,
    /** The text is aligned to the center of the container. */
    Center
  }

  /**
   * The padX variable represents the amount of padding to be added horizontally to the text in a
   * FlatImageLabel object.
   *
   * <p>This value is used in the getDimensions() and render() methods of the FlatImageLabel class
   * to calculate the width and positioning of the rendered text.
   *
   * <p>The padding is added on both sides of the text, resulting in a wider width of the label. It
   * ensures that the text is not rendered too close to the edges of the label, providing better
   * visual aesthetics.
   *
   * <p>The padX value should be a non-negative integer, representing the number of pixels of
   * padding. A higher value will result in greater horizontal spacing between the text and the
   * edges of the label.
   *
   * @see FlatImageLabel#getDimensions(Graphics2D, String)
   * @see FlatImageLabel#render(Graphics2D, int, int, String)
   */
  private final int padX;

  /**
   * The private final integer padY represents the vertical padding value for a FlatImageLabel. It
   * specifies the amount of empty space (in pixels) to be added above and below the text or image
   * within the label.
   *
   * <p>This value is set during the initialization of a FlatImageLabel object, using the padY
   * parameter of the constructor. Once set, the padY value cannot be changed.
   *
   * <p>The padY value should be a non-negative integer, representing the number of pixels of
   * padding. A higher value will result in greater vertical spacing between the text and the edges
   * of the label.
   *
   * @see FlatImageLabel#getDimensions(Graphics2D, String)
   * @see FlatImageLabel#render(Graphics2D, int, int, String)
   */
  private final int padY;

  /** The background variable represents the color used as the background of a FlatImageLabel. */
  private final Color background;

  /**
   * The foreground variable holds the color value for the foreground of a FlatImageLabel object.
   */
  private final Color foreground;

  /**
   * The private final variable 'font' represents the font used for rendering text in the
   * FlatImageLabel class.
   */
  private final Font font;

  /**
   * The Justification enum represents the different types of text justification. It can be used to
   * align text to the left, right, or center of a container.
   */
  private final Justification justification;

  /** The borderColor variable represents the color used as the border of a FlatImageLabel. */
  private final Color borderColor;

  /** The borderSize variable represents the size of the border for a FlatImageLabel. */
  private final int borderWidth;

  /** The borderArc variable represents the size of the border arc for a FlatImageLabel. */
  private final int borderArc;

  /**
   * The FlatImageLabel class represents an image label with customizable properties such as
   * padding, colors, font, and justification. It can be used to create labels for images in various
   * containers.
   *
   * @param padX the horizontal padding value for the label.
   * @param padY the vertical padding value for the label.
   * @param foreground the color value for the foreground of the label.
   * @param background the color value for the background of the label.
   * @param borderColor the color value for the border of the label.
   * @param font the font used for rendering text in the label.
   * @param justification the type of text justification used for the label.
   * @param borderWidth the size of the border for the label.
   */
  public FlatImageLabel(
      int padX,
      int padY,
      Color foreground,
      Color background,
      Color borderColor,
      Font font,
      Justification justification,
      int borderWidth,
      int borderArc) {
    this.padX = padX;
    this.padY = padY;
    this.foreground = foreground;
    this.background = background;
    this.font = font;
    this.justification = justification;
    this.borderColor = borderColor;
    this.borderWidth = borderWidth;
    this.borderArc = borderArc;
  }

  /**
   * Calculates the dimensions required to display the given string using the specified graphics
   * context.
   *
   * @param graphics2D the graphics context used for rendering
   * @param string the string to be displayed
   * @return the dimensions required to display the string with padding
   */
  public Dimension getDimensions(Graphics2D graphics2D, String string) {
    var g2d = (Graphics2D) graphics2D.create();
    g2d.setFont(font);
    var fm = g2d.getFontMetrics();
    int strWidth = SwingUtilities.computeStringWidth(fm, string);
    int strHeight = fm.getHeight();
    return new Dimension(
        strWidth + padX * 2 + borderWidth * 2, strHeight + padY * 2 + borderWidth * 2);
  }

  /**
   * Renders a string with customizable properties such as font, padding, colors, and justification
   * using the specified graphics context.
   *
   * @param graphics2D the graphics context used for rendering
   * @param x the x-coordinate of the top-left corner of the rendered string
   * @param y the y-coordinate of the top-left corner of the rendered string
   * @param string the string to be rendered
   * @return a Rectangle representing the dimensions and position of the rendered string with
   *     padding
   */
  public Rectangle render(Graphics2D graphics2D, int x, int y, String string) {
    var g2d = (Graphics2D) graphics2D.create();
    g2d.setFont(font);
    var fm = g2d.getFontMetrics();
    int strWidth = SwingUtilities.computeStringWidth(fm, string);
    int strHeight = fm.getAscent() - fm.getDescent() - fm.getLeading();

    var dim = getDimensions(g2d, string);
    int width = (int) dim.getWidth();
    int height = (int) dim.getHeight();

    var bounds = new Rectangle(x, y, width, height);

    int stringY = y + height / 2 + strHeight / 2;
    int stringX =
        switch (justification) {
          case Left -> x + padY;
          case Right -> width - strWidth - padX;
          case Center -> x + padX + (width - strWidth) / 2 - padX;
        };

    var labelRect = new RoundRectangle2D.Float(x, y, width - 1, height - 1, borderArc, borderArc);
    g2d.setBackground(background);
    g2d.setColor(background);
    g2d.fill(labelRect);
    g2d.setColor(foreground);
    g2d.drawString(string, stringX, stringY);
    if (borderWidth > 0) {
      g2d.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
      g2d.setColor(borderColor);
      g2d.draw(labelRect);
    }

    return bounds;
  }
}
