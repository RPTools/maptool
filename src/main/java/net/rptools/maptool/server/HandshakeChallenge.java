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
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException {
    Cipher encryptor = CipherUtil.createEncryptor(key);
    String toEncrypt = username + password;
    byte[] challenge = encryptor.doFinal(toEncrypt.getBytes(StandardCharsets.UTF_8));
    byte[] response;
    var revPassword = new StringBuilder(password).reverse().toString();
    if (key.asymmetric()) {
      response = revPassword.getBytes(StandardCharsets.UTF_8);
    } else {
      response = encryptor.doFinal(revPassword.getBytes(StandardCharsets.UTF_8));
    }
    return new HandshakeChallenge(challenge, response);
  }

  static HandshakeChallenge fromChallengeBytes(
      String username, byte[] challenge, CipherUtil.Key key)
      throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException,
          IllegalBlockSizeException, BadPaddingException {
    Cipher decryptor = CipherUtil.createDecryptor(key);
    String challengeString = new String(decryptor.doFinal(challenge));
    if (!challengeString.startsWith(username)) {
      throw new IllegalStateException(
          "Handhshake challenge " + challengeString + " does not start with username " + username);
    }
    String responseString = challengeString.replace(username, "");
    byte[] response;
    var revPassword = new StringBuilder(responseString).reverse().toString();
    if (key.asymmetric()) {
      response = revPassword.getBytes(StandardCharsets.UTF_8);
    } else {
      Cipher encryptor = CipherUtil.createEncryptor(key);
      response = encryptor.doFinal(revPassword.getBytes(StandardCharsets.UTF_8));
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
