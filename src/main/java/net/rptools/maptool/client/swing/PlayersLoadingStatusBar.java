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
package net.rptools.maptool.client.swing;

import com.google.common.eventbus.Subscribe;
import java.awt.Dimension;
import javax.swing.Icon;
import javax.swing.JLabel;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.PlayerConnected;
import net.rptools.maptool.client.events.PlayerDisconnected;
import net.rptools.maptool.client.events.PlayerStatusChanged;
import net.rptools.maptool.client.events.ServerStopped;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;

/** */
public class PlayersLoadingStatusBar extends JLabel {
  private static final Dimension minSize = new Dimension(60, 10);
  private static Icon checkmarkIcon;
  private static Icon loadingIcon;

  static {
    checkmarkIcon = RessourceManager.getSmallIcon(Icons.STATUSBAR_PLAYERS_DONE_LOADING);
    loadingIcon = RessourceManager.getSmallIcon(Icons.STATUSBAR_PLAYERS_LOADING);
  }

  public PlayersLoadingStatusBar() {
    refreshCount();
    new MapToolEventBus().getMainEventBus().register(this);
  }

  private void refreshCount() {
    var players = MapTool.getPlayerList();
    var total = players.size();
    var loaded = players.stream().filter(x -> x.getLoaded()).count();

    var sb =
        new StringBuilder(I18N.getText("ConnectionStatusPanel.playersLoadedZone", loaded, total));

    var self = MapTool.getPlayer();

    for (Player player : players) {
      // The Player in the list is a seperate entity to the one from MapTool.getPlayer()
      // So it doesn't have the correct status data.
      if (player.getName().equals(self.getName())) {
        player = self;
      }
      var zone =
          player.getZoneId() == null ? null : MapTool.getCampaign().getZone(player.getZoneId());

      var text =
          I18N.getText(
              player.getLoaded() ? "connections.playerIsInZone" : "connections.playerIsLoadingZone",
              player.toString(),
              zone == null ? null : zone.getDisplayName());
      sb.append("\n");
      sb.append(text);
    }

    String text = loaded + "/" + total;

    if (total == loaded) {
      setIcon(checkmarkIcon);
    } else {
      setIcon(loadingIcon);
    }
    this.setText(text);
    this.setToolTipText(sb.toString());
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getMinimumSize()
   */
  public Dimension getMinimumSize() {
    return minSize;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  @Subscribe
  private void onPlayerConnected(PlayerConnected event) {
    refreshCount();
  }

  @Subscribe
  private void onPlayerStatusChanged(PlayerStatusChanged event) {
    refreshCount();
  }

  @Subscribe
  private void onPlayerDisconnected(PlayerDisconnected event) {
    refreshCount();
  }

  @Subscribe
  private void onServerStopped(ServerStopped event) {
    refreshCount();
  }
}
