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

import com.google.common.collect.ImmutableList;
import com.google.protobuf.StringValue;
import java.awt.geom.Area;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.server.proto.LightSourceDto;
import org.apache.commons.lang.math.NumberUtils;

/**
 * Represents a light source that can be attached to tokens.
 *
 * <p>This class is immutable.
 */
public final class LightSource implements Comparable<LightSource>, Serializable {
  public enum Type {
    NORMAL,
    AURA
  }

  private final @Nullable String name;
  private final @Nullable GUID id;
  private final @Nonnull Type type;
  private final boolean scaleWithToken;
  private final boolean ignoresVBL;

  /**
   * This light segments that make up the light source.
   *
   * <p>In practice this will be an {@code ImmutableList} during runtime. However, previously
   * serialized {@code LightSource} instances may have specified that it must be a {@code
   * LinkedList} or other specific {@code List} implementation. So we need to keep this as a {@code
   * List} in order to deserialize those.
   *
   * <p>There is also one case where it won't be an {@code ImmutableList}, and that is during
   * serialization. At such a time, a temporary {@code LightSource} is created with an {@code
   * ArrayList} instead. (see {@link #writeReplace()}) so that the XML does not depend on the use of
   * {@code ImmutableList} or any other particular {@code List} implementation.
   */
  private final @Nonnull List<Light> lightList;

  // Lumens are now in the individual Lights. This field is only here for backwards compatibility
  // and should not otherwise be used.
  @Deprecated private int lumens = Integer.MIN_VALUE;

  /**
   * Constructs a personal light source.
   *
   * <p>Since a personal light source is directly attached to a specific sight type, they do not
   * need (or have) names and GUIDs.
   *
   * @param scaleWithToken if {@code true}, the size of the lights will scale with the token size.
   * @param lights The set of lights that constitute the personal light source.
   */
  public static LightSource createPersonal(
      boolean scaleWithToken, boolean ignoresVBL, Collection<Light> lights) {
    return new LightSource(
        null, null, Type.NORMAL, scaleWithToken, ignoresVBL, ImmutableList.copyOf(lights));
  }

  /**
   * Constructs a non-personal light source.
   *
   * <p>These light sources are referenced both by name and GUID, and thus need both.
   *
   * @param name The name of the light source.
   * @param id The unique ID of the light source.
   * @param type The type of light, whether a normal light or an aura.
   * @param scaleWithToken if {@code true}, the size of the lights will scale with the token size.
   * @param ignoresVBL if {@code true}, the light will ignore vbl
   * @param lights The set of lights that constitute the personal light source.
   */
  public static LightSource createRegular(
      @Nonnull String name,
      @Nonnull GUID id,
      @Nonnull Type type,
      boolean scaleWithToken,
      boolean ignoresVBL,
      @Nonnull Collection<Light> lights) {
    return new LightSource(
        name, id, type, scaleWithToken, ignoresVBL, ImmutableList.copyOf(lights));
  }

  private LightSource(
      @Nullable String name,
      @Nullable GUID id,
      @Nonnull Type type,
      boolean scaleWithToken,
      boolean ignoresVBL,
      @Nonnull List<Light> lights) {
    this.name = name;
    this.id = id;
    this.type = type;
    this.scaleWithToken = scaleWithToken;
    this.ignoresVBL = ignoresVBL;
    this.lightList = lights;
  }

  @Serial
  public Object writeReplace() {
    // Make sure XStream keeps the serialization nice. We don't need the XML to contain
    // implementation details of the ImmutableList in use.
    return new LightSource(name, id, type, scaleWithToken, ignoresVBL, new ArrayList<>(lightList));
  }

