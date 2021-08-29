package net.rptools.maptool.server;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.util.cipher.CipherUtil;

public class HandshakeChallenge {
  private final byte[] challenge;
  private final byte[] expectedResponse;

  private HandshakeChallenge(byte[] challenge, byte[] expectedResponse) {
    this.challenge = challenge;
    this.expectedResponse = expectedResponse;
  }


  static HandshakeChallenge createChallenge(String username, String password, CipherUtil.Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher encryptor = CipherUtil.createEncryptor(key);
    String toEncrypt = username + password;
    byte[] challenge = encryptor.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8));
    byte[] response;
    if (key.asymmetric()) {
      response = password.getBytes(StandardCharsets.UTF_8);
    } else {
      response = encryptor.doFinal(password.getBytes(StandardCharsets.UTF_8));
    }
    return new HandshakeChallenge(challenge, response);
  }

  static HandshakeChallenge fromChallengeBytes(String username, byte[] challenge,
      CipherUtil.Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    Cipher decryptor = CipherUtil.createDecryptor(key);
    String challengeString = new String(decryptor.doFinal(challenge));
    if (!challengeString.startsWith(username)) {
      throw new IllegalStateException("Handhshake challenge " + challengeString +
          " does not start with username " + username);
    }
    String responseString = challengeString.replace(username, "");
    byte[] response;
    if (key.asymmetric()) {
      response = responseString.getBytes(StandardCharsets.UTF_8);
    } else {
      Cipher encryptor = CipherUtil.createEncryptor(key);
      response = encryptor.doFinal(responseString.getBytes(StandardCharsets.UTF_8));
    }

    return new HandshakeChallenge(challenge, response);
  }


  public byte[] getChallenge() {
    return challenge;
  }

  public byte[] getExpectedResponse() {
    return expectedResponse;
  }
}

