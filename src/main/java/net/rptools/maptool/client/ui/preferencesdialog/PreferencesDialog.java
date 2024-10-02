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

import static net.rptools.maptool.util.UserJvmOptions.getLanguages;
import static net.rptools.maptool.util.UserJvmOptions.setJvmOption;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppPreferences.RenderQuality;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.PreferencesChanged;
import net.rptools.maptool.client.functions.MediaPlayerAdapter;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeDetails;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.StringUtil;
import net.rptools.maptool.util.UserJvmOptions;
import net.rptools.maptool.util.UserJvmOptions.JVM_OPTION;
import net.rptools.maptool.util.cipher.CipherUtil;
import net.rptools.maptool.util.cipher.PublicPrivateKeyStore;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Class: PreferencesDialog
 *
 * <p>A dialog box to manage user preferences.
 */
public class PreferencesDialog extends JDialog {

  /** Logger instance used for logging messages in the PreferencesDialog class. */
  private static final Logger log = LogManager.getLogger(PreferencesDialog.class);

  /**
   * Represents a tabbed panel in the PreferencesDialog class. This panel is used for displaying and
   * managing multiple tabs.
   */
  // Tabbed Panel
  private final JTabbedPane tabbedPane;

  /**
   * Represents a check box for the preference setting to have fog of war (FOW) enabled on new maps.
   */
  // Interactions
  private final JCheckBox newMapsHaveFOWCheckBox;

  /** Represents a check box for enabling a warning popup when a token is deleted. */
  private final JCheckBox tokensPopupWarningWhenDeletedCheckBox;

  /** The checkbox used to indicate whether tokens should start snapping to the grid. */
  private final JCheckBox tokensStartSnapToGridCheckBox;

  /**
   * Represents a checkbox for the preference setting to have tokens snap to the grid while being
   * dragged.
   */
  private final JCheckBox tokensSnapWhileDraggingCheckBox;

  /** Represents a checkbox that allows the user to hide the mouse pointer while dragging. */
  private final JCheckBox hideMousePointerWhileDraggingCheckBox;

  /** Represents a checkbox indicating whether to hide the token stack indicator. */
  private final JCheckBox hideTokenStackIndicatorCheckBox;

  /** Represents a checkbox for controlling the visibility of new maps. */
  private final JCheckBox newMapsVisibleCheckBox;

  /** Represents the checkbox for controlling the visibility of new tokens. */
  private final JCheckBox newTokensVisibleCheckBox;

  /** Represents the checkbox for starting tokens with free size. */
  private final JCheckBox tokensStartFreeSizeCheckBox;

  /** The checkbox for new stamps with snap-to-grid enabled. */
  private final JCheckBox stampsStartSnapToGridCheckBox;

  /** The checkbox for new stamps with free size enabled. */
  private final JCheckBox stampsStartFreeSizeCheckBox;

  /** Provides a checkbox for new backgrounds snap-to-grid enabled. */
  private final JCheckBox backgroundsStartSnapToGridCheckBox;

  /** The checkbox for new backgrounds free size enabled. */
  private final JCheckBox backgroundsStartFreeSizeCheckBox;

  /** JComboBox variable used to display duplicate token naming options. */
  private final JComboBox<LocalizedComboItem> duplicateTokenCombo;

  /** JComboBox variable used to display token naming options for imported tokens. */
  private final JComboBox<LocalizedComboItem> tokenNamingCombo;

  /** JComboBox variable used to display token numbering options. */
  private final JComboBox<LocalizedComboItem> showNumberingCombo;

  /** JComboBox variable used to display movement metric options. */
  private final JComboBox<WalkerMetric> movementMetricCombo;

  /** JComboBox variable used to display vision type options. */
  private final JComboBox<Zone.VisionType> visionTypeCombo;

  /** JComboBox variable used to display map sorting options. */
  private final JComboBox<AppPreferences.MapSortType> mapSortType;

  /** Checkbox for displaying or hiding * the statistics sheet on token mouseover. */
  private final JCheckBox showStatSheetCheckBox;

  /** Checkbox for displaying or hiding the token portrait on token mouseover. */
  private final JCheckBox showPortraitCheckBox;

  /** Checkbox for if the modifier key needs to be held down to show the stat sheet. */
  private final JCheckBox showStatSheetModifierCheckBox;

  /** Checkbox for if the facing arrow should be forced to be shown. */
  private final JCheckBox forceFacingArrowCheckBox;

  /** Checkbox for if the map visibility warning should be shown. */
  private final JCheckBox mapVisibilityWarning;

  /** Spinner for the halo line width. */
  private final JSpinner haloLineWidthSpinner;

  /** Spinner for the halo overlay opacity. */
  private final JSpinner haloOverlayOpacitySpinner;

  /** Spinner for the aura overlay opacity. */
  private final JSpinner auraOverlayOpacitySpinner;

  /** Spinner for the light overlay opacity. */
  private final JSpinner lightOverlayOpacitySpinner;

  /** Spinner for the luminosity overlay opacity. */
  private final JSpinner lumensOverlayOpacitySpinner;

  /** Spinner for the luminosity overlay border thickness. */
  private final JSpinner lumensOverlayBorderThicknessSpinner;

  /** Checkbox for if the luminosity overlay should be shown by default. */
  private final JCheckBox lumensOverlayShowByDefaultCheckBox;

  /** Checkbox for if the environmental lights should be shown by default. */
  private final JCheckBox lightsShowByDefaultCheckBox;

  /** Spinner for the fog opacity. */
  private final JSpinner fogOverlayOpacitySpinner;

  /** Checkbox for if the halo color should be used as the vision overlay. */
  private final JCheckBox useHaloColorAsVisionOverlayCheckBox;

  /** Checkbox for if the vision should be auto-revealed on GM move. */
  private final JCheckBox autoRevealVisionOnGMMoveCheckBox;

  /** Checkbox for if smilies should be converted to images in chat. */
  private final JCheckBox showSmiliesCheckBox;

  /** Checkbox for if system sounds should be played. */
  private final JCheckBox playSystemSoundCheckBox;

  /** Checkbox for if audio streams should be played. */
  private final JCheckBox playStreamsCheckBox;

  /** Checkbox for if system sounds should be played only when not focused. */
  private final JCheckBox playSystemSoundOnlyWhenNotFocusedCheckBox;

  /** Checkbox for if Syrinscape integration should be enabled. */
  private final JCheckBox syrinscapeActiveCheckBox;

  /** Checkbox for if token facing is allowed to point to edges of the grid cells. */
  private final JCheckBox facingFaceEdges;

  /** Checkbox for if token facing is allowed to point to vertices of the grid cells. */
  private final JCheckBox facingFaceVertices;

  /** Checkbox for if the avatar should be shown in chat. */
  private final JCheckBox showAvatarInChat;

  /** Checkbox for if new macros should be editable by players by default. */
  private final JCheckBox allowPlayerMacroEditsDefault;

  /** Checkbox for if the details of inline rolls should be shown in tooltips. */
  private final JCheckBox toolTipInlineRolls;

  /** Checkbox for if macro link details should be suppressed in tooltips. */
  private final JCheckBox suppressToolTipsMacroLinks;

  /** ColorWell for the completed trusted path output foreground color. */
  private final ColorWell trustedOutputForeground;

  /** ColorWell for the completed trusted path output background color. */
  private final ColorWell trustedOutputBackground;

  /** Spinner for the chat autosave time. */
  private final JSpinner chatAutosaveTime;

  /** Text field for the chat autosave filename format. */
  private final JTextField chatFilenameFormat;

  /** Spinner for the typing notification duration. */
  private final JSpinner typingNotificationDuration;

  /** ComboBox for the macro editor theme. */
  private final JComboBox<String> macroEditorThemeCombo;

  /** ComboBox for the icon theme. */
  private final JComboBox<String> iconThemeCombo;

  // Chat Notification
  /** ColorWell for the chat notification color. */
  private final ColorWell chatNotificationColor;

  /** Checkbox for if the chat notification background should be shown. */
  private final JCheckBox chatNotificationShowBackground;

  // Defaults
  /** ComboBox for the default grid type to use for new maps. */
  private final JComboBox<LocalizedComboItem> defaultGridTypeCombo;

  /** Text field for the default grid size to use for new maps. */
  private final JTextField defaultGridSizeTextField;

  /** Text field for the default units per cell to use for new maps. */
  private final JTextField defaultUnitsPerCellTextField;

  /** Text field for the default vision distance to use for new maps. */
  private final JTextField defaultVisionDistanceTextField;

  /** Spinner for the stat sheet portrait size. */
  private final JTextField statsheetPortraitSize;

  /** Spinner for the auto-save interval for campaign data. */
  private final JSpinner autoSaveSpinner;

  /** Checkbox for if the save reminder should be shown on exit, new campaign etc. */
  private final JCheckBox saveReminderCheckBox;

  /** Checkbox for if the dialog should be shown on new token creation. */
  private final JCheckBox showDialogOnNewToken;

  // Accessibility
  /** Text field for the chat font size. */
  private final JTextField fontSizeTextField;

  /** Spinner for the initial delay for tooltips. */
  private final JTextField toolTipInitialDelay;

