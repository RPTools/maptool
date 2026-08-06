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
package net.rptools.maptool.client.ui.token;

import java.util.List;
import net.rptools.lib.MD5Key;

public abstract sealed class AbstractImageTokenOverlay extends BooleanTokenOverlay
    permits ImageTokenOverlay, CornerImageTokenOverlay, FlowImageTokenOverlay {
  /** ID of the image displayed in the overlay. */
  private MD5Key assetId;

  /**
   * Create new image overlay.
   *
   * @param name Name of the new token overlay
   * @param assetId ID of the image displayed in the new token overlay.
   */
  public AbstractImageTokenOverlay(String name, MD5Key assetId) {
    super(name);
    this.assetId = assetId;
  }

  public AbstractImageTokenOverlay(AbstractImageTokenOverlay other) {
    super(other.name);
    this.assetId = other.assetId;
  }

  public MD5Key getAssetId() {
    return assetId;
  }

  @Override
  public List<MD5Key> getAssetIds() {
    return List.of(assetId);
  }
}
