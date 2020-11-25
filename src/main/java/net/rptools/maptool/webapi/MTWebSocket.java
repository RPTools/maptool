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
package net.rptools.maptool.webapi;

import com.google.gson.JsonObject;
import java.io.IOException;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class MTWebSocket extends WebSocketAdapter {

  /** The Session of this socket. */
  private Session session;

  @Override
  /**
   * Adds the session to the chatroom participants list, and sends back to the user the last three
   * messages in the conversation.
   */
  public void onWebSocketConnect(Session session) {
    System.out.println("DEBUG: Websocket Connect from " + session.getRemoteAddress().getAddress());
    this.session = session;
    System.out.println("DEBUG: Connected");
    MTWebClientManager.getInstance().addClient(this);
  }

  @Override
  public void onWebSocketBinary(byte[] bytes, int x, int y) {
    // not used
  }

  @Override
  public void onWebSocketText(String message) {
    System.out.println("DEBUG: Got Message" + message);
    // FIXME: need to test this is valid
    try {
      JsonObject json = JSONMacroFunctions.getInstance().asJsonElement(message).getAsJsonObject();
      String messageType = json.get("messageType").getAsString();
      String messageId = json.get("messageId").getAsString();
      JsonObject data = json.get("data").getAsJsonObject();

      if ("initiative".equals(messageType)) {
        System.out.println("DEBUG: Got an initiative message");
        WebAppInitiative.getInstance().processInitiativeMessage(data);
      } else if ("tokenInfo".equals(messageType) || "tokenProperties".equals(messageType)) {
        WebTokenInfo.getInstance().sendTokenInfo(this, messageId, data);
      } else if ("macro".equals(messageType)) {
        WebTokenInfo.getInstance().processMacro(data);
      } else if ("setProperties".equals(messageType)) {
        WebTokenInfo.getInstance().processSetProperties(data);
      }
    } catch (Exception e) {
      e.printStackTrace(); // FIXME: fix this to deal with error properly.
    }
  }

  @Override
  public void onWebSocketError(Throwable cause) {
    System.out.println("DEBUG: Websocket Error " + cause.getMessage());
    System.out.println(
        "DEBUG: number connections = "
            + MTWebClientManager.getInstance().getClientSessions().size());
    MTWebClientManager.getInstance().removeClient(this);
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    System.out.println("DEBUG: Websocket Close from " + session.getRemoteAddress().getAddress());
    MTWebClientManager.getInstance().removeClient(this);
  }

  void sendMessage(String messageType, JsonObject data) {
    sendMessage(messageType, null, data);
  }

  /**
   * Sends a message to the client.
   *
   * @param messageType The type of the message.
   * @param inResponseTo The message this is a response to.
   * @param data The data in the message.
   */
  void sendMessage(String messageType, String inResponseTo, JsonObject data) {
    JsonObject message = new JsonObject();
    message.addProperty("messageType", messageType);
    message.add("data", data);
    if (inResponseTo != null) {
      message.addProperty("inResponseTo", inResponseTo);
    }

    try {
      session.getRemote().sendString(message.toString());
      System.out.println("DEBUG: Wrote: " + message.toString());
    } catch (IOException ioe) {
      System.out.println("DEBUG: websocket write error.");
      System.out.println(
          "DEBUG: number connections = "
              + MTWebClientManager.getInstance().getClientSessions().size());
      MTWebClientManager.getInstance().removeClient(this);
    }
  }
}
