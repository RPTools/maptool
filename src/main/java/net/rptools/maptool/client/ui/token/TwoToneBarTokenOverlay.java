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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.token;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.BarTokenOverlayDto;

/**
 * @author Jay
 */
public class TwoToneBarTokenOverlay extends BarTokenOverlay {
  /** The color of the bar. */
  private Color barColor;

  /** The thickness of the bar in pixels */
  private int thickness;

  /** Background color of the bar. */
  private Color bgColor;

  /**
   * Construct a complete bar
   *
   * @param name Name of the bar
   * @param aBarColor The color of the bar.
   * @param aBgColor The background color.
   * @param aThickness The thickness of the bar and background.
   */
  public TwoToneBarTokenOverlay(String name, Color aBarColor, Color aBgColor, int aThickness) {
    super(name);
    barColor = aBarColor;
    thickness = aThickness;
    bgColor = aBgColor;
  }

  /**
   * @return Getter for barColor
   */
  public Color getBarColor() {
    return barColor;
  }

  /**
   * @param barColor Setter for barColor
   */
  public void setBarColor(Color barColor) {
    this.barColor = barColor;
  }

  /**
   * @return Getter for thickness
   */
  public int getThickness() {
    return thickness;
  }

  /**
   * @param thickness Setter for thickness
   */
  public void setThickness(int thickness) {
    this.thickness = thickness;
  }

  /**
   * @return Getter for bgColor
   */
  public Color getBgColor() {
    return bgColor;
  }

  /**
   * @param bgColor Setter for bgColor
   */
  public void setBgColor(Color bgColor) {
    this.bgColor = bgColor;
  }

  @Override
  public void paintOverlay(
      Graphics2D g, Token token, Rectangle bounds, double value, ImageObserver observer) {
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
    Color tempColor = g.getColor();
    g.setColor(bgColor);
    g.fillRect(x, y, width, height);

    // Draw the bar
    int borderSize = thickness > 5 ? 2 : 1;
    x += borderSize;
    y += borderSize;
    width -= borderSize * 2;
    height -= borderSize * 2;
    if (getSide() == Side.TOP || getSide() == Side.BOTTOM) {
      width = calcBarSize(width, value);
    } else {
      height = calcBarSize(height, value);
      y += bounds.height - borderSize * 2 - height;
    }
    g.setColor(barColor);
    g.fillRect(x, y, width, height);
    g.setColor(tempColor);
  }

  @Override
  public Object clone() {
    BarTokenOverlay overlay = new TwoToneBarTokenOverlay(getName(), barColor, bgColor, thickness);
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

  protected static TwoToneBarTokenOverlay fromDtoImpl(BarTokenOverlayDto dto) {
    return new TwoToneBarTokenOverlay(
        DEFAULT_STATE_NAME,
        new Color(dto.getColor(), true),
        new Color(dto.getBgColor(), true),
        dto.getThickness());
  }

  @Override
  protected BarTokenOverlayDto.Builder toDtoImpl() {
    return super.toDtoImpl()
        .setType(BarTokenOverlayDto.BarTokenOverlayTypeDto.TWO_TONE)
        .setBgColor(bgColor.getRGB());
  }
}
