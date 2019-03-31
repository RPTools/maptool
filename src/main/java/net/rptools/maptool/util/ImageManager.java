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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetAvailableListener;
import net.rptools.maptool.model.AssetManager;
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

  private static final Map<MD5Key, byte[]> textureMap = new HashMap<MD5Key, byte[]>();

  /**
   * The unknown image, a "?" is used for all situations where the image will eventually appear e.g.
   * asset download, and image loading.
   */
  private static final String UNKNOWN_IMAGE_PNG = "net/rptools/maptool/client/image/unknown.png";

  public static BufferedImage TRANSFERING_IMAGE;

  /** The broken image, a "X" is used for all situations where the asset or image was invalid. */
  private static final String BROKEN_IMAGE_PNG = "net/rptools/maptool/client/image/broken.png";

  public static BufferedImage BROKEN_IMAGE;

  /** Small and large thread pools for background processing of asset raw image data. */
  private static ExecutorService smallImageLoader = Executors.newFixedThreadPool(1);

  private static ExecutorService largeImageLoader = Executors.newFixedThreadPool(1);

  private static Object imageLoaderMutex = new Object();

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
   * @param asset Load image data from this asset
   * @return BufferedImage Return the loaded image
   */
  public static BufferedImage getImageAndWait(MD5Key assetId) {
    return getImageAndWait(assetId, null);
  }

  /**
   * Flush all images that are <b>not</b> in the provided set. This presumes that the images in the
   * exception set will still be in use after the flush.
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
   * @param asset Load image data from this asset
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
            new ImageObserver() {
              public boolean imageUpdate(
                  Image img, int infoflags, int x, int y, int width, int height) {
                // If we're here then the image has just finished loading
                // release the blocked thread
                log.debug("Countdown: " + assetId);
                loadLatch.countDown();
                return false;
              }
            });
    if (image == TRANSFERING_IMAGE) {
      try {
        synchronized (loadLatch) {
          log.debug("Wait for:  " + assetId);
          loadLatch.await();
        }
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

  public static BufferedImage getImage(MD5Key assetId, ImageObserver... observers) {
    return getImage(assetId, null, observers);
  }

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
      // Make note that we're currently processing it
      imageMap.put(assetId, TRANSFERING_IMAGE);

      // Make sure we are informed when it's done loading
      addObservers(assetId, observers);

      // Force a load of the asset, this will trigger a transfer if the
      // asset is not available locally
      if (image == null) {
        AssetManager.getAssetAsynchronously(assetId, new AssetListener(assetId, hints));
      }
      return TRANSFERING_IMAGE;
    }
  }

  /**
   * Remove the image associated the asset from the cache.
   *
   * @param asset Asset associated with this image
   */
  public static void flushImage(Asset asset) {
    flushImage(asset.getId());
  }

  /**
   * Remove the image associated this MD5Key from the cache.
   *
   * @param assetId MD5Key associated with this image
   */
  public static void flushImage(MD5Key assetId) {
    // LATER: investigate how this effects images that are already in progress
    imageMap.remove(assetId);
    textureMap.remove(assetId);
  }

  /**
   * Add observers, and associated hints for image loading, to be notified when the asset has
   * completed loading.
   *
   * @param assetId Waiting for this asset to load
   * @param hints Load the asset image with these hints
   * @param observers Observers to be notified
   */
  public static void addObservers(MD5Key assetId, ImageObserver... observers) {
    if (observers == null || observers.length == 0) {
      return;
    }
    Set<ImageObserver> observerSet = imageObserverMap.get(assetId);
    if (observerSet == null) {
      observerSet = new HashSet<ImageObserver>();
      imageObserverMap.put(assetId, observerSet);
    }
    for (ImageObserver observer : observers) {
      observerSet.add(observer);
    }
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
      log.debug("Loading asset: " + asset.getId());
      BufferedImage image = imageMap.get(asset.getId());

      if (image != null && image != TRANSFERING_IMAGE) {
        // We've somehow already loaded this image
        log.debug("Image wasn't in transit: " + asset.getId());
        return;
      }

      if (asset.getImageExtension().equals(Asset.DATA_EXTENSION)) {
        log.debug(
            "BackgroundImageLoader.run("
                + asset.getName()
                + ","
                + asset.getImageExtension()
                + ", "
                + asset.getId()
                + "): looks like data and skipped");
        image = BROKEN_IMAGE; // we should never see this
      } else {
        try {
          assert asset.getImage() != null
              : "asset.getImage() for " + asset.toString() + "returns null?!";
          image = ImageUtil.createCompatibleImage(ImageUtil.bytesToImage(asset.getImage()), hints);
        } catch (Throwable t) {
          log.error(
              "BackgroundImageLoader.run("
                  + asset.getName()
                  + ","
                  + asset.getImageExtension()
                  + ", "
                  + asset.getId()
                  + "): image not resolved",
              t);
          image = BROKEN_IMAGE;
        }
      }

      synchronized (imageLoaderMutex) {
        // Replace placeholder with actual image
        imageMap.put(asset.getId(), image);
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
    log.debug("Notifying observers of image availability: " + asset.getId());
    Set<ImageObserver> observerSet = imageObserverMap.remove(asset.getId());
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
    if (asset.getImage().length > 128 * 1024) {
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
    public boolean equals(Object obj) {
      return id.equals(obj);
    }
  }
}
