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

public class ClientCommand {

  public static enum COMMAND {
    // @formatter:off
    startAssetTransfer,
    updateAssetTransfer,
    bootPlayer,
    setCampaign,
    putZone,
    removeZone,
    putAsset,
    getAsset,
    removeAsset,
    putToken,
    updateTokenProperty,
    removeToken,
    draw,
    clearAllDrawings,
    updateDrawing,
    setZoneGridSize,
    setZoneVisibility,
    playerConnected,
    playerDisconnected,
    message,
    execLink,
    undoDraw,
    showPointer,
    hidePointer,
    movePointer,
    startTokenMove,
    stopTokenMove,
    toggleTokenMoveWaypoint,
    updateTokenMove,
    enforceZoneView,
    setZoneHasFoW,
    exposeFoW,
    hideFoW,
    setFoW,
    putLabel,
    removeLabel,
    enforceZone,
    setServerPolicy,
    addTopology,
    removeTopology,
    renameZone,
    updateCampaign,
    updateInitiative,
    updateTokenInitiative,
    setUseVision,
    updateCampaignMacros,
    setTokenLocation, // NOTE: This is to support third party token placement and shouldn't be
    // depended on for general purpose token movement
    setLiveTypingLabel, // Experimental chat notification
    enforceNotification, // enforces notification of typing in the chat window
    exposePCArea,
    setBoard,
    updateExposedAreaMeta,
    clearExposedArea,
    restoreZoneView // Jamz: New command to restore player's view and let GM temporarily center and
    // scale a player's view
    // @formatter:on
  };
}
