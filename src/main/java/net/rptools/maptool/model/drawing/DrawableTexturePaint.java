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
package net.rptools.maptool.model.drawing;

import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.util.ImageManager;

public class DrawableTexturePaint extends DrawablePaint implements Serializable {
  private MD5Key assetId;
  private double scale;
  private transient BufferedImage image;
  private transient Asset asset;

  public DrawableTexturePaint() {
    // Serializable
  }

  public DrawableTexturePaint(MD5Key id) {
    this(id, 1);
  }

  public DrawableTexturePaint(MD5Key id, double scale) {
    assetId = id;
    this.scale = scale;
  }

  public DrawableTexturePaint(Asset asset) {
    this(asset != null ? asset.getMD5Key() : null);
    this.asset = asset;
  }

  public DrawableTexturePaint(Asset asset, double scale) {
    this(asset.getMD5Key(), 1);
    this.asset = asset;
  }

  @Override
  public Paint getPaint(int offsetX, int offsetY, double scale, ImageObserver... observers) {
    BufferedImage texture = null;
    if (image != null) {
      texture = image;
    } else {
      texture = ImageManager.getImage(assetId, observers);
      if (texture != ImageManager.TRANSFERING_IMAGE) {
        image = texture;
      }
    }
    return new TexturePaint(
        texture,
        new Rectangle2D.Double(
            offsetX,
            offsetY,
            texture.getWidth() * scale * this.scale,
            texture.getHeight() * scale * this.scale));
  }

  @Override
  public Paint getPaint(ImageObserver... observers) {
    return getPaint(0, 0, 1, observers);
  }

  public Asset getAsset() {
    if (asset == null && assetId != null) {
      asset = AssetManager.getAsset(assetId);
    }
    return asset;
  }

  public MD5Key getAssetId() {
    return assetId;
  }
}
