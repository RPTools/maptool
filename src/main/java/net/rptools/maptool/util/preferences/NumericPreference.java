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

import java.util.prefs.Preferences;

class NumericPreference<T extends Number> extends BasicPreference<T>
    implements Preference.Numeric<T> {
  private final PreferenceType.Numeric<T> type;
  private final T minValue;
  private final T maxValue;

  public NumericPreference(
      Preferences prefs,
      String key,
      T defaultValue,
      T minValue,
      T maxValue,
      PreferenceType.Numeric<T> type) {
    super(prefs, key, null, null, defaultValue, type);
    this.type = type;
    this.minValue = minValue;
    this.maxValue = maxValue;
  }

  /**
   * @return The minimum value of the preference.
   */
  @Override
  public T getMinValue() {
    return minValue;
  }

  /**
   * @return The maximum value of the preference.
   */
  @Override
  public T getMaxValue() {
    return maxValue;
  }

  /**
   * Clamp {@code value} to be between {@link #getMinValue()} and {@link #getMaxValue()}.
   *
   * @param value The value to constrain.
   * @return If {@code value < getMinValue()}, then {@code getMinValue()}. If {@code value >
   *     getMaxValue()}, then {@code getMaxValue()}. Otherwise, {@code value}.
   */
  @Override
  protected T constrain(T value) {
    return type.clamp(value, getMinValue(), getMaxValue());
  }
}
