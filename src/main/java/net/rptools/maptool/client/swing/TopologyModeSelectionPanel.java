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

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;

public class TopologyModeSelectionPanel extends JToolBar {
  /** The instance. Used to update the button when the ZoneRenderer is changed. */
  private static TopologyModeSelectionPanel instance;

  public static TopologyModeSelectionPanel getInstance() {
    return instance;
  }

  private final Map<Zone.TopologyMode, JToggleButton> modeButtons;
  private final ButtonGroup buttonGroup;

  public TopologyModeSelectionPanel() {
    instance = this;

    setFloatable(false);
    setRollover(true);
    setBorder(null);
    setBorderPainted(false);

    modeButtons = new EnumMap<>(Zone.TopologyMode.class);
    buttonGroup = new ButtonGroup();

    try {
      var initialMode = AppPreferences.getTopologyDrawingMode();
      createAndAddModeButton(
          Zone.TopologyMode.VBL,
          "net/rptools/maptool/client/image/tool/vbl-only.png",
          "tools.topology_mode_selection.vbl.tooltip",
          initialMode);
      createAndAddModeButton(
          Zone.TopologyMode.TERRAIN_VBL,
          "net/rptools/maptool/client/image/tool/terrain-vbl-only.png",
          "tools.topology_mode_selection.terrain_vbl.tooltip",
          initialMode);
      createAndAddModeButton(
          Zone.TopologyMode.MBL,
          "net/rptools/maptool/client/image/tool/mbl-only.png",
          "tools.topology_mode_selection.mbl.tooltip",
          initialMode);
      createAndAddModeButton(
          Zone.TopologyMode.COMBINED,
          "net/rptools/maptool/client/image/tool/mbl-vbl-on.png",
          "tools.topology_mode_selection.combined_vbl_mbl.tooltip",
          initialMode);
      createAndAddModeButton(
          Zone.TopologyMode.COMBINED_TERRAIN_VBL,
          "net/rptools/maptool/client/image/tool/mbl-terrain-vbl-on.png",
          "tools.topology_mode_selection.combined_terrain_vbl_mbl.tooltip",
          initialMode);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    this.add(Box.createHorizontalStrut(5));
  }

  private void createAndAddModeButton(
      Zone.TopologyMode mode, String imageFile, String toolTipKey, Zone.TopologyMode initialMode)
      throws IOException {
    final var button = new JToggleButton(new ImageIcon(ImageUtil.getImage(imageFile)));
    button.setToolTipText(I18N.getText(toolTipKey));
    button.setSelected(mode == initialMode);
    buttonGroup.add(button);
    this.add(button);
    modeButtons.put(mode, button);
    button.addChangeListener(
        new ChangeListener() {
          @Override
          public void stateChanged(ChangeEvent e) {
            if (button.isSelected()) {
              setMode(mode);
            }
          }
        });
  }

  public void setMode(Zone.TopologyMode topologyMode) {
    AppPreferences.setTopologyDrawingMode(topologyMode);
    if (topologyMode == null) {
      topologyMode = AppPreferences.getTopologyDrawingMode();
    }

    this.modeButtons.get(topologyMode).setSelected(true);

    // Since setting selection also triggers change listeners, we need this work even early on.
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    // Check if there is a map. Fix #1605
    if (zr != null) {
      zr.getZone().setTopologyMode(topologyMode);
    }
  }
}
