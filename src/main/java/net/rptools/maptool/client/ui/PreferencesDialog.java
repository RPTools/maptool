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
package net.rptools.maptool.client.ui;

import static net.rptools.maptool.util.UserJvmOptions.getLanguages;
import static net.rptools.maptool.util.UserJvmOptions.setJvmOption;

import com.jeta.forms.components.colors.JETAColorWell;
import com.jeta.forms.components.panel.FormPanel;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.MediaPlayerAdapter;
import net.rptools.maptool.client.swing.FormPanelI18N;
import net.rptools.maptool.client.ui.theme.ThemeSupport;
import net.rptools.maptool.client.ui.theme.ThemeSupport.ThemeDetails;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Grid;
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

public class PreferencesDialog extends JDialog {

  private static final Logger log = LogManager.getLogger(PreferencesDialog.class);
  // Tabbed Panel
  private final JTabbedPane tabbedPane;
  // Interactions
  private final JCheckBox newMapsHaveFOWCheckBox;
  private final JCheckBox tokensPopupWarningWhenDeletedCheckBox;
  private final JCheckBox tokensStartSnapToGridCheckBox;
  private final JCheckBox tokensSnapWhileDraggingCheckBox;
  private final JCheckBox hideMousePointerWhileDraggingCheckBox;
  private final JCheckBox newMapsVisibleCheckBox;
  private final JCheckBox newTokensVisibleCheckBox;
  private final JCheckBox tokensStartFreeSizeCheckBox;
  private final JCheckBox stampsStartSnapToGridCheckBox;
  private final JCheckBox stampsStartFreeSizeCheckBox;
  private final JCheckBox backgroundsStartSnapToGridCheckBox;
  private final JCheckBox backgroundsStartFreeSizeCheckBox;
  private final JComboBox<LocalizedComboItem> duplicateTokenCombo;
  private final JComboBox<LocalizedComboItem> tokenNamingCombo;
  private final JComboBox<LocalizedComboItem> showNumberingCombo;
  private final JComboBox<WalkerMetric> movementMetricCombo;
  private final JComboBox<Zone.VisionType> visionTypeCombo;
  private final JComboBox<AppPreferences.MapSortType> mapSortType;
  private final JCheckBox showStatSheetCheckBox;
  private final JCheckBox showPortraitCheckBox;
  private final JCheckBox showStatSheetModifierCheckBox;
  private final JCheckBox forceFacingArrowCheckBox;
  private final JCheckBox mapVisibilityWarning;
  private final JSpinner haloLineWidthSpinner;
  private final JSpinner haloOverlayOpacitySpinner;
  private final JSpinner auraOverlayOpacitySpinner;
  private final JSpinner lightOverlayOpacitySpinner;
  private final JSpinner darknessOverlayOpacitySpinner;
  private final JSpinner fogOverlayOpacitySpinner;
  private final JCheckBox useHaloColorAsVisionOverlayCheckBox;
  private final JCheckBox autoRevealVisionOnGMMoveCheckBox;
  private final JCheckBox showSmiliesCheckBox;
  private final JCheckBox playSystemSoundCheckBox;
  private final JCheckBox playStreamsCheckBox;
  private final JCheckBox playSystemSoundOnlyWhenNotFocusedCheckBox;
  private final JCheckBox syrinscapeActiveCheckBox;
  private final JCheckBox facingFaceEdges;
  private final JCheckBox facingFaceVertices;
  private final JCheckBox showAvatarInChat;
  private final JCheckBox allowPlayerMacroEditsDefault;
  private final JCheckBox toolTipInlineRolls;
  private final JCheckBox suppressToolTipsMacroLinks;
  private final JETAColorWell trustedOuputForeground;
  private final JETAColorWell trustedOuputBackground;
  private final JSpinner chatAutosaveTime;
  private final JTextField chatFilenameFormat;
  private final JSpinner typingNotificationDuration;
  private final JComboBox<String> macroEditorThemeCombo;
  // Chat Notification
  private final JETAColorWell chatNotificationColor;
  private final JCheckBox chatNotificationShowBackground;
  // Defaults
  private final JComboBox<LocalizedComboItem> defaultGridTypeCombo;
  private final JTextField defaultGridSizeTextField;
  private final JTextField defaultUnitsPerCellTextField;
  private final JTextField defaultVisionDistanceTextField;
  private final JTextField statsheetPortraitSize;
  private final JSpinner autoSaveSpinner;
  private final JCheckBox saveReminderCheckBox;
  private final JCheckBox showDialogOnNewToken;
  // Accessibility
  private final JTextField fontSizeTextField;
  private final JTextField toolTipInitialDelay;
  private final JTextField toolTipDismissDelay;
  // Application
  private final JCheckBox fitGMView;
  private final JCheckBox fillSelectionCheckBox;
  private final JTextField frameRateCapTextField;
  private final JTextField defaultUsername;

