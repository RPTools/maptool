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

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.Token;
import net.sf.json.JSONObject;

@SuppressWarnings("serial")
public class HTMLFrame extends DockableFrame implements HTMLPanelContainer {
  private static final Map<String, HTMLFrame> frames = new HashMap<String, HTMLFrame>();
  private final Map<String, String> macroCallbacks = new HashMap<String, String>();

  private boolean temporary;
  private Object value;
  private final HTMLPanel panel;
  private final String name;

  /**
   * Returns if the frame is visible or not.
   *
   * @param name The name of the frame.
   * @return true if the frame is visible.
   */
  static boolean isVisible(String name) {
    if (frames.containsKey(name)) {
      return frames.get(name).isVisible();
    }
    return false;
  }

  /**
   * Requests that the frame close.
   *
   * @param name The name of the frame.
   */
  static void close(String name) {
    if (frames.containsKey(name)) {
      frames.get(name).closeRequest();
    }
  }

  /**
   * Creates a new HTMLFrame and displays it or displays an existing frame. The width and height are
   * ignored for existing frames so that they will not override the size that the player may have
   * resized them to.
   *
   * @param name The name of the frame.
   * @param title The title of the frame.
   * @param width The width of the frame in pixels.
   * @param height The height of the frame in pixels.
   * @param temp Is the frame temporary.
   * @param val A value that can be returned by getFrameProperties().
   * @param html The html to display in the frame.
   * @return The HTMLFrame that is displayed.
   */
  public static HTMLFrame showFrame(
      String name,
      String title,
      String tabTitle,
      int width,
      int height,
      boolean temp,
      Object val,
      String html) {
    HTMLFrame frame;

    if (frames.containsKey(name)) {
      frame = frames.get(name);
      frame.setTitle(title);
      frame.setTabTitle(tabTitle);
      frame.updateContents(html, temp, val);
      if (!frame.isVisible()) {
        frame.setVisible(true);
        frame.getDockingManager().showFrame(name);
      }
    } else {
      // Only set size on creation so we don't override players resizing.
      width = width < 100 ? 400 : width;
      height = height < 50 ? 200 : height;

      frame = new HTMLFrame(MapTool.getFrame(), name, title, width, height);
      frames.put(name, frame);
      frame.updateContents(html, temp, val);
      frame.getDockingManager().showFrame(name);
      frame.setTabTitle(tabTitle);
      // Jamz: why undock frames to center them?
      if (!frame.isDocked()) center(name);
    }
    frame.setTemporary(temp);
    return frame;
  }

  public void setValue(Object val) {
    this.value = val;
  }

  public Object getValue() {
    return value;
  }

  public void setTemporary(boolean temp) {
    this.temporary = temp;
  }

  public boolean getTemporary() {
    return this.temporary;
  }

  /**
   * Creates a new HTMLFrame.
   *
   * @param parent The parent of this frame.
   * @param name The name of the frame.
   * @param title The title of the frame.
   * @param width The width of the frame.
   * @param height The height of the frame.
   */
  private HTMLFrame(Frame parent, String name, String title, int width, int height) {
    super(name, new ImageIcon(AppStyle.chatPanelImage));

    this.name = name;
    setTitle(title);
    setPreferredSize(new Dimension(width, height));
    panel = new HTMLPanel(this, true, true); // closeOnSubmit is true so we don't get close button
    add(panel);
    this.getContext().setInitMode(DockContext.STATE_FLOATING);
    MapTool.getFrame().getDockingManager().addFrame(this);
    this.setVisible(true);
    addDockableFrameListener(
        new DockableFrameAdapter() {
          @Override
          public void dockableFrameHidden(DockableFrameEvent dockableFrameEvent) {
            closeRequest();
          }
        });
  }

  public static void center(String name) {
    if (!frames.containsKey(name)) {
      return;
    }
    HTMLFrame frame = frames.get(name);
    Dimension outerSize = MapTool.getFrame().getSize();

    int x = MapTool.getFrame().getLocation().x + (outerSize.width - frame.getWidth()) / 2;
    int y = MapTool.getFrame().getLocation().y + (outerSize.height - frame.getHeight()) / 2;

    Rectangle rect =
        new Rectangle(x < 0 ? 0 : x, y < 0 ? 0 : y, frame.getWidth(), frame.getHeight());
    MapTool.getFrame().getDockingManager().floatFrame(frame.getKey(), rect, true);
  }

