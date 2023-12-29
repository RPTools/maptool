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
package net.rptools.maptool.server;

import net.rptools.maptool.model.player.LocalPlayer;
import net.rptools.maptool.model.player.LocalPlayerDatabase;
import net.rptools.maptool.model.player.PlayerDatabaseFactory;

public class PersonalServer implements IMapToolServer {
  private final LocalPlayer localPlayer;
  private final LocalPlayerDatabase playerDatabase;

  public PersonalServer(LocalPlayer player) {
    localPlayer = player;
    playerDatabase = PlayerDatabaseFactory.getLocalPlayerDatabase(player);
  }

  public LocalPlayer getLocalPlayer() {
    return localPlayer;
  }

  @Override
  public boolean isPersonalServer() {
    return true;
  }

  @Override
  public boolean isServerRegistered() {
    return false;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public int getPort() {
    return -1;
  }

  @Override
  public void stop() {}

  @Override
  public LocalPlayerDatabase getPlayerDatabase() {
    return playerDatabase;
  }
}
