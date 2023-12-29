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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.PublicPrivateKeyStore;

/** This class provides the implementation for the "database" for the client local player. */
public class LocalPlayerDatabase implements PlayerDatabase {

  private final LocalPlayer localPlayer;
  private final LoggedInPlayers loggedInPlayers = new LoggedInPlayers();

  public LocalPlayerDatabase(LocalPlayer player) {
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
  public boolean isBlocked(Player player) {
    return false;
  }

  @Override
  public String getBlockedReason(Player player) {
    return "";
  }

  @Override
  public Set<Player> getOnlinePlayers() {
    return new HashSet<>(loggedInPlayers.getPlayers());
  }

  @Override
  public AuthMethod getAuthMethod(Player player) {
    return AuthMethod.PASSWORD; // This will always be password authentication
  }

  @Override
  public CompletableFuture<CipherUtil.Key> getPublicKey(Player player, MD5Key md5key) {
    return new PublicPrivateKeyStore().getKeys();
  }

  @Override
  public Set<String> getEncodedPublicKeys(String name) {
    return Set.of();
  }

  @Override
  public CompletableFuture<Boolean> hasPublicKey(Player player, MD5Key md5key) {
    return new PublicPrivateKeyStore()
        .getKeys()
        .thenApply(k -> CipherUtil.publicKeyMD5(k.publicKey()).equals(md5key));
  }

  @Override
  public boolean isPlayerRegistered(String name)
      throws InterruptedException, InvocationTargetException {
    return localPlayer != null
        && localPlayer.getName() != null
        && localPlayer.getName().equals(name);
  }

  @Override
  public void playerSignedIn(Player player) {
    loggedInPlayers.playerSignedIn(player);
  }

  @Override
  public void playerSignedOut(Player player) {
    loggedInPlayers.playerSignedOut(player);
  }

  @Override
  public boolean isPlayerConnected(String name) {
    return loggedInPlayers.isLoggedIn(name);
  }
}
