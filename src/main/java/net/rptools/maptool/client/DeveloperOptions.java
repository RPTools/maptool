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
package net.rptools.maptool.client;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import net.rptools.maptool.language.I18N;

public class DeveloperOptions {
  private static final Preferences prefs =
      Preferences.userRoot().node(AppConstants.APP_NAME + "/dev");

  public enum Toggle {
    /**
     * When enabled, make auto-save 60x more frequent by interpreting the user-provided value as
     * seconds instead of minutes.
     */
    AutoSaveMeasuredInSeconds("autoSaveMeasuredInSeconds"),

    /** When enabled, draw boundaries around each partition. */
    ShowPartitionDrawableBoundaries("showPartitionDrawableBoundaries"),

    /**
     * When enabled, shows F, G, H scores for each cell encountered during pathfinding, as well as
     * blocked moved.
     */
    ShowAiDebugging("showAiDebugging"),

    /** When enabled, recalculates the grid shape each time it is needed. */
    IgnoreGridShapeCache("ignoreGridShapeCache"),

    /**
     * When enabled, highlights the important points used during token drags, for example, the drag
     * anchor and starting position of the cursor.
     */
    DebugTokenDragging("debugTokenDragging");

    private final String key;

    Toggle(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }

    public boolean isEnabled() {
      return prefs.getBoolean(key, false);
    }

    public void setEnabled(boolean enabled) {
      prefs.putBoolean(key, enabled);
    }

    public String getLabel() {
      return I18N.getText(String.format("Preferences.developer.%s.label", key));
    }

    public String getTooltip() {
      return I18N.getText(String.format("Preferences.developer.%s.tooltip", key));
    }
  }

  public static List<Toggle> getEnabledOptions() {
    return Arrays.stream(Toggle.values()).filter(Toggle::isEnabled).toList();
  }
}