  /** Spinner for the dismiss delay for tooltips. */
  private final JTextField toolTipDismissDelay;

  // Application
  /** Checkbox for if the client should fit the GM view automatically. */
  private final JCheckBox fitGMView;

  /** Checkbox for if the selection should be filled when selecting objects. */
  private final JCheckBox fillSelectionCheckBox;

  /** Text field for the frame rate cap for rendering. */
  private final JTextField frameRateCapTextField;

  /** ComboBox for the render performance optimization level. */
  private final JComboBox<LocalizedComboItem> renderPerformanceComboBox;

  /** Text field for the default username when not logged into a server. */
  private final JTextField defaultUsername;

  // private final JCheckBox initEnableServerSyncCheckBox;
  /** Checkbox for if non-player characters should be hidden when creating a new map. */
  private final JCheckBox hideNPCs;

  /** Checkbox for if owner permissions should be granted when creating a new campaign. */
  private final JCheckBox ownerPermissions;

  /** Checkbox for if movement should be locked in new campaigns. */
  private final JCheckBox lockMovement;

  /** Checkbox for if initiative gain messages should be shown. */
  private final JCheckBox showInitGainMessage;

  /** Text field for the UPnP discovery timeout. */
  private final JTextField upnpDiscoveryTimeoutTextField;

  /** Text field for the file synchronization path. */
  private final JTextField fileSyncPath;

  /** Button for opening the file synchronization path selection dialog. */
  private final JButton fileSyncPathButton;

  /** Checkbox for if macros should be allowed to access external resources. */
  private final JCheckBox allowExternalMacroAccessCheckBox;

  // Authentication
  /** Text area for displaying the public key for authentication. */
  private final JTextArea publicKeyTextArea;

  /** Button for regenerating the public key. */
  private final JButton regeneratePublicKey;

  /** Button for copying the public key to the clipboard. */
  private final JButton copyPublicKey;

  // Themes
  /** List for displaying available themes. */
  private final JList<String> themeList;

  /** Label for displaying the theme image. */
  private final JLabel themeImageLabel;

  /** Label for displaying the theme name. */
  private final JLabel themeNameLabel;

  /** List model for all available themes. */
  private final ListModel<String> allThemesListModel;

  /** List model for light themes. */
  private final ListModel<String> lightThemesListModel;

  /** List model for dark themes. */
  private final ListModel<String> darkThemesListModel;

  /** Combo box for selecting the theme filter. */
  private final JComboBox<LocalizedComboItem> themeFilterCombo;

  /** Checkbox for if the theme should be applied to the chat window. */
  private final JCheckBox useThemeForChat;

  // Startup
  /** Text field for the JVM maximum memory allocation. */
  private final JTextField jvmXmxTextField;

  /** Text field for the JVM initial memory allocation. */
  private final JTextField jvmXmsTextField;

  /** Text field for the JVM thread stack size. */
  private final JTextField jvmXssTextField;

  /** Text field for the data directory. */
  private final JTextField dataDirTextField;

  /** Checkbox for if the JVM should use Direct3D. */
  private final JCheckBox jvmDirect3dCheckbox;

  /** Checkbox for if the JVM should use OpenGL. */
  private final JCheckBox jvmOpenGLCheckbox;

  /** Checkbox for if the JVM should initialize AWT. */
  private final JCheckBox jvmInitAwtCheckbox;

  /** Combo box for selecting the language override for JAM messages. */
  private final JComboBox<String> jamLanguageOverrideComboBox;

  /** Label for displaying startup information. */
  private final JLabel startupInfoLabel;

  /** Flag indicating if JVM values have been changed. */
  private boolean jvmValuesChanged = false;

  /** Flag indicating if theme has been changed. */
  private boolean themeChanged = false;

  // Map Token Labels
  /** ColorWell for displaying the PC token label foreground color. */
  private final ColorWell pcTokenLabelFG;

  /** ColorWell for displaying the PC token label background color. */
  private final ColorWell pcTokenLabelBG;

  /** ColorWell for displaying the NPC token label foreground color. */
  private final ColorWell npcTokenLabelFG;

  /** ColorWell for displaying the NPC token label background color. */
  private final ColorWell npcTokenLabelBG;

  /** ColorWell for displaying the non-visibility token label foreground color. */
  private final ColorWell nonVisTokenLabelFG;

  /** ColorWell for displaying the non-visibility token label background color. */
  private final ColorWell nonVisTokenLabelBG;

  /** Spinner for setting the token label font size. */
  private final JSpinner labelFontSizeSpinner;

  /** ColorWell for displaying the token label border color for PCs. */
  private final ColorWell pcTokenLabelBorderColor;

  /** ColorWell for displaying the token label border color for NPCs. */
  private final ColorWell npcTokenLabelBorderColor;

  /** ColorWell for displaying the token label border color for non-visible tokens. */
  private final ColorWell nonVisTokenLabelBorderColor;

  /** Spinner for setting the token label border width. */
  private final JSpinner labelBorderWidthSpinner;

  /** Spinner for setting the token label border arc. */
  private final JSpinner labelBorderArcSpinner;

  /** Checkbox for showing the token label border. */
  private final JCheckBox showLabelBorderCheckBox;

  // ** Checkbox for loading the most recently used campaign on startup */
  private final JCheckBox loadMRUcheckbox;

  /**
   * Array of LocalizedComboItems representing the default grid types for the preferences dialog.
   * Each item in the array consists of a grid type and its corresponding localized display name.
   */
  private static final LocalizedComboItem[] defaultGridTypeComboItems = {
    new LocalizedComboItem(GridFactory.SQUARE, "Preferences.combo.maps.grid.square"),
    new LocalizedComboItem(GridFactory.HEX_HORI, "Preferences.combo.maps.grid.hexHori"),
    new LocalizedComboItem(GridFactory.HEX_VERT, "Preferences.combo.maps.grid.hexVert"),
    new LocalizedComboItem(GridFactory.ISOMETRIC, "Preferences.combo.maps.grid.isometric"),
    new LocalizedComboItem(GridFactory.NONE, "MapPropertiesDialog.image.nogrid")
  };

  /**
   * Stores the localized combo items for duplicate token preferences.
   *
   * @see LocalizedComboItem
   */
  private static final LocalizedComboItem[] duplicateTokenComboItems = {
    new LocalizedComboItem(Token.NUM_INCREMENT, "Preferences.combo.tokens.duplicate.increment"),
    new LocalizedComboItem(Token.NUM_RANDOM, "Preferences.combo.tokens.duplicate.random"),
  };

  /**
   * Array of LocalizedComboItem objects for showing numbering options in a combo box.
   *
   * @see LocalizedComboItem
   */
  private static final LocalizedComboItem[] showNumberingComboItems = {
    new LocalizedComboItem(Token.NUM_ON_NAME, "Preferences.combo.tokens.numbering.name"),
    new LocalizedComboItem(Token.NUM_ON_GM, "Preferences.combo.tokens.numbering.gm"),
    new LocalizedComboItem(Token.NUM_ON_BOTH, "Preferences.combo.tokens.numbering.both")
  };

  /**
   * Array of LocalizedComboItem objects for showing vision type options in a combo box.
   *
   * @see LocalizedComboItem
   */
  private static final LocalizedComboItem[] tokenNamingComboItems = {
    new LocalizedComboItem(Token.NAME_USE_FILENAME, "Preferences.combo.tokens.naming.filename"),
    new LocalizedComboItem(
        Token.NAME_USE_CREATURE,
        "Preferences.combo.tokens.naming.creature",
        I18N.getString("Token.name.creature"))
  };

  /**
   * An array of WalkerMetric objects that represent different movement metrics.
   *
   * @see WalkerMetric
   */
  private static final WalkerMetric[] movementMetricComboItems = {
    WalkerMetric.ONE_TWO_ONE,
    WalkerMetric.ONE_ONE_ONE,
    WalkerMetric.MANHATTAN,
    WalkerMetric.NO_DIAGONALS
  };

  /**
   * Array of LocalizedComboItem objects for showing render performance options in a combo box.
   *
   * @see LocalizedComboItem
   */
  private static final LocalizedComboItem[] renderPerformanceComboItems = {
    new LocalizedComboItem(RenderQuality.LOW_SCALING.name(), "Preferences.combo.render.low"),
    new LocalizedComboItem(
        RenderQuality.PIXEL_ART_SCALING.name(), "Preferences.combo.render.pixel"),
    new LocalizedComboItem(RenderQuality.MEDIUM_SCALING.name(), "Preferences.combo.render.medium"),
    new LocalizedComboItem(RenderQuality.HIGH_SCALING.name(), "Preferences.combo.render.high")
  };

  /**
   * Array of LocalizedComboItem objects for showing theme filter options in a combo box.
   *
   * @see LocalizedComboItem
   */
  private static final LocalizedComboItem[] themeFilterComboItems = {
    new LocalizedComboItem("All", "Preferences.combo.themes.filter.all"),
    new LocalizedComboItem("Dark", "Preferences.combo.themes.filter.dark"),
    new LocalizedComboItem("Light", "Preferences.combo.themes.filter.light")
  };

