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
package net.rptools.maptool.client;

import java.io.File;
import java.io.FilenameFilter;
import javax.imageio.ImageIO;
import net.rptools.maptool.model.Token;
import net.tsc.servicediscovery.ServiceGroup;

public class AppConstants {

  public static final String APP_NAME = "MapTool";

  public static final File UNZIP_DIR = AppUtil.getAppHome("resource");

  public static final ServiceGroup SERVICE_GROUP = new ServiceGroup("maptool");

  public static final String DEFAULT_MACRO_THEMES = "net/rptools/maptool/client/ui/syntax/themes/";
  public static final File THEMES_DIR = AppUtil.getAppHome("themes/syntax/");
  public static final String DEFAULT_THEME_NAME = "Default";

  public static final String DEFAULT_UI_THEMES = "net/rptools/maptool/client/ui/themes";

  /** Directory to search for theme files. */
  public static final File UI_THEMES_DIR = AppUtil.getAppHome("themes/ui");

  public static final int NOTE_PORTRAIT_SIZE = 200;
  public static final int ASSET_SEARCH_LIMIT = 1000;

  public static final String[] IMAGE_FILES = ImageIO.getReaderFormatNames();
  public static final FilenameFilter IMAGE_FILE_FILTER =
      (dir, name) -> {
        name = name.toLowerCase();
        for (String suffix : IMAGE_FILES) {
          if (name.endsWith("." + suffix)) {
            return true;
          }
        }
        return
        // name.endsWith(".pdf") || name.endsWith(".por") ||
        name.endsWith(Token.FILE_EXTENSION); // RPTools Token format
      };

  public static final String CAMPAIGN_FILE_EXTENSION = ".cmpgn";
  public static final String CAMPAIGN_FILE_EXTENSION_ND = "cmpgn";
  public static final String CAMPAIGN_PROPERTIES_FILE_EXTENSION = ".mtprops";
  public static final String MAP_FILE_EXTENSION = ".rpmap";
  public static final String MACRO_FILE_EXTENSION = ".mtmacro";
  public static final String MACROSET_FILE_EXTENSION = ".mtmacset";
  public static final String TABLE_FILE_EXTENSION = ".mttable";

  public static final String DEFAULT_CAMPAIGN_PROPERTIES =
      "net/rptools/maptool/model/campaignProps/";
  public static final File CAMPAIGN_PROPERTIES_DIR = AppUtil.getAppHome("campaignProps");

  public static final FilenameFilter CAMPAIGN_PROPERTIES_FILE_FILTER =
      (dir, name) -> name.toLowerCase().endsWith(CAMPAIGN_PROPERTIES_FILE_EXTENSION);
  public static final String THEME_CONFIG_FILE =
      AppUtil.getAppHome("config").toPath().resolve("theme.json").toString();

  /**
   * MapTool internal frame/dialog/overlay prefix. These should only be managed by MapTool code not
   * macros.
   */
  public static final String INTERNAL_FRAME_PREFIX = "MapTool-Internal:";

  /**
   * Internal overlay for displaying information about what is under the pointer (token stat sheets
   * for example);
   */
  public static final String INTERNAL_MAP_UNDER_POINTER_HTML_OVERLAY_NAME =
      INTERNAL_FRAME_PREFIX + "Internal Map Under Pointer Overlay";
}
