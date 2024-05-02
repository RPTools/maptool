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
package net.rptools.maptool.client;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.clientserver.simple.connection.Connection;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CampaignFactory;
import net.rptools.maptool.model.campaign.CampaignManager;

/** This class handles when the server inexplicably disconnects */
public class ServerDisconnectHandler implements DisconnectHandler {
  // TODO: This is a temporary hack until I can come up with a cleaner mechanism
  public static boolean disconnectExpected;

  public void handleDisconnect(Connection connection) {
    // Update internal state
    MapTool.disconnect();

    // TODO: attempt to reconnect if this was unexpected
    if (!disconnectExpected) {
      var errorText = I18N.getText("msg.error.server.disconnected");
      var connectionError = connection.getError();
      var errorMessage = errorText + (connectionError != null ? (": " + connectionError) : "");
      MapTool.showError(errorMessage);

      // hide map so player doesn't get a brief GM view
      MapTool.getFrame().setCurrentZoneRenderer(null);
      MapTool.getFrame().getToolbarPanel().getMapselect().setVisible(true);
      MapTool.getFrame().getAssetPanel().enableAssets();
      new CampaignManager().clearCampaignData();
      MapTool.getFrame().getToolbarPanel().setTokenSelectionGroupEnabled(true);
      try {
        MapTool.startPersonalServer(CampaignFactory.createBasicCampaign());
      } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
        MapTool.showError(I18N.getText("msg.error.server.cantrestart"), e);
      }
    } else if (!MapTool.isPersonalServer() && !MapTool.isHostingServer()) {
      // expected disconnect from someone else's server
      // hide map so player doesn't get a brief GM view
      MapTool.getFrame().setCurrentZoneRenderer(null);
    }
    disconnectExpected = false;
  }
}
