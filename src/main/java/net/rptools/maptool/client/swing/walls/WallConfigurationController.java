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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JComboBox;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.topology.VisibilityType;
import net.rptools.maptool.model.topology.Wall;

public class WallConfigurationController {
  private final WallConfigurationView view;
  private @Nullable Zone modelZone;
  private @Nonnull Wall model;

  public WallConfigurationController() {
    this.view = new WallConfigurationView();

    this.modelZone = null;
    this.model = new Wall();

    var directionSelect = view.getDirectionSelect();
    directionSelect.addActionListener(
        e -> {
          var direction = directionSelect.getItemAt(directionSelect.getSelectedIndex());
          if (direction != null && !direction.equals(this.model.direction())) {
            this.model.direction(direction);
            wallUpdated();
          }
        });

    var movementModifierSelect = view.getMovementModifier();
    movementModifierSelect.addActionListener(
        e -> {
          var modifier =
              movementModifierSelect.getItemAt(movementModifierSelect.getSelectedIndex());
          if (modifier != null && !modifier.equals(this.model.movementModifier())) {
            this.model.movementModifier(modifier);
            wallUpdated();
          }
        });

    for (var visibilityType : VisibilityType.values()) {
      final var input = getModifierInput(visibilityType);
      input.addActionListener(
          e -> {
            var modifier = input.getItemAt(input.getSelectedIndex());
            if (modifier != null
                && !modifier.equals(this.model.directionModifier(visibilityType))) {
              this.model.directionModifier(visibilityType, modifier);
              wallUpdated();
            }
          });
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

  private void wallUpdated() {
    if (modelZone != null) {
      MapTool.serverCommand().updateWall(modelZone, this.model);
    }
  }

  public void unbind() {
    // Preserve the current settings in a new prototype wall.
    var prototype = new Wall();
    prototype.copyDataFrom(model);
    bind(null, prototype);
  }

  public void bind(@Nullable Zone zone, @Nonnull Wall model) {
    // Avoid events firing during binding.
    this.modelZone = null;
    this.model = new Wall();

    this.view.getDirectionSelect().setSelectedItem(model.direction());
    this.view.getMovementModifier().setSelectedItem(model.movementModifier());
    for (var visibilityType : VisibilityType.values()) {
      getModifierInput(visibilityType).setSelectedItem(model.directionModifier(visibilityType));
    }

    // Now that the state is set, remember which wall we're bound to.
    this.modelZone = zone;
    this.model = model;
  }

  public @Nonnull Wall getModel() {
    return model;
  }
}
