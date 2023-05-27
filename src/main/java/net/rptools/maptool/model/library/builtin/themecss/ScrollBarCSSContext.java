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

import javax.swing.UIDefaults;

/**
 * Class that extracts and represents the scrollbar information passed to handlebars for building
 * the themed CSS.
 */
public class ScrollBarCSSContext {

  /** The background color of the scroll bar. */
  private final String backgroundColor;
  /** The foreground color of the scroll bar. */
  private final String foregroundColor;

  /** The thumb color of the scroll bar. */
  private final String thumbColor;

  /** The hover thumb color of the scroll bar. */
  private final String hoverThumbColor;

  /** The width of the scroll bar. */
  private final String width;

  /**
   * Creates a new instance of the scroll bar CSS context.
   *
   * @param uiDef the UI defaults to use to extract the scroll bar information.
   */
  public ScrollBarCSSContext(UIDefaults uiDef) {
    backgroundColor = ThemeCssContext.formatColor(uiDef.getColor("ScrollBar.background"));
    foregroundColor = ThemeCssContext.formatColor(uiDef.getColor("ScrollBar.foreground"));
    thumbColor = ThemeCssContext.formatColor(uiDef.getColor("ScrollBar.thumb"));
    hoverThumbColor = ThemeCssContext.formatColor(uiDef.getColor("ScrollBar.hoverThumbColor"));
    width = uiDef.getInt("ScrollBar.width") * 2 / 3 + "px";
  }

  /**
   * Gets the background color of the scroll bar.
   *
   * @return the background color of the scroll bar.
   */
  public String getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Gets the foreground color of the scroll bar.
   *
   * @return the foreground color of the scroll bar.
   */
  public String getForegroundColor() {
    return foregroundColor;
  }

  /**
   * Gets the thumb color of the scroll bar.
   *
   * @return the thumb color of the scroll bar.
   */
  public String getThumbColor() {
    return thumbColor;
  }

  /**
   * Gets the hover thumb color of the scroll bar.
   *
   * @return the hover thumb color of the scroll bar.
   */
  public String getHoverThumbColor() {
    return hoverThumbColor;
  }

  /**
   * Gets the width of the scroll bar.
   *
   * @return the width of the scroll bar.
   */
  public String getWidth() {
    return width;
  }
}
