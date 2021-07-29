package net.rptools.maptool.util.cipher;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.client.AppUtil;

public class PublicPrivateKeyStore {

  private static final File PUBLIC_KEY_FILE =
      AppUtil.getAppHome("config").toPath().resolve("public.key").toFile();
  private static final File PRIVATE_KEY_FILE =
      AppUtil.getAppHome("config").toPath().resolve("private.key").toFile();


  /*
   * Returns the public and private keys for this client. If none exists it will attempt to
   * create them and save them to the key files.
   *
   * @return the keys.
   *
   * @throws IOException if an error occurs reading the key file or saving a newly generated key
   * to the files.
   * @throws NoSuchAlgorithmException if the JDK install does not have the specified encryption
   * algorithm.
   * @throws InvalidKeySpecException if the key specification is invalid.
   * @throws NoSuchPaddingException If the padding for the key is invalid.
   * @throws InvalidKeyException if they key is invalid.
   */
  public CipherUtil.Key getKeys()
      throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
    if (!PUBLIC_KEY_FILE.exists() || !PRIVATE_KEY_FILE.exists()) {
      KeyPair keyPair = CipherUtil.generateKeyPair();
      CipherUtil.writeKeyPair(keyPair, PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);
    }

    return CipherUtil.fromPublicPrivatePair(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE).getKey();
  }

  /**
   * Regenerates and returns a new public / private key pair. This will also save the new keys to
   * the key files.
   * @return the newly generated keys.
   *
   * @throws IOException if an error occurs reading the key file or saving a newly generated key
   * to the files.
   * @throws NoSuchAlgorithmException if the JDK install does not have the specified encryption
   * algorithm.
   * @throws InvalidKeySpecException if the key specification is invalid.
   * @throws NoSuchPaddingException If the padding for the key is invalid.
   * @throws InvalidKeyException if they key is invalid.
   */
  public CipherUtil.Key regenerateKeys()
      throws NoSuchAlgorithmException, IOException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {
    KeyPair keyPair = CipherUtil.generateKeyPair();
    CipherUtil.writeKeyPair(keyPair, PUBLIC_KEY_FILE, PRIVATE_KEY_FILE);

    return CipherUtil.fromPublicPrivatePair(PUBLIC_KEY_FILE, PRIVATE_KEY_FILE).getKey();
  }
}
