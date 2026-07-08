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

import java.awt.*;
import java.util.function.Function;
import javax.swing.*;

public class MeterProgressBarCssContext {

  /** The progress bar background color. */
  private final String backgroundColor;

  /** The progress bar foreground color. */
  private final String foregroundColor;

  /**
   * Creates a new instance of the meter progress bar CSS context.
   *
   * @param uiDef The UI defaults to use.
   * @param getColorOrBlank The function to use to convert the color key into a string color format.
   */
  public MeterProgressBarCssContext(UIDefaults uiDef, Function<String, String> getColorOrBlank) {
    backgroundColor = getColorOrBlank.apply("MeterProgressBar.background");
    foregroundColor = getColorOrBlank.apply("MeterProgressBar.foreground");
  }

  /**
   * Gets the meter progress bar background color.
   *
   * @return The meter progress bar background color.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Gets the meter progress bar foreground color.
   *
   * @return The meter progress bar foreground color.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }
}
