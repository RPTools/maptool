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

import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;
import net.rptools.maptool.util.preferences.Preference;
import net.rptools.maptool.util.preferences.PreferenceStore;

public class DeveloperOptions {
  private static final PreferenceStore store =
      new PreferenceStore(Preferences.userRoot().node(AppConstants.APP_NAME + "/dev"));

  public static final class Toggle {
    public static final Preference<Boolean> AutoSaveMeasuredInSeconds =
        store.defineBoolean(
            "autoSaveMeasuredInSeconds",
            "Preferences.developer.autoSaveMeasuredInSeconds.label",
            "Preferences.developer.autoSaveMeasuredInSeconds.tooltip",
            false);
    public static final Preference<Boolean> ShowPartitionDrawableBoundaries =
        store.defineBoolean(
            "showPartitionDrawableBoundaries",
            "Preferences.developer.showPartitionDrawableBoundaries.label",
            "Preferences.developer.showPartitionDrawableBoundaries.tooltip",
            false);
    public static final Preference<Boolean> ShowAiDebugging =
        store.defineBoolean(
            "showAiDebugging",
            "Preferences.developer.showAiDebugging.label",
            "Preferences.developer.showAiDebugging.tooltip",
            false);
    public static final Preference<Boolean> IgnoreGridShapeCache =
        store.defineBoolean(
            "ignoreGridShapeCache",
            "Preferences.developer.ignoreGridShapeCache.label",
            "Preferences.developer.ignoreGridShapeCache.tooltip",
            false);
    public static final Preference<Boolean> DebugTokenDragging =
        store.defineBoolean(
            "debugTokenDragging",
            "Preferences.developer.debugTokenDragging.label",
            "Preferences.developer.debugTokenDragging.tooltip",
            false);
    public static final Preference<Boolean> EnableLibGdxRendererToggleButton =
        store.defineBoolean(
            "enableLibGDXRendererToggleButton",
            "Preferences.developer.enableLibGDXRendererToggleButton.label",
            "Preferences.developer.enableLibGDXRendererToggleButton.tooltip",
            false);

    public static List<Preference<Boolean>> getOptions() {
      return store.getDefinedPreferences().stream()
          .map(p -> p.cast(Boolean.class))
          .<Preference<Boolean>>mapMulti(Optional::ifPresent)
          .toList();
    }

    public static List<Preference<Boolean>> getEnabledOptions() {
      return getOptions().stream().filter(Preference::get).toList();
    }
  }
}
