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
package net.rptools.maptool.model.topology;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.server.proto.TopologyDirectionModifier;
import net.rptools.maptool.server.proto.TopologyMovementDirectionModifier;
import net.rptools.maptool.server.proto.WallDataDto;
import net.rptools.maptool.server.proto.WallDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a single wall.
 *
 * <p>Even though the underlying graph is undirected, walls still have a concept of an orientation
 * or bearing. This is only important when considering directionality, which is why it is not part
 * of the graph structure.
 */
public final class Wall {
  private static final Logger log = LogManager.getLogger(Wall.class);

  private final GUID from;
  private final GUID to;
  private Data data;

  public Wall() {
    this(new GUID(), new GUID(), new Data());
  }

  public Wall(Wall other) {
    this(other.from, other.to, other.data);
  }

  /**
   * @param from The ID of the source vertex.
   * @param to The ID of the target vertex.
   */
  public Wall(GUID from, GUID to) {
    this(from, to, new Data());
  }

  /**
   * @param from The ID of the source vertex.
   * @param to The ID of the target vertex.
   * @param data The payload of the wall.
   */
  public Wall(GUID from, GUID to, Data data) {
    this.from = from;
    this.to = to;
    this.data = data;
  }

  public Wall(
      GUID from,
      GUID to,
      Direction direction,
      MovementDirectionModifier movementModifier,
      Map<VisibilityType, DirectionModifier> modifiers) {
    this(from, to, new Data(direction, movementModifier, modifiers));
  }

  public GUID from() {
    return from;
  }

  public GUID to() {
    return to;
  }

  /**
   * Swap the heading of the wall.
   *
   * <p>The source and target vertices in the result are swapped around. To make the direction be
   * equivalent, it is also reversed.
   *
   * @return An equivalent but reversed wall.
   */
  public Wall reversed() {
    return new Wall(
        to, from, data.direction().reversed(), data.movementModifier(), data.modifiers());
  }

  public void setData(Data otherData) {
    this.data = otherData;
  }

  /**
   * Gets the wall's data.
   *
   * <p>This is useful for stashing the data somewhere. If the data needs to be manipulated, use
   * methods on {@link Wall} itself.
   *
   * @return The wall's data.
   */
  public Data data() {
    return data;
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Wall wall
        && Objects.equals(this.from, wall.from)
        && Objects.equals(this.to, wall.to);
  }

  @Override
  public int hashCode() {
    return Objects.hash(from, to);
  }

  public WallDto toDto() {
    return WallDto.newBuilder()
        .setFrom(from.toString())
        .setTo(to.toString())
        .setData(data.toDto())
        .build();
  }

  public static Wall fromDto(WallDto dto) {
    return new Wall(new GUID(dto.getFrom()), new GUID(dto.getTo()), Data.fromDto(dto.getData()));
  }

  public enum Direction {
    Both,
    Left,
    Right;

    public Direction reversed() {
      return switch (this) {
        case Both -> Both;
        case Left -> Right;
        case Right -> Left;
      };
    }

    public Direction merged(Direction other) {
      if (this == other) {
        // The directions agree.
        return this;
      }

      // Either:
      // 1. One of the directions is Both, and therefore as strict as possible; or
      // 2. The directions are opposite to each other, thus meaning both ways should be blocked.
      return Both;
    }

    @Override
    public String toString() {
      var key =
          switch (this) {
            case Both -> "WallDirection.Both";
            case Left -> "WallDirection.Left";
            case Right -> "WallDirection.Right";
          };
      return I18N.getText(key);
    }

    public net.rptools.maptool.server.proto.WallDirection toDto() {
      return switch (this) {
        case Both -> net.rptools.maptool.server.proto.WallDirection.Both;
        case Left -> net.rptools.maptool.server.proto.WallDirection.Left;
        case Right -> net.rptools.maptool.server.proto.WallDirection.Right;
      };
    }

