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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapTool.CampaignEvent;
import net.rptools.maptool.client.ui.javfx.SwingJavaFXDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.notebook.Note;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookEntryTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookGroupTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookTableTreeModel;
import net.rptools.maptool.model.notebook.tabletreemodel.NoteBookZoneTreeItem;
import net.rptools.maptool.model.notebook.tabletreemodel.TableTreeItemHolder;

/**
 * Panel used for displaying the UI listing all of the note book entries for the campaign.
 *
 * @note Several of the methods in this class have restrictions on which threads they can be called
 *     on. Each of the methods details any restrictions.
 */
public class NoteBookPanel extends JFXPanel {

  /** The note that is currently being edited, or {@code null} if there is no note being edited. */
  private EditNotePanel editNoteDialog;

  /**
   * The {@link TreeTableView} used to display all the {@link
   * net.rptools.maptool.model.notebook.NoteBookEntry}s for the campaign.
   */
  private final TreeTableView<TableTreeItemHolder> notebookTable = new TreeTableView<>();

  /**
   * The {@link NoteBookTableTreeModel} with all the {@link
   * net.rptools.maptool.model.notebook.NoteBookEntry}s for the campaign.
   */
  private NoteBookTableTreeModel noteBookTableTreeModel;

  /** The dialog used to show the {@link EditNotePanel} for editing / creating {@link Note}s. */
  private SwingJavaFXDialog editDialog;

  /** The button used to edit {@link net.rptools.maptool.model.notebook.NoteBookEntry}s. */
  private final Button editButton = new Button(I18N.getText("panel.NoteBook.button.edit"));

  /**
   * Returns an instance of {@code NoteBookPanel}.
   *
   * @return an instance of {@code NoteBookPanel}.
   * @throws IllegalStateException if not run on the Swing EDT thread.
   * @note This method can only be run on the Swing EDT thread.
   */
  public static NoteBookPanel createMapBookmarkPanel() {

    if (!SwingUtilities.isEventDispatchThread()) {
      throw new IllegalStateException(
          "NoteBookPanel.createMapBookmarkPanel() can only be called from Swing EDT thread.");
    }

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

  /**
   * Initialized the JavaFX controls for this panel.
   *
   * @note This method can only be run on the JavaFX Platform thread.
   */
  private void initFX() {
    assert SwingUtilities.isEventDispatchThread()
        : "NoteBookPanel.createMapBookmarkPanel() can only be called from Swing EDT thread.";

    editNoteDialog = new EditNotePanel();
    notebookTable.setEditable(false);
    notebookTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    notebookTable
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSel, newSel) -> {
              if (newSel != null && newSel.getValue() instanceof NoteBookEntryTreeItem) {
                editButton.setDisable(false);
              } else {
                editButton.setDisable(true);
              }
            });
    TreeTableColumn<TableTreeItemHolder, String> firstColumn = new TreeTableColumn<>("");
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

    TreeTableColumn<TableTreeItemHolder, String> secondColumn =
        new TreeTableColumn<>(I18N.getText("panel.NoteBook.nameColumn"));
    secondColumn.setCellValueFactory(
        cellDataFeatures -> {
          TableTreeItemHolder holder = cellDataFeatures.getValue().getValue();
          if (holder instanceof NoteBookEntryTreeItem) {
            var entry = (NoteBookEntryTreeItem) holder;
            return new SimpleStringProperty(entry.getEntry().getName());
          }
          return new SimpleStringProperty("");
        });

    TreeTableColumn<TableTreeItemHolder, String> thirdColumn =
        new TreeTableColumn<>(I18N.getText("panel.NoteBook.referenceColumn"));
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

    notebookTable.getColumns().add(firstColumn);
    notebookTable.getColumns().add(secondColumn);
    notebookTable.getColumns().add(thirdColumn);

    notebookTable.setShowRoot(false);

    VBox vBox = new VBox();
    Scene scene = new Scene(vBox);
    Button addNote = new Button(I18N.getText("panel.NoteBook.button.addNote"));
    Button addView = new Button(I18N.getText("panel.NoteBook.button.addView"));
    Button addMarker = new Button(I18N.getText("panel.NoteBook.button.addMapMarker"));
    addNote.setOnAction(
        a -> {
          if (!editDialog.isShowing()) {
            editNoteDialog.editNew();
            SwingUtilities.invokeLater(() -> editDialog.showDialog());
          }
        });
    editButton.setDisable(true);
    editButton.setOnAction(
        a -> {
          TableTreeItemHolder holder =
              notebookTable.getSelectionModel().getSelectedItem().getValue();
          if (holder instanceof NoteBookEntryTreeItem) {
            if (!editDialog.isShowing()) {
              var item = (NoteBookEntryTreeItem) holder;
              if (item.getEntry() instanceof Note) {
                editNoteDialog.edit((Note) item.getEntry());
                SwingUtilities.invokeLater(() -> editDialog.showDialog());
              }
            }
          }
        });
    vBox.setSpacing(5);
    vBox.setPadding(new Insets(10, 0, 0, 10));
    HBox buttonsHBox = new HBox();
    buttonsHBox.getChildren().addAll(addNote, addView, addMarker, editButton);
    vBox.getChildren().addAll(notebookTable, buttonsHBox);
    setScene(scene);

    JFXPanel jfxPanel = new JFXPanel();
    editNoteDialog.init(jfxPanel, () -> editDialog.closeDialog());

    SwingUtilities.invokeLater(
        () -> {
          editDialog =
              new SwingJavaFXDialog("noteBook.editNote.title", MapTool.getFrame(), jfxPanel, false);
        });
  }

  /**
   * Method called when the {@link Campaign} is changed.
   *
   * @param oldCampaign The previous {@link Campaign}.
   * @param newCampaign The new {@link Campaign}.
   * @note This method can safely be called from any thread.
   */
  private void campaignChanged(Campaign oldCampaign, Campaign newCampaign) {

    if (newCampaign != null) {
      Platform.runLater(
          () -> {
            NoteBookTableTreeModel oldNoteBookTableTreeModel = noteBookTableTreeModel;

            noteBookTableTreeModel =
                NoteBookTableTreeModel.getTreeModelFor(newCampaign.getNotebook());
            notebookTable.setRoot(noteBookTableTreeModel.getRoot());
            notebookTable.refresh();

            /*if (oldNoteBookTableTreeModel != null) {
              oldNoteBookTableTreeModel.dispose();
            }*/
          });
    }
  }
}
