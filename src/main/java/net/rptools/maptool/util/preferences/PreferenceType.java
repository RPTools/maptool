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
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * Defines how preferences with a value of type {@code T} can be read and written.
 *
 * <p>For numeric types, also defines how to constrain the value to its bounds.
 *
 * @param <T> The preference value type, as for {@link Preference}.
 */
interface PreferenceType<T> {
  Class<T> getValueClass();

  /**
   * Serializes and writes {@code value} to {@code storage}, associating it with {@code key}.
   *
   * @param storage The preferences node to write to.
   * @param key The key to write to.
   * @param value The value to associate with {@code key}.
   */
  void set(Preferences storage, String key, T value);

  /**
   * Reads and deserialized the value associated with {@code key} in {@code storage}.
   *
   * @param storage The preferences node to read from.
   * @param key The key to read.
   * @param defaultValue If the key is not found, use {@code defaultValue.get()} instead.
   * @return The deserialized value, or {@code defaultValue} if not found.
   */
  T get(Preferences storage, String key, Supplier<T> defaultValue);

  /** Refines {@link PreferenceType} to allow clamping a numeric value to some bounds. */
  interface Numeric<T extends Number> extends PreferenceType<T> {
    /**
     * Constrains {@code value} to lie between {@code minValue} and {@code maxValue}, inclusive.
     *
     * @param value The value to constrain.
     * @param minValue The least value that can be returned.
     * @param maxValue The greatest value that can be returned.
     * @return If {@code value} is less than {@code minValue}, then {@code minValue}. If {@code
     *     value} is greater than {@code maxValue}, then {@code maxValue}. Otherwise, {@code value}.
     */
    T clamp(T value, T minValue, T maxValue);
  }

  /**
   * Reads and writes {@code boolean} values.
   *
   * <p>Values are read using {@link Preferences#getBoolean(String, boolean)} and written using
   * {@link Preferences#putBoolean(String, boolean)}.
   */
  final class BooleanType implements PreferenceType<Boolean> {
    @Override
    public Class<Boolean> getValueClass() {
      return Boolean.class;
    }

    @Override
    public void set(Preferences storage, String key, Boolean value) {
      storage.putBoolean(key, value);
    }

    @Override
    public Boolean get(Preferences storage, String key, Supplier<Boolean> defaultValue) {
      return storage.getBoolean(key, defaultValue.get());
    }
  }

  /**
   * Reads and writes {@code int} values.
   *
   * <p>Values are read using {@link Preferences#getInt(String, int)} and written using {@link
   * Preferences#putInt(String, int)}.
   */
  final class IntegerType implements Numeric<Integer> {
    @Override
    public Class<Integer> getValueClass() {
      return Integer.class;
    }

    @Override
    public Integer clamp(Integer value, Integer minValue, Integer maxValue) {
      return Math.clamp(value, minValue, maxValue);
    }

    @Override
    public void set(Preferences storage, String key, Integer value) {
      storage.putInt(key, value);
    }

    @Override
    public Integer get(Preferences storage, String key, Supplier<Integer> defaultValue) {
      return storage.getInt(key, defaultValue.get());
    }
  }

  /**
   * Reads and writes {@code double} values.
   *
   * <p>Values are read using {@link Preferences#getDouble(String, double)} and written using {@link
   * Preferences#putDouble(String, double)}.
   */
  final class DoubleType implements Numeric<Double> {
    @Override
    public Class<Double> getValueClass() {
      return Double.class;
    }

    @Override
    public Double clamp(Double value, Double minValue, Double maxValue) {
      return Math.clamp(value, minValue, maxValue);
    }

    @Override
    public void set(Preferences storage, String key, Double value) {
      storage.putDouble(key, value);
    }

    @Override
    public Double get(Preferences storage, String key, Supplier<Double> defaultValue) {
      return storage.getDouble(key, defaultValue.get());
    }
  }

  /**
   * Reads and writes {@code double} values.
   *
   * <p>Values are read using {@link Preferences#get(String, String)} and written using {@link
   * Preferences#put(String, String)}.
   */
  final class StringType implements PreferenceType<String> {
    @Override
    public Class<String> getValueClass() {
      return String.class;
    }

    @Override
    public void set(Preferences storage, String key, String value) {
      storage.put(key, value);
    }

    @Override
    public String get(Preferences storage, String key, Supplier<String> defaultValue) {
      return storage.get(key, defaultValue.get());
    }
  }

  /**
   * Reads and writes {@code File} objects.
   *
   * <p>Files are represented as their path strings when stored. The path strings are stored as with
   * {@link StringType}.
   */
  final class FileType implements PreferenceType<File> {
    @Override
    public Class<File> getValueClass() {
      return File.class;
    }

    @Override
    public void set(Preferences storage, String key, File value) {
      storage.put(key, value.toString());
    }

    @Override
    public File get(Preferences storage, String key, Supplier<File> defaultValue) {
      String filePath = storage.get(key, null);
      if (filePath != null) {
        return new File(filePath);
      }

      return defaultValue.get();
    }
  }

  /**
   * Reads and writes arbitrary {@code Enum<T>} objects.
   *
   * <p>The enum values are represented as their names. The names are stored as with {@link
   * StringType}.
   */
  final class EnumType<T extends Enum<T>> implements PreferenceType<T> {
    private final Class<T> class_;

    public EnumType(Class<T> class_) {
      this.class_ = class_;
    }

    @Override
    public Class<T> getValueClass() {
      return class_;
    }

    @Override
    public void set(Preferences storage, String key, T value) {
      storage.put(key, value.name());
    }

    @Override
    public T get(Preferences storage, String key, Supplier<T> defaultValue) {
      var stored = storage.get(key, null);
      if (stored == null) {
        return defaultValue.get();
      }

      try {
        return Enum.valueOf(class_, stored);
      } catch (Exception e) {
        return defaultValue.get();
      }
    }
  }

  /**
   * Reads and writes {@code Color} objects.
   *
   * <p>The colors are represented by their 32-bit ARGB values. The values are stored as with {@link
   * IntegerType}.
   */
  final class ColorType implements PreferenceType<Color> {
    private final boolean hasAlpha;

    public ColorType(boolean hasAlpha) {
      this.hasAlpha = hasAlpha;
    }

    @Override
    public Class<Color> getValueClass() {
      return Color.class;
    }

    @Override
    public void set(Preferences storage, String key, Color value) {
      storage.putInt(key, value.getRGB());
    }

    @Override
    public Color get(Preferences storage, String key, Supplier<Color> defaultValue) {
      return new Color(storage.getInt(key, defaultValue.get().getRGB()), hasAlpha);
    }
  }
}
