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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.tools;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class I18NTools {
  private static final Logger log = LogManager.getLogger(I18NTools.class);
  private static final String ACCELERATOR_EXTENSION = ".accel";
  private static final String DESCRIPTION_EXTENSION = ".description";

  public static void main(String[] args) {
    var bundle = ResourceBundle.getBundle("net.rptools.maptool.language.i18n");
    var valueKeys = new LinkedHashMap<String, List<String>>();
    var accelerators = new ArrayList<String>();
    var descriptions = new ArrayList<String>();

    Set<String> keys = bundle.keySet();
    StringBuilder sb = new StringBuilder();
    for (String key : keys) {
      if (key.endsWith(ACCELERATOR_EXTENSION)) {
        accelerators.add(key);
      }
      if (key.endsWith(DESCRIPTION_EXTENSION)) {
        descriptions.add(key);
      }
      String value = I18N.getText(key).trim();

      if (valueKeys.containsKey(value)) {
        List<String> keyList = valueKeys.get(value);
        keyList.add(key);
        valueKeys.replace(value, keyList);
      } else {
        List<String> keyList = new ArrayList<>();
        keyList.add(key);
        if (valueKeys.containsKey(value + ":")) {
          keyList.add(":");
        }
        valueKeys.put(value, keyList);
      }
    }
    if (!accelerators.isEmpty()) {
      sb.append("Accelerators\n_____________\n");
      for (String key : accelerators) {
        sb.append(key).append("\n");
      }
    }
    if (!descriptions.isEmpty()) {
      sb.append("\n\nDescriptions\n_____________\n");
      for (String key : descriptions) {
        sb.append(key).append("\n");
      }
    }
    sb.append("\n\nDuplicates\n_____________");

    /* alpha-sort the list */
    LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
    valueKeys.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEachOrdered(entry -> result.put(entry.getKey(), entry.getValue()));

    for (String value : result.keySet()) {
      List<String> keyList = result.get(value);
      if (keyList.size() > 1) {
        sb.append("\n").append(value).append(":");
        for (String s : keyList) {
          sb.append("\n\t").append(s);
        }
      }
    }
    log.info(sb.toString());
  }
}