    public static Direction fromDto(net.rptools.maptool.server.proto.WallDirection direction) {
      return switch (direction) {
        case Both -> Both;
        case Left -> Left;
        case Right -> Right;
        case UNRECOGNIZED -> {
          log.error("Unrecognized wall direction. Setting to default");
          yield Both;
        }
      };
    }
  }

  public enum DirectionModifier {
    SameDirection,
    ReverseDirection,
    ForceBoth,
    Disabled;

    public DirectionModifier merged(DirectionModifier other) {
      if (this == other) {
        return this;
      } else if (this == ForceBoth || other == ForceBoth) {
        return DirectionModifier.ForceBoth;
      } else if (this == DirectionModifier.Disabled) {
        return other;
      } else if (other == DirectionModifier.Disabled) {
        return this;
      } else {
        // One is SameDirection and the other is ReverseDirection.
        return DirectionModifier.ForceBoth;
      }
    }

    @Override
    public String toString() {
      var key =
          switch (this) {
            case SameDirection -> "DirectionModifier.SameDirection";
            case ReverseDirection -> "DirectionModifier.ReverseDirection";
            case ForceBoth -> "DirectionModifier.ForceBoth";
            case Disabled -> "DirectionModifier.Disabled";
          };
      return I18N.getText(key);
    }

    public TopologyDirectionModifier toDto() {
      return switch (this) {
        case SameDirection -> TopologyDirectionModifier.SameDirection;
        case ReverseDirection -> TopologyDirectionModifier.ReverseDirection;
        case ForceBoth -> TopologyDirectionModifier.ForceBoth;
        case Disabled -> TopologyDirectionModifier.Disabled;
      };
    }

    public static DirectionModifier fromDto(TopologyDirectionModifier modifier) {
      return switch (modifier) {
        case SameDirection -> SameDirection;
        case ReverseDirection -> ReverseDirection;
        case ForceBoth -> ForceBoth;
        case Disabled -> Disabled;
        case UNRECOGNIZED -> {
          log.error("Unrecognized wall direction modifier. Setting to default");
          yield SameDirection;
        }
      };
    }
  }

  /**
   * Like {@link DirectionModifier} but only allowing forcing both direction or being disabled.
   *
   * <p>This is since movement blocking does not yet support directional walls. This enum allows
   * movement blocking to be disabled just as for sights, lights, and auras while accepting the more
   * limited functionality.
   *
   * <p>If movement blocking does at some point support the same options, this type should be merged
   * with {@link DirectionModifier}.
   */
  public enum MovementDirectionModifier {
    ForceBoth,
    Disabled;

    private static final Logger log = LogManager.getLogger(MovementDirectionModifier.class);

    public MovementDirectionModifier merged(MovementDirectionModifier other) {
      if (this == ForceBoth || other == ForceBoth) {
        return ForceBoth;
      } else {
        return Disabled;
      }
    }

    @Override
    public String toString() {
      var key =
          switch (this) {
            case ForceBoth -> "DirectionModifier.ForceBoth";
            case Disabled -> "DirectionModifier.Disabled";
          };
      return I18N.getText(key);
    }

    public TopologyMovementDirectionModifier toDto() {
      return switch (this) {
        case ForceBoth -> TopologyMovementDirectionModifier.MovementForceBoth;
        case Disabled -> TopologyMovementDirectionModifier.MovementDisabled;
      };
    }

    public static MovementDirectionModifier fromDto(TopologyMovementDirectionModifier modifier) {
      return switch (modifier) {
        case MovementForceBoth -> ForceBoth;
        case MovementDisabled -> Disabled;
        case UNRECOGNIZED -> {
          log.error("Unrecognized wall direction modifier. Setting to default");
          yield ForceBoth;
        }
      };
    }
  }