  /**
   * The PreferencesDialog class represents a dialog window that allows users to customize their
   * preferences.
   *
   * <p>This dialog window is modal.
   */
  public PreferencesDialog() {
    super(MapTool.getFrame(), I18N.getString("Label.preferences"), true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    var lm = new DefaultListModel<String>();
    Arrays.stream(ThemeSupport.THEMES).map(ThemeDetails::name).sorted().forEach(lm::addElement);
    allThemesListModel = lm;

    lm = new DefaultListModel<String>();
    Arrays.stream(ThemeSupport.THEMES)
        .filter(ThemeDetails::dark)
        .map(ThemeDetails::name)
        .sorted()
        .forEach(lm::addElement);
    darkThemesListModel = lm;

    lm = new DefaultListModel<String>();
    Arrays.stream(ThemeSupport.THEMES)
        .filter(t -> !t.dark())
        .map(ThemeDetails::name)
        .sorted()
        .forEach(lm::addElement);
    lightThemesListModel = lm;

    AbeillePanel panel = new AbeillePanel(new PreferencesDialogView().getRootComponent());

    JButton okButton = (JButton) panel.getButton("okButton");
    getRootPane().setDefaultButton(okButton);
    okButton.addActionListener(
        e -> {
          boolean close = true;
          if (close) {
            setVisible(false);
            dispose();
          }
          new MapToolEventBus().getMainEventBus().post(new PreferencesChanged());
          if (ThemeSupport.needsRestartForNewTheme()) {
            MapTool.showMessage(
                "PreferencesDialog.themeChangeWarning",
                "PreferencesDialog.themeChangeWarningTitle",
                JOptionPane.WARNING_MESSAGE);
          }
        });

    tabbedPane = panel.getTabbedPane("TabPane");

    forceFacingArrowCheckBox = panel.getCheckBox("forceFacingArrow");
    showStatSheetCheckBox = panel.getCheckBox("showStatSheet");
    showPortraitCheckBox = panel.getCheckBox("showPortrait");
    showStatSheetModifierCheckBox = panel.getCheckBox("showStatSheetModifier");
    showNumberingCombo = panel.getComboBox("showNumberingCombo");
    saveReminderCheckBox = panel.getCheckBox("saveReminderCheckBox");
    fillSelectionCheckBox = panel.getCheckBox("fillSelectionCheckBox");
    frameRateCapTextField = panel.getTextField("frameRateCapTextField");
    renderPerformanceComboBox = panel.getComboBox("renderPerformanceComboBox");

    defaultUsername = panel.getTextField("defaultUsername");
    autoSaveSpinner = panel.getSpinner("autoSaveSpinner");
    duplicateTokenCombo = panel.getComboBox("duplicateTokenCombo");
    tokenNamingCombo = panel.getComboBox("tokenNamingCombo");
    newMapsHaveFOWCheckBox = panel.getCheckBox("newMapsHaveFOWCheckBox");
    tokensPopupWarningWhenDeletedCheckBox =
        panel.getCheckBox("tokensPopupWarningWhenDeletedCheckBox"); // new
    tokensStartSnapToGridCheckBox = panel.getCheckBox("tokensStartSnapToGridCheckBox");
    tokensSnapWhileDraggingCheckBox = panel.getCheckBox("tokensSnapWhileDragging");
    hideMousePointerWhileDraggingCheckBox = panel.getCheckBox("hideMousePointerWhileDragging");
    hideTokenStackIndicatorCheckBox = panel.getCheckBox("hideTokenStackIndicator");
    newMapsVisibleCheckBox = panel.getCheckBox("newMapsVisibleCheckBox");
    newTokensVisibleCheckBox = panel.getCheckBox("newTokensVisibleCheckBox");
    stampsStartFreeSizeCheckBox = panel.getCheckBox("stampsStartFreeSize");
    tokensStartFreeSizeCheckBox = panel.getCheckBox("tokensStartFreeSize");
    stampsStartSnapToGridCheckBox = panel.getCheckBox("stampsStartSnapToGrid");
    backgroundsStartFreeSizeCheckBox = panel.getCheckBox("backgroundsStartFreeSize");
    backgroundsStartSnapToGridCheckBox = panel.getCheckBox("backgroundsStartSnapToGrid");
    defaultGridTypeCombo = panel.getComboBox("defaultGridTypeCombo");
    defaultGridSizeTextField = panel.getTextField("defaultGridSize");
    defaultUnitsPerCellTextField = panel.getTextField("defaultUnitsPerCell");
    defaultVisionDistanceTextField = panel.getTextField("defaultVisionDistance");
    statsheetPortraitSize = panel.getTextField("statsheetPortraitSize");
    fontSizeTextField = panel.getTextField("fontSize");

    haloLineWidthSpinner = panel.getSpinner("haloLineWidthSpinner");
    haloOverlayOpacitySpinner = panel.getSpinner("haloOverlayOpacitySpinner");
    auraOverlayOpacitySpinner = panel.getSpinner("auraOverlayOpacitySpinner");
    lightOverlayOpacitySpinner = panel.getSpinner("lightOverlayOpacitySpinner");
    lumensOverlayOpacitySpinner = panel.getSpinner("lumensOverlayOpacitySpinner");
    lumensOverlayBorderThicknessSpinner = panel.getSpinner("lumensOverlayBorderThicknessSpinner");
    lumensOverlayShowByDefaultCheckBox = panel.getCheckBox("lumensOverlayShowByDefaultCheckBox");
    lightsShowByDefaultCheckBox = panel.getCheckBox("lightsShowByDefaultCheckBox");
    fogOverlayOpacitySpinner = panel.getSpinner("fogOverlayOpacitySpinner");
    mapVisibilityWarning = panel.getCheckBox("mapVisibilityWarning");

    useHaloColorAsVisionOverlayCheckBox = panel.getCheckBox("useHaloColorAsVisionOverlayCheckBox");
    autoRevealVisionOnGMMoveCheckBox = panel.getCheckBox("autoRevealVisionOnGMMoveCheckBox");
    showSmiliesCheckBox = panel.getCheckBox("showSmiliesCheckBox");
    playSystemSoundCheckBox = panel.getCheckBox("playSystemSounds");
    playStreamsCheckBox = panel.getCheckBox("playStreams");
    playSystemSoundOnlyWhenNotFocusedCheckBox = panel.getCheckBox("soundsOnlyWhenNotFocused");
    syrinscapeActiveCheckBox = panel.getCheckBox("syrinscapeActive");
    showAvatarInChat = panel.getCheckBox("showChatAvatar");
    showDialogOnNewToken = panel.getCheckBox("showDialogOnNewToken");
    visionTypeCombo = panel.getComboBox("defaultVisionType");
    mapSortType = panel.getComboBox("mapSortType");
    movementMetricCombo = panel.getComboBox("movementMetric");
    allowPlayerMacroEditsDefault = panel.getCheckBox("allowPlayerMacroEditsDefault");
    toolTipInlineRolls = panel.getCheckBox("toolTipInlineRolls");
    suppressToolTipsMacroLinks = panel.getCheckBox("suppressToolTipsMacroLinks");
    trustedOutputForeground = (ColorWell) panel.getComponent("trustedOuputForeground");
    trustedOutputBackground = (ColorWell) panel.getComponent("trustedOuputBackground");
    toolTipInitialDelay = panel.getTextField("toolTipInitialDelay");
    toolTipDismissDelay = panel.getTextField("toolTipDismissDelay");
    facingFaceEdges = panel.getCheckBox("facingFaceEdges");
    facingFaceVertices = panel.getCheckBox("facingFaceVertices");

    chatNotificationColor = (ColorWell) panel.getComponent("chatNotificationColor");
    chatNotificationShowBackground = panel.getCheckBox("chatNotificationShowBackground");

    chatAutosaveTime = panel.getSpinner("chatAutosaveTime");
    chatFilenameFormat = panel.getTextField("chatFilenameFormat");

    macroEditorThemeCombo = panel.getComboBox("macroEditorThemeCombo");
    iconThemeCombo = panel.getComboBox("iconThemeCombo");

    fitGMView = panel.getCheckBox("fitGMView");
    hideNPCs = panel.getCheckBox("hideNPCs");
    ownerPermissions = panel.getCheckBox("ownerPermission");
    lockMovement = panel.getCheckBox("lockMovement");
    showInitGainMessage = panel.getCheckBox("showInitGainMessage");
    upnpDiscoveryTimeoutTextField = panel.getTextField("upnpDiscoveryTimeoutTextField");
    typingNotificationDuration = panel.getSpinner("typingNotificationDuration");
    allowExternalMacroAccessCheckBox = panel.getCheckBox("allowExternalMacroAccessCheckBox");
    fileSyncPath = panel.getTextField("fileSyncPath");
    fileSyncPathButton = (JButton) panel.getButton("fileSyncPathButton");
    loadMRUcheckbox = (JCheckBox) panel.getCheckBox("loadMRUcheckbox");
    final var installDirTextField = (JTextField) panel.getComponent("InstallDirTextField");
    installDirTextField.setText(AppUtil.getInstallDirectory().toString());

    publicKeyTextArea = (JTextArea) panel.getTextComponent("publicKeyTextArea");
    regeneratePublicKey = (JButton) panel.getButton("regeneratePublicKey");
    copyPublicKey = (JButton) panel.getButton("copyKey");

    themeList = (JList<String>) panel.getList("themeList");
    themeImageLabel = (JLabel) panel.getComponent("themeImage");
    themeNameLabel = (JLabel) panel.getComponent("currentThemeName");
    useThemeForChat = (JCheckBox) panel.getComponent("useThemeForChat");
    themeFilterCombo = panel.getComboBox("themeFilterCombo");

    jvmXmxTextField = panel.getTextField("jvmXmxTextField");
    jvmXmxTextField.setToolTipText(I18N.getText("prefs.jvm.xmx.tooltip"));
    jvmXmsTextField = panel.getTextField("jvmXmsTextField");
    jvmXmsTextField.setToolTipText(I18N.getText("prefs.jvm.xms.tooltip"));
    jvmXssTextField = panel.getTextField("jvmXssTextField");
    jvmXssTextField.setToolTipText(I18N.getText("prefs.jvm.xss.tooltip"));

    dataDirTextField = panel.getTextField("dataDirTextField");

    jvmDirect3dCheckbox = panel.getCheckBox("jvmDirect3dCheckbox");
    jvmDirect3dCheckbox.setToolTipText(I18N.getText("prefs.jvm.advanced.direct3d.tooltip"));

    jvmOpenGLCheckbox = panel.getCheckBox("jvmOpenGLCheckbox");
    jvmOpenGLCheckbox.setToolTipText(I18N.getText("prefs.jvm.advanced.opengl.tooltip"));

    jvmInitAwtCheckbox = panel.getCheckBox("jvmInitAwtCheckbox");
    jvmInitAwtCheckbox.setToolTipText(I18N.getText("prefs.jvm.advanced.initAWTbeforeJFX.tooltip"));

    jamLanguageOverrideComboBox = panel.getComboBox("jvmLanguageOverideComboBox");
    jamLanguageOverrideComboBox.setToolTipText(I18N.getText("prefs.language.override.tooltip"));

    startupInfoLabel = panel.getLabel("startupInfoLabel");

    pcTokenLabelFG = (ColorWell) panel.getComponent("pcTokenLabelFG");
    pcTokenLabelFG.setColor(AppPreferences.getPCMapLabelFG());
    pcTokenLabelBG = (ColorWell) panel.getComponent("pcTokenLabelBG");
    pcTokenLabelBG.setColor(AppPreferences.getPCMapLabelBG());
    pcTokenLabelBorderColor = (ColorWell) panel.getComponent("pcTokenLabelBorder");
    pcTokenLabelBorderColor.setColor(AppPreferences.getPCMapLabelBorder());
    npcTokenLabelFG = (ColorWell) panel.getComponent("npcTokenLabelFG");
    npcTokenLabelFG.setColor(AppPreferences.getNPCMapLabelFG());
    npcTokenLabelBG = (ColorWell) panel.getComponent("npcTokenLabelBG");
    npcTokenLabelBG.setColor(AppPreferences.getNPCMapLabelBG());
    npcTokenLabelBorderColor = (ColorWell) panel.getComponent("npcTokenLabelBorder");
    npcTokenLabelBorderColor.setColor(AppPreferences.getNPCMapLabelBorder());
    nonVisTokenLabelFG = (ColorWell) panel.getComponent("nonVisTokenLabelFG");
    nonVisTokenLabelFG.setColor(AppPreferences.getNonVisMapLabelFG());
    nonVisTokenLabelBG = (ColorWell) panel.getComponent("nonVisTokenLabelBG");
    nonVisTokenLabelBG.setColor(AppPreferences.getNonVisMapLabelBG());
    nonVisTokenLabelBorderColor = (ColorWell) panel.getComponent("nonVisTokenLabelBorder");
    nonVisTokenLabelBorderColor.setColor(AppPreferences.getNonVisMapLabelBorder());

    labelFontSizeSpinner = (JSpinner) panel.getComponent("labelFontSizeSpinner");
    labelFontSizeSpinner.setValue(AppPreferences.getMapLabelFontSize());
    labelBorderWidthSpinner = (JSpinner) panel.getComponent("labelBorderWidthSpinner");
    labelBorderWidthSpinner.setValue(AppPreferences.getMapLabelBorderWidth());
    labelBorderArcSpinner = (JSpinner) panel.getComponent("labelBorderArcSpinner");
    labelBorderArcSpinner.setValue(AppPreferences.getMapLabelBorderArc());
    showLabelBorderCheckBox = (JCheckBox) panel.getComponent("showLabelBorder");
    showLabelBorderCheckBox.addActionListener(
        e -> {
          if (showLabelBorderCheckBox.isSelected()) {
            pcTokenLabelBorderColor.setVisible(true); // Disabling a color well does not work
            npcTokenLabelBorderColor.setVisible(true); // Disabling a color well does not work
            nonVisTokenLabelBorderColor.setVisible(true); // Disabling a color well does not work
            labelBorderWidthSpinner.setEnabled(true);
            labelBorderArcSpinner.setEnabled(true);
            AppPreferences.setShowMapLabelBorder(true);
          } else {
            pcTokenLabelBorderColor.setVisible(false); // Disabling a color well does not work
            npcTokenLabelBorderColor.setVisible(false); // Disabling a color well does not work
            nonVisTokenLabelBorderColor.setVisible(false); // Disabling a color well does not  work
            labelBorderWidthSpinner.setEnabled(false);
            labelBorderArcSpinner.setEnabled(false);
            AppPreferences.setShowMapLabelBorder(false);
          }
        });

    boolean showBorder = AppPreferences.getShowMapLabelBorder();
    showLabelBorderCheckBox.setSelected(showBorder);
    if (showBorder) {
      pcTokenLabelBorderColor.setVisible(true);
      npcTokenLabelBorderColor.setVisible(true);
      nonVisTokenLabelBorderColor.setVisible(true);
      labelBorderWidthSpinner.setEnabled(true);
      labelBorderArcSpinner.setEnabled(true);
    } else {
      pcTokenLabelBorderColor.setVisible(false);
      npcTokenLabelBorderColor.setVisible(false);
      nonVisTokenLabelBorderColor.setVisible(false);
      labelBorderWidthSpinner.setEnabled(false);
      labelBorderArcSpinner.setEnabled(false);
    }

    {
      final var developerOptionToggles = (JPanel) panel.getComponent("developerOptionToggles");
      final var developerLayout = developerOptionToggles.getLayout();

      final var labelConstraints = new GridBagConstraints();
      labelConstraints.insets = new Insets(6, 0, 6, 5);
      labelConstraints.gridx = 0;
      labelConstraints.gridy = 0;
      labelConstraints.weightx = 0.;
      labelConstraints.weighty = 1.;
      labelConstraints.fill = GridBagConstraints.HORIZONTAL;

      final var checkboxConstraints = new GridBagConstraints();
      checkboxConstraints.insets = new Insets(6, 5, 6, 0);
      checkboxConstraints.gridx = 1;
      checkboxConstraints.gridy = 0;
      checkboxConstraints.weightx = 0.;
      checkboxConstraints.weighty = 1.;
      checkboxConstraints.fill = GridBagConstraints.HORIZONTAL;

      for (final var option : DeveloperOptions.Toggle.values()) {
        labelConstraints.gridy += 1;
        checkboxConstraints.gridy += 1;

        final var label = new JLabel(option.getLabel());
        label.setToolTipText(option.getTooltip());
        label.setHorizontalAlignment(SwingConstants.LEADING);
        label.setHorizontalTextPosition(SwingConstants.TRAILING);

        final var checkbox = new JCheckBox();
        checkbox.setName(option.getKey());
        checkbox.setModel(new DeveloperToggleModel(option));
        checkbox.addActionListener(e -> option.setEnabled(!checkbox.isSelected()));

        label.setLabelFor(checkbox);

        developerOptionToggles.add(label, labelConstraints);
        developerOptionToggles.add(checkbox, checkboxConstraints);
      }
    }

    File appCfgFile = AppUtil.getAppCfgFile();
    String copyInfo = "";
    if (appCfgFile != null) { // Don't try to display message if running from dev.
      copyInfo = I18N.getText("startup.preferences.info.manualCopy", appCfgFile.toString());
    }
    String startupInfoMsg = I18N.getText("startup.preferences.info", copyInfo);
    startupInfoLabel.setText(startupInfoMsg);

    DefaultComboBoxModel<String> languageModel = new DefaultComboBoxModel<String>();
    languageModel.addAll(getLanguages());
    jamLanguageOverrideComboBox.setModel(languageModel);

    setInitialState();

    // And keep it updated
    facingFaceEdges.addActionListener(
        e -> {
          AppPreferences.setFaceEdge(facingFaceEdges.isSelected());
        });
    facingFaceVertices.addActionListener(
        e -> {
          AppPreferences.setFaceVertex(facingFaceVertices.isSelected());
        });

    toolTipInlineRolls.addActionListener(
        e -> AppPreferences.setUseToolTipForInlineRoll(toolTipInlineRolls.isSelected()));

    suppressToolTipsMacroLinks.addActionListener(
        e ->
            AppPreferences.setSuppressToolTipsForMacroLinks(
                suppressToolTipsMacroLinks.isSelected()));

    toolTipInitialDelay
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(toolTipInitialDelay) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setToolTipInitialDelay(value);
                ToolTipManager.sharedInstance().setInitialDelay(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });
    toolTipDismissDelay
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(toolTipDismissDelay) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setToolTipDismissDelay(value);
                ToolTipManager.sharedInstance().setDismissDelay(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });

