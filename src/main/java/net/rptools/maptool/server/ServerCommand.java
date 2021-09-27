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
import net.rptools.maptool.model.Zone.TopologyMode;
import net.rptools.maptool.model.Zone.VisionType;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;

public interface ServerCommand {
  enum COMMAND {
    // @formatter:off
    setCampaign,
    setCampaignName,
    getZone,
    putZone,
    removeZone,
    putAsset,
    getAsset,
    removeAsset,
    putToken,
    editToken,
    removeToken,
    removeTokens,
    updateTokenProperty,
    updateDrawing,
    setZoneGridSize,
    message,
    execLink,
    execFunction,
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
    enforceZone,
    setServerPolicy,
    removeTopology,
    renameZone,
    heartbeat,
    updateCampaign,
    updateInitiative,
    updateTokenInitiative,
    setVisionType,
    updateCampaignMacros,
    updateGmMacros,
    setTokenLocation, // NOTE: This is to support third party token placement and shouldn't be
    // depended on for general purpose token movement
    setLiveTypingLabel, // Experimental
    enforceNotification, // Override toggle button to show typing notifications
    exposePCArea,
    setBoard,
    updateExposedAreaMeta,
    restoreZoneView // Jamz: New command to restore player's view and let GM temporarily center and
    // scale a player's view
    // @formatter:on
  }

  void bootPlayer(String player);

  void setZoneHasFoW(GUID zoneGUID, boolean hasFog);

  void exposeFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks);

  void hideFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks);

  void setFoW(GUID zoneGUID, Area area, Set<GUID> selectedToks);

  void addTopology(GUID zoneGUID, Area area, TopologyMode topologyMode);

  void removeTopology(GUID zoneGUID, Area area, TopologyMode topologyMode);

  void enforceZoneView(GUID zoneGUID, int x, int y, double scale, int width, int height);

  void restoreZoneView(GUID zoneGUID);

  void setCampaign(Campaign campaign);

  void setCampaignName(String name);

  void getZone(GUID zoneGUID);

  void putZone(Zone zone);

  void removeZone(GUID zoneGUID);

  void setZoneVisibility(GUID zoneGUID, boolean visible);

  void putAsset(Asset asset);

  void getAsset(MD5Key assetID);

  void removeAsset(MD5Key assetID);

  void editToken(GUID zoneGUID, Token token);

  void putToken(GUID zoneGUID, Token token);

  /**
   * Removes a token from a zone.
   *
   * @param zoneGUID the ID of the zone
   * @param tokenGUID the ID of the token
   */
  void removeToken(GUID zoneGUID, GUID tokenGUID);

  /**
   * Removes a list of tokens from a zone.
   *
   * @param zoneGUID the ID of the zone
   * @param tokenGUIDs the list of IDs of the tokens
   */
  void removeTokens(GUID zoneGUID, List<GUID> tokenGUIDs);

  void updateTokenProperty(
      GUID zoneGUID, GUID tokenGUID, Token.Update update, Object[] parameters);

  void updateTokenProperty(Token token, Token.Update update, Object... parameters);

  void putLabel(GUID zoneGUID, Label label);

  void removeLabel(GUID zoneGUID, GUID labelGUID);

  void draw(GUID zoneGUID, Pen pen, Drawable drawable);

  void updateDrawing(GUID zoneGUID, Pen pen, DrawnElement drawnElement);

  void undoDraw(GUID zoneGUID, GUID drawableGUID);

  void setZoneGridSize(GUID zoneGUID, int xOffset, int yOffset, int size, int color);

  void message(TextMessage message);

  void execFunction(String target, String source, String functionName, List<Object> args);

  void execLink(String link, String target, String source);

  void showPointer(String player, Pointer pointer);

  void hidePointer(String player);

  void movePointer(String player, int x, int y);

  void startTokenMove(String playerId, GUID zoneGUID, GUID tokenGUID, Set<GUID> tokenList);

  void updateTokenMove(GUID zoneGUID, GUID tokenGUID, int x, int y);

  void stopTokenMove(GUID zoneGUID, GUID tokenGUID);

  void toggleTokenMoveWaypoint(GUID zoneGUID, GUID tokenGUID, ZonePoint cp);

  void sendTokensToBack(GUID zoneGUID, Set<GUID> tokenSet);

  void bringTokensToFront(GUID zoneGUID, Set<GUID> tokenSet);

  void clearAllDrawings(GUID zoneGUID, Zone.Layer layer);

  void enforceZone(GUID zoneGUID);

  void setServerPolicy(ServerPolicy policy);

  void renameZone(GUID zoneGUID, String name);

  void changeZoneDispName(GUID zoneGUID, String name);

  void heartbeat(String data);

  void updateCampaign(CampaignProperties properties);

  void updateInitiative(InitiativeList list, Boolean ownerPermission);

  void updateTokenInitiative(
      GUID zone, GUID token, Boolean hold, String state, Integer index);

  void setVisionType(GUID zoneGUID, VisionType visionType);

  void updateCampaignMacros(List<MacroButtonProperties> properties);

  void updateGmMacros(List<MacroButtonProperties> properties);

  void setBoard(GUID zoneGUID, MD5Key mapAsset, int X, int Y);

  void setLiveTypingLabel(String name, boolean show);

  void enforceNotification(Boolean enforce);

  void exposePCArea(GUID zoneGUID);

  void updateExposedAreaMeta(
      GUID zoneGUID, GUID tokenExposedAreaGUID, ExposedAreaMetaData meta);

  void clearExposedArea(GUID zoneGUID, boolean globalOnly);
}
