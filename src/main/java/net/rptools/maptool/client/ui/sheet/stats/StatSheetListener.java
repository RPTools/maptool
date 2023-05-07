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
package net.rptools.maptool.client.ui.sheet.stats;

import com.google.common.eventbus.Subscribe;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.TokenHoverEnter;
import net.rptools.maptool.client.events.TokenHoverExit;
import net.rptools.maptool.client.ui.zone.MapOverlay;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;

public class StatSheetListener {

  private final MapOverlay overlay = new MapOverlay();
  private StatSheet statSheet;

  private static final String SHEET_URL =
      "lib://net.rptools.statSheetTest/sheets/stats/basic/index.html";
  // private final String htmlString;

  public StatSheetListener() {
    MapTool.getFrame().addMapOverlay(overlay);
    overlay.setLayout(null);
  }

  @Subscribe
  public void onHoverEnter(TokenHoverEnter event) {
    System.out.println("TokenHoverListener.onHoverEnter");
    if (statSheet == null) {
      statSheet = new StatSheet();
      overlay.add(statSheet);
      var size = overlay.getSize();
      int x = 0;
      int y = (int) size.getHeight() - 200;
      statSheet.setBounds(x, y, 200, 200);
      statSheet.setContent(
          new StatSheetManager().getStatSheetContent("net.rptools.statSheetTest", "Basic"));
    }
    statSheet.setVisible(true);
  }

  @Subscribe
  public void onHoverExit(TokenHoverExit event) {
    System.out.println("TokenHoverListener.onHoverLeave");
    if (statSheet != null) {
      statSheet.setVisible(false);
      overlay.remove(statSheet);
      statSheet = null;
    }
  }
}
