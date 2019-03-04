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
package net.rptools.maptool.client.ui.macrobuttons.panels;

import java.util.ArrayList;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.AbstractButtonGroup;
import net.rptools.maptool.model.MacroButtonProperties;

public class CampaignPanel extends AbstractMacroPanel {

  public CampaignPanel() {
    setPanelClass("CampaignPanel");
    addMouseListener(this);
    init();
  }

  private void init() {
    if (MapTool.getPlayer() == null
        || MapTool.getPlayer().isGM()
        || MapTool.getServerPolicy().playersReceiveCampaignMacros()) {
      addArea(MapTool.getCampaign().getMacroButtonPropertiesArray(), "");
    }
  }

  public void reset() {
    clear();
    init();
  }

  public static void deleteButtonGroup(String macroGroup) {
    AbstractButtonGroup.clearHotkeys(MapTool.getFrame().getCampaignPanel(), macroGroup);
    List<MacroButtonProperties> campProps = MapTool.getCampaign().getMacroButtonPropertiesArray();
    List<MacroButtonProperties> startingProps =
        new ArrayList<MacroButtonProperties>(MapTool.getCampaign().getMacroButtonPropertiesArray());
    campProps.clear();
    for (MacroButtonProperties nextProp : startingProps) {
      if (!macroGroup.equals(nextProp.getGroup())) {
        MapTool.getCampaign().saveMacroButtonProperty(nextProp);
      }
    }
    MapTool.getFrame().getCampaignPanel().reset();
  }

  public static void clearPanel() {
    AbstractMacroPanel.clearHotkeys(MapTool.getFrame().getCampaignPanel());
    MapTool.getCampaign().getMacroButtonPropertiesArray().clear();
    MapTool.getFrame().getCampaignPanel().reset();
  }
}
