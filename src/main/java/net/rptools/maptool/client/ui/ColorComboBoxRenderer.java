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
package net.rptools.maptool.client.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.language.I18N;

/** This class renders the list entries of a color selection combo box. */
public class ColorComboBoxRenderer extends JLabel implements ListCellRenderer {
  private static final long serialVersionUID = -8994115147056186827L;

  public static final String DEFAULT_COLOR_NAME = "Default";

  /** Color to use for rendering the default color foreground. */
  private final Color defaultForeground;

  /** Color to use for rendering the default color background. */
  private final Color defaultBackground;

  /** The name to use for the default color. */
  private final String defaultName;

  /**
   * Selects a black or white text color for the given background color. The function tries to
   * optimize readability by computing the gray value of the background color. Black is used only if
   * the color becomes lighten than 70% gray because white it is significantly more difficult to
   * read. The luma of the background color is calculated using the standard PAL/NTSC luma
   * algorithm.
   *
   * @param background color
   * @return returns the chosen background color, either black or white.
   */
  public static Color selectForegroundColor(Color background) {
    float[] rgbValues = background.getColorComponents(null);
    float contrast = 0.299f * rgbValues[0] + 0.587f * rgbValues[1] + 0.114f * rgbValues[2];
    if (contrast > 0.7) {
      return Color.black;
    } else {
      return Color.white;
    }
  }

  /** Creates a new label with an opaque background. */
  public ColorComboBoxRenderer(
      String defaultName, Color defaultForeground, Color defaultBackground) {
    super();
    setOpaque(true);
    this.defaultForeground = defaultForeground;
    this.defaultBackground = defaultBackground;
    this.defaultName = defaultName;
  }

  // @Override
  /** Renders the label as a color selection combo box entry. */
  public Component getListCellRendererComponent(
      JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    String name = (String) value;
    String colorPropertyKey = "Color.".concat(name);
    String colorName = I18N.getString(colorPropertyKey);
    if (colorName == null) {
      colorName = name;
    }

    Color fgColor;
    Color bgColor;

    if (defaultName.equals(name)) {
      if (isSelected & !cellHasFocus) {
        fgColor = defaultForeground;
        bgColor = defaultBackground;
      } else {
        fgColor = defaultBackground;
        bgColor = defaultForeground;
      }
    } else if (isSelected && !cellHasFocus) {
      fgColor = list.getSelectionForeground();
      bgColor = list.getSelectionBackground();
    } else {
      bgColor = MapToolUtil.getColor(name);
      fgColor = selectForegroundColor(bgColor);
    }
    setForeground(fgColor);
    setBackground(bgColor);

    setText(colorName);

    return this;
  }
}