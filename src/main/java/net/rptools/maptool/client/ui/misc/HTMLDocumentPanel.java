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
package net.rptools.maptool.client.ui.misc;

import java.awt.Component;
import java.net.URL;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

/**
 * This class reads a HTML document from /net/rptools/maptool/doc/<language>/html/<docPath> (on the
 * classpath) and adds it to a JavaFX WebView which is then added to a JFXPanel.
 */
public class HTMLDocumentPanel {

  /**
   * The path to the HTML documents. The language is added to the path to get the correct document.
   */
  private static final String PATH = "/net/rptools/maptool/doc/";
  /** The JFXPanel that contains the WebView that displays the HTML document. */
  private JFXPanel jfxPanel;

  /**
   * Creates a new HTMLDocumentPanel.
   *
   * @param docPath the path to the HTML document relative to
   *     /net/rptools/maptool/doc/<language>/html/
   */
  public HTMLDocumentPanel(String docPath) {
    jfxPanel = new JFXPanel();
    Platform.runLater(() -> setupScene(docPath));
  }

  /**
   * Sets up the scene with the WebView that displays the HTML document.
   *
   * @param docPath the path to the HTML document relative to
   *     /net/rptools/maptool/doc/<language>/html/
   */
  private void setupScene(String docPath) {
    var root = new StackPane();
    var scene = new Scene(root);
    var webView = new WebView();
    String language = System.getProperty("user.language");
    String path = PATH + language + "/html/" + docPath;
    URL url = getClass().getResource(path);
    if (url == null) { // Fallback to English
      url = getClass().getResource(PATH + "en/html/" + docPath);
    }
    webView.getEngine().load(url.toString());
    webView.getEngine().setOnError(e -> e.getException().printStackTrace());
    root.getChildren().add(webView);
    jfxPanel.setScene(scene);
  }

  /**
   * Returns the content pane of this HTMLDocumentPanel.
   *
   * @return the content pane of this HTMLDocumentPanel
   */
  public Component getComponent() {
    return jfxPanel;
  }
}
