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
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Player.Role;
import net.rptools.maptool_fx.MapTool;

public class MapExplorer {
	public final String WINDOW_NAME = I18N.getString("panel.MapExplorer");
	
	private static final Logger log = LogManager.getLogger(MapExplorer.class);
	private String MAP_EXPLORER_FXML = "/net/rptools/maptool/fx/view/MapExplorer.fxml";

	private TreeView<Player> mapExplorerTreeView;

	public MapExplorer() {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(MAP_EXPLORER_FXML), ResourceBundle.getBundle(AppConstants.MAP_TOOL_BUNDLE));
		try {
			mapExplorerTreeView = fxmlLoader.load();
		} catch (IOException e) {
			log.error("Error loading ClientConnections FXML!", e);
		}

	}

	public Node getRootNode() {
		return mapExplorerTreeView;
	}
}