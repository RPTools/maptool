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
import java.util.prefs.Preferences;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.preferences.Preference;
import net.rptools.maptool.util.preferences.PreferenceStore;

/** Manages and persists user preferences for the application. */
public class AppPreferences {
  private static final PreferenceStore store =
      new PreferenceStore(Preferences.userRoot().node(AppConstants.APP_NAME + "/prefs"));

  public static final Preference<Boolean> fillSelectionBox =
      store.defineBoolean("fillSelectionBox", true);

  public static final Preference<Color> chatColor =
      store.defineColor("chatColor", Color.black, false);

  public static final Preference<Boolean> saveReminder =
      store.defineBoolean("autoSaveReminder", true);

  public static final Preference.Numeric<Integer> autoSaveIncrement =
      store.defineInteger("autoSaveIncrement", 5);

  public static final Preference.Numeric<Integer> chatAutoSaveTimeInMinutes =
      store.defineInteger("chatAutosaveTime", 0);

  public static final Preference<String> chatFilenameFormat =
      store.defineString("chatFilenameFormat", "chatlog-%1$tF-%1$tR.html");

  public static final Preference<String> tokenNumberDisplay =
      store.defineString("tokenNumberDisplayg", "Name");

  public static final Preference<String> duplicateTokenNumber =
      store.defineString("duplicateTokenNumber", "Increment");

  public static final Preference<String> newTokenNaming =
      store.defineString("newTokenNaming", "Use Filename");

  public static final Preference<Boolean> useHaloColorOnVisionOverlay =
      store.defineBoolean("useHaloColorForVisionOverlay", false);

  public static final Preference<Boolean> mapVisibilityWarning =
      store.defineBoolean("mapVisibilityWarning", false);

  public static final Preference<Boolean> autoRevealVisionOnGMMovement =
      store.defineBoolean("autoRevealVisionOnGMMove", false);

  public static final Preference.Numeric<Integer> haloOverlayOpacity =
      store.defineByte("haloOverlayOpacity", 60);

  public static final Preference.Numeric<Integer> auraOverlayOpacity =
      store.defineByte("auraOverlayOpacity", 60);

  public static final Preference.Numeric<Integer> lightOverlayOpacity =
      store.defineByte("lightOverlayOpacity", 60);

  public static final Preference.Numeric<Integer> lumensOverlayOpacity =
      store.defineByte("lumensOverlayOpacity", 120);

  public static final Preference.Numeric<Integer> fogOverlayOpacity =
      store.defineByte("fogOverlayOpacity", 100);

  public static final Preference.Numeric<Integer> lumensOverlayBorderThickness =
      store.defineInteger("lumensOverlayBorderThickness", 5);

  public static final Preference<Boolean> lumensOverlayShowByDefault =
      store.defineBoolean("lumensOverlayShowByDefault", false);

  public static final Preference<Boolean> lightsShowByDefault =
      store.defineBoolean("lightsShowByDefault", true);

  public static final Preference.Numeric<Integer> haloLineWidth =
      store.defineInteger("haloLineWidth", 2);

  public static final Preference.Numeric<Integer> typingNotificationDurationInSeconds =
      store.defineInteger("typingNotificationDuration", 5);

  public static final Preference<Boolean> chatNotificationBackground =
      store.defineBoolean("chatNotificationShowBackground", true);

  public static final Preference<Boolean> useToolTipForInlineRoll =
      store.defineBoolean("toolTipInlineRolls", false);

  public static final Preference<Boolean> suppressToolTipsForMacroLinks =
      store.defineBoolean("suppressToolTipsMacroLinks", false);

  public static final Preference<Color> chatNotificationColor =
      store.defineColor("chatNotificationColor", Color.white, false);

  public static final Preference<Color> trustedPrefixBackground =
      store.defineColor("trustedPrefixBG", new Color(0xD8, 0xE9, 0xF6), false);

  public static final Preference<Color> trustedPrefixForeground =
      store.defineColor("trustedPrefixFG", Color.BLACK, false);

  public static final Preference.Numeric<Integer> toolTipInitialDelay =
      store.defineInteger("toolTipInitialDelay", 250);

  public static final Preference.Numeric<Integer> toolTipDismissDelay =
      store.defineInteger("toolTipDismissDelay", 30000);

  public static final Preference<Boolean> openEditorForNewMacro =
      store.defineBoolean("openEditorForNewMacro", true);

  public static final Preference<Boolean> allowPlayerMacroEditsDefault =
      store.defineBoolean("allowPlayerMacroEditsDefault", true);

