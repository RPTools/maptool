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
package net.rptools.maptool.client.ui;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.util.ImageManager;

public class AssetPaint extends TexturePaint {

  private Asset asset;

  public AssetPaint(Asset asset) {
    this(ImageManager.getImageAndWait(asset.getId()));
    this.asset = asset;
  }

  // Only used to avoid a bunch of calls to getImageAndWait() that the compiler may
  // not be able to optimize (method calls may not be optimizable when side effects
  // of the method are not known to the compiler).
  private AssetPaint(BufferedImage img) {
    super(img, new Rectangle2D.Float(0, 0, img.getWidth(), img.getHeight()));
  }

  public Asset getAsset() {
    return asset;
  }
}
