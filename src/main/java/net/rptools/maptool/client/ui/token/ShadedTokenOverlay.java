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
import java.util.Objects;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.ShadedTokenOverlayDto;

/**
 * Paints a single reduced alpha color over the token.
 *
 * @author jgorrell
 */
public final class ShadedTokenOverlay extends BooleanTokenOverlay {
  /** The color that is painted over the token. */
  private Color color;

  /**
   * Create the new token overlay
   *
   * @param name Name of the new overlay.
   * @param color The color that is painted over the token. If the alpha is 100%, it will be reduced
   *     to 25%.
   */
  public ShadedTokenOverlay(String name, Color color) {
    super(name);
    this.color = Objects.requireNonNullElse(color, Color.red);
    setOpacity(25);
  }

  public ShadedTokenOverlay(ShadedTokenOverlay other) {
    super(other);
    this.color = other.color;
  }

  @Override
  public ShadedTokenOverlay clone() {
    return new ShadedTokenOverlay(this);
  }

  /**
   * Get the color for this ShadedTokenOverlay.
   *
   * @return Returns the current value of color.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Set the value of color for this ShadedTokenOverlay.
   *
   * @param aColor The color to set.
   */
  public void setColor(Color aColor) {
    color = aColor;
  }

  @Override
  public void paintOverlay(Graphics2D g, Token aToken, Rectangle bounds) {
    g.setColor(color);
    g.fill(bounds);
  }

  public ShadedTokenOverlayDto toShadedDto() {
    var dto = ShadedTokenOverlayDto.newBuilder();
    dto.setColor(color.getRGB());
    return dto.build();
  }

  public static ShadedTokenOverlay fromDto(ShadedTokenOverlayDto dto) {
    var color = new Color(dto.getColor(), true);
    return new ShadedTokenOverlay(DEFAULT_STATE_NAME, color);
  }
}