  public static final Preference.Numeric<Integer> portraitSize =
      store.defineInteger("portraitSize", 175);

  public static final Preference.Numeric<Integer> thumbnailSize =
      store.defineInteger("thumbnailSize", 500);

  public static final Preference<Boolean> showSmilies = store.defineBoolean("insertSmilies", true);

  public static final Preference<Boolean> showDialogOnNewToken =
      store.defineBoolean("showDialogOnNewToken", true);

  public static final Preference<Boolean> showAvatarInChat =
      store.defineBoolean("showAvatarInChat", true);

  public static final Preference<Boolean> playSystemSounds =
      store.defineBoolean("playSystemSounds", true);

  public static final Preference<Boolean> playSystemSoundsOnlyWhenNotFocused =
      store.defineBoolean("playSystemSoundsOnlyWhenNotFocused", false);

  public static final Preference<Boolean> playStreams = store.defineBoolean("playStreams", true);

  public static final Preference<Boolean> syrinscapeActive =
      store.defineBoolean("syrinscapeActive", false);

  public static final Preference.Numeric<Integer> fontSize = store.defineInteger("fontSize", 12);

  public static final Preference<Color> defaultGridColor =
      store.defineColor("defaultGridColor", Color.black, false);

  public static final Preference.Numeric<Integer> defaultGridSize =
      store.defineInteger("defaultGridSize", 100);

  public static final Preference.Numeric<Double> defaultUnitsPerCell =
      store.defineDouble("unitsPerCell", 5.);

  public static final Preference<Boolean> faceVertex = store.defineBoolean("faceVertex", false);

  public static final Preference<Boolean> faceEdge = store.defineBoolean("faceEdge", true);

  public static final Preference.Numeric<Integer> defaultVisionDistance =
      store.defineInteger("defaultVisionDistance", 1000);

  public static final Preference<Zone.VisionType> defaultVisionType =
      store.defineEnum(Zone.VisionType.class, "defaultVisionType", Zone.VisionType.OFF);

  public static final Preference<MapSortType> mapSortType =
      store.defineEnum(MapSortType.class, "sortByGMName", MapSortType.GMNAME);

  public static final Preference<UvttLosImportType> uvttLosImportType =
      store.defineEnum(UvttLosImportType.class, "uvttLosImportType", UvttLosImportType.Prompt);

  public static final Preference<Boolean> useSoftFogEdges = store.defineBoolean("useSoftFog", true);

  public static final Preference<Boolean> newMapsHaveFow =
      store.defineBoolean("newMapsHaveFow", false);

  public static final Preference<Boolean> newTokensVisible =
      store.defineBoolean("newTokensVisible", true);

  public static final Preference<Boolean> newMapsVisible =
      store.defineBoolean("newMapsVisible", true);

  public static final Preference<Boolean> newObjectsVisible =
      store.defineBoolean("newObjectsVisible", true);

  public static final Preference<Boolean> newBackgroundsVisible =
      store.defineBoolean("newBackgroundsVisible", true);

  public static final Preference<File> saveDirectory =
      store.defineFile("saveDir", () -> new File(File.separator));

  public static final Preference<File> tokenSaveDirectory =
      store.defineFile("saveTokenDir", saveDirectory::get);

  public static final Preference<File> mapSaveDirectory =
      store.defineFile("saveMapDir", saveDirectory::get);

  public static final Preference<File> addOnLoadDirectory =
      store.defineFile("addOnLoadDir", saveDirectory::get);

  public static final Preference<File> loadDirectory =
      store.defineFile("loadDir", () -> new File(File.separator));

  public static final Preference<RenderQuality> renderQuality =
      store
          .defineEnum(RenderQuality.class, "renderScaleQuality", RenderQuality.LOW_SCALING)
          .cacheIt();

  /** The background color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelBackground =
      store.defineColor("npcMapLabelBG", Color.LIGHT_GRAY, true);

  /** The foreground color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelForeground =
      store.defineColor("npcMapLabelFG", Color.BLACK, true);

  /** The border color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelBorder =
      store.defineColor("mapLabelBorderColor", npcMapLabelForeground.getDefault(), true);

  /** The background color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelBackground =
      store.defineColor("pcMapLabelBG", Color.WHITE, true);

  /** The foreground color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelForeground =
      store.defineColor("pcMapLabelFG", Color.BLUE, true);

  /** The border color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelBorder =
      store.defineColor("pcMapLabelBorderColor", pcMapLabelForeground.getDefault(), true);

  /** The background color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelBackground =
      store.defineColor("nonVisMapLabelBG", Color.BLACK, true);

  /** The foreground color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelForeground =
      store.defineColor("nonVisMapLabelFG", Color.WHITE, true);

  /** The border color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelBorder =
      store.defineColor(
          "nonVisMapLabelBorderColor", nonVisibleTokenMapLabelForeground.getDefault(), true);

  /** The font size to use for token map labels. */
  public static final Preference.Numeric<Integer> mapLabelFontSize =
      store.defineInteger("mapLabelFontSize", AppStyle.labelFont.getSize());

