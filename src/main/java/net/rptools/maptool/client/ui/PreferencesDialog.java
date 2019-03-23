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

import com.jeta.forms.components.colors.JETAColorWell;
import com.jeta.forms.components.panel.FormPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.List;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.StringUtil;
import net.rptools.maptool.util.UserJvmPrefs;
import net.rptools.maptool.util.UserJvmPrefs.JVM_OPTION;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreferencesDialog extends JDialog {
  private static final Logger log = LogManager.getLogger(PreferencesDialog.class);

  /** @author frank */
  private abstract class DocumentListenerProxy implements DocumentListener {
    JTextField comp;

    public DocumentListenerProxy(JTextField tf) {
      comp = tf;
    }

    public void changedUpdate(DocumentEvent e) {
      updateValue();
    }

    public void insertUpdate(DocumentEvent e) {
      updateValue();
    }

    public void removeUpdate(DocumentEvent e) {
      updateValue();
    }

    protected void updateValue() {
      try {
        int value = StringUtil.parseInteger(comp.getText()); // Localized
        storeNumericValue(value);
      } catch (ParseException nfe) {
        // Ignore it
      }
    }

    protected abstract void storeNumericValue(int value);
  }

  /** @author frank */
  private abstract class ChangeListenerProxy implements ChangeListener {
    public void stateChanged(ChangeEvent ce) {
      JSpinner sp = (JSpinner) ce.getSource();
      int value = (Integer) sp.getValue();
      storeSpinnerValue(value);
    }

    protected abstract void storeSpinnerValue(int value);
  }

  // Tabbed Panel
  private final JTabbedPane tabbedPane;

  // Interactions
  private final JCheckBox newMapsHaveFOWCheckBox;
  private final JCheckBox tokensPopupWarningWhenDeletedCheckBox;
  private final JCheckBox tokensStartSnapToGridCheckBox;
  private final JCheckBox newMapsVisibleCheckBox;
  private final JCheckBox newTokensVisibleCheckBox;
  private final JCheckBox tokensStartFreeSizeCheckBox;
  private final JCheckBox stampsStartSnapToGridCheckBox;
  private final JCheckBox stampsStartFreeSizeCheckBox;
  private final JCheckBox backgroundsStartSnapToGridCheckBox;
  private final JCheckBox backgroundsStartFreeSizeCheckBox;
  private final JComboBox duplicateTokenCombo;
  private final JComboBox tokenNamingCombo;
  private final JComboBox showNumberingCombo;
  private final JComboBox movementMetricCombo;
  private final JCheckBox showStatSheetCheckBox;
  private final JCheckBox showPortraitCheckBox;
  private final JCheckBox showStatSheetModifierCheckBox;
  private final JCheckBox forceFacingArrowCheckBox;

  private final JSpinner haloLineWidthSpinner;
  private final JSpinner haloOverlayOpacitySpinner;
  private final JSpinner auraOverlayOpacitySpinner;
  private final JSpinner lightOverlayOpacitySpinner;
  private final JSpinner fogOverlayOpacitySpinner;
  private final JCheckBox useHaloColorAsVisionOverlayCheckBox;
  private final JCheckBox autoRevealVisionOnGMMoveCheckBox;
  private final JCheckBox showSmiliesCheckBox;
  private final JCheckBox playSystemSoundCheckBox;
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

  // Chat Notification
  private final JETAColorWell chatNotificationColor;
  private final JCheckBox chatNotificationShowBackground;

  // Defaults
  private final JComboBox defaultGridTypeCombo;
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
  // private final JCheckBox initEnableServerSyncCheckBox;
  private final JCheckBox hideNPCs;
  private final JCheckBox ownerPermissions;
  private final JCheckBox lockMovement;
  private final JCheckBox showInitGainMessage;
  private final JTextField upnpDiscoveryTimeoutTextField;
  private final JTextField fileSyncPath;
  private final JButton fileSyncPathButton;
  private final JCheckBox allowExternalMacroAccessCheckBox;

  // Startup
  private final JTextField jvmXmxTextField;
  private final JTextField jvmXmsTextField;
  private final JTextField jvmXssTextField;
  private final JTextField dataDirTextField;

  private final JCheckBox jvmAssertionsCheckbox;
  private final JCheckBox jvmDirect3dCheckbox;
  private final JCheckBox jvmOpenGLCheckbox;
  private final JCheckBox jvmInitAwtCheckbox;

  private final JComboBox<String> jvmLanguageOverideComboBox;

  private boolean jvmValuesChanged = false;

  public PreferencesDialog() {
    super(MapTool.getFrame(), "Preferences", true);
    setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    FormPanel panel = new FormPanel("net/rptools/maptool/client/ui/forms/preferencesDialog.xml");

    JButton okButton = (JButton) panel.getButton("okButton");
    getRootPane().setDefaultButton(okButton);
    okButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            // Warn the user that they changed JVM options and need to restart MapTool for them to
            // take affect.
            // Also warn user to double check settings...
            if (jvmValuesChanged) if (!MapTool.confirm("msg.confirm.jvm.options")) return;

            setVisible(false);
            dispose();
            MapTool.getEventDispatcher().fireEvent(MapTool.PreferencesEvent.Changed);
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
    // initEnableServerSyncCheckBox = panel.getCheckBox("initEnableServerSyncCheckBox");
    autoSaveSpinner = panel.getSpinner("autoSaveSpinner");
    duplicateTokenCombo = panel.getComboBox("duplicateTokenCombo");
    tokenNamingCombo = panel.getComboBox("tokenNamingCombo");
    newMapsHaveFOWCheckBox = panel.getCheckBox("newMapsHaveFOWCheckBox");
    tokensPopupWarningWhenDeletedCheckBox =
        panel.getCheckBox("tokensPopupWarningWhenDeletedCheckBox"); // new
    // JCheckBox();//panel.getCheckBox("testCheckBox");
    tokensStartSnapToGridCheckBox = panel.getCheckBox("tokensStartSnapToGridCheckBox");
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
    fogOverlayOpacitySpinner = panel.getSpinner("fogOverlayOpacitySpinner");

    useHaloColorAsVisionOverlayCheckBox = panel.getCheckBox("useHaloColorAsVisionOverlayCheckBox");
    autoRevealVisionOnGMMoveCheckBox = panel.getCheckBox("autoRevealVisionOnGMMoveCheckBox");
    showSmiliesCheckBox = panel.getCheckBox("showSmiliesCheckBox");
    playSystemSoundCheckBox = panel.getCheckBox("playSystemSounds");
    playSystemSoundOnlyWhenNotFocusedCheckBox = panel.getCheckBox("soundsOnlyWhenNotFocused");
    syrinscapeActiveCheckBox = panel.getCheckBox("syrinscapeActive");
    showAvatarInChat = panel.getCheckBox("showChatAvatar");
    showDialogOnNewToken = panel.getCheckBox("showDialogOnNewToken");
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

    jvmXmxTextField = panel.getTextField("jvmXmxTextField");
    jvmXmsTextField = panel.getTextField("jvmXmsTextField");
    jvmXssTextField = panel.getTextField("jvmXssTextField");
    dataDirTextField = panel.getTextField("dataDirTextField");

    jvmAssertionsCheckbox = panel.getCheckBox("jvmAssertionsCheckbox");
    jvmDirect3dCheckbox = panel.getCheckBox("jvmDirect3dCheckbox");
    jvmOpenGLCheckbox = panel.getCheckBox("jvmOpenGLCheckbox");
    jvmInitAwtCheckbox = panel.getCheckBox("jvmInitAwtCheckbox");

    jvmLanguageOverideComboBox = panel.getComboBox("jvmLanguageOverideComboBox");

    DefaultComboBoxModel<String> languageModel = new DefaultComboBoxModel<String>();
    for (Entry<String, String> language : UserJvmPrefs.getLanguages())
      languageModel.addElement(language.getKey());

    jvmLanguageOverideComboBox.setModel(languageModel);

    setInitialState();

    // And keep it updated
    facingFaceEdges.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setFaceEdge(facingFaceEdges.isSelected());
            updateFacings();
          }
        });
    facingFaceVertices.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setFaceVertex(facingFaceVertices.isSelected());
            updateFacings();
          }
        });

    toolTipInlineRolls.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setUseToolTipForInlineRoll(toolTipInlineRolls.isSelected());
          }
        });

    suppressToolTipsMacroLinks.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setSuppressToolTipsForMacroLinks(
                suppressToolTipsMacroLinks.isSelected());
          }
        });

    toolTipInitialDelay
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(toolTipInitialDelay) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setToolTipInitialDelay(value);
                ToolTipManager.sharedInstance().setInitialDelay(value);
              }
            });
    toolTipDismissDelay
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(toolTipDismissDelay) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setToolTipDismissDelay(value);
                ToolTipManager.sharedInstance().setDismissDelay(value);
              }
            });

    chatNotificationColor.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setChatNotificationColor(chatNotificationColor.getColor());
            MapTool.getFrame().setChatTypingLabelColor(AppPreferences.getChatNotificationColor());
          }
        });

    trustedOuputForeground.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setTrustedPrefixFG(trustedOuputForeground.getColor());
            MapTool.getFrame()
                .getCommandPanel()
                .setTrustedMacroPrefixColors(
                    AppPreferences.getTrustedPrefixFG(), AppPreferences.getTrustedPrefixBG());
          }
        });
    trustedOuputBackground.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setTrustedPrefixBG(trustedOuputBackground.getColor());
            MapTool.getFrame()
                .getCommandPanel()
                .setTrustedMacroPrefixColors(
                    AppPreferences.getTrustedPrefixFG(), AppPreferences.getTrustedPrefixBG());
          }
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
              StringBuffer saveFile = new StringBuffer(chatFilenameFormat.getText());
              if (saveFile.indexOf(".") < 0) {
                saveFile.append(".html");
              }
              AppPreferences.setChatFilenameFormat(saveFile.toString());
            }
          }
        });

    allowPlayerMacroEditsDefault.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setAllowPlayerMacroEditsDefault(
                allowPlayerMacroEditsDefault.isSelected());
          }
        });

    showAvatarInChat.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowAvatarInChat(showAvatarInChat.isSelected());
          }
        });
    saveReminderCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setSaveReminder(saveReminderCheckBox.isSelected());
          }
        });
    fillSelectionCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setFillSelectionBox(fillSelectionCheckBox.isSelected());
          }
        });
    // initEnableServerSyncCheckBox.addActionListener(new ActionListener() {
    // public void actionPerformed(ActionEvent e) {
    // AppPreferences.setInitEnableServerSync(initEnableServerSyncCheckBox.isSelected());
    // }
    // });
    allowExternalMacroAccessCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setAllowExternalMacroAccess(
                allowExternalMacroAccessCheckBox.isSelected());
          }
        });
    showDialogOnNewToken.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowDialogOnNewToken(showDialogOnNewToken.isSelected());
          }
        });
    autoSaveSpinner.addChangeListener(
        new ChangeListener() {
          public void stateChanged(ChangeEvent ce) {
            int newInterval = (Integer) autoSaveSpinner.getValue();
            AppPreferences.setAutoSaveIncrement(newInterval);
            MapTool.getAutoSaveManager().restart();
          }
        });
    newMapsHaveFOWCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setNewMapsHaveFOW(newMapsHaveFOWCheckBox.isSelected());
          }
        });
    tokensPopupWarningWhenDeletedCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setTokensWarnWhenDeleted(
                tokensPopupWarningWhenDeletedCheckBox.isSelected());
          }
        });
    tokensStartSnapToGridCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setTokensStartSnapToGrid(tokensStartSnapToGridCheckBox.isSelected());
          }
        });
    newMapsVisibleCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setNewMapsVisible(newMapsVisibleCheckBox.isSelected());
          }
        });
    newTokensVisibleCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setNewTokensVisible(newTokensVisibleCheckBox.isSelected());
          }
        });
    stampsStartFreeSizeCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setObjectsStartFreesize(stampsStartFreeSizeCheckBox.isSelected());
          }
        });
    tokensStartFreeSizeCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setTokensStartFreesize(tokensStartFreeSizeCheckBox.isSelected());
          }
        });
    stampsStartSnapToGridCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setObjectsStartSnapToGrid(stampsStartSnapToGridCheckBox.isSelected());
          }
        });
    showStatSheetCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowStatSheet(showStatSheetCheckBox.isSelected());
          }
        });
    showPortraitCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowPortrait(showPortraitCheckBox.isSelected());
          }
        });
    showStatSheetModifierCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowStatSheetModifier(showStatSheetModifierCheckBox.isSelected());
          }
        });
    forceFacingArrowCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setForceFacingArrow(forceFacingArrowCheckBox.isSelected());
          }
        });
    backgroundsStartFreeSizeCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setBackgroundsStartFreesize(
                backgroundsStartFreeSizeCheckBox.isSelected());
          }
        });
    backgroundsStartSnapToGridCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setBackgroundsStartSnapToGrid(
                backgroundsStartSnapToGridCheckBox.isSelected());
          }
        });
    defaultGridSizeTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(defaultGridSizeTextField) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setDefaultGridSize(value);
              }
            });

    defaultUnitsPerCellTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(defaultUnitsPerCellTextField) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setDefaultUnitsPerCell(value);
              }
            });
    defaultVisionDistanceTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(defaultVisionDistanceTextField) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setDefaultVisionDistance(value);
              }
            });
    statsheetPortraitSize
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(statsheetPortraitSize) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setPortraitSize(value);
              }
            });
    haloLineWidthSpinner.addChangeListener(
        new ChangeListener() {
          public void stateChanged(ChangeEvent ce) {
            AppPreferences.setHaloLineWidth((Integer) haloLineWidthSpinner.getValue());
          }
        });

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
    fogOverlayOpacitySpinner.addChangeListener(
        new ChangeListenerProxy() {
          @Override
          protected void storeSpinnerValue(int value) {
            AppPreferences.setFogOverlayOpacity(value);
            MapTool.getFrame().refresh();
          }
        });
    useHaloColorAsVisionOverlayCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setUseHaloColorOnVisionOverlay(
                useHaloColorAsVisionOverlayCheckBox.isSelected());
          }
        });
    autoRevealVisionOnGMMoveCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setAutoRevealVisionOnGMMovement(
                autoRevealVisionOnGMMoveCheckBox.isSelected());
          }
        });
    showSmiliesCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowSmilies(showSmiliesCheckBox.isSelected());
          }
        });
    playSystemSoundCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setPlaySystemSounds(playSystemSoundCheckBox.isSelected());
          }
        });

    playSystemSoundOnlyWhenNotFocusedCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setPlaySystemSoundsOnlyWhenNotFocused(
                playSystemSoundOnlyWhenNotFocusedCheckBox.isSelected());
          }
        });

    syrinscapeActiveCheckBox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setSyrinscapeActive(syrinscapeActiveCheckBox.isSelected());
          }
        });

    fontSizeTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(fontSizeTextField) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setFontSize(value);
              }
            });

    fitGMView.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setFitGMView(fitGMView.isSelected());
          }
        });
    hideNPCs.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setInitHideNpcs(hideNPCs.isSelected());
          }
        });
    ownerPermissions.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setInitOwnerPermissions(ownerPermissions.isSelected());
          }
        });
    lockMovement.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setInitLockMovement(lockMovement.isSelected());
          }
        });
    showInitGainMessage.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setShowInitGainMessage(showInitGainMessage.isSelected());
          }
        });
    upnpDiscoveryTimeoutTextField
        .getDocument()
        .addDocumentListener(
            new DocumentListenerProxy(upnpDiscoveryTimeoutTextField) {
              @Override
              protected void storeNumericValue(int value) {
                AppPreferences.setUpnpDiscoveryTimeout(value);
              }
            });
    fileSyncPathButton.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser(AppPreferences.getFileSyncPath());
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = fileChooser.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
              String selectedPath = fileChooser.getSelectedFile().getPath().toString();

              // Set the text field
              fileSyncPath.setText(selectedPath);
              fileSyncPath.setCaretPosition(0);

              // Save to preferences
              AppPreferences.setFileSyncPath(selectedPath);
            }
          }
        });
    jvmXmxTextField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            if (!e.isTemporary()) {
              String jvmXmx = jvmXmxTextField.getText().trim();

              if (UserJvmPrefs.verifyJvmOptions(jvmXmx)) {
                UserJvmPrefs.setJvmOption(JVM_OPTION.MAX_MEM, jvmXmx);
              } else {
                jvmXmxTextField.setText(JVM_OPTION.MAX_MEM.getDefaultValue());
                UserJvmPrefs.setJvmOption(JVM_OPTION.MAX_MEM, JVM_OPTION.MAX_MEM.getDefaultValue());
                log.warn("Invalid JVM Xmx paramater entered: " + jvmXmx);
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

              if (UserJvmPrefs.verifyJvmOptions(jvmXms)) {
                UserJvmPrefs.setJvmOption(JVM_OPTION.MIN_MEM, jvmXms);
              } else {
                jvmXmsTextField.setText(JVM_OPTION.MIN_MEM.getDefaultValue());
                UserJvmPrefs.setJvmOption(JVM_OPTION.MIN_MEM, JVM_OPTION.MIN_MEM.getDefaultValue());
                log.warn("Invalid JVM Xms paramater entered: " + jvmXms);
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

              if (UserJvmPrefs.verifyJvmOptions(jvmXss)) {
                UserJvmPrefs.setJvmOption(JVM_OPTION.STACK_SIZE, jvmXss);
              } else {
                jvmXssTextField.setText(JVM_OPTION.STACK_SIZE.getDefaultValue());
                UserJvmPrefs.setJvmOption(
                    JVM_OPTION.STACK_SIZE, JVM_OPTION.STACK_SIZE.getDefaultValue());
                log.warn("Invalid JVM Xss paramater entered: " + jvmXss);
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
              UserJvmPrefs.setJvmOption(JVM_OPTION.DATA_DIR, dataDirTextField.getText().trim());
              jvmValuesChanged = true;
            }
          }
        });

    jvmAssertionsCheckbox.addActionListener(
        new ActionListener() {

          public void actionPerformed(ActionEvent e) {
            UserJvmPrefs.setJvmOption(JVM_OPTION.ASSERTIONS, jvmAssertionsCheckbox.isSelected());
            jvmValuesChanged = true;
          }
        });
    jvmDirect3dCheckbox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            UserJvmPrefs.setJvmOption(JVM_OPTION.JAVA2D_D3D, jvmDirect3dCheckbox.isSelected());
            jvmValuesChanged = true;
          }
        });
    jvmOpenGLCheckbox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            UserJvmPrefs.setJvmOption(
                JVM_OPTION.JAVA2D_OPENGL_OPTION, jvmOpenGLCheckbox.isSelected());
            jvmValuesChanged = true;
          }
        });
    jvmInitAwtCheckbox.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            UserJvmPrefs.setJvmOption(
                JVM_OPTION.MACOSX_EMBEDDED_OPTION, jvmInitAwtCheckbox.isSelected());
            jvmValuesChanged = true;
          }
        });

    jvmLanguageOverideComboBox.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            UserJvmPrefs.setJvmOption(
                JVM_OPTION.LOCALE_LANGUAGE,
                jvmLanguageOverideComboBox.getSelectedItem().toString());
            jvmValuesChanged = true;
          }
        });

    chatNotificationShowBackground.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            AppPreferences.setChatNotificationShowBackground(
                chatNotificationShowBackground.isSelected());
          }
        });

    DefaultComboBoxModel gridTypeModel = new DefaultComboBoxModel();
    gridTypeModel.addElement(GridFactory.SQUARE);
    gridTypeModel.addElement(GridFactory.HEX_HORI);
    gridTypeModel.addElement(GridFactory.HEX_VERT);
    gridTypeModel.addElement(GridFactory.ISOMETRIC);
    gridTypeModel.setSelectedItem(AppPreferences.getDefaultGridType());
    defaultGridTypeCombo.setModel(gridTypeModel);
    defaultGridTypeCombo.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            AppPreferences.setDefaultGridType((String) defaultGridTypeCombo.getSelectedItem());
          }
        });

    DefaultComboBoxModel tokenNumModel = new DefaultComboBoxModel();
    tokenNumModel.addElement(Token.NUM_INCREMENT);
    tokenNumModel.addElement(Token.NUM_RANDOM);
    tokenNumModel.setSelectedItem(AppPreferences.getDuplicateTokenNumber());
    duplicateTokenCombo.setModel(tokenNumModel);
    duplicateTokenCombo.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            AppPreferences.setDuplicateTokenNumber((String) duplicateTokenCombo.getSelectedItem());
          }
        });

    DefaultComboBoxModel tokenNameModel = new DefaultComboBoxModel();
    tokenNameModel.addElement(Token.NAME_USE_FILENAME);
    tokenNameModel.addElement(Token.NAME_USE_CREATURE);
    tokenNameModel.setSelectedItem(AppPreferences.getNewTokenNaming());
    tokenNamingCombo.setModel(tokenNameModel);
    tokenNamingCombo.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            AppPreferences.setNewTokenNaming((String) tokenNamingCombo.getSelectedItem());
          }
        });

    DefaultComboBoxModel showNumModel = new DefaultComboBoxModel();
    showNumModel.addElement(Token.NUM_ON_NAME);
    showNumModel.addElement(Token.NUM_ON_GM);
    showNumModel.addElement(Token.NUM_ON_BOTH);
    showNumModel.setSelectedItem(AppPreferences.getTokenNumberDisplay());
    showNumberingCombo.setModel(showNumModel);
    showNumberingCombo.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            AppPreferences.setTokenNumberDisplay((String) showNumberingCombo.getSelectedItem());
          }
        });

    DefaultComboBoxModel movementMetricModel = new DefaultComboBoxModel();
    movementMetricModel.addElement(WalkerMetric.ONE_TWO_ONE);
    movementMetricModel.addElement(WalkerMetric.ONE_ONE_ONE);
    movementMetricModel.addElement(WalkerMetric.MANHATTAN);
    movementMetricModel.addElement(WalkerMetric.NO_DIAGONALS);
    movementMetricModel.setSelectedItem(AppPreferences.getMovementMetric());

    movementMetricCombo.setModel(movementMetricModel);
    movementMetricCombo.addItemListener(
        new ItemListener() {
          public void itemStateChanged(ItemEvent e) {
            AppPreferences.setMovementMetric((WalkerMetric) movementMetricCombo.getSelectedItem());
          }
        });
    // showInitGainMessage

    add(panel);
    pack();
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
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
    // initEnableServerSyncCheckBox.setSelected(AppPreferences.getInitEnableServerSync());
    autoSaveSpinner.setValue(AppPreferences.getAutoSaveIncrement());
    newMapsHaveFOWCheckBox.setSelected(AppPreferences.getNewMapsHaveFOW());
    tokensPopupWarningWhenDeletedCheckBox.setSelected(AppPreferences.getTokensWarnWhenDeleted());
    tokensStartSnapToGridCheckBox.setSelected(AppPreferences.getTokensStartSnapToGrid());
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
    defaultUnitsPerCellTextField.setText(Integer.toString(AppPreferences.getDefaultUnitsPerCell()));
    defaultVisionDistanceTextField.setText(
        Integer.toString(AppPreferences.getDefaultVisionDistance()));
    statsheetPortraitSize.setText(Integer.toString(AppPreferences.getPortraitSize()));
    fontSizeTextField.setText(Integer.toString(AppPreferences.getFontSize()));
    haloLineWidthSpinner.setValue(AppPreferences.getHaloLineWidth());

    haloOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getHaloOverlayOpacity(), 0, 255, 1));
    auraOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getAuraOverlayOpacity(), 0, 255, 1));
    lightOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getLightOverlayOpacity(), 0, 255, 1));
    fogOverlayOpacitySpinner.setModel(
        new SpinnerNumberModel(AppPreferences.getFogOverlayOpacity(), 0, 255, 1));

    useHaloColorAsVisionOverlayCheckBox.setSelected(
        AppPreferences.getUseHaloColorOnVisionOverlay());
    autoRevealVisionOnGMMoveCheckBox.setSelected(AppPreferences.getAutoRevealVisionOnGMMovement());
    showSmiliesCheckBox.setSelected(AppPreferences.getShowSmilies());
    playSystemSoundCheckBox.setSelected(AppPreferences.getPlaySystemSounds());
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
    try {
      jvmXmxTextField.setText(UserJvmPrefs.getJvmOption(JVM_OPTION.MAX_MEM));
      jvmXmsTextField.setText(UserJvmPrefs.getJvmOption(JVM_OPTION.MIN_MEM));
      jvmXssTextField.setText(UserJvmPrefs.getJvmOption(JVM_OPTION.STACK_SIZE));
      dataDirTextField.setText(UserJvmPrefs.getJvmOption(JVM_OPTION.DATA_DIR));

      jvmAssertionsCheckbox.setSelected(UserJvmPrefs.hasJvmOption(JVM_OPTION.ASSERTIONS));
      jvmDirect3dCheckbox.setSelected(UserJvmPrefs.hasJvmOption(JVM_OPTION.JAVA2D_D3D));
      jvmOpenGLCheckbox.setSelected(UserJvmPrefs.hasJvmOption(JVM_OPTION.JAVA2D_OPENGL_OPTION));
      jvmInitAwtCheckbox.setSelected(UserJvmPrefs.hasJvmOption(JVM_OPTION.MACOSX_EMBEDDED_OPTION));

      jvmLanguageOverideComboBox.setSelectedItem(
          UserJvmPrefs.getJvmOption(JVM_OPTION.LOCALE_LANGUAGE));
    } catch (UnsatisfiedLinkError | NoClassDefFoundError | ServiceConfigurationError e) {
      log.warn(
          "Warning, unable to get JVM options from preferences. Most likely cause, manual launch of JAR.");
      tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Startup"), false);
    } catch (Exception e) {
      log.error(
          "Error getting JVM options from preferences. Most likely cause, manual launch of JAR.",
          e);
      tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Startup"), false);
    }

    Integer rawVal = AppPreferences.getTypingNotificationDuration();
    Integer typingVal = null;
    if (rawVal != null
        && rawVal > 99) { // backward compatibility -- used to be stored in ms, now in seconds
      Double dbl = (double) (rawVal / 1000);
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
  }
}
