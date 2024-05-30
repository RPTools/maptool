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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.EnumSet;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javax.crypto.NoSuchPaddingException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogEventHandler;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.model.player.PlayerInfo;
import net.rptools.maptool.model.player.Players;
import net.rptools.maptool.util.PasswordGenerator;
import net.rptools.maptool.util.cipher.CipherUtil;

/** Controller for the dialog used to edit player details in the database. */
public class PlayerDatabaseEditController implements SwingJavaFXDialogController {

  /** Minimum acceptable password length. */
  private static final int MIN_PASSWORD_LENGTH = 8;

  /** Event handlers to listen to close requests. */
  private final Set<SwingJavaFXDialogEventHandler> eventHandlers = ConcurrentHashMap.newKeySet();

  /** The localised name for GMs. */
  private final String GM_ROLE_NAME = I18N.getText("userTerm.GM");

  /** The localised name for Players. */
  private final String PLAYER_ROLE_NAME = I18N.getText("userTerm.Player");

  /** The localised name for public key. */
  private final String AUTH_PUB_KEY_NAME = I18N.getText("playerDB.dialog.publicKey");

  /** The localised name for password. */
  private final String AUTH_PASSWORD_NAME = I18N.getText("playerDB.dialog.password");

  /** public key entered. */
  private String publicKeyString = "";

  /** The reason entered for the player block. */
  private String blockedReason = "";

  /** the name entered for the player */
  private String playerName = "";

  /** The localised nae for encoded password. */
  private String password = I18N.getText("playerDB.dialog.encodedPassword");

  /** is the dialog in new player mode. */
  private boolean newPlayerMode = false;

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

  @FXML // fx:id="blockedCheckBox"
  private CheckBox blockedCheckBox; // Value injected by FXMLLoader

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

  /** Different validation errors that can occur. */
  private enum ValidationErrors {
    NAME_MISSING("playerDB.dialog.error.missingName"),
    PASSWORD_MISSING("playerDB.dialog.error.passwordMissing"),
    PASSWORD_TOO_SHORT("playerDB.dialog.error.passwordTooShort"),
    INVALID_PUBLIC_KEY("playerDB.dialog.error.invalidPublicKey"),
    BLOCKED_REASON_EMPTY("playerDB.dialog.error.emptyBlockReason"),
    PLAYER_EXISTS("playerDB.dialog.error.playerExists");

    /** the key for the localised version of the error message. */
    private final String i81nKey;

    /**
     * Creates a new {@code ValidationErrors} enum value.
     *
     * @param i81nKey the key for the localised string.
     */
    ValidationErrors(String i81nKey) {
      this.i81nKey = i81nKey;
    }

    /**
     * Returns the localised validation error string.
     *
     * @return the localised validation error string.
     */
    private String getDescription() {
      return I18N.getText(i81nKey);
    }
  }

  /** Current dialog validation errors. */
  private final Set<ValidationErrors> validationErrors = EnumSet.noneOf(ValidationErrors.class);

  @FXML // fx:id="labelErrors"
  private Label labelErrors; // Value injected by FXMLLoader

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert okButton != null
        : "fx:id=\"okButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert cancelButton != null
        : "fx:id=\"cancelButton\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert playerNameText != null
        : "fx:id=\"playerNameText\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
    assert blockedCheckBox != null
        : "fx:id=\"blockedCheckBoc\" was not injected: check your FXML file 'PlayerDatabaseEdit.fxml'.";
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
    assert labelErrors != null
        : "fx:id=\"labelErrors\" was not injected: check your FXML "
            + "file 'PlayerDatabaseEdit.fxml'.";
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
    roleCombo.getSelectionModel().select(PLAYER_ROLE_NAME);

    ObservableList<String> authType =
        FXCollections.observableArrayList(AUTH_PUB_KEY_NAME, AUTH_PASSWORD_NAME);

    authTypeCombo.setItems(authType);
    authTypeCombo.getSelectionModel().select(AUTH_PUB_KEY_NAME);

    authTypeCombo.setOnAction(
        a -> {
          enableDisableFields();
        });

    blockedCheckBox.setOnAction(
        a -> {
          enableDisableFields();
          validateBlockedReason();
          ;
        });

    generatePasswordButton.setOnAction(
        a -> {
          passwordText.setText(new PasswordGenerator().getPassword());
        });

