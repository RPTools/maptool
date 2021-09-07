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
import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.server.ServerConfig;
import net.rptools.maptool.util.cipher.CipherUtil;

public class PersonalServerPlayerDatabase implements PlayerDatabase {

  private final LocalPlayer player;

  public PersonalServerPlayerDatabase() throws NoSuchAlgorithmException, InvalidKeySpecException {
    player =
        new LocalPlayer(
            AppPreferences.getDefaultUserName(),
            Role.GM,
            ServerConfig.getPersonalServerGMPassword());
  }

  @Override
  public boolean playerExists(String playerName) {
    return true; // Player always exists no matter what the name
  }

  @Override
  public Player getPlayer(String playerName)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    return player;
  }

  @Override
  public Optional<CipherUtil.Key> getPlayerPassword(String playerName) {
    return Optional.of(player.getPassword());
  }

  @Override
  public byte[] getPlayerPasswordSalt(String playerName) {
    return player.getPassword().salt();
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
  public AuthMethod getAuthMethod(Player player) {
    return AuthMethod.PASSWORD; // This will always be password authentication
  }

  @Override
  public CompletableFuture<CipherUtil> getPublicKey(Player player, MD5Key md5key)
      throws ExecutionException, InterruptedException {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public boolean isPlayerRegistered(String name)
      throws InterruptedException, InvocationTargetException {
    return player != null && player.getName() != null && player.getName().equals(name);
  }

  @Override
  public String getDisabledReason(Player player) {
    return "";
  }

  @Override
  public boolean isDisabled(Player player) {
    return false;
  }

  @Override
  public void disablePlayer(Player player, String reason) throws PasswordDatabaseException {
    throw new PasswordDatabaseException("msg.err.passFile.cantDisablePlayer");
  }

  @Override
  public Optional<CipherUtil.Key> getRolePassword(Player.Role role) {
    if (role == Role.GM) {
      return Optional.of(player.getPassword());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Player getPlayerWithRole(String playerName, Player.Role role)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    return player; // There is no non GM personal server player so just return the GM
  }
}
