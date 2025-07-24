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
package net.rptools.maptool.client.ui.preferencesdialog;

import static net.rptools.maptool.client.ui.preferencesdialog.Classify.Group.*;
import static net.rptools.maptool.client.ui.preferencesdialog.Classify.Section.*;

import java.util.*;
import java.util.List;
import java.util.function.Predicate;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.searchable.SearchWords;
import net.rptools.maptool.language.I18N;

import net.rptools.maptool.model.localisedObject.LocalObject;
import net.rptools.maptool.util.preferences.Preference;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class Classify {
  /** Broad groupings, along the lines of tab names, used for panel titles, table entries, etc */
  enum Section implements LocalObject {
    // @formatter:off
    // spotless:off
    Audio     ("Label.sounds", false),
    Developer ("Preferences.tab.developer", true),
    JVMConfig ("Label.startup", true),
    Config    ("EditTokenDialog.tab.config", false),
    Campaign  ("panel.Campaign", false),
    Chat      ("panel.Chat", false),
    Initiative("panel.Initiative", false),
    Map       ("Label.maps", false),
    Theme     ("Label.themes", true),
    Token     ("Label.token", false),
    ;
    // spotless:on
    // @formatter:on
    /** has a separate dialog or panel to open */
    final boolean separate;

    /** the i18n lookup key */
    final String i18nKey;

    /** the resolved i18n string */
    final String displayName;

    Section(String i18nKey, boolean separate) {
      this.separate = separate;
      this.i18nKey = i18nKey;
      this.displayName = I18N.getText(i18nKey);
    }

    /**
     * @return the value used by the preference.
     */
    @Override
    public @NotNull Section getValue() {
      return this;
    }

    /**
     * @return the key to look up the localised display value
     */
    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }


    @Override
    public @NotNull String toString() {
      return this.displayName;
    }
  }

  /**
   * Narrow groupings, to separate settings within sections, used for panel titles, table entries,
   * etc
   */
  enum Group implements LocalObject {
    // @formatter:off
    // spotless:off,
    NONE          (""),
    Authentication("Label.auth"),
    Facing        ("Label.facing"),
    Fog           ("Label.fogOfWar"),
    Grid          ("Label.grid"),
    Halo          ("token.popup.menu.halo"),
    Label         ("Preferences.label.access.tokenLabel"),
    Light         ("CampaignPropertiesDialog.tab.light"),
    Macro         ("Label.macros"),
    New_Token     ("dialog.NewToken.title"),
    Notification  ("Label.notifications"),
    Performance   ("Label.performance"),
    Save          ("Button.save"),
    Size          ("EditTokenDialog.label.size"),
    Snap          ("Label.snapToGrid"),
    StatSheet     ("EditTokenDialog.label.statSheet"),
    StatusBar     ("Preferences.label.access.status.options"),
    Tooltip       ("component.label.macro.toolTip"),
    Trusted       ("Label.trusted"),
    Visible       ("Label.visibility"),
    Vision        ("Label.lights"),
    ;
    // spotless:on
    // @formatter:on
    /** the i18n lookup key */
    final String i18nKey;

    /** the resolved i18n string */
    final String displayName;

    Group(String i18nKey) {
      this.i18nKey = i18nKey;
      this.displayName = this.i18nKey.isBlank() ? "" : I18N.getText(i18nKey);
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }

    @Override
    public String getI18nKey() {
      return i18nKey;
    }

    @Override
    public Group getValue() {
      return this;
    }

    @Override
    public String toString() {
      return this.displayName;
    }
  }

  public enum Collated implements SearchWords {
    // @formatter:off
    // spotless:off
    _1_001(null, Audio     , null  , AppPreferences.playStreams                                 , false, true),
    _1_002(null, Audio     , null  , AppPreferences.playSystemSounds                            , false, true),
    _1_003(null, Audio     , null  , AppPreferences.playSystemSoundsOnlyWhenNotFocused          , false, true),
    _1_004(null, Audio     , null  , AppPreferences.syrinscapeActive                            , false, true),
    _1_005(null, Chat      , null  , AppPreferences.fontSize                                    , false, true),
    _1_006(null, Chat      , null  , AppPreferences.showAvatarInChat                            , false, true),
    _1_007(null, Chat      , null  , AppPreferences.showSmilies                                 , false, true),
    _1_008(null, Chat      , Save        , AppPreferences.chatAutoSaveTimeInMinutes                   , false, true),
    _1_009(null, Chat      , Save        , AppPreferences.chatFilenameFormat                          , false, true),
    _1_010(null, Chat      , Tooltip     , AppPreferences.suppressToolTipsForMacroLinks               , false, true),
    _1_011(null, Chat      , Tooltip     , AppPreferences.useToolTipForInlineRoll                     , false, true),
    _1_012(null, Chat      , Trusted     , AppPreferences.trustedPrefixBackground                     , false, true),
    _1_013(null, Chat      , Trusted     , AppPreferences.trustedPrefixForeground                     , false, true),
    _1_014(null, Chat      , Notification, AppPreferences.chatNotificationBackground                  , false, true),
    _1_015(null, Chat      , Notification, AppPreferences.chatNotificationColor                       , false, true),
    _1_016(null, Chat      , Notification, AppPreferences.typingNotificationDurationInSeconds         , false, true),
    _1_020(null, Config    , null  , AppPreferences.defaultUserName                             , false, true),
    _1_039(null, Config    , null  , AppPreferences.upnpDiscoveryTimeout                        , false, true),
    _1_017(null, Config    , Macro       , AppPreferences.allowExternalMacroAccess                    , false, true),
    _1_018(null, Config    , Macro       , AppPreferences.allowPlayerMacroEditsDefault                , false, true),
    _1_019(null, Config    , Macro       , AppPreferences.openEditorForNewMacro                       , false, true),
    _1_021(null, Config    , Performance , AppPreferences.fillSelectionBox                            , false, true),
    _1_022(null, Config    , Performance , AppPreferences.frameRateCap                                , false, true),
    _1_023(null, Config    , Performance , AppPreferences.renderQuality                               , false, true),
    _1_024(null, Config    , Save        , AppPreferences.autoSaveIncrement                           , true , true),
    _1_025(null, Config    , Save        , AppPreferences.fileSyncPath                                , false, true),
    _1_026(null, Config    , Save        , AppPreferences.loadMruCampaignAtStart                      , false, true),
    _1_027(null, Config    , Save        , AppPreferences.saveReminder                                , true , true),
    _1_028(null, Config    , StatSheet   , AppPreferences.portraitSize                                , false, true),
    _1_029(null, Config    , StatSheet   , AppPreferences.showPortrait                                , false, true),
    _1_030(null, Config    , StatSheet   , AppPreferences.showStatSheet                               , false, true),
    _1_031(null, Config    , StatSheet   , AppPreferences.showStatSheetRequiresModifierKey            , false, true),
    _1_032(null, Config    , StatusBar   , AppPreferences.scrollStatusEndPause                        , false, true),
    _1_033(null, Config    , StatusBar   , AppPreferences.scrollStatusMessages                        , false, true),
    _1_034(null, Config    , StatusBar   , AppPreferences.scrollStatusSpeed                           , false, true),
    _1_035(null, Config    , StatusBar   , AppPreferences.scrollStatusStartDelay                      , false, true),
    _1_036(null, Config    , StatusBar   , AppPreferences.scrollStatusTempDuration                    , false, true),
    _1_037(null, Config    , Tooltip     , AppPreferences.toolTipDismissDelay                         , false, true),
    _1_038(null, Config    , Tooltip     , AppPreferences.toolTipInitialDelay                         , false, true),
    _1_040(null, Initiative, null  , AppPreferences.initiativeMovementLocked                    , false, true),
    _1_041(null, Initiative, null  , AppPreferences.initiativePanelAllowsOwnerPermissions       , false, true),
    _1_042(null, Initiative, null  , AppPreferences.initiativePanelHidesNpcs                    , false, true),
    _1_043(null, Initiative, null  , AppPreferences.initiativePanelShowsInitiativeOnLine2       , false, true),
    _1_044(null, Initiative, null  , AppPreferences.initiativePanelShowsTokenImage              , false, true),
    _1_045(null, Initiative, null  , AppPreferences.initiativePanelShowsTokenState              , false, true),
    _1_046(null, Initiative, null  , AppPreferences.initiativePanelWarnWhenResettingRoundCounter, true , true),
    _1_047(null, Initiative, null  , AppPreferences.showInitiativeGainedMessage                 , false, true),
    _1_061(null, Map       , null  , AppPreferences.mapSortType                                 , false, true),
    _1_062(null, Map       , null  , AppPreferences.mapVisibilityWarning                        , true , true),
    _1_063(null, Map       , null  , AppPreferences.newMapsVisible                              , false, true),
    _1_064(null, Map       , null  , AppPreferences.auraOverlayOpacity                          , false, true),
    _1_065(null, Map       , null  , AppPreferences.fitGmView                                   , false, true),
    _1_066(null, Map       , null  , AppPreferences.drawingsWarnWhenDeleted                     , true , true),
    _1_067(null, Map       , null  , AppPreferences.uvttLosImportType                           , false, true),
    _1_048(null, Map       , Fog         , AppPreferences.autoRevealVisionOnGMMovement                , false, true),
    _1_049(null, Map       , Fog         , AppPreferences.fogOverlayOpacity                           , false, true),
    _1_050(null, Map       , Fog         , AppPreferences.newMapsHaveFow                              , false, true),
    _1_051(null, Map       , Grid        , AppPreferences.defaultGridColor                            , false, true),
    _1_052(null, Map       , Grid        , AppPreferences.defaultGridSize                             , false, true),
    _1_053(null, Map       , Grid        , AppPreferences.defaultGridType                             , false, true),
    _1_054(null, Map       , Grid        , AppPreferences.defaultUnitsPerCell                         , false, true),
    _1_055(null, Map       , Grid        , AppPreferences.movementMetric                              , false, true),
    _1_056(null, Map       , Halo        , AppPreferences.haloLineWidth                               , false, true),
    _1_057(null, Map       , Halo        , AppPreferences.haloOverlayOpacity                          , false, true),
    _1_058(null, Map       , Light       , AppPreferences.lightOverlayOpacity                         , false, true),
    _1_059(null, Map       , Light       , AppPreferences.lumensOverlayOpacity                        , false, true),
    _1_060(null, Map       , Light       , AppPreferences.useHaloColorOnVisionOverlay                 , false, true),
    _1_068(null, Map       , Vision      , AppPreferences.defaultVisionDistance                       , false, true),
    _1_069(null, Map       , Vision      , AppPreferences.defaultVisionType                           , false, true),
    _1_092(null, Token     , null  , AppPreferences.hideTokenStackIndicator                     , false, true),
    _1_093(null, Token     , null  , AppPreferences.tokensSnapWhileDragging                     , false, true),
    _1_094(null, Token     , null  , AppPreferences.hideMousePointerWhileDragging               , false, true),
    _1_095(null, Token     , null  , AppPreferences.tokensWarnWhenDeleted                       , true , true),
    _1_072(null, Token     , Facing      , AppPreferences.forceFacingArrow                            , false, true),
    _1_073(null, Token     , Facing      , AppPreferences.faceEdge                                    , false, true),
    _1_074(null, Token     , Facing      , AppPreferences.faceVertex                                  , false, true),
    _1_075(null, Token     , Label       , AppPreferences.mapLabelFontSize                            , false, true),
    _1_076(null, Token     , Label       , AppPreferences.mapLabelShowBorder                          , false, true),
    _1_077(null, Token     , Label       , AppPreferences.mapLabelBorderArc                           , false, true),
    _1_078(null, Token     , Label       , AppPreferences.mapLabelBorderWidth                         , false, true),
    _1_079(null, Token     , Label       , AppPreferences.pcMapLabelBackground                        , false, true),
    _1_080(null, Token     , Label       , AppPreferences.pcMapLabelBorder                            , false, true),
    _1_081(null, Token     , Label       , AppPreferences.pcMapLabelForeground                        , false, true),
    _1_082(null, Token     , Label       , AppPreferences.npcMapLabelBackground                       , false, true),
    _1_083(null, Token     , Label       , AppPreferences.npcMapLabelBorder                           , false, true),
    _1_084(null, Token     , Label       , AppPreferences.npcMapLabelForeground                       , false, true),
    _1_085(null, Token     , Label       , AppPreferences.nonVisibleTokenMapLabelBackground           , false, true),
    _1_086(null, Token     , Label       , AppPreferences.nonVisibleTokenMapLabelBorder               , false, true),
    _1_087(null, Token     , Label       , AppPreferences.nonVisibleTokenMapLabelForeground           , false, true),
    _1_088(null, Token     , New_Token   , AppPreferences.showDialogOnNewToken                        , true , true),
    _1_089(null, Token     , New_Token   , AppPreferences.newTokenNaming                              , false, true),
    _1_090(null, Token     , New_Token   , AppPreferences.tokenNumberDisplay                          , false, true),
    _1_091(null, Token     , New_Token   , AppPreferences.duplicateTokenNumber                        , false, true),
    _1_096(null, Token     , Size        , AppPreferences.tokensStartFreesize                         , false, true),
    _1_097(null, Token     , Size        , AppPreferences.objectsStartFreesize                        , false, true),
    _1_098(null, Token     , Size        , AppPreferences.backgroundsStartFreesize                    , false, true),
    _1_099(null, Token     , Snap        , AppPreferences.tokensStartSnapToGrid                       , false, true),
    _1_100(null, Token     , Snap        , AppPreferences.objectsStartSnapToGrid                      , false, true),
    _1_101(null, Token     , Snap        , AppPreferences.backgroundsStartSnapToGrid                  , false, true),
    _1_102(null, Token     , Visible     , AppPreferences.newTokensVisible                            , false, true),
    _1_103(null, Token     , Visible     , AppPreferences.newObjectsVisible                           , false, true),
    _1_104(null, Token     , Visible     , AppPreferences.newBackgroundsVisible                       , false, true),

    /* Things that don't have a preference in AppPreferences or need special treatment */
    //     (id                , section        ,group, pref, bold , useDefaultControls
    DEV_OPTIONS("developerOptions", Developer, null, null, false, false),
    STARTUP("startupConfig", Config, null, null, false, false),
    AUTH_KEY("authentication", Config, Authentication, null, false, false),
    THEME("theme", Theme, null, null, false, false),
    MACRO_ED_THEME(null, Theme     , null  , AppPreferences.defaultMacroEditorTheme, false, false),
    ICONS(null, Theme     , null  , AppPreferences.iconTheme, false, false),

    ;
    //spotless:on
    // @formatter:on

    /** seemed like a good idea for filtering stuff */
    final String id;

    /**
     * @see Section
     */
    final Section section;

    /**
     * @see Group
     */
    final @Nullable Group group;

    /**
     * @see Preference
     */
    final @Nullable Preference<?> preference;

    /** not significant enough to be its own thing but should be made to stand out */
    final boolean emphasise;

    /** generate default controls in preference dialogue rather than custom-building something */
    final boolean useDefaultControls;

    /** a generated list of words that will hopefully become used in filtering */
    final List<String> searchWords;

    Collated(
        @Nullable String id,
        Section section,
        @Nullable Group group,
        @Nullable Preference<?> preference,
        boolean emphasise,
        boolean useDefaultControls) {
      this.id = id != null ? id : preference != null ? preference.getKey() : null;
      this.section = section;
      this.group = group;// == null ? NONE : group;
//      this.group = group == null ? NONE : group;
      this.preference = preference;
      this.emphasise = emphasise;
      this.useDefaultControls = useDefaultControls;
      this.searchWords = generateKeyWords(this.id, this.section, this.group, this.preference);
    }

    /**
     * Takes one or more strings and splits them in a variety of ways to create useful keywords
     *
     * @param strings Source strings
     * @return List of identified bits
     */
    private static List<String> divideAndConquer(String... strings) {
      List<String> stringList = new ArrayList<>();
      for (String s : strings) {
        if (s == null) {
          continue;
        }
        stringList.add(s);
        s = StringUtils.normalizeSpace(s);
        Collections.addAll(stringList, StringUtils.splitByCharacterTypeCamelCase(s));
        s = s.replaceAll("[\\.,-:;/]+", " ");
//        Collections.addAll(stringList, StringUtils.splitByWholeSeparator(s, "."));
        Collections.addAll(stringList, StringUtils.splitByWholeSeparator(s, null));
      }
      return stringList.stream()
          .distinct()
          .filter(Predicate.not(String::isBlank))
          .map(String::toLowerCase)
          .filter(s -> s.length() > 2)
          .toList();
    }

    /**
     * Generates keywords from various string sources
     *
     * @return List<String>
     */
    private static List<String> generateKeyWords(
        @Nullable String id,
        @Nullable Section section,
        @Nullable Group group,
        @Nullable Preference<?> preference) {
      final List<String> keyWords = new ArrayList<>();
      if (id != null) {
        keyWords.addAll(divideAndConquer(id));
      }
      if (section != null) {
        keyWords.addAll(divideAndConquer(section.name(), section.i18nKey, section.displayName));
      }
      if (group != null) {
        keyWords.addAll(divideAndConquer(group.name(), group.i18nKey, group.displayName));
      }
      if (preference != null) {
        keyWords.addAll(
            divideAndConquer(
                preference.getClass().getSimpleName(),
                preference.getValueClass().getSimpleName(),
                preference.getKey(),
                preference.getLabel(),
                preference.getTooltip(),
                String.valueOf(preference.getDefault()),
                String.valueOf(preference.get())));
      }
      return keyWords;
    }

    @Override
    public List<String> getSearchWords() {
      return searchWords;
    }
  }
}
