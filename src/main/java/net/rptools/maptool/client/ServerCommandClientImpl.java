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

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import java.awt.geom.Area;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.functions.ExecFunction;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.ExposedAreaMetaData;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.InitiativeList;
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
import net.rptools.maptool.model.gamedata.proto.DataStoreDto;
import net.rptools.maptool.model.gamedata.proto.GameDataDto;
import net.rptools.maptool.model.gamedata.proto.GameDataValueDto;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.ServerCommand;
import net.rptools.maptool.server.ServerMethodHandler;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.server.proto.*;

/**
 * This class is used by a client to send commands to the server. The methods of this class are
 * typically accessed through MapTool.serverCommand(). Once sent, the commands are then received by
 * the {@link ServerMethodHandler ServerMethodHandler}
 */
public class ServerCommandClientImpl implements ServerCommand {

  private final TimedEventQueue movementUpdateQueue = new TimedEventQueue(100);
  private final LinkedBlockingQueue<MD5Key> assetRetrieveQueue = new LinkedBlockingQueue<MD5Key>();

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
    try {
      campaign.setBeingSerialized(true);
      makeServerCall(COMMAND.setCampaign, campaign);
    } finally {
      campaign.setBeingSerialized(false);
    }
  }

  public void setCampaignName(String name) {
    MapTool.getCampaign().setName(name);
    MapTool.getFrame().setTitle();
    makeServerCall(COMMAND.setCampaignName, name);
  }

  public void setVisionType(GUID zoneGUID, VisionType visionType) {
    makeServerCall(COMMAND.setVisionType, zoneGUID, visionType);
  }

  public void updateCampaign(CampaignProperties properties) {
    makeServerCall(COMMAND.updateCampaign, properties);
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
    makeServerCall(COMMAND.removeZone, zoneGUID);
  }

  public void renameZone(GUID zoneGUID, String name) {
    makeServerCall(COMMAND.renameZone, zoneGUID, name);
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
    makeServerCall(COMMAND.removeAsset, assetID);
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
    makeServerCall(COMMAND.restoreZoneView, zoneGUID);
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
    makeServerCall(COMMAND.removeToken, zoneGUID, tokenGUID);
  }

  @Override
  public void removeTokens(GUID zoneGUID, List<GUID> tokenGUIDs) {
    // delete local tokens immediately
    MapTool.getCampaign().getZone(zoneGUID).removeTokens(tokenGUIDs);
    makeServerCall(COMMAND.removeTokens, zoneGUID, tokenGUIDs);
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
      GUID zoneGUID, GUID tokenGUID, Token.Update update, Object[] parameters) {
    makeServerCall(COMMAND.updateTokenProperty, zoneGUID, tokenGUID, update, parameters);
  }

  /**
   * Simplifies the arguments for the method above.
   *
   * @param token the token to be updated
   * @param update the type of token update
   * @param parameters an array of parameters
   */
  public void updateTokenProperty(Token token, Token.Update update, Object... parameters) {
    Zone zone = token.getZoneRenderer().getZone();
    GUID tokenGUID = token.getId();
    GUID zoneGUID = zone.getId();

    token.updateProperty(zone, update, parameters); // update locally right away
    updateTokenProperty(zoneGUID, tokenGUID, update, parameters);
  }

  public void putLabel(GUID zoneGUID, Label label) {
    var msg = PutLabelMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setLabel(label.toDto());
    makeServerCall(Message.newBuilder().setPutLabelMsg(msg).build());
  }

  public void removeLabel(GUID zoneGUID, GUID labelGUID) {
    makeServerCall(COMMAND.removeLabel, zoneGUID, labelGUID);
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
    makeServerCall(COMMAND.updateDrawing, zoneGUID, pen, drawnElement);
  }

  public void clearAllDrawings(GUID zoneGUID, Zone.Layer layer) {
    var msg =
        ClearAllDrawingsMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setLayer(layer.name());

    makeServerCall(Message.newBuilder().setClearAllDrawingsMsg(msg).build());
  }

  public void undoDraw(GUID zoneGUID, GUID drawableGUID) {
    makeServerCall(COMMAND.undoDraw, zoneGUID, drawableGUID);
  }

  public void setZoneGridSize(GUID zoneGUID, int xOffset, int yOffset, int size, int color) {
    makeServerCall(COMMAND.setZoneGridSize, zoneGUID, xOffset, yOffset, size, color);
  }

  public void setZoneVisibility(GUID zoneGUID, boolean visible) {
    makeServerCall(COMMAND.setZoneVisibility, zoneGUID, visible);
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
    makeServerCall(COMMAND.showPointer, player, pointer);
  }

  public void hidePointer(String player) {
    var msg = HidePointerMsg.newBuilder().setPlayer(player);
    makeServerCall(Message.newBuilder().setHidePointerMsg(msg).build());
  }

  public void setLiveTypingLabel(String label, boolean show) {
    makeServerCall(COMMAND.setLiveTypingLabel, label, show);
  }

  public void enforceNotification(Boolean enforce) {
    var msg = EnforceNotificationMsg.newBuilder().setEnforce(enforce);
    makeServerCall(Message.newBuilder().setEnforceNotificationMsg(msg).build());
  }

  public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList) {
    makeServerCall(COMMAND.startTokenMove, playerId, zoneGUID, tokenGUID, tokenList);
  }

  public void stopTokenMove(GUID zoneGUID, GUID tokenGUID) {
    movementUpdateQueue.flush();
    makeServerCall(COMMAND.stopTokenMove, zoneGUID, tokenGUID);
  }

  public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y) {
    movementUpdateQueue.enqueue(COMMAND.updateTokenMove, zoneGUID, tokenGUID, x, y);
  }

  public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp) {
    movementUpdateQueue.flush();
    makeServerCall(COMMAND.toggleTokenMoveWaypoint, zoneGUID, tokenGUID, cp);
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
    makeServerCall(COMMAND.removeTopology, zoneGUID, area, topologyType);
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
    makeServerCall(COMMAND.setFoW, zoneGUID, area, selectedToks);
  }

  public void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    var msg = HideFowMsg.newBuilder().setZoneGuid(zoneGUID.toString()).setArea(Mapper.map(area));
    msg.addAllTokenGuid(selectedToks.stream().map(g -> g.toString()).collect(Collectors.toList()));
    makeServerCall(Message.newBuilder().setHideFowMsg(msg).build());
  }

  public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
    makeServerCall(COMMAND.setZoneHasFoW, zoneGUID, hasFog);
  }

  public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenList) {
    var msg = BringTokensToFrontMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    tokenList.stream().forEach(guid -> msg.addTokenGuids(guid.toString()));

    makeServerCall(Message.newBuilder().setBringTokensToFrontMsg(msg).build());
  }

  public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenList) {
    makeServerCall(COMMAND.sendTokensToBack, zoneGUID, tokenList);
  }

  public void enforceZone(GUID zoneGUID) {
    var msg = EnforceZoneMsg.newBuilder().setZoneGuid(zoneGUID.toString());
    makeServerCall(Message.newBuilder().setEnforceZoneMsg(msg).build());
  }

  public void setServerPolicy(ServerPolicy policy) {
    makeServerCall(COMMAND.setServerPolicy, policy);
  }

  public void updateInitiative(InitiativeList list, Boolean ownerPermission) {
    makeServerCall(COMMAND.updateInitiative, list, ownerPermission);
  }

  public void updateTokenInitiative(
      GUID zone, GUID token, Boolean holding, String state, Integer index) {
    makeServerCall(COMMAND.updateTokenInitiative, zone, token, holding, state, index);
  }

  public void updateCampaignMacros(List<MacroButtonProperties> properties) {
    makeServerCall(COMMAND.updateCampaignMacros, properties);
  }

  public void updateGmMacros(List<MacroButtonProperties> properties) {
    makeServerCall(COMMAND.updateGmMacros, properties);
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

  private static void makeServerCall(ServerCommand.COMMAND command, Object... params) {
    if (MapTool.getConnection() != null) {
      MapTool.getConnection().callMethod(command.name(), params);
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
    makeServerCall(COMMAND.setBoard, zoneGUID, mapAssetId, x, y);
  }

  /*
   * (non-Javadoc)
   *
   * @see net.rptools.maptool.server.ServerCommand#updateExposedAreaMeta(net. rptools.maptool.model.GUID, net.rptools.maptool.model.GUID, net.rptools.maptool.model.ExposedAreaMetaData)
   */
  public void updateExposedAreaMeta(
      GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta) {
    makeServerCall(COMMAND.updateExposedAreaMeta, zoneGUID, tokenExposedAreaGUID, meta);
  }

  @Override
  public void addAddOnLibrary(List<TransferableAddOnLibrary> addOnLibraries) {
    var libs = new ArrayList<TransferableAddOnLibrary>();
    libs.addAll(addOnLibraries);
    makeServerCall(COMMAND.addAddOnLibrary, libs);
  }

  @Override
  public void removeAddOnLibrary(List<String> namespaces) {
    var libs = new ArrayList<String>();
    libs.addAll(namespaces);
    makeServerCall(COMMAND.removeAddOnLibrary, libs);
  }

  @Override
  public void removeAllAddOnLibraries() {
    makeServerCall(COMMAND.removeAllAddOnLibraries);
  }

  @Override
  public void updateDataStore(DataStoreDto dataStore) {
    try {
      byte[] bytes = JsonFormat.printer().print(dataStore).getBytes(StandardCharsets.UTF_8);
      makeServerCall(COMMAND.updateDataStore, bytes);
    } catch (InvalidProtocolBufferException e) {
      MapTool.showError("data.error.sendingUpdate", e);
    }
  }

  @Override
  public void updateDataNamespace(GameDataDto gameData) {
    try {
      byte[] bytes = JsonFormat.printer().print(gameData).getBytes(StandardCharsets.UTF_8);
      makeServerCall(COMMAND.updateDataNamespace, bytes);
    } catch (InvalidProtocolBufferException e) {
      MapTool.showError("data.error.sendingUpdate", e);
    }
  }

  @Override
  public void updateData(String type, String namespace, GameDataValueDto gameData) {
    try {
      byte[] bytes = JsonFormat.printer().print(gameData).getBytes(StandardCharsets.UTF_8);
      makeServerCall(COMMAND.updateData, type, namespace, bytes);
    } catch (InvalidProtocolBufferException e) {
      MapTool.showError("data.error.sendingUpdate", e);
    }
  }

  @Override
  public void removeDataStore() {
    makeServerCall(COMMAND.removeDataStore);
  }

  @Override
  public void removeDataNamespace(String type, String namespace) {
    makeServerCall(COMMAND.removeDataNamespace, type, namespace);
  }

  @Override
  public void removeData(String type, String namespace, String name) {
    makeServerCall(COMMAND.removeData, type, namespace, name);
  }

  /**
   * Some events become obsolete very quickly, such as dragging a token around. This queue always
   * has exactly one element, the more current version of the event. The event is then dispatched at
   * some time interval. If a new event arrives before the time interval elapses, it is replaced. In
   * this way, only the most current version of the event is released.
   */
  private static class TimedEventQueue extends Thread {

    ServerCommand.COMMAND command;
    Object[] params;

    long delay;

    final Object sleepSemaphore = new Object();

    public TimedEventQueue(long millidelay) {
      setName("ServerCommandClientImpl.TimedEventQueue");
      delay = millidelay;
    }

    public synchronized void enqueue(ServerCommand.COMMAND command, Object... params) {

      this.command = command;
      this.params = params;
    }

    public synchronized void flush() {

      if (command != null) {
        makeServerCall(command, params);
      }
      command = null;
      params = null;
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
