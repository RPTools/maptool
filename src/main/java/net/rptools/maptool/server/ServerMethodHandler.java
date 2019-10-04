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
package net.rptools.maptool.server;

import java.awt.geom.Area;
import java.io.IOException;
import java.util.*;
import net.rptools.clientserver.hessian.AbstractMethodHandler;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.ClientCommand;
import net.rptools.maptool.client.ClientMethodHandler;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ServerCommandClientImpl;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.ExposedAreaMetaData;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Pointer;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.transfer.AssetProducer;

/**
 * This class is used by the server host to receive client commands sent through {@link
 * ServerCommandClientImpl ServerCommandClientImpl}. Once the command is received, this will update
 * the server data, before forwarding the command to the clients. Clients will then handle the
 * command through {@link ClientMethodHandler ClientMethodHandler}. Updating the server itself is
 * important as new client receive the server's campaign data when connecting.
 *
 * @author drice *
 */
public class ServerMethodHandler extends AbstractMethodHandler implements ServerCommand {
  private final MapToolServer server;
  private final Object MUTEX = new Object();

  public ServerMethodHandler(MapToolServer server) {
    this.server = server;
  }

  @SuppressWarnings("unchecked")
  public void handleMethod(String id, String method, Object... parameters) {
    ServerCommand.COMMAND cmd = Enum.valueOf(ServerCommand.COMMAND.class, method);
    // System.out.println("ServerMethodHandler#handleMethod: " + id + " - " + cmd.name());

    try {
      RPCContext context = new RPCContext(id, method, parameters);
      RPCContext.setCurrent(context);
      switch (cmd) {
        case bootPlayer:
          bootPlayer(context.getString(0));
          break;
        case bringTokensToFront:
          bringTokensToFront(context.getGUID(0), (Set<GUID>) context.get(1));
          break;
        case draw:
          draw(context.getGUID(0), (Pen) context.get(1), (Drawable) context.get(2));
          break;
        case updateDrawing:
          updateDrawing(context.getGUID(0), (Pen) context.get(1), (DrawnElement) context.get(2));
          break;
        case enforceZoneView:
          enforceZoneView(
              context.getGUID(0),
              context.getInt(1),
              context.getInt(2),
              context.getDouble(3),
              context.getInt(4),
              context.getInt(5));
          break;
        case restoreZoneView:
          restoreZoneView(context.getGUID(0));
          break;
        case exposeFoW:
          exposeFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
          break;
        case getAsset:
          getAsset((MD5Key) context.get(0));
          break;
        case getZone:
          getZone(context.getGUID(0));
          break;
        case hideFoW:
          hideFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
          break;
        case setFoW:
          setFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
          break;
        case hidePointer:
          hidePointer(context.getString(0));
          break;
        case setLiveTypingLabel:
          setLiveTypingLabel(context.getString(0), context.getBool(1));
          break;
        case enforceNotification:
          enforceNotification(context.getBool(0));
          break;
        case message:
          message((TextMessage) context.get(0));
          break;
        case execLink:
          execLink((String) context.get(0), (String) context.get(1));
          break;
        case putAsset:
          putAsset((Asset) context.get(0));
          break;
        case putLabel:
          putLabel(context.getGUID(0), (Label) context.get(1));
          break;
        case updateTokenProperty:
          updateTokenProperty(
              context.getGUID(0), context.getGUID(1), context.getString(2), context.getObjArray(3));
          break;

        case putToken:
          putToken(context.getGUID(0), (Token) context.get(1));
          break;
        case putZone:
          putZone((Zone) context.get(0));
          break;
        case removeZone:
          removeZone(context.getGUID(0));
          break;
        case removeAsset:
          removeAsset((MD5Key) context.get(0));
          break;
        case removeToken:
          removeToken(context.getGUID(0), context.getGUID(1));
          break;
        case removeLabel:
          removeLabel(context.getGUID(0), context.getGUID(1));
          break;
        case sendTokensToBack:
          sendTokensToBack(context.getGUID(0), (Set<GUID>) context.get(1));
          break;
        case setCampaign:
          setCampaign((Campaign) context.get(0));
          break;
        case setZoneGridSize:
          setZoneGridSize(
              context.getGUID(0),
              context.getInt(1),
              context.getInt(2),
              context.getInt(3),
              context.getInt(4));
          break;
        case setZoneVisibility:
          setZoneVisibility(context.getGUID(0), (Boolean) context.get(1));
          break;
        case setZoneHasFoW:
          setZoneHasFoW(context.getGUID(0), context.getBool(1));
          break;
        case showPointer:
          showPointer(context.getString(0), (Pointer) context.get(1));
          break;
        case startTokenMove:
          startTokenMove(
              context.getString(0),
              context.getGUID(1),
              context.getGUID(2),
              (Set<GUID>) context.get(3));
          break;
        case stopTokenMove:
          stopTokenMove(context.getGUID(0), context.getGUID(1));
          break;
        case toggleTokenMoveWaypoint:
          toggleTokenMoveWaypoint(
              context.getGUID(0), context.getGUID(1), (ZonePoint) context.get(2));
          break;
        case undoDraw:
          undoDraw(context.getGUID(0), context.getGUID(1));
          break;
        case updateTokenMove:
          updateTokenMove(
              context.getGUID(0), context.getGUID(1), context.getInt(2), context.getInt(3));
          break;
        case clearAllDrawings:
          clearAllDrawings(context.getGUID(0), (Zone.Layer) context.get(1));
          break;
        case enforceZone:
          enforceZone(context.getGUID(0));
          break;
        case setServerPolicy:
          setServerPolicy((ServerPolicy) context.get(0));
          break;
        case addTopology:
          addTopology(context.getGUID(0), (Area) context.get(1));
          break;
        case removeTopology:
          removeTopology(context.getGUID(0), (Area) context.get(1));
          break;
        case renameZone:
          renameZone(context.getGUID(0), context.getString(1));
          break;
        case heartbeat:
          heartbeat(context.getString(0));
          break;
        case updateCampaign:
          updateCampaign((CampaignProperties) context.get(0));
          break;
        case movePointer:
          movePointer(context.getString(0), context.getInt(1), context.getInt(2));
          break;
        case updateInitiative:
          updateInitiative((InitiativeList) context.get(0), (Boolean) context.get(1));
          break;
        case updateTokenInitiative:
          updateTokenInitiative(
              context.getGUID(0),
              context.getGUID(1),
              context.getBool(2),
              context.getString(3),
              context.getInt(4));
          break;
        case setVisionType:
          setVisionType(context.getGUID(0), (VisionType) context.get(1));
          break;
        case setBoard:
          setBoard(
              context.getGUID(0), (MD5Key) context.get(1), context.getInt(2), context.getInt(3));
          break;
        case updateCampaignMacros:
          updateCampaignMacros((List<MacroButtonProperties>) context.get(0));
          break;
        case setTokenLocation:
          setTokenLocation(
              context.getGUID(0), context.getGUID(1), context.getInt(2), context.getInt(3));
          break;
        case exposePCArea:
          exposePCArea(context.getGUID(0));
          break;
        case updateExposedAreaMeta:
          updateExposedAreaMeta(
              context.getGUID(0), context.getGUID(1), (ExposedAreaMetaData) context.get(2));
          break;
        case clearExposedArea:
          clearExposedArea(context.getGUID(0));
          break;
      }
    } finally {
      RPCContext.setCurrent(null);
    }
  }

