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
package net.rptools.maptool.util;

import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetAvailableListener;
import net.rptools.maptool.model.AssetManager;
import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The ImageManager class keeps a cache of loaded images. This class can be used to load the raw
 * image data from an asset. The loading of the raw image data into a usable class is done in the
 * background by one of two threads. The ImageManager will return a "?" (UNKNOWN_IMAGE) if the asset
 * is still downloading or the asset image is still being loaded, and a "X" (BROKEN_IMAGE) if the
 * asset or image is invalid. Small images are loaded using a different thread pool from large
 * images, and allows small images to load quicker.
 *
 * @author RPTools Team.
 */
public class ImageManager {
  private static final Logger log = LogManager.getLogger(ImageManager.class);

  /** Cache of images loaded for assets. */
  private static final Map<MD5Key, BufferedImage> imageMap = new HashMap<MD5Key, BufferedImage>();

  /** Additional Soft-reference Cache of images that allows best . */
  private static final Map<MD5Key, BufferedImage> backupImageMap =
      new ReferenceMap(
          AbstractReferenceMap.ReferenceStrength.HARD, AbstractReferenceMap.ReferenceStrength.SOFT);

  /**
   * The unknown image, a "?" is used for all situations where the image will eventually appear e.g.
   * asset download, and image loading.
   */
  private static final String UNKNOWN_IMAGE_PNG = "net/rptools/maptool/client/image/unknown.png";

  /** The buffered "?" image to display while transferring the image. */
  public static BufferedImage TRANSFERING_IMAGE;

  /** The broken image, a "X" is used for all situations where the asset or image was invalid. */
  private static final String BROKEN_IMAGE_PNG = "net/rptools/maptool/client/image/broken.png";

  public static BufferedImage BROKEN_IMAGE;

  /** Small and large thread pools for background processing of asset raw image data. */
  private static ExecutorService smallImageLoader = Executors.newFixedThreadPool(1);

  private static ExecutorService largeImageLoader = Executors.newFixedThreadPool(1);

  private static final Object imageLoaderMutex = new Object();

  /**
   * A Map containing sets of observers for each asset id. Observers are notified when the image is
   * done loading.
   */
  private static Map<MD5Key, Set<ImageObserver>> imageObserverMap =
      new ConcurrentHashMap<MD5Key, Set<ImageObserver>>();

  static {
    try {
      TRANSFERING_IMAGE = ImageUtil.getCompatibleImage(UNKNOWN_IMAGE_PNG);
    } catch (IOException ioe) {
      log.error("static for 'unknown.png':  not resolved; IOException", ioe);
      TRANSFERING_IMAGE = ImageUtil.createCompatibleImage(10, 10, 0);
    }

    try {
      BROKEN_IMAGE = ImageUtil.getCompatibleImage(BROKEN_IMAGE_PNG);
    } catch (IOException ioe) {
      log.error("static for 'broken.png':  not resolved; IOException", ioe);
      BROKEN_IMAGE = ImageUtil.createCompatibleImage(10, 10, 0);
    }
  }

  /**
   * Remove all images from the image cache. The observers and image load hints are not flushed. The
   * same observers will be notified when the image is reloaded, and the same hints will be used for
   * loading.
   */
  public static void flush() {
    imageMap.clear();
  }

  /**
   * Loads the asset's raw image data into a buffered image, and waits for the image to load.
   *
   * @param assetId Load image data from this asset
   * @return BufferedImage Return the loaded image
   */
  public static BufferedImage getImageAndWait(MD5Key assetId) {
    return getImageAndWait(assetId, null);
  }

  /**
   * Flush all images that are <b>not</b> in the provided set. This presumes that the images in the
   * exception set will still be in use after the flush.
   *
   * @param exceptionSet a set of images not to be flushed
   */
  public static void flush(Set<MD5Key> exceptionSet) {
    synchronized (imageLoaderMutex) {
      for (MD5Key id : new HashSet<MD5Key>(imageMap.keySet())) {
        if (!exceptionSet.contains(id)) {
          imageMap.remove(id);
        }
      }
    }
  }

