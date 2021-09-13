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

public class UseTerrainVblSelectionTool extends DefaultTool {
  private ImageIcon useRegularVBLIcon = new ImageIcon();
  private ImageIcon useTerrainVBLIcon = new ImageIcon();

  public UseTerrainVblSelectionTool() {
    try {
      useRegularVBLIcon =
          new ImageIcon(
              ImageUtil.getImage("net/rptools/maptool/client/image/tool/ignore-vbl-on-move.png"));
      useTerrainVBLIcon =
          new ImageIcon(
              ImageUtil.getImage("net/rptools/maptool/client/image/tool/use-vbl-on-move.png"));

      setIcon(useRegularVBLIcon);
      setSelectedIcon(useTerrainVBLIcon);
      setSelected(AppPreferences.getDrawTerrainVbl());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  private void setMode(boolean useTerrainVBL) {
    AppPreferences.setDrawTerrainVbl(useTerrainVBL);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    setMode(isSelected());
  }

  @Override
  public String getTooltip() {
    return "tools.use_terrain_selection.tooltip";
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
