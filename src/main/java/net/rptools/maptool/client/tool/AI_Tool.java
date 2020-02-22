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
package net.rptools.maptool.client.tool;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.ImageIcon;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;

/** Controls the toolbar button which drives if AI is used or not to move tokens */
public class AI_Tool extends DefaultTool {

  public AI_Tool() {
    try {
      setIcon(
          new ImageIcon(
              ImageUtil.getImage("net/rptools/maptool/client/image/tool/ai-blue-off.png")));
      setSelectedIcon(
          new ImageIcon(
              ImageUtil.getImage("net/rptools/maptool/client/image/tool/ai-blue-green.png")));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    // Server policy is not available yet but that's ok, we have it saved as a preference which
    // is OK at this stage of initialization.
    setSelected(AppPreferences.isUsingAstarPathfinding());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    AppPreferences.setUseAstarPathfinding(isSelected());
    MapTool.getServerPolicy().setUseAstarPathfinding(isSelected());
    MapTool.updateServerPolicy();

    // Trigger AI_UseVblTool's isAvailable
    MapTool.getFrame().getToolbox().updateTools();
  }

  public void updateButtonState() {
    if (MapTool.getServerPolicy() != null) {
      setSelected(MapTool.getServerPolicy().isUsingAstarPathfinding());
    }
  }

  @Override
  public String getTooltip() {
    return "tools.ai_selector.tooltip";
  }

  @Override
  public String getInstructions() {
    return ""; // Not used, only displayed for currentTool
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  public boolean hasGroup() {
    return false;
  }
}
