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
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapTool.CampaignEvent;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookEntryTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookGroupTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookTableTreeModel;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookZoneTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.TableTreeItemHolder;

/** Panel used for displaying */
public class NoteBookPanel extends JFXPanel {

  private EditNoteDialog editNoteDialog;

  private final TreeTableView<TableTreeItemHolder> bookmarkTable = new TreeTableView<>();
  private NoteBookTableTreeModel noteBookTableTreeModel;

  public static NoteBookPanel createMapBookmarkPanel() {
    NoteBookPanel panel = new NoteBookPanel();
    panel.setVisible(true);

    Platform.runLater(panel::initFX);

    MapTool.getEventDispatcher()
        .addListener(
            e -> panel.campaignChanged((Campaign) e.getOldValue(), (Campaign) e.getNewValue()),
            CampaignEvent.Changed);
    return panel;
  }

  private NoteBookPanel() {}

  private void initFX() {
    editNoteDialog = new EditNoteDialog();
    bookmarkTable.setEditable(false);
    TreeTableColumn<TableTreeItemHolder, String> firstColumn = new TreeTableColumn<>("Type");
    firstColumn.setCellValueFactory(
        cellDataFeatures -> {
          TableTreeItemHolder holder = cellDataFeatures.getValue().getValue();
          if (holder instanceof NoteBookGroupTreeItem) {
            var group = (NoteBookGroupTreeItem) holder;
            return new SimpleStringProperty(group.getName());
          } else if (holder instanceof NoteBookZoneTreeItem) {
            var zoneHolder = (NoteBookZoneTreeItem) holder;
            Zone zone = MapTool.getCampaign().getZone(zoneHolder.getId());
            return new SimpleStringProperty(zone.getName());
          }
          return new SimpleStringProperty("");
        });

    TreeTableColumn<TableTreeItemHolder, String> secondColumn = new TreeTableColumn<>("Name");
    secondColumn.setCellValueFactory(
        cellDataFeatures -> {
          TableTreeItemHolder holder = cellDataFeatures.getValue().getValue();
          if (holder instanceof NoteBookEntryTreeItem) {
            var entry = (NoteBookEntryTreeItem) holder;
            return new SimpleStringProperty(entry.getEntry().getName());
          }
          return new SimpleStringProperty("");
        });

    TreeTableColumn<TableTreeItemHolder, String> thirdColumn = new TreeTableColumn<>("Reference");
    thirdColumn.setCellValueFactory(
        cellDataFeatures -> {
          TableTreeItemHolder holder = cellDataFeatures.getValue().getValue();
          if (holder instanceof NoteBookEntryTreeItem) {
            var entry = (NoteBookEntryTreeItem) holder;
            if (entry.getEntry().getReference().isPresent()) {
              return new SimpleStringProperty(entry.getEntry().getReference().get());
            }
          }
          return new SimpleStringProperty("");
        });

    bookmarkTable.getColumns().add(firstColumn);
    bookmarkTable.getColumns().add(secondColumn);
    bookmarkTable.getColumns().add(thirdColumn);

    bookmarkTable.setShowRoot(false);

    VBox vBox = new VBox();
    Scene scene = new Scene(vBox);
    Button addNote = new Button("Add Note");
    Button addView = new Button("Add View");
    Button addMarker = new Button("Add Marker");
    addNote.setOnAction(a -> editNoteDialog.editNew());
    vBox.setSpacing(5);
    vBox.setPadding(new Insets(10, 0, 0, 10));
    HBox buttonsHBox = new HBox();
    buttonsHBox.getChildren().addAll(addNote, addView, addMarker);
    vBox.getChildren().addAll(bookmarkTable, buttonsHBox);
    setScene(scene);

    editNoteDialog.init();
  }

  private void campaignChanged(Campaign oldCampaign, Campaign newCampaign) {
    NoteBookTableTreeModel oldNoteBookTableTreeModel = noteBookTableTreeModel;

    if (newCampaign != null) {
      Platform.runLater(
          () -> {
            noteBookTableTreeModel =
                NoteBookTableTreeModel.getTreeModelFor(newCampaign.getNotebook());
            bookmarkTable.setRoot(noteBookTableTreeModel.getRoot());
          });
    }

    if (oldNoteBookTableTreeModel != null) {
      oldNoteBookTableTreeModel.dispose();
    }
  }
}
