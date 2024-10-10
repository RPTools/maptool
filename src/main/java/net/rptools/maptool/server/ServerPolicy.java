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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.startserverdialog.StartServerDialogPreferences;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.server.proto.ServerPolicyDto;
import net.rptools.maptool.server.proto.WalkerMetricDto;

public class ServerPolicy {
  private boolean strictTokenMovement;
  private boolean isMovementLocked;
  private boolean isTokenEditorLocked;
  private boolean playersCanRevealVision;
  private boolean gmRevealsVisionForUnownedTokens;
  private boolean useIndividualViews;
  private boolean restrictedImpersonation;
  private boolean playersReceiveCampaignMacros;
  private boolean useToolTipsForDefaultRollFormat;
  private boolean useIndividualFOW;
  private boolean isAutoRevealOnMovement;
  private boolean includeOwnedNPCs = true; // Include Owned NPC Tokens in FoW views
  private WalkerMetric movementMetric;
  private boolean hidemapselectui;
  private boolean disablePlayerAssetPanel;

  private boolean useAstarPathfinding = AppPreferences.pathfindingEnabled.get();
  private boolean vblBlocksMove = AppPreferences.pathfindingBlockedByVbl.get();

  public ServerPolicy() {
    // Default tool tip usage for inline rolls to user preferences.
    useToolTipsForDefaultRollFormat = AppPreferences.useToolTipForInlineRoll.get();
    // Default movement metric from preferences
    movementMetric = AppPreferences.movementMetric.get();
  }

  public ServerPolicy(ServerPolicy other) {
    this.strictTokenMovement = other.strictTokenMovement;
    this.isMovementLocked = other.isMovementLocked;
    this.isTokenEditorLocked = other.isTokenEditorLocked;
    this.playersCanRevealVision = other.playersCanRevealVision;
    this.gmRevealsVisionForUnownedTokens = other.gmRevealsVisionForUnownedTokens;
    this.useIndividualViews = other.useIndividualViews;
    this.restrictedImpersonation = other.restrictedImpersonation;
    this.playersReceiveCampaignMacros = other.playersReceiveCampaignMacros;
    this.useToolTipsForDefaultRollFormat = other.useToolTipsForDefaultRollFormat;
    this.useIndividualFOW = other.useIndividualFOW;
    this.isAutoRevealOnMovement = other.isAutoRevealOnMovement;
    this.includeOwnedNPCs = other.includeOwnedNPCs;
    this.movementMetric = other.movementMetric;
    this.hidemapselectui = other.hidemapselectui;
    this.disablePlayerAssetPanel = other.disablePlayerAssetPanel;
    this.useAstarPathfinding = other.useAstarPathfinding;
    this.vblBlocksMove = other.vblBlocksMove;
  }

  /**
   * Whether token management can be done by everyone or only the GM and assigned tokens
   *
   * @return true if tokens only can be handled by GM and assignee
   */
  public boolean useStrictTokenManagement() {
    return strictTokenMovement;
  }

  public void setUseStrictTokenManagement(boolean strict) {
    strictTokenMovement = strict;
  }

  public boolean isMovementLocked() {
    return isMovementLocked;
  }

  public void setIsMovementLocked(boolean locked) {
    isMovementLocked = locked;
  }

  public boolean isTokenEditorLocked() {
    return isTokenEditorLocked;
  }

  public void setIsTokenEditorLocked(boolean locked) {
    isTokenEditorLocked = locked;
  }

  public void setPlayersCanRevealVision(boolean flag) {
    playersCanRevealVision = flag;
  }

  public boolean getPlayersCanRevealVision() {
    return playersCanRevealVision;
  }

  public void setGmRevealsVisionForUnownedTokens(boolean flag) {
    gmRevealsVisionForUnownedTokens = flag;
  }

  public boolean getGmRevealsVisionForUnownedTokens() {
    return gmRevealsVisionForUnownedTokens;
  }

  public void setAutoRevealOnMovement(boolean revealFlag) {
    this.isAutoRevealOnMovement = revealFlag;
  }

  public boolean isAutoRevealOnMovement() {
    return isAutoRevealOnMovement;
  }

  public boolean isUseIndividualViews() {
    return useIndividualViews;
  }

