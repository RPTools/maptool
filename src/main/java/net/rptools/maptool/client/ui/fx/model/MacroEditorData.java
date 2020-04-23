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
package net.rptools.maptool.client.ui.fx.model;

import javafx.scene.control.Label;

// Replace me with a record or data class please...
public class MacroEditorData {

  private String label;
  private String macroGroup;
  private String command;
  private String mapName;

  public MacroEditorData() {
    label = "";
    macroGroup = "";
    command = "";
    mapName = "";
  }

  public MacroEditorData(String label, String macroGroup, String command, String mapName) {
    this.label = label;
    this.macroGroup = macroGroup;
    this.command = command;
    this.mapName = mapName;
  }

  //  private MacroButtonProperties macroButtonProperties;
  //  private String displayName;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getMacroGroup() {
    return macroGroup;
  }

  public void setMacroGroup(String macroGroup) {
    this.macroGroup = macroGroup;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getMapName() {
    return mapName;
  }

  public void setMapName(String mapName) {
    this.mapName = mapName;
  }

  public Label getFxLabel() {
    return new Label(label);
  }
}
