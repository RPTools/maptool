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
package net.rptools.maptool.model.permissions;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.DisplayName;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.player.Player;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;

/**
 *
 *
 * <h4>Defined Player permissions</h4>
 *
 * <li>Accommodates ordinary RPG use for GM vs Players
 * <li>Adds a level {@link #ALLIED} where friendlies have permission as a step before {@link #ALL}
 * <li>This opens the door for Teams of Players
 * <li>Introduces exclusive permissions beyond GM Only
 *
 *     <p>Used in {@link TokenProperty} for determining who can see a property on a pop-up attribute
 *
 *     <p>sheet, and whether a property appears in the token property editor.
 */
public enum Permissions implements DisplayName.WithTooltip {
  // Order follows increasing visibility for accumulated permissions

  /** Nobody has permission */
  NONE("permission.none.name"),
  /** Only the GMs have permission */
  GM("permission.gm.name"),
  /** The GMs and the Owners have permission */
  OWNER("permission.owner.name"),
  /** The GMs, Owners, and the Owners' Allies have permission */
  ALLIED("permission.allies.name"),
  /** Everyone has permission */
  ALL("permission.all.name"),

  // Exclusive permissions, i.e. Can exclude the GM, etc.
  /** Only the Owners have permission */
  OWNER_ONLY("permission.owner.discrete.name"),
  /** Only the Owners' Allies have permission */
  ALLIED_ONLY("permission.allies.discrete.name"),
  /** Only the Owners and the Owners' Allies have permission */
  OWNER_ALLIED_ONLY("permission.owner.allies.discrete.name"),
  /** Only the Owners' Opponents have permission */
  OPPONENT_ONLY("permission.opponent.name");

  private static PermissionsTests gateKeeper = new RPGGateKeeper();
  final String i18nNameKey;
  final String i18nDescriptionKey;

  Permissions(String i18nKey) {
    this.i18nNameKey = i18nKey;
    this.i18nDescriptionKey = i18nKey + ".description";
    I18N.getText(i18nKey);
  }

  public static Permissions fromString(String lookup) {
    try {
      return valueOf(lookup);
    } catch (IllegalArgumentException e) {
      for (Permissions p : values()) {
        if (p.name().equalsIgnoreCase(lookup) || p.i18nNameKey.equalsIgnoreCase(lookup)) {
          return p;
        }
      }
    }
    return null;
  }

  public static void setGateKeeper(PermissionsTests gateKeeper) {
    Permissions.gateKeeper = gateKeeper;
  }

  public boolean hasPermission(@Nullable Object accessor) {
    return gateKeeper.getAccessorTest().apply(this, accessor);
  }

  public boolean hasPermission(@Nullable Object accessor, @Nullable Object object) {
    return gateKeeper.getObjectAccessTest().apply(this, accessor, object);
  }

