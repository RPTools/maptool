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
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Disposable;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.ui.zone.ZoneView;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.AssetManager;
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

  // this atlas is shared by all zones and must not be disposed here.
  private TextureAtlas sharedAtlas;
  private final Map<String, Sprite> fetchedSprites = new HashMap<>();
  private final Map<MD5Key, Sprite> isoSprites = new HashMap<>();
  private final Map<String, TextureRegion> fetchedRegions = new HashMap<>();
  private final Map<MD5Key, Sprite> bigSprites = new HashMap<>();
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

  private Sprite TRANSFERING_SPRITE;
  private Sprite BROKEN_SPRITE;

  public void setSharedAtlas(TextureAtlas atlas) {
    sharedAtlas = atlas;
    if (atlas == null) return;
    TRANSFERING_SPRITE = new Sprite(sharedAtlas.findRegion("unknown"));
    BROKEN_SPRITE = new Sprite(sharedAtlas.findRegion("broken"));
  }

  public ZoneCache(@Nonnull ZoneRenderer zoneRenderer, @Nonnull TextureAtlas sharedAtlas) {
    this.zone = zoneRenderer.getZone();
    this.zoneRenderer = zoneRenderer;
    setSharedAtlas(sharedAtlas);
  }

  /*
    @Override
    public void assetAvailable(MD5Key key) {
      var asset = AssetManager.getAsset(key);
      if (asset.getExtension().equals("gif")) {

        Gdx.app.postRunnable(
            () -> {
              // var ass = AssetManager.getAsset(key);
              var is = new ByteArrayInputStream(asset.getData());
              var animation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, is);
              animationMap.put(key, animation);
            });
        return;
      }
      if (asset.getExtension().equals("data")) {
        var videoPlayer = VideoPlayerCreator.createVideoPlayer();
        videoPlayerMap.put(key, videoPlayer);
        return;
      }
      BufferedImage img;
      byte[] bytes;
      try {
        img =
            ImageUtil.createCompatibleImage(
                ImageUtil.bytesToImage(asset.getData(), asset.getName()), null);
        bytes = ImageUtil.imageToBytes(img, "png");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      // without ImageUtil there seem to be some issues with transparency  for some images.
      // (black background instead of transparent)
      var pix = new Pixmap(bytes, 0, bytes.length);

      try {
        var name = key.toString();
        synchronized (packer) {
          if (packer.getRect(name) == null) packer.pack(name, pix);

          pix.dispose();
        }
      } catch (GdxRuntimeException x) {
        // this means that the pixmap is too big for the atlas.
        Gdx.app.postRunnable(
            () -> {
              synchronized (bigSprites) {
                if (!bigSprites.containsKey(key)) bigSprites.put(key, new Sprite(new Texture(pix)));
              }
              pix.dispose();
            });
      }
      Gdx.app.postRunnable(
          () -> {
            packer.updateTextureAtlas(
                tokenAtlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
          });
    }
  */

  private void imageToSprite(MD5Key key, BufferedImage image) {
    byte[] bytes;

    try {
      bytes = ImageUtil.imageToBytes(image, "png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    var name = key.toString();
    var pixmap = new Pixmap(bytes, 0, bytes.length);
    try {
      synchronized (packer) {
        if (packer.getRect(name) == null) {
          packer.pack(name, pixmap);
        }
        pixmap.dispose();
      }
    } catch (Exception x) {
      // this means that the pixmap is too big for the atlas.

      synchronized (bigSprites) {
        if (!bigSprites.containsKey(key)) {
          bigSprites.put(key, new Sprite(new Texture(pixmap)));
        }
      }
      pixmap.dispose();
    }
    packer.updateTextureAtlas(
        tokenAtlas, Texture.TextureFilter.Linear, Texture.TextureFilter.Linear, false);
  }

  public TextureRegion fetch(String regionName) {
    var region = fetchedRegions.get(regionName);
    if (region != null) {
      return region;
    }

    region = tokenAtlas.findRegion(regionName);
    if (region == null) {
      region = sharedAtlas.findRegion(regionName);
    }

    if (region != null) {
      fetchedRegions.put(regionName, region);
    }
    return region;
  }

  public Sprite getSprite(String name) {
    var sprite = fetchedSprites.get(name);
    if (sprite != null) {
      var region = fetchedRegions.get(name);
      sprite.setSize(region.getRegionWidth(), region.getRegionHeight());
      return sprite;
    }

    var region = fetch(name);

    if (region == null) {
      var key = new MD5Key(name);
      var image = ImageManager.getImage(key);
      if (image == ImageManager.TRANSFERING_IMAGE) {
        return TRANSFERING_SPRITE;
      }

      if (image == ImageManager.BROKEN_IMAGE) {
        return BROKEN_SPRITE;
      }

      imageToSprite(key, image);

      region = fetch(name);
    }

    if (region == null) {
      sprite = bigSprites.get(name);
    } else {
      sprite = new Sprite(region);
      sprite.setSize(region.getRegionWidth(), region.getRegionHeight());
    }

    if (sprite == null) {
      return BROKEN_SPRITE;
    }

    fetchedSprites.put(name, sprite);
    return sprite;
  }

  public Sprite getIsoSprite(MD5Key key) {
    if (isoSprites.containsKey(key)) {
      return isoSprites.get(key);
    }

    var workImage = IsometricGrid.isoImage(ImageManager.getImage(key));

    byte[] bytes;
    try {
      bytes = ImageUtil.imageToBytes(workImage, "png");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    var pix = new Pixmap(bytes, 0, bytes.length);
    var image = new Sprite(new Texture(pix));
    pix.dispose();
    isoSprites.put(key, image);
    return image;
  }

  public Sprite getSprite(MD5Key key) {
    if (key == null) return null;

    var sprite = bigSprites.get(key);
    if (sprite != null) {
      sprite.setSize(sprite.getTexture().getWidth(), sprite.getTexture().getHeight());
      return sprite;
    }

    return getSprite(key.toString());
  }

  public Texture getPaintTexture(MD5Key assetId) {
    return paintTextures.computeIfAbsent(
        assetId,
        key -> {
          var asset = AssetManager.getAsset(key);
          var image = asset.getData();
          var pix = new Pixmap(image, 0, image.length);
          var texture = new Texture(pix);
          texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
          pix.dispose();
          return texture;
        });
  }

  @Override
  public void dispose() {
    fetchedRegions.clear();
    fetchedSprites.clear();

    Gdx.app.postRunnable(
        () -> {
          packer.dispose();
          tokenAtlas.dispose();

          for (var sprite : isoSprites.values()) {
            sprite.getTexture().dispose();
          }
          isoSprites.clear();

          for (var texture : paintTextures.values()) {
            texture.dispose();
          }
          paintTextures.clear();

          for (var sprite : bigSprites.values()) {
            sprite.getTexture().dispose();
          }
          bigSprites.clear();
        });
  }
}
