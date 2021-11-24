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
package net.rptools.maptool.client.ui.addon;

import com.google.common.eventbus.Subscribe;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.javfx.AbstractSwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.AddOnsAddedEvent;
import net.rptools.maptool.model.library.AddOnsRemovedEvent;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.LibraryType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Controller for the add-on libraries dialog. */
public class AddOnLibrariesDialogController extends AbstractSwingJavaFXDialogController
    implements SwingJavaFXDialogController {
  private static final Logger log = LogManager.getLogger(AddOnLibrariesDialogController.class);

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="addButton"
  private Button addButton; // Value injected by FXMLLoader

  @FXML // fx:id="addOnsTable"
  private TableView<LibraryInfo> addOnsTable; // Value injected by FXMLLoader

  @FXML // fx:id="closeButton"
  private Button closeButton; // Value injected by FXMLLoader

  private final ObservableList<LibraryInfo> addOnList = FXCollections.observableArrayList();

  @FXML
  // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert addButton != null
        : "fx:id=\"addButton\" was not injected: check your FXML file 'AddOnLibraryDialog.fxml'.";
    assert addOnsTable != null
        : "fx:id=\"addOnsTable\" was not injected: check your FXML file 'AddOnLibraryDialog.fxml'.";
    assert closeButton != null
        : "fx:id=\"closeButton\" was not injected: check your FXML file 'AddOnLibraryDialog.fxml'.";
  }

  @Override
  public void init() {
    addOnsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    var nameCol =
        new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog.table" + ".name"));
    nameCol.setCellValueFactory(lib -> new ReadOnlyObjectWrapper<>(lib.getValue().name()));
    var versionCol =
        new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog.table" + ".version"));
    versionCol.setCellValueFactory(lib -> new ReadOnlyObjectWrapper<>(lib.getValue().version()));
    var namespaceCol =
        new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog" + ".table.namespace"));
    namespaceCol.setCellValueFactory(
        lib -> new ReadOnlyObjectWrapper<>(lib.getValue().namespace()));

    var deleteCol = new TableColumn<LibraryInfo, Void>();
    var deleteCellFactory =
        createButtonCellFactory(
            I18N.getText("library.dialog.button.delete"),
            lib -> {
              SwingUtilities.invokeLater(
                  () -> {
                    if (MapTool.confirm("library.dialog.delete.confirm", lib.name())) {
                      new LibraryManager().removeAddOnLibrary(lib.namespace());
                    }
                  });
            });
    deleteCol.setCellFactory(deleteCellFactory);

    var deleteDataCol = new TableColumn<LibraryInfo, Void>();
    var deleteDataCellFactory =
        createButtonCellFactory(
            I18N.getText("library.dialog.button.deleteData"),
            lib -> {
              SwingUtilities.invokeLater(
                  () -> {
                    if (MapTool.confirm("library.dialog.deleteData.confirm", lib.name())) {
                      new LibraryManager().removeAddOnLibrary(lib.namespace());
                    }
                  });
            });
    deleteDataCol.setCellFactory(deleteDataCellFactory);

    addOnsTable.getColumns().addAll(nameCol, versionCol, namespaceCol, deleteCol, deleteDataCol);

    try {
      addOnList.addAll(new LibraryManager().getLibraries(LibraryType.ADD_ON));
    } catch (ExecutionException | InterruptedException e) {
      log.error("Error loading add-on libraries", e);
    }
    new MapToolEventBus().getMainEventBus().register(this);
    addOnsTable.setItems(addOnList);
  }

  @Override
  public void close() {
    addOnList.clear();
    new MapToolEventBus().getMainEventBus().unregister(this);
  }

  @Subscribe
  void addOnAdded(AddOnsAddedEvent event) {
    Platform.runLater(
        () -> {
          for (var addOn : event.addOns()) {
            if (!addOnList.contains(addOn)) {
              addOnList.add(addOn);
            }
          }
          ;
        });
  }

  @Subscribe
  void removedAddOn(AddOnsRemovedEvent event) {
    Platform.runLater(
        () -> {
          for (var addOn : event.addOns()) {
            addOnList.remove(addOn);
          }
        });
  }

  private Callback<TableColumn<LibraryInfo, Void>, TableCell<LibraryInfo, Void>>
      createButtonCellFactory(String buttonText, Consumer<LibraryInfo> callback) {
    return new Callback<>() {
      @Override
      public TableCell<LibraryInfo, Void> call(final TableColumn<LibraryInfo, Void> param) {
        return new TableCell<>() {

          private final Button btn = new Button(buttonText);

          {
            btn.setOnAction(
                (event) -> {
                  LibraryInfo lib = getTableView().getItems().get(getIndex());
                  callback.accept(lib);
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
    };
  }
}
