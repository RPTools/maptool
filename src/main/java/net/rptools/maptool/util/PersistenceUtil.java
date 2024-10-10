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

import com.google.protobuf.util.JsonFormat;
import com.thoughtworks.xstream.converters.ConversionException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.FileUtil;
import net.rptools.lib.MD5Key;
import net.rptools.lib.ModelVersionManager;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.io.PackedFile;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.LookupTable;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.campaign.CampaignManager;
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.gamedata.GameDataImporter;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibrary;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.proto.AddOnLibraryListDto;
import net.rptools.maptool.model.transform.campaign.AssetNameTransform;
import net.rptools.maptool.model.transform.campaign.ExportInfoTransform;
import net.rptools.maptool.model.transform.campaign.LabelFontAndBGTransform;
import net.rptools.maptool.model.transform.campaign.PCVisionTransform;
import net.rptools.maptool.model.transform.campaign.TokenPropertyMapTransform;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** This class provides utility methods for persistence operations in the application. */
public class PersistenceUtil {
  /**
   * The log variable is an instance of the Logger class provided by the
   * LogManager.getLogger(PersistenceUtil.class) method. The Logger class is used for logging
   * purposes and can be used to log debug, info, warning, and error messages.
   */
  private static final Logger log = LogManager.getLogger(PersistenceUtil.class);

  /** The version property for a persistencd file. */
  public static final String PROP_VERSION = "version"; // $NON-NLS-1$

  /** The campaign version property for a persisted campaign. */
  public static final String PROP_CAMPAIGN_VERSION = "campaignVersion"; // $NON-NLS-1$

  /**
   * The ASSET_DIR variable represents the directory path where assets are stored within a persisted
   * file.
   */
  private static final String ASSET_DIR = "assets/"; // $NON-NLS-1$;

  /**
   * The HERO_LAB variable represents the directory path where HeroLab assets are stored within a
   * persisted file.
   */
  public static final String HERO_LAB = "herolab"; // $NON-NLS-1$

  /**
   * This variable represents the directory where drop-in library files are stored within a
   * persisted file.
   */
  private static final String DROP_IN_LIBRARY_DIR = "libraries/";

  /** The file path for the list of drop-in libraries stored within a persisted campaign file. */
  private static final String DROP_IN_LIBRARY_LIST_FILE = DROP_IN_LIBRARY_DIR + "libraries.json";

  /** The directory path for the drop-in library assets stored within a persisted campaign file. */
  private static final String DROP_IN_LIBRARY_ASSET_DIR = DROP_IN_LIBRARY_DIR + ASSET_DIR;

  /** The directory where game data is stored within a persisted campaign file. */
  private static final String GAME_DATA_DIR = "data/";

  /** Represents the file path of the game data content file within a persisted campaign file. */
  private static final String GAME_DATA_FILE = GAME_DATA_DIR + "game-data.json";

  /**
   * The version number of the campaign.
   *
   * <p>The {@code CAMPAIGN_VERSION} variable represents the version number of the campaign. It is a
   * string value that follows the standard format of major.minor.patch. Each part of the version
   * number represents a different level of changes and updates in the campaign.
   *
   * @since 1.3.70 ownerOnly added to model.Light (not backward compatible)
   * @since 1.3.75 model.Token.visibleOnlyToOwner (actually added to b74 but I didn't catch it
   *     before release)
   * @since 1.3.83 ExposedAreaData added to tokens in b78 but again not caught until b82 :(
   * @since 1.3.85 Added CampaignProperties.hasUsedFogToolbar
   * @since 1.4.0 Added lumens to LightSource class, old versions will not load unless saved as b89
   *     compatible
   * @since 1.11.0 Added add-on libraries, if loaded and saved with an older version then add-on
   *     libraries will be removed.
   * @since 1.15.0 Labels now have background color and font, will default old lables to a similar
   *     background color to what they had before.
   */
  private static final String CAMPAIGN_VERSION = "1.15.0";

  /**
   * Manager of the versioning of campaign models.
   *
   * @see ModelVersionManager
   */
  private static final ModelVersionManager campaignVersionManager = new ModelVersionManager();

  /**
   * Manager of the versioning of assets within a campaign.
   *
   * @see ModelVersionManager
   */
  private static final ModelVersionManager assetnameVersionManager = new ModelVersionManager();

  /**
   * Manages the versioning of the token in the system.
   *
   * @see ModelVersionManager
   */
  private static final ModelVersionManager tokenVersionManager = new ModelVersionManager();

  static {
    PackedFile.init(AppUtil.getAppHome("tmp")); // $NON-NLS-1$

    // Whenever a new transformation needs to be added, put the version of MT into the
    // CAMPAIGN_VERSION
    // variable, and use that as the key to the following register call.
    // This gives us a rough estimate how far backwards compatible the model is.
    // If you need sub-minor version level granularity, simply add another dot value at the end
    // (e.g. 1.3.51.1)

    // To be clear: the transformation will be applied if the file version is < the version number
    // provided.
    // Or to put it another way, the version you register should probably be equal to
    // the new value of CAMPAIGN_VERSION as of the time of your code changes.

    // FIXME We should be using javax.xml.transform to do XSL transforms with a mechanism
    // that allows the XSL to be stored externally, perhaps via a URL with version number(s)
    // as parameters. Then if some backward compatibility fix is needed it could be
    // provided dynamically via the RPTools.net web site or somewhere else. We'll add this
    // in 1.4 <wink, wink>

    // Note: This only allows you to fix up outdated XML data. If you _added_
    // variables to any persistent class which must be initialized, you need to make sure to
    // modify the object's readResolve() function, because XStream does _not_ call the
    // regular constructors! Using factory methods won't help here, since it won't be called by
    // XStream.

    // Notes: any XML earlier than this ---v will have this --v applied to it
    // V V
    campaignVersionManager.registerTransformation("1.3.51", new PCVisionTransform());
    campaignVersionManager.registerTransformation("1.3.75", new ExportInfoTransform());
    campaignVersionManager.registerTransformation(
        "1.3.78", new TokenPropertyMapTransform()); // FJE 2010-12-29
    // Label background color and font
    campaignVersionManager.registerTransformation("1.15.0", new LabelFontAndBGTransform());

    // For a short time, assets were stored separately in files ending with ".dat". As of 1.3.64,
    // they are
    // stored in separate files using the correct filename extension for the image type. This
    // transform
    // is used to convert asset filenames and not XML. Old assets with the image embedded as Base64
    // text are still supported for reading by using an XStream custom Converter. See the Asset
    // class for the annotation used to reference the converter.
    assetnameVersionManager.registerTransformation(
        "1.3.51", new AssetNameTransform("^(.*)\\.(dat)?$", "$1"));

    // This version manager is only for loading and saving tokens. Note that many (all?) of its
    // contents will
    // be used by the campaign version manager since campaigns contain tokens...
    tokenVersionManager.registerTransformation(
        "1.3.78", new TokenPropertyMapTransform()); // FJE 2010-12-29
  }

