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
package net.rptools.maptool.client.ui.startserverdialog;

import java.util.Objects;
import java.util.prefs.Preferences;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.server.ServerConfig;

public class StartServerDialogPreferences {
  private static Preferences prefs =
      Preferences.userRoot().node(AppConstants.APP_NAME + "/prefs/server");

  private static final String KEY_USERNAME = "name";
  private static final String KEY_ROLE = "playerRole";
  private static final String KEY_PORT = "port";
  private static final String KEY_GM_PASSWORD = "gmPassword";
  private static final String KEY_PLAYER_PASSWORD = "playerPassword";
  private static final String KEY_STRICT_TOKEN_OWNERSHIP = "strictTokenOwnership";
  private static final String KEY_REGISTER_SERVER = "registerServer";
  private static final String KEY_RPTOOLS_NAME = "rptoolsName";
  private static final String KEY_RPTOOLS_PRIVATE = "rptoolsPrivate";
  private static final String KEY_PLAYERS_CAN_REVEAL_VISION = "playersCanRevealVisionCheckbox";
  private static final String KEY_GM_REVEALS_VISION = "gmRevealsVisionForUnownedTokens";
  private static final String KEY_USE_INDIVIDUAL_VIEWS = "useIndividualViews";
  private static final String KEY_USE_UPNP = "useUPnP";
  private static final String KEY_RESTRICTED_IMPERSONATION = "restrictedImpersonation";
  private static final String KEY_PLAYERS_RECEIVE_CAMPAIGN_MACROS = "playersReceiveCampaignMacros";
  private static final String KEY_WALKER_METRIC = "movementMetric";
  private static final String KEY_USE_INDIVIDUAL_FOW = "useIndividualFOW";
  private static final String KEY_AUTO_REVEAL_ON_MOVE = "autoRevealOnMovement";

  private static final String KEY_USE_EASY_CONNECT = "useEasyConnect";
  private static final String KEY_USE_PASSWORD_FILE = "usePasswordFile";
  private static final String KEY_HIDE_MAP_SELECT_UI = "hideMapSelectUI";
  private static final String KEY_START_LOCKED_TOKEN_EDIT = "lockTokenEditOnStartup";
  private static final String KEY_START_LOCKED_PLAYER_MOVEMENT = "lockPlayerMovementOnStartup";
  private static final String KEY_LOCK_PLAYER_LIBRARY = "lockPlayerLibrary";

  private static final String KEY_USE_WEBRTC = "useWebRTC";

  private static Boolean useToolTipsForUnformattedRolls = null;

  public Player.Role getRole() {
    return Player.Role.valueOf(prefs.get(KEY_ROLE, Player.Role.GM.name()));
  }

  public void setRole(Player.Role role) {
    prefs.put(KEY_ROLE, role.name());
  }

  public String getUsername() {
    return prefs.get(KEY_USERNAME, "");
  }

  public void setUsername(String name) {
    prefs.put(KEY_USERNAME, name.trim());
  }

  public void setGMPassword(String password) {
    prefs.put(KEY_GM_PASSWORD, password.trim());
  }

  public String getGMPassword() {
    return prefs.get(KEY_GM_PASSWORD, "");
  }

  public void setPlayerPassword(String password) {
    prefs.put(KEY_PLAYER_PASSWORD, password.trim());
  }

  public String getPlayerPassword() {
    return prefs.get(KEY_PLAYER_PASSWORD, "");
  }

  public int getPort() {
    return prefs.getInt(KEY_PORT, ServerConfig.DEFAULT_PORT);
  }

  public void setPort(int port) {
    prefs.putInt(KEY_PORT, port);
  }

  public boolean getUseStrictTokenOwnership() {
    return prefs.getBoolean(KEY_STRICT_TOKEN_OWNERSHIP, false);
  }

  public void setUseStrictTokenOwnership(boolean use) {
    prefs.putBoolean(KEY_STRICT_TOKEN_OWNERSHIP, use);
  }

  // my addition
  public boolean getRestrictedImpersonation() {
    return prefs.getBoolean(KEY_RESTRICTED_IMPERSONATION, false);
  }

  public void setRestrictedImpersonation(boolean impersonation) {
    prefs.putBoolean(KEY_RESTRICTED_IMPERSONATION, impersonation);
  }

  public boolean registerServer() {
    return prefs.getBoolean(KEY_REGISTER_SERVER, false);
  }

  public void setRegisterServer(boolean register) {
    prefs.putBoolean(KEY_REGISTER_SERVER, register);
  }

  public void setRPToolsName(String name) {
    prefs.put(KEY_RPTOOLS_NAME, name.trim());
  }

  public String getRPToolsName() {
    return prefs.get(KEY_RPTOOLS_NAME, "");
  }

  public void setRPToolsPrivate(boolean flag) {
    prefs.putBoolean(KEY_RPTOOLS_PRIVATE, flag);
  }

  public boolean getRPToolsPrivate() {
    return prefs.getBoolean(KEY_RPTOOLS_PRIVATE, false);
  }

