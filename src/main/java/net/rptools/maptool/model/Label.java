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
package net.rptools.maptool.model;

import java.awt.Color;
import net.rptools.maptool.server.proto.LabelDto;

/**
 * The Label class represents a label in a graphical user interface. Labels are used to display text
 * on the screen.
 *
 * <p>A label can have various properties such as the text content, position, visibility of the
 * background and border, and the colors and font size.
 *
 * <p>This class provides methods to set and retrieve these properties of a label object.
 * Additionally, it provides constructors to create label objects with different properties.
 */
public class Label {

  /** The default font size for labels. */
  public static final int DEFAULT_LABEL_FONT_SIZE = 12;

  /** The default background color for labels. */
  public static final Color DEFAULT_LABEL_BACKGROUND_COLOR = new Color(0.82f, 0.82f, 0.82f, 1.0f);

  /** The default foreground color for labels. */
  public static final Color DEFAULT_LABEL_FOREGROUND_COLOR = Color.BLACK;

  /** The default border color for labels. */
  public static final Color DEFAULT_LABEL_BORDER_COLOR = DEFAULT_LABEL_FOREGROUND_COLOR;

  /** The default border width for labels. */
  public static final int DEFAULT_LABEL_BORDER_WIDTH = 2;

  /** The default border arc for labels. */
  public static final int DEFAULT_LABEL_BORDER_ARC = 10;

  /**
   * The unique identifier for a Label object.
   *
   * <p>The {@link GUID} object is used to generate a globally unique identifier for each instance
   * of the Label class. The ID is assigned when a new Label object is created and cannot be
   * modified afterwards.
   *
   * <p>The ID is used to uniquely identify each Label object.
   *
   * @see GUID
   */
  private final GUID id;

  /** The text of the label. The text of the label is the text that is displayed on the map. */
  private String label;

  /** The x coordinate of the label is the x coordinate of the location of the label on the map. */
  private int x;

  /** The y coordinate of the label is the y coordinate of the location of the label on the map. */
  private int y;

  /** Whether the background of the label is shown. */
  private boolean showBackground;

  /** Whether the border of the label is shown. */
  private boolean showBorder;

  /** The border arc of the label. */
  private int borderArc = DEFAULT_LABEL_BORDER_ARC;

  /** The border color of the label. */
  private int borderColor = DEFAULT_LABEL_BORDER_COLOR.getRGB();

  /** The border width of the label. */
  private int borderWidth = DEFAULT_LABEL_BORDER_WIDTH;

  /** The foreground color of the label. */
  private int foregroundColor = DEFAULT_LABEL_FOREGROUND_COLOR.getRGB();

  /** The background color of the label. */
  private int backgroundColor = DEFAULT_LABEL_BACKGROUND_COLOR.getRGB();

  /** The font size of the label. */
  private int fontSize = DEFAULT_LABEL_FONT_SIZE;

  /**
   * Creates a new instance of the {@link Label} class.
   *
   * @param id the global unique identifier of the label
   * @param label the text content of the label
   * @param x the x-coordinate of the label's position
   * @param y the y-coordinate of the label's position
   * @param showBackground indicates whether the label should show the background
   * @param foregroundColor the color of the label's foreground
   * @param backgroundColor the color of the label's background
   * @param showBorder indicates whether the label should show the border
   * @param borderArc the arc of the label's border
   * @param fontSize the font size of the label
   */
  private Label(
      GUID id,
      String label,
      int x,
      int y,
      boolean showBackground,
      int foregroundColor,
      int backgroundColor,
      int borderColor,
      boolean showBorder,
      int borderWidth,
      int borderArc,
      int fontSize) {
    this.id = id;
    this.label = label;
    this.x = x;
    this.y = y;
    this.showBackground = showBackground;
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.borderColor = borderColor;
    this.showBorder = showBorder;
    this.borderWidth = borderWidth;
    this.borderArc = borderArc;
    this.fontSize = fontSize;
  }

  /** Creates a new instance of the Label class with an empty string as the label text. */
  public Label() {
    this("");
  }

  /**
   * Creates a new instance of the Label class with the specified label text.
   *
   * @param label the text content of the label
   */
  public Label(String label) {
    this(label, 0, 0);
  }

  /**
   * Creates a new instance of the Label class with the specified label text, x-coordinate, and
   * y-coordinate.
   *
   * @param label the text content of the label
   * @param x the x-coordinate of the label's position
   * @param y the y-coordinate of the label's position
   */
  public Label(String label, int x, int y) {
    id = new GUID();
    this.label = label;
    this.x = x;
    this.y = y;
    showBackground = true;
    showBorder = true;
  }

  /**
   * Creates a new instance of the Label class by copying the properties from another Label object.
   *
   * @param label The Label object to copy from.
   */
  public Label(Label label) {
    this(
        label.id,
        label.label,
        label.x,
        label.y,
        label.showBackground,
        label.foregroundColor,
        label.backgroundColor,
        label.borderColor,
        label.showBorder,
        label.borderWidth,
        label.borderArc,
        label.fontSize);
  }

