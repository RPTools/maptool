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
package net.rptools.maptool.client.swing.walls;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComboBox;
import net.rptools.lib.CollectionUtil;
import net.rptools.maptool.model.topology.VisibilityType;
import net.rptools.maptool.model.topology.Wall;

public class WallConfigurationController {
  /**
   * Like Wall.Data, but mutable, observable, and nullable (in case of multiple selected walls not
   * agreeing).
   */
  public static final class WallData {

    private final @Nullable Wall.Direction direction;
    private final @Nullable Wall.MovementDirectionModifier movementModifier;
    // DO NOT MODIFY `modifiers`!
    private final EnumMap<VisibilityType, Wall.DirectionModifier> modifiers;

    public WallData(
        @Nullable Wall.Direction direction,
        @Nullable Wall.MovementDirectionModifier movementModifier,
        Map<VisibilityType, Wall.DirectionModifier> modifiers) {
      this.direction = direction;
      this.movementModifier = movementModifier;
      this.modifiers = new EnumMap<>(VisibilityType.class);
      this.modifiers.putAll(modifiers);
    }

    public WallData(WallData other) {
      this(other.direction, other.movementModifier, other.modifiers);
    }

    public WallData(Wall.Data data) {
      this(
          data.direction(),
          data.movementModifier(),
          CollectionUtil.newFilledEnumMap(VisibilityType.class, data::directionModifier));
    }

    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof WallData other)) {
        return false;
      }

      return this.direction == other.direction
          && this.movementModifier == other.movementModifier
          && this.modifiers.equals(other.modifiers);
    }

    public @Nullable Wall.Direction direction() {
      return direction;
    }

    public WallData withDirection(@Nullable Wall.Direction direction) {
      return new WallData(direction, this.movementModifier, this.modifiers);
    }

    public @Nullable Wall.MovementDirectionModifier movementModifier() {
      return movementModifier;
    }

    public WallData withMovementModifier(
        @Nullable Wall.MovementDirectionModifier movementModifier) {
      return new WallData(this.direction, movementModifier, this.modifiers);
    }

    public @Nullable Wall.DirectionModifier directionModifier(VisibilityType visibilityType) {
      return this.modifiers.get(visibilityType);
    }

    public WallData withDirectionModifier(
        VisibilityType visibilityType, @Nullable Wall.DirectionModifier modifier) {
      var newModifiers = new EnumMap<>(this.modifiers);
      if (modifier == null) {
        newModifiers.remove(visibilityType);
      } else {
        newModifiers.put(visibilityType, modifier);
      }
      return new WallData(this.direction, this.movementModifier, newModifiers);
    }

    public Wall.Data toWallData() {
      return toWallData(new Wall.Data());
    }

    public Wall.Data toWallData(Wall.Data original) {
      return new Wall.Data(
          Objects.requireNonNullElse(direction, original.direction()),
          Objects.requireNonNullElse(movementModifier, original.movementModifier()),
          CollectionUtil.newFilledEnumMap(
              VisibilityType.class,
              type ->
                  Objects.requireNonNullElseGet(
                      modifiers.get(type), () -> original.directionModifier(type))));
    }
  }

  private final WallConfigurationView view;
  private final PropertyChangeListener changeListener;
  private @Nonnull WallData wallData = new WallData(new Wall.Data());

  public WallConfigurationController(PropertyChangeListener changeListener) {
    this.view = new WallConfigurationView();
    this.changeListener = changeListener;

    var directionSelect = view.getDirectionSelect();
    directionSelect.addActionListener(
        e -> {
          var newDirection = directionSelect.getItemAt(directionSelect.getSelectedIndex());
          if (newDirection != null) {
            updateWall(wallData.withDirection(newDirection));
          }
        });

    var movementModifierSelect = view.getMovementModifier();
    movementModifierSelect.addActionListener(
        e -> {
          var modifier =
              movementModifierSelect.getItemAt(movementModifierSelect.getSelectedIndex());
          if (modifier != null) {
            updateWall(wallData.withMovementModifier(modifier));
          }
        });

    for (var visibilityType : VisibilityType.values()) {
      final var input = getModifierInput(visibilityType);
      input.addActionListener(
          e -> {
            var modifier = input.getItemAt(input.getSelectedIndex());
            if (modifier != null) {
              updateWall(wallData.withDirectionModifier(visibilityType, modifier));
            }
          });
    }
  }

  private void updateWall(WallData newWallData) {
    if (!wallData.equals(newWallData)) {
      var oldWallData = wallData;
      wallData = newWallData;
      changeListener.propertyChange(
          new PropertyChangeEvent(this, "wallData", oldWallData, wallData));
    }
  }

  public WallConfigurationView getView() {
    return view;
  }

  private JComboBox<Wall.DirectionModifier> getModifierInput(VisibilityType visibilityType) {
    return switch (visibilityType) {
      case Sight -> view.getSightModifier();
      case Light -> view.getLightModifier();
      case Aura -> view.getAuraModifier();
    };
  }

  public void bind(@Nonnull Wall.Data model) {
    bind(new WallData(model));
  }

  public void bind(@Nonnull WallData model) {
    this.wallData = new WallData(model);

    this.view.getDirectionSelect().setSelectedItem(this.wallData.direction());
    this.view.getMovementModifier().setSelectedItem(this.wallData.movementModifier());
    for (var visibilityType : VisibilityType.values()) {
      getModifierInput(visibilityType)
          .setSelectedItem(this.wallData.directionModifier(visibilityType));
    }
  }

  public @Nonnull WallData getModel() {
    return new WallData(wallData);
  }
}
