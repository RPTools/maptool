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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import javax.annotation.Nullable;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Label;
import net.rptools.maptool.model.Zone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Manages and persists user preferences for the application. */
public class AppPreferences {
  private static final Logger log = LogManager.getLogger(AppPreferences.class);

  /**
   * This private static variable represents the user preferences node for the application. It is an
   * instance of the Preferences class, which provides a way to store and retrieve user preferences
   * using a hierarchical tree of preference nodes, starting from the user root node.
   *
   * <p>The preferences are stored under a specific node path derived from the application name
   * using the AppConstants.APP_NAME constant.
   *
   * <p>This variable is used to access and modify user preferences throughout the application.
   */
  private static final Preferences prefs =
      Preferences.userRoot().node(AppConstants.APP_NAME + "/prefs");

  public static final Preference<Boolean> fillSelectionBox =
      BooleanType.create("fillSelectionBox", true);

  public static final Preference<Color> chatColor =
      ColorType.create("chatColor", Color.black, false);

  public static final Preference<Boolean> saveReminder =
      BooleanType.create("autoSaveReminder", true);

  public static final Preference<Integer> autoSaveIncrement =
      IntegerType.create("autoSaveIncrement", 5);

  public static final Preference<Integer> chatAutoSaveTimeInMinutes =
      IntegerType.create("chatAutosaveTime", 0);

  public static final Preference<String> chatFilenameFormat =
      StringType.create("chatFilenameFormat", "chatlog-%1$tF-%1$tR.html");

  public static final Preference<String> tokenNumberDisplay =
      StringType.create("tokenNumberDisplayg", "Name");

  public static final Preference<String> duplicateTokenNumber =
      StringType.create("duplicateTokenNumber", "Increment");

  public static final Preference<String> newTokenNaming =
      StringType.create("newTokenNaming", "Use Filename");

  public static final Preference<Boolean> useHaloColorOnVisionOverlay =
      BooleanType.create("useHaloColorForVisionOverlay", false);

  public static final Preference<Boolean> mapVisibilityWarning =
      BooleanType.create("mapVisibilityWarning", false);

  public static final Preference<Boolean> autoRevealVisionOnGMMovement =
      BooleanType.create("autoRevealVisionOnGMMove", false);

  public static final Preference<Integer> haloOverlayOpacity =
      ByteType.create("haloOverlayOpacity", 60);

  public static final Preference<Integer> auraOverlayOpacity =
      ByteType.create("auraOverlayOpacity", 60);

  public static final Preference<Integer> lightOverlayOpacity =
      ByteType.create("lightOverlayOpacity", 60);

  public static final Preference<Integer> lumensOverlayOpacity =
      ByteType.create("lumensOverlayOpacity", 120);

  public static final Preference<Integer> fogOverlayOpacity =
      ByteType.create("fogOverlayOpacity", 100);

  public static final Preference<Integer> lumensOverlayBorderThickness =
      IntegerType.create("lumensOverlayBorderThickness", 5);

  public static final Preference<Boolean> lumensOverlayShowByDefault =
      BooleanType.create("lumensOverlayShowByDefault", false);

  public static final Preference<Boolean> lightsShowByDefault =
      BooleanType.create("lightsShowByDefault", true);

  public static final Preference<Integer> haloLineWidth = IntegerType.create("haloLineWidth", 2);

  public static final Preference<Integer> typingNotificationDurationInSeconds =
      IntegerType.create("typingNotificationDuration", 5);

  public static final Preference<Boolean> chatNotificationBackground =
      BooleanType.create("chatNotificationShowBackground", true);

  public static final Preference<Boolean> useToolTipForInlineRoll =
      BooleanType.create("toolTipInlineRolls", false);

  public static final Preference<Boolean> suppressToolTipsForMacroLinks =
      BooleanType.create("suppressToolTipsMacroLinks", false);

  public static final Preference<Color> chatNotificationColor =
      ColorType.create("chatNotificationColor", Color.white, false);

  public static final Preference<Color> trustedPrefixBackground =
      ColorType.create("trustedPrefixBG", new Color(0xD8, 0xE9, 0xF6), false);

  public static final Preference<Color> trustedPrefixForeground =
      ColorType.create("trustedPrefixFG", Color.BLACK, false);

  public static final Preference<Integer> toolTipInitialDelay =
      IntegerType.create("toolTipInitialDelay", 250);

