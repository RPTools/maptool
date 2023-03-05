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
package net.rptools.maptool.server;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
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
  private final CipherUtil.Key key;

  private HandshakeChallenge(byte[] challenge, byte[] expectedResponse, CipherUtil.Key key) {
    this.challenge = challenge;
    this.expectedResponse = expectedResponse;
    this.key = key;
  }

  private HandshakeChallenge(byte[] challenge, byte[] expectedResponse) {
    this.challenge = challenge;
    this.expectedResponse = expectedResponse;
    this.key = null;
  }

  static HandshakeChallenge createSymmetricChallenge(
      String username, String password, CipherUtil.Key key, byte[] iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException, AssertionError,
          InvalidAlgorithmParameterException {
    if (key.asymmetric()) {
      throw new AssertionError("Expected Symmetric algorithm and IV, got Asymmetric algorithm");
    }
    return createChallenge(username, password, key, iv);
  }

  static HandshakeChallenge createAsymmetricChallenge(
      String username, String password, CipherUtil.Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException, AssertionError,
          InvalidAlgorithmParameterException {
    if (!key.asymmetric()) {
      throw new AssertionError("Expected Asymmetric algorithm without IV, got Symmetric algorithm");
    }
    return createChallenge(username, password, key, null);
  }

  private static HandshakeChallenge createChallenge(
      String username, String password, CipherUtil.Key key, byte[] iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    Cipher encryptor;
    if (key.asymmetric()) {
      encryptor = CipherUtil.createAsymmetricEncryptor(key);
    } else {
      encryptor = CipherUtil.createSymmetricEncryptor(key, iv);
    }
    String toEncrypt = username + password;
    byte[] challenge = encryptor.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8));
    var revPassword = new StringBuilder(password).reverse().toString();
    var response = revPassword.getBytes(StandardCharsets.UTF_8);

    if (key.asymmetric()) {
      return new HandshakeChallenge(challenge, response);
    } else {
      return new HandshakeChallenge(challenge, response, key);
    }
  }

  static HandshakeChallenge fromSymmetricChallengeBytes(
      String username, byte[] challenge, CipherUtil.Key key, byte[] iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException, AssertionError,
          InvalidAlgorithmParameterException {
    if (key.asymmetric()) {
      throw new AssertionError("Expected Symmetric algorithm and IV, got Asymmetric algorithm");
    }
    return fromChallengeBytes(username, challenge, key, iv);
  }

  static HandshakeChallenge fromAsymmetricChallengeBytes(
      String username, byte[] challenge, CipherUtil.Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException, AssertionError,
          InvalidAlgorithmParameterException {
    if (!key.asymmetric()) {
      throw new AssertionError("Expected Symmetric algorithm and IV, got Asymmetric algorithm");
    }
    return fromChallengeBytes(username, challenge, key, null);
  }

  private static HandshakeChallenge fromChallengeBytes(
      String username, byte[] challenge, CipherUtil.Key key, byte[] iv)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
    Cipher decryptor;
    if (key.asymmetric()) {
      decryptor = CipherUtil.createAsymmetricDecryptor(key);
    } else {
      decryptor = CipherUtil.createSymmetricDecryptor(key, iv);
    }
    String challengeString = new String(decryptor.doFinal(challenge));
    if (!challengeString.startsWith(username)) {
      throw new IllegalStateException(
          "Handhshake challenge " + challengeString + " does not start with username " + username);
    }
    String responseString = challengeString.substring(username.length());
    var revPassword = new StringBuilder(responseString).reverse().toString();
    var response = revPassword.getBytes(StandardCharsets.UTF_8);

    if (key.asymmetric()) {
      return new HandshakeChallenge(challenge, response);
    } else {
      return new HandshakeChallenge(challenge, response, key);
    }
  }

  public byte[] getChallenge() {
    return challenge;
  }

  public byte[] getExpectedResponse() {
    return expectedResponse;
  }

  public byte[] getExpectedResponse(byte[] iv)
      throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException,
          BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
    Cipher encryptor = CipherUtil.createSymmetricEncryptor(key, iv);
    return encryptor.doFinal(expectedResponse);
  }
}
