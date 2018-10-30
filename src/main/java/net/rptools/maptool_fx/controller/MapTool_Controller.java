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

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import net.rptools.maptool.client.ui.MapToolFrame;

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

	private static final Logger log = LogManager.getLogger(MapTool_Controller.class);

	@FXML private ResourceBundle resources;

	@FXML private URL location;

	@FXML private VBox rootContainer;

	@FXML private Accordion leftAccordion;

	@FXML private TitledPane connectionsTitledPane;
	@FXML private TitledPane mapExplorerTitledPane;
	@FXML private TitledPane drawExplorerTitledPane;
	@FXML private TitledPane initiativeTitledPane;
	@FXML private TitledPane resourceLibraryTitledPane;
	@FXML private TitledPane chatTitledPane;
	@FXML private TitledPane tablesTitledPane;
	@FXML private TitledPane globalTitledPane;
	@FXML private TitledPane campaignTitledPane;
	@FXML private TitledPane selectedTitledPane;
	@FXML private TitledPane impersonatedTitledPane;

	@FXML private StackPane mainStackPane;
	@FXML private AnchorPane mainAnchorPane;

	@FXML
	void initialize() {
		assert rootContainer != null : "fx:id=\"rootContainer\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert leftAccordion != null : "fx:id=\"leftAccordion\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert connectionsTitledPane != null : "fx:id=\"connectionsTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert mapExplorerTitledPane != null : "fx:id=\"mapExplorerTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert drawExplorerTitledPane != null : "fx:id=\"drawExplorerTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert initiativeTitledPane != null : "fx:id=\"initiativeTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert resourceLibraryTitledPane != null : "fx:id=\"assetTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert chatTitledPane != null : "fx:id=\"chatTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert tablesTitledPane != null : "fx:id=\"tablesTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert globalTitledPane != null : "fx:id=\"globalTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert campaignTitledPane != null : "fx:id=\"campaignTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert selectedTitledPane != null : "fx:id=\"selectedTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert impersonatedTitledPane != null : "fx:id=\"impersonatedTitledPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert mainAnchorPane != null : "fx:id=\"mainAnchorPane\" was not injected: check your FXML file 'MapTool.fxml'.";
		assert mainStackPane != null : "fx:id=\"mainStackPane\" was not injected: check your FXML file 'MapTool.fxml'.";
	}

	public void setDefaultPanes(MapToolFrame clientFrame) {
		// Set the main map view
		var mapAchorPane = new AnchorPane();
		var mapViewSwingNode = new SwingNode();
		mapViewSwingNode.setContent(clientFrame.getZoneRendererPanel());
		mapAchorPane.getChildren().add(mapViewSwingNode);
		mainStackPane.getChildren().add(mapAchorPane);
		anchorIt(mapViewSwingNode);

		var webViewAnchorPane = new AnchorPane();
		var webView = new WebView();
		var webEngine = webView.getEngine();
		webEngine.loadContent("<b>Hello MapTool</b>");

		webViewAnchorPane.getChildren().add(webView);
		mainStackPane.getChildren().add(webViewAnchorPane);
		anchorIt(webView);

		// Set other panes in accordions for now
		addSwingNode(clientFrame.getConnectionPanel(), connectionsTitledPane, CONNECTIONS_ICON);
		addSwingNode(clientFrame.getTokenTreePanel(), mapExplorerTitledPane, MAP_EXPLORER_ICON);
		addSwingNode(clientFrame.getDrawablesTreePanel(), drawExplorerTitledPane, DRAW_EXPLORER_ICON); // vs
																										// getDrawablesPanel()
																										// ?
		addSwingNode(clientFrame.getInitiativePanel(), initiativeTitledPane, INITIATIVE_ICON);
		addSwingNode(clientFrame.getAssetPanel(), resourceLibraryTitledPane, RESOURCE_LIBRARY_ICON);
		addSwingNode(clientFrame.getCommandPanel(), chatTitledPane, CHAT_ICON);
		addSwingNode(clientFrame.getLookupTablePanel(), tablesTitledPane, TABLES_ICON);
		addSwingNode(clientFrame.getGlobalPanel(), globalTitledPane, GLOBAL_ICON);
		addSwingNode(clientFrame.getCampaignPanel(), campaignTitledPane, CAMPAIGN_ICON);
		addSwingNode(clientFrame.getSelectionPanel(), selectedTitledPane, SELECTED_ICON);
		addSwingNode(clientFrame.getImpersonatePanel(), impersonatedTitledPane, IMPERSONATED_ICON);

		// chatTitledPane.setExpanded(true); // Default expand first pane
	}

	public void setIntialTitledPane() {
		Platform.runLater(() -> {
			connectionsTitledPane.setExpanded(true);
			mapExplorerTitledPane.setExpanded(true);
			impersonatedTitledPane.setExpanded(true);
			// leftAccordian.setExpandedPane(impersonatedTitledPane);
			log.info("impersonatedTitledPane is now expanded? " + impersonatedTitledPane.isExpanded());
		});
	}

	private void addSwingNode(JComponent content, TitledPane titledPane, String graphicURI) {
		var swingNode = new SwingNode();
		var anchorPane = new AnchorPane();

		swingNode.setContent(content);
		anchorPane.getChildren().add(swingNode);
		titledPane.setContent(anchorPane);
		anchorIt(swingNode);

		// setGraphicIcon(titledPane, graphicURI);
	}

	private void setGraphicIcon(TitledPane titledPane, String graphicURI) {
		if (graphicURI.isEmpty())
			return;

		var imageView = new ImageView(new Image(graphicURI));
		imageView.setFitHeight(30);
		imageView.setFitWidth(30);
		imageView.setPreserveRatio(true);
		imageView.setSmooth(true);
		titledPane.setGraphic(imageView);
	}

	private void anchorIt(Node node) {
		AnchorPane.setTopAnchor(node, 0.0);
		AnchorPane.setBottomAnchor(node, 0.0);
		AnchorPane.setLeftAnchor(node, 0.0);
		AnchorPane.setRightAnchor(node, 0.0);
	}
}