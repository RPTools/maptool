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

import com.google.gson.JsonObject;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.util.*;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;

/**
 * Represents a JDialog holding an HTML panel. Can hold either an HTML3.2 (Swing) or a HTML5
 * (JavaFX) panel.
 */
@SuppressWarnings("serial")
public class HTMLDialog extends JDialog implements HTMLPanelContainer {
  /** The static map of the HTMLDialogs. */
  private static final Map<String, HTMLDialog> dialogs = new HashMap<>();

  /** The map of the macro callbacks. */
  private final Map<String, String> macroCallbacks = new HashMap<String, String>();

  /** The temporary status of the dialog. A temporary dialog isn't stored after being closed. */
  private boolean temporary;

  /** The input status of the dialog (input=true: automatically close on form submit) */
  private boolean input;

  /** The value stored in the frame. */
  private Object value;

  /** Panel for HTML. */
  private HTMLPanelInterface panel;

  /** The name of the frame. */
  private final String name;

  /** Can the dialog be resized? */
  private static final boolean canResize = true;

  /** The parent of the dialog */
  private final Frame parent;

  /** The panel for the close button. */
  private final JPanel closePanel = new JPanel();

  /** Is the panel HTML5 or HTML3.2. */
  private boolean isHTML5;

  @Override
  public Map<String, String> macroCallbacks() {
    return macroCallbacks;
  }

  /**
   * Return whether the frame is visible or not.
   *
   * @param name the name of the frame.
   * @return true if the frame is visible.
   */
  static boolean isVisible(String name) {
    if (dialogs.containsKey(name)) {
      return dialogs.get(name).isVisible();
    }
    return false;
  }

  /**
   * Gets an unmodifiable set view of the names of all known dialogs.
   *
   * @return the dialog names
   */
  public static Set<String> getDialogNames() {
    return Collections.unmodifiableSet(dialogs.keySet());
  }

  /**
   * Runs a javascript on a dialog.
   *
   * @param name the name of the dialog
   * @param script the script to run
   * @return true if the dialog exists and can run the script, false otherwise
   */
  public static boolean runScript(String name, String script) {
    HTMLDialog dialog = dialogs.get(name);
    return dialog != null && dialog.panel.runJavascript(script);
  }

  /**
   * Request that the frame close.
   *
   * @param name The name of the frame.
   */
  static void close(String name) {
    if (dialogs.containsKey(name)) {
      dialogs.get(name).closeRequest();
    }
  }

