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
package net.rptools.maptool.util;

import java.util.EnumMap;
import java.util.function.Function;

public class CollectionUtil {
  public static <K extends Enum<K>, V> EnumMap<K, V> newFilledEnumMap(
      Class<K> keyType, Function<K, V> instantiator) {
    final var map = new EnumMap<K, V>(keyType);
    for (final var key : keyType.getEnumConstants()) {
      final var value = instantiator.apply(key);
      map.put(key, value);
    }
    return map;
  }
}
