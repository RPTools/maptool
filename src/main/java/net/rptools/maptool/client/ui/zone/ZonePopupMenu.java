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
package net.rptools.maptool.client.ui.zone;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;

public class ZonePopupMenu extends JPopupMenu {

  private Zone zone;

  public ZonePopupMenu(Zone zone) {
    super("Zone");

    this.zone = zone;

    Action action = null;
    if (zone.isVisible()) {
      action =
          new AbstractAction() {
            {
              putValue(NAME, "Hide from players");
            }

            public void actionPerformed(ActionEvent e) {
              ZonePopupMenu.this.zone.setVisible(false);
              MapTool.serverCommand().setZoneVisibility(ZonePopupMenu.this.zone.getId(), false);
              MapTool.getFrame().getZoneMiniMapPanel().flush();
              MapTool.getFrame().refresh();
            }
          };
    } else {
      action =
          new AbstractAction() {
            {
              putValue(NAME, "Show to players");
            }

            public void actionPerformed(ActionEvent e) {

              ZonePopupMenu.this.zone.setVisible(true);
              MapTool.serverCommand().setZoneVisibility(ZonePopupMenu.this.zone.getId(), true);
              MapTool.getFrame().getZoneMiniMapPanel().flush();
              MapTool.getFrame().refresh();
            }
          };
    }
    add(new JMenuItem(action));
  }
}
