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
package net.rptools.maptool.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class handles the caching, loading, and downloading of assets. All assets are loaded through
 * this class.
 *
 * @author RPTools Team
 */
public class AssetManager {
  private static final Logger log = LogManager.getLogger(AssetManager.class);

  /** Assets are associated with the MD5 sum of their raw data */
  private static Map<MD5Key, Asset> assetMap = new ConcurrentHashMap<MD5Key, Asset>();

  /** Location of the cache on the filesystem */
  private static File cacheDir;

  /** True if a persistent cache should be used */
  private static boolean usePersistentCache;

  /**
   * A list of listeners which should be notified when the asset associated with a given MD5 sum has
   * finished downloading.
   */
  private static Map<MD5Key, List<AssetAvailableListener>> assetListenerListMap =
      new ConcurrentHashMap<MD5Key, List<AssetAvailableListener>>();

  /** Property string associated with asset name */
  public static final String NAME = "name";

  /** Used to load assets from storage */
  private static AssetLoader assetLoader = new AssetLoader();

  private static ExecutorService assetLoaderThreadPool = Executors.newFixedThreadPool(1);

  static {
    cacheDir = AppUtil.getAppHome("assetcache");
    if (cacheDir != null) {
      usePersistentCache = true;
    }
  }

  /**
   * Brute force clear asset cache... TODO: Create preferences and filter to clear cache
   * automatically by age of asset
   *
   * @author Jamz
   * @since 1.4.0.1
   */
  public static void clearCache() {
    try {
      if (cacheDir != null) {
        FileUtils.cleanDirectory(cacheDir);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Remove all existing repositories and load all the repositories from the currently loaded
   * campaign.
   */
  public static void updateRepositoryList() {
    assetLoader.removeAllRepositories();
    for (String repo : MapTool.getCampaign().getRemoteRepositoryList()) {
      assetLoader.addRepository(repo);
    }
  }

  /**
   * Determine if the asset is currently being requested. While an asset is being loaded it will be
   * marked as requested and this function will return true. Once the asset is done loading this
   * function will return false and the asset will be available from the cache.
   *
   * @param key MD5Key of asset being requested
   * @return True if asset is currently being requested, false otherwise
   */
  public static boolean isAssetRequested(MD5Key key) {
    return assetLoader.isIdRequested(key);
  }

  /**
   * Register a listener with the asset manager. The listener will be notified when the asset is
   * done loading.
   *
   * @param key MD5Key of the asset
   * @param listener Listener to notify when the asset is done loading
   */
  public static void addAssetListener(MD5Key key, AssetAvailableListener... listeners) {

    if (listeners == null || listeners.length == 0) {
      return;
    }

    List<AssetAvailableListener> listenerList = assetListenerListMap.get(key);
    if (listenerList == null) {
      listenerList = new LinkedList<AssetAvailableListener>();
      assetListenerListMap.put(key, listenerList);
    }

    for (AssetAvailableListener listener : listeners) {
      if (!listenerList.contains(listener)) {
        listenerList.add(listener);
      }
    }
  }

  public static void removeAssetListener(MD5Key key, AssetAvailableListener... listeners) {

    if (listeners == null || listeners.length == 0) {
      return;
    }

    List<AssetAvailableListener> listenerList = assetListenerListMap.get(key);
    if (listenerList == null) {
      // Nothing to do
      return;
    }

    for (AssetAvailableListener listener : listeners) {
      listenerList.remove(listener);
    }
  }

  /**
   * Determine if the asset manager has the asset. This does not tell you if the asset is done
   * downloading.
   *
   * @param asset Asset to look for
   * @return True if the asset exists, false otherwise
   */
  public static boolean hasAsset(Asset asset) {
    return hasAsset(asset.getId());
  }

  /**
   * Determine if the asset manager has the asset. This does not tell you if the asset is done
   * downloading.
   *
   * @param key
   * @return
   */
  public static boolean hasAsset(MD5Key key) {
    return assetMap.containsKey(key)
        || assetIsInPersistentCache(key)
        || assetHasLocalReference(key);
  }

  /**
   * Determines if the asset data is in memory.
   *
   * @param key MD5 sum associated with asset
   * @return True if hte asset is loaded, false otherwise
   */
  public static boolean hasAssetInMemory(MD5Key key) {
    return assetMap.containsKey(key);
  }

  /**
   * Add the asset to the asset cache. Listeners for this asset are notified.
   *
   * @param asset Asset to add to cache
   */
  public static void putAsset(Asset asset) {

    if (asset == null) {
      return;
    }

    assetMap.put(asset.getId(), asset);

    // Invalid images are represented by empty assets.
    // Don't persist those
    if (asset.getImage().length > 0) {
      putInPersistentCache(asset);
    }

    // Clear the waiting status
    assetLoader.completeRequest(asset.getId());

    // Listeners
    List<AssetAvailableListener> listenerList = assetListenerListMap.get(asset.getId());
    if (listenerList != null) {
      for (AssetAvailableListener listener : listenerList) {
        listener.assetAvailable(asset.getId());
      }

      assetListenerListMap.remove(asset.getId());
    }
  }

  /**
   * Similar to getAsset(), but does not block. It will always use the listeners to pass the data
   */
  public static void getAssetAsynchronously(
      final MD5Key id, final AssetAvailableListener... listeners) {

    assetLoaderThreadPool.submit(
        new Runnable() {
          public void run() {

            Asset asset = getAsset(id);

            // Simplest case, we already have it
            if (asset != null) {
              for (AssetAvailableListener listener : listeners) {
                listener.assetAvailable(id);
              }

              return;
            }

            // Let's get it from the server
            // As a last resort we request the asset from the server
            if (asset == null && !isAssetRequested(id)) {
              requestAssetFromServer(id, listeners);
            }
          }
        });
  }

  /**
   * Get the asset from the cache. If the asset is not currently available, will return null. Does
   * not request the asset from the server
   *
   * @param id MD5 of the asset requested
   * @return Asset object for the MD5 sum
   */
  public static Asset getAsset(MD5Key id) {

    if (id == null) {
      return null;
    }

    Asset asset = assetMap.get(id);

    if (asset == null && usePersistentCache && assetIsInPersistentCache(id)) {
      // Guaranteed that asset is in the cache.
      asset = getFromPersistentCache(id);
    }

    if (asset == null && assetHasLocalReference(id)) {

      File imageFile = getLocalReference(id);

      if (imageFile != null) {

        try {
          String name = FileUtil.getNameWithoutExtension(imageFile);
          byte[] data = FileUtils.readFileToByteArray(imageFile);

          asset = new Asset(name, data);

          // Just to be sure the image didn't change
          if (!asset.getId().equals(id)) {
            throw new IOException("Image reference did not match the requested image");
          }

          // Put it in the persistent cache so we'll find it faster next time
          putInPersistentCache(asset);
        } catch (IOException ioe) {
          // Log, but continue as if we didn't have a link
          ioe.printStackTrace();
        }
      }
    }

    return asset;
  }

  /**
   * Remove the asset from the asset cache.
   *
   * @param id MD5 of the asset to remove
   */
  public static void removeAsset(MD5Key id) {
    assetMap.remove(id);
  }

  /**
   * Enable the use of the persistent asset cache.
   *
   * @param enable True to enable the cache, false to disable
   */
  public static void setUsePersistentCache(boolean enable) {
    if (enable && cacheDir == null) {
      throw new IllegalArgumentException("Could not enable persistent cache: no such directory");
    }

    usePersistentCache = enable;
  }

  /**
   * Request that the asset be loaded from the server
   *
   * @param id MD5 of the asset to load from the server
   */
  private static void requestAssetFromServer(MD5Key id, AssetAvailableListener... listeners) {

    if (id != null) {
      addAssetListener(id, listeners);
      assetLoader.requestAsset(id);
    }
  }

  /**
   * Request that the asset be loaded from the server, blocks access while loading, use with
   * caution!
   *
   * @param id MD5 of the asset to load from the server
   * @return
   */
  public static Asset requestAssetFromServer(MD5Key id) {

    if (id != null) {
      assetLoader.requestAsset(id);
      return getAsset(id);
    }

    return null;
  }

  /**
   * Retrieve the asset from the persistent cache. If the asset is not in the cache, or loading from
   * the cache failed then this function returns null.
   *
   * @param id MD5 of the requested asset
   * @return Asset from the cache
   */
  private static Asset getFromPersistentCache(MD5Key id) {

    if (id == null || id.toString().length() == 0) {
      return null;
    }

    if (!assetIsInPersistentCache(id)) {
      return null;
    }

    File assetFile = getAssetCacheFile(id);

    try {
      byte[] data = FileUtils.readFileToByteArray(assetFile);
      Properties props = getAssetInfo(id);

      Asset asset = new Asset(props.getProperty(NAME), data);

      if (!asset.getId().equals(id)) {
        log.error("MD5 for asset " + asset.getName() + " corrupted");
      }

      assetMap.put(id, asset);

      return asset;
    } catch (IOException ioe) {
      log.error("Could not load asset from persistent cache", ioe);
      return null;
    }
  }

  /**
   * Create an asset from a file.
   *
   * @param file File to use for asset
   * @return Asset associated with the file
   * @throws IOException
   */
  public static Asset createAsset(File file) throws IOException {
    return new Asset(FileUtil.getNameWithoutExtension(file), FileUtils.readFileToByteArray(file));
  }

  /**
   * Create an asset from a file.
   *
   * @param file File to use for asset
   * @return Asset associated with the file
   * @throws IOException
   */
  public static Asset createAsset(URL url) throws IOException {
    // Create a temporary file from the downloaded URL
    File newFile = File.createTempFile("remote", null, null);
    try {
      FileUtils.copyURLToFile(url, newFile);
      if (!newFile.exists() || newFile.length() < 20) return null;
      Asset temp =
          new Asset(FileUtil.getNameWithoutExtension(url), FileUtils.readFileToByteArray(newFile));
      return temp;
    } finally {
      newFile.delete();
    }
  }

  /**
   * Return a set of properties associated with the asset.
   *
   * @param id MD5 of the asset
   * @return Properties object containing asset properties.
   */
  public static Properties getAssetInfo(MD5Key id) {

    File infoFile = getAssetInfoFile(id);
    try {

      Properties props = new Properties();
      InputStream is = new FileInputStream(infoFile);
      props.load(is);
      is.close();
      return props;

    } catch (Exception e) {
      return new Properties();
    }
  }

  /**
   * Serialize the asset into the persistent cache.
   *
   * @param asset Asset to serialize
   */
  private static void putInPersistentCache(final Asset asset) {

    if (!usePersistentCache) {
      return;
    }

    if (!assetIsInPersistentCache(asset)) {

      final File assetFile = getAssetCacheFile(asset);

      new Thread() {
        @Override
        public void run() {

          try {
            assetFile.getParentFile().mkdirs();
            // Image
            OutputStream out = new FileOutputStream(assetFile);
            out.write(asset.getImage());
            out.close();
          } catch (IOException ioe) {
            log.error("Could not persist asset while writing image data", ioe);
            return;
          } catch (NullPointerException npe) {
            // Not an issue, will update once th frame is finished loading...
            log.warn("Could not update statusbar while MapTool frame is loading.", npe);
          }
        }
      }.start();
    }
    if (!assetInfoIsInPersistentCache(asset)) {

      File infoFile = getAssetInfoFile(asset);

      try {
        // Info
        OutputStream out = new FileOutputStream(infoFile);
        Properties props = new Properties();
        props.put(NAME, asset.getName() != null ? asset.getName() : "");
        props.store(out, "Asset Info");
        out.close();

      } catch (IOException ioe) {
        log.error("Could not persist asset while writing image properties", ioe);
        return;
      }
    }
  }

  /**
   * Return the file associated with the asset, if any.
   *
   * @param id MD5 of the asset
   * @return The file associated with the asset, null if none.
   */
  private static File getLocalReference(MD5Key id) {

    File lnkFile = getAssetLinkFile(id);
    if (!lnkFile.exists()) {
      return null;
    }
    try {
      List<String> refList = FileUtil.getLines(lnkFile);

      for (String ref : refList) {
        File refFile = new File(ref);
        if (refFile.exists()) {
          return refFile;
        }
      }

    } catch (IOException ioe) {
      // Just so we know, but fall through to return null
      ioe.printStackTrace();
    }

    // Guess we don't have one
    return null;
  }

  /**
   * Store an absolute path to where this asset exists. Perhaps this should be saved in a single
   * data structure that is read/written when it's modified? This would allow the fileFilterText
   * field from the AssetPanel the option of searching through all directories and not just the
   * current one. FJE
   *
   * @param image
   */
  public static void rememberLocalImageReference(File image) throws IOException {

    MD5Key id = new MD5Key(new BufferedInputStream(new FileInputStream(image)));
    File lnkFile = getAssetLinkFile(id);

    // See if we know about this one already
    if (lnkFile.exists()) {

      List<String> referenceList = FileUtil.getLines(lnkFile);
      for (String ref : referenceList) {
        if (ref.equals(id.toString())) {

          // We already know about this one
          return;
        }
      }
    }

    // Keep track of this reference
    FileOutputStream out = new FileOutputStream(lnkFile, true); // For appending

    out.write((image.getAbsolutePath() + "\n").getBytes());

    out.close();
  }

  /**
   * Determine if the asset has a local reference
   *
   * @param id MD5 sum of the asset
   * @return True if there is a local reference, false otherwise
   */
  private static boolean assetHasLocalReference(MD5Key id) {

    return getLocalReference(id) != null;
  }

  /**
   * Determine if the asset is in the persistent cache.
   *
   * @param asset Asset to search for
   * @return True if asset is in the persistent cache, false otherwise
   */
  private static boolean assetIsInPersistentCache(Asset asset) {
    return assetIsInPersistentCache(asset.getId());
  }

  /**
   * The assets information is in the persistent cache.
   *
   * @param asset Asset to search for
   * @return True if the assets information exists in the persistent cache
   */
  private static boolean assetInfoIsInPersistentCache(Asset asset) {
    return getAssetInfoFile(asset.getId()).exists();
  }

  /**
   * Determine if the asset is in the persistent cache.
   *
   * @param id MD5 sum of the asset
   * @return True if asset is in the persistent cache, false otherwise
   * @see assetIsInPersistentCache(Asset asset)
   */
  private static boolean assetIsInPersistentCache(MD5Key id) {

    return getAssetCacheFile(id).exists() && getAssetCacheFile(id).length() > 0;
  }

  /**
   * Return the assets cache file, if any
   *
   * @param asset Asset to search for
   * @return The assets cache file, or null if it doesn't have one
   */
  public static File getAssetCacheFile(Asset asset) {
    return getAssetCacheFile(asset.getId());
  }

  /**
   * Return the assets cache file, if any
   *
   * @param is MD5 sum of the asset
   * @return The assets cache file, or null if it doesn't have one
   * @see getAssetCacheFile(Asset asset)
   */
  public static File getAssetCacheFile(MD5Key id) {
    return new File(cacheDir.getAbsolutePath() + File.separator + id);
  }

  /**
   * Return the asset info file, if any
   *
   * @param asset Asset to search for
   * @return The assets info file, or null if it doesn't have one
   */
  private static File getAssetInfoFile(Asset asset) {
    return getAssetInfoFile(asset.getId());
  }

  /**
   * Return the asset info file, if any
   *
   * @param id MD5 sum of the asset
   * @return File - The assets info file, or null if it doesn't have one
   * @see getAssetInfoFile(Asset asset)
   */
  private static File getAssetInfoFile(MD5Key id) {
    return new File(cacheDir.getAbsolutePath() + File.separator + id + ".info");
  }

  /**
   * Return the asset link file, if any
   *
   * @param id MD5 sum of the asset
   * @return File The asset link file
   */
  private static File getAssetLinkFile(MD5Key id) {
    return new File(cacheDir.getAbsolutePath() + File.separator + id + ".lnk");
  }

  /**
   * Recursively search from the rootDir, filtering files based on fileFilter, and store a reference
   * to every file seen.
   *
   * @param rootDir Starting directory to recurse from
   * @param fileFilter Only add references to image files that are allowed by the filter
   */
  public static void searchForImageReferences(File rootDir, FilenameFilter fileFilter) {
    for (File file : rootDir.listFiles()) {
      if (file.isDirectory()) {
        searchForImageReferences(file, fileFilter);
        continue;
      }
      try {
        if (fileFilter.accept(rootDir, file.getName())) {
          if (MapTool.getFrame() != null) {
            MapTool.getFrame().setStatusMessage("Caching image reference: " + file.getName());
          }
          rememberLocalImageReference(file);
        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    // Done
    if (MapTool.getFrame() != null) {
      MapTool.getFrame().setStatusMessage("");
    }
  }

  /**
   * This method accepts the name of a repository (as it appears in the CampaignProperties) and
   * updates it by adding the additional mappings that are in <code>add</code>.
   *
   * <p>This method first retrieves the mapping from the AssetLoader. It then adds in the new
   * assets. Last, it has to create the new index file. The index file should be stored in the local
   * repository cache. Note that this function <b>does not</b> update the original (network storage)
   * repository location.
   *
   * <p>If the calling function does not update the network storage for <b>index.gz</b>, a restart
   * of MapTool will lose the information when the index is downloaded again.
   *
   * @param repo name of the repository to update
   * @param add entries to add to the repository
   * @return the contents of the new repository in uploadable format
   */
  public static byte[] updateRepositoryMap(String repo, Map<String, String> add) {
    Map<String, String> repoMap = assetLoader.getRepositoryMap(repo);
    repoMap.putAll(add);
    byte[] index = assetLoader.createIndexFile(repo);
    try {
      assetLoader.storeIndexFile(repo, index);
    } catch (IOException e) {
      log.error("Couldn't save updated index to local repository cache", e);
      e.printStackTrace();
    }
    return index;
  }

  /**
   * Constructs a set of all assets in the given list of repositories, then builds a map of <code>
   * MD5Key</code> and <code>Asset</code> for all assets that do not appear in that set.
   *
   * <p>This provides the calling function with a list of all assets currently in use by the
   * campaign that do not appear in one of the listed repositories. It's entirely possible that the
   * asset is in a different repository or in none at all.
   *
   * @param repos list of repositories to exclude
   * @return Map of all known assets that are NOT in the specified repositories
   */
  public static Map<MD5Key, Asset> findAllAssetsNotInRepositories(List<String> repos) {
    // For performance reasons, we calculate the size of the Set in advance...
    int size = 0;
    for (String repo : repos) {
      size += assetLoader.getRepositoryMap(repo).size();
    }

    // Now create the aggregate of all repositories.
    Set<String> aggregate = new HashSet<String>(size);
    for (String repo : repos) {
      aggregate.addAll(assetLoader.getRepositoryMap(repo).keySet());
    }

    /*
     * The 'aggregate' now holds the sum total of all asset keys that are in repositories. Now we go through the 'assetMap' and copy over <K,V> pairs that are NOT in 'aggregate' to our 'missing'
     * Map.
     *
     * Unfortunately, the repository is a Map<String, String> while the return value is going to be a Map<MD5Key, Asset>, which means each individual entry needs to be checked and references
     * copied. If both were the same data type, converting both to Set<String> would allow for an addAll() and removeAll() and be done with it!
     */
    Map<MD5Key, Asset> missing =
        new HashMap<MD5Key, Asset>(Math.min(assetMap.size(), aggregate.size()));
    for (MD5Key key : assetMap.keySet()) {
      if (aggregate.contains(key) == false) // Not in any repository so add it.
      missing.put(key, assetMap.get(key));
    }
    return missing;
  }
}
