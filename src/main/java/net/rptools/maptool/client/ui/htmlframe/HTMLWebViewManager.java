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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sun.webkit.dom.HTMLSelectElementImpl;
import java.awt.*;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.web.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.swing.*;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.util.PromiseUtil;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.*;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.*;

/** The manager for a WebView that can display HTML5. */
public class HTMLWebViewManager {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLWebViewManager.class);

  /** The action listeners for the container. */
  private ActionListener actionListeners;

  /** The WebView that displays HTML5. */
  WebView webView;

  /** The WebEngine of the WebView. */
  private WebEngine webEngine;

  /** Whether the scrolling to be reset. */
  private boolean scrollReset = true;

  /** The horizontal scrolling. */
  private int scrollX = 0;

  /** The vertical scrolling. */
  private int scrollY = 0;

  /** Whether the WebView has been flushed out. */
  private boolean isFlushed = true;

  /** The bridge from Javascript to Java. */
  private final JavaBridge bridge;

  /** Represents a bridge from Javascript to Java. */
  public class JavaBridge {

    public JavaBridge(HTMLPanelContainer container, String kind, String name) {
      this.kind = kind;
      this.name = name;
      this.container = container;
    }

    /** Magic value that window.status must take to initiate the bridge. */
    public static final String BRIDGE_VALUE = "MY_INITIALIZING_VALUE";

    /** Name of the Bridge. */
    private static final String NAME = "MapTool";

    private final String name;

    private final String kind;

    private final HTMLPanelContainer container;

    private JSObject window;

    public MTXMLHttpRequest makeXMLHttpRequest(JSObject ctx, String href) {
      return new MTXMLHttpRequest(ctx, href);
    }

    public String getName() {
      return name;
    }

    public String getKind() {
      return kind;
    }

    public JSObject getUserData() {
      Object frameValue = container.getValue();
      if (frameValue == null) {
        frameValue = "";
      }
      return PromiseUtil.convertToPromise(
          window, CompletableFuture.completedFuture(frameValue.toString()));
    }

    /**
     * Display a self-only message in the chat window.
     *
     * @param text the message to display
     */
    public void log(String text) {
      MapTool.addMessage(TextMessage.me(null, text));
    }

    /**
     * Receives an added node sent by the JS Mutation Observer and attaches an event listener to it
     * and its descendants, if need be. The elements affected are Anchors, Areas, Forms, Buttons,
     * and Inputs.
     *
     * @param object the node sent by the Mutation Observer
     */
    public void handleAddedNode(Object object) {
      if (object instanceof HTMLElement) {
        HTMLElement addedNode = (HTMLElement) object;

        // Add listeners to the node itself.
        if (addedNode instanceof EventTarget) {
          EventTarget target = (EventTarget) addedNode;
          if (addedNode instanceof HTMLAnchorElement || addedNode instanceof HTMLAreaElement) {
            target.addEventListener("click", HTMLWebViewManager.this::fixHref, true);
          } else if (target instanceof HTMLFormElement) {
            target.addEventListener("submit", HTMLWebViewManager.this::getDataAndSubmit, true);
          } else if (target instanceof HTMLInputElement || target instanceof HTMLButtonElement) {
            target.addEventListener("click", HTMLWebViewManager.this::getDataAndSubmit, true);
          }
        }
      }
    }
  }

  /** Meta-tag that blocks external file access. */
  private static final String SCRIPT_BLOCK_EXT =
      "<meta http-equiv=\"Content-Security-Policy\" "
          + "content=\" "
          + " default-src asset: lib: "
          + " https://code.jquery.com " // JQuery CDN
          + " https://cdn.jsdelivr.net " // JSDelivr CDN
          + " https://stackpath.bootstrapcdn.com " // Bootstrap CDN
          + " https://unpkg.com " // unpkg CDN
          + " https://cdnjs.cloudflare.com " // CloudFlare JS CDN
          + " https://ajax.googleapis.com " // Google CDN
          + " https://fonts.googleapis.com  https://fonts.gstatic.com " // Google Fonts
          + " 'unsafe-inline' 'unsafe-eval' ; "
          + " img-src * asset: lib: ; "
          + " font-src https://fonts.gstatic.com 'self'"
          + "\">\n";

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

  /** JS to initialize the Java bridge. Needs to be the first script of the page. */
  private static final String SCRIPT_BRIDGE =
      String.format(
          "<SCRIPT>window.status = '%s'; window.status = '';</SCRIPT>", JavaBridge.BRIDGE_VALUE);

  private static final String[] INITIALIZATION_SCRIPTS = {
    "net/rptools/maptool/client/html5/javascript/Console.js",
    "net/rptools/maptool/client/html5/javascript/Replace_Submit.js",
    "net/rptools/maptool/client/html5/javascript/Mutation_Observer.js",
    "net/rptools/maptool/client/html5/javascript/XMLHttpRequest.js"
  };

  HTMLWebViewManager(String kind, String name) {
    bridge = new JavaBridge((HTMLPanelContainer) this, kind, name);
  }

  HTMLWebViewManager(HTMLPanelContainer container, String kind, String name) {
    bridge = new JavaBridge(container, kind, name);
  }

  /**
   * Setup the WebView
   *
   * @param webView the webView to manage
   */
  public void setupWebView(WebView webView) {
    this.webView = webView;
    this.webView.setContextMenuEnabled(false); // disable "reload' right click menu.
    this.webView.setPickOnBounds(false);

    webEngine = webView.getEngine();
    webEngine.getLoadWorker().stateProperty().addListener(this::changed);

    // For alert / confirm / prompt JS events.
    webEngine.setOnAlert(HTMLWebViewManager::showAlert);
    webEngine.setConfirmHandler(HTMLWebViewManager::showConfirm);
    webEngine.setPromptHandler(HTMLWebViewManager::showPrompt);
    webEngine.setCreatePopupHandler(HTMLWebViewManager::showPopup);
    webEngine.setOnError(HTMLWebViewManager::showError);

    // Workaround to load Java Bridge before everything else.
    webEngine.onStatusChangedProperty().set(this::setBridge);
  }

  public WebView getWebView() {
    return webView;
  }

  public WebEngine getWebEngine() {
    return webEngine;
  }

  public void addActionListener(ActionListener listener) {
    actionListeners = AWTEventMulticaster.add(actionListeners, listener);
  }

  public void flush() {
    // Stores the x,y scrolling of the previous WebView
    scrollX = getHScrollValue();
    scrollY = getVScrollValue();

    // Delete cache for navigate back
    webEngine.load("about:blank");
    // Delete cookies
    java.net.CookieHandler.setDefault(new java.net.CookieManager());

    isFlushed = true;
  }

  public void updateContents(final String html, boolean scrollReset) {
    log.debug("setting text in WebView: {}", html);
    this.scrollReset = scrollReset;
    // If the WebView has been flushed, the scrolling has already been stored
    if (!scrollReset && !isFlushed) {
      scrollX = getHScrollValue();
      scrollY = getVScrollValue();
    }
    isFlushed = false;
    webEngine.loadContent(SCRIPT_BLOCK_EXT + SCRIPT_BRIDGE + HTMLPanelInterface.fixHTML(html));
  }

  /**
   * Setups the JavaBridge and the console.log command before JS is ran. Approach described at
   * https://stackoverflow.com/questions/26400925/.
   *
   * @param event the onStatusChanged event triggering the bridge to load
   */
  private void setBridge(WebEvent<String> event) {
    if (JavaBridge.BRIDGE_VALUE.equals(event.getData())) {
      JSObject window = (JSObject) webEngine.executeScript("window");
      window.setMember(JavaBridge.NAME, bridge);
      bridge.window = window;

      for (String rsrc : INITIALIZATION_SCRIPTS) {
        try {
          String src = new String(FileUtil.loadResource(rsrc), StandardCharsets.UTF_8);
          webEngine.executeScript(src);
        } catch (Exception e) {
          log.error("Failed initializing: " + rsrc);
          log.error(e);
        }
      }
    }
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

  /** Handles the page after it has loaded. */
  void handlePage() {
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
      fixLink(nodeList.item(i), doc);
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

    // Restores the previous scrolling.
    if (!scrollReset) {
      scrollTo(scrollX, scrollY);
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
      log.debug("registerMacro event: type='{}' link='{}'", type, link);
      actionListeners.actionPerformed(
          new HTMLActionEvent.RegisterMacroActionEvent(this, type, link));
    }
  }

  /**
   * Handles the CSS and the events of a link. For a stylesheet link with a macro location as a
   * href, the CSS sheet is attached at the end of the refNode. If the href instead starts with
   * "macro", register the href as a callback macro.
   *
   * @param node the node for the link tag
   * @param doc the document to update with the modified link
   */
  private void fixLink(Node node, Document doc) {

    NamedNodeMap attr = node.getAttributes();
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

          var lib = new LibraryManager().getLibrary(vals[1].substring(4));
          if (lib.isPresent()) {
            var library = lib.get();
            var macroInfo = library.getMTScriptMacroInfo(vals[0]).get();

            if (macroInfo.isPresent()) {
              String cssText = macroInfo.get().macro();

              Element styleNode = doc.createElement("style");
              Text styleContent = doc.createTextNode(cssText);
              styleNode.appendChild(styleContent);
              // Insert the style node before the link.
              node.getParentNode().insertBefore(styleNode, node);
            }
          }
        } catch (ExecutionException | InterruptedException e) {
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
   * Checks the passed in href string to see if it is one that the WebView component should handle
   * or not.
   *
   * @param href the href tag to check.
   * @return {@code true} if the WebView should handle it/
   */
  private boolean webViewHandledHref(String href) {
    String href2 = href.toLowerCase();
    if (href.startsWith("lib://")) {
      return true;
    } else if (href.startsWith("./")) {
      return true;
    } else if (href.startsWith("../")) {
      return true;
    } else {
      return href.startsWith("/");
    }
  }

  /**
   * Handles the href events. MacroLinks are executed, external links open the browsers, and anchor
   * links scroll the browser to the link.
   *
   * @param event the href event triggered
   */
  private void fixHref(org.w3c.dom.events.Event event) {
    log.debug("Responding to hyperlink event: {} {}", event.getType(), event);

    final String href = ((Element) event.getCurrentTarget()).getAttribute("href");
    if (href != null && !href.equals("")) {
      String href2 = href.trim().toLowerCase();
      if (!webViewHandledHref(href2)) {
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
  }

  /**
   * Handle a change in title.
   *
   * @param title The title to change to.
   */
  private void doChangeTitle(String title) {
    if (actionListeners != null) {
      log.debug("changeTitle event: {}", title);
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
      log.debug("metaTag found: name='{}' content='{}'", name, content);
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
      String type = input.getType();
      if (!"image".equalsIgnoreCase(type) && !"submit".equalsIgnoreCase(type)) {
        // Only allow submit from input with image or with submit type
        return;
      }
      form = input.getForm();
      addToObject(jObj, input.getName(), input.getValue());
      formnovalidate = input.getAttribute("formnovalidate") != null;
    } else if (target instanceof HTMLButtonElement) {
      HTMLButtonElement button = (HTMLButtonElement) target;
      String type = button.getType();
      if (type != null && !"submit".equalsIgnoreCase(type)) {
        // Only allow submit from buttons without type or submit type
        return;
      }
      form = button.getForm();
      addToObject(jObj, button.getName(), button.getValue());
      formnovalidate = button.getAttribute("formnovalidate") != null;
    }
    if (form == null) return;

    // formAction can override action
    String formAction = target.getAttribute("formaction");
    String action = (formAction == null || "".equals(formAction)) ? form.getAction() : formAction;

    // Check for non-macrolinktext action
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
        if (element.getMultiple()) {
          // If multiple selection enabled, returns a JSON array of selected values
          JsonArray selected = new JsonArray();
          // We avoid iterating over the result of `element.getOptions()` in order to workaround a
          // missing symbol for `HTMLOptionsCollectionImpl::itemImpl()` on linux. Ideally we would
          // go back to using `element.getOptions()` in the future so we don't keep a dependency on
          // `HTMLSelectElementImpl`.
          for (int o = 0; o < element.getLength(); o++) {
            var node = ((HTMLSelectElementImpl) element).item(o);
            // instanceof check always seems to be true, but we'll keep it to be safe.
            if (node instanceof HTMLOptionElement option && option.getSelected()) {
              selected.add(option.getValue());
            }
          }

          value = selected.toString();
        } else {
          value = element.getValue();
        }
      } else if (collection.item(i) instanceof HTMLTextAreaElement) {
        HTMLTextAreaElement element = (HTMLTextAreaElement) collection.item(i);
        name = element.getName();
        value = element.getValue();
      } else continue; // skip elements not containing data
      addToObject(jObj, name, value);
    }

    // Find the link data
    Matcher m = MacroLinkFunction.LINK_DATA_PATTERN.matcher(action);
    JsonElement linkData = null;
    if (m.matches()) {
      // Separate the action from the data
      action = m.group(1);
      linkData = MacroLinkFunction.getInstance().getLinkDataAsJson(m.group(2));
    }

    // Combines and encodes the form data with the link data
    String data = getEncodedCombinedData(jObj, linkData);

    // Form submit should be ran on EDT. Fixes #2056.
    final String finalAction = action;
    SwingUtilities.invokeLater(() -> doSubmit("json", finalAction, data));
  }

  /**
   * Combines and encodes the form data with the link data. If there is no link data, uses the form
   * data only. If the link data is a json, adds the form data as the "form" property. Otherwise,
   * only uses the link data.
   *
   * @param formData the JsonObject containing the form data
   * @param linkData the JsonElement containing the link data
   * @return the encoded data
   */
  private String getEncodedCombinedData(JsonObject formData, JsonElement linkData) {
    JsonObject jobjLinkData = null;
    if (linkData != null && linkData.isJsonObject()) {
      jobjLinkData = linkData.getAsJsonObject();
    }

    String combinedData;
    if (linkData == null) {
      // Returns the encoded json of the form data if there is no link data
      combinedData = formData.toString();
    } else if (jobjLinkData == null || jobjLinkData.has("form")) {
      // Ignores the form data if the link data is not a json object or already has "form" field
      combinedData = linkData.toString();
    } else {
      // Adds the form data to the json object link data
      jobjLinkData.add("form", formData);
      combinedData = jobjLinkData.toString();
    }
    return URLEncoder.encode(combinedData, StandardCharsets.UTF_8);
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
      log.debug("submit event: method='{}' action='{}' data='{}'", method, action, data);
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
