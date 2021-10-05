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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;

public class SimpleSwingJavaFXDialog<T extends SwingJavaFXDialogController> {

  private final String fxmlPath;
  private final String title;
  private SwingJavaFXDialog dialog;
  private final Consumer<T> controllerCallback;

  public SimpleSwingJavaFXDialog(String fxmlPath, String title, Consumer<T> callback) {
    this.fxmlPath = fxmlPath;
    this.title = I18N.getText(title);
    this.controllerCallback = callback;
  }

  public SimpleSwingJavaFXDialog(String fxmlPath, String title) {
    this(fxmlPath, title, c -> {} /* do nothing if no call back provided */);
  }

  /** Shows the dialog and its contents. This method must be called on the Swing Event thread. */
  public void show() {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new AssertionError(
          "PlayerDatabaseDialog.show() can only be called on the Swing thread.");
    }

    FXMLLoaderUtil loaderUtil = new FXMLLoaderUtil();
    loaderUtil.jfxPanelFromFXML(fxmlPath, this::showEDT);
  }

  /**
   * Displays the contents of the {@link JFXPanel} in a {@link SwingJavaFXDialog}. This method must
   * be called on the Swing EDT thread.
   *
   * @param panel the panel to display in the dialog.
   * @param controller the controller class for the dialog.
   */
  private void showEDT(JFXPanel panel, SwingJavaFXDialogController controller) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new AssertionError("showEDT() can only be called on the Swing thread.");
    }
    dialog = new SwingJavaFXDialog(I18N.getText(title), MapTool.getFrame(), panel, controller);
    Platform.runLater(
        () -> {
          controller.registerEventHandler(this::closeDialog);
          controller.init();
        });
    dialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            controller.deregisterEventHandler(SimpleSwingJavaFXDialog.this::closeDialog);
            e.getWindow().dispose();
          }
        });
    controllerCallback.accept((T) controller);
    dialog.showDialog();
  }

  /**
   * This method closes the dialog. It is safe to call this method on any thread.
   *
   * @param controller the controller for the JavaFX gui.
   */
  private void closeDialog(SwingJavaFXDialogController controller) {
    if (SwingUtilities.isEventDispatchThread()) {
      closeDialogEDT(controller);
    } else {
      SwingUtilities.invokeLater(() -> closeDialogEDT(controller));
    }
  }

  /**
   * This method closes the dialog It must be called only on the Swing EDT thread.
   *
   * @param controller the controller for the JavaFX gui.
   */
  private void closeDialogEDT(SwingJavaFXDialogController controller) {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new AssertionError("closeDialogEDT() can only be called on the Swing thread.");
    }
    dialog.closeDialog();
  }

  /** Closes the dialog. */
  public void closeDialog() {
    dialog.closeDialog();
  }
}
