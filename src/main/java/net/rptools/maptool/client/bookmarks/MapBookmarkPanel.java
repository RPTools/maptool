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
package net.rptools.maptool.client.bookmarks;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class MapBookmarkPanel extends JFXPanel {

  private TableView bookmarkTable = new TableView();

  public static MapBookmarkPanel createMapBookmarkPanel() {
    MapBookmarkPanel panel = new MapBookmarkPanel();
    panel.setVisible(true);
    Platform.runLater(panel::initFX);

    return panel;
  }

  private MapBookmarkPanel() {}

  private void initFX() {
    bookmarkTable.setEditable(false);
    TableColumn firstColumn = new TableColumn("Label");
    TableColumn secondColumn = new TableColumn("Name");
    bookmarkTable.getColumns().addAll(firstColumn, secondColumn);

    VBox vBox = new VBox();
    Scene scene = new Scene(vBox);
    Button addButton = new Button("Add Bookmark");
    vBox.setSpacing(5);
    vBox.setPadding(new Insets(10, 0, 0, 10));
    vBox.getChildren().addAll(bookmarkTable, addButton);
    setScene(scene);
  }
}
