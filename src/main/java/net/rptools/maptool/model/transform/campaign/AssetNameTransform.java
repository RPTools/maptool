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
package net.rptools.maptool.model.transform.campaign;

import net.rptools.lib.ModelVersionTransformation;

/**
 * This transform is for asset filenames, not the actual XML data. So the XML passed to the {@link
 * #transform(String)} method should be the asset's base name, typically <code>ASSET_DIR + key
 * </code>. This means that this transform should <b>NOT</b> be registered with any
 * ModelVersionManager or it will be executed in the wrong context.
 *
 * <p>pre-1.3.51: asset names had ".dat" tacked onto the end and held only binary data 1.3.51-63:
 * assets were stored in XML under their asset name, no extension 1.3.64+: asset objects are in XML
 * (name, MD5key), but the image is in another file with the asset's image type as an extension
 * (.jpeg, .png)
 *
 * @author frank
 */
public class AssetNameTransform implements ModelVersionTransformation {
  private final String regexOld;
  private final String regexNew;

  public AssetNameTransform(String from, String to) {
    regexOld = from;
    regexNew = to;
  }

  public String transform(String name) {
    return name.replace(regexOld, regexNew);
  }
}
