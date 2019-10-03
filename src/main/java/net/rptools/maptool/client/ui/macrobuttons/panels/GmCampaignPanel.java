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

public class GmCampaignPanel extends AbstractMacroPanel {

  public GmCampaignPanel() {
    setPanelClass("GmCampaignPanel");
    addMouseListener(this);
    init();
  }

  private static GmCampaignPanel getGmPanel() {
    return MapTool.getFrame().getGmCampaignPanel();
  }

  private static List<MacroButtonProperties> getMacroGmButtonArray() {
    return MapTool.getCampaign().getMacroGmButtonPropertiesArray();
  }

  private void init() {
    if (MapTool.getPlayer() == null || MapTool.getPlayer().isGM())
      addArea(getMacroGmButtonArray(), "");
  }

  public void reset() {
    clear();
    init();
  }

  public static void deleteButtonGroup(String macroGroup) {
    AbstractButtonGroup.clearHotkeys(getGmPanel(), macroGroup);
    List<MacroButtonProperties> campProps = getMacroGmButtonArray();
    List<MacroButtonProperties> startingProps =
        new ArrayList<MacroButtonProperties>(getMacroGmButtonArray());
    campProps.clear();
    for (MacroButtonProperties nextProp : startingProps) {
      if (!macroGroup.equals(nextProp.getGroup())) {
        MapTool.getCampaign().saveMacroGmButtonProperty(nextProp);
      }
    }
    getGmPanel().reset();
  }

  public static void clearPanel() {
    AbstractMacroPanel.clearHotkeys(getGmPanel());
    getMacroGmButtonArray().clear();
    getGmPanel().reset();
  }
}
