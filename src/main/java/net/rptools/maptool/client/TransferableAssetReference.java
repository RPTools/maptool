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
package main.java.net.rptools.maptool.client;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import main.java.net.rptools.lib.MD5Key;
import main.java.net.rptools.maptool.model.Asset;
import main.java.net.rptools.maptool.model.GUID;

/** */
public class TransferableAssetReference implements Transferable {

  public static final DataFlavor dataFlavor = new DataFlavor(GUID.class, "AssetReference");

  private MD5Key assetID;

  public TransferableAssetReference(Asset asset) {
    this.assetID = asset.getId();
  }

  public Object getTransferData(DataFlavor flavor) {
    return assetID;
  }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {dataFlavor};
  }

  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return flavor.equals(dataFlavor);
  }
}
