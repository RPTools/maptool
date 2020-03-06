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
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.notebook.entry.Note;
import net.rptools.maptool.model.notebook.entry.NoteBuilder;
import net.rptools.maptool.model.notebook.NoteBook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The dialog used for editing or creating notes to be stored in the {@link
 * NoteBook}.
 */
public class EditNotePanel {

  /** Logger for this class. */
  private static final Logger log = LogManager.getLogger(EditNotePanel.class);

  /** The current {@link Note} being edited, or {@code null} if the dialog is for a new note. */
  private Note editingNotes;

  /** The {@link TextField} for editing the name of the note. */
  private final TextField nameTextField = new TextField();

  /** The {@link TextField} for editing the reference value of the note. */
  private final TextField referenceTextField = new TextField();

  /** The {@link ComboBox} for changing the {@link Zone} that a note belongs to. */
  private final ComboBox<Zone> zoneComboBox = new ComboBox<>();

  /** The {@link CheckBox} used to determine if the note belongs to a zone or not. */
  private final CheckBox zoneCheckBox =
      new CheckBox(I18N.getText("noteBook.editNote.belongsToMap"));

  /** HTMLEditor used for editing the note. */
  private final HTMLEditor htmlEditor = new HTMLEditor();

  /** Has the dialog been initialized. */
  private boolean hasBeenInitialized = false;

  /**
   * Initializes the structure of the dialog. This must be called once after creation of the {@code
   * EditNoteDialog} object.
   */
  synchronized void init(JFXPanel stage, Runnable closeCallback) {
    if (hasBeenInitialized) {
      return;
    }

    hasBeenInitialized = true;
    BorderPane root = new BorderPane();

    root.setCenter(htmlEditor);

    // Set up the details block on the dialog for the not details
    GridPane detailsGrid = new GridPane();

    detailsGrid.setPadding(new Insets(10, 10, 10, 10));

    detailsGrid.setVgap(5);
    detailsGrid.setHgap(5);

    detailsGrid.add(new Text(I18N.getText("noteBook.editNote.nameLabel")), 0, 0);
    detailsGrid.add(nameTextField, 1, 0);
    detailsGrid.add(new Text(I18N.getText("noteBook.editNote.required")), 2, 0);
    detailsGrid.add(new Text(I18N.getText("noteBook.editNote.mapLabel")), 0, 1);
    detailsGrid.add(zoneComboBox, 1, 1);
    detailsGrid.add(zoneCheckBox, 2, 1);
    detailsGrid.add(new Text(I18N.getText("noteBook.editNote.referenceLabel")), 0, 2);
    detailsGrid.add(referenceTextField, 1, 2);

    root.setTop(detailsGrid);

    // Create the Ok and Cancel buttons for the dialog.
    TilePane buttons = new TilePane(Orientation.HORIZONTAL);
    buttons.setPadding(new Insets(20, 0, 20, 0));
    buttons.setHgap(100.0);
    buttons.setVgap(8.0);
    buttons.setAlignment(Pos.CENTER);

    Button okButton = new Button(I18N.getText("noteBook.editNote.ok"));
    okButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    okButton.setOnAction(a -> handleOk(closeCallback));

    Button cancelButton = new Button(I18N.getText("noteBook.editNote.cancel"));
    cancelButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    if (closeCallback != null) {
      cancelButton.setOnAction(a -> closeCallback.run());
    }

    buttons.getChildren().addAll(cancelButton, okButton);

    // Enable / Disable the Zone Combo box on change of the Zone Check Box
    zoneCheckBox.setOnAction(
        a -> {
          if (zoneCheckBox.isSelected()) {
            zoneComboBox.setDisable(false);
          } else {
            zoneComboBox.setDisable(true);
          }
        });

    // Disable ok button if name is empty
    nameTextField
        .textProperty()
        .addListener(
            (ob, oldVal, newVal) -> {
              if (newVal.trim().isEmpty()) {
                okButton.setDisable(true);
              } else {
                okButton.setDisable(false);
              }
            });
    okButton.setDisable(true);

    root.setBottom(buttons);

    stage.setScene(new Scene(root, 600, 800));
  }

  /**
   * Shows the dialog with blank values to create a new {@link Note}. If the note is created
   * successfully it will be added to the campaign note book.
   */
  synchronized void editNew() {
    htmlEditor.setHtmlText("");
    editingNotes = null;
    nameTextField.clear();
    referenceTextField.clear();
    zoneCheckBox.setSelected(true);
    Zone currentZone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    populateZoneComboBox(currentZone);
  }

  /**
   * Shows the dialog with blank values to create a new {@link Note}. If the note is created
   * successfully it will be added to the campaign note book.
   */
  synchronized void edit(Note note) {
    editingNotes = note;

    if (note.getNotesKey().isPresent()) {
      AssetManager.getAssetAsynchronously(
          note.getNotesKey().get(),
          (key) -> {
            String noteString = AssetManager.getAsset(key).getDataAsString();
            Platform.runLater(() -> htmlEditor.setHtmlText(noteString));
          });
    }

    nameTextField.setText(editingNotes.getName());
    if (editingNotes.getReference().isPresent()) {
      referenceTextField.setText(editingNotes.getReference().get());
    } else {
      referenceTextField.clear();
    }

    Zone currentZone;

    if (editingNotes.getZoneId().isPresent()) {
      GUID zoneId = editingNotes.getZoneId().get();
      currentZone = MapTool.getCampaign().getZone(zoneId);
      zoneCheckBox.setSelected(true);
    } else {
      currentZone = null;
      zoneCheckBox.setSelected(false);
    }

    populateZoneComboBox(currentZone);
  }

  /**
   * Populates the {@code zoneComboBox} with the zones in the campaign
   *
   * @param defaultZone the {@link Zone} that should be the default value for the combo box. If this
   *     is {@code null} there is no default value set.
   */
  private synchronized void populateZoneComboBox(Zone defaultZone) {
    zoneComboBox.getItems().clear();
    for (Zone zone : MapTool.getCampaign().getZones()) {
      zoneComboBox.getItems().add(zone);
    }
    zoneComboBox.setValue(defaultZone);
  }

  /**
   * Checks that the edits are valid and if the are it adds the {@link Note} to the campaign note
   * book.
   */
  private synchronized void handleOk(Runnable closeCallback) {
    boolean valid = true;

    if (nameTextField.getText().trim().isEmpty()) {
      MapTool.showInformation("noteBook.editNode.noteNameRequired");
      valid = false;
    }

    if (valid) {
      NoteBuilder noteBuilder;
      if (editingNotes == null) {
        noteBuilder = new NoteBuilder();
      } else {
        noteBuilder = NoteBuilder.copy(editingNotes);
      }
      noteBuilder.setName(nameTextField.getText()).setReference(referenceTextField.getText());
      if (zoneCheckBox.isSelected()) {
        GUID zoneID = zoneComboBox.getSelectionModel().getSelectedItem().getId();
        noteBuilder.setZoneId(zoneID);
      }

      noteBuilder.setNotes(htmlEditor.getHtmlText());

      MapTool.getCampaign().getNotebook().putEntry(noteBuilder.build());
      if (closeCallback != null) {
        closeCallback.run();
      }
    }
  }
}
