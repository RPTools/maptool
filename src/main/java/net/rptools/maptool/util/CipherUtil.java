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

/** Utility class used for creating objects to encipher/decipher text. */
public class CipherUtil {

  /** The singleton instance of {@code CipherUtil}. */
  private static final CipherUtil instance = new CipherUtil();

  /** The algorithm to use for encoding / decoding. */
  private static final String CIPHER_ALGORITHM = "AES";

  /** The algorithm used for turning the password into a 256 bit key. */
  private static final String MESSAGE_DIGEST_ALGORITHM = "SHA3-256";

  /** Logger used for log messages. */
  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);

  /**
   * Returns the singleton instance of {@code CipherUtil}.
   *
   * @return the singleton instance of {@code CipherUtil}.
   */
  public static CipherUtil getInstance() {
    return instance;
  }

  /** {@link MessageDigest} used for generating a 256 bit key from the password. */
  private final MessageDigest messageDigest;

  /** Creates a new {@code CipherUtil} instance. */
  private CipherUtil() {
    try {
      messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
      Cipher.getInstance("AES");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      log.error(e);
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns a {@link SecretKeySpec} from the supplied {@link String} key.
   *
   * @param key the string containing the key.
   * @return the {@link SecretKeySpec}.
   */
  public synchronized SecretKeySpec createSecretKeySpec(String key) {
    messageDigest.reset();
    byte[] digest = messageDigest.digest(key.getBytes());
    return new SecretKeySpec(digest, CIPHER_ALGORITHM);
  }

  /**
   * Returns a {@link Cipher} that can be used to decipher encoded values.
   *
   * @param key the key used for deciphering.
   * @return a {@link Cipher} that can be used for deciphering encoded values.
   * @throws NoSuchPaddingException if the requested padding algorithm is not available.
   * @throws NoSuchAlgorithmException if the requested encryption algorithm is not available.
   * @throws InvalidKeyException if there are problems with the supplied key.
   */
  public Cipher createDecrypter(String key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return createCipher(Cipher.DECRYPT_MODE, createSecretKeySpec(key));
  }

  /**
   * Returns a {@link Cipher} that can be used to encipher encoded values.
   *
   * @param key the key used for encipher.
   * @return a {@link Cipher} that can be used for deciphering encoded values.
   * @throws NoSuchPaddingException if the requested padding algorithm is not available.
   * @throws NoSuchAlgorithmException if the requested encryption algorithm is not available.
   * @throws InvalidKeyException if there are problems with the supplied key.
   */
  public Cipher createEncrypter(String key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return createCipher(Cipher.ENCRYPT_MODE, createSecretKeySpec(key));
  }

  /**
   * Returns a {@link Cipher} that can be used to decipher encoded values.
   *
   * @param key the key used for deciphering.
   * @return a {@link Cipher} that can be used for deciphering encoded values.
   * @throws NoSuchPaddingException if the requested padding algorithm is not available.
   * @throws NoSuchAlgorithmException if the requested encryption algorithm is not available.
   * @throws InvalidKeyException if there are problems with the supplied key.
   */
  public Cipher createDecrypter(SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (!key.getAlgorithm().equals(CIPHER_ALGORITHM)) {
      throw new AssertionError(
          "Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.getAlgorithm());
    }
    return createCipher(Cipher.DECRYPT_MODE, key);
  }

  /**
   * Returns a {@link Cipher} that can be used to encipher encoded values.
   *
   * @param key the key used for encipher.
   * @return a {@link Cipher} that can be used for deciphering encoded values.
   * @throws NoSuchPaddingException if the requested padding algorithm is not available.
   * @throws NoSuchAlgorithmException if the requested encryption algorithm is not available.
   * @throws InvalidKeyException if there are problems with the supplied key.
   */
  public Cipher createEncrypter(SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (!key.getAlgorithm().equals(CIPHER_ALGORITHM)) {
      throw new AssertionError(
          "Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.getAlgorithm());
    }
    return createCipher(Cipher.ENCRYPT_MODE, key);
  }

  /**
   * Returns a {@link Cipher} that can be used to encipher / decipher encoded values.
   *
   * @param encryptMode the mode for the {@link Cipher}.
   * @param key the key used for encipher.
   * @return a {@link Cipher} that can be used for enciphering / deciphering encoded values.
   * @throws NoSuchPaddingException if the requested padding algorithm is not available.
   * @throws NoSuchAlgorithmException if the requested encryption algorithm is not available.
   * @throws InvalidKeyException if there are problems with the supplied key.
   */
  private Cipher createCipher(int encryptMode, SecretKeySpec key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(encryptMode, key);
    return cipher;
  }
}
