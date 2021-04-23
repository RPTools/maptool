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
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StrPropFunctionsTest {
  public static final String DEFAULT_DELIMITER = ";";
  Map<String, String> map = new HashMap<>();
  List<String> oldKeys = new ArrayList<>();
  List<String> oldKeysNormalized = new ArrayList<>();
  private String key;

  private StrPropFunctionsTest inMap(Map<String, String> map) {
    this.map = map;
    return this;
  }

  private StrPropFunctionsTest key(String key) {
    this.key = key;
    return this;
  }

  private void hasValue(String value) {
    assertEquals(
        map.get(key),
        value,
        "with key: <"
            + key
            + "> expected value: <"
            + value
            + "> actual value: <"
            + map.get(key)
            + ">");
  }

  @Test
  void parse_onePropWithSpace() {
    String testProps = "a 1=1";

    StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

    inMap(map).key("A 1").hasValue("1");
  }

  @Test
  void parse_twoPropsFirstWithSpace() {
    String testProps = "a 1=1; nospace=2";

    StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

    inMap(map).key("A 1").hasValue("1");
    inMap(map).key("NOSPACE").hasValue("2");
  }

  @Test
  void parse_twoPropsLastWithSpace() {
    String testProps = "nospace=2; a 1=1";

    StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

    inMap(map).key("A 1").hasValue("1");
    inMap(map).key("NOSPACE").hasValue("2");
  }

  @Test
  void parse_onePropWithTwoSpaces() {
    String testProps = "a b 1=1";

    StrPropFunctions.parse(testProps, map, oldKeys, oldKeysNormalized, DEFAULT_DELIMITER);

    inMap(map).key("A B 1").hasValue("1");
  }
}
