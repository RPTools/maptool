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

import java.awt.geom.Area;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import net.rptools.lib.MD5Key;
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
import net.rptools.maptool.server.ServerCommand;
import net.rptools.maptool.server.ServerPolicy;

public class ServerCommandClientImpl implements ServerCommand {

  private final TimedEventQueue movementUpdateQueue = new TimedEventQueue(100);
  private final LinkedBlockingQueue<MD5Key> assetRetrieveQueue = new LinkedBlockingQueue<MD5Key>();

  public ServerCommandClientImpl() {
    movementUpdateQueue.start();
    // new AssetRetrievalThread().start();
  }

  public void heartbeat(String data) {
    makeServerCall(COMMAND.heartbeat, data);
  }

  public void movePointer(String player, int x, int y) {
    makeServerCall(COMMAND.movePointer, player, x, y);
  }

  public void bootPlayer(String player) {
    makeServerCall(COMMAND.bootPlayer, player);
  }

  public void setCampaign(Campaign campaign) {
    try {
      campaign.setBeingSerialized(true);
      makeServerCall(COMMAND.setCampaign, campaign);
    } finally {
      campaign.setBeingSerialized(false);
    }
  }

  public void setVisionType(GUID zoneGUID, VisionType visionType) {
    makeServerCall(COMMAND.setVisionType, zoneGUID, visionType);
  }

  public void updateCampaign(CampaignProperties properties) {
    makeServerCall(COMMAND.updateCampaign, properties);
  }

  public void getZone(GUID zoneGUID) {
    makeServerCall(COMMAND.getZone, zoneGUID);
  }

  public void putZone(Zone zone) {
    makeServerCall(COMMAND.putZone, zone);
  }

  public void removeZone(GUID zoneGUID) {
    makeServerCall(COMMAND.removeZone, zoneGUID);
  }

  public void renameZone(GUID zoneGUID, String name) {
    makeServerCall(COMMAND.renameZone, zoneGUID, name);
  }

  public void putAsset(Asset asset) {
    makeServerCall(COMMAND.putAsset, asset);
  }

  public void getAsset(MD5Key assetID) {
    makeServerCall(COMMAND.getAsset, assetID);
  }

  public void removeAsset(MD5Key assetID) {
    makeServerCall(COMMAND.removeAsset, assetID);
  }

  public void enforceZoneView(GUID zoneGUID, int x, int y, double scale, int width, int height) {
    makeServerCall(COMMAND.enforceZoneView, zoneGUID, x, y, scale, width, height);
  }

  public void restoreZoneView(GUID zoneGUID) {
    makeServerCall(COMMAND.restoreZoneView, zoneGUID);
  }

  public void putToken(GUID zoneGUID, Token token) {
    // Hack to generate zone event. All functions that update tokens call this method
    // after changing the token. But they don't tell the zone about it so classes
    // waiting for the zone change event don't get it.
    MapTool.getCampaign().getZone(zoneGUID).putToken(token);
    makeServerCall(COMMAND.putToken, zoneGUID, token);
  }

  public void removeToken(GUID zoneGUID, GUID tokenGUID) {
    makeServerCall(COMMAND.removeToken, zoneGUID, tokenGUID);
  }

  public void putLabel(GUID zoneGUID, Label label) {
    makeServerCall(COMMAND.putLabel, zoneGUID, label);
  }

  public void removeLabel(GUID zoneGUID, GUID labelGUID) {
    makeServerCall(COMMAND.removeLabel, zoneGUID, labelGUID);
  }

  public void draw(GUID zoneGUID, Pen pen, Drawable drawable) {
    makeServerCall(COMMAND.draw, zoneGUID, pen, drawable);
  }

  public void updateDrawing(GUID zoneGUID, Pen pen, DrawnElement drawnElement) {
    makeServerCall(COMMAND.updateDrawing, zoneGUID, pen, drawnElement);
  }

  public void clearAllDrawings(GUID zoneGUID, Zone.Layer layer) {
    makeServerCall(COMMAND.clearAllDrawings, zoneGUID, layer);
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
    makeServerCall(COMMAND.message, message);
  }

  public void showPointer(String player, Pointer pointer) {
    makeServerCall(COMMAND.showPointer, player, pointer);
  }

  public void hidePointer(String player) {
    makeServerCall(COMMAND.hidePointer, player);
  }

  public void setLiveTypingLabel(String label, boolean show) {
    makeServerCall(COMMAND.setLiveTypingLabel, label, show);
  }

  public void enforceNotification(Boolean enforce) {
    // MapTool.showInformation(enforce.toString());
    makeServerCall(COMMAND.enforceNotification, enforce);
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

  public void addTopology(GUID zoneGUID, Area area) {
    makeServerCall(COMMAND.addTopology, zoneGUID, area);
  }

  public void removeTopology(GUID zoneGUID, Area area) {
    makeServerCall(COMMAND.removeTopology, zoneGUID, area);
  }

  public void exposePCArea(GUID zoneGUID) {
    makeServerCall(COMMAND.exposePCArea, zoneGUID);
  }

  public void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    makeServerCall(COMMAND.exposeFoW, zoneGUID, area, selectedToks);
  }

  public void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    makeServerCall(COMMAND.setFoW, zoneGUID, area, selectedToks);
  }

  public void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks) {
    makeServerCall(COMMAND.hideFoW, zoneGUID, area, selectedToks);
  }

  public void setZoneHasFoW(GUID zoneGUID, boolean hasFog) {
    makeServerCall(COMMAND.setZoneHasFoW, zoneGUID, hasFog);
  }

  public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenList) {
    makeServerCall(COMMAND.bringTokensToFront, zoneGUID, tokenList);
  }

  public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenList) {
    makeServerCall(COMMAND.sendTokensToBack, zoneGUID, tokenList);
  }

  public void enforceZone(GUID zoneGUID) {
    makeServerCall(COMMAND.enforceZone, zoneGUID);
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

  public void clearExposedArea(GUID zoneGUID) {
    // System.out.println("in ServerCommandClientImpl");
    makeServerCall(COMMAND.clearExposedArea, zoneGUID);
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

    Object sleepSemaphore = new Object();

    public TimedEventQueue(long millidelay) {
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
