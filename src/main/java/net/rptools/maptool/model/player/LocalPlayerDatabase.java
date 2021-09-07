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

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.CipherUtil.Key;
import net.rptools.maptool.util.cipher.PublicPrivateKeyStore;

/** This class provides the implementation for the "database" for the client local player. */
public class LocalPlayerDatabase implements PlayerDatabase {

  private LocalPlayer localPlayer;

  LocalPlayerDatabase() throws NoSuchAlgorithmException, InvalidKeySpecException {
    localPlayer = new LocalPlayer("None", Role.GM, ServerConfig.getPersonalServerGMPassword());
  }

  public synchronized void setLocalPlayer(LocalPlayer player) {
    localPlayer = player;
  }

  private synchronized LocalPlayer getLocalPlayer() {
    return localPlayer;
  }

  @Override
  public boolean playerExists(String playerName) {
    return getLocalPlayer().getName().equals(playerName);
  }

  @Override
  public Player getPlayer(String playerName) {
    LocalPlayer player = getLocalPlayer();
    if (player != null && player.getName().equals(playerName)) {
      return localPlayer;
    } else {
      return null;
    }
  }

  @Override
  public Optional<Key> getPlayerPassword(String playerName) {
    LocalPlayer player = (LocalPlayer) getPlayer(playerName);
    if (player != null && player.getName().equals(playerName)) {
      return Optional.of(player.getPassword());
    }
    return Optional.empty();
  }

  @Override
  public byte[] getPlayerPasswordSalt(String playerName) {
    LocalPlayer player = (LocalPlayer) getPlayer(playerName);
    if (player != null && player.getName().equals(playerName)) {
      return player.getPassword().salt();
    }
    return new byte[0];
  }

  @Override
  public Player getPlayerWithRole(String playerName, Role role)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    LocalPlayer player = (LocalPlayer) getPlayer(playerName);
    if (player != null && player.getName().equals(playerName)) {
      player.setRole(role);
    } else {
      player =
          new LocalPlayer(
              playerName,
              role,
              role == Role.GM
                  ? ServerConfig.getPersonalServerGMPassword()
                  : ServerConfig.getPersonalServerPlayerPassword());
    }
    setLocalPlayer(player);
    return player;
  }

  @Override
  public Optional<Key> getRolePassword(Role role) {
    return Optional.empty();
  }

  @Override
  public boolean supportsDisabling() {
    return false;
  }

  @Override
  public boolean supportsAsymmetricalKeys() {
    return false;
  }

  @Override
  public boolean supportsRolePasswords() {
    return false;
  }

  @Override
  public void disablePlayer(Player player, String reason) throws PasswordDatabaseException {
    throw new PasswordDatabaseException("msg.err.passFile.cantDisablePlayer");
  }

  @Override
  public boolean isDisabled(Player player) {
    return false;
  }

  @Override
  public String getDisabledReason(Player player) {
    return "";
  }

  @Override
  public AuthMethod getAuthMethod(Player player) {
    return AuthMethod.PASSWORD; // This will always be password authentication
  }

  @Override
  public CompletableFuture<CipherUtil> getPublicKey(Player player, MD5Key md5key) {
    return new PublicPrivateKeyStore().getKeys();
  }

  @Override
  public boolean isPlayerRegistered(String name)
      throws InterruptedException, InvocationTargetException {
    return localPlayer != null
        && localPlayer.getName() != null
        && localPlayer.getName().equals(name);
  }
}
