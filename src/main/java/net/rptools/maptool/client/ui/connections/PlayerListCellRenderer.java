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
package net.rptools.maptool.client.ui.connections;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;

public class PlayerListCellRenderer extends DefaultListCellRenderer {
  @Override
  public Component getListCellRendererComponent(
      JList<?> list, Object value, int index, boolean isSelected, boolean hasFocus) {
    if (value instanceof Player player) {
      // GMs can see everyone's zone, players can only see each other's.
      var showZone = MapTool.getPlayer().isGM() || !player.isGM();

      String text;
      if (showZone) {
        var zone =
            player.getZoneId() == null ? null : MapTool.getCampaign().getZone(player.getZoneId());
        text =
            I18N.getText(
                player.getLoaded()
                    ? "connections.playerIsInZone"
                    : "connections.playerIsLoadingZone",
                player.toString(),
                zone == null ? null : zone.getDisplayName());
      } else {
        text = player.toString();
      }
      return super.getListCellRendererComponent(list, text, index, isSelected, hasFocus);
    }

    return super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
  }
}