  /**
   * A class representing a game map that can be persisted.
   *
   * <p>The PersistedMap class contains information about a map, including the zone, asset map, and
   * map tool version.
   */
  public static class PersistedMap {
    /**
     * Represents a zone in a game map.
     *
     * @see Zone
     */
    public Zone zone;

    /**
     * Represents a mapping of MD5Key objects to Asset objects.
     *
     * <p>The key of the map is an MD5Key object, which represents the MD5 key for a certain set of
     * data. The value of the map is an Asset object, which represents an asset in a game map.
     */
    public Map<MD5Key, Asset> assetMap = new HashMap<MD5Key, Asset>();

    /** Represents the version of the Map Tool being used. */
    public String mapToolVersion;
  }

  /** Class representing a persisted campaign. */
  public static class PersistedCampaign {
    public Campaign campaign;
    public Map<MD5Key, Asset> assetMap = new HashMap<MD5Key, Asset>();
    public GUID currentZoneId;
    public Scale currentView;
    public String mapToolVersion;
  }

  /**
   * Saves the given Zone object in a file as a persisted map.
   *
   * @param z The Zone object to save.
   * @param mapFile The file to save the map to.
   * @throws IOException If an error occurs while saving the map.
   */
  public static void saveMap(Zone z, File mapFile) throws IOException {
    PersistedMap pMap = new PersistedMap();
    pMap.zone = z;

    // Save all assets in active use (consolidate duplicates)
    Set<MD5Key> allAssetIds = z.getAllAssetIds();
    for (MD5Key key : allAssetIds) {
      // Put in a placeholder
      pMap.assetMap.put(key, null);
    }

    try (PackedFile pakFile = new PackedFile(mapFile)) {
      saveAssets(z.getAllAssetIds(), pakFile);
      pakFile.setContent(pMap);
      pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
      pakFile.setProperty(PROP_CAMPAIGN_VERSION, CAMPAIGN_VERSION);
      pakFile.save();
    }
  }

  /**
   * Loads a persisted map from a file.
   *
   * @param mapFile The file to load the map from.
   * @return The loaded PersistedMap object representing the map, or null if the map file is not
   *     valid.
   * @throws IOException If an error occurs while loading the map.
   */
  public static PersistedMap loadMap(File mapFile) throws IOException {
    PersistedMap persistedMap = null;

    // TODO: split in a try with resources and a try/catch
    try (PackedFile pakFile = new PackedFile(mapFile)) {

      // Sanity check
      String progVersion = (String) pakFile.getProperty(PROP_VERSION);
      if (!versionCheck(progVersion)) return null;

      Object o = pakFile.getContent();
      if (o instanceof PersistedMap) {
        persistedMap = (PersistedMap) o;

        // Now load up any images that we need
        loadAssets(persistedMap.assetMap.keySet(), pakFile);

        // FJE We only want the token's graphical data, so loop through all tokens and
        // destroy all properties and macros. Keep some fields, though. Since that type
        // of object editing doesn't belong here, we just call Token.imported() and let
        // that method Do The Right Thing.
        for (Token token : persistedMap.zone.getAllTokens()) {
          token.imported();
        }
        // XXX FJE This doesn't work the way I want it to. But doing this the Right Way
        // is too much work right now. :-}
        Zone z = persistedMap.zone;
        String n = fixupZoneName(z.getName());
        z.setName(n);
        z.imported(); // Resets creation timestamp and init panel, among other things
        z.optimize(); // Collapses overlaid or redundant drawables

        // Make sure the imported zone is as fresh as possible (new IDs all the way down).
        persistedMap.zone = new Zone(z, false);
      } else {
        // TODO: Not a map but it is something with a property.xml file in it.
        // Should we have a filetype property in there?
        throw new IOException(
            I18N.getText("PersistenceUtil.warn.importWrongFileType", o.getClass().getSimpleName()));
      }
    } catch (ConversionException ce) {
      throw new IOException(I18N.getText("PersistenceUtil.error.mapVersion"), ce);
    } catch (IOException ioe) {
      throw new IOException(I18N.getText("PersistenceUtil.error.mapRead"), ioe);
    }
    return persistedMap;
  }

  /**
   * Determines whether the incoming map name is unique. If it is, it's returned as-is. If it's not
   * unique, a newly generated name is returned.
   *
   * @param n name from imported map
   * @return new name to use for the map
   */
  private static String fixupZoneName(String n) {
    List<Zone> zones = MapTool.getCampaign().getZones();
    for (Zone zone : zones) {
      if (zone.getName().equals(n)) {
        String count = n.replaceFirst("Import (\\d+) of.*", "$1"); // $NON-NLS-1$
        int next = 1;
        try {
          next = StringUtil.parseInteger(count) + 1;
          n = n.replaceFirst("Import \\d+ of", "Import " + next + " of"); // $NON-NLS-1$
        } catch (ParseException e) {
          n = "Import " + next + " of " + n; // $NON-NLS-1$
        }
      }
    }
    return n;
  }

