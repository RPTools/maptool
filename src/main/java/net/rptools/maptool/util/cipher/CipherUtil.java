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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.server.MapToolServerConnection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility class used for creating objects to encipher/decipher text. */
public class CipherUtil {

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

  /** Asynchronous Key Algorithm */
  private static final String ASYNC_KEY_ALGORITHM = "RSA";


  private static final String PUBLIC_KEY_FIRST_LINE = "====== Begin Public Key ======";
  private static final String PUBLIC_KEY_LAST_LINE = "====== End Public Key ======";


  private final Key key;
  private final Cipher encryptionCipher;
  private final Cipher decryptionCipher;;

  /** {@link MessageDigest} used for generating a 256 bit key from the password. */
  private final MessageDigest messageDigest;

  public static record Key(
      SecretKeySpec secretKeySpec,
      byte[] salt,
      PublicKey publicKey,
      PrivateKey privateKey,
      boolean asymmetric) {

    public Key(SecretKeySpec secretKeySpec, byte[] salt) {
      this(secretKeySpec, salt, null, null, false);
    }

    public Key(PublicKey publicKey, PrivateKey privateKey) {
      this(null, null, publicKey, privateKey, true);
    }
  };


  private CipherUtil(Key keyToUse)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    messageDigest = MessageDigest.getInstance(MESSAGE_DIGEST_ALGORITHM);
    key = keyToUse;
    encryptionCipher = createEncryptor(key);
    if (!key.asymmetric() || key.privateKey() != null) {
      decryptionCipher = createDecryptor(key);
    } else {
      decryptionCipher = null;
    }
  }

  public static CipherUtil fromPublicPrivatePair(File publicKeyFile, File privateKeyFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    KeyPair keyPair = readKeyPair(publicKeyFile, privateKeyFile);
    return CipherUtil.fromPublicPrivatePair(keyPair.getPublic(), keyPair.getPrivate());
  }

  public static CipherUtil fromPublicPrivatePair(PublicKey publicKey, PrivateKey privateKey)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return new CipherUtil(new Key(publicKey, privateKey));
  }


  public static CipherUtil fromPublicKeyString(String pk)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    return new CipherUtil(new Key(CipherUtil.decodePublicKeyString(pk), null));
  }

  public static CipherUtil fromSharedKey(String pass, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    Key key = createKey(pass, salt);
    return new CipherUtil(key);
  }

  public static CipherUtil fromSharedKeyNewSalt(String pass)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
    byte[] salt = createSalt();
    return fromSharedKey(pass, salt);
  }
  
  public static CipherUtil fromKey(Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return new CipherUtil(key);
  }

  public static CipherUtil fromSecretKeySpec(SecretKeySpec keySpec)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Key keyTo =  new Key(keySpec, createSalt());

    return new CipherUtil(keyTo);
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
  public static Cipher createDecryptor(Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (key.asymmetric()) {
      if (!key.privateKey.getAlgorithm().equals(ASYNC_KEY_ALGORITHM)) {
        throw new AssertionError(
            "Expected Algorithm " + ASYNC_KEY_ALGORITHM + " got " + key.privateKey.getAlgorithm());
      }
    } else {
      if (!key.secretKeySpec.getAlgorithm().equals(CIPHER_ALGORITHM)) {
        throw new AssertionError(
            "Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.secretKeySpec.getAlgorithm());
      }
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
  public static Cipher createEncryptor(Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (key.asymmetric()) {
      if (!key.publicKey.getAlgorithm().equals(ASYNC_KEY_ALGORITHM)) {
        throw new AssertionError(
            "Expected Algorithm " + ASYNC_KEY_ALGORITHM + " got " + key.publicKey.getAlgorithm());
      }
    } else {
      if (!key.secretKeySpec.getAlgorithm().equals(CIPHER_ALGORITHM)) {
        throw new AssertionError(
            "Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.secretKeySpec.getAlgorithm());
      }
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
  private static Cipher createCipher(int encryptMode, Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    if (key.asymmetric()) {
      Cipher cipher = Cipher.getInstance(ASYNC_KEY_ALGORITHM);
      cipher.init(encryptMode, encryptMode == Cipher.ENCRYPT_MODE ? key.publicKey() :
          key.privateKey());
      return cipher;
    } else {
      Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
      cipher.init(encryptMode, key.secretKeySpec);
      return cipher;
    }
  }

  private Cipher createPublicKeyCipher(PublicKey key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(ASYNC_KEY_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);

    return cipher;
  }

  private Cipher createPrivateKeyCipher(PrivateKey key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    Cipher cipher = Cipher.getInstance(ASYNC_KEY_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);

    return cipher;
  }

  public static byte[] createSalt(int size) {
    SecureRandom secureRandom = new SecureRandom();
    byte salt[] = new byte[size];
    secureRandom.nextBytes(salt);

    return salt;
  }

  public static byte[] createSalt() {
    return createSalt(DEFAULT_SALT_SIZE);
  }

  public Cipher createEncryptor(String key, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
          InvalidKeyException {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, createKey(key, salt).secretKeySpec);

    return cipher;
  }

  public Cipher createDecryptor(String key, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException,
          InvalidKeyException {
    Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, createKey(key, salt).secretKeySpec);

    return cipher;
  }

  public static Key createKey(String key, byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, KEY_ITERATION_KEY_COUNT, 128);
    SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_GENERATION_ALGORITHM);

    return new Key(new SecretKeySpec(factory.generateSecret(spec).getEncoded(), CIPHER_ALGORITHM), salt);
  }

  public Key createKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return createKey(key, createSalt());
  }

  public static byte[] generateMacAndSalt(String password) throws IOException {
    byte[] salt = createSalt();
    return generateMacWithSalt(password, salt);
  }

  public static byte[] generateMacWithSalt(String password, byte[] salt) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    Key key;
    try {
      key = createKey(password, salt);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IOException(e);
    }

    byte[] mac = key.secretKeySpec.getEncoded();

    dataOutputStream.writeInt(key.salt.length);
    dataOutputStream.write(key.salt);
    dataOutputStream.writeInt(mac.length);
    dataOutputStream.write(mac);
    dataOutputStream.flush();
    return byteArrayOutputStream.toByteArray();
  }

  public static byte[] readMac(DataInputStream in) throws IOException {

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

    int saltLen = in.readInt();
    byte[] salt = in.readNBytes(saltLen);

    int macLen = in.readInt();
    byte[] mac = in.readNBytes(macLen);

    dataOutputStream.writeInt(saltLen);
    dataOutputStream.write(salt);
    dataOutputStream.writeInt(macLen);
    dataOutputStream.write(mac);

    dataOutputStream.flush();

    return byteArrayOutputStream.toByteArray();
  }

  public static boolean validateMac(byte[] macWithSalt, String password) {
    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(macWithSalt));
    try {
      int saltLen = dataInputStream.readInt();
      byte[] salt = dataInputStream.readNBytes(saltLen);
      if (saltLen != salt.length) {
        log.warn(I18N.getText("Handshake.msg.failedLoginDecode"));
        return false;
      }
      int macLen = dataInputStream.readInt();
      byte[] mac = dataInputStream.readNBytes(macLen);
      if (macLen != mac.length) {
        log.warn(I18N.getText("Handshake.msg.failedLoginDecode"));
        return false;
      }
      byte[] compareTo = generateMacWithSalt(password, salt);

      return Arrays.compare(macWithSalt, compareTo) == 0;
    } catch (IOException e) {
      log.warn(I18N.getText("Handshake.msg.failedLoginDecode"), e);
      return false;
    }
  }

  public static byte[] getMacSalt(byte[] macWithSalt) {
    DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(macWithSalt));
    try {
      int saltLen = dataInputStream.readInt();
      byte[] salt = dataInputStream.readNBytes(saltLen);
      return salt;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static String encodeBase64(SecretKey key) {
    return Base64.getEncoder().withoutPadding().encodeToString(key.getEncoded());
  }

  public static SecretKeySpec decodeBase64(String encoded) {
    return new SecretKeySpec(Base64.getDecoder().decode(encoded), CIPHER_ALGORITHM);
  }

  public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
    SecureRandom secureRandom = new SecureRandom();

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ASYNC_KEY_ALGORITHM);
    keyPairGenerator.initialize(2048, secureRandom);
    return keyPairGenerator.generateKeyPair();
  }

  static void writeKeyPair(KeyPair keyPair, File publicFile, File privateFile)
      throws IOException {
    publicFile.getParentFile().mkdirs();
    privateFile.getParentFile().mkdirs();
    try (FileOutputStream fos = new FileOutputStream(publicFile)) {
      fos.write(encodedPublicKeyText(keyPair.getPublic()).getBytes(StandardCharsets.UTF_8));
    }

    try (FileOutputStream fos = new FileOutputStream(privateFile)) {
      fos.write(keyPair.getPrivate().getEncoded());
    }
  }

  public String getEncodedPublicKeyText() {
    return encodedPublicKeyText(key.publicKey());
  }

  public Key getKey() {
    return key;
  }

  public Cipher getEncryptionCipher() {
    return encryptionCipher;
  }

  public Cipher getDecryptionCipher() {
    return decryptionCipher;
  }

  private static String encodedPublicKeyText(PublicKey publicKey) {
    byte[] bytes = publicKey.getEncoded();
    String b64 = Base64.getEncoder().encodeToString(bytes);
    return b64.replaceAll("(\\S{80})", "$1\n")
        .replaceFirst("^", PUBLIC_KEY_FIRST_LINE + "\n")
        .replaceFirst("$", "\n" + PUBLIC_KEY_LAST_LINE + "\n");
  }

  private static byte[] decodePublicKeyText(String pks) {
    byte[] bytes = pks.replaceFirst(PUBLIC_KEY_FIRST_LINE, "")
        .replaceFirst(PUBLIC_KEY_LAST_LINE, "")
        .replaceAll("\\s", "")
        .getBytes(StandardCharsets.UTF_8);
    return Base64.getDecoder().decode(bytes);
  }

  private static KeyPair readKeyPair(File publicFile, File privateFile)
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] privateKeyBytes = Files.readAllBytes(privateFile.toPath());
    String publicKey = String.join("\n", Files.readAllLines(publicFile.toPath()));
        decodePublicKeyText(new String(Files.readAllBytes(publicFile.toPath())));
    return generateKeyPair(publicKey, privateKeyBytes);
  }

  private static KeyPair generateKeyPair(String publicKey, byte[] privateKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKey);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return new KeyPair(
        decodePublicKeyString(publicKey),
        keyFactory.generatePrivate(privateSpec)
    );
  }

  public static PublicKey decodePublicKeyString(String pub)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] publicKeyBytes = decodePublicKeyText(pub);
    X509EncodedKeySpec publicSpec = new X509EncodedKeySpec (publicKeyBytes);

    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(publicSpec);
  }

  public PublicKey decodePublicKeyBytes(byte[] bytes)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    PKCS8EncodedKeySpec publicSpec = new PKCS8EncodedKeySpec(bytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
    return keyFactory.generatePublic(publicSpec);
  }

  public byte[] encode(byte[] toEncode) throws IllegalBlockSizeException, BadPaddingException {
    return encryptionCipher.doFinal(toEncode);
  }

  public byte[] decode(byte[] toDecode) throws IllegalBlockSizeException, BadPaddingException {
    return decryptionCipher.doFinal(toDecode);
  }

  public static String[] splitPublicKeys(String concatKeys) {
    List<String> publicKeys = new ArrayList<>();

    StringBuilder sb = new StringBuilder();
    for (String line : concatKeys.split("\n")) {
      String strippedLine = line.replaceAll("\\s", "");
      if (strippedLine.length() > 0 && !strippedLine.startsWith("#")) {
        sb.append(line);
        if (line.equals(PUBLIC_KEY_LAST_LINE)) {
          publicKeys.add(sb.toString());
          sb.setLength(0);
        }
      }
    }
    return publicKeys.toArray(new String[0]);
  }

  public static String concatenatePublicKeys(List<String> keys) {
    return String.join("\n\n", keys);
  }

  public static MD5Key publicKeyMD5(String key) {
    key = key.trim();
    if (!key.startsWith(PUBLIC_KEY_FIRST_LINE) || !key.endsWith(PUBLIC_KEY_LAST_LINE)) {
      throw new IllegalArgumentException("Not a public key string.");
    }
    return new MD5Key(key.replaceAll("\\s", "").getBytes(StandardCharsets.UTF_8));
  }

  public static MD5Key publicKeyMD5(PublicKey publicKey) {
    return publicKeyMD5(CipherUtil.encodedPublicKeyText(publicKey));
  }
}