  /** Send the current call to all other clients except for the sender */
  private void forwardToClients() {
    server
        .getConnection()
        .broadcastCallMethod(
            new String[] {RPCContext.getCurrent().id},
            RPCContext.getCurrent().method,
            RPCContext.getCurrent().parameters);
  }

  /** Send the current call to all clients including the sender */
  private void forwardToAllClients() {
    server
        .getConnection()
        .broadcastCallMethod(
            new String[] {}, RPCContext.getCurrent().method, RPCContext.getCurrent().parameters);
  }

  private void broadcastToClients(String exclude, String method, Object... parameters) {
    server.getConnection().broadcastCallMethod(new String[] {exclude}, method, parameters);
  }

  private void broadcastToAllClients(String method, Object... parameters) {
    server.getConnection().broadcastCallMethod(new String[] {}, method, parameters);
  }

  ////
  // SERVER COMMAND
  public void setVisionType(GUID zoneGUID, VisionType visionType) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setVisionType(visionType);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setUseVision.name(), RPCContext.getCurrent().parameters);
  }

  public void heartbeat(String data) {
    // Nothing to do yet
  }

  public void enforceZone(GUID zoneGUID) {
    forwardToClients();
  }

  public void updateCampaign(CampaignProperties properties) {
    server.getCampaign().replaceCampaignProperties(properties);
    forwardToClients();
  }

  public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet) {
    synchronized (MUTEX) {
      Zone zone = server.getCampaign().getZone(zoneGUID);

      // Get the tokens to update
      List<Token> tokenList = new ArrayList<Token>();
      for (GUID tokenGUID : tokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token != null) {
          tokenList.add(token);
        }
      }
      // Arrange
      Collections.sort(tokenList, Zone.TOKEN_Z_ORDER_COMPARATOR);

      // Update
      int z = zone.getLargestZOrder() + 1;
      for (Token token : tokenList) {
        token.setZOrder(z++);
      }
      // Broadcast
      for (Token token : tokenList) {
        broadcastToAllClients(ClientCommand.COMMAND.putToken.name(), zoneGUID, token);
      }
      zone.sortZOrder(); // update new ZOrder on server zone
    }
  }

  public void clearAllDrawings(GUID zoneGUID, Zone.Layer layer) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    List<DrawnElement> list = zone.getDrawnElements(layer);
    zone.clearDrawables(list); // FJE Empties the DrawableUndoManager and empties the list
    forwardToAllClients();
  }

  public void draw(GUID zoneGUID, Pen pen, Drawable drawable) {
    server
        .getConnection()
        .broadcastCallMethod(ClientCommand.COMMAND.draw.name(), RPCContext.getCurrent().parameters);
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.addDrawable(new DrawnElement(drawable, pen));
  }

  public void updateDrawing(GUID zoneGUID, Pen pen, DrawnElement drawnElement) {
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.updateDrawing.name(), RPCContext.getCurrent().parameters);
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.updateDrawable(drawnElement, pen);
  }

  public void enforceZoneView(GUID zoneGUID, int x, int y, double scale, int width, int height) {
    forwardToClients();
  }

  public void restoreZoneView(GUID zoneGUID) {
    forwardToClients();
  }

  public void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    Zone zone =
        server
            .getCampaign()
            .getZone(
                zoneGUID); // this can return a zone that's not in MapToolFrame.zoneRenderList???
    zone.exposeArea(area, selectedToks);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.exposeFoW.name(), RPCContext.getCurrent().parameters);
  }

  public void exposePCArea(GUID zoneGUID) {
    ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
    FogUtil.exposePCArea(renderer);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.exposePCArea.name(), RPCContext.getCurrent().parameters);
  }

  public void getAsset(MD5Key assetID) {
    if (assetID == null || assetID.toString().length() == 0) {
      return;
    }
    try {
      AssetProducer producer =
          new AssetProducer(
              assetID,
              AssetManager.getAssetInfo(assetID).getProperty(AssetManager.NAME),
              AssetManager.getAssetCacheFile(assetID));
      server
          .getConnection()
          .callMethod(
              RPCContext.getCurrent().id,
              MapToolConstants.Channel.IMAGE,
              ClientCommand.COMMAND.startAssetTransfer.name(),
              producer.getHeader());
      server.addAssetProducer(RPCContext.getCurrent().id, producer);

    } catch (IOException ioe) {
      ioe.printStackTrace();

      // Old fashioned way
      server
          .getConnection()
          .callMethod(
              RPCContext.getCurrent().id,
              ClientCommand.COMMAND.putAsset.name(),
              AssetManager.getAsset(assetID));
    } catch (IllegalArgumentException iae) {
      // Sending an empty asset will cause a failure of the image to load on the client side,
      // showing a broken
      // image instead of blowing up
      Asset asset = new Asset("broken", new byte[] {});
      asset.setId(assetID);
      server
          .getConnection()
          .callMethod(RPCContext.getCurrent().id, ClientCommand.COMMAND.putAsset.name(), asset);
    }
  }

  public void getZone(GUID zoneGUID) {
    server
        .getConnection()
        .callMethod(
            RPCContext.getCurrent().id,
            ClientCommand.COMMAND.putZone.name(),
            server.getCampaign().getZone(zoneGUID));
  }

  public void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.hideArea(area, selectedToks);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.hideFoW.name(), RPCContext.getCurrent().parameters);
  }

  public void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setFogArea(area, selectedToks);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setFoW.name(), RPCContext.getCurrent().parameters);
  }

  public void hidePointer(String player) {
    forwardToAllClients();
  }

  public void movePointer(String player, int x, int y) {
    forwardToAllClients();
  }

  public void updateInitiative(InitiativeList list, Boolean ownerPermission) {
    if (list != null) {
      if (list.getZone() == null) return;
      Zone zone = server.getCampaign().getZone(list.getZone().getId());
      zone.setInitiativeList(list);
    } else if (ownerPermission != null) {
      MapTool.getFrame().getInitiativePanel().setOwnerPermissions(ownerPermission.booleanValue());
    }
    forwardToAllClients();
  }

  public void updateTokenInitiative(
      GUID zoneId, GUID tokenId, Boolean hold, String state, Integer index) {
    Zone zone = server.getCampaign().getZone(zoneId);
    InitiativeList list = zone.getInitiativeList();
    TokenInitiative ti = list.getTokenInitiative(index);
    if (!ti.getId().equals(tokenId)) {
      // Index doesn't point to same token, try to find it
      Token token = zone.getToken(tokenId);
      List<Integer> tokenIndex = list.indexOf(token);

      // If token in list more than one time, punt
      if (tokenIndex.size() != 1) return;
      ti = list.getTokenInitiative(tokenIndex.get(0));
    } // endif
    ti.update(hold, state);
    forwardToAllClients();
  }

  public void renameZone(GUID zoneGUID, String name) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    if (zone != null) {
      zone.setName(name);
      forwardToAllClients();
    }
  }

  public void message(TextMessage message) {
    forwardToClients();
  }

  @Override
  public void execLink(String link, String target) {
    forwardToAllClients();
  }

  public void putAsset(Asset asset) {
    AssetManager.putAsset(asset);
  }

  public void putLabel(GUID zoneGUID, Label label) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.putLabel(label);
    forwardToClients();
  }

  public void putToken(GUID zoneGUID, Token token) {
    Zone zone = server.getCampaign().getZone(zoneGUID);

    boolean newToken = zone.getToken(token.getId()) == null;
    synchronized (MUTEX) {
      // Set z-order for new tokens
      if (newToken) {
        token.setZOrder(zone.getLargestZOrder() + 1);
      }
      zone.putToken(token);
    }
    if (newToken) {
      forwardToAllClients();
    } else {
      forwardToClients();
    }
  }

  public void putZone(Zone zone) {
    server.getCampaign().putZone(zone);
    forwardToClients();
  }

  public void removeAsset(MD5Key assetID) {
    AssetManager.removeAsset(assetID);
  }

  public void removeLabel(GUID zoneGUID, GUID labelGUID) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.removeLabel(labelGUID);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.removeLabel.name(), RPCContext.getCurrent().parameters);
  }

  public void removeToken(GUID zoneGUID, GUID tokenGUID) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.removeToken(tokenGUID);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.removeToken.name(), RPCContext.getCurrent().parameters);
  }

  public void updateTokenProperty(
      GUID zoneGUID, GUID tokenGUID, String methodName, Object[] parameters) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    Token token = zone.getToken(tokenGUID);
    token.updateProperty(zone, methodName, parameters); // update server version of token

    forwardToClients();
  }

  public void updateTokenProperty(
      Token token,
      String methodName,
      Object...
          parameters) {} // never actually called, but necessary to satisfy interface requirements

  public void removeZone(GUID zoneGUID) {
    server.getCampaign().removeZone(zoneGUID);
    forwardToClients();
  }

  public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet) {
    synchronized (MUTEX) {
      Zone zone = server.getCampaign().getZone(zoneGUID);

      // Get the tokens to update
      List<Token> tokenList = new ArrayList<Token>();
      for (GUID tokenGUID : tokenSet) {
        Token token = zone.getToken(tokenGUID);
        if (token != null) {
          tokenList.add(token);
        }
      }
      // Arrange
      Collections.sort(tokenList, Zone.TOKEN_Z_ORDER_COMPARATOR);

      // Update
      int z = zone.getSmallestZOrder() - 1;
      for (Token token : tokenList) {
        token.setZOrder(z--);
      }
      // Broadcast
      for (Token token : tokenList) {
        broadcastToAllClients(ClientCommand.COMMAND.putToken.name(), zoneGUID, token);
      }
      zone.sortZOrder(); // update new ZOrder on server zone
    }
  }

  public void setCampaign(Campaign campaign) {
    server.setCampaign(campaign);
    forwardToClients();
  }

  public void setZoneGridSize(GUID zoneGUID, int offsetX, int offsetY, int size, int color) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    Grid grid = zone.getGrid();
    grid.setSize(size);
    grid.setOffset(offsetX, offsetY);
    zone.setGridColor(color);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setZoneGridSize.name(), RPCContext.getCurrent().parameters);
  }

  public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setHasFog(hasFog);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setZoneHasFoW.name(), RPCContext.getCurrent().parameters);
  }

  public void setZoneVisibility(GUID zoneGUID, boolean visible) {
    server.getCampaign().getZone(zoneGUID).setVisible(visible);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setZoneVisibility.name(), RPCContext.getCurrent().parameters);
  }

  public void showPointer(String player, Pointer pointer) {
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.showPointer.name(), RPCContext.getCurrent().parameters);
  }

  public void setLiveTypingLabel(String label, boolean show) {
    forwardToClients();
  }

  public void enforceNotification(Boolean enforce) {
    forwardToClients();
  }

  public void bootPlayer(String player) {
    forwardToClients();

    // And just to be sure, remove them from the server
    server.releaseClientConnection(server.getConnectionId(player));
  }

  public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
    forwardToClients();
  }

  public void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
    forwardToClients();
  }

  public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp) {
    forwardToClients();
  }

  public void undoDraw(GUID zoneGUID, GUID drawableGUID) {
    // This is a problem. The contents of the UndoManager are not synchronized across machines
    // so if one machine uses Meta-Z to undo a drawing, that drawable will be removed on all
    // machines, but there is no attempt to keep the UndoManager in sync. So that same drawable
    // will still be in the UndoManager queue on other machines. Ideally we should be filtering
    // the local Undomanager queue based on the drawable (removing it when we find it), but
    // the Swing UndoManager doesn't provide that capability so we would need to subclass it.
    // And if we're going to do that, we may as well fix the other problems: the UndoManager should
    // be per-map and per-layer (?) and not a singleton instance for the entire application! But
    // now we're talking a pretty intrusive set of changes: when a zone is deleted, the UndoManagers
    // would need to be cleared and duplicating a zone means doing a deep copy on the UndoManager
    // or flushing it entirely in the new zone. We'll save all of this for a separate patch against
    // 1.3 or
    // for 1.4.
    server
        .getConnection()
        .broadcastCallMethod(ClientCommand.COMMAND.undoDraw.name(), zoneGUID, drawableGUID);
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.removeDrawable(drawableGUID);
  }

  public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
    forwardToClients();
  }

  public void setTokenLocation(GUID zoneGUID, GUID tokenGUID, int x, int y) {
    forwardToClients();
  }

  public void setServerPolicy(ServerPolicy policy) {
    forwardToClients();
  }

  public void addTopology(GUID zoneGUID, Area area) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.addTopology(area);
    forwardToClients();
  }

  public void removeTopology(GUID zoneGUID, Area area) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.removeTopology(area);
    forwardToClients();
  }

  public void updateCampaignMacros(List<MacroButtonProperties> properties) {
    ArrayList campaignMacros = new ArrayList<MacroButtonProperties>(properties);
    MapTool.getCampaign().setMacroButtonPropertiesArray(campaignMacros);
    server.getCampaign().setMacroButtonPropertiesArray(campaignMacros);
    forwardToClients();
  }

  public void setBoard(GUID zoneGUID, MD5Key mapId, int x, int y) {
    forwardToClients();
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.server.ServerCommand#updateExposedAreaMeta(net. rptools.maptool.model.GUID, net.rptools.maptool.model.GUID, net.rptools.maptool.model.ExposedAreaMetaData)
   */
  public void updateExposedAreaMeta(
      GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta) {
    forwardToClients();
  }

  public void clearExposedArea(GUID zoneGUID) {
    Zone zone = MapTool.getCampaign().getZone(zoneGUID);
    zone.clearExposedArea();
    forwardToAllClients();

    // same as forwardToClients?
    // server.getConnection().broadcastCallMethod(
    // ClientCommand.COMMAND.clearExposedArea.name(),
    // RPCContext.getCurrent().parameters);
  }

  ////
  // CONTEXT
  private static class RPCContext {
    private static ThreadLocal<RPCContext> threadLocal = new ThreadLocal<RPCContext>();

    public String id;
    public String method;
    public Object[] parameters;

    public RPCContext(String id, String method, Object[] parameters) {
      this.id = id;
      this.method = method;
      this.parameters = parameters;
    }

    public static boolean hasCurrent() {
      return threadLocal.get() != null;
    }

    public static RPCContext getCurrent() {
      return threadLocal.get();
    }

    public static void setCurrent(RPCContext context) {
      threadLocal.set(context);
    }

    ////
    // Convenience methods
    public GUID getGUID(int index) {
      return (GUID) parameters[index];
    }

    public Integer getInt(int index) {
      return (Integer) parameters[index];
    }

    public Double getDouble(int index) {
      return (Double) parameters[index];
    }

    public Object get(int index) {
      return parameters[index];
    }

    public String getString(int index) {
      return (String) parameters[index];
    }

    public Boolean getBool(int index) {
      return (Boolean) parameters[index];
    }

    public Object[] getObjArray(int index) {
      return (Object[]) parameters[index];
    }
  }
}
