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
import javax.annotation.Nullable;
import net.rptools.maptool.server.proto.ShapeTypeDto;
import net.rptools.maptool.server.proto.SightTypeDto;

public class SightType {
  private String name;
  private double multiplier;
  private LightSource personalLightSource;
  private ShapeType shape;
  private double width = 0;
  private int arc = 0;
  private float distance = 0;
  private int offset = 0;
  private boolean scaleWithToken = false;

  public double getWidth() {
    return this.width;
  }

  public void setWidth(double width) {
    this.width = width;
  }

  public int getOffset() {
    return this.offset;
  }

  public void setOffset(int offset2) {
    this.offset = offset2;
  }

  public float getDistance() {
    return this.distance;
  }

  public void setDistance(float range) {
    this.distance = range;
  }

  public ShapeType getShape() {
    return shape != null ? shape : ShapeType.CIRCLE;
  }

  public void setShape(ShapeType shape) {
    this.shape = shape;
  }

  public void setScaleWithToken(boolean scaleWithToken) {
    this.scaleWithToken = scaleWithToken;
  }

  public boolean isScaleWithToken() {
    return scaleWithToken;
  }

  public SightType() {
    // For serialization
  }

  public SightType(String name, double multiplier, @Nullable LightSource personalLightSource) {
    this(name, multiplier, personalLightSource, ShapeType.CIRCLE);
  }

  public SightType(
      String name, double multiplier, @Nullable LightSource personalLightSource, ShapeType shape) {
    this.name = name;
    this.multiplier = multiplier;
    this.personalLightSource = personalLightSource;
    this.shape = shape;
  }

  public SightType(
      String name,
      double multiplier,
      LightSource personalLightSource,
      ShapeType shape,
      double width,
      int arc,
      boolean scaleWithToken) {
    this.name = name;
    this.multiplier = multiplier;
    this.personalLightSource = personalLightSource;
    this.shape = shape;
    this.width = width;
    this.arc = arc;
    this.scaleWithToken = scaleWithToken;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getMultiplier() {
    return multiplier;
  }

  public void setMultiplier(double multiplier) {
    this.multiplier = multiplier;
  }

  public boolean hasPersonalLightSource() {
    return personalLightSource != null;
  }

  public LightSource getPersonalLightSource() {
    return personalLightSource;
  }

  public void setPersonalLightSource(LightSource personalLightSource) {
    this.personalLightSource = personalLightSource;
  }

  public void setArc(int arc) {
    this.arc = arc;
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
            getShape(), token, getDistance(), getWidth(), getArc(), getOffset(), scaleWithToken);
  }

  public static SightType fromDto(SightTypeDto dto) {
    var sightType = new SightType();
    sightType.name = dto.getName();
    sightType.multiplier = dto.getMultiplier();
    sightType.personalLightSource =
        dto.hasPersonalLightSource() ? LightSource.fromDto(dto.getPersonalLightSource()) : null;
    sightType.shape = ShapeType.valueOf(dto.getShape().name());
    sightType.width = dto.getWidth();
    sightType.arc = dto.getArc();
    sightType.distance = dto.getDistance();
    sightType.offset = dto.getOffset();
    sightType.scaleWithToken = dto.getScaleWithToken();
    return sightType;
  }

  public SightTypeDto toDto() {
    var dto = SightTypeDto.newBuilder();
    dto.setName(name);
    dto.setMultiplier(multiplier);
    if (personalLightSource != null) dto.setPersonalLightSource(personalLightSource.toDto());
    if (shape == null) shape = ShapeType.CIRCLE;
    dto.setShape(ShapeTypeDto.valueOf(shape.name()));
    dto.setWidth(width);
    dto.setArc(arc);
    dto.setDistance(distance);
    dto.setOffset(offset);
    dto.setScaleWithToken(scaleWithToken);
    return dto.build();
  }
}
