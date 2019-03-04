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

import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.buttongroups.AbstractButtonGroup;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButtonPrefs;
import net.rptools.maptool.model.MacroButtonProperties;

public class GlobalPanel extends AbstractMacroPanel {
  public GlobalPanel() {
    super();
    setPanelClass("GlobalPanel");
    addMouseListener(this);
    init();
  }

  private void init() {
    List<MacroButtonProperties> properties = MacroButtonPrefs.getButtonProperties();
    addArea(properties, "");
  }

  public void reset() {
    clear();
    init();
  }

  public static void deleteButtonGroup(String macroGroup) {
    AbstractButtonGroup.clearHotkeys(MapTool.getFrame().getGlobalPanel(), macroGroup);
    for (MacroButtonProperties nextProp : MacroButtonPrefs.getButtonProperties()) {
      if (macroGroup.equals(nextProp.getGroup())) {
        MacroButtonPrefs.delete(nextProp);
      }
    }
    MapTool.getFrame().getGlobalPanel().reset();
  }

  public static void clearPanel() {
    MacroButtonPrefs.deletePanel();
    AbstractMacroPanel.clearHotkeys(MapTool.getFrame().getGlobalPanel());
    MapTool.getFrame().getGlobalPanel().reset();
  }
}