  /**
   * Loads the asset's raw image data into a buffered image, and waits for the image to load.
   *
   * @param assetId Load image data from this asset
   * @param hintMap Hints used when loading the image
   * @return BufferedImage Return the loaded image
   */
  public static BufferedImage getImageAndWait(final MD5Key assetId, Map<String, Object> hintMap) {
    if (assetId == null) {
      return BROKEN_IMAGE;
    }
    BufferedImage image = null;
    final CountDownLatch loadLatch = new CountDownLatch(1);
    image =
        getImage(
            assetId,
            (img, infoflags, x, y, width, height) -> {
              // If we're here then the image has just finished loading
              // release the blocked thread
              log.debug("Countdown: " + assetId);
              loadLatch.countDown();
              return false;
            });
    if (image == TRANSFERING_IMAGE) {
      try {
        log.debug("Wait for:  " + assetId);
        loadLatch.await();
        // This time we'll get the cached version
        image = getImage(assetId);
      } catch (InterruptedException ie) {
        log.error(
            "getImageAndWait(" + assetId + "):  image not resolved; InterruptedException", ie);
        image = BROKEN_IMAGE;
      }
    }
    return image;
  }

  /**
   * Return the image corresponding to the assetId.
   *
   * @param assetId Load image data from this asset.
   * @param observers the observers to be notified when the image loads, if it hasn't already.
   * @return the image, or BROKEN_IMAGE if assetId null, or TRANSFERING_IMAGE if loading.
   */
  public static BufferedImage getImage(MD5Key assetId, ImageObserver... observers) {
    return getImage(assetId, null, observers);
  }

  /**
   * Return the image corresponding to the assetId.
   *
   * @param assetId Load image data from this asset.
   * @param hints hints used when loading image data, if it isn't in the imageMap already.
   * @param observers the observers to be notified when the image loads, if it hasn't already.
   * @return the image, or BROKEN_IMAGE if assetId null, or TRANSFERING_IMAGE if loading.
   */
  public static BufferedImage getImage(
      MD5Key assetId, Map<String, Object> hints, ImageObserver... observers) {
    if (assetId == null) {
      return BROKEN_IMAGE;
    }
    synchronized (imageLoaderMutex) {
      BufferedImage image = imageMap.get(assetId);
      if (image != null && image != TRANSFERING_IMAGE) {
        return image;
      }

      // check if the soft reference still resolves image
      image = backupImageMap.get(assetId);
      if (image != null) {
        imageMap.put(assetId, image);
        return image;
      }

      // Make note that we're currently processing it
      imageMap.put(assetId, TRANSFERING_IMAGE);

      // Make sure we are informed when it's done loading
      addObservers(assetId, observers);

      // Force a load of the asset, this will trigger a transfer if the
      // asset is not available locally
      AssetManager.getAssetAsynchronously(assetId, new AssetListener(assetId, hints));
      return TRANSFERING_IMAGE;
    }
  }

