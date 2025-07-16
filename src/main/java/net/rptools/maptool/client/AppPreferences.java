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

import java.awt.Color;
import java.io.File;
import java.util.prefs.Preferences;
import net.rptools.lib.image.RenderQuality;
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
  private static final boolean PRINT_KEYS_MISSING_I18N_ON_STARTUP =
      false; // for finding pesky wabbitses

  public static PreferenceStore getAppPreferenceStore() {
    return store;
  }

  public static final Preference<Boolean> fillSelectionBox =
      store.defineBoolean(
          "fillSelectionBox",
          "Preferences.label.performance.fillselection",
          "Preferences.label.performance.fillselection.tooltip",
          true);

  public static final Preference<Color> chatColor =
      store.defineColor("chatColor", Color.black, false);

  public static final Preference<Boolean> saveReminder =
      store.defineBoolean(
          "autoSaveReminder",
          "Preferences.label.save.reminder",
          "Preferences.label.save.reminder.tooltip",
          true);

  public static final Preference.Numeric<Integer> autoSaveIncrement =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("autoSaveIncrement", 5)
              .setLabel("Preferences.label.autosave")
              .setTooltip("Preferences.label.autosave.tooltip");

  public static final Preference.Numeric<Integer> chatAutoSaveTimeInMinutes =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("chatAutosaveTime", 0)
              .setLabel("Preferences.label.autosave.chat")
              .setTooltip("Preferences.label.autosave.chat.tooltip");

  public static final Preference<String> chatFilenameFormat =
      store
          .defineString("chatFilenameFormat", "chatlog-%1$tF-%1$tR.html")
          .setLabel("Preferences.label.autosave.chat.filename")
          .setTooltip("Preferences.label.autosave.chat.filename.tooltip");

  public static final Preference<String> tokenNumberDisplay =
      store.defineString("tokenNumberDisplay", "Name");

  public static final Preference<String> duplicateTokenNumber =
      store
          .defineString("duplicateTokenNumber", "Increment")
          .setLabel("Preferences.label.tokens.duplicate")
          .setTooltip("Preferences.label.tokens.duplicate.tooltip");

  public static final Preference<String> newTokenNaming =
      store
          .defineString("newTokenNaming", "Use Filename")
          .setLabel("Preferences.label.tokens.naming")
          .setTooltip("Preferences.label.tokens.naming.tooltip");

  public static final Preference<Boolean> useHaloColorOnVisionOverlay =
      store.defineBoolean(
          "useHaloColorForVisionOverlay",
          "Preferences.label.halo.color",
          "Preferences.label.halo.color.tooltip",
          false);

  public static final Preference<Boolean> mapVisibilityWarning =
      store.defineBoolean(
          "mapVisibilityWarning",
          "Preferences.label.fog.mapvisibilitywarning",
          "Preferences.label.fog.mapvisibilitywarning.tooltip",
          false);

  public static final Preference<Boolean> autoRevealVisionOnGMMovement =
      store.defineBoolean(
          "autoRevealVisionOnGMMove",
          "Preferences.label.fog.autoexpose",
          "Preferences.label.fog.autoexpose.tooltip",
          false);

  public static final Preference.Numeric<Integer> haloOverlayOpacity =
      (Preference.Numeric<Integer>)
          store
              .defineByte("haloOverlayOpacity", 60)
              .setLabel("Preferences.label.halo.opacity")
              .setTooltip("Preferences.label.halo.opacity.tooltip");

  public static final Preference.Numeric<Integer> auraOverlayOpacity =
      (Preference.Numeric<Integer>)
          store
              .defineByte("auraOverlayOpacity", 60)
              .setLabel("Preferences.label.aura.opacity")
              .setTooltip("Preferences.label.aura.opacity.tooltip");

  public static final Preference.Numeric<Integer> lightOverlayOpacity =
      (Preference.Numeric<Integer>)
          store
              .defineByte("lightOverlayOpacity", 60)
              .setLabel("Preferences.label.light.opacity")
              .setTooltip("Preferences.label.light.opacity.tooltip");

  public static final Preference.Numeric<Integer> lumensOverlayOpacity =
      (Preference.Numeric<Integer>)
          store
              .defineByte("lumensOverlayOpacity", 120)
              .setLabel("Preferences.label.lumens.opacity")
              .setTooltip("Preferences.label.lumens.opacity.tooltip");

  public static final Preference.Numeric<Integer> fogOverlayOpacity =
      (Preference.Numeric<Integer>)
          store
              .defineByte("fogOverlayOpacity", 100)
              .setLabel("Preferences.label.fog.opacity")
              .setTooltip("Preferences.label.fog.opacity.tooltip");

  public static final Preference.Numeric<Integer> lumensOverlayBorderThickness =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("lumensOverlayBorderThickness", 5)
              .setLabel("Preferences.label.lumens.borderThickness")
              .setTooltip("Preferences.label.lumens.borderThickness.tooltip");

  public static final Preference<Boolean> lumensOverlayShowByDefault =
      store.defineBoolean(
          "lumensOverlayShowByDefault",
          "Preferences.label.lumens.startEnabled",
          "Preferences.label.lumens.startEnabled.tooltip",
          false);

  public static final Preference<Boolean> lightsShowByDefault =
      store.defineBoolean(
          "lightsShowByDefault",
          "Preferences.label.lights.startEnabled",
          "Preferences.label.lights.startEnabled.tooltip",
          true);

  public static final Preference.Numeric<Integer> haloLineWidth =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("haloLineWidth", 2)
              .setLabel("Preferences.label.halo.width")
              .setTooltip("Preferences.label.halo.width.tooltip");

  public static final Preference.Numeric<Integer> typingNotificationDurationInSeconds =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("typingNotificationDuration", 5)
              .setLabel("Preferences.label.chat.type.duration")
              .setTooltip("Preferences.label.chat.type.duration.tooltip");

  public static final Preference<Boolean> chatNotificationBackground =
      store.defineBoolean(
          "chatNotificationShowBackground",
          "Preferences.label.chat.type.background",
          "Preferences.label.chat.type.background.tooltip",
          true);

  public static final Preference<Boolean> useToolTipForInlineRoll =
      store.defineBoolean(
          "toolTipInlineRolls",
          "Preferences.label.chat.rolls",
          "Preferences.label.chat.rolls.tooltip",
          false);

  public static final Preference<Boolean> suppressToolTipsForMacroLinks =
      store.defineBoolean(
          "suppressToolTipsMacroLinks",
          "Preferences.label.chat.macrolinks",
          "Preferences.label.chat.macrolinks.tooltip",
          false);

  public static final Preference<Color> chatNotificationColor =
      store
          .defineColor("chatNotificationColor", Color.white, false)
          .setLabel("Preferences.label.chat.type.color")
          .setTooltip("Preferences.label.chat.type.color.tooltip");

  public static final Preference<Color> trustedPrefixBackground =
      store
          .defineColor("trustedPrefixBG", new Color(0xD8, 0xE9, 0xF6), false)
          .setLabel("Preferences.label.chat.trusted.background")
          .setTooltip("Preferences.label.chat.trusted.background.tooltip");

  public static final Preference<Color> trustedPrefixForeground =
      store
          .defineColor("trustedPrefixFG", Color.BLACK, false)
          .setLabel("Preferences.label.chat.trusted.foreground")
          .setTooltip("Preferences.label.chat.trusted.foreground.tooltip");

  public static final Preference.Numeric<Integer> toolTipInitialDelay =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("toolTipInitialDelay", 250)
              .setLabel("Preferences.label.access.delay")
              .setTooltip("Preferences.label.access.delay.tooltip");

  public static final Preference.Numeric<Integer> toolTipDismissDelay =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("toolTipDismissDelay", 30000)
              .setLabel("Preferences.label.access.delay.dismiss")
              .setTooltip("Preferences.label.access.delay.dismiss.tooltip");

  public static final Preference<Boolean> openEditorForNewMacro =
      store.defineBoolean(
          "openEditorForNewMacro",
          "Preferences.label.macro.editor",
          "Preferences.label.macro.editor.tooltip",
          true);

  public static final Preference<Boolean> allowPlayerMacroEditsDefault =
      store.defineBoolean(
          "allowPlayerMacroEditsDefault",
          "Preferences.label.macros.edit",
          "Preferences.label.macros.edit.tooltip",
          true);

  public static final Preference.Numeric<Integer> portraitSize =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("portraitSize", 175)
              .setLabel("Preferences.label.tokens.statsheet")
              .setTooltip("Preferences.label.tokens.statsheet.tooltip");

  public static final Preference.Numeric<Integer> thumbnailSize =
      store.defineInteger("thumbnailSize", 500);

  public static final Preference<Boolean> showSmilies =
      store.defineBoolean(
          "insertSmilies",
          "Preferences.label.chat.smilies",
          "Preferences.label.chat.smilies.tooltip",
          true);

  public static final Preference<Boolean> showDialogOnNewToken =
      store.defineBoolean(
          "showDialogOnNewToken",
          "Preferences.label.tokens.dialog",
          "Preferences.label.tokens.dialog.tooltip",
          true);

  public static final Preference<Boolean> showAvatarInChat =
      store.defineBoolean(
          "showAvatarInChat",
          "Preferences.label.chat.avatar",
          "Preferences.label.chat.avatar.tooltip",
          true);

  public static final Preference<Boolean> playSystemSounds =
      store.defineBoolean(
          "playSystemSounds",
          "Preferences.label.sound.system",
          "Preferences.label.sound.system.tooltip",
          true);

  public static final Preference<Boolean> playSystemSoundsOnlyWhenNotFocused =
      store.defineBoolean(
          "playSystemSoundsOnlyWhenNotFocused",
          "Preferences.label.sound.focus",
          "Preferences.label.sound.focus.tooltip",
          false);

  public static final Preference<Boolean> playStreams =
      store.defineBoolean(
          "playStreams",
          "Preferences.label.sound.stream",
          "Preferences.label.sound.stream.tooltip",
          true);

  public static final Preference<Boolean> syrinscapeActive =
      store.defineBoolean(
          "syrinscapeActive",
          "Preferences.label.sound.syrinscape",
          "Preferences.label.sound.syrinscape.tooltip",
          false);

  public static final Preference.Numeric<Integer> fontSize =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("fontSize", 12)
              .setLabel("Preferences.label.access.size")
              .setTooltip("Preferences.label.access.size.tooltip");

  public static final Preference<Color> defaultGridColor =
      store.defineColor("defaultGridColor", Color.black, false);

  public static final Preference.Numeric<Integer> defaultGridSize =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("defaultGridSize", 100)
              .setLabel("Preferences.label.maps.gridSize.new")
              .setTooltip("Preferences.label.maps.gridSize.tooltip");

  public static final Preference.Numeric<Double> defaultUnitsPerCell =
      (Preference.Numeric<Double>)
          store
              .defineDouble("unitsPerCell", 5.)
              .setLabel("Preferences.label.maps.units")
              .setTooltip("Preferences.label.maps.units.tooltip");

  public static final Preference<Boolean> faceVertex =
      store.defineBoolean(
          "faceVertex",
          "Preferences.label.facing.vertices",
          "Preferences.label.facing.vertices.tooltip",
          false);

  public static final Preference<Boolean> faceEdge =
      store.defineBoolean(
          "faceEdge",
          "Preferences.label.facing.edge",
          "Preferences.label.facing.edge.tooltip",
          true);

  public static final Preference.Numeric<Integer> defaultVisionDistance =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("defaultVisionDistance", 1000)
              .setLabel("Preferences.label.maps.vision")
              .setTooltip("Preferences.label.maps.vision.tooltip");

  public static final Preference<Zone.VisionType> defaultVisionType =
      store
          .defineEnum(Zone.VisionType.class, "defaultVisionType", Zone.VisionType.OFF)
          .setLabel("Preferences.label.maps.light")
          .setTooltip("Preferences.label.maps.light.tooltip");

  public static final Preference<MapSortType> mapSortType =
      store
          .defineEnum(MapSortType.class, "sortByGMName", MapSortType.GMNAME)
          .setLabel("Preferences.label.maps.sortType")
          .setTooltip("Preferences.label.maps.sortType.tooltip");

  public static final Preference<UvttLosImportType> uvttLosImportType =
      store
          .defineEnum(UvttLosImportType.class, "uvttLosImportType", UvttLosImportType.Prompt)
          .setLabel("Preferences.label.maps.uvttLosImport")
          .setTooltip("Preferences.label.maps.uvttLosImport.tooltip");

  public static final Preference<Boolean> useSoftFogEdges = store.defineBoolean("useSoftFog", true);

  public static final Preference<Boolean> newMapsHaveFow =
      store.defineBoolean(
          "newMapsHaveFow",
          "Preferences.label.maps.fow",
          "Preferences.label.maps.fow.tooltip",
          false);

  public static final Preference<Boolean> newTokensVisible =
      store.defineBoolean(
          "newTokensVisible",
          "Preferences.label.tokens.visible",
          "Preferences.label.tokens.visible.tooltip",
          true);

  public static final Preference<Boolean> newMapsVisible =
      store.defineBoolean(
          "newMapsVisible",
          "Preferences.label.maps.visible",
          "Preferences.label.maps.visible.tooltip",
          true);

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
          .cacheIt()
          .setLabel("Preferences.label.performance.render")
          .setTooltip("Preferences.label.performance.render.tooltip");

  /** The background color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelBackground =
      store
          .defineColor("npcMapLabelBG", Color.LIGHT_GRAY, true)
          .setLabel("Preferences.label.access.tokenLabel.npcBackground");

  /** The foreground color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelForeground =
      store
          .defineColor("npcMapLabelFG", Color.BLACK, true)
          .setLabel("Preferences.label.access.tokenLabel.npcForeground");

  /** The border color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelBorder =
      store
          .defineColor("mapLabelBorderColor", npcMapLabelForeground.getDefault(), true)
          .setLabel("Preferences.label.access.tokenLabel.npcBorderColor");

  /** The background color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelBackground =
      store
          .defineColor("pcMapLabelBG", Color.WHITE, true)
          .setLabel("Preferences.label.access.tokenLabel.pcBackground");

  /** The foreground color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelForeground =
      store
          .defineColor("pcMapLabelFG", Color.BLUE, true)
          .setLabel("Preferences.label.access.tokenLabel.pcForeground");

  /** The border color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelBorder =
      store
          .defineColor("pcMapLabelBorderColor", pcMapLabelForeground.getDefault(), true)
          .setLabel("Preferences.label.access.tokenLabel.pcBorderColor");

  /** The background color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelBackground =
      store
          .defineColor("nonVisMapLabelBG", Color.BLACK, true)
          .setLabel("Preferences.label.access.tokenLabel.nonVisBackground");

  /** The foreground color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelForeground =
      store
          .defineColor("nonVisMapLabelFG", Color.WHITE, true)
          .setLabel("Preferences.label.access.tokenLabel.nonVisForeground");

  /** The border color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelBorder =
      store
          .defineColor(
              "nonVisMapLabelBorderColor", nonVisibleTokenMapLabelForeground.getDefault(), true)
          .setLabel("Preferences.label.access.tokenLabel.nonVisBorderColor");

  /** The font size to use for token map labels. */
  public static final Preference.Numeric<Integer> mapLabelFontSize =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("mapLabelFontSize", AppStyle.labelFont.getSize())
              .setLabel("Preferences.label.access.tokenLabel.size");

  /** The width of the border for token map labels, in pixels. */
  public static final Preference.Numeric<Integer> mapLabelBorderWidth =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("mapLabelBorderWidth", Label.DEFAULT_LABEL_BORDER_WIDTH)
              .setLabel("Preferences.label.access.tokenLabel.borderSize");

  /** The size of the border arc for token map labels. */
  public static final Preference.Numeric<Integer> mapLabelBorderArc =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("mapLabelBorderArc", Label.DEFAULT_LABEL_BORDER_ARC)
              .setLabel("Preferences.label.access.tokenLabel.borderArc");

  /** {@code true} if borders should be shown around map labels, {@code false} otherwise. */
  public static final Preference<Boolean> mapLabelShowBorder =
      store.defineBoolean("mapLabelShowBorder", true).setLabel("Label.showBorder");

  public static final Preference.Numeric<Integer> webEndpointPort =
      store.defineInteger("webEndPointPort", 654555);

  public static final Preference<Boolean> tokensWarnWhenDeleted =
      store
          .defineBoolean("tokensWarnWhenDeleted", true)
          .setLabel("Preferences.label.tokens.delete")
          .setTooltip("Preferences.label.tokens.delete.tooltip");

  public static final Preference<Boolean> drawingsWarnWhenDeleted =
      store.defineBoolean("drawWarnWhenDeleted", true);

  public static final Preference<Boolean> tokensSnapWhileDragging =
      store.defineBoolean(
          "tokensSnapWhileDragging",
          "Preferences.label.tokens.drag.snap",
          "Preferences.label.tokens.drag.snap.tooltip",
          true);

  public static final Preference<Boolean> hideMousePointerWhileDragging =
      store.defineBoolean(
          "hideMousePointerWhileDragging",
          "Preferences.label.tokens.drag.hide",
          "Preferences.label.tokens.drag.hide.tooltip",
          true);

  public static final Preference<Boolean> hideTokenStackIndicator =
      store.defineBoolean(
          "hideTokenStackIndicator",
          "Preferences.label.tokens.stack.hide",
          "Preferences.label.tokens.stack.hide.tooltip",
          false);

  public static final Preference<Boolean> tokensStartSnapToGrid =
      store.defineBoolean(
          "newTokensStartSnapToGrid",
          "Preferences.label.objects.snap",
          "Preferences.label.tokens.snap.tooltip",
          true);

  public static final Preference<Boolean> objectsStartSnapToGrid =
      store.defineBoolean(
          "newStampsStartSnapToGrid",
          "Preferences.label.objects.snap",
          "Preferences.label.objects.snap.tooltip",
          false);

  public static final Preference<Boolean> backgroundsStartSnapToGrid =
      store.defineBoolean(
          "newBackgroundsStartSnapToGrid",
          "Preferences.label.objects.snap",
          "Preferences.label.background.snap.tooltip",
          false);

  public static final Preference<Boolean> tokensStartFreesize =
      store.defineBoolean(
          "newTokensStartFreesize",
          "Preferences.label.objects.free",
          "Preferences.label.tokens.free.tooltip",
          false);

  public static final Preference<Boolean> objectsStartFreesize =
      store.defineBoolean(
          "newStampsStartFreesize",
          "Preferences.label.objects.free",
          "Preferences.label.objects.free.tooltip",
          true);

  public static final Preference<Boolean> backgroundsStartFreesize =
      store.defineBoolean(
          "newBackgroundsStartFreesize",
          "Preferences.label.objects.free",
          "Preferences.label.background.free.tooltip",
          true);

  public static final Preference<String> defaultGridType =
      store
          .defineString("defaultGridType", GridFactory.SQUARE)
          .setLabel("Preferences.label.maps.grid")
          .setTooltip("Preferences.label.maps.grid.tooltip");

  public static final Preference<Boolean> showStatSheet =
      store.defineBoolean(
          "showStatSheet",
          "Preferences.label.tokens.statsheet.mouse",
          "Preferences.label.tokens.statsheet.mouse.tooltip",
          true);

  public static final Preference<Boolean> showStatSheetRequiresModifierKey =
      store.defineBoolean(
          "showStatSheetModifier",
          "Preferences.label.tokens.statsheet.shift",
          "Preferences.label.tokens.statsheet.shift.tooltip",
          false);

  public static final Preference<Boolean> showPortrait =
      store.defineBoolean(
          "showPortrait",
          "Preferences.label.tokens.portrait.mouse",
          "Preferences.label.tokens.portrait.mouse.tooltip",
          true);

  public static final Preference<Boolean> forceFacingArrow =
      store.defineBoolean(
          "forceFacingArrow",
          "Preferences.label.tokens.arrow",
          "Preferences.label.tokens.arrow.tooltip",
          false);

  public static final Preference<Boolean> fitGmView =
      store.defineBoolean(
          "fitGMView",
          "Preferences.label.client.fitview",
          "Preferences.label.client.fitview.tooltip",
          true);

  public static final Preference<String> defaultUserName =
      store
          .defineString(
              "defaultUsername", I18N.getString("Preferences.client.default.username.value"))
          .setLabel("Preferences.label.client.default.username")
          .setTooltip("Preferences.label.client.default.username.tooltip");

  public static final Preference<WalkerMetric> movementMetric =
      store
          .defineEnum(WalkerMetric.class, "movementMetric", WalkerMetric.ONE_TWO_ONE)
          .setLabel("Preferences.label.maps.metric")
          .setTooltip("Preferences.label.maps.metric.tooltip");

  public static final Preference.Numeric<Integer> frameRateCap =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("frameRateCap", 60, 1, Integer.MAX_VALUE)
              .setLabel("Preferences.label.performance.cap")
              .setTooltip("Preferences.label.performance.cap.tooltip");

  /* Scroll status bar information messages that exceed the available size */
  public static final Preference<Boolean> scrollStatusMessages =
      store.defineBoolean(
          "statusBarScroll", "Preferences.label.access.status.allowScroll", null, true);
  /* Scroll status bar scrolling speed */
  public static final Preference.Numeric<Double> scrollStatusSpeed =
      (Preference.Numeric<Double>)
          store
              .defineDouble("statusBarSpeed", 0.85)
              .setLabel("Preferences.label.access.status.scrollSpeed");
  /* Scroll status bar scrolling start delay */
  public static final Preference.Numeric<Double> scrollStatusStartDelay =
      (Preference.Numeric<Double>)
          store
              .defineDouble("statusBarDelay", 2.4)
              .setLabel("Preferences.label.access.status.scrollStartDelay");
  /* Scroll status bar scrolling end pause */
  public static final Preference.Numeric<Double> scrollStatusEndPause =
      (Preference.Numeric<Double>)
          store
              .defineDouble("statusBarDelay", 1.8)
              .setLabel("Preferences.label.access.status.scrollEndPause");
  /* Status bar temporary notification duration */
  public static final Preference.Numeric<Double> scrollStatusTempDuration =
      (Preference.Numeric<Double>)
          store
              .defineDouble("scrollStatusTempDuration", 12d)
              .setLabel("Preferences.label.access.status.tempTime");

  public static final Preference.Numeric<Integer> upnpDiscoveryTimeout =
      (Preference.Numeric<Integer>)
          store
              .defineInteger("upnpDiscoveryTimeout", 5000)
              .setLabel("Preferences.label.upnp.timeout")
              .setTooltip("Preferences.label.upnp.timeout.tooltip");

  public static final Preference<String> fileSyncPath =
      store
          .defineString("fileSyncPath", "")
          .setLabel("Preferences.label.directory.sync")
          .setTooltip("Preferences.label.directory.sync.tooltip");

  public static final Preference<Boolean> skipAutoUpdate =
      store.defineBoolean("skipAutoUpdate", false);

  public static final Preference<String> skipAutoUpdateRelease =
      store.defineString("skipAutoUpdateRelease", "");

  public static final Preference<Boolean> allowExternalMacroAccess =
      store.defineBoolean(
          "allowExternalMacroAccess",
          "Preferences.label.macros.permissions",
          "Preferences.label.macros.permissions.tooltip",
          false);

  public static final Preference<Boolean> loadMruCampaignAtStart =
      store.defineBoolean(
          "loadMRUCampaignAtStart",
          "Preferences.label.loadMRU",
          "Preferences.label.loadMRU.tooltip",
          false);

  public static final Preference<Boolean> initiativePanelShowsTokenImage =
      store.defineBoolean("initShowTokens", true);

  public static final Preference<Boolean> initiativePanelShowsTokenState =
      store.defineBoolean("initShowTokenStates", true);

  public static final Preference<Boolean> initiativePanelShowsInitiative =
      store.defineBoolean("initShowInitiative", true);

  public static final Preference<Boolean> initiativePanelShowsInitiativeOnLine2 =
      store.defineBoolean("initShow2ndLine", false);

  public static final Preference<Boolean> initiativePanelHidesNpcs =
      store.defineBoolean(
          "initHideNpcs",
          "Preferences.label.initiative.hidenpc",
          "Preferences.label.initiative.hidenpc.tooltip",
          false);

  public static final Preference<Boolean> initiativePanelAllowsOwnerPermissions =
      store.defineBoolean(
          "initOwnerPermissions",
          "Preferences.label.initiative.owner",
          "Preferences.label.initiative.owner.tooltip",
          false);

  public static final Preference<Boolean> initiativeMovementLocked =
      store.defineBoolean(
          "initLockMovement",
          "Preferences.label.initiative.lock",
          "Preferences.label.initiative.lock.tooltip",
          false);

  public static final Preference<Boolean> showInitiativeGainedMessage =
      store.defineBoolean(
          "showInitGainMessage",
          "Preferences.label.initiative.msg",
          "Preferences.label.initiative.msg.tooltip",
          true);

  public static final Preference<Boolean> initiativePanelWarnWhenResettingRoundCounter =
      store.defineBoolean(
          "initWarnWhenResettingRoundCounter",
          "initPanel.warnWhenResettingRoundCounter",
          "initPanel.warnWhenResettingRoundCounter.description",
          true);

  public static final Preference<Boolean> pathfindingEnabled =
      store.defineBoolean("useAstarPathfinding", true);

  public static final Preference<Boolean> pathfindingBlockedByVbl =
      store.defineBoolean("vblBlocksMove", true);

  public static final Preference<String> defaultMacroEditorTheme =
      store.defineString("macroEditorTheme", "Default");

  public static final Preference<String> iconTheme =
      store
          .defineString("iconTheme", "Rod Takehara")
          .setLabel("Label.icontheme")
          .setTooltip("Preferences.label.icontheme.tooltip");

  public static final Preference<Boolean> useCustomThemeFontProperties =
      store.defineBoolean(
          "useCustomUIProperties", "Preferences.theme.override.checkbox", null, false);

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

  static {
    if (PRINT_KEYS_MISSING_I18N_ON_STARTUP) {
      store.getDefinedPreferences().stream()
          .filter(p -> p.getLabel().equals(p.getKey()))
          .forEach(p -> System.out.println(p.getKey()));
    }
  }
}
