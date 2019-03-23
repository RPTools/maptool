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
import net.rptools.clientserver.simple.AbstractConnection;
import net.rptools.clientserver.simple.DisconnectHandler;
import net.rptools.maptool.model.CampaignFactory;

/** This class handles when the server inexplicably disconnects */
public class ServerDisconnectHandler implements DisconnectHandler {
  // TODO: This is a temporary hack until I can come up with a cleaner mechanism
  public static boolean disconnectExpected;

  public void handleDisconnect(AbstractConnection arg0) {
    // Update internal state
    MapTool.disconnect();

    // TODO: attempt to reconnect if this was unexpected
    if (!disconnectExpected) {
      MapTool.showError("Server has disconnected.");

      // hide map so player doesn't get a brief GM view
      MapTool.getFrame().setCurrentZoneRenderer(null);

      try {
        MapTool.startPersonalServer(CampaignFactory.createBasicCampaign());
      } catch (IOException ioe) {
        MapTool.showError("Could not restart personal server");
      }
    } else if (!MapTool.isPersonalServer() && !MapTool.isHostingServer()) {
      // expected disconnect from someone else's server
      // hide map so player doesn't get a brief GM view
      MapTool.getFrame().setCurrentZoneRenderer(null);
    }
    disconnectExpected = false;
  }
}
