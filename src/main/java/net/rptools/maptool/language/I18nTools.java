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
package net.rptools.maptool.language;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class I18nTools {
  private static ResourceBundle BUNDLE = null;
  private static final Logger log = LogManager.getLogger(I18nTools.class);
  private static final String ACCELERATOR_EXTENSION = ".accel";
  private static final String DESCRIPTION_EXTENSION = ".description";
  private static Map<String, List<String>> valueKeys = new LinkedHashMap<>();
  private static List<String> accelerators = new ArrayList<>();
  private static List<String> descriptions = new ArrayList<>();

  public I18nTools(boolean report) {
    if (!report) {
      return;
    }
    BUNDLE = ResourceBundle.getBundle("net.rptools.maptool.language.i18n");
    generateReport();
  }

  private void generateReport() {
    Set<String> keys = BUNDLE.keySet();
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
        sb.append(key + "\n");
      }
    }
    if (!descriptions.isEmpty()) {
      sb.append("\n\nDescriptions\n_____________\n");
      for (String key : descriptions) {
        sb.append(key + "\n");
      }
    }
    sb.append("\n\nDuplicates\n_____________");

    /* alpha-sort the list */
    LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
    valueKeys.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEachOrdered(entry -> result.put(entry.getKey(), entry.getValue()));

    for (String value : result.keySet()) {
      List keyList = result.get(value);
      if (keyList.size() > 1) {
        sb.append("\n" + value + ":");
        for (int i = 0; i < keyList.size(); i++) {
          sb.append("\n\t" + keyList.get(i));
        }
      }
    }
    log.info(sb.toString());
  }
}
