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
package net.rptools.maptool.model.library.data;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.model.gamedata.data.DataType;
import net.rptools.maptool.model.gamedata.data.DataValue;

public interface LibraryData {

  String libraryName();

  CompletableFuture<Set<String>> getAllKeys();

  CompletableFuture<Map<String, DataType>> getKeyDataTypeMap();

  CompletableFuture<DataType> getDataType(String key);

  CompletableFuture<Boolean> isDefined(String key);

  CompletableFuture<DataValue> getValue(String key);
}
