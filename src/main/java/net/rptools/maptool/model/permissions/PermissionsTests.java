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
package net.rptools.maptool.model.permissions;

import java.util.function.BiFunction;
import javax.annotation.Nullable;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.NonNull;

/** A set of tests for determining access by an object for a Permissions permission. */
public interface PermissionsTests {
  /** Does an accessor object(e.g. Player) have permission generally */
  BiFunction<Permissions, Object, Boolean> getAccessorTest();

  /** Does an accessor object(e.g. Player) have permission generally */
  void setAccessorTest(@NonNull BiFunction<Permissions, Object, Boolean> accessorTest);

  /**
   * Does an accessor object(e.g. Player) have permission to access the target object (e.g. Token)
   */
  TriFunction<Permissions, Object, Object, Boolean> getObjectAccessTest();

  /**
   * Does an accessor object(e.g. Player) have permission to access the target object (e.g. Token)
   */
  void setObjectAccessTest(
      @NonNull TriFunction<Permissions, Object, Object, Boolean> objectAccessTest);

  /** Is the first object an ally of the second object. Used in other tests. */
  BiFunction<Object, Object, Boolean> getObjectAllyTest();

  /** Is the first object an ally of the second object. Used in other tests. */
  void setObjectAllyTest(@Nullable BiFunction<Object, Object, Boolean> objectAllyTest);
}
