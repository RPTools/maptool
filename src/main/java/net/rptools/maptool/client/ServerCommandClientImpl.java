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

import com.google.protobuf.BoolValue;
import com.google.protobuf.StringValue;
import java.awt.geom.Area;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.functions.ExecFunction;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.ServerCommand;
import net.rptools.maptool.server.ServerMessageHandler;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.server.proto.drawing.IntPointDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used by a client to send commands to the server. The methods of this class are
 * typically accessed through MapTool.serverCommand(). Once sent, the commands are then received by
 * the {@link ServerMessageHandler ServerMessageHandler}
 */
public class ServerCommandClientImpl implements ServerCommand {

  private final TimedEventQueue movementUpdateQueue = new TimedEventQueue(100);
  private final LinkedBlockingQueue<MD5Key> assetRetrieveQueue = new LinkedBlockingQueue<MD5Key>();
  private static final Logger log = LogManager.getLogger(ServerCommandClientImpl.class);

  public ServerCommandClientImpl() {
    movementUpdateQueue.start();
    // new AssetRetrievalThread().start();
  }

  public void heartbeat(String data) {
    var msg = HeartbeatMsg.newBuilder().setData(data);
    makeServerCall(Message.newBuilder().setHeartbeatMsg(msg).build());
  }

  public void movePointer(String player, int x, int y) {
    var msg = MovePointerMsg.newBuilder().setPlayer(player).setX(x).setY(y);
    makeServerCall(Message.newBuilder().setMovePointerMsg(msg).build());
  }

  public void bootPlayer(String player) {
    var msg = BootPlayerMsg.newBuilder().setPlayerName(player);
    makeServerCall(Message.newBuilder().setBootPlayerMsg(msg).build());
  }

  public void setCampaign(Campaign campaign) {
    var msg = SetCampaignMsg.newBuilder();
    try {
      campaign.setBeingSerialized(true);
      msg.setCampaign(campaign.toDto());
    } finally {
      campaign.setBeingSerialized(false);
    }
    makeServerCall(Message.newBuilder().setSetCampaignMsg(msg).build());
  }

  public void setCampaignName(String name) {
    MapTool.getCampaign().setName(name);
    MapTool.getFrame().setTitle();
    var msg = SetCampaignNameMsg.newBuilder().setName(name);
    makeServerCall(Message.newBuilder().setSetCampaignNameMsg(msg).build());
  }

  public void setVisionType(GUID zoneGUID, VisionType visionType) {
    var msg =
        SetVisionTypeMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setVision(ZoneDto.VisionTypeDto.valueOf(visionType.name()));
    makeServerCall(Message.newBuilder().setSetVisionTypeMsg(msg).build());
  }

  public void updateCampaign(CampaignProperties properties) {
    var msg = UpdateCampaignMsg.newBuilder().setProperties(properties.toDto());
    makeServerCall(Message.newBuilder().setUpdateCampaignMsg(msg).build());
  }

