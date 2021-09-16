package net.rptools.maptool.client.ui.players;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.javfx.FXMLLoaderUtil;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialog;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.language.I18N;

public class PlayerDatabaseDialog {
  /** The path of the FXML file for the dialog. */
  private static final String FXML_PATH = "/net/rptools/maptool/client/ui/fxml"
      + "/PlayerDatabaseDialog.fxml";
  /** The {@link SwingJavaFXDialog} used to display the dialog. */
  private SwingJavaFXDialog dialog;

  /** Shows the dialog and its contents. This method must be called on the Swing Event thread. */
  public void show() {
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new AssertionError(
          "PlayerDatabaseDialog.show() can only be called on the Swing thread.");
    }

    FXMLLoaderUtil loaderUtil = new FXMLLoaderUtil();
    loaderUtil.jfxPanelFromFXML(FXML_PATH, this::showEDT);
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
      throw new AssertionError(
          "PlayerDatabaseDialog.showEDT() can only be called on the Swing thread.");
    }
    dialog =
        new SwingJavaFXDialog(
            I18N.getText("playerDB.dialogTitle"), MapTool.getFrame(), panel);
    Platform.runLater(
        () -> {
          controller.registerEventHandler(this::closeDialog);
          controller.init();
        });
    dialog.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosed(WindowEvent e) {
            controller.deregisterEventHandler(PlayerDatabaseDialog.this::closeDialog);
            e.getWindow().dispose();
          }
        });
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
      throw new AssertionError(
          "PlayerDatabaseDialog.showEDT() can only be called on the Swing thread.");
    }
    dialog.closeDialog();
  }
}
