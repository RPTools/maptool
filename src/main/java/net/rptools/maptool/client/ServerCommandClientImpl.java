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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client;

import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.StringValue;
import java.awt.Color;
import java.awt.geom.Area;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.functions.ExecFunction;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Token.TerrainModifierOperation;
import net.rptools.maptool.model.Zone.ShowCoordinates;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.topology.Wall;
import net.rptools.maptool.model.topology.WallTopology;
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
  private static final Logger log = LogManager.getLogger(ServerCommandClientImpl.class);

  private final MapToolClient client;
  private final TimedEventQueue movementUpdateQueue = new TimedEventQueue(100);

  public ServerCommandClientImpl(MapToolClient client) {
    this.client = client;
  }

  public void start() {
    try {
      movementUpdateQueue.start();
    } catch (IllegalThreadStateException e) {
      log.error("ServerCommand was already started", e);
    }
  }

  public void stop() {
    movementUpdateQueue.stopRunning();
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
    client.getCampaign().setName(name);
    MapTool.getFrame().setTitle();
    var msg = SetCampaignNameMsg.newBuilder().setName(name);
    makeServerCall(Message.newBuilder().setSetCampaignNameMsg(msg).build());
  }

  public void setLandingMap(@Nullable GUID landingMapId) {
    client.getCampaign().setLandingMapId(landingMapId);
    var msg = SetCampaignLandingMapMsg.newBuilder();
    if (landingMapId != null) {
      msg.setLandingMapId(landingMapId.toString());
    }
    makeServerCall(Message.newBuilder().setSetCampaignLandingMapMsg(msg).build());
  }

  public void setVisionType(GUID zoneGUID, VisionType visionType) {
    var msg =
        SetVisionTypeMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setVision(ZoneDto.VisionTypeDto.valueOf(visionType.name()));
    makeServerCall(Message.newBuilder().setSetVisionTypeMsg(msg).build());
  }

  public void setShowCoordinates(GUID zoneGUID, ShowCoordinates showCoordinates) {
    var msg =
        SetShowCoordinatesMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setShowCoordinates(ZoneDto.ShowCoordinatesDto.valueOf(showCoordinates.name()));
    makeServerCall(Message.newBuilder().setSetShowCoordinatesMsg(msg).build());
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
    client.getCampaign().getZone(zoneGUID).editToken(token);
    var msg = EditTokenMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setToken(token.toDto());
    makeServerCall(Message.newBuilder().setEditTokenMsg(msg).build());
  }

  public void putToken(GUID zoneGUID, Token token) {
    // Hack to generate zone event. All functions that update tokens call this method
    // after changing the token. But they don't tell the zone about it so classes
    // waiting for the zone change event don't get it.
    client.getCampaign().getZone(zoneGUID).putToken(token);
    var msg = PutTokenMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setToken(token.toDto());
    makeServerCall(Message.newBuilder().setPutTokenMsg(msg).build());
  }

  @Override
  public void removeToken(GUID zoneGUID, GUID tokenGUID) {
    // delete local token immediately
    client.getCampaign().getZone(zoneGUID).removeToken(tokenGUID);
    var msg =
        RemoveTokenMsg.newBuilder()
            .setZoneGuid(zoneGUID.toString())
            .setTokenGuid(tokenGUID.toString());
    makeServerCall(Message.newBuilder().setRemoveTokenMsg(msg).build());
  }

  @Override
  public void removeTokens(GUID zoneGUID, List<GUID> tokenGUIDs) {
    // delete local tokens immediately
    client.getCampaign().getZone(zoneGUID).removeTokens(tokenGUIDs);
    var msg = RemoveTokensMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    msg.addAllTokenGuid(tokenGUIDs.stream().map(t -> t.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setRemoveTokensMsg(msg).build());
  }

  /**
   * Simplifies the arguments for the method above.
   *
   * @param token the token to be updated
   */
  private void updateTokenProperty(Token token, List<TokenUpdateDto> updates) {
    updateTokenProperty(token.getZoneRenderer().getZone(), token, updates);
  }

  /**
   * Simplifies the arguments for the method above.
   *
   * @param token the token to be updated
   */
  private void updateTokenProperty(Zone zone, Token token, List<TokenUpdateDto> updates) {
    // Update locally right away.
    token.updateProperties(zone, updates);

    // And then update remotely.
    var msg =
        UpdateTokenMsg.newBuilder()
            .setTokenGuid(token.getId().toDto())
            .setZoneGuid(zone.getId().toDto())
            .addAllUpdate(updates)
            .build();
    makeServerCall(Message.newBuilder().setUpdateTokenPropertyMsg(msg).build());
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

  public void replaceWalls(Zone zone, WallTopology walls) {
    zone.replaceWalls(walls);
    var msg =
        SetWallTopologyMsg.newBuilder()
            .setZoneGuid(zone.getId().toString())
            .setTopology(walls.toDto());
    makeServerCall(Message.newBuilder().setSetWallTopologyMsg(msg).build());
  }

  public void updateWall(Zone zone, Wall wall) {
    var msg =
        UpdateWallDataMsg.newBuilder().setZoneGuid(zone.getId().toString()).setWall(wall.toDto());

    zone.updateWall(wall);
    makeServerCall(Message.newBuilder().setUpdateWallDataMsg(msg).build());
  }

  @Override
  public void updateMaskTopology(
      Zone zone, Area area, boolean erase, Zone.TopologyType topologyType) {
    var msg =
        UpdateMaskTopologyMsg.newBuilder()
            .setZoneGuid(zone.getId().toString())
            .setArea(Mapper.map(area))
            .setErase(erase)
            .setType(TopologyTypeDto.valueOf(topologyType.name()));

    // Update locally as well.
    zone.updateMaskTopology(area, erase, topologyType);
    makeServerCall(Message.newBuilder().setUpdateMaskTopologyMsg(msg).build());
  }

  public void exposePCArea(GUID zoneGUID) {
    var msg = ExposePcAreaMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setExposePcAreaMsg(msg).build());
  }

  public void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    // Expose locally right away.
    client.getCampaign().getZone(zoneGUID).exposeArea(area, selectedToks);
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

  private void makeServerCall(Message msg) {
    log.debug(
        "{} making server call {}; state is {}",
        client.getPlayer().getName(),
        msg.getMessageTypeCase(),
        client.getState());

    if (client.getState() == MapToolClient.State.Connected) {
      client.getConnection().sendMessage(msg);
    }
  }

  public void setBoard(
      GUID zoneGUID, MD5Key mapAssetId, int x, int y, double scaleX, double scaleY) {
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
            .setPoint(IntPointDto.newBuilder().setX(x).setY(y))
            .setScaleX(scaleX)
            .setScaleY(scaleY);
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
  public void setTokenType(Token token, Token.Type type) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetType(type.toDto()).build()));
  }

  @Override
  public void setTokenName(Token token, String name) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetName(name).build()));
  }

  @Override
  public void setTokenGmName(Token token, String gmName) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetGmName(gmName).build()));
  }

  @Override
  public void setTokenLabel(Token token, String label) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetLabel(label).build()));
  }

  @Override
  public void setTokenSpeechName(Token token, String speechName) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetSpeechName(speechName).build()));
  }

  @Override
  public void setTokenNotes(Token token, String notes) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetNotes(notes).build()));
  }

  @Override
  public void setTokenGmNotes(Token token, String gmNotes) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetGmNotes(gmNotes).build()));
  }

  @Override
  public void toggleLightSourceOnToken(Token token, boolean toggleOn, LightSource lightSource) {
    // We only need to send the ID of the light source.
    var update = TokenUpdateDto.newBuilder();
    if (toggleOn) {
      update.setEnableLightSource(lightSource.getId().toDto());
    } else {
      update.setDisableLightSource(lightSource.getId().toDto());
    }

    updateTokenProperty(token, List.of(update.build()));
  }

  @Override
  public void clearTokenLightSources(Token token) {
    updateTokenProperty(
        token,
        List.of(TokenUpdateDto.newBuilder().setClearLightSources(Empty.newBuilder()).build()));
  }

  @Override
  public void toggleHaloSourceOnToken(Token token, boolean toggleOn, Halo halo) {
    var update = TokenUpdateDto.newBuilder();
    if (toggleOn) {
      update.setEnableHalo(halo.getId().toDto());
    } else {
      update.setDisableHalo(halo.getId().toDto());
    }

    // We only need to send the ID of the halo source.
    updateTokenProperty(token, List.of(update.build()));
  }

  @Override
  public void clearTokenHalos(Token token) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setClearHalos(Empty.newBuilder()).build()));
  }

  @Override
  public void setTokenHaloColor(Token token, Color color) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetHaloColor(color.getRGB()).build()));
  }

  @Override
  public void clearTokenHaloColor(Token token) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setClearHaloColor(Empty.newBuilder()).build()));
  }

  @Override
  public void setTokenHasSight(Token token, boolean hasSight) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetHasSight(hasSight).build()));
  }

  @Override
  public void setTokenSightType(Token token, String sightTypeName) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetSightType(sightTypeName).build()));
  }

  @Override
  public void setTokenMaskTopology(
      Token token, @Nullable Area area, Zone.TopologyType topologyType) {
    var mask = TokenUpdateDto.Mask.newBuilder();
    mask.setType(topologyType.toDto());
    if (area != null) {
      mask.setArea(Mapper.map(area));
    }

    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetTokenMaskTopology(mask).build()));
  }

  @Override
  public void setTokenFootprint(Zone zone, Token token, GridType gridType, GUID footprintId) {
    updateTokenProperty(
        zone,
        token,
        List.of(
            TokenUpdateDto.newBuilder()
                .setSetFootprint(
                    TokenUpdateDto.Footprint.newBuilder()
                        .setType(gridType.toDto())
                        .setId(footprintId.toDto())
                        .build())
                .build()));
  }

  @Override
  public void setTokenPropertyType(Token token, String propertyTypeName) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetPropertyType(propertyTypeName).build()));
  }

  @Override
  public void setTokenProperty(Token token, String propertyName, String propertyValue) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder()
                .setSetProperty(
                    TokenUpdateDto.Property.newBuilder()
                        .setName(propertyName)
                        .setValue(propertyValue)
                        .build())
                .build()));
  }

  @Override
  public void resetTokenProperty(Token token, String propertyName) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setResetProperty(propertyName).build()));
  }

  @Override
  public void setTokenLayer(Token token, Zone.Layer layer) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetLayer(layer.toDto()).build()));
  }

  @Override
  public void setTokenShape(Token token, Token.TokenShape shape) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetShape(shape.toDto()).build()));
  }

  @Override
  public void setTokenLayerShape(Token token, Zone.Layer layer, Token.TokenShape shape) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder().setSetLayer(layer.toDto()).build(),
            TokenUpdateDto.newBuilder().setSetShape(shape.toDto()).build()));
  }

  @Override
  public void setTokenImageAsset(Token token, MD5Key assetId) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetImageAsset(assetId.toString()).build()));
  }

  @Override
  public void setTokenPortraitImage(Token token, MD5Key assetId) {
    var update = TokenUpdateDto.newBuilder();
    if (assetId == null) {
      update.setClearPortraitAsset(Empty.newBuilder());
    } else {
      update.setSetPortraitAsset(assetId.toString());
    }
    updateTokenProperty(token, List.of(update.build()));
  }

  @Override
  public void setTokenCharsheetImage(Token token, @Nullable MD5Key assetId) {
    var update = TokenUpdateDto.newBuilder();
    if (assetId == null) {
      update.setClearCharsheetAsset(Empty.newBuilder());
    } else {
      update.setSetCharsheetAsset(assetId.toString());
      ;
    }
    updateTokenProperty(token, List.of(update.build()));
  }

  @Override
  public void setTokenOpacity(Token token, double opacity) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetOpacity(opacity).build()));
  }

  @Override
  public void addTokenOwner(Token token, String ownerName) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setAddOwner(ownerName).build()));
  }

  @Override
  public void clearTokenOwners(Token token) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setClearOwners(Empty.newBuilder()).build()));
  }

  @Override
  public void setTokenOwnedByAll(Token token, boolean ownedByAll) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetOwnedByAll(ownedByAll).build()));
  }

  @Override
  public void setTokenVisible(Token token, boolean visible) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetVisible(visible).build()));
  }

  @Override
  public void setTokenVisibleOnlyToOwner(Token token, boolean visibleOnlyToOwner) {
    updateTokenProperty(
        token,
        List.of(TokenUpdateDto.newBuilder().setSetVisibleToOwnerOnly(visibleOnlyToOwner).build()));
  }

  @Override
  public void setTokenAlwaysVisible(Token token, boolean alwaysVisible) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetAlwaysVisible(alwaysVisible).build()));
  }

  @Override
  public void putTokenMacros(Token token, List<MacroButtonProperties> macros) {
    var macrosDto = TokenUpdateDto.Macros.newBuilder();
    for (var macro : macros) {
      macrosDto.addMacros(macro.toDto());
    }
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setPutMacros(macrosDto).build()));
  }

  @Override
  public void replaceTokenMacros(Token token, List<MacroButtonProperties> macros) {
    var macrosDto = TokenUpdateDto.Macros.newBuilder();
    for (var macro : macros) {
      macrosDto.addMacros(macro.toDto());
    }
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setReplaceMacros(macrosDto).build()));
  }

  @Override
  public void saveTokenMacro(Token token, MacroButtonProperties macro) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSaveMacro(macro.toDto()).build()));
  }

  @Override
  public void deleteTokenMacro(Token token, int macroIndex) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setDeleteMacro(macroIndex).build()));
  }

  @Override
  public void setTokenXY(Token token, int x, int y) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder().setSetX(x).build(),
            TokenUpdateDto.newBuilder().setSetY(y).build()));
  }

  @Override
  public void setTokenZOrder(Token token, int zOrder) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetZ(zOrder).build()));
  }

  @Override
  public void clearTokenFacing(Token token) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setClearFacing(Empty.newBuilder()).build()));
  }

  @Override
  public void setTokenFlippedX(Token token, boolean flippedX) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetFlippedX(flippedX).build()));
  }

  @Override
  public void setTokenFlippedY(Token token, boolean flippedY) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetFlippedY(flippedY).build()));
  }

  @Override
  public void setTokenFlippedIso(Token token, boolean flippedIso) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetFlippedIso(flippedIso).build()));
  }

  @Override
  public void setTokenFacing(Token token, int facing) {
    updateTokenProperty(token, List.of(TokenUpdateDto.newBuilder().setSetFacing(facing).build()));
  }

  @Override
  public void setTokenScaleXY(Token token, double scaleX, double scaleY) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder().setSetScaleX(scaleX).build(),
            TokenUpdateDto.newBuilder().setSetScaleY(scaleY).build()));
  }

  @Override
  public void setTokenLayout(Token token, double scale, int offsetX, int offsetY) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder().setSetSizeScale(scale).build(),
            TokenUpdateDto.newBuilder().setSetAnchorX(offsetX).build(),
            TokenUpdateDto.newBuilder().setSetAnchorY(offsetY).build()));
  }

  @Override
  public void setTokenTerrainModifiersIgnored(
      Token token, Collection<TerrainModifierOperation> operations) {
    var ignored = TokenUpdateDto.IgnoredModifiers.newBuilder();
    for (var operation : operations) {
      ignored.addModifiers(operation.toDto());
    }

    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetIgnoredModifiers(ignored).build()));
  }

  @Override
  public void setTokenTerrainModifier(Token token, double modifier) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetTerrainModifier(modifier).build()));
  }

  @Override
  public void setTokenTerrainModifierOperation(Token token, TerrainModifierOperation operation) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder().setSetTerrainModifierOperation(operation.toDto()).build()));
  }

  @Override
  public void setTokenSnapToGrid(Token token, boolean snapToGrid) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetSnapToGrid(snapToGrid).build()));
  }

  @Override
  public void setTokenSnapToGridAndXY(Token token, boolean snapToGrid, int x, int y) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder().setSetSnapToGrid(snapToGrid).build(),
            TokenUpdateDto.newBuilder().setSetX(x).build(),
            TokenUpdateDto.newBuilder().setSetY(y).build()));
  }

  @Override
  public void setTokenSnapToScale(Token token, boolean snapToScale) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetSnapToScale(snapToScale).build()));
  }

  @Override
  public void setTokenState(Token token, String stateName, boolean stateOn) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder()
                .setSetState(
                    TokenUpdateDto.State.newBuilder().setName(stateName).setOn(stateOn).build())
                .build()));
  }

  @Override
  public void setTokenBar(Token token, String barName, BigDecimal barValue) {
    updateTokenProperty(
        token,
        List.of(
            TokenUpdateDto.newBuilder()
                .setSetBar(
                    TokenUpdateDto.Bar.newBuilder()
                        .setName(barName)
                        .setValue(barValue.doubleValue())
                        .build())
                .build()));
  }

  @Override
  public void setAllTokenStates(Token token, boolean statesOn) {
    updateTokenProperty(
        token, List.of(TokenUpdateDto.newBuilder().setSetAllStates(statesOn).build()));
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

  @Override
  public void putLookupTable(LookupTable lookupTable) {
    var msg = PutLookupTableMsg.newBuilder().setTable(lookupTable.toDto());
    client.getCampaign().getLookupTableMap().put(lookupTable.getName(), lookupTable);
    makeServerCall(Message.newBuilder().setUpdateLookupTableMsg(msg).build());
  }

  @Override
  public void deleteLookupTable(String tableName) {
    var msg = DeleteLookupTableMsg.newBuilder().setTableName(tableName);
    client.getCampaign().getLookupTableMap().remove(tableName);
    makeServerCall(Message.newBuilder().setDeleteLookupTableMsg(msg).build());
  }

  /**
   * Some events become obsolete very quickly, such as dragging a token around. This queue always
   * has exactly one element, the more current version of the event. The event is then dispatched at
   * some time interval. If a new event arrives before the time interval elapses, it is replaced. In
   * this way, only the most current version of the event is released.
   */
  private class TimedEventQueue extends Thread {
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final long delay;
    private Message msg;

    public TimedEventQueue(long millidelay) {
      setName("ServerCommandClientImpl.TimedEventQueue");
      delay = millidelay;
    }

    public void stopRunning() {
      done.set(true);
      try {
        interrupt();
        join();
      } catch (InterruptedException e) {
        log.error("Interrupted thread join. Thread may not be done running.", e);
      }
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
      while (!done.get()) {
        flush();
        try {
          Thread.sleep(delay);
        } catch (InterruptedException ie) {
          // nothing to do
        }
      }
    }
  }
}