  public void setPlayersCanRevealVision(boolean flag) {
    prefs.putBoolean(KEY_PLAYERS_CAN_REVEAL_VISION, flag);
  }

  public boolean getPlayersCanRevealVision() {
    return prefs.getBoolean(KEY_PLAYERS_CAN_REVEAL_VISION, false);
  }

  public void setGmRevealsVisionForUnownedTokens(boolean flag) {
    prefs.putBoolean(KEY_GM_REVEALS_VISION, flag);
  }

  public boolean getGmRevealsVisionForUnownedTokens() {
    return prefs.getBoolean(KEY_GM_REVEALS_VISION, false);
  }

  public void setUseIndividualViews(boolean flag) {
    prefs.putBoolean(KEY_USE_INDIVIDUAL_VIEWS, flag);
  }

  public boolean getUseIndividualViews() {
    return prefs.getBoolean(KEY_USE_INDIVIDUAL_VIEWS, false);
  }

  public void setUseUPnP(boolean op) {
    prefs.putBoolean(KEY_USE_UPNP, op);
  }

  public boolean getUseUPnP() {
    return prefs.getBoolean(KEY_USE_UPNP, false);
  }

  public void setPlayersReceiveCampaignMacros(boolean flag) {
    prefs.putBoolean(KEY_PLAYERS_RECEIVE_CAMPAIGN_MACROS, flag);
  }

  public boolean getPlayersReceiveCampaignMacros() {
    return prefs.getBoolean(KEY_PLAYERS_RECEIVE_CAMPAIGN_MACROS, false);
  }

  public boolean getUseToolTipsForUnformattedRolls() {
    // Tool tips works slightly differently as its a setting that has to be available
    // to the user to configure before the start server dialog. So if it has not been
    // specified we default to the users preferences.
    return Objects.requireNonNullElseGet(
        useToolTipsForUnformattedRolls, AppPreferences.useToolTipForInlineRoll::get);
  }

  public void setUseToolTipsForUnformattedRolls(boolean flag) {
    useToolTipsForUnformattedRolls = flag;
  }

  public WalkerMetric getMovementMetric() {
    String metric = prefs.get(KEY_WALKER_METRIC, "ONE_ONE_ONE");
    return WalkerMetric.valueOf(metric);
  }

  public void setMovementMetric(WalkerMetric metric) {
    prefs.put(KEY_WALKER_METRIC, metric.name());
  }

  public boolean getUseIndividualFOW() {
    return prefs.getBoolean(KEY_USE_INDIVIDUAL_FOW, false);
  }

  public void setUseIndividualFOW(boolean flag) {
    prefs.putBoolean(KEY_USE_INDIVIDUAL_FOW, flag);
  }

  public boolean isAutoRevealOnMovement() {
    return prefs.getBoolean(KEY_AUTO_REVEAL_ON_MOVE, false);
  }

  public void setAutoRevealOnMovement(boolean flag) {
    prefs.putBoolean(KEY_AUTO_REVEAL_ON_MOVE, flag);
  }

  public boolean getUsePasswordFile() {
    return prefs.getBoolean(KEY_USE_PASSWORD_FILE, false);
  }

  public void setUsePasswordFile(boolean flag) {
    prefs.putBoolean(KEY_USE_PASSWORD_FILE, flag);
  }

  public boolean getUseEasyConnect() {
    return prefs.getBoolean(KEY_USE_EASY_CONNECT, false);
  }

  public void setUseEasyConnect(boolean flag) {
    prefs.putBoolean(KEY_USE_EASY_CONNECT, flag);
  }

  public boolean getMapSelectUIHidden() {
    return prefs.getBoolean(KEY_HIDE_MAP_SELECT_UI, false);
  }

  public void setMapSelectUIHidden(boolean flag) {
    prefs.putBoolean(KEY_HIDE_MAP_SELECT_UI, flag);
  }

  public boolean getLockTokenEditOnStart() {
    return prefs.getBoolean(KEY_START_LOCKED_TOKEN_EDIT, false);
  }

  public void setLockTokenEditOnStart(boolean flag) {
    prefs.putBoolean(KEY_START_LOCKED_TOKEN_EDIT, flag);
  }

  public boolean getLockPlayerMovementOnStart() {
    return prefs.getBoolean(KEY_START_LOCKED_PLAYER_MOVEMENT, false);
  }

  public void setLockPlayerMovementOnStart(boolean flag) {
    prefs.putBoolean(KEY_START_LOCKED_PLAYER_MOVEMENT, flag);
  }

  public boolean getPlayerLibraryLock() {
    return prefs.getBoolean(KEY_LOCK_PLAYER_LIBRARY, false);
  }

  public void setPlayerLibraryLock(boolean flag) {
    prefs.putBoolean(KEY_LOCK_PLAYER_LIBRARY, flag);
  }

  public boolean getUseWebRtc() {
    return prefs.getBoolean(KEY_USE_WEBRTC, false);
  }

  public void setKeyUseWebrtc(boolean flag) {
    prefs.putBoolean(KEY_USE_WEBRTC, flag);
  }
}
