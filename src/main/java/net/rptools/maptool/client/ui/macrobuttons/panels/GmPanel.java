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

public class GmPanel extends AbstractMacroPanel {

  public GmPanel() {
    setPanelClass("GmPanel");
    addMouseListener(this);
    init();
  }

  private static GmPanel getGmPanel() {
    return MapTool.getFrame().getGmPanel();
  }

  private static List<MacroButtonProperties> getGmMacroButtonArray() {
    return MapTool.getCampaign().getGmMacroButtonPropertiesArray();
  }

  private void init() {
    if (MapTool.getPlayer() == null || MapTool.getPlayer().isGM())
      addArea(getGmMacroButtonArray(), "");
  }

  public void reset() {
    clear();
    init();
  }

  public static void deleteButtonGroup(String macroGroup) {
    AbstractButtonGroup.clearHotkeys(getGmPanel(), macroGroup);
    List<MacroButtonProperties> campProps = getGmMacroButtonArray();
    List<MacroButtonProperties> startingProps =
        new ArrayList<MacroButtonProperties>(getGmMacroButtonArray());
    campProps.clear();
    for (MacroButtonProperties nextProp : startingProps) {
      if (!macroGroup.equals(nextProp.getGroup())) {
        MapTool.getCampaign().saveGmMacroButtonProperty(nextProp);
      }
    }
    getGmPanel().reset();
  }

  public static void clearPanel() {
    AbstractMacroPanel.clearHotkeys(getGmPanel());
    getGmMacroButtonArray().clear();
    getGmPanel().reset();
  }
}
