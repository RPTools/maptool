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

import java.io.IOException;
import javafx.application.Platform;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.sheet.stats.StatSheetContext;
import net.rptools.maptool.model.sheet.stats.StatSheetLocation;
import net.rptools.maptool.util.HandlebarsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Class that represents a pop up stat sheet. */
public class StatSheet {

  /** Object for logging messages. */
  private static final Logger log = LogManager.getLogger(StatSheet.class);

  /**
   * Sets the content for the stat sheet. The content is a HTML page that is rendered using the
   * Handlebars template engine.
   *
   * @param token the token to render the stat sheet for.
   * @param content the content of the stat sheet.
   * @param location the location of the stat sheet.
   */
  public void setContent(Token token, String content, StatSheetLocation location) {
    try {
      var statSheetContext = new StatSheetContext(token, MapTool.getPlayer(), location);
      var output = new HandlebarsUtil<>(content).apply(statSheetContext);
      Platform.runLater(
          () -> {
            var overlay =
                MapTool.getFrame()
                    .getOverlayPanel()
                    .getOverlay(AppConstants.INTERNAL_MAP_UNDER_POINTER_HTML_OVERLAY_NAME);
            if (overlay != null) {
              overlay.updateContents(output, true);
            } else {
              MapTool.getFrame()
                  .getOverlayPanel()
                  .showOverlay(
                      AppConstants.INTERNAL_MAP_UNDER_POINTER_HTML_OVERLAY_NAME,
                      Integer.MIN_VALUE,
                      output,
                      null);
            }
          });
    } catch (IOException e) {
      MapTool.showError("msg.error.renderingStatSheet", e);
    }
  }

  /** Clears the content of the stat sheet. */
  public void clearContent() {
    Platform.runLater(
        () -> {
          var overlay =
              MapTool.getFrame()
                  .getOverlayPanel()
                  .getOverlay(AppConstants.INTERNAL_MAP_UNDER_POINTER_HTML_OVERLAY_NAME);
          overlay.updateContents("", true);
        });
  }
}
