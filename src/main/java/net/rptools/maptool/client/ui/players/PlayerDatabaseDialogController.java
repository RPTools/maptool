package net.rptools.maptool.client.ui.players;

/**
 * Sample Skeleton for 'PlayerDatabaseDialog.fxml' Controller Class
 */

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogEventHandler;

public class PlayerDatabaseDialogController implements SwingJavaFXDialogController {

  private final KeySetView<SwingJavaFXDialogEventHandler, Boolean> eventHandlers =
      ConcurrentHashMap.newKeySet();

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="playersTable"
  private TableView<?> playersTable; // Value injected by FXMLLoader

  @FXML // fx:id="cancelButton"
  private Button cancelButton; // Value injected by FXMLLoader

  @FXML // fx:id="saveChangesButton"
  private Button saveChangesButton; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert playersTable != null : "fx:id=\"playersTable\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert saveChangesButton != null : "fx:id=\"saveChangesButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";

  }

  @Override
  public void registerEventHandler(SwingJavaFXDialogEventHandler handler) {
    eventHandlers.add(handler);
  }

  @Override
  public void deregisterEventHandler(SwingJavaFXDialogEventHandler handler) {
    eventHandlers.remove(handler);
  }

  @Override
  public void init() {
    // TODO: CDW
  }
}
