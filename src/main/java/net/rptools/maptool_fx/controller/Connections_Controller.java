/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.ContextMenuEvent;

public class Connections_Controller {

	@FXML private ListView<?> connectionsListView;

	private static final Logger log = LogManager.getLogger(Connections_Controller.class);

	@FXML
	void initialize() {
		assert connectionsListView != null : "fx:id=\"connectionsListView\" was not injected: check your FXML file 'Connections.fxml'.";
	}
}
