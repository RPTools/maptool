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

import static net.rptools.maptool.server.proto.Message.MessageTypeCase.HEARTBEAT_MSG;

import java.awt.EventQueue;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.rptools.clientserver.simple.MessageHandler;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.ClientMessageHandler;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ServerCommandClientImpl;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.common.MapToolConstants;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.zones.TokensAdded;
import net.rptools.maptool.model.zones.TokensRemoved;
import net.rptools.maptool.model.zones.ZoneAdded;
import net.rptools.maptool.model.zones.ZoneRemoved;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.transfer.AssetProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is used by the server host to receive client commands sent through {@link
 * ServerCommandClientImpl ServerCommandClientImpl}. Once the command is received, this will update
 * the server data, before forwarding the command to the clients. Clients will then handle the
 * command through {@link ClientMessageHandler ClientMethodHandler}. Updating the server itself is
 * important as new client receive the server's campaign data when connecting.
 *
 * @author drice *
 */
public class ServerMessageHandler implements MessageHandler {
  private final MapToolServer server;
  private static final Logger log = LogManager.getLogger(ServerMessageHandler.class);

  public ServerMessageHandler(MapToolServer server) {
    this.server = server;
  }

  @Override
  public void handleMessage(String id, byte[] message) {
    try {
      var msg = Message.parseFrom(message);
      var msgType = msg.getMessageTypeCase();

      log.debug("from " + id + " got: " + msgType);

      // We don't do anything with heartbeats they are only there to avoid routers dropping the
      // connection. So just ignore then.
      if (msgType == HEARTBEAT_MSG) {
        return;
      }

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
        case ENFORCE_NOTIFICATION_MSG,
            ENFORCE_ZONE_MSG,
            ENFORCE_ZONE_VIEW_MSG,
            EXEC_LINK_MSG,
            EXEC_FUNCTION_MSG,
            MESSAGE_MSG,
            SET_BOARD_MSG,
            RESTORE_ZONE_VIEW_MSG,
            SET_LIVE_TYPING_LABEL_MSG,
            SET_TOKEN_LOCATION_MSG,
            START_TOKEN_MOVE_MSG,
            STOP_TOKEN_MOVE_MSG,
            TOGGLE_TOKEN_MOVE_WAYPOINT_MSG,
            UPDATE_TOKEN_MOVE_MSG,
            ADD_ADD_ON_LIBRARY_MSG,
            REMOVE_ADD_ON_LIBRARY_MSG,
            REMOVE_ALL_ADD_ON_LIBRARIES_MSG,
            UPDATE_DATA_STORE_MSG,
            UPDATE_DATA_NAMESPACE_MSG,
            UPDATE_DATA_MSG,
            REMOVE_DATA_MSG,
            REMOVE_DATA_NAMESPACE_MSG,
            REMOVE_DATA_STORE_MSG -> sendToClients(id, msg);
        case EXPOSE_FOW_MSG -> {
          handle(msg.getExposeFowMsg());
          sendToClients(id, msg);
        }
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
        case HIDE_POINTER_MSG, MOVE_POINTER_MSG, SHOW_POINTER_MSG -> sendToAllClients(msg);
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
        case REMOVE_ASSET_MSG -> handle(msg.getRemoveAssetMsg());
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
        case SEND_TOKENS_TO_BACK_MSG -> handle(msg.getSendTokensToBackMsg());
        case SET_CAMPAIGN_MSG -> {
          handle(msg.getSetCampaignMsg());
          sendToClients(id, msg);
        }
        case SET_CAMPAIGN_NAME_MSG -> {
          handle(msg.getSetCampaignNameMsg());
          sendToClients(id, msg);
        }
        case SET_FOW_MSG -> {
          handle(msg.getSetFowMsg());
          sendToAllClients(msg);
        }
        case SET_VISION_TYPE_MSG -> {
          handle(msg.getSetVisionTypeMsg());
          sendToAllClients(msg);
        }
        case SET_ZONE_GRID_SIZE_MSG -> {
          handle(msg.getSetZoneGridSizeMsg());
          sendToAllClients(msg);
        }
        case SET_ZONE_HAS_FOW_MSG -> {
          handle(msg.getSetZoneHasFowMsg());
          sendToAllClients(msg);
        }
        case UPDATE_DRAWING_MSG -> {
          handle(msg.getUpdateDrawingMsg());
          sendToAllClients(msg);
        }
        case UPDATE_TOKEN_PROPERTY_MSG -> {
          handle(msg.getUpdateTokenPropertyMsg());
          sendToClients(id, msg);
        }
        case SET_ZONE_VISIBILITY_MSG -> {
          handle(msg.getSetZoneVisibilityMsg());
          sendToAllClients(msg);
        }
        case UNDO_DRAW_MSG -> {
          sendToAllClients(msg);
          handle(msg.getUndoDrawMsg());
        }
        case SET_SERVER_POLICY_MSG -> {
          handle(msg.getSetServerPolicyMsg());
          sendToClients(id, msg);
        }
        case UPDATE_CAMPAIGN_MSG -> {
          handle(msg.getUpdateCampaignMsg());
          sendToClients(id, msg);
        }
        case UPDATE_INITIATIVE_MSG -> {
          handle(msg.getUpdateInitiativeMsg());
          sendToAllClients(msg);
        }
        case UPDATE_TOKEN_INITIATIVE_MSG -> {
          handle(msg.getUpdateTokenInitiativeMsg());
          sendToAllClients(msg);
        }
        case UPDATE_CAMPAIGN_MACROS_MSG -> {
          handle(msg.getUpdateCampaignMacrosMsg());
          sendToClients(id, msg);
        }
        case UPDATE_GM_MACROS_MSG -> {
          handle(msg.getUpdateGmMacrosMsg());
          sendToClients(id, msg);
        }
        case UPDATE_EXPOSED_AREA_META_MSG -> {
          handle(msg.getUpdateExposedAreaMetaMsg());
          sendToClients(id, msg);
        }
        case UPDATE_PLAYER_STATUS_MSG -> {
          handle(id, msg.getUpdatePlayerStatusMsg());
          sendToClients(id, msg);
        }

        default -> log.warn(msgType + " not handled.");
      }
      log.debug("from " + id + " handled: " + msgType);
    } catch (Exception e) {
      MapTool.showError("Unexpected error during message handling", e);
    }
  }

  private void handle(UpdateExposedAreaMetaMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          zone.setExposedAreaMetaData(
              msg.hasTokenGuid() ? GUID.valueOf(msg.getTokenGuid().getValue()) : null,
              new ExposedAreaMetaData(Mapper.map(msg.getArea()))); // update the server
        });
  }

  private void handle(UpdateGmMacrosMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var campaignMacros =
              msg.getMacrosList().stream()
                  .map(MacroButtonProperties::fromDto)
                  .collect(Collectors.toList());
          MapTool.getCampaign().setGmMacroButtonPropertiesArray(campaignMacros);
          server.getCampaign().setGmMacroButtonPropertiesArray(campaignMacros);
        });
  }

  private void handle(UpdateCampaignMacrosMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var campaignMacros =
              msg.getMacrosList().stream()
                  .map(MacroButtonProperties::fromDto)
                  .collect(Collectors.toList());
          MapTool.getCampaign().setMacroButtonPropertiesArray(campaignMacros);
          server.getCampaign().setMacroButtonPropertiesArray(campaignMacros);
        });
  }

  private void handle(UpdateTokenInitiativeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          var tokenId = GUID.valueOf(msg.getTokenGuid());
          InitiativeList list = zone.getInitiativeList();
          TokenInitiative ti = list.getTokenInitiative(msg.getIndex());
          if (!ti.getId().equals(tokenId)) {
            // Index doesn't point to same token, try to find it
            Token token = zone.getToken(tokenId);
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
            var list = InitiativeList.fromDto(msg.getList());
            if (list.getZone() == null) return;
            Zone zone = server.getCampaign().getZone(list.getZone().getId());
            zone.setInitiativeList(list);
          } else if (msg.hasOwnerPermission()) {
            MapTool.getFrame()
                .getInitiativePanel()
                .setOwnerPermissions(msg.getOwnerPermission().getValue());
          }
        });
  }

  private void handle(UpdateCampaignMsg msg) {
    EventQueue.invokeLater(
        () -> {
          server
              .getCampaign()
              .replaceCampaignProperties(CampaignProperties.fromDto(msg.getProperties()));
        });
  }

  private void handle(SetServerPolicyMsg msg) {
    EventQueue.invokeLater(
        () -> {
          server.updateServerPolicy(
              ServerPolicy.fromDto(msg.getPolicy())); // updates the server policy, fixes #1648
          MapTool.getFrame().getToolbox().updateTools();
        });
  }

  private void handle(UndoDrawMsg msg) {
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
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          zone.removeDrawable(GUID.valueOf(msg.getDrawableGuid()));
        });
  }

  private void handle(SetZoneVisibilityMsg msg) {
    EventQueue.invokeLater(
        () -> {
          server
              .getCampaign()
              .getZone(GUID.valueOf(msg.getZoneGuid()))
              .setVisible(msg.getIsVisible());
        });
  }

  private void handle(UpdateTokenPropertyMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          Token token = zone.getToken(GUID.valueOf(msg.getTokenGuid()));
          token.updateProperty(
              zone,
              Token.Update.valueOf(msg.getProperty().name()),
              msg.getValuesList()); // update server version of token
        });
  }

  private void handle(UpdateDrawingMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          zone.updateDrawable(DrawnElement.fromDto(msg.getDrawing()), Pen.fromDto(msg.getPen()));
        });
  }

  private void handle(SetZoneHasFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          zone.setHasFog(msg.getHasFow());
        });
  }

  private void handle(SetZoneGridSizeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          Grid grid = zone.getGrid();
          grid.setSize(msg.getSize());
          grid.setOffset(msg.getXOffset(), msg.getYOffset());
          zone.setGridColor(msg.getColor());
        });
  }

  private void handle(SetVisionTypeMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          zone.setVisionType(VisionType.valueOf(msg.getVision().name()));
        });
  }

  private void handle(SetFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          var area = Mapper.map(msg.getArea());
          var selectedTokens =
              msg.getSelectedTokensList().stream().map(GUID::valueOf).collect(Collectors.toSet());
          zone.setFogArea(area, selectedTokens);
        });
  }

  private void handle(SetCampaignNameMsg msg) {
    EventQueue.invokeLater(
        () -> {
          server.getCampaign().setName(msg.getName());
        });
  }

  private void handle(SetCampaignMsg msg) {
    EventQueue.invokeLater(
        () -> {
          server.setCampaign(Campaign.fromDto(msg.getCampaign()));
        });
  }

  private void handle(SendTokensToBackMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGuid = GUID.valueOf(msg.getZoneGuid());
          var tokens =
              msg.getTokenGuidsList().stream().map(GUID::valueOf).collect(Collectors.toSet());
          sendTokensToBack(zoneGuid, tokens);
        });
  }

  private void handle(RenameZoneMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var name = msg.getName();
          Zone zone = server.getCampaign().getZone(zoneGUID);
          if (zone != null) {
            zone.setName(name);
          }
        });
  }

  private void handle(RemoveZoneMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var zone = server.getCampaign().getZone(zoneGUID);
          server.getCampaign().removeZone(zoneGUID);

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
          Zone zone = server.getCampaign().getZone(zoneGUID);
          zone.removeTopology(area, topologyType);
        });
  }

  private void handle(RemoveTokensMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var tokenGUIDs =
              msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toList());
          Zone zone = server.getCampaign().getZone(zoneGUID);
          zone.removeTokens(tokenGUIDs); // remove server tokens
        });
  }

  private void handle(RemoveTokenMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var tokenGUID = GUID.valueOf(msg.getTokenGuid());
          var zone = server.getCampaign().getZone(zoneGUID);
          zone.removeToken(tokenGUID); // remove server tokens
        });
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
    EventQueue.invokeLater(
        () -> {
          final var zone = Zone.fromDto(msg.getZone());
          server.getCampaign().putZone(zone);

          // Now we have fire off adding the tokens in the zone
          new MapToolEventBus().getMainEventBus().post(new ZoneAdded(zone));
          new MapToolEventBus().getMainEventBus().post(new TokensAdded(zone, zone.getAllTokens()));
        });
  }

  private void handle(PutLabelMsg msg) {
    EventQueue.invokeLater(
        () -> {
          Zone zone = server.getCampaign().getZone(GUID.valueOf(msg.getZoneGuid()));
          zone.putLabel(Label.fromDto(msg.getLabel()));
        });
  }

  private void handle(PutAssetMsg msg) {
    EventQueue.invokeLater(
        () -> {
          AssetManager.putAsset(Asset.fromDto(msg.getAsset()));
        });
  }

  private void handle(HideFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          var area = Mapper.map(msg.getArea());
          var selectedTokens =
              msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toSet());

          Zone zone = server.getCampaign().getZone(zoneGUID);
          zone.hideArea(area, selectedTokens);
        });
  }

  private void handle(String id, GetZoneMsg msg) {
    getZone(id, GUID.valueOf(msg.getZoneGuid()));
  }

  private void handle(String id, GetAssetMsg msg) {
    getAsset(id, new MD5Key(msg.getAssetId()));
  }

  private void handle(ExposePcAreaMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          ZoneRenderer renderer = MapTool.getFrame().getZoneRenderer(zoneGUID);
          FogUtil.exposePCArea(renderer);
        });
  }

  private void handle(ExposeFowMsg msg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(msg.getZoneGuid());
          Zone zone = server.getCampaign().getZone(zoneGUID);
          Area area = Mapper.map(msg.getArea());
          var selectedTokens =
              msg.getTokenGuidList().stream().map(GUID::valueOf).collect(Collectors.toSet());
          zone.exposeArea(area, selectedTokens);
        });
  }

  private void handle(String clientId, PutTokenMsg putTokenMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(putTokenMsg.getZoneGuid());
          var token = Token.fromDto(putTokenMsg.getToken());
          putToken(clientId, zoneGUID, token);
        });
  }

  private void handle(String clientId, EditTokenMsg editTokenMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(editTokenMsg.getZoneGuid());
          var token = Token.fromDto(editTokenMsg.getToken());
          putToken(clientId, zoneGUID, token);
        });
  }

  private void handle(DrawMsg drawMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGuid = GUID.valueOf(drawMsg.getZoneGuid());
          var pen = Pen.fromDto(drawMsg.getPen());
          var drawable = Drawable.fromDto(drawMsg.getDrawable());
          Zone zone = server.getCampaign().getZone(zoneGuid);
          zone.addDrawable(new DrawnElement(drawable, pen));
        });
  }

  private void handle(ClearExposedAreaMsg clearExposedAreaMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(clearExposedAreaMsg.getZoneGuid());
          var globalOnly = clearExposedAreaMsg.getGlobalOnly();
          Zone zone = server.getCampaign().getZone(zoneGUID);
          zone.clearExposedArea(globalOnly);
        });
  }

  private void handle(ClearAllDrawingsMsg clearAllDrawingsMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(clearAllDrawingsMsg.getZoneGuid());
          var layer = Zone.Layer.valueOf(clearAllDrawingsMsg.getLayer());
          Zone zone = server.getCampaign().getZone(zoneGUID);
          List<DrawnElement> list = zone.getDrawnElements(layer);
          zone.clearDrawables(list); // FJE Empties the DrawableUndoManager and empties the list
        });
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
    EventQueue.invokeLater(
        () -> {
          var zoneGuid = GUID.valueOf(bringTokensToFrontMsg.getZoneGuid());
          var tokenSet =
              bringTokensToFrontMsg.getTokenGuidsList().stream()
                  .map(GUID::valueOf)
                  .collect(Collectors.toSet());
          bringTokensToFront(zoneGuid, tokenSet);
        });
  }

  private void handle(AddTopologyMsg addTopologyMsg) {
    EventQueue.invokeLater(
        () -> {
          var zoneGUID = GUID.valueOf(addTopologyMsg.getZoneGuid());
          var area = Mapper.map(addTopologyMsg.getArea());
          var topologyType = Zone.TopologyType.valueOf(addTopologyMsg.getType().name());
          Zone zone = server.getCampaign().getZone(zoneGUID);
          zone.addTopology(area, topologyType);
        });
  }

  private void handle(BootPlayerMsg bootPlayerMsg) {
    // And just to be sure, remove them from the server
    server.releaseClientConnection(server.getConnectionId(bootPlayerMsg.getPlayerName()));
  }

  private void handle(String id, UpdatePlayerStatusMsg updatePlayerStatusMsg) {
    var playerName = updatePlayerStatusMsg.getPlayer();
    var zoneId =
        updatePlayerStatusMsg.getZoneGuid().equals("")
            ? null
            : GUID.valueOf(updatePlayerStatusMsg.getZoneGuid());
    var loaded = updatePlayerStatusMsg.getLoaded();
    server.updatePlayerStatus(playerName, zoneId, loaded);
  }

  private void sendToClients(String excludedId, Message message) {
    server.getConnection().broadcastMessage(new String[] {excludedId}, message);
  }

  private void sendToAllClients(Message message) {
    server.getConnection().broadcastMessage(message);
  }

  private void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet) {
    Zone zone = server.getCampaign().getZone(zoneGUID);

    // Get the tokens to update
    List<Token> tokenList = new ArrayList<>();
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
      var msg = StartAssetTransferMsg.newBuilder().setHeader(producer.getHeader().toDto());
      server
          .getConnection()
          .sendMessage(
              id,
              MapToolConstants.Channel.IMAGE,
              Message.newBuilder().setStartAssetTransferMsg(msg).build());
      server.addAssetProducer(id, producer);

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

  private void putToken(String clientId, GUID zoneGUID, Token token) {
    Zone zone = server.getCampaign().getZone(zoneGUID);

    int zOrder = 0;
    boolean newToken = zone.getToken(token.getId()) == null;
    // Set z-order for new tokens
    if (newToken) {
      zOrder = zone.getLargestZOrder() + 1;
      token.setZOrder(zOrder);
    }
    zone.putToken(token);
    if (newToken) {
      // don't send whole token back to sender, instead just send new ZOrder
      var msg =
          UpdateTokenPropertyMsg.newBuilder()
              .setZoneGuid(zoneGUID.toString())
              .setTokenGuid(token.getId().toString())
              .setProperty(TokenUpdateDto.valueOf(Token.Update.setZOrder.name()))
              .addValues(0, TokenPropertyValueDto.newBuilder().setIntValue(zOrder));
      server
          .getConnection()
          .sendMessage(clientId, Message.newBuilder().setUpdateTokenPropertyMsg(msg).build());
    }
  }

  private void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet) {
    Zone zone = server.getCampaign().getZone(zoneGUID);

    // Get the tokens to update
    List<Token> tokenList = new ArrayList<>();
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
