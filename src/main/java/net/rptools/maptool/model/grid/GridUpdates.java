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
package net.rptools.maptool.model.grid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public interface GridUpdates {
  Map<String, String> nameConversionOldNew =
      new HashMap<>() {
        {
          put("GRIDLESS", "GRIDLESS");
          put("NONE", "GRIDLESS");
          put("UNSET", "GRIDLESS");
          put("HEX_H", "HEX_HORI");
          put("HEX_V", "HEX_VERT");
          put("ISOMETRIC_HEX", "GRIDLESS");
          put("ISOMETRIC_SQUARE", "ISOMETRIC");
          put("ISOMETRIC_TRIANGLE", "GRIDLESS");
          put("SQUARE", "SQUARE");
          put("TRIANGLE", "GRIDLESS");
          put("HEX_HORI", "HEX_H");
          put("HEX_VERT", "HEX_V");
          put("ISOMETRIC", "ISOMETRIC_SQUARE");
        }
      };
  Map<String, String> nameConversionShortLong =
      new HashMap<>() {
        {
          put("HEX_VERT", "Vertical Hex");
          put("HEX_HORI", "Horizontal Hex");
          put("SQUARE", "Square");
          put("ISOMETRIC", "Isometric");
          put("ISOMETRIC_HEX", "Isometric Hex");
          put("GRIDLESS", "Gridless");
          put("TYPE_NOT_SET", "Type not set");
          put("NONE", "None");
          put("Vertical Hex", "HEX_VERT");
          put("Horizontal Hex", "HEX_HORI");
          put("Square", "SQUARE");
          put("Isometric", "ISOMETRIC");
          put("Isometric Hex", "ISOMETRIC_HEX");
          put("Gridless", "GRIDLESS");
          put("Type not set", "TYPE_NOT_SET");
          put("None", "NONE");
        }
      };
  String[] newNames =
      new String[] {
        "GRIDLESS",
        "HEX_H",
        "HEX_V",
        "ISOMETRIC_HEX",
        "ISOMETRIC_SQUARE",
        "ISOMETRIC_TRIANGLE",
        "SQUARE",
        "TRIANGLE"
      };

  static String convertName(String oldName) {
    return nameConversionOldNew.get(oldName);
  }

  static boolean isNewName(String checkName) {
    return Arrays.asList(newNames).contains(checkName);
  }

  static boolean isLongName(String checkName) {
    return !checkName.toUpperCase().equals(checkName);
  }

  static String nameLookup(String oldName) {
    return nameConversionShortLong.get(oldName);
  }

  static String moderniseName(String checkName) {
    if (isNewName(checkName)) return checkName;
    if (isLongName(checkName)) {
      String intermediate = nameLookup(checkName);
      return convertName(intermediate);
    }
    return convertName(checkName);
  }
}