  public static final Preference<Integer> toolTipDismissDelay =
      IntegerType.create("toolTipDismissDelay", 30000);

  public static final Preference<Boolean> allowPlayerMacroEditsDefault =
      BooleanType.create("allowPlayerMacroEditsDefault", true);

  public static final Preference<Integer> portraitSize = IntegerType.create("portraitSize", 175);

  public static final Preference<Integer> thumbnailSize = IntegerType.create("thumbnailSize", 500);

  public static final Preference<Boolean> showSmilies = BooleanType.create("insertSmilies", true);

  public static final Preference<Boolean> showDialogOnNewToken =
      BooleanType.create("showDialogOnNewToken", true);

  public static final Preference<Boolean> showAvatarInChat =
      BooleanType.create("showAvatarInChat", true);

  public static final Preference<Boolean> playSystemSounds =
      BooleanType.create("playSystemSounds", true);

  public static final Preference<Boolean> playSystemSoundsOnlyWhenNotFocused =
      BooleanType.create("playSystemSoundsOnlyWhenNotFocused", false);

  public static final Preference<Boolean> playStreams = BooleanType.create("playStreams", true);

  public static final Preference<Boolean> syrinscapeActive =
      BooleanType.create("syrinscapeActive", false);

  public static final Preference<Integer> fontSize = IntegerType.create("fontSize", 12);

  public static final Preference<Color> defaultGridColor =
      ColorType.create("defaultGridColor", Color.black, false);

  public static final Preference<Integer> defaultGridSize =
      IntegerType.create("defaultGridSize", 100);

  public static final Preference<Double> defaultUnitsPerCell =
      DoubleType.create("unitsPerCell", 5.);

  public static final Preference<Boolean> faceVertex = BooleanType.create("faceVertex", false);

  public static final Preference<Boolean> faceEdge = BooleanType.create("faceEdge", true);

  public static final Preference<Integer> defaultVisionDistance =
      IntegerType.create("defaultVisionDistance", 1000);

  public static final Preference<Zone.VisionType> defaultVisionType =
      EnumType.create(Zone.VisionType.class, "defaultVisionType", Zone.VisionType.OFF);

  public static final Preference<MapSortType> mapSortType =
      EnumType.create(MapSortType.class, "sortByGMName", MapSortType.GMNAME);

  public static final Preference<Boolean> useSoftFogEdges = BooleanType.create("useSoftFog", true);

  public static final Preference<Boolean> newMapsHaveFow =
      BooleanType.create("newMapsHaveFow", false);

  public static final Preference<Boolean> newTokensVisible =
      BooleanType.create("newTokensVisible", true);

  public static final Preference<Boolean> newMapsVisible =
      BooleanType.create("newMapsVisible", true);

  public static final Preference<Boolean> newObjectsVisible =
      BooleanType.create("newObjectsVisible", true);

  public static final Preference<Boolean> newBackgroundsVisible =
      BooleanType.create("newBackgroundsVisible", true);

  public static final Preference<File> saveDirectory =
      FileType.create("saveDir", () -> new File(File.separator));

  public static final Preference<File> tokenSaveDirectory =
      FileType.create("saveTokenDir", saveDirectory::get);

  public static final Preference<File> mapSaveDirectory =
      FileType.create("saveMapDir", saveDirectory::get);

  public static final Preference<File> addOnLoadDirectory =
      FileType.create("addOnLoadDir", saveDirectory::get);

  public static final Preference<File> loadDirectory =
      FileType.create("loadDir", () -> new File(File.separator));

  public static final Preference<RenderQuality> renderQuality =
      EnumType.create(RenderQuality.class, "renderScaleQuality", RenderQuality.LOW_SCALING)
          .cacheIt();

  /** The background color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelBackground =
      ColorType.create("npcMapLabelBG", Color.LIGHT_GRAY, true);

  /** The foreground color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelForeground =
      ColorType.create("npcMapLabelFG", Color.BLACK, true);

  /** The border color to use for NPC map labels. */
  public static final Preference<Color> npcMapLabelBorder =
      ColorType.create("mapLabelBorderColor", npcMapLabelForeground.getDefault(), true);

  /** The background color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelBackground =
      ColorType.create("pcMapLabelBG", Color.WHITE, true);

  /** The foreground color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelForeground =
      ColorType.create("pcMapLabelFG", Color.BLUE, true);

  /** The border color to use for PC map labels. */
  public static final Preference<Color> pcMapLabelBorder =
      ColorType.create("pcMapLabelBorderColor", pcMapLabelForeground.getDefault(), true);