    chatNotificationColor.addActionListener(
        e -> {
          AppPreferences.setChatNotificationColor(chatNotificationColor.getColor());
          MapTool.getFrame().setChatTypingLabelColor(AppPreferences.getChatNotificationColor());
        });

    trustedOutputForeground.addActionListener(
        e -> {
          AppPreferences.setTrustedPrefixFG(trustedOutputForeground.getColor());
          MapTool.getFrame()
              .getCommandPanel()
              .setTrustedMacroPrefixColors(
                  AppPreferences.getTrustedPrefixFG(), AppPreferences.getTrustedPrefixBG());
        });
    trustedOutputBackground.addActionListener(
        e -> {
          AppPreferences.setTrustedPrefixBG(trustedOutputBackground.getColor());
          MapTool.getFrame()
              .getCommandPanel()
              .setTrustedMacroPrefixColors(
                  AppPreferences.getTrustedPrefixFG(), AppPreferences.getTrustedPrefixBG());
        });

    chatAutosaveTime.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setChatAutosaveTime(value);
          }
        });
    typingNotificationDuration.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setTypingNotificationDuration(value);
          }
        });

    chatFilenameFormat.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              StringBuilder saveFile = new StringBuilder(chatFilenameFormat.getText());
              if (saveFile.indexOf(".") < 0) {
                saveFile.append(".html");
              }
              AppPreferences.setChatFilenameFormat(saveFile.toString());
            }
          }
        });

    allowPlayerMacroEditsDefault.addActionListener(
        e ->
            AppPreferences.setAllowPlayerMacroEditsDefault(
                allowPlayerMacroEditsDefault.isSelected()));

    showAvatarInChat.addActionListener(
        e -> AppPreferences.setShowAvatarInChat(showAvatarInChat.isSelected()));
    saveReminderCheckBox.addActionListener(
        e -> AppPreferences.setSaveReminder(saveReminderCheckBox.isSelected()));
    fillSelectionCheckBox.addActionListener(
        e -> AppPreferences.setFillSelectionBox(fillSelectionCheckBox.isSelected()));
    frameRateCapTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(frameRateCapTextField) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setFrameRateCap(value);

                // AppPreferences may have rejected the value, so read it back.
                final var cap = AppPreferences.getFrameRateCap();
                for (final var renderer : MapTool.getFrame().getZoneRenderers()) {
                  renderer.setFrameRateCap(cap);
                }
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                final var result = StringUtil.parseInteger(value);
                if (result <= 0) {
                  throw new ParseException("Frame rate cap must be positive", 0);
                }
                return result;
              }
            });

    renderPerformanceComboBox.setModel(
        getLocalizedModel(renderPerformanceComboItems, AppPreferences.getRenderQuality().name()));
    renderPerformanceComboBox.addItemListener(
        e -> {
          AppPreferences.setRenderQuality(
              RenderQuality.valueOf(
                  ((LocalizedComboItem) renderPerformanceComboBox.getSelectedItem()).getValue()));
        });

    defaultUsername.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              StringBuilder userName = new StringBuilder(defaultUsername.getText());
              AppPreferences.setDefaultUserName(userName.toString());
            }
          }
        });

    loadMRUcheckbox.addActionListener(
        e -> AppPreferences.setLoadMRUCampaignAtStart(loadMRUcheckbox.isSelected()));
    allowExternalMacroAccessCheckBox.addActionListener(
        e ->
            AppPreferences.setAllowExternalMacroAccess(
                allowExternalMacroAccessCheckBox.isSelected()));
    showDialogOnNewToken.addActionListener(
        e -> AppPreferences.setShowDialogOnNewToken(showDialogOnNewToken.isSelected()));
    autoSaveSpinner.addChangeListener(
        ce -> {
          int newInterval = (Integer) autoSaveSpinner.getValue();
          AppPreferences.setAutoSaveIncrement(newInterval);
        });
    newMapsHaveFOWCheckBox.addActionListener(
        e -> AppPreferences.setNewMapsHaveFOW(newMapsHaveFOWCheckBox.isSelected()));
    tokensPopupWarningWhenDeletedCheckBox.addActionListener(
        e ->
            AppPreferences.setTokensWarnWhenDeleted(
                tokensPopupWarningWhenDeletedCheckBox.isSelected()));
    tokensStartSnapToGridCheckBox.addActionListener(
        e -> AppPreferences.setTokensStartSnapToGrid(tokensStartSnapToGridCheckBox.isSelected()));
    tokensSnapWhileDraggingCheckBox.addActionListener(
        e ->
            AppPreferences.setTokensSnapWhileDragging(
                tokensSnapWhileDraggingCheckBox.isSelected()));
    hideMousePointerWhileDraggingCheckBox.addActionListener(
        e ->
            AppPreferences.setHideMousePointerWhileDragging(
                hideMousePointerWhileDraggingCheckBox.isSelected()));
    hideTokenStackIndicatorCheckBox.addActionListener(
        e ->
            AppPreferences.setHideTokenStackIndicator(
                hideTokenStackIndicatorCheckBox.isSelected()));
    newMapsVisibleCheckBox.addActionListener(
        e -> AppPreferences.setNewMapsVisible(newMapsVisibleCheckBox.isSelected()));
    newTokensVisibleCheckBox.addActionListener(
        e -> AppPreferences.setNewTokensVisible(newTokensVisibleCheckBox.isSelected()));
    stampsStartFreeSizeCheckBox.addActionListener(
        e -> AppPreferences.setObjectsStartFreesize(stampsStartFreeSizeCheckBox.isSelected()));
    tokensStartFreeSizeCheckBox.addActionListener(
        e -> AppPreferences.setTokensStartFreesize(tokensStartFreeSizeCheckBox.isSelected()));
    stampsStartSnapToGridCheckBox.addActionListener(
        e -> AppPreferences.setObjectsStartSnapToGrid(stampsStartSnapToGridCheckBox.isSelected()));
    showStatSheetCheckBox.addActionListener(
        e -> AppPreferences.setShowStatSheet(showStatSheetCheckBox.isSelected()));
    showPortraitCheckBox.addActionListener(
        e -> AppPreferences.setShowPortrait(showPortraitCheckBox.isSelected()));
    showStatSheetModifierCheckBox.addActionListener(
        e -> AppPreferences.setShowStatSheetModifier(showStatSheetModifierCheckBox.isSelected()));
    forceFacingArrowCheckBox.addActionListener(
        e -> AppPreferences.setForceFacingArrow(forceFacingArrowCheckBox.isSelected()));
    backgroundsStartFreeSizeCheckBox.addActionListener(
        e ->
            AppPreferences.setBackgroundsStartFreesize(
                backgroundsStartFreeSizeCheckBox.isSelected()));
    backgroundsStartSnapToGridCheckBox.addActionListener(
        e ->
            AppPreferences.setBackgroundsStartSnapToGrid(
                backgroundsStartSnapToGridCheckBox.isSelected()));
    defaultGridSizeTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(defaultGridSizeTextField) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setDefaultGridSize(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });

    defaultUnitsPerCellTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Double>(defaultUnitsPerCellTextField) {
              @Override
              protected void storeNumericValue(Double value) {
                AppPreferences.setDefaultUnitsPerCell(value);
              }

              @Override
              protected Double convertString(String value) throws ParseException {
                return StringUtil.parseDecimal(value);
              }
            });
    defaultVisionDistanceTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(defaultVisionDistanceTextField) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setDefaultVisionDistance(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });
    statsheetPortraitSize
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(statsheetPortraitSize) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setPortraitSize(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });
    haloLineWidthSpinner.addChangeListener(
        ce -> AppPreferences.setHaloLineWidth((Integer) haloLineWidthSpinner.getValue()));

    // Overlay opacity options in AppPreferences, with
    // error checking to ensure values are within the acceptable range
    // of 0 and 255.
    haloOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setHaloOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
    auraOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setAuraOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
    lightOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setLightOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
    lumensOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setLumensOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
    lumensOverlayBorderThicknessSpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setLumensOverlayBorderThickness(value);
            MapTool.getFrame().refresh();
          }
        });
    lumensOverlayShowByDefaultCheckBox.addActionListener(
        e ->
            AppPreferences.setLumensOverlayShowByDefault(
                lumensOverlayShowByDefaultCheckBox.isSelected()));
    lightsShowByDefaultCheckBox.addActionListener(
        e -> AppPreferences.setLightsShowByDefault(lightsShowByDefaultCheckBox.isSelected()));
    fogOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setFogOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
    useHaloColorAsVisionOverlayCheckBox.addActionListener(
        e ->
            AppPreferences.setUseHaloColorOnVisionOverlay(
                useHaloColorAsVisionOverlayCheckBox.isSelected()));
    autoRevealVisionOnGMMoveCheckBox.addActionListener(
        e ->
            AppPreferences.setAutoRevealVisionOnGMMovement(
                autoRevealVisionOnGMMoveCheckBox.isSelected()));
    showSmiliesCheckBox.addActionListener(
        e -> AppPreferences.setShowSmilies(showSmiliesCheckBox.isSelected()));
    playSystemSoundCheckBox.addActionListener(
        e -> AppPreferences.setPlaySystemSounds(playSystemSoundCheckBox.isSelected()));
    mapVisibilityWarning.addActionListener(
        e -> AppPreferences.setMapVisibilityWarning(mapVisibilityWarning.isSelected()));

    playStreamsCheckBox.addActionListener(
        e -> {
          AppPreferences.setPlayStreams(playStreamsCheckBox.isSelected());
          if (!playStreamsCheckBox.isSelected()) {
            MediaPlayerAdapter.stopStream("*", true, 0);
          }
        });

    playSystemSoundOnlyWhenNotFocusedCheckBox.addActionListener(
        e ->
            AppPreferences.setPlaySystemSoundsOnlyWhenNotFocused(
                playSystemSoundOnlyWhenNotFocusedCheckBox.isSelected()));

    syrinscapeActiveCheckBox.addActionListener(
        e -> AppPreferences.setSyrinscapeActive(syrinscapeActiveCheckBox.isSelected()));

    fontSizeTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(fontSizeTextField) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setFontSize(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });

    npcTokenLabelBG.addActionListener(
        e -> {
          AppPreferences.setNPCMapLabelBG(npcTokenLabelBG.getColor());
        });

    npcTokenLabelFG.addActionListener(
        e -> {
          AppPreferences.setNPCMapLabelFG(npcTokenLabelFG.getColor());
        });

    pcTokenLabelBG.addActionListener(
        e -> {
          AppPreferences.setPCMapLabelBG(pcTokenLabelBG.getColor());
        });

    pcTokenLabelFG.addActionListener(
        e -> {
          AppPreferences.setPCMapLabelFG(pcTokenLabelFG.getColor());
        });

    nonVisTokenLabelBG.addActionListener(
        e -> {
          AppPreferences.setNonVisMapLabelBG(nonVisTokenLabelBG.getColor());
        });

    nonVisTokenLabelFG.addActionListener(
        e -> {
          AppPreferences.setNonVisMapLabelFG(nonVisTokenLabelFG.getColor());
        });

    labelFontSizeSpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setMapLabelFontSize(Math.max(value, 0));
          }
        });

    pcTokenLabelBorderColor.addActionListener(
        e -> {
          AppPreferences.setPCMapLabelBorder(pcTokenLabelBorderColor.getColor());
        });

    npcTokenLabelBorderColor.addActionListener(
        e -> {
          AppPreferences.setNPCMapLabelBorder(npcTokenLabelBorderColor.getColor());
        });

    nonVisTokenLabelBorderColor.addActionListener(
        e -> {
          AppPreferences.setNonVisMapLabelBorder(nonVisTokenLabelBorderColor.getColor());
        });

    labelBorderWidthSpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setMapLabelBorderWidth(Math.max(value, 0));
          }
        });

    labelBorderArcSpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setMapLabelBorderArc(Math.max(value, 0));
          }
        });

    fitGMView.addActionListener(e -> AppPreferences.setFitGMView(fitGMView.isSelected()));
    hideNPCs.addActionListener(e -> AppPreferences.setInitHideNpcs(hideNPCs.isSelected()));
    ownerPermissions.addActionListener(
        e -> AppPreferences.setInitOwnerPermissions(ownerPermissions.isSelected()));
    lockMovement.addActionListener(
        e -> AppPreferences.setInitLockMovement(lockMovement.isSelected()));
    showInitGainMessage.addActionListener(
        e -> AppPreferences.setShowInitGainMessage(showInitGainMessage.isSelected()));
    upnpDiscoveryTimeoutTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy<Integer>(upnpDiscoveryTimeoutTextField) {
              @Override
              protected void storeNumericValue(Integer value) {
                AppPreferences.setUpnpDiscoveryTimeout(value);
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
            });
    fileSyncPathButton.addActionListener(
        e -> {
          JFileChooser fileChooser = new JFileChooser(AppPreferences.getFileSyncPath());
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          int returnVal = fileChooser.showOpenDialog(null);

          if (returnVal == JFileChooser.APPROVE_OPTION) {
            String selectedPath = fileChooser.getSelectedFile().getPath();

            // Set the text field
            fileSyncPath.setText(selectedPath);
            fileSyncPath.setCaretPosition(0);

            // Save to preferences
            AppPreferences.setFileSyncPath(selectedPath);
          }
        });
    jvmXmxTextField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              String jvmXmx = jvmXmxTextField.getText().trim();

              if (UserJvmOptions.verifyJvmOptions(jvmXmx)) {
                setJvmOption(JVM_OPTION.MAX_MEM, jvmXmx);
              } else {
                jvmXmxTextField.setText(JVM_OPTION.MAX_MEM.getDefaultValue());
                setJvmOption(JVM_OPTION.MAX_MEM, JVM_OPTION.MAX_MEM.getDefaultValue());
                log.warn("Invalid JVM Xmx parameter entered: " + jvmXmx);
              }
              jvmValuesChanged = true;
            }
          }
        });
    jvmXmsTextField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              String jvmXms = jvmXmsTextField.getText().trim();

              if (UserJvmOptions.verifyJvmOptions(jvmXms)) {
                setJvmOption(JVM_OPTION.MIN_MEM, jvmXms);
              } else {
                jvmXmsTextField.setText(JVM_OPTION.MIN_MEM.getDefaultValue());
                setJvmOption(JVM_OPTION.MIN_MEM, JVM_OPTION.MIN_MEM.getDefaultValue());
                log.warn("Invalid JVM Xms parameter entered: " + jvmXms);
              }
              jvmValuesChanged = true;
            }
          }
        });
    jvmXssTextField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              String jvmXss = jvmXssTextField.getText().trim();

              if (UserJvmOptions.verifyJvmOptions(jvmXss)) {
                setJvmOption(JVM_OPTION.STACK_SIZE, jvmXss);
              } else {
                jvmXssTextField.setText(JVM_OPTION.STACK_SIZE.getDefaultValue());
                setJvmOption(JVM_OPTION.STACK_SIZE, JVM_OPTION.STACK_SIZE.getDefaultValue());
                log.warn("Invalid JVM Xss parameter entered: " + jvmXss);
              }
              jvmValuesChanged = true;
            }
          }
        });
    dataDirTextField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              setJvmOption(JVM_OPTION.DATA_DIR, dataDirTextField.getText().trim());
              jvmValuesChanged = true;
            }
          }
        });
    jvmDirect3dCheckbox.addActionListener(
        e -> {
          setJvmOption(JVM_OPTION.JAVA2D_D3D, jvmDirect3dCheckbox.isSelected());
          jvmValuesChanged = true;
        });
    jvmOpenGLCheckbox.addActionListener(
        e -> {
          setJvmOption(JVM_OPTION.JAVA2D_OPENGL_OPTION, jvmOpenGLCheckbox.isSelected());
          jvmValuesChanged = true;
        });
    jvmInitAwtCheckbox.addActionListener(
        e -> {
          setJvmOption(JVM_OPTION.MACOSX_EMBEDDED_OPTION, jvmInitAwtCheckbox.isSelected());
          jvmValuesChanged = true;
        });

    jamLanguageOverrideComboBox.addItemListener(
        e -> {
          setJvmOption(
              JVM_OPTION.LOCALE_LANGUAGE,
              Objects.requireNonNull(jamLanguageOverrideComboBox.getSelectedItem()).toString());
          jvmValuesChanged = true;
        });

    chatNotificationShowBackground.addActionListener(
        e ->
            AppPreferences.setChatNotificationShowBackground(
                chatNotificationShowBackground.isSelected()));

    defaultGridTypeCombo.setModel(
        getLocalizedModel(defaultGridTypeComboItems, AppPreferences.getDefaultGridType()));
    defaultGridTypeCombo.addItemListener(
        e ->
            AppPreferences.setDefaultGridType(
                ((LocalizedComboItem) (defaultGridTypeCombo.getSelectedItem())).getValue()));

    duplicateTokenCombo.setModel(
        getLocalizedModel(duplicateTokenComboItems, AppPreferences.getDuplicateTokenNumber()));
    duplicateTokenCombo.addItemListener(
        e ->
            AppPreferences.setDuplicateTokenNumber(
                ((LocalizedComboItem) (duplicateTokenCombo.getSelectedItem())).getValue()));

    showNumberingCombo.setModel(
        getLocalizedModel(showNumberingComboItems, AppPreferences.getTokenNumberDisplay()));
    showNumberingCombo.addItemListener(
        e ->
            AppPreferences.setTokenNumberDisplay(
                ((LocalizedComboItem) showNumberingCombo.getSelectedItem()).getValue()));

    tokenNamingCombo.setModel(
        getLocalizedModel(tokenNamingComboItems, AppPreferences.getNewTokenNaming()));
    tokenNamingCombo.addItemListener(
        e ->
            AppPreferences.setNewTokenNaming(
                ((LocalizedComboItem) (tokenNamingCombo.getSelectedItem())).getValue()));

    movementMetricCombo.setModel(new DefaultComboBoxModel<>(movementMetricComboItems));
    movementMetricCombo.setSelectedItem(AppPreferences.getMovementMetric());
    movementMetricCombo.addItemListener(
        e ->
            AppPreferences.setMovementMetric((WalkerMetric) movementMetricCombo.getSelectedItem()));

    visionTypeCombo.setModel(new DefaultComboBoxModel<>(Zone.VisionType.values()));
    visionTypeCombo.setSelectedItem(AppPreferences.getDefaultVisionType());
    visionTypeCombo.addItemListener(
        e ->
            AppPreferences.setDefaultVisionType(
                (Zone.VisionType) visionTypeCombo.getSelectedItem()));

    mapSortType.setModel(new DefaultComboBoxModel<>(AppPreferences.MapSortType.values()));
    mapSortType.setSelectedItem(AppPreferences.getMapSortType());
    mapSortType.addItemListener(
        e ->
            AppPreferences.setMapSortType(
                (AppPreferences.MapSortType) mapSortType.getSelectedItem()));

    macroEditorThemeCombo.setModel(new DefaultComboBoxModel<>());
    try (Stream<Path> paths = Files.list(AppConstants.THEMES_DIR.toPath())) {
      paths
          .filter(Files::isRegularFile)
          .filter(p -> p.toString().toLowerCase().endsWith(".xml"))
          .forEach(
              p ->
                  macroEditorThemeCombo.addItem(
                      FilenameUtils.removeExtension(p.getFileName().toString())));
      macroEditorThemeCombo.setSelectedItem(AppPreferences.getDefaultMacroEditorTheme());
    } catch (IOException ioe) {
      log.warn("Unable to list macro editor themes.", ioe);
      macroEditorThemeCombo.addItem("Default");
    }
    macroEditorThemeCombo.addItemListener(
        e ->
            AppPreferences.setDefaultMacroEditorTheme(
                (String) macroEditorThemeCombo.getSelectedItem()));

    iconThemeCombo.setModel(new DefaultComboBoxModel<>());
    iconThemeCombo.addItem(RessourceManager.CLASSIC);
    iconThemeCombo.addItem(RessourceManager.ROD_TAKEHARA);
    iconThemeCombo.setSelectedItem(AppPreferences.getIconTheme());
    iconThemeCombo.addItemListener(
        e -> AppPreferences.setIconTheme((String) iconThemeCombo.getSelectedItem()));

    themeFilterCombo.setModel(getLocalizedModel(themeFilterComboItems, "All"));
    themeFilterCombo.addItemListener(
        e -> {
          String filter = ((LocalizedComboItem) themeFilterCombo.getSelectedItem()).getValue();
          switch (filter) {
            case "All":
              themeList.setModel(allThemesListModel);
              break;
            case "Dark":
              themeList.setModel(darkThemesListModel);
              break;
            case "Light":
              themeList.setModel(lightThemesListModel);
              break;
          }
        });

    copyPublicKey.addActionListener(
        e -> {
          Toolkit.getDefaultToolkit()
              .getSystemClipboard()
              .setContents(new StringSelection(publicKeyTextArea.getText()), null);
        });

    regeneratePublicKey.addActionListener(
        e -> {
          CompletableFuture<CipherUtil.Key> keys = new PublicPrivateKeyStore().regenerateKeys();

          keys.thenAccept(
              cu -> {
                SwingUtilities.invokeLater(
                    () -> {
                      publicKeyTextArea.setText(cu.getEncodedPublicKeyText());
                    });
              });
        });

    add(panel);
    pack();
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
      themeChanged = false;
    }
    super.setVisible(b);
  }

  /**
   * Initializes and sets the initial state of various user preferences in the application. This
   * method is called during the initialization process.
   */
  private void setInitialState() {
    showDialogOnNewToken.setSelected(AppPreferences.getShowDialogOnNewToken());
    saveReminderCheckBox.setSelected(AppPreferences.getSaveReminder());
    fillSelectionCheckBox.setSelected(AppPreferences.getFillSelectionBox());
    frameRateCapTextField.setText(Integer.toString(AppPreferences.getFrameRateCap()));
    defaultUsername.setText(AppPreferences.getDefaultUserName());
    // initEnableServerSyncCheckBox.setSelected(AppPreferences.getInitEnableServerSync());
    autoSaveSpinner.setValue(AppPreferences.getAutoSaveIncrement());
    loadMRUcheckbox.setSelected(AppPreferences.getLoadMRUCampaignAtStart());
    newMapsHaveFOWCheckBox.setSelected(AppPreferences.getNewMapsHaveFOW());
    tokensPopupWarningWhenDeletedCheckBox.setSelected(AppPreferences.getTokensWarnWhenDeleted());
    tokensStartSnapToGridCheckBox.setSelected(AppPreferences.getTokensStartSnapToGrid());
    tokensSnapWhileDraggingCheckBox.setSelected(AppPreferences.getTokensSnapWhileDragging());
    hideMousePointerWhileDraggingCheckBox.setSelected(
        AppPreferences.getHideMousePointerWhileDragging());
    hideTokenStackIndicatorCheckBox.setSelected(AppPreferences.getHideTokenStackIndicator());
    newMapsVisibleCheckBox.setSelected(AppPreferences.getNewMapsVisible());
    newTokensVisibleCheckBox.setSelected(AppPreferences.getNewTokensVisible());
    stampsStartFreeSizeCheckBox.setSelected(AppPreferences.getObjectsStartFreesize());
    tokensStartFreeSizeCheckBox.setSelected(AppPreferences.getTokensStartFreesize());
    stampsStartSnapToGridCheckBox.setSelected(AppPreferences.getObjectsStartSnapToGrid());
    backgroundsStartFreeSizeCheckBox.setSelected(AppPreferences.getBackgroundsStartFreesize());
    showStatSheetCheckBox.setSelected(AppPreferences.getShowStatSheet());
    showPortraitCheckBox.setSelected(AppPreferences.getShowPortrait());
    showStatSheetModifierCheckBox.setSelected(AppPreferences.getShowStatSheetModifier());
    forceFacingArrowCheckBox.setSelected(AppPreferences.getForceFacingArrow());
    backgroundsStartSnapToGridCheckBox.setSelected(AppPreferences.getBackgroundsStartSnapToGrid());
    defaultGridSizeTextField.setText(Integer.toString(AppPreferences.getDefaultGridSize()));
    // Localizes units per cell, using the proper separator. Fixes #507.
    defaultUnitsPerCellTextField.setText(
        StringUtil.formatDecimal(AppPreferences.getDefaultUnitsPerCell(), 1));
    defaultVisionDistanceTextField.setText(
        Integer.toString(AppPreferences.getDefaultVisionDistance()));
    statsheetPortraitSize.setText(Integer.toString(AppPreferences.getPortraitSize()));
    fontSizeTextField.setText(Integer.toString(AppPreferences.getFontSize()));
    haloLineWidthSpinner.setValue(AppPreferences.getHaloLineWidth());
    mapVisibilityWarning.setSelected(AppPreferences.getMapVisibilityWarning());

    haloOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getHaloOverlayOpacity(), 0, 255, 1));
    auraOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getAuraOverlayOpacity(), 0, 255, 1));
    lightOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getLightOverlayOpacity(), 0, 255, 1));
    lumensOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getLumensOverlayOpacity(), 0, 255, 1));
    lumensOverlayBorderThicknessSpinner.setModel(
        new SpinnerNumberModel(
            AppPreferences.getLumensOverlayBorderThickness(), 0, Integer.MAX_VALUE, 1));
    lumensOverlayShowByDefaultCheckBox.setSelected(AppPreferences.getLumensOverlayShowByDefault());
    lightsShowByDefaultCheckBox.setSelected(AppPreferences.getLightsShowByDefault());
    fogOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getFogOverlayOpacity(), 0, 255, 1));

    useHaloColorAsVisionOverlayCheckBox.setSelected(
        AppPreferences.getUseHaloColorOnVisionOverlay());
    autoRevealVisionOnGMMoveCheckBox.setSelected(AppPreferences.getAutoRevealVisionOnGMMovement());
    showSmiliesCheckBox.setSelected(AppPreferences.getShowSmilies());
    playSystemSoundCheckBox.setSelected(AppPreferences.getPlaySystemSounds());
    playStreamsCheckBox.setSelected(AppPreferences.getPlayStreams());
    playSystemSoundOnlyWhenNotFocusedCheckBox.setSelected(
        AppPreferences.getPlaySystemSoundsOnlyWhenNotFocused());
    syrinscapeActiveCheckBox.setSelected(AppPreferences.getSyrinscapeActive());
    showAvatarInChat.setSelected(AppPreferences.getShowAvatarInChat());
    allowPlayerMacroEditsDefault.setSelected(AppPreferences.getAllowPlayerMacroEditsDefault());
    toolTipInlineRolls.setSelected(AppPreferences.getUseToolTipForInlineRoll());
    suppressToolTipsMacroLinks.setSelected(AppPreferences.getSuppressToolTipsForMacroLinks());
    trustedOutputForeground.setColor(AppPreferences.getTrustedPrefixFG());
    trustedOutputBackground.setColor(AppPreferences.getTrustedPrefixBG());
    toolTipInitialDelay.setText(Integer.toString(AppPreferences.getToolTipInitialDelay()));
    toolTipDismissDelay.setText(Integer.toString(AppPreferences.getToolTipDismissDelay()));
    facingFaceEdges.setSelected(AppPreferences.getFaceEdge());
    facingFaceVertices.setSelected(AppPreferences.getFaceVertex());

    chatAutosaveTime.setModel(
        new SpinnerNumberModel(AppPreferences.getChatAutosaveTime(), 0, 24 * 60, 1));
    chatFilenameFormat.setText(AppPreferences.getChatFilenameFormat());

    fitGMView.setSelected(AppPreferences.getFitGMView());
    hideNPCs.setSelected(AppPreferences.getInitHideNpcs());
    ownerPermissions.setSelected(AppPreferences.getInitOwnerPermissions());
    lockMovement.setSelected(AppPreferences.getInitLockMovement());
    showInitGainMessage.setSelected(AppPreferences.isShowInitGainMessage());
    upnpDiscoveryTimeoutTextField.setText(
        Integer.toString(AppPreferences.getUpnpDiscoveryTimeout()));
    allowExternalMacroAccessCheckBox.setSelected(AppPreferences.getAllowExternalMacroAccess());
    fileSyncPath.setText(AppPreferences.getFileSyncPath());

    // get JVM User Defaults/User override preferences
    if (!UserJvmOptions.loadAppCfg()) {
      tabbedPane.setEnabledAt(tabbedPane.indexOfTab(I18N.getString("Label.startup")), false);
    } else {
      try {

        jvmXmxTextField.setText(UserJvmOptions.getJvmOption(JVM_OPTION.MAX_MEM));
        jvmXmsTextField.setText(UserJvmOptions.getJvmOption(JVM_OPTION.MIN_MEM));
        jvmXssTextField.setText(UserJvmOptions.getJvmOption(JVM_OPTION.STACK_SIZE));
        dataDirTextField.setText(UserJvmOptions.getJvmOption(JVM_OPTION.DATA_DIR));

        jvmDirect3dCheckbox.setSelected(UserJvmOptions.hasJvmOption(JVM_OPTION.JAVA2D_D3D));
        jvmOpenGLCheckbox.setSelected(UserJvmOptions.hasJvmOption(JVM_OPTION.JAVA2D_OPENGL_OPTION));
        jvmInitAwtCheckbox.setSelected(
            UserJvmOptions.hasJvmOption(JVM_OPTION.MACOSX_EMBEDDED_OPTION));

        jamLanguageOverrideComboBox.setSelectedItem(
            UserJvmOptions.getJvmOption(JVM_OPTION.LOCALE_LANGUAGE));
      } catch (Exception e) {
        log.error("Unable to retrieve JVM user options!", e);
      }
    }

    Integer rawVal = AppPreferences.getTypingNotificationDuration();
    Integer typingVal = null;
    if (rawVal != null
        && rawVal > 99) { // backward compatibility -- used to be stored in ms, now in seconds
      double dbl = rawVal / 1000;
      if (dbl >= 1) {
        long fixedUp = Math.round(dbl);
        typingVal = (int) fixedUp;
        typingVal = typingVal > 99 ? 99 : typingVal;
      } else {
        typingVal = 1;
      }
    }
    int value = Math.abs((typingVal == null || typingVal > rawVal) ? rawVal : typingVal);
    AppPreferences.setTypingNotificationDuration(value);

    SpinnerNumberModel typingDurationModel =
        new SpinnerNumberModel((int) AppPreferences.getTypingNotificationDuration(), 0, 99, 1);
    typingNotificationDuration.setModel(typingDurationModel);

    chatNotificationColor.setColor(AppPreferences.getChatNotificationColor());
    chatNotificationShowBackground.setSelected(AppPreferences.getChatNotificationShowBackground());

    CompletableFuture<CipherUtil.Key> keys = new PublicPrivateKeyStore().getKeys();

    keys.thenAccept(
        cu -> {
          SwingUtilities.invokeLater(
              () -> {
                publicKeyTextArea.setText(cu.getEncodedPublicKeyText());
              });
        });

    themeList.setModel(allThemesListModel);
    themeList.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
    themeList.setSelectedValue(ThemeSupport.getThemeName(), true);
    SwingUtilities.invokeLater(
        () -> {
          themeImageLabel.setIcon(ThemeSupport.getExampleImageIcon(themeImageLabel.getSize()));
        });
    themeList.addListSelectionListener(
        e -> {
          if (!e.getValueIsAdjusting()) {
            String theme = themeList.getSelectedValue();
            ThemeSupport.setTheme(theme);
            themeImageLabel.setIcon(
                ThemeSupport.getExampleImageIcon(theme, themeImageLabel.getSize()));
          }
        });
    themeNameLabel.setText(ThemeSupport.getThemeName());
    useThemeForChat.setSelected(ThemeSupport.shouldUseThemeColorsForChat());
    useThemeForChat.addActionListener(
        l -> {
          ThemeSupport.setUseThemeColorsForChat(useThemeForChat.isSelected());
        });
  }

  /** Utility method to create and set the selected item for LocalizedComboItem combo box models. */
  private ComboBoxModel<LocalizedComboItem> getLocalizedModel(
      LocalizedComboItem[] items, String currPref) {
    DefaultComboBoxModel<LocalizedComboItem> model = new DefaultComboBoxModel<>(items);
    model.setSelectedItem(
        Stream.of(items).filter(i -> i.getValue().equals(currPref)).findFirst().orElse(items[0]));
    return model;
  }

  private static class DeveloperToggleModel extends DefaultButtonModel {
    private final DeveloperOptions.Toggle option;

    public DeveloperToggleModel(DeveloperOptions.Toggle option) {
      this.option = option;
    }

    @Override
    public boolean isSelected() {
      return option.isEnabled();
    }

    @Override
    public void setSelected(boolean b) {
      option.setEnabled(b);
      super.setEnabled(b);
    }
  }

  /**
   * Private abstract static class representing a proxy implementation of the DocumentListener
   * interface. This class is used to handle document changes in a JTextField and update a numeric
   * value based on the entered text.
   *
   * @param <T> The type of numeric value to handle.
   */
  private abstract static class DocumentListenerProxy<T> implements DocumentListener {

    /**
     * This variable represents a JTextField component to listen for chqnges on.
     *
     * @see JTextField
     */
    JTextField comp;

    /**
     * A proxy implementation of the DocumentListener interface. This class is used to handle
     * document changes in a JTextField and update a numeric value based on the entered text.
     */
    public DocumentListenerProxy(JTextField tf) {
      comp = tf;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
      updateValue();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
      updateValue();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
      updateValue();
    }

    /**
     * This method is used to update a numeric value based on the text entered in a JTextField
     * component. It calls the convertString() method to parse the text and convert it into the
     * appropriate numeric value. The parsed value is then passed to the storeNumericValue() method
     * to update the numeric value. If the text cannot be parsed, a ParseException is caught and
     * ignored.
     */
    protected void updateValue() {
      try {
        storeNumericValue(convertString(comp.getText())); // Localized
      } catch (ParseException nfe) {
        // Ignore it
      }
    }

    /**
     * Converts a string value to a specific type.
     *
     * @param value the string value to convert
     * @return the converted value
     * @throws ParseException if the string value cannot be converted
     */
    protected abstract T convertString(String value) throws ParseException;

    /**
     * This method is used to store a numeric value.
     *
     * @param value the numeric value to store
     */
    protected abstract void storeNumericValue(T value);
  }

  /**
   * @author frank
   */
  private abstract static class ChangeListenerProxy implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent ce) {
      JSpinner sp = (JSpinner) ce.getSource();
      int value = (Integer) sp.getValue();
      storeSpinnerValue(value);
    }

    /**
     * This method is used to store the value of a spinner. It is called when the state of the
     * spinner changes.
     *
     * @param value the new value of the spinner
     */
    protected abstract void storeSpinnerValue(int value);
  }

  /**
   * Stores the localized display name and preference value String for menu items that don't have a
   * corresponding enum.
   */
  private static class LocalizedComboItem {
    /** Represents the localized display name of a menu item or combo box option. */
    private final String displayName;

    /**
     * This variable represents a preference value. It stores a string that is used as a preference
     * value for menu items or combo box options.
     */
    private final String prefValue;

    /**
     * Creates a localized combo box item.
     *
     * @param prefValue the preference key to store.
     * @param i18nKey the i18n key to use for the display name.
     */
    LocalizedComboItem(String prefValue, String i18nKey) {
      this.prefValue = prefValue;
      displayName = I18N.getText(i18nKey);
    }

    /**
     * Creates a localized combo box item.
     *
     * @param prefValue the preference key to store.
     * @param i18nKey the i18n key to use for the display name.
     * @param args the arguments to use for the i18n key.
     */
    LocalizedComboItem(String prefValue, String i18nKey, Object... args) {
      this.prefValue = prefValue;
      displayName = I18N.getText(i18nKey, args);
    }

    /**
     * Returns the preference key value for this item.
     *
     * @return the preference key value for this item.
     */
    public String getValue() {
      return prefValue;
    }

    /**
     * Returns the localized display name of the menu item or combo box option.
     *
     * @return the localized display name
     */
    public String toString() {
      return displayName;
    }
  }
}
