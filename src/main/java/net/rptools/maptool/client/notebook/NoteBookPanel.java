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
package net.rptools.maptool.client.notebook;

import java.util.Optional;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.rptools.maptool.model.notebook.NoteBookEntry;

public class NoteBookPanel extends JFXPanel {

  private TableView bookmarkTable = new TableView();

  public static NoteBookPanel createMapBookmarkPanel() {
    NoteBookPanel panel = new NoteBookPanel();
    panel.setVisible(true);
    Platform.runLater(panel::initFX);

    return panel;
  }

  private NoteBookPanel() {}

  private void initFX() {
    bookmarkTable.setEditable(false);
    TableColumn firstColumn = new TableColumn("Label");
    TableColumn secondColumn = new TableColumn("Name");
    bookmarkTable.getColumns().addAll(firstColumn, secondColumn);

    VBox vBox = new VBox();
    Scene scene = new Scene(vBox);
    Button addNote = new Button("Add Note");
    Button addView = new Button("Add View");
    Button addMarker = new Button("Add Marker");
    addNote.setOnAction(
        a -> {
          Dialog<NoteBookEntry> dialog = new Dialog<>();
          dialog.setTitle("Add Bookmark");
          dialog.setHeaderText("New Bookmark Details");
          Optional<NoteBookEntry> result = dialog.showAndWait();
        });
    vBox.setSpacing(5);
    vBox.setPadding(new Insets(10, 0, 0, 10));
    HBox buttonsHBox = new HBox();
    buttonsHBox.getChildren().addAll(addNote, addView, addMarker);
    vBox.getChildren().addAll(bookmarkTable, buttonsHBox);
    setScene(scene);
  }
}