    playerNameText
        .textProperty()
        .addListener(
            l -> {
              playerName = playerNameText.getText();
              validatePlayerName();
            });

    publicKeyText
        .textProperty()
        .addListener(
            l -> {
              publicKeyString = publicKeyText.getText();
              validatePublicKey();
            });

    passwordText
        .textProperty()
        .addListener(
            l -> {
              password = passwordText.getText();
              validatePassword();
            });

    blockedReasonText
        .textProperty()
        .addListener(
            l -> {
              blockedReason = blockedReasonText.getText();
              validateBlockedReason();
            });

    enableDisableFields();
    playerNameText.setText("");

    cancelButton.setOnAction(a -> performClose());
    okButton.setOnAction(h -> updateDatabase());
  }

  /** Validates the blocked reason text field. */
  private void validateBlockedReason() {
    validationErrors.remove(ValidationErrors.BLOCKED_REASON_EMPTY);
    if (blockedCheckBox.isSelected() && blockedReasonText.getText().isEmpty()) {
      validationErrors.add(ValidationErrors.BLOCKED_REASON_EMPTY);
    }
    displayValidationErrors();
  }

  /** This method validates the password field. */
  private void validatePassword() {
    validationErrors.remove(ValidationErrors.PASSWORD_MISSING);
    validationErrors.remove(ValidationErrors.PASSWORD_TOO_SHORT);
    if (authTypeCombo.getSelectionModel().getSelectedItem().equals(AUTH_PASSWORD_NAME)) {
      int passLen = passwordText.getText().length();
      if (passLen == 0) {
        validationErrors.add(ValidationErrors.PASSWORD_MISSING);
      } else if (passLen < MIN_PASSWORD_LENGTH) {
        validationErrors.add(ValidationErrors.PASSWORD_TOO_SHORT);
      }
    }
    displayValidationErrors();
  }

  /** This method validates the public key text areas. */
  private void validatePublicKey() {
    validationErrors.remove(ValidationErrors.INVALID_PUBLIC_KEY);
    if (authTypeCombo.getSelectionModel().getSelectedItem().equals(AUTH_PUB_KEY_NAME)) {
      if (publicKeyText.getText().length() == 0) {
        validationErrors.add(ValidationErrors.INVALID_PUBLIC_KEY);
      } else {
        boolean isValidPk = false;
        // If this player has a public key wee need to make sure its valid
        for (String pk : CipherUtil.splitPublicKeys(publicKeyString)) {
          try {
            CipherUtil.fromPublicKeyString(pk);
            isValidPk = true;
          } catch (NoSuchAlgorithmException
              | InvalidKeySpecException
              | NoSuchPaddingException
              | InvalidKeyException
              | InvalidAlgorithmParameterException e) {
            isValidPk = false;
            break;
          }
        }
        if (!isValidPk) {
          validationErrors.add(ValidationErrors.INVALID_PUBLIC_KEY);
        }
      }
    }
    displayValidationErrors();
  }

  /** Updates the values in the database based on the form values. */
  private void updateDatabase() {
    Players players = new Players(MapTool.getClient().getPlayerDatabase());
    Role role =
        roleCombo.getSelectionModel().getSelectedItem().equals(GM_ROLE_NAME)
            ? Role.GM
            : Role.PLAYER;
    if (authTypeCombo.getSelectionModel().getSelectedItem().equals(AUTH_PUB_KEY_NAME)) {
      if (newPlayerMode) {
        players.addPlayerWithPublicKey(playerName, role, publicKeyString);
      } else {
        players.setRole(playerName, role);
        players.setPublicKeys(playerName, publicKeyString);
      }
    } else {
      if (newPlayerMode) {
        players.addPlayerWithPassword(playerName, role, password);
      } else {
        players.setRole(playerName, role);
        players.setPassword(playerName, password);
      }
    }

    if (blockedCheckBox.isSelected()) {
      players.blockPlayer(playerName, blockedReason);
    } else {
      players.unblockPlayer(playerName);
    }

    performClose();
  }

  /** Perform the actions required to close the form. */
  private void performClose() {
    eventHandlers.forEach(h -> h.close(this));
  }

  @Override
  public void close() {
    // Nothing to do
  }

  /** Validates the player name field. */
  private void validatePlayerName() {
    validationErrors.remove(ValidationErrors.NAME_MISSING);
    validationErrors.remove(ValidationErrors.PLAYER_EXISTS);
    if (playerNameText.getText().length() == 0) {
      validationErrors.add(ValidationErrors.NAME_MISSING);
    } else if (playerNameText.isEditable()) {
      try {
        PlayerInfo playerInfo =
            new Players(MapTool.getClient().getPlayerDatabase())
                .getPlayer(playerNameText.getText())
                .get();
        if (playerInfo != null) {
          validationErrors.add(ValidationErrors.PLAYER_EXISTS);
        }
      } catch (InterruptedException | ExecutionException e) {
        // Do nothing.
      }
    }
    displayValidationErrors();
  }

  /** Displays any validation errors. */
  private void displayValidationErrors() {
    String errorText =
        validationErrors.stream()
            .map(ValidationErrors::getDescription)
            .collect(Collectors.joining(", "));
    labelErrors.setText(errorText);
    okButton.setDisable(validationErrors.size() > 0);
  }

  /**
   * Sets the fields on the dialog from the player information. This method must be called from the
   * JavaFX thread.
   *
   * @param playerInfo the player information used to set the dialog fields.
   */
  private void setPlayerInfoJFX(PlayerInfo playerInfo) {
    playerNameText.setText(playerInfo.name());
    blockedReasonText.setText(playerInfo.blockedReason());
    if (playerInfo.authMethod() == AuthMethod.PASSWORD) {
      authTypeCombo.setValue(AUTH_PASSWORD_NAME);
      passwordText.setText(password);
    } else {
      authTypeCombo.setValue(AUTH_PUB_KEY_NAME);
      password = "";
      publicKeyString = String.join("\n", playerInfo.publicKeys());
    }
    blockedReason = Objects.requireNonNullElse(playerInfo.blockedReason(), "");

    blockedCheckBox.setSelected(!blockedReason.isEmpty());
    blockedReasonText.setText(blockedReason);

    if (playerInfo.role() == Role.GM) {
      roleCombo.setValue(GM_ROLE_NAME);
    } else {
      roleCombo.setValue(PLAYER_ROLE_NAME);
    }

    enableDisableFields();
  }

  /**
   * Enables and disables the fields on the dialog based on combo box values. This must be called
   * from the JavaFX thread.
   */
  private void enableDisableFields() {
    if (authTypeCombo.getSelectionModel().getSelectedItem().equals(AUTH_PUB_KEY_NAME)) {
      publicKeyText.setDisable(false);
      publicKeyText.setText(publicKeyString);
      generatePasswordButton.setDisable(true);
      passwordText.setDisable(true);

      // Need to take a copy as the update of field will clobber it
      String savedPassword = password;
      passwordText.setText("");
      password = savedPassword;
      validatePublicKey();
    } else {
      passwordText.setDisable(false);
      generatePasswordButton.setDisable(false);
      publicKeyText.setDisable(true);
      // Need to take a copy as the update of field will clobber it
      String savedPublicKey = publicKeyString;
      publicKeyText.setText("");
      publicKeyString = savedPublicKey;
      passwordText.setText(password);
      validatePassword();
    }

    if (blockedCheckBox.isSelected()) {
      blockedReasonText.setText(blockedReason);
      blockedReasonText.setEditable(true);
    } else {
      blockedReasonText.setText("");
      blockedReasonText.setEditable(false);
    }
  }

  /**
   * Sets the dialog fields based on the player info. It is safe to call this method from any
   * thread.
   *
   * @param playerInfo the player information to set the dialog fields to.
   */
  public void setPlayerInfo(PlayerInfo playerInfo) {
    if (Platform.isFxApplicationThread()) {
      setPlayerInfoJFX(playerInfo);
    } else {
      Platform.runLater(() -> setPlayerInfoJFX(playerInfo));
    }
  }

  /**
   * Sets the mode of the dialog, new player or edit player. This must be called from the JavaFX
   * thread.
   *
   * @param newPlayer {@code true} if dialog is to be displayed in new player mode, {@code false} if
   *     the dualog is to displayed in edit player mode.
   */
  public void setNewPlayerMode(boolean newPlayer) {
    newPlayerMode = newPlayer;
    playerNameText.setEditable(newPlayerMode);
    if (newPlayerMode) {
      password = "";
    }
  }
}
