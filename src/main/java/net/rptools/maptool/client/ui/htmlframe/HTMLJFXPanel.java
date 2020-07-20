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
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.*;
import javax.swing.*;

/** Class handles JFXPanel that contains a WebView that can display HTML5. */
public class HTMLJFXPanel extends JFXPanel implements HTMLPanelInterface {
  /** The WebView that displays HTML5. */
  HTMLWebViewManager webViewManager;

  /** Key adapter to block key presses from affected the rest of MapTool. */
  private static final KeyAdapter keyAdapter;

  static {
    keyAdapter =
        new KeyAdapter() {
          private void keyBlock(KeyEvent e) {
            e.consume();
          }

          @Override
          public void keyTyped(KeyEvent e) {
            keyBlock(e);
          }

          @Override
          public void keyPressed(KeyEvent e) {
            keyBlock(e);
          }

          @Override
          public void keyReleased(KeyEvent e) {
            keyBlock(e);
          }
        };
  }

  /**
   * Creates a new HTMLJFXPanel.
   *
   * @param container The container that will hold the HTML panel.
   */
  HTMLJFXPanel(final HTMLPanelContainer container, HTMLWebViewManager webViewManager) {
    this.webViewManager = webViewManager;
    Platform.runLater(() -> setupScene(container, new WebView()));
    if (container != null) {
      // Block key presses from affected the rest of MapTool. Fixes #1614.
      addKeyListener(keyAdapter);
    }
  }

  /**
   * Setup the JavaFX scene that will hold the WebView
   *
   * @param container the container to close on escape
   */
  void setupScene(HTMLPanelContainer container, WebView webview) {
    webViewManager.setupWebView(webview);

    StackPane root = new StackPane(); // VBox would create empty space at bottom on resize
    root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);"); // set stackpane transparent
    root.setPickOnBounds(false);
    root.getChildren().add(webViewManager.getWebView());
    Scene scene = new Scene(root);
    scene.setFill(javafx.scene.paint.Color.TRANSPARENT); // set scene transparent

    // ESCAPE closes the window.
    if (container != null) {
      scene.setOnKeyPressed(
          e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
              SwingUtilities.invokeLater(container::closeRequest);
            }
          });
    }
    this.setScene(scene); // set the scene on the JFXPanel
  }

  @Override
  public void addToContainer(HTMLPanelContainer container) {
    container.add(this);
  }

  @Override
  public void removeFromContainer(HTMLPanelContainer container) {
    container.remove(this);
  }

  @Override
  public void addActionListener(ActionListener listener) {
    webViewManager.addActionListener(listener);
  }

  @Override
  public void flush() {
    Platform.runLater(() -> webViewManager.flush());
  }

  @Override
  public void updateContents(final String html, boolean scrollReset) {
    Platform.runLater(() -> webViewManager.updateContents(html, scrollReset));
  }

  /**
   * Runs a javascript on the webView.
   *
   * @param script the script to run
   * @return true
   */
  @Override
  public boolean runJavascript(String script) {
    Platform.runLater(() -> webViewManager.getWebEngine().executeScript(script));
    return true;
  }
}