  /**
   * Create a HTMLDialog
   *
   * @param parent the parent frame.
   * @param name the name of the dialog
   * @param decorated whether the dialog is decorated (no frame/title)
   * @param width the width of the dialog
   * @param height the height of the dialog
   * @param isHTML5 whether the dialog should use HTML5 or HTML3.2
   */
  private HTMLDialog(
      Frame parent, String name, boolean decorated, int width, int height, boolean isHTML5) {
    super(parent, name, false);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            closeRequest();
          }
        });
    this.name = name;
    this.isHTML5 = isHTML5;
    this.parent = parent;

    // Only set preferred size at creation.
    width = width < 100 ? 400 : width;
    height = height < 50 ? 200 : height;
    setPreferredSize(new Dimension(width, height));

    // Creation of HTML panel.
    addHTMLPanel(decorated, isHTML5);

    // Creation of close panel.
    JButton jcloseButton = new JButton(I18N.getText("msg.button.close"));
    jcloseButton.setActionCommand("Close");
    jcloseButton.addActionListener(this);
    closePanel.setLayout(new BoxLayout(closePanel, BoxLayout.LINE_AXIS));
    closePanel.add(Box.createHorizontalGlue());
    closePanel.add(jcloseButton);
    closePanel.add(Box.createHorizontalGlue());

    // Size the dialog, make it displayable, and validate it
    pack();

    // Center dialog
    SwingUtil.centerOver(this, parent);
  }

  /**
   * Add an HTML panel to the dialog.
   *
   * @param scrollBar whether the dialog is to have a toolbar; not working for HTML5
   * @param isHTML5 whether the panel supports HTML5
   */
  public void addHTMLPanel(boolean scrollBar, boolean isHTML5) {
    if (isHTML5) {
      panel = new HTMLJFXPanel(this, new HTMLWebViewManager(this, "dialog5", this.name));
    } else {
      panel = new HTMLPanel(this, scrollBar);
    }
    panel.addToContainer(this);
    panel.addActionListener(this);
  }

  /**
   * Shows the HTML Dialog. This will create a new dialog if the named dialog does not already
   * exist. The width and height fields are ignored if the dialog has already been opened so that it
   * will not override any resizing that the user may have done.
   *
   * @param name the name of the dialog.
   * @param title the title for the dialog window .
   * @param width the width in pixels of the dialog.
   * @param height the height in pixels of the dialog.
   * @param frame whether the frame is decorated (frame and title)
   * @param input whether submitting the form closes it
   * @param temp whether the frame is temporary
   * @param closeButton whether the close button is to be displayed
   * @param scrollReset whether the scrollbar should be reset
   * @param isHTML5 whether the frame should support HTML5
   * @param value a value to be returned by getDialogProperties()
   * @param html the HTML to display in the dialog
   * @return the dialog
   */
  static HTMLDialog showDialog(
      String name,
      String title,
      int width,
      int height,
      boolean frame,
      boolean input,
      boolean temp,
      boolean closeButton,
      boolean scrollReset,
      boolean isHTML5,
      Object value,
      String html) {
    HTMLDialog dialog;
    if (dialogs.containsKey(name)) {
      dialog = dialogs.get(name);
    } else {
      dialog = new HTMLDialog(MapTool.getFrame(), name, frame, width, height, isHTML5);
      dialogs.put(name, dialog);
    }
    dialog.updateContents(
        html, title, frame, input, temp, closeButton, scrollReset, isHTML5, value);

    // dialog.canResize = false;
    if (!dialog.isVisible()) {
      dialog.setVisible(true);
    }
    return dialog;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean getTemporary() {
    return this.temporary;
  }

  @Override
  public void setTemporary(boolean temp) {
    this.temporary = temp;
  }

  /**
   * Return a json with the width, height, temporary variable and title of the dialog
   *
   * @param name The name of the frame.
   * @return A json with the width, height, temporary, title, and value of dialog, if one was found
   */
  public static Optional<JsonObject> getDialogProperties(String name) {
    if (dialogs.containsKey(name)) {
      HTMLDialog dialog = dialogs.get(name);
      JsonObject dialogProperties = new JsonObject();

      dialogProperties.addProperty("width", dialog.getWidth());
      dialogProperties.addProperty("height", dialog.getHeight());
      dialogProperties.addProperty(
          "temporary", FunctionUtil.getDecimalForBoolean(dialog.getTemporary()));
      dialogProperties.addProperty("title", dialog.getTitle());
      dialogProperties.addProperty(
          "visible", FunctionUtil.getDecimalForBoolean(dialog.isVisible()));
      dialogProperties.addProperty(
          "noframe", FunctionUtil.getDecimalForBoolean(dialog.isUndecorated()));
      dialogProperties.addProperty("input", FunctionUtil.getDecimalForBoolean(dialog.input));
      dialogProperties.addProperty(
          "closebutton", FunctionUtil.getDecimalForBoolean(dialog.isAncestorOf(dialog.closePanel)));
      dialogProperties.addProperty("html5", FunctionUtil.getDecimalForBoolean(dialog.isHTML5));
      Object dialogValue = dialog.getValue();
      if (dialogValue == null) {
        dialogValue = "";
      } else {
        if (dialogValue instanceof String) {
          // try to convert to a number
          try {
            BigDecimal dialogValueBD = new BigDecimal(dialogValue.toString());
          } catch (Exception e) {
          }
        }
      }
      dialogProperties.add("value", JSONMacroFunctions.getInstance().asJsonElement(dialogValue));

      return Optional.of(dialogProperties);
    } else {
      return Optional.empty();
    }
  }

  /** Run all callback macros for "onChangeSelection". */
  public static void doSelectedChanged() {
    for (HTMLDialog dialog : dialogs.values()) {
      if (dialog.isVisible()) {
        HTMLPanelContainer.selectedChanged(dialog.macroCallbacks());
      }
    }
  }

  /** Run all callback macros for "onChangeImpersonated". */
  public static void doImpersonatedChanged() {
    for (HTMLDialog dialog : dialogs.values()) {
      if (dialog.isVisible()) {
        HTMLPanelContainer.impersonatedChanged(dialog.macroCallbacks());
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
      for (HTMLDialog dialog : dialogs.values()) {
        if (dialog.isVisible()) {
          HTMLPanelContainer.tokenChanged(token, dialog.macroCallbacks());
        }
      }
    }
  }

  /**
   * Updates the contents of the dialog.
   *
   * @param html the html contents of the dialog
   * @param title the title of the dialog
   * @param decorated whether to decorate form with frame and title bar
   * @param input whether to close the dialog on form submit
   * @param temp whether to make the dialog temporary
   * @param closeButton whether to show a close button
   * @param scrollReset whether the scrollbar should be reset
   * @param isHTML5 whether to make the dialog HTML5 (JavaFX)
   * @param val the value held in the frame
   */
  private void updateContents(
      String html,
      String title,
      boolean decorated,
      boolean input,
      boolean temp,
      boolean closeButton,
      boolean scrollReset,
      boolean isHTML5,
      Object val) {
    if (this.isHTML5 != isHTML5) {
      this.isHTML5 = isHTML5;
      panel.removeFromContainer(this); // remove previous panel
      addHTMLPanel(decorated, isHTML5); // add new panel of the other HTML type
    }
    if (decorated == isUndecorated()) {
      dispose(); // required by setUndecorated
      setUndecorated(!decorated);
      pack();
      SwingUtil.centerOver(this, parent);
    }
    this.input = input;
    this.temporary = temp;
    this.value = val;
    this.setTitle(title);
    macroCallbacks.clear();
    updateButton(closeButton);
    panel.updateContents(html, scrollReset);
  }

  /**
   * Updates the close button of the dialog.
   *
   * @param closeButton whether to show the closeButton
   */
  public void updateButton(boolean closeButton) {
    if (closeButton) {
      add(closePanel, BorderLayout.SOUTH);
    } else {
      remove(closePanel);
    }
    revalidate();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e instanceof HTMLActionEvent.FormActionEvent) {
      if (input) {
        closeRequest();
      }
      HTMLActionEvent.FormActionEvent fae = (HTMLActionEvent.FormActionEvent) e;
      MacroLinkFunction.runMacroLink(fae.getAction() + fae.getData());
    }
    if (e instanceof HTMLActionEvent.ChangeTitleActionEvent) {
      this.setTitle(((HTMLActionEvent.ChangeTitleActionEvent) e).getNewTitle());
    }
    if (e instanceof HTMLActionEvent.RegisterMacroActionEvent) {
      HTMLActionEvent.RegisterMacroActionEvent rmae = (HTMLActionEvent.RegisterMacroActionEvent) e;
      macroCallbacks.put(rmae.getType(), rmae.getMacro());
    }

    if (e instanceof HTMLActionEvent.MetaTagActionEvent) {
      String name = ((HTMLActionEvent.MetaTagActionEvent) e).getName();
      String content = ((HTMLActionEvent.MetaTagActionEvent) e).getContent();
      if (name.equalsIgnoreCase("input")) {
        input = Boolean.parseBoolean(content);
      } else if (name.equalsIgnoreCase("closebutton")) {
        updateButton(Boolean.parseBoolean(content));
      } else if (name.equalsIgnoreCase("onChangeToken")
          || name.equalsIgnoreCase("onChangeSelection")
          || name.equalsIgnoreCase("onChangeImpersonated")) {
        macroCallbacks.put(name, content);
      } else if (name.equalsIgnoreCase("width")) {
        if (canResize) {
          setSize(new Dimension(Integer.parseInt(content), getHeight()));
          validate();
        }
      } else if (name.equalsIgnoreCase("height")) {
        if (canResize) {
          setSize(new Dimension(getWidth(), Integer.parseInt(content)));
          SwingUtil.centerOver(this, parent);
          this.validate();
        }
      } else if (name.equalsIgnoreCase("temporary")) {
        temporary = Boolean.parseBoolean(content);
        SwingUtil.centerOver(this, parent);
      } else if (name.equalsIgnoreCase("value")) {
        SwingUtil.centerOver(this, parent);
        setValue(content);
      }
    }
    if (e.getActionCommand().equals("Close")) {
      closeRequest();
    }
  }

  @Override
  public void closeRequest() {
    setVisible(false);
    panel.flush();
    if (temporary) {
      dialogs.remove(this.name);
      dispose();
    }
  }

  @Override
  public Component add(Component component) {
    return super.add(component);
  }
}
