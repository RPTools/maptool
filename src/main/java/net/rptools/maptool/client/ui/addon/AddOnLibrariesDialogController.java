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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppActions.MapPreviewFileChooser;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.ViewAssetDialog;
import net.rptools.maptool.client.ui.javfx.AbstractSwingJavaFXDialogController;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialogController;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.AddOnsAddedEvent;
import net.rptools.maptool.model.library.AddOnsRemovedEvent;
import net.rptools.maptool.model.library.Library;
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

  @FXML // fx:id="labelName"
  private Label labelName; // Value injected by FXMLLoader

  @FXML // fx:id="labelVersion"
  private Label labelVersion; // Value injected by FXMLLoader

  @FXML // fx:id="labelNamespace"
  private Label labelNamespace; // Value injected by FXMLLoader

  @FXML // fx:id="labelShortDescription"
  private Label labelShortDescription; // Value injected by FXMLLoader

  @FXML // fx:id="linkWebsite"
  private Hyperlink linkWebsite; // Value injected by FXMLLoader

  @FXML // fx:id="linkGitURL"
  private Hyperlink linkGitURL; // Value injected by FXMLLoader

  @FXML // fx:id="labelAuthors"
  private Label labelAuthors; // Value injected by FXMLLoader

  @FXML // fx:id="labelLicense"
  private Label labelLicense; // Value injected by FXMLLoader

  @FXML // fx:id="textAreaDescription"
  private TextArea textAreaDescription; // Value injected by FXMLLoader

  @FXML private Button buttonViewLicenceFile;

  @FXML private Button buttonViewReadMeFile;

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
    assert labelName != null
        : "fx:id=\"labelName\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert labelVersion != null
        : "fx:id=\"labelVersion\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert labelNamespace != null
        : "fx:id=\"labelNamespace\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert labelShortDescription != null
        : "fx:id=\"labelShortDescription\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert linkWebsite != null
        : "fx:id=\"linkWebsite\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert linkGitURL != null
        : "fx:id=\"linkGitURL\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert labelAuthors != null
        : "fx:id=\"labelAuthors\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert labelLicense != null
        : "fx:id=\"labelLicense\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert textAreaDescription != null
        : "fx:id=\"textAreaDescription\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert buttonViewLicenceFile != null
        : "fx:id=\"buttonViewLicenceFile\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
    assert buttonViewReadMeFile != null
        : "fx:id=\"buttonViewReadMeFile\" was not injected: check your FXML file 'AddOnLibrariesDialog.fxml'.";
  }

  @Override
  public void init() {
    addOnsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    var nameCol = new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog.addon.name"));
    nameCol.setCellValueFactory(lib -> new ReadOnlyObjectWrapper<>(lib.getValue().name()));
    nameCol.prefWidthProperty().bind(addOnsTable.widthProperty().divide(4)); // w * 1/4
    var versionCol =
        new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog.addon.version"));
    versionCol.setCellValueFactory(lib -> new ReadOnlyObjectWrapper<>(lib.getValue().version()));
    versionCol.prefWidthProperty().bind(addOnsTable.widthProperty().divide(4)); // w * 1/4
    var namespaceCol =
        new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog.addon.namespace"));
    namespaceCol.setCellValueFactory(
        lib -> new ReadOnlyObjectWrapper<>(lib.getValue().namespace()));
    namespaceCol.prefWidthProperty().bind(addOnsTable.widthProperty().divide(4)); // w * 1/4
    var shortDescCol =
        new TableColumn<LibraryInfo, String>(I18N.getText("library.dialog.addon.shortDescription"));
    shortDescCol.setCellValueFactory(
        lib -> new ReadOnlyObjectWrapper<>(lib.getValue().shortDescription()));
    shortDescCol.prefWidthProperty().bind(addOnsTable.widthProperty().divide(4)); // w * 1/4

    addOnsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    addOnsTable
        .getSelectionModel()
        .getSelectedItems()
        .addListener(
            (ListChangeListener<LibraryInfo>)
                c -> {
                  if (c.getList().size() > 0) {
                    removeLibButton.setDisable(false);
                    showDetails(c.getList().get(0));
                  } else {
                    removeLibButton.setDisable(true);
                    clearDetails();
                  }
                });
    addOnsTable.getColumns().addAll(nameCol, versionCol, namespaceCol, shortDescCol);

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

    clearDetails();
    addOnsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    try {
      addOnList.addAll(new LibraryManager().getLibraries(LibraryType.ADD_ON));
    } catch (ExecutionException | InterruptedException e) {
      log.error("Error displaying add-on libraries", e);
    }
    new MapToolEventBus().getMainEventBus().register(this);
    addOnsTable.setItems(addOnList);

    addButton.setOnAction(a -> addAddOnLibrary());

    linkWebsite.setOnAction(
        a -> {
          var link = linkWebsite.getText();
          if (link != null && !link.trim().isEmpty()) {
            SwingUtilities.invokeLater(
                () -> {
                  String url;
                  if (link.startsWith("http://") || link.startsWith("https://")) {
                    url = link;
                  } else {
                    url = "https://" + link;
                  }
                  MapTool.showDocument(url);
                });
          }
        });

    linkGitURL.setOnMouseClicked(
        e -> {
          var link = linkGitURL.getText();
          if (link != null && !link.trim().isEmpty()) {
            SwingUtilities.invokeLater(
                () -> {
                  String url;
                  if (link.startsWith("http://") || link.startsWith("https://")) {
                    url = link;
                  } else {
                    url = "https://" + link;
                  }
                  MapTool.showDocument(url);
                });
          }
        });

    buttonViewLicenceFile.setOnAction(
        a -> {
          var lib = addOnsTable.getSelectionModel().getSelectedItems().get(0);
          viewLicenseFile(lib);
        });
    buttonViewReadMeFile.setOnAction(
        a -> {
          var lib = addOnsTable.getSelectionModel().getSelectedItems().get(0);
          viewReadMeFile(lib);
        });
  }

  private void viewLicenseFile(LibraryInfo libInfo) {
    Optional<Library> lib = new LibraryManager().getLibrary(libInfo.namespace());
    if (lib.isPresent()) {
      lib.get()
          .getLicenseAsset()
          .thenAccept(
              a ->
                  a.ifPresent(
                      asset ->
                          SwingUtilities.invokeLater(
                              () -> new ViewAssetDialog(asset, "License", 640, 480).showModal())));
    }
  }

  private void viewReadMeFile(LibraryInfo libInfo) {
    Optional<Library> lib = new LibraryManager().getLibrary(libInfo.namespace());
    if (lib.isPresent()) {
      lib.get()
          .getReadMeAsset()
          .thenAccept(
              a ->
                  a.ifPresent(
                      asset ->
                          SwingUtilities.invokeLater(
                              () -> new ViewAssetDialog(asset, "License", 640, 480).showModal())));
    }
  }

  /** Clears the details of the library in the dialog. */
  private void clearDetails() {
    labelName.setText("");
    labelVersion.setText("");
    labelNamespace.setText("");
    labelShortDescription.setText("");
    labelAuthors.setText("");
    linkGitURL.setText("");
    linkWebsite.setText("");
    labelLicense.setText("");
    textAreaDescription.setText("");
    buttonViewLicenceFile.setDisable(true);
    buttonViewReadMeFile.setDisable(true);
  }

  /**
   * Updates the details of the library in the dialog.
   *
   * @param libraryInfo the library to display
   */
  private void showDetails(LibraryInfo libraryInfo) {
    labelName.setText(libraryInfo.name());
    labelVersion.setText(libraryInfo.version());
    labelNamespace.setText(libraryInfo.namespace());
    labelShortDescription.setText(libraryInfo.shortDescription());
    labelAuthors.setText(String.join(", ", libraryInfo.authors()));
    linkGitURL.setText(libraryInfo.gitUrl());
    linkWebsite.setText(libraryInfo.website());
    labelLicense.setText(libraryInfo.license());
    textAreaDescription.setText(libraryInfo.description());
    buttonViewLicenceFile.setDisable(
        libraryInfo.licenseFile() == null || libraryInfo.licenseFile().isEmpty());
    buttonViewReadMeFile.setDisable(
        libraryInfo.readMeFile() == null || libraryInfo.readMeFile().isEmpty());
  }

  /** Handle adding of add-on library. */
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
    Platform.runLater(
        () -> {
          addOnList.clear();
          new MapToolEventBus().getMainEventBus().unregister(this);
        });
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
}
