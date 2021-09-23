/**
 * Sample Skeleton for 'PlayerDatabaseEdit.fxml' Controller Class
 */

package net.rptools.maptool.client.ui.players;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogEventHandler;
import net.rptools.maptool.model.player.Players;

public class PlayerDatabaseEditController  implements SwingJavaFXDialogController {

  private final Set<SwingJavaFXDialogEventHandler> eventHandlers = ConcurrentHashMap.newKeySet();

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="okButton"
  private Button okButton; // Value injected by FXMLLoader

  @FXML // fx:id="cancelButton"
  private Button cancelButton; // Value injected by FXMLLoader

  @FXML // fx:id="playerNameText"
  private TextField playerNameText; // Value injected by FXMLLoader

  @FXML // fx:id="blockedReasonText"
  private TextField blockedReasonText; // Value injected by FXMLLoader

  @FXML // fx:id="roleCombo"
  private ComboBox<?> roleCombo; // Value injected by FXMLLoader

  @FXML // fx:id="authTypeCombo"
  private ComboBox<?> authTypeCombo; // Value injected by FXMLLoader

  @FXML // fx:id="passwordText"
  private TextField passwordText; // Value injected by FXMLLoader

  @FXML // fx:id="generatePasswordButton"
  private Button generatePasswordButton; // Value injected by FXMLLoader

  @FXML // fx:id="publicKeyText"
  private TextArea publicKeyText; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert okButton != null : "fx:id=\"okButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert cancelButton != null : "fx:id=\"cancelButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert playerNameText != null : "fx:id=\"playerNameText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert blockedReasonText != null : "fx:id=\"blockedReasonText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert roleCombo != null : "fx:id=\"roleCombo\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert authTypeCombo != null : "fx:id=\"authTypeCombo\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert passwordText != null : "fx:id=\"passwordText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert generatePasswordButton != null : "fx:id=\"generatePasswordButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert publicKeyText != null : "fx:id=\"publicKeyText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";

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

  }
}