  /**
   * Saves a Campaign to a specified File.
   *
   * @param campaign The Campaign to save.
   * @param campaignFile The File to save the Campaign to.
   * @throws IOException If an I/O error occurs during the save process.
   */
  public static void saveCampaign(Campaign campaign, File campaignFile) throws IOException {
    CodeTimer.using(
        "CampaignSave",
        saveTimer -> {
          saveTimer.setThreshold(5);

          // Strategy: save the file to a tmp location so that if there's a failure the original
          // file won't be touched. Then once we're finished, replace the old with the new.
          File tmpDir = AppUtil.getTmpDir();
          File tmpFile = new File(tmpDir.getAbsolutePath(), campaignFile.getName());
          if (tmpFile.exists()) tmpFile.delete();

          PackedFile pakFile = null;
          try {
            pakFile = new PackedFile(tmpFile);
            // Configure the meta file (this is for legacy support)
            PersistedCampaign persistedCampaign = new PersistedCampaign();

            persistedCampaign.campaign = campaign;

            // Keep track of the current view
            ZoneRenderer currentZoneRenderer = MapTool.getFrame().getCurrentZoneRenderer();
            if (currentZoneRenderer != null) {
              persistedCampaign.currentZoneId = currentZoneRenderer.getZone().getId();
              persistedCampaign.currentView = currentZoneRenderer.getZoneScale();
            }
            // Save all assets in active use (consolidate duplicates between maps)
            saveTimer.start("Collect all assets");
            Set<MD5Key> allAssetIds = campaign.getAllAssetIds();
            for (MD5Key key : allAssetIds) {
              // Put in a placeholder; all we really care about is the MD5Key for now...
              persistedCampaign.assetMap.put(key, null);
            }
            saveTimer.stop("Collect all assets");

            // And store the asset elsewhere
            saveTimer.start("Save assets");
            saveAssets(allAssetIds, pakFile);
            saveTimer.stop("Save assets");

            // Store the Drop In Libraries.
            saveTimer.start("Save Drop In Libraries");
            saveAddOnLibraries(pakFile);
            saveTimer.stop("Save Drop In Libraries");

            // Store the Game Data
            saveTimer.start("Save Game Data");
            saveGameData(pakFile);
            saveTimer.stop("Save Game Data");

            try {
              saveTimer.start("Set content");

              pakFile.setContent(persistedCampaign);
              pakFile.setProperty(PROP_CAMPAIGN_VERSION, CAMPAIGN_VERSION);
              pakFile.setProperty(PROP_VERSION, MapTool.getVersion());

              saveTimer.stop("Set content");
              saveTimer.start("Save");
              pakFile.save();
              saveTimer.stop("Save");
            } catch (OutOfMemoryError oom) {
              /*
               * This error is normally because the heap space has been exceeded while trying to save the campaign. Since MapTool caches the images used by the current Zone, and since the
               * VersionManager must keep the XML for objects in memory in order to apply transforms to them, the memory usage can spike very high during the save() operation. A common solution is
               * to switch to an empty map and perform the save from there; this causes MapTool to unload any images that it may have had cached and this can frequently free up enough memory for the
               * save() to work. We'll tell the user all this right here and then fail the save and they can try again.
               */
              saveTimer.start("OOM Close");
              pakFile.close(); // Have to close the tmpFile first on some OSes
              pakFile = null;
              tmpFile.delete(); // Delete the temporary file
              saveTimer.stop("OOM Close");
              MapTool.showError("msg.error.failedSaveCampaignOOM");
              return;
            }
          } finally {
            saveTimer.start("Close");
            try {
              if (pakFile != null) pakFile.close();
            } catch (Exception e) {
            }
            saveTimer.stop("Close");
            pakFile = null;
          }

          /*
           * Copy to the new location. Not the fastest solution in the world if renameTo() fails, but worth the safety net it provides. Jamz: So, renameTo() is causing more issues than it is worth. It
           * has a tendency to lock a file under Google Drive/Drop box causing the save to fail. Removed the for final save location...
           */
          saveTimer.start("Backup");
          File bakFile = new File(tmpDir.getAbsolutePath(), campaignFile.getName() + ".bak");

          bakFile.delete(); // Delete the last backup file...

          if (campaignFile.exists()) {
            saveTimer.start("Backup campaignFile");
            FileUtil.copyFile(campaignFile, bakFile);
            // campaignFile.delete();
            saveTimer.stop("Backup campaignFile");
          }

          saveTimer.start("Backup tmpFile");
          FileUtil.copyFile(tmpFile, campaignFile);
          tmpFile.delete();
          saveTimer.stop("Backup tmpFile");
          if (bakFile.exists()) bakFile.delete();
          saveTimer.stop("Backup");

          // Save the campaign thumbnail
          saveTimer.start("Thumbnail");
          saveCampaignThumbnail(campaignFile.getName());
          saveTimer.stop("Thumbnail");
        });
  }

  /**
   * Saves a thumbnail image for a campaign.
   *
   * @param fileName The file name of the campaign.
   */
  public static void saveCampaignThumbnail(String fileName) {
    BufferedImage screen = MapTool.takeMapScreenShot(new PlayerView(MapTool.getPlayer().getRole()));
    if (screen == null) return;

    Dimension imgSize = new Dimension(screen.getWidth(null), screen.getHeight(null));
    SwingUtil.constrainTo(imgSize, 200, 200);

    BufferedImage thumb =
        new BufferedImage(imgSize.width, imgSize.height, BufferedImage.TYPE_INT_BGR);
    Graphics2D g2d = thumb.createGraphics();
    g2d.drawImage(screen, 0, 0, imgSize.width, imgSize.height, null);
    g2d.dispose();

    File thumbFile = getCampaignThumbnailFile(fileName);

    try {
      ImageIO.write(thumb, "jpg", thumbFile);
    } catch (IOException ioe) {
      MapTool.showError("msg.error.failedSaveCampaignPreview", ioe);
    }
  }

