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
package net.rptools.maptool.util.preferences;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/** A wrapper around {@link Preferences} that adds strong typing and a host of other features. */
public class PreferenceStore {
  private final Preferences prefs;
  private final List<Preference<?>> definedPreferences = new ArrayList<>();

  public PreferenceStore(Preferences prefs) {
    this.prefs = prefs;
  }

  public Preferences getStorage() {
    return prefs;
  }

  public List<Preference<?>> getDefinedPreferences() {
    return Collections.unmodifiableList(definedPreferences);
  }

  private <T extends Preference<?>> T register(T preference) {
    definedPreferences.add(preference);
    return preference;
  }

  public Preference<Boolean> defineBoolean(String key, boolean defaultValue) {
    return defineBoolean(key, null, null, defaultValue);
  }

  public Preference<Boolean> defineBoolean(
      String key, String labelKey, String tooltip, boolean defaultValue) {
    return register(
        new BasicPreference<>(
            prefs, key, labelKey, tooltip, defaultValue, new PreferenceType.BooleanType()));
  }

  public Preference.Numeric<Integer> defineInteger(String key, int defaultValue) {
    return defineInteger(key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  public Preference.Numeric<Integer> defineByte(String key, int defaultValue) {
    return defineInteger(key, defaultValue, 0, 255);
  }

  public Preference.Numeric<Integer> defineInteger(
      String key, int defaultValue, int minValue, int maxValue) {
    return register(
        new NumericPreference<>(
            prefs, key, defaultValue, minValue, maxValue, new PreferenceType.IntegerType()));
  }

  public Preference.Numeric<Double> defineDouble(String key, double defaultValue) {
    return defineDouble(key, defaultValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  }

  public Preference.Numeric<Double> defineDouble(
      String key, double defaultValue, double minValue, double maxValue) {
    return register(
        new NumericPreference<>(
            prefs, key, defaultValue, minValue, maxValue, new PreferenceType.DoubleType()));
  }

  public Preference<String> defineString(String key, String defaultValue) {
    return register(
        new BasicPreference<>(
            prefs, key, null, null, defaultValue, new PreferenceType.StringType()));
  }

  public Preference<File> defineFile(String key, Supplier<File> defaultValue) {
    return register(
        new BasicPreference<>(prefs, key, null, null, defaultValue, new PreferenceType.FileType()));
  }

  public <T extends Enum<T>> Preference<T> defineEnum(Class<T> class_, String key, T defaultValue) {
    return register(
        new BasicPreference<>(
            prefs, key, null, null, defaultValue, new PreferenceType.EnumType<>(class_)));
  }

  public Preference<Color> defineColor(String key, Color defaultValue, boolean hasAlpha) {
    return register(
        new BasicPreference<>(
            prefs, key, null, null, defaultValue, new PreferenceType.ColorType(hasAlpha)));
  }
}
