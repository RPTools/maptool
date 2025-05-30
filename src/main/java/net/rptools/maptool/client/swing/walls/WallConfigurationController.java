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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import net.rptools.maptool.model.topology.VisibilityType;
import net.rptools.maptool.model.topology.Wall;

public class WallConfigurationController {
  private final WallConfigurationView view;
  private final PropertyChangeListener changeListener;
  private @Nonnull Wall.Data wallData = new Wall.Data();

  public WallConfigurationController(PropertyChangeListener changeListener) {
    this.view = new WallConfigurationView();
    this.changeListener = changeListener;

    var directionSelect = view.getDirectionSelect();
    directionSelect.addActionListener(
        e -> {
          var newDirection = directionSelect.getItemAt(directionSelect.getSelectedIndex());
          if (newDirection != null) {
            updateWall(newDirection, wallData.movementModifier(), wallData.modifiers());
          }
        });

    var movementModifierSelect = view.getMovementModifier();
    movementModifierSelect.addActionListener(
        e -> {
          var modifier =
              movementModifierSelect.getItemAt(movementModifierSelect.getSelectedIndex());
          if (modifier != null) {
            updateWall(wallData.direction(), modifier, wallData.modifiers());
          }
        });

    for (var visibilityType : VisibilityType.values()) {
      final var input = getModifierInput(visibilityType);
      input.addActionListener(
          e -> {
            var modifier = input.getItemAt(input.getSelectedIndex());
            if (modifier != null) {
              var newModifiers =
                  new EnumMap<VisibilityType, Wall.DirectionModifier>(VisibilityType.class);
              newModifiers.putAll(wallData.modifiers());
              newModifiers.put(visibilityType, modifier);

              updateWall(
                  wallData.direction(),
                  wallData.movementModifier(),
                  Maps.immutableEnumMap(newModifiers));
            }
          });
    }
  }

  private void updateWall(
      Wall.Direction direction,
      Wall.MovementDirectionModifier movementModifier,
      ImmutableMap<VisibilityType, Wall.DirectionModifier> modifiers) {
    var newWallData = new Wall.Data(direction, movementModifier, modifiers);

    if (wallData.equals(newWallData)) {
      return;
    }

    var oldWallData = wallData;
    wallData = newWallData;

    changeListener.propertyChange(new PropertyChangeEvent(this, "wallData", oldWallData, wallData));
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
    this.wallData = model;

    this.view.getDirectionSelect().setSelectedItem(this.wallData.direction());
    this.view.getMovementModifier().setSelectedItem(this.wallData.movementModifier());
    for (var visibilityType : VisibilityType.values()) {
      getModifierInput(visibilityType)
          .setSelectedItem(this.wallData.directionModifier(visibilityType));
    }
  }

  public @Nonnull Wall.Data getModel() {
    return wallData;
  }
}
