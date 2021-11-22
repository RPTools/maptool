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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.awt.geom.Area;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import net.rptools.clientserver.hessian.AbstractMethodHandler;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.ClientCommand;
import net.rptools.maptool.client.ClientMethodHandler;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ServerCommandClientImpl;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.transfer.AssetProducer;
import org.apache.log4j.Logger;
import org.apache.tika.utils.ExceptionUtils;

/**
 * This class is used by the server host to receive client commands sent through {@link
 * ServerCommandClientImpl ServerCommandClientImpl}. Once the command is received, this will update
 * the server data, before forwarding the command to the clients. Clients will then handle the
 * command through {@link ClientMethodHandler ClientMethodHandler}. Updating the server itself is
 * important as new client receive the server's campaign data when connecting.
 *
 * @author drice *
 */
public class ServerMethodHandler extends AbstractMethodHandler {
  private final MapToolServer server;
  private final Object MUTEX = new Object();
  private static final Logger log = Logger.getLogger(ServerMethodHandler.class);

  public ServerMethodHandler(MapToolServer server) {
    this.server = server;
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var msg = Message.parseFrom(message);
      var msgType = msg.getMessageTypeCase();
      log.info(id + " :got: " + msgType);

      switch (msgType) {
        case ADD_TOPOLOGY_MSG -> {
          handle(msg.getAddTopologyMsg());
          sendToClients(id, msg);
        }
        case BRING_TOKENS_TO_FRONT_MSG -> handle(msg.getBringTokensToFrontMsg());
        case BOOT_PLAYER_MSG -> {
          handle(msg.getBootPlayerMsg());
          sendToClients(id, msg);
        }
        case CHANGE_ZONE_DISPLAY_NAME_MSG -> handle(msg.getChangeZoneDisplayNameMsg(), msg);
        case CLEAR_ALL_DRAWINGS_MSG -> {
          handle(msg.getClearAllDrawingsMsg());
          sendToAllClients(msg);
        }
        case CLEAR_EXPOSED_AREA_MSG -> {
          handle(msg.getClearExposedAreaMsg());
          sendToClients(id, msg);
        }
        case DRAW_MSG -> {
          sendToAllClients(msg);
          handle(msg.getDrawMsg());
        }
        case EDIT_TOKEN_MSG -> {
          handle(id, msg.getEditTokenMsg());
          sendToClients(id, msg);
        }
        case ENFORCE_NOTIFICATION_MSG -> sendToClients(id, msg);
        case ENFORCE_ZONE_MSG -> sendToClients(id, msg);
        case ENFORCE_ZONE_VIEW_MSG -> sendToClients(id, msg);
        case EXEC_LINK_MSG -> sendToClients(id, msg);
        case EXPOSE_FOW_MSG -> {
          handle(msg.getExposeFowMsg());
          sendToClients(id, msg);
        }
        case EXEC_FUNCTION_MSG -> sendToClients(id, msg);
        case EXPOSE_PC_AREA_MSG -> {
          handle(msg.getExposePcAreaMsg());
          sendToAllClients(msg);
        }
        case GET_ASSET_MSG -> handle(id, msg.getGetAssetMsg());
        case GET_ZONE_MSG -> handle(id, msg.getGetZoneMsg());
        case HEARTBEAT_MSG -> {
          /* nothing yet */
        }
        case HIDE_FOW_MSG -> {
          handle(msg.getHideFowMsg());
          sendToAllClients(msg);
        }
        case HIDE_POINTER_MSG -> sendToAllClients(msg);
        case MESSAGE_MSG -> sendToClients(id, msg);
        case MOVE_POINTER_MSG -> sendToAllClients(msg);
        case PUT_ASSET_MSG -> handle(msg.getPutAssetMsg());
        case PUT_LABEL_MSG -> {
          handle(msg.getPutLabelMsg());
          sendToClients(id, msg);
        }
        case PUT_TOKEN_MSG -> {
          handle(id, msg.getPutTokenMsg());
          sendToClients(id, msg);
        }
        case PUT_ZONE_MSG -> {
          handle(msg.getPutZoneMsg());
          sendToClients(id, msg);
        }
        case REMOVE_ASSET_MSG -> {
          handle(msg.getRemoveAssetMsg());
        }
        case REMOVE_LABEL_MSG -> {
          handle(msg.getRemoveLabelMsg());
          sendToAllClients(msg);
        }
        case REMOVE_TOKEN_MSG -> {
          handle(msg.getRemoveTokenMsg());
          sendToClients(id, msg);
        }
        case REMOVE_TOKENS_MSG -> {
          handle(msg.getRemoveTokensMsg());
          sendToClients(id, msg);
        }
        case REMOVE_TOPOLOGY_MSG -> {
          handle(msg.getRemoveTopologyMsg());
          sendToClients(id, msg);
        }
        case REMOVE_ZONE_MSG -> {
          handle(msg.getRemoveZoneMsg());
          sendToClients(id, msg);
        }
        case RENAME_ZONE_MSG -> {
          handle(msg.getRenameZoneMsg());
          sendToAllClients(msg);
        }
        case SET_BOARD_MSG -> sendToClients(id, msg);
        case RESTORE_ZONE_VIEW_MSG -> sendToClients(id, msg);
        case SEND_TOKENS_TO_BACK_MSG -> handle(msg.getSendTokensToBackMsg());
        case SET_CAMPAIGN_MSG -> {
          handle(msg.getSetCampaignMsg());
          sendToClients(id, msg);
        }
        default -> log.warn(msgType + "not handled.");
      }

    } catch (InvalidProtocolBufferException e) {
      super.handleMessage(id, message);
    } catch (Exception e) {
      log.error(ExceptionUtils.getStackTrace(e));
      MapTool.showError(ExceptionUtils.getStackTrace(e));
    }
  }

  private void handle(SetCampaignMsg msg) {
    server.setCampaign(Campaign.fromDto(msg.getCampaign()));
  }

  private void handle(SendTokensToBackMsg msg) {
    var zoneGuid = GUID.valueOf(msg.getZoneGuid());
    var tokens = msg.getTokenGuidsList().stream().map(t -> GUID.valueOf(t)).collect(Collectors.toSet());
    sendTokensToBack(zoneGuid, tokens);
  }

  private void handle(RenameZoneMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    var name = msg.getName();
    Zone zone = server.getCampaign().getZone(zoneGUID);
    if (zone != null) {
      zone.setName(name);
    }
  }

  private void handle(RemoveZoneMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    server.getCampaign().removeZone(zoneGUID);
  }

  private void handle(RemoveTopologyMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    var area = Mapper.map(msg.getArea());
    var topologyType = Zone.TopologyType.valueOf(msg.getType().name());
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.removeTopology(area, topologyType);
  }

  private void handle(RemoveTokensMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    var tokenGUIDs =
        msg.getTokenGuidList().stream().map(t -> GUID.valueOf(t)).collect(Collectors.toList());
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.removeTokens(tokenGUIDs); // remove server tokens
  }

  private void handle(RemoveTokenMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    var tokenGUID = GUID.valueOf(msg.getTokenGuid());
    var zone = server.getCampaign().getZone(zoneGUID);
    zone.removeToken(tokenGUID); // remove server tokens
  }

  private void handle(RemoveLabelMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    var labelGUID = GUID.valueOf(msg.getLabelGuid());
    var zone = server.getCampaign().getZone(zoneGUID);
    zone.removeLabel(labelGUID);
  }

  private void handle(RemoveAssetMsg msg) {
    AssetManager.removeAsset(new MD5Key(msg.getAssetId()));
  }

  private void handle(PutZoneMsg msg) {
    server.getCampaign().putZone(Zone.fromDto(msg.getZone()));
  }

  private void handle(PutLabelMsg msg) {
    Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
    zone.putLabel(Label.fromDto(msg.getLabel()));
  }

  private void handle(PutAssetMsg msg) {
    AssetManager.putAsset(Asset.fromDto(msg.getAsset()));
  }

  private void handle(HideFowMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    var area = Mapper.map(msg.getArea());
    var selectedToks =
        msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toSet());

    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.hideArea(area, selectedToks);
  }

  private void handle(String id, GetZoneMsg msg) {
    getZone(id, GUID.valueOf(msg.getZoneGuid()));
  }

  private void handle(String id, GetAssetMsg msg) {
    getAsset(id, new MD5Key(msg.getAssetId()));
  }

  private void handle(ExposePcAreaMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
    FogUtil.exposePCArea(renderer);
  }

  private void handle(ExposeFowMsg msg) {
    var zoneGUID = GUID.valueOf(msg.getZoneGuid());
    Zone zone = server.getCampaign().getZone(zoneGUID);
    Area area = Mapper.map(msg.getArea());
    var selectedToks =
        msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toSet());
    zone.exposeArea(area, selectedToks);
  }

  private void handle(String clientId, PutTokenMsg putTokenMsg) {
    var zoneGUID = GUID.valueOf(putTokenMsg.getZoneGuid());
    var token = Token.fromDto(putTokenMsg.getToken());
    putToken(clientId, zoneGUID, token);
  }

  private void handle(String clientId, EditTokenMsg editTokenMsg) {
    var zoneGUID = GUID.valueOf(editTokenMsg.getZoneGuid());
    var token = Token.fromDto(editTokenMsg.getToken());
    putToken(clientId, zoneGUID, token);
  }

  private void handle(DrawMsg drawMsg) {
    var zoneGuid = GUID.valueOf(drawMsg.getZoneGuid());
    var pen = Pen.fromDto(drawMsg.getPen());
    var drawable = Drawable.fromDto(drawMsg.getDrawable());
    Zone zone = server.getCampaign().getZone(zoneGuid);
    zone.addDrawable(new DrawnElement(drawable, pen));
  }

  private void handle(ClearExposedAreaMsg clearExposedAreaMsg) {
    var zoneGUID = GUID.valueOf(clearExposedAreaMsg.getZoneGuid());
    var globalOnly = clearExposedAreaMsg.getGlobalOnly();
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.clearExposedArea(globalOnly);
  }

  private void handle(ClearAllDrawingsMsg clearAllDrawingsMsg) {
    var zoneGUID = GUID.valueOf(clearAllDrawingsMsg.getZoneGuid());
    var layer = Zone.Layer.valueOf(clearAllDrawingsMsg.getLayer());
    Zone zone = server.getCampaign().getZone(zoneGUID);
    List<DrawnElement> list = zone.getDrawnElements(layer);
    zone.clearDrawables(list); // FJE Empties the DrawableUndoManager and empties the list
  }

  private void handle(ChangeZoneDisplayNameMsg changeZoneDisplayNameMsg, Message msg) {
    var zoneGUID = GUID.valueOf(changeZoneDisplayNameMsg.getZoneGuid());
    var name = changeZoneDisplayNameMsg.getName();

    Zone zone = server.getCampaign().getZone(zoneGUID);
    if (zone != null) {
      zone.setPlayerAlias(name);
      sendToAllClients(msg);
    }
  }

  private void handle(BringTokensToFrontMsg bringTokensToFrontMsg) {
    var zoneGuid = GUID.valueOf(bringTokensToFrontMsg.getZoneGuid());
    var tokenSet =
        bringTokensToFrontMsg.getTokenGuidsList().stream()
            .map(str -> GUID.valueOf(str))
            .collect(Collectors.toSet());
    bringTokensToFront(zoneGuid, tokenSet);
  }

  private void handle(AddTopologyMsg addTopologyMsg) {
    var zoneGUID = GUID.valueOf(addTopologyMsg.getZoneGuid());
    var area = Mapper.map(addTopologyMsg.getArea());
    var topologyType = Zone.TopologyType.valueOf(addTopologyMsg.getType().name());
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.addTopology(area, topologyType);
  }

  private void handle(BootPlayerMsg bootPlayerMsg) {
    // And just to be sure, remove them from the server
    server.releaseClientConnection(server.getConnectionId(bootPlayerMsg.getPlayerName()));
  }

  @SuppressWarnings("unchecked")
  public void handleMethod(String id, String method, Object... parameters) {
    ServerCommand.COMMAND cmd = Enum.valueOf(ServerCommand.COMMAND.class, method);

    log.debug("from " + id + " got " + method);

    try {
      RPCContext context = new RPCContext(id, method, parameters);
      RPCContext.setCurrent(context);
      switch (cmd) {
        case updateDrawing:
          updateDrawing(context.getGUID(0), (Pen) context.get(1), (DrawnElement) context.get(2));
          break;
        case setFoW:
          setFoW(context.getGUID(0), (Area) context.get(1), (Set<GUID>) context.get(2));
          break;
        case setLiveTypingLabel:
          setLiveTypingLabel(context.getString(0), context.getBool(1));
          break;
        case updateTokenProperty:
          Token.Update update = (Token.Update) context.parameters[2];
          updateTokenProperty(
              context.getGUID(0), context.getGUID(1), update, context.getObjArray(3));
          break;
        case setCampaignName:
          setCampaignName((String) context.get(0));
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
        case setServerPolicy:
          setServerPolicy((ServerPolicy) context.get(0));
          break;
        case updateCampaign:
          updateCampaign((CampaignProperties) context.get(0));
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
        case updateCampaignMacros:
          updateCampaignMacros((List<MacroButtonProperties>) context.get(0));
          break;
        case updateGmMacros:
          updateGmMacros((List<MacroButtonProperties>) context.get(0));
          break;
        case setTokenLocation:
          setTokenLocation(
              context.getGUID(0), context.getGUID(1), context.getInt(2), context.getInt(3));
          break;
        case updateExposedAreaMeta:
          updateExposedAreaMeta(
              context.getGUID(0), context.getGUID(1), (ExposedAreaMetaData) context.get(2));
          break;
        case addAddOnLibrary:
          addAddOnLibrary((List<TransferableAddOnLibrary>) context.get(0));
          break;
        case removeAddOnLibrary:
          removeAddOnLibrary((List<String>) context.get(0));
          break;
        case removeAllAddOnLibraries:
          removeAllAddOnLibraries();
          break;
        case updateDataStore:
          var storeBuilder = DataStoreDto.newBuilder();
          try {
            JsonFormat.parser()
                .merge(
                    new InputStreamReader(new ByteArrayInputStream((byte[]) parameters[0])),
                    storeBuilder);
            var dataStoreDto = storeBuilder.build();
            updateDataStore(dataStoreDto);
          } catch (IOException e) {
            log.error(I18N.getText("data.error.sendingUpdate"), e);
          }
          break;
        case updateDataNamespace:
          var namespaceBuilder = GameDataDto.newBuilder();
          try {
            JsonFormat.parser()
                .merge(
                    new InputStreamReader(new ByteArrayInputStream((byte[]) parameters[0])),
                    namespaceBuilder);
            var dataNamespaceDto = namespaceBuilder.build();
            updateDataNamespace(dataNamespaceDto);
          } catch (IOException e) {
            log.error(I18N.getText("data.error.sendingUpdate"), e);
          }
          break;
        case updateData:
          String type = (String) parameters[0];
          String namespace = (String) parameters[1];
          var dataBuilder = GameDataValueDto.newBuilder();
          try {
            JsonFormat.parser()
                .merge(
                    new InputStreamReader(new ByteArrayInputStream((byte[]) parameters[2])),
                    dataBuilder);
            var dataDto = dataBuilder.build();
            updateData((String) context.get(0), (String) context.get(1), dataDto);
          } catch (IOException e) {
            log.error(I18N.getText("data.error.sendingUpdate"), e);
          }
          break;
      }
    } finally {
      RPCContext.setCurrent(null);
    }
  }

  private void sendToClients(String excludedId, Message message) {
    server.getConnection().broadcastMessage(new String[] {excludedId}, message.toByteArray());
  }

  private void sendToAllClients(Message message) {
    server.getConnection().broadcastMessage(message.toByteArray());
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
        .broadcastCallMethod(RPCContext.getCurrent().method, RPCContext.getCurrent().parameters);
  }

  /**
   * Broadcast a method to a single client
   *
   * @param client the client to send the method to
   * @param method the method to send
   * @param parameters an array of parameters related to the method
   */
  private void broadcastToClient(String client, String method, Object... parameters) {
    server.getConnection().callMethod(client, method, parameters);
  }

  ////
  // SERVER COMMAND
  private void setVisionType(GUID zoneGUID, VisionType visionType) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setVisionType(visionType);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setUseVision.name(), RPCContext.getCurrent().parameters);
  }

  private void updateCampaign(CampaignProperties properties) {
    server.getCampaign().replaceCampaignProperties(properties);
    forwardToClients();
  }

  private void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet) {
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
      tokenList.sort(Zone.TOKEN_Z_ORDER_COMPARATOR);

      // Update
      int z = zone.getLargestZOrder() + 1;
      for (Token token : tokenList) {
        token.setZOrder(z++);
      }
      // Broadcast
      for (Token token : tokenList) {
        var putTokenMsg =
            PutTokenMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setToken(token.toDto());
        sendToAllClients(Message.newBuilder().setPutTokenMsg(putTokenMsg).build());
      }
      zone.sortZOrder(); // update new ZOrder on server zone
    }
  }

  private void updateDrawing(GUID zoneGUID, Pen pen, DrawnElement drawnElement) {
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.updateDrawing.name(), RPCContext.getCurrent().parameters);
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.updateDrawable(drawnElement, pen);
  }

  public void restoreZoneView(GUID zoneGUID) {
    forwardToClients();
  }

  private void getAsset(String id, MD5Key assetID) {
    if (assetID == null) {
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

    } catch (IllegalArgumentException iae) {
      // Sending an empty asset will cause a failure of the image to load on the client side,
      // showing a broken
      // image instead of blowing up
      Asset asset = Asset.createBrokenImageAsset(assetID);
      var msg = PutAssetMsg.newBuilder().setAsset(asset.toDto());
      server.getConnection().sendMessage(id, Message.newBuilder().setPutAssetMsg(msg).build());
    }
  }

  private void getZone(String id, GUID zoneGUID) {
    var zone = server.getCampaign().getZone(zoneGUID);
    var msg = PutZoneMsg.newBuilder().setZone(zone.toDto());
    server.getConnection().sendMessage(id, Message.newBuilder().setPutZoneMsg(msg).build());
  }

  private void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setFogArea(area, selectedToks);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setFoW.name(), RPCContext.getCurrent().parameters);
  }

  private void updateInitiative(InitiativeList list, Boolean ownerPermission) {
    if (list != null) {
      if (list.getZone() == null) return;
      Zone zone = server.getCampaign().getZone(list.getZone().getId());
      zone.setInitiativeList(list);
    } else if (ownerPermission != null) {
      MapTool.getFrame().getInitiativePanel().setOwnerPermissions(ownerPermission);
    }
    forwardToAllClients();
  }

  private void updateTokenInitiative(
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

  private void putToken(String clientId, GUID zoneGUID, Token token) {
    Zone zone = server.getCampaign().getZone(zoneGUID);

    int zOrder = 0;
    boolean newToken = zone.getToken(token.getId()) == null;
    synchronized (MUTEX) {
      // Set z-order for new tokens
      if (newToken) {
        zOrder = zone.getLargestZOrder() + 1;
        token.setZOrder(zOrder);
      }
      zone.putToken(token);
    }
    if (newToken) {
      // don't send whole token back to sender, instead just send new ZOrder
      Object[] parameters = {
        zoneGUID, token.getId(), Token.Update.setZOrder, new Object[] {zOrder}
      };
      broadcastToClient(clientId, ClientCommand.COMMAND.updateTokenProperty.name(), parameters);
    }
  }

  private void updateTokenProperty(
      GUID zoneGUID, GUID tokenGUID, Token.Update update, Object[] parameters) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    Token token = zone.getToken(tokenGUID);
    token.updateProperty(zone, update, parameters); // update server version of token

    forwardToClients();
  }

  private void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet) {
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
      tokenList.sort(Zone.TOKEN_Z_ORDER_COMPARATOR);

      // Update
      int z = zone.getSmallestZOrder() - 1;
      for (Token token : tokenList) {
        token.setZOrder(z--);
      }
      // Broadcast
      for (Token token : tokenList) {
        var putTokenMsg =
            PutTokenMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setToken(token.toDto());
        sendToAllClients(Message.newBuilder().setPutTokenMsg(putTokenMsg).build());
      }
      zone.sortZOrder(); // update new ZOrder on server zone
    }
  }

  private void setCampaignName(String name) {
    server.getCampaign().setName(name);
    forwardToClients();
  }

  private void setZoneGridSize(GUID zoneGUID, int offsetX, int offsetY, int size, int color) {
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

  private void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setHasFog(hasFog);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setZoneHasFoW.name(), RPCContext.getCurrent().parameters);
  }

  private void setZoneVisibility(GUID zoneGUID, boolean visible) {
    server.getCampaign().getZone(zoneGUID).setVisible(visible);
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.setZoneVisibility.name(), RPCContext.getCurrent().parameters);
  }

  private void showPointer(String player, Pointer pointer) {
    server
        .getConnection()
        .broadcastCallMethod(
            ClientCommand.COMMAND.showPointer.name(), RPCContext.getCurrent().parameters);
  }

  private void setLiveTypingLabel(String label, boolean show) {
    forwardToClients();
  }

  private void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
    forwardToClients();
  }

  private void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
    forwardToClients();
  }

  private void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp) {
    forwardToClients();
  }

  private void undoDraw(GUID zoneGUID, GUID drawableGUID) {
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

  private void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
    forwardToClients();
  }

  private void setTokenLocation(GUID zoneGUID, GUID tokenGUID, int x, int y) {
    forwardToClients();
  }

  private void setServerPolicy(ServerPolicy policy) {
    server.updateServerPolicy(policy); // updates the server policy, fixes #1648
    forwardToClients();
    MapTool.getFrame().getToolbox().updateTools();
  }

  private void updateCampaignMacros(List<MacroButtonProperties> properties) {
    ArrayList campaignMacros = new ArrayList<MacroButtonProperties>(properties);
    MapTool.getCampaign().setMacroButtonPropertiesArray(campaignMacros);
    server.getCampaign().setMacroButtonPropertiesArray(campaignMacros);
    forwardToClients();
  }

  private void updateGmMacros(List<MacroButtonProperties> properties) {
    ArrayList campaignMacros = new ArrayList<MacroButtonProperties>(properties);
    MapTool.getCampaign().setGmMacroButtonPropertiesArray(campaignMacros);
    server.getCampaign().setGmMacroButtonPropertiesArray(campaignMacros);
    forwardToClients();
  }

  /**
   * Update the server exposed area meta data, and forward the change to the clients
   *
   * @param zoneGUID the zone GUID of the map
   * @param tokenExposedAreaGUID the GUID of the token to update the exposed meta data
   * @param meta the exposed area meta data
   * @see
   *     net.rptools.maptool.server.ServerCommand#updateExposedAreaMeta(net.rptools.maptool.model.GUID,
   *     net.rptools.maptool.model.GUID, net.rptools.maptool.model.ExposedAreaMetaData)
   */
  private void updateExposedAreaMeta(
      GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta) {
    Zone zone = server.getCampaign().getZone(zoneGUID);
    zone.setExposedAreaMetaData(tokenExposedAreaGUID, meta); // update the server
    forwardToClients();
  }

  private void addAddOnLibrary(List<TransferableAddOnLibrary> addOnLibraries) {
    forwardToClients();
  }

  private void removeAddOnLibrary(List<String> namespaces) {
    forwardToClients();
  }

  private void removeAllAddOnLibraries() {
    forwardToClients();
  }

  private void updateDataStore(DataStoreDto dataStore) {
    forwardToClients();
  }

  private void updateDataNamespace(GameDataDto gameData) {
    forwardToClients();
  }

  private void updateData(String type, String namespace, GameDataValueDto gameData) {
    forwardToClients();
  }

  private void removeDataStore() {
    forwardToClients();
  }

  private void removeDataNamespace(String type, String namespace) {
    forwardToClients();
  }

  private void removeData(String type, String namespace, String name) {
    forwardToClients();
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

    public List<GUID> getGUIDs(int index) {
      return (List<GUID>) parameters[index];
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
