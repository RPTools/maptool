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
package net.rptools.maptool.client.ui.token.dialog.create;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.sheet.stats.StatSheetComboBoxRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.sheet.stats.StatSheet;
import net.rptools.maptool.model.sheet.stats.StatSheetLocation;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import net.rptools.maptool.util.ImageManager;

/** This dialog is used to display all of the token states and notes to the user. */
public class NewTokenDialog extends AbeillePanel<Token> {

  /** The size used to constrain the icon. */
  public static final int SIZE = 64;

  private final Token token;
  private boolean success;

  private final int centerX;
  private final int centerY;

  private GenericDialog dialog;

  /**
   * Create a new token notes dialog.
   *
   * @param token The token being displayed.
   * @param x x value for center point of the token dialog
   * @param y y value for center point of the token dialog
   */
  public NewTokenDialog(Token token, int x, int y) {
    super(new NewTokenDialogView().getRootComponent());

    this.token = token;
    centerX = x;
    centerY = y;

    panelInit();
  }

  public void showDialog() {
    dialog =
        new GenericDialog(I18N.getString("dialog.NewToken.title"), MapTool.getFrame(), this) {
          @Override
          protected void positionInitialView() {

            // Position over the drop spot
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension size = getSize();
            int x = centerX - size.width / 2;
            int y = centerY - size.height / 2;
            if (x < 0) {
              x = 0;
            }
            if (y < 0) {
              y = 0;
            }
            if (x + size.width > screenSize.width) {
              x = screenSize.width - size.width;
            }
            if (y + size.height > screenSize.height) {
              y = screenSize.height - size.height;
            }

            setLocation(x, y);
          }
        };

    bind(token);

    getRootPane().setDefaultButton(getOKButton());
    dialog.showDialog();
  }

  public JLabel getTokenIconPanel() {
    return (JLabel) getComponent("tokenIcon");
  }

  public JTextField getNameTextField() {
    return (JTextField) getComponent("@name");
  }

  //
  // public JTextField getGMNameTextField() {
  // return (JTextField) getComponent("gmName");
  // }
  //
  // public JRadioButton getNPCTypeRadio() {
  // return (JRadioButton) getComponent("npcType");
  // }
  //
  // public JRadioButton getMarkerTypeRadio() {
  // return (JRadioButton) getComponent("markerType");
  // }
  //
  // public JRadioButton getPCTypeRadio() {
  // return (JRadioButton) getComponent("pcType");
  // }
  //
  public JButton getOKButton() {
    return (JButton) getComponent("okButton");
  }

  public JButton getCancelButton() {
    return (JButton) getComponent("cancelButton");
  }

  public JCheckBox getShowDialogCheckbox() {
    return (JCheckBox) getComponent("showDialogCheckbox");
  }

  public JComboBox getPropertyTypeComboBox() {
    return (JComboBox) getComponent("@propertyType");
  }

  public JComboBox getStatSheetComboBox() {
    return (JComboBox) getComponent("statSheetComboBox");
  }

  public JComboBox getStatSheetLocationComboBox() {
    return (JComboBox) getComponent("statSheetLocationComboBox");
  }

  // public void initNameTextField() {
  // getNameTextField().setText(token.getName());
  // }
  //
  // public void initGMNameTextField() {
  // getGMNameTextField().setText(token.getGMName());
  // }
  //
  // public void initNPCTypeRadio() {
  // getNPCTypeRadio().setSelected(true);
  // }
  //
  // public void initMarkerTypeRadio() {
  // getMarkerTypeRadio().setVisible(false);
  // }
  //
  public void initTokenIconPanel() {
    getTokenIconPanel().setPreferredSize(new Dimension(100, 100));
    getTokenIconPanel().setMinimumSize(new Dimension(100, 100));
    getTokenIconPanel().setIcon(getTokenIcon());
  }

  public void initOKButton() {
    getOKButton()
        .addActionListener(
            e -> {
              success = true;
              if (!getShowDialogCheckbox().isSelected()) {
                AppPreferences.showDialogOnNewToken.set(false);
              }
              if (getNameTextField().getText().equals("")) {
                MapTool.showError(I18N.getText("msg.error.emptyTokenName"));
                return;
              }
              if (commit()) {
                dialog.closeDialog();
              }
            });
  }

  public boolean commit() {
    if (!super.commit()) {
      return false;
    }

    var token = getModel();
    if (token == null) {
      return false;
    }

    var statSheet = (StatSheet) getStatSheetComboBox().getSelectedItem();
    var location = (StatSheetLocation) getStatSheetLocationComboBox().getSelectedItem();
    var ssManager = new StatSheetManager();
    if (statSheet == null || (statSheet.name() == null && statSheet.namespace() == null)) {
      token.useDefaultStatSheet();
    } else {
      if (location == null) {
        location = StatSheetLocation.BOTTOM_LEFT;
      }
      token.setStatSheet(new StatSheetProperties(ssManager.getId(statSheet), location));
    }

    return true;
  }

