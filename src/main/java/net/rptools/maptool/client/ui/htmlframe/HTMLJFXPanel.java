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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.web.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.parser.ParserException;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.*;

/** Class handles JFXPanel that contains a WebView that can display HTML5. */
public class HTMLJFXPanel extends JFXPanel implements HTMLPanelInterface {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLJFXPanel.class);

  /** The action listeners for the container. */
  private ActionListener actionListeners;

  /** The WebView that displays HTML5. */
  public WebView webView;

  /** The WebEngine of the WebView. */
  private WebEngine webEngine;

  /** Whether the scrolling should be reset. */
  private boolean scrollReset = true;
  /** The horizontal scrolling. */
  private int scrollX = 0;
  /** The vertical scrolling. */
  private int scrollY = 0;

  /** The bridge from Javascript to Java. */
  private static final JavaBridge bridge = new JavaBridge();

  /** Represents a bridge from Javascript to Java. */
  public static class JavaBridge {
    /** Name of the Bridge. */
    private static final String NAME = "MapTool";
    /**
     * Display a self-only message in the chat window.
     *
     * @param text the message to display
     */
    public void log(String text) {
      MapTool.addMessage(TextMessage.me(null, text));
    }
  }

  /** Meta-tag that blocks external file access. */
  private static final String SCRIPT_BLOCK_EXT =
      "<meta http-equiv=\"Content-Security-Policy\" content=\"default-src asset:; style-src 'unsafe-inline'; script-src 'unsafe-inline' 'unsafe-eval'\">\n";

  /** The default rule for the body tag. */
  static final String CSS_BODY =
      "body { font-family: sans-serif; font-size: %dpt; background: #ECE9D8;}";
  /** The default rule for the div tag. */
  static final String CSS_DIV = "div {margin-bottom: 5px}";
  /** The default rule for the span tag. */
  static final String CSS_SPAN = "span.roll {background:#efefef}";

  /** JS that scroll the view to an element from its Id. */
  private static final String SCRIPT_ANCHOR =
      "element = document.getElementById('%s'); if(element != null) {element.scrollIntoView();}";

  /** JS that directs the console.log function to the Java bridge function "log". */
  private static final String SCRIPT_REPLACE_LOG =
      "console.log = function(message){" + JavaBridge.NAME + ".log(message);};";

  /** JS that replace the form.submit() in JS by a function that works. */
  private static final String SCRIPT_REPLACE_SUBMIT =
      "HTMLFormElement.prototype.submit = function(){this.dispatchEvent(new Event('submit'));};";

  /**
   * Creates a new HTMLJFXPanel.
   *
   * @param container The container that will hold the HTML panel.
   */
  HTMLJFXPanel(final HTMLPanelContainer container) {
    Platform.runLater(() -> setupScene(container));
  }

  void setupScene(final HTMLPanelContainer container) {
    webView = new WebView();
    webView.setContextMenuEnabled(false); // disable "reload' right click menu.
    webEngine = webView.getEngine();
    webEngine.getLoadWorker().stateProperty().addListener(this::changed);

    // For alert / confirm / prompt JS events.
    webEngine.setOnAlert(HTMLJFXPanel::showAlert);
    webEngine.setConfirmHandler(HTMLJFXPanel::showConfirm);
    webEngine.setPromptHandler(HTMLJFXPanel::showPrompt);
    webEngine.setCreatePopupHandler(HTMLJFXPanel::showPopup);
    webEngine.setOnError(HTMLJFXPanel::showError);

    StackPane root = new StackPane(); // VBox would create empty space at bottom on resize
    root.setStyle("-fx-background-color: rgba(0, 0, 0, 0);"); // set stackpane transparent

    webView.setPickOnBounds(false);
    root.setPickOnBounds(false);

    root.getChildren().add(webView);
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

  public WebEngine getWebEngine() {
    return webEngine;
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
  public void updateContents(final String html, boolean scrollReset) {
    if (log.isDebugEnabled()) {
      log.debug("setting text in WebView: " + html);
    }
    Platform.runLater(
        () -> {
          this.scrollReset = scrollReset;
          if (!scrollReset) {
            scrollX = getHScrollValue();
            scrollY = getVScrollValue();
          }
          webEngine.loadContent(SCRIPT_BLOCK_EXT + HTMLPanelInterface.fixHTML(html));
        });
  }

  /**
   * Show an alert message.
   *
   * @param event the event of the alert
   */
  private static void showAlert(WebEvent<String> event) {
    javafx.scene.control.Dialog<ButtonType> alert = new javafx.scene.control.Dialog<>();
    alert.getDialogPane().setContentText(event.getData());
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
   * Shows a prompt for a value.
   *
   * @param promptData the promptData object holding the default value and text
   * @return string holding the value entered by the user, or a null
   */
  private static String showPrompt(PromptData promptData) {
    TextInputDialog dialog = new TextInputDialog(promptData.getDefaultValue());
    dialog.setTitle(I18N.getText("lineParser.dialogTitleNoToken"));
    dialog.setContentText(promptData.getMessage());
    return dialog.showAndWait().orElse(null);
  }

  /**
   * Shows a popup window.
   *
   * @param popupFeatures the popup features
   * @return the webEngine of the popup
   */
  private static WebEngine showPopup(PopupFeatures popupFeatures) {
    Stage stage = new Stage((StageStyle.UTILITY));
    WebView webViewPopup = new WebView();
    stage.setScene(new Scene(webViewPopup, 300, 300));
    stage.show();
    return webViewPopup.getEngine();
  }

  /**
   * Shows an error message in the chat window.
   *
   * @param event the error event
   */
  private static void showError(WebErrorEvent event) {
    // Hide error "User data directory is already in use", directory not used anyway
    if (event.getEventType() != WebErrorEvent.USER_DATA_DIRECTORY_ALREADY_IN_USE) {
      MapTool.addMessage(TextMessage.me(null, event.getMessage()));
    }
  }

  String getCSSRule() {
    return String.format(CSS_BODY, AppPreferences.getFontSize()) + CSS_SPAN + CSS_DIV;
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
      handlePage();
    }
  }

  void handlePage() {
    // Redirect console.log to the JavaBridge
    JSObject window = (JSObject) webEngine.executeScript("window");
    window.setMember(JavaBridge.NAME, bridge);
    webEngine.executeScript(SCRIPT_REPLACE_LOG);

    // Replace the broken javascript form.submit method
    webEngine.executeScript(SCRIPT_REPLACE_SUBMIT);

    // Event listener for the href macro link clicks.
    EventListener listenerA = this::fixHref;
    // Event listener for form submission.
    EventListener listenerSubmit = this::getDataAndSubmit;

    Document doc = webEngine.getDocument();
    NodeList nodeList;

    // Add default CSS as first element of the head tag
    Element styleNode = doc.createElement("style");
    Text styleContent = doc.createTextNode(getCSSRule());
    styleNode.appendChild(styleContent);
    Node head = doc.getDocumentElement().getElementsByTagName("head").item(0);
    Node nodeCSS = head.insertBefore(styleNode, head.getFirstChild());

    // Deal with CSS and events of <link>.
    nodeList = doc.getElementsByTagName("link");
    for (int i = 0; i < nodeList.getLength(); i++) {
      fixLink(nodeList.item(i).getAttributes(), nodeCSS, doc);
    }

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

    // Add event handlers for <a> hyperlinks.
    nodeList = doc.getElementsByTagName("a");
    for (int i = 0; i < nodeList.getLength(); i++) {
      EventTarget node = (EventTarget) nodeList.item(i);
      node.addEventListener("click", listenerA, false);
    }

    // Add event handlers for hyperlinks for maps.
    nodeList = doc.getElementsByTagName("area");
    for (int i = 0; i < nodeList.getLength(); i++) {
      EventTarget node = (EventTarget) nodeList.item(i);
      node.addEventListener("click", listenerA, false);
    }

    // Set the "submit" handler to get the data on submission not based on buttons
    nodeList = doc.getElementsByTagName("form");
    for (int i = 0; i < nodeList.getLength(); i++) {
      EventTarget target = (EventTarget) nodeList.item(i);
      target.addEventListener("submit", listenerSubmit, false);
    }

    // Set the "submit" handler to get the data on submission based on input
    nodeList = doc.getElementsByTagName("input");
    for (int i = 0; i < nodeList.getLength(); i++) {
      String type = ((Element) nodeList.item(i)).getAttribute("type");
      if ("image".equals(type) || "submit".equals(type)) {
        EventTarget target = (EventTarget) nodeList.item(i);
        target.addEventListener("click", listenerSubmit, false);
      }
    }
    // Set the "submit" handler to get the data on submission based on button
    nodeList = doc.getElementsByTagName("button");
    for (int i = 0; i < nodeList.getLength(); i++) {
      String type = ((Element) nodeList.item(i)).getAttribute("type");
      if (type == null || "submit".equals(type)) {
        EventTarget target = (EventTarget) nodeList.item(i);
        target.addEventListener("click", listenerSubmit, false);
      }

      // Restores the previous scrolling.
      if (!scrollReset) {
        scrollTo(scrollX, scrollY);
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
   * Handles the CSS and the events of a link. For a stylesheet link with a macro location as a
   * href, the CSS sheet is attached at the end of the refNode. If the href instead starts with
   * "macro", register the href as a callback macro.
   *
   * @param attr the attributes of the link tag
   * @param refNode the node to append the new CSS rules to
   * @param doc the document to update with the modified link
   */
  private void fixLink(NamedNodeMap attr, Node refNode, Document doc) {
    Node rel = attr.getNamedItem("rel");
    Node type = attr.getNamedItem("type");
    Node href = attr.getNamedItem("href");

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
          // Append the style sheet node to the refNode
          refNode.appendChild(styleNode);
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
   * Handles the href events. MacroLinks are executed, external links open the browsers, and anchor
   * links scroll the browser to the link.
   *
   * @param event the href event triggered
   */
  private void fixHref(org.w3c.dom.events.Event event) {
    if (log.isDebugEnabled()) {
      log.debug("Responding to hyperlink event: " + event.getType() + " " + event.toString());
    }

    final String href = ((Element) event.getCurrentTarget()).getAttribute("href");
    if (href != null && !href.equals("")) {
      String href2 = href.trim().toLowerCase();
      if (href2.startsWith("macro")) {
        // ran as macroLink;
        SwingUtilities.invokeLater(() -> MacroLinkFunction.runMacroLink(href));
      } else if (href2.startsWith("#")) {
        // Java bug JDK-8199014 workaround
        webEngine.executeScript(String.format(SCRIPT_ANCHOR, href.substring(1)));
      } else if (!href2.startsWith("javascript")) {
        // non-macrolink, non-anchor link, non-javascript code
        MapTool.showDocument(href); // show in usual browser
      }
      event.preventDefault(); // don't change webview
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

    if (actionListeners != null && name != null && content != null) {
      if (log.isDebugEnabled()) {
        log.debug("metaTag found: name='" + name + "' content='" + content + "'");
      }
      actionListeners.actionPerformed(new HTMLActionEvent.MetaTagActionEvent(this, name, content));
    }
  }

  /**
   * Get the data of the form and submit it as a json.
   *
   * @param event the event of the form submission
   */
  private void getDataAndSubmit(org.w3c.dom.events.Event event) {
    boolean formnovalidate = false; // if true, the form validation is bypassed
    HTMLFormElement form = null;
    Element target = (Element) event.getCurrentTarget();
    JsonObject jObj = new JsonObject();
    // Get the form based on the target of the event
    if (target instanceof HTMLFormElement) {
      form = (HTMLFormElement) target;
    } else if (target instanceof HTMLInputElement) {
      HTMLInputElement input = (HTMLInputElement) target;
      form = input.getForm();
      addToObject(jObj, input.getName(), input.getValue());
      formnovalidate = input.getAttribute("formnovalidate") != null;
    } else if (target instanceof HTMLButtonElement) {
      HTMLButtonElement button = (HTMLButtonElement) target;
      form = button.getForm();
      addToObject(jObj, button.getName(), button.getValue());
      formnovalidate = button.getAttribute("formnovalidate") != null;
    }
    if (form == null) return;

    // Check for non-macrolinktext action
    String action = form.getAction();
    if (action == null || action.startsWith("javascript:")) {
      return;
    }

    // Check for validity
    boolean novalidate = form.getAttribute("novalidate") != null;
    if (!formnovalidate && !novalidate) {
      JSObject jsObject = (JSObject) form;
      if (!(boolean) jsObject.call("checkValidity")) {
        return;
      }
    }

    event.preventDefault(); // prevent duplicated form submit

    // Gets the data from the form
    final HTMLCollection collection = form.getElements();
    for (int i = 0; i < collection.getLength(); i++) {
      String name, value;
      if (collection.item(i) instanceof HTMLInputElement) {
        HTMLInputElement element = (HTMLInputElement) collection.item(i);
        String type = element.getType().toLowerCase();
        if (type.equals("checkbox") || type.equals("radio")) {
          if (element.getChecked()) {
            name = element.getName();
            value = element.getValue();
          } else continue; // skip unchecked elements
        } else if (type.equals("submit") || type.equals("image")) {
          continue; // skip input button/images that were not pressed
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
      } else continue; // skip elements not containing data
      addToObject(jObj, name, value);
    }
    String data = URLEncoder.encode(jObj.toString(), StandardCharsets.UTF_8);
    doSubmit("json", action, data);
  }

  /**
   * Convenience method to put name and value in the object.
   *
   * @param jObj the JsonObject to put the data in
   * @param name the name
   * @param value the value
   */
  private static void addToObject(JsonObject jObj, String name, String value) {
    if (name != null && !"".equals(name)) {
      value = value == null ? "" : value;
      try {
        BigDecimal number = new BigDecimal(value);
        jObj.addProperty(name, number);
      } catch (NumberFormatException nfe) {
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(value);
        jObj.add(name, json);
      }
    }
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

  /** Returns the vertical scroll value, i.e. thumb position. */
  private int getVScrollValue() {
    return (Integer) webEngine.executeScript("document.body.scrollTop");
  }

  /** Returns the horizontal scroll value, i.e. thumb position. */
  private int getHScrollValue() {
    return (Integer) webEngine.executeScript("document.body.scrollLeft");
  }

  /**
   * Scrolls the WebView.
   *
   * @param x the horizontal scrolling
   * @param y the vertical scrolling
   */
  private void scrollTo(int x, int y) {
    webEngine.executeScript("window.scrollTo(" + x + ", " + y + ")");
  }
}
