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

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.events.PlayerStatusChanged;
import net.rptools.maptool.client.functions.ExecFunction;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.client.ui.MapToolFrame;
import net.rptools.maptool.client.ui.tokenpanel.InitiativePanel;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRendererFactory;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.CellPoint;
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
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.gamedata.GameDataImporter;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.addon.AddOnLibraryImporter;
import net.rptools.maptool.model.library.addon.TransferableAddOnLibrary;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.zones.TokensAdded;
import net.rptools.maptool.model.zones.TokensRemoved;
import net.rptools.maptool.model.zones.ZoneAdded;
import net.rptools.maptool.model.zones.ZoneRemoved;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.ServerMessageHandler;
import net.rptools.maptool.server.ServerPolicy;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.transfer.AssetConsumer;
import net.rptools.maptool.transfer.AssetHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used by the clients to receive server commands sent through {@link
 * ServerMessageHandler ServerMethodHandler}.
 *
 * @author drice
 */
public class ClientMessageHandler implements MessageHandler {
  private static final Logger log = LogManager.getLogger(ClientMessageHandler.class);

  public ClientMessageHandler() {}

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var msg = Message.parseFrom(message);
      var msgType = msg.getMessageTypeCase();
      log.debug(id + " got: " + msgType);

      switch (msgType) {
        case ADD_TOPOLOGY_MSG -> handle(msg.getAddTopologyMsg());
        case BOOT_PLAYER_MSG -> handle(msg.getBootPlayerMsg());
        case CHANGE_ZONE_DISPLAY_NAME_MSG -> handle(msg.getChangeZoneDisplayNameMsg());
        case CLEAR_ALL_DRAWINGS_MSG -> handle(msg.getClearAllDrawingsMsg());
        case CLEAR_EXPOSED_AREA_MSG -> handle(msg.getClearExposedAreaMsg());
        case DRAW_MSG -> handle(msg.getDrawMsg());
        case EDIT_TOKEN_MSG -> handle(msg.getEditTokenMsg());
        case PUT_TOKEN_MSG -> handle(msg.getPutTokenMsg());
        case ENFORCE_NOTIFICATION_MSG -> handle(msg.getEnforceNotificationMsg());
        case ENFORCE_ZONE_MSG -> handle(msg.getEnforceZoneMsg());
        case ENFORCE_ZONE_VIEW_MSG -> handle(msg.getEnforceZoneViewMsg());
        case EXEC_FUNCTION_MSG -> handle(msg.getExecFunctionMsg());
        case EXEC_LINK_MSG -> handle(msg.getExecLinkMsg());
        case EXPOSE_FOW_MSG -> handle(msg.getExposeFowMsg());
        case EXPOSE_PC_AREA_MSG -> handle(msg.getExposePcAreaMsg());
        case HIDE_FOW_MSG -> handle(msg.getHideFowMsg());
        case HIDE_POINTER_MSG -> handle(msg.getHidePointerMsg());
        case MESSAGE_MSG -> handle(msg.getMessageMsg());
        case MOVE_POINTER_MSG -> handle(msg.getMovePointerMsg());
        case PLAYER_CONNECTED_MSG -> handle(msg.getPlayerConnectedMsg());
        case PLAYER_DISCONNECTED_MSG -> handle(msg.getPlayerDisconnectedMsg());
        case PUT_ASSET_MSG -> handle(msg.getPutAssetMsg());
        case PUT_LABEL_MSG -> handle(msg.getPutLabelMsg());
        case PUT_ZONE_MSG -> handle(msg.getPutZoneMsg());
        case REMOVE_LABEL_MSG -> handle(msg.getRemoveLabelMsg());
        case REMOVE_TOKEN_MSG -> handle(msg.getRemoveTokenMsg());
        case REMOVE_TOKENS_MSG -> handle(msg.getRemoveTokensMsg());
        case REMOVE_TOPOLOGY_MSG -> handle(msg.getRemoveTopologyMsg());
        case REMOVE_ZONE_MSG -> handle(msg.getRemoveZoneMsg());
        case RENAME_ZONE_MSG -> handle(msg.getRenameZoneMsg());
        case RESTORE_ZONE_VIEW_MSG -> handle(msg.getRestoreZoneViewMsg());
        case SET_BOARD_MSG -> handle(msg.getSetBoardMsg());
        case SET_CAMPAIGN_MSG -> handle(msg.getSetCampaignMsg());
        case SET_CAMPAIGN_NAME_MSG -> handle(msg.getSetCampaignNameMsg());
        case SET_FOW_MSG -> handle(msg.getSetFowMsg());
        case SET_LIVE_TYPING_LABEL_MSG -> handle(msg.getSetLiveTypingLabelMsg());
        case SET_TOKEN_LOCATION_MSG -> handle(msg.getSetTokenLocationMsg());
        case SET_VISION_TYPE_MSG -> handle(msg.getSetVisionTypeMsg());
        case SET_ZONE_GRID_SIZE_MSG -> handle(msg.getSetZoneGridSizeMsg());
        case SET_ZONE_HAS_FOW_MSG -> handle(msg.getSetZoneHasFowMsg());
        case START_ASSET_TRANSFER_MSG -> handle(msg.getStartAssetTransferMsg());
        case UPDATE_ASSET_TRANSFER_MSG -> handle(msg.getUpdateAssetTransferMsg());
        case ADD_ADD_ON_LIBRARY_MSG -> handle(msg.getAddAddOnLibraryMsg());
        case REMOVE_ADD_ON_LIBRARY_MSG -> handle(msg.getRemoveAddOnLibraryMsg());
        case REMOVE_ALL_ADD_ON_LIBRARIES_MSG -> handle(msg.getRemoveAllAddOnLibrariesMsg());
        case UPDATE_DATA_STORE_MSG -> handle(msg.getUpdateDataStoreMsg());
        case UPDATE_DATA_NAMESPACE_MSG -> handle(msg.getUpdateDataNamespaceMsg());
        case UPDATE_DATA_MSG -> handle(msg.getUpdateDataMsg());
        case REMOVE_DATA_STORE_MSG -> handle(msg.getRemoveDataStoreMsg());
        case REMOVE_DATA_NAMESPACE_MSG -> handle(msg.getRemoveDataNamespaceMsg());
        case REMOVE_DATA_MSG -> handle(msg.getRemoveDataMsg());
        case UPDATE_TOKEN_PROPERTY_MSG -> handle(msg.getUpdateTokenPropertyMsg());
        case UPDATE_DRAWING_MSG -> handle(msg.getUpdateDrawingMsg());
        case UNDO_DRAW_MSG -> handle(msg.getUndoDrawMsg());
        case SET_ZONE_VISIBILITY_MSG -> handle(msg.getSetZoneVisibilityMsg());
        case SHOW_POINTER_MSG -> handle(msg.getShowPointerMsg());
        case START_TOKEN_MOVE_MSG -> handle(msg.getStartTokenMoveMsg());
        case STOP_TOKEN_MOVE_MSG -> handle(msg.getStopTokenMoveMsg());
        case TOGGLE_TOKEN_MOVE_WAYPOINT_MSG -> handle(msg.getToggleTokenMoveWaypointMsg());
        case SET_SERVER_POLICY_MSG -> handle(msg.getSetServerPolicyMsg());
        case UPDATE_CAMPAIGN_MSG -> handle(msg.getUpdateCampaignMsg());
        case UPDATE_INITIATIVE_MSG -> handle(msg.getUpdateInitiativeMsg());
        case UPDATE_TOKEN_INITIATIVE_MSG -> handle(msg.getUpdateTokenInitiativeMsg());
        case UPDATE_CAMPAIGN_MACROS_MSG -> handle(msg.getUpdateCampaignMacrosMsg());
        case UPDATE_GM_MACROS_MSG -> handle(msg.getUpdateGmMacrosMsg());
        case UPDATE_EXPOSED_AREA_META_MSG -> handle(msg.getUpdateExposedAreaMetaMsg());
        case UPDATE_TOKEN_MOVE_MSG -> handle(msg.getUpdateTokenMoveMsg());
        case UPDATE_PLAYER_STATUS_MSG -> handle(msg.getUpdatePlayerStatusMsg());
        default -> log.warn(msgType + "not handled.");
      }
      log.debug(id + " handled: " + msgType);
    } catch (Exception e) {
      log.error(e);
    }
  }

  private void handle(UpdateTokenMoveMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var keyToken = GUID.valueOf(msg.getKeyTokenId());

          var renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          renderer.updateMoveSelectionSet(
              keyToken, new ZonePoint(msg.getPoint().getX(), msg.getPoint().getY()));
        });
  }

  private void handle(UpdateExposedAreaMetaMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var tokenGUID = msg.hasTokenGuid() ? GUID.valueOf(msg.getTokenGuid().getValue()) : null;
          ExposedAreaMetaData meta = new ExposedAreaMetaData(Mapper.map(msg.getArea()));
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.setExposedAreaMetaData(tokenGUID, meta);
        });
  }

  private void handle(UpdateGmMacrosMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var macros =
              msg.getMacrosList().stream()
                  .map(MacroButtonProperties::fromDto)
                  .collect(Collectors.toList());
          MapTool.getCampaign().setGmMacroButtonPropertiesArray(macros);
          MapTool.getFrame().getGmPanel().reset();
        });
  }

  private void handle(UpdateCampaignMacrosMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var macros =
              msg.getMacrosList().stream()
                  .map(MacroButtonProperties::fromDto)
                  .collect(Collectors.toList());
          MapTool.getCampaign().setMacroButtonPropertiesArray(macros);
          MapTool.getFrame().getCampaignPanel().reset();
        });
  }

  private void handle(UpdateTokenInitiativeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var tokenGUID = GUID.valueOf(msg.getTokenGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          var list = zone.getInitiativeList();
          TokenInitiative ti = list.getTokenInitiative(msg.getIndex());
          if (!ti.getId().equals(tokenGUID)) {
            // Index doesn't point to same token, try to find it
            var token = zone.getToken(tokenGUID);
            List<Integer> tokenIndex = list.indexOf(token);

            // If token in list more than one time, punt
            if (tokenIndex.size() != 1) return;
            ti = list.getTokenInitiative(tokenIndex.get(0));
          } // endif
          ti.update(msg.getIsHolding(), msg.hasState() ? msg.getState().getValue() : null);
        });
  }

  private void handle(UpdateInitiativeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          if (msg.hasList()) {
            InitiativeList list = InitiativeList.fromDto(msg.getList());
            var zone = list.getZone();
            if (zone == null) return;
            zone.setInitiativeList(list);
          }
          if (msg.hasOwnerPermission()) {
            var ownerPermission = msg.getOwnerPermission();
            MapTool.getFrame().getInitiativePanel().setOwnerPermissions(ownerPermission.getValue());
          }
        });
  }

  private void handle(UpdateCampaignMsg msg) {
    EventQueue.invokeLater(
        () -> {
          CampaignProperties properties = CampaignProperties.fromDto(msg.getProperties());

          MapTool.getCampaign().replaceCampaignProperties(properties);
          MapToolFrame frame = MapTool.getFrame();
          ZoneRenderer zr = frame.getCurrentZoneRenderer();
          if (zr != null) {
            zr.getZoneView().flush();
            zr.repaint();
          }
          AssetManager.updateRepositoryList();

          InitiativePanel ip = frame.getInitiativePanel();
          ip.setOwnerPermissions(properties.isInitiativeOwnerPermissions());
          ip.setMovementLock(properties.isInitiativeMovementLock());
          ip.setInitUseReverseSort(properties.isInitiativeUseReverseSort());
          ip.setInitPanelButtonsDisabled(properties.isInitiativePanelButtonsDisabled());
          ip.updateView();
          MapTool.getFrame().getLookupTablePanel().updateView();
        });
  }

  private void handle(SetServerPolicyMsg msg) {
    EventQueue.invokeLater(
        () -> {
          ServerPolicy policy = ServerPolicy.fromDto(msg.getPolicy());
          MapTool.setServerPolicy(policy);
          MapTool.getFrame().getToolbox().updateTools();
        });
  }

  private void handle(ToggleTokenMoveWaypointMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var keyToken = GUID.valueOf(msg.getKeyTokenId());
          ZonePoint zp = new ZonePoint(msg.getPoint().getX(), msg.getPoint().getY());

          var renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          renderer.toggleMoveSelectionSetWaypoint(keyToken, zp);
        });
  }

  private void handle(StopTokenMoveMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var keyToken = GUID.valueOf(msg.getKeyTokenId());

          var renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          renderer.removeMoveSelectionSet(keyToken);
        });
  }

  private void handle(StartTokenMoveMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var playerId = msg.getPlayerId();
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var keyToken = GUID.valueOf(msg.getKeyTokenId());
          var selectedSet =
              msg.getSelectedTokensList().stream().map(GUID::valueOf).collect(Collectors.toSet());

          var renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          renderer.addMoveSelectionSet(playerId, keyToken, selectedSet);
        });
  }

  private void handle(ShowPointerMsg msg) {
    EventQueue.invokeLater(
        () -> {
          MapTool.getFrame()
              .getPointerOverlay()
              .addPointer(msg.getPlayer(), Pointer.fromDto(msg.getPointer()));
          MapTool.getFrame().refresh();
        });
  }

  private void handle(SetZoneVisibilityMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          boolean visible = msg.getIsVisible();

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.setVisible(visible);
          ZoneRenderer currentRenderer = MapTool.getFrame().getCurrentZoneRenderer();
          if (!visible
              && !MapTool.getPlayer().isGM()
              && currentRenderer != null
              && currentRenderer.getZone().getId().equals(zoneGUID)) {
            Collection<GUID> AllTokenIDs = new ArrayList<>();
            for (Token token : currentRenderer.getZone().getAllTokens()) {
              AllTokenIDs.add(token.getId());
            }
            currentRenderer.getSelectionModel().removeTokensFromSelection(AllTokenIDs);
            MapTool.getFrame().setCurrentZoneRenderer(null);
          }
          if (visible && currentRenderer == null) {
            currentRenderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
            MapTool.getFrame().setCurrentZoneRenderer(currentRenderer);
          }
          MapTool.getFrame().getZoneMiniMapPanel().flush();
          MapTool.getFrame().refresh();
        });
  }

  private void handle(UndoDrawMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          GUID drawableId = GUID.valueOf(msg.getDrawableGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          if (zone == null) {
            return;
          }
          zone.removeDrawable(drawableId);
          if (MapTool.getFrame().getCurrentZoneRenderer().getZone().getId().equals(zoneGUID)) {
            MapTool.getFrame().refresh();
          }
        });
  }

  private void handle(UpdateDrawingMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          Pen p = Pen.fromDto(msg.getPen());
          DrawnElement de = DrawnElement.fromDto(msg.getDrawing());

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.updateDrawable(de, p);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(UpdateTokenPropertyMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          var tokenGUID = GUID.valueOf(msg.getTokenGuid());
          var token = zone.getToken(tokenGUID);
          if (token != null) {
            Token.Update update = Token.Update.valueOf(msg.getProperty().name());
            token.updateProperty(zone, update, msg.getValuesList());
          }
        });
  }

  private void handle(RemoveDataMsg msg) {
    String removeDType = msg.getType();
    String removeDNamespace = msg.getNamespace();
    String removeDName = msg.getName();
    try {
      new DataStoreManager()
          .getDefaultDataStoreForRemoteUpdate()
          .removeProperty(removeDType, removeDNamespace, removeDName)
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error(
          I18N.getText("data.error.removingData", removeDType, removeDNamespace, removeDName), e);
    }
  }

  private void handle(RemoveDataNamespaceMsg msg) {
    try {
      new DataStoreManager()
          .getDefaultDataStoreForRemoteUpdate()
          .clearNamespace(msg.getType(), msg.getNamespace())
          .get();
    } catch (InterruptedException | ExecutionException e) {
      log.error(I18N.getText("data.error.clearingNamespace", msg.getType(), msg.getNamespace()), e);
    }
  }

  private void handle(RemoveDataStoreMsg msg) {
    new DataStoreManager().getDefaultDataStoreForRemoteUpdate().clear();
  }

  private void handle(UpdateDataMsg msg) {
    try {
      var dataStore = new DataStoreManager().getDefaultDataStoreForRemoteUpdate();
      new GameDataImporter(dataStore).importData(msg.getType(), msg.getNamespace(), msg.getValue());
    } catch (ExecutionException | InterruptedException e) {
      MapTool.showError("data.error.receivingUpdate", e);
    }
  }

  private void handle(UpdateDataNamespaceMsg msg) {
    try {
      var dataStore = new DataStoreManager().getDefaultDataStoreForRemoteUpdate();
      new GameDataImporter(dataStore).importData(msg.getData());
    } catch (ExecutionException | InterruptedException e) {
      MapTool.showError("data.error.receivingUpdate", e);
    }
  }

  private void handle(UpdateDataStoreMsg msg) {
    try {
      var dataStore = new DataStoreManager().getDefaultDataStoreForRemoteUpdate();
      new GameDataImporter(dataStore).importData(msg.getStore());
    } catch (ExecutionException | InterruptedException e) {
      MapTool.showError("data.error.receivingUpdate", e);
    }
  }

  private void handle(RemoveAllAddOnLibrariesMsg msg) {
    new LibraryManager().deregisterAddOnLibraries();
  }

  private void handle(RemoveAddOnLibraryMsg msg) {
    var remLibraryManager = new LibraryManager();
    var removedNamespaces = msg.getNamespacesList();
    for (String namespace : removedNamespaces) {
      remLibraryManager.deregisterAddOnLibrary(namespace);
    }
  }

  private void handle(AddAddOnLibraryMsg msg) {
    var addedLibs =
        msg.getAddOnsList().stream()
            .map(TransferableAddOnLibrary::fromDto)
            .collect(Collectors.toList());
    for (var lib : addedLibs) {
      AssetManager.getAssetAsynchronously(
          lib.getAssetKey(),
          a -> {
            Asset asset = AssetManager.getAsset(a);
            try {
              var addOnLibrary = new AddOnLibraryImporter().importFromAsset(asset);
              new LibraryManager().reregisterAddOnLibrary(addOnLibrary);
            } catch (IOException e) {
              SwingUtilities.invokeLater(
                  () ->
                      MapTool.showError(
                          I18N.getText("library.import.error", lib.getNamespace()), e));
            }
          });
    }
  }

  private void handle(UpdateAssetTransferMsg msg) {
    try {
      MapTool.getAssetTransferManager().update(msg.getChunk());
    } catch (IOException ioe) {
      log.error(ioe.toString());
    }
  }

  private void handle(StartAssetTransferMsg msg) {
    AssetHeader header = AssetHeader.fromDto(msg.getHeader());
    MapTool.getAssetTransferManager().addConsumer(new AssetConsumer(AppUtil.getTmpDir(), header));
  }

  private void handle(SetZoneHasFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          boolean hasFog = msg.getHasFow();

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.setHasFog(hasFog);

          // In case we're looking at the zone
          MapTool.getFrame().refresh();
        });
  }

  private void handle(SetZoneGridSizeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          int xOffset = msg.getXOffset();
          int yOffset = msg.getYOffset();
          int size = msg.getSize();
          int color = msg.getColor();

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.getGrid().setSize(size);
          zone.getGrid().setOffset(xOffset, yOffset);
          zone.setGridColor(color);

          MapTool.getFrame().refresh();
        });
  }

  private void handle(SetVisionTypeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          VisionType visionType = VisionType.valueOf(msg.getVision().name());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          if (zone != null) {
            zone.setVisionType(visionType);
            if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
              MapTool.getFrame().getCurrentZoneRenderer().flushFog();
              MapTool.getFrame().getCurrentZoneRenderer().getZoneView().flush();
            }
            MapTool.getFrame().refresh();
          }
        });
  }

  private void handle(SetTokenLocationMsg msg) {
    EventQueue.invokeLater(
        () -> {
          // Only the table should process this
          if (MapTool.getPlayer().getName().equalsIgnoreCase("Table")) {
            var zoneGUID = GUID.valueOf(msg.getZoneGuid());
            var keyToken = GUID.valueOf(msg.getTokenGuid());

            // This X,Y is the where the center of the token needs to be placed in
            // relation to the screen. So 0,0 would be top left which means only 1/4
            // of token would be drawn. 1024,768 would be lower right (on my table).
            var x = msg.getLocation().getX();
            var y = msg.getLocation().getY();

            // Get the zone
            var zone = MapTool.getCampaign().getZone(zoneGUID);
            // Get the token
            var token = zone.getToken(keyToken);

            Grid grid = zone.getGrid();
            // Convert the X/Y to the screen point
            var renderer = MapTool.getFrame().getZoneRenderer(zone);
            CellPoint newPoint = renderer.getCellAt(new ScreenPoint(x, y));
            ZonePoint zp2 = grid.convert(newPoint);

            token.setX(zp2.x);
            token.setY(zp2.y);

            MapTool.serverCommand().putToken(zoneGUID, token);
          }
        });
  }

  private void handle(SetLiveTypingLabelMsg msg) {
    EventQueue.invokeLater(
        () -> {
          if (msg.getTyping()) {
            // add a typer
            MapTool.getFrame().getChatNotificationTimers().setChatTyper(msg.getPlayerName());
          } else {
            // remove typer from list
            MapTool.getFrame().getChatNotificationTimers().removeChatTyper(msg.getPlayerName());
          }
        });
  }

  private void handle(SetFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var area = Mapper.map(msg.getArea());
          var selectedTokens =
              msg.getSelectedTokensList().stream().map(GUID::valueOf).collect(Collectors.toSet());

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.setFogArea(area, selectedTokens);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(SetCampaignNameMsg msg) {
    EventQueue.invokeLater(
        () -> {
          MapTool.getCampaign().setName(msg.getName());
          MapTool.getFrame().setTitle();
        });
  }

  private void handle(SetCampaignMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Campaign campaign = Campaign.fromDto(msg.getCampaign());
          MapTool.setCampaign(campaign);

          // Hide the "Connecting" overlay
          MapTool.getFrame().hideGlassPane();
        });
  }

  private void handle(SetBoardMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);

          Point boardXY = Mapper.map(msg.getPoint());
          var assetId = new MD5Key(msg.getAssetId());
          zone.setBoard(boardXY, assetId);
        });
  }

  private void handle(RestoreZoneViewMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          MapTool.getFrame().getZoneRenderer(zoneGUID).restoreView();
        });
  }

  private void handle(RenameZoneMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          String name = msg.getName();

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          if (zone != null) {
            zone.setName(name);
          }
          MapTool.getFrame().setTitleViaRenderer(MapTool.getFrame().getCurrentZoneRenderer());
        });
  }

  private void handle(RemoveZoneMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          final var renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          final var zone = renderer.getZone();
          MapTool.getCampaign().removeZone(zoneGUID);
          MapTool.getFrame().removeZoneRenderer(renderer);

          // Now we have fire off adding the tokens in the zone
          new MapToolEventBus()
              .getMainEventBus()
              .post(new TokensRemoved(zone, zone.getAllTokens()));
          new MapToolEventBus().getMainEventBus().post(new ZoneRemoved(zone));
        });
  }

  private void handle(RemoveTopologyMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var area = Mapper.map(msg.getArea());
          var topologyType = Zone.TopologyType.valueOf(msg.getType().name());

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.removeTopology(area, topologyType);

          MapTool.getFrame().getZoneRenderer(zoneGUID).repaint();
        });
  }

  private void handle(RemoveTokensMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          var tokenGUIDs =
              msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toList());
          zone.removeTokens(tokenGUIDs);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(RemoveTokenMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          var tokenGUID = GUID.valueOf(msg.getTokenGuid());
          zone.removeToken(tokenGUID);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(RemoveLabelMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          GUID labelGUID = GUID.valueOf(msg.getLabelGuid());
          zone.removeLabel(labelGUID);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(PutZoneMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = Zone.fromDto(msg.getZone());
          MapTool.getCampaign().putZone(zone);

          // TODO: combine this with MapTool.addZone()
          var renderer = ZoneRendererFactory.newRenderer(zone);
          MapTool.getFrame().addZoneRenderer(renderer);
          if (MapTool.getFrame().getCurrentZoneRenderer() == null && zone.isVisible()) {
            MapTool.getFrame().setCurrentZoneRenderer(renderer);
          }

          new MapToolEventBus().getMainEventBus().post(new ZoneAdded(zone));
          // Now we have fire off adding the tokens in the zone
          new MapToolEventBus().getMainEventBus().post(new TokensAdded(zone, zone.getAllTokens()));
        });
  }

  private void handle(PutLabelMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          Label label = Label.fromDto(msg.getLabel());
          zone.putLabel(label);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(PutAssetMsg msg) {
    AssetManager.putAsset(Asset.fromDto(msg.getAsset()));
    EventQueue.invokeLater(
        () -> {
          MapTool.getFrame().getCurrentZoneRenderer().flushDrawableRenderer();
          MapTool.getFrame().refresh();
        });
  }

  private void handle(PlayerDisconnectedMsg msg) {
    EventQueue.invokeLater(
        () -> {
          MapTool.removePlayer(Player.fromDto(msg.getPlayer()));
          MapTool.getFrame().refresh();
        });
  }

  private void handle(PlayerConnectedMsg msg) {
    EventQueue.invokeLater(
        () -> {
          MapTool.addPlayer(Player.fromDto(msg.getPlayer()));
          MapTool.getFrame().refresh();
        });
  }

  private void handle(MovePointerMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Pointer pointer = MapTool.getFrame().getPointerOverlay().getPointer(msg.getPlayer());
          if (pointer == null) {
            return;
          }
          pointer.setX(msg.getX());
          pointer.setY(msg.getY());

          MapTool.getFrame().refresh();
        });
  }

  private void handle(MessageMsg msg) {
    EventQueue.invokeLater(
        () -> {
          TextMessage message = TextMessage.fromDto(msg.getMessage());
          MapTool.addServerMessage(message);
        });
  }

  private void handle(HidePointerMsg msg) {
    EventQueue.invokeLater(
        () -> {
          MapTool.getFrame().getPointerOverlay().removePointer(msg.getPlayer());
          MapTool.getFrame().refresh();
        });
  }

  private void handle(HideFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var area = Mapper.map(msg.getArea());
          var selectedTokens =
              msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toSet());

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.hideArea(area, selectedTokens);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(ExposePcAreaMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGuid = GUID.valueOf(msg.getZoneGuid());
          var renderer = MapTool.getFrame().getZoneRenderer(zoneGuid);
          FogUtil.exposePCArea(renderer);
        });
  }

  private void handle(ExposeFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          Area area = Mapper.map(msg.getArea());
          var selectedTokens =
              msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toSet());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.exposeArea(area, selectedTokens);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(ExecLinkMsg msg) {
    EventQueue.invokeLater(
        () -> MacroLinkFunction.receiveExecLink(msg.getLink(), msg.getTarget(), msg.getSource()));
  }

  private void handle(ExecFunctionMsg msg) {
    EventQueue.invokeLater(
        () ->
            ExecFunction.receiveExecFunction(
                msg.getTarget(),
                msg.getSource(),
                msg.getFunctionName(),
                Mapper.map(msg.getArgumentList())));
  }

  private void handle(EnforceZoneViewMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          int x = msg.getX();
          int y = msg.getY();
          double scale = msg.getScale();
          int gmWidth = msg.getGmWidth();
          int gmHeight = msg.getGmHeight();

          var renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          if (renderer == null) {
            return;
          }
          if (AppPreferences.getFitGMView()) {
            renderer.enforceView(x, y, scale, gmWidth, gmHeight);
          } else {
            renderer.setScale(scale);
            renderer.centerOn(new ZonePoint(x, y));
          }
        });
  }

  private void handle(EnforceZoneMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);

          if (renderer != null
              && renderer != MapTool.getFrame().getCurrentZoneRenderer()
              && (renderer.getZone().isVisible() || MapTool.getPlayer().isGM())) {
            MapTool.getFrame().setCurrentZoneRenderer(renderer);
          }
        });
  }

  private void handle(EnforceNotificationMsg msg) {
    EventQueue.invokeLater(
        () -> MapTool.getFrame().getCommandPanel().disableNotifyButton(msg.getEnforce()));
  }

  private void handle(PutTokenMsg putTokenMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(putTokenMsg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          var token = Token.fromDto(putTokenMsg.getToken());
          zone.putToken(token);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(EditTokenMsg editTokenMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(editTokenMsg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          var token = Token.fromDto(editTokenMsg.getToken());
          zone.editToken(token);
          MapTool.getFrame().refresh();
        });
  }

  private void handle(DrawMsg drawMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGuid = GUID.valueOf(drawMsg.getZoneGuid());
          Pen pen = Pen.fromDto(drawMsg.getPen());
          Drawable drawable = Drawable.fromDto(drawMsg.getDrawable());

          var zone = MapTool.getCampaign().getZone(zoneGuid);
          zone.addDrawable(new DrawnElement(drawable, pen));
          MapTool.getFrame().refresh();
        });
  }

  private void handle(ClearExposedAreaMsg clearExposedAreaMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(clearExposedAreaMsg.getZoneGuid());
          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.clearExposedArea(clearExposedAreaMsg.getGlobalOnly());
        });
  }

  private void handle(ClearAllDrawingsMsg clearAllDrawingsMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(clearAllDrawingsMsg.getZoneGuid());
          var layer = Zone.Layer.valueOf(clearAllDrawingsMsg.getLayer());

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.clearDrawables(zone.getDrawnElements(layer));
          MapTool.getFrame().refresh();
        });
  }

  private void handle(ChangeZoneDisplayNameMsg changeZoneDisplayNameMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(changeZoneDisplayNameMsg.getZoneGuid());
          String displayName = changeZoneDisplayNameMsg.getName();

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          if (zone != null) {
            zone.setPlayerAlias(displayName);
          }
          MapTool.getFrame()
              .setTitleViaRenderer(
                  MapTool.getFrame()
                      .getCurrentZoneRenderer()); // fixes a bug where the display name at the
          // program title was not updating
        });
  }

  private void handle(AddTopologyMsg addTopologyMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(addTopologyMsg.getZoneGuid());
          var area = Mapper.map(addTopologyMsg.getArea());
          var topologyType = Zone.TopologyType.valueOf(addTopologyMsg.getType().name());

          var zone = MapTool.getCampaign().getZone(zoneGUID);
          zone.addTopology(area, topologyType);

          MapTool.getFrame().getZoneRenderer(zoneGUID).repaint();
        });
  }

  private void handle(BootPlayerMsg bootPlayerMsg) {
    String playerName = bootPlayerMsg.getPlayerName();
    if (MapTool.getPlayer().getName().equals(playerName))
      EventQueue.invokeLater(
          () -> {
            ServerDisconnectHandler.disconnectExpected = true;
            AppActions.disconnectFromServer();
            MapTool.showInformation("You have been booted from the server.");
          });
  }

  private void handle(UpdatePlayerStatusMsg updatePlayerStatusMsg) {
    var playerName = updatePlayerStatusMsg.getPlayer();
    var zoneGUID = GUID.valueOf(updatePlayerStatusMsg.getZoneGuid());
    var loaded = updatePlayerStatusMsg.getLoaded();

    Player player =
        MapTool.getPlayerList().stream()
            .filter(x -> x.getName().equals(playerName))
            .findFirst()
            .orElse(null);

    if (player == null) {
      log.warn("UpdatePlayerStatusMsg failed. No player with name: '" + playerName + "'");
      return;
    }

    player.setZoneId(zoneGUID);
    player.setLoaded(loaded);

    final var eventBus = new MapToolEventBus().getMainEventBus();
    eventBus.post(new PlayerStatusChanged(player));
  }
}
