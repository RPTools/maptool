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
package net.rptools.maptool.client.swing;

import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;

public class TopologyModeSelectionPanel extends JToolBar {
  /** The instance. Used to update the button when the ZoneRenderer is changed. */
  private static TopologyModeSelectionPanel instance;

  public static TopologyModeSelectionPanel getInstance() {
    return instance;
  }

  private final Map<Zone.TopologyType, JToggleButton> modeButtons;

  public TopologyModeSelectionPanel() {
    instance = this;

    setFloatable(false);
    setRollover(true);
    setBorder(null);
    setBorderPainted(false);

    modeButtons = new EnumMap<>(Zone.TopologyType.class);

    var initiallySelectedTypes = AppPreferences.getTopologyTypes();
    createAndAddModeButton(
        Zone.TopologyType.WALL_VBL,
        Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON,
        Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_OFF,
        "tools.topology_mode_selection.vbl.tooltip",
        initiallySelectedTypes);
    createAndAddModeButton(
        Zone.TopologyType.HILL_VBL,
        Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON,
        Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_OFF,
        "tools.topology_mode_selection.hill_vbl.tooltip",
        initiallySelectedTypes);
    createAndAddModeButton(
        Zone.TopologyType.PIT_VBL,
        Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON,
        Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_OFF,
        "tools.topology_mode_selection.pit_vbl.tooltip",
        initiallySelectedTypes);
    createAndAddModeButton(
        Zone.TopologyType.COVER_VBL,
        Icons.TOOLBAR_TOPOLOGY_TYPE_COVER_ON,
        Icons.TOOLBAR_TOPOLOGY_TYPE_COVER_OFF,
        "tools.topology_mode_selection.cover_vbl.tooltip",
        initiallySelectedTypes);
    createAndAddModeButton(
        Zone.TopologyType.MBL,
        Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON,
        Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_OFF,
        "tools.topology_mode_selection.mbl.tooltip",
        initiallySelectedTypes);

    this.add(Box.createHorizontalStrut(5));
  }

  private void createAndAddModeButton(
      Zone.TopologyType type,
      final Icons icon,
      final Icons offIcon,
      String toolTipKey,
      Zone.TopologyTypeSet initiallySelectedTypes) {
    final var button = new JToggleButton();

    button.setIcon(RessourceManager.getBigIcon(offIcon));
    button.setSelectedIcon(RessourceManager.getBigIcon(icon));

    button.setToolTipText(I18N.getText(toolTipKey));
    button.setSelected(initiallySelectedTypes.contains(type));
    this.add(button);
    modeButtons.put(type, button);
    button.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            if (zr != null) {
              var zone = zr.getZone();
              var mode = zone.getTopologyTypes();
              if (button.isSelected()) {
                mode = mode.with(type);
              } else {
                mode = mode.without(type);
              }

              setMode(mode);
            }
          }
        });
  }

  public void setMode(Zone.TopologyTypeSet topologyTypes) {
    AppPreferences.setTopologyTypes(topologyTypes);
    if (topologyTypes == null) {
      topologyTypes = AppPreferences.getTopologyTypes();
    }

    for (final var entry : modeButtons.entrySet()) {
      final var topologyType = entry.getKey();
      final var button = entry.getValue();

      button.setSelected(topologyTypes.contains(topologyType));
    }

    // Since setting selection also triggers change listeners, we need this work even early on.
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    // Check if there is a map. Fix #1605
    if (zr != null) {
      zr.getZone().setTopologyTypes(topologyTypes);
    }
  }
}