  /**
   * Contains a wall's configuration.
   *
   * <p>This type is immutable. If the data needs to be changed, use methods on {@link Wall} to
   * achieve it.
   */
  public record Data(
      Direction direction,
      MovementDirectionModifier movementModifier,
      ImmutableMap<VisibilityType, DirectionModifier> modifiers) {
    public Data() {
      this(Direction.Both, MovementDirectionModifier.ForceBoth, Map.of());
    }

    public Data(
        Direction direction,
        MovementDirectionModifier movementModifier,
        Map<VisibilityType, DirectionModifier> modifiers) {
      this(direction, movementModifier, Maps.immutableEnumMap(modifiers));
    }

    public DirectionModifier directionModifier(VisibilityType visibilityType) {
      return Objects.requireNonNullElse(
          modifiers.get(visibilityType), DirectionModifier.SameDirection);
    }

    /**
     * Produces equivalent wall data that points in the given direction.
     *
     * <p>If the wall is bidirectional or already points in the given direction, this method does
     * nothing. Otherwise, it flips the direction and replaces all modifiers to point in the
     * opposite direction.
     *
     * <p>Normalizing the wall direction like this makes some casework easier when merging walls.
     */
    private Data normalizedTo(Direction direction) {
      if (direction == Direction.Both
          || this.direction == Direction.Both
          || direction == this.direction) {
        // Nothing to do.
        return this;
      }

      // Nothing for movement direction right now.
      var newMovementModifier = this.movementModifier;
      var newModifiers = new EnumMap<VisibilityType, DirectionModifier>(VisibilityType.class);
      for (var visibilityType : VisibilityType.values()) {
        var newModifier =
            switch (directionModifier(visibilityType)) {
              case SameDirection -> DirectionModifier.ReverseDirection;
              case ReverseDirection -> DirectionModifier.SameDirection;
              case ForceBoth -> DirectionModifier.ForceBoth;
              case Disabled -> DirectionModifier.Disabled;
            };
        newModifiers.put(visibilityType, newModifier);
      }
      return new Data(direction, newMovementModifier, newModifiers);
    }

    public Data merge(Data other) {
      var resultDirection = this.direction.merged(other.direction);

      // Normalized copy so we know that both walls are pointing in the same direction,
      // (assuming neither are set to `Both`; otherwise it doesn't matter).
      var normalizedOther = other.normalizedTo(this.direction);

      var resultMovementModifier = this.movementModifier.merged(normalizedOther.movementModifier);

      var resultModifiers = new EnumMap<VisibilityType, DirectionModifier>(VisibilityType.class);
      for (var visibilityType : VisibilityType.values()) {
        var thisMod = this.directionModifier(visibilityType);
        var otherMod = normalizedOther.directionModifier(visibilityType);

        // Block in every direction that the inputs block.
        var newModifier = thisMod.merged(otherMod);
        resultModifiers.put(visibilityType, newModifier);
      }

      return new Data(resultDirection, resultMovementModifier, resultModifiers);
    }

    public WallDataDto toDto() {
      return WallDataDto.newBuilder()
          .setDirection(direction.toDto())
          .setMovementDirectionModifier(movementModifier.toDto())
          .setSightDirectionModifier(directionModifier(VisibilityType.Sight).toDto())
          .setLightDirectionModifier(directionModifier(VisibilityType.Light).toDto())
          .setAuraDirectionModifier(directionModifier(VisibilityType.Aura).toDto())
          .build();
    }

    public static Data fromDto(WallDataDto dto) {
      var direction = Direction.fromDto(dto.getDirection());
      var movementModifier = MovementDirectionModifier.fromDto(dto.getMovementDirectionModifier());
      var modifiers =
          Map.of(
              VisibilityType.Sight,
              DirectionModifier.fromDto(dto.getSightDirectionModifier()),
              VisibilityType.Light,
              DirectionModifier.fromDto(dto.getLightDirectionModifier()),
              VisibilityType.Aura,
              DirectionModifier.fromDto(dto.getAuraDirectionModifier()));

      return new Data(direction, movementModifier, modifiers);
    }
  }
}
