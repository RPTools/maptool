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
import net.rptools.lib.swing.ImageBorder;
import net.rptools.maptool.model.Token;
import net.tsc.servicediscovery.ServiceGroup;

public class AppConstants {

  public static final String APP_NAME = "MapTool";

  public static final File UNZIP_DIR = AppUtil.getAppHome("resource");

  public static final ServiceGroup SERVICE_GROUP = new ServiceGroup("maptool");

  public static final String DEFAULT_MACRO_THEMES = "net/rptools/maptool/client/ui/syntax/themes/";
  public static final File THEMES_DIR = AppUtil.getAppHome("themes/syntax/");

  public static final ImageBorder GRAY_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/gray");
  public static final ImageBorder SHADOW_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/shadow");
  public static final ImageBorder HIGHLIGHT_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/highlight");
  public static final ImageBorder GREEN_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/green");
  public static final ImageBorder YELLOW_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/yellow");
  public static final ImageBorder PURPLE_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/purple");
  public static final ImageBorder FOW_TOOLS_BORDER =
      new ImageBorder("net/rptools/maptool/client/image/border/fowtools");
  public static final int NOTE_PORTRAIT_SIZE = 200;
  public static final int ASSET_SEARCH_LIMIT = 1000;

  public static final FilenameFilter IMAGE_FILE_FILTER =
      new FilenameFilter() {
        public boolean accept(File dir, String name) {
          name = name.toLowerCase();
          // I can drop TIFF files into TokenTool and it works. Should that extension be
          // added here? The question is really whether the Java2D libraries can read TIFF
          // or my desktop GUI is converting the image during the drop operation... FJE
          return name.endsWith(".bmp")
              || name.endsWith(".png")
              || name.endsWith(".gif")
              || name.endsWith(".jpg")
              || name.endsWith(".jpeg")
              ||
              // name.endsWith(".pdf") || name.endsWith(".por") ||
              name.endsWith(Token.FILE_EXTENSION); // RPTools Token format
        }
      };

  public static final String CAMPAIGN_FILE_EXTENSION = ".cmpgn";
  public static final String CAMPAIGN_PROPERTIES_FILE_EXTENSION = ".mtprops";
  public static final String MAP_FILE_EXTENSION = ".rpmap";
  public static final String MACRO_FILE_EXTENSION = ".mtmacro";
  public static final String MACROSET_FILE_EXTENSION = ".mtmacset";
  public static final String TABLE_FILE_EXTENSION = ".mttable";
}