  // private final JCheckBox initEnableServerSyncCheckBox;
  private final JCheckBox hideNPCs;
  private final JCheckBox ownerPermissions;
  private final JCheckBox lockMovement;
  private final JCheckBox showInitGainMessage;
  private final JTextField upnpDiscoveryTimeoutTextField;
  private final JTextField fileSyncPath;
  private final JButton fileSyncPathButton;
  private final JCheckBox allowExternalMacroAccessCheckBox;

  // Authentication
  private final JTextArea publicKeyTextArea;
  private final JButton regeneratePublicKey;
  private final JButton copyPublicKey;

  // Themes
  private final JList<String> themeList;
  private final JLabel themeImageLabel;
  private final JLabel themeNameLabel;

  private final ListModel<String> allThemesListModel;

  private final ListModel<String> lightThemesListModel;

  private final ListModel<String> darkThemesListModel;
  private final JComboBox<LocalizedComboItem> themeFilterCombo;

  private final JCheckBox useThemeForChat;

  // Startup
  private final JTextField jvmXmxTextField;
  private final JTextField jvmXmsTextField;
  private final JTextField jvmXssTextField;
  private final JTextField dataDirTextField;
  private final JCheckBox jvmDirect3dCheckbox;
  private final JCheckBox jvmOpenGLCheckbox;
  private final JCheckBox jvmInitAwtCheckbox;
  private final JComboBox<String> jamLanguageOverrideComboBox;
  private final JLabel startupInfoLabel;
  private boolean jvmValuesChanged = false;

  private boolean themeChanged = false;
  private static final LocalizedComboItem[] defaultGridTypeComboItems = {
    new LocalizedComboItem(GridFactory.SQUARE, "Preferences.combo.maps.grid.square"),
    new LocalizedComboItem(GridFactory.HEX_HORI, "Preferences.combo.maps.grid.hexHori"),
    new LocalizedComboItem(GridFactory.HEX_VERT, "Preferences.combo.maps.grid.hexVert"),
    new LocalizedComboItem(GridFactory.ISOMETRIC, "Preferences.combo.maps.grid.isometric"),
    new LocalizedComboItem(GridFactory.NONE, "MapPropertiesDialog.image.nogrid")
  };
  private static final LocalizedComboItem[] duplicateTokenComboItems = {
    new LocalizedComboItem(Token.NUM_INCREMENT, "Preferences.combo.tokens.duplicate.increment"),
    new LocalizedComboItem(Token.NUM_RANDOM, "Preferences.combo.tokens.duplicate.random"),
  };
  private static final LocalizedComboItem[] showNumberingComboItems = {
    new LocalizedComboItem(Token.NUM_ON_NAME, "Preferences.combo.tokens.numbering.name"),
    new LocalizedComboItem(Token.NUM_ON_GM, "Preferences.combo.tokens.numbering.gm"),
    new LocalizedComboItem(Token.NUM_ON_BOTH, "Preferences.combo.tokens.numbering.both")
  };
  private static final LocalizedComboItem[] tokenNamingComboItems = {
    new LocalizedComboItem(Token.NAME_USE_FILENAME, "Preferences.combo.tokens.naming.filename"),
    new LocalizedComboItem(
        Token.NAME_USE_CREATURE,
        "Preferences.combo.tokens.naming.creature",
        I18N.getString("Token.name.creature"))
  };
  private static final WalkerMetric[] movementMetricComboItems = {
    WalkerMetric.ONE_TWO_ONE,
    WalkerMetric.ONE_ONE_ONE,
    WalkerMetric.MANHATTAN,
    WalkerMetric.NO_DIAGONALS
  };

