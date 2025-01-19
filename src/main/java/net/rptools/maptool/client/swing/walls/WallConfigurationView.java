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

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import net.rptools.maptool.model.topology.Wall;

public class WallConfigurationView {
  private JPanel mainPanel;
  private JComboBox<Wall.Direction> direction;
  private JComboBox<Wall.DirectionModifier> sightModifier;
  private JComboBox<Wall.DirectionModifier> lightModifier;
  private JComboBox<Wall.DirectionModifier> auraModifier;
  private JComboBox<Wall.MovementDirectionModifier> movementModifier;

  public WallConfigurationView() {
    direction.setModel(new DefaultComboBoxModel<>(Wall.Direction.values()));
    sightModifier.setModel(new DefaultComboBoxModel<>(Wall.DirectionModifier.values()));
    lightModifier.setModel(new DefaultComboBoxModel<>(Wall.DirectionModifier.values()));
    auraModifier.setModel(new DefaultComboBoxModel<>(Wall.DirectionModifier.values()));
    movementModifier.setModel(new DefaultComboBoxModel<>(Wall.MovementDirectionModifier.values()));
  }

  public JPanel getRootComponent() {
    return mainPanel;
  }

  public JComboBox<Wall.Direction> getDirectionSelect() {
    return direction;
  }

  public JComboBox<Wall.DirectionModifier> getSightModifier() {
    return sightModifier;
  }

  public JComboBox<Wall.DirectionModifier> getLightModifier() {
    return lightModifier;
  }

  public JComboBox<Wall.DirectionModifier> getAuraModifier() {
    return auraModifier;
  }

  public JComboBox<Wall.MovementDirectionModifier> getMovementModifier() {
    return movementModifier;
  }
}