  /**
   * Gets a file pointing to where the campaign's thumbnail image should be.
   *
   * @param fileName The campaign's file name.
   * @return the file for the campaign thumbnail
   */
  public static File getCampaignThumbnailFile(String fileName) {
    return new File(AppUtil.getAppHome("campaignthumbs"), fileName + ".jpg");
  }

  /**
   * Loads a campaign from the given campaign file.
   *
   * @param campaignFile The campaign file to load.
   * @return The loaded PersistedCampaign object representing the campaign, or null if the campaign
   *     file is not valid.
   * @throws IOException If an I/O error occurs during the loading process.
   */
  public static PersistedCampaign loadCampaign(File campaignFile) throws IOException {
    PersistedCampaign persistedCampaign = null;

    // Try the new way first
    try (PackedFile pakFile = new PackedFile(campaignFile)) {
      pakFile.setModelVersionManager(campaignVersionManager);

      // Sanity check
      String progVersion = (String) pakFile.getProperty(PROP_VERSION);
      if (!versionCheck(progVersion)) return null;

      String campaignVersion = (String) pakFile.getProperty(PROP_CAMPAIGN_VERSION);
      // This is where the campaignVersion was added
      campaignVersion = campaignVersion == null ? "1.3.50" : campaignVersion;

      try {
        persistedCampaign = (PersistedCampaign) pakFile.getContent(campaignVersion);
      } catch (ConversionException ce) {
        // Ignore the exception and check for "campaign == null" below...
        MapTool.showError("PersistenceUtil.error.campaignVersion", ce);
      } catch (ClassCastException cce) {
        // Ignore the exception and check for "campaign == null" below...
        MapTool.showWarning(
            I18N.getText(
                "PersistenceUtil.warn.campaignWrongFileType",
                pakFile.getContent().getClass().getSimpleName()));
      }

      if (persistedCampaign != null) {
        // Now load up any images that we need
        // Note that the values are all placeholders
        Set<MD5Key> allAssetIds = persistedCampaign.assetMap.keySet();
        loadAssets(allAssetIds, pakFile);
        for (Zone zone : persistedCampaign.campaign.getZones()) {
          zone.optimize();
        }

        new CampaignManager().clearCampaignData();
        loadGameData(pakFile);
        loadAddOnLibraries(pakFile);

        // for (Entry<String, Map<GUID, LightSource>> entry :
        // persistedCampaign.campaign.getLightSourcesMap().entrySet()) {
        // for (Entry<GUID, LightSource> entryLs : entry.getValue().entrySet()) {
        // System.out.println(entryLs.getValue().getName() + " :: " + entryLs.getValue().getType() +
        // " :: " + entryLs.getValue().getLumens());
        // }
        // }

        return persistedCampaign;
      }
    } catch (OutOfMemoryError oom) {
      MapTool.showError("Out of memory while reading campaign.", oom);
      return null;
    } catch (RuntimeException rte) {
      MapTool.showError("PersistenceUtil.error.campaignRead", rte);
    } catch (Error e) {
      // Probably an issue with XStream not being able to instantiate a given class
      // The old legacy technique probably won't work, but we should at least try...
      MapTool.showError("PersistenceUtil.error.unknown", e);
    }

    // No longer try to load a legacy (very early 1.3 and before) campaign

    if (persistedCampaign == null) {
      MapTool.showWarning("PersistenceUtil.warn.campaignNotLoaded");
    }
    return persistedCampaign;
  }

  /**
   * Returns the filename of the thumbnail for the given PackedFile.
   *
   * @param pakFile The PackedFile for which to retrieve the thumbnail filename.
   * @throws IOException If an error occurs while retrieving the thumbnail filename.
   */
  private static String getThumbFilename(PackedFile pakFile) throws IOException {
    if ((MapTool.getThumbnailSize().width > 50 || MapTool.getThumbnailSize().height > 50)
        && pakFile.hasFile(Token.FILE_THUMBNAIL_LARGE)) return Token.FILE_THUMBNAIL_LARGE;
    return Token.FILE_THUMBNAIL;
  }

  public static BufferedImage getTokenThumbnail(File file) throws Exception {
    BufferedImage thumb;
    try (PackedFile pakFile = new PackedFile(file)) {
      // Jamz: Lets use the Large thumbnail if needed
      String thumbFileName = getThumbFilename(pakFile);

      thumb = null;
      if (pakFile.hasFile(thumbFileName)) {
        try (InputStream is = pakFile.getFileAsInputStream(thumbFileName)) {
          thumb = ImageIO.read(is);
        }
      }
    }
    return thumb;
  }

  /**
   * Saves the given Token object to a specified File.
   *
   * @param token The Token object to save.
   * @param file The File to save the Token to.
   * @throws IOException If an I/O error occurs during the save process.
   */
  public static void saveToken(Token token, File file) throws IOException {
    saveToken(token, file, false);
  }

  /**
   * Saves the provided Token object to the specified file.
   *
   * @param token The Token object to save.
   * @param file The destination file to save the Token to.
   * @param doWait If true, wait for the image to load before saving.
   * @throws IOException If an I/O error occurs during the save process.
   */
  public static void saveToken(Token token, File file, boolean doWait) throws IOException {
    // Added a "Large Thumbnail" to support larger image grid previews
    BufferedImage image = null;
    if (doWait) {
      image = ImageManager.getImageAndWait(token.getImageAssetId());
    } else {
      image = ImageManager.getImage(token.getImageAssetId());
    }

    Dimension sz = new Dimension(image.getWidth(), image.getHeight());
    SwingUtil.constrainTo(sz, 50);

    BufferedImage thumb = new BufferedImage(sz.width, sz.height, BufferedImage.TRANSLUCENT);
    Graphics2D g = thumb.createGraphics();
    AppPreferences.renderQuality.get().setShrinkRenderingHints(g);
    g.drawImage(image, 0, 0, sz.width, sz.height, null);
    g.dispose();

    // Create large thumbnail using current ThumbnailSize or Image size,
    // which ever is smallest.
    sz =
        new Dimension(
            Math.min(image.getWidth(), MapTool.getThumbnailSize().width),
            Math.min(image.getHeight(), MapTool.getThumbnailSize().height));
    BufferedImage thumbLarge = new BufferedImage(sz.width, sz.height, BufferedImage.TRANSLUCENT);
    g = thumbLarge.createGraphics();
    AppPreferences.renderQuality.get().setShrinkRenderingHints(g);
    g.drawImage(image, 0, 0, sz.width, sz.height, null);
    g.dispose();

    try (PackedFile pakFile = new PackedFile(file)) {
      saveAssets(token.getAllImageAssets(), pakFile);
      pakFile.putFile(Token.FILE_THUMBNAIL, ImageUtil.imageToBytes(thumb, "png"));
      pakFile.putFile(Token.FILE_THUMBNAIL_LARGE, ImageUtil.imageToBytes(thumbLarge, "png"));
      pakFile.setContent(token);
      pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
      pakFile.setProperty(HERO_LAB, (token.getHeroLabData() != null));
      pakFile.save();
    }
  }

