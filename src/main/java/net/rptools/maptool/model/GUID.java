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
package net.rptools.maptool.model;

import com.withay.util.HexCode;
import java.io.Serializable;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;

/** Global unique identification object. */
public final class GUID extends Object implements Serializable, Comparable<GUID> {
  /** Serial version unique identifier. */
  private static final long serialVersionUID = 6361057925697403643L;

  /** GUIDs always have 16 bytes. */
  public static final int GUID_LENGTH = 16;

  // NOTE: THIS CAN NEVER BE CHANGED, OR IT WILL AFFECT ALL THINGS THAT PREVIOUSLY USED IT
  public static final int GUID_BUCKETS = 100;
  // NOTE: THIS CAN NEVER BE CHANGED, OR IT WILL AFFECT ALL THINGS THAT PREVIOUSLY USED IT

  /** The id value. */
  private final byte[] baGUID;

  // Cache of the hashCode for a GUID
  private transient int hash;

  /** Creates a new {@code GUID} object with a new id value. */
  public GUID() {
    this.baGUID = generateGUID();
    validateGUID();
  }

  /**
   * Creates a new GUID based on the specified GUID value
   *
   * @param baGUID the id value.
   */
  public GUID(byte[] baGUID) throws InvalidGUIDException {
    this.baGUID = baGUID.clone();
    validateGUID();
  }

  /**
   * Creates a new GUID based on the specified hexadecimal-code string.
   *
   * @param strGUID a {@code String} representing the id value.
   */
  public GUID(String strGUID) {
    if (strGUID == null) {
      throw new InvalidGUIDException("GUID is null");
    }

    this.baGUID = HexCode.decode(strGUID);
    validateGUID();
  }

  /** Ensures the GUID is legal. */
  private void validateGUID() throws InvalidGUIDException {
    if (baGUID == null) {
      throw new InvalidGUIDException("GUID is null");
    }
    if (baGUID.length != GUID_LENGTH) {
      throw new InvalidGUIDException("GUID length is invalid: " + baGUID.length);
    }
  }

  /**
   * Returns the GUID representation of the {@link byte} array argument.
   *
   * @param bits the id value.
   */
  public static GUID valueOf(byte[] bits) {
    if (bits == null) {
      return null;
    }
    return new GUID(bits);
  }

  /**
   * Returns the GUID representation of the {@link String} argument.
   *
   * @param s The {@code String} value of the id.
   */
  public static GUID valueOf(String s) {
    if (s == null) {
      return null;
    }
    return new GUID(s);
  }

  @Override
  public boolean equals(Object object) {
    if (object == null) {
      return this == null;
    }
    Class<? extends Object> objClass = object.getClass();

    GUID guid;
    try {
      if (objClass == String.class) { // string
        guid = new GUID((String) object);
      } else { // try to cast to a GUID
        guid = (GUID) object;
      }
    } catch (ClassCastException e) { // not a GUID
      return false;
    }

    // Compare bytes.
    for (int i = 0; i < GUID_LENGTH; i++) {
      if (this.baGUID[i] != guid.baGUID[i]) {
        return false;
      }
    }

    // All tests pass.
    return true;
  }

  /**
   * Returns the {@link byte} array representing the id value.
   *
   * @return the {@link byte} array representing the id value.
   */
  public byte[] getBytes() {
    return baGUID.clone();
  }

  @Override
  public String toString() {
    return HexCode.encode(baGUID, false); // false means uppercase
  }

  /**
   * Returns a hashcode for this GUID. This function is based on the algorithm that JDK 1.3 uses for
   * a String.
   *
   * @return a hash code value for this object.
   */
  @Override
  public int hashCode() {
    int h = hash;
    if (h == 0) {

      for (int i = 0; i < GUID_LENGTH; i++) h = 31 * h + baGUID[i];
      hash = h;
    }
    return h;
  }

  /**
   * Returns a new non-numeric GUID using UUID to generate a unique alphanumeric string
   *
   * @return a byte array representing the new id.
   */
  public static byte[] generateGUID() throws InvalidGUIDException {
    String newGUID = UUID.randomUUID().toString().replaceAll("-", "");

    while (StringUtils.isNumeric(newGUID)) {
      newGUID = UUID.randomUUID().toString().replaceAll("-", "");
    }

    return new GUID(newGUID).getBytes();
  }

  public static void main(String[] args) throws Exception {
    for (int i = 0; i < 10; i++) {
      GUID guid = new GUID();
      System.out.println("insert into sys_guids values ('" + guid.toString() + "');");
    }
  }

  @Override
  public int compareTo(GUID o) {
    if (o != this) {
      for (int i = 0; i < GUID_LENGTH; i++) {
        if (this.baGUID[i] != o.baGUID[i]) {
          return this.baGUID[i] - o.baGUID[i];
        }
      }
    }
    return 0;
  }
}
