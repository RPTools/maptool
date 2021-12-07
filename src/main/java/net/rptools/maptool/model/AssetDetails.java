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
package net.rptools.maptool.model;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset.Type;

public class AssetDetails {
  private final MD5Key md5Key;
  private final String name;

  private final String extension;
  private final Asset.Type type;
  private final byte[] data;

  public AssetDetails(MD5Key md5Key, String name, String extension, Type type, byte[] data) {
    this.md5Key = md5Key;
    this.name = name;
    this.extension = extension;
    this.type = type;
    this.data = data;
  }

  public MD5Key getMd5Key() {
    return md5Key;
  }

  public String getName() {
    return name;
  }

  public String getExtension() {
    return extension;
  }

  public Type getType() {
    return type;
  }

  public byte[] getData() {
    return data;
  }
}
