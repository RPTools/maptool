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
package net.rptools.maptool.model.library.builtin.themecss;

import java.awt.Color;
import java.util.function.Function;
import javax.swing.UIDefaults;

public class ProgressBarCSSContext {

  /** The progress bar arc. */
  private final int arc;

  /** The progress bar background color. */
  private final String backgroundColor;

  /** The progress bar font family. */
  private final String fontFamily;

  /** The progress bar font size. */
  private final String fontSize;

  /**
   * Creates a new instance of the theme CSS context.
   *
   * @param uiDef the UI defaults to use.
   * @param formatColor the function to use to format colors.
   */
  public ProgressBarCSSContext(UIDefaults uiDef, Function<Color, String> formatColor) {
    arc = uiDef.getInt("ProgressBar.arc");
    backgroundColor = formatColor.apply(uiDef.getColor("ProgressBar.background"));
    fontFamily = uiDef.getFont("ProgressBar.font").getFamily();
    fontSize = uiDef.getFont("ProgressBar.font").getSize() + "px";
  }

  /**
   * Gets the progress bar arc.
   *
   * @return The progress bar arc.
   */
  public int getArc() {
    return arc;
  }

  /** Gets the progress bar background color. */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /** Gets the progress bar font family. */
  public String getFontFamily() {
    return fontFamily;
  }

  /** Gets the progress bar font size. */
  public String getFontSize() {
    return fontSize;
  }
}
