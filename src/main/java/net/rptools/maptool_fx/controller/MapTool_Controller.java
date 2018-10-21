/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import net.rptools.maptool.client.ui.MapToolFrame;

public class MapTool_Controller {

	@FXML // ResourceBundle that was given to the FXMLLoader
	private ResourceBundle resources;

	@FXML // URL location of the FXML file that was given to the FXMLLoader
	private URL location;

	@FXML // fx:id="rootContainer"
	private VBox rootContainer;

	@FXML // fx:id="mainStackPane"
	private StackPane mainStackPane;

	@FXML // fx:id="mainAnchorPane"
	private AnchorPane mainAnchorPane;

	@FXML // fx:id="assetAnchorPane"
	private AnchorPane assetAnchorPane;

	@FXML // fx:id="mapExplorerAnchorPane"
	private AnchorPane mapExplorerAnchorPane;

	@FXML // fx:id="chatAnchorPane"
	private AnchorPane chatAnchorPane;

	@FXML // fx:id="assetTitledPane"
	private TitledPane assetTitledPane;

	@FXML // fx:id="mapExplorerTitledPane"
	private TitledPane mapExplorerTitledPane;

	@FXML // fx:id="chatTitledPane"
	private TitledPane chatTitledPane;

	@FXML // This method is called by the FXMLLoader when initialization is complete
	void initialize() {
		assert rootContainer != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'MapTool.fxml'.";

		assert mainStackPane != null : "fx:id=\"mainStackPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert assetAnchorPane != null : "fx:id=\"assetAnchorPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert mapExplorerAnchorPane != null : "fx:id=\"mapExplorerAnchorPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert chatAnchorPane != null : "fx:id=\"chatAnchorPane\" was not injected: check your FXML file 'MapTool.fxml'.";

		assert assetTitledPane != null : "fx:id=\"assetTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert mapExplorerTitledPane != null : "fx:id=\"mapExplorerTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert chatTitledPane != null : "fx:id=\"chatTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
	}

	public void setDefaultPanes(MapToolFrame clientFrame) {
		// Set the main map view
		AnchorPane mapAchorPane = new AnchorPane();
		SwingNode mapViewSwingNode = new SwingNode();
		mapViewSwingNode.setContent(clientFrame.getZoneRendererPanel());
		mapAchorPane.getChildren().add(mapViewSwingNode);
		mainStackPane.getChildren().add(mapAchorPane);
		anchorIt(mapViewSwingNode);

		// Set other panes in accordians for now
		SwingNode assetViewSwingNode = new SwingNode();
		SwingNode mapExplorerSwingNode = new SwingNode();
		SwingNode chatSwingNode = new SwingNode();

		assetViewSwingNode.setContent(clientFrame.getAssetPanel());
		mapExplorerSwingNode.setContent(clientFrame.getTokenTreePanel());
		chatSwingNode.setContent(clientFrame.getCommandPanel());

		assetAnchorPane.getChildren().add(assetViewSwingNode);
		// mapExplorerAnchorPane.getChildren().add(mapExplorerSwingNode);
		chatAnchorPane.getChildren().add(chatSwingNode);

		anchorIt(assetViewSwingNode);
		anchorIt(mapExplorerSwingNode);
		anchorIt(chatSwingNode);

		chatTitledPane.setExpanded(true); // Default expand first pane
	}

	private void anchorIt(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
	}
}