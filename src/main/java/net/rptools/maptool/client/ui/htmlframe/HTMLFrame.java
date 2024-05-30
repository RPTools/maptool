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
import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import com.jidesoft.docking.event.DockableFrameAdapter;
import com.jidesoft.docking.event.DockableFrameEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.ParserException;

/**
 * Represents a dockable frame holding an HTML panel. Can hold either an HTML3.2 (Swing) or a HTML5
 * (JavaFX) panel.
 */
@SuppressWarnings("serial")
public class HTMLFrame extends DockableFrame implements HTMLPanelContainer {
  /** The static map of the HTMLFrames. */
  private static final Map<String, HTMLFrame> frames = new HashMap<String, HTMLFrame>();

  /** The map of the macro callbacks. */
  private final Map<String, String> macroCallbacks = new HashMap<String, String>();

  /** The temporary status of the frame. A temporary frame isn't stored after being closed. */
  private boolean temporary;

  /** The value stored in the frame. */
  private Object value;

  /** Panel for HTML. */
  private HTMLPanelInterface panel;

  /** The name of the frame. */
  private final String name;

  /** Is the panel HTML5 or HTML3.2. */
  private boolean isHTML5;

  /**
   * Runs a javascript on a frame.
   *
   * @param name the name of the frame
   * @param script the script to run
   * @return true if the frame exists and can run the script, false otherwise
   */
  public static boolean runScript(String name, String script) {
    HTMLFrame frame = frames.get(name);
    return frame != null && frame.panel.runJavascript(script);
  }

  @Override
  public Map<String, String> macroCallbacks() {
    return macroCallbacks;
  }

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
   * Gets an unmodifiable set view of the names of all known frames.
   *
   * @return the frame names
   */
  public static Set<String> getFrameNames() {
    return Collections.unmodifiableSet(frames.keySet());
  }

  /**
   * Creates a new HTMLFrame and displays it or displays an existing frame. The width and height are
   * ignored for existing frames so that they will not override the size that the player may have
   * resized them to.
   *
   * @param name the name of the frame.
   * @param title the title of the frame.
   * @param tabTitle the title of the tab.
   * @param width the width of the frame in pixels.
   * @param height the height of the frame in pixels.
   * @param temp whether the frame should be temporary.
   * @param scrollReset whether the scrollbar should be reset.
   * @param isHTML5 whether it should use HTML5 (JavaFX) or HTML 3.2 (Swing).
   * @param val a value that can be returned by getFrameProperties().
   * @param html the html to display in the frame.
   * @return the HTMLFrame that is displayed.
   */
  public static HTMLFrame showFrame(
      String name,
      String title,
      String tabTitle,
      int width,
      int height,
      boolean temp,
      boolean scrollReset,
      boolean isHTML5,
      Object val,
      String html)
      throws ParserException {
    HTMLFrame frame;

    if (frames.containsKey(name)) {
      frame = frames.get(name);
      if (!frame.isVisible()) {
        frame.setVisible(true);
        frame.getDockingManager().showFrame(name);
      }
    } else {
      // Make sure there isn't a name conflict with the normal MT frames
      boolean isMtframeName =
          Stream.of(MapToolFrame.MTFrame.values())
                  .filter(e -> e.name().equals(name))
                  .findFirst()
                  .orElse(null)
              != null;
      if (isMtframeName) {
        String opt = isHTML5 ? "frame5" : "frame";
        throw new ParserException(I18N.getText("lineParser.optReservedName", opt, name));
      }

      // Only set size on creation so we don't override players resizing.
      frame = new HTMLFrame(name, width, height, isHTML5);
      frames.put(name, frame);

      frame.getDockingManager().showFrame(name);
      // Jamz: why undock frames to center them?
      if (!frame.isDocked()) center(name);
    }
    frame.updateContents(html, title, tabTitle, temp, scrollReset, isHTML5, val);
    return frame;
  }