  /** The background color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelBackground =
      ColorType.create("nonVisMapLabelBG", Color.BLACK, true);

  /** The foreground color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelForeground =
      ColorType.create("nonVisMapLabelFG", Color.WHITE, true);

  /** The border color to use for Non-Visible Token map labels. */
  public static final Preference<Color> nonVisibleTokenMapLabelBorder =
      ColorType.create(
          "nonVisMapLabelBorderColor", nonVisibleTokenMapLabelForeground.getDefault(), true);

  /** The font size to use for token map labels. */
  public static final Preference<Integer> mapLabelFontSize =
      IntegerType.create("mapLabelFontSize", AppStyle.labelFont.getSize());

  /** The width of the border for token map labels, in pixels. */
  public static final Preference<Integer> mapLabelBorderWidth =
      IntegerType.create("mapLabelBorderWidth", Label.DEFAULT_LABEL_BORDER_WIDTH);

  /** The size of the border arc for token map labels. */
  public static final Preference<Integer> mapLabelBorderArc =
      IntegerType.create("mapLabelBorderArc", Label.DEFAULT_LABEL_BORDER_ARC);

  /** {@code true} if borders should be shown around map labels, {@code false} otherwise. */
  public static final Preference<Boolean> mapLabelShowBorder =
      BooleanType.create("mapLabelShowBorder", true);

  // TODO Why is the default not a valid port?
  public static final Preference<Integer> webEndpointPort =
      IntegerType.create("webEndPointPort", 654555);

  public static final Preference<Boolean> tokensWarnWhenDeleted =
      BooleanType.create("tokensWarnWhenDeleted", true);

  public static final Preference<Boolean> drawingsWarnWhenDeleted =
      BooleanType.create("drawWarnWhenDeleted", true);

  public static final Preference<Boolean> tokensSnapWhileDragging =
      BooleanType.create("tokensSnapWhileDragging", true);

  public static final Preference<Boolean> hideMousePointerWhileDragging =
      BooleanType.create("hideMousePointerWhileDragging", true);

  public static final Preference<Boolean> hideTokenStackIndicator =
      BooleanType.create("hideTokenStackIndicator", false);

  public static final Preference<Boolean> tokensStartSnapToGrid =
      BooleanType.create("newTokensStartSnapToGrid", true);

  public static final Preference<Boolean> objectsStartSnapToGrid =
      BooleanType.create("newStampsStartSnapToGrid", false);

  public static final Preference<Boolean> backgroundsStartSnapToGrid =
      BooleanType.create("newBackgroundsStartSnapToGrid", false);

  public static final Preference<Boolean> tokensStartFreesize =
      BooleanType.create("newTokensStartFreesize", false);

  public static final Preference<Boolean> objectsStartFreesize =
      BooleanType.create("newStampsStartFreesize", true);

  public static final Preference<Boolean> backgroundsStartFreesize =
      BooleanType.create("newBackgroundsStartFreesize", true);

  public static final Preference<String> defaultGridType =
      StringType.create("defaultGridType", GridFactory.SQUARE);

  public static final Preference<Boolean> showStatSheet = BooleanType.create("showStatSheet", true);

  public static final Preference<Boolean> showStatSheetRequiresModifierKey =
      BooleanType.create("showStatSheetModifier", false);

  public static final Preference<Boolean> showPortrait = BooleanType.create("showPortrait", true);

  public static final Preference<Boolean> forceFacingArrow =
      BooleanType.create("forceFacingArrow", false);

  public static final Preference<Boolean> fitGmView = BooleanType.create("fitGMView", true);

  public static final Preference<String> defaultUserName =
      StringType.create(
          "defaultUsername", I18N.getString("Preferences.client.default.username.value"));

  public static final Preference<WalkerMetric> movementMetric =
      EnumType.create(WalkerMetric.class, "movementMetric", WalkerMetric.ONE_TWO_ONE);

  public static final Preference<Integer> frameRateCap =
      IntegerType.create("frameRateCap", 60).validateIt(cap -> cap > 0);

  public static final Preference<Integer> upnpDiscoveryTimeout =
      IntegerType.create("upnpDiscoveryTimeout", 5000);

  public static final Preference<String> fileSyncPath = StringType.create("fileSyncPath", "");