  /**
   * Returns an image from an asset:// URL.<br>
   * The returned image may be scaled based on parameters in the URL:<br>
   * <b>width</b> - Query parameter. Desired width in px.<br>
   * <b>height</b> - Query parameter. Desired height in px.<br>
   * <b>size</b> - A suffix added after the asset id in the form of "-size" where size is a value
   * indicating the size in px to scale the largest side of the image to, maintaining aspect ratio.
   * This parameter is ignored if a width or height are present.<br>
   * (ex. asset://9e9687c80a3c9796b328711df6bd67cf-50)<br>
   * All parameters expect an integer >0. Any invalid value (a value <= 0 or non-integer) will
   * result in {@code BROKEN_IMAGE} being returned. Images are only scaled down and if any parameter
   * exceeds the image's native size the image will be returned unscaled.
   *
   * @param url URL to an asset
   * @return the image, scaled if indicated by the URL, or {@code BROKEN_IMAGE} if url is null, the
   *     URL protocol is not asset://, or it has invalid parameter values.
   */
  public static BufferedImage getImageFromUrl(URL url) {
    if (url == null || !url.getProtocol().equals("asset")) {
      return BROKEN_IMAGE;
    }

    String id = url.getHost();
    String query = url.getQuery();
    BufferedImage image;
    int imageW, imageH, scaleW = -1, scaleH = -1, size = -1;

    // Get size parameter
    int szIndex = id.indexOf('-');
    if (szIndex != -1) {
      String szStr = id.substring(szIndex + 1);
      id = id.substring(0, szIndex);
      try {
        size = Integer.parseInt(szStr);
      } catch (NumberFormatException nfe) {
        // Do nothing
      }
      if (size <= 0) {
        return BROKEN_IMAGE;
      }
    }

    // Get query parameters
    if (query != null && !query.isEmpty()) {
      HashMap<String, String> params = new HashMap<>();

      for (String param : query.split("&")) {
        if (param.isBlank()) continue;

        int eqIndex = param.indexOf("=");
        if (eqIndex != -1) {
          String k, v;
          k = param.substring(0, eqIndex).trim();
          v = param.substring(eqIndex + 1).trim();
          params.put(k, v);
        } else {
          params.put(param.trim(), "");
        }
      }

      if (params.containsKey("width")) {
        size = -1; // Don't use size param if width is present
        try {
          scaleW = Integer.parseInt(params.get("width"));
        } catch (NumberFormatException nfe) {
          // Do nothing
        }
        if (scaleW <= 0) {
          return BROKEN_IMAGE;
        }
      }

      if (params.containsKey("height")) {
        size = -1; // Don't use size param if height is present
        try {
          scaleH = Integer.parseInt(params.get("height"));
        } catch (NumberFormatException nfe) {
          // Do nothing
        }
        if (scaleH <= 0) {
          return BROKEN_IMAGE;
        }
      }
    }

    image = getImageAndWait(new MD5Key(id), null);
    imageW = image.getWidth();
    imageH = image.getHeight();

    // We only want to scale down, so if scaleW or ScaleH are too large just return the image
    if (scaleW > imageW || scaleH > imageH) {
      return image;
    }

    // Note: size will never be >0 if height or width parameters are present
    if (size > 0) {
      if (imageW > imageH) {
        scaleW = size;
      } else if (imageH > imageW) {
        scaleH = size;
      } else {
        scaleW = scaleH = size;
      }
    }

    if ((scaleW > 0 && imageW > scaleW) || (scaleH > 0 && imageH > scaleH)) {
      // Maintain aspect ratio if one dimension isn't given
      if (scaleW <= 0) {
        scaleW = Math.max((int) ((double) scaleH / imageH * imageW), 1);
      } else if (scaleH <= 0) {
        scaleH = Math.max((int) ((double) scaleW / imageW * imageH), 1);
      }
      image = ImageUtil.scaleBufferedImage(image, scaleW, scaleH);
    }

    return image;
  }

  /**
   * Remove the image associated the asset from the cache.
   *
   * @param asset Asset associated with this image
   */
  public static void flushImage(Asset asset) {
    flushImage(asset.getMD5Key());
  }

  /**
   * Remove the image associated this MD5Key from the cache.
   *
   * @param assetId MD5Key associated with this image
   */
  public static void flushImage(MD5Key assetId) {
    // LATER: investigate how this effects images that are already in progress
    imageMap.remove(assetId);
  }

  /**
   * Add observers, and associated hints for image loading, to be notified when the asset has
   * completed loading.
   *
   * @param assetId Waiting for this asset to load
   * @param observers Observers to be notified
   */
  public static void addObservers(MD5Key assetId, ImageObserver... observers) {
    if (observers == null || observers.length == 0) {
      return;
    }
    Set<ImageObserver> observerSet =
        imageObserverMap.computeIfAbsent(assetId, k -> new HashSet<ImageObserver>());
    observerSet.addAll(Arrays.asList(observers));
  }

  /**
   * Load the asset's raw image data into a BufferedImage.
   *
   * @author RPTools Team.
   */
  private static class BackgroundImageLoader implements Runnable {
    private final Asset asset;
    private final Map<String, Object> hints;

