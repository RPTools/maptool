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

import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.parser.ParserException;
import net.sf.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.*;

/** Class handles JFXPanel that contains a WebView that can display HTML5. */
public class HTMLJFXPanel extends JFXPanel implements HTMLPanelInterface {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLJFXPanel.class);

  /** The action listeners for the container. */
  private ActionListener actionListeners;

  /** The WebEngine of the WebView. */
  private WebEngine webEngine;

  /** JS that disables all following external script downloads. */
  private static final String BLOCK_EXT_JS_SCRIPT =
      "<script>"
          + "const config = { attributes: true, childList: true, subtree: true };"
          + "const observer = new MutationObserver(mutations => {"
          + "    mutations.forEach(({ addedNodes }) => {"
          + "        addedNodes.forEach(node => {"
          + "            if(node.nodeType === 1 && node.tagName === 'SCRIPT' && node.src !== '') {"
          + "                    node.src = '...';"
          + "                    node.type = 'javascript/blocked';"
          + "                    node.parentElement.removeChild(node);"
          + "            }"
          + "        });"
          + "    });"
          + "});"
          + "observer.observe(document.documentElement, config);"
          + "</script>";

  /** JS that replaces all non-asset sources by "EXTERNAL_LINK_PROHIBITED". */
  private static final String BLOCK_EXT_SRC_SCRIPT =
      "      var nodeList = document.querySelectorAll('[src]');"
          + "for(var i = 0; i < nodeList.length; i++) {  "
          + "    var element = nodeList[i];"
          + "    var src = element.getAttribute('src').trim().toLowerCase();"
          + "    if(!src.startsWith('asset')){"
          + "        element.setAttribute('src', 'EXTERNAL_LINK_PROHIBITED');"
          + "        element.alt = 'EXTERNAL_LINK_PROHIBITED';"
          + "        element.innerHTML = 'EXTERNAL_LINK_PROHIBITED';"
          + "}}";

  /**
   * JS that replaces non-macro, non-hyperlink, non-internal css hrefs by
   * "EXTERNAL_LINK_PROHIBITED".
   */
  private static final String BLOCK_EXT_HREF_SCRIPT =
      "      var nodeList = document.querySelectorAll('[href]');"
          + "for(var i = 0; i < nodeList.length; i++) {  "
          + "    var element = nodeList[i];"
          + "    var href = element.getAttribute('href').trim().toLowerCase();"
          + "    var tag = element.tagName;"
          + "    if(!href.startsWith('macro') && (tag != 'A') && typeof href === 'string'){"
          + "        var matches = href.match(/@/gi);"
          + "        if(matches == null || matches.length != 1) {"
          + "            element.setAttribute('href', 'EXTERNAL_LINK_PROHIBITED');"
          + "            element.innerHTML = 'EXTERNAL_LINK_PROHIBITED';"
          + "}}}";

  /**
   * Creates a new HTMLJFXPanel.
   *
   * @param container The container that will hold the HTML panel.
   */
  HTMLJFXPanel(final HTMLPanelContainer container) {
    Platform.runLater(
        () -> {
          WebView webView = new WebView();
          webView.setContextMenuEnabled(false); // disable "reload' right click menu.
          webEngine = webView.getEngine();
          webEngine.getLoadWorker().stateProperty().addListener(this::changed);

          // For alert / confirm JS events.
          webEngine.setOnAlert(event -> showAlert(event.getData()));
          webEngine.setConfirmHandler(HTMLJFXPanel::showConfirm);

          StackPane root = new StackPane(); // VBox would create empty space at bottom on resize
          root.getChildren().add(webView);
          Scene scene = new Scene(root);

          // ESCAPE closes the window.
          scene.setOnKeyPressed(
              e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                  SwingUtilities.invokeLater(container::closeRequest);
                }
              });
          this.setScene(scene); // set the scene on the JFXPanel
        });
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
    actionListeners = AWTEventMulticaster.add(actionListeners, listener);
  }

  @Override
  public void flush() {
    Platform.runLater(
        () -> {
          // Delete cache for navigate back
          webEngine.load("about:blank");
          // Delete cookies
          java.net.CookieHandler.setDefault(new java.net.CookieManager());
        });
  }

  @Override
  public void updateContents(final String html) {
    Platform.runLater(
        () -> {
          webEngine.loadContent(BLOCK_EXT_JS_SCRIPT + html);
        });
  }

  /**
   * Show an alert message.
   *
   * @param message the message to display.
   */
  private static void showAlert(String message) {
    javafx.scene.control.Dialog<ButtonType> alert = new javafx.scene.control.Dialog<>();
    alert.getDialogPane().setContentText(message);
    alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
    alert.showAndWait();
  }

  /**
   * Show a confirmation box.
   *
   * @param message the message to display.
   * @return boolean true if OK was pressed, false otherwise.
   */
  private static boolean showConfirm(String message) {
    javafx.scene.control.Dialog<ButtonType> confirm = new javafx.scene.control.Dialog<>();
    confirm.getDialogPane().setContentText(message);
    confirm.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Optional<ButtonType> result = confirm.showAndWait();
    return (result.isPresent() && result.get() == ButtonType.OK);
  }

  /**
   * Check if the worker succeeded, then deal with the MapTool macro references and add event
   * listeners for the buttons and hyperlinks.
   *
   * @param observable what is observed.
   * @param oldState the previous state of the worker.
   * @param newState the new state of the worker.
   */
  private void changed(
      ObservableValue<? extends Worker.State> observable,
      Worker.State oldState,
      Worker.State newState) {
    if (newState == Worker.State.SUCCEEDED) {
      // Event listener for the href macro link clicks.
      EventListener listenerA =
          event -> {
            if (log.isDebugEnabled()) {
              log.debug(
                  "Responding to hyperlink event: " + event.getType() + " " + event.toString());
            }

            final String href = ((Element) event.getCurrentTarget()).getAttribute("href");
            if (href != null && !href.equals("")) {
              String href2 = href.trim().toLowerCase();
              if (href2.startsWith("macro")) {
                // ran as macroLink;
                SwingUtilities.invokeLater(() -> MacroLinkFunction.runMacroLink(href));
              } else if (!href2.startsWith("#") && !href2.startsWith("javascript")) {
                // non-macrolink, non-anchor link, non-javascript code
                MapTool.showDocument(href); // show in usual browser
              }
            }
            event.preventDefault(); // don't change webview
          };
      // Event listener for form submission.
      EventListener listenerSubmit =
          event -> {
            getDataAndSubmit((HTMLFormElement) event.getCurrentTarget());
          };

      Document doc = webEngine.getDocument();
      NodeList nodeList;

      // Set the title if using <title>.
      nodeList = doc.getElementsByTagName("title");
      if (nodeList.getLength() > 0) {
        doChangeTitle(nodeList.item(0).getTextContent());
      }

      // Handle the <meta> tags.
      nodeList = doc.getElementsByTagName("meta");
      for (int i = 0; i < nodeList.getLength(); i++) {
        handleMetaTag((Element) nodeList.item(i));
      }

      // Add event handlers for hyperlinks.
      nodeList = doc.getElementsByTagName("a");
      for (int i = 0; i < nodeList.getLength(); i++) {
        EventTarget node = (EventTarget) nodeList.item(i);
        node.addEventListener("click", listenerA, false);
      }

      // Set the "submit" handler to get the data on submission.
      nodeList = doc.getElementsByTagName("form");
      for (int i = 0; i < nodeList.getLength(); i++) {
        Element node = (Element) nodeList.item(i);
        ((EventTarget) node).addEventListener("submit", listenerSubmit, false);
      }

      // Replace src attributes that aren't assetIds, as per #972
      webEngine.executeScript(BLOCK_EXT_SRC_SCRIPT);

      // Replace href attributes that aren't macros or hyperlinks, as per #972
      webEngine.executeScript(BLOCK_EXT_HREF_SCRIPT);

      // Deal with CSS and events of <link>.
      nodeList = doc.getElementsByTagName("link");
      for (int i = 0; i < nodeList.getLength(); i++) {
        fixLink(nodeList.item(i), doc);
      }
    }
  }

  /**
   * Handle a request to register a macro callback.
   *
   * @param type The type of event.
   * @param link The link to the macro.
   */
  private void doRegisterMacro(String type, String link) {
    if (actionListeners != null) {
      if (log.isDebugEnabled()) {
        log.debug("registerMacro event: type='" + type + "' link='" + link + "'");
      }
      actionListeners.actionPerformed(
          new HTMLActionEvent.RegisterMacroActionEvent(this, type, link));
    }
  }

  /**
   * Handles the CSS and the events of a link.
   *
   * @param node the node of the link tag.
   * @param doc the document to update with the modified link.
   */
  private void fixLink(org.w3c.dom.Node node, org.w3c.dom.Document doc) {
    org.w3c.dom.NamedNodeMap attr = node.getAttributes();

    org.w3c.dom.Node rel = attr.getNamedItem("rel");
    org.w3c.dom.Node type = attr.getNamedItem("type");
    org.w3c.dom.Node href = attr.getNamedItem("href");

    if (rel != null && type != null && href != null) {
      String content = href.getTextContent();
      if (rel.getTextContent().equalsIgnoreCase("stylesheet")) {
        String[] vals = content.split("@");
        if (vals.length != 2) {
          return;
        }
        try {
          String cssText = MapTool.getParser().getTokenLibMacro(vals[0], vals[1]);
          Element styleNode = doc.createElement("style");
          Text styleContent = doc.createTextNode(cssText);
          styleNode.appendChild(styleContent);
          // Add the style sheet node to the head.
          doc.getDocumentElement().getElementsByTagName("head").item(0).appendChild(styleNode);
        } catch (ParserException e) {
          // Do nothing
        }
      } else if (type.getTextContent().equalsIgnoreCase("macro")) {
        if (rel.getTextContent().equalsIgnoreCase("onChangeImpersonated")) {
          doRegisterMacro("onChangeImpersonated", content);
        } else if (rel.getTextContent().equalsIgnoreCase("onChangeSelection")) {
          doRegisterMacro("onChangeSelection", content);
        } else if (rel.getTextContent().equalsIgnoreCase("onChangeToken")) {
          doRegisterMacro("onChangeToken", content);
        }
      }
    }
  }

  /**
   * Handle a change in title.
   *
   * @param title The title to change to.
   */
  private void doChangeTitle(String title) {
    if (actionListeners != null) {
      if (log.isDebugEnabled()) {
        log.debug("changeTitle event: " + title);
      }
      actionListeners.actionPerformed(new HTMLActionEvent.ChangeTitleActionEvent(this, title));
    }
  }

  /**
   * Handle any meta tag information in the html.
   *
   * @param element the element of the meta tag.
   */
  private void handleMetaTag(Element element) {
    String name = element.getAttribute("name");
    String content = element.getAttribute("content");

    if (actionListeners != null && !name.isEmpty() && !content.isEmpty()) {
      if (log.isDebugEnabled()) {
        log.debug("metaTag found: name='" + name + "' content='" + content + "'");
      }
      actionListeners.actionPerformed(new HTMLActionEvent.MetaTagActionEvent(this, name, content));
    }
  }

  /**
   * Get the data of the form and submit it as a json.
   *
   * @param form the form to submit.
   */
  private void getDataAndSubmit(HTMLFormElement form) {
    JSONObject jobj = new JSONObject();
    final HTMLCollection collection = form.getElements();
    for (int i = 0; i < collection.getLength(); i++) {
      String name = null, value = null;
      if (collection.item(i) instanceof HTMLInputElement) {
        HTMLInputElement element = (HTMLInputElement) collection.item(i);
        String type = element.getType().toLowerCase();
        if (type.equals("checkbox") || type.equals("radio")) {
          if (element.getChecked()) {
            name = element.getName();
            value = element.getValue();
          }
        } else {
          name = element.getName();
          value = element.getValue();
        }
      } else if (collection.item(i) instanceof HTMLSelectElement) {
        HTMLSelectElement element = (HTMLSelectElement) collection.item(i);
        name = element.getName();
        value = element.getValue();
      } else if (collection.item(i) instanceof HTMLTextAreaElement) {
        HTMLTextAreaElement element = (HTMLTextAreaElement) collection.item(i);
        name = element.getName();
        value = element.getValue();
      }
      if (name != null) jobj.put(name, value == null ? "" : value);
    }

    String action = form.getAction();
    String data = URLEncoder.encode(jobj.toString(), StandardCharsets.UTF_8);

    doSubmit("json", action, data);
  }

  /**
   * Handle a submit.
   *
   * @param method The method of the submit.
   * @param action The action for the submit.
   * @param data The data from the form.
   */
  private void doSubmit(String method, String action, String data) {
    if (actionListeners != null) {
      if (log.isDebugEnabled()) {
        log.debug(
            "submit event: method='" + method + "' action='" + action + "' data='" + data + "'");
      }
      actionListeners.actionPerformed(
          new HTMLActionEvent.FormActionEvent(this, method, action, data));
    }
  }
}