  /**
   * Retrieves the label text of the Label object.
   *
   * @return the label text
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label text of the Label object.
   *
   * @param label the text content of the label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Retrieves the global unique identifier (GUID) of the Label object.
   *
   * @return the GUID of the Label object
   */
  public GUID getId() {
    return id;
  }

  /**
   * Retrieves the x-coordinate of the Label object's position.
   *
   * @return the x-coordinate
   */
  public int getX() {
    return x;
  }

  /**
   * Sets the x-coordinate of the Label object's position.
   *
   * @param x the x-coordinate to set
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Retrieves the y-coordinate of the Label object's position.
   *
   * @return the y-coordinate
   */
  public int getY() {
    return y;
  }

  /**
   * Sets the y-coordinate of the Label object's position.
   *
   * @param y the y-coordinate to set
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Checks whether the background of the label should be shown.
   *
   * @return true if the background should be shown, false otherwise
   */
  public boolean isShowBackground() {
    return showBackground;
  }

  /**
   * Sets whether the background of the label should be shown.
   *
   * @param showBackground indicates whether the background should be shown
   */
  public void setShowBackground(boolean showBackground) {
    this.showBackground = showBackground;
  }

  /**
   * Retrieves the foreground color of the Label object.
   *
   * @return the foreground color
   */
  public Color getForegroundColor() {
    return new Color(foregroundColor, true);
  }

  /**
   * Retrieves the background color of the Label object.
   *
   * @return the background color
   */
  public Color getBackgroundColor() {
    return new Color(backgroundColor, true);
  }

  /**
   * Retrieves the font size of the Label object.
   *
   * @return the font size
   */
  public int getFontSize() {
    return fontSize;
  }

  /**
   * Retrieves the foreground color value of the Label object.
   *
   * @return the foreground color value
   */
  public int getForegroundColorValue() {
    return foregroundColor;
  }

  /**
   * Retrieves the background color value of the Label object.
   *
   * @return the background color value
   */
  public int getBackgroundColorValue() {
    return backgroundColor;
  }

  /**
   * Sets the foreground color of the Label object.
   *
   * @param foregroundColor the foreground color to set
   */
  public void setForegroundColor(Color foregroundColor) {
    this.foregroundColor = foregroundColor.getRGB();
  }

  /**
   * Sets the background color of the Label object.
   *
   * @param backgroundColor the color to set as the background color
   */
  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor.getRGB();
  }

  /**
   * Sets the font size of the Label object.
   *
   * @param fontSize the font size to set
   */
  public void setFontSize(int fontSize) {
    this.fontSize = fontSize;
  }

  /**
   * Retrieves the border color of the Label object.
   *
   * @return the border color
   */
  public boolean isShowBorder() {
    return showBorder;
  }

  /**
   * Sets whether the border of the Label object should be shown.
   *
   * @param showBorder indicates whether the border should be shown
   */
  public void setShowBorder(boolean showBorder) {
    this.showBorder = showBorder;
  }

  /**
   * Retrieves the border color of the Label object.
   *
   * @return the border color
   */
  public Color getBorderColor() {
    return new Color(borderColor, true);
  }

  /**
   * Sets the border color of the Label object.
   *
   * @param borderColor the color to set as the border color
   */
  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor.getRGB();
  }

  /**
   * Retrieves the width of the border of the Label object.
   *
   * @return the width of the border
   */
  public int getBorderWidth() {
    return borderWidth;
  }

  /**
   * Sets the width of the border for the Label object.
   *
   * @param borderWidth the width of the border to set
   */
  public void setBorderWidth(int borderWidth) {
    this.borderWidth = borderWidth;
  }

  /**
   * Retrieves the arc of the border of the Label object.
   *
   * @return the arc of the border
   */
  public int getBorderArc() {
    return borderArc;
  }

  /**
   * Sets the arc of the border for the Label object.
   *
   * @param borderArc the arc of the border to set
   */
  public void setBorderArc(int borderArc) {
    this.borderArc = borderArc;
  }

  /**
   * Creates a new instance of the {@link Label} class based on the provided LabelDto object.
   *
   * @param dto the LabelDto object used to create the Label
   * @return a new instance of the Label class
   */
  public static Label fromDto(LabelDto dto) {
    return new Label(
        GUID.valueOf(dto.getId()),
        dto.getLabel(),
        dto.getX(),
        dto.getY(),
        dto.getShowBackground(),
        dto.getForegroundColor(),
        dto.getBackgroundColor(),
        dto.getBorderColor(),
        dto.getShowBorder(),
        dto.getBorderWidth(),
        dto.getBorderArc(),
        dto.getFontSize());
  }

  /**
   * Converts the {@link Label} object to a {@link LabelDto} object.
   *
   * @return a new instance of the {@link LabelDto} class with the properties of the {@link Label}
   *     object.
   */
  public LabelDto toDto() {
    return LabelDto.newBuilder()
        .setId(id.toString())
        .setLabel(label)
        .setX(x)
        .setY(y)
        .setShowBackground(showBackground)
        .setForegroundColor(foregroundColor)
        .setBackgroundColor(backgroundColor)
        .setFontSize(fontSize)
        .setBorderColor(borderColor)
        .setShowBorder(showBorder)
        .setBorderWidth(borderWidth)
        .setBorderArc(borderArc)
        .build();
  }
}