    /**
     * Create a background image loader to load the asset image using the hints provided.
     *
     * @param asset Asset to load
     * @param hints Hints to use for image loading
     */
    public BackgroundImageLoader(Asset asset, Map<String, Object> hints) {
      this.asset = asset;
      this.hints = hints;
    }

    /** Load the asset raw image data and notify observers that the image is loaded. */
    public void run() {
      log.debug("Loading asset: " + asset.getMD5Key());
      BufferedImage image = imageMap.get(asset.getMD5Key());

      if (image != null && image != TRANSFERING_IMAGE) {
        // We've somehow already loaded this image
        log.debug("Image wasn't in transit: " + asset.getMD5Key());
        return;
      }

      if (asset.getExtension().equals(Asset.DATA_EXTENSION)) {
        log.debug(
            "BackgroundImageLoader.run("
                + asset.getName()
                + ","
                + asset.getExtension()
                + ", "
                + asset.getMD5Key()
                + "): looks like data and skipped");
        image = BROKEN_IMAGE; // we should never see this
      } else {
        try {
          assert asset.getData() != null
              : "asset.getImage() for " + asset.toString() + "returns null?!";
          image =
              ImageUtil.createCompatibleImage(
                  ImageUtil.bytesToImage(asset.getData(), asset.getName()), hints);
        } catch (Throwable t) {
          if (!AssetManager.BAD_ASSET_LOCATION_KEY.toString().equals(asset.getMD5Key())) {
            // Don't bother logging cache miss of internal bad location asset
            log.error(
                "BackgroundImageLoader.run("
                    + asset.getName()
                    + ","
                    + asset.getExtension()
                    + ", "
                    + asset.getMD5Key()
                    + "): image not resolved",
                t);
          }
          image = BROKEN_IMAGE;
        }
      }

      synchronized (imageLoaderMutex) {
        // Replace placeholder with actual image
        imageMap.put(asset.getMD5Key(), image);
        backupImageMap.put(asset.getMD5Key(), image);
        notifyObservers(asset, image);
      }
    }
  }

  /**
   * Notify all observers watching the asset that the image is loaded.
   *
   * @param asset Loaded image from this asset
   * @param image Result of loading the asset raw image data
   */
  private static void notifyObservers(Asset asset, BufferedImage image) {
    // Notify observers
    log.debug("Notifying observers of image availability: " + asset.getMD5Key());
    Set<ImageObserver> observerSet = imageObserverMap.remove(asset.getMD5Key());
    if (observerSet != null) {
      for (ImageObserver observer : observerSet) {
        observer.imageUpdate(
            image, ImageObserver.ALLBITS, 0, 0, image.getWidth(), image.getHeight());
      }
    }
  }

  /**
   * Run a thread to load the asset raw image data in the background using the provided hints.
   *
   * @param asset Load raw image data from this asset
   * @param hints Hints used when loading image data
   */
  private static void backgroundLoadImage(Asset asset, Map<String, Object> hints) {
    // Use large image loader if the image is larger than 128kb.
    if (asset.getData().length > 128 * 1024) {
      largeImageLoader.execute(new BackgroundImageLoader(asset, hints));
    } else {
      smallImageLoader.execute(new BackgroundImageLoader(asset, hints));
    }
  }

  private static class AssetListener implements AssetAvailableListener {
    private final MD5Key id;
    private final Map<String, Object> hints;

    public AssetListener(MD5Key id, Map<String, Object> hints) {
      this.id = id;
      this.hints = hints;
    }

    public void assetAvailable(MD5Key key) {
      if (!key.equals(id)) {
        return;
      }
      // No longer need to be notified when this asset is available
      AssetManager.removeAssetListener(id, this);

      // Image is now available for loading
      log.debug("Asset available: " + id);
      backgroundLoadImage(AssetManager.getAsset(id), hints);
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      AssetListener that = (AssetListener) o;
      return Objects.equals(id, that.id);
    }
  }
}
