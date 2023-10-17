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

import com.twelvemonkeys.image.ResampleOp;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppPreferences {

  private static final Logger log = LogManager.getLogger(AppPreferences.class);
  private static Preferences prefs = Preferences.userRoot().node(AppConstants.APP_NAME + "/prefs");

  private static RenderQuality renderQuality;

  private static final String KEY_ASSET_ROOTS = "assetRoots";
  private static final String KEY_SAVE_DIR = "saveDir";
  private static final String KEY_SAVE_TOKEN_DIR = "saveTokenDir";
  private static final String KEY_SAVE_MAP_DIR = "saveMapDir";
  private static final String KEY_LOAD_DIR = "loadDir";
  private static final String KEY_ADD_ON_LOAD_DIR = "addOnLoadDir";
  private static final String KEY_MRU_CAMPAIGNS = "mruCampaigns";
  private static final String KEY_SAVED_PAINT_TEXTURES = "savedTextures";

  private static final String KEY_SAVE_REMINDER = "autoSaveReminder";
  private static final boolean DEFAULT_SAVE_REMINDER = true;

  private static final String KEY_TOKEN_NUMBER_DISPLAY = "tokenNumberDisplayg";
  private static final String DEFAULT_TOKEN_NUMBER_DISPLAY = Token.NUM_ON_NAME;

  private static final String KEY_AUTO_SAVE_INCREMENT = "autoSaveIncrement";
  private static final int DEFAULT_AUTO_SAVE_INCREMENT = 5; // Minutes

  // private static final String KEY_ENABLE_MAP_EXPORT_IMPORT = "enableMapExportImport";
  // private static final boolean DEFAULT_ENABLE_MAP_EXPORT_IMPORT = false;

  private static final String KEY_CHAT_AUTOSAVE_TIME = "chatAutosaveTime";
  private static final int DEFAULT_CHAT_AUTOSAVE_TIME = 0; // Minutes; zero=disabled

  private static final String KEY_CHAT_FILENAME_FORMAT = "chatFilenameFormat";
  private static final String DEFAULT_CHAT_FILENAME_FORMAT =
      "chatlog-%1$tF-%1$tR.html"; // http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html

  private static final String KEY_DUPLICATE_TOKEN_NUMBER = "duplicateTokenNumber";
  private static final String DEFAULT_DUPLICATE_TOKEN_NUMBER = Token.NUM_INCREMENT;

  private static final String KEY_NEW_TOKEN_NAMING = "newTokenNaming";
  private static final String DEFAULT_NEW_TOKEN_NAMING = Token.NAME_USE_FILENAME;

  private static final String KEY_USE_HALO_COLOR_ON_VISION_OVERLAY = "useHaloColorForVisionOverlay";
  private static final boolean DEFAULT_USE_HALO_COLOR_ON_VISION_OVERLAY = false;

  private static final String KEY_HALO_OVERLAY_OPACITY = "haloOverlayOpacity";
  private static final int DEFAULT_HALO_OVERLAY_OPACITY = 60;

  private static final String KEY_AURA_OVERLAY_OPACITY = "auraOverlayOpacity";
  private static final int DEFAULT_AURA_OVERLAY_OPACITY = 60;

  private static final String KEY_LIGHT_OVERLAY_OPACITY = "lightOverlayOpacity";
  private static final int DEFAULT_LIGHT_OVERLAY_OPACITY = 60;

  private static final String KEY_LUMENS_OVERLAY_OPACITY = "lumensOverlayOpacity";
  private static final int DEFAULT_LUMENS_OVERLAY_OPACITY = 120;

  private static final String KEY_LUMENS_OVERLAY_BORDER_THICKNESS = "lumensOverlayBorderThickness";
  private static final int DEFAULT_LUMENS_OVERLAY_BORDER_THICKNESS = 5;

  private static final String KEY_LUMENS_OVERLAY_SHOW_BY_DEFAULT = "lumensOverlayShowByDefault";
  private static final boolean DEFAULT_LUMENS_OVERLAY_SHOW_BY_DEFAULT = false;

  private static final String KEY_LIGHTS_SHOW_BY_DEFAULT = "lightsShowByDefault";
  private static final boolean DEFAULT_LIGHTS_SHOW_BY_DEFAULT = true;

  private static final String KEY_FOG_OVERLAY_OPACITY = "fogOverlayOpacity";
  private static final int DEFAULT_FOG_OVERLAY_OPACITY = 100;

  private static final String KEY_HALO_LINE_WIDTH = "haloLineWidth";
  private static final int DEFAULT_HALO_LINE_WIDTH = 2;

  private static final String KEY_AUTO_REVEAL_VISION_ON_GM_MOVEMENT = "autoRevealVisionOnGMMove";
  private static final boolean DEFAULT_AUTO_REVEAL_VISION_ON_GM_MOVEMENT = false;

  private static final String KEY_USE_SOFT_FOG_EDGES = "useSoftFog";
  private static final boolean DEFAULT_USE_SOFT_FOG_EDGES = true;

  private static final String KEY_MAP_VISIBILITY_WARNING = "mapVisibilityWarning";

  private static final String KEY_NEW_MAPS_HAVE_FOW = "newMapsHaveFow";
  private static final boolean DEFAULT_NEW_MAPS_HAVE_FOW = false;

  private static final String KEY_NEW_TOKENS_VISIBLE = "newTokensVisible";
  private static final boolean DEFAULT_NEW_TOKENS_VISIBLE = true;

  private static final String KEY_NEW_MAPS_VISIBLE = "newMapsVisible";
  private static final boolean DEFAULT_NEW_MAPS_VISIBLE = true;

  private static final String KEY_NEW_OBJECTS_VISIBLE = "newObjectsVisible";
  private static final boolean DEFAULT_NEW_OBJECTS_VISIBLE = true;

  private static final String KEY_NEW_BACKGROUNDS_VISIBLE = "newBackgroundsVisible";
  private static final boolean DEFAULT_NEW_BACKGROUNDS_VISIBLE = true;

  private static final String KEY_TOKENS_START_FREESIZE = "newTokensStartFreesize";
  private static final boolean DEFAULT_TOKENS_START_FREESIZE = false;

  private static final String KEY_TOKENS_WARN_WHEN_DELETED = "tokensWarnWhenDeleted";
  private static final boolean DEFAULT_TOKENS_WARN_WHEN_DELETED = true;

  private static final String KEY_DRAW_WARN_WHEN_DELETED = "drawWarnWhenDeleted";
  private static final boolean DEFAULT_DRAW_WARN_WHEN_DELETED = true;

  private static final String KEY_TOKENS_START_SNAP_TO_GRID = "newTokensStartSnapToGrid";
  private static final boolean DEFAULT_TOKENS_START_SNAP_TO_GRID = true;

  private static final String KEY_TOKENS_SNAP_WHILE_DRAGGING = "tokensSnapWhileDragging";
  private static final boolean DEFAULT_KEY_TOKENS_SNAP_WHILE_DRAGGING = true;

  private static final String KEY_HIDE_MOUSE_POINTER_WHILE_DRAGGING =
      "hideMousePointerWhileDragging";
  private static final boolean DEFAULT_KEY_HIDE_MOUSE_POINTER_WHILE_DRAGGING = true;

  private static final String KEY_HIDE_TOKEN_STACK_INDICATOR = "hideTokenStackIndicator";
  private static final boolean DEFAULT_KEY_HIDE_TOKEN_STACK_INDICATOR = false;

  private static final String KEY_OBJECTS_START_SNAP_TO_GRID = "newStampsStartSnapToGrid";
  private static final boolean DEFAULT_OBJECTS_START_SNAP_TO_GRID = false;

  private static final String KEY_OBJECTS_START_FREESIZE = "newStampsStartFreesize";
  private static final boolean DEFAULT_OBJECTS_START_FREESIZE = true;

  private static final String KEY_BACKGROUNDS_START_SNAP_TO_GRID = "newBackgroundsStartSnapToGrid";
  private static final boolean DEFAULT_BACKGROUNDS_START_SNAP_TO_GRID = false;

  private static final String KEY_BACKGROUNDS_START_FREESIZE = "newBackgroundsStartFreesize";
  private static final boolean DEFAULT_BACKGROUNDS_START_FREESIZE = true;

  private static final String KEY_SOUNDS_ONLY_WHEN_NOT_FOCUSED =
      "playSystemSoundsOnlyWhenNotFocused";
  private static final boolean DEFAULT_SOUNDS_ONLY_WHEN_NOT_FOCUSED = false;

  private static final String KEY_SYRINSCAPE_ACTIVE = "syrinscapeActive";
  private static final boolean DEFAULT_SYRINSCAPE_ACTIVE = false;

  private static final String KEY_SHOW_AVATAR_IN_CHAT = "showAvatarInChat";
  private static final boolean DEFAULT_SHOW_AVATAR_IN_CHAT = true;

  private static final String KEY_SHOW_DIALOG_ON_NEW_TOKEN = "showDialogOnNewToken";
  private static final boolean DEFAULT_SHOW_DIALOG_ON_NEW_TOKEN = true;

  private static final String KEY_INSERT_SMILIES = "insertSmilies";
  private static final boolean DEFAULT_SHOW_SMILIES = true;

  private static final String KEY_MOVEMENT_METRIC = "movementMetric";
  private static final WalkerMetric DEFAULT_MOVEMENT_METRIC = WalkerMetric.ONE_TWO_ONE;

  private static final String KEY_SHOW_STAT_SHEET = "showStatSheet";
  private static final boolean DEFAULT_SHOW_STAT_SHEET = true;

  private static final String KEY_SHOW_PORTRAIT = "showPortrait";
  private static final boolean DEFAULT_SHOW_PORTRAIT = true;

  private static final String KEY_SHOW_STAT_SHEET_MODIFIER = "showStatSheetModifier";
  private static final boolean DEFAULT_SHOW_STAT_SHEET_MODIFIER = false;

  private static final String KEY_FILL_SELECTION_BOX = "fillSelectionBox";
  private static final boolean DEFAULT_FILL_SELECTION_BOX = true;

  private static final String KEY_SHOW_INIT_GAIN_MESSAGE = "showInitGainMessage";
  private static final boolean DEFAULT_SHOW_INIT_GAIN_MESSAGE = true;

  private static final String KEY_FORCE_FACING_ARROW = "forceFacingArrow";
  private static final boolean DEFAULT_FORCE_FACING_ARROW = false;

  private static final String KEY_USE_ASTAR_PATHFINDING = "useAstarPathfinding";
  private static final boolean DEFAULT_USE_ASTAR_PATHFINDING = true;

  private static final String KEY_VBL_BLOCKS_MOVE = "vblBlocksMove";
  private static final boolean DEFAULT_VBL_BLOCKS_MOVE = true;

  private static final String MACRO_EDITOR_THEME = "macroEditorTheme";
  private static final String DEFAULT_MACRO_EDITOR_THEME = "Default";

  private static final String ICON_THEME = "iconTheme";
  private static final String DEFAULT_ICON_THEME = RessourceManager.ROD_TAKEHARA;

  // When hill VBL was introduced, older versions of MapTool were unable to read the new topology
  // modes. So we use a different preference key than in the past so older versions would not
  // unexpectedly break.
  private static final String KEY_TOPOLOGY_TYPES = "topologyTypes";
  private static final String KEY_OLD_TOPOLOGY_DRAWING_MODE = "topologyDrawingMode";
  private static final String DEFAULT_TOPOLOGY_TYPE = "VBL";

  private static final String KEY_WEB_END_POINT_PORT = "webEndPointPort";
  private static final int DEFAULT_WEB_END_POINT = 654555;

  public static void setFillSelectionBox(boolean fill) {
    prefs.putBoolean(KEY_FILL_SELECTION_BOX, fill);
  }

  public static boolean getFillSelectionBox() {
    return prefs.getBoolean(KEY_FILL_SELECTION_BOX, DEFAULT_FILL_SELECTION_BOX);
  }

  public static Color getChatColor() {
    return new Color(prefs.getInt(KEY_CHAT_COLOR, DEFAULT_CHAT_COLOR.getRGB()));
  }

  public static void setSaveReminder(boolean reminder) {
    prefs.putBoolean(KEY_SAVE_REMINDER, reminder);
  }

  public static boolean getSaveReminder() {
    return prefs.getBoolean(KEY_SAVE_REMINDER, DEFAULT_SAVE_REMINDER);
  }

  // public static void setEnabledMapExportImport(boolean reminder) {
  // prefs.putBoolean(KEY_ENABLE_MAP_EXPORT_IMPORT, reminder);
  // AppActions.updateActions();
  // }

  // public static boolean isEnabledMapExportImport() {
  // return prefs.getBoolean(KEY_ENABLE_MAP_EXPORT_IMPORT, DEFAULT_ENABLE_MAP_EXPORT_IMPORT);
  // }

  public static void setAutoSaveIncrement(int increment) {
    prefs.putInt(KEY_AUTO_SAVE_INCREMENT, increment);
  }

  public static int getAutoSaveIncrement() {
    return prefs.getInt(KEY_AUTO_SAVE_INCREMENT, DEFAULT_AUTO_SAVE_INCREMENT);
  }

  public static void setChatAutosaveTime(int minutes) {
    if (minutes >= 0) {
      prefs.putInt(KEY_CHAT_AUTOSAVE_TIME, minutes);
      ChatAutoSave.changeTimeout(minutes);
    }
  }

  public static int getChatAutosaveTime() {
    return prefs.getInt(KEY_CHAT_AUTOSAVE_TIME, DEFAULT_CHAT_AUTOSAVE_TIME);
  }

  public static void setChatFilenameFormat(String pattern) {
    prefs.put(KEY_CHAT_FILENAME_FORMAT, pattern);
  }

  public static String getChatFilenameFormat() {
    return prefs.get(KEY_CHAT_FILENAME_FORMAT, DEFAULT_CHAT_FILENAME_FORMAT);
  }

  public static void clearChatFilenameFormat() {
    prefs.remove(KEY_CHAT_FILENAME_FORMAT);
  }

  public static void setTokenNumberDisplay(String display) {
    prefs.put(KEY_TOKEN_NUMBER_DISPLAY, display);
  }

  public static String getTokenNumberDisplay() {
    return prefs.get(KEY_TOKEN_NUMBER_DISPLAY, DEFAULT_TOKEN_NUMBER_DISPLAY);
  }

  public static void setDuplicateTokenNumber(String numbering) {
    prefs.put(KEY_DUPLICATE_TOKEN_NUMBER, numbering);
  }

  public static String getDuplicateTokenNumber() {
    return prefs.get(KEY_DUPLICATE_TOKEN_NUMBER, DEFAULT_DUPLICATE_TOKEN_NUMBER);
  }

  public static void setNewTokenNaming(String naming) {
    prefs.put(KEY_NEW_TOKEN_NAMING, naming);
  }

  public static String getNewTokenNaming() {
    return prefs.get(KEY_NEW_TOKEN_NAMING, DEFAULT_NEW_TOKEN_NAMING);
  }

  public static void setUseHaloColorOnVisionOverlay(boolean flag) {
    prefs.putBoolean(KEY_USE_HALO_COLOR_ON_VISION_OVERLAY, flag);
  }

  public static boolean getUseHaloColorOnVisionOverlay() {
    return prefs.getBoolean(
        KEY_USE_HALO_COLOR_ON_VISION_OVERLAY, DEFAULT_USE_HALO_COLOR_ON_VISION_OVERLAY);
  }

  public static void setMapVisibilityWarning(boolean flag) {
    prefs.putBoolean(KEY_MAP_VISIBILITY_WARNING, flag);
  }

  public static boolean getMapVisibilityWarning() {
    return prefs.getBoolean(KEY_MAP_VISIBILITY_WARNING, false);
  }

  public static void setAutoRevealVisionOnGMMovement(boolean flag) {
    prefs.putBoolean(KEY_AUTO_REVEAL_VISION_ON_GM_MOVEMENT, flag);
  }

  public static boolean getAutoRevealVisionOnGMMovement() {
    return prefs.getBoolean(
        KEY_AUTO_REVEAL_VISION_ON_GM_MOVEMENT, DEFAULT_AUTO_REVEAL_VISION_ON_GM_MOVEMENT);
  }

  private static int range0to255(int value) {
    return value < 1 ? 0 : Math.min(value, 255);
  }

  public static void setHaloOverlayOpacity(int size) {
    prefs.putInt(KEY_HALO_OVERLAY_OPACITY, range0to255(size));
  }

  public static int getHaloOverlayOpacity() {
    int value = prefs.getInt(KEY_HALO_OVERLAY_OPACITY, DEFAULT_HALO_OVERLAY_OPACITY);
    return range0to255(value);
  }

  public static void setAuraOverlayOpacity(int size) {
    prefs.putInt(KEY_AURA_OVERLAY_OPACITY, range0to255(size));
  }

  public static int getAuraOverlayOpacity() {
    int value = prefs.getInt(KEY_AURA_OVERLAY_OPACITY, DEFAULT_AURA_OVERLAY_OPACITY);
    return range0to255(value);
  }

  public static void setLightOverlayOpacity(int size) {
    prefs.putInt(KEY_LIGHT_OVERLAY_OPACITY, range0to255(size));
  }

  public static int getLightOverlayOpacity() {
    int value = prefs.getInt(KEY_LIGHT_OVERLAY_OPACITY, DEFAULT_LIGHT_OVERLAY_OPACITY);
    return range0to255(value);
  }

  public static void setLumensOverlayOpacity(int size) {
    prefs.putInt(KEY_LUMENS_OVERLAY_OPACITY, range0to255(size));
  }

  public static int getLumensOverlayOpacity() {
    int value = prefs.getInt(KEY_LUMENS_OVERLAY_OPACITY, DEFAULT_LUMENS_OVERLAY_OPACITY);
    return range0to255(value);
  }

  public static void setLumensOverlayBorderThickness(int thickness) {
    prefs.putInt(KEY_LUMENS_OVERLAY_BORDER_THICKNESS, thickness);
  }

  public static int getLumensOverlayBorderThickness() {
    return prefs.getInt(
        KEY_LUMENS_OVERLAY_BORDER_THICKNESS, DEFAULT_LUMENS_OVERLAY_BORDER_THICKNESS);
  }

  public static void setLumensOverlayShowByDefault(boolean show) {
    prefs.putBoolean(KEY_LUMENS_OVERLAY_SHOW_BY_DEFAULT, show);
  }

  public static boolean getLumensOverlayShowByDefault() {
    return prefs.getBoolean(
        KEY_LUMENS_OVERLAY_SHOW_BY_DEFAULT, DEFAULT_LUMENS_OVERLAY_SHOW_BY_DEFAULT);
  }

  public static void setLightsShowByDefault(boolean show) {
    prefs.putBoolean(KEY_LIGHTS_SHOW_BY_DEFAULT, show);
  }

  public static boolean getLightsShowByDefault() {
    return prefs.getBoolean(KEY_LIGHTS_SHOW_BY_DEFAULT, DEFAULT_LIGHTS_SHOW_BY_DEFAULT);
  }

  public static void setFogOverlayOpacity(int size) {
    prefs.putInt(KEY_FOG_OVERLAY_OPACITY, range0to255(size));

    // FIXME Force ModelChange event to flush fog from zone :(
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    zone.setHasFog(zone.hasFog());
  }

  public static int getFogOverlayOpacity() {
    int value = prefs.getInt(KEY_FOG_OVERLAY_OPACITY, DEFAULT_FOG_OVERLAY_OPACITY);
    return range0to255(value);
  }

  private static final String KEY_DEFAULT_GRID_TYPE = "defaultGridType";
  private static final String DEFAULT_DEFAULT_GRID_TYPE = GridFactory.SQUARE;

  private static final String KEY_FACE_VERTEX = "faceVertex";
  private static final boolean DEFAULT_FACE_VERTEX = false;

  private static final String KEY_FACE_EDGE = "faceEdge";
  private static final boolean DEFAULT_FACE_EDGE = true;

  private static final String KEY_DEFAULT_GRID_SIZE = "defaultGridSize";
  private static final int DEFAULT_DEFAULT_GRID_SIZE = 100;

  private static final String KEY_DEFAULT_GRID_COLOR = "defaultGridColor";
  private static final int DEFAULT_DEFAULT_GRID_COLOR = Color.black.getRGB();

  private static final String KEY_DEFAULT_UNITS_PER_CELL = "unitsPerCell";
  private static final int DEFAULT_DEFAULT_UNITS_PER_CELL = 5;

  private static final String KEY_DEFAULT_VISION_DISTANCE = "defaultVisionDistance";
  private static final int DEFAULT_DEFAULT_VISION_DISTANCE = 1000;

  private static final String KEY_DEFAULT_VISION_TYPE = "defaultVisionType";
  private static final Zone.VisionType DEFAULT_VISION_TYPE = Zone.VisionType.OFF;

  private static final String KEY_MAP_SORT_TYPE = "sortByGMName";
  private static final MapSortType DEFAULT_MAP_SORT_TYPE = MapSortType.GMNAME;

  private static final String KEY_FONT_SIZE = "fontSize";
  private static final int DEFAULT_FONT_SIZE = 12;

  private static final String KEY_CHAT_COLOR = "chatColor";
  private static final Color DEFAULT_CHAT_COLOR = Color.black;

  private static final String KEY_PLAY_SYSTEM_SOUNDS = "playSystemSounds";
  private static final boolean DEFAULT_PLAY_SYSTEM_SOUNDS = true;

  private static final String KEY_PLAY_STREAMS = "playStreams";
  private static final boolean DEFAULT_PLAY_STREAMS = true;

  public static void setHaloLineWidth(int size) {
    prefs.putInt(KEY_HALO_LINE_WIDTH, size);
  }

  public static int getHaloLineWidth() {
    return prefs.getInt(KEY_HALO_LINE_WIDTH, DEFAULT_HALO_LINE_WIDTH);
  }

  private static final String KEY_PORTRAIT_SIZE = "portraitSize";
  private static final int DEFAULT_PORTRAIT_SIZE = 175;

  private static final String KEY_THUMBNAIL_SIZE = "thumbnailSize";
  private static final int DEFAULT_THUMBNAIL_SIZE = 500;

  private static final String KEY_ALLOW_PLAYER_MACRO_EDITS_DEFAULT = "allowPlayerMacroEditsDefault";
  private static final boolean DEFAULT_ALLOW_PLAYER_MACRO_EDITS_DEFAULT = true;

  private static final String KEY_TOOLTIP_INITIAL_DELAY = "toolTipInitialDelay";
  private static final int DEFAULT_TOOLTIP_INITIAL_DELAY = 250;

  private static final String KEY_TOOLTIP_DISMISS_DELAY = "toolTipDismissDelay";
  private static final int DEFAULT_TOOLTIP_DISMISS_DELAY = 30000;

  private static final String KEY_TOOLTIP_FOR_INLINE_ROLLS = "toolTipInlineRolls";
  private static final boolean DEFAULT_TOOLTIP_FOR_INLINE_ROLLS = false;

  private static final String KEY_SUPPRESS_TOOLTIPS_FOR_MACROLINKS = "suppressToolTipsMacroLinks";
  private static final boolean DEFAULT_SUPPRESS_TOOLTIPS_FOR_MACROLINKS = false;

  // chat notification colors
  private static final String KEY_CHAT_NOTIFICATION_COLOR_RED = "chatNotificationColorRed";
  private static final int DEFAULT_CHAT_NOTIFICATION_COLOR_RED = 0xFF;

  private static final String KEY_CHAT_NOTIFICATION_COLOR_GREEN = "chatNotificationColorGreen";
  private static final int DEFAULT_CHAT_NOTIFICATION_COLOR_GREEN = 0xFF;

  private static final String KEY_CHAT_NOTIFICATION_COLOR_BLUE = "chatNotificationColorBlue";
  private static final int DEFAULT_CHAT_NOTIFICATION_COLOR_BLUE = 0xFF;

  // end chat notification colors

  private static final String KEY_CHAT_NOTIFICATION_SHOW_BACKGROUND =
      "chatNotificationShowBackground";
  private static final boolean DEFAULT_CHAT_NOTIFICATION_SHOW_BACKGROUND = true;

  private static final String KEY_TRUSTED_PREFIX_BG_RED = "trustedPrefixBGRed";
  private static final int DEFAULT_TRUSTED_PREFIX_BG_RED = 0xD8;

  private static final String KEY_TRUSTED_PREFIX_BG_GREEN = "trustedPrefixBGGreen";
  private static final int DEFAULT_TRUSTED_PREFIX_BG_GREEN = 0xE9;

  private static final String KEY_TRUSTED_PREFIX_BG_BLUE = "trustedPrefixBBlue";
  private static final int DEFAULT_TRUSTED_PREFIX_BG_BLUE = 0xF6;

  private static final String KEY_TRUSTED_PREFIX_FG_RED = "trustedPrefixFGRed";
  private static final int DEFAULT_TRUSTED_PREFIX_FG_RED = 0x00;

  private static final String KEY_TRUSTED_PREFIX_FG_GREEN = "trustedPrefixFGGreen";
  private static final int DEFAULT_TRUSTED_PREFIX_FG_GREEN = 0x00;

  private static final String KEY_TRUSTED_PREFIX_FG_BLUE = "trustedPrefixFBlue";
  private static final int DEFAULT_TRUSTED_PREFIX_FG_BLUE = 0x00;

  private static final String KEY_FIT_GM_VIEW = "fitGMView";
  private static final boolean DEFAULT_FIT_GM_VIEW = true;

  private static final String KEY_DEFAULT_USERNAME = "defaultUsername";
  private static final String DEFAULT_USERNAME =
      I18N.getString("Preferences.client.default.username.value");

  private static final String KEY_TYPING_NOTIFICATION_DURATION = "typingNotificationDuration";
  private static final int DEFAULT_TYPING_NOTIFICATION_DURATION = 5000;

  private static final String KEY_FRAME_RATE_CAP = "frameRateCap";
  private static final int DEFAULT_FRAME_RATE_CAP = 60;

  private static final String KEY_UPNP_DISCOVERY_TIMEOUT = "upnpDiscoveryTimeout";
  private static final int DEFAULT_UPNP_DISCOVERY_TIMEOUT = 5000;

  private static final String KEY_FILE_SYNC_PATH = "fileSyncPath";
  private static final String DEFAULT_FILE_SYNC_PATH = "";

  private static final String KEY_SKIP_AUTO_UPDATE = "skipAutoUpdate";
  private static final boolean DEFAULT_SKIP_AUTO_UPDATE = false;
  private static final String KEY_SKIP_AUTO_UPDATE_RELEASE = "skipAutoUpdateRelease";
  private static final String DEFAULT_SKIP_AUTO_UPDATE_RELEASE = "";

  private static final String KEY_ALLOW_EXTERNAL_MACRO_ACCESS = "allowExternalMacroAccess";
  private static final boolean DEFAULT_ALLOW_EXTERNAL_MACRO_ACCESS = false;

  private static final String KEY_RENDER_QUALITY = "renderScaleQuality";

  private static final RenderQuality DEFAULT_RENDER_QUALITY = RenderQuality.LOW_SCALING;

  public enum RenderQuality {
    LOW_SCALING,
    PIXEL_ART_SCALING,
    MEDIUM_SCALING,
    HIGH_SCALING;

    public void setRenderingHints(Graphics2D g) {
      switch (this) {
        case LOW_SCALING, PIXEL_ART_SCALING -> {
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        case MEDIUM_SCALING -> {
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        }
        case HIGH_SCALING -> {
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
      }
    }

    public void setShrinkRenderingHints(Graphics2D d) {
      switch (this) {
        case LOW_SCALING, PIXEL_ART_SCALING -> {
          d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
          d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        case MEDIUM_SCALING -> {
          d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
        }
        case HIGH_SCALING -> {
          d.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
      }
    }

    public int getResampleOpFilter() {
      return switch (this) {
        case LOW_SCALING, PIXEL_ART_SCALING -> ResampleOp.FILTER_POINT;
        case MEDIUM_SCALING -> ResampleOp.FILTER_TRIANGLE;
        case HIGH_SCALING -> ResampleOp.FILTER_QUADRATIC;
      };
    }
  }

  public static void setRenderQuality(RenderQuality quality) {
    prefs.put(KEY_RENDER_QUALITY, quality.name());
    renderQuality = quality;
  }

  public static RenderQuality getRenderQuality() {
    if (renderQuality == null) {
      try {
        renderQuality =
            RenderQuality.valueOf(prefs.get(KEY_RENDER_QUALITY, DEFAULT_RENDER_QUALITY.name()));
      } catch (Exception e) {
        renderQuality = DEFAULT_RENDER_QUALITY;
      }
    }
    return renderQuality;
  }

  public static void setTypingNotificationDuration(int ms) {
    prefs.putInt(KEY_TYPING_NOTIFICATION_DURATION, ms);
    MapTool.getFrame().setChatNotifyDuration(ms);
  }

  public static Integer getTypingNotificationDuration() {
    Integer value =
        prefs.getInt(KEY_TYPING_NOTIFICATION_DURATION, DEFAULT_TYPING_NOTIFICATION_DURATION);
    return value;
  }

  public static void setUseToolTipForInlineRoll(boolean tooltip) {
    prefs.putBoolean(KEY_TOOLTIP_FOR_INLINE_ROLLS, tooltip);
  }

  public static boolean getUseToolTipForInlineRoll() {
    return prefs.getBoolean(KEY_TOOLTIP_FOR_INLINE_ROLLS, DEFAULT_TOOLTIP_FOR_INLINE_ROLLS);
  }

  public static void setSuppressToolTipsForMacroLinks(boolean tooltip) {
    prefs.putBoolean(KEY_SUPPRESS_TOOLTIPS_FOR_MACROLINKS, tooltip);
  }

  public static boolean getSuppressToolTipsForMacroLinks() {
    return prefs.getBoolean(
        KEY_SUPPRESS_TOOLTIPS_FOR_MACROLINKS, DEFAULT_SUPPRESS_TOOLTIPS_FOR_MACROLINKS);
  }

  public static void setChatNotificationColor(Color color) {
    prefs.putInt(KEY_CHAT_NOTIFICATION_COLOR_RED, color.getRed());
    prefs.putInt(KEY_CHAT_NOTIFICATION_COLOR_GREEN, color.getGreen());
    prefs.putInt(KEY_CHAT_NOTIFICATION_COLOR_BLUE, color.getBlue());
  }

  public static Color getChatNotificationColor() {
    return new Color(
        prefs.getInt(KEY_CHAT_NOTIFICATION_COLOR_RED, DEFAULT_CHAT_NOTIFICATION_COLOR_RED),
        prefs.getInt(KEY_CHAT_NOTIFICATION_COLOR_GREEN, DEFAULT_CHAT_NOTIFICATION_COLOR_GREEN),
        prefs.getInt(KEY_CHAT_NOTIFICATION_COLOR_BLUE, DEFAULT_CHAT_NOTIFICATION_COLOR_BLUE));
  }

  public static void setTrustedPrefixBG(Color color) {
    prefs.putInt(KEY_TRUSTED_PREFIX_BG_RED, color.getRed());
    prefs.putInt(KEY_TRUSTED_PREFIX_BG_GREEN, color.getGreen());
    prefs.putInt(KEY_TRUSTED_PREFIX_BG_BLUE, color.getBlue());
  }

  public static Color getTrustedPrefixBG() {
    return new Color(
        prefs.getInt(KEY_TRUSTED_PREFIX_BG_RED, DEFAULT_TRUSTED_PREFIX_BG_RED),
        prefs.getInt(KEY_TRUSTED_PREFIX_BG_GREEN, DEFAULT_TRUSTED_PREFIX_BG_GREEN),
        prefs.getInt(KEY_TRUSTED_PREFIX_BG_BLUE, DEFAULT_TRUSTED_PREFIX_BG_BLUE));
  }

  public static void setTrustedPrefixFG(Color color) {
    prefs.putInt(KEY_TRUSTED_PREFIX_FG_RED, color.getRed());
    prefs.putInt(KEY_TRUSTED_PREFIX_FG_GREEN, color.getGreen());
    prefs.putInt(KEY_TRUSTED_PREFIX_FG_BLUE, color.getBlue());
  }

  public static Color getTrustedPrefixFG() {
    return new Color(
        prefs.getInt(KEY_TRUSTED_PREFIX_FG_RED, DEFAULT_TRUSTED_PREFIX_FG_RED),
        prefs.getInt(KEY_TRUSTED_PREFIX_FG_GREEN, DEFAULT_TRUSTED_PREFIX_FG_GREEN),
        prefs.getInt(KEY_TRUSTED_PREFIX_FG_BLUE, DEFAULT_TRUSTED_PREFIX_FG_BLUE));
  }

  public static void setToolTipInitialDelay(int ms) {
    prefs.putInt(KEY_TOOLTIP_INITIAL_DELAY, ms);
  }

  public static int getToolTipInitialDelay() {
    return prefs.getInt(KEY_TOOLTIP_INITIAL_DELAY, DEFAULT_TOOLTIP_INITIAL_DELAY);
  }

  public static void setToolTipDismissDelay(int ms) {
    prefs.putInt(KEY_TOOLTIP_DISMISS_DELAY, ms);
  }

  public static int getToolTipDismissDelay() {
    return prefs.getInt(KEY_TOOLTIP_DISMISS_DELAY, DEFAULT_TOOLTIP_DISMISS_DELAY);
  }

  public static void setAllowPlayerMacroEditsDefault(boolean show) {
    prefs.putBoolean(KEY_ALLOW_PLAYER_MACRO_EDITS_DEFAULT, show);
  }

  public static boolean getAllowPlayerMacroEditsDefault() {
    return prefs.getBoolean(
        KEY_ALLOW_PLAYER_MACRO_EDITS_DEFAULT, DEFAULT_ALLOW_PLAYER_MACRO_EDITS_DEFAULT);
  }

  public static void setPortraitSize(int size) {
    prefs.putInt(KEY_PORTRAIT_SIZE, size);
  }

  public static int getPortraitSize() {
    return prefs.getInt(KEY_PORTRAIT_SIZE, DEFAULT_PORTRAIT_SIZE);
  }

  public static void setThumbnailSize(int size) {
    prefs.putInt(KEY_THUMBNAIL_SIZE, size);
  }

  public static int getThumbnailSize() {
    return prefs.getInt(KEY_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE);
  }

  public static void setShowSmilies(boolean show) {
    prefs.putBoolean(KEY_INSERT_SMILIES, show);
  }

  public static boolean getShowSmilies() {
    return prefs.getBoolean(KEY_INSERT_SMILIES, DEFAULT_SHOW_SMILIES);
  }

  public static void setShowDialogOnNewToken(boolean show) {
    prefs.putBoolean(KEY_SHOW_DIALOG_ON_NEW_TOKEN, show);
  }

  public static boolean getShowDialogOnNewToken() {
    return prefs.getBoolean(KEY_SHOW_DIALOG_ON_NEW_TOKEN, DEFAULT_SHOW_DIALOG_ON_NEW_TOKEN);
  }

  public static void setShowAvatarInChat(boolean show) {
    prefs.putBoolean(KEY_SHOW_AVATAR_IN_CHAT, show);
  }

  public static boolean getShowAvatarInChat() {
    return prefs.getBoolean(KEY_SHOW_AVATAR_IN_CHAT, DEFAULT_SHOW_AVATAR_IN_CHAT);
  }

  public static void setPlaySystemSounds(boolean play) {
    prefs.putBoolean(KEY_PLAY_SYSTEM_SOUNDS, play);
  }

  public static void setPlayStreams(boolean play) {
    prefs.putBoolean(KEY_PLAY_STREAMS, play);
  }

  public static boolean getPlaySystemSounds() {
    return prefs.getBoolean(KEY_PLAY_SYSTEM_SOUNDS, DEFAULT_PLAY_SYSTEM_SOUNDS);
  }

  public static boolean getPlayStreams() {
    return prefs.getBoolean(KEY_PLAY_STREAMS, DEFAULT_PLAY_STREAMS);
  }

  public static void setPlaySystemSoundsOnlyWhenNotFocused(boolean play) {
    prefs.putBoolean(KEY_SOUNDS_ONLY_WHEN_NOT_FOCUSED, play);
  }

  public static boolean getPlaySystemSoundsOnlyWhenNotFocused() {
    return prefs.getBoolean(KEY_SOUNDS_ONLY_WHEN_NOT_FOCUSED, DEFAULT_SOUNDS_ONLY_WHEN_NOT_FOCUSED);
  }

  public static void setSyrinscapeActive(boolean active) {
    prefs.putBoolean(KEY_SYRINSCAPE_ACTIVE, active);
  }

  public static boolean getSyrinscapeActive() {
    return prefs.getBoolean(KEY_SYRINSCAPE_ACTIVE, DEFAULT_SYRINSCAPE_ACTIVE);
  }

  public static void setChatColor(Color color) {
    prefs.putInt(KEY_CHAT_COLOR, color.getRGB());
  }

  public static void setFontSize(int size) {
    prefs.putInt(KEY_FONT_SIZE, size);
  }

  public static int getFontSize() {
    return prefs.getInt(KEY_FONT_SIZE, DEFAULT_FONT_SIZE);
  }

  public static void setDefaultGridColor(Color color) {
    prefs.putInt(KEY_DEFAULT_GRID_COLOR, color.getRGB());
  }

  public static Color getDefaultGridColor() {
    return new Color(prefs.getInt(KEY_DEFAULT_GRID_COLOR, DEFAULT_DEFAULT_GRID_COLOR));
  }

  public static boolean getFaceVertex() {
    return prefs.getBoolean(KEY_FACE_VERTEX, DEFAULT_FACE_VERTEX);
  }

  public static void setFaceVertex(boolean yesNo) {
    prefs.putBoolean(KEY_FACE_VERTEX, yesNo);
  }

  public static boolean getFaceEdge() {
    return prefs.getBoolean(KEY_FACE_EDGE, DEFAULT_FACE_EDGE);
  }

  public static void setFaceEdge(boolean yesNo) {
    prefs.putBoolean(KEY_FACE_EDGE, yesNo);
  }

  public static void clearAssetRoots() {
    prefs.put(KEY_ASSET_ROOTS, "");
  }

  public static void setSaveDir(File file) {
    prefs.put(KEY_SAVE_DIR, file.toString());
  }

  public static void setDefaultGridSize(int size) {
    prefs.putInt(KEY_DEFAULT_GRID_SIZE, size);
  }

  public static int getDefaultGridSize() {
    return prefs.getInt(KEY_DEFAULT_GRID_SIZE, DEFAULT_DEFAULT_GRID_SIZE);
  }

  public static void setDefaultUnitsPerCell(double size) {
    prefs.putDouble(KEY_DEFAULT_UNITS_PER_CELL, size);
  }

  public static double getDefaultUnitsPerCell() {
    return prefs.getDouble(KEY_DEFAULT_UNITS_PER_CELL, DEFAULT_DEFAULT_UNITS_PER_CELL);
  }

  public static void setDefaultVisionDistance(int dist) {
    prefs.putInt(KEY_DEFAULT_VISION_DISTANCE, dist);
  }

  public static int getDefaultVisionDistance() {
    return prefs.getInt(KEY_DEFAULT_VISION_DISTANCE, DEFAULT_DEFAULT_VISION_DISTANCE);
  }

  public static void setDefaultVisionType(Zone.VisionType visionType) {
    prefs.put(KEY_DEFAULT_VISION_TYPE, visionType.name());
  }

  public static void setMapSortType(MapSortType mapSortType) {
    prefs.put(KEY_MAP_SORT_TYPE, mapSortType.name());
  }

  public static Zone.VisionType getDefaultVisionType() {
    try {
      return Zone.VisionType.valueOf(
          prefs.get(KEY_DEFAULT_VISION_TYPE, DEFAULT_VISION_TYPE.name()));
    } catch (Exception e) {
      return DEFAULT_VISION_TYPE;
    }
  }

  public static MapSortType getMapSortType() {
    try {
      return MapSortType.valueOf(prefs.get(KEY_MAP_SORT_TYPE, DEFAULT_MAP_SORT_TYPE.name()));
    } catch (Exception e) {
      return DEFAULT_MAP_SORT_TYPE;
    }
  }

  public static void setUseSoftFogEdges(boolean flag) {
    prefs.putBoolean(KEY_USE_SOFT_FOG_EDGES, flag);
  }

  public static boolean getUseSoftFogEdges() {
    return prefs.getBoolean(KEY_USE_SOFT_FOG_EDGES, DEFAULT_USE_SOFT_FOG_EDGES);
  }

  public static void setNewMapsHaveFOW(boolean flag) {
    prefs.putBoolean(KEY_NEW_MAPS_HAVE_FOW, flag);
  }

  public static boolean getNewMapsHaveFOW() {
    return prefs.getBoolean(KEY_NEW_MAPS_HAVE_FOW, DEFAULT_NEW_MAPS_HAVE_FOW);
  }

  public static void setNewTokensVisible(boolean flag) {
    prefs.putBoolean(KEY_NEW_TOKENS_VISIBLE, flag);
  }

  public static boolean getNewTokensVisible() {
    return prefs.getBoolean(KEY_NEW_TOKENS_VISIBLE, DEFAULT_NEW_TOKENS_VISIBLE);
  }

  public static void setNewMapsVisible(boolean flag) {
    prefs.putBoolean(KEY_NEW_MAPS_VISIBLE, flag);
  }

  public static boolean getNewMapsVisible() {
    return prefs.getBoolean(KEY_NEW_MAPS_VISIBLE, DEFAULT_NEW_MAPS_VISIBLE);
  }

  public static void setNewObjectsVisible(boolean flag) {
    prefs.putBoolean(KEY_NEW_OBJECTS_VISIBLE, flag);
  }

  public static boolean getNewObjectsVisible() {
    return prefs.getBoolean(KEY_NEW_OBJECTS_VISIBLE, DEFAULT_NEW_OBJECTS_VISIBLE);
  }

  public static void setNewBackgroundsVisible(boolean flag) {
    prefs.putBoolean(KEY_NEW_BACKGROUNDS_VISIBLE, flag);
  }

  public static boolean getNewBackgroundsVisible() {
    return prefs.getBoolean(KEY_NEW_BACKGROUNDS_VISIBLE, DEFAULT_NEW_BACKGROUNDS_VISIBLE);
  }

  public static void setTokensWarnWhenDeleted(boolean flag) {
    prefs.putBoolean(KEY_TOKENS_WARN_WHEN_DELETED, flag);
  }

  public static boolean getTokensWarnWhenDeleted() {
    return prefs.getBoolean(KEY_TOKENS_WARN_WHEN_DELETED, DEFAULT_TOKENS_WARN_WHEN_DELETED);
  }

  public static void setDrawWarnWhenDeleted(boolean flag) {
    prefs.putBoolean(KEY_DRAW_WARN_WHEN_DELETED, flag);
  }

  public static boolean getDrawWarnWhenDeleted() {
    return prefs.getBoolean(KEY_DRAW_WARN_WHEN_DELETED, DEFAULT_DRAW_WARN_WHEN_DELETED);
  }

  public static void setTokensStartSnapToGrid(boolean flag) {
    prefs.putBoolean(KEY_TOKENS_START_SNAP_TO_GRID, flag);
  }

  public static boolean getTokensStartSnapToGrid() {
    return prefs.getBoolean(KEY_TOKENS_START_SNAP_TO_GRID, DEFAULT_TOKENS_START_SNAP_TO_GRID);
  }

  public static void setTokensSnapWhileDragging(boolean flag) {
    prefs.putBoolean(KEY_TOKENS_SNAP_WHILE_DRAGGING, flag);
  }

  public static boolean getTokensSnapWhileDragging() {
    return prefs.getBoolean(KEY_TOKENS_SNAP_WHILE_DRAGGING, DEFAULT_KEY_TOKENS_SNAP_WHILE_DRAGGING);
  }

  public static void setHideMousePointerWhileDragging(boolean flag) {
    prefs.putBoolean(KEY_HIDE_MOUSE_POINTER_WHILE_DRAGGING, flag);
  }

  public static boolean getHideMousePointerWhileDragging() {
    return prefs.getBoolean(
        KEY_HIDE_MOUSE_POINTER_WHILE_DRAGGING, DEFAULT_KEY_HIDE_MOUSE_POINTER_WHILE_DRAGGING);
  }

  public static void setHideTokenStackIndicator(boolean flag) {
    prefs.putBoolean(KEY_HIDE_TOKEN_STACK_INDICATOR, flag);
  }

  public static boolean getHideTokenStackIndicator() {
    return prefs.getBoolean(KEY_HIDE_TOKEN_STACK_INDICATOR, DEFAULT_KEY_HIDE_TOKEN_STACK_INDICATOR);
  }

  public static void setObjectsStartSnapToGrid(boolean flag) {
    prefs.putBoolean(KEY_OBJECTS_START_SNAP_TO_GRID, flag);
  }

  public static boolean getObjectsStartSnapToGrid() {
    return prefs.getBoolean(KEY_OBJECTS_START_SNAP_TO_GRID, DEFAULT_OBJECTS_START_SNAP_TO_GRID);
  }

  public static void setTokensStartFreesize(boolean flag) {
    prefs.putBoolean(KEY_TOKENS_START_FREESIZE, flag);
  }

  public static boolean getTokensStartFreesize() {
    return prefs.getBoolean(KEY_TOKENS_START_FREESIZE, DEFAULT_TOKENS_START_FREESIZE);
  }

  public static void setObjectsStartFreesize(boolean flag) {
    prefs.putBoolean(KEY_OBJECTS_START_FREESIZE, flag);
  }

  public static boolean getObjectsStartFreesize() {
    return prefs.getBoolean(KEY_OBJECTS_START_FREESIZE, DEFAULT_OBJECTS_START_FREESIZE);
  }

  public static void setBackgroundsStartSnapToGrid(boolean flag) {
    prefs.putBoolean(KEY_BACKGROUNDS_START_SNAP_TO_GRID, flag);
  }

  public static boolean getBackgroundsStartSnapToGrid() {
    return prefs.getBoolean(
        KEY_BACKGROUNDS_START_SNAP_TO_GRID, DEFAULT_BACKGROUNDS_START_SNAP_TO_GRID);
  }

  public static void setBackgroundsStartFreesize(boolean flag) {
    prefs.putBoolean(KEY_BACKGROUNDS_START_FREESIZE, flag);
  }

  public static boolean getBackgroundsStartFreesize() {
    return prefs.getBoolean(KEY_BACKGROUNDS_START_FREESIZE, DEFAULT_BACKGROUNDS_START_FREESIZE);
  }

  public static String getDefaultGridType() {
    return prefs.get(KEY_DEFAULT_GRID_TYPE, DEFAULT_DEFAULT_GRID_TYPE);
  }

  public static void setDefaultGridType(String type) {
    prefs.put(KEY_DEFAULT_GRID_TYPE, type);
  }

  public static boolean getShowStatSheet() {
    return prefs.getBoolean(KEY_SHOW_STAT_SHEET, DEFAULT_SHOW_STAT_SHEET);
  }

  public static void setShowStatSheet(boolean show) {
    prefs.putBoolean(KEY_SHOW_STAT_SHEET, show);
  }

  public static boolean getShowPortrait() {
    return prefs.getBoolean(KEY_SHOW_PORTRAIT, DEFAULT_SHOW_PORTRAIT);
  }

  public static void setShowPortrait(boolean show) {
    prefs.putBoolean(KEY_SHOW_PORTRAIT, show);
  }

  public static boolean getShowStatSheetModifier() {
    return prefs.getBoolean(KEY_SHOW_STAT_SHEET_MODIFIER, DEFAULT_SHOW_STAT_SHEET_MODIFIER);
  }

  public static void setShowStatSheetModifier(boolean show) {
    prefs.putBoolean(KEY_SHOW_STAT_SHEET_MODIFIER, show);
  }

  public static boolean getForceFacingArrow() {
    return prefs.getBoolean(KEY_FORCE_FACING_ARROW, DEFAULT_FORCE_FACING_ARROW);
  }

  public static void setForceFacingArrow(boolean show) {
    prefs.putBoolean(KEY_FORCE_FACING_ARROW, show);
  }

  public static boolean getFitGMView() {
    return prefs.getBoolean(KEY_FIT_GM_VIEW, DEFAULT_FIT_GM_VIEW);
  }

  public static void setFitGMView(boolean fit) {
    prefs.putBoolean(KEY_FIT_GM_VIEW, fit);
  }

  public static String getDefaultUserName() {
    return prefs.get(KEY_DEFAULT_USERNAME, DEFAULT_USERNAME);
  }

  public static void setDefaultUserName(String uname) {
    prefs.put(KEY_DEFAULT_USERNAME, uname);
  }

  public static void setMovementMetric(WalkerMetric metric) {
    prefs.put(KEY_MOVEMENT_METRIC, metric.name());
  }

  public static void setFrameRateCap(int cap) {
    prefs.putInt(KEY_FRAME_RATE_CAP, cap);
  }

  public static int getFrameRateCap() {
    return prefs.getInt(KEY_FRAME_RATE_CAP, DEFAULT_FRAME_RATE_CAP);
  }

  public static void setUpnpDiscoveryTimeout(int timeout) {
    prefs.putInt(KEY_UPNP_DISCOVERY_TIMEOUT, timeout);
  }

  public static int getUpnpDiscoveryTimeout() {
    return prefs.getInt(KEY_UPNP_DISCOVERY_TIMEOUT, DEFAULT_UPNP_DISCOVERY_TIMEOUT);
  }

  public static String getFileSyncPath() {
    return prefs.get(KEY_FILE_SYNC_PATH, DEFAULT_FILE_SYNC_PATH);
  }

  public static void setFileSyncPath(String path) {
    prefs.put(KEY_FILE_SYNC_PATH, path);
  }

  public static boolean getSkipAutoUpdate() {
    return prefs.getBoolean(KEY_SKIP_AUTO_UPDATE, DEFAULT_SKIP_AUTO_UPDATE);
  }

  public static void setSkipAutoUpdate(boolean value) {
    prefs.putBoolean(KEY_SKIP_AUTO_UPDATE, value);
  }

  public static String getSkipAutoUpdateRelease() {
    return prefs.get(KEY_SKIP_AUTO_UPDATE_RELEASE, DEFAULT_SKIP_AUTO_UPDATE_RELEASE);
  }

  public static void setSkipAutoUpdateRelease(String releaseId) {
    prefs.put(KEY_SKIP_AUTO_UPDATE_RELEASE, releaseId);
  }

  public static boolean getAllowExternalMacroAccess() {
    return prefs.getBoolean(KEY_ALLOW_EXTERNAL_MACRO_ACCESS, DEFAULT_ALLOW_EXTERNAL_MACRO_ACCESS);
  }

  public static void setAllowExternalMacroAccess(boolean value) {
    prefs.putBoolean(KEY_ALLOW_EXTERNAL_MACRO_ACCESS, value);
  }

  public static WalkerMetric getMovementMetric() {
    WalkerMetric metric;
    try {
      metric = WalkerMetric.valueOf(prefs.get(KEY_MOVEMENT_METRIC, DEFAULT_MOVEMENT_METRIC.name()));
    } catch (Exception exc) {
      metric = DEFAULT_MOVEMENT_METRIC;
    }
    return metric;
  }

  public static File getSaveDir() {
    String filePath = prefs.get(KEY_SAVE_DIR, null);
    return filePath != null ? new File(filePath) : new File(File.separator);
  }

  public static File getSaveTokenDir() {
    String filePath = prefs.get(KEY_SAVE_TOKEN_DIR, null);
    return filePath != null ? new File(filePath) : getSaveDir();
  }

  public static void setTokenSaveDir(File file) {
    prefs.put(KEY_SAVE_TOKEN_DIR, file.toString());
  }

  public static File getSaveMapDir() {
    String filePath = prefs.get(KEY_SAVE_MAP_DIR, null);
    return filePath != null ? new File(filePath) : getSaveDir();
  }

  public static void setSaveMapDir(File file) {
    prefs.put(KEY_SAVE_MAP_DIR, file.toString());
  }

  public static void setLoadDir(File file) {
    prefs.put(KEY_LOAD_DIR, file.toString());
  }

  public static File getLoadDir() {
    String filePath = prefs.get(KEY_LOAD_DIR, null);
    return filePath != null ? new File(filePath) : new File(File.separator);
  }

  public static File getAddOnLoadDir() {
    String filePath = prefs.get(KEY_ADD_ON_LOAD_DIR, null);
    return filePath != null ? new File(filePath) : getSaveDir();
  }

  public static void setAddOnLoadDir(File file) {
    prefs.put(KEY_ADD_ON_LOAD_DIR, file.toString());
  }

  public static void addAssetRoot(File root) {
    String list = prefs.get(KEY_ASSET_ROOTS, "");
    if (!list.isEmpty()) {
      // Add the new one and then remove all duplicates.
      list += ";" + root.getPath();
      String[] roots = list.split(";");
      StringBuilder result = new StringBuilder(list.length() + root.getPath().length() + 10);
      Set<String> rootList = new HashSet<String>(roots.length);

      // This loop ensures that each path only appears once. If there are currently
      // duplicates in the list, only the first one is kept.
      for (String r : roots) {
        if (!rootList.contains(r)) {
          result.append(';').append(r);
          rootList.add(r);
        }
      }
      list = result.substring(1);
    } else {
      list += root.getPath();
    }
    prefs.put(KEY_ASSET_ROOTS, list);
  }

  public static Set<File> getAssetRoots() {
    String list = prefs.get(KEY_ASSET_ROOTS, "");
    String[] roots = list.split(";"); // FJE Probably should be File.path_separator ...

    Set<File> rootList = new HashSet<File>();
    for (String root : roots) {
      File file = new File(root);

      // LATER: Should this actually remove it from the pref list ?
      if (!file.exists()) {
        continue;
      }
      rootList.add(file);
    }
    return rootList;
  }

  public static void removeAssetRoot(File root) {
    String list = prefs.get(KEY_ASSET_ROOTS, "");
    if (!list.isEmpty()) {
      // Add the new one and then remove all duplicates.
      String[] roots = list.split(";");
      StringBuilder result = new StringBuilder(list.length());
      Set<String> rootList = new HashSet<String>(roots.length);
      String rootPath = root.getPath();

      // This loop ensures that each path only appears once. If there are
      // duplicates in the list, only the first one is kept.
      for (String r : roots) {
        if (!r.equals(rootPath) && !rootList.contains(r)) {
          result.append(';').append(r);
          rootList.add(r);
        }
      }
      list = result.substring(result.length() > 0 ? 1 : 0);
      prefs.put(KEY_ASSET_ROOTS, list);
    }
  }

  public static void setMruCampaigns(List<File> mruCampaigns) {
    StringBuilder combined = new StringBuilder();
    for (File file : mruCampaigns) {
      String path = null;
      try {
        path = file.getCanonicalPath();
      } catch (IOException e) {
        // Probably pretty rare, but we want to know about it
        log.info("unexpected during file.getCanonicalPath()", e); // $NON-NLS-1$
        path = file.getPath();
      }
      // It's important that '%3A' is done last. Note that the pathSeparator may not be a colon on
      // the current platform, but it doesn't matter since it will be reconverted when read back in
      // again.
      // THink of the '%3A' as a symbol of the separator, not an encoding of the character.
      combined.append(path.replaceAll("%", "%25").replaceAll(File.pathSeparator, "%3A"));
      combined.append(File.pathSeparator);
    }
    prefs.put(KEY_MRU_CAMPAIGNS, combined.toString());
  }

  public static List<File> getMruCampaigns() {
    List<File> mruCampaigns = new ArrayList<File>();
    String combined = prefs.get(KEY_MRU_CAMPAIGNS, null);
    if (combined != null) {
      // It's important that '%3A' is done first
      combined = combined.replaceAll("%3A", File.pathSeparator).replaceAll("%25", "%");
      String[] all = combined.split(File.pathSeparator);
      for (String s : all) {
        mruCampaigns.add(new File(s));
      }
    }
    return mruCampaigns;
  }

  public static void setSavedPaintTextures(List<File> savedTextures) {
    StringBuilder combined = new StringBuilder();
    for (File savedTexture : savedTextures) {
      combined.append(savedTexture.getPath());
      combined.append(File.pathSeparator);
    }
    prefs.put(KEY_SAVED_PAINT_TEXTURES, combined.toString());
  }

  public static List<File> getSavedPaintTextures() {
    List<File> savedTextures = new ArrayList<File>();
    String combined = prefs.get(KEY_SAVED_PAINT_TEXTURES, null);
    if (combined != null) {
      String[] all = combined.split(File.pathSeparator);
      for (String s : all) {
        savedTextures.add(new File(s));
      }
    }
    return savedTextures;
  }

  private static final String INIT_SHOW_TOKENS = "initShowTokens";
  private static final boolean DEFAULT_INIT_SHOW_TOKENS = true;

  private static final String INIT_SHOW_TOKEN_STATES = "initShowTokenStates";
  private static final boolean DEFAULT_INIT_SHOW_TOKEN_STATES = true;

  private static final String INIT_SHOW_INITIATIVE = "initShowInitiative";
  private static final boolean DEFAULT_INIT_SHOW_INITIATIVE = true;

  private static final String INIT_SHOW_2ND_LINE = "initShow2ndLine";
  private static final boolean DEFAULT_INIT_SHOW_2ND_LINE = false;

  private static final String INIT_HIDE_NPCS = "initHideNpcs";
  private static final boolean DEFAULT_INIT_HIDE_NPCS = false;

  private static final String INIT_OWNER_PERMISSIONS = "initOwnerPermissions";
  private static final boolean DEFAULT_INIT_OWNER_PERMISSIONS = false;

  private static final String INIT_LOCK_MOVEMENT = "initLockMovement";
  private static final boolean DEFAULT_INIT_LOCK_MOVEMENT = false;

  public static boolean getInitShowTokens() {
    return prefs.getBoolean(INIT_SHOW_TOKENS, DEFAULT_INIT_SHOW_TOKENS);
  }

  public static void setInitShowTokens(boolean showTokens) {
    prefs.putBoolean(INIT_SHOW_TOKENS, showTokens);
  }

  public static boolean getInitShowTokenStates() {
    return prefs.getBoolean(INIT_SHOW_TOKEN_STATES, DEFAULT_INIT_SHOW_TOKEN_STATES);
  }

  public static void setInitShowTokenStates(boolean showTokenStates) {
    prefs.putBoolean(INIT_SHOW_TOKEN_STATES, showTokenStates);
  }

  public static boolean getInitShowInitiative() {
    return prefs.getBoolean(INIT_SHOW_INITIATIVE, DEFAULT_INIT_SHOW_INITIATIVE);
  }

  public static void setInitShowInitiative(boolean showInitiative) {
    prefs.putBoolean(INIT_SHOW_INITIATIVE, showInitiative);
  }

  public static boolean getInitShow2ndLine() {
    return prefs.getBoolean(INIT_SHOW_2ND_LINE, DEFAULT_INIT_SHOW_2ND_LINE);
  }

  public static void setInitShow2ndLine(boolean secondLine) {
    prefs.putBoolean(INIT_SHOW_2ND_LINE, secondLine);
  }

  public static boolean getInitHideNpcs() {
    return prefs.getBoolean(INIT_HIDE_NPCS, DEFAULT_INIT_HIDE_NPCS);
  }

  public static void setInitHideNpcs(boolean hideNpcs) {
    prefs.putBoolean(INIT_HIDE_NPCS, hideNpcs);
  }

  public static boolean getInitOwnerPermissions() {
    return prefs.getBoolean(INIT_OWNER_PERMISSIONS, DEFAULT_INIT_OWNER_PERMISSIONS);
  }

  public static void setInitOwnerPermissions(boolean ownerPermissions) {
    prefs.putBoolean(INIT_OWNER_PERMISSIONS, ownerPermissions);
  }

  public static boolean getInitLockMovement() {
    return prefs.getBoolean(INIT_LOCK_MOVEMENT, DEFAULT_INIT_LOCK_MOVEMENT);
  }

  public static void setInitLockMovement(boolean lockMovement) {
    prefs.putBoolean(INIT_LOCK_MOVEMENT, lockMovement);
  }

  public static boolean getChatNotificationShowBackground() {
    // System.out.println("Getting Value:" + prefs.getBoolean(KEY_CHAT_NOTIFICATION_SHOW_BACKGROUND,
    // DEFAULT_CHAT_NOTIFICATION_SHOW_BACKGROUND));
    return prefs.getBoolean(
        KEY_CHAT_NOTIFICATION_SHOW_BACKGROUND, DEFAULT_CHAT_NOTIFICATION_SHOW_BACKGROUND);
  }

  public static void setChatNotificationShowBackground(boolean flag) {
    prefs.putBoolean(KEY_CHAT_NOTIFICATION_SHOW_BACKGROUND, flag);
  }

  public static boolean isShowInitGainMessage() {
    // KEY_SHOW_INIT_GAIN_MESSAGE
    return prefs.getBoolean(KEY_SHOW_INIT_GAIN_MESSAGE, DEFAULT_SHOW_INIT_GAIN_MESSAGE);
  }

  public static void setShowInitGainMessage(boolean flag) {
    prefs.putBoolean(KEY_SHOW_INIT_GAIN_MESSAGE, flag);
  }

  public static boolean isUsingAstarPathfinding() {
    return prefs.getBoolean(KEY_USE_ASTAR_PATHFINDING, DEFAULT_USE_ASTAR_PATHFINDING);
  }

  public static void setUseAstarPathfinding(boolean show) {
    prefs.putBoolean(KEY_USE_ASTAR_PATHFINDING, show);
  }

  public static boolean getVblBlocksMove() {
    return prefs.getBoolean(KEY_VBL_BLOCKS_MOVE, DEFAULT_VBL_BLOCKS_MOVE);
  }

  public static void setVblBlocksMove(boolean use) {
    prefs.putBoolean(KEY_VBL_BLOCKS_MOVE, use);
  }

  public static String getDefaultMacroEditorTheme() {
    return prefs.get(MACRO_EDITOR_THEME, DEFAULT_MACRO_EDITOR_THEME);
  }

  public static void setDefaultMacroEditorTheme(String type) {
    prefs.put(MACRO_EDITOR_THEME, type);
  }

  public static String getIconTheme() {
    return prefs.get(ICON_THEME, DEFAULT_ICON_THEME);
  }

  public static void setIconTheme(String theme) {
    prefs.put(ICON_THEME, theme);
  }

  public static Zone.TopologyTypeSet getTopologyTypes() {
    try {
      String typeNames = prefs.get(KEY_TOPOLOGY_TYPES, "");
      if ("".equals(typeNames)) {
        // Fallback to the key used prior to the introduction of various VBL types.
        String oldDrawingMode = prefs.get(KEY_OLD_TOPOLOGY_DRAWING_MODE, DEFAULT_TOPOLOGY_TYPE);
        return switch (oldDrawingMode) {
          default -> new Zone.TopologyTypeSet(Zone.TopologyType.WALL_VBL);
          case "VBL" -> new Zone.TopologyTypeSet(Zone.TopologyType.WALL_VBL);
          case "MBL" -> new Zone.TopologyTypeSet(Zone.TopologyType.MBL);
          case "COMBINED" -> new Zone.TopologyTypeSet(
              Zone.TopologyType.WALL_VBL, Zone.TopologyType.MBL);
        };
      } else {
        return Zone.TopologyTypeSet.valueOf(typeNames);
      }
    } catch (Exception exc) {
      return new Zone.TopologyTypeSet(Zone.TopologyType.WALL_VBL);
    }
  }

  public static void setWebEndPointPort(int value) {
    prefs.putInt(KEY_WEB_END_POINT_PORT, value);
  }

  public static int getWebEndPointPort() {
    return prefs.getInt(KEY_WEB_END_POINT_PORT, DEFAULT_WEB_END_POINT);
  }

  // Based off vision type enum in Zone.java, this could easily get tossed somewhere else if
  // preferred.
  public enum MapSortType {
    DISPLAYNAME(),
    GMNAME();

    private final String displayName;

    MapSortType() {
      displayName = I18N.getString("mapSortType." + name());
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  /**
   * Sets the topology mode preference.
   *
   * @param types the topology types. A value of null resets to default.
   */
  public static void setTopologyTypes(Zone.TopologyTypeSet types) {
    if (types == null) {
      prefs.remove(KEY_TOPOLOGY_TYPES);
    } else {
      prefs.put(KEY_TOPOLOGY_TYPES, types.toString());
    }
  }
}
