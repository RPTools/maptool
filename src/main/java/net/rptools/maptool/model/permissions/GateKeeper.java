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

public abstract class GateKeeper implements PermissionsTests {
  protected BiFunction<Permissions, Object, Boolean> accessorTest;
  protected TriFunction<Permissions, Object, Object, Boolean> objectAccessTest;
  protected BiFunction<Object, Object, Boolean> objectAllyTest;

  public GateKeeper() {}

  @Override
  public BiFunction<Permissions, Object, Boolean> getAccessorTest() {
    return accessorTest;
  }

  @Override
  public void setAccessorTest(@NonNull BiFunction<Permissions, Object, Boolean> accessorTest) {
    this.accessorTest = accessorTest;
  }

  @Override
  public TriFunction<Permissions, Object, Object, Boolean> getObjectAccessTest() {
    return objectAccessTest;
  }

  @Override
  public void setObjectAccessTest(
      @NonNull TriFunction<Permissions, Object, Object, Boolean> objectAccessTest) {
    this.objectAccessTest = objectAccessTest;
  }

  @Override
  public BiFunction<Object, Object, Boolean> getObjectAllyTest() {
    return objectAllyTest;
  }

  @Override
  public void setObjectAllyTest(@Nullable BiFunction<Object, Object, Boolean> objectAllyTest) {
    this.objectAllyTest = objectAllyTest;
  }
}
