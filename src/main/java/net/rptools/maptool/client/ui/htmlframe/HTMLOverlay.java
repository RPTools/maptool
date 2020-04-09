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
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.Token;
import netscape.javascript.JSObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Represents the transparent HTML overlay over the map. */
public class HTMLOverlay extends HTMLJFXPanel implements HTMLPanelContainer {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLOverlay.class);

  /** The default rule for an invisible body tag. */
  private static final String CSS_BODY =
      "body { font-family: sans-serif; font-size: %dpt; background: none; -webkit-user-select: none; margin: 0; --pointermap:pass;}";

  /** CSS rule: clicks on hyperlinks, buttons and input elements are not forwarded to map. */
  private static final String CSS_POINTERMAP =
      "a {--pointermap:block;} button {--pointermap:block;} input {--pointermap:block;} area {--pointermap:block;} select {--pointermap:block}";

  /** Script to return the HTML element at coordinates %d, %d. */
  private static final String SCRIPT_GET_FROM_POINT = "document.elementFromPoint(%d, %d)";

  /** Script to return the calculated --pointermap value of an HTML element. */
  private static final String SCRIPT_GET_POINTERMAP =
      "window.getComputedStyle(this).getPropertyValue('--pointermap')";

  /** The map of the macro callbacks. */
  private final Map<String, String> macroCallbacks = new HashMap<>();

  public HTMLOverlay() {
    super(null);
    addMouseListeners(); // mouse listeners to transmit to the ZR
    addActionListener(this); // add the action listeners for form events
    setBackground(new Color(0, 0, 0, 0)); // transparent overlay
  }

  @Override
  void handlePage() {
    super.handlePage();
    makeWebEngineTransparent(); // transparent WebView

    // Set the listener for the cursor to switch the webView cursor to the correct one. This is
    // caused by WebView attempting to return to its default instead of our tool cursor.
    webView
        .cursorProperty()
        .addListener((obs, oldCursor, newCursor) -> blockCursorChange(newCursor));
  }

  /**
   * Blocks the cursor change of WebView, if it is a default cursor different from the one of the
   * ZoneRenderer.
   *
   * @param newCursor the cursor that WebView tries to impose
   */
  private void blockCursorChange(javafx.scene.Cursor newCursor) {
    if (newCursor != null && "DEFAULT".equals(newCursor.toString())) {
      Cursor cursor = MapTool.getFrame().getCurrentZoneRenderer().getCursor();
      if (!cursor.getName().equals("Default Cursor")) {
        webView.setCursor(null);
        this.setCursor(cursor);
      }
    }
  }

  /**
   * Pass the mouse event to the map, if valide. Takes a MouseEvent caught by the Overlay, check if
   * it was done on an HTML element enabling a click on the map. If so, forward the mouse event to
   * the ZoneRenderer.
   *
   * @param swingEvent the mouse event from Swing that could be passed
   */
  private void validateAndPassEvent(MouseEvent swingEvent) {
    Platform.runLater(
        () -> {
          boolean passClick = isPassClick(swingEvent.getX(), swingEvent.getY());
          if (passClick) {
            SwingUtilities.invokeLater(() -> passMouseEvent(swingEvent));
          }
        });
  }

  /**
   * Returns true if the click to the map should be forwarded to the ZR, false otherwise.
   *
   * @param x the x coordinate of the click
   * @param y the y coordinate of the click
   * @return false if the element has --pointermap=block, true otherwise
   */
  private boolean isPassClick(int x, int y) {
    JSObject element =
        (JSObject) getWebEngine().executeScript(String.format(SCRIPT_GET_FROM_POINT, x, y));
    if (element == null) {
      return true;
    } else {
      String pe = (String) element.eval(SCRIPT_GET_POINTERMAP);
      return "blockopaque".equals(pe) ? !isOpaque(x, y) : !"block".equals(pe);
    }
  }

  /**
   * Returns whether the overlay is opaque (alpha > 0) at the x,y pixel. Method provided by gthanop
   * at https://stackoverflow.com/questions/60906929
   *
   * @param x the x coordinate of the pixel
   * @param y the y coordinate of the pixel
   * @return true if alpha isn't 0, false if it is
   */
  private boolean isOpaque(int x, int y) {
    if (!getBounds().contains(x, y)) return false; // no overlay outside the bounds
    final BufferedImage bimg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    final Graphics2D g2d = bimg.createGraphics();
    g2d.translate(-x, -y); // pixel of interest is now at 0,0
    printAll(g2d); // draw a 1,1 pixel at 0,0
    g2d.dispose();
    Color c = new Color(bimg.getRGB(0, 0), true);
    bimg.flush();
    return c.getAlpha() != 0;
  }

  /** @return the rule for an invisible body. */
  @Override
  String getCSSRule() {
    return String.format(CSS_BODY, AppPreferences.getFontSize())
        + CSS_SPAN
        + CSS_DIV
        + CSS_POINTERMAP;
  }

  /** Run the callback macro for "onChangeSelection". */
  void doSelectedChanged() {
    HTMLPanelContainer.selectedChanged(macroCallbacks);
  }

  /** Run the callback macro for "onChangeImpersonated". */
  void doImpersonatedChanged() {
    HTMLPanelContainer.impersonatedChanged(macroCallbacks);
  }

  /** Run the callback macro for "onChangeToken". */
  void doTokenChanged(Token token) {
    HTMLPanelContainer.tokenChanged(token, macroCallbacks);
  }

  /**
   * Add the mouse listeners to forward the mouse events to the current ZoneRenderer. Clicks, mouse
   * press, and mouse released get validated first to see if they need forwarding.
   */
  private void addMouseListeners() {
    addMouseWheelListener(this::passMouseEvent);
    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseDragged(MouseEvent e) {
            passMouseEvent(e);
          }
        });
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            validateAndPassEvent(e);
          }

          @Override
          public void mousePressed(MouseEvent e) {
            validateAndPassEvent(e);
            e.consume(); // workaround for java bug JDK-8200224
          }

          @Override
          public void mouseReleased(MouseEvent e) {
            validateAndPassEvent(e);
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            passMouseEvent(e);
          }
        });
  }

  /**
   * Passes a mouse event to the ZoneRenderer.
   *
   * @param e the mouse event to forward
   */
  private void passMouseEvent(MouseEvent e) {
    Component c = MapTool.getFrame().getCurrentZoneRenderer();
    c.dispatchEvent(SwingUtilities.convertMouseEvent(e.getComponent(), e, c));
  }

  @Override
  public Map<String, String> macroCallbacks() {
    return macroCallbacks;
  }

  @Override
  public boolean getTemporary() {
    return false;
  }

  @Override
  public void setTemporary(boolean temp) {}

  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public void setValue(Object value) {}

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
  public void updateContents(final String html) {
    macroCallbacks.clear(); // clear the old callbacks
    super.updateContents(html);
    getDropTarget().setActive(false); // disables drop on overlay, drop goes to map
    if ("".equals(html)) {
      closeRequest(); // turn off the overlay
    } else {
      setVisible(true);
    }
  }

  @Override
  public void closeRequest() {
    flush();
    setVisible(false);
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
}
