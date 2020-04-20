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
package net.rptools.maptool.client.ui.javfx.vieweditpane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

public abstract class ViewEditPane extends BorderPane {

  /** The mode of the dialog. */
  enum MODE {
    /** Only view the contents, dont allow for editing. */
    VIEW,
    /** Only edit the contents, dont allow for read only mode. */
    EDIT,
    /** Allow for both view only and edit mode. */
    BOTH
  }

  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;

  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;

  @FXML // fx:id="buttonPane"
  private FlowPane buttonPane; // Value injected by FXMLLoader

  @FXML // fx:id="contentPane"
  private AnchorPane contentPane; // Value injected by FXMLLoader

  @FXML
  // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    assert buttonPane != null
        : "fx:id=\"buttonPane\" was not injected: check your FXML file 'ViewEditPane.fxml'.";
    assert contentPane != null
        : "fx:id=\"contentPane\" was not injected: check your FXML file 'ViewEditPane.fxml'.";
  }

  ViewEditPane() throws IOException {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("net.rptools.maptool.language.i18n");
    var loader =
        new FXMLLoader(
            getClass().getResource("/net/rptools/maptool/client/ui/fxml/ViewEditPane.fxml"),
            resourceBundle);
    loader.setRoot(this);
    loader.setController(this);
    loader.load();
  }

  void setContentPane(Pane content) {
    contentPane.getChildren().clear();
    contentPane.getChildren().add(content);
    AnchorPane.setTopAnchor(content, 0.0);
    AnchorPane.setBottomAnchor(content, 0.0);
    AnchorPane.setLeftAnchor(content, 0.0);
    AnchorPane.setRightAnchor(content, 0.0);
  }
}