  public void setUseIndividualViews(boolean useIndividualViews) {
    this.useIndividualViews = useIndividualViews;
  }

  public boolean isRestrictedImpersonation() {
    return restrictedImpersonation;
  }

  public void setRestrictedImpersonation(boolean restrictimp) {
    restrictedImpersonation = restrictimp;
  }

  public boolean playersReceiveCampaignMacros() {
    return playersReceiveCampaignMacros;
  }

  public void setPlayersReceiveCampaignMacros(boolean flag) {
    playersReceiveCampaignMacros = flag;
  }

  public boolean getMapSelectUIHidden() {
    return hidemapselectui;
  }

  public void setHiddenMapSelectUI(boolean flag) {
    hidemapselectui = flag;
  }

  /**
   * Sets if ToolTips should be used instead of extended output for [ ] rolls with no formatting
   * option.
   *
   * @param flag true if tool tips should be used.
   */
  public void setUseToolTipsForDefaultRollFormat(boolean flag) {
    useToolTipsForDefaultRollFormat = flag;
  }

  /**
   * Gets if ToolTips should be used instead of extended output for [ ] rolls with no formatting
   * option.
   *
   * @return true if tool tips should be used.
   */
  public boolean getUseToolTipsForDefaultRollFormat() {
    return useToolTipsForDefaultRollFormat;
  }

  /**
   * Gets the local server time
   *
   * @return the current server time as the difference, measured in milliseconds, between the now
   *     and midnight, January 1, 1970 UTC
   */
  private long getSystemTime() {
    return System.currentTimeMillis();
  }

