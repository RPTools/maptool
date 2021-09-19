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
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogEventHandler;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player.Role;
import net.rptools.maptool.model.player.PlayerDatabase.AuthMethod;
import net.rptools.maptool.model.player.PlayerInfo;
import net.rptools.maptool.model.player.Players;

public class PlayerDatabaseDialogController implements SwingJavaFXDialogController {

  private final Set<SwingJavaFXDialogEventHandler> eventHandlers =
      ConcurrentHashMap.newKeySet();

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="playersTable"
  private TableView<PlayerInfo> playersTable; // Value injected by FXMLLoader

  @FXML // fx:id="cancelButton"
  private Button cancelButton; // Value injected by FXMLLoader

  @FXML // fx:id="saveChangesButton"
  private Button saveChangesButton; // Value injected by FXMLLoader

  @FXML // fx:id="addButton"
  private Button addButton; // Value injected by FXMLLoader

  private final PropertyChangeListener changeListener = e -> {
    Platform.runLater(() -> {
      switch (e.getPropertyName()) {
        case Players.PROPERTY_CHANGE_PLAYER_CHANGED -> {
          removePlayer((PlayerInfo) e.getOldValue());
          addPlayer((PlayerInfo) e.getNewValue());
        }
        case Players.PROPERTY_CHANGE_PLAYER_ADDED -> {
          addPlayer((PlayerInfo) e.getNewValue());
        }
        case Players.PROPERTY_CHANGE_PLAYER_REMOVE -> {
          removePlayer((PlayerInfo) e.getOldValue());
        }
        case Players.PROPERTY_CHANGE_DATABASE_CHANGED -> {
          addPlayers();
        }
      }
    });
  };

  ObservableList<PlayerInfo> playerInfoList = FXCollections.observableArrayList();

  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert playersTable != null
        : "fx:id=\"playersTable\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert cancelButton != null
        : "fx:id=\"cancelButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert saveChangesButton != null
        : "fx:id=\"saveChangesButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
    assert addButton != null
        : "fx:id=\"addButton\" was not injected: check your FXML file 'PlayerDatabaseDialog.fxml'.";
  }

  @Override
  public void registerEventHandler(SwingJavaFXDialogEventHandler handler) {
    eventHandlers.add(handler);
  }

  @Override
  public void deregisterEventHandler(SwingJavaFXDialogEventHandler handler) {
    eventHandlers.remove(handler);
    Players.removePropertyChangeListener(changeListener);
  }

  @Override
  public void init() {

    Players.addPropertyChangeListener(changeListener);

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
    var disabledCol =
        new TableColumn<PlayerInfo, Boolean>(I18N.getText("playerDB.dialog.disabled"));
    disabledCol.setCellValueFactory(p -> new ReadOnlyBooleanWrapper(p.getValue().blocked()));
    disabledCol.setCellFactory(CheckBoxTableCell.<PlayerInfo>forTableColumn(disabledCol));
    var reasonCol =
        new TableColumn<PlayerInfo, String>(I18N.getText("playerDB.dialog.disabledReason"));
    reasonCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().blockedReason()));
    var connectedCol =
        new TableColumn<PlayerInfo, Boolean>(I18N.getText("playerDB.dialog.connected"));
    connectedCol.setCellValueFactory(p -> new ReadOnlyBooleanWrapper(p.getValue().connected()));
    connectedCol.setCellFactory(CheckBoxTableCell.<PlayerInfo>forTableColumn(connectedCol));

    var editCol = new TableColumn<PlayerInfo, Void>();
    var editCallFactory = new Callback<TableColumn<PlayerInfo, Void>,
        TableCell<PlayerInfo, Void>>() {

      private final Button editButton = new Button("...");
      @Override
      public TableCell<PlayerInfo, Void> call(TableColumn<PlayerInfo, Void> param) {
        return null;
      }
    };
    editCol.setCellFactory(editCallFactory);

    playersTable
        .getColumns()
        .addAll(playerCol, roleCol, authCol, disabledCol, reasonCol, connectedCol);

    playersTable.setItems(playerInfoList);
    addPlayers();
  }

  private void addPlayer(PlayerInfo playerInfo) {
    playerInfoList.add(playerInfo);
  }

  private void removePlayer(PlayerInfo playerInfo) {
    playerInfoList.remove(playerInfo);
  }

  private void addPlayers() {
    new Players()
        .getDatabasePlayers()
        .thenAccept(p -> Platform.runLater(() -> {
          playerInfoList.clear();
          playerInfoList.addAll(p.stream().filter(
              PlayerInfo::persistent).collect(Collectors.toList()));
        }));
  }

}
