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

import java.net.URI;
import java.util.Optional;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.parser.ParserException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class StatSheet extends JFXPanel {

  private static final Logger log = LogManager.getLogger(StatSheet.class);
  private static final String SHEET_URI =
      "lib://net.rptools.statSheetTest/sheets/stats/basic/index.html";
  private final String htmlString;

  private WebView webView;

  public StatSheet() {
    super();
    var html = "<html><body><h1>Stat Sheet Not Found</h1></body></html>";
    try {
      URI uri = new URI(SHEET_URI);
      Optional<Library> library = new LibraryManager().getLibrary(uri.toURL()).get();
      if (library.isEmpty()) {
        throw new ParserException(
            I18N.getText("macro.function.html5.invalidURI", uri.toURL().toExternalForm()));
      }

      var str = library.get().readAsString(uri.toURL()).get();

      var document = Jsoup.parse(str);
      var head = document.select("head").first();
      if (head != null) {
        String baseURL = uri.toURL().toExternalForm().replaceFirst("\\?.*", "");
        baseURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
        var baseElement = new Element(Tag.valueOf("base"), "").attr("href", baseURL);
        if (head.children().isEmpty()) {
          head.appendChild(baseElement);
        } else {
          head.child(0).before(baseElement);
        }

        html = document.html();
      }
    } catch (Exception e) {
      log.error("Error loading stat sheet", e);
    }

    htmlString = html;

    Platform.runLater(this::setupScene);
  }

  private void setupScene() {
    StackPane root = new StackPane(); // VBox would create empty space at bottom on resize
    webView = new WebView();
    root.getChildren().add(webView);
    setScene(new Scene(root));
    setVisible(true);
  }

  public void setContent(String content) {
    Platform.runLater(
        () -> {
          webView.getEngine().loadContent(content);
        });
  }

  public String getHtmlString() {
    return htmlString;
  }
}