  private String getLocalTimeDate() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(cal.getTime());
  }

  private String getTimeDate() {
    return getLocalTimeDate();
  }

  public void setMovementMetric(final WalkerMetric walkerMetric) {
    movementMetric = walkerMetric;
  }

  public WalkerMetric getMovementMetric() {
    return movementMetric;
  }

  public boolean isUseIndividualFOW() {
    return useIndividualFOW;
  }

  public void setUseIndividualFOW(boolean flag) {
    useIndividualFOW = flag;
  }

  /**
   * @return the includeOwnedNPCs
   */
  public boolean isIncludeOwnedNPCs() {
    return includeOwnedNPCs;
  }

  /**
   * @param includeOwnedNPCs the includeOwnedNPCs to set
   */
  public void setIncludeOwnedNPCs(boolean includeOwnedNPCs) {
    this.includeOwnedNPCs = includeOwnedNPCs;
  }

  public boolean isUsingAstarPathfinding() {
    return useAstarPathfinding;
  }

  public void setUsingAstarPathfinding(boolean useAstarPathfinding) {
    this.useAstarPathfinding = useAstarPathfinding;
  }

  public boolean getVblBlocksMove() {
    return vblBlocksMove;
  }

  public void setVblBlocksMove(boolean vblBlocksMove) {
    this.vblBlocksMove = vblBlocksMove;
  }

  public boolean getDisablePlayerAssetPanel() {
    return disablePlayerAssetPanel;
  }

  public void setDisablePlayerAssetPanel(boolean flag) {
    this.disablePlayerAssetPanel = flag;
  }

  /**
   * Retrieves the server side preferences as a json object.
   *
   * @return the server side preferences
   */
  public JsonObject toJSON() {
    JsonObject sinfo = new JsonObject();

    sinfo.addProperty(
        "tooltips for default roll format",
        getUseToolTipsForDefaultRollFormat() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "GM reveals vision for unowned tokens",
        getGmRevealsVisionForUnownedTokens() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "players can reveal", getPlayersCanRevealVision() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "auto reveal on movement", isAutoRevealOnMovement() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty("movement locked", isMovementLocked() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "token editor locked", isTokenEditorLocked() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "restricted impersonation", isRestrictedImpersonation() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "individual views", isUseIndividualViews() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty("individual fow", isUseIndividualFOW() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "strict token management", useStrictTokenManagement() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "players receive campaign macros",
        playersReceiveCampaignMacros() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "hide map select ui", getMapSelectUIHidden() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "disable player asset panel",
        getDisablePlayerAssetPanel() ? BigDecimal.ONE : BigDecimal.ZERO);

    WalkerMetric metric =
        MapTool.isPersonalServer() ? AppPreferences.movementMetric.get() : getMovementMetric();
    sinfo.addProperty("movement metric", metric.name());

    sinfo.addProperty("using ai", isUsingAstarPathfinding() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty("vbl blocks movement", getVblBlocksMove() ? BigDecimal.ONE : BigDecimal.ZERO);

    sinfo.addProperty("timeInMs", getSystemTime());
    sinfo.addProperty("timeDate", getTimeDate());

    JsonArray gms = new JsonArray();

    for (String gm : MapTool.getGMs()) {
      gms.add(gm);
    }
    sinfo.add("gm", gms);
    sinfo.addProperty(
        "hosting server", MapTool.isHostingServer() ? BigDecimal.ONE : BigDecimal.ZERO);

    sinfo.addProperty(
        "personal server", MapTool.isPersonalServer() ? BigDecimal.ONE : BigDecimal.ZERO);

    StartServerDialogPreferences prefs = new StartServerDialogPreferences();
    sinfo.addProperty("useWebRTC", prefs.getUseWebRtc() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "usePasswordFile", prefs.getUsePasswordFile() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty("server name", prefs.getRPToolsName());
    sinfo.addProperty("port number", prefs.getPort());
    return sinfo;
  }

  public static ServerPolicy fromDto(ServerPolicyDto dto) {
    var policy = new ServerPolicy();
    policy.strictTokenMovement = dto.getUseStrictTokenManagement();
    policy.isMovementLocked = dto.getIsMovementLocked();
    policy.isTokenEditorLocked = dto.getIsTokenEditorLocked();
    policy.playersCanRevealVision = dto.getPlayersCanRevealVision();
    policy.gmRevealsVisionForUnownedTokens = dto.getGmRevealsVisionForUnownedTokens();
    policy.useIndividualViews = dto.getUseIndividualViews();
    policy.restrictedImpersonation = dto.getRestrictedImpersonation();
    policy.playersReceiveCampaignMacros = dto.getPlayersReceiveCampaignMacros();
    policy.useToolTipsForDefaultRollFormat = dto.getUseToolTipsForDefaultRollFormat();
    policy.useIndividualFOW = dto.getUseIndividualFOW();
    policy.isAutoRevealOnMovement = dto.getIsAutoRevealOnMovement();
    policy.includeOwnedNPCs = dto.getIncludeOwnedNPCs();
    policy.movementMetric = WalkerMetric.valueOf(dto.getMovementMetric().name());
    policy.useAstarPathfinding = dto.getUsingAstarPathfinding();
    policy.vblBlocksMove = dto.getVblBlocksMove();
    policy.hidemapselectui = dto.getHideMapSelectUi();
    policy.disablePlayerAssetPanel = dto.getLockPlayerLibrary();
    return policy;
  }

  public ServerPolicyDto toDto() {
    var dto = ServerPolicyDto.newBuilder();
    dto.setUseStrictTokenManagement(strictTokenMovement);
    dto.setIsMovementLocked(isMovementLocked);
    dto.setIsTokenEditorLocked(isTokenEditorLocked);
    dto.setPlayersCanRevealVision(playersCanRevealVision);
    dto.setGmRevealsVisionForUnownedTokens(gmRevealsVisionForUnownedTokens);
    dto.setUseIndividualViews(useIndividualViews);
    dto.setRestrictedImpersonation(restrictedImpersonation);
    dto.setPlayersReceiveCampaignMacros(playersReceiveCampaignMacros);
    dto.setUseToolTipsForDefaultRollFormat(useToolTipsForDefaultRollFormat);
    dto.setUseIndividualFOW(useIndividualFOW);
    dto.setIsAutoRevealOnMovement(isAutoRevealOnMovement);
    dto.setIncludeOwnedNPCs(includeOwnedNPCs);
    dto.setMovementMetric(WalkerMetricDto.valueOf(movementMetric.name()));
    dto.setUsingAstarPathfinding(useAstarPathfinding);
    dto.setVblBlocksMove(vblBlocksMove);
    dto.setHideMapSelectUi(hidemapselectui);
    dto.setLockPlayerLibrary(disablePlayerAssetPanel);
    return dto.build();
  }
}
