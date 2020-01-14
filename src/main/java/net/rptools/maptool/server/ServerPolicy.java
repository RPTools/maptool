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
import net.rptools.maptool.client.walker.WalkerMetric;

public class ServerPolicy {
  private boolean strictTokenMovement;
  private boolean isMovementLocked;
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

  public ServerPolicy() {
    // Default tool tip usage for inline rolls to user preferences.
    useToolTipsForDefaultRollFormat = AppPreferences.getUseToolTipForInlineRoll();
    // Default movement metric from preferences
    movementMetric = AppPreferences.getMovementMetric();
  }

  /**
   * Whether token management can be done by everyone or only the GM and assigned tokens
   *
   * @return
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

  /** Gets the local server time */
  public long getSystemTime() {
    return System.currentTimeMillis();
  }

  private String getLocalTimeDate() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(cal.getTime());
  }

  public String getTimeDate() {
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

  /** @return the includeOwnedNPCs */
  public boolean isIncludeOwnedNPCs() {
    return includeOwnedNPCs;
  }

  /** @param includeOwnedNPCs the includeOwnedNPCs to set */
  public void setIncludeOwnedNPCs(boolean includeOwnedNPCs) {
    this.includeOwnedNPCs = includeOwnedNPCs;
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
        "restricted impersonation", isRestrictedImpersonation() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "individual views", isUseIndividualViews() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty("individual fow", isUseIndividualFOW() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "strict token management", useStrictTokenManagement() ? BigDecimal.ONE : BigDecimal.ZERO);
    sinfo.addProperty(
        "players receive campaign macros",
        playersReceiveCampaignMacros() ? BigDecimal.ONE : BigDecimal.ZERO);

    WalkerMetric metric =
        MapTool.isPersonalServer() ? AppPreferences.getMovementMetric() : getMovementMetric();
    sinfo.addProperty("movement metric", metric.toString());

    sinfo.addProperty("timeInMs", getSystemTime());
    sinfo.addProperty("timeDate", getTimeDate());

    JsonArray gms = new JsonArray();

    for (String gm : MapTool.getGMs()) {
      gms.add(gm);
    }
    sinfo.add("gm", gms);
    sinfo.addProperty(
        "hosting server", MapTool.isHostingServer() ? BigDecimal.ONE : BigDecimal.ZERO);

    return sinfo;
  }
}
