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
package net.rptools.maptool.client.ui.token.dialog.edit;

import com.jidesoft.combobox.MultilineStringExComboBox;
import com.jidesoft.combobox.PopupPanel;
import com.jidesoft.grid.AbstractPropertyTableModel;
import com.jidesoft.grid.MultilineStringCellEditor;
import com.jidesoft.grid.NavigableModel;
import com.jidesoft.grid.Property;
import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.plaf.basic.BasicExComboBoxUI;
import com.jidesoft.swing.CheckBoxListWithSelectable;
import com.jidesoft.swing.DefaultSelectable;
import com.jidesoft.swing.Selectable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.*;
import java.awt.geom.Area;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import net.miginfocom.swing.MigLayout;
import net.rptools.lib.MD5Key;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.functions.TokenBarFunction;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.client.swing.htmleditorsplit.HtmlEditorSplit;
import net.rptools.maptool.client.ui.ImageAssetPanel;
import net.rptools.maptool.client.ui.sheet.stats.StatSheetComboBoxRenderer;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL.JTS_SimplifyMethodType;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Token.TerrainModifierOperation;
import net.rptools.maptool.model.Token.Type;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.player.Player;
import net.rptools.maptool.model.sheet.stats.StatSheet;
import net.rptools.maptool.model.sheet.stats.StatSheetLocation;
import net.rptools.maptool.model.sheet.stats.StatSheetManager;
import net.rptools.maptool.model.sheet.stats.StatSheetProperties;
import net.rptools.maptool.util.ExtractHeroLab;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;

/** This dialog is used to display all of the token states and notes to the user. */
public class EditTokenDialog extends AbeillePanel<Token> {

  /** The size used to constrain the icon. */
  public static final int SIZE = 64;

  private static final Logger log = LogManager.getLogger();
  private static final long serialVersionUID = 1295729281890170792L;
  private static final ImageIcon REFRESH_ICON_ON =
      RessourceManager.getBigIcon(Icons.EDIT_TOKEN_REFRESH_ON);
  private static final ImageIcon REFRESH_ICON_OFF =
      RessourceManager.getBigIcon(Icons.EDIT_TOKEN_REFRESH_OFF);
  // private CharSheetController controller;
  private final RSyntaxTextArea xmlStatblockRSyntaxTextArea = new RSyntaxTextArea(2, 2);
  private final RSyntaxTextArea textStatblockRSyntaxTextArea = new RSyntaxTextArea(2, 2);
  private final WordWrapCellRenderer propertyCellRenderer = new WordWrapCellRenderer();

  private boolean tokenSaved;
  private GenericDialog dialog;
  private ImageAssetPanel imagePanel;
  private final LibraryManager libraryManager = new LibraryManager();

  // private final Toolbox toolbox = new Toolbox();
  private HeroLabData heroLabData;
  private AutoGenerateTopologySwingWorker autoGenerateTopologySwingWorker =
      new AutoGenerateTopologySwingWorker(false, Color.BLACK);

  /** Create a new token notes dialog. */
  public EditTokenDialog() {
    super(new TokenPropertiesDialog().getRootComponent());
    panelInit();
  }

  public void initGMNotesEditorPane() {
    setGmNotesEnabled(MapTool.getPlayer().isGM());
  }

  public void initStatSheetComboBoxes() {
    var sheetCombo = getStatSheetCombo();
    sheetCombo.setRenderer(new StatSheetComboBoxRenderer());
    var locationCombo = getStatSheetLocationCombo();
    Arrays.stream(StatSheetLocation.values()).forEach(locationCombo::addItem);
    sheetCombo.addActionListener(
        l -> {
          var sheet = (StatSheet) sheetCombo.getSelectedItem();
          var ssManager = new StatSheetManager();
          boolean usingDefault =
              sheet != null && (sheet.name() == null && sheet.namespace() == null);
          if (sheet == null || ssManager.isLegacyStatSheet(sheet) || usingDefault) {
            locationCombo.setEnabled(false);
            locationCombo.setSelectedItem(null);
          } else {
            locationCombo.setEnabled(true);
            var tokenSheet = getModel().getStatSheet();
            if (tokenSheet != null) {
              locationCombo.setSelectedItem(tokenSheet.location());
            } else {
              var sheetProp =
                  MapTool.getCampaign().getTokenTypeDefaultSheetId(getModel().getPropertyType());
              locationCombo.setSelectedItem(sheetProp.location());
            }
          }
        });
  }

  public void initTerrainModifierOperationComboBox() {
    getTerrainModifierOperationComboBox()
        .setModel(new DefaultComboBoxModel<>(TerrainModifierOperation.values()));
  }

  public void initTerrainModifiersIgnoredList() {
    DefaultListModel<TerrainModifierOperation> operationModel = new DefaultListModel<>();
    getTerrainModifiersIgnoredList().setModel(operationModel);
    EnumSet.allOf(TerrainModifierOperation.class).forEach(operationModel::addElement);
  }

  public void initJtsMethodComboBox() {
    getJtsMethodComboBox().setModel(new DefaultComboBoxModel<>(JTS_SimplifyMethodType.values()));
  }

  public void showDialog(Token token) {
    dialog =
        new GenericDialog(I18N.getString("EditTokenDialog.msg.title"), MapTool.getFrame(), this) {
          private static final long serialVersionUID = 5439449816096482201L;

          @Override
          public void closeDialog() {
            // TODO: I don't like this. There should really be a AbeilleDialog class that does this

            if (!autoGenerateTopologySwingWorker.isDone()) {
              log.info("Stopping autoGenerateTopologySwingWorker...");
              autoGenerateTopologySwingWorker.cancel(true);
            }

            unbind();
            super.closeDialog();
          }
        };
    getTokenTopologyPanel().reset(token);
    bind(token);

    getRootPane().setDefaultButton(getOKButton());
    setGmNotesEnabled(MapTool.getPlayer().isGM());
    getComponent("@GMName").setEnabled(MapTool.getPlayer().isGM());

    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    setLibTokenPaneEnabled(token.isLibToken());
    validateLibTokenURIAccess(getNameField().getName());
    var combo = getStatSheetCombo();
    combo.removeAllItems();
    // Default Entry
    var defaultSS =
        new StatSheet(null, I18N.getText("token.statSheet.useDefault"), null, Set.of(), null);
    combo.addItem(defaultSS);
    var ssManager = new StatSheetManager();
    ssManager.getStatSheets(token.getPropertyType()).stream()
        .sorted(Comparator.comparing(StatSheet::description))
        .forEach(ss -> combo.addItem(ss));
    if (token.usingDefaultStatSheet()) {
      combo.setSelectedItem(defaultSS);
    } else {
      combo.setSelectedItem(new StatSheetManager().getStatSheet(token.getStatSheet().id()));
    }
    dialog.showDialog();
  }

  private void validateLibTokenURIAccess(String name) {
    if (!name.toLowerCase().startsWith("lib:")) {
      getLibTokenURIErrorLabel()
          .setText(I18N.getText("EditTokenDialog.libTokenURI.error.notLibToken", name));
      getAllowURLAccess().setEnabled(false);
      return;
    } else {
      if (libraryManager.usesReservedPrefix(name.substring(4))) {
        getLibTokenURIErrorLabel()
            .setText(
                I18N.getText(
                    "macro.setAllowsURIAccess.reservedPrefix",
                    libraryManager.getReservedPrefix(name.substring(4))));
        getAllowURLAccess().setEnabled(false);
        return;
      } else if (libraryManager.usesReservedName(name.substring(4))) {
        getLibTokenURIErrorLabel()
            .setText(I18N.getText("EditTokenDialog.libTokenURI.error.reserved", name));

        getAllowURLAccess().setEnabled(false);
        return;
      }
    }

    getAllowURLAccess().setEnabled(true);
    getLibTokenURIErrorLabel().setText(" ");
  }

