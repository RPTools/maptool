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
package net.rptools.maptool.client.ui.zone.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.IsometricGrid;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZoneCache implements Disposable {

  private static final Logger log = LogManager.getLogger(ZoneCache.class);
  private final Zone zone;
  private final ZoneRenderer zoneRenderer;
  private final PixmapPacker packer =
      new PixmapPacker(2048, 2048, Pixmap.Format.RGBA8888, 2, false);
  private final TextureAtlas tokenAtlas = new TextureAtlas();

  private final Map<MD5Key, TextureRegion> tokenAtlasAssetRegions = new HashMap<>();
  private final Map<MD5Key, Texture> largeAssets = new HashMap<>();

  private final Map<MD5Key, Texture> isoTextures = new HashMap<>();
  private final Map<MD5Key, Texture> paintTextures = new HashMap<>();

  public Zone getZone() {
    return zone;
  }

  public ZoneRenderer getZoneRenderer() {
    return zoneRenderer;
  }

  public ZoneViewModel getZoneViewModel() {
    return zoneRenderer.getViewModel();
  }

  public ZoneView getZoneView() {
    return zoneRenderer.getZoneView();
  }

  public ZoneCache(@Nonnull ZoneRenderer zoneRenderer) {
    this.zone = zoneRenderer.getZone();
    this.zoneRenderer = zoneRenderer;
  }

  /**
   * Converts a {@link BufferedImage} to a {@link Pixmap} in ARGB8888 format with premultiplied
   * alpha.
   *
   * @param image The image asset to convert.
   * @return A premultiplied ARGB888 pixmap equivalent of {@code image}.
   */
  private Pixmap assetToPixmap(BufferedImage image) {
    var pixmap = new Pixmap(image.getWidth(), image.getHeight(), packer.getPageFormat());
    pixmap.setBlending(Pixmap.Blending.None);

    Color gdxColor = new Color();
    for (var y = 0; y < image.getHeight(); ++y) {
      for (var x = 0; x < image.getWidth(); ++x) {
        var awtColor = image.getRGB(x, y);
        Color.argb8888ToColor(gdxColor, awtColor);
        gdxColor.premultiplyAlpha();

        pixmap.setColor(gdxColor);
        pixmap.drawPixel(x, y);
      }
    }

    return pixmap;
  }

  public @Nonnull TextureRegion getImageAsset(
      @Nonnull MD5Key key, TextureRegion transferringAsset, TextureRegion brokenAsset) {
    // We have two levels of cache:
    // 1. `largeAssets`, for images too large to fit in one of the pages in `tokenAtlas`.
    // 2. `tokenAtlasAssetRegions`, for images that have been packed into `tokenAtlas`.

    var regionName = key.toString();

    Texture knownLargeAsset = largeAssets.get(key);
    if (knownLargeAsset != null) {
      return new TextureRegion(knownLargeAsset);
    }

    TextureRegion knownRegion = tokenAtlasAssetRegions.get(key);
    if (knownRegion != null) {
      return knownRegion;
    }

    // This asset is not cached. Our atlas cache could have been invalidated, though, so check if
    // the asset is in the atlas, and cache it if it is.
    knownRegion = tokenAtlas.findRegion(regionName);
    if (knownRegion != null) {
      tokenAtlasAssetRegions.put(key, knownRegion);
      return knownRegion;
    }

    // This is a brand new asset. We'll need to resolve it, convert it, and try to pack it.

    var image = ImageManager.getImage(key);
    // Handle the cases where we could not resolve the image.
    if (image == ImageManager.TRANSFERING_IMAGE) {
      return transferringAsset;
    }
    if (image == ImageManager.BROKEN_IMAGE) {
      return brokenAsset;
    }

    // We're about to convert an image and pack it. But play it safe in case we somehow try to pack
    // a duplicate.
    if (packer.getRect(regionName) != null) {
      log.warn("Asset {} has already been packed, but somehow isn't in the atlas yet.", key);
    } else {
      // Convert the image.
      var pixmap = assetToPixmap(image);
      try {
        // Attempt to pack the image.
        packer.pack(regionName, pixmap);
      } catch (GdxRuntimeException e) {
        // Indicates a duplicate or a pixmap that is too large. We checked for duplicates already,
        // so it must be the latter.
        var texture = new Texture(pixmap);
        largeAssets.put(key, texture);
        return new TextureRegion(texture);
      } finally {
        pixmap.dispose();
      }
    }

    // Rebuild the texture so it has the asset.
    // This invalidates existing cached texture regions.
    packer.updateTextureAtlas(
        tokenAtlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
    tokenAtlasAssetRegions.clear();

    var newRegion = tokenAtlas.findRegion(regionName);
    if (newRegion == null) {
      log.warn("Asset {} is not available in the atlas after being packed", key);
      return brokenAsset;
    }
    return newRegion;
  }

  public @Nonnull Texture getPaintTexture(
      MD5Key assetId, Texture transferringAsset, Texture brokenAsset) {
    var texture = paintTextures.get(assetId);
    if (texture != null) {
      return texture;
    }

    // We'll have to load, convert, and cache the asset.
    var image = ImageManager.getImageAndWait(assetId);
    if (image == ImageManager.TRANSFERING_IMAGE) {
      return transferringAsset;
    }
    if (image == ImageManager.BROKEN_IMAGE) {
      return brokenAsset;
    }

    // Convert the image to a properly formatted pixmap.
    var pixmap = assetToPixmap(image);
    try {
      texture = new Texture(pixmap);
      texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
      paintTextures.put(assetId, texture);
      return texture;
    } finally {
      pixmap.dispose();
    }
  }

  public Texture getIsoImage(MD5Key key, Texture transferringAsset, Texture brokenAsset) {
    if (isoTextures.containsKey(key)) {
      return isoTextures.get(key);
    }

    var image = ImageManager.getImage(key);
    if (image == ImageManager.TRANSFERING_IMAGE) {
      return transferringAsset;
    }
    if (image == ImageManager.BROKEN_IMAGE) {
      return brokenAsset;
    }

    var workImage = IsometricGrid.isoImage(image);
    var pixmap = assetToPixmap(workImage);
    try {
      var region = new Texture(pixmap);
      isoTextures.put(key, region);
      return region;
    } finally {
      pixmap.dispose();
    }
  }

  @Override
  public void dispose() {
    Gdx.app.postRunnable(
        () -> {
          for (var texture : largeAssets.values()) {
            texture.dispose();
          }
          largeAssets.clear();

          tokenAtlasAssetRegions.clear();

          for (var texture : paintTextures.values()) {
            texture.dispose();
          }
          paintTextures.clear();

          for (var texture : isoTextures.values()) {
            texture.dispose();
          }
          isoTextures.clear();

          packer.dispose();
          tokenAtlas.dispose();
        });
  }
}
