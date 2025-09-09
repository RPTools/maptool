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

/** Sample Skeleton for 'PlayerDatabaseDialog.fxml' Controller Class */
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.Callback;
import javax.crypto.NoSuchPaddingException;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.javfx.AbstractSwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.PasswordDatabaseException;
import net.rptools.maptool.model.player.PersistedPlayerDatabase;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDBPropertyChange;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.model.player.PlayerInfo;
import net.rptools.maptool.model.player.Players;

public class PlayerDatabaseDialogController extends AbstractSwingJavaFXDialogController
    implements SwingJavaFXDialogController {

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="playersTable"
  private TableView<PlayerInfo> playersTable; // Value injected by FXMLLoader

  @FXML // fx:id="saveChangesButton"
  private Button saveChangesButton; // Value injected by FXMLLoader

  @FXML // fx:id="addButton"
  private Button addButton; // Value injected by FXMLLoader

  private final PropertyChangeListener changeListener =
      e -> {
        Platform.runLater(
            () -> {
              switch (e.getPropertyName()) {
                case PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_CHANGED -> {
                  removePlayer(e.getNewValue().toString());
                  addPlayer(e.getNewValue().toString());
                  playersTable.sort();
                }
                case PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_ADDED -> {
                  addPlayer(e.getNewValue().toString());
                  playersTable.sort();
                }
                case PlayerDBPropertyChange.PROPERTY_CHANGE_PLAYER_REMOVED -> {
                  removePlayer(e.getOldValue().toString());
                  playersTable.sort();
                }
                case PlayerDBPropertyChange.PROPERTY_CHANGE_DATABASE_CHANGED -> {
                  addPlayers();
                }
              }
            });
      };

  ObservableList<PlayerInfo> playerInfoList = FXCollections.observableArrayList();
  private PersistedPlayerDatabase playerDatabase;
  private Players players;

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert playersTable != null
        : "fx:id=\"playersTable\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert saveChangesButton != null
        : "fx:id=\"saveChangesButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert addButton != null
        : "fx:id=\"addButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
  }

  @Override
  public void init() {
    var currentDb = MapTool.getClient().getPlayerDatabase();
    if (!(currentDb instanceof PersistedPlayerDatabase persistedPlayerDatabase)) {
      throw new RuntimeException(
          "Player database dialog is only valid for persisted player databases");
    }
    playerDatabase = persistedPlayerDatabase;
    playerDatabase.addPropertyChangeListener(changeListener);
    players = new Players(currentDb);

    String gmI81n = I18N.getString("userTerm.GM");
    String playerI81n = I18N.getString("userTerm.Player");

    String passI81n = I18N.getString("playerDB.dialog.password");
    String pubKeyI81n = I18N.getString("playerDB.dialog.publicKey");

    playersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    var playerCol = new TableColumn<PlayerInfo, String>(I18N.getText("playerDB.dialog.player"));
    playerCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().name()));

    var roleCol = new TableColumn<PlayerInfo, String>(I18N.getText("playerDB.dialog.role"));
    roleCol.setCellValueFactory(
        p -> new ReadOnlyObjectWrapper<>(p.getValue().role() == Role.GM ? gmI81n : playerI81n));
    var authCol = new TableColumn<PlayerInfo, String>(I18N.getText("playerDB.dialog.authType"));
    authCol.setCellValueFactory(
        p ->
            new ReadOnlyObjectWrapper<>(
                p.getValue().authMethod() == AuthMethod.PASSWORD ? passI81n : pubKeyI81n));
    var disabledCol = new TableColumn<PlayerInfo, Boolean>(I18N.getText("playerDB.dialog.blocked"));
    disabledCol.setCellValueFactory(p -> new ReadOnlyBooleanWrapper(p.getValue().blocked()));
    disabledCol.setCellFactory(CheckBoxTableCell.<PlayerInfo>forTableColumn(disabledCol));
    var connectedCol =
        new TableColumn<PlayerInfo, Boolean>(I18N.getText("playerDB.dialog.connected"));
    connectedCol.setCellValueFactory(p -> new ReadOnlyBooleanWrapper(p.getValue().connected()));
    connectedCol.setCellFactory(CheckBoxTableCell.<PlayerInfo>forTableColumn(connectedCol));

    var editCol = new TableColumn<PlayerInfo, Void>();
    var editCellFactory =
        createButtonCellFactory(
            I18N.getText("playerDB.dialog.edit"),
            p -> {
              SwingUtilities.invokeLater(
                  () -> {
                    PlayerDatabaseEditDialog dialog =
                        PlayerDatabaseEditDialog.getEdtPlayerDialog(c -> c.setPlayerInfo(p));
                    dialog.show();
                  });
            });
    editCol.setCellFactory(editCellFactory);

    var deleteCol = new TableColumn<PlayerInfo, Void>();
    var deleteCellFactory =
        createButtonCellFactory(
            I18N.getText("playerDB.dialog.delete"),
            p -> {
              SwingUtilities.invokeLater(
                  () -> {
                    if (MapTool.confirm("playerDB.dialog.deleteConfirm", p.name())) {
                      Platform.runLater(() -> playerDatabase.deletePlayer(p.name()));
                    }
                  });
            });
    deleteCol.setCellFactory(deleteCellFactory);

    playersTable
        .getColumns()
        .addAll(playerCol, roleCol, authCol, disabledCol, connectedCol, editCol, deleteCol);

    playerCol.setComparator(String.CASE_INSENSITIVE_ORDER);

    playersTable.setItems(playerInfoList);
    playersTable.getSortOrder().add(playerCol);
    playersTable.sort();
    addPlayers();

    addButton.setOnAction(
        a -> {
          SwingUtilities.invokeLater(
              () -> {
                PlayerDatabaseEditDialog.getNewPlayerDialog(c -> {}).show();
              });
        });

    saveChangesButton.setOnAction(a -> performClose());
  }

  @Override
  public void close() {
    try {
      playerDatabase.commitChanges();
    } catch (NoSuchPaddingException
        | NoSuchAlgorithmException
        | InvalidKeySpecException
        | PasswordDatabaseException
        | InvalidKeyException e) {
      MapTool.showError("playerDB.dialog.error.savingChanges", e);
    }

    playerDatabase.removePropertyChangeListener(changeListener);
  }

  private void addPlayer(String name) {
    try {
      PlayerInfo playerInfo = players.getPlayer(name).get();
      playerInfoList.add(playerInfo);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private void removePlayer(String name) {
    playerInfoList.removeIf(p -> p.name().equals(name));
  }

  private void addPlayers() {
    players
        .getDatabasePlayers()
        .thenAccept(
            p ->
                Platform.runLater(
                    () -> {
                      playerInfoList.clear();
                      playerInfoList.addAll(
                          p.stream().filter(PlayerInfo::persistent).collect(Collectors.toList()));
                      playersTable.sort();
                    }));
  }

  private Callback<TableColumn<PlayerInfo, Void>, TableCell<PlayerInfo, Void>>
      createButtonCellFactory(String buttonText, Consumer<PlayerInfo> callback) {
    return new Callback<>() {
      @Override
      public TableCell<PlayerInfo, Void> call(final TableColumn<PlayerInfo, Void> param) {
        return new TableCell<>() {

          private final Button btn = new Button(buttonText);

          {
            btn.setOnAction(
                (event) -> {
                  PlayerInfo pi = getTableView().getItems().get(getIndex());
                  callback.accept(pi);
                });
          }

          @Override
          public void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
              setGraphic(null);
            } else {
              setGraphic(btn);
            }
          }
        };
      }
      ;
    };
  }
}
