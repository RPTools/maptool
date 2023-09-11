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
package net.rptools.maptool.model.player;

import com.google.common.eventbus.Subscribe;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.PlayerStatusChanged;
import net.rptools.maptool.client.events.ZoneLoaded;
import net.rptools.maptool.client.events.ZoneLoading;
import net.rptools.maptool.events.MapToolEventBus;

public class PlayerZoneListener {
  public PlayerZoneListener() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  public void OnZoneLoading(ZoneLoading event) {
    var player = MapTool.getPlayer();
    player.setLoaded(false);
    player.setZoneId(event.zone().getId());

    // To keep everything tidy we're also updating the player entry
    // in the player list since they are seperate entities
    var playerListPlayer =
        MapTool.getPlayerList().stream()
            .filter(x -> x.getName().equals(player.getName()))
            .findAny()
            .orElse(null);

    // On startup when we start to load the grassland zone the player list is still empty so we skip
    if (playerListPlayer != null) {
      playerListPlayer.setLoaded(false);
      playerListPlayer.setZoneId(event.zone().getId());
    }

    final var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.post(new PlayerStatusChanged(player));

    MapTool.serverCommand().updatePlayerStatus(player);
  }

  @Subscribe
  public void OnZoneLoaded(ZoneLoaded event) {
    var player = MapTool.getPlayer();
    player.setLoaded(true);
    player.setZoneId(event.zone().getId());

    // To keep everything tidy we're also updating the player entry
    // in the player list since they are seperate entities
    var playerListPlayer =
        MapTool.getPlayerList().stream()
            .filter(x -> x.getName().equals(player.getName()))
            .findAny()
            .orElse(null);

    // On startup when we load the grassland zone the player list is still empty so we skip this
    if (playerListPlayer != null) {
      playerListPlayer.setLoaded(true);
      playerListPlayer.setZoneId(event.zone().getId());
    }

    final var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.post(new PlayerStatusChanged(player));

    MapTool.serverCommand().updatePlayerStatus(player);
  }
}