  /**
   * Loads a Token object from the specified File.
   *
   * @param file The File from which to load the Token.
   * @return The loaded Token object, or null if the file is not valid.
   * @note if there is an error loading the token, the token then a message is displayed to the user
   *     and null is returned.
   */
  public static Token loadToken(File file) {
    Token token = null;
    try (PackedFile pakFile = new PackedFile(file)) {
      pakFile.setModelVersionManager(tokenVersionManager);

      // Sanity check
      String progVersion = (String) pakFile.getProperty(PROP_VERSION);
      if (!versionCheck(progVersion)) return null;

      token = (Token) pakFile.getContent(progVersion);
      loadAssets(token.getAllImageAssets(), pakFile);
    } catch (ConversionException ce) {
      MapTool.showError("PersistenceUtil.error.tokenVersion", ce);
    } catch (IOException ioe) {
      MapTool.showError("PersistenceUtil.error.tokenRead", ioe);
    }
    return token;
  }

  /**
   * Loads a Token object from the specified URL.
   *
   * @param url The URL from which to load the Token.
   * @return The loaded Token object, or null if the URL is not valid.
   * @throws IOException If an I/O error occurs during the loading process.
   */
  public static Token loadToken(URL url) throws IOException {
    // Create a temporary file from the downloaded URL
    File newFile = new File(PackedFile.getTmpDir(), new GUID() + ".url");
    FileUtils.copyURLToFile(url, newFile);
    Token token = loadToken(newFile);
    newFile.delete();
    return token;
  }

  /**
   * Loads assets from a packed file and adds them to the asset manager.
   *
   * @param assetIds The collection of MD5 keys representing the assets to be loaded
   * @param pakFile The packed file containing the assets
   * @throws IOException If an I/O error occurs while loading the assets
   */
  private static void loadAssets(Collection<MD5Key> assetIds, PackedFile pakFile)
      throws IOException {
    // Special handling of assets: XML file to describe the Asset, but binary file for the image
    // data
    pakFile.getXStream().processAnnotations(Asset.class);

    String campaignVersion = (String) pakFile.getProperty(PROP_CAMPAIGN_VERSION);
    String progVersion = (String) pakFile.getProperty(PROP_VERSION);
    List<Asset> addToServer = new ArrayList<Asset>(assetIds.size());

    // FJE: Ugly fix for a bug I introduced in b64. :(
    boolean fixRequired = "1.3.b64".equals(progVersion);

    for (MD5Key key : assetIds) {
      if (key == null) continue;

      if (!AssetManager.hasAsset(key)) {
        String pathname = ASSET_DIR + key;
        Asset asset = null;
        if (fixRequired) {
          try (InputStream is = pakFile.getFileAsInputStream(pathname)) {
            asset =
                Asset.createAssetDetectType(
                    key.toString(), IOUtils.toByteArray(is)); // Ugly bug fix :(
          } catch (FileNotFoundException fnf) {
            // Doesn't need to be reported, since that's handled below.
          } catch (Exception e) {
            log.error("Could not load asset from 1.3.b64 file in compatibility mode", e);
          }
        } else {
          try {
            asset = pakFile.getAsset(pathname);
          } catch (Exception e) {
            // Do nothing. The asset will be 'null' and it'll be handled below.
            log.info("Exception while handling asset '" + pathname + "'", e);
          }
        }
        if (asset == null) { // Referenced asset not included in PackedFile??
          log.error("Referenced asset '" + pathname + "' not found while loading?!");
          continue;
        }
        // If the asset was marked as "broken" then ignore it completely. The end
        // result is that MT will attempt to load it from a repository again, as normal.
        if ("broken".equals(asset.getName())) {
          log.warn("Reference to 'broken' asset '" + pathname + "' not restored.");
          ImageManager.flushImage(asset);
          continue;
        }
        // pre 1.3b52 campaign files stored the image data directly in the asset serialization.
        // New XStreamConverter creates empty byte[] for image.
        if (asset.getData() == null || asset.getData().length < 4) {
          String ext = asset.getExtension();
          pathname = pathname + "." + (StringUtil.isEmpty(ext) ? "dat" : ext);
          pathname = assetnameVersionManager.transform(pathname, campaignVersion);
          try (InputStream is = pakFile.getFileAsInputStream(pathname)) {
            asset = asset.setData(IOUtils.toByteArray(is), false);
          } catch (FileNotFoundException fnf) {
            log.error("Image data for '" + pathname + "' not found?!", fnf);
            continue;
          } catch (Exception e) {
            log.error("While reading image data for '" + pathname + "'", e);
            continue;
          }
        }
        AssetManager.putAsset(asset);
        addToServer.add(asset);
      }
    }
    if (!addToServer.isEmpty()) {
      // Isn't this the same as (MapTool.getServer() == null) ? And won't there always
      // be a server? Even if we don't start one explicitly, MapTool keeps a server
      // running in the background all the time (called a "personal server") so that the rest
      // of the code is consistent with regard to client<->server operations...
      boolean server = !MapTool.isHostingServer() && !MapTool.isPersonalServer();
      if (server) {
        if (MapTool.isDevelopment())
          MapTool.showInformation(
              "Please report this:  (!isHostingServer() && !isPersonalServer()) == true");
        // If we are remotely installing this token, we'll need to send the image data to the
        // server.
        for (Asset asset : addToServer) {
          MapTool.serverCommand().putAsset(asset);
        }
      }
      addToServer.clear();
    }
  }

