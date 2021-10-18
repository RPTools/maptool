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
package net.rptools.maptool.model.framework.dropinlibrary;

import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;

public class TransferableDropInLibrary {
  private final String namespace;
  private final String version;
  private final MD5Key assetKey;

  public TransferableDropInLibrary(DropInLibrary dropInLibrary)
      throws ExecutionException, InterruptedException {
    namespace = dropInLibrary.getNamespace().get();
    version = dropInLibrary.getVersion().get();
    assetKey = dropInLibrary.getAssetKey();
  }

  public String getNamespace() {
    return namespace;
  }

  public String getVersion() {
    return version;
  }

  public MD5Key getAssetKey() {
    return assetKey;
  }
}
