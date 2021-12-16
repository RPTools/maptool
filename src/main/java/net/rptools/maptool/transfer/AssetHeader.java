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
package net.rptools.maptool.transfer;

import java.io.Serializable;
import net.rptools.maptool.server.proto.AssetTransferHeaderDto;

public class AssetHeader implements Serializable {
  private String id;
  private String name;
  private long size;

  public AssetHeader(String id, String name, long size) {
    this.id = id;
    this.size = size;
    this.name = name;
  }

  public static AssetHeader fromDto(AssetTransferHeaderDto dto) {
    return new AssetHeader(dto.getId(), dto.getName(), dto.getSize());
  }

  public Serializable getId() {
    return id;
  }

  public long getSize() {
    return size;
  }

  public String getName() {
    return name;
  }

  public AssetTransferHeaderDto toDto() {
    return AssetTransferHeaderDto.newBuilder().setName(name).setId(id).setSize(size).build();
  }
}
