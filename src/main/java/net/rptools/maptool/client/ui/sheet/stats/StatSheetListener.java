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
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.TokenHoverEnter;
import net.rptools.maptool.client.events.TokenHoverExit;
import net.rptools.maptool.model.Token.Type;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;

/**
 * This class is used to listen for token hover events and display a stat sheet when the token is
 * hovered over.
 */
public class StatSheetListener {

  /** The stat sheet to display. */
  private StatSheet statSheet;

  /**
   * Called when a token is hovered over.
   *
   * @param event The event that was fired.
   */
  @Subscribe
  public void onHoverEnter(TokenHoverEnter event) {
    if (AppPreferences.showStatSheet.get()
        && AppPreferences.showStatSheetRequiresModifierKey.get() == event.shiftDown()) {
      var ssManager = new StatSheetManager();
      if (statSheet == null && !ssManager.isLegacyStatSheet(event.token().getStatSheet())) {
        /*
         * We have to hide the control panel as we don't know how big the stat sheet is going
         * to be, and we don't want to obscure it.
         */
        MapTool.getFrame().hideControlPanel();
        statSheet = new StatSheet();
        var ssProperties = event.token().getStatSheet();
        var ssId = ssProperties.id();
        var ssRecord = ssManager.getStatSheet(ssId);
        var token = event.token();
        if (MapTool.getPlayer().isGM()
            || AppUtil.playerOwns(token)
            || token.getType() != Type.NPC) {
          statSheet.setContent(
              event.token(),
              ssManager.getStatSheetContent(ssId),
              ssRecord.entry(),
              ssProperties.location());
        }
      }
    }
  }

  /**
   * Called when a token is no longer hovered over.
   *
   * @param event The event that was fired.
   */
  @Subscribe
  public void onHoverExit(TokenHoverExit event) {
    MapTool.getFrame().showControlPanel();
    if (statSheet != null) {
      statSheet.clearContent();
      statSheet = null;
    }
  }
}
