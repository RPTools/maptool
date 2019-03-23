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

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JDialog;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.Token;

@SuppressWarnings("serial")
public class HTMLDialog extends JDialog implements HTMLPanelContainer {
  private static Map<String, HTMLDialog> dialogs = new HashMap<String, HTMLDialog>();

  private final Map<String, String> macroCallbacks = new HashMap<String, String>();
  private boolean temporary;
  private boolean input;
  private final HTMLPanel panel;
  private final String name;
  private final boolean canResize = true;
  private final Frame parent;
  private boolean closeButton;

  /**
   * Returns if the frame is visible or not.
   *
   * @param name The name of the frame.
   * @return true if the frame is visible.
   */
  static boolean isVisible(String name) {
    if (dialogs.containsKey(name)) {
      return dialogs.get(name).isVisible();
    }
    return false;
  }

  /**
   * Requests that the frame close.
   *
   * @param name The name of the frame.
   */
  static void close(String name) {
    if (dialogs.containsKey(name)) {
      dialogs.get(name).closeRequest();
    }
  }

  /**
   * Creates a HTMLDialog
   *
   * @param parent The parent frame.
   * @param name The name of the dialog.
   * @param title The title of the dialog.
   * @param undecorated If the dialog is decorated or not.
   * @param width The width of the dialog.
   * @param height The height of the dialog.
   * @param closeButton if the close button should be displayed or not.
   */
  private HTMLDialog(
      Frame parent,
      String name,
      String title,
      boolean undecorated,
      boolean closeButton,
      int width,
      int height) {
    super(parent, title, false);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            closeRequest();
          }
        });
    this.name = name;
    setUndecorated(undecorated);

    width = width < 100 ? 400 : width;
    height = height < 50 ? 200 : height;
    setPreferredSize(new Dimension(width, height));

    panel = new HTMLPanel(this, closeButton, !undecorated);
    add(panel);
    pack();
    this.parent = parent;

    SwingUtil.centerOver(this, parent);
  }

  /**
   * Shows the HTML Dialog. This will create a new dialog if the named dialog does not already
   * exist. The width and height fields are ignored if the dialog has already been opened so that it
   * will not override any resizing that the user may have done.
   *
   * @param name The name of the dialog.
   * @param title The title for the dialog window .
   * @param width The width in pixels of the dialog.
   * @param height The height in pixels of the dialog.
   * @param frame If the dialog is decorated with frame or not.
   * @param input Is the dialog an input only dialog.
   * @param temp Is the dialog temporary.
   * @param closeButton should the close button be displayed or not.
   * @param html The HTML to display in the dialog.
   * @return The dialog.
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
      String html) {
    HTMLDialog dialog;
    if (dialogs.containsKey(name)) {
      dialog = dialogs.get(name);
      dialog.updateContents(html, temp, closeButton, input);
    } else {
      dialog = new HTMLDialog(MapTool.getFrame(), name, title, !frame, closeButton, width, height);
      dialogs.put(name, dialog);
      dialog.updateContents(html, temp, closeButton, input);
    }
    // dialog.canResize = false;
    if (!dialog.isVisible()) {
      dialog.setVisible(true);
    }
    return dialog;
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
    for (HTMLDialog dialog : dialogs.values()) {
      if (dialog.isVisible()) {
        dialog.selectedChanged();
      }
    }
  }

  /** A new token has been impersonated or the impersonated token is cleared. */
  public static void doImpersonatedChanged() {
    for (HTMLDialog dialog : dialogs.values()) {
      if (dialog.isVisible()) {
        dialog.impersonatedChanged();
      }
    }
  }

  /** One of the tokens has changed. */
  public static void doTokenChanged(Token token) {
    if (token != null) {
      for (HTMLDialog dialog : dialogs.values()) {
        if (dialog.isVisible()) {
          dialog.tokenChanged(token);
        }
      }
    }
  }

  /**
   * Updates the contents of the dialog.
   *
   * @param html The html contents of the dialog.
   * @param temp Is the dialog temporary or not.
   * @param closeButton does the dialog have a close button.
   */
  private void updateContents(String html, boolean temp, boolean closeButton, boolean input) {
    this.input = input;
    this.closeButton = closeButton;
    this.temporary = temp;
    macroCallbacks.clear();
    panel.updateContents(html, closeButton);
  }

  public void actionPerformed(ActionEvent e) {
    if (e instanceof HTMLPane.FormActionEvent) {
      if (input) {
        closeRequest();
      }
      HTMLPane.FormActionEvent fae = (HTMLPane.FormActionEvent) e;
      MacroLinkFunction.getInstance().runMacroLink(fae.getAction() + fae.getData());
    }
    if (e instanceof HTMLPane.ChangeTitleActionEvent) {
      this.setTitle(((HTMLPane.ChangeTitleActionEvent) e).getNewTitle());
    }
    if (e instanceof HTMLPane.RegisterMacroActionEvent) {
      HTMLPane.RegisterMacroActionEvent rmae = (HTMLPane.RegisterMacroActionEvent) e;
      macroCallbacks.put(rmae.getType(), rmae.getMacro());
    }
    if (e instanceof HTMLPane.MetaTagActionEvent) {
      HTMLPane.MetaTagActionEvent mtae = (HTMLPane.MetaTagActionEvent) e;
      if (mtae.getName().equalsIgnoreCase("input")) {
        Boolean val = Boolean.valueOf(mtae.getContent());
        input = val;
        closeButton = !input;
      } else if (mtae.getName().equalsIgnoreCase("closebutton")) {
        Boolean val = Boolean.valueOf(mtae.getContent());
        closeButton = val;
        panel.updateContents(closeButton);
      } else if (mtae.getName().equalsIgnoreCase("onChangeToken")
          || mtae.getName().equalsIgnoreCase("onChangeSelection")
          || mtae.getName().equalsIgnoreCase("onChangeImpersonated")) {
        macroCallbacks.put(mtae.getName(), mtae.getContent());
      } else if (mtae.getName().equalsIgnoreCase("width")) {
        if (canResize) {
          setSize(new Dimension(Integer.parseInt(mtae.getContent()), getHeight()));
          validate();
        }
      } else if (mtae.getName().equalsIgnoreCase("height")) {
        if (canResize) {
          setSize(new Dimension(getWidth(), Integer.parseInt(mtae.getContent())));
          SwingUtil.centerOver(this, parent);
          this.validate();
        }
      } else if (mtae.getName().equalsIgnoreCase("temporary")) {
        Boolean val = Boolean.valueOf(mtae.getContent());
        SwingUtil.centerOver(this, parent);
        temporary = val;
      }
    }
    if (e.getActionCommand().equals("Close")) {
      closeRequest();
    }
  }

  public void closeRequest() {
    setVisible(false);
    panel.flush();
    if (temporary) {
      dialogs.remove(this.name);
      dispose();
    }
  }
}
