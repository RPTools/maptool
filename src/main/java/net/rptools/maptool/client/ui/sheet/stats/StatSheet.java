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

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import javafx.application.Platform;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatSheet {

  private static final Logger log = LogManager.getLogger(StatSheet.class);
  private static final String SHEET_URI =
      "lib://net.rptools.statSheetTest/sheets/stats/basic/index.html";

  public StatSheet() {}

  public void setContent(Token token, String content) {
    MustacheFactory mf = new DefaultMustacheFactory();
    var mustache = mf.compile(new StringReader(content), "sheet");
    var output = new StringWriter();
    var context = new HashMap<String, Object>();
    context.put("name", token.getName());
    context.put("gmName", token.getGMName());
    try {
      mustache.execute(output, context).flush();
    } catch (IOException e) {
      e.printStackTrace(); // TODO: CDW
    }

    Platform.runLater(
        () -> {
          var overlay =
              MapTool.getFrame()
                  .getOverlayPanel()
                  .getOverlay(AppConstants.INTERNAL_MAP_HTML_OVERLAY_NAME);
          System.out.println(output); // TODO: CDW
          overlay.updateContents(output.toString(), true);
        });
  }

  public void clearContent() {
    Platform.runLater(
        () -> {
          var overlay =
              MapTool.getFrame()
                  .getOverlayPanel()
                  .getOverlay(AppConstants.INTERNAL_MAP_HTML_OVERLAY_NAME);
          overlay.updateContents("", true);
        });
  }
}
