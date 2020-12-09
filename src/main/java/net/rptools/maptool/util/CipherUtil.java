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
package net.rptools.maptool.util;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.maptool.server.MapToolServerConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CipherUtil {

  private static final CipherUtil instance = new CipherUtil();

  private static final String CIPHER_ALGORITHM = "AES";
  private static final String MESSAGE_DIGEST_ALGORITHM = "SHA3-256";

  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  public static CipherUtil getInstance() {
    return instance;
  }

  private final MessageDigest messageDigest;

  private CipherUtil() {
    try {
      messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
      Cipher.getInstance("AES");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      log.error(e);
      throw new IllegalStateException(e);
    }
  }

  public synchronized SecretKeySpec createSecretKeySpec(String key) {
    messageDigest.reset();
    byte[] digest = messageDigest.digest(key.getBytes());
    return new SecretKeySpec(digest, CIPHER_ALGORITHM);
  }

  public Cipher createDecrypter(String key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return createCipher(Cipher.DECRYPT_MODE, createSecretKeySpec(key));
  }

  public Cipher createEncrypter(String key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return createCipher(Cipher.ENCRYPT_MODE, createSecretKeySpec(key));
  }

  public Cipher createDecrypter(SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (!key.getAlgorithm().equals(CIPHER_ALGORITHM)) {
      throw new AssertionError(
          "Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.getAlgorithm());
    }
    return createCipher(Cipher.DECRYPT_MODE, key);
  }

  public Cipher createEncrypter(SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (!key.getAlgorithm().equals(CIPHER_ALGORITHM)) {
      throw new AssertionError(
          "Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.getAlgorithm());
    }
    return createCipher(Cipher.DECRYPT_MODE, key);
  }

  private Cipher createCipher(int encryptMode, SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(encryptMode, key);
    return cipher;
  }
}
