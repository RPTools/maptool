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
package net.rptools.maptool.client.ui.macrobuttons.buttons;

import java.io.Serializable;
import net.rptools.maptool.model.MacroButtonProperties;

public class TransferData implements Serializable {

  public int index = 0;
  public String command = "";
  public String colorKey = "";
  public String hotKey = "";
  public String label = "";
  public String group = "";
  public String sortby = "";
  public boolean autoExecute = true;
  public boolean includeLabel = false;
  public boolean applyToTokens = true;
  public String fontColorKey = "";
  public String fontSize = "";
  public String minWidth = "";
  public String maxWidth = "";
  public String panelClass = "";
  public String toolTip = "";

  public TransferData(MacroButton button) {
    MacroButtonProperties prop = button.getProperties();
    this.index = prop.getIndex();
    this.label = prop.getLabel();
    this.command = prop.getCommand();
    this.colorKey = prop.getColorKey();
    this.hotKey = prop.getHotKey();
    this.group = prop.getGroup();
    this.sortby = prop.getSortby();
    this.autoExecute = prop.getAutoExecute();
    this.includeLabel = prop.getIncludeLabel();
    this.applyToTokens = prop.getApplyToTokens();
    this.panelClass = button.getPanelClass();
    this.fontColorKey = prop.getFontColorKey();
    this.fontSize = prop.getFontSize();
    this.minWidth = prop.getMinWidth();
    this.maxWidth = prop.getMaxWidth();
    this.toolTip = prop.getToolTip();
  }
}
