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
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppActions.MapPreviewFileChooser;
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
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
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

  @FXML // fx:id="removeLibButton"
  private Button removeLibButton; // Value injected by FXMLLoader

  private final ObservableList<LibraryInfo> addOnList = FXCollections.observableArrayList();

  @FXML
  // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert addButton != null
        : "fx:id=\"addButton\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert addOnsTable != null
        : "fx:id=\"addOnsTable\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert closeButton != null
        : "fx:id=\"closeButton\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert removeLibButton != null
        : "fx:id=\"removeLibButton\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
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

    addOnsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    addOnsTable
        .getSelectionModel()
        .getSelectedItems()
        .addListener(
            (ListChangeListener<LibraryInfo>)
                c -> {
                  if (c.getList().size() > 0) {
                    removeLibButton.setDisable(false);
                  } else {
                    removeLibButton.setDisable(true);
                  }
                });
    addOnsTable.getColumns().addAll(nameCol, versionCol, namespaceCol);

    removeLibButton.setOnAction(
        a -> {
          LibraryInfo lib = addOnsTable.getSelectionModel().getSelectedItems().get(0);
          SwingUtilities.invokeLater(
              () -> {
                if (MapTool.confirm("library.dialog.delete.confirm", lib.name())) {
                  new LibraryManager().removeAddOnLibrary(lib.namespace());
                }
              });
        });
    removeLibButton.setDisable(true);

    closeButton.setOnAction(a -> performClose());

    try {
      addOnList.addAll(new LibraryManager().getLibraries(LibraryType.ADD_ON));
    } catch (ExecutionException | InterruptedException e) {
      log.error("Error displaying add-on libraries", e);
    }
    new MapToolEventBus().getMainEventBus().register(this);
    addOnsTable.setItems(addOnList);

    addButton.setOnAction(a -> addAddOnLibrary());
  }

  private void addAddOnLibrary() {
    SwingUtilities.invokeLater(
        () -> {
          JFileChooser chooser = new MapPreviewFileChooser();
          chooser.setDialogTitle(I18N.getText("library.dialog.import.title"));
          chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
          chooser.setFileFilter(AddOnLibraryImporter.getAddOnLibraryFileFilter());

          if (chooser.showOpenDialog(MapTool.getFrame()) == JFileChooser.APPROVE_OPTION) {
            File libFile = chooser.getSelectedFile();
            try {
              var addOnLibrary = new AddOnLibraryImporter().importFromFile(libFile);
              var libraryManager = new LibraryManager();
              String namespace = addOnLibrary.getNamespace().get();
              if (libraryManager.addOnLibraryExists(addOnLibrary.getNamespace().get())) {
                if (!MapTool.confirm(I18N.getText("library.error.addOnLibraryExists", namespace))) {
                  return;
                }
                libraryManager.deregisterAddOnLibrary(namespace);
              }
              libraryManager.reregisterAddOnLibrary(addOnLibrary);
            } catch (IOException | InterruptedException | ExecutionException e) {
              MapTool.showError("library.import.ioError", e);
            }
          }
        });
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
