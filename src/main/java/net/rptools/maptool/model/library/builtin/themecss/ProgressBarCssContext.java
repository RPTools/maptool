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

import java.util.function.Function;
import javax.swing.UIDefaults;

public class ProgressBarCssContext {

  /** The progress bar arc. */
  private final int arc;

  /** The progress bar background color. */
  private final String backgroundColor;

  /** The progress bar foreground color. */
  private final String foregroundColor;

  /** The progress bar font family. */
  private final String fontFamily;

  /** The progress bar font size. */
  private final String fontSize;

  /**
   * Creates a new instance of the progress bar CSS context.
   *
   * @param uiDef The UI defaults to use.
   * @param getColorOrBlank The function to use to convert the color key into a string color format
   */
  public ProgressBarCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {
    arc = uiDef.getInt("ProgressBar.arc");
    backgroundColor = getColorOrBlank.apply("ProgressBar.background");
    foregroundColor = getColorOrBlank.apply("ProgressBar.foreground");
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

  /**
   * Gets the progress bar background color.
   *
   * @return The progress bar background color.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Gets the progress bar foreground color.
   *
   * @return The progress bar foreground color.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Gets the progress bar font family.
   *
   * @return The progress bar font family.
   */
  public String getFontFamily() {
    return fontFamily;
  }

  /**
   * Gets the progress bar font size.
   *
   * @return The progress bar font size.
   */
  public String getFontSize() {
    return fontSize;
  }
}