  @Override
  public void bind(final Token token) {
    // ICON
    getTokenIconPanel().setImageId(token.getImageAssetId());

    // NOTES, GM NOTES. Due to the way things happen on different gui threads, the type must be set
    // before the text
    // otherwise the wrong values can get populated when the tab change listener fires.
    getGMNotesEditor().setTextType(token.getGmNotesType());
    getGMNotesEditor().setText(token.getGMNotes());
    getPlayerNotesEditor().setTextType(token.getNotesType());
    getPlayerNotesEditor().setText(token.getNotes());

    // TYPE
    getTypeCombo().setSelectedItem(token.getType());

    // SIGHT
    updateSightTypeCombo();

    // Image Tables
    updateImageTableCombo();

    // STATES
    Component barPanel = null;
    updateStatesPanel();
    Component[] statePanels = getStatesPanel().getComponents();
    for (Component statePanel : statePanels) {
      if ("bar".equals(statePanel.getName())) {
        barPanel = statePanel;
        continue;
      }
      Component[] states = ((Container) statePanel).getComponents();
      for (Component component : states) {
        JCheckBox state = (JCheckBox) component;
        state.setSelected(FunctionUtil.getBooleanValue(token.getState(state.getText())));
      }
    }

    // BARS
    if (barPanel != null) {
      Component[] barComponents = ((Container) barPanel).getComponents();
      JCheckBox cb = null;
      JSlider bar = null;
      for (var tokenBarPanel : barComponents) {
        for (var component : ((Container) tokenBarPanel).getComponents()) {
          if (component instanceof JCheckBox) {
            cb = (JCheckBox) component;
          } else if (component instanceof JSlider) {
            bar = (JSlider) component;
          }
        }
        if (token.getState(bar.getName()) == null) {
          cb.setSelected(true);
          bar.setEnabled(false);
          bar.setValue(100);
        } else {
          cb.setSelected(false);
          bar.setEnabled(true);
          bar.setValue(
              (int)
                  (TokenBarFunction.getBigDecimalValue(token.getState(bar.getName())).doubleValue()
                      * 100));
        }
      }
    }

    // OWNER LIST
    EventQueue.invokeLater(() -> getOwnerList().setModel(new OwnerListModel()));

    // SPEECH TABLE
    EventQueue.invokeLater(() -> getSpeechTable().setModel(new SpeechTableModel(token)));

    // Player player = MapTool.getPlayer();
    // boolean editable = player.isGM() ||
    // !MapTool.getServerPolicy().useStrictTokenManagement() ||
    // token.isOwner(player.getName());
    // getAllPlayersCheckBox().setSelected(token.isOwnedByAll());

    // OTHER
    getShapeCombo().setSelectedItem(token.getShape());
    setSizeCombo(token);

    // Updates the Property Type list.
    updatePropertyTypeCombo();

    // Set the selected item in Property Type list. Triggers a itemStateChanged event if index != 0
    getPropertyTypeCombo().setSelectedItem(token.getPropertyType());

    // If index == 0, the itemStateChanged event wasn't triggered, so we update. Fix #1504
    if (getPropertyTypeCombo().getSelectedIndex() == 0) {
      updatePropertiesTable((String) getPropertyTypeCombo().getSelectedItem());
    }

    getSightTypeCombo()
        .setSelectedItem(
            token.getSightType() != null
                ? token.getSightType()
                : MapTool.getCampaign().getCampaignProperties().getDefaultSightType());
    getCharSheetPanel().setImageId(token.getCharsheetImage());
    getPortraitPanel().setImageId(token.getPortraitImage());
    getTokenLayoutPanel().setToken(token);
    getImageTableCombo().setSelectedItem(token.getImageTableName());
    getTokenOpacitySlider()
        .setValue(new BigDecimal(token.getTokenOpacity()).multiply(new BigDecimal(100)).intValue());
    getTerrainModifier().setText(Double.toString(token.getTerrainModifier()));
    getTerrainModifierOperationComboBox().setSelectedItem(token.getTerrainModifierOperation());

    // Get tokens ignored list, match to the index in the JList then select them.
    getTerrainModifiersIgnoredList()
        .setSelectedIndices(
            token.getTerrainModifiersIgnored().stream()
                .map(
                    operation ->
                        getTerrainModifiersIgnoredList()
                            .getNextMatch(operation.toString(), 0, Bias.Forward))
                .collect(Collectors.toCollection(ArrayList::new))
                .stream()
                .mapToInt(Integer::valueOf)
                .toArray());

    // Jamz: Init the Topology tab...
    JTabbedPane tabbedPane = getTabbedPane();

    String topologyTitle = I18N.getText("EditTokenDialog.tab.vbl");
    if (MapTool.getPlayer().isGM()) {
      tabbedPane.setEnabledAt(tabbedPane.indexOfTab(topologyTitle), true);
      getTokenTopologyPanel().setToken(token);
      getInverseTopologyCheckbox().setSelected(getTokenTopologyPanel().isInverseTopology());
      getColorSensitivitySpinner().setValue(getTokenTopologyPanel().getColorSensitivity());
      getTopologyIgnoreColorWell().setColor(getTokenTopologyPanel().getTopologyColorPick());
      getJtsDistanceToleranceSpinner().setValue(getTokenTopologyPanel().getJtsDistanceTolerance());
      getVisibilityToleranceSpinner().setValue(token.getAlwaysVisibleTolerance());
      getJtsMethodComboBox().setSelectedItem(getTokenTopologyPanel().getJtsMethod());

      // Reset scale
      getTokenTopologyPanel().setScale(1d);
    } else {
      tabbedPane.setEnabledAt(tabbedPane.indexOfTab(topologyTitle), false);
      if (tabbedPane.getSelectedIndex() == tabbedPane.indexOfTab(topologyTitle)) {
        tabbedPane.setSelectedIndex(0);
      }
    }
    getWallVblToggle()
        .setSelected(getTokenTopologyPanel().isTopologyTypeSelected(Zone.TopologyType.WALL_VBL));
    getWallVblToggle()
        .setSelectedIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON));
    getWallVblToggle().setIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_VBL_ON));

    getHillVblToggle()
        .setSelected(getTokenTopologyPanel().isTopologyTypeSelected(Zone.TopologyType.HILL_VBL));
    getHillVblToggle()
        .setSelectedIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON));
    getHillVblToggle().setIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_HILL_ON));

    getPitVblToggle()
        .setSelected(getTokenTopologyPanel().isTopologyTypeSelected(Zone.TopologyType.PIT_VBL));
    getPitVblToggle()
        .setSelectedIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON));
    getPitVblToggle().setIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_PIT_ON));

    getCoverVblToggle()
        .setSelected(getTokenTopologyPanel().isTopologyTypeSelected(Zone.TopologyType.COVER_VBL));
    getCoverVblToggle()
        .setSelectedIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_COVER_ON));
    getCoverVblToggle().setIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_COVER_ON));

    getMblToggle()
        .setSelected(getTokenTopologyPanel().isTopologyTypeSelected(Zone.TopologyType.MBL));
    getMblToggle().setSelectedIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON));
    getMblToggle().setIcon(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_TYPE_MBL_ON));

    getAlwaysVisibleButton().setSelected(token.isAlwaysVisible());

    setLibTokenPaneEnabled(token.isLibToken());

    getAllowURLAccess().setSelected(token.getAllowURIAccess());

    // Jamz: Init the Hero Lab tab...
    heroLabData = token.getHeroLabData();
    String heroLabTitle = I18N.getString("EditTokenDialog.tab.hero");

    if (heroLabData != null) {
      boolean isDirty = heroLabData.isDirty() && heroLabData.getPortfolioFile().exists();
      JButton refreshDataButton = (JButton) getComponent("refreshDataButton");

      if (isDirty && refreshDataButton.getIcon() != REFRESH_ICON_ON) {
        refreshDataButton.setIcon(REFRESH_ICON_ON);
        refreshDataButton.setToolTipText(
            I18N.getString("EditTokenDialog.button.hero.refresh.tooltip.on"));
      } else if (!isDirty && refreshDataButton.getIcon() != REFRESH_ICON_OFF) {
        refreshDataButton.setIcon(REFRESH_ICON_OFF);
        refreshDataButton.setToolTipText(
            I18N.getString("EditTokenDialog.button.hero.refresh.tooltip.off"));
      }

      tabbedPane.setEnabledAt(tabbedPane.indexOfTab(heroLabTitle), true);
      tabbedPane.setIconAt(
          tabbedPane.indexOfTab(heroLabTitle),
          RessourceManager.getSmallIcon(Icons.EDIT_TOKEN_HEROLAB));
      getHtmlStatblockEditor().setText(heroLabData.getStatBlock_html());
      getHtmlStatblockEditor().setCaretPosition(0);

      xmlStatblockRSyntaxTextArea.setText(heroLabData.getStatBlock_xml());
      xmlStatblockRSyntaxTextArea.setCaretPosition(0);

      textStatblockRSyntaxTextArea.setText(heroLabData.getStatBlock_text());
      textStatblockRSyntaxTextArea.setCaretPosition(0);

      ((JCheckBox) getComponent("isAllyCheckBox")).setSelected(heroLabData.isAlly());
      ((JLabel) getComponent("summaryText")).setText(heroLabData.getSummary());

      if (heroLabData.getPortfolioFile().exists()) {
        getComponent("portfolioLocation").setForeground(Color.BLACK);
      } else {
        getComponent("portfolioLocation").setForeground(Color.RED);
      }

      ((JLabel) getComponent("portfolioLocation"))
          .setText(heroLabData.getPortfolioFile().getAbsolutePath());
      ((JLabel) getComponent("portfolioLocation")).setToolTipText(heroLabData.getPortfolioPath());

      ((JLabel) getComponent("lastModified")).setText(heroLabData.getLastModifiedDateString());

      EventQueue.invokeLater(this::loadHeroLabImageList);

      // loadHeroLabImageList();
    } else {
      tabbedPane.setEnabledAt(tabbedPane.indexOfTab(heroLabTitle), false);
      if (tabbedPane.getSelectedIndex() == tabbedPane.indexOfTab(heroLabTitle)) {
        tabbedPane.setSelectedIndex(6);
      }
    }

    // we will disable the Owner only visible check box if the token is not
    // visible to players to signify the relationship
    ActionListener tokenVisibleActionListener =
        actionEvent -> {
          AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
          boolean selected = abstractButton.getModel().isSelected();
          getVisibleOnlyToOwnerCheckBox().setEnabled(selected);
          getVisibleOnlyToOwnerLabel().setEnabled(selected);
        };
    getVisibleCheckBox().addActionListener(tokenVisibleActionListener);

    // Character Sheets
    // controller = null;
    // String form =
    // MapTool.getCampaign().getCharacterSheets().get(token.getPropertyType());
    // if (form == null)
    // return;
    // URL formUrl = getClass().getClassLoader().getResource(form);
    // if (formUrl == null)
    // return;
    // controller = new CharSheetController(formUrl, null);
    // HashMap<String, Object> properties = new HashMap<String, Object>();
    // for (String prop : token.getPropertyNames())
    // properties.put(prop, token.getProperty(prop));
    // controller.setData(properties);
    // controller.getPanel().setName("characterSheet");
    // replaceComponent("sheetPanel", "characterSheet", controller.getPanel());

    super.bind(token);
  }

  private void setGmNotesEnabled(boolean enabled) {
    JTabbedPane tabbedPane = getTabbedPane();
    String libTokenTile = I18N.getString("EditTokenDialog.label.gmnotes");
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(libTokenTile), enabled);
    getGMNotesEditor().setEnabled(enabled);
  }

  private void setLibTokenPaneEnabled(boolean show) {
    JTabbedPane tabbedPane = getTabbedPane();
    String libTokenTile = I18N.getString("EditTokenDialog.tab.libToken");
    tabbedPane.setEnabledAt(tabbedPane.indexOfTab(libTokenTile), show);
    getAllowURLAccess().setEnabled(show);
  }

  public JTabbedPane getTabbedPane() {
    return (JTabbedPane) getComponent("TabPane");
  }

  public HtmlEditorSplit getPlayerNotesEditor() {
    return (HtmlEditorSplit) getComponent("playerNotesEditor");
  }

  public void initTypeCombo() {
    getTypeCombo().setModel(new DefaultComboBoxModel<>(Token.Type.values()));
  }

  public void initLibTokenTable() {
    getNameField()
        .getDocument()
        .addDocumentListener(
            new DocumentListener() {
              private void checkName() {
                String name = getNameField().getText();
                setLibTokenPaneEnabled(Token.isValidLibTokenName(name));
                validateLibTokenURIAccess(name);
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

  public JComboBox getTypeCombo() {
    return (JComboBox) getComponent("type");
  }

  public JComboBox getStatSheetCombo() {
    return (JComboBox) getComponent("statSheetComboBox");
  }

  public JComboBox getStatSheetLocationCombo() {
    return (JComboBox) getComponent("statSheetLocationComboBox");
  }

  public void initTokenIconPanel() {
    getTokenIconPanel().setPreferredSize(new Dimension(100, 100));
    getTokenIconPanel().setMinimumSize(new Dimension(100, 100));
  }

  public ImageAssetPanel getTokenIconPanel() {
    if (imagePanel == null) {
      imagePanel = new ImageAssetPanel();
      imagePanel.setAllowEmptyImage(false);
      replaceComponent("mainPanel", "tokenImage", imagePanel);
    }
    return imagePanel;
  }

  public void initShapeCombo() {
    getShapeCombo().setModel(new DefaultComboBoxModel(Token.TokenShape.values()));
  }

  public JComboBox getShapeCombo() {
    return (JComboBox) getComponent("shape");
  }

  /** Initializes the Property Type dropdown list. */
  public void initPropertyTypeCombo() {
    getPropertyTypeCombo()
        .addItemListener(
            e -> {
              if (e.getStateChange() == ItemEvent.SELECTED) {
                updatePropertiesTable((String) getPropertyTypeCombo().getSelectedItem());
              }
            });
  }

  /** Updates the Property Type dropdown list with the current campaign types. */
  private void updatePropertyTypeCombo() {
    List<String> typeList = new ArrayList<String>(MapTool.getCampaign().getTokenTypes());
    Collections.sort(typeList);
    DefaultComboBoxModel model = new DefaultComboBoxModel(typeList.toArray());
    getPropertyTypeCombo().setModel(model);
  }

  private void updateSightTypeCombo() {
    List<String> typeList = new ArrayList<String>(MapTool.getCampaign().getSightTypes());
    Collections.sort(typeList);

    DefaultComboBoxModel model = new DefaultComboBoxModel(typeList.toArray());
    getSightTypeCombo().setModel(model);
  }

  private void updateImageTableCombo() {
    List<String> typeList = new ArrayList<String>(MapTool.getCampaign().getLookupTables());
    Collections.sort(typeList);

    DefaultComboBoxModel model = new DefaultComboBoxModel(typeList.toArray());
    getImageTableCombo().setModel(model);
  }

  /**
   * Updates the property table.
   *
   * @param propertyType the property type of the token (unused).
   */
  private void updatePropertiesTable(final String propertyType) {
    EventQueue.invokeLater(
        () -> {
          PropertyTable pp = getPropertyTable();
          pp.setModel(new TokenPropertyTableModel());
          pp.expandAll();
        });
  }

  public JComboBox getSizeCombo() {
    return (JComboBox) getComponent("size");
  }

  public void setSizeCombo(Token token) {
    JComboBox size = getSizeCombo();
    Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
    DefaultComboBoxModel model = new DefaultComboBoxModel(grid.getFootprints().toArray());
    model.insertElementAt(
        !token.getLayer().isStampLayer()
            ? I18N.getString("token.popup.menu.size.native")
            : I18N.getString("token.popup.menu.size.free"),
        0);
    size.setModel(model);
    if (token.isSnapToScale()) {
      size.setSelectedItem(token.getFootprint(grid));
    } else {
      size.setSelectedIndex(0);
    }
  }

  public JComboBox getPropertyTypeCombo() {
    return (JComboBox) getComponent("propertyTypeCombo");
  }

  public JComboBox getSightTypeCombo() {
    return (JComboBox) getComponent("sightTypeCombo");
  }

  public JComboBox getImageTableCombo() {
    return (JComboBox) getComponent("imageTableCombo");
  }

  public void initTokenOpacitySlider() {
    getTokenOpacitySlider().addChangeListener(new SliderListener());
  }

  public JSlider getTokenOpacitySlider() {
    return (JSlider) getComponent("tokenOpacitySlider");
  }

  public JLabel getTokenOpacityValueLabel() {
    return (JLabel) getComponent("tokenOpacityValueLabel");
  }

  public JTextField getTerrainModifier() {
    return (JTextField) getComponent("terrainModifier");
  }

  public JComboBox<TerrainModifierOperation> getTerrainModifierOperationComboBox() {
    return (JComboBox<TerrainModifierOperation>) getComponent("terrainModifierOperation");
  }

  public JList<TerrainModifierOperation> getTerrainModifiersIgnoredList() {
    return (JList<TerrainModifierOperation>) getComponent("terrainModifiersIgnored");
  }

  public JLabel getLibTokenURIErrorLabel() {
    return (JLabel) getComponent("Label.LibURIError");
  }

  public void initOKButton() {
    getOKButton()
        .addActionListener(
            e -> {
              if (commit()) {
                unbind();
                dialog.closeDialog();
              }
            });
  }

  @Override
  public boolean commit() {
    Token token = getModel();

    if (getNameField().getText().equals("")) {
      MapTool.showError("msg.error.emptyTokenName");
      return false;
    }
    if (getSpeechTable().isEditing()) {
      getSpeechTable().getCellEditor().stopCellEditing();
    }
    if (getPropertyTable().isEditing()) {
      getPropertyTable().getCellEditor().stopCellEditing();
    }
    // Commit the changes to the token properties
    // If no map available, cancel the commit. Fixes #1646.
    if (!super.commit() || MapTool.getFrame().getCurrentZoneRenderer() == null) {
      return false;
    }
    // TYPE
    // Only update this if it actually changed
    if (getTypeCombo().getSelectedItem() != token.getType()) {
      token.setType((Token.Type) getTypeCombo().getSelectedItem());
    }

    // NOTES
    token.setGMNotes(getGMNotesEditor().getText());
    token.setGmNotesType(getGMNotesEditor().getTextType());
    token.setNotes(getPlayerNotesEditor().getText());
    token.setNotesType(getPlayerNotesEditor().getTextType());

    // SIZE
    token.setSnapToScale(getSizeCombo().getSelectedIndex() != 0);
    if (getSizeCombo().getSelectedIndex() > 0) {
      Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
      token.setFootprint(grid, (TokenFootprint) getSizeCombo().getSelectedItem());
    }
    // Other
    token.setPropertyType((String) getPropertyTypeCombo().getSelectedItem());
    token.setSightType((String) getSightTypeCombo().getSelectedItem());
    token.setImageTableName((String) getImageTableCombo().getSelectedItem());
    token.setTokenOpacity(
        new BigDecimal(getTokenOpacitySlider().getValue())
            .divide(new BigDecimal(100))
            .floatValue());

    try {
      token.setTerrainModifier(Double.parseDouble(getTerrainModifier().getText()));
    } catch (NumberFormatException e) {
      // User didn't enter a valid float...
      token.setTerrainModifier(1);
    }

    token.setTerrainModifierOperation(
        (TerrainModifierOperation) getTerrainModifierOperationComboBox().getSelectedItem());

    token.setTerrainModifiersIgnored(
        new HashSet<>(getTerrainModifiersIgnoredList().getSelectedValuesList()));

    // Get the states
    Component[] stateComponents = getStatesPanel().getComponents();
    Container barPanel = null;
    for (Component stateComponent : stateComponents) {
      if ("bar".equals(stateComponent.getName())) {
        barPanel = (Container) stateComponent;
        continue;
      }
      Component[] components = ((Container) stateComponent).getComponents();
      for (Component component : components) {
        JCheckBox cb = (JCheckBox) component;
        String state = cb.getText();
        token.setState(state, cb.isSelected() ? Boolean.TRUE : Boolean.FALSE);
      }
    } // endfor

    // BARS
    if (barPanel != null) {
      for (var barContainer : barPanel.getComponents()) {
        var barComponents = ((Container) barContainer).getComponents();

        JSlider bar = (JSlider) barComponents[1];
        JCheckBox cb = (JCheckBox) barComponents[2];

        BigDecimal value =
            cb.isSelected() ? null : new BigDecimal(bar.getValue()).divide(new BigDecimal(100));
        token.setState(bar.getName(), value);
        bar.setValue(
            (int)
                (TokenBarFunction.getBigDecimalValue(token.getState(bar.getName())).doubleValue()
                    * 100));
      }
    }
    // Ownership
    // If the token is owned by all and we are a player don't alter the ownership
    // list.
    if (MapTool.getPlayer().isGM() || !token.isOwnedByAll()) {
      token.clearAllOwners();

      for (int i = 0; i < getOwnerList().getModel().getSize(); i++) {
        DefaultSelectable selectable =
            (DefaultSelectable) getOwnerList().getModel().getElementAt(i);
        if (selectable.isSelected()) {
          token.addOwner((String) selectable.getObject());
        }
      }
      // If we are not a GM and the only non GM owner make sure we can't
      // take our selves off of the owners list
      if (!MapTool.getPlayer().isGM()) {
        boolean hasPlayer = token.isOwnedByAny(MapTool.getNonGMs());
        if (!hasPlayer) {
          token.addOwner(MapTool.getPlayer().getName());
        }
      }
    }
    // SHAPE
    token.setShape((Token.TokenShape) getShapeCombo().getSelectedItem());

    // Stat Sheet
    var ss = (StatSheet) getStatSheetCombo().getSelectedItem();
    if (ss == null || (ss.name() == null && ss.namespace() == null)) {
      token.useDefaultStatSheet();
    } else {
      var ssManager = new StatSheetManager();
      var location = (StatSheetLocation) getStatSheetLocationCombo().getSelectedItem();
      if (location == null) {
        location = StatSheetLocation.BOTTOM_LEFT;
      }
      token.setStatSheet(new StatSheetProperties(ssManager.getId(ss), location));
    }

    // Macros
    token.setSpeechMap(((KeyValueTableModel) getSpeechTable().getModel()).getMap());

    // Properties
    ((TokenPropertyTableModel) getPropertyTable().getModel()).applyTo(token);

    // Charsheet
    if (getCharSheetPanel().getImageId() != null) {
      MapToolUtil.uploadAsset(AssetManager.getAsset(getCharSheetPanel().getImageId()));
    }
    token.setCharsheetImage(getCharSheetPanel().getImageId());

    // IMAGE
    if (!token.getImageAssetId().equals(getTokenIconPanel().getImageId())) {
      MapToolUtil.uploadAsset(AssetManager.getAsset(getTokenIconPanel().getImageId()));
      token.setImageAsset(null, getTokenIconPanel().getImageId()); // Default image for now
    }
    // PORTRAIT
    if (getPortraitPanel().getImageId() != null) {
      // Make sure the server has the image
      if (!MapTool.getCampaign().containsAsset(getPortraitPanel().getImageId())) {
        MapTool.serverCommand().putAsset(AssetManager.getAsset(getPortraitPanel().getImageId()));
      }
    }
    token.setPortraitImage(getPortraitPanel().getImageId());

    // LAYOUT
    token.setSizeScale(getTokenLayoutPanel().getSizeScale());
    token.setAnchor(getTokenLayoutPanel().getAnchorX(), getTokenLayoutPanel().getAnchorY());

    // TOPOLOGY
    for (final var type : Zone.TopologyType.values()) {
      token.setTopology(type, getTokenTopologyPanel().getTopology(type));
    }
    token.setIsAlwaysVisible(getAlwaysVisibleButton().isSelected());
    token.setAlwaysVisibleTolerance((int) getVisibilityToleranceSpinner().getValue());
    if (getTokenTopologyPanel().getAutoGenerated()) {
      token.setColorSensitivity(getTokenTopologyPanel().getColorSensitivity());
    } else {
      token.setColorSensitivity(-1);
    }

    token.setHeroLabData(heroLabData);

    // URI Access
    token.setAllowURIAccess(getAllowURLAccess().isEnabled() && getAllowURLAccess().isSelected());
    // OTHER
    tokenSaved = true;

    // Character Sheet
    // Map<String, Object> properties = controller.getData();
    // for (String prop : token.getPropertyNames())
    // token.setProperty(prop, properties.get(prop));

    // Update UI
    MapTool.getFrame().updateTokenTree();
    MapTool.getFrame().resetTokenPanels();

    // Jamz: TODO check if topology changed on token first
    MapTool.getFrame().getCurrentZoneRenderer().getZone().tokenTopologyChanged();
    return true;
  }

  public JButton getOKButton() {
    return (JButton) getComponent("okButton");
  }

  public void initCancelButton() {
    getCancelButton()
        .addActionListener(
            e -> {
              unbind();
              dialog.closeDialog();
            });
  }

  public JButton getCancelButton() {
    return (JButton) getComponent("cancelButton");
  }

  public PropertyTable getPropertyTable() {
    return (PropertyTable) getComponent("propertiesTable");
  }

  private void updateStatesPanel() {
    // Group the states first into individual panels
    List<BooleanTokenOverlay> overlays =
        new ArrayList<BooleanTokenOverlay>(MapTool.getCampaign().getTokenStatesMap().values());
    Map<String, JPanel> groups = new TreeMap<String, JPanel>();
    var noGroupPanel =
        new JPanel(new MigLayout("wrap 4", "[fill,grow][fill,grow][fill,grow][fill,grow]"));
    noGroupPanel.setName("no group");
    groups.put("", noGroupPanel);
    for (BooleanTokenOverlay overlay : overlays) {
      String group = overlay.getGroup();
      if (group != null && (group = group.trim()).length() != 0) {
        JPanel panel = groups.get(group);
        if (panel == null) {
          panel =
              new JPanel(new MigLayout("wrap 4", "[fill,grow][fill,grow][fill,grow][fill,grow]"));
          panel.setName(group);
          panel.setBorder(BorderFactory.createTitledBorder(group));
          groups.put(group, panel);
        }
      }
    }

    // Add the group panels and bar panel to the states panel
    JPanel statesPanel = getStatesPanel();
    MigLayout layout = new MigLayout("wrap", "[fill,grow]");
    statesPanel.setLayout(layout);
    statesPanel.removeAll();

    // Add the individual check boxes.
    for (BooleanTokenOverlay state : overlays) {
      String group = state.getGroup();
      var panel = groups.get("");
      if (group != null && (group = group.trim()).length() != 0) {
        panel = groups.get(group);
      }
      panel.add(new JCheckBox(state.getName()));
    }

    for (JPanel gPanel : groups.values()) {
      if (gPanel.getComponentCount() == 0) continue;

      statesPanel.add(gPanel);
    }

    JPanel barPanel = new JPanel(new MigLayout("wrap 2", "[fill,grow][fill,grow]"));
    barPanel.setName("bar");
    // Add sliders to the bar panel
    if (MapTool.getCampaign().getTokenBarsMap().size() > 0) {
      barPanel.setBorder(
          BorderFactory.createTitledBorder(I18N.getText("CampaignPropertiesDialog.tab.bars")));

      for (BarTokenOverlay bar : MapTool.getCampaign().getTokenBarsMap().values()) {
        JSlider slider = new JSlider(0, 100);
        JCheckBox hide = new JCheckBox(I18N.getString("EditTokenDialog.checkbox.state.hide"));
        hide.putClientProperty("JSlider", slider);
        hide.addChangeListener(
            e -> {
              JSlider js = (JSlider) ((JCheckBox) e.getSource()).getClientProperty("JSlider");
              js.setEnabled(!((JCheckBox) e.getSource()).isSelected());
            });
        slider.setName(bar.getName());
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.setMajorTickSpacing(20);
        slider.createStandardLabels(20);
        slider.setMajorTickSpacing(10);

        JPanel tokenbarPanel = new JPanel(new MigLayout("wrap 2", "[fill,grow][fill,grow]"));
        tokenbarPanel.add(new JLabel(bar.getName() + ":"));
        tokenbarPanel.add(slider, "span 1 2 align right");
        tokenbarPanel.add(hide);
        barPanel.add(tokenbarPanel);
      }
      statesPanel.add(barPanel);
    }
  }

  /**
   * @return Getter for tokenSaved
   */
  public boolean isTokenSaved() {
    return tokenSaved;
  }

  public JPanel getStatesPanel() {
    return (JPanel) getComponent("statesPanel");
  }

  public JTable getSpeechTable() {
    return (JTable) getComponent("speechTable");
  }

  public JButton getSpeechClearAllButton() {
    return (JButton) getComponent("speechClearAllButton");
  }

  private JLabel getVisibleLabel() {
    return (JLabel) getComponent("visibleLabel");
  }

  private JCheckBox getVisibleCheckBox() {
    return (JCheckBox) getComponent("@visible");
  }

  private JLabel getVisibleOnlyToOwnerLabel() {
    return (JLabel) getComponent("visibleOnlyToOwnerLabel");
  }

  private JCheckBox getVisibleOnlyToOwnerCheckBox() {
    return (JCheckBox) getComponent("@visibleOnlyToOwner");
  }

  private HtmlEditorSplit getGMNotesEditor() {
    return (HtmlEditorSplit) getComponent("gmNotesEditor");
  }

  private JTextField getNameField() {
    return (JTextField) getComponent("@name");
  }

  private JTextField getSpeechNameField() {
    return (JTextField) getComponent("@speechName");
  }

  public CheckBoxListWithSelectable getOwnerList() {
    return (CheckBoxListWithSelectable) getComponent("ownerList");
  }

  public JToggleButton getWallVblToggle() {
    return (JToggleButton) getComponent("wallVblToggle");
  }

  public JToggleButton getHillVblToggle() {
    return (JToggleButton) getComponent("hillVblToggle");
  }

  public JToggleButton getPitVblToggle() {
    return (JToggleButton) getComponent("pitVblToggle");
  }

  public JToggleButton getCoverVblToggle() {
    return (JToggleButton) getComponent("coverVblToggle");
  }

  public JToggleButton getMblToggle() {
    return (JToggleButton) getComponent("mblToggle");
  }

  public JButton getAutoGenerateTopologyButton() {
    return (JButton) getComponent("autoGenerateVblButton");
  }

  public JButton getClearTopologyButton() {
    return (JButton) getComponent("clearVblButton");
  }

  public JButton getTransferTopologyToMap() {
    return (JButton) getComponent("transferVblToMap");
  }

  public JButton getTransferTopologyFromMap() {
    return (JButton) getComponent("transferVblFromMap");
  }

  public JCheckBox getCopyOrMoveCheckbox() {
    return (JCheckBox) getComponent("copyOrMoveCheckbox");
  }

  public JCheckBox getHideTokenCheckbox() {
    return (JCheckBox) getComponent("hideTokenCheckbox");
  }

  public JCheckBox getInverseTopologyCheckbox() {
    return (JCheckBox) getComponent("inverseVblCheckbox");
  }

  public JCheckBox getAlwaysVisibleButton() {
    return (JCheckBox) getComponent("alwaysVisibleButton");
  }

  public ColorWell getTopologyIgnoreColorWell() {
    return (ColorWell) getComponent("vblIgnoreColorWell");
  }

  public JToggleButton getTopologyColorPickerToggleButton() {
    return (JToggleButton) getComponent("vblColorPickerToggleButton");
  }

  public JSpinner getJtsDistanceToleranceSpinner() {
    return (JSpinner) getComponent("jtsDistanceToleranceSpinner");
  }

  public JComboBox getJtsMethodComboBox() {
    return (JComboBox) getComponent("jtsMethodComboBox");
  }

  public JSpinner getColorSensitivitySpinner() {
    return (JSpinner) getComponent("alphaSensitivitySpinner");
  }

  public JToggleButton getRectangleTopologyButton() {
    return (JToggleButton) getComponent("rectangleTopologyButton");
  }

  public JSpinner getVisibilityToleranceSpinner() {
    return (JSpinner) getComponent("visibilityToleranceSpinner");
  }

  public JCheckBox getAllowURLAccess() {
    return (JCheckBox) getComponent("@allowURIAccess");
  }

  public void initSpeechPanel() {
    getSpeechClearAllButton()
        .addActionListener(
            e -> {
              if (!MapTool.confirm("EditTokenDialog.confirm.clearSpeech")) {
                return;
              }
              EventQueue.invokeLater(() -> getSpeechTable().setModel(new SpeechTableModel()));
            });
  }

  public void initOwnershipPanel() {
    CheckBoxListWithSelectable list = new CheckBoxListWithSelectable();
    list.setName("ownerList");
    replaceComponent(
        "ownershipPanel",
        "ownershipList",
        new JScrollPane(
            list,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
  }

  public void initPropertiesPanel() {
    PropertyTable propertyTable =
        new PropertyTable() {
          @Override
          public String getToolTipText(MouseEvent event) {
            String text = super.getToolTipText(event);
            return text != null && text.length() > 100 ? text.substring(0, 100) + " ..." : text;
          }
        };
    propertyTable.setFillsViewportHeight(true); // XXX This is Java6-only -- need
    //  Java5 solution
    propertyTable.setName("propertiesTable");

    // wrap button and functionality
    JPanel buttonsAndPropertyTable = new JPanel();
    buttonsAndPropertyTable.setLayout(new BorderLayout());
    JCheckBox wrapToggle = new JCheckBox(I18N.getString("EditTokenDialog.msg.wrap"));
    wrapToggle.addActionListener(
        e -> {
          propertyCellRenderer.setLineWrap(wrapToggle.isSelected());
          propertyTable.repaint();
        });
    buttonsAndPropertyTable.add(wrapToggle, BorderLayout.PAGE_END);

    PropertyPane pane = new PropertyPane(propertyTable);
    // pane.setPreferredSize(new Dimension(100, 300));
    buttonsAndPropertyTable.add(pane, BorderLayout.CENTER);
    replaceComponent("propertiesPanel", "propertiesTable", buttonsAndPropertyTable);
  }

  public void initTokenDetails() {
    // tokenGMNameLabel = panel.getLabel("tokenGMNameLabel");
  }

  public void initTokenLayoutPanel() {
    TokenLayoutPanel layoutPanel = new TokenLayoutPanel();
    layoutPanel.setPreferredSize(new Dimension(150, 125));
    layoutPanel.setName("tokenLayout");

    replaceComponent("tokenLayoutPanel", "tokenLayout", layoutPanel);
  }

  public void initCharsheetPanel() {
    ImageAssetPanel panel = new ImageAssetPanel();
    panel.setPreferredSize(new Dimension(150, 125));
    panel.setName("charsheet");
    panel.setLayout(new GridLayout());

    replaceComponent("charsheetPanel", "charsheet", panel);
  }

  public void initPortraitPanel() {
    ImageAssetPanel panel = new ImageAssetPanel();
    panel.setPreferredSize(new Dimension(150, 125));
    panel.setName("portrait");
    panel.setLayout(new GridLayout());

    replaceComponent("portraitPanel", "portrait", panel);
  }

  public ImageAssetPanel getPortraitPanel() {
    return (ImageAssetPanel) getComponent("portrait");
  }

  public ImageAssetPanel getCharSheetPanel() {
    return (ImageAssetPanel) getComponent("charsheet");
  }

  public TokenLayoutPanel getTokenLayoutPanel() {
    return (TokenLayoutPanel) getComponent("tokenLayout");
  }

  public TokenTopologyPanel getTokenTopologyPanel() {
    return (TokenTopologyPanel) getComponent("vblPreview");
  }

  public JEditorPane getHtmlStatblockEditor() {
    return (JEditorPane) getComponent("HTMLstatblockTextArea");
  }

  public JList getHeroLabImagesList() {
    return (JList) getComponent("heroLabImagesList");
  }

  public void initIcons() {
    var test = getComponent("heroLabImagesList");
  }

  public void initTokenTopologyPanel() {
    TokenTopologyPanel topologyPanel = new TokenTopologyPanel(this);
    topologyPanel.setPreferredSize(new Dimension(300, 200));
    topologyPanel.setName("vblPreview");
    replaceComponent("vblPreviewPanel", "vblPreview", topologyPanel);

    getWallVblToggle()
        .addActionListener(
            e ->
                getTokenTopologyPanel()
                    .setTopologyTypeSelected(
                        Zone.TopologyType.WALL_VBL, ((AbstractButton) e.getSource()).isSelected()));
    getHillVblToggle()
        .addActionListener(
            e ->
                getTokenTopologyPanel()
                    .setTopologyTypeSelected(
                        Zone.TopologyType.HILL_VBL, ((AbstractButton) e.getSource()).isSelected()));
    getPitVblToggle()
        .addActionListener(
            e ->
                getTokenTopologyPanel()
                    .setTopologyTypeSelected(
                        Zone.TopologyType.PIT_VBL, ((AbstractButton) e.getSource()).isSelected()));
    getCoverVblToggle()
        .addActionListener(
            e ->
                getTokenTopologyPanel()
                    .setTopologyTypeSelected(
                        Zone.TopologyType.COVER_VBL,
                        ((AbstractButton) e.getSource()).isSelected()));
    getMblToggle()
        .addActionListener(
            e ->
                getTokenTopologyPanel()
                    .setTopologyTypeSelected(
                        Zone.TopologyType.MBL, ((AbstractButton) e.getSource()).isSelected()));

    getAutoGenerateTopologyButton()
        .addActionListener(
            e -> {
              if (getTokenTopologyPanel().hasAnyOptimizedTopologySet()) {
                if (!MapTool.confirm("EditTokenDialog.confirm.vbl.autoGenerate")) {
                  return;
                }
              }

              getTokenTopologyPanel().setAutoGenerated(true);
              updateAutoGeneratedTopology(true);
              getTokenTopologyPanel().repaint();
            });

    getClearTopologyButton()
        .addActionListener(
            e -> {
              if (getTokenTopologyPanel().hasAnyOptimizedTopologySet()) {
                if (!MapTool.confirm("EditTokenDialog.confirm.vbl.clearVBL")) {
                  return;
                }
              }
              getTokenTopologyPanel().clearGeneratedTopologies();
              getTokenTopologyPanel().repaint();
            });

    getTransferTopologyToMap()
        .addActionListener(
            e -> {
              if (getTokenTopologyPanel().hasAnyOptimizedTopologySet()) {
                final boolean clearTokenTopology = getCopyOrMoveCheckbox().isSelected();
                if (clearTokenTopology) {
                  if (!MapTool.confirm("EditTokenDialog.confirm.vbl.clearVBL")) {
                    return;
                  }
                }

                for (final var type : Zone.TopologyType.values()) {
                  final var topology = getTokenTopologyPanel().getTopology(type);
                  if (topology != null) {
                    TokenVBL.renderTopology(
                        MapTool.getFrame().getCurrentZoneRenderer(),
                        getTokenTopologyPanel().getToken().getTransformedTopology(topology),
                        false,
                        type);
                  }
                }

                if (clearTokenTopology) {
                  getTokenTopologyPanel().clearGeneratedTopologies();
                  getTokenTopologyPanel().repaint();
                }
              }
            });

    getTransferTopologyFromMap()
        .addActionListener(
            e -> {
              final boolean removeFromMap = getCopyOrMoveCheckbox().isSelected();
              for (final var type : getTokenTopologyPanel().getSelectedTopologyTypes()) {
                Area mapTopology =
                    TokenVBL.getMapTopology_transformed(
                        MapTool.getFrame().getCurrentZoneRenderer(),
                        getTokenTopologyPanel().getToken(),
                        type);

                getTokenTopologyPanel().putCustomTopology(type, mapTopology);

                if (removeFromMap) {
                  Area topologyToDelete =
                      TokenVBL.getTopology_underToken(
                          MapTool.getFrame().getCurrentZoneRenderer(),
                          getTokenTopologyPanel().getToken(),
                          type);
                  TokenVBL.renderTopology(
                      MapTool.getFrame().getCurrentZoneRenderer(), topologyToDelete, true, type);
                }
              }

              getTokenTopologyPanel().repaint();
            });

    getCopyOrMoveCheckbox()
        .addActionListener(
            e -> {
              if (getCopyOrMoveCheckbox().isSelected()) {
                getTransferTopologyFromMap()
                    .setText(
                        I18N.getString("token.properties.button.transferVblFromMap.move.text"));
                getTransferTopologyToMap()
                    .setText(I18N.getString("token.properties.button.transferVblToMap.move.text"));
              } else {
                getTransferTopologyFromMap()
                    .setText(
                        I18N.getString("token.properties.button.transferVblFromMap.copy.text"));
                getTransferTopologyToMap()
                    .setText(I18N.getString("token.properties.button.transferVblToMap.copy.text"));
              }

              getTokenTopologyPanel().repaint();
            });

    getHideTokenCheckbox()
        .addActionListener(
            e -> {
              getTokenTopologyPanel().setHideTokenImage(getHideTokenCheckbox().isSelected());
              getTokenTopologyPanel().repaint();
            });

    getInverseTopologyCheckbox()
        .addActionListener(
            e -> {
              getTokenTopologyPanel().setInverseTopology(getInverseTopologyCheckbox().isSelected());
              updateAutoGeneratedTopology(true);
            });

    getColorSensitivitySpinner().setModel(new SpinnerNumberModel(1, 0, 255, 1));
    getColorSensitivitySpinner()
        .addChangeListener(
            e -> {
              getTokenTopologyPanel()
                  .setColorSensitivity((int) getColorSensitivitySpinner().getValue());
              updateAutoGeneratedTopology(true);
            });

    getJtsDistanceToleranceSpinner().setModel(new SpinnerNumberModel(2, 0, 100, 1));
    getJtsDistanceToleranceSpinner()
        .addChangeListener(
            e -> {
              getTokenTopologyPanel()
                  .setJtsDistanceTolerance((int) getJtsDistanceToleranceSpinner().getValue());
              updateAutoGeneratedTopology(false);
            });

    getJtsMethodComboBox()
        .addActionListener(
            e -> {
              getTokenTopologyPanel()
                  .setJtsMethod((JTS_SimplifyMethodType) getJtsMethodComboBox().getSelectedItem());
              updateAutoGeneratedTopology(false);
            });

    getTopologyIgnoreColorWell()
        .addActionListener(
            e ->
                getTokenTopologyPanel()
                    .setTopologyColorPick(getTopologyIgnoreColorWell().getColor()));

    getTopologyColorPickerToggleButton()
        .setIcon(RessourceManager.getBigIcon(Icons.EDIT_TOKEN_COLOR_PICKER));
    getTopologyColorPickerToggleButton()
        .addActionListener(
            e -> {
              getTokenTopologyPanel()
                  .setColorPickerActive(getTopologyColorPickerToggleButton().isSelected());
              getTokenTopologyPanel().updateUI();

              if (getTopologyColorPickerToggleButton().isSelected()) {
                getTokenTopologyPanel()
                    .setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
              } else {
                getTokenTopologyPanel().setCursor(Cursor.getDefaultCursor());
              }
            });

    getVisibilityToleranceSpinner().setModel(new SpinnerNumberModel(2, 1, 9, 1));
  }

  /**
   * @param regenerate Only regenerate topology from token image when needed
   */
  protected void updateAutoGeneratedTopology(boolean regenerate) {
    if (getTokenTopologyPanel().getAutoGenerated()) {
      getTokenTopologyPanel().setInProgress(true);
      getTokenTopologyPanel().setEnabled(false);
      getTokenTopologyPanel().repaint();

      autoGenerateTopologySwingWorker =
          new AutoGenerateTopologySwingWorker(
              regenerate, getTokenTopologyPanel().getTopologyColorPick());
      autoGenerateTopologySwingWorker.execute();
    }
  }

  /*
   * Initialize the Hero Lab Images tab
   */
  @SuppressWarnings("unchecked")
  public void initHeroLabImageList() {
    getHeroLabImagesList().setCellRenderer(new HeroLabImageListRenderer());

    JButton setTokenImage = (JButton) getComponent("setAsImageButton");
    setTokenImage.addActionListener(
        e -> {
          int index = getHeroLabImagesList().getSelectedIndex();

          if (heroLabData != null) {
            getTokenIconPanel().setImageId(heroLabData.getImageAssetID(index));
            getTokenLayoutPanel().setTokenImage(heroLabData.getImageAssetID(index));
          }
        });

    JButton setPortraitImage = (JButton) getComponent("setAsPortraitButton");
    setPortraitImage.addActionListener(
        e -> {
          int index = getHeroLabImagesList().getSelectedIndex();

          if (heroLabData != null) {
            getPortraitPanel().setImageId(heroLabData.getImageAssetID(index));
          }
        });

    JButton setHandoutImage = (JButton) getComponent("setAsHandoutButton");
    setHandoutImage.addActionListener(
        e -> {
          int index = getHeroLabImagesList().getSelectedIndex();

          if (heroLabData != null) {
            getCharSheetPanel().setImageId(heroLabData.getImageAssetID(index));
          }
        });
  }

  @SuppressWarnings({"unchecked"})
  public void loadHeroLabImageList() {
    if (heroLabData.getAssetMap() != null) {
      getHeroLabImagesList().setEnabled(true);
      getHeroLabImagesList().setFixedCellHeight(190);
      getHeroLabImagesList().setListData(heroLabData.getAssetMap().keySet().toArray());
      getHeroLabImagesList().setSelectedIndex(0);
    } else {
      getHeroLabImagesList().setEnabled(false);
      String[] empty = {""};
      getHeroLabImagesList().setListData(empty);
    }
  }

  /*
   * Initialize the Hero Lab statblock tabs
   */
  public void initStatBlocks() {
    // Setup the HTML panel
    JEditorPane statblockPane = getHtmlStatblockEditor();
    HTMLEditorKit kit = new HTMLEditorKit();
    HTMLDocument statblockDoc = (HTMLDocument) kit.createDefaultDocument();
    statblockPane.setEditorKit(kit);
    statblockPane.setDocument(statblockDoc);

    // We need this property as the kit can't handle <meta http-equiv="Content-Type"
    // content="text/html; charset=XYZ> and the rendered html is blank
    statblockDoc.putProperty("IgnoreCharsetDirective", true);

    // Setup the XML panel
    xmlStatblockRSyntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
    xmlStatblockRSyntaxTextArea.setEditable(false);
    xmlStatblockRSyntaxTextArea.setCodeFoldingEnabled(true);
    xmlStatblockRSyntaxTextArea.setLineWrap(true);
    xmlStatblockRSyntaxTextArea.setWrapStyleWord(true);
    xmlStatblockRSyntaxTextArea.setTabSize(2);
    // Set the color style via Theme
    try {
      File themeFile =
          new File(AppConstants.THEMES_DIR, AppPreferences.getDefaultMacroEditorTheme() + ".xml");
      Theme theme = Theme.load(new FileInputStream(themeFile));
      theme.apply(xmlStatblockRSyntaxTextArea);

      xmlStatblockRSyntaxTextArea.revalidate();
    } catch (IOException e) {
      e.printStackTrace();
    }

    RTextScrollPane xmlStatblockRTextScrollPane = new RTextScrollPane(xmlStatblockRSyntaxTextArea);
    xmlStatblockRTextScrollPane.setLineNumbersEnabled(false);
    replaceComponent(
        "xmlStatblockPanel", "xmlStatblockRTextScrollPane", xmlStatblockRTextScrollPane);

    // Setup the TEXT panel
    textStatblockRSyntaxTextArea.setEditable(false);
    textStatblockRSyntaxTextArea.setLineWrap(true);
    textStatblockRSyntaxTextArea.setWrapStyleWord(true);
    textStatblockRSyntaxTextArea.setTabSize(2);
    // Set the color style via Theme
    try {
      File themeFile =
          new File(AppConstants.THEMES_DIR, AppPreferences.getDefaultMacroEditorTheme() + ".xml");
      Theme theme = Theme.load(new FileInputStream(themeFile));
      theme.apply(textStatblockRSyntaxTextArea);

      textStatblockRSyntaxTextArea.revalidate();
    } catch (IOException e) {
      e.printStackTrace();
    }

    RTextScrollPane textStatblockRTextScrollPane =
        new RTextScrollPane(textStatblockRSyntaxTextArea);
    textStatblockRTextScrollPane.setLineNumbersEnabled(false);
    replaceComponent(
        "textStatblockPanel", "textStatblockRTextScrollPane", textStatblockRTextScrollPane);

    // Setup the refresh button, #refreshes the HeroLabData from the portfolio
    JButton refreshDataButton = (JButton) getComponent("refreshDataButton");
    refreshDataButton.addActionListener(
        e -> {
          if (heroLabData != null) {
            ExtractHeroLab heroLabExtract =
                new ExtractHeroLab(heroLabData.getPortfolioFile(), true);
            heroLabData = heroLabExtract.refreshCharacter(heroLabData);

            if (heroLabData != null) {
              refreshDataButton.setIcon(REFRESH_ICON_OFF);
              refreshDataButton.setToolTipText(
                  I18N.getString("EditTokenDialog.button.hero.refresh.tooltip.off"));

              ((JLabel) getComponent("portfolioLocation"))
                  .setToolTipText(heroLabData.getPortfolioPath());
              ((JLabel) getComponent("lastModified"))
                  .setText(heroLabData.getLastModifiedDateString());

              getHtmlStatblockEditor().setText(heroLabData.getStatBlock_html());
              getHtmlStatblockEditor().setCaretPosition(0);

              xmlStatblockRSyntaxTextArea.setText(heroLabData.getStatBlock_xml());
              xmlStatblockRSyntaxTextArea.setCaretPosition(0);

              textStatblockRSyntaxTextArea.setText(heroLabData.getStatBlock_text());
              textStatblockRSyntaxTextArea.setCaretPosition(0);

              // Update the images
              MD5Key tokenImageKey = heroLabData.getTokenImage();
              if (tokenImageKey != null) {
                getTokenIconPanel().setImageId(tokenImageKey);
                getTokenLayoutPanel().setTokenImage(tokenImageKey);
              }

              MD5Key portraitAssetKeY = heroLabData.getPortraitImage();
              if (portraitAssetKeY != null) {
                getPortraitPanel().setImageId(portraitAssetKeY);
              }

              MD5Key handoutAssetKey = heroLabData.getHandoutImage();
              if (handoutAssetKey != null) {
                getCharSheetPanel().setImageId(handoutAssetKey);
              }

              // If NPC, lets not overwrite the Name, it may be "Creature 229" or such, GM
              // name is enough
              ((JTextField) getComponent("@GMName")).setText(heroLabData.getName());
              if (heroLabData.isAlly()) {
                getTypeCombo().setSelectedItem(Type.PC);
                getNameField().setText(heroLabData.getName());
              } else {
                getTypeCombo().setSelectedItem(Type.NPC);
              }

              // Update image list
              loadHeroLabImageList();
            }
          }
        });

    // Setup xPath searching for XML StatBlock
    JTextField xmlStatblockSearchTextField =
        (JTextField) getComponent("xmlStatblockSearchTextField");
    JButton xmlStatblockSearchButton = (JButton) getComponent("xmlStatblockSearchButton");

    xmlStatblockSearchTextField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              xmlStatblockSearchButton.doClick();
              e.consume();
            }
          }
        });

    xmlStatblockSearchButton.addActionListener(
        e -> {
          String searchText = xmlStatblockSearchTextField.getText();

          if (searchText.isEmpty()) {
            return;
          }

          String results;
          try {
            results = heroLabData.parseXML(searchText, ", ");
          } catch (IllegalArgumentException exception) {
            results = I18N.getText("macro.function.herolab.xpath.invalid", searchText);
          }
          xmlStatblockRSyntaxTextArea.setText(results);
          xmlStatblockRSyntaxTextArea.setCaretPosition(0);
        });

    // Setup regular expression searching for TEXT StatBlock
    JTextField textStatblockSearchTextField =
        (JTextField) getComponent("textStatblockSearchTextField");
    JButton textStatblockSearchButton = (JButton) getComponent("textStatblockSearchButton");

    textStatblockSearchTextField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              textStatblockSearchButton.doClick();
              e.consume();
            }
          }
        });

    textStatblockSearchButton.addActionListener(
        e -> {
          String searchText = textStatblockSearchTextField.getText();

          if (searchText.isEmpty()) {
            return;
          }

          SearchContext context = new SearchContext();
          context.setSearchFor(searchText);
          context.setRegularExpression(true);
          // context.setMatchCase(matchCaseCB.isSelected());
          // context.setSearchForward(forward);
          // context.setWholeWord(false);

          SearchEngine.find(textStatblockRSyntaxTextArea, context).wasFound();
        });
  }

  private static class SpeechTableModel extends KeyValueTableModel {

    private static final long serialVersionUID = 1601750325218502846L;

    public SpeechTableModel(Token token) {
      List<Association<String, String>> rowList = new ArrayList<Association<String, String>>();
      for (String speechName : token.getSpeechNames()) {
        rowList.add(new Association<String, String>(speechName, token.getSpeech(speechName)));
      }
      rowList.sort((o1, o2) -> o1.getLeft().compareToIgnoreCase(o2.getLeft()));
      init(rowList);
    }

    public SpeechTableModel() {
      init(new ArrayList<Association<String, String>>());
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return I18N.getString("EditTokenDialog.msg.speech.colID");
        case 1:
          return I18N.getString("EditTokenDialog.msg.speech.colSpeechText");
      }
      return "";
    }
  }

  private static class KeyValueTableModel extends AbstractTableModel {

    private static final long serialVersionUID = -1006405977882120853L;

    private Association<String, String> newRow = new Association<String, String>("", "");
    private List<Association<String, String>> rowList;

    protected void init(List<Association<String, String>> rowList) {
      this.rowList = rowList;
    }

    public int getColumnCount() {
      return 2;
    }

    public int getRowCount() {
      return rowList.size() + 1;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
      if (rowIndex == getRowCount() - 1) {
        switch (columnIndex) {
          case 0:
            return newRow.getLeft();
          case 1:
            return newRow.getRight();
        }
        return "";
      }
      switch (columnIndex) {
        case 0:
          return rowList.get(rowIndex).getLeft();
        case 1:
          return rowList.get(rowIndex).getRight();
      }
      return "";
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      if (rowIndex == getRowCount() - 1) {
        switch (columnIndex) {
          case 0:
            newRow.setLeft((String) aValue);
            break;
          case 1:
            newRow.setRight((String) aValue);
            break;
        }
        rowList.add(newRow);
        newRow = new Association<String, String>("", "");
        return;
      }
      switch (columnIndex) {
        case 0:
          rowList.get(rowIndex).setLeft((String) aValue);
          break;
        case 1:
          rowList.get(rowIndex).setRight((String) aValue);
          break;
      }
    }

    @Override
    public String getColumnName(int column) {
      switch (column) {
        case 0:
          return I18N.getString("EditTokenDialog.msg.generic.colKey");
        case 1:
          return I18N.getString("EditTokenDialog.msg.generic.colValue");
      }
      return "";
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return true;
    }

    public Map<String, String> getMap() {
      Map<String, String> map = new HashMap<String, String>();

      for (Association<String, String> row : rowList) {
        if (row.getLeft() == null || row.getLeft().trim().length() == 0) {
          continue;
        }
        map.put(row.getLeft(), row.getRight());
      }
      return map;
    }
  }

  // needed to change the popup for properties
  private static class MTMultilineStringExComboBox extends MultilineStringExComboBox {

    final ResourceBundle a = ResourceBundle.getBundle("com.jidesoft.combobox.combobox");

    public ResourceBundle getResourceBundle(Locale paramLocale) {
      return ResourceBundle.getBundle("com.jidesoft.combobox.combobox", paramLocale);
    }

    public PopupPanel createPopupComponent() {
      MTMultilineStringPopupPanel pp =
          new MTMultilineStringPopupPanel(
              getResourceBundle(Locale.getDefault()).getString("ComboBox.multilineStringTitle"));
      return pp;
    }
  }

  // the cell editor for property popups
  private static class MTMultilineStringCellEditor extends MultilineStringCellEditor {

    protected MTMultilineStringExComboBox createMultilineStringComboBox() {
      MTMultilineStringExComboBox localMultilineStringExComboBox =
          new MTMultilineStringExComboBox();
      localMultilineStringExComboBox.setEditable(true);
      localMultilineStringExComboBox.setUI(new BasicExComboBoxUI());
      return localMultilineStringExComboBox;
    }
  }

  // the property popup table
  private static class MTMultilineStringPopupPanel extends PopupPanel {

    private RSyntaxTextArea j = createTextArea();

    public MTMultilineStringPopupPanel() {
      this("");
    }

    public MTMultilineStringPopupPanel(String paramString) {
      this.setResizable(true);
      // Set the color style via Theme
      try {
        File themeFile =
            new File(AppConstants.THEMES_DIR, AppPreferences.getDefaultMacroEditorTheme() + ".xml");
        Theme theme = Theme.load(new FileInputStream(themeFile));
        theme.apply(j);

        j.revalidate();
      } catch (IOException e) {
        e.printStackTrace();
      }
      JScrollPane localJScrollPane = new RTextScrollPane(j);
      localJScrollPane.setVerticalScrollBarPolicy(22);
      localJScrollPane.setAutoscrolls(true);
      localJScrollPane.setPreferredSize(new Dimension(300, 200));
      setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
      setLayout(new BorderLayout());
      setTitle(paramString);
      add(localJScrollPane, "Center");
      setDefaultFocusComponent(j);
      j.setLineWrap(false);
      JCheckBox wrapToggle = new JCheckBox(I18N.getString("EditTokenDialog.msg.wrap"));
      wrapToggle.addActionListener(e -> j.setLineWrap(!j.getLineWrap()));

      DefaultComboBoxModel syntaxListModel = new DefaultComboBoxModel();
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_NONE);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_JSON);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_HTML);
      syntaxListModel.addElement(SyntaxConstants.SYNTAX_STYLE_XML);
      JComboBox syntaxComboBox = new JComboBox(syntaxListModel);
      syntaxComboBox.addActionListener(
          e -> j.setSyntaxEditingStyle(syntaxComboBox.getSelectedItem().toString()));

      // content.add(wrapToggle);
      add(syntaxComboBox, BorderLayout.BEFORE_FIRST_LINE);
      add(wrapToggle, BorderLayout.AFTER_LAST_LINE);
    }

    public Object getSelectedObject() {
      return j.getText();
    }

    public void setSelectedObject(Object paramObject) {
      if (paramObject != null) {
        j.setText(paramObject.toString());
      } else {
        j.setText("");
      }
    }

    protected RSyntaxTextArea createTextArea() {
      RSyntaxTextArea textArea = new RSyntaxTextArea();
      textArea.setAnimateBracketMatching(true);
      textArea.setBracketMatchingEnabled(true);
      textArea.setLineWrap(false);
      textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
      return textArea;
    }
  }

  // cell renderer for properties table
  private static class WordWrapCellRenderer extends RSyntaxTextArea implements TableCellRenderer {

    WordWrapCellRenderer() {
      setLineWrap(false);
      setWrapStyleWord(true);

      // Set the color style via Theme
      try {
        File themeFile =
            new File(AppConstants.THEMES_DIR, AppPreferences.getDefaultMacroEditorTheme() + ".xml");
        Theme theme = Theme.load(new FileInputStream(themeFile));
        theme.apply(this);

        revalidate();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      if (value == null) {
        value = "";
      }
      setText(value.toString());
      setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
      if (table.getRowHeight(row) != getPreferredSize().height) {
        table.setRowHeight(row, getPreferredSize().height);
      }
      return this;
    }
  }

  private class AutoGenerateTopologySwingWorker extends SwingWorker<Void, Area> {
    private final boolean regenerate;
    private final Color ignoredColor;

    public AutoGenerateTopologySwingWorker(boolean regenerate, Color ignoredColor) {
      this.regenerate = regenerate;
      this.ignoredColor = ignoredColor;
    }

    @Override
    protected Void doInBackground() {
      if (regenerate || !getTokenTopologyPanel().hasAnyOriginalTopologySet()) {
        final var generatedTopology =
            TokenVBL.createTopologyAreaFromToken(
                getTokenTopologyPanel().getToken(),
                getTokenTopologyPanel().getColorSensitivity(),
                getTokenTopologyPanel().isInverseTopology(),
                ignoredColor);
        publish(generatedTopology);
      }

      // Nothing to do, so nothing to publish.
      return null;
    }

    @Override
    protected void process(List<Area> areaChunk) {
      if (!isCancelled()) {
        final var newArea = areaChunk.get(areaChunk.size() - 1);
        final var optimizedArea =
            TokenVBL.simplifyArea(
                newArea,
                getTokenTopologyPanel().getJtsDistanceTolerance(),
                getTokenTopologyPanel().getJtsMethod());

        getTokenTopologyPanel().setGeneratedTopologies(newArea, optimizedArea);
      }
    }

    @Override
    protected void done() {
      getTokenTopologyPanel().setInProgress(false);
      requestFocusInWindow();
      getTokenTopologyPanel().requestFocus();
    }
  }

  class SliderListener implements ChangeListener {

    public void stateChanged(ChangeEvent e) {
      JSlider source = (JSlider) e.getSource();
      if (!source.getValueIsAdjusting()) {
        BigDecimal value = new BigDecimal(source.getValue());
        getTokenOpacityValueLabel().setText(value.toString() + "%");
        float opacity = value.divide(new BigDecimal(100)).floatValue();
        getTokenIconPanel().setOpacity(opacity);
        getTokenIconPanel().repaint();
      }
    }
  }

  public class HeroLabImageListRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 7113815213979044509L;
    Font font = new Font("helvitica", Font.BOLD, 24);

    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label =
          (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      try {
        ImageIcon finalImage =
            ImageUtil.scaleImage(
                new ImageIcon(ImageManager.getImageAndWait(heroLabData.getImageAssetID(index))),
                250,
                175);
        label.setIcon(finalImage);
      } catch (Exception e) {
        e.printStackTrace();
      }
      label.setIconTextGap(10);
      label.setHorizontalTextPosition(JLabel.LEFT);
      label.setFont(font);
      return label;
    }
  }

  // //
  // HANDLER
  public static class MouseHandler extends MouseAdapter {

    HtmlEditorSplit source;

    public MouseHandler(HtmlEditorSplit source) {
      this.source = source;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (SwingUtilities.isRightMouseButton(e)) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem sendToChatItem =
            new JMenuItem(I18N.getString("EditTokenDialog.menu.notes.sendChat"));
        sendToChatItem.addActionListener(
            e12 -> {
              String selectedText = source.getSelectedText();
              if (selectedText == null) {
                selectedText = source.getText();
              }
              // TODO: Combine this with the code in MacroButton
              JTextComponent commandArea =
                  MapTool.getFrame().getCommandPanel().getCommandTextArea();

              commandArea.setText(commandArea.getText() + selectedText);
              commandArea.requestFocusInWindow();
            });
        menu.add(sendToChatItem);

        JMenuItem sendAsEmoteItem =
            new JMenuItem(I18N.getString("EditTokenDialog.menu.notes.sendEmit"));
        sendAsEmoteItem.addActionListener(
            e1 -> {
              String selectedText = source.getSelectedText();
              if (selectedText == null) {
                selectedText = source.getText();
              }
              // TODO: Combine this with the code in MacroButton
              MapTool.getFrame().getCommandPanel().commitCommand("/emit " + selectedText);
              MapTool.getFrame().getCommandPanel().getCommandTextArea().requestFocusInWindow();
            });
        menu.add(sendAsEmoteItem);
        menu.show((JComponent) e.getSource(), e.getX(), e.getY());
      }
    }
  }

  // //
  // MODELS
  private class TokenPropertyTableModel
      extends AbstractPropertyTableModel<EditTokenDialog.TokenPropertyTableModel.EditTokenProperty>
      implements NavigableModel {

    private static final long serialVersionUID = 2822797264738675580L;

    private Map<String, String> propertyMap;
    private List<TokenProperty> propertyList;

    private Map<String, String> getPropertyMap() {
      Token token = getModel();

      if (propertyMap == null) {
        propertyMap = new HashMap<String, String>();

        List<TokenProperty> propertyList = getPropertyList();
        for (TokenProperty property : propertyList) {
          String value = (String) token.getProperty(property.getName());
          if (value == null) {
            value = property.getDefaultValue();
          }
          propertyMap.put(property.getName(), value);
        }
      }
      return propertyMap;
    }

    private List<TokenProperty> getPropertyList() {
      if (propertyList == null) {
        propertyList =
            MapTool.getCampaign()
                .getTokenPropertyList((String) getPropertyTypeCombo().getSelectedItem());
      }
      return propertyList;
    }

    public void applyTo(Token token) {
      for (TokenProperty property : getPropertyList()) {
        String value = getPropertyMap().get(property.getName());
        if (property.getDefaultValue() != null && property.getDefaultValue().equals(value)) {
          token.setProperty(property.getName(), null); // Clear original value
          continue;
        }
        token.setProperty(property.getName(), value);
      }
    }

    @Override
    public boolean isNavigableAt(int rowIndex, int columnIndex) {
      // make the property name column non-navigable so that tab takes you
      // directly to the next property value cell.
      return (columnIndex != 0);
    }

    @Override
    public boolean isNavigationOn() {
      return true;
    }

    @Override
    public EditTokenProperty getProperty(int index) {
      return new EditTokenProperty(getPropertyList().get(index).getName());
    }

    @Override
    public int getPropertyCount() {
      return getPropertyList() != null ? getPropertyList().size() : 0;
    }

    class EditTokenProperty extends Property {

      private static final long serialVersionUID = 4129033551005743554L;
      private final String key;

      public EditTokenProperty(String key) {
        super(key, key, String.class, (String) getPropertyTypeCombo().getSelectedItem());
        this.setTableCellRenderer(propertyCellRenderer);
        this.key = key;
        setCellEditor(new MTMultilineStringCellEditor());
      }

      @Override
      public Object getValue() {
        return getPropertyMap().get(key);
      }

      @Override
      public void setValue(Object value) {
        getPropertyMap().put(key, (String) value);
      }

      @Override
      public boolean hasValue() {
        return getPropertyMap().get(key) != null;
      }
    }
  }

  private class OwnerListModel extends AbstractListModel {

    private static final long serialVersionUID = 2375600545516097234L;

    List<Selectable> ownerList = new ArrayList<Selectable>();

    public OwnerListModel() {
      Set<String> ownerSet = getModel().getOwners();
      List<String> list = new ArrayList<String>(ownerSet);

      List<Player> playerList = MapTool.getPlayerList();
      for (Object item : playerList) {
        Player player = (Player) item;
        String playerId = player.getName();
        if (!list.contains(playerId)) {
          list.add(playerId);
        }
      }
      Collections.sort(list);

      for (String id : list) {
        Selectable selectable = new DefaultSelectable(id);
        selectable.setSelected(ownerSet.contains(id));
        ownerList.add(selectable);
      }
    }

    public Object getElementAt(int index) {
      return ownerList.get(index);
    }

    public int getSize() {
      return ownerList.size();
    }
  }
}
