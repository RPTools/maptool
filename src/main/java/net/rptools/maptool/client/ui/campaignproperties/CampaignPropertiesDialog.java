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

import com.google.common.collect.Iterables;
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
import java.util.TreeMap;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
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
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.SightType;
import net.rptools.maptool.server.proto.CampaignPropertiesDto;
import net.rptools.maptool.util.AuraSyntax;
import net.rptools.maptool.util.LightSyntax;
import net.rptools.maptool.util.PersistenceUtil;
import net.rptools.maptool.util.SightSyntax;
import org.apache.commons.text.*;

public class CampaignPropertiesDialog extends JDialog {

  public enum Status {
    OK,
    CANCEL
  }

  private final CampaignPropertiesDialogView view;
  private TokenPropertiesManagementPanel tokenPropertiesPanel;
  private TokenStatesController tokenStatesController;
  private TokenBarController tokenBarController;

  private Status status;
  private AbeillePanel formPanel;
  private Campaign campaign;

  public CampaignPropertiesDialog(JFrame owner) {
    super(owner, I18N.getText("CampaignPropertiesDialog.label.title"), true);
    view = new CampaignPropertiesDialogView();

    initialize();

    pack();
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
    formPanel = new AbeillePanel(view.getRootComponent());

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
    String[] helpText = generateHelpText();

    HyperlinkListener hyperLinkListener =
        e -> {
          if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            MapTool.showDocument(e.getURL().toString());
          }
        };

    JEditorPane lightHelp = view.getLightHelp();
    lightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    lightHelp.addHyperlinkListener(hyperLinkListener);
    lightHelp.setText(helpText[1]);
    lightHelp.setCaretPosition(0);

    JEditorPane auraHelp = view.getAuraHelp();
    auraHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    auraHelp.addHyperlinkListener(hyperLinkListener);
    auraHelp.setText(helpText[2]);
    auraHelp.setCaretPosition(0);

