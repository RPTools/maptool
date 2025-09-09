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

/**
 * A context for the scroll bar CSS. This is used to extract and provide the scroll bar CSS values.
 */
public class ScrollBarCSSContext {
  /** The progress bar foreground color. */
  private final String backgroundColor;

  /** The scroll bar border color. */
  private final int showButtons;

  /** The scroll bar border color. */
  private final String thumbHighlightColor;

  /** The scroll bar border color. */
  private final int thumbArc;

  /** The scroll bar border color. */
  private final String thumbBorderColor;

  /** The scroll bar border color. */
  private final String thumbShadowColor;

  /** The scroll bar border color. */
  private final String thumbDarkShadowColor;

  /** The scroll bar border color. */
  private final String thumbColor;

  /** The scroll bar border color. */
  private final int thumbInsetsLeft;

  /** The scroll bar border color. */
  private final int thumbInsetsTop;

  /** The scroll bar border color. */
  private final int thumbInsetsRight;

  /** The scroll bar border color. */
  private final int thumbInsetsBottom;

  /**
   * Creates a new instance of the theme CSS context.
   *
   * @param uiDef the UI defaults to use.
   */
  public ScrollBarCSSContext(UIDefaults uiDef, Function<Color, String> formatColor) {
    backgroundColor = formatColor.apply(uiDef.getColor("ScrollBar.background"));
    showButtons = uiDef.getBoolean("ScrollBar.showButtons") ? 1 : 0;
    thumbHighlightColor = formatColor.apply(uiDef.getColor("ScrollBar.thumbHighlight"));
    thumbArc = uiDef.getInt("ScrollBar.thumbArc");
    thumbShadowColor = formatColor.apply(uiDef.getColor("ScrollBar.thumbShadow"));
    var dscol = uiDef.getColor("ScrollBar.thumbDarkShadow");
    thumbDarkShadowColor = dscol != null ? formatColor.apply(dscol) : thumbShadowColor;
    var tcol = uiDef.getColor("ScrollBar.thumbBorderColor");
    thumbBorderColor = tcol != null ? formatColor.apply(tcol) : thumbShadowColor;
    thumbColor = formatColor.apply(uiDef.getColor("ScrollBar.thumb"));
    thumbInsetsLeft = uiDef.getInt("ScrollBar.thumbInsets.left");
    thumbInsetsTop = uiDef.getInt("ScrollBar.thumbInsets.top");
    thumbInsetsRight = uiDef.getInt("ScrollBar.thumbInsets.right");
    thumbInsetsBottom = uiDef.getInt("ScrollBar.thumbInsets.bottom");
  }

  /** Gets the scroll background color. */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /** Gets the scroll bar show buttons. */
  public int getShowButtons() {
    return showButtons;
  }

  /** Gets the scroll bar thumb highlight color. */
  public String getThumbHighlightColor() {
    return thumbHighlightColor;
  }

  /** Gets the scroll bar thumb arc. */
  public int getThumbArc() {
    return thumbArc;
  }

  /** Gets the scroll bar thumb border color. */
  public String getThumbBorderColor() {
    return thumbBorderColor;
  }

  /** Gets the scroll bar thumb shadow color. */
  public String getThumbShadowColor() {
    return thumbShadowColor;
  }

  /** Gets the scroll bar thumb dark shadow color. */
  public String getThumbDarkShadowColor() {
    return thumbDarkShadowColor;
  }

  /** Gets the scroll bar thumb color. */
  public String getThumbColor() {
    return thumbColor;
  }

  /** Gets the scroll bar thumb insets left. */
  public int getThumbInsetsLeft() {
    return thumbInsetsLeft;
  }

  /** Gets the scroll bar thumb insets top. */
  public int getThumbInsetsTop() {
    return thumbInsetsTop;
  }

  /** Gets the scroll bar thumb insets right. */
  public int getThumbInsetsRight() {
    return thumbInsetsRight;
  }

  /** Gets the scroll bar thumb insets bottom. */
  public int getThumbInsetsBottom() {
    return thumbInsetsBottom;
  }
}