  /**
   * Loads the add-on libraries from the campaign file.
   *
   * @param packedFile the file to load from.
   * @throws IOException if there is a problem reading the add-o library information.
   */
  private static void loadAddOnLibraries(PackedFile packedFile) throws IOException {
    var libraryManager = new LibraryManager();
    if (!packedFile.hasFile(DROP_IN_LIBRARY_LIST_FILE)) {
      return; // No Libraries to import
    }
    var builder = AddOnLibraryListDto.newBuilder();
    JsonFormat.parser()
        .merge(
            new InputStreamReader(packedFile.getFileAsInputStream(DROP_IN_LIBRARY_LIST_FILE)),
            builder);
    var listDto = builder.build();

    for (var library : listDto.getLibrariesList()) {
      String libraryData = DROP_IN_LIBRARY_ASSET_DIR + library.getMd5Hash();
      byte[] bytes = packedFile.getFileAsInputStream(libraryData).readAllBytes();
      String libraryNamespace = library.getDetails().getNamespace();
      Asset asset = Type.MTLIB.getFactory().apply(libraryNamespace, bytes);
      if (!AssetManager.hasAsset(asset)) {
        AssetManager.putAsset(asset);
      }
      AddOnLibrary addOnLibrary = new AddOnLibraryImporter().importFromAsset(asset);
      libraryManager.registerAddOnLibrary(addOnLibrary);
    }
  }

  /**
   * Saves the add-on libraries to the provided packed file.
   *
   * @param packedFile The packed file to save the add-on libraries to.
   * @throws IOException If an error occurs while saving the add-on libraries.
   */
  private static void saveAddOnLibraries(PackedFile packedFile) throws IOException {
    // remove all drop-in libraries from the packed file first.
    for (String path : packedFile.getPaths()) {
      if (path.startsWith(DROP_IN_LIBRARY_ASSET_DIR) && !path.equals(DROP_IN_LIBRARY_ASSET_DIR)) {
        packedFile.removeFile(path);
      }
    }

    AddOnLibraryListDto dto = null;
    try {
      dto = new LibraryManager().addOnLibrariesToDto().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new IOException(e);
    }
    packedFile.putFile(
        DROP_IN_LIBRARY_LIST_FILE,
        JsonFormat.printer().print(dto).getBytes(StandardCharsets.UTF_8));

    for (var ldto : dto.getLibrariesList()) {
      Asset asset = AssetManager.getAsset(new MD5Key(ldto.getMd5Hash()));
      packedFile.putFile(DROP_IN_LIBRARY_ASSET_DIR + asset.getMD5Key().toString(), asset.getData());
    }
  }

  /**
   * Loads game data from a PackedFile.
   *
   * @param packedFile The PackedFile containing the game data to load.
   * @throws IOException If an I/O error occurs during the loading process.
   */
  private static void loadGameData(PackedFile packedFile) throws IOException {

    if (!packedFile.hasFile(GAME_DATA_FILE)) {
      return; // No game data to import
    }

    var builder = DataStoreDto.newBuilder();
    JsonFormat.parser()
        .merge(new InputStreamReader(packedFile.getFileAsInputStream(GAME_DATA_FILE)), builder);
    var dataStoreDto = builder.build();

    try {
      var dataStore = new DataStoreManager().getDefaultDataStore();
      new GameDataImporter(dataStore).importData(dataStoreDto);
    } catch (ExecutionException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * Saves the game data to the specified packed file.
   *
   * @param packedFile The packed file to save the game data to.
   * @throws IOException If an I/O error occurs while saving the game data.
   */
  private static void saveGameData(PackedFile packedFile) throws IOException {
    // Remove all the game data from the packed file first.
    for (String path : packedFile.getPaths()) {
      if (path.startsWith(GAME_DATA_DIR) && !path.equals(GAME_DATA_DIR)) {
        packedFile.removeFile(path);
      }
    }

    try {
      DataStoreManager dataStoreManager = new DataStoreManager();
      DataStoreDto dto = dataStoreManager.toDto().get();
      packedFile.putFile(
          GAME_DATA_FILE, JsonFormat.printer().print(dto).getBytes(StandardCharsets.UTF_8));

      Set<MD5Key> assets = dataStoreManager.getAssets().get();
      saveAssets(dataStoreManager.getAssets().get(), packedFile);
    } catch (ExecutionException | InterruptedException e) {
      throw new IOException(e);
    }
  }

  /**
   * Saves a collection of assets to a specified packed file.
   *
   * @param assetIds A collection of MD5Key objects representing the asset IDs to be saved.
   * @param pakFile The PackedFile object representing the file to save the assets to.
   * @throws IOException If there is an error writing the assets to the file.
   */
  private static void saveAssets(Collection<MD5Key> assetIds, PackedFile pakFile)
      throws IOException {
    // Special handling of assets: XML file to describe the Asset, but binary file for the image
    // data
    pakFile.getXStream().processAnnotations(Asset.class);

    for (MD5Key assetId : assetIds) {
      if (assetId == null) continue;

      // And store the asset elsewhere
      // As of 1.3.b64, assets are written in binary to allow them to be readable
      // when a campaign file is unpacked.
      Asset asset = AssetManager.getAsset(assetId);
      if (asset == null) {
        log.error("AssetId " + assetId + " not found while saving?!");
        continue;
      }

      String extension = asset.getExtension();
      byte[] assetData = asset.getData();
      // System.out.println("Saving AssetId " + assetId + "." + extension + " with size of " +
      // assetData.length);

      pakFile.putFile(ASSET_DIR + assetId + "." + extension, assetData);
      pakFile.putFile(ASSET_DIR + assetId + "", asset); // Does not write the image
    }
  }

  /**
   * Clears all assets in the given packed file that are located in the asset directory.
   *
   * @param pakFile the packed file from which assets will be cleared
   * @throws IOException if an I/O error occurs
   */
  private static void clearAssets(PackedFile pakFile) throws IOException {
    for (String path : pakFile.getPaths()) {
      if (path.startsWith(ASSET_DIR) && !path.equals(ASSET_DIR)) {
        pakFile.removeFile(path);
      }
    }
  }

  /**
   * Loads the legacy campaign properties from a file.
   *
   * @param file The file containing the legacy campaign properties.
   * @return The loaded legacy campaign properties.
   * @throws IOException If an I/O error occurs while reading the file.
   * @throws FileNotFoundException If the specified file does not exist.
   */
  public static CampaignProperties loadLegacyCampaignProperties(File file) throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException();
    }

    try (FileInputStream in = new FileInputStream(file)) {
      return loadCampaignProperties(in);
    }
  }