  /**
   * Updates the html contents of the frame.
   *
   * @param html the html contents.
   * @param temp Is the frame temporary or not.
   * @param val the value of the frame.
   */
  public void updateContents(String html, boolean temp, Object val) {
    macroCallbacks.clear();
    panel.updateContents(html, false);
    setTemporary(temp);
    setValue(val);
  }

  /** The selected token list has changed. */
  private void selectedChanged() {
    if (macroCallbacks.get("onChangeSelection") != null) {
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              MacroLinkFunction.getInstance().runMacroLink(macroCallbacks.get("onChangeSelection"));
            }
          });
    }
  }

  /** A new token has been impersonated or the impersonated token is cleared. */
  private void impersonatedChanged() {
    if (macroCallbacks.get("onChangeImpersonated") != null) {
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              MacroLinkFunction.getInstance()
                  .runMacroLink(macroCallbacks.get("onChangeImpersonated"));
            }
          });
    }
  }

  /** One of the tokens has changed. */
  private void tokenChanged(final Token token) {
    if (macroCallbacks.get("onChangeToken") != null) {
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              MacroLinkFunction.getInstance()
                  .runMacroLink(macroCallbacks.get("onChangeToken") + token.getId().toString());
            }
          });
    }
  }

  /** The selected token list has changed. */
  public static void doSelectedChanged() {
    for (HTMLFrame frame : frames.values()) {
      if (frame.isVisible()) {
        frame.selectedChanged();
      }
    }
  }

  /** A new token has been impersonated or the impersonated token is cleared. */
  public static void doImpersonatedChanged() {
    for (HTMLFrame frame : frames.values()) {
      if (frame.isVisible()) {
        frame.impersonatedChanged();
      }
    }
  }

  /** One of the tokens has changed. */
  public static void doTokenChanged(Token token) {
    if (token != null) {
      for (HTMLFrame frame : frames.values()) {
        if (frame.isVisible()) {
          frame.tokenChanged(token);
        }
      }
    }
  }

  /**
   * Return a json with the width, height, title, temporary, and value of the frame
   *
   * @param name The name of the frame.
   * @return A json with the width, height, title, temporary, and value of the frame
   */
  public static Object getFrameProperties(String name) {
    if (frames.containsKey(name)) {
      HTMLFrame frame = frames.get(name);
      JSONObject frameProperties = new JSONObject();

      frameProperties.put("width", frame.getWidth());
      frameProperties.put("height", frame.getHeight());
      frameProperties.put("temporary", frame.getTemporary() ? BigDecimal.ONE : BigDecimal.ZERO);
      frameProperties.put("title", frame.getTitle());

      Object frameValue = frame.getValue();
      if (frameValue == null) {
        frameValue = "";
      } else {
        if (frameValue instanceof String) {
          // try to convert to a number
          try {
            frameValue = new BigDecimal(frameValue.toString());
          } catch (Exception e) {
          }
        }
      }
      frameProperties.put("value", frameValue);

      return frameProperties;
    } else {
      return "";
    }
  }

  public void closeRequest() {
    MapTool.getFrame().getDockingManager().hideFrame(getKey());
    setVisible(false);
    panel.flush();
    if (getTemporary()) {
      MapTool.getFrame().getDockingManager().removeFrame(this.name, false);
      frames.remove(this.name);
      dispose();
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e instanceof HTMLPane.FormActionEvent) {
      HTMLPane.FormActionEvent fae = (HTMLPane.FormActionEvent) e;
      MacroLinkFunction.getInstance().runMacroLink(fae.getAction() + fae.getData());
    }
    if (e instanceof HTMLPane.RegisterMacroActionEvent) {
      HTMLPane.RegisterMacroActionEvent rmae = (HTMLPane.RegisterMacroActionEvent) e;
      macroCallbacks.put(rmae.getType(), rmae.getMacro());
    }
    if (e instanceof HTMLPane.ChangeTitleActionEvent) {
      String newTitle = ((HTMLPane.ChangeTitleActionEvent) e).getNewTitle();
      this.setTitle(newTitle);
      this.setTabTitle(newTitle);
    }
    if (e instanceof HTMLPane.MetaTagActionEvent) {
      HTMLPane.MetaTagActionEvent mtae = (HTMLPane.MetaTagActionEvent) e;
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
}
