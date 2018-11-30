/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * TokenTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx.client.ui;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.asymmetric.ec.SignatureSpi.ecCVCDSA;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool_fx.MapTool;

public class ClientConnections {
	private static final Logger log = LogManager.getLogger(ClientConnections.class);
	private String CONNECTIONS_FXML = "/net/rptools/maptool/fx/view/Connections.fxml";

	private ListView<Player> clientListView;

	public ClientConnections() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CONNECTIONS_FXML), ResourceBundle.getBundle(AppConstants.MAP_TOOL_BUNDLE));
		try {
			clientListView = fxmlLoader.load();
		} catch (IOException e) {
			log.error("Error loading ClientConnections FXML!", e);
		}

		// TODO: Remove Me later, for testing only
		// MapTool.getPlayerList().add(new Player("Mogo", Role.PLAYER, ""));
		// MapTool.getPlayerList().add(new Player("Clint", Role.PLAYER, ""));
		// MapTool.getPlayerList().add(new Player("JamzTheMan", Role.GM, ""));
		// MapTool.getPlayerList().add(new Player("Mogo2", Role.PLAYER, ""));

		clientListView.setItems(MapTool.getPlayerList());
		clientListView.setCellFactory(param -> new PlayerListCell());
	}

	public Node getRootNode() {
		return clientListView;
	}

	private final class PlayerListCell extends ListCell<Player> {
		@Override
		protected void updateItem(Player player, boolean empty) {
			super.updateItem(player, empty);

			if (empty || player == null || player.getName() == null) {
				setText(null);
			} else {
				var borderPane = new BorderPane();
				var hBox = new HBox();

				var label = new Label(player.toString());
				label.setPadding(new Insets(0, 10, 0, 0));
				borderPane.setLeft(label);

				if ((MapTool.isHostingServer() || MapTool.getPlayer().isGM()) && !MapTool.getPlayer().equals(player)) {
					var kickButton = new Button(I18N.getText("action.kickConnectedPlayer"));
					var banButton = new Button(I18N.getText("action.banConnectedPlayer"));

					kickButton.setOnAction((event) -> kickConnectedPlayer(player));
					banButton.setOnAction((event) -> kickConnectedPlayer(player, true));

					kickButton.setId("kick-button");
					banButton.setId("ban-button");

					hBox.getChildren().add(banButton);
					hBox.getChildren().add(kickButton);
					hBox.setSpacing(5);

					borderPane.setRight(hBox);
				}

				setGraphic(borderPane);
			}
		}

	}

	private void kickConnectedPlayer(Player player) {
		kickConnectedPlayer(player, false);
	}

	private void kickConnectedPlayer(Player selectedPlayer, boolean banPlayer) {
		if (MapTool.isPlayerConnected(selectedPlayer.getName())) {
			if (banPlayer) {
				MapTool.showInformation("CURRENTLY NOT IMPLEMENTED!");
				return;
			}

			String msg;
			if (banPlayer)
				msg = I18N.getText("msg.confirm.banPlayer", selectedPlayer.getName());
			else
				msg = I18N.getText("msg.confirm.kickPlayer", selectedPlayer.getName());

			if (MapTool.confirm(msg)) {
				if (banPlayer) {
					// TODO need a ban hammer!
					MapTool.showInformation(I18N.getText("msg.info.playerBanned", selectedPlayer.getName()));
				} else {
					MapTool.serverCommand().bootPlayer(selectedPlayer.getName());

					// Check if player is really removed?
					if (MapTool.isPlayerConnected(selectedPlayer.getName()))
						MapTool.showError("msg.error.failedToBoot");
					else
						MapTool.showInformation(I18N.getText("msg.info.playerBooted", selectedPlayer.getName()));
				}
			}

			return;
		}

		MapTool.showError("msg.error.failedToBoot");
	};
}