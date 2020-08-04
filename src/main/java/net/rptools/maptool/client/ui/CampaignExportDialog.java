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

import com.jeta.forms.components.panel.FormPanel;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.io.File;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.FormPanelI18N;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.CampaignExport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a dialog for saving campaigns as a previous version
 *
 * <p>This uses a modal dialog based on an Abeille form. It allows the user to select a version of
 * MapTool to save the campaign file as for backward compatibility.
 */
@SuppressWarnings("serial")
public class CampaignExportDialog extends JDialog {
  private static final Logger log = LogManager.getLogger(CampaignExportDialog.class);

  private static FormPanel mainPanel;
  private static JEditorPane versionNotesText;
  private static JComboBox selectVersionCombo;
  private static File campaignFile;
  private static int saveStatus = -1;

  private static final CampaignExportDialog instance = new CampaignExportDialog();

  public static CampaignExportDialog getInstance() {
    return instance;
  }

  private CampaignExportDialog() {
    super(MapTool.getFrame(), "Export Campaign", true);

    setDefaultCloseOperation(HIDE_ON_CLOSE);

    //
    // Initialize the panel and button actions
    //
    mainPanel = new FormPanelI18N("net/rptools/maptool/client/ui/forms/campaignExportDialog.xml");
    setLayout(new GridLayout());
    add(mainPanel);
    getRootPane().setDefaultButton((JButton) mainPanel.getButton("exportButton"));
    pack();

    mainPanel.getButton("exportButton").addActionListener(evt -> exportButtonAction());
    mainPanel
        .getButton("cancelButton")
        .addActionListener(
            evt -> {
              saveStatus = -1;
              dispose();
            });

    versionNotesText = (JEditorPane) mainPanel.getComponentByName("versionNotesText");
    versionNotesText.setEditable(false);

    selectVersionCombo = mainPanel.getComboBox("selectVersionCombo");
    initSelectVersionCombo();
  }

  @Override
  public void setVisible(boolean b) {
    SwingUtil.centerOver(this, MapTool.getFrame());
    super.setVisible(b);
  }

  private void initSelectVersionCombo() {
    DefaultComboBoxModel defaultCombo = new DefaultComboBoxModel();
    selectVersionCombo.setModel(defaultCombo);

    for (String version : CampaignExport.getVersionArray()) {
      defaultCombo.addElement(version);
    }

    versionNotesText.setText(
        I18N.getString("dialog.campaignExport.notes.version." + getVersionText()));

    selectVersionCombo.addItemListener(
        event -> {
          if (event.getStateChange() == ItemEvent.SELECTED) {
            versionNotesText.setText(
                I18N.getString("dialog.campaignExport.notes.version." + getVersionText()));
          }
        });
  }

  public String getVersionText() {
    return selectVersionCombo.getSelectedItem().toString();
  }

  public int getSaveStatus() {
    return saveStatus;
  }

  public File getCampaignFile() {
    return campaignFile;
  }

  private void exportButtonAction() {
    try {
      JFileChooser chooser = MapTool.getFrame().getSaveCmpgnFileChooser();
      saveStatus = chooser.showSaveDialog(MapTool.getFrame());
      campaignFile = chooser.getSelectedFile();
    } catch (Exception ex) {
      MapTool.showError(I18N.getString("dialog.campaignexport.error.failedExporting"), ex);
    } finally {
      setVisible(false);
    }
  }
}
