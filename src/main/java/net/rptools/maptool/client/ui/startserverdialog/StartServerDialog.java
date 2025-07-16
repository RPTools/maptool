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

import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.rptools.lib.StringUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.GenericDialogFactory;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.util.PasswordGenerator;
import yasb.Binder;

/**
 * @author trevor
 */
public class StartServerDialog extends AbeillePanel<StartServerDialogPreferences> {
  private StartServerDialogPreferences prefs;
  private final GenericDialogFactory dialogFactory =
      GenericDialog.getFactory()
          .setDialogTitle(I18N.getText("ServerDialog.msg.title"))
          .setCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
          .addButton(ButtonKind.OK)
          .setDefaultButton(ButtonKind.OK);
  private JComboBox movementMetricCombo;
  private JCheckBox useIndividualFOW;
  private JCheckBox useIndividualViews;
  private JCheckBox autoRevealOnMovement;
  private JCheckBox playersCanRevealVision;
  private JCheckBox hideMapSelectUI;
  private JCheckBox lockTokenEditOnStartup;
  private JCheckBox lockPlayerMoveOnStartup;
  private JCheckBox lockPlayerLibrary;
  private JButton generateGMPassword;
  private JButton generatePlayerPassword;
  private JTextField gmPassword;
  private JTextField playerPassword;
  private JCheckBox usePasswordFile;
  private JCheckBox useEasyConnect;

  public StartServerDialog() {
    super(new StartServerDialogView().getRootComponent());
    panelInit();
    dialogFactory
        .setContent(this)
        .addButton(
            ButtonKind.NETWORKING_HELP,
            e -> {
              // We don't have a good, server-side way of testing anymore.
              boolean ok = MapTool.confirm("msg.info.server.networkingHelp");
              if (ok) {
                MapTool.showDocument(I18N.getString("msg.info.server.forumNFAQ_URL"));
              }
            })
        .addButton(
            ButtonKind.CANCEL,
            e -> dialogFactory.setDialogResult(GenericDialog.DENY).closeDialog());
  }

  public String showDialog() {
    prefs = new StartServerDialogPreferences();
    bind(prefs);
    useIndividualFOW = (JCheckBox) getComponent("@useIndividualFOW");
    useIndividualViews = (JCheckBox) getComponent("@useIndividualViews");
    autoRevealOnMovement = (JCheckBox) getComponent("@autoRevealOnMovement");
    playersCanRevealVision = (JCheckBox) getComponent("@playersCanRevealVision");
    generateGMPassword = (JButton) getComponent("@generateGMPassword");
    generatePlayerPassword = (JButton) getComponent("@generatePlayerPassword");
    gmPassword = (JTextField) getComponent("@GMPassword");
    playerPassword = (JTextField) getComponent("@playerPassword");
    useEasyConnect = (JCheckBox) getComponent("@useEasyConnect");
    usePasswordFile = (JCheckBox) getComponent("@usePasswordFile");
    hideMapSelectUI = (JCheckBox) getComponent("@hideMapSelectUI");
    lockTokenEditOnStartup = (JCheckBox) getComponent("@lockTokenEditOnStartup");
    lockPlayerMoveOnStartup = (JCheckBox) getComponent("@lockPlayerMovementOnStartup");
    lockPlayerLibrary = (JCheckBox) getComponent("@disablePlayerLibrary");

    getRoleCombo().setModel(new DefaultComboBoxModel<>(Player.Role.values()));
    getRoleCombo().setSelectedItem(prefs.getRole());

    useIndividualFOW.setEnabled(prefs.getUseIndividualViews());
    useIndividualViews.addItemListener(
        e -> {
          if (!useIndividualViews.isSelected()) {
            useIndividualFOW.setSelected(false);
            useIndividualFOW.setEnabled(false);
          } else {
            useIndividualFOW.setEnabled(true);
          }
        });

    autoRevealOnMovement.setEnabled(prefs.getPlayersCanRevealVision());
    playersCanRevealVision.addItemListener(
        e -> {
          if (!playersCanRevealVision.isSelected()) {
            autoRevealOnMovement.setSelected(false);
            autoRevealOnMovement.setEnabled(false);
          } else {
            autoRevealOnMovement.setEnabled(true);
          }
        });

    boolean usePf = usePasswordFile.isSelected();
    boolean useEC = useEasyConnect.isSelected();
    playerPassword.setEnabled(!usePf);
    gmPassword.setEnabled(!usePf);
    usePasswordFile.setEnabled(!useEC);
    usePasswordFile.addItemListener(
        e -> {
          boolean passwordFile = usePasswordFile.isSelected();
          playerPassword.setEnabled(!passwordFile);
          gmPassword.setEnabled(!passwordFile);
        });
    useEasyConnect.addItemListener(
        e -> {
          boolean easyConnect = useEasyConnect.isSelected();
          if (easyConnect) {
            usePasswordFile.setSelected(true);
            usePasswordFile.setEnabled(false);
          } else {
            usePasswordFile.setEnabled(true);
          }
        });
    hideMapSelectUI.setSelected(prefs.getMapSelectUIHidden());
    lockTokenEditOnStartup.setSelected(prefs.getLockTokenEditOnStart());
    lockPlayerMoveOnStartup.setSelected(prefs.getLockPlayerMovementOnStart());
    lockPlayerLibrary.setSelected(prefs.getPlayerLibraryLock());

    movementMetricCombo = getMovementMetric();
    DefaultComboBoxModel movementMetricModel = new DefaultComboBoxModel();
    movementMetricModel.addElement(WalkerMetric.ONE_TWO_ONE);
    movementMetricModel.addElement(WalkerMetric.ONE_ONE_ONE);
    movementMetricModel.addElement(WalkerMetric.MANHATTAN);
    movementMetricModel.addElement(WalkerMetric.NO_DIAGONALS);
    movementMetricModel.setSelectedItem(AppPreferences.movementMetric.get());

    movementMetricCombo.setModel(movementMetricModel);
    movementMetricCombo.addItemListener(
        e -> prefs.setMovementMetric((WalkerMetric) movementMetricCombo.getSelectedItem()));

    generateGMPassword.addActionListener(
        l -> {
          PasswordGenerator passwordGenerator = new PasswordGenerator();
          String password = passwordGenerator.getPassword();
          gmPassword.setText(password);
        });

    generatePlayerPassword.addActionListener(
        l -> {
          PasswordGenerator passwordGenerator = new PasswordGenerator();
          String password = passwordGenerator.getPassword();
          playerPassword.setText(password);
        });

    getRootPane().setDefaultButton(dialogFactory.getDefaultButton());
    getUseWebRTCCheckBox().setEnabled(!getRPToolsAlias().getText().isEmpty());
    return dialogFactory.displayWithReturnValue();
  }

