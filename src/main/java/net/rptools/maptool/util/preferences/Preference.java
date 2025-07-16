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

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Represents an individual preference that is convertible to and from {@code T}.
 *
 * <p>{@code Preference} objects are the various {@code define} methods of {@link PreferenceStore}.
 *
 * @param <T> The type read and written by the preference.
 */
public interface Preference<T> {
  Class<T> getValueClass();

  @SuppressWarnings("unchecked")
  default <U> Optional<Preference<U>> cast(Class<U> target) {
    if (target.equals(getValueClass())) {
      return Optional.of((Preference<U>) this);
    }
    return Optional.empty();
  }

  /**
   * @return The unique ID of this preference, and the key used to look up a value in the underlying
   *     storage.
   */
  String getKey();

  /**
   * @return A human-readable label for the preference that can be shown to users. If not available,
   *     will return {@link #getKey()}.
   */
  String getLabel();

  /**
   * A builder method for adding the label key using the i18nkey
   *
   * @param labelKey the i18n key for fetching the label
   * @return preference
   */
  Preference<T> setLabel(String labelKey);

  /**
   * @return A human-readable description of the preference that can be shown to users in tooltips.
   *     If not available, will return {@link #getKey()}.
   */
  String getTooltip();

  /**
   * A builder method for adding the tooltip using the i18n key
   *
   * @param tooltipKey the i18n key for fetching the label
   * @return preference
   */
  Preference<T> setTooltip(String tooltipKey);

  /**
   * @return The default value of the preference when not set in the underlying storage.
   */
  T getDefault();

  /**
   * Reads the serialized preference from the underlying storage and converts it to {@code T}.
   *
   * @return The value read.
   */
  T get();

  /**
   * Serialized {@code value} and write it to the underlying storage.
   *
   * @param value The value to store.
   */
  void set(T value);

  /**
   * Removes the preference from the underlying storage.
   *
   * <p>Afterward, calls to {@link #get()} will return {@link #getDefault()} until the preference is
   * set again.
   */
  void remove();

  /**
   * Registers a listener to respond to changes to the preference value.
   *
   * <p>{@code handler} will be called any time the preference value is changed via {@link
   * #set(Object)}. There is currently no way to detect changes from the underlying storage, so
   * {@code handler} will not be called if another instance modifies the value.
   *
   * @param handler The listener to be called when the preference value changes.
   */
  void onChange(Consumer<T> handler);

  /**
   * Enables caching on the preference.
   *
   * <p>When caching is enabled, {@link #get()} will only read the underlying storage once, and then
   * remember the value. Similarly, {@link #set(Object)} will remember the stored value so it does
   * not need to be read again. {@link #remove()} will set the cached value to {@link
   * #getDefault()}.
   *
   * @return {@code this}
   */
  Preference<T> cacheIt();

  /** A specialized {@link Preference} that adds bounds to a numeric preference. */
  interface Numeric<T extends Number> extends Preference<T> {
    /**
     * @return The minimum value of the preference.
     */
    T getMinValue();

    /**
     * @return The maximum value of the preference.
     */
    T getMaxValue();
  }
}
