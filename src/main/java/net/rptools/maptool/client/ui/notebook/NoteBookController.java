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
package net.rptools.maptool.client.ui.notebook;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.AssetAvailableListener;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.notebook.entry.NoteBookEntry;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookEntryTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookGroupTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookZoneTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.TableTreeItemHolder;

public class NoteBookController {


  @FXML private BorderPane noteBookPanel;

  @FXML private TreeTableView<TableTreeItemHolder> noteBookTreeTableView;

  @FXML private TreeTableColumn<TableTreeItemHolder, String> groupColumn;

  @FXML private Button addNoteButton;

  @FXML private StackPane mainViewStackPane;

  @FXML private AnchorPane notePane;

  @FXML private WebView noteWebView;

  @FXML private AnchorPane editorPane;

  @FXML private AnchorPane detailsAnchorPane;


  @FXML private TextField nameTextField;

  @FXML private ComboBox<Zone> mapComboBox;

  @FXML private CheckBox mapCheckBox;

  @FXML private TextField referenceTextField;

  @FXML private Button editButton;

  private NoteBookEntry entryShowing;

  @FXML
  void initialize() {
    assert noteBookPanel != null
        : "fx:id=\"noteBookPanel\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert noteBookTreeTableView != null
        : "fx:id=\"noteBookTreeTableView\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert groupColumn != null
        : "fx:id=\"groupColumn\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert addNoteButton != null
        : "fx:id=\"addNoteButton\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert mainViewStackPane != null
        : "fx:id=\"mainViewStackPane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert notePane != null
        : "fx:id=\"notePane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert noteWebView != null
        : "fx:id=\"noteWebView\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert editorPane != null
        : "fx:id=\"editorPane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert detailsAnchorPane != null
        : "fx:id=\"detailsAnchorPane\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert nameTextField != null
        : "fx:id=\"nameTextField\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert mapComboBox != null
        : "fx:id=\"mapComboBox\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert mapCheckBox != null
        : "fx:id=\"mapCheckBox\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert referenceTextField != null
        : "fx:id=\"referenceTextField\" was not injected: check your FXML file 'NoteBook.fxml'.";
    assert editButton != null
        : "fx:id=\"editButton\" was not injected: check your FXML file 'NoteBook.fxml'.";

    groupColumn.setCellValueFactory(
        cellDataFeatures -> {
          TableTreeItemHolder holder = cellDataFeatures.getValue().getValue();
          if (holder instanceof NoteBookGroupTreeItem) {
            var group = (NoteBookGroupTreeItem) holder;
            return new SimpleStringProperty(group.getName());
          } else if (holder instanceof NoteBookZoneTreeItem) {
            var zoneHolder = (NoteBookZoneTreeItem) holder;
            Zone zone = MapTool.getCampaign().getZone(zoneHolder.getId());
            return new SimpleStringProperty(zone.getName());
          } else if (holder instanceof NoteBookEntryTreeItem) {
            var entry = (NoteBookEntryTreeItem) holder;
            return new SimpleStringProperty(entry.getEntry().getName());
          }
          return new SimpleStringProperty("");
        });

    noteBookTreeTableView.setEditable(false);
    noteBookTreeTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    noteBookTreeTableView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSel, newSel) -> {
              if (newSel != null && newSel.getValue() instanceof NoteBookEntryTreeItem) {
                showEntry(((NoteBookEntryTreeItem) newSel.getValue()).getEntry());
              } else {
                showEntry(null);
              }
            });
  }

  private void showEntry(NoteBookEntry entry) {
    entryShowing = entry;

    if (entry == null) {
      noteWebView.getEngine().loadContent("");
      nameTextField.clear();
      mapCheckBox.setSelected(false);
      referenceTextField.clear();
    } else {
      final AssetAvailableListener aal =
          k -> {
            Platform.runLater(
                () -> {
                  noteWebView.getEngine().loadContent(AssetManager.getAsset(k).toString());
                });
          };

      /* TODO: CDW
      if (entry.getNotesKey().isPresent()) {
        AssetManager.getAssetAsynchronously(
            entry.getNotesKey().get(),
            (key) -> {
              String note = AssetManager.getAsset(key).getDataAsString();
              Platform.runLater(() -> noteWebView.getEngine().loadContent(note));
            });
      } else { */
        noteWebView.getEngine().loadContent("");
      // TODO: CDW }

      nameTextField.setText(entry.getName());
      Zone currentZone;
      if (entry.getZoneId().isPresent()) {
        currentZone = MapTool.getCampaign().getZone(entry.getZoneId().get());
        mapCheckBox.setSelected(true);
      } else {
        currentZone = null;
        mapCheckBox.setSelected(false);
      }
      loadMapComboBox(currentZone);
      referenceTextField.clear(); // TODO: CDW
    }
    nameTextField.setEditable(false);
    mapComboBox.setDisable(true);
    mapCheckBox.setDisable(true);
    referenceTextField.setEditable(false);
  }

  private void loadMapComboBox(Zone defaultZone) {
    mapComboBox.getItems().clear();
    for (Zone zone : MapTool.getCampaign().getZones()) {
      mapComboBox.getItems().add(zone);
    }
    mapComboBox.setValue(defaultZone);
  }

  void setTreeRoot(TreeItem<TableTreeItemHolder> root) {
    noteBookTreeTableView.setRoot(root);
    noteBookTreeTableView.refresh();
  }



  @FXML
  void addNoteAction(ActionEvent event) {}
}
