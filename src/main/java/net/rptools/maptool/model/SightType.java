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

import java.awt.geom.Area;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.server.proto.ShapeTypeDto;
import net.rptools.maptool.server.proto.SightTypeDto;

public final class SightType implements Serializable {
  private final @Nonnull String name;
  private final double multiplier;
  private final @Nullable LightSource personalLightSource;
  private final @Nonnull ShapeType shape;
  private final double width;
  private final int arc;
  private final float distance;
  private final int offset;
  private final boolean scaleWithToken;

  public SightType(
      @Nonnull String name,
      float distance,
      double multiplier,
      @Nonnull ShapeType shape,
      double width,
      int arc,
      int offset,
      boolean scaleWithToken,
      @Nullable LightSource personalLightSource) {
    this.name = name;
    this.distance = distance;
    this.multiplier = multiplier;
    this.personalLightSource = personalLightSource;
    this.shape = shape;
    this.width = width;
    this.arc = arc;
    this.offset = offset;
    this.scaleWithToken = scaleWithToken;
  }

  @Serial
  private Object readResolve() {
    return new SightType(
        name,
        distance,
        multiplier,
        Objects.requireNonNullElse(shape, ShapeType.CIRCLE),
        width,
        arc,
        offset,
        scaleWithToken,
        personalLightSource);
  }

  public double getWidth() {
    return this.width;
  }

  public int getOffset() {
    return this.offset;
  }

  public float getDistance() {
    return this.distance;
  }

  public @Nonnull ShapeType getShape() {
    return shape;
  }

  public boolean isScaleWithToken() {
    return scaleWithToken;
  }

  public @Nonnull String getName() {
    return name;
  }

  public double getMultiplier() {
    return multiplier;
  }

  public @Nullable LightSource getPersonalLightSource() {
    return personalLightSource;
  }

  public int getArc() {
    return arc;
  }

  /**
   * Get the shapedArea of a token's vision in a zone
   *
   * @param token the token.
   * @param zone the zone.
   * @return the Area of the vision shape.
   */
  public Area getVisionShape(Token token, Zone zone) {
    return zone.getGrid()
        .getShapedArea(
            getShape(),
            token,
            getDistance(),
            getWidth(),
            getArc(),
            getOffset(),
            isScaleWithToken());
  }

  public static SightType fromDto(SightTypeDto dto) {
    return new SightType(
        dto.getName(),
        dto.getDistance(),
        dto.getMultiplier(),
        ShapeType.valueOf(dto.getShape().name()),
        dto.getWidth(),
        dto.getArc(),
        dto.getOffset(),
        dto.getScaleWithToken(),
        dto.hasPersonalLightSource() ? LightSource.fromDto(dto.getPersonalLightSource()) : null);
  }

  public SightTypeDto toDto() {
    var dto = SightTypeDto.newBuilder();
    dto.setName(name);
    dto.setMultiplier(multiplier);
    if (personalLightSource != null) {
      dto.setPersonalLightSource(personalLightSource.toDto());
    }
    dto.setShape(ShapeTypeDto.valueOf(shape.name()));
    dto.setWidth(width);
    dto.setArc(arc);
    dto.setDistance(distance);
    dto.setOffset(offset);
    dto.setScaleWithToken(scaleWithToken);
    return dto.build();
  }
}