  /**
   * Loads the CampaignProperties from the given InputStream.
   *
   * @param in the InputStream to load the CampaignProperties from
   * @return the CampaignProperties instance loaded from the InputStream, or null if an error
   *     occurred
   */
  public static CampaignProperties loadCampaignProperties(InputStream in) {
    CampaignProperties props = null;
    try {
      props =
          (CampaignProperties)
              FileUtil.getConfiguredXStream()
                  .fromXML(new InputStreamReader(in, StandardCharsets.UTF_8));
    } catch (ConversionException ce) {
      MapTool.showError("PersistenceUtil.error.campaignPropertiesVersion", ce);
    }
    return props;
  }

  /**
   * Answers the question, "Can this version of MapTool load an XML file with a version string of
   * <code>progVersion</code>?"
   *
   * @param progVersion version string read from the XML file
   * @return <code>true</code> if this MT can read the file based on the version string, <code>false
   *     </code> if it can't.
   */
  private static boolean versionCheck(String progVersion) {
    boolean okay = true;
    String mtversion = ModelVersionManager.cleanVersionNumber(MapTool.getVersion());
    String cleanedProgVersion =
        ModelVersionManager.cleanVersionNumber(progVersion); // uses "0" if version is null

    // If this version of MapTool (check added in 1.3b78) is earlier than the one that created the
    // file, warn the user. :)
    if (!MapTool.isDevelopment() && ModelVersionManager.isBefore(mtversion, cleanedProgVersion)) {
      // Give the user a chance to abort this attempt to load the file
      okay = MapTool.confirm("msg.confirm.newerVersion", MapTool.getVersion(), progVersion);
    }
    return okay;
  }

  public static CampaignProperties loadCampaignProperties(File file) {
    try (PackedFile pakFile = new PackedFile(file)) {
      String progVersion = (String) pakFile.getProperty(PROP_VERSION);
      if (!versionCheck(progVersion)) return null;
      CampaignProperties props = null;
      try {
        props = (CampaignProperties) pakFile.getContent();
        loadAssets(props.getAllImageAssets(), pakFile);
      } catch (ConversionException ce) {
        MapTool.showError(I18N.getText("PersistenceUtil.error.campaignPropertiesVersion"), ce);
      } catch (IOException ioe) {
        MapTool.showError(I18N.getText("PersistenceUtil.error.campaignPropertiesRead"), ioe);
      } catch (ClassCastException cce) {
        MapTool.showWarning(
            I18N.getText(
                "PersistenceUtil.warn.campaignProperties.importWrongFileType",
                pakFile.getContent().getClass().getSimpleName()));
      }
      return props;
    } catch (IOException e) {
      try {
        // Some OSes won't allow a file to be opened twice (ugh). But we're okay here since
        // try-with-resources ensures that .close() was already called by this point.
        return loadLegacyCampaignProperties(file);
      } catch (IOException ioe) {
        MapTool.showError("PersistenceUtil.error.campaignPropertiesLegacy", ioe);
      }
    }
    return null;
  }

  public static void saveCampaignProperties(Campaign campaign, File file) throws IOException {
    // Put this in FileUtil
    if (!file.getName().contains(".")) {
      file = new File(file.getAbsolutePath() + AppConstants.CAMPAIGN_PROPERTIES_FILE_EXTENSION);
    }
    try (PackedFile pakFile = new PackedFile(file)) {
      clearAssets(pakFile);
      saveAssets(campaign.getCampaignProperties().getAllImageAssets(), pakFile);
      pakFile.setContent(campaign.getCampaignProperties());
      pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
      pakFile.save();
    }
  }

  // Macro import/export support
  public static MacroButtonProperties loadLegacyMacro(File file) throws IOException {
    if (!file.exists()) throw new FileNotFoundException();

    try (FileInputStream in = new FileInputStream(file)) {
      return loadMacro(in);
    }
  }

  public static MacroButtonProperties loadMacro(InputStream in) {
    MacroButtonProperties mbProps = null;

    try {
      mbProps =
          asMacro(
              FileUtil.getConfiguredXStream()
                  .fromXML(new InputStreamReader(in, StandardCharsets.UTF_8)));
    } catch (ConversionException ce) {
      MapTool.showError("PersistenceUtil.error.macroVersion", ce);
    }
    return mbProps;
  }

  public static MacroButtonProperties loadMacro(File file) throws IOException {
    try (PackedFile pakFile = new PackedFile(file)) {
      // Sanity check
      String progVersion = (String) pakFile.getProperty(PROP_VERSION);
      if (!versionCheck(progVersion)) return null;

      return asMacro(pakFile.getContent());
    } catch (IOException e) {
      return loadLegacyMacro(file);
    }
  }

  /**
   * Saves the macro.
   *
   * @param macroButton the button holding the macro
   * @param file the file to save
   * @throws IOException if the file can't be saved
   */
  public static void saveMacro(MacroButtonProperties macroButton, File file) throws IOException {
    try (PackedFile pakFile = new PackedFile(file)) {
      pakFile.setContent(macroButton);
      pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
      pakFile.save();
    }
  }

  public static List<MacroButtonProperties> loadLegacyMacroSet(File file) throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException();
    }
    try (FileInputStream in = new FileInputStream(file)) {
      return loadMacroSet(in);
    }
  }

  /**
   * Converts an object to a macroset, launching an error message if of an incorrect type
   *
   * @param object the object to convert
   * @return the macroset, or null if no conversion done
   */
  @SuppressWarnings("unchecked")
  private static List<MacroButtonProperties> asMacroSet(Object object) {
    if (object instanceof List<?>) {
      return (List<MacroButtonProperties>) object;
    } else {
      String className = object.getClass().getSimpleName();
      MapTool.showError(I18N.getText("PersistenceUtil.warn.macrosetWrongFileType", className));
      return null;
    }
  }

  /**
   * Converts an object to a macro, launching an error message if of an incorrect type
   *
   * @param object the object to convert
   * @return the macro, or null if no conversion done
   */
  private static MacroButtonProperties asMacro(Object object) {
    if (object instanceof MacroButtonProperties) {
      return (MacroButtonProperties) object;
    } else if (object instanceof List && ((List) object).get(0) instanceof MacroButtonProperties) {
      MapTool.showError(
          I18N.getText(
              "PersistenceUtil.warn.macroWrongFileType",
              I18N.getText("PersistenceUtil.warn.macroSet")));
    } else {
      String className = object.getClass().getSimpleName();
      MapTool.showError(I18N.getText("PersistenceUtil.warn.macroWrongFileType", className));
    }
    return null;
  }

  /**
   * Returns a macroset from an inputstream
   *
   * @param in the inputstream
   * @return the macroset
   */
  public static List<MacroButtonProperties> loadMacroSet(InputStream in) {
    List<MacroButtonProperties> macroButtonSet = null;
    try {
      macroButtonSet =
          asMacroSet(
              FileUtil.getConfiguredXStream()
                  .fromXML(new InputStreamReader(in, StandardCharsets.UTF_8)));
    } catch (ConversionException ce) {
      MapTool.showError("PersistenceUtil.error.macrosetVersion", ce);
    }
    return macroButtonSet;
  }

  @SuppressWarnings("unchecked")
  public static List<MacroButtonProperties> loadMacroSet(File file) throws IOException {
    List<MacroButtonProperties> macroButtonSet = null;
    try {
      try (PackedFile pakFile = new PackedFile(file)) {
        // Sanity check
        String progVersion = (String) pakFile.getProperty(PROP_VERSION);
        if (!versionCheck(progVersion)) return null;

        macroButtonSet = asMacroSet(pakFile.getContent());
      } catch (ConversionException ce) {
        MapTool.showError("PersistenceUtil.error.macrosetVersion", ce);
      }
    } catch (IOException e) {
      return loadLegacyMacroSet(file);
    }
    return macroButtonSet;
  }

  public static void saveMacroSet(List<MacroButtonProperties> macroButtonSet, File file)
      throws IOException {
    // Put this in FileUtil
    if (!file.getName().contains(".")) {
      file = new File(file.getAbsolutePath() + AppConstants.MACROSET_FILE_EXTENSION);
    }

    try (PackedFile pakFile = new PackedFile(file)) {
      pakFile.setContent(macroButtonSet);
      pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
      pakFile.save();
    }
  }

  // end of Macro import/export support

  // Table import/export support
  public static LookupTable loadLegacyTable(File file) throws IOException {
    if (!file.exists()) throw new FileNotFoundException();

    try (FileInputStream in = new FileInputStream(file)) {
      return loadTable(in);
    }
  }

  public static LookupTable loadTable(InputStream in) {
    LookupTable table = null;
    try {
      table =
          (LookupTable)
              FileUtil.getConfiguredXStream()
                  .fromXML(new InputStreamReader(in, StandardCharsets.UTF_8));
    } catch (ConversionException ce) {
      MapTool.showError("PersistenceUtil.error.tableVersion", ce);
    }
    return table;
  }

  public static LookupTable loadTable(File file) {

    try {
      try (PackedFile pakFile = new PackedFile(file)) {
        // Sanity check
        String progVersion = (String) pakFile.getProperty(PROP_VERSION);
        if (!versionCheck(progVersion)) return null;

        LookupTable lookupTable = (LookupTable) pakFile.getContent();
        loadAssets(lookupTable.getAllAssetIds(), pakFile);
        return lookupTable;
      } catch (ConversionException ce) {
        MapTool.showError("PersistenceUtil.error.tableVersion", ce);
      }
    } catch (IOException e) {
      try {
        return loadLegacyTable(file);
      } catch (IOException ioe) {
        MapTool.showError("PersistenceUtil.error.tableRead", ioe);
      }
    }
    return null;
  }

  public static void saveTable(LookupTable lookupTable, File file) throws IOException {
    // Put this in FileUtil
    if (!file.getName().contains(".")) {
      file = new File(file.getAbsolutePath() + AppConstants.TABLE_FILE_EXTENSION);
    }

    try (PackedFile pakFile = new PackedFile(file)) {
      pakFile.setContent(lookupTable);
      saveAssets(lookupTable.getAllAssetIds(), pakFile);
      pakFile.setProperty(PROP_VERSION, MapTool.getVersion());
      pakFile.save();
    }
  }

  public static void saveTokenImage(MD5Key assetID, File tokenSaveFile) {
    Asset asset = AssetManager.getAsset(assetID);

    if (asset == null) {
      log.error("AssetId " + assetID + " not found while saving?!");
      return;
    }

    try {
      tokenSaveFile = new File(tokenSaveFile.getAbsolutePath() + ".png");
      BufferedImage image =
          ImageUtil.createCompatibleImage(
              ImageUtil.bytesToImage(asset.getData(), tokenSaveFile.getCanonicalPath()));
      ImageIO.write(image, "png", tokenSaveFile);
      image.flush();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
