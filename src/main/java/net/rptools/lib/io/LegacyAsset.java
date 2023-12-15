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
package net.rptools.lib.io;

import net.rptools.lib.MD5Key;

/**
 * This class represents a legacy asset where the asset is serialized with xstream including the
 * image data. This is only used for xstream serialization.
 */
public class LegacyAsset {
  /** the key of the asset */
  private MD5Key id;

  /** The image data */
  private byte[] image;

  /** The name of the asset */
  private String name;

  /**
   * Returns the key of the asset.
   *
   * @return the key of the asset.
   */
  public MD5Key getId() {
    return id;
  }

  /**
   * Returns the image data.
   *
   * @return the image data.
   */
  public byte[] getImageData() {
    return image;
  }

  /**
   * Returns the name of the asset.
   *
   * @return the name of the asset.
   */
  public String getName() {
    return name;
  }
}
