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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import javax.annotation.Nullable;
import net.rptools.maptool.language.I18N;

class BasicPreference<T> implements Preference<T> {
  private final Preferences prefs;
  private final String key;
  private String label;
  private String tooltip;
  private final Supplier<T> defaultValue;
  private final PreferenceType<T> type;

  private boolean cachingEnabled = false;
  private @Nullable T cachedValue;

  private final List<Consumer<T>> onChangeHandlers = new CopyOnWriteArrayList<>();

  public BasicPreference(
      Preferences prefs,
      String key,
      @Nullable String labelKey,
      @Nullable String tooltipKey,
      T defaultValue,
      PreferenceType<T> type) {
    this(prefs, key, labelKey, tooltipKey, () -> defaultValue, type);
  }

  public BasicPreference(
      Preferences prefs,
      String key,
      @Nullable String labelKey,
      @Nullable String tooltipKey,
      Supplier<T> defaultValue,
      PreferenceType<T> type) {
    this.prefs = prefs;
    this.key = key;
    this.label = labelKey == null ? key : I18N.getString(labelKey);
    this.tooltip = tooltipKey == null ? key : I18N.getString(tooltipKey);
    this.defaultValue = defaultValue;
    this.type = type;
  }

  @Override
  public Class<T> getValueClass() {
    return type.getValueClass();
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public Preference<T> setLabel(String labelKey) {
    this.label = labelKey == null ? key : I18N.getString(labelKey);
    return this;
  }

  @Override
  public Preference<T> setTooltip(String tooltipKey) {
    this.tooltip = tooltipKey == null ? key : I18N.getString(tooltipKey);
    return this;
  }

  @Override
  public String getTooltip() {
    return tooltip;
  }

  /**
   * Constrains {@code value} to a valid value.
   *
   * <p>For general preferences, there is no special subset which values must belong to. For numeric
   * preferences, this method will clamp the value to the range of valid values.
   *
   * @param value The value to constrain.
   * @return The constrained value.
   */
  protected T constrain(T value) {
    return value;
  }

  /**
   * Loads and validates the value of the preference.
   *
   * <p>After being loaded, the value will be constrained to a valid value via the {@link
   * #constrain(Object)} method.
   *
   * @return The value of the preference.
   */
  @Override
  public T get() {
    if (cachingEnabled && cachedValue != null) {
      return cachedValue;
    }

    var value = type.get(prefs, key, defaultValue);
    value = constrain(value);

    cachedValue = value;
    return value;
  }

  /**
   * Validates and stores the value of the preference.
   *
   * <p>Before being stored, {@code value} will be constrained to a valid value via {@link
   * #constrain(Object)}.
   *
   * @param value The value to set.
   */
  @Override
  public void set(T value) {
    value = constrain(value);

    type.set(prefs, key, value);
    cachedValue = value;

    for (var handler : onChangeHandlers) {
      handler.accept(value);
    }
  }

  @Override
  public void remove() {
    prefs.remove(key);
    cachedValue = getDefault();

    for (var handler : onChangeHandlers) {
      handler.accept(cachedValue);
    }
  }

  @Override
  public T getDefault() {
    return defaultValue.get();
  }

  @Override
  public BasicPreference<T> cacheIt() {
    this.cachingEnabled = true;
    return this;
  }

  @Override
  public void onChange(Consumer<T> handler) {
    onChangeHandlers.add(handler);
  }
}
