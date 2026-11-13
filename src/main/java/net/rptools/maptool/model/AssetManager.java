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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.regex.Pattern;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tika.mime.MediaType;

/**
 * This class handles the caching, loading, and downloading of assets. All assets are loaded through
 * this class.
 *
 * @author RPTools Team
 */
public class AssetManager {

  /** {@link MD5Key} to use for assets trying to specify a location outside of asset cache. */
  public static final MD5Key BAD_ASSET_LOCATION_KEY = new MD5Key("bad-location");

  /** {@link Asset}s that are required and should never be removed. */
  private static final Set<MD5Key> REQUIRED_ASSETS = Set.of(BAD_ASSET_LOCATION_KEY);

  private static final Logger log = LogManager.getLogger(AssetManager.class);

  /** Assets are associated with the MD5 sum of their raw data */
  private static final Map<MD5Key, Asset> assetMap =
      Collections.synchronizedMap(new HashMap<MD5Key, Asset>());

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

  /** Property string associated with asset type. */
  public static final String TYPE = "type";

  /** Property string associated with asset format version in .info file. */
  public static final String VERSION = "version";

  /** Property string associated with asset media type in .info file. */
  public static final String MEDIA_TYPE = "mediaType";

  /** Property string associated with asset hash algorithm in .info file. */
  public static final String HASH_ALGORITHM = "hashAlgorithm";

  /** Current info file format version. */
  public static final String CURRENT_INFO_VERSION = "1";

  /** Default hash algorithm. */
  public static final String DEFAULT_HASH_ALGORITHM = "MD5";

  /** Pattern that valid asset keys must match, keys are hex encoded hashes. */
  private static final Pattern VALID_ASSET_KEY = Pattern.compile("[0-9a-fA-F]+");

  /** Used to load assets from storage */
  private static AssetLoader assetLoader = new AssetLoader();

  private static ExecutorService assetLoaderThreadPool = ForkJoinPool.commonPool();
  private static ExecutorService assetWriterThreadPool = Executors.newFixedThreadPool(1);

  static {
    cacheDir = AppUtil.getAppHome("assetcache");
    usePersistentCache = true;
  }

  /**
   * Return the directory used for the persistent asset cache.
   *
   * @return File representing the cache directory
   */
  static File getCacheDir() {
    return cacheDir;
  }

  /**
   * Set the directory used for the persistent asset cache.
   *
   * @param dir the cache directory to use
   */
  static void setCacheDir(File dir) {
    cacheDir = dir;
  }

  /**
   * Brute force clear asset cache...
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
      log.error("Error while clearing cache", e);
    }
  }

  /**
   * Remove all existing repositories and load all the repositories from the currently loaded
   * campaign.
   */
  public static void updateRepositoryList() {
    List<String> invalidRepos = new ArrayList<>();
    assetLoader.removeAllRepositories();
    for (String repo : MapTool.getCampaign().getRemoteRepositoryList()) {
      if (!assetLoader.addRepository(repo)) {
        invalidRepos.add(repo);
      }
    }

    if (!invalidRepos.isEmpty()) {
      if (MapTool.isHostingServer()) {
        String tab = "    ";
        String repos = tab + String.join("\n" + tab, invalidRepos);
        MapTool.showError(I18N.getText("msg.error.host.inaccessibleRepo", repos));
      } else {
        invalidRepos.forEach(
            repo -> MapTool.addLocalMessage(I18N.getText("msg.error.inaccessibleRepo", repo)));
      }
    }
  }

  /**
   * Register a listener with the asset manager. The listener will be notified when the asset is
   * done loading.
   *
   * @param key MD5Key of the asset
   * @param listeners Listener to notify when the asset is done loading
   */
  public static void addAssetListener(MD5Key key, AssetAvailableListener... listeners) {

    if (listeners == null || listeners.length == 0) {
      return;
    }

    List<AssetAvailableListener> listenerList =
        assetListenerListMap.computeIfAbsent(key, k -> new LinkedList<AssetAvailableListener>());

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
    return hasAsset(asset.getMD5Key());
  }