  public static final Preference<Boolean> skipAutoUpdate =
      BooleanType.create("skipAutoUpdate", false);

  public static final Preference<String> skipAutoUpdateRelease =
      StringType.create("skipAutoUpdateRelease", "");

  public static final Preference<Boolean> allowExternalMacroAccess =
      BooleanType.create("allowExternalMacroAccess", false);

  public static final Preference<Boolean> loadMruCampaignAtStart =
      BooleanType.create("loadMRUCampaignAtStart", false);

  public static final Preference<Boolean> initiativePanelShowsTokenImage =
      BooleanType.create("initShowTokens", true);

  public static final Preference<Boolean> initiativePanelShowsTokenState =
      BooleanType.create("initShowTokenStates", true);

  public static final Preference<Boolean> initiativePanelShowsInitiative =
      BooleanType.create("initShowInitiative", true);

  public static final Preference<Boolean> initiativePanelShowsInitiativeOnLine2 =
      BooleanType.create("initShow2ndLine", false);

  public static final Preference<Boolean> initiativePanelHidesNpcs =
      BooleanType.create("initHideNpcs", false);

  public static final Preference<Boolean> initiativePanelAllowsOwnerPermissions =
      BooleanType.create("initOwnerPermissions", false);

  public static final Preference<Boolean> initiativeMovementLocked =
      BooleanType.create("initLockMovement", false);

  public static final Preference<Boolean> showInitiativeGainedMessage =
      BooleanType.create("showInitGainMessage", true);

  public static final Preference<Boolean> pathfindingEnabled =
      BooleanType.create("useAstarPathfinding", true);

  public static final Preference<Boolean> pathfindingBlockedByVbl =
      BooleanType.create("vblBlocksMove", true);

  public static final Preference<String> defaultMacroEditorTheme =
      StringType.create("macroEditorTheme", "Default");

  public static final Preference<String> iconTheme = StringType.create("iconTheme", "Rod Takehara");