  public void getZone(GUID zoneGUID) {
    var msg = GetZoneMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setGetZoneMsg(msg).build());
  }

  public void putZone(Zone zone) {
    var msg = PutZoneMsg.newBuilder().setZone(zone.toDto());
    makeServerCall(Message.newBuilder().setPutZoneMsg(msg).build());
  }

  public void removeZone(GUID zoneGUID) {
    var msg = RemoveZoneMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setRemoveZoneMsg(msg).build());
  }

  public void renameZone(GUID zoneGUID, String name) {
    var msg = RenameZoneMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setName(name);
    makeServerCall(Message.newBuilder().setRenameZoneMsg(msg).build());
  }

  public void changeZoneDispName(GUID zoneGUID, String name) {
    var msg = ChangeZoneDisplayNameMsg.newBuilder().setName(name).setZoneGuid(zoneGUID.toString());

    makeServerCall(Message.newBuilder().setChangeZoneDisplayNameMsg(msg).build());
  }

  public void putAsset(Asset asset) {
    var msg = PutAssetMsg.newBuilder().setAsset(asset.toDto());
    makeServerCall(Message.newBuilder().setPutAssetMsg(msg).build());
  }

  public void getAsset(MD5Key assetID) {
    var msg = GetAssetMsg.newBuilder().setAssetId(assetID.toString());
    makeServerCall(Message.newBuilder().setGetAssetMsg(msg).build());
  }

  public void removeAsset(MD5Key assetID) {
    var msg = RemoveAssetMsg.newBuilder().setAssetId(assetID.toString());
    makeServerCall(Message.newBuilder().setRemoveAssetMsg(msg).build());
  }

  public void enforceZoneView(GUID zoneGUID, int x, int y, double scale, int width, int height) {
    var msg =
        EnforceZoneViewMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setX(x)
            .setY(y)
            .setScale(scale)
            .setGmWidth(width)
            .setGmHeight(height);
    makeServerCall(Message.newBuilder().setEnforceZoneViewMsg(msg).build());
  }

  public void restoreZoneView(GUID zoneGUID) {
    var msg = RestoreZoneViewMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setRestoreZoneViewMsg(msg).build());
  }

  public void editToken(GUID zoneGUID, Token token) {
    MapTool.getCampaign().getZone(zoneGUID).editToken(token);
    var msg = EditTokenMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setToken(token.toDto());
    makeServerCall(Message.newBuilder().setEditTokenMsg(msg).build());
  }

  public void putToken(GUID zoneGUID, Token token) {
    // Hack to generate zone event. All functions that update tokens call this method
    // after changing the token. But they don't tell the zone about it so classes
    // waiting for the zone change event don't get it.
    MapTool.getCampaign().getZone(zoneGUID).putToken(token);
    var msg = PutTokenMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setToken(token.toDto());
    makeServerCall(Message.newBuilder().setPutTokenMsg(msg).build());
  }

  @Override
  public void removeToken(GUID zoneGUID, GUID tokenGUID) {
    // delete local token immediately
    MapTool.getCampaign().getZone(zoneGUID).removeToken(tokenGUID);
    var msg =
        RemoveTokenMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setTokenGuid(tokenGUID.toString());
    makeServerCall(Message.newBuilder().setRemoveTokenMsg(msg).build());
  }

  @Override
  public void removeTokens(GUID zoneGUID, List<GUID> tokenGUIDs) {
    // delete local tokens immediately
    MapTool.getCampaign().getZone(zoneGUID).removeTokens(tokenGUIDs);
    var msg = RemoveTokensMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    msg.addAllTokenGuid(tokenGUIDs.stream().map(t -> t.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setRemoveTokensMsg(msg).build());
  }

  /**
   * Send the command updateTokenProperty to the server. The method doesn't send the whole Token,
   * greatly reducing lag.
   *
   * @param zoneGUID the GUID of the zone the token is on
   * @param tokenGUID the GUID of the token
   * @param update the type of token update
   * @param parameters an array of parameters
   */
  public void updateTokenProperty(
      GUID zoneGUID, GUID tokenGUID, Token.Update update, List<TokenPropertyValueDto> parameters) {
    var msg =
        UpdateTokenPropertyMsg.newBuilder()
            .setTokenGuid(tokenGUID.toString())
            .setZoneGuid(zoneGUID.toString())
            .setProperty(TokenUpdateDto.valueOf(update.name()))
            .addAllValues(parameters);
    makeServerCall(Message.newBuilder().setUpdateTokenPropertyMsg(msg).build());
  }

  /**
   * Simplifies the arguments for the method above.
   *
   * @param token the token to be updated
   * @param update the type of token update
   * @param parameters an array of parameters
   */
  public void updateTokenProperty(
      Token token, Token.Update update, TokenPropertyValueDto... parameters) {
    Zone zone = token.getZoneRenderer().getZone();
    GUID tokenGUID = token.getId();
    GUID zoneGUID = zone.getId();

    var parameterList = Arrays.stream(parameters).toList();
    token.updateProperty(zone, update, parameterList); // update locally right away
    updateTokenProperty(zoneGUID, tokenGUID, update, parameterList);
  }

  public void putLabel(GUID zoneGUID, Label label) {
    var msg = PutLabelMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setLabel(label.toDto());
    makeServerCall(Message.newBuilder().setPutLabelMsg(msg).build());
  }

  public void removeLabel(GUID zoneGUID, GUID labelGUID) {
    var msg =
        RemoveLabelMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setLabelGuid(labelGUID.toString());
    makeServerCall(Message.newBuilder().setRemoveLabelMsg(msg).build());
  }

  public void draw(GUID zoneGUID, Pen pen, Drawable drawable) {
    var msg =
        DrawMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setPen(pen.toDto())
            .setDrawable(drawable.toDto());

    makeServerCall(Message.newBuilder().setDrawMsg(msg).build());
  }

  public void updateDrawing(GUID zoneGUID, Pen pen, DrawnElement drawnElement) {
    var msg =
        UpdateDrawingMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setPen(pen.toDto())
            .setDrawing(drawnElement.toDto());
    makeServerCall(Message.newBuilder().setUpdateDrawingMsg(msg).build());
  }

  public void clearAllDrawings(GUID zoneGUID, Zone.Layer layer) {
    var msg =
        ClearAllDrawingsMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setLayer(layer.name());

    makeServerCall(Message.newBuilder().setClearAllDrawingsMsg(msg).build());
  }

  public void undoDraw(GUID zoneGUID, GUID drawableGUID) {
    var msg =
        UndoDrawMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setDrawableGuid(drawableGUID.toString());
    makeServerCall(Message.newBuilder().setUndoDrawMsg(msg).build());
  }

  public void setZoneGridSize(GUID zoneGUID, int xOffset, int yOffset, int size, int color) {
    var msg =
        SetZoneGridSizeMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setXOffset(xOffset)
            .setYOffset(yOffset)
            .setSize(size)
            .setColor(color);
    makeServerCall(Message.newBuilder().setSetZoneGridSizeMsg(msg).build());
  }

  public void setZoneVisibility(GUID zoneGUID, boolean visible) {
    var msg =
        SetZoneVisibilityMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setIsVisible(visible);
    makeServerCall(Message.newBuilder().setSetZoneVisibilityMsg(msg).build());
  }

  public void message(TextMessage message) {
    var msg = MessageMsg.newBuilder().setMessage(message.toDto());
    makeServerCall(Message.newBuilder().setMessageMsg(msg).build());
  }

  @Override
  public void execFunction(String target, String source, String functionName, List<Object> args) {
    // Execute locally right away
    ExecFunction.receiveExecFunction(target, source, functionName, args);

    if (ExecFunction.isMessageGlobal(target, source)) {
      var msg =
          ExecFunctionMsg.newBuilder()
              .setTarget(target)
              .setSource(source)
              .setFunctionName(functionName)
              .addAllArgument(Mapper.mapToScriptTypeDto(args));

      makeServerCall(Message.newBuilder().setExecFunctionMsg(msg).build());
    }
  }

  @Override
  public void execLink(String link, String target, String source) {
    MacroLinkFunction.receiveExecLink(link, target, source); // receive locally right away

    if (ExecFunction.isMessageGlobal(target, source)) {
      var msg = ExecLinkMsg.newBuilder().setTarget(target).setSource(source).setLink(link);
      makeServerCall(Message.newBuilder().setExecLinkMsg(msg).build());
    }
  }

  public void showPointer(String player, Pointer pointer) {
    var msg = ShowPointerMsg.newBuilder().setPlayer(player).setPointer(pointer.toDto());
    makeServerCall(Message.newBuilder().setShowPointerMsg(msg).build());
  }

  public void hidePointer(String player) {
    var msg = HidePointerMsg.newBuilder().setPlayer(player);
    makeServerCall(Message.newBuilder().setHidePointerMsg(msg).build());
  }

  public void setLiveTypingLabel(String label, boolean show) {
    var msg = SetLiveTypingLabelMsg.newBuilder().setPlayerName(label).setTyping(show);
    makeServerCall(Message.newBuilder().setSetLiveTypingLabelMsg(msg).build());
  }

  public void enforceNotification(Boolean enforce) {
    var msg = EnforceNotificationMsg.newBuilder().setEnforce(enforce);
    makeServerCall(Message.newBuilder().setEnforceNotificationMsg(msg).build());
  }

  public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
    var msg =
        StartTokenMoveMsg.newBuilder()
            .setPlayerId(playerId)
            .setZoneGuid(zoneGUID.toString())
            .setKeyTokenId(tokenGUID.toString())
            .addAllSelectedTokens(
                tokenList.stream().map(GUID::toString).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setStartTokenMoveMsg(msg).build());
  }

  public void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
    movementUpdateQueue.flush();
    var msg =
        StopTokenMoveMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setKeyTokenId(tokenGUID.toString());
    makeServerCall(Message.newBuilder().setStopTokenMoveMsg(msg).build());
  }

  public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
    var msg =
        UpdateTokenMoveMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setKeyTokenId(tokenGUID.toString())
            .setPoint(IntPointDto.newBuilder().setX(x).setY(y).build());
    movementUpdateQueue.enqueue(Message.newBuilder().setUpdateTokenMoveMsg(msg).build());
  }

  public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp) {
    movementUpdateQueue.flush();
    var msg =
        ToggleTokenMoveWaypointMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setKeyTokenId(tokenGUID.toString())
            .setPoint(cp.toDto());
    makeServerCall(Message.newBuilder().setToggleTokenMoveWaypointMsg(msg).build());
  }

  public void addTopology(GUID zoneGUID, Area area, Zone.TopologyType topologyType) {
    var msg =
        AddTopologyMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setType(TopologyTypeDto.valueOf(topologyType.name()))
            .setArea(Mapper.map(area));

    makeServerCall(Message.newBuilder().setAddTopologyMsg(msg).build());
  }

  public void removeTopology(GUID zoneGUID, Area area, Zone.TopologyType topologyType) {
    var msg =
        RemoveTopologyMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setArea(Mapper.map(area))
            .setType(TopologyTypeDto.valueOf(topologyType.name()));
    makeServerCall(Message.newBuilder().setRemoveTopologyMsg(msg).build());
  }

  public void exposePCArea(GUID zoneGUID) {
    var msg = ExposePcAreaMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setExposePcAreaMsg(msg).build());
  }

  public void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    // Expose locally right away.
    MapTool.getCampaign().getZone(zoneGUID).exposeArea(area, selectedToks);
    var msg = ExposeFowMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setArea(Mapper.map(area));
    msg.addAllTokenGuid(selectedToks.stream().map(g -> g.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setExposeFowMsg(msg).build());
  }

  public void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    var msg =
        SetFowMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setArea(Mapper.map(area))
            .addAllSelectedTokens(
                selectedToks.stream().map(t -> t.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setSetFowMsg(msg).build());
  }

  public void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    var msg = HideFowMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setArea(Mapper.map(area));
    msg.addAllTokenGuid(selectedToks.stream().map(g -> g.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setHideFowMsg(msg).build());
  }

  public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
    var msg = SetZoneHasFowMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setHasFow(hasFog);
    makeServerCall(Message.newBuilder().setSetZoneHasFowMsg(msg).build());
  }

  public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenList) {
    var msg = BringTokensToFrontMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    msg.addAllTokenGuids(tokenList.stream().map(g -> g.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setBringTokensToFrontMsg(msg).build());
  }

  public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenList) {
    var msg = SendTokensToBackMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    msg.addAllTokenGuids(tokenList.stream().map(g -> g.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setSendTokensToBackMsg(msg).build());
  }

  public void enforceZone(GUID zoneGUID) {
    var msg = EnforceZoneMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setEnforceZoneMsg(msg).build());
  }

  public void setServerPolicy(ServerPolicy policy) {
    var msg = SetServerPolicyMsg.newBuilder().setPolicy(policy.toDto());
    makeServerCall(Message.newBuilder().setSetServerPolicyMsg(msg).build());
  }

  public void updateInitiative(InitiativeList list, Boolean ownerPermission) {
    var msg = UpdateInitiativeMsg.newBuilder();
    if (list != null) msg.setList(list.toDto());
    if (ownerPermission != null) msg.setOwnerPermission(BoolValue.of(ownerPermission));
    makeServerCall(Message.newBuilder().setUpdateInitiativeMsg(msg).build());
  }

  public void updateTokenInitiative(
      GUID zone, GUID token, Boolean holding, String state, Integer index) {
    var msg =
        UpdateTokenInitiativeMsg.newBuilder()
            .setZoneGuid(zone.toString())
            .setTokenGuid(token.toString())
            .setIsHolding(holding)
            .setIndex(index);
    if (state != null) {
      msg.setState(StringValue.of(state));
    }
    makeServerCall(Message.newBuilder().setUpdateTokenInitiativeMsg(msg).build());
  }

  public void updateCampaignMacros(List<MacroButtonProperties> properties) {
    var msg =
        UpdateCampaignMacrosMsg.newBuilder()
            .addAllMacros(
                properties.stream().map(MacroButtonProperties::toDto).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setUpdateCampaignMacrosMsg(msg).build());
  }

  public void updateGmMacros(List<MacroButtonProperties> properties) {
    var msg =
        UpdateGmMacrosMsg.newBuilder()
            .addAllMacros(
                properties.stream().map(MacroButtonProperties::toDto).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setUpdateGmMacrosMsg(msg).build());
  }

  /**
   * Send the message to server to clear the exposed area of a map
   *
   * @param zoneGUID the GUID of the zone
   * @param globalOnly should all token exposed areas be cleared?
   */
  public void clearExposedArea(GUID zoneGUID, boolean globalOnly) {
    var msg =
        ClearExposedAreaMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setGlobalOnly(globalOnly);

    makeServerCall(Message.newBuilder().setClearExposedAreaMsg(msg).build());
  }

  private static void makeServerCall(Message msg) {
    if (MapTool.getConnection() != null) {
      MapTool.getConnection().sendMessage(msg);
    }
  }

  public void setBoard(GUID zoneGUID, MD5Key mapAssetId, int x, int y) {
    // First, ensure that the possibly new map texture is available on the client
    // note: This may not be the optimal solution... can't tell from available documentation.
    // it may send a texture that is already sent
    // it might be better to do it in the background(?)
    // there seem to be other ways to upload textures (?) (e.g. in MapToolUtil)
    putAsset(AssetManager.getAsset(mapAssetId));
    // Second, tell the client to change the zone's board info
    var msg =
        SetBoardMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setAssetId(mapAssetId.toString())
            .setPoint(IntPointDto.newBuilder().setY(x).setY(y));
    makeServerCall(Message.newBuilder().setSetBoardMsg(msg).build());
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.server.ServerCommand#updateExposedAreaMeta(net. rptools.maptool.model.GUID, net.rptools.maptool.model.GUID, net.rptools.maptool.model.ExposedAreaMetaData)
   */
  public void updateExposedAreaMeta(
      GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta) {
    var msg =
        UpdateExposedAreaMetaMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setArea(Mapper.map(meta.getExposedAreaHistory()));
    if (tokenExposedAreaGUID != null) {
      msg.setTokenGuid(StringValue.of(tokenExposedAreaGUID.toString()));
    }
    makeServerCall(Message.newBuilder().setUpdateExposedAreaMetaMsg(msg).build());
  }

  @Override
  public void addAddOnLibrary(List<TransferableAddOnLibrary> addOnLibraries) {
    var msg =
        AddAddOnLibraryMsg.newBuilder()
            .addAllAddOns(
                addOnLibraries.stream()
                    .map(TransferableAddOnLibrary::toDto)
                    .collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setAddAddOnLibraryMsg(msg).build());
  }

  @Override
  public void removeAddOnLibrary(List<String> namespaces) {
    var msg = RemoveAddOnLibraryMsg.newBuilder().addAllNamespaces(namespaces);
    makeServerCall(Message.newBuilder().setRemoveAddOnLibraryMsg(msg).build());
  }

  @Override
  public void removeAllAddOnLibraries() {
    makeServerCall(
        Message.newBuilder()
            .setRemoveAllAddOnLibrariesMsg(RemoveAllAddOnLibrariesMsg.newBuilder())
            .build());
  }

  @Override
  public void updateDataStore(DataStoreDto dataStore) {
    var msg = UpdateDataStoreMsg.newBuilder().setStore(dataStore);
    makeServerCall(Message.newBuilder().setUpdateDataStoreMsg(msg).build());
  }

  @Override
  public void updateDataNamespace(GameDataDto gameData) {
    var msg = UpdateDataNamespaceMsg.newBuilder().setData(gameData);
    makeServerCall(Message.newBuilder().setUpdateDataNamespaceMsg(msg).build());
  }

  @Override
  public void updateData(String type, String namespace, GameDataValueDto gameData) {
    var msg = UpdateDataMsg.newBuilder().setType(type).setNamespace(namespace).setValue(gameData);
    makeServerCall(Message.newBuilder().setUpdateDataMsg(msg).build());
  }

  @Override
  public void removeDataStore() {
    makeServerCall(
        Message.newBuilder().setRemoveDataStoreMsg(RemoveDataStoreMsg.newBuilder()).build());
  }

  @Override
  public void removeDataNamespace(String type, String namespace) {
    var msg = RemoveDataNamespaceMsg.newBuilder().setType(type).setNamespace(namespace);
    makeServerCall(Message.newBuilder().setRemoveDataNamespaceMsg(msg).build());
  }

  @Override
  public void removeData(String type, String namespace, String name) {
    var msg = RemoveDataMsg.newBuilder().setType(type).setNamespace(namespace).setName(name);
    makeServerCall(Message.newBuilder().setRemoveDataMsg(msg).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, int value) {
    updateTokenProperty(
        token, update, TokenPropertyValueDto.newBuilder().setIntValue(value).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, String value1, String value2) {
    var value1Dto = TokenPropertyValueDto.newBuilder();
    if (value1 != null) {
      value1Dto.setStringValue(value1);
    }

    var value2Dto = TokenPropertyValueDto.newBuilder();
    if (value2 != null) {
      value2Dto.setStringValue(value2);
    }

    updateTokenProperty(token, update, value1Dto.build(), value2Dto.build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, List<MacroButtonProperties> workingMacros, boolean b) {
    var list =
        MacroButtonPropertiesListDto.newBuilder()
            .addAllMacros(
                workingMacros.stream()
                    .map(MacroButtonProperties::toDto)
                    .collect(Collectors.toList()));
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setMacros(list).build(),
        TokenPropertyValueDto.newBuilder().setBoolValue(b).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update) {
    updateTokenProperty(token, update, new TokenPropertyValueDto[] {});
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, MacroButtonProperties value) {
    var list = MacroButtonPropertiesListDto.newBuilder().addMacros(value.toDto());
    updateTokenProperty(token, update, TokenPropertyValueDto.newBuilder().setMacros(list).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, String value) {
    updateTokenProperty(
        token, update, TokenPropertyValueDto.newBuilder().setStringValue(value).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, LightSource value) {
    updateTokenProperty(
        token, update, TokenPropertyValueDto.newBuilder().setLightSource(value.toDto()).build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, LightSource value1, String value2) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setLightSource(value1.toDto()).build(),
        TokenPropertyValueDto.newBuilder().setStringValue(value2).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, int value1, int value2) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setIntValue(value1).build(),
        TokenPropertyValueDto.newBuilder().setIntValue(value2).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, boolean value) {
    updateTokenProperty(
        token, update, TokenPropertyValueDto.newBuilder().setBoolValue(value).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, double value1, double value2) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setDoubleValue(value1).build(),
        TokenPropertyValueDto.newBuilder().setDoubleValue(value2).build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, double value1, int value2, int value3) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setDoubleValue(value1).build(),
        TokenPropertyValueDto.newBuilder().setIntValue(value2).build(),
        TokenPropertyValueDto.newBuilder().setIntValue(value3).build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, Grid grid, TokenFootprint footprint) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setGrid(grid.toDto()).build(),
        TokenPropertyValueDto.newBuilder().setTokenFootPrint(footprint.toDto()).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, List<String> values) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder()
            .setStringValues(StringListDto.newBuilder().addAllValues(values).build())
            .build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, double value) {
    updateTokenProperty(
        token, update, TokenPropertyValueDto.newBuilder().setDoubleValue(value).build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, boolean value1, int value2, int value3) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setBoolValue(value1).build(),
        TokenPropertyValueDto.newBuilder().setIntValue(value2).build(),
        TokenPropertyValueDto.newBuilder().setIntValue(value3).build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, Zone.TopologyType topologyType, Area area) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setTopologyType(topologyType.name()).build(),
        TokenPropertyValueDto.newBuilder().setArea(Mapper.map(area)).build());
  }

  @Override
  public void updateTokenProperty(Token token, Token.Update update, String value1, boolean value2) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setStringValue(value1).build(),
        TokenPropertyValueDto.newBuilder().setBoolValue(value2).build());
  }

  @Override
  public void updateTokenProperty(
      Token token, Token.Update update, String value, BigDecimal value2) {
    updateTokenProperty(
        token,
        update,
        TokenPropertyValueDto.newBuilder().setStringValue(value).build(),
        TokenPropertyValueDto.newBuilder().setDoubleValue(value2.doubleValue()).build());
  }

  @Override
  public void updatePlayerStatus(Player player) {
    var msg =
        UpdatePlayerStatusMsg.newBuilder()
            .setPlayer(player.getName())
            .setZoneGuid(player.getZoneId().toString())
            .setLoaded(player.getLoaded());
    makeServerCall(Message.newBuilder().setUpdatePlayerStatusMsg(msg).build());
  }

  /**
   * Some events become obsolete very quickly, such as dragging a token around. This queue always
   * has exactly one element, the more current version of the event. The event is then dispatched at
   * some time interval. If a new event arrives before the time interval elapses, it is replaced. In
   * this way, only the most current version of the event is released.
   */
  private static class TimedEventQueue extends Thread {

    Message msg;
    long delay;

    final Object sleepSemaphore = new Object();

    public TimedEventQueue(long millidelay) {
      setName("ServerCommandClientImpl.TimedEventQueue");
      delay = millidelay;
    }

    public void enqueue(Message message) {
      msg = message;
    }

    public synchronized void flush() {

      if (msg != null) {
        makeServerCall(msg);
        msg = null;
      }
    }

    @Override
    public void run() {

      while (true) {

        flush();
        synchronized (sleepSemaphore) {
          try {
            Thread.sleep(delay);
          } catch (InterruptedException ie) {
            // nothing to do
          }
        }
      }
    }
  }
}