  public void initCancelButton() {
    getCancelButton()
        .addActionListener(
            e -> {
              success = false;
              dialog.closeDialog();
            });
  }

  public void initPropertyTypeComboBox() {
    var combo = getPropertyTypeComboBox();
    MapTool.getCampaign()
        .getCampaignProperties()
        .getTokenTypeMap()
        .keySet()
        .forEach(combo::addItem);
    combo.setSelectedItem(
        MapTool.getCampaign().getCampaignProperties().getDefaultTokenPropertyType());
    combo.addActionListener(
        l -> {
          if (combo.hasFocus()) {
            var type = (String) combo.getSelectedItem();
            if (type != null) {
              populateStatSheetComboBoxes(type, null);
            }
          }
        });
    populateStatSheetComboBoxes((String) combo.getSelectedItem(), null);
  }

  private void populateStatSheetComboBoxes(String propertyType, StatSheet statSheet) {
    var combo = getStatSheetComboBox();
    var locationCombo = getStatSheetLocationComboBox();
    if (propertyType == null) {
      combo.removeAllItems();
      combo.setEnabled(false);
      locationCombo.setEnabled(false);
      locationCombo.setSelectedItem(null);
      return;
    }

    var ssManager = new StatSheetManager();
    var sheet =
        MapTool.getCampaign().getCampaignProperties().getTokenTypeDefaultStatSheet(propertyType);
    if (sheet == null) {
      combo.setEnabled(false);
      combo.removeAllItems();
      locationCombo.setEnabled(false);
      locationCombo.setSelectedItem(null);
      return;
    }

    if (statSheet == null) {
      combo.removeAllItems();
      // Default Entry
      var defaultSS =
          new StatSheet(null, I18N.getText("token.statSheet.useDefault"), null, Set.of(), null);
      combo.addItem(defaultSS);
      ssManager.getStatSheets(propertyType).stream()
          .sorted(Comparator.comparing(StatSheet::description))
          .forEach(ss -> combo.addItem(ss));
      combo.setSelectedItem(defaultSS);

      combo.setEnabled(true);
      locationCombo.setEnabled(false);
      locationCombo.setSelectedItem(null);

    } else {
      var ss =
          MapTool.getCampaign().getCampaignProperties().getTokenTypeDefaultStatSheet(propertyType);
      boolean isLegacy = ssManager.isLegacyStatSheet(statSheet);
      boolean isDefault = statSheet.name() == null && statSheet.namespace() == null;
      if (isLegacy || isDefault) {
        locationCombo.setEnabled(false);
        locationCombo.setSelectedItem(null);
      } else {
        locationCombo.setEnabled(true);
        locationCombo.setSelectedItem(ss.location());
      }
    }
  }

  public void initStatSheetComboBoxes() {
    var combo = getStatSheetComboBox();
    combo.setRenderer(new StatSheetComboBoxRenderer());
    combo.addActionListener(
        l -> {
          if (combo.hasFocus()) {
            var type = (String) getPropertyTypeComboBox().getSelectedItem();
            if (type != null) {
              populateStatSheetComboBoxes(type, (StatSheet) combo.getSelectedItem());
            }
          }
        });
    var locationCombo = getStatSheetLocationComboBox();
    Arrays.stream(StatSheetLocation.values()).forEach(locationCombo::addItem);
    populateStatSheetComboBoxes((String) getPropertyTypeComboBox().getSelectedItem(), null);
  }

  public boolean isSuccess() {
    return success;
  }

  // /**
  // * Update the token to match the state of the dialog
  // */
  // public void updateToken() {
  //
  // token.setName(getNameTextField().getText());
  // token.setGMName(getGMNameTextField().getText());
  // if (getNPCTypeRadio().isSelected()) {
  // token.setType(Token.Type.NPC);
  // }
  // if (getPCTypeRadio().isSelected()) {
  // token.setType(Token.Type.PC);
  // }
  // if (getMarkerTypeRadio().isSelected()) {
  // token.setType(Token.Type.NPC);
  // token.setLayer(Zone.Layer.OBJECT);
  // token.setGMNote("Marker"); // In order for it to be recognized as a marker, it needs something
  // in the notes field
  // token.setVisible(false);
  // }
  // }
  //
  /**
   * Get and icon from the asset manager and scale it properly.
   *
   * @return An icon scaled to fit within a cell.
   */
  private Icon getTokenIcon() {

    // Get the base image && find the new size for the icon
    BufferedImage assetImage = ImageManager.getImageAndWait(token.getImageAssetId());

    // Need to resize?
    Dimension imgSize = new Dimension(assetImage.getWidth(), assetImage.getHeight());
    SwingUtil.constrainTo(imgSize, SIZE);
    BufferedImage image = new BufferedImage(imgSize.width, imgSize.height, Transparency.BITMASK);
    Graphics2D g = image.createGraphics();
    g.drawImage(
        assetImage,
        (SIZE - imgSize.width) / 2,
        (SIZE - imgSize.height) / 2,
        imgSize.width,
        imgSize.height,
        null);
    g.dispose();
    return new ImageIcon(image);
  }
}
