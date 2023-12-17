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
import org.apache.commons.text.StringSubstitutor;
import org.bouncycastle.jcajce.provider.asymmetric.GM;

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
    String[] helpText = generateHelpText();
    JEditorPane lightHelp = (JEditorPane) formPanel.getComponent("lightHelp");
    lightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    lightHelp.setText(helpText[1]);
    lightHelp.setCaretPosition(0);

    JEditorPane sightHelp = (JEditorPane) formPanel.getComponent("sightHelp");
    sightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    sightHelp.setText(helpText[0]);
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
        """
            D20
            ----
            Lantern, Bullseye - 60 : cone arc=60 60#f0f0f0 120#330000
            Lantern, Hooded - 30 : circle 30 60#330000 arc=60 120#f0f0f0
            Torch - 20 : circle 20 40#330000

            Aura
            ----
            Arc 120deg OwnerOnly - 20 : owner aura arc=120 22.5#115511
            Arc 60deg - 60 : aura cone arc=60 facing=15 62.5#77ffaa
            Circle - 20 : aura circle 22.5#220000
            Circle GM+Owner : aura circle GM Owner 62.5#ff8080
            Circle GM Only : aura circle GM 62.5#ff8080
            Fancy - 30/60/120 : aura GM circle 30 60#330000 owner arc=60 120#f0f0f0
            Ranges 30/60/90: aura circle 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000
            """;
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

    /**
     * Fetches all the translations necessary to construct the sight and light help text
     * @return Map of keys to translations
     */
    private Map<String, String> createSightLightHelpTextMap() {
    Map<String, String> parameters = new HashMap();
    /* Useful words and phrases */
    parameters.put("wordSyntax", I18N.getText("word.syntax"));
    parameters.put("wordOptional", I18N.getText("word.optional"));
    parameters.put("wordUnused", I18N.getText("word.unused"));
    parameters.put("wordExamples", I18N.getText("word.examples"));
    parameters.put("multipleEntriesAllowed", I18N.getText("phrase.multipleEntriesAllowed"));

    parameters.put("optionTypeKeyword", I18N.getText("optionType.keyword"));
    parameters.put("optionTypeKeyValue", I18N.getText("optionType.key.equals.value"));
    parameters.put("optionTypePrefixedValue", I18N.getText("optionType.prefixed.value"));
    parameters.put("optionTypeSpecial", I18N.getText("optionType.special"));
    /* Shape names */
    parameters.put("shapeTypeBeam", I18N.getText("shape.type.name.beam"));
    parameters.put("shapeTypeCircle", I18N.getText("shape.type.name.circle"));
    parameters.put("shapeTypeCone", I18N.getText("shape.type.name.cone"));
    parameters.put("shapeTypeGrid", I18N.getText("shape.type.name.grid"));
    parameters.put("shapeTypeHexagon", I18N.getText("shape.type.name.hexagon"));
    parameters.put("shapeTypeSquare", I18N.getText("shape.type.name.square"));

    /* Structure and syntax fields */
    parameters.put(
        "wikiLinkReferral",
        I18N.getText(
            "sightLight.wikiLinkReferral",
            "<i>wiki.rptools.info/index.php/Introduction_to_Lights_and_Sights</i>"));
    parameters.put("subheadingStructure", I18N.getText("sightLight.subheading.structure"));
    parameters.put("structureLines", I18N.getText("sightLight.structure.listItem.lines"));
    parameters.put("structureMeasure", I18N.getText("sightLight.structure.listItem.measurement"));
    parameters.put("structureDefaults", I18N.getText("sightLight.structure.listItem.defaults"));
    parameters.put("structureComments", I18N.getText("sightLight.structure.listItem.comments"));
    parameters.put("structureCase", I18N.getText("sightLight.structure.listItem.letterCase"));
    parameters.put("structureMultiple", I18N.getText("sightLight.structure.listItem.multiple"));
    parameters.put("structureLabel", I18N.getText("sightLight.structure.label"));
    parameters.put(
        "structureGroupName",
        I18N.getText(
                "sightLight.structure.listItem.groupName",
            I18N.getText("panel.MapExplorer.View.LIGHT_SOURCES")));
    parameters.put("structureGroupNameLabel", I18N.getText("sightLight.syntax.label.groupName"));
    parameters.put("structureGroupedNames", I18N.getText("sightLight.structure.listItem.groupedNames"));
    parameters.put("structureGroups", I18N.getText("sightLight.structure.listItem.groups"));
    parameters.put("structureSorting", I18N.getText("sightLight.structure.listItem.sorting"));

    parameters.put("subheadingSyntax", I18N.getText("sightLight.subheading.definitionSyntax"));
    parameters.put("syntaxLabelName", I18N.getText("sightLight.syntax.label.name"));
    parameters.put("syntaxLabelOptions", I18N.getText("sightLight.syntax.label.options"));

    /* Option column headers */
    parameters.put("columnHeadOption", I18N.getText("sightLight.columnHeading.option"));
    parameters.put("columnHeadOptionType", I18N.getText("sightLight.columnHeading.optionType"));
    parameters.put(
        "columnHeadOptionDescription", I18N.getText("sightLight.columnHeading.optionDescription"));
    parameters.put(
        "columnHeadOptionDefaultValue",
        I18N.getText("sightLight.columnHeading.optionDefaultValue"));
    parameters.put("columnHeadOptionExample", I18N.getText("word.example"));
    parameters.put("columnHeadComponent", I18N.getText("sightLight.columnHeading.optionComponent"));

    /* Option names */
    parameters.put("labelAura", I18N.getText("sightLight.optionLabel.aura"));
    parameters.put("labelShape", I18N.getText("sightLight.optionLabel.shape"));
    parameters.put("labelDistance", I18N.getText("sightLight.optionLabel.distance"));
    parameters.put("labelScale", I18N.getText("sightLight.optionLabel.scale"));
    parameters.put("labelArc", I18N.getText("sightLight.optionLabel.arc"));
    parameters.put("labelWidth", I18N.getText("sightLight.optionLabel.width"));
    parameters.put("labelOffset", I18N.getText("sightLight.optionLabel.offset"));
    parameters.put("labelMagnifier", I18N.getText("sightLight.optionLabel.magnifier"));
    parameters.put("labelPersonalSight", I18N.getText("sightLight.optionLabel.personalSight"));
    parameters.put("labelComponent", I18N.getText("sightLight.optionLabel.component"));
    parameters.put("labelRange", I18N.getText("sightLight.optionLabel.range"));
    parameters.put("labelColor", I18N.getText("sightLight.optionLabel.color"));
    parameters.put("labelLumens", I18N.getText("sightLight.optionLabel.lumens"));
    parameters.put("labelRestriction", I18N.getText("sightLight.optionLabel.restriction"));

    /* Option descriptions */
    parameters.put("descriptionName", I18N.getText("sightLight.optionDescription.name"));
    parameters.put("descriptionAura", I18N.getText("sightLight.optionDescription.aura"));
    if (MapTool.getLanguage().toLowerCase().startsWith("en")) {
      /* remove translated version of words for English locales. */
      parameters.put(
          "descriptionShape",
          I18N.getText("sightLight.optionDescription.shape", "", "", "", "", "", "")
              .replace("(", "")
              .replace(")", ""));
    } else {
      parameters.put(
          "descriptionShape",
          I18N.getText(
              "sightLight.optionDescription.shape",
              I18N.getText("shape.type.name.beam"),
              I18N.getText("shape.type.name.circle"),
              I18N.getText("shape.type.name.cone"),
              I18N.getText("shape.type.name.grid"),
              I18N.getText("shape.type.name.hexagon"),
              I18N.getText("shape.type.name.square")));
    }
    parameters.put("descriptionRange", I18N.getText("sightLight.optionDescription.range"));
    parameters.put("descriptionDistance", I18N.getText("sightLight.optionDescription.distance"));
    parameters.put("descriptionScale", I18N.getText("sightLight.optionDescription.scale"));
    parameters.put("descriptionArc", I18N.getText("sightLight.optionDescription.arc"));
    parameters.put("descriptionWidth", I18N.getText("sightLight.optionDescription.width"));
    parameters.put("descriptionOffset1", I18N.getText("sightLight.optionDescription.offset1"));
    parameters.put("descriptionOffset2", I18N.getText("sightLight.optionDescription.offset2"));
    parameters.put("descriptionMagnifier", I18N.getText("sightLight.optionDescription.magnifier"));
    parameters.put(
        "descriptionPersonalSight", I18N.getText("sightLight.optionDescription.personalSight"));
    parameters.put(
        "descriptionPersonalSightRange",
        I18N.getText("sightLight.optionDescription.personalSight.component.range"));
    parameters.put(
        "descriptionLightComponents", I18N.getText("sightLight.optionDescription.lightComponents"));
    parameters.put(
        "descriptionColor",
        I18N.getText("sightLight.optionDescription.personalSight.component.color", "#rrggbb"));
    parameters.put(
        "descriptionLumens",
        I18N.getText("sightLight.optionDescription.personalSight.component.lumens"));
    parameters.put(
        "descriptionRestriction",
        I18N.getText("sightLight.optionDescription.restriction", "gm", "owner"));

    /* default values */
    parameters.put("mapVisionDistance", I18N.getText("sight.default.distance"));

    /* footnotes */
    parameters.put("multipleLightsFootnote", I18N.getText("sightLight.footnote.multiple.lights"));
    parameters.put(
        "multipleShapesFootnote1", I18N.getText("sightLight.footnote.multiple.shapes.1"));
    parameters.put(
        "multipleShapesFootnote2", I18N.getText("sightLight.footnote.multiple.shapes.2"));
    parameters.put(
        "multipleRangeColorLumensFootnote",
        I18N.getText("sightLight.footnote.multiple.rangeColourLumens"));
    parameters.put("lumensFootnote1", I18N.getText("sightLight.footnote.lumens.line.1"));
    parameters.put("lumensFootnote2", I18N.getText("sightLight.footnote.lumens.footnote.2"));

    /* example names - light */
    parameters.put("lightExampleNameLantern", I18N.getText("light.example.name.lantern"));
    parameters.put("lightExampleNameForwardArc", I18N.getText("light.example.name.forwardArcAura"));
    /* example names - sight */
    parameters.put("sightExampleNameDarkVision", I18N.getText("sight.example.name.darkVision"));
    parameters.put("sightExampleNameConeVision", I18N.getText("sight.example.name.coneVision"));
    parameters.put("sightExampleNameElfVision", I18N.getText("sight.example.name.elfVision"));
    parameters.put("sightExampleNameBlind", I18N.getText("sight.example.name.blind"));
    /* example names - auras */
    parameters.put("exampleAuraGroupName", I18N.getText("light.example.auras.groupName"));
    parameters.put(
        "exampleAuraNameAuraGMRedSquare", I18N.getText("light.example.name.aura.gmRedSquare"));
    parameters.put("exampleAuraNameAuraGMRed", I18N.getText("light.example.name.aura.gmRed"));
    parameters.put("exampleAuraNameAuraOwner", I18N.getText("light.example.name.aura.owner"));
    parameters.put(
        "exampleAuraNameAuraAllPlayers", I18N.getText("light.example.name.aura.allPlayers"));
    parameters.put("exampleAuraNameSideFields", I18N.getText("light.example.name.aura.sideFields"));
    parameters.put("exampleAuraNameDonutHole", I18N.getText("light.example.name.aura.donutHole"));
    parameters.put("exampleAuraNameDonutCone", I18N.getText("light.example.name.aura.donutCone"));
    parameters.put(
        "exampleAuraNameRangeCircles", I18N.getText("light.example.name.aura.rangeCircles"));
    parameters.put("exampleAuraNameRangeArcs", I18N.getText("light.example.name.aura.rangeArcs"));
    parameters.put("exampleAuraNameLoS", I18N.getText("light.example.name.aura.lineOfSight"));

    /* example descriptions - auras */
    parameters.put(
        "exampleAuraTextAuraGMRedSquare", I18N.getText("light.example.text.aura.gmRedSquare"));
    parameters.put("exampleAuraTextAuraGMRed", I18N.getText("light.example.text.aura.gmRed"));
    parameters.put("exampleAuraTextAuraOwner", I18N.getText("light.example.text.aura.owner"));
    parameters.put(
        "exampleAuraTextAuraAllPlayers", I18N.getText("light.example.text.aura.allPlayers"));
    parameters.put("exampleAuraTextSideFields", I18N.getText("light.example.text.aura.sideFields"));
    parameters.put("exampleAuraTextDonutHole", I18N.getText("light.example.text.aura.donutHole"));
    parameters.put("exampleAuraTextDonutCone", I18N.getText("light.example.text.aura.donutCone"));
    parameters.put(
        "exampleAuraTextRangeCircles", I18N.getText("light.example.text.aura.rangeCircles"));
    parameters.put("exampleAuraTextRangeArcs", I18N.getText("light.example.text.aura.rangeArcs"));
    parameters.put("exampleAuraTextLoS", I18N.getText("light.example.text.aura.lineOfSight"));

    /* cell formatting string */
    parameters.put("alignCellCenter", " align=center");
    return parameters;
  }

    /**
     * Creates HTML for both sight and light help
     * @return String[]
     */
  private String[] generateHelpText() {
    Map<String, String> parameters = createSightLightHelpTextMap();
    String structureCommon =
        """
        <html>
        <body >
        <font size=4>${wikiLinkReferral}</font><br>
        <u><font size=5>${subheadingStructure}</font></u><br>
        <ul compact>
        <li>${structureLines}</li>
        <li>${structureMeasure}</li>
        <li>${structureDefaults}</li>
        <li>${structureComments}</li>
        <li>${structureCase}</li>
        """;
    String structureLight =
        """
        <li>${structureMultiple}<sup>1</sup></li>
        <li>${structureGroupName}</li>
        <li>${structureGroupedNames}</li>
        <li>${structureGroups}</li>
        <li>${structureSorting}</li>
        """;
      String syntaxHeading =
          """
          </ul>
          <u><font size=5>${subheadingSyntax}</font></u><br><br>
          <code>""";
      String syntaxSight =
          """
          <font size=4>[ ${syntaxLabelName} ] <b>:</b> [ ${labelShape} [ ${labelArc} ${labelWidth} ${labelOffset} ]] [ ${labelDistance} ] [ ${labelScale} ] [ ${labelMagnifier} ] [ ${labelPersonalSight} ]</font><br>
          """;
      String syntaxLight =
        """
        <font size=4>${structureGroupNameLabel}<br>
        -------<br>
        [ ${syntaxLabelName} ] : [ ${labelAura} [ ${labelRestriction} ]] [ ${labelShape} [ ${labelArc} ${labelWidth} ${labelOffset} ]] [ ${labelScale} ] [ ${labelRange}|${labelColor}|${labelLumens} ]...<sup>1</sup></font><br>
        ${lightExampleNameLantern} :  circle 4#ffffaa cone arc=300 7.5#666600 circle 10#000000<sup>1</sup><br>
        ${lightExampleNameForwardArc} : aura owner cone arc=90 25#00ff00<br></code>
        """;
    /*
     * Tabular options presentation
     * Columns are; Option Name, Option Type, Description, Default Value, Example
     */
    String optionsCommon_1 =
        """
        <br>
        <hr>
        <font size=5>${syntaxLabelOptions}</font><br>
        <table border=1 cellpadding=3 cellspacing=0>
        <tr>
          <th>${columnHeadOption}</th>
          <th>${columnHeadOptionType}</th>
          <th>${columnHeadOptionDescription}</th>
          <th>${columnHeadOptionDefaultValue}</th>
          <th>${columnHeadOptionExample}</th>
        </tr>
        <tr>
          <th>${labelShape}</th>
          <td${alignCellCenter}>${optionTypeKeyword}</td>
          <td>${descriptionShape} ${multipleEntriesAllowed}<sup>2</sup></td>
          <td${alignCellCenter}>circle</td>
          <td></td>
        </tr>
        <tr>
          <th>${labelArc}</th>
          <td${alignCellCenter}>${optionTypeKeyValue}</td>
          <td>${descriptionArc}</td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td${alignCellCenter}>arc=120</td>
        </tr>
        <tr>
          <th>${labelWidth}</th>
          <td${alignCellCenter}>${optionTypeKeyValue}</td>
          <td>${descriptionWidth}</td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td${alignCellCenter}>width=0.4</td>
        </tr>
        <tr>
          <th>${labelOffset}</th>
          <td${alignCellCenter}>${optionTypeKeyValue}</td>
          <td>${descriptionOffset1} ${descriptionOffset2}</td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td${alignCellCenter}>offset=140</td>
        </tr>
        """;
    String optionsLight =
       """
        <tr>
          <th>${labelAura}</th>
          <td${alignCellCenter}>${optionTypeKeyword}</td>
          <td>${descriptionAura}</td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td></td>
        </tr>
        <tr>
          <th>${labelRestriction}</th>
          <td${alignCellCenter}>${optionTypeKeyword}</td>
          <td>${descriptionRestriction}</td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td${alignCellCenter}></td>
        </tr>
        <tr>
          <th>${labelRange}</th>
          <td${alignCellCenter}>${optionTypeSpecial}</td>
          <td>${descriptionLightComponents} ${multipleEntriesAllowed}<sup>3</sup></td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td${alignCellCenter}>30#afafaa+100</td>
        </tr>
        <tr>
          <th></th>
          <th>${columnHeadComponent}</th>
          <th>${wordSyntax}&nbsp;&#10233;&nbsp; 00|#rrggbb|+y&nbsp;&nbsp;(${labelRange}|${labelColor}|${labelLumens})</th>
          <td></td>
          <td></td>
        </tr>
        <tr>
          <th></th>
          <th>${labelRange}</th>
          <td>${descriptionRange}</td>
          <td></td>
          <td${alignCellCenter}>30</td>
        </tr>
        """;
    String optionsSight =
        """
        <tr>
          <th>${labelDistance}</th>
          <td${alignCellCenter}>${optionTypeKeyValue}</td>
          <td>${descriptionDistance}</td>
          <td${alignCellCenter}>${mapVisionDistance}</td>
          <td${alignCellCenter}>distance=120</td>
        </tr>
        <tr>
          <th>${labelScale}</th>
          <td${alignCellCenter}>${optionTypeKeyword}</td>
          <td>${descriptionScale}</td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td></td>
        </tr>
        <tr>
          <th>${labelMagnifier}</th>
          <td${alignCellCenter}>${optionTypePrefixedValue}</td>
          <td><i>[ x0.0 ]</i> ${descriptionMagnifier}</td>
          <td${alignCellCenter}>x1</td>
          <td${alignCellCenter}>x2.5</td>
        </tr>
        <tr>
          <th>${labelPersonalSight}</th>
          <td${alignCellCenter}>${optionTypeSpecial}</td>
          <td>${descriptionPersonalSight} ${multipleEntriesAllowed}<sup>3</sup></td>
          <td${alignCellCenter}>${wordUnused}</td>
          <td${alignCellCenter}>r30#afafaa+100</td>
        </tr>
        <tr>
          <th></th>
          <th>${columnHeadComponent}</th>
          <th>${wordSyntax}&nbsp;&#10233;&nbsp;r00|#rrggbb|+y&nbsp;&nbsp;(${labelRange}|${labelColor}|${labelLumens})</th>
          <td></td>
          <td></td>
        </tr>
        <tr>
          <th></th>
          <th>${labelRange}</th>
          <td><i>[${optionTypePrefixedValue} "r"]</i> ${descriptionPersonalSightRange}</td>
          <td></td>
          <td${alignCellCenter}>r30</td>
        </tr>
        """;
    String optionsCommon_2 =
        """
        <tr>
          <th></th>
          <th>${labelColor}</th>
          <td><i>[${wordOptional}]</i>&nbsp;${descriptionColor}</td>
          <td></td>
          <td${alignCellCenter}>#afafaa</td>
        </tr>
        <tr>
          <th></th>
          <th>${labelLumens}</th>
          <td><i>[${wordOptional}]</i>&nbsp;${descriptionLumens}<sup>4</sup></td>
          <td${alignCellCenter}>+100</td>
          <td${alignCellCenter}>+100</td>
        </tr>
        </table>
        """;
    String footnotesSight =
        """
        <ol start=2>
        <li>${multipleShapesFootnote1} ${multipleShapesFootnote2}</li>
        <li>${multipleRangeColorLumensFootnote}</li>
        <li>${lumensFootnote1}<br>${lumensFootnote2}</li>
        </ol>
        """;
    String footnotesLight =
        """
        <ol>
        <li>${multipleLightsFootnote}</li>
        <li>${multipleShapesFootnote1} ${multipleShapesFootnote2}</li>
        <li>${multipleRangeColorLumensFootnote}</li>
        <li>${lumensFootnote1}<br>${lumensFootnote2}</li>
        </ol>
        """;
    String examplesHeading =
        """
        <hr>
        <u><font size=5>${wordExamples}</font></u><br><br>
        """;
    String examplesSight =
        """
        <code><font size=5>${sightExampleNameDarkVision} : circle scale r60#000000+100<br>
        ${sightExampleNameConeVision} : cone arc=60 distance=120<br>
        ${sightExampleNameElfVision}  : circle scale x3<br>
        ${sightExampleNameBlind}      : r10000-1000<br></font></code>
        """;
    String examplesLight =
        """
        <font size=4>${exampleAuraGroupName}<br>
        ----<br>
        <code>&nbsp;1. ${exampleAuraNameAuraGMRedSquare} : aura square GM 2.5#ff0000</code><br>
        <code>&nbsp;2. ${exampleAuraNameAuraGMRed} : aura GM 7.5#ff0000</code><br>
        <code>&nbsp;3. ${exampleAuraNameAuraOwner}: aura owner 7.5#00ff00</code><br>
        <code>&nbsp;4. ${exampleAuraNameAuraAllPlayers} : aura 7.5#0000ff</code><br>
        <code>&nbsp;5. ${exampleAuraNameSideFields}: aura cone arc=90 12.5#6666ff offset=90  12.5#aadd00 offset=-90  12.5#aadd00 offset=180  12.5#bb00aa</code><br>
        <code>&nbsp;6. ${exampleAuraNameDonutHole}: aura circle 20 40#ffff00</code><br>
        <code>&nbsp;7. ${exampleAuraNameDonutCone}: aura cone arc=30 10 20#ffff00</code><br>
        <code>&nbsp;8. ${exampleAuraNameRangeCircles} 30/60/90: aura circle 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000</code><br>
        <code>&nbsp;9. ${exampleAuraNameRangeArcs} 30/60/90: aura cone arc=135 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000</code><br>
        <code>10. ${exampleAuraNameLoS}: aura beam width=0.4 150#ffff00</code><br>
        <br>
        <code>&nbsp;1. </code>${exampleAuraTextAuraGMRedSquare}<br>
        <code>&nbsp;2. </code>${exampleAuraTextAuraGMRed}<br>
        <code>&nbsp;3. </code>${exampleAuraTextAuraOwner}<br>
        <code>&nbsp;4. </code>${exampleAuraTextAuraAllPlayers}<br>
        <code>&nbsp;5. </code>${exampleAuraTextSideFields}<br>
        <code>&nbsp;6. </code>${exampleAuraTextDonutHole}<br>
        <code>&nbsp;7. </code>${exampleAuraTextDonutCone}<br>
        <code>&nbsp;8. </code>${exampleAuraTextRangeCircles}<br>
        <code>&nbsp;9. </code>${exampleAuraTextRangeArcs}<br>
        <code>10. </code>${exampleAuraTextLoS}<br>
        </font>
        """;

    String lightString = structureCommon + structureLight + syntaxHeading + syntaxLight + optionsCommon_1 + optionsLight + optionsCommon_2 +footnotesLight + examplesHeading + examplesLight;
    String sightString = structureCommon + syntaxHeading + syntaxSight + optionsCommon_1 + optionsSight + optionsCommon_2 +footnotesSight + examplesHeading + examplesSight;
    StringSubstitutor substitutor = new StringSubstitutor(parameters);
    String sightResult = substitutor.replace(sightString);
    String lightResult = substitutor.replace(lightString);
    return new String[] {sightResult, lightResult};
  }
}