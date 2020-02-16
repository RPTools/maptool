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
package net.rptools.maptool.client.ui.javfx;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javafx.embed.swing.JFXPanel;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import net.rptools.lib.swing.SwingUtil;

/**
 * Implements a Swing dialog with JavaFX contents. This is currently preferable to creating a
 * top level JavaFX dialog as it wont get pushed being MapTool if a modal dialog opens.
 */
public class SwingJavaFXDialog extends JDialog {

  /** Keeps track of if the dialog has already positioned itself. */
  private boolean hasPositionedItself;

  /**
   *  Creates a new modal {@code SwingJavaFXDialog}.
   *
   * @param title The title of the dialog.
   * @param parent The swing {@link Frame} that is the parent.
   * @param panel The JavaFX content to display.
   */
  public SwingJavaFXDialog(String title, Frame parent, JFXPanel panel) {
    this(title, parent, panel, true);
  }

  /**
   *  Creates a new {@code SwingJavaFXDialog}.
   *
   * @param title The title of the dialog.
   * @param parent The swing {@link Frame} that is the parent.
   * @param panel The JavaFX content to display.
   * @param modal if {@code true} to create a modal dialog.
   */
  public SwingJavaFXDialog(String title, Frame parent, JFXPanel panel, boolean modal) {
    super(parent, title, modal);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    setLayout(new GridLayout());

    add(panel);
    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            closeDialog();
          }
        });
    // ESCAPE cancels the window without committing
    panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
         .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    panel.getActionMap()
         .put("cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                closeDialog();
              }
            });
  }

  /**
   * Closes the dialog and disposes of resources.
   */
  public void closeDialog() {
    dispose();
  }

  /**
   * Centers the dialog over the parent.
   */
  protected void positionInitialView() {
    SwingUtil.centerOver(this, getOwner());
  }

  /**
   * Displays the dialog.
   */
  public void showDialog() {
    // We want to center over our parent, but only the first time.
    // If this dialog is reused, we want it to show up where it was last.
    if (!hasPositionedItself) {
      pack();
      positionInitialView();
      hasPositionedItself = true;
    }
    setVisible(true);
  }
}
