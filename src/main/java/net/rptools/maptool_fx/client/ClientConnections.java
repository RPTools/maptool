/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * TokenTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx.client;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import net.rptools.maptool.client.AppActions;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.model.Player;
import net.rptools.maptool_fx.MapTool;

public class ClientConnections {
	private static final Logger log = LogManager.getLogger(ClientConnections.class);
	private String CONNECTIONS_FXML = "/net/rptools/maptool/fx/view/Connections.fxml";

	private ListView<Player> clientListView;
	// private ObservableList<Player> names = FXCollections.observableArrayList();

	public ClientConnections() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CONNECTIONS_FXML), ResourceBundle.getBundle(AppConstants.MAP_TOOL_BUNDLE));
		try {
			clientListView = fxmlLoader.load();
		} catch (IOException e) {
			log.error("Error loading ClientConnections FXML!", e);
		}

		clientListView.setItems(MapTool.getPlayerList());

		clientListView.setCellFactory(param -> new ListCell<Player>() {
		    @Override
		    protected void updateItem(Player player, boolean empty) {
		        super.updateItem(player, empty);

		        if (empty || player == null || player.getName() == null) {
		            setText(null);
		        } else {
		        	this.setContentDisplay(ContentDisplay.RIGHT);
		            setText(player.toString());
		            
		            var button = new Button("Kick");
		            // button.setOnAction(ACTION HERE);
		            // OLD ACTION :: AppActions.BOOT_CONNECTED_PLAYER

		            setGraphic(button);
		            this.setGraphicTextGap(10);
		        }
		    }
		});
	}

	public Node getRootNode() {
		return clientListView;
	}
}