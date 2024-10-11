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

import static org.apache.commons.text.WordUtils.capitalize;
import static org.apache.commons.text.WordUtils.uncapitalize;

import com.google.protobuf.util.JsonFormat;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
import net.rptools.maptool.server.proto.CampaignPropertiesDto;
import net.rptools.maptool.util.LightSyntax;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.SightSyntax;
import org.apache.commons.text.*;

public class CampaignPropertiesDialog extends JDialog {
  // private static final Logger log = LogManager.getLogger(CampaignPropertiesDialog.class);

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
    tokenPropertiesPanel.prettify();
  }

  public JTextField getNewServerTextField() {
    return formPanel.getTextField("newServer");
  }

  private void initHelp() {
    /* simple check to see if one of the keys has been translated from English. */
    // TODO: this should be a proper method instead of a dodgy workaround
    boolean isTranslated =
        MapTool.getLanguage().toLowerCase().startsWith("en")
            || !I18N.getText("sightLight.optionDescription.shape")
                .equalsIgnoreCase(
                    "Shape may be {0}(beam), {1}(circle), {2}(cone), {3}(grid), {4}(hexagon), or {5}(square).");
    /* use old text if new text not available */
    String[] helpText =
        isTranslated
            ? generateHelpText()
            : new String[] {
              I18N.getText("CampaignPropertiesDialog.label.sight"),
              I18N.getText("CampaignPropertiesDialog.label.light")
            };
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
          if (newRepo == null || newRepo.isEmpty()) {
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
      tokenPropertiesPanel.finalizeCellEditing();
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
                if (selectedFile.getName().endsWith(".mtprops")) {
                  PersistenceUtil.saveCampaignProperties(campaign, chooser.getSelectedFile());
                  MapTool.showInformation("Properties Saved.");
                } else {
                  MapTool.showMessage(
                      "CampaignPropertiesDialog.export.message",
                      "msg.title.exportProperties",
                      JOptionPane.INFORMATION_MESSAGE);
                  CampaignPropertiesDto campaignPropertiesDto =
                      MapTool.getCampaign().getCampaignProperties().toDto();
                  FileOutputStream fos = new FileOutputStream(chooser.getSelectedFile());
                  fos.write(JsonFormat.printer().print(campaignPropertiesDto).getBytes());
                }

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
   *
   * @return Map of keys to translations
   */
  private Map<String, String> createSightLightHelpTextMap() {
    Map<String, String> parameters = new HashMap<>();
    /* cell formatting string */
    parameters.put("alignCellCenter", " align=center");
    /* Useful words and phrases */
    parameters.put("phraseMultipleEntriesAllowed", I18N.getText("phrase.multipleEntriesAllowed"));
    parameters.put("mapVisionDistance", I18N.getText("sight.default.distance"));

    /* Build list of useful words and phrases */
    List<String> helpKeys = I18N.getMatchingKeys(Pattern.compile("^word."));
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^option.type.")));
    /* Shape names */
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^shape.type.name.")));
    /* example text */
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^sight.example.")));
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^light.example.")));

    /* Generate parameter map from list */
    for (String key : helpKeys) {
      parameters.putIfAbsent(
          uncapitalize(capitalize(key, '.').replace(".", "")), I18N.getText(key));
    }

    /* Parameterised strings - need to be done individually */
    parameters.put(
        "wikiLinkReferral",
        I18N.getText(
            "sightLight.wikiLinkReferral",
            "<i>wiki.rptools.info/index.php/Introduction_to_Lights_and_Sights</i>"));
    if (MapTool.getLanguage().toLowerCase().startsWith("en")) {
      /* remove translated version of words for English locales. */
      parameters.put(
          "optionDescriptionShape",
          I18N.getText("sightLight.optionDescription.shape", "", "", "", "", "", "")
              .replace("(", "")
              .replace(")", ""));
    } else {
      parameters.put(
          "optionDescriptionShape",
          I18N.getText(
              "sightLight.optionDescription.shape",
              parameters.get("shapeTypeNameBeam"),
              parameters.get("shapeTypeNameCircle"),
              parameters.get("shapeTypeNameCone"),
              parameters.get("shapeTypeNameGrid"),
              parameters.get("shapeTypeNameHexagon"),
              parameters.get("shapeTypeNameSquare")));
    }
    parameters.put(
        "optionDescriptionPersonalSightComponentColor",
        I18N.getText("sightLight.optionDescription.personalSight.component.color", "#rrggbb"));
    parameters.put(
        "optionDescriptionRestriction",
        I18N.getText("sightLight.optionDescription.restriction", "gm", "owner"));

    /* everything else */
    helpKeys = I18N.getMatchingKeys("sightLight");
    for (String key : helpKeys) {
      parameters.putIfAbsent(
          uncapitalize(
              capitalize(key, new char[] {'.'}).replace("SightLight", "").replace(".", "")),
          I18N.getText(key));
    }
    return parameters;
  }

  /**
   * Creates HTML for both sight and light help
   *
   * @return String[]
   */
  private String[] generateHelpText() {
    Map<String, String> parameters = createSightLightHelpTextMap();
    /* html building blocks */
    String wikiLink = "<font size=4>${wikiLinkReferral}</font><br>";
    String structureListStart =
        """
            <u><font size=5>${subheadingStructure}</font></u><br><br>
            <ul compact>
            <li>${structureListItemLines}</li>
            <li>${structureListItemMeasurement}</li>
            <li>${structureListItemDefaults}</li>
            <li>${structureListItemComments}</li>
            <li>${structureListItemLetterCase}</li>
            """;
    String structureListLight =
        """
            <li>${structureListItemMultiple}<sup>1</sup></li>
            <li>${structureListItemGroupName}</li>
            <li>${structureListItemGroupedNames}</li>
            <li>${structureListItemGroups}</li>
            <li>${structureListItemSorting}</li>
            """;
    String structureListClose = "</ul>";
    String syntaxHeading = "<u><font size=5>${subheadingDefinitionSyntax}</font></u><br><br>";
    String syntaxSight =
        """
              <code>
              <font size=4>[ ${syntaxLabelName} ] <b>:</b> [ ${optionLabelShape} [ ${optionLabelArc} ${optionLabelWidth} ${optionLabelOffset}]] [ ${optionLabelDistance} ] [ ${optionLabelScale} ] [ ${optionLabelMagnifier} ] [ ${optionLabelPersonalSight} ]</font><br>
              </code>
              """;
    String syntaxLight =
        """
            <code>
            <font size=4>${syntaxLabelGroupName}<br>
            -------<br>
            [ ${syntaxLabelName} ] : [ ${optionLabelAura} [ ${optionLabelRestriction} ]] [ ${optionLabelIgnoresVBL} ] [ ${optionLabelShape} [ ${optionLabelArc} ${optionLabelWidth} ${optionLabelOffset} ]] [ ${optionLabelScale} ] [ ${optionLabelRange}|${optionLabelColor}|${optionLabelLumens} ]...<sup>1</sup></font><br>
            </code>
            """;
    /*
     * Tabular options presentation
     * Columns are; Option Name, Option Type, Description, Default Value, Example
     */
    String optionsTableStart =
        """
            <br>
            <hr>
            <font size=5>${syntaxLabelOptions}</font><br>
            <table border=1 cellpadding=3 cellspacing=0>
            <tr>
              <th>${columnHeadingOption}</th>
              <th>${columnHeadingOptionType}</th>
              <th>${columnHeadingOptionDescription}</th>
              <th>${columnHeadingOptionDefaultValue}</th>
              <th>${wordExample}</th>
            </tr>
            <tr>
              <th>${optionLabelShape}</th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionShape} ${phraseMultipleEntriesAllowed}<sup>2</sup></td>
              <td${alignCellCenter}>circle</td>
              <td${alignCellCenter}>cone</td>
            </tr>
            <tr>
              <th>${optionLabelArc}</th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue} (${wordInteger})</td>
              <td>${optionDescriptionArc}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>arc=120</td>
            </tr>
            <tr>
              <th>${optionLabelWidth}</th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue}</td>
              <td>${optionDescriptionWidth}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>width=0.4</td>
            </tr>
            <tr>
              <th>${optionLabelOffset}</th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue} (${wordInteger})</td>
              <td>${optionDescriptionOffset1} ${optionDescriptionOffset2}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>offset=140</td>
            </tr>
            """;
    String optionsTableLightRows =
        """
            <tr>
              <th>${optionLabelAura}</th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionAura}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>aura</td>
            </tr>
            <tr>
              <th>${optionLabelIgnoresVBL}</th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionIgnoresVBL}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>ignores-vbl</td>
            </tr>
            <tr>
              <th>${optionLabelRestriction}</th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionRestriction}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>owner</td>
            </tr>
            <tr>
              <th>${optionLabelRange}</th>
              <td${alignCellCenter}>${optionTypeSpecial}(${wordString})</td>
              <td>${optionDescriptionLightComponents} ${phraseMultipleEntriesAllowed}<sup>3</sup></td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>30#afafaa+100</td>
            </tr>
            <tr>
              <th></th>
              <th>${columnHeadingOptionComponent}</th>
              <th>${wordSyntax}&nbsp;&#10233;&nbsp; 00|#rrggbb|+y&nbsp;&nbsp;(${optionLabelRange}|${optionLabelColor}|${optionLabelLumens})</th>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelRange}</th>
              <td>${optionDescriptionRange}</td>
              <td></td>
              <td${alignCellCenter}>30</td>
            </tr>
            """;
    String optionsTableSightRows =
        """
            <tr>
              <th>${optionLabelDistance}</th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue}</td>
              <td>${optionDescriptionDistance}</td>
              <td${alignCellCenter}>${mapVisionDistance}</td>
              <td${alignCellCenter}>distance=120</td>
            </tr>
            <tr>
              <th>${optionLabelScale}</th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionScale}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>scale</td>
            </tr>
            <tr>
              <th>${optionLabelMagnifier}</th>
              <td${alignCellCenter}>${optionTypePrefixedValue}</td>
              <td><i>[ x0.0 ]</i> ${optionDescriptionMagnifier}</td>
              <td${alignCellCenter}>x1</td>
              <td${alignCellCenter}>x2.5</td>
            </tr>
            <tr>
              <th>${optionLabelPersonalSight}</th>
              <td${alignCellCenter}>${optionTypeSpecial}(${wordString})</td>
              <td>${optionDescriptionPersonalSight} ${phraseMultipleEntriesAllowed}<sup>3</sup></td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}>r30#afafaa+100</td>
            </tr>
            <tr>
              <th></th>
              <th>${columnHeadingOptionComponent}</th>
              <th>${wordSyntax}&nbsp;&#10233;&nbsp;r00|#rrggbb|+y&nbsp;&nbsp;(${optionLabelRange}|${optionLabelColor}|${optionLabelLumens})</th>
              <td></td>
              <td></td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelRange}</th>
              <td><i>[${optionTypePrefixedValue} "r"]</i> ${optionDescriptionPersonalSightComponentRange}</td>
              <td></td>
              <td${alignCellCenter}>r30</td>
            </tr>
            """;
    String optionsTableEnd =
        """
            <tr>
              <th></th>
              <th>${optionLabelColor}</th>
              <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalSightComponentColor}</td>
              <td></td>
              <td${alignCellCenter}>#afafaa</td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelLumens}</th>
              <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalSightComponentLumens}<sup>4</sup></td>
              <td${alignCellCenter}>+100</td>
              <td${alignCellCenter}>+100</td>
            </tr>
            </table>
            """;
    String footnotesSight =
        """
            <ol start=2>
            <li>${footnoteMultipleShapes1} ${footnoteMultipleShapes2}</li>
            <li>${footnoteMultipleRangeColourLumens}</li>
            <li>${footnoteLumensLine1}<br>${footnoteLumensLine2}</li>
            </ol>
            """;
    String footnotesLight =
        """
            <ol>
            <li>${footnoteMultipleLights}</li>
            <li>${footnoteMultipleShapes1} ${footnoteMultipleShapes2}</li>
            <li>${footnoteMultipleRangeColourLumens}</li>
            <li>${footnoteLumensLine1}<br>${footnoteLumensLine2}</li>
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
            - ${sightExampleComment}<br>
            ${sightExampleNameBlind}      : r10000-1000<br></font></code>
            """;
    String examplesLight =
        """
            <font size=4>${lightExampleGroupName}<br>
            ${lightExampleNameLantern} :  circle 4#ffffaa cone arc=300 7.5#666600 circle 10#000000<sup>1</sup><br>
            ${lightExampleNameStreetLight} :  cone arc=350 1 10.05#aaaaaa arc=230 10 22.05#444444 arc=220 22 30#000000<br>
            ${lightExampleNameForwardArcAura} : aura owner cone arc=90 25#00ff00<br></code><br>
            <br>
            <font size=4>${lightExampleAurasGroupName}<br>
            ---- ${sightExampleComment}<br>
            <code>&nbsp;1. ${lightExampleNameAuraGmRedSquare} : aura square GM 2.5#ff0000</code><br>
            <code>&nbsp;2. ${lightExampleNameAuraGmRed} : aura GM 7.5#ff0000</code><br>
            <code>&nbsp;3. ${lightExampleNameAuraOwner}: aura owner 7.5#00ff00</code><br>
            <code>&nbsp;4. ${lightExampleNameAuraAllPlayers} : aura 7.5#0000ff</code><br>
            <code>&nbsp;5. ${lightExampleNameAuraSideFields}: aura cone arc=90 12.5#6666ff offset=90  12.5#aadd00 offset=-90  12.5#aadd00 offset=180  12.5#bb00aa</code><br>
            <code>&nbsp;6. ${lightExampleNameAuraDonutHole}: aura circle 20 40#ffff00</code><br>
            <code>&nbsp;7. ${lightExampleNameAuraDonutCone}: aura cone arc=30 10 20#ffff00</code><br>
            <code>&nbsp;8. ${lightExampleNameAuraRangeCircles} 30/60/90: aura circle 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000</code><br>
            <code>&nbsp;9. ${lightExampleNameAuraRangeArcs} 30/60/90: aura cone arc=135 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000</code><br>
            <code>10. ${lightExampleNameAuraLineOfSight}: aura beam width=0.4 150#ffff00</code><br>
            <br>
            <code>&nbsp;1. </code>${lightExampleTextAuraGmRedSquare}<br>
            <code>&nbsp;2. </code>${lightExampleTextAuraGmRed}<br>
            <code>&nbsp;3. </code>${lightExampleTextAuraOwner}<br>
            <code>&nbsp;4. </code>${lightExampleTextAuraAllPlayers}<br>
            <code>&nbsp;5. </code>${lightExampleTextAuraSideFields}<br>
            <code>&nbsp;6. </code>${lightExampleTextAuraDonutHole}<br>
            <code>&nbsp;7. </code>${lightExampleTextAuraDonutCone}<br>
            <code>&nbsp;8. </code>${lightExampleTextAuraRangeCircles}<br>
            <code>&nbsp;9. </code>${lightExampleTextAuraRangeArcs}<br>
            <code>10. </code>${lightExampleTextAuraLineOfSight}<br>
            </font>
            """;
    String htmlLight =
        "<html><body>"
            + wikiLink
            + structureListStart
            + structureListLight
            + structureListClose
            + syntaxHeading
            + syntaxLight
            + optionsTableStart
            + optionsTableLightRows
            + optionsTableEnd
            + footnotesLight
            + examplesHeading
            + examplesLight
            + "</body></html>";
    String htmlSight =
        "<html><body>"
            + wikiLink
            + structureListStart
            + structureListClose
            + syntaxHeading
            + syntaxSight
            + optionsTableStart
            + optionsTableSightRows
            + optionsTableEnd
            + footnotesSight
            + examplesHeading
            + examplesSight
            + "</body></html>";

    StringSubstitutor substitute = new StringSubstitutor(parameters);
    String sightResult = substitute.replace(htmlSight);
    String lightResult = substitute.replace(htmlLight);
    return new String[] {sightResult, lightResult};
  }
}
