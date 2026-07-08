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
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.rptools.maptool.server.proto.HaloDto;

/**
 * Represents a halo that can be attached to tokens, each containing {@link HaloPart}{@code s}.
 *
 * <p>This class is immutable.
 */
public final class Halo implements Serializable {

  private final @Nonnull GUID id;
  private final @Nonnull String name;
  private final boolean gmOnly;
  private final boolean ownerOnly;
  private final boolean inner;
  private final boolean facingWithToken;
  private final boolean flipWithToken;
  private final boolean scaleWithToken;

  /** The halo parts that make up the halo. */
  private final @Nonnull List<HaloPart> haloParts;

  public Halo(
      @Nonnull GUID id,
      @Nonnull String name,
      boolean gmOnly,
      boolean ownerOnly,
      boolean inner,
      boolean facingWithToken,
      boolean flipWithToken,
      boolean scaleWithToken,
      @Nonnull List<HaloPart> haloParts) {
    this.id = id;
    this.name = name;
    this.gmOnly = gmOnly;
    this.ownerOnly = ownerOnly;
    this.inner = inner;
    this.facingWithToken = facingWithToken;
    this.flipWithToken = flipWithToken;
    this.scaleWithToken = scaleWithToken;
    this.haloParts = haloParts;
  }

  @Serial
  public Object writeReplace() {
    // Make sure XStream keeps the serialization nice. We don't need the XML to contain
    // implementation details of the ImmutableList in use.
    return new Halo(
        id,
        name,
        gmOnly,
        ownerOnly,
        inner,
        facingWithToken,
        flipWithToken,
        scaleWithToken,
        new ArrayList<>(haloParts));
  }

  @Serial
  private @Nonnull Object readResolve() {
    // Rather than modifying the current object, we'll create a replacement that is definitely
    // initialized properly.
    return new Halo(
        this.id,
        this.name,
        this.gmOnly,
        this.ownerOnly,
        this.inner,
        this.facingWithToken,
        this.flipWithToken,
        this.scaleWithToken,
        ImmutableList.copyOf(this.haloParts));
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Halo)) {
      return false;
    }
    return Objects.equals(((Halo) obj).id, id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  public @Nonnull GUID getId() {
    return id;
  }

  public @Nonnull String getName() {
    return name;
  }

  /**
   * @return A read-only list of halo parts belonging to this halo.
   */
  public @Nonnull List<HaloPart> getHaloParts() {
    return haloParts;
  }

  /**
   * @return Whether the halo is visible to GMs only.
   */
  public boolean isGMOnly() {
    return gmOnly;
  }

  /**
   * @return Whether the halo is visible to owners only.
   */
  public boolean isOwnerOnly() {
    return ownerOnly;
  }

  /**
   * @return Whether to place the halo next to the token and ignore any halo concentric offsets from
   *     multiple halos.
   */
  public boolean isInner() {
    return inner;
  }

  /**
   * @return Whether to rotate the halo with the token's facing.
   */
  public boolean isFacingWithToken() {
    return facingWithToken;
  }

  /**
   * @return Whether to flip the halo with the token's flipping.
   */
  public boolean isFlipWithToken() {
    return flipWithToken;
  }

  /**
   * @return Whether to scale the halo with the token's size.
   */
  public boolean isScaleWithToken() {
    return scaleWithToken;
  }

  @Override
  public String toString() {
    return name;
  }

  public static @Nonnull Halo fromDto(@Nonnull HaloDto dto) {
    return new Halo(
        GUID.valueOf(dto.getId()),
        dto.getName(),
        dto.getGmOnly(),
        dto.getOwnerOnly(),
        dto.getInner(),
        dto.getFacingWithToken(),
        dto.getFlipWithToken(),
        dto.getScaleWithToken(),
        dto.getHaloPartsList().stream()
            .map(HaloPart::fromDto)
            .collect(ImmutableList.toImmutableList()));
  }

  public @Nonnull HaloDto toDto() {
    var dto = HaloDto.newBuilder();
    dto.setId(id.toString());
    dto.setName(name);
    dto.setGmOnly(gmOnly);
    dto.setOwnerOnly(ownerOnly);
    dto.setInner(inner);
    dto.setFacingWithToken(facingWithToken);
    dto.setFlipWithToken(flipWithToken);
    dto.setScaleWithToken(scaleWithToken);
    dto.addAllHaloParts(haloParts.stream().map(hp -> hp.toDto()).collect(Collectors.toList()));
    return dto.build();
  }
}