  @SuppressWarnings("ConstantConditions")
  @Serial
  private @Nonnull Object readResolve() {
    final List<Light> originalLights =
        Objects.requireNonNullElse(lightList, Collections.emptyList());
    final List<Light> lights;
    if (lumens == Integer.MIN_VALUE) {
      // This is an up-to-date LightSource with lumens already stored in the Lights.
      lights = originalLights;
    } else {
      // This is an old light source with a lumens value that needs to be pushed into the individual
      // Lights.
      lights = new ArrayList<>();
      for (final var light : originalLights) {
        lights.add(
            new Light(
                light.getShape(),
                light.getFacingOffset(),
                light.getRadius(),
                light.getWidth(),
                light.getArcAngle(),
                light.getPaint(),
                lumens == 0 ? 100 : lumens,
                light.isGM(),
                light.isOwnerOnly()));
      }
    }

    // Rather than modifying the current object, we'll create a replacement that is definitely
    // initialized properly.
    return new LightSource(
        this.name,
        this.id,
        Objects.requireNonNullElse(this.type, Type.NORMAL),
        this.scaleWithToken,
        this.ignoresVBL,
        ImmutableList.copyOf(lights));
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LightSource)) {
      return false;
    }
    return Objects.equals(((LightSource) obj).id, id);
  }

  public double getMaxRange() {
    double range = 0;
    for (Light light : lightList) {
      range = Math.max(range, light.getRadius());
    }
    return range;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public @Nullable GUID getId() {
    return id;
  }

  public @Nullable String getName() {
    return name;
  }

  /**
   * @return A read-only list of lights belonging to this LightSource
   */
  public @Nonnull List<Light> getLightList() {
    return lightList;
  }

  public @Nonnull Type getType() {
    return type;
  }

  public boolean isScaleWithToken() {
    return scaleWithToken;
  }

  public boolean isIgnoresVBL() {
    return ignoresVBL;
  }

  public record LightArea(Light light, Area area) {}

  public @Nonnull List<LightArea> getLightAreas(
      @Nonnull Token token, @Nonnull Zone zone, double multiplier) {
    // Tracks the cumulative inner ranges of light sources so that we can cut them out of the
    // outer ranges and end up with disjoint sets, even when magnifying.
    // Note that this "hole punching" has nothing to do with lumen strength, it's just a way of
    // making smaller ranges act as lower bounds for larger ranges.

    // Auras do not get magnified.
    if (type != Type.NORMAL) {
      multiplier = 1.0;
    }

    final var result = new ArrayList<LightArea>();
    final var cummulativeNotTransformedArea = new Area();

    for (final var light : lightList) {
      final var notScaledLightArea = light.getArea(token, zone, scaleWithToken);

      final var lightArea = light.getArea(token, zone, multiplier, scaleWithToken);
      lightArea.subtract(cummulativeNotTransformedArea);
      result.add(new LightArea(light, lightArea));

      cummulativeNotTransformedArea.add(notScaledLightArea);
    }
    return result;
  }

  /* Area for all lights combined */
  public @Nonnull Area getArea(@Nonnull Token token, @Nonnull Zone zone, double multiplier) {
    // Auras do not get magnified.
    if (type != Type.NORMAL) {
      multiplier = 1.0;
    }

    Area area = new Area();
    for (Light light : lightList) {
      area.add(light.getArea(token, zone, multiplier, isScaleWithToken()));
    }

    return area;
  }

  /*
   * Area for a single light, subtracting any previous lights
   */
  public @Nonnull Area getArea(@Nonnull Token token, @Nonnull Zone zone, @Nonnull Light light) {
    Area area = light.getArea(token, zone, scaleWithToken);
    // TODO: This seems horribly inefficient
    // Subtract out the lights that are previously defined
    for (int i = lightList.indexOf(light) - 1; i >= 0; i--) {
      Light lessLight = lightList.get(i);
      area.subtract(lessLight.getArea(token, zone, scaleWithToken));
    }
    return area;
  }

  /* Area for all lights combined */
  public @Nonnull Area getArea(@Nonnull Token token, @Nonnull Zone zone) {
    return getArea(token, zone, 1.0);
  }

  @SuppressWarnings("unchecked")
  public static @Nonnull Map<String, List<LightSource>> getDefaultLightSources()
      throws IOException {
    Object defaultLights =
        FileUtil.objFromResource("net/rptools/maptool/model/defaultLightSourcesMap.xml");
    return (Map<String, List<LightSource>>) defaultLights;
  }

  @Override
  public String toString() {
    return name;
  }

  /*
   * Compares this light source with another.
   *
   * Light sources are compared by name. If both names are numeric strings, they will be compared as
   * integers. Otherwise they will be compared lexicographically.
   *
   * This must only be called on light source that have a name, i.e., not on personal lights.
   *
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(@Nonnull LightSource o) {
    if (o != this) {
      int nameLong = NumberUtils.toInt(name, Integer.MIN_VALUE);
      int onameLong = NumberUtils.toInt(o.name, Integer.MIN_VALUE);
      if (nameLong != Integer.MIN_VALUE && onameLong != Integer.MIN_VALUE)
        return nameLong - onameLong;
      return name.compareTo(o.name);
    }
    return 0;
  }

  public static @Nonnull LightSource fromDto(@Nonnull LightSourceDto dto) {
    return new LightSource(
        dto.hasName() ? dto.getName().getValue() : null,
        dto.hasId() ? GUID.valueOf(dto.getId().getValue()) : null,
        Type.valueOf(dto.getType().name()),
        dto.getScaleWithToken(),
        dto.getIgnoresVBL(),
        dto.getLightsList().stream().map(Light::fromDto).collect(ImmutableList.toImmutableList()));
  }

  public @Nonnull LightSourceDto toDto() {
    var dto = LightSourceDto.newBuilder();
    dto.addAllLights(lightList.stream().map(l -> l.toDto()).collect(Collectors.toList()));
    if (name != null) {
      dto.setName(StringValue.of(name));
    }
    if (id != null) {
      dto.setId(StringValue.of(id.toString()));
    }
    dto.setType(LightSourceDto.LightTypeDto.valueOf(type.name()));
    dto.setScaleWithToken(scaleWithToken);
    dto.setIgnoresVBL(ignoresVBL);
    return dto.build();
  }
}