  /** The width of the border for token map labels, in pixels. */
  public static final Preference.Numeric<Integer> mapLabelBorderWidth =
      store.defineInteger("mapLabelBorderWidth", Label.DEFAULT_LABEL_BORDER_WIDTH);

  /** The size of the border arc for token map labels. */
  public static final Preference.Numeric<Integer> mapLabelBorderArc =
      store.defineInteger("mapLabelBorderArc", Label.DEFAULT_LABEL_BORDER_ARC);

  /** {@code true} if borders should be shown around map labels, {@code false} otherwise. */
  public static final Preference<Boolean> mapLabelShowBorder =
      store.defineBoolean("mapLabelShowBorder", true);

  public static final Preference.Numeric<Integer> webEndpointPort =
      store.defineInteger("webEndPointPort", 654555);

  public static final Preference<Boolean> tokensWarnWhenDeleted =
      store.defineBoolean("tokensWarnWhenDeleted", true);

  public static final Preference<Boolean> drawingsWarnWhenDeleted =
      store.defineBoolean("drawWarnWhenDeleted", true);

  public static final Preference<Boolean> tokensSnapWhileDragging =
      store.defineBoolean("tokensSnapWhileDragging", true);

  public static final Preference<Boolean> hideMousePointerWhileDragging =
      store.defineBoolean("hideMousePointerWhileDragging", true);

  public static final Preference<Boolean> hideTokenStackIndicator =
      store.defineBoolean("hideTokenStackIndicator", false);

  public static final Preference<Boolean> tokensStartSnapToGrid =
      store.defineBoolean("newTokensStartSnapToGrid", true);

  public static final Preference<Boolean> objectsStartSnapToGrid =
      store.defineBoolean("newStampsStartSnapToGrid", false);

  public static final Preference<Boolean> backgroundsStartSnapToGrid =
      store.defineBoolean("newBackgroundsStartSnapToGrid", false);

  public static final Preference<Boolean> tokensStartFreesize =
      store.defineBoolean("newTokensStartFreesize", false);

  public static final Preference<Boolean> objectsStartFreesize =
      store.defineBoolean("newStampsStartFreesize", true);

  public static final Preference<Boolean> backgroundsStartFreesize =
      store.defineBoolean("newBackgroundsStartFreesize", true);

  public static final Preference<String> defaultGridType =
      store.defineString("defaultGridType", GridFactory.SQUARE);

  public static final Preference<Boolean> showStatSheet =
      store.defineBoolean("showStatSheet", true);

  public static final Preference<Boolean> showStatSheetRequiresModifierKey =
      store.defineBoolean("showStatSheetModifier", false);

  public static final Preference<Boolean> showPortrait = store.defineBoolean("showPortrait", true);

  public static final Preference<Boolean> forceFacingArrow =
      store.defineBoolean("forceFacingArrow", false);

  public static final Preference<Boolean> fitGmView = store.defineBoolean("fitGMView", true);

  public static final Preference<String> defaultUserName =
      store.defineString(
          "defaultUsername", I18N.getString("Preferences.client.default.username.value"));

  public static final Preference<WalkerMetric> movementMetric =
      store.defineEnum(WalkerMetric.class, "movementMetric", WalkerMetric.ONE_TWO_ONE);

  public static final Preference.Numeric<Integer> frameRateCap =
      store.defineInteger("frameRateCap", 60, 1, Integer.MAX_VALUE);

  /* Scroll status bar information messages that exceed the available size */
  public static final Preference<Boolean> scrollStatusMessages =
      store.defineBoolean("statusBarScroll", true);
  /* Scroll status bar scrolling speed */
  public static final Preference.Numeric<Double> scrollStatusSpeed =
      store.defineDouble("statusBarSpeed", 0.85);
  /* Scroll status bar scrolling start delay */
  public static final Preference.Numeric<Double> scrollStatusStartDelay =
      store.defineDouble("statusBarDelay", 2.4);
  /* Scroll status bar scrolling end pause */
  public static final Preference.Numeric<Double> scrollStatusEndPause =
      store.defineDouble("statusBarDelay", 1.8);
  /* Status bar temporary notification duration */
  public static final Preference.Numeric<Double> scrollStatusTempDuration =
      store.defineDouble("scrollStatusTempDuration", 12d);

