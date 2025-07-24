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

import com.jidesoft.swing.Searchable;
import com.jidesoft.swing.TreeSearchable;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.preferences.Preference;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

class ClassifyII {
//  record PrefRecord(Preference<?> preference, KeyWord section, @Nullable KeyWord group, KeyWord[] keyWords){};
//  private static final DefaultMutableTreeNode ROOT = new DefaultMutableTreeNode(null, true);
//  private static final TreeModel PREFERENCE_TREE = new DefaultTreeModel(ROOT);
//  private static final BiFunction<DefaultMutableTreeNode, Object,DefaultMutableTreeNode> createBranch = (parent, object) -> {
//    DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(object, true);
//    parent.add(dmtn);
//    return dmtn;
//  };
//  static {
//    DefaultMutableTreeNode APPLICATION = createBranch.apply(ROOT, null);
//    DefaultMutableTreeNode STAT_SHEET = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode STATUS_BAR = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode TOOLTIP = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode CHAT = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode CHAT_FONT_SIZE  = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode INITIATIVE = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode MAP = createBranch.apply(APPLICATION, null);
//    DefaultMutableTreeNode TOKEN = createBranch.apply(APPLICATION, null);
//
//  }
//  public record PreferenceDetails(String name){};
//  private enum old {
//    // spotless:off
//    _000(false, false, 1, "root", "", null, null),
//    _001(false, false, 1, PreferencesDialog.APPEARANCE, "Appearance                         ", "Label.appearance", null),
//    _002(false, false, 2, PreferencesDialog.APPEARANCE, "Chat                               ", "panel.Chat", null),
//    _003(false, false, 3, PreferencesDialog.APPEARANCE, "Chat.Font                          ", "Label.font", null),
//    _004(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Font.Size                     ", null, AppPreferences.fontSize),
//    _005(false, false, 3, PreferencesDialog.APPEARANCE, "Chat.Trusted                       ", "Label.trusted", null),
//    _006(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Trusted.Foreground            ", null, AppPreferences.trustedPrefixForeground),
//    _007(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Trusted.Background            ", null, AppPreferences.trustedPrefixBackground),
//    _008(false, false, 3, PreferencesDialog.APPEARANCE, "Chat.Tooltip                       ", "component.label.macro.toolTip", null),
//    _009(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Tooltip.Inline                ", null, AppPreferences.useToolTipForInlineRoll),
//    _010(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Tooltip.Link.Suppress         ", null, AppPreferences.suppressToolTipsForMacroLinks),
//    _011(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Smilies                       ", null, AppPreferences.showSmilies),
//    _012(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Avatar                        ", null, AppPreferences.showAvatarInChat),
//    _013(false, false, 3, PreferencesDialog.APPEARANCE, "Chat.Notification                  ", "Label.notifications", null),
//    _014(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Notification.Color.Foreground ", null, AppPreferences.chatNotificationColor),
//    _015(false, false, 4, PreferencesDialog.APPEARANCE, "Chat.Notification.Color.Background ", null, AppPreferences.chatNotificationBackground),
//    _016(false, false, 2, PreferencesDialog.APPEARANCE, "Initiative                         ", "initiative.menu", null),
//    _017(false, false, 4, PreferencesDialog.APPEARANCE, "Initiative.Token.Image             ", null, AppPreferences.initiativePanelShowsTokenImage),
//    _018(false, false, 4, PreferencesDialog.APPEARANCE, "Initiative.Token.State             ", null, AppPreferences.initiativePanelShowsTokenState),
//    _020(false, false, 4, PreferencesDialog.APPEARANCE, "Initiative.Token.NextLine          ", null, AppPreferences.initiativePanelShowsInitiativeOnLine2),
//    _021(false, false, 4, PreferencesDialog.APPEARANCE, "Initiative.NPC.Hide                ", null, AppPreferences.initiativePanelHidesNpcs),
//    _022(false, false, 2, PreferencesDialog.APPEARANCE, "Map                                ", "Button.map", null),
//    _023(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Fog                            ", null, AppPreferences.fogOverlayOpacity),
//    _024(false, false, 3, PreferencesDialog.APPEARANCE, "Map.Halo                           ", "token.popup.menu.halo", null),
//    _025(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Halo.Opacity                   ", null, AppPreferences.haloOverlayOpacity),
//    _026(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Halo.Weight                    ", null, AppPreferences.haloLineWidth),
//    _027(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Aura.Opacity                   ", null, AppPreferences.auraOverlayOpacity),
//    _028(false, false, 3, PreferencesDialog.APPEARANCE, "Map.Light                          ", "CampaignPropertiesDialog.tab.light", null),
//    _029(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Light.UseHalo                  ", null, AppPreferences.useHaloColorOnVisionOverlay),
//    _030(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Light.Opacity                  ", null, AppPreferences.lightOverlayOpacity),
//    _031(false, false, 4, PreferencesDialog.APPEARANCE, "Map.Light.Lumens.Opacity           ", null, AppPreferences.lumensOverlayOpacity),
//    _032(false, false, 2, PreferencesDialog.APPEARANCE, "Token                              ", "Label.token", null),
//    _033(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Stack.Hide                   ", null, AppPreferences.hideTokenStackIndicator),
//    _034(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Facing.Arrow.Force           ", null, AppPreferences.forceFacingArrow),
//    _035(false, false, 3, PreferencesDialog.APPEARANCE, "Token.Label                        ", "Preferences.label.access.tokenLabel", null),
//    _036(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color                  ", "sightLight.optionLabel.color", null),
//    _037(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Foreground       ", "Label.foreground", null),
//    _038(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Foreground.NPC   ", null, AppPreferences.npcMapLabelForeground),
//    _039(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Foreground.PC    ", null, AppPreferences.pcMapLabelForeground),
//    _040(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Foreground.Hidden", null, AppPreferences.nonVisibleTokenMapLabelForeground),
//    _041(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Background       ", "Label.background", null),
//    _042(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Background.NPC   ", null, AppPreferences.npcMapLabelBackground),
//    _043(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Background.PC    ", null, AppPreferences.pcMapLabelBackground),
//    _044(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Background.Hidden", null, AppPreferences.nonVisibleTokenMapLabelBackground),
//    _045(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Border           ", "Label.border", null),
//    _052(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Border.Show            ", null, AppPreferences.mapLabelShowBorder),
//    _046(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Border.NPC       ", null, AppPreferences.npcMapLabelBorder),
//    _047(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Border.PC        ", null, AppPreferences.pcMapLabelBorder),
//    _048(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Color.Border.NPC       ", null, AppPreferences.nonVisibleTokenMapLabelBorder),
//    _049(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Border.Font.Size       ", null, AppPreferences.mapLabelFontSize),
//    _050(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Border.Arc             ", null, AppPreferences.mapLabelBorderArc),
//    _051(false, false, 4, PreferencesDialog.APPEARANCE, "Token.Label.Border.Weight          ", null, AppPreferences.mapLabelBorderWidth),
//    _053(false, false, 2, PreferencesDialog.APPEARANCE, "StatSheet                          ", "EditTokenDialog.label.statSheet", null),
//    _054(false, false, 4, PreferencesDialog.APPEARANCE, "StatSheet.Show                     ", null, AppPreferences.showStatSheet),
//    _055(false, false, 3, PreferencesDialog.APPEARANCE, "StatSheet.Portrait                 ", "EditTokenDialog.border.title.portrait", null),
//    _056(false, false, 4, PreferencesDialog.APPEARANCE, "StatSheet.Portrait.Size            ", null, AppPreferences.portraitSize),
//    _057(false, false, 4, PreferencesDialog.APPEARANCE, "StatSheet.Portrait.Show            ", null, AppPreferences.showPortrait),
//    _058(false, false, 2, PreferencesDialog.APPEARANCE, "StatusBar                          ", "Preferences.label.access.status.options", null),
//    _059(false, false, 4, PreferencesDialog.APPEARANCE, "StatusBar.Scroll                   ", null, AppPreferences.scrollStatusMessages),
//    _060(false, false, 4, PreferencesDialog.APPEARANCE, "StatusBar.Scroll.Speed             ", null, AppPreferences.scrollStatusSpeed),
//    _062(false, false, 4, PreferencesDialog.APPEARANCE, "StatusBar.Scroll.Delay.Start       ", null, AppPreferences.scrollStatusStartDelay),
//    _063(false, false, 4, PreferencesDialog.APPEARANCE, "StatusBar.Scroll.Delay.End         ", null, AppPreferences.scrollStatusEndPause),
//    _064(false, false, 4, PreferencesDialog.APPEARANCE, "StatusBar.Duration.Temp            ", null, AppPreferences.scrollStatusTempDuration),
//    _065(false, false, 2, PreferencesDialog.APPEARANCE, "Theme                              ", "Label.themes", null),
//    _066(false, false, 3, PreferencesDialog.APPEARANCE, "Theme.Application                  ", "Label.application", null),
//    _067(false, false, 4, PreferencesDialog.APPEARANCE, "Theme.Application.Icon             ", null, AppPreferences.UIIcons),
//    _069(false, true, 4, PreferencesDialog.APPEARANCE, "Theme.Application.Font.Settings    ", "Preferences.heading.theme.fonts", null),
//    _070(false, false, 4, PreferencesDialog.APPEARANCE, "Theme.Macro.Editor                 ", null, AppPreferences.defaultMacroEditorTheme),
//    _071(false, false, 1, PreferencesDialog.OTHER, "Default                            ", "Label.default", null),
//    _072(false, false, 2, PreferencesDialog.OTHER, "Default.Map                        ", "Label.maps", null),
//    _073(false, false, 3, PreferencesDialog.OTHER, "Default.Map.Grid                   ", "Label.grid", null),
//    _074(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Grid.Type              ", null, AppPreferences.defaultGridType),
//    _075(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Grid.Color             ", null, AppPreferences.defaultGridColor),
//    _076(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Grid.Size              ", null, AppPreferences.defaultGridSize),
//    _077(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Grid.Units             ", null, AppPreferences.defaultUnitsPerCell),
//    _078(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Grid.Metric            ", null, AppPreferences.movementMetric),
//    _079(false, false, 3, PreferencesDialog.OTHER, "Default.Map.Vision                 ", "Label.lights", null),
//    _080(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Vision.Distance        ", null, AppPreferences.defaultVisionDistance),
//    _081(false, false, 4, PreferencesDialog.OTHER, "Default.Map.Vision.Type            ", null, AppPreferences.defaultVisionType),
//    _082(false, false, 2, PreferencesDialog.OTHER, "Default.User                       ", null, AppPreferences.defaultUserName),
//    _083(false, false, 4, PreferencesDialog.OTHER, "Default.User.Name                  ", null, AppPreferences.defaultUserName),
//    _084(false, false, 1, PreferencesDialog.BEHAVIOUR, "Behaviour                          ", "Label.behaviour", null),
//    _085(false, false, 2, PreferencesDialog.BEHAVIOUR, "Initiative                         ", "initiative.menu", null),
//    _086(false, false, 4, PreferencesDialog.BEHAVIOUR, "Initiative.Movement.Lock           ", null, AppPreferences.initiativeMovementLocked),
//    _087(false, false, 4, PreferencesDialog.BEHAVIOUR, "Initiative.Owner.Permission        ", null, AppPreferences.initiativePanelAllowsOwnerPermissions),
//    _088(false, false, 4, PreferencesDialog.BEHAVIOUR, "Initiative.Gain.Message            ", null, AppPreferences.showInitiativeGainedMessage),
//    _089(false, false, 2, PreferencesDialog.BEHAVIOUR, "Notify                             ", "Label.notifications", null),
//    _090(false, false, 4, PreferencesDialog.BEHAVIOUR, "Notify.Typing.Duration             ", null, AppPreferences.typingNotificationDurationInSeconds),
//    _091(false, false, 4, PreferencesDialog.BEHAVIOUR, "Notify.Save                        ", null, AppPreferences.saveReminder),
//    _092(false, false, 2, PreferencesDialog.BEHAVIOUR, "Save                               ", "token.popup.menu.save", null),
//    _093(false, false, 4, PreferencesDialog.BEHAVIOUR, "Save.Campaign                      ", null, AppPreferences.autoSaveIncrement),
//    _094(false, false, 4, PreferencesDialog.BEHAVIOUR, "Save.Chat                          ", null, AppPreferences.chatAutoSaveTimeInMinutes),
//    _095(false, false, 4, PreferencesDialog.BEHAVIOUR, "Save.Chat.FileName                 ", null, AppPreferences.chatFilenameFormat),
//    _096(false, true, 4, PreferencesDialog.BEHAVIOUR, "Save.File.Sync.Path                ", null, AppPreferences.fileSyncPath),
//    _097(false, false, 4, PreferencesDialog.BEHAVIOUR, "Save.Campaign.Load                 ", null, AppPreferences.loadMruCampaignAtStart),
//    _098(false, false, 2, PreferencesDialog.BEHAVIOUR, "Sound                              ", "Label.sounds", null),
//    _099(false, false, 4, PreferencesDialog.BEHAVIOUR, "Sound.System                       ", null, AppPreferences.playSystemSounds),
//    _100(false, false, 4, PreferencesDialog.BEHAVIOUR, "Sound.System.NoFocus               ", null, AppPreferences.playSystemSoundsOnlyWhenNotFocused),
//    _101(false, false, 4, PreferencesDialog.BEHAVIOUR, "Sound.Stream                       ", null, AppPreferences.playStreams),
//    _102(false, false, 4, PreferencesDialog.BEHAVIOUR, "Sound.Syrinscape                   ", null, AppPreferences.syrinscapeActive),
//    _103(false, false, 2, PreferencesDialog.BEHAVIOUR, "StatSheet                          ", "EditTokenDialog.label.statSheet", null),
//    _104(false, false, 4, PreferencesDialog.BEHAVIOUR, "StatSheet.Modifier                 ", null, AppPreferences.showStatSheetRequiresModifierKey),
//    _105(false, false, 2, PreferencesDialog.BEHAVIOUR, "Map                                ", "Label.maps", null),
//    _106(false, false, 3, PreferencesDialog.BEHAVIOUR, "Map.Fog                            ", "Button.fog", null),
//    _107(false, false, 4, PreferencesDialog.BEHAVIOUR, "Map.Fog.New                        ", null, AppPreferences.newMapsHaveFow),
//    _108(false, false, 4, PreferencesDialog.BEHAVIOUR, "Map.Fog.Reveal                     ", null, AppPreferences.autoRevealVisionOnGMMovement),
//    _109(false, false, 4, PreferencesDialog.BEHAVIOUR, "Map.Import                         ", null, AppPreferences.uvttLosImportType),
//    _110(false, false, 4, PreferencesDialog.BEHAVIOUR, "Map.Sort                           ", null, AppPreferences.mapSortType),
//    _111(false, false, 4, PreferencesDialog.BEHAVIOUR, "Map.New.Visible                    ", null, AppPreferences.newMapsVisible),
//    _112(false, false, 4, PreferencesDialog.BEHAVIOUR, "Map.View                           ", null, AppPreferences.fitGmView),
//    _113(false, false, 2, PreferencesDialog.BEHAVIOUR, "New.Token                          ", "Label.token", null),
//    _114(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Dialog                   ", null, AppPreferences.showDialogOnNewToken),
//    _115(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Duplicate                ", null, AppPreferences.numberTokenDuplicateMethod),
//    _116(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Name                     ", null, AppPreferences.newTokenName),
//    _117(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Numbering                ", null, AppPreferences.showTokenNumberOn),
//    _118(false, false, 3, PreferencesDialog.BEHAVIOUR, "New.Token.Size                     ", "EditTokenDialog.label.size", null),
//    _119(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Size.Background          ", null, AppPreferences.backgroundsStartFreesize),
//    _120(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Size.Object              ", null, AppPreferences.objectsStartFreesize),
//    _121(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Size.Token               ", null, AppPreferences.tokensStartFreesize),
//    _122(false, false, 3, PreferencesDialog.BEHAVIOUR, "New.Token.Snap                     ", "Label.snapToGrid", null),
//    _123(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Snap.Background          ", null, AppPreferences.backgroundsStartSnapToGrid),
//    _124(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Snap.Object              ", null, AppPreferences.objectsStartSnapToGrid),
//    _125(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Snap.Token               ", null, AppPreferences.tokensStartSnapToGrid),
//    _126(false, false, 3, PreferencesDialog.BEHAVIOUR, "New.Token.Visible                  ", "Label.visibility", null),
//    _127(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Visible.Background       ", null, AppPreferences.newBackgroundsVisible),
//    _128(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Visible.Object           ", null, AppPreferences.newObjectsVisible),
//    _129(false, false, 4, PreferencesDialog.BEHAVIOUR, "New.Token.Visible.Token            ", null, AppPreferences.newTokensVisible),
//    _130(false, false, 2, PreferencesDialog.BEHAVIOUR, "Facing                             ", "Label.facing", null),
//    _131(false, false, 4, PreferencesDialog.BEHAVIOUR, "Facing.Vertices                    ", null, AppPreferences.faceVertex),
//    _132(false, false, 4, PreferencesDialog.BEHAVIOUR, "Facing.Edge                        ", null, AppPreferences.faceEdge),
//    _133(false, false, 2, PreferencesDialog.BEHAVIOUR, "Snap                               ", "Label.snapToGrid", null),
//    _134(false, false, 4, PreferencesDialog.BEHAVIOUR, "Snap.Drag                          ", null, AppPreferences.tokensSnapWhileDragging),
//    _135(false, false, 4, PreferencesDialog.BEHAVIOUR, "Snap.Drag.Pointer                  ", null, AppPreferences.hideMousePointerWhileDragging),
//    _136(false, false, 2, PreferencesDialog.BEHAVIOUR, "Tooltip                            ", "component.label.macro.toolTip", null),
//    _137(false, false, 4, PreferencesDialog.BEHAVIOUR, "Tooltip.Delay.Initial              ", null, AppPreferences.toolTipInitialDelay),
//    _138(false, false, 4, PreferencesDialog.BEHAVIOUR, "Tooltip.Delay.Dismiss              ", null, AppPreferences.toolTipDismissDelay),
//    _139(false, false, 2, PreferencesDialog.BEHAVIOUR, "UPnP                               ", "Label.upnp", null),
//    _140(false, false, 4, PreferencesDialog.BEHAVIOUR, "UPnP.Timeout                       ", null, AppPreferences.upnpDiscoveryTimeout),
//    _141(false, false, 2, PreferencesDialog.BEHAVIOUR, "Warn                               ", "MapToolEventQueue.warning.title", null),
//    _142(false, false, 4, PreferencesDialog.BEHAVIOUR, "Warn.Delete.Drawing                ", null, AppPreferences.drawingsWarnWhenDeleted),
//    _143(false, false, 4, PreferencesDialog.BEHAVIOUR, "Warn.Delete.Token                  ", null, AppPreferences.tokensWarnWhenDeleted),
//    _144(false, false, 4, PreferencesDialog.BEHAVIOUR, "Warn.Map.Visible                   ", null, AppPreferences.mapVisibilityWarning),
//    _145(false, false, 4, PreferencesDialog.BEHAVIOUR, "Warn.Initiative.Reset              ", null, AppPreferences.initiativePanelWarnWhenResettingRoundCounter),
//    _146(false, false, 2, PreferencesDialog.BEHAVIOUR, "Macro.Editor                       ", null, AppPreferences.openEditorForNewMacro),
//    _147(false, false, 4, PreferencesDialog.BEHAVIOUR, "Macro.Edit.Allow                   ", null, AppPreferences.allowPlayerMacroEditsDefault),
//    _148(false, false, 4, PreferencesDialog.BEHAVIOUR, "Macro.External.Allow               ", null, AppPreferences.allowExternalMacroAccess),
//    _149(false, false, 1, PreferencesDialog.PERFORMANCE, "Performance                        ", "Label.performance", null),
//    _150(false, false, 4, PreferencesDialog.PERFORMANCE, "Render.Selection.Fill              ", null, AppPreferences.fillSelectionBox),
//    _151(false, false, 4, PreferencesDialog.PERFORMANCE, "Render.Scaling                     ", null, AppPreferences.renderQuality),
//    _152(false, false, 4, PreferencesDialog.PERFORMANCE, "Render.Frame.Rate                  ", null, AppPreferences.frameRateCap),
//    _153(false, false, 1, PreferencesDialog.OTHER, "Authentication                     ", "Label.auth", null),
//    _154(false, true, 4, PreferencesDialog.OTHER, "Authentication.key                 ", null, null),
//    _155(false, false, 1, PreferencesDialog.DEVELOPER, "Developer                          ", , null),
//    _156(true, true, 4, PreferencesDialog.DEVELOPER, "Developer                          ", null, null),
//    _157(false, false, 1, PreferencesDialog.OTHER, "Config                             ", , null),
//    _158(false, true, 4, PreferencesDialog.OTHER, "Config.Path                        ", null, null);
//
//    //spotless:on
//    final boolean hasAction;
//    final int level;
//    final String preferenceKey;
//    final String contentType;
//    final String path;
//    final String i18nKey;
//    final @Nullable Preference<?> preference;
//
//    ClassifyII(
//            boolean warning,
//            boolean hasAction,
//            int level,
//            String contentType,
//            String path,
//            String i18nKey,
//            @Nullable Preference<?> preference) {
//      this.hasAction = hasAction;
//      this.level = level;
//      this.contentType = contentType;
//      this.path = path.trim();
//      this.i18nKey = i18nKey;
//      this.preference = preference;
//      this.preferenceKey = preference == null ? null : preference.getKey();
//    }
//
//    static final EnumSet<ClassifyII> classifyEnumSet = EnumSet.allOf(ClassifyII.class);
//    static final Set<ClassifyII> allSet = new HashSet<>(classifyEnumSet);
//
//    private static final Set<ClassifyII> preferenceSet =
//            classifyEnumSet.stream()
//                    .filter(classify -> classify.preference != null)
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> appearanceSet =
//            classifyEnumSet.stream()
//                    .filter(classify -> classify.contentType.equals(PreferencesDialog.APPEARANCE))
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> behaviourSet =
//            classifyEnumSet.stream()
//                    .filter(classify -> classify.contentType.equals(PreferencesDialog.BEHAVIOUR))
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> performanceSet =
//            classifyEnumSet.stream()
//                    .filter(classify -> classify.contentType.equals(PreferencesDialog.PERFORMANCE))
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> otherSet =
//            classifyEnumSet.stream()
//                    .filter(classify -> classify.contentType.equals(PreferencesDialog.OTHER))
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> numberSet =
//            preferenceSet.stream()
//                    .filter(
//                            classify ->
//                                    Objects.requireNonNull(classify.preference).cast(Integer.class).isPresent()
//                                            || classify.preference.cast(Double.class).isPresent())
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> booleanSet =
//            preferenceSet.stream()
//                    .filter(
//                            classify ->
//                                    Objects.requireNonNull(classify.preference).cast(Boolean.class).isPresent())
//                    .collect(Collectors.toSet());
//    static final Set<ClassifyII> colourSet =
//            preferenceSet.stream()
//                    .filter(
//                            classify -> Objects.requireNonNull(classify.preference).cast(Color.class).isPresent())
//                    .collect(Collectors.toSet());
//
//    static final Set<ClassifyII> stringSet =
//            preferenceSet.stream()
//                    .filter(
//                            classify ->
//                                    Objects.requireNonNull(classify.preference).cast(String.class).isPresent())
//                    .collect(Collectors.toSet());
//
//    private static List<ClassifyII> getChildren(ClassifyII classify) {
//      List<ClassifyII> out = new ArrayList<>();
//      if (classify.level == 4) {
//        return out;
//      }
//      out =
//              allSet.stream()
//                      .filter(c -> c.level == classify.level + 1)
//                      .filter(c -> c.path.startsWith(classify.path) && !c.path.equals(classify.path))
//                      .toList();
//      if (!out.isEmpty()) {
//        return out;
//      }
//      return allSet.stream()
//              .filter(c -> c.level < classify.level)
//              .filter(c -> c.path.startsWith(classify.path) && !c.path.equals(classify.path))
//              .toList();
//    }
//
//    private static void addTreeNodes(DefaultMutableTreeNode parent, List<ClassifyII> list) {
//      for (ClassifyII c : list) {
//
//        DefaultMutableTreeNode node = new DefaultMutableTreeNode(c, c.level != 4);
//        parent.add(node);
//        if (c.level != 4) {
//          addTreeNodes(node, getChildren(c));
//        }
//      }
//    }
//
//    @Override
//    public String toString() {
//      if (i18nKey == null) {
//        return path;
//      } else {
//        return I18N.getText(i18nKey);
//      }
//    }
//
//    public static final TreeModel TREE_MODEL = buildTree();
//    public static final JTree TREE = new JTree(TREE_MODEL);
//    public static final Searchable SEARCHABLE = new TreeSearchable(TREE);
//
//    private static TreeModel buildTree() {
//      DefaultMutableTreeNode root = new DefaultMutableTreeNode(_000, true);
//      TreeModel model = new DefaultTreeModel(root);
//      List<ClassifyII> startingList = allSet.stream().filter(o -> o.level == 1).toList();
//      for (ClassifyII c : startingList) {
//        DefaultMutableTreeNode node = new DefaultMutableTreeNode(c, c.level != 4);
//        addTreeNodes(node, getChildren(c));
//      }
//      return model;
//    }
//  }
}