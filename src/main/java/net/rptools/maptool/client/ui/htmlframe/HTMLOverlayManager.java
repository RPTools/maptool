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
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Rectangle2D;
import javafx.scene.web.WebView;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/** The manager for the HTMLWebView of an overlay. */
public class HTMLOverlayManager extends HTMLWebViewManager
    implements Comparable<HTMLOverlayManager>, HTMLPanelContainer {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLOverlayManager.class);

  /** The default rule for an invisible body tag. */
  private static final String CSS_BODY =
      "body { font-family: sans-serif; font-size: %dpt; background: none; -webkit-user-select: none; margin: 0; --pointermap:pass; overflow-x: hidden; overflow-y: hidden;}";

  /** CSS rule: clicks on hyperlinks, buttons and input elements are not forwarded to map. */
  private static final String CSS_POINTERMAP =
      "a {--pointermap:block;} button {--pointermap:block;} input {--pointermap:block;} area {--pointermap:block;} select {--pointermap:block}";

  /** Script to return the HTML element at coordinates %d, %d. */
  private static final String SCRIPT_GET_FROM_POINT = "document.elementFromPoint(%d, %d)";

  /** Script to return the calculated --pointermap value of an HTML element. */
  private static final String SCRIPT_GET_POINTERMAP =
      "window.getComputedStyle(this).getPropertyValue('--pointermap')";

  /** The ZOrder of the overlay. */
  private int zOrder;

  /** The name of the overlay. */
  private final String name;

  /** The value stored in the overlay. */
  private Object value;

  /** The map of the macro callbacks. */
  private final Map<String, String> macroCallbacks = new HashMap<String, String>();

  HTMLOverlayManager(String name, int zOrder) {
    addActionListener(this); // add the action listeners for form events
    this.name = name;
    this.zOrder = zOrder;
  }

  @Override
  public void setupWebView(WebView webView) {
    super.setupWebView(webView);
  }

  /** @return the zOrder of the overlay. */
  int getZOrder() {
    return zOrder;
  }

  /**
   * Sets the zOrder of the overlay.
   *
   * @param zOrder the zOrder
   */
  void setZOrder(int zOrder) {
    this.zOrder = zOrder;
  }

  /** @return the name of the overlay. */
  public String getName() {
    return name;
  }

  @Override
  public int compareTo(@NotNull HTMLOverlayManager o) {
    return getZOrder() - o.getZOrder();
  }

  @Override
  void handlePage() {
    super.handlePage();
    makeWebEngineTransparent(); // transparent WebView

    getWebView()
        .cursorProperty()
        .addListener((obs, oldCursor, newCursor) -> updateOverlayCursor(newCursor));
  }

  /**
   * Updates the overlay cursor to match the WebView cursor, if needs be.
   *
   * @param newCursor the cursor that WebView tries to impose
   */
  private void updateOverlayCursor(javafx.scene.Cursor newCursor) {
    if (newCursor != null && "DEFAULT".equals(newCursor.toString())) {
      // Only changes to the default cursor if all WebViews have the default cursor
      if (MapTool.getFrame().getOverlayPanel().areWebViewCursorsDefault()) {
        Cursor cursor = MapTool.getFrame().getCurrentZoneRenderer().getCursor();
        MapTool.getFrame().getOverlayPanel().setOverlayCursor(cursor);
      }
    } else if (newCursor != null) {
      MapTool.getFrame().getOverlayPanel().setOverlayCursor(newCursor);
    }
  }

  /** @return the rule for an invisible body. */
  @Override
  String getCSSRule() {
    return String.format(CSS_BODY, AppPreferences.getFontSize())
        + CSS_SPAN
        + CSS_DIV
        + CSS_POINTERMAP;
  }

  /** Makes the webEngine transparent through reflection. */
  private void makeWebEngineTransparent() {
    try {
      Field f = getWebEngine().getClass().getDeclaredField("page");
      f.setAccessible(true);
      Object page = f.get(getWebEngine());
      Method m = page.getClass().getMethod("setBackgroundColor", int.class);
      m.setAccessible(true);
      m.invoke(page, (new Color(0, 0, 0, 0)).getRGB());
    } catch (NoSuchFieldException
        | IllegalAccessException
        | NoSuchMethodException
        | InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns true if the click to the map should be forwarded to the ZR, false otherwise.
   *
   * @param x the x coordinate of the click
   * @param y the y coordinate of the click
   * @return false if the element has --pointermap=block, true otherwise
   */
  HTMLOverlayPanel.mousePassResult getMousePassResult(int x, int y) {
    if (!isVisible()) {
      return HTMLOverlayPanel.mousePassResult.PASS;
    }
    JSObject element =
        (JSObject) getWebEngine().executeScript(String.format(SCRIPT_GET_FROM_POINT, x, y));
    if (element == null) {
      return HTMLOverlayPanel.mousePassResult.BLOCK;
    } else {
      String pe = (String) element.eval(SCRIPT_GET_POINTERMAP);
      if ("blockopaque".equals(pe)) return HTMLOverlayPanel.mousePassResult.CHECK_OPACITY;
      if ("block".equals(pe)) return HTMLOverlayPanel.mousePassResult.BLOCK;
      return HTMLOverlayPanel.mousePassResult.PASS;
    }
  }

  private static Rectangle2D onePixel = new Rectangle2D(0, 0, 1, 1);

  @Override
  public boolean isVisible() {
    return getWebView().isVisible();
  }

  @Override
  public Map<String, String> macroCallbacks() {
    return macroCallbacks;
  }

  @Override
  public void setVisible(boolean visible) {
    getWebView().setVisible(visible);
  }

  @Override
  public boolean getTemporary() {
    return true;
  }

  @Override
  public void setTemporary(boolean temp) {}

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public Component add(Component component) {
    return null;
  }

  @Override
  public void remove(Component component) {}

  /**
   * Act when an action is performed.
   *
   * @param e the ActionEvent.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e instanceof HTMLActionEvent.FormActionEvent) {
      HTMLActionEvent.FormActionEvent fae = (HTMLActionEvent.FormActionEvent) e;
      MacroLinkFunction.runMacroLink(fae.getAction() + fae.getData());
    }
    if (e instanceof HTMLActionEvent.RegisterMacroActionEvent) {
      HTMLActionEvent.RegisterMacroActionEvent rmae = (HTMLActionEvent.RegisterMacroActionEvent) e;
      macroCallbacks.put(rmae.getType(), rmae.getMacro());
    }
    if (e instanceof HTMLActionEvent.MetaTagActionEvent) {
      HTMLActionEvent.MetaTagActionEvent mtae = (HTMLActionEvent.MetaTagActionEvent) e;
      if (mtae.getName().equalsIgnoreCase("onChangeToken")
          || mtae.getName().equalsIgnoreCase("onChangeSelection")
          || mtae.getName().equalsIgnoreCase("onChangeImpersonated")) {
        macroCallbacks.put(mtae.getName(), mtae.getContent());
      }
    }
    if (e.getActionCommand().equals("Close")) {
      closeRequest();
    }
  }

  @Override
  public void closeRequest() {
    setVisible(false);
    flush();
  }
}
