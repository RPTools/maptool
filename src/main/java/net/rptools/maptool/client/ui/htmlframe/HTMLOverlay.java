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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Represents the transparent HTML overlay over the map. */
public class HTMLOverlay extends HTMLJFXPanel implements HTMLPanelContainer {
  /** The logger. */
  private static final Logger log = LogManager.getLogger(HTMLOverlay.class);

  /** The default rule for an invisible body tag. */
  private static final String CSS_RULE_BODY =
      "body { font-family: sans-serif; font-size: %dpt; background: none; -webkit-user-select: none; margin: 0;}";

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
    makeWebEngineTransparent();

    // Set the listener for the cursor to switch the webView cursor to the correct one. This is
    // caused by WebView attempting to return to its default instead of our tool cursor.
    webView
        .cursorProperty()
        .addListener(
            (observable, oldCursor, newCursor) -> {
              if (newCursor != null && "DEFAULT".equals(newCursor.toString())) {
                Cursor cursor = MapTool.getFrame().getCurrentZoneRenderer().getCursor();
                if (!cursor.getName().equals("Default Cursor")) {
                  webView.setCursor(null);
                  this.setCursor(cursor);
                }
              }
            });
  }

  /** @return the rule for an invisible body. */
  @Override
  String getRuleBody() {
    return String.format(CSS_RULE_BODY, AppPreferences.getFontSize());
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

  /** Add the mouse listeners to forward the mouse events to the current ZoneRenderer. */
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
          public void mousePressed(MouseEvent e) {
            passMouseEvent(e);
            e.consume(); // prevents double mouse press bug
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            passMouseEvent(e);
          }

          @Override
          public void mouseReleased(MouseEvent e) {
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
