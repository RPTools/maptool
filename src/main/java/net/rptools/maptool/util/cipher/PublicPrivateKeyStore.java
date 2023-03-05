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
package net.rptools.maptool.util.cipher;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.client.AppUtil;

public class PublicPrivateKeyStore {

  private static final File PUBLIC_KEY_FILE =
      AppUtil.getAppHome("config").toPath().resolve("public.key").toFile();
  private static final File PRIVATE_KEY_FILE =
      AppUtil.getAppHome("config").toPath().resolve("private.key").toFile();

  /**
   * Returns the public and private keys for this client. If none exists it will attempt to create
   * them and save them to the key files.
   *
   * @return the keys.
   */
  public CompletableFuture<CipherUtil.Key> getKeys() {

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            if (!PUBLIC_KEY_FILE.exists() || !PRIVATE_KEY_FILE.exists()) {
              KeyPair keyPair = null;
              keyPair = CipherUtil.generateKeyPair();
              CipherUtil.writeKeyPair(keyPair, PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
            }
            return CipherUtil.fromPublicPrivatePair(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
          } catch (NoSuchAlgorithmException
              | IOException
              | InvalidAlgorithmParameterException
              | InvalidKeySpecException
              | NoSuchPaddingException
              | InvalidKeyException e) {
            throw new CompletionException(e);
          }
        });
  }

  /**
   * Regenerates and returns a new public / private key pair. This will also save the new keys to
   * the key files.
   *
   * @return the newly generated keys.
   */
  public CompletableFuture<CipherUtil.Key> regenerateKeys() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            KeyPair keyPair = CipherUtil.generateKeyPair();
            CipherUtil.writeKeyPair(keyPair, PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);

            return CipherUtil.fromPublicPrivatePair(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
          } catch (IOException
              | NoSuchAlgorithmException
              | InvalidAlgorithmParameterException
              | InvalidKeySpecException
              | NoSuchPaddingException
              | InvalidKeyException e) {
            throw new CompletionException(e);
          }
        });
  }
}
