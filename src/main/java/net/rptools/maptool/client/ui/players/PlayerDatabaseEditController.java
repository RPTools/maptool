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
package net.rptools.maptool.client.ui.players;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogEventHandler;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.model.player.PlayerInfo;

public class PlayerDatabaseEditController implements SwingJavaFXDialogController {

  private final Set<SwingJavaFXDialogEventHandler> eventHandlers = ConcurrentHashMap.newKeySet();

  private final String GM_ROLE_NAME = I18N.getText("userTerm.GM");
  private final String PLAYER_ROLE_NAME = I18N.getText("userTerm.Player");

  private final String AUTH_PUB_KEY_NAME = I18N.getText("playerDB.dialog.publicKey");
  private final String AUTH_PASSWORD_NAME = I18N.getText("playerDB.dialog.password");

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
  private ComboBox<String> roleCombo; // Value injected by FXMLLoader

  @FXML // fx:id="authTypeCombo"
  private ComboBox<String> authTypeCombo; // Value injected by FXMLLoader

  @FXML // fx:id="passwordText"
  private TextField passwordText; // Value injected by FXMLLoader

  @FXML // fx:id="generatePasswordButton"
  private Button generatePasswordButton; // Value injected by FXMLLoader

  @FXML // fx:id="publicKeyText"
  private TextArea publicKeyText; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert okButton != null
        : "fx:id=\"okButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert cancelButton != null
        : "fx:id=\"cancelButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert playerNameText != null
        : "fx:id=\"playerNameText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert blockedReasonText != null
        : "fx:id=\"blockedReasonText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert roleCombo != null
        : "fx:id=\"roleCombo\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert authTypeCombo != null
        : "fx:id=\"authTypeCombo\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert passwordText != null
        : "fx:id=\"passwordText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert generatePasswordButton != null
        : "fx:id=\"generatePasswordButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert publicKeyText != null
        : "fx:id=\"publicKeyText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
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
    ObservableList<String> roles =
        FXCollections.observableArrayList(PLAYER_ROLE_NAME, GM_ROLE_NAME);
    roleCombo.setItems(roles);
    ObservableList<String> authType =
        FXCollections.observableArrayList(AUTH_PUB_KEY_NAME, AUTH_PASSWORD_NAME);

    authTypeCombo.setItems(authType);
  }

  public void setPlayerInfo(PlayerInfo playerInfo) {
    playerNameText.setText(playerInfo.name());
    blockedReasonText.setText(playerInfo.blockedReason());
    roleCombo.setValue(playerInfo.role() == Role.GM ? GM_ROLE_NAME : PLAYER_ROLE_NAME);
    if (playerInfo.authMethod() == AuthMethod.PASSWORD) {
      authTypeCombo.setValue(AUTH_PASSWORD_NAME);
      passwordText.setText("-- Encoded Password --");
      publicKeyText.setText("");
    } else {
      authTypeCombo.setValue(AUTH_PUB_KEY_NAME);
      passwordText.setText("");
      publicKeyText.setText("TODO: CDW Need to put public key here.");
    }
  }
}
