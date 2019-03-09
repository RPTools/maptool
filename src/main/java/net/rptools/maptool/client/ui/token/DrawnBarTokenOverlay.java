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
package net.rptools.maptool.client.ui.token;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.rptools.maptool.model.Token;

/**
 * Draws a single color bar along one side of a token.
 *
 * @author Jay
 */
public class DrawnBarTokenOverlay extends BarTokenOverlay {

  /** The color of the bar. */
  private Color barColor;

  /** The thickness of the bar in pixels */
  private int thickness;

  /**
   * Build the bar with all of the details
   *
   * @param name Name of the overlay
   * @param aBarColor Color of the bar
   * @param aThickness Thickness of the bar
   */
  public DrawnBarTokenOverlay(String name, Color aBarColor, int aThickness) {
    super(name);
    barColor = aBarColor;
    thickness = aThickness;
  }

  /** Default constructor for serialization. */
  public DrawnBarTokenOverlay() {
    this(AbstractTokenOverlay.DEFAULT_STATE_NAME, Color.RED, 5);
  }

  /**
   * @see net.rptools.maptool.client.ui.token.BarTokenOverlay#paintOverlay(java.awt.Graphics2D,
   *     net.rptools.maptool.model.Token, java.awt.Rectangle, double)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds, double value) {
    int width = (getSide() == Side.TOP || getSide() == Side.BOTTOM) ? bounds.width : thickness;
    int height = (getSide() == Side.LEFT || getSide() == Side.RIGHT) ? bounds.height : thickness;
    int x = 0;
    int y = 0;
    switch (getSide()) {
      case RIGHT:
        x = bounds.width - width;
        break;
      case BOTTOM:
        y = bounds.height - height;
    } // endswitch

    if (getSide() == Side.TOP || getSide() == Side.BOTTOM) {
      width = calcBarSize(width, value);
    } else {
      height = calcBarSize(height, value);
      y += bounds.height - height;
    }
    Color tempColor = g.getColor();
    g.setColor(barColor);
    g.fillRect(x, y, width, height);
    g.setColor(tempColor);
  }

  /** @see net.rptools.maptool.client.ui.token.AbstractTokenOverlay#clone() */
  @Override
  public Object clone() {
    BarTokenOverlay overlay = new DrawnBarTokenOverlay(getName(), barColor, thickness);
    overlay.setOrder(getOrder());
    overlay.setGroup(getGroup());
    overlay.setMouseover(isMouseover());
    overlay.setOpacity(getOpacity());
    overlay.setIncrements(getIncrements());
    overlay.setSide(getSide());
    overlay.setShowGM(isShowGM());
    overlay.setShowOwner(isShowOwner());
    overlay.setShowOthers(isShowOthers());
    return overlay;
  }

  /** @return Getter for barColor */
  public Color getBarColor() {
    return barColor;
  }

  /** @param barColor Setter for barColor */
  public void setBarColor(Color barColor) {
    this.barColor = barColor;
  }

  /** @return Getter for thickness */
  public int getThickness() {
    return thickness;
  }

  /** @param thickness Setter for thickness */
  public void setThickness(int thickness) {
    this.thickness = thickness;
  }
}
