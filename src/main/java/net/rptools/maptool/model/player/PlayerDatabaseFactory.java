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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import javax.crypto.NoSuchPaddingException;
import net.rptools.lib.cipher.CipherUtil;
import net.rptools.lib.cipher.PublicPrivateKeyStore;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;

public class PlayerDatabaseFactory {
  private enum PlayerDatabaseType {
    LOCAL_PLAYER,
    DEFAULT,
    PASSWORD_FILE
  }

  private static final Map<PlayerDatabaseType, PlayerDatabase> playerDatabaseMap =
      new ConcurrentHashMap<>();
  private static final PublicPrivateKeyStore keyStore = MapTool.getKeyStore();

  public static LocalPlayerDatabase getLocalPlayerDatabase(LocalPlayer player) {
    return new LocalPlayerDatabase(player, keyStore);
  }

  public static PersonalServerPlayerDatabase getPersonalServerPlayerDatabase(LocalPlayer player) {
    return new PersonalServerPlayerDatabase(player);
  }

  public static DefaultPlayerDatabase getDefaultPlayerDatabase(
      String playerPassword, String gmPassword) {
    try {
      return new DefaultPlayerDatabase(playerPassword, gmPassword);
    } catch (NoSuchAlgorithmException
        | InvalidKeySpecException
        | NoSuchPaddingException
        | InvalidKeyException e) {
      throw new IllegalStateException(e);
    }
  }

  public static PasswordFilePlayerDatabase getPasswordFilePlayerDatabase() {
    return (PasswordFilePlayerDatabase)
        getStashed(
            PlayerDatabaseType.PASSWORD_FILE,
            () -> {
              try {
                Path configRoot = AppUtil.getAppHome("config").toPath();
                File passwordFile = configRoot.resolve("passwords.json").toFile();
                File additionalFile = configRoot.resolve("passwords_add.json").toFile();

                CipherUtil.Key serverPublicPrivateKey;
                try {
                  serverPublicPrivateKey = keyStore.getKeys().get();
                } catch (InterruptedException | ExecutionException e) {
                  if (e.getCause() instanceof IOException) {
                    throw (IOException) e.getCause();
                  }
                  if (e.getCause() instanceof NoSuchAlgorithmException) {
                    throw (NoSuchAlgorithmException) e.getCause();
                  } else if (e.getCause() instanceof InvalidKeySpecException) {
                    throw (InvalidKeySpecException) e.getCause();
                  } else {
                    throw new IOException(e.getCause());
                  }
                }

                return new PasswordFilePlayerDatabase(
                    passwordFile, additionalFile, serverPublicPrivateKey);
              } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new IllegalStateException(e);
              }
            });
  }

  private static PlayerDatabase getStashed(
      PlayerDatabaseType databaseType, Supplier<PlayerDatabase> supplier) {
    return playerDatabaseMap.computeIfAbsent(databaseType, type -> supplier.get());
  }
}
