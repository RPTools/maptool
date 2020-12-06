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


  private final static String CIPHER_ALGORITHM = "AES";
  private final static String MESSAGE_DIGEST_ALGORITHM = "SHA-256";



  private static final Logger log = LogManager.getLogger(MapToolServerConnection.class);


  public static CipherUtil getInstance() {
    return instance;
  }


  private final MessageDigest messageDigest;


  private CipherUtil() {
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
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
    return createCipher(Cipher.DECRYPT_MODE, key);
  }

  public Cipher createEncrypter(String key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    return createCipher(Cipher.ENCRYPT_MODE, key);

  }

  public Cipher createDecrypter(SecretKeySpec key) {
    if (!key.getAlgorithm().equals(CIPHER_ALGORITHM)) {
      throw new AssertionError("Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.getAlgorithm());
    }
    return createCipher(Cipher.DECRYPT_MODE, key);
  }

  public Cipher createEncrypter(SecretKeySpec key) {
    if (!key.getAlgorithm().equals(CIPHER_ALGORITHM)) {
      throw new AssertionError("Expected Algorithm " + CIPHER_ALGORITHM + " got " + key.getAlgorithm());
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