    JEditorPane sightHelp = view.getSightHelp();
    sightHelp.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    sightHelp.addHyperlinkListener(hyperLinkListener);
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
          ((DefaultListModel) getRepositoryList().getModel()).addElement(newRepo);
        });
  }

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

    String sightText = new SightSyntax().stringify(campaignProperties.getSightTypeMap());
    view.getSightPanel().setText(sightText);
    view.getSightPanel().setCaretPosition(0);

    // Separate auras from lights before populating fields.
    Map<String, Map<GUID, LightSource>> lightSources = new TreeMap<>();
    Map<String, Map<GUID, LightSource>> auras = new TreeMap<>();
    for (var entry : campaignProperties.getLightSourcesMap().entrySet()) {
      String category = entry.getKey();
      for (var sourceEntry : entry.getValue().entrySet()) {
        GUID guid = sourceEntry.getKey();
        LightSource source = sourceEntry.getValue();

        Map<String, Map<GUID, LightSource>> targetMap =
            switch (source.getType()) {
              case NORMAL -> lightSources;
              case AURA -> auras;
            };
        targetMap.computeIfAbsent(category, c -> new HashMap<>()).put(guid, source);
      }
    }

    String lightText = new LightSyntax().stringifyCategorizedLights(lightSources);
    view.getLightPanel().setText(lightText);
    view.getLightPanel().setCaretPosition(0);

    String auraText = new AuraSyntax().stringifyCategorizedAuras(auras);
    view.getAuraPanel().setText(auraText);
    view.getAuraPanel().setCaretPosition(0);

    tokenStatesController.copyCampaignToUI(campaignProperties);
    tokenBarController.copyCampaignToUI(campaignProperties);
    // updateTableList();
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

    Map<String, Map<GUID, LightSource>> existingLightSources = campaign.getLightSourcesMap();

    Map<String, Map<GUID, LightSource>> newLights = new TreeMap<>();

    Map<String, Map<GUID, LightSource>> lightMap =
        commitLightMap(view.getLightPanel().getText(), existingLightSources);
    Map<String, Map<GUID, LightSource>> aurasMap =
        commitAurasMap(view.getAuraPanel().getText(), existingLightSources);

    for (var entry : Iterables.concat(lightMap.entrySet(), aurasMap.entrySet())) {
      var category = entry.getKey();
      for (var sourceEntry : entry.getValue().entrySet()) {
        var guid = sourceEntry.getKey();
        var source = sourceEntry.getValue();

        newLights.computeIfAbsent(category, c -> new HashMap<>()).put(guid, source);
      }
    }

    campaign.setLightSourcesMap(newLights);

    List<SightType> sightMap = commitSightMap(view.getSightPanel().getText());
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
   *
   * @param text The light definitions to parse.
   * @param originalLightSourcesMap Used to compare names in the new text to existing names so that
   *     GUIDs can be preserved. This is only necessary because we don't have a UI for lights.
   */
  private Map<String, Map<GUID, LightSource>> commitLightMap(
      final String text, final Map<String, Map<GUID, LightSource>> originalLightSourcesMap) {
    return new LightSyntax().parseCategorizedLights(text, originalLightSourcesMap);
  }

  /**
   * Converts the string stored in <code>getAurasPanel().getText()</code> into a Map that relates a
   * group of auras to a Map of GUID and LightSource.
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
   *
   * @param text The area definitions to parse.
   * @param originalLightSourcesMap Used to compare names in the new text to existing names so that
   *     GUIDs can be preserved. This is only necessary because we don't have a UI for auras.
   */
  private Map<String, Map<GUID, LightSource>> commitAurasMap(
      final String text, final Map<String, Map<GUID, LightSource>> originalLightSourcesMap) {
    return new AuraSyntax().parseCategorizedAuras(text, originalLightSourcesMap);
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
              copyUIToCampaign();

              JFileChooser fileChooser = MapTool.getFrame().getSaveCampaignPropsFileChooser();

              boolean tryAgain = true;
              while (tryAgain) {
                if (fileChooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
                  return;
                }
                var installDir = AppUtil.getInstallDirectory().toAbsolutePath();
                var saveDir = fileChooser.getSelectedFile().toPath().getParent().toAbsolutePath();
                if (saveDir.startsWith(installDir)) {
                  MapTool.showWarning("msg.warning.savePropToInstallDir");
                } else {
                  tryAgain = false;
                }
              }

              File selectedFile = fileChooser.getSelectedFile();
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
                  PersistenceUtil.saveCampaignProperties(campaign, fileChooser.getSelectedFile());
                  MapTool.showInformation("Properties Saved.");
                } else {
                  MapTool.showMessage(
                      "CampaignPropertiesDialog.export.message",
                      "msg.title.exportProperties",
                      JOptionPane.INFORMATION_MESSAGE);
                  CampaignPropertiesDto campaignPropertiesDto =
                      MapTool.getCampaign().getCampaignProperties().toDto();
                  FileOutputStream fos = new FileOutputStream(fileChooser.getSelectedFile());
                  fos.write(JsonFormat.printer().print(campaignPropertiesDto).getBytes());
                  fos.close();
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
    /* example text */
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^sight.example.")));
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^light.example.")));
    helpKeys.addAll(I18N.getMatchingKeys(Pattern.compile("^auras.example.")));

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
            "<a href=\"https://wiki.rptools.info/index.php/Introduction_to_Lights_and_Sights\">https://wiki.rptools.info/index.php/Introduction_to_Lights_and_Sights</a>"));

    var optionShapeDescription =
        I18N.getText(
            "sightLight.optionDescription.shape",
            "<code>beam</code>",
            "<code>circle</code>",
            "<code>cone</code>",
            "<code>grid</code>",
            "<code>hex</code>",
            "<code>square</code>");
    if (MapTool.getLanguage().toLowerCase().startsWith("en")) {
      /* remove translated version of words for English locales. */
      optionShapeDescription = optionShapeDescription.replaceAll("\\s*[(][^)]+[)]", "");
    }
    parameters.put("optionDescriptionShape", optionShapeDescription);

    parameters.put(
        "optionDescriptionPersonalLightComponentColor",
        I18N.getText("sightLight.optionDescription.personalLight.component.color", "#rrggbb"));
    parameters.put(
        "optionDescriptionAuraRestriction",
        I18N.getText(
            "sightLight.optionDescription.auraRestriction",
            "<code>gm</code>",
            "<code>owner</code>"));

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
            <h1>${subheadingStructure}</h1>
            <ul compact>
            <li>${structureListItemLines}</li>
            <li>${structureListItemMeasurement}</li>
            <li>${structureListItemDefaults}</li>
            <li>${structureListItemComments}</li>
            <li>${structureListItemLetterCase}</li>
            """;
    String structureListLight =
        """
            <li>${structureListItemMultipleLights}<sup>1</sup></li>
            <li>${structureListItemGroupName}</li>
            <li>${structureListItemGroupedNames}</li>
            <li>${structureListItemGroups}</li>
            <li>${structureListItemSorting}</li>
            """;
    String structureListAuras =
        """
                <li>${structureListItemMultipleAuras}<sup>1</sup></li>
                <li>${structureListItemGroupName}</li>
                <li>${structureListItemGroupedNames}</li>
                <li>${structureListItemGroups}</li>
                <li>${structureListItemSorting}</li>
                """;
    String structureListClose = "</ul>";
    String syntaxHeading = "<h1>${subheadingDefinitionSyntax}</h1>";
    String syntaxSight =
        """
              <pre><font size="3">
              [ ${syntaxLabelName} ] <b>:</b> [ ${optionLabelShape} [ arc= ] [ width= ] [ offset= ]] [ distance= ] [ scale ] [ ${optionLabelMagnifier} ] ([ ${optionLabelPersonalLight} ])...<sup>1</sup>
              </font></pre>
              """;
    String syntaxLight =
        """
            <pre><font size="3">
            ${syntaxLabelGroupName}
            -------
            [ ${syntaxLabelName} ] : [ scale ] [ ignores-vbl ] ([ ${optionLabelShape} [ arc= ] [ width= ] [ offset= ]] [ ${optionLabelRange}|${optionLabelColor}|${optionLabelLumens} ])...<sup>1</sup>
            </font></pre>
            """;
    String syntaxAuras =
        """
                <pre><font size="3">
                ${syntaxLabelGroupName}
                -------
                [ ${syntaxLabelName} ] : [ scale ] [ ignores-vbl ] ([ ${optionLabelRestriction} ] [ ${optionLabelShape} [ arc= ] [ width= ] [ offset= ]] [ ${optionLabelRange}|${optionLabelColor} ])...<sup>1</sup>
                </font></pre>
                """;
    /*
     * Tabular options presentation
     * Columns are; Option Name, Option Type, Description, Default Value, Example
     */
    String optionsTableStart =
        """
            <h2>${syntaxLabelOptions}</h2>
            <table border=1 cellpadding=3 cellspacing=0>
            <tr>
              <th>${columnHeadingOption}</th>
              <th>${columnHeadingOptionType}</th>
              <th>${columnHeadingOptionDescription}</th>
              <th>${columnHeadingOptionDefaultValue}</th>
              <th>${wordExample}</th>
            </tr>
            <tr>
              <th>${syntaxLabelName}</th>
              <td${alignCellCenter}>${wordString}</td>
              <td>${optionDescriptionName}</td>
              <td${alignCellCenter}>&mdash;</td>
              <td${alignCellCenter}>Torch</td>
            </tr>
            <tr>
              <th>${optionLabelShape}</th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionShape} ${phraseMultipleEntriesAllowed}<sup>2</sup></td>
              <td${alignCellCenter}><code>circle</code></td>
              <td${alignCellCenter}><code>cone</code></td>
            </tr>
            <tr>
              <th><code>arc=</code></th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue} (${wordInteger})</td>
              <td>${optionDescriptionArc}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>arc=120</code></td>
            </tr>
            <tr>
              <th><code>width=</code></th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue}</td>
              <td>${optionDescriptionWidth}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>width=0.4</code></td>
            </tr>
            <tr>
              <th><code>offset=</code></th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue} (${wordInteger})</td>
              <td>${optionDescriptionOffset1} ${optionDescriptionOffset2}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>offset=140</code></td>
            </tr>
            """;
    String optionsTableLightRows =
        """
            <tr>
              <th><code>scale</code></th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionScale}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>scale</code></td>
            </tr>
            <tr>
              <th><code>ignores-vbl</code></th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionIgnoresVBL}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>ignores-vbl</code></td>
            </tr>
            <tr>
              <th>${optionLabelRange}</th>
              <td${alignCellCenter}>${optionTypeSpecial} (${wordString})</td>
              <td>${optionDescriptionLightComponents} ${phraseMultipleEntriesAllowed}<sup>3</sup></td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>30#afafaa+100</code></td>
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
              <td${alignCellCenter}><code>30</code></td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelColor}</th>
              <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalLightComponentColor}</td>
              <td>${wordUnused}</td>
              <td${alignCellCenter}><code>#afafaa</code></td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelLumens}</th>
              <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalLightComponentLumens}<sup>4</sup></td>
              <td${alignCellCenter}><code>+100</code></td>
              <td${alignCellCenter}><code>+100</code></td>
            </tr>
            """;
    String optionsTableAurasRows =
        """
                <tr>
                  <th><code>scale</code></th>
                  <td${alignCellCenter}>${optionTypeKeyword}</td>
                  <td>${optionDescriptionScale}</td>
                  <td${alignCellCenter}>${wordUnused}</td>
                  <td${alignCellCenter}><code>scale</code></td>
                </tr>
                <tr>
                  <th><code>ignores-vbl</code></th>
                  <td${alignCellCenter}>${optionTypeKeyword}</td>
                  <td>${optionDescriptionIgnoresVBL}</td>
                  <td${alignCellCenter}>${wordUnused}</td>
                  <td${alignCellCenter}><code>ignores-vbl</code></td>
                </tr>
                <tr>
                  <th>${optionLabelRestriction}</th>
                  <td${alignCellCenter}>${optionTypeKeyword}</td>
                  <td>${optionDescriptionAuraRestriction}</td>
                  <td${alignCellCenter}>${wordUnused}</td>
                  <td${alignCellCenter}><code>owner</code></td>
                </tr>
                <tr>
                  <th>${optionLabelRange}</th>
                  <td${alignCellCenter}>${optionTypeSpecial} (${wordString})</td>
                  <td>${optionDescriptionLightComponents} ${phraseMultipleEntriesAllowed}<sup>3</sup></td>
                  <td${alignCellCenter}>${wordUnused}</td>
                  <td${alignCellCenter}><code>30#afafaa</code></td>
                </tr>
                <tr>
                  <th></th>
                  <th>${columnHeadingOptionComponent}</th>
                  <th>${wordSyntax}&nbsp;&#10233;&nbsp; 00|#rrggbb|&nbsp;&nbsp;(${optionLabelRange}|${optionLabelColor})</th>
                  <td></td>
                  <td></td>
                </tr>
                <tr>
                  <th></th>
                  <th>${optionLabelRange}</th>
                  <td>${optionDescriptionRange}</td>
                  <td></td>
                  <td${alignCellCenter}><code>30</code></td>
                </tr>
                <tr>
                  <th></th>
                  <th>${optionLabelColor}</th>
                  <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalLightComponentColor}</td>
                  <td>${wordUnused}</td>
                  <td${alignCellCenter}><code>#afafaa</code></td>
                </tr>
                """;
    String optionsTableSightRows =
        """
            <tr>
              <th><code>distance=</code></th>
              <td${alignCellCenter}>${optionTypeKeyEqualsValue}</td>
              <td>${optionDescriptionDistance}</td>
              <td${alignCellCenter}>${mapVisionDistance}</td>
              <td${alignCellCenter}><code>distance=120</code></td>
            </tr>
            <tr>
              <th><code>scale</code></th>
              <td${alignCellCenter}>${optionTypeKeyword}</td>
              <td>${optionDescriptionScale}</td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>scale</code></td>
            </tr>
            <tr>
              <th>${optionLabelMagnifier}</th>
              <td${alignCellCenter}>${optionTypePrefixedValue}</td>
              <td><i>[ x0.0 ]</i> ${optionDescriptionMagnifier}</td>
              <td${alignCellCenter}><code>x1</code></td>
              <td${alignCellCenter}><code>x2.5</code></td>
            </tr>
            <tr>
              <th>${optionLabelPersonalLight}</th>
              <td${alignCellCenter}>${optionTypeSpecial} (${wordString})</td>
              <td>${optionDescriptionPersonalLight} ${phraseMultipleEntriesAllowed}<sup>3</sup></td>
              <td${alignCellCenter}>${wordUnused}</td>
              <td${alignCellCenter}><code>r30#afafaa+100</code></td>
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
              <td><i>[${optionTypePrefixedValue} "r"]</i> ${optionDescriptionPersonalLightComponentRange}</td>
              <td></td>
              <td${alignCellCenter}><code>r30</code></td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelColor}</th>
              <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalLightComponentColor}</td>
              <td>${wordUnused}</td>
              <td${alignCellCenter}><code>#afafaa</code></td>
            </tr>
            <tr>
              <th></th>
              <th>${optionLabelLumens}</th>
              <td><i>[${wordOptional}]</i>&nbsp;${optionDescriptionPersonalLightComponentLumens}<sup>4</sup></td>
              <td${alignCellCenter}><code>+100</code></td>
              <td${alignCellCenter}><code>+100</code></td>
            </tr>
            """;
    String optionsTableEnd =
        """
            </table>
            """;
    String footnotesSight =
        """
            <ol>
            <li>${footnoteMultipleLights}</li>
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
    String footnotesAuras =
        """
                <ol>
                <li>${footnoteMultipleLights}</li>
                <li>${footnoteMultipleShapes1} ${footnoteMultipleShapes2}</li>
                <li>${footnoteMultipleRangeColour}</li>
                </ol>
                """;
    String examplesHeading =
        """
            <hr>
            <h1>${wordExamples}</font></h1>
            """;
    String examplesSight =
        """
            <pre><font size="3">
            ${sightExampleNameNormal}: circle
            ${sightExampleNameDarkVision}: circle scale r60#000000+100
            ${sightExampleNameConeVision}: cone arc=60 distance=120
            ${sightExampleNameElfVision}: circle scale x3
            - ${sightExampleComment}
            ${sightExampleNameBlind}: r10000-1000
            </font></pre>
            <dl>
              <dt>${sightExampleNameNormal}</dt><dd>${sightExampleTextNormal}</dd>
              <dt>${sightExampleNameDarkVision}</dt><dd>${sightExampleTextDarkVision}</dd>
              <dt>${sightExampleNameConeVision}</dt><dd>${sightExampleTextConeVision}</dd>
              <dt>${sightExampleNameElfVision}</dt><dd>${sightExampleTextElfVision}</dd>
              <dt>${sightExampleNameBlind}</dt><dd>${sightExampleTextBlind}</dd>
            </dl>
            """;
    String examplesLight =
        """
            <pre><font size="3">
            ${lightExampleGroupName}
            ----
            - ${sightExampleComment}
            ${lightExampleNameLantern}:  circle 4#ffffaa cone arc=300 7.5#666600 circle 10#000000
            ${lightExampleNameStreetLight}:  cone arc=350 1 10.05#aaaaaa arc=230 10 22.05#444444 arc=220 22 30#000000
            </font></pre>
            <dl>
              <dt>${lightExampleNameLantern}</dt><dd>${lightExampleTextLantern}</dd>
              <dt>${lightExampleNameStreetLight}</dt><dd>${lightExampleTextStreetLight}</dd>
            </dl>
            """;
    String examplesAuras =
        """
                <pre><font size="3">
                ${aurasExampleGroupName}
                ----
                - ${sightExampleComment}
                ${aurasExampleNameGmRedSquare}: square GM 2.5#ff0000
                ${aurasExampleNameGmRed}: GM 7.5#ff0000
                ${aurasExampleNameOwner}: owner 7.5#00ff00
                ${aurasExampleNameAllPlayers}: 7.5#0000ff
                ${aurasExampleNameSideFields}: cone arc=90 12.5#6666ff offset=90  12.5#aadd00 offset=-90  12.5#aadd00 offset=180  12.5#bb00aa
                ${aurasExampleNameDonutHole}: circle 20 40#ffff00
                ${aurasExampleNameDonutCone}: cone arc=30 10 20#ffff00
                ${aurasExampleNameRangeCircles} 30/60/90: circle 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000
                ${aurasExampleNameRangeArcs} 30/60/90: cone arc=135 30.5 30.9#000000 60.5 60.9#000000 90.5 90.9#000000
                ${aurasExampleNameLineOfSight}: beam width=0.4 150#ffff00
                </font></pre>
                <dl>
                  <dt>${aurasExampleNameGmRedSquare}</dt><dd>${aurasExampleTextGmRedSquare}</dd>
                  <dt>${aurasExampleNameGmRed}</dt><dd>${aurasExampleTextGmRed}</dd>
                  <dt>${aurasExampleNameOwner}</dt><dd>${aurasExampleTextOwner}</dd>
                  <dt>${aurasExampleNameAllPlayers}</dt><dd>${aurasExampleTextAllPlayers}</dd>
                  <dt>${aurasExampleNameSideFields}</dt><dd>${aurasExampleTextSideFields}</dd>
                  <dt>${aurasExampleNameDonutHole}</dt><dd>${aurasExampleTextDonutHole}</dd>
                  <dt>${aurasExampleNameDonutCone}</dt><dd>${aurasExampleTextDonutCone}</dd>
                  <dt>${aurasExampleNameRangeCircles}</dt><dd>${aurasExampleTextRangeCircles}</dd>
                  <dt>${aurasExampleNameRangeArcs}</dt><dd>${aurasExampleTextRangeArcs}</dd>
                  <dt>${aurasExampleNameLineOfSight}</dt><dd>${aurasExampleTextLineOfSight}</dd>
                </dl>
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
    String htmlAuras =
        "<html><body>"
            + wikiLink
            + structureListStart
            + structureListAuras
            + structureListClose
            + syntaxHeading
            + syntaxAuras
            + optionsTableStart
            + optionsTableAurasRows
            + optionsTableEnd
            + footnotesAuras
            + examplesHeading
            + examplesAuras
            + "</body></html>";
    ;
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
    String aurasResult = substitute.replace(htmlAuras);
    return new String[] {sightResult, lightResult, aurasResult};
  }
}