  static {
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

  private interface Type<T> {
    void set(Preferences node, String key, T value);

    T get(Preferences node, String key, Supplier<T> defaultValue);
  }

  public static final class Preference<T> {
    private final String key;
    private final Supplier<T> defaultValue;
    private final Type<T> type;

    private Predicate<T> validator = value -> true;
    private boolean cachingEnabled = false;
    private @Nullable T cachedValue;

    private final List<Consumer<T>> onChangeHandlers = new CopyOnWriteArrayList<>();

    private Preference(String key, T defaultValue, Type<T> type) {
      this.key = key;
      this.defaultValue = () -> defaultValue;
      this.type = type;
    }

    private Preference(String key, Supplier<T> defaultValue, Type<T> type) {
      this.key = key;
      this.defaultValue = defaultValue;
      this.type = type;
    }

    public String name() {
      return key;
    }

    /**
     * Loads and validates the value of the preference.
     *
     * <p>If validation is unsuccessful, clears the preference and returns it the default value.
     *
     * @return The value of the preference.
     */
    public T get() {
      if (cachingEnabled && cachedValue != null) {
        return cachedValue;
      }

      var value = type.get(prefs, key, defaultValue);
      if (!validator.test(value)) {
        log.warn("Value read from preference {} did not pass validation: {}", name(), value);
        value = getDefault();
        remove();
      }

      cachedValue = value;
      return value;
    }

    /**
     * Validates and stores the value of the preference.
     *
     * <p>If validation is unsuccessful, stores the default value instead.
     *
     * @param value The value to set.
     */
    public void set(T value) {
      if (!validator.test(value)) {
        log.warn("Value written to preference {} did not pass validation: {}", name(), value);
        value = getDefault();
      }

      type.set(prefs, key, value);
      cachedValue = value;

      for (var handler : onChangeHandlers) {
        handler.accept(value);
      }
    }

    public void remove() {
      prefs.remove(key);
      cachedValue = getDefault();

      for (var handler : onChangeHandlers) {
        handler.accept(cachedValue);
      }
    }

    public T getDefault() {
      return defaultValue.get();
    }

    public Preference<T> cacheIt() {
      this.cachingEnabled = true;
      return this;
    }

    public Preference<T> validateIt(Predicate<T> predicate) {
      validator = predicate;
      return this;
    }

    public void onChange(Consumer<T> handler) {
      onChangeHandlers.add(handler);
    }
  }

  private static final class BooleanType implements Type<Boolean> {
    public static Preference<Boolean> create(String key, boolean defaultValue) {
      return new Preference<>(key, defaultValue, new BooleanType());
    }

    @Override
    public void set(Preferences prefs, String key, Boolean value) {
      prefs.putBoolean(key, value);
    }

    @Override
    public Boolean get(Preferences prefs, String key, Supplier<Boolean> defaultValue) {
      return prefs.getBoolean(key, defaultValue.get());
    }
  }

  private static final class IntegerType implements Type<Integer> {
    public static Preference<Integer> create(String key, int defaultValue) {
      return new Preference<>(key, defaultValue, new IntegerType());
    }

    @Override
    public void set(Preferences prefs, String key, Integer value) {
      prefs.putInt(key, value);
    }

    @Override
    public Integer get(Preferences prefs, String key, Supplier<Integer> defaultValue) {
      return prefs.getInt(key, defaultValue.get());
    }
  }

  private static final class ByteType implements Type<Integer> {
    public static Preference<Integer> create(String key, int defaultValue) {
      return new Preference<>(key, defaultValue, new ByteType());
    }

    @Override
    public void set(Preferences prefs, String key, Integer value) {
      prefs.putInt(key, range0to255(value));
    }

    @Override
    public Integer get(Preferences prefs, String key, Supplier<Integer> defaultValue) {
      return range0to255(prefs.getInt(key, defaultValue.get()));
    }

    private static int range0to255(int value) {
      return Math.clamp(value, 0, 255);
    }
  }

  private static final class DoubleType implements Type<Double> {
    public static Preference<Double> create(String key, double defaultValue) {
      return new Preference<>(key, defaultValue, new DoubleType());
    }

    @Override
    public void set(Preferences prefs, String key, Double value) {
      prefs.putDouble(key, value);
    }

    @Override
    public Double get(Preferences prefs, String key, Supplier<Double> defaultValue) {
      return prefs.getDouble(key, defaultValue.get());
    }
  }

  private static final class StringType implements Type<String> {
    public static Preference<String> create(String key, String defaultValue) {
      return new Preference<>(key, defaultValue, new StringType());
    }

    @Override
    public void set(Preferences prefs, String key, String value) {
      prefs.put(key, value);
    }

    @Override
    public String get(Preferences prefs, String key, Supplier<String> defaultValue) {
      return prefs.get(key, defaultValue.get());
    }
  }

  private static final class FileType implements Type<File> {
    public static Preference<File> create(String key, Supplier<File> defaultValue) {
      return new Preference<>(key, defaultValue, new FileType());
    }

    @Override
    public void set(Preferences prefs, String key, File value) {
      prefs.put(key, value.toString());
    }

    @Override
    public File get(Preferences prefs, String key, Supplier<File> defaultValue) {
      String filePath = prefs.get(key, null);
      if (filePath != null) {
        return new File(filePath);
      }

      return defaultValue.get();
    }
  }

  private static final class EnumType<T extends Enum<T>> implements Type<T> {
    public static <T extends Enum<T>> Preference<T> create(
        Class<T> class_, String key, T defaultValue) {
      return new Preference<>(key, defaultValue, new EnumType<>(class_));
    }

    private final Class<T> class_;

    public EnumType(Class<T> class_) {
      this.class_ = class_;
    }

    @Override
    public void set(Preferences prefs, String key, T value) {
      prefs.put(key, value.name());
    }

    @Override
    public T get(Preferences prefs, String key, Supplier<T> defaultValue) {
      var stored = prefs.get(key, null);
      if (stored == null) {
        return defaultValue.get();
      }

      try {
        return Enum.valueOf(class_, stored);
      } catch (Exception e) {
        return defaultValue.get();
      }
    }
  }

  private static final class ColorType implements Type<Color> {
    public static Preference<Color> create(String key, Color defaultValue, boolean hasAlpha) {
      return new Preference<>(key, defaultValue, new ColorType(hasAlpha));
    }

    private final boolean hasAlpha;

    public ColorType(boolean hasAlpha) {
      this.hasAlpha = hasAlpha;
    }

    @Override
    public void set(Preferences prefs, String key, Color value) {
      prefs.putInt(key, value.getRGB());
    }

    @Override
    public Color get(Preferences prefs, String key, Supplier<Color> defaultValue) {
      return new Color(prefs.getInt(key, defaultValue.get().getRGB()), hasAlpha);
    }
  }
}
