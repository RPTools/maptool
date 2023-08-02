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
package net.rptools.maptool.model.library.addon;

import java.util.concurrent.ExecutionException;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.server.proto.TransferableAddOnLibraryDto;

public class TransferableAddOnLibrary {
  private final String namespace;
  private final String version;
  private final MD5Key assetKey;

  private TransferableAddOnLibrary(String namespace, String version, String assetKey) {
    this.namespace = namespace;
    this.version = version;
    this.assetKey = new MD5Key(assetKey);
  }

  public TransferableAddOnLibrary(AddOnLibrary addOnLibrary)
      throws ExecutionException, InterruptedException {
    namespace = addOnLibrary.getNamespace().get();
    version = addOnLibrary.getVersion().get();
    assetKey = addOnLibrary.getAssetKey();
  }

  public static TransferableAddOnLibrary fromDto(TransferableAddOnLibraryDto dto) {
    return new TransferableAddOnLibrary(dto.getNamespace(), dto.getVersion(), dto.getAssetKey());
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

  public TransferableAddOnLibraryDto toDto() {
    var dto = TransferableAddOnLibraryDto.newBuilder();
    return dto.setNamespace(namespace).setVersion(version).setAssetKey(assetKey.toString()).build();
  }
}