  /**
   * Determine if the asset manager has the asset. This does not tell you if the asset is done
   * downloading.
   *
   * @param key the key
   * @return true if the asset manager has the key
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
   * @return True if the asset is loaded, false otherwise
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

    if (asset == null || asset.getMD5Key().equals(BAD_ASSET_LOCATION_KEY)) {
      return;
    }

    try {
      if (sanitizeAssetId(asset.getMD5Key()) != asset.getMD5Key()) {
        // If a different asset is returned we know this asset is invalid so dont add it
        return;
      }
    } catch (IOException e) {
      if (!asset.getMD5Key().equals(BAD_ASSET_LOCATION_KEY)) {
        log.error(I18N.getText("msg.error.errorResolvingCacheDir", asset.getMD5Key(), e));
      }
    }

    synchronized (assetMap) {
      var oldAsset = assetMap.get(asset.getMD5Key());
      if (oldAsset == null || oldAsset.getData() == null || oldAsset.getData().length == 0) {
        assetMap.put(asset.getMD5Key(), asset);
      }
    }

    // Invalid images are represented by empty assets.
    // Don't persist those
    if (asset.getData().length > 0) {
      putInPersistentCache(asset);
    }

    // Clear the waiting status
    assetLoader.completeRequest(asset.getMD5Key());

    // Listeners
    List<AssetAvailableListener> listenerList = assetListenerListMap.get(asset.getMD5Key());
    if (listenerList != null) {
      for (AssetAvailableListener listener : listenerList) {
        listener.assetAvailable(asset.getMD5Key());
      }

      assetListenerListMap.remove(asset.getMD5Key());
    }
  }

  /**
   * Similar to getAsset(), but does not block. It will always use the listeners to pass the data
   *
   * @param id MD5 of the asset requested
   * @param listeners instances of {@link AssetAvailableListener} that will be notified when the
   *     asset is available
   */
  public static void getAssetAsynchronously(
      final MD5Key id, final AssetAvailableListener... listeners) {

    assetLoaderThreadPool.submit(
        () -> {
          Asset asset = getAsset(id);

          // Simplest case, we already have it
          if (asset != null && asset.getData() != null && asset.getData().length > 0) {
            for (AssetAvailableListener listener : listeners) {
              listener.assetAvailable(id);
            }

            return;
          }

          // Let's get it from the server
          // As a last resort we request the asset from the server
          requestAssetFromServer(id, listeners);
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

    MD5Key assetId = null;
    try {
      assetId = sanitizeAssetId(id);
    } catch (IOException e) {
      log.error(I18N.getText("msg.error.errorResolvingCacheDir", id, e));
    }

    Asset asset = assetMap.get(assetId);

    if (asset == null && usePersistentCache && assetIsInPersistentCache(assetId)) {
      // Guaranteed that asset is in the cache.
      asset = getFromPersistentCache(assetId);
    }

    if (asset == null && assetHasLocalReference(assetId)) {

      File imageFile = getLocalReference(assetId);

      if (imageFile != null) {

        try {
          String name = FileUtil.getNameWithoutExtension(imageFile);
          byte[] data = FileUtils.readFileToByteArray(imageFile);

          asset = Asset.createAssetDetectType(name, data);

          // Just to be sure the image didn't change
          if (!asset.getMD5Key().equals(assetId)) {
            throw new IOException("Image reference did not match the requested image");
          }

          // Put it in the persistent cache so we'll find it faster next time
          putInPersistentCache(asset);
        } catch (IOException ioe) {
          // Log, but continue as if we didn't have a link
          log.error("Error while creating asset from link", ioe);
        }
      }
    }

    return asset;
  }

  /**
   * Return the two-character subdirectory prefix for an asset key, or null if the key is less than
   * 2 characters long.
   *
   * @param id MD5Key of the asset
   * @return two-character lowercase string prefix, or null if shorter than 2 characters
   */
  static String getSubdirectoryPrefix(MD5Key id) {
    if (id == null) {
      return null;
    }
    String keyStr = id.toString();
    if (keyStr.length() < 2) {
      return null;
    }
    return keyStr.substring(0, 2).toLowerCase(Locale.ROOT);
  }

  /**
   * Checks if the asset key is a valid hex encoded hash, and so is safe to use as a file name in
   * the asset cache.
   *
   * @param id the {@link MD5Key} to check.
   * @return {@code true} if the key is valid.
   */
  private static boolean isValidAssetKey(MD5Key id) {
    return id != null && id.toString() != null && VALID_ASSET_KEY.matcher(id.toString()).matches();
  }

  /**
   * Checks the {@link Asset} id to ensure that the is {@link Asset} is valid.
   *
   * @param md5Key the {@link MD5Key} to check.
   * @return The passed in {@code md5Key} if it is ok, otherwise the key of an {@link Asset} in the
   *     asset cache to use in its place.
   */
  private static MD5Key sanitizeAssetId(MD5Key md5Key) throws IOException {
    if (md5Key == null) {
      return null;
    }

    // Only hex keys are allowed, this means that the asset path can not escape the asset cache
    // directory or refer to a different file within it.
    if (!isValidAssetKey(md5Key)) {
      return BAD_ASSET_LOCATION_KEY;
    }

    return md5Key;
  }

  /**
   * Remove the asset from the asset cache.
   *
   * @param id MD5 of the asset to remove
   */
  public static void removeAsset(MD5Key id) {
    if (!REQUIRED_ASSETS.contains(id)) {
      assetMap.remove(id);
    }
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
   * @return Asset from the server
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
   * <p>If the asset is only found in the legacy location an attempt is made to hard link it into
   * its subdirectory, see {@link #tryLinkLegacyAssetToSubdir(MD5Key, Asset)}.
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

      String hashAlgorithm = props.getProperty(HASH_ALGORITHM, DEFAULT_HASH_ALGORITHM);
      if (!DEFAULT_HASH_ALGORITHM.equalsIgnoreCase(hashAlgorithm)) {
        // Written by a newer version, leave it alone rather than treating it as corrupt.
        log.warn("Unsupported hash algorithm {} for cached asset {}", hashAlgorithm, id);
        return null;
      }

      if (!new MD5Key(data).equals(id)) {
        log.error("MD5 for asset " + id + " corrupted; purging corrupted file");
        purgeFromPersistentCache(id);
        return null;
      }

      Asset asset = createAssetFromInfo(id, props, data);
      tryLinkLegacyAssetToSubdir(id, asset);

      synchronized (assetMap) {
        var oldAsset = assetMap.get(id);
        if (oldAsset == null || oldAsset.getData() == null || oldAsset.getData().length == 0) {
          assetMap.put(id, asset);
        }
      }

      return asset;
    } catch (IOException ioe) {
      log.error("Could not load asset from persistent cache", ioe);
      return null;
    }
  }

  /**
   * Creates the {@link Asset} for the data using the information from its .info file. The {@link
   * #TYPE} is used if present, as it can not always be determined from the media type (e.g. {@link
   * Asset.Type#DATA} or {@link Asset.Type#MTLIB}), otherwise the {@link #MEDIA_TYPE}, and finally
   * the type is detected from the data.
   *
   * @param id the key of the asset.
   * @param props the contents of the .info file.
   * @param data the data for the asset.
   * @return the {@link Asset}.
   * @throws IOException if an error occurs detecting the type.
   */
  private static Asset createAssetFromInfo(MD5Key id, Properties props, byte[] data)
      throws IOException {
    String name = props.getProperty(NAME);
    String type = props.getProperty(TYPE);
    String mediaType = props.getProperty(MEDIA_TYPE);

    if (type != null && !type.isBlank()) {
      try {
        return Asset.Type.valueOf(type).getFactory().apply(name, data);
      } catch (IllegalArgumentException e) {
        log.warn("Unknown type '{}' for asset {}", type, id);
      }
    }

    if (mediaType != null && !mediaType.isBlank()) {
      var parsedMediaType = MediaType.parse(mediaType);
      if (parsedMediaType != null) {
        return Asset.Type.fromMediaType(parsedMediaType, name).getFactory().apply(name, data);
      }
      log.warn("Unable to parse mediaType '{}' for asset {}", mediaType, id);
    }

    return Asset.createAssetDetectType(name, data);
  }

  /**
   * Removes the asset and its companion files from both the subdirectory and legacy locations of
   * the persistent cache.
   *
   * @param id MD5 of the asset to purge.
   */
  private static void purgeFromPersistentCache(MD5Key id) {
    for (File file :
        new File[] {
          getSubdirAssetFile(id),
          getLegacyAssetFile(id),
          getSubdirAssetInfoFile(id),
          getLegacyAssetInfoFile(id),
          getSubdirAssetLinkFile(id),
          getLegacyAssetLinkFile(id)
        }) {
      if (file != null) {
        file.delete();
      }
    }
  }

  /**
   * Create an asset from a file.
   *
   * @param file File to use for asset
   * @return Asset associated with the file
   * @throws IOException in case of an I/O error
   */
  public static Asset createAsset(File file) throws IOException {
    return Asset.createAssetDetectType(
        FileUtil.getNameWithoutExtension(file), FileUtils.readFileToByteArray(file));
  }

  /**
   * Create an asset from a file.
   *
   * @param url File to use for asset
   * @return Asset associated with the file
   * @throws IOException in case of an I/O error
   */
  public static Asset createAsset(URL url) throws IOException {
    return createAsset(url, null);
  }

  public static Asset createAsset(URL url, Asset.Type assetType) throws IOException {
    // Create a temporary file from the downloaded URL
    File newFile = File.createTempFile("remote", null, null);
    try {
      FileUtils.copyURLToFile(url, newFile);
      if (!newFile.exists() || newFile.length() < 20) return null;
      if (assetType != null) {
        return Asset.createAsset(
            FileUtil.getNameWithoutExtension(url),
            FileUtils.readFileToByteArray(newFile),
            assetType);
      } else {
        return Asset.createAssetDetectType(
            FileUtil.getNameWithoutExtension(url), FileUtils.readFileToByteArray(newFile));
      }
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
    Properties props = new Properties();
    if (infoFile != null && infoFile.exists()) {
      try (InputStream is = new FileInputStream(infoFile)) {
        props.load(is);
      } catch (IOException ioe) {
        // do nothing
      }
    }
    return props;
  }

  /**
   * Serialize the asset into the persistent cache.
   *
   * <p>If the asset already exists in the legacy location an attempt is made to hard link it into
   * its subdirectory, see {@link #tryLinkLegacyAssetToSubdir(MD5Key, Asset)}. If that is not
   * possible any missing .info file is written next to the legacy asset.
   *
   * @param asset Asset to serialize
   */
  private static void putInPersistentCache(final Asset asset) {

    if (!usePersistentCache) {
      return;
    }

    if (!assetIsInPersistentCache(asset)) {
      final File assetFile = getAssetCacheFile(asset);

      assetWriterThreadPool.submit(
          () -> {
            assetFile.getParentFile().mkdirs();

            try (var operation = new AssetWriteRenameOperation(assetFile)) {
              try (var temporaryFileStream = new FileOutputStream(operation.temporaryFile)) {
                temporaryFileStream.write(asset.getData());
              }

              // Now that the data is in a file, we move it to its final resting place.
              operation.commit();
            } catch (IOException ioe) {
              log.error("Could not persist asset while writing image data", ioe);
            } catch (NullPointerException npe) {
              // Not an issue, will update once th frame is finished loading...
              log.warn("Could not update statusbar while MapTool frame is loading.", npe);
            }
          });
    } else {
      tryLinkLegacyAssetToSubdir(asset.getMD5Key(), asset);
    }
    if (!assetInfoIsInPersistentCache(asset)) {
      try {
        writeAssetInfo(getAssetInfoFile(asset), asset);
      } catch (IOException ioe) {
        log.error("Could not persist asset while writing image properties", ioe);
      }
    }
  }

  /**
   * Writes the .info file for the asset.
   *
   * @param infoFile the .info file to write.
   * @param asset the asset to write the information for.
   * @throws IOException if an error occurs writing the file.
   */
  private static void writeAssetInfo(File infoFile, Asset asset) throws IOException {
    if (infoFile.getParentFile() != null) {
      infoFile.getParentFile().mkdirs();
    }
    Properties props = new Properties();
    props.put(VERSION, CURRENT_INFO_VERSION);
    props.put(NAME, asset.getName() != null ? asset.getName() : "");
    props.put(TYPE, asset.getType().name());
    props.put(MEDIA_TYPE, asset.getMediaType().toString());
    props.put(HASH_ALGORITHM, DEFAULT_HASH_ALGORITHM);
    try (OutputStream out = new FileOutputStream(infoFile)) {
      props.store(out, "Asset Info");
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
    if (lnkFile == null || !lnkFile.exists()) {
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
      log.error("Error while loading local reference file", ioe);
    }

    // Guess we don't have one
    return null;
  }

  /**
   * Store an absolute path to where this asset exists. Perhaps this should be saved in a single
   * data structure that is read/written when it's modified? This would allow the fileFilterText
   * field from the AssetPanel the option of searching through all directories and not just the
   * current one.
   *
   * @param image the file to be stored
   * @throws IOException in case of an I/O error
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
    if (lnkFile.getParentFile() != null) {
      lnkFile.getParentFile().mkdirs();
    }
    try (FileOutputStream out = new FileOutputStream(lnkFile, true)) { // For appending
      out.write((image.getAbsolutePath() + "\n").getBytes());
    }
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
    return assetIsInPersistentCache(asset.getMD5Key());
  }

  /**
   * Return the subdirectory asset file for the given asset id.
   *
   * @param id MD5 sum of the asset
   * @return File in the subdirectory, or in root if id length &lt; 2, or null if the id is invalid
   */
  static File getSubdirAssetFile(MD5Key id) {
    return getSubdirCacheFile(id, "");
  }

  /**
   * Return the legacy (root) asset file for the given asset id.
   *
   * @param id MD5 sum of the asset
   * @return File in the root cache directory, or null if the id is invalid
   */
  static File getLegacyAssetFile(MD5Key id) {
    return getLegacyCacheFile(id, "");
  }

  /**
   * Return the subdirectory asset info file for the given asset id.
   *
   * @param id MD5 sum of the asset
   * @return File in the subdirectory, or in root if id length &lt; 2, or null if the id is invalid
   */
  static File getSubdirAssetInfoFile(MD5Key id) {
    return getSubdirCacheFile(id, ".info");
  }

  /**
   * Return the legacy (root) asset info file for the given asset id.
   *
   * @param id MD5 sum of the asset
   * @return File in the root cache directory, or null if the id is invalid
   */
  static File getLegacyAssetInfoFile(MD5Key id) {
    return getLegacyCacheFile(id, ".info");
  }

  /**
   * Return the subdirectory asset link file for the given asset id.
   *
   * @param id MD5 sum of the asset
   * @return File in the subdirectory, or in root if id length &lt; 2, or null if the id is invalid
   */
  static File getSubdirAssetLinkFile(MD5Key id) {
    return getSubdirCacheFile(id, ".lnk");
  }

  /**
   * Return the legacy (root) asset link file for the given asset id.
   *
   * @param id MD5 sum of the asset
   * @return File in the root cache directory, or null if the id is invalid
   */
  static File getLegacyAssetLinkFile(MD5Key id) {
    return getLegacyCacheFile(id, ".lnk");
  }

  /**
   * Return the file in the asset's subdirectory of the cache.
   *
   * @param id MD5 sum of the asset
   * @param suffix the suffix to add to the asset id for the file name.
   * @return File in the subdirectory, or in root if id length &lt; 2, or null if the id is invalid
   */
  private static File getSubdirCacheFile(MD5Key id, String suffix) {
    if (!isValidAssetKey(id) || cacheDir == null) {
      return null;
    }
    String prefix = getSubdirectoryPrefix(id);
    if (prefix == null) {
      return getLegacyCacheFile(id, suffix);
    }
    return new File(new File(cacheDir, prefix), id + suffix);
  }

  /**
   * Return the file in the legacy (root) location of the cache.
   *
   * @param id MD5 sum of the asset
   * @param suffix the suffix to add to the asset id for the file name.
   * @return File in the root cache directory, or null if the id is invalid
   */
  private static File getLegacyCacheFile(MD5Key id, String suffix) {
    if (!isValidAssetKey(id) || cacheDir == null) {
      return null;
    }
    return new File(cacheDir, id + suffix);
  }

  /**
   * The assets information is in the persistent cache. Checks both the subdirectory and legacy
   * locations.
   *
   * @param asset Asset to search for
   * @return True if the assets information exists in the persistent cache
   */
  private static boolean assetInfoIsInPersistentCache(Asset asset) {
    if (asset == null || asset.getMD5Key() == null) {
      return false;
    }
    File subDirInfoFile = getSubdirAssetInfoFile(asset.getMD5Key());
    if (subDirInfoFile != null && subDirInfoFile.exists() && subDirInfoFile.length() > 0) {
      return true;
    }
    File legacyInfoFile = getLegacyAssetInfoFile(asset.getMD5Key());
    return legacyInfoFile != null && legacyInfoFile.exists() && legacyInfoFile.length() > 0;
  }

  /**
   * Determine if the asset is in the persistent cache. Checks both the subdirectory and legacy
   * locations.
   *
   * @param id MD5 sum of the asset
   * @return True if asset is in the persistent cache, false otherwise
   * @see this#assetIsInPersistentCache(Asset)
   */
  private static boolean assetIsInPersistentCache(MD5Key id) {
    if (id == null) {
      return false;
    }
    File subDirFile = getSubdirAssetFile(id);
    if (subDirFile != null && subDirFile.exists() && subDirFile.length() > 0) {
      return true;
    }
    File legacyFile = getLegacyAssetFile(id);
    return legacyFile != null && legacyFile.exists() && legacyFile.length() > 0;
  }

  /**
   * Return the assets cache file, if any
   *
   * @param asset Asset to search for
   * @return The assets cache file, or null if it doesn't have one
   */
  public static File getAssetCacheFile(Asset asset) {
    return getAssetCacheFile(asset.getMD5Key());
  }

  /**
   * Loads the asset, and waits for the asset to load.
   *
   * @param assetId Load image data from this asset
   * @return Asset Return the loaded asset
   */
  public static Asset getAssetAndWait(final MD5Key assetId) {
    if (assetId == null) {
      return null;
    }
    Asset asset = null;
    final CountDownLatch loadLatch = new CountDownLatch(1);
    getAssetAsynchronously(
        assetId,
        (key) -> {
          // If we're here then the image has just finished loading
          // release the blocked thread
          log.debug("Countdown: " + assetId);
          loadLatch.countDown();
        });
    if (asset == null) {
      try {
        log.debug("Wait for:  " + assetId);
        loadLatch.await();
        // This time we'll get the cached version
        asset = getAsset(assetId);
      } catch (InterruptedException ie) {
        log.error(
            "getAssetAndWait(" + assetId + "):  asset not resolved; InterruptedException", ie);
        asset = null;
      }
    }
    return asset;
  }

  /**
   * Return the assets cache file, if any. Checks the subdirectory location first, then the legacy
   * (root) location. If it doesn't exist in either, returns the subdirectory location (or legacy if
   * key length &lt; 2) for writes.
   *
   * @param id MD5 sum of the asset
   * @return The assets cache file, or null if id is not a valid asset key
   * @see AssetManager#getAssetCacheFile(Asset asset)
   */
  public static File getAssetCacheFile(MD5Key id) {
    File subDirFile = getSubdirAssetFile(id);
    if (subDirFile == null || subDirFile.exists()) {
      return subDirFile;
    }
    File legacyFile = getLegacyAssetFile(id);
    return legacyFile.exists() ? legacyFile : subDirFile;
  }

  /**
   * Attempts to create a hard link in the subdirectory for an asset that only exists in the legacy
   * (root) location, and writes a new .info file in that subdirectory. If the hard link creation
   * fails, no copy or move is attempted and no .info file is created.
   *
   * @param id MD5Key of the asset
   * @param asset the asset, used to write the .info file
   * @return {@code true} if the asset is now in the subdirectory
   */
  static boolean tryLinkLegacyAssetToSubdir(MD5Key id, Asset asset) {
    if (getSubdirectoryPrefix(id) == null) {
      return false;
    }
    File subDirFile = getSubdirAssetFile(id);
    File legacyFile = getLegacyAssetFile(id);
    if (subDirFile == null || subDirFile.exists() || !legacyFile.exists()) {
      return false;
    }

    try {
      subDirFile.getParentFile().mkdirs();
      Files.createLink(subDirFile.toPath(), legacyFile.toPath());
    } catch (FileAlreadyExistsException e) {
      // Created concurrently by another thread, continue
    } catch (UnsupportedOperationException | IOException | SecurityException e) {
      log.debug("Could not create hard link from legacy asset {} to {}", legacyFile, subDirFile, e);
      return false;
    }

    File subDirInfoFile = getSubdirAssetInfoFile(id);
    if (!subDirInfoFile.exists()) {
      try {
        writeAssetInfo(subDirInfoFile, asset);
      } catch (IOException ioe) {
        log.error("Could not write .info file after hard linking to {}", subDirInfoFile, ioe);
      }
    }

    return true;
  }

  /**
   * Return the asset info file, if any
   *
   * @param asset Asset to search for
   * @return The assets info file, or null if it doesn't have one
   */
  static File getAssetInfoFile(Asset asset) {
    return getAssetInfoFile(asset.getMD5Key());
  }

  /**
   * Return the asset info file, if any. Checks the subdirectory location first, then the legacy
   * (root) location. If neither exists, places it next to the asset data file (legacy if asset is
   * legacy, else subdir).
   *
   * @param id MD5 sum of the asset
   * @return File - The assets info file, or null if id is not a valid asset key
   * @see this#getAssetInfoFile(Asset asset)
   */
  static File getAssetInfoFile(MD5Key id) {
    return getCompanionFile(getSubdirAssetInfoFile(id), getLegacyAssetInfoFile(id), id);
  }

  /**
   * Return the asset link file, if any. Checks the subdirectory location first, then the legacy
   * (root) location. If neither exists, places it next to the asset data file (legacy if asset is
   * legacy, else subdir).
   *
   * @param id MD5 sum of the asset
   * @return File The asset link file, or null if id is not a valid asset key
   */
  static File getAssetLinkFile(MD5Key id) {
    return getCompanionFile(getSubdirAssetLinkFile(id), getLegacyAssetLinkFile(id), id);
  }

  /**
   * Return the companion (.info, .lnk) file to use for an asset. Checks the subdirectory location
   * first, then the legacy (root) location. If neither exists, places it next to the asset data
   * file (legacy if asset is legacy, else subdir).
   *
   * @param subDirFile the companion file in the subdirectory
   * @param legacyFile the companion file in the legacy location
   * @param id MD5 sum of the asset
   * @return the companion file to use, or null if id is not a valid asset key
   */
  private static File getCompanionFile(File subDirFile, File legacyFile, MD5Key id) {
    if (subDirFile == null || subDirFile.exists()) {
      return subDirFile;
    }
    if (legacyFile.exists()) {
      return legacyFile;
    }
    if (!getSubdirAssetFile(id).exists() && getLegacyAssetFile(id).exists()) {
      return legacyFile;
    }
    return subDirFile;
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
        log.error("Error while searching for image", ioe);
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
    Set<MD5Key> aggregate = new HashSet<>(size);
    for (String repo : repos) {
      for (String key : assetLoader.getRepositoryMap(repo).keySet()) {
        aggregate.add(new MD5Key(key));
      }
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

    for (var entry : assetMap.entrySet()) {
      if (aggregate.contains(entry.getKey()) == false) // Not in any repository so add it.
      missing.put(entry.getKey(), entry.getValue());
    }
    return missing;
  }

  /** Helper type to handle creating and moving temporary files. */
  private static class AssetWriteRenameOperation implements AutoCloseable {
    private final File assetFile;
    private final File temporaryFile;

    public AssetWriteRenameOperation(File assetFile) throws IOException {
      this.assetFile = assetFile;
      // Placing the temp file in the cache dir means it will be on the same filesystem in typical
      // cases.
      this.temporaryFile =
          Files.createTempFile(assetFile.getParentFile().toPath(), "tmp.", "").toFile();
    }

    /**
     * Move the temporary file to its final location.
     *
     * <p>The move will be done atomically if possible, but a non-atomic move may be used as a
     * fallback.
     *
     * @throws IOException If the move fails.
     */
    public void commit() throws IOException {
      try {
        Files.move(temporaryFile.toPath(), assetFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException e) {
        Files.move(temporaryFile.toPath(), assetFile.toPath());
      }
    }

    /**
     * Clean up the temporary file.
     *
     * <p>If the operation has been committed, there is no longer a temporary file and this does
     * nothing.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
      Files.deleteIfExists(temporaryFile.toPath());
    }
  }
}