  public static final Preference.Numeric<Integer> upnpDiscoveryTimeout =
      store.defineInteger("upnpDiscoveryTimeout", 5000);

  public static final Preference<String> fileSyncPath = store.defineString("fileSyncPath", "");

  public static final Preference<Boolean> skipAutoUpdate =
      store.defineBoolean("skipAutoUpdate", false);

  public static final Preference<String> skipAutoUpdateRelease =
      store.defineString("skipAutoUpdateRelease", "");

  public static final Preference<Boolean> allowExternalMacroAccess =
      store.defineBoolean("allowExternalMacroAccess", false);

  public static final Preference<Boolean> loadMruCampaignAtStart =
      store.defineBoolean("loadMRUCampaignAtStart", false);

  public static final Preference<Boolean> initiativePanelShowsTokenImage =
      store.defineBoolean("initShowTokens", true);

  public static final Preference<Boolean> initiativePanelShowsTokenState =
      store.defineBoolean("initShowTokenStates", true);

  public static final Preference<Boolean> initiativePanelShowsInitiative =
      store.defineBoolean("initShowInitiative", true);

  public static final Preference<Boolean> initiativePanelShowsInitiativeOnLine2 =
      store.defineBoolean("initShow2ndLine", false);

  public static final Preference<Boolean> initiativePanelHidesNpcs =
      store.defineBoolean("initHideNpcs", false);

  public static final Preference<Boolean> initiativePanelAllowsOwnerPermissions =
      store.defineBoolean("initOwnerPermissions", false);

  public static final Preference<Boolean> initiativeMovementLocked =
      store.defineBoolean("initLockMovement", false);

  public static final Preference<Boolean> showInitiativeGainedMessage =
      store.defineBoolean("showInitGainMessage", true);

  public static final Preference<Boolean> initiativePanelWarnWhenResettingRoundCounter =
      store.defineBoolean("initWarnWhenResettingRoundCounter", true);

  public static final Preference<Boolean> pathfindingEnabled =
      store.defineBoolean("useAstarPathfinding", true);

  public static final Preference<Boolean> pathfindingBlockedByVbl =
      store.defineBoolean("vblBlocksMove", true);

  public static final Preference<String> defaultMacroEditorTheme =
      store.defineString("macroEditorTheme", "Default");

  public static final Preference<String> iconTheme =
      store.defineString("iconTheme", "Rod Takehara");

  public static final Preference<Boolean> useCustomThemeFontProperties =
      store.defineBoolean("useCustomUIProperties", false);

  static {
    var prefs = store.getStorage();

    // Used to be stored as separate components but now is one color. Add if not already there.
    if (prefs.get("trustedPrefixFG", null) == null) {
      var defaultValue = trustedPrefixForeground.getDefault();
      trustedPrefixForeground.set(
          new Color(
              prefs.getInt("trustedPrefixFGRed", defaultValue.getRed()),
              prefs.getInt("trustedPrefixFGGreen", defaultValue.getGreen()),
              prefs.getInt("trustedPrefixFBlue", defaultValue.getBlue())));
    }
    if (prefs.get("trustedPrefixBG", null) == null) {
      var defaultValue = trustedPrefixBackground.getDefault();
      trustedPrefixBackground.set(
          new Color(
              prefs.getInt("trustedPrefixBGRed", defaultValue.getRed()),
              prefs.getInt("trustedPrefixBGGreen", defaultValue.getGreen()),
              prefs.getInt("trustedPrefixBBlue", defaultValue.getBlue())));
    }
    if (prefs.get("chatNotificationColor", null) == null) {
      var defaultValue = chatNotificationColor.getDefault();
      chatNotificationColor.set(
          new Color(
              prefs.getInt("chatNotificationColorRed", defaultValue.getRed()),
              prefs.getInt("chatNotificationColorGreen", defaultValue.getGreen()),
              prefs.getInt("chatNotificationColorBlue", defaultValue.getBlue())));
    }
  }

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

  public enum UvttLosImportType {
    Walls("uvttLosImportType.walls"),
    Masks("uvttLosImportType.masks"),
    Prompt("uvttLosImportType.prompt");

    private final String displayName;

    UvttLosImportType(String key) {
      displayName = I18N.getString(key);
    }

    @Override
    public String toString() {
      return displayName;
    }
  }
}