  @Override
  public void setValue(Object val) {
    this.value = val;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setTemporary(boolean temp) {
    this.temporary = temp;
  }

  @Override
  public boolean getTemporary() {
    return this.temporary;
  }

  /**
   * Add an HTML panel to the frame.
   *
   * @param isHTML5 whether the panel supports HTML5
   */
  public void addHTMLPanel(boolean isHTML5) {
    if (isHTML5) {
      panel = new HTMLJFXPanel(this, new HTMLWebViewManager(this, "frame5", this.name));
    } else {
      panel = new HTMLPanel(this, true);
    }
    panel.addToContainer(this);
    panel.addActionListener(this);
  }

  /**
   * Create a new HTMLFrame.
   *
   * @param name the name of the frame
   * @param width the width of the frame
   * @param height the height of the frame
   * @param isHTML5 whether the frame is HTML5 (JavaFx)
   */
  private HTMLFrame(String name, int width, int height, boolean isHTML5) {
    super(name, RessourceManager.getSmallIcon(Icons.WINDOW_HTML));
    this.name = name;
    this.isHTML5 = isHTML5;
    width = width < 100 ? 400 : width;
    height = height < 50 ? 200 : height;
    setPreferredSize(new Dimension(width, height));

    addHTMLPanel(isHTML5);

    this.getContext().setInitMode(DockContext.STATE_FLOATING);

    /* Issue #2485
     * If the frame exists, then it's a placeholder frame that should be removed
     * Note: There should be no risk of MT frames being removed, as that is checked
     * for in showFrame() (the only place this constructor is called)
     */
    DockingManager dm = MapTool.getFrame().getDockingManager();
    if (dm.getFrame(name) != null) {
      // The frame needs to be shown before being removed otherwise the layout gets messed up
      dm.showFrame(name);
      dm.removeFrame(name, true);
    }
    /* /Issue #2485 */

    dm.addFrame(this);
    this.setVisible(true);
    addDockableFrameListener(
        new DockableFrameAdapter() {
          @Override
          public void dockableFrameHidden(DockableFrameEvent dockableFrameEvent) {
            closeRequest();
          }
        });
  }

  /**
   * Center a frame.
   *
   * @param name the name of the frame to center.
   */
  public static void center(String name) {
    if (!frames.containsKey(name)) {
      return;
    }
    HTMLFrame frame = frames.get(name);
    Dimension outerSize = MapTool.getFrame().getSize();

    int x = MapTool.getFrame().getLocation().x + (outerSize.width - frame.getWidth()) / 2;
    int y = MapTool.getFrame().getLocation().y + (outerSize.height - frame.getHeight()) / 2;

    Rectangle rect =
        new Rectangle(Math.max(x, 0), Math.max(y, 0), frame.getWidth(), frame.getHeight());
    MapTool.getFrame().getDockingManager().floatFrame(frame.getKey(), rect, true);
  }

  /**
   * Update the html content of the frame.
   *
   * @param html the html content
   * @param title the title of the frame
   * @param tabTitle the tabTitle of the frame
   * @param temp whether the frame is temporary
   * @param scrollReset whether the scrollbar should be reset
   * @param isHTML5 whether the frame should support HTML5 (JavaFX)
   * @param val the value to put in the frame
   */
  public void updateContents(
      String html,
      String title,
      String tabTitle,
      boolean temp,
      boolean scrollReset,
      boolean isHTML5,
      Object val) {
    if (this.isHTML5 != isHTML5) {
      this.isHTML5 = isHTML5;
      panel.removeFromContainer(this); // remove previous panel
      addHTMLPanel(isHTML5); // add new panel of the other HTML type
      this.revalidate();
    }
    macroCallbacks.clear();
    setTitle(title);
    setTabTitle(tabTitle);
    setTemporary(temp);
    setValue(val);
    panel.updateContents(html, scrollReset);
  }

  /** Run all callback macros for "onChangeSelection". */
  public static void doSelectedChanged() {
    for (HTMLFrame frame : frames.values()) {
      if (!frame.isHidden()) {
        HTMLPanelContainer.selectedChanged(frame.macroCallbacks);
      }
    }
  }

  /** Run all callback macros for "onChangeImpersonated". */
  public static void doImpersonatedChanged() {
    for (HTMLFrame frame : frames.values()) {
      if (!frame.isHidden()) {
        HTMLPanelContainer.impersonatedChanged(frame.macroCallbacks);
      }
    }
  }

  /**
   * Run all callback macros for "onChangeToken".
   *
   * @param token the token that changed.
   */
  public static void doTokenChanged(Token token) {
    if (token != null) {
      for (HTMLFrame frame : frames.values()) {
        if (!frame.isHidden()) {
          HTMLPanelContainer.tokenChanged(token, frame.macroCallbacks);
        }
      }
    }
  }

  /**
   * Return a json with the width, height, title, temporary, and value of the frame
   *
   * @param name the name of the frame.
   * @return a json with the width, height, title, temporary, and value of the frame, if one was
   *     found
   */
  public static Optional<JsonObject> getFrameProperties(String name) {
    if (frames.containsKey(name)) {
      HTMLFrame frame = frames.get(name);
      JsonObject frameProperties = new JsonObject();
      DockContext dc = frame.getContext();

      frameProperties.addProperty("title", frame.getTitle());
      frameProperties.addProperty("tabtitle", frame.getTabTitle());
      frameProperties.addProperty("html5", FunctionUtil.getDecimalForBoolean(frame.isHTML5));
      frameProperties.addProperty(
          "temporary", FunctionUtil.getDecimalForBoolean(frame.getTemporary()));
      frameProperties.addProperty("visible", FunctionUtil.getDecimalForBoolean(frame.isVisible()));
      frameProperties.addProperty("docked", FunctionUtil.getDecimalForBoolean(frame.isDocked()));
      frameProperties.addProperty(
          "floating",
          FunctionUtil.getDecimalForBoolean(dc.isFloated())); // Always opposite of docked?
      frameProperties.addProperty(
          "autohide", FunctionUtil.getDecimalForBoolean(frame.isAutohide()));
      frameProperties.addProperty("height", frame.getHeight());
      frameProperties.addProperty("width", frame.getWidth());
      final var undockedBounds = dc.getUndockedBounds();
      // A frame docked prior to a Restore Layout will lose its undocked bounds, causing NPE here.
      if (undockedBounds != null) {
        // The x & y are screen coordinates.
        frameProperties.addProperty("undocked_x", undockedBounds.getX());
        frameProperties.addProperty("undocked_y", undockedBounds.getY());
        frameProperties.addProperty("undocked_h", undockedBounds.getHeight());
        frameProperties.addProperty("undocked_w", undockedBounds.getWidth());
      }
      // Many of the Frame/DockContext attributes shown in the JIDE javadocs don't seem to
      // get updated.  Docked height never changes but docked width does and matches Frame
      // width.  AutoHide Height/Width never change.

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
      if (frameValue instanceof JsonElement) {
        frameProperties.add("value", (JsonElement) frameValue);
      }
      frameProperties.addProperty("value", frameValue.toString());

      return Optional.of(frameProperties);
    } else {
      return Optional.empty();
    }
  }

  @Override
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
    if (e instanceof HTMLActionEvent.ChangeTitleActionEvent) {
      String newTitle = ((HTMLActionEvent.ChangeTitleActionEvent) e).getNewTitle();
      this.setTitle(newTitle);
      this.setTabTitle(newTitle);
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
}
