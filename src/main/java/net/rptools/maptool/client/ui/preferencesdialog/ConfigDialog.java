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

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import javax.swing.*;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ButtonKind;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.GenericDialogFactory;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;

public class ConfigDialog extends AbeillePanel<Object> {
  private static final String CONFIG_FILE_PANEL = "configFilePanel";
  private static final String CONFIG_FILE_WARNING_PANEL = "configFileWarningPanel";
  private static final String ICON_LABEL_NAME = "configFileWarningIcon";
  private static final Icon ICON = RessourceManager.getBigIcon(Icons.WARNING);
  private static final JLabel ICON_LABEL = new JLabel(ICON);
  private static final String COPY_PATH_BUTTON_NAME = "copyCfgPath";
  private static final AbstractButton COPY_CONFIG_PATH =
      new JButton(RessourceManager.getSmallIcon(Icons.ACTION_COPY));
  private static final String APP_CFG_PATH;
  private static final String PATH_TEXT_FIELD_NAME = "cfgFilePath";
  private static final JTextField CFG_FILE_PATH_TEXT_FIELD;
  private static final JComponent DIALOG_CONTENT = new AppConfigDialog().getRootComponent();
  private final GenericDialogFactory dialogFactory = GenericDialog.getFactory();

  static {
    COPY_CONFIG_PATH.setToolTipText(
        I18N.getString("startup.preferences.button.copyCfgPath.tooltip"));
    File appCfgFile = AppUtil.getAppCfgFile();
    if (appCfgFile != null) {
      APP_CFG_PATH = appCfgFile.toString();
    } else {
      APP_CFG_PATH = "";
    }
    CFG_FILE_PATH_TEXT_FIELD = new JTextField(APP_CFG_PATH);
    COPY_CONFIG_PATH.addActionListener(
        e ->
            Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(CFG_FILE_PATH_TEXT_FIELD.getText()), null));
    CFG_FILE_PATH_TEXT_FIELD.setEditable(false);
  }

  public ConfigDialog() {
    super(DIALOG_CONTENT);
    replaceComponent(CONFIG_FILE_WARNING_PANEL, ICON_LABEL_NAME, ICON_LABEL);
    replaceComponent(CONFIG_FILE_PANEL, PATH_TEXT_FIELD_NAME, CFG_FILE_PATH_TEXT_FIELD);
    replaceComponent(CONFIG_FILE_PANEL, COPY_PATH_BUTTON_NAME, COPY_CONFIG_PATH);

    dialogFactory
        .setDialogTitle(I18N.getText("Label.startup"))
        .setContent(DIALOG_CONTENT)
        .makeModal(true)
        .setCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
        .addButton(ButtonKind.CLOSE);
  }

  public void showDialog() {
    dialogFactory.display();
  }
}
