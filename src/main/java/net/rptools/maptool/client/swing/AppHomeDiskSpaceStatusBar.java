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
import javax.swing.JLabel;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppHomeDiskSpaceStatusBar extends JLabel {
  private static final long serialVersionUID = 3149155977860280954L;
  private static final Logger LOGGER = LogManager.getLogger(AppHomeDiskSpaceStatusBar.class);
  private static final File CACHE_DIR = AppUtil.getAppHome();
  private static final long POLLING_INTERVAL = 60000;
  private static Icon diskSpaceIcon;

  static {
    diskSpaceIcon = RessourceManager.getSmallIcon(Icons.STATUSBAR_FREE_SPACE);
  }

  public AppHomeDiskSpaceStatusBar() {
    setIcon(diskSpaceIcon);
    setToolTipText(I18N.getString("AppHomeDiskSpaceStatusBar.toolTip"));
    update();

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            if (e.getClickCount() == 2) {
              update();
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
    setText(AppUtil.getFreeDiskSpace(CACHE_DIR));
    LOGGER.debug("AppHomeDiskSpaceStatusBar updated...");
  }
}
