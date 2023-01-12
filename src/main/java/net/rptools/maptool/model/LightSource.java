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

import com.google.protobuf.StringValue;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.server.proto.LightSourceDto;
import net.rptools.maptool.server.proto.ShapeTypeDto;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

public class LightSource implements Comparable<LightSource> {
  public enum Type {
    NORMAL,
    AURA
  }

  private List<Light> lightList;
  private String name;
  private GUID id;
  private Type type = Type.NORMAL;
  private ShapeType shapeType = ShapeType.CIRCLE;
  private int lumens = 0;
  private boolean scaleWithToken = false;

  public LightSource() {
    // for serialization
  }

  public LightSource(String name) {
    id = new GUID();
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LightSource)) {
      return false;
    }
    return ((LightSource) obj).id.equals(id);
  }

  public double getMaxRange() {
    double range = 0;
    for (Light light : getLightList()) {
      range = Math.max(range, light.getRadius());
    }
    return range;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  public void setId(GUID id) {
    this.id = id;
  }

  public GUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void add(Light source) {
    getLightList().add(source);
  }

  public void remove(Light source) {
    getLightList().remove(source);
  }

  /** @return the lightList of the LightSource */
  public List<Light> getLightList() {
    if (lightList == null) {
      lightList = new LinkedList<Light>();
    }
    return lightList;
  }

  public Type getType() {
    return type != null ? type : Type.NORMAL;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public void setShapeType(ShapeType type) {
    this.shapeType = type;
  }

  public ShapeType getShapeType() {
    return shapeType != null ? shapeType : ShapeType.CIRCLE;
  }

  public void setLumens(int lumens) {
    this.lumens = lumens;
  }

  public int getLumens() {
    return lumens;
  }

  public void setScaleWithToken(boolean scaleWithToken) {
    this.scaleWithToken = scaleWithToken;
  }

  public boolean isScaleWithToken() {
    return scaleWithToken;
  }

  /*
   * Area for a single light, subtracting any previous lights
   */
  public Area getArea(Token token, Zone zone, Light light) {
    Area area = light.getArea(token, zone, scaleWithToken);
    // TODO: This seems horribly inefficient
    // Subtract out the lights that are previously defined
    for (int i = getLightList().indexOf(light) - 1; i >= 0; i--) {
      Light lessLight = getLightList().get(i);
      area.subtract(lessLight.getArea(token, zone, scaleWithToken));
    }
    return area;
  }

  /* Area for all lights combined */
  public Area getArea(Token token, Zone zone) {
    Area area = new Area();

    for (Light light : getLightList()) {
      area.add(light.getArea(token, zone, isScaleWithToken()));
    }

    return area;
  }

  @SuppressWarnings("unchecked")
  public static Map<String, List<LightSource>> getDefaultLightSources() throws IOException {
    Object defaultLights =
        FileUtil.objFromResource("net/rptools/maptool/model/defaultLightSourcesMap.xml");
    return (Map<String, List<LightSource>>) defaultLights;
  }

  @Override
  public String toString() {
    return name;
  }

  private Object readResolve() {
    if (type == null) {
      type = Type.NORMAL;
    }
    if (lightList == null) {
      lightList = new LinkedList<>();
    }
    return this;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(@NotNull LightSource o) {
    if (o != this) {
      int nameLong = NumberUtils.toInt(name, Integer.MIN_VALUE);
      int onameLong = NumberUtils.toInt(o.name, Integer.MIN_VALUE);
      if (nameLong != Integer.MIN_VALUE && onameLong != Integer.MIN_VALUE)
        return nameLong - onameLong;
      return name.compareTo(o.name);
    }
    return 0;
  }

  public static LightSource fromDto(LightSourceDto dto) {
    var lightSource = new LightSource();
    lightSource.lightList =
        dto.getLightsList().stream().map(l -> Light.fromDto(l)).collect(Collectors.toList());
    lightSource.name = dto.hasName() ? dto.getName().getValue() : null;
    lightSource.id = dto.hasId() ? GUID.valueOf(dto.getId().getValue()) : null;
    lightSource.type = Type.valueOf(dto.getType().name());
    lightSource.shapeType = ShapeType.valueOf(dto.getShapeType().name());
    lightSource.lumens = dto.getLumens();
    lightSource.scaleWithToken = dto.getScaleWithToken();
    return lightSource;
  }

  public LightSourceDto toDto() {
    var dto = LightSourceDto.newBuilder();
    dto.addAllLights(lightList.stream().map(l -> l.toDto()).collect(Collectors.toList()));
    if (name != null) {
      dto.setName(StringValue.of(name));
    }
    if (id != null) {
      dto.setId(StringValue.of(id.toString()));
    }
    dto.setType(LightSourceDto.LightTypeDto.valueOf(type.name()));
    // default shape type
    if (shapeType == null) shapeType = ShapeType.CIRCLE;
    dto.setShapeType(ShapeTypeDto.valueOf(shapeType.name()));
    dto.setLumens(lumens);
    dto.setScaleWithToken(scaleWithToken);
    return dto.build();
  }
}
