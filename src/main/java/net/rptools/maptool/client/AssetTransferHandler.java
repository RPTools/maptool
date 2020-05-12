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
package net.rptools.maptool.client;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.transfer.ConsumerListener;
import org.apache.commons.io.FileUtils;

/**
 * Handles incoming segmented assets
 *
 * @author trevor
 */
public class AssetTransferHandler implements ConsumerListener {
  public void assetComplete(Serializable id, String name, File data) {
    byte[] assetData = null;
    try {
      assetData = FileUtils.readFileToByteArray(data);
    } catch (IOException ioe) {
      MapTool.showError("Error loading composed asset file: " + id);
      return;
    }
    Asset asset = Asset.createUnknownAssetType(name, assetData);
    if (!asset.getMD5Key().equals(id)) {
      MapTool.showError("Received an invalid image: " + id);
      return;
    }
    // Install it into our system
    AssetManager.putAsset(asset);

    // Remove the temp file
    data.delete();
    MapTool.getFrame().refresh();
  }

  public void assetUpdated(Serializable id) {
    // Nothing to do
  }

  public void assetAdded(Serializable id) {
    // Nothing to do
  }
}