  public boolean isAllied(@Nullable Object accessor, @Nullable Object other) {
    return gateKeeper.getObjectAllyTest().apply(accessor, other);
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  @Override
  public String getDescription() {
    return I18N.getMessage(i18nDescriptionKey);
  }

  @Override
  public void setDescription(String value) {
    // unused
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public void setName(String value) {
    // unused
  }

  @Override
  public String getDisplayName() {
    return I18N.getMessage(i18nNameKey);
  }

  @Override
  public void setDisplayName(String value) {
    // unused
  }

  /** A set of tests for determining access by an object for a Permissions permission. */
  public interface PermissionsTests {
    /** Does an accessor object(e.g. Player) have permission generally */
    BiFunction<Permissions, Object, Boolean> getAccessorTest();

    /** Does an accessor object(e.g. Player) have permission generally */
    void setAccessorTest(@NonNull BiFunction<Permissions, Object, Boolean> accessorTest);

    /**
     * Does an accessor object(e.g. Player) have permission to access the target object (e.g. Token)
     */
    TriFunction<Permissions, Object, Object, Boolean> getObjectAccessTest();

    /**
     * Does an accessor object(e.g. Player) have permission to access the target object (e.g. Token)
     */
    void setObjectAccessTest(
        @NonNull TriFunction<Permissions, Object, Object, Boolean> objectAccessTest);

    /** Is the first object an ally of the second object. Used in other tests. */
    BiFunction<Object, Object, Boolean> getObjectAllyTest();

    /** Is the first object an ally of the second object. Used in other tests. */
    void setObjectAllyTest(@Nullable BiFunction<Object, Object, Boolean> objectAllyTest);
  }

  public abstract static class GateKeeper implements PermissionsTests {
    protected BiFunction<Permissions, Object, Boolean> accessorTest;
    protected TriFunction<Permissions, Object, Object, Boolean> objectAccessTest;
    protected BiFunction<Object, Object, Boolean> objectAllyTest;

    public GateKeeper() {}

    @Override
    public BiFunction<Permissions, Object, Boolean> getAccessorTest() {
      return accessorTest;
    }

    @Override
    public void setAccessorTest(@NonNull BiFunction<Permissions, Object, Boolean> accessorTest) {
      this.accessorTest = accessorTest;
    }

    @Override
    public TriFunction<Permissions, Object, Object, Boolean> getObjectAccessTest() {
      return objectAccessTest;
    }

    @Override
    public void setObjectAccessTest(
        @NonNull TriFunction<Permissions, Object, Object, Boolean> objectAccessTest) {
      this.objectAccessTest = objectAccessTest;
    }

    @Override
    public BiFunction<Object, Object, Boolean> getObjectAllyTest() {
      return objectAllyTest;
    }

    @Override
    public void setObjectAllyTest(@Nullable BiFunction<Object, Object, Boolean> objectAllyTest) {
      this.objectAllyTest = objectAllyTest;
    }
  }

  public static class RPGGateKeeper extends GateKeeper {
    private static final Logger log = LogManager.getLogger(RPGGateKeeper.class);

    /**
     * Simplistic check for alliance based on GM vs. players where all players on the same side. All
     * PCs and player-owned NPCs are on the same side. Objects tested must be a player and a token.
     */
    protected BiFunction<Object, Object, Boolean> objectAllyTest;

    public RPGGateKeeper() {
      super();
      setObjectAllyTest(
          (Object token_, Object player_) -> {
            Token token;
            Player player;

            // make sure we have a token and a player
            if (token_ instanceof Token t && player_ instanceof Player p) {
              token = t;
              player = p;
            } else if (player_ instanceof Token t && token_ instanceof Player p) {
              token = t;
              player = p;
            } else {
              return false;
            }

            if (!player.isGM() && token.getType().equals(Token.Type.PC)) {
              // All players and PCs are on the same side
              return true;
            } else {
              // For a GM, all GMs; for a player, all players
              List<Player> teamMates =
                  MapTool.getPlayerList().stream()
                      .dropWhile(pl -> pl.isGM() != player.isGM())
                      .toList();
              final Set<String> owners = token.getOwners();
              // test token is owned by a teammate
              return !teamMates.stream()
                  .filter(pl -> owners.contains(pl.getName()))
                  .toList()
                  .isEmpty();
            }
          });
      setAccessorTest(
          (Permissions permission, Object player) ->
              // for RPG the only real test is for GM status
              switch (permission) {
                case ALL -> true;
                case GM -> player instanceof Player p && p.isGM();
                default -> false;
              });
      setObjectAccessTest(
          (Permissions permission, Object player, Object token) -> {
            if (permission.equals(ALL)) {
              return true;
            } else if (permission.equals(NONE) || player == null || token == null) {
              return false;
            }

            if (player instanceof Player p && token instanceof Token t) {
              // need to determine if player, is GM, Owner, Friend, or Opponent
              AccessorType accessorType;
              if (p.isGM()) {
                accessorType = AccessorType.GM;
              } else if (t.isOwner(p.getName())) {
                accessorType = AccessorType.OWNER;
              } else if (objectAllyTest.apply(t, p)) {
                accessorType = AccessorType.ALLY;
              } else {
                accessorType = AccessorType.OPPONENT;
              }
              // See if they are on the list
              return accessorType.accessAllowed(permission);
            }

            log.warn("Bad arguments somewhere this should not be reached");
            return false;
          });
    }

    /**
     *
     *
     * <h4>Just a convenient holder for allowable permissions lookup</h4>
     */
    private enum AccessorType {
      GM(EnumSet.of(Permissions.GM, Permissions.OWNER, ALLIED, ALL)),
      OWNER(EnumSet.of(Permissions.OWNER, ALL, OWNER_ONLY, OWNER_ALLIED_ONLY)),
      ALLY(EnumSet.of(ALLIED, ALL, ALLIED_ONLY, OWNER_ALLIED_ONLY)),
      OPPONENT(EnumSet.of(ALL, OPPONENT_ONLY));

      final EnumSet<Permissions> access;

      AccessorType(EnumSet<Permissions> access) {
        this.access = access;
      }

      boolean accessAllowed(Permissions permission) {
        return access.contains(permission);
      }
    }
  }
}
