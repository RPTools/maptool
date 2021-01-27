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

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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

  /** The length of the generated key. */
  public static final int DEFAULT_GENERATED_KEY_LEN = 128;

  /** The default number of bytes to be used for the salt if it is not spcified. */
  public static final int DEFAULT_SALT_SIZE = DEFAULT_GENERATED_KEY_LEN;


  /** The number of iterations used for key generation. */
  private static final int KEY_ITERATION_KEY_COUNT = 2000;

  /** Key generation algorithm. */
  private static final String KEY_GENERATION_ALGORITHM = "PBKDF2WithHmacSHA1";


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
   * @param key the key used to encrypt/decrypt
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
  public Cipher createDecryptor(String key)
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
  public Cipher createDecryptor(SecretKeySpec key)
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

  public byte[] createSalt(int size) {
    SecureRandom secureRandom = new SecureRandom();
    byte salt[] = new byte[size];
    secureRandom.nextBytes(salt);

    return salt;
  }

  public byte[] createSalt() {
    return createSalt(DEFAULT_SALT_SIZE);
  }



  public Cipher createEncryptor(String key, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, createSecretKeySpec(key, salt));

    return cipher;
  }


  public Cipher createDecryptor(String key, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, createSecretKeySpec(key, salt));

    return cipher;
  }

  public SecretKeySpec createSecretKeySpec(String key, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, KEY_ITERATION_KEY_COUNT, 128);
    SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_GENERATION_ALGORITHM);

    return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), CIPHER_ALGORITHM);
  }

}
