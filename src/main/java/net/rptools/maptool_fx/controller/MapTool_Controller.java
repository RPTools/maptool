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
import java.util.HashMap;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import net.rptools.maptool.client.ui.MapToolFrame;

public class MapTool_Controller {
	@FXML private ResourceBundle resources;
	@FXML private URL location;
	@FXML private BorderPane rootPane;
	@FXML private MenuBar_Controller menuBar_Controller;

	private static final Logger log = LogManager.getLogger(MapTool_Controller.class);

	private static final String MAP_ICON = "/net/rptools/maptool/client/image/dock/map.png";
	private static final String CONNECTIONS_ICON = "/net/rptools/maptool/client/image/dock/connections.png";
	private static final String MAP_EXPLORER_ICON = "/net/rptools/maptool/client/image/dock/mapExplorer.png";
	private static final String DRAW_EXPLORER_ICON = "/net/rptools/maptool/client/image/dock/drawExplorer.png";
	private static final String INITIATIVE_ICON = "/net/rptools/maptool/client/image/dock/intiative.png";
	private static final String RESOURCE_LIBRARY_ICON = "/net/rptools/maptool/client/image/dock/resourceLibrary.png";
	private static final String CHAT_ICON = "/net/rptools/maptool/client/image/dock/chat.png";
	private static final String TABLES_ICON = "/net/rptools/maptool/client/image/dock/tables.png";
	private static final String GLOBAL_ICON = "/net/rptools/maptool/client/image/dock/localMacros.png";
	private static final String CAMPAIGN_ICON = "/net/rptools/maptool/client/image/dock/campaignMacros.png";
	private static final String SELECTED_ICON = "/net/rptools/maptool/client/image/dock/selectedMacros.png";
	private static final String IMPERSONATED_ICON = "/net/rptools/maptool/client/image/dock/impersonatedMacros.png";

	private static final Image DOCK_IMAGE = new Image("/net/rptools/maptool/client/image/dock/docknode.png");

	private DockPane dockPane;

	// Key is fx:id of the checkMenuItem the dock is associated with, if any.
	private HashMap<String, DockNode> dockNodes = new HashMap<String, DockNode>();

	@FXML
	void initialize() {
		assert rootPane != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert menuBar_Controller != null : "fx:id=\"menuBar_Controller\" was not injected: check your FXML file 'MapTool.fxml'.";

		menuBar_Controller.setParentControl(this);
	}

	public void setDefaultPanes(MapToolFrame clientFrame, DockPane dockPane) {
		this.dockPane = dockPane;

		addMapView(clientFrame, dockPane);

		// Set other panes in accordions for now
		dockNodes.put("connectionsWindowMenuItem", addSwingNode(clientFrame.getConnectionPanel(), CONNECTIONS_ICON));
		dockNodes.put("mapExplorerWindowMenuItem", addSwingNode(clientFrame.getTokenTreePanel(), MAP_EXPLORER_ICON));
		dockNodes.put("drawExplorerWindowMenuItem", addSwingNode(clientFrame.getDrawablesTreePanel(), DRAW_EXPLORER_ICON));
		dockNodes.put("intitiativeWindowMenuItem", addSwingNode(clientFrame.getInitiativePanel(), INITIATIVE_ICON));
		dockNodes.put("resourceLibraryWindowMenuItem", addSwingNode(clientFrame.getAssetPanel(), RESOURCE_LIBRARY_ICON));
		dockNodes.put("chatWindowMenuItem", addSwingNode(clientFrame.getCommandPanel(), CHAT_ICON));
		dockNodes.put("tablesWindowMenuItem", addSwingNode(clientFrame.getLookupTablePanel(), TABLES_ICON));
		dockNodes.put("globalWindowMenuItem", addSwingNode(clientFrame.getGlobalPanel(), GLOBAL_ICON));
		dockNodes.put("campaignWindowMenuItem", addSwingNode(clientFrame.getCampaignPanel(), CAMPAIGN_ICON));
		dockNodes.put("selectedWindowMenuItem", addSwingNode(clientFrame.getSelectionPanel(), SELECTED_ICON));
		dockNodes.put("impersonatedWindowMenuItem", addSwingNode(clientFrame.getImpersonatePanel(), IMPERSONATED_ICON));

		rootPane.setCenter(dockPane);
	}

	private void addMapView(MapToolFrame clientFrame, DockPane dockPane) {
		var mapAchorPane = new AnchorPane();
		var mapViewSwingNode = new SwingNode();
		mapViewSwingNode.setContent(clientFrame.getZoneRendererPanel());
		mapAchorPane.getChildren().add(mapViewSwingNode);
		anchorIt(mapViewSwingNode);

		var mapDockNode = addDockNode("Current Map", MAP_ICON, mapAchorPane);
		mapDockNode.dock(dockPane, DockPos.CENTER);
		mapDockNode.setClosable(false);
	}

	private DockNode addSwingNode(JComponent content, String graphicURI) {
		var swingNode = new SwingNode();
		swingNode.setContent(content);

		return addDockNode(content.getName(), graphicURI, swingNode);
	}

	private DockNode addDockNode(String dockName, String graphicURI, Node node) {
		var dockNode = new DockNode(node, dockName, setGraphicIcon(graphicURI));
		dockNode.closedProperty().addListener((arg, oldVal, newVal) -> ((CheckMenuItem) dockNode.getUserData()).setSelected(!newVal));

		return dockNode;
	}

	private ImageView setGraphicIcon(String graphicURI) {
		if (graphicURI.isEmpty())
			return new ImageView(DOCK_IMAGE);

		var imageView = new ImageView(new Image(graphicURI));
		imageView.setFitHeight(16);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);

		return imageView;
	}

	private void anchorIt(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
	}

	public void showWindow(CheckMenuItem checkMenuItem) {
		log.info("Show window for : " + checkMenuItem.getId());
		var dockNode = dockNodes.get(checkMenuItem.getId());

		if (checkMenuItem.isSelected()) {
			DockPos dockPosition = dockNode.getLastDockPos();
			if (dockPosition == null)
				dockPosition = DockPos.LEFT;
			else
				log.info("last pos " + dockPosition);

			dockNode.dock(dockPane, dockPosition);
			dockNode.setUserData(checkMenuItem); // Used to uncheck menu if dock is closed via X button
		} else {
			dockNode.close();
		}
	}
}