  private static final LocalizedComboItem[] themeFilterComboItems = {
    new LocalizedComboItem("All", "Preferences.combo.themes.filter.all"),
    new LocalizedComboItem("Dark", "Preferences.combo.themes.filter.dark"),
    new LocalizedComboItem("Light", "Preferences.combo.themes.filter.light")
  };

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

    FormPanel panel =
        new FormPanelI18N("net/rptools/maptool/client/ui/forms/preferencesDialog.xml");

    JButton okButton = (JButton) panel.getButton("okButton");
    getRootPane().setDefaultButton(okButton);
    okButton.addActionListener(
        e -> {
          // Warn the user that they changed JVM options and need to restart MapTool for
          // them to take affect.
          // Also warn user to double check settings...
          /* This cant happen until jpackager allows startup parameters outside of app
             see https://github.com/RPTools/maptool/issues/2174
          if (jvmValuesChanged) {
            if (!MapTool.confirm("msg.confirm.jvm.options")) {
              return;
            }
          }
           */

          boolean close = true;
          /* This cant happen until jpackager allows startup parameters outside of app
             see https://github.com/RPTools/maptool/issues/2174
          if (jvmValuesChanged) {
            close = UserJvmOptions.saveAppCfg();
          }
           */

          if (close) {
            setVisible(false);
            dispose();
          }
          MapTool.getEventDispatcher().fireEvent(MapTool.PreferencesEvent.Changed);
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
    defaultUsername = panel.getTextField("defaultUsername");
    // initEnableServerSyncCheckBox = panel.getCheckBox("initEnableServerSyncCheckBox");
    autoSaveSpinner = panel.getSpinner("autoSaveSpinner");
    duplicateTokenCombo = panel.getComboBox("duplicateTokenCombo");
    tokenNamingCombo = panel.getComboBox("tokenNamingCombo");
    newMapsHaveFOWCheckBox = panel.getCheckBox("newMapsHaveFOWCheckBox");
    tokensPopupWarningWhenDeletedCheckBox =
        panel.getCheckBox("tokensPopupWarningWhenDeletedCheckBox"); // new
    // JCheckBox();//panel.getCheckBox("testCheckBox");
    tokensStartSnapToGridCheckBox = panel.getCheckBox("tokensStartSnapToGridCheckBox");
    tokensSnapWhileDraggingCheckBox = panel.getCheckBox("tokensSnapWhileDragging");
    hideMousePointerWhileDraggingCheckBox = panel.getCheckBox("hideMousePointerWhileDragging");
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
    darknessOverlayOpacitySpinner = panel.getSpinner("darknessOverlayOpacitySpinner");
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
    trustedOuputForeground = (JETAColorWell) panel.getComponentByName("trustedOuputForeground");
    trustedOuputBackground = (JETAColorWell) panel.getComponentByName("trustedOuputBackground");
    toolTipInitialDelay = panel.getTextField("toolTipInitialDelay");
    toolTipDismissDelay = panel.getTextField("toolTipDismissDelay");
    facingFaceEdges = panel.getCheckBox("facingFaceEdges");
    facingFaceVertices = panel.getCheckBox("facingFaceVertices");

    chatNotificationColor = (JETAColorWell) panel.getComponentByName("chatNotificationColor");
    chatNotificationShowBackground = panel.getCheckBox("chatNotificationShowBackground");

    chatAutosaveTime = panel.getSpinner("chatAutosaveTime");
    chatFilenameFormat = panel.getTextField("chatFilenameFormat");

    macroEditorThemeCombo = panel.getComboBox("macroEditorThemeCombo");

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

    publicKeyTextArea = (JTextArea) panel.getTextComponent("publicKeyTextArea");
    regeneratePublicKey = (JButton) panel.getButton("regeneratePublicKey");
    copyPublicKey = (JButton) panel.getButton("copyKey");

    themeList = (JList<String>) panel.getList("themeList");
    themeImageLabel = (JLabel) panel.getComponentByName("themeImage");
    themeNameLabel = (JLabel) panel.getComponentByName("currentThemeName");
    useThemeForChat = (JCheckBox) panel.getComponentByName("useThemeForChat");
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
          updateFacings();
        });
    facingFaceVertices.addActionListener(
        e -> {
          AppPreferences.setFaceVertex(facingFaceVertices.isSelected());
          updateFacings();
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

    trustedOuputForeground.addActionListener(
        e -> {
          AppPreferences.setTrustedPrefixFG(trustedOuputForeground.getColor());
          MapTool.getFrame()
              .getCommandPanel()
              .setTrustedMacroPrefixColors(
                  AppPreferences.getTrustedPrefixFG(), AppPreferences.getTrustedPrefixBG());
        });
    trustedOuputBackground.addActionListener(
        e -> {
          AppPreferences.setTrustedPrefixBG(trustedOuputBackground.getColor());
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
              }

              @Override
              protected Integer convertString(String value) throws ParseException {
                return StringUtil.parseInteger(value);
              }
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
    darknessOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setDarknessOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
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
   * Used by the ActionListeners of the facing checkboxes to update the facings for all of the
   * current zones. Redundant to go through all zones because all zones using the same grid type
   * share facings but it doesn't hurt anything and avoids having to track what grid types are being
   * used.
   */
  private void updateFacings() {
    // List<Zone> zlist = MapTool.getServer().getCampaign().getZones(); // generated NPE
    // http://forums.rptools.net/viewtopic.php?f=3&t=17334
    List<Zone> zlist = MapTool.getCampaign().getZones();
    boolean faceEdges = AppPreferences.getFaceEdge();
    boolean faceVertices = AppPreferences.getFaceVertex();
    for (Zone z : zlist) {
      Grid g = z.getGrid();
      g.setFacings(faceEdges, faceVertices);
    }
  }

  private void setInitialState() {
    showDialogOnNewToken.setSelected(AppPreferences.getShowDialogOnNewToken());
    saveReminderCheckBox.setSelected(AppPreferences.getSaveReminder());
    fillSelectionCheckBox.setSelected(AppPreferences.getFillSelectionBox());
    frameRateCapTextField.setText(Integer.toString(AppPreferences.getFrameRateCap()));
    defaultUsername.setText(AppPreferences.getDefaultUserName());
    // initEnableServerSyncCheckBox.setSelected(AppPreferences.getInitEnableServerSync());
    autoSaveSpinner.setValue(AppPreferences.getAutoSaveIncrement());
    newMapsHaveFOWCheckBox.setSelected(AppPreferences.getNewMapsHaveFOW());
    tokensPopupWarningWhenDeletedCheckBox.setSelected(AppPreferences.getTokensWarnWhenDeleted());
    tokensStartSnapToGridCheckBox.setSelected(AppPreferences.getTokensStartSnapToGrid());
    tokensSnapWhileDraggingCheckBox.setSelected(AppPreferences.getTokensSnapWhileDragging());
    hideMousePointerWhileDraggingCheckBox.setSelected(
        AppPreferences.getHideMousePointerWhileDragging());
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
    darknessOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getDarknessOverlayOpacity(), 0, 255, 1));
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
    trustedOuputForeground.setColor(AppPreferences.getTrustedPrefixFG());
    trustedOuputBackground.setColor(AppPreferences.getTrustedPrefixBG());
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

  /** @author frank */
  private abstract static class DocumentListenerProxy<T> implements DocumentListener {

    JTextField comp;

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

    protected void updateValue() {
      try {
        storeNumericValue(convertString(comp.getText())); // Localized
      } catch (ParseException nfe) {
        // Ignore it
      }
    }

    protected abstract T convertString(String value) throws ParseException;

    protected abstract void storeNumericValue(T value);
  }

  /** @author frank */
  private abstract static class ChangeListenerProxy implements ChangeListener {

    @Override
    public void stateChanged(ChangeEvent ce) {
      JSpinner sp = (JSpinner) ce.getSource();
      int value = (Integer) sp.getValue();
      storeSpinnerValue(value);
    }

    protected abstract void storeSpinnerValue(int value);
  }

  /**
   * Stores the localized display name and preference value String for menu items that don't have a
   * corresponding enum.
   */
  private static class LocalizedComboItem {
    private final String displayName;
    private final String prefValue;

    LocalizedComboItem(String prefValue, String i18nKey) {
      this.prefValue = prefValue;
      displayName = I18N.getText(i18nKey);
    }

    LocalizedComboItem(String prefValue, String i18nKey, Object... args) {
      this.prefValue = prefValue;
      displayName = I18N.getText(i18nKey, args);
    }

    public String getValue() {
      return prefValue;
    }

    public String toString() {
      return displayName;
    }
  }
}
