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
import java.util.List;
import java.util.Set;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Asset;
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

public interface ServerCommand {
  public static enum COMMAND {
    // @formatter:off
    bootPlayer,
    setCampaign,
    getZone,
    putZone,
    removeZone,
    putAsset,
    getAsset,
    removeAsset,
    putToken,
    removeToken,
    draw,
    updateDrawing,
    clearAllDrawings,
    setZoneGridSize,
    message,
    undoDraw,
    showPointer,
    movePointer,
    hidePointer,
    startTokenMove,
    stopTokenMove,
    toggleTokenMoveWaypoint,
    updateTokenMove,
    setZoneVisibility,
    enforceZoneView,
    setZoneHasFoW,
    exposeFoW,
    hideFoW,
    setFoW,
    putLabel,
    removeLabel,
    sendTokensToBack,
    bringTokensToFront,
    enforceZone,
    setServerPolicy,
    addTopology,
    removeTopology,
    renameZone,
    heartbeat,
    updateCampaign,
    updateInitiative,
    updateTokenInitiative,
    setVisionType,
    updateCampaignMacros,
    setTokenLocation, // NOTE: This is to support third party token placement and shouldn't be
    // depended on for general purpose token movement
    setLiveTypingLabel, // Experimental
    enforceNotification, // Override toggle button to show typing notifications
    exposePCArea,
    setBoard,
    updateExposedAreaMeta,
    clearExposedArea,
    restoreZoneView // Jamz: New command to restore player's view and let GM temporarily center and
    // scale a player's view
    // @formatter:on
  };

  public void bootPlayer(String player);

  public void setZoneHasFoW(GUID zoneGUID, boolean hasFog);

  public void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks);

  public void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks);

  public void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks);

  public void addTopology(GUID zoneGUID, Area area);

  public void removeTopology(GUID zoneGUID, Area area);

  public void enforceZoneView(GUID zoneGUID, int x, int y, double scale, int width, int height);

  public void restoreZoneView(GUID zoneGUID);

  public void setCampaign(Campaign campaign);

  public void getZone(GUID zoneGUID);

  public void putZone(Zone zone);

  public void removeZone(GUID zoneGUID);

  public void setZoneVisibility(GUID zoneGUID, boolean visible);

  public void putAsset(Asset asset);

  public void getAsset(MD5Key assetID);

  public void removeAsset(MD5Key assetID);

  public void putToken(GUID zoneGUID, Token token);

  public void removeToken(GUID zoneGUID, GUID tokenGUID);

  public void putLabel(GUID zoneGUID, Label label);

  public void removeLabel(GUID zoneGUID, GUID labelGUID);

  public void draw(GUID zoneGUID, Pen pen, Drawable drawable);

  public void updateDrawing(GUID zoneGUID, Pen pen, DrawnElement drawnElement);

  public void undoDraw(GUID zoneGUID, GUID drawableGUID);

  public void setZoneGridSize(GUID zoneGUID, int xOffset, int yOffset, int size, int color);

  public void message(TextMessage message);

  public void showPointer(String player, Pointer pointer);

  public void hidePointer(String player);

  public void movePointer(String player, int x, int y);

  public void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList);

  public void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y);

  public void stopTokenMove(GUID zoneGUID, GUID tokenGUID);

  public void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp);

  public void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet);

  public void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet);

  public void clearAllDrawings(GUID zoneGUID, Zone.Layer layer);

  public void enforceZone(GUID zoneGUID);

  public void setServerPolicy(ServerPolicy policy);

  public void renameZone(GUID zoneGUID, String name);

  public void heartbeat(String data);

  public void updateCampaign(CampaignProperties properties);

  public void updateInitiative(InitiativeList list, Boolean ownerPermission);

  public void updateTokenInitiative(
      GUID zone, GUID token, Boolean hold, String state, Integer index);

  public void setVisionType(GUID zoneGUID, VisionType visionType);

  public void updateCampaignMacros(List<MacroButtonProperties> properties);

  public void setBoard(GUID zoneGUID, MD5Key mapAsset, int X, int Y);

  public void setLiveTypingLabel(String name, boolean show);

  public void enforceNotification(Boolean enforce);

  public void exposePCArea(GUID zoneGUID);

  public void updateExposedAreaMeta(
      GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta);

  public void clearExposedArea(GUID zoneGUID);
}
