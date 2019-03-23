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
package net.rptools.lib;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Represents the MD5 key for a certain set of data. Can be used in maps as keys. */
@SuppressWarnings("serial")
public class MD5Key implements Serializable {

  private static MessageDigest md5Digest;

  String id;

  static {
    try {
      md5Digest = MessageDigest.getInstance("md5");
    } catch (NoSuchAlgorithmException e) {
      // TODO: handle this more gracefully
      e.printStackTrace();
    }
  }

  public MD5Key() {}

  public MD5Key(String id) {
    this.id = id;
  }

  public MD5Key(byte[] data) {
    id = encodeToHex(digestData(data));
  }

  public MD5Key(InputStream data) {

    id = encodeToHex(digestData(data));
  }

  public String toString() {
    return id;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof MD5Key)) {
      return false;
    }

    return id.equals(((MD5Key) obj).id);
  }

  public int hashCode() {
    return id.hashCode();
  }

  private static synchronized byte[] digestData(byte[] data) {

    md5Digest.reset();

    md5Digest.update(data);

    return md5Digest.digest();
  }

  private static synchronized byte[] digestData(InputStream data) {

    md5Digest.reset();

    int b;
    try {
      while (((b = data.read()) >= 0)) {
        md5Digest.update((byte) b);
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }

    return md5Digest.digest();
  }

  private static String encodeToHex(byte[] data) {

    StringBuilder strbuild = new StringBuilder();
    for (int i = 0; i < data.length; i++) {

      String hex = Integer.toHexString(data[i]);
      if (hex.length() < 2) {
        strbuild.append("0");
      }
      if (hex.length() > 2) {
        hex = hex.substring(hex.length() - 2);
      }
      strbuild.append(hex);
    }

    return strbuild.toString();
  }
}
