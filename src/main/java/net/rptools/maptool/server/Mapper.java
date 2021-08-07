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

import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.server.proto.ServerPolicyDto;
import net.rptools.maptool.server.proto.WalkerMetricDto;

public class Mapper {
  public static ServerPolicy map(ServerPolicyDto source) {
    var destination = new ServerPolicy();
    destination.setUseStrictTokenManagement(source.getUseStrictTokenManagement());
    destination.setIsMovementLocked(source.getIsMovementLocked());
    destination.setIsTokenEditorLocked(source.getIsTokenEditorLocked());
    destination.setPlayersCanRevealVision(source.getPlayersCanRevealVision());
    destination.setGmRevealsVisionForUnownedTokens(source.getGmRevealsVisionForUnownedTokens());
    destination.setUseIndividualViews(source.getUseIndividualViews());
    destination.setRestrictedImpersonation(source.getRestrictedImpersonation());
    destination.setPlayersReceiveCampaignMacros(source.getPlayersReceiveCampaignMacros());
    destination.setUseToolTipsForDefaultRollFormat(source.getUseToolTipsForDefaultRollFormat());
    destination.setUseIndividualFOW(source.getUseIndividualFOW());
    destination.setAutoRevealOnMovement(source.getIsAutoRevealOnMovement());
    destination.setIncludeOwnedNPCs(source.getIncludeOwnedNPCs());
    destination.setMovementMetric(WalkerMetric.valueOf(source.getMovementMetric().name()));
    destination.setUsingAstarPathfinding(source.getUsingAstarPathfinding());
    destination.setVblBlocksMove(source.getVblBlocksMove());
    return destination;
  }

  public static ServerPolicyDto map(ServerPolicy source) {
    var destination = ServerPolicyDto.newBuilder();
    destination.setUseStrictTokenManagement(source.useStrictTokenManagement());
    destination.setIsMovementLocked(source.isMovementLocked());
    destination.setIsTokenEditorLocked(source.isTokenEditorLocked());
    destination.setPlayersCanRevealVision(source.getPlayersCanRevealVision());
    destination.setGmRevealsVisionForUnownedTokens(source.getGmRevealsVisionForUnownedTokens());
    destination.setUseIndividualViews(source.isUseIndividualViews());
    destination.setRestrictedImpersonation(source.isRestrictedImpersonation());
    destination.setPlayersReceiveCampaignMacros(source.playersReceiveCampaignMacros());
    destination.setUseToolTipsForDefaultRollFormat(source.getUseToolTipsForDefaultRollFormat());
    destination.setUseIndividualFOW(source.isUseIndividualFOW());
    destination.setIsAutoRevealOnMovement(source.isAutoRevealOnMovement());
    destination.setIncludeOwnedNPCs(source.isIncludeOwnedNPCs());
    destination.setMovementMetric(WalkerMetricDto.valueOf(source.getMovementMetric().name()));
    destination.setUsingAstarPathfinding(source.isUsingAstarPathfinding());
    destination.setVblBlocksMove(source.getVblBlocksMove());
    return destination.build();
  }
}
