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

import javax.swing.JComponent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dockfx.DockNode;
import org.dockfx.DockPane;
import org.dockfx.DockPos;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.language.I18N;

public class MapTool_Controller {
	private static final String CONNECTIONS_ICON = "http://chittagongit.com/images/network-connect-icon/network-connect-icon-20.jpg";
	private static final String MAP_EXPLORER_ICON = "https://cdn0.iconfinder.com/data/icons/map-navigation-filled/64/Map_Navigation_Dirrection_Road-50_Location_GPS-512.png";
	private static final String DRAW_EXPLORER_ICON = "https://img.clipartxtras.com/c04de3a05a430c9fcb2f08f3d9ba601d_pencil-and-ruler-icon-free-icons-download-ruler-and-pencil-clipart_256-256.png";
	private static final String INITIATIVE_ICON = "https://orig00.deviantart.net/49f4/f/2007/207/7/a/d20_icon_de_la_gartoon_by_turtlegirlman.png";
	private static final String RESOURCE_LIBRARY_ICON = "https://tos.neet.tv/images/payment/icon_item_expbookx4.png";
	private static final String CHAT_ICON = "https://cdn.pixabay.com/photo/2013/07/13/12/10/chat-159319_640.png";
	private static final String TABLES_ICON = "https://cdn.pixabay.com/photo/2012/04/11/10/14/insert-27273_640.png";
	private static final String GLOBAL_ICON = "https://cdn0.iconfinder.com/data/icons/engineers6/143/Untitled-5-512.png";
	private static final String CAMPAIGN_ICON = "https://cdn3.iconfinder.com/data/icons/seo-glyph-2/24/gear-setting-512.png";
	private static final String SELECTED_ICON = "https://cdn.onlinewebfonts.com/svg/img_353313.png";
	private static final String IMPERSONATED_ICON = "https://seef.reputelligence.com/wp-content/uploads/2015/08/impersonation2.png";

	private static final Image DOCK_IMAGE = new Image("file:///D:/Development/git/JamzTheMan/dockfx/src/main/resources/org/dockfx/demo/docknode.png");

	private static final Logger log = LogManager.getLogger(MapTool_Controller.class);

	@FXML private ResourceBundle resources;

	@FXML private URL location;

	@FXML private BorderPane rootPane;

	@FXML
	void initialize() {
		assert rootPane != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'MapTool.fxml'.";
	}

	public void setDefaultPanes(MapToolFrame clientFrame, DockPane dockPane) {
		addMapView(clientFrame, dockPane);

		// Set other panes in accordions for now
		// addSwingNode(clientFrame.getConnectionPanel(), dockPane, CONNECTIONS_ICON);
		addSwingNode(clientFrame.getTokenTreePanel(), dockPane, MAP_EXPLORER_ICON);
		// addSwingNode(clientFrame.getDrawablesTreePanel(), dockPane, DRAW_EXPLORER_ICON);
		// addSwingNode(clientFrame.getInitiativePanel(), dockPane, INITIATIVE_ICON);
		addSwingNode(clientFrame.getAssetPanel(), dockPane, RESOURCE_LIBRARY_ICON);
		// addSwingNode(clientFrame.getCommandPanel(), dockPane, CHAT_ICON);
		addSwingNode(clientFrame.getLookupTablePanel(), dockPane, TABLES_ICON);
		// addSwingNode(clientFrame.getGlobalPanel(), dockPane, GLOBAL_ICON);
		// addSwingNode(clientFrame.getCampaignPanel(), dockPane, CAMPAIGN_ICON);
		// addSwingNode(clientFrame.getSelectionPanel(), dockPane, SELECTED_ICON);
		// addSwingNode(clientFrame.getImpersonatePanel(), dockPane, IMPERSONATED_ICON);

		rootPane.setCenter(dockPane);
	}

	private void addMapView(MapToolFrame clientFrame, DockPane dockPane) {
		var mapAchorPane = new AnchorPane();
		var mapViewSwingNode = new SwingNode();
		mapViewSwingNode.setContent(clientFrame.getZoneRendererPanel());
		mapAchorPane.getChildren().add(mapViewSwingNode);
		anchorIt(mapViewSwingNode);
		addDockNode("Current Map", dockPane, "", mapAchorPane, DockPos.CENTER);
	}

	private void addSwingNode(JComponent content, DockPane dockPane, String graphicURI) {
		var swingNode = new SwingNode();
		swingNode.setContent(content);

		addDockNode(content.getName(), dockPane, graphicURI, swingNode, DockPos.LEFT);
	}

	private void addDockNode(String dockName, DockPane dockPane, String graphicURI, Node node, DockPos dockPosition) {
		var dockNode = new DockNode(node, dockName, setGraphicIcon(graphicURI));
		// dockNode.setPrefSize(300, 300);
		dockNode.dock(dockPane, dockPosition);
		dockNode.closedProperty().addListener((arg, oldVal, newVal) -> System.out.println("dockNode " + dockName + " was closed."));
	}

	private ImageView setGraphicIcon(String graphicURI) {
		if (graphicURI.isEmpty())
			return new ImageView(DOCK_IMAGE);

		var imageView = new ImageView(new Image(graphicURI));
		imageView.setFitHeight(30);
		// imageView.setFitWidth(30);
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
}