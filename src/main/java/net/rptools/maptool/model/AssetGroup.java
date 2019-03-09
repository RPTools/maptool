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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.rptools.maptool.client.MapTool;

/** Model for arranging assets in a hierarchical way */
public class AssetGroup {
  private final String name;
  private final File location;

  private boolean groupsLoaded;
  private boolean filesLoaded;

  // Asset refresh data
  private Map<File, AssetTS> assetTSMap = new HashMap<File, AssetTS>();

  // Group refresh data
  private Map<File, AssetGroup> assetGroupTSMap = new HashMap<File, AssetGroup>();

  private final List<Asset> assetList = new ArrayList<Asset>();
  private final List<AssetGroup> assetGroupList = new ArrayList<AssetGroup>();

  private static final FilenameFilter IMAGE_FILE_FILTER =
      new FilenameFilter() {
        private Pattern extensionPattern = null;
        private String[] extensions = new String[] {"bmp", "gif", "png", "jpg", "jpeg"};

        public boolean accept(File dir, String name) {
          if (extensionPattern == null) {
            // Setup defaults, then override if we have Java 1.6+
            if (MapTool.JAVA_VERSION >= 1.6) {
              try {
                Class<?> imageIO = Class.forName("javax.imageio.ImageIO");
                Method getReaderFileSuffixes =
                    imageIO.getDeclaredMethod("getReaderFileSuffixes", (Class[]) null);
                Object result = getReaderFileSuffixes.invoke(null, (Object[]) null);
                extensions = (String[]) result;
                // extensions = ImageIO.getReaderFileSuffixes();
              } catch (Exception e) {
                // NoSuchMethodException
                // ClassNotFoundException
                // IllegalAccessException
                // InvocationTargetException
              }
            }
            String list = Arrays.deepToString(extensions);
            // Final result is something like: \.(jpeg|jpg|bmp|wbmp|png|gif|tiff)$
            String pattern =
                "\\." + list.replace('[', '(').replace(']', ')').replace(", ", "|") + "$";
            extensionPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
          }
          return extensionPattern.matcher(name).find();
        }
      };

  private static final FilenameFilter DIRECTORY_FILE_FILTER =
      new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return new File(dir.getPath() + File.separator + name).isDirectory();
        }
      };

  public AssetGroup(File location, String name) {
    assert name != null : "Name cannot be null";

    this.location = location;
    this.name = name;

    groupsLoaded = false;
    filesLoaded = false;
  }

  public String getName() {
    return name;
  }

  public boolean hasChildGroups() {
    loadGroupData();
    return !assetGroupList.isEmpty();
  }

  public boolean hasAssets() {
    loadFileData();
    return !assetList.isEmpty();
  }

  public int getChildGroupCount() {
    loadGroupData();
    return assetGroupList.size();
  }

  public int getAssetCount() {
    loadFileData();
    return assetList.size();
  }

  public int indexOf(Asset asset) {
    loadFileData();
    return assetList.indexOf(asset);
  }

  public int indexOf(AssetGroup group) {
    loadGroupData();
    return assetGroupList.indexOf(group);
  }

  /** */
  public List<AssetGroup> getChildGroups() {
    loadGroupData();
    return Collections.unmodifiableList(assetGroupList);
  }

  /** */
  public List<Asset> getAssets() {
    loadFileData();
    return Collections.unmodifiableList(assetList);
  }

  public void add(AssetGroup group) {
    assetGroupList.add(group);
    assetGroupTSMap.put(group.location, group);

    // Keeps the groups ordered
    Collections.sort(assetGroupList, GROUP_COMPARATOR);
  }

  public void remove(AssetGroup group) {
    assetGroupList.remove(group);
    assetGroupTSMap.remove(group.location);
  }

  @Override
  public String toString() {
    return "AssetGroup["
        + name
        + "]: "
        + assetList.size()
        + " assets and "
        + assetGroupList.size()
        + " groups";
  }

  /** Release the assets and groups so that they can be garbage collected. */
  private void clear() {
    assetTSMap.clear();
    assetGroupTSMap.clear();
    assetList.clear();

    for (AssetGroup group : assetGroupList) {
      group.clear();
    }
  }

  private synchronized void loadGroupData() {
    if (!groupsLoaded) {
      // Copy the asset and group files map so that files that were deleted go away.
      Map<File, AssetGroup> tempAssetGroupFiles = assetGroupTSMap;
      assetGroupTSMap = new HashMap<File, AssetGroup>();

      assetGroupList.clear();

      try {
        // Update subgroups
        File[] subdirArray = location.listFiles(DIRECTORY_FILE_FILTER);
        for (File subdir : subdirArray) {
          // Get the group or create a new one
          AssetGroup subgroup = tempAssetGroupFiles.get(subdir);
          if (subgroup == null) {
            subgroup = new AssetGroup(subdir, subdir.getName());
          } else {
            tempAssetGroupFiles.remove(subdir);
          }
          assetGroupTSMap.put(subdir, subgroup);
          assetGroupList.add(subgroup);
        }
        Collections.sort(assetGroupList, GROUP_COMPARATOR);
      } finally {
        // Cleanup
        for (AssetGroup group : tempAssetGroupFiles.values()) {
          group.clear();
        }
        tempAssetGroupFiles.clear();
      }
      groupsLoaded = true;
    }
  }

  private synchronized void loadFileData() {
    if (!filesLoaded) {
      // Copy the asset and group files map so that files that were deleted go away.
      Map<File, AssetTS> tempAssetFiles = assetTSMap;
      assetTSMap = new HashMap<File, AssetTS>();

      assetList.clear();

      try {
        // Update images for this group
        File[] imageFileArray = location.listFiles(IMAGE_FILE_FILTER);
        if (imageFileArray != null) {
          for (File file : imageFileArray) {
            // Latest file already in the group?
            AssetTS data = tempAssetFiles.get(file);
            if (data != null && data.lastModified == file.lastModified()) {
              assetTSMap.put(file, data);
              tempAssetFiles.remove(file);
              assetList.add(data.asset);
              continue;
            }
            // Get the asset, is it already in the game?
            try {
              Asset asset = AssetManager.createAsset(file);
              if (AssetManager.hasAsset(asset.getId())) {
                asset = AssetManager.getAsset(asset.getId());
              }
              // Add the asset
              assetTSMap.put(file, new AssetTS(asset, file.lastModified()));
              assetList.add(asset);
            } catch (IOException ioe) {
              // TODO: Handle this better
              ioe.printStackTrace();
            }
          }
        }
      } finally {
        // Cleanup
        tempAssetFiles.clear();
      }
      filesLoaded = true;
    }
  }

  /**
   * This method will cause the assets to be updated the next time one is read. The child groups are
   * updated as well.
   */
  public void updateGroup() {
    groupsLoaded = false;
    filesLoaded = false;

    for (AssetGroup group : assetGroupList) group.updateGroup();
  }

  private static class AssetTS {
    public Asset asset;
    public long lastModified;

    public AssetTS(Asset asset, long lastModified) {
      this.asset = asset;
      this.lastModified = lastModified;
    }
  }

  public static final Comparator<AssetGroup> GROUP_COMPARATOR =
      new Comparator<AssetGroup>() {
        public int compare(AssetGroup o1, AssetGroup o2) {
          return o1.getName().toUpperCase().compareTo(o2.getName().toUpperCase());
        }
      };
}
