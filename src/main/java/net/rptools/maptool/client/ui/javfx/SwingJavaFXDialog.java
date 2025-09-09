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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javafx.embed.swing.JFXPanel;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.swing.SwingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Implements a Swing dialog with JavaFX contents. This is currently preferable to creating a top
 * level JavaFX dialog as it wont get pushed being MapTool if a modal dialog opens.
 *
 * <p>Objects of this class must be created on the Swing EDT. All the other methods will ensure that
 * they perform their tasks on the Swing EDT.
 */
public class SwingJavaFXDialog extends JDialog {

  /** Class for log messages. */
  private static final Logger log = LogManager.getLogger(SwingJavaFXDialog.class);

  /** Keeps track of if the dialog has already positioned itself. */
  private boolean hasPositionedItself;

  /** The controller for the JFX Panel. */
  private final SwingJavaFXDialogController controller;

  /**
   * Creates a new modal {@code SwingJavaFXDialog}.
   *
   * @param title The title of the dialog.
   * @param parent The swing {@link Frame} that is the parent.
   * @param panel The JavaFX content to display.
   * @throws IllegalStateException if not run on the Swing EDT thread.
   * @note This constructor must only be used on the Swing EDT thread.
   */
  public SwingJavaFXDialog(String title, Frame parent, JFXPanel panel) {
    this(title, parent, panel, true, null);
  }

  /**
   * Creates a new modal {@code SwingJavaFXDialog}.
   *
   * @param title The title of the dialog.
   * @param parent The swing {@link Frame} that is the parent.
   * @param panel The JavaFX content to display.
   * @param jfxController the controller for the javaFX UI.
   * @throws IllegalStateException if not run on the Swing EDT thread.
   * @note This constructor must only be used on the Swing EDT thread.
   */
  public SwingJavaFXDialog(
      String title, Frame parent, JFXPanel panel, SwingJavaFXDialogController jfxController) {
    this(title, parent, panel, true, jfxController);
  }

  /**
   * Creates a new {@code SwingJavaFXDialog}.
   *
   * @param title The title of the dialog.
   * @param parent The swing {@link Frame} that is the parent.
   * @param panel The JavaFX content to display.
   * @param modal if {@code true} to create a modal dialog.
   * @throws IllegalStateException if not run on the Swing EDT thread.
   * @note This constructor must only be used on the Swing EDT thread.
   */
  public SwingJavaFXDialog(String title, Frame parent, JFXPanel panel, boolean modal) {
    this(title, parent, panel, modal, null);
  }

  /**
   * Creates a new {@code SwingJavaFXDialog}.
   *
   * @param title The title of the dialog.
   * @param parent The swing {@link Frame} that is the parent.
   * @param panel The JavaFX content to display.
   * @param modal if {@code true} to create a modal dialog.
   * @param jfxController the controller for the javaFX UI.
   * @throws IllegalStateException if not run on the Swing EDT threadO.
   * @note This constructor must only be used on the Swing EDT thread.
   */
  public SwingJavaFXDialog(
      String title,
      Frame parent,
      JFXPanel panel,
      boolean modal,
      SwingJavaFXDialogController jfxController) {
    super(parent, title, modal);

    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException("SwingJavaFXDialog must be created on the Swing EDT thread.");
    }

    controller = jfxController;

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
    panel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    panel
        .getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                closeDialog();
              }
            });
    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            revalidate();
          }
        });
  }

  /**
   * Closes the dialog and disposes of resources.
   *
   * @note It is safe to run this method from any thread.
   */
  public void closeDialog() {
    if (SwingUtilities.isEventDispatchThread()) {
      closeDialogEDT();
    } else {
      SwingUtilities.invokeLater(this::closeDialogEDT);
    }
  }

  /**
   * Closes the dialog and disposes of resources.
   *
   * @note This method must be run from the Swing EDT thread.
   */
  private void closeDialogEDT() {
    if (controller != null) {
      controller.close();
    }
    dispose();
  }

  /**
   * Centers the dialog over the parent.
   *
   * @note It is safe to run this method from any thread.
   */
  protected void positionInitialView() {
    if (SwingUtilities.isEventDispatchThread()) {
      positionInitialViewEDT();
    } else {
      SwingUtilities.invokeLater(this::positionInitialViewEDT);
    }
  }

  /**
   * Centers the dialog over the parent.
   *
   * @note This method must be run from the Swing EDT thread.
   */
  private void positionInitialViewEDT() {
    SwingUtil.centerOver(this, getOwner());
  }

  /**
   * Displays the dialog.
   *
   * @note This method must be run from the Swing EDT thread.
   */
  public void showDialogEDT() {
    // We want to center over our parent, but only the first time.
    // If this dialog is reused, we want it to show up where it was last.
    if (!hasPositionedItself) {
      pack();
      positionInitialView();
      hasPositionedItself = true;
    }
    setVisible(true);
  }

  /**
   * Displays the dialog.
   *
   * @note It is safe to run this method from any thread.
   */
  public void showDialog() {
    if (SwingUtilities.isEventDispatchThread()) {
      showDialogEDT();
    } else {
      SwingUtilities.invokeLater(this::showDialogEDT);
    }
  }
}