  public JCheckBox getUseWebRTCCheckBox() {
    return (JCheckBox) getComponent("@useWebRTC");
  }

  public JTextField getRPToolsAlias() {
    return (JTextField) getComponent("@RPToolsName");
  }

  public JButton getGenerateGMPasswordButton() {
    return (JButton) getComponent("@generateGMPassword");
  }

  public JButton getGeneratePlayerPasswordButton() {
    return (JButton) getComponent("@generatePlayerPassword");
  }

  public JTextField getPortTextField() {
    return (JTextField) getComponent("@port");
  }

  public JTextField getUsernameTextField() {
    return (JTextField) getComponent("@username");
  }

  public JComboBox getRoleCombo() {
    return (JComboBox) getComponent("role");
  }

  public JButton getNetworkingHelpButton() {
    return (JButton) getComponent("networkingHelpButton");
  }

  public JCheckBox getUseUPnPCheckbox() {
    return (JCheckBox) getComponent("@useUPnP");
  }

  public JCheckBox getUseTooltipForRolls() {
    return (JCheckBox) getComponent("@useToolTipsForUnformattedRolls");
  }

  public JComboBox getMovementMetric() {
    return (JComboBox) getComponent("movementMetric");
  }

  @Override
  protected void preModelBind() {
    Binder.setFormat(getPortTextField(), new DecimalFormat("####"));
  }

  public void initOKButton() {
    dialogFactory
        .getOKButton()
        .addActionListener(
            e -> {
              if (getPortTextField().getText().isEmpty()) {
                MapTool.showError("ServerDialog.error.port");
                return;
              }
              // If not using the password file then both the player and GM passwords must be
              // present and must differ.
              if (!usePasswordFile.isSelected()) {
                if (gmPassword.getText().isEmpty() || playerPassword.getText().isEmpty()) {
                  MapTool.showError("ServerDialog.error.passwordMissing");
                  return;
                }
                if (gmPassword.getText().equals(playerPassword.getText())) {
                  MapTool.showError("ServerDialog.error.passwordMustDiffer");
                  return;
                }
              }
              try {
                Integer.parseInt(getPortTextField().getText());
              } catch (NumberFormatException nfe) {
                MapTool.showError("ServerDialog.error.port");
                return;
              }
              if (StringUtil.isEmpty(getUsernameTextField().getText())) {
                MapTool.showError("ServerDialog.error.username");
                return;
              }
              if (commit()) {
                prefs.setRole((Player.Role) getRoleCombo().getSelectedItem());
                prefs.setMovementMetric((WalkerMetric) movementMetricCombo.getSelectedItem());
                prefs.setAutoRevealOnMovement(autoRevealOnMovement.isSelected());
                prefs.setUsePasswordFile(usePasswordFile.isSelected());
                prefs.setUseEasyConnect(useEasyConnect.isSelected());
                prefs.setMapSelectUIHidden(hideMapSelectUI.isSelected());
                prefs.setLockTokenEditOnStart(lockTokenEditOnStartup.isSelected());
                prefs.setLockPlayerMovementOnStart(lockPlayerMoveOnStartup.isSelected());
                prefs.setPlayerLibraryLock(lockPlayerLibrary.isSelected());

                JCheckBox useWebRTCCheckBox = getUseWebRTCCheckBox();
                prefs.setKeyUseWebrtc(
                    useWebRTCCheckBox.isEnabled() && useWebRTCCheckBox.isSelected());

                dialogFactory.setDialogResult(GenericDialog.AFFIRM).closeDialog();
              }
            });
    getRPToolsAlias()
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              private void checkName() {
                getUseWebRTCCheckBox().setEnabled(!getRPToolsAlias().getText().isEmpty());
              }

              @Override
              public void insertUpdate(DocumentEvent e) {
                checkName();
              }

              @Override
              public void removeUpdate(DocumentEvent e) {
                checkName();
              }

              @Override
              public void changedUpdate(DocumentEvent e) {
                checkName();
              }
            });
  }
}
