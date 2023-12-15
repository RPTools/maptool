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
package net.rptools.maptool.client.ui.campaignproperties;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.StaticMessageDialog;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Light;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.ShapeType;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.util.LightSyntax;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.SightSyntax;

public class CampaignPropertiesDialog extends JDialog {
  public enum Status {
    OK,
    CANCEL
  }

  private TokenPropertiesManagementPanel tokenPropertiesPanel;
  private TokenStatesController tokenStatesController;
  private TokenBarController tokenBarController;

  private Status status;
  private AbeillePanel formPanel;
  private Campaign campaign;

  public CampaignPropertiesDialog(JFrame owner) {
    super(owner, I18N.getText("CampaignPropertiesDialog.label.title"), true);

    initialize();

    pack(); // FJE
  }

  public Status getStatus() {
    return status;
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    } else {
      MapTool.getFrame().repaint();
    }
    super.setVisible(b);
  }

  private void initialize() {
    setLayout(new GridLayout());
    formPanel = new AbeillePanel(new CampaignPropertiesDialogView().getRootComponent());

    initTokenPropertiesDialog(formPanel);
    tokenStatesController = new TokenStatesController(formPanel);
    tokenBarController = new TokenBarController(formPanel);
    tokenBarController.setNames(tokenStatesController.getNames());

    initHelp();
    initOKButton();
    initCancelButton();
    initAddRepoButton();
    //    initAddGalleryIndexButton();
    initDeleteRepoButton();

    initImportButton();
    initExportButton();
    initImportPredefinedButton();
    initPredefinedPropertiesComboBox();

    add(formPanel);

    // Escape key
    formPanel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    formPanel
        .getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                cancel();
              }
            });
    getRootPane().setDefaultButton(getOKButton());
  }

  private void initTokenPropertiesDialog(AbeillePanel panel) {
    tokenPropertiesPanel = new TokenPropertiesManagementPanel();
    panel.replaceComponent("propertiesPanel", "tokenPropertiesPanel", tokenPropertiesPanel);
  }

  public JTextField getNewServerTextField() {
    return formPanel.getTextField("newServer");
  }

  private void initHelp() {
    JEditorPane lightHelp = (JEditorPane) formPanel.getComponent("lightHelp");
    lightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    lightHelp.setText(I18N.getString("CampaignPropertiesDialog.label.light"));
    lightHelp.setCaretPosition(0);

    JEditorPane sightHelp = (JEditorPane) formPanel.getComponent("sightHelp");
    sightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    sightHelp.setText(I18N.getString("CampaignPropertiesDialog.label.sight"));
    sightHelp.setCaretPosition(0);
  }

  private void initAddRepoButton() {
    JButton button = (JButton) formPanel.getButton("addRepoButton");
    button.addActionListener(
        e -> {
          String newRepo = getNewServerTextField().getText();
          if (newRepo == null || newRepo.length() == 0) {
            return;
          }
          // TODO: Check for uniqueness
          ((DefaultListModel) getRepositoryList().getModel()).addElement(newRepo);
        });
  }

  //  private void initAddGalleryIndexButton() {
  //    JButton button = (JButton) formPanel.getButton("addGalleryIndexButton");
  //    button.addActionListener(
  //        new ActionListener() {
  //          public void actionPerformed(ActionEvent e) {
  //            // TODO: Check for uniqueness
  //            ((DefaultListModel) getRepositoryList().getModel())
  //                .addElement("http://www.rptools.net/image-indexes/gallery.rpax.gz");
  //          }
  //        });
  //  }

  public void initDeleteRepoButton() {
    JButton button = (JButton) formPanel.getButton("deleteRepoButton");
    button.addActionListener(
        e -> {
          int[] selectedRows = getRepositoryList().getSelectedIndices();
          Arrays.sort(selectedRows);
          for (int i = selectedRows.length - 1; i >= 0; i--) {
            ((DefaultListModel) getRepositoryList().getModel()).remove(selectedRows[i]);
          }
        });
  }

  private void cancel() {
    status = Status.CANCEL;
    setVisible(false);
  }

  private void accept() {
    try {
      MapTool.getFrame()
          .showFilledGlassPane(
              new StaticMessageDialog("campaignPropertiesDialog.tokenTypeNameRename"));
      tokenPropertiesPanel
          .getRenameTypes()
          .forEach(
              (o, n) -> {
                campaign.renameTokenTypes(o, n);
              });
      MapTool.getFrame().hideGlassPane();
      copyUIToCampaign();
      AssetManager.updateRepositoryList();
      status = Status.OK;
      setVisible(false);
    } catch (IllegalArgumentException iae) {
      MapTool.showError(iae.getMessage());
    }
  }

  public void setCampaign(Campaign campaign) {
    this.campaign = campaign;
    copyCampaignToUI(campaign.getCampaignProperties());
  }

  private void copyCampaignToUI(CampaignProperties campaignProperties) {

    tokenPropertiesPanel.copyCampaignToUI(campaignProperties);
    updateRepositoryList(campaignProperties);

    String text;
    text = updateSightPanel(campaignProperties.getSightTypeMap());
    getSightPanel().setText(text);
    getSightPanel().setCaretPosition(0);

    text = updateLightPanel(campaignProperties.getLightSourcesMap());
    getLightPanel().setText(text);
    getLightPanel().setCaretPosition(0);

    tokenStatesController.copyCampaignToUI(campaignProperties);
    tokenBarController.copyCampaignToUI(campaignProperties);
    // updateTableList();
  }

  private String updateSightPanel(Map<String, SightType> sightTypeMap) {
    return new SightSyntax().stringify(sightTypeMap);
  }

  private String updateLightPanel(Map<String, Map<GUID, LightSource>> lightSources) {
    return new LightSyntax().stringifyCategorizedLights(lightSources);
  }

  private void updateRepositoryList(CampaignProperties properties) {
    DefaultListModel model = new DefaultListModel();
    for (String repo : properties.getRemoteRepositoryList()) {
      model.addElement(repo);
    }
    getRepositoryList().setModel(model);
  }

  public JList getRepositoryList() {
    return formPanel.getList("repoList");
  }

  private void copyUIToCampaign() {
    tokenPropertiesPanel.copyUIToCampaign(campaign);

    campaign.getRemoteRepositoryList().clear();
    for (int i = 0; i < getRepositoryList().getModel().getSize(); i++) {
      String repo = (String) getRepositoryList().getModel().getElementAt(i);
      campaign.getRemoteRepositoryList().add(repo);
    }
    Map<String, Map<GUID, LightSource>> lightMap;
    lightMap = commitLightMap(getLightPanel().getText(), campaign.getLightSourcesMap());
    campaign.getLightSourcesMap().clear();
    campaign.getLightSourcesMap().putAll(lightMap);

    List<SightType> sightMap = commitSightMap(getSightPanel().getText());
    campaign.setSightTypes(sightMap);

    tokenStatesController.copyUIToCampaign(campaign);
    tokenBarController.copyUIToCampaign(campaign);

    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    if (zr != null) {
      zr.getZoneView().flush();
      zr.flushFog();
      zr.flushLight();
      MapTool.getFrame().refresh();
    }
  }

  private List<SightType> commitSightMap(final String text) {
    return new SightSyntax().parse(text);
  }

  /**
   * Converts the string stored in <code>getLightPanel().getText()</code> into a Map that relates a
   * group of light sources to a Map of GUID and LightSource.
   *
   * <p>The format for the text is as follows:
   *
   * <ol>
   *   <li>Any line starting with a dash ("-") is a comment and is ignored.
   *   <li>Blank lines (those containing only zero or more spaces) are group separators.
   *   <li>The first line of a sequence is the group name.
   *   <li>Within a group, any line without a colon (":") is ignored.
   *   <li>Remaining lines are of the following format:
   *       <p><b> <code>
   *       [Gm | Owner] [Circle+ | Square | Cone] [Normal+ | Aura] [Arc=angle] [Offset=angle] distance [#rrggbb]
   *       </code> </b>
   *       <p>Brackets indicate optional components. A plus sign follows any default value for a
   *       given field. Fields starting with an uppercase letter are literal text (although they are
   *       case-insensitive). Fields that do not start with an uppercase letter represent
   *       user-supplied values, typically numbers (such as <code>angle</code>, <code>distance
   *       </code>, and <code>#rrggbb</code>). The <code>GM</code>/<code>Owner</code> field is only
   *       valid for Auras.
   * </ol>
   */
  private Map<String, Map<GUID, LightSource>> commitLightMap(
      final String text, final Map<String, Map<GUID, LightSource>> originalLightSourcesMap) {
    return new LightSyntax().parseCategorizedLights(text, originalLightSourcesMap);
  }

  public JEditorPane getLightPanel() {
    return (JEditorPane) formPanel.getTextComponent("lightPanel");
  }

  public JEditorPane getSightPanel() {
    return (JEditorPane) formPanel.getTextComponent("sightPanel");
  }

  public JTextArea getTokenPropertiesTextArea() {
    return (JTextArea) formPanel.getTextComponent("tokenProperties");
  }

  public JButton getOKButton() {
    return (JButton) formPanel.getButton("okButton");
  }

  private void initOKButton() {
    getOKButton().addActionListener(e -> accept());
  }

  public JButton getCancelButton() {
    return (JButton) formPanel.getButton("cancelButton");
  }

  public JButton getImportButton() {
    return (JButton) formPanel.getButton("importButton");
  }

  public JButton getExportButton() {
    return (JButton) formPanel.getButton("exportButton");
  }

  public JButton getImportPredefinedButton() {
    return (JButton) formPanel.getButton("importPredefinedButton");
  }

  public JComboBox<String> getPredefinedPropertiesComboBox() {
    return (JComboBox<String>) formPanel.getComboBox("predefinedPropertiesComboBox");
  }

  private void initCancelButton() {
    getCancelButton()
        .addActionListener(
            e -> {
              status = Status.CANCEL;
              setVisible(false);
            });
  }

  private void initImportButton() {
    getImportButton()
        .addActionListener(
            e -> {
              JFileChooser chooser = MapTool.getFrame().getLoadPropsFileChooser();

              if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) return;

              final File selectedFile = chooser.getSelectedFile();
              EventQueue.invokeLater(
                  () -> {
                    CampaignProperties properties =
                        PersistenceUtil.loadCampaignProperties(selectedFile);
                    // TODO: Allow specifying whether it is a replace or merge
                    if (properties != null) {
                      MapTool.getCampaign().mergeCampaignProperties(properties);
                      copyCampaignToUI(properties);
                    }
                  });
            });
  }

  private void initExportButton() {
    getExportButton()
        .addActionListener(
            e -> {
              // TODO: Remove this hack. Specifically, make the export use a properties object
              // composed of the current dialog entries instead of directly from the campaign
              copyUIToCampaign();
              // END HACK

              JFileChooser chooser = MapTool.getFrame().getSavePropsFileChooser();
              boolean tryAgain = true;
              while (tryAgain) {
                if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                  return;
                }
                var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
                var saveDir = chooser.getSelectedFile().toPath().getParent().toAbsolutePath();
                if (saveDir.startsWith(installDir)) {
                  MapTool.showWarning("msg.warning.savePropToInstallDir");
                } else {
                  tryAgain = false;
                }
              }

              File selectedFile = chooser.getSelectedFile();
              if (selectedFile.exists()) {
                if (selectedFile.getName().endsWith(".rpgame")) {
                  if (!MapTool.confirm("Import into game settings file?")) {
                    return;
                  }
                } else if (!MapTool.confirm("Overwrite existing file?")) {
                  return;
                }
              }
              try {
                PersistenceUtil.saveCampaignProperties(campaign, chooser.getSelectedFile());
                MapTool.showInformation("Properties Saved.");
              } catch (IOException ioe) {
                MapTool.showError("Could not save properties: ", ioe);
              }
            });
  }

  private void initImportPredefinedButton() {
    getImportPredefinedButton()
        .addActionListener(
            new ActionListener() {

              private File getSelectedPropertyFile() {
                String property = (String) getPredefinedPropertiesComboBox().getSelectedItem();
                return new File(
                    AppConstants.CAMPAIGN_PROPERTIES_DIR,
                    property + AppConstants.CAMPAIGN_PROPERTIES_FILE_EXTENSION);
              }

              @Override
              public void actionPerformed(ActionEvent e) {
                File selectedFile = getSelectedPropertyFile();
                EventQueue.invokeLater(
                    () -> {
                      CampaignProperties properties =
                          PersistenceUtil.loadCampaignProperties(selectedFile);
                      if (properties != null) {
                        MapTool.getCampaign().mergeCampaignProperties(properties);
                        copyCampaignToUI(properties);
                      }
                    });
              }
            });
  }

  private void initPredefinedPropertiesComboBox() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
    for (File f : getPredefinedProperty()) {

      model.addElement(FileUtil.getNameWithoutExtension(f));
    }
    getPredefinedPropertiesComboBox().setModel(model);
  }

  private List<File> getPredefinedProperty() {
    File[] result = getPredefinedPropertyFiles(AppConstants.CAMPAIGN_PROPERTIES_DIR);
    if (result == null) {
      return Collections.emptyList();
    }
    return Arrays.asList(result);
  }

  protected File[] getPredefinedPropertyFiles(File propertyDir) {
    return propertyDir.listFiles(AppConstants.CAMPAIGN_PROPERTIES_FILE_FILTER);
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame("Testing campaign properties dialog syntax-specific fields");
    CampaignPropertiesDialog cpd = new CampaignPropertiesDialog(frame);
    // @formatter:off
    String lights =
        "D20\n"
            + "----\n"
            + "Lantern, Bullseye - 60 : cone arc=60 60#f0f0f0 120#330000\n"
            + "Lantern, Hooded - 30 : circle 30 60#330000 arc=60 120#f0f0f0\n"
            + "Torch - 20 : circle 20 40#330000\n"
            + "\n"
            + "Aura\n"
            + "----\n"
            + "Arc 120deg OWNERonly - 20 : owner aura arc=120 22.5#115511\n"
            + "Arc 60deg - 60 : aura cone arc=60 facing=15 62.5#77ffaa\n"
            + "Circle - 20 : aura circle 22.5#220000\n"
            + "Circle GM+Owner : aura circle GM Owner 62.5#ff8080\n"
            + "Circle GMonly : aura circle GM 62.5#ff8080\n"
            + "Fancy - 30/60/120 : aura GM circle 30 60#330000 owner arc=60 120#f0f0f0\n"
            + "\n";
    // @formatter:on
    System.out.print(lights);

    Map<String, Map<GUID, LightSource>> originalLightSourcesMap =
        new HashMap<String, Map<GUID, LightSource>>();
    Map<String, Map<GUID, LightSource>> lightMap = new HashMap<String, Map<GUID, LightSource>>();
    try {
      lightMap = cpd.commitLightMap(lights, originalLightSourcesMap);
    } catch (Exception e) {
    }

    String text = cpd.updateLightPanel(lightMap);
    System.out.print(text);

    // keySet() might be empty if an exception occurred.
    for (String string : lightMap.keySet()) {
      System.out.println("\nGroup Name: " + string);
      System.out.println("-------------");
      for (GUID guid : lightMap.get(string).keySet()) {
        LightSource ls = lightMap.get(string).get(guid);
        System.out.println(ls.getType() + ", " + ls.getName() + ":");
        for (Light light : ls.getLightList()) {
          System.out.print("  [shape=" + light.getShape());
          if (light.getShape() == ShapeType.CONE) {
            System.out.print(", arc=" + light.getArcAngle());
            System.out.print(", facing=" + light.getFacingOffset());
          }
          System.out.print(", gm=" + light.isGM());
          System.out.print(", owner=" + light.isOwnerOnly());
          System.out.print(", radius=" + light.getRadius());
          System.out.print(", color=" + light.getPaint() + "]\n");
        }
      }
    }
    System.exit(1);
  }
}
