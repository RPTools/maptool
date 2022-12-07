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
package net.rptools.maptool.client.swing;

import java.awt.event.MouseAdapter;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.IconMap;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ImageCacheStatusBar extends JLabel {
  private static final long serialVersionUID = -9102370395342902494L;
  private static final Logger LOGGER = LogManager.getLogger(ImageCacheStatusBar.class);
  private static final File CACHE_DIR = AppUtil.getAppHome("imageThumbs");
  private static final long POLLING_INTERVAL = 60000;
  private static Icon imageCacheIcon;

  static {
    imageCacheIcon =
        new ImageIcon(IconMap.getIcon(IconMap.Icons.STATUSBAR_IMAGE_CACHE, StatusPanel.ICON_W_H));
  }

  public ImageCacheStatusBar() {
    setIcon(imageCacheIcon);
    setToolTipText(I18N.getString("ImageCacheStatusBar.toolTip"));
    update();

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
              LOGGER.info("Clearing imageThumbs cache...");
              MapTool.getThumbnailManager().clearImageThumbCache();
              update();
              MapTool.getFrame().getAppHomeDiskSpaceStatusBar().update();
            }
          }
        });

    AppUtil.fileCheckExecutor.scheduleWithFixedDelay(
        this::update, POLLING_INTERVAL, POLLING_INTERVAL, TimeUnit.MILLISECONDS);
  }

  public void clear() {
    setText("");
  }

  public void update() {
    setText(AppUtil.getDiskSpaceUsed(CACHE_DIR));
    LOGGER.debug("ImageCacheStatusBar updated...");
  }
}
