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
package net.rptools.maptool.client.ui.footprintEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.FootprintManager;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.util.FootPrintToolbox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tool for visually editing the token footprints associated with each grid type
 *
 * <p>Horizontal Hex = Pointy hat
 *
 * <p>Iso Hex = not yet implemented
 *
 * <p>Gridless = not yet implemented
 */
public class FootprintEditorDialog extends JDialog {
  private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

  public FootprintEditorDialog(JFrame owner) {
    super(owner, I18N.getText("FootprintEditorDialog.label.title"), false);
    initialise();
    pack();
  }

  // form elements
  private FootprintEditingPanel editor;
  AbeillePanel<JDialog> formPanel;
  private JSpinner scaleSpinner;
  private JCheckBox defaultCheckbox;
  public JRadioButton hexHorizontalRadio;
  public JRadioButton hexVerticalRadio;
  public JRadioButton squareRadio;
  public JRadioButton noGridRadio;
  public JRadioButton isoRadio;
  public JRadioButton isoHexRadio;
  public JLabel hexHorizontalIcon;
  public JLabel hexVerticalIcon;
  public JLabel isoIcon;
  public JLabel isoHexIcon;
  public JLabel noGridIcon;
  public JLabel squareIcon;
  public JTextField nameField;
  public JComboBox<TokenFootprint> footprintCombo;
  public JButton okButton;
  public JButton cancelButton;
  public JButton addButton;
  public JButton deleteButton;
  public JButton revertButton;
  public JButton saveButton;
  public JButton listOrderButton;
  public JPanel footprintDisplayContainer;
  // variables
  final boolean oldShowGrid = AppState.isShowGrid();
  private ComboBoxManager comboBoxManager;
  String currentGridType;
  private FPManager fpManager;
  private final ChangeTracking changeTrack = new ChangeTracking();
  Changes changes;
  private static final CellPoint ZERO_POINT = new CellPoint(0, 0);
  JLayeredPane layeredPane = new JLayeredPane();

  /** set up all the controls and variables prior to display */
  private void initialise() {
    //    RevsIconViewer riv = new RevsIconViewer(MapTool.getFrame());
    //    riv.setVisible(true);

    log.info("initialise");
    setLayout(new GridLayout());
    formPanel = new AbeillePanel<>(new FootPrintEditorView().getRootComponent());
    getRootPane().setDefaultButton(okButton);

    AppState.setShowGrid(true);
    currentGridType = FootPrintToolbox.getCurrentMapGridType();
    connectControls();
    initRadioButtons();

    fpManager = new FPManager();
    comboBoxManager = new ComboBoxManager();

    editor = new FootprintEditingPanel();
    footprintDisplayContainer.add(editor);
    editor.setGrid(currentGridType);

    addMiscellaneousListeners();
    fpManager.selectionChanged((TokenFootprint) footprintCombo.getSelectedItem(), null, null);
    add(formPanel);
    this.pack();
  }

  /**
   * add listeners to:
   *
   * <ul>
   *   <li>reset the the grid visibility to its original state on window closing
   *   <li>link escape key to cancel action
   *   <li>link +/- pgUp/pgDown to editor zoom level
   */
  // spotless:off
  private void addMiscellaneousListeners() {
    log.info("addMiscellaneousListeners");
    // reset grid visibility
    this.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) { AppState.setShowGrid(oldShowGrid);}});
    // Escape key
    formPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    formPanel.getActionMap().put("cancel", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {cancel();}});

    formPanel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "zoomOut");
    formPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut");
    formPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "zoomIn");
    formPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "zoomIn");
    formPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoomReset");
    formPanel.getActionMap().put("zoomOut", new AbstractAction() {
        public void actionPerformed(ActionEvent e) { editor.zoomOut();}});
    formPanel.getActionMap().put("zoomIn", new AbstractAction() {
        public void actionPerformed(ActionEvent e) { editor.zoomIn(); }});
    formPanel.getActionMap().put("zoomReset", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {editor.zoomReset();}});
  }
// spotless:on
  /** connect variables to their associated controls */
  private void connectControls() {
    log.info("connectControls");
    // editor panel
    footprintDisplayContainer = (JPanel) formPanel.getComponent("footprintDisplayContainer");
    // default checkbox
    defaultCheckbox = formPanel.getCheckBox("defaultCheckbox");
    defaultCheckbox.addActionListener(l -> fpManager.setAsDefault(defaultCheckbox.isSelected()));
    // radio button icons
    hexHorizontalIcon = (JLabel) formPanel.getComponent("hexHoriIcon");
    hexVerticalIcon = (JLabel) formPanel.getComponent("hexVertIcon");
    isoIcon = (JLabel) formPanel.getComponent("isoIcon");
    isoHexIcon = (JLabel) formPanel.getComponent("isoHexIcon");
    noGridIcon = (JLabel) formPanel.getComponent("noGridIcon");
    squareIcon = (JLabel) formPanel.getComponent("squareIcon");

    // radio buttons
    hexHorizontalRadio = formPanel.getRadioButton("hexHoriRadio");
    hexVerticalRadio = formPanel.getRadioButton("hexVertRadio");
    squareRadio = formPanel.getRadioButton("squareRadio");
    noGridRadio = formPanel.getRadioButton("noGridRadio");
    noGridRadio.setEnabled(false); // not yet implemented
    isoRadio = formPanel.getRadioButton("isoRadio");
    isoHexRadio = formPanel.getRadioButton("isoHexRadio");
    isoHexRadio.setEnabled(false); // not yet implemented

    // editable text field
    nameField = formPanel.getTextField("footprintName");
    nameField.addFocusListener(
        new FocusAdapter() {
          @Override
          public void focusLost(FocusEvent e) {
            super.focusLost(e);
            fpManager.setCurrentFootprintName();
          }
        });
    nameField.addActionListener(e -> fpManager.setCurrentFootprintName());
    nameField.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyPressed(KeyEvent e) {
            super.keyPressed(e);
            fpManager.setCurrentFootprintName();
          }
        });

    // scale slider
    scaleSpinner = formPanel.getSpinner("scaleSpinner");

    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(1.0, 0.2, 1.0, 0.05);
    scaleSpinner.setModel(spinnerModel);
    JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(scaleSpinner, "0.###");
    scaleSpinner.setEditor(numberEditor);

    scaleSpinner.addChangeListener(
        e ->
            fpManager.setScale(
                ((SpinnerNumberModel) scaleSpinner.getModel()).getNumber().doubleValue()));

    // editing buttons
    addButton = (JButton) formPanel.getButton("addButton");
    deleteButton = (JButton) formPanel.getButton("deleteButton");
    revertButton = (JButton) formPanel.getButton("revertButton");
    saveButton = (JButton) formPanel.getButton("saveButton");
    listOrderButton = (JButton) formPanel.getButton("listOrderButton");
    listOrderButton.setIcon(RessourceManager.getSmallIcon(Icons.PROPERTIES_TABLE_EXPAND));
    // add listeners assigning button actions to footprint manager functions
    addButton.addActionListener(e -> fpManager.addFootprint());
    deleteButton.addActionListener(e -> fpManager.deleteFootprint());
    revertButton.addActionListener(e -> fpManager.revertFootprint());
    saveButton.addActionListener(e -> fpManager.saveFootprint());
    listOrderButton.addActionListener(e -> fpManager.reorderList());

    // form buttons
    okButton = (JButton) formPanel.getButton("okButton");
    cancelButton = (JButton) formPanel.getButton("okButton");

    okButton.addActionListener(e -> okay());
    cancelButton.addActionListener(e -> cancel());
  }

  // spotless: on
  /**
   * set up the radio buttons:
   *
   * <ul>
   *   <li>assign action commands with grid types
   *   <li>assign icons to their labels
   *   <li>collect them into a group
   *   <li>add listener to control the footprint manager
   */
  private void initRadioButtons() {
    log.info("initRadioButtons");
    hexHorizontalRadio.setActionCommand("Horizontal Hex");
    hexVerticalRadio.setActionCommand("Vertical Hex");
    isoHexRadio.setActionCommand("Isometric Hex");
    isoRadio.setActionCommand("Isometric");
    noGridRadio.setActionCommand("None");
    squareRadio.setActionCommand("Square");

    // set icons //TODO: make iso hex icon
    isoIcon.setIcon(RessourceManager.getBigIcon(Icons.GRID_ISOMETRIC));
    isoHexIcon.setIcon(RessourceManager.getBigIcon(Icons.GRID_NONE));
    hexHorizontalIcon.setIcon(RessourceManager.getBigIcon(Icons.GRID_HEX_HORIZONTAL));
    hexVerticalIcon.setIcon(RessourceManager.getBigIcon(Icons.GRID_HEX_VERTICAL));
    noGridIcon.setIcon(RessourceManager.getBigIcon(Icons.GRID_NONE));
    squareIcon.setIcon(RessourceManager.getBigIcon(Icons.GRID_SQUARE));

    // add to group and add listeners
    ButtonGroup gridRadios = new ButtonGroup();
    gridRadios.add(hexHorizontalRadio);
    gridRadios.add(hexVerticalRadio);
    gridRadios.add(isoHexRadio);
    gridRadios.add(isoRadio);
    gridRadios.add(noGridRadio);
    gridRadios.add(squareRadio);

    ActionListener radioListener =
        e -> {
          currentGridType = e.getActionCommand();
          fpManager.changeCurrentGridType(currentGridType);
        };

    hexHorizontalRadio.addActionListener(radioListener);
    hexVerticalRadio.addActionListener(radioListener);
    isoHexRadio.addActionListener(radioListener);
    isoRadio.addActionListener(radioListener);
    noGridRadio.addActionListener(radioListener);
    squareRadio.addActionListener(radioListener);
    // set initial selection to default grid
    switch (currentGridType) {
      case GridFactory.HEX_VERT -> hexVerticalRadio.setSelected(true);
      case GridFactory.HEX_HORI -> hexHorizontalRadio.setSelected(true);
      case GridFactory.ISOMETRIC -> isoRadio.setSelected(true);
      case GridFactory.ISOMETRIC_HEX -> isoHexRadio.setSelected(false);
      case GridFactory.SQUARE -> squareRadio.setSelected(true);
      case GridFactory.NONE -> noGridRadio.setSelected(false);
    }
  }

  /** close dialogue accepting all changes */
  void okay() {
    fpManager.writeUpdates();
    setVisible(false);
    this.dispose();
  }

  /** close dialogue rejecting all changes */
  void cancel() {
    setVisible(false);
    this.dispose();
  }

  /**
   * As all fields of a footprint cannot be modified on-the-fly, this class is used to store the
   * current state of footprint fields
   */
  private static class Changes {
    double scale;
    String name;
    boolean asDefault;
    Set<CellPoint> cells;

    Changes(double scale, String name, Set<CellPoint> cells) {
      this.scale = scale;
      this.name = name;
      this.cells = cells;
    }

    Changes(TokenFootprint footprint) {
      this(footprint.getScale(), footprint.getName(), footprint.getOccupiedCells(ZERO_POINT));
    }

    void put(String fieldName, Object value) {
      switch (fieldName) {
        case "scale" -> this.scale = (double) value;
        case "name" -> this.name = (String) value;
        case "cells" -> {
          if (value instanceof Set<?>) {
            if (((Set<?>) value).stream().toList().getFirst() instanceof CellPoint) {
              cells.clear();
              cells.addAll((Set<CellPoint>) value);
            }
          }
        }
      }
    }

    double getScale() {
      return this.scale;
    }

    String getName() {
      return this.name;
    }

    Set<CellPoint> getCells() {
      return this.cells;
    }

    public String toString() {
      return "scale: "
          + this.scale
          + ", name: "
          + this.name
          + ", default: "
          + this.asDefault
          + ", cells: "
          + this.cells;
    }
  }

  /**
   * A class to store the changes linked to each footprint. just a glorified HashMap with a couple
   * of utility functions
   */
  private static class ChangeTracking {
    private final Map<TokenFootprint, Changes> changeTrack = new HashMap<>();

    public void put(TokenFootprint fp, Changes changeMap) {
      changeTrack.put(fp, changeMap);
    }

    public Changes getChanges(TokenFootprint fp) {
      return changeTrack.get(fp);
    }

    public void addChange(TokenFootprint footprint, String field, Object value) {
      Changes changes = getChanges(footprint);
      changes.put(field, value);
      changeTrack.put(footprint, changes);
    }

    public void remove(TokenFootprint footprint) {
      changeTrack.remove(footprint);
    }
  }

  /**
   * A manager class for keeping track of footprints - extends FootprintManager:
   *
   * <ul>
   *   <li>the current footprint being worked on "currentFootprint"
   *   <li>If there is one, the original version of the footprint to allow reversion
   *       "originalFootprint"
   *   <li>originals are stored in "originalMap"
   *   <li>a list of all the footprints mapped to grid type "gridFootprintListMap"
   *   <li>the list of footprints for the currently selected grid type "currentFootprintList"
   * </ul>
   *
   * Contains methods for:
   *
   * <ul>
   *   <li>footprint editing control actions, i.e. setScale, changeName, etc.
   *   <li>updating editing controls from the change track
   *   <li>identifying if the footprint has changed
   *   <li>writing footprints to the campaign
   *   <li>etc.
   * </ul>
   */
  private class FPManager extends FootprintManager {
    protected TokenFootprint currentFootprint;
    protected List<TokenFootprint> currentGridFootprintList = new LinkedList<>();
    protected Map<String, List<TokenFootprint>> gridMapToFootprintList = new HashMap<>();
    protected Map<String, TokenFootprint> gridDefaultFootprint = new HashMap<>();
    protected Map<TokenFootprint, TokenFootprint> originalsMap = new HashMap<>();
    protected TokenFootprint originalFootprint;

    public FPManager() {
      super(false);
      log.info("new FPManager");
      addAllFootprintsFromCampaign();
      if (currentFootprint == null) {
        currentFootprint = FootprintManager.getGlobalDefaultFootprint();
      }
      originalFootprint = currentFootprint;
    }

    private void addAllFootprintsFromCampaign() {
      for (String gridType : campaignFootprints.keySet()) {
        List<TokenFootprint> gridList = new LinkedList<>();
        for (TokenFootprint fp : campaignFootprints.get(gridType)) {
          changeTrack.put(fp, new Changes(fp));
          originalsMap.put(fp, fp);
          gridList.add(fp);
          if (fp.isDefault()) {
            gridDefaultFootprint.put(gridType, fp);
          }
          if (currentGridType.equalsIgnoreCase(gridType)) {
            currentFootprint = fp;
          }
        }
        gridMapToFootprintList.put(gridType, gridList);
        if (currentGridType.equalsIgnoreCase(gridType)) {
          currentGridFootprintList = gridList;
        }
      }
    }

    /**
     * Actions to take when the combo box selection changes
     *
     * @param toFootprint the newly selected footprint
     * @param fromFootprint the previously selected footprint(where available)
     */
    private void selectionChanged(
        TokenFootprint toFootprint, TokenFootprint fromFootprint, Changes changeUpdate) {
      log.info(
          "selectionChanged - "
              + FootPrintToolbox.stringifyFootprint(toFootprint)
              + " : "
              + FootPrintToolbox.stringifyFootprint(fromFootprint)
              + " : "
              + changes);
      if (fromFootprint != null && changeUpdate != null) {
        // apply all control values to the change track on the last footprint
        changeTrack.put(fromFootprint, changeUpdate);
      }
      // apply all values from the change track to the controls
      changes = changeTrack.getChanges(toFootprint);
      if (changes != null) {
        nameField.setText(
            changes.getName().isBlank() || changes.getName().isEmpty()
                ? ((TokenFootprint) footprintCombo.getSelectedItem()).getLocalizedName()
                : changes.getName());
        scaleSpinner.setValue(changes.getScale());
        editor.setTokenFootprint(currentGridType, toFootprint, changes.getCells());
      } else {
        nameField.setText(((TokenFootprint) footprintCombo.getSelectedItem()).getLocalizedName());
        editor.setTokenFootprint(currentGridType, toFootprint, null);
      }
      log.info(
          "Selection isDefault: " + getGridDefaultFootprint(currentGridType).equals(toFootprint));
      defaultCheckbox.setSelected(getGridDefaultFootprint(currentGridType).equals(toFootprint));
      setCurrentFootprint(toFootprint);
    }

    TokenFootprint getGridDefaultFootprint(String gridType) {
      return gridDefaultFootprint.get(gridType.equals("Isometric") ? "Square" : gridType);
    }

    List<TokenFootprint> getCurrentGridFootprintList() {
      return currentGridFootprintList;
    }

    /**
     * Compares a footprint against the stored changes
     *
     * @param fp TokenFootprint
     * @return boolean
     */
    public boolean hasChanged(TokenFootprint fp) {
      changes = changeTrack.getChanges(fp);
      boolean result = false;
      if (originalsMap.containsKey(fp)) {
        result =
            originalsMap.get(fp).isDefault() != getGridDefaultFootprint(currentGridType).equals(fp);
      }
      return result
          || fp.getScale() != changes.getScale()
          || !fp.getName().equals(changes.getName())
          || !fp.getOccupiedCells(ZERO_POINT).equals(changes.getCells())
          || !fp.getLocalizedName().equals(fp.getLocalizedName());
    }

    void writeUpdate(TokenFootprint fp, String gridType) {
      log.info("writeUpdate - " + gridType + fp);
      writeFootprintToCampaign(fp, gridType);
    }

    void writeUpdates() {
      writeAllFootprintsToCampaign(gridMapToFootprintList);
    }

    private void setScale(double s) {
      log.info("setScale: " + s);
      if (currentFootprint.getScale() != s) {
        changeTrack.addChange(currentFootprint, "scale", s);
      }
      editor.setScale(s);
      // TODO: check validity and update editor
    }

    @Override
    public void changeCurrentGridType(String gridType) {
      log.info("setCurrentGridFootprints");
      // store the old
      gridMapToFootprintList.put(currentGridType, getCurrentGridFootprintList());
      // replace with the new
      currentGridType = gridType;
      currentGridFootprintList = gridMapToFootprintList.get(currentGridType);
      // update the ui
      comboBoxManager.setVisibleComboBox();
      selectionChanged((TokenFootprint) footprintCombo.getSelectedItem(), null, null);
    }

    private void setCurrentFootprint(TokenFootprint footprint) {
      log.info("setCurrentFootprint - " + footprint.toString());
      currentFootprint = footprint;
      boolean exists = originalsMap.containsKey(footprint);
      revertButton.setEnabled(exists);
      if (exists) {
        originalFootprint = originalsMap.get(footprint);
      } else {
        originalFootprint = null;
      }
    }

    void FPMReplace(TokenFootprint fp1, TokenFootprint fp2) {
      if (getCurrentGridFootprintList().contains(fp1)) {
        int idx = currentGridFootprintList.indexOf(fp1);
        currentGridFootprintList.remove(idx);
        currentGridFootprintList.add(idx, fp2);
        changeTrack.remove(fp1);
        changeTrack.put(fp2, new Changes(fp2));
        comboBoxManager.comboReplace(fp1, fp2);
      }
    }

    public void setCurrentFootprintName() {
      log.info("setCurrentFootprintName");
      String name = nameField.getText();
      changeTrack.addChange(currentFootprint, "name", name);
      currentFootprint.setLocalizedName(name);
    }

    public TokenFootprint buildNewFootprint(TokenFootprint footprint) {
      changes = changeTrack.getChanges(footprint);
      String name = newName(changes.getName());
      return createTokenFootprint(
          currentGridType,
          name,
          getGridDefaultFootprint(currentGridType).equals(footprint),
          changes.getScale(),
          true,
          name,
          changes.getCells());
    }

    public TokenFootprint buildNewFootprint() {
      log.info("buildNewFootprint");
      changeTrack.addChange(currentFootprint, "name", nameField.getText());
      changeTrack.addChange(
          currentFootprint,
          "scale",
          ((SpinnerNumberModel) scaleSpinner.getModel()).getNumber().doubleValue());
      changeTrack.addChange(currentFootprint, "cells", editor.getCellSet());
      return buildNewFootprint(currentFootprint);
    }

    void setAsDefault(boolean value) {
      if (value) {
        gridDefaultFootprint.replace(currentGridType, currentFootprint);
      } else {
        gridDefaultFootprint.replace(currentGridType, currentGridFootprintList.getFirst());
      }
    }

    void addFootprint() {
      log.info("addFootprint");
      TokenFootprint newFp = buildNewFootprint();
      changeTrack.put(newFp, new Changes(newFp));
      currentGridFootprintList.add(newFp);
      setCurrentFootprint(newFp);
      setAsDefault(defaultCheckbox.isSelected());
      comboBoxManager.addToComboBox(newFp);
    }

    void deleteFootprint() {
      log.info("deleteFootprint");
      currentGridFootprintList.remove(currentFootprint);
      changeTrack.remove(currentFootprint);
      comboBoxManager.remove(currentFootprint);
    }

    String newName(String text) {
      List<TokenFootprint> useList = getCurrentGridFootprintList();
      text = text.isEmpty() ? "new_footprint" : text;
      boolean test;
      do {
        test = useList.stream().map(TokenFootprint::getName).toList().contains(text);
        if (!test) {
          test = useList.stream().map(TokenFootprint::getLocalizedName).toList().contains(text);
        }
        text = test ? text + "_" : text;
      } while (test);
      return text;
    }

    void saveFootprint() {
      log.info("saveFootprint");
      if (hasChanged(currentFootprint)) {
        TokenFootprint newFP = buildNewFootprint(currentFootprint);
        FPMReplace(currentFootprint, newFP);
        writeUpdate(newFP, currentGridType);
      }
    }

    void revertFootprint() {
      log.info("revertFootprint");
      if (originalFootprint != null) {
        currentFootprint = originalFootprint;
        changeTrack.remove(currentFootprint);
        changes = new Changes(originalFootprint);
        changeTrack.put(originalFootprint, changes);

        setAsDefault(originalFootprint.isDefault());
        scaleSpinner.setValue(changes.getScale());
        nameField.setText(changes.getName());
        editor.setTokenFootprint(currentGridType, originalFootprint, null);
      }
    }

    public void reorderList() {
      log.info("reorderList");
      JOptionPane.showMessageDialog(formPanel, "Whole lotta nuthin");
      // TODO: Token list order - update comboBox model
    }
  }

  /**
   * A class for juggling multiple combo boxes where only one is visible.
   *
   * <p>To reflect that footprints are stored against a type of grid, each unique grid type has its
   * own combo box stored in a Card Layout. When the type of grid is changed the relevant combo box
   * is made visible.
   *
   * <p>As a convenience measure the visible combo box is assigned to "footprintCombo".
   *
   * <p>Contains additional methods for edit actions such as adding/removing footprints from the
   * list.
   */
  private class ComboBoxManager {
    public JPanel comboBoxPanel;
    JComboBox<TokenFootprint> activeCombo = footprintCombo;
    static TokenFootprint lastSelectedFootprint = null;

    JComboBox<TokenFootprint> createComboBox(List<TokenFootprint> footprints) {
      JComboBox<TokenFootprint> combo = new JComboBox<>();
      MutableComboBoxModel<TokenFootprint> comboModel = new DefaultComboBoxModel<>();
      for (TokenFootprint footprint : footprints) {
        comboModel.addElement(footprint);
        if (footprint.isDefault()) {
          comboModel.setSelectedItem(footprint);
        }
      }
      combo.setModel(comboModel);
      combo.addItemListener(comboListener);
      return combo;
    }

    ComboBoxManager() {
      log.info("ComboBoxManager");
      footprintCombo = (JComboBox<TokenFootprint>) formPanel.getComboBox("footprintCombo");
      comboBoxPanel = (JPanel) formPanel.getComponent("comboBoxPanel");
      initFootprintCombo();
      setVisibleComboBox();
    }

    private void setVisibleComboBox() {
      log.info("setVisibleComboBox");
      lastSelectedFootprint = null;
      CardLayout cl = (CardLayout) (comboBoxPanel.getLayout());
      String useGrid = currentGridType.equalsIgnoreCase("Isometric") ? "Square" : currentGridType;
      cl.show(comboBoxPanel, useGrid + "Combo");

      for (Component comp : comboBoxPanel.getComponents()) {
        if ((useGrid + "Combo").equalsIgnoreCase(comp.getName())) {
          activeCombo = (JComboBox<TokenFootprint>) comp;
        }
      }
      footprintCombo = activeCombo;
      activeCombo.requestFocus();
    }

    void remove(TokenFootprint footprint) {
      if (footprintCombo.getModel().getSize() > 1) {
        MutableComboBoxModel<TokenFootprint> model =
            (MutableComboBoxModel<TokenFootprint>) footprintCombo.getModel();
        model.removeElement(footprint);
      }
    }

    void comboReplace(TokenFootprint fp1, TokenFootprint fp2) {
      MutableComboBoxModel<TokenFootprint> model =
          (MutableComboBoxModel<TokenFootprint>) footprintCombo.getModel();
      int i = 0;
      boolean finished = false;
      while (i < model.getSize() && !finished) {
        if (model.getElementAt(i).equals(fp1)) {
          model.removeElementAt(i);
          model.insertElementAt(fp2, i);
          finished = true;
        }
        i++;
      }
    }

    ItemListener comboListener =
        new ItemListener() {
          private Changes lastChangeRecord;

          @Override
          public void itemStateChanged(ItemEvent e) {
            log.info("Combo box event: " + e);
            TokenFootprint item = (TokenFootprint) e.getItem();
            if (e.getStateChange() == ItemEvent.DESELECTED) {
              // store the state of the outgoing footprint
              lastChangeRecord =
                  new Changes(
                      ((SpinnerNumberModel) scaleSpinner.getModel()).getNumber().doubleValue(),
                      nameField.getText(),
                      editor.getCellSet());
              // store the identity of the outgoing footprint
              lastSelectedFootprint = item;
            }
            if (e.getStateChange() == ItemEvent.SELECTED) {
              // advise the footprint manager of the selection change with the state at the point of
              // change
              fpManager.selectionChanged(item, lastSelectedFootprint, lastChangeRecord);
              lastChangeRecord = null;
            }
          }
        };

    private void initFootprintCombo() {
      log.info("initFootprintCombo");
      footprintCombo.setVisible(false);
      Map<String, List<TokenFootprint>> useCampaignFootprints =
          FootprintManager.getCampaignFootprints();

      for (String key : useCampaignFootprints.keySet()) {
        List<TokenFootprint> footPrints = useCampaignFootprints.get(key);
        JComboBox<TokenFootprint> comboBox = createComboBox(footPrints);
        comboBox.setEnabled(true);
        comboBox.setFocusable(true);
        comboBox.setRequestFocusEnabled(true);
        comboBox.setActionCommand(footprintCombo.getActionCommand());
        comboBox.setName(key + "Combo");
        comboBox.addItemListener(comboListener);
        comboBoxPanel.add(comboBox, comboBox.getName());
      }
    }

    public void addToComboBox(TokenFootprint newFp) {
      log.info("addToComboBox - " + newFp);
      ((MutableComboBoxModel<TokenFootprint>) footprintCombo.getModel()).addElement(newFp);
      footprintCombo.setSelectedItem(newFp);
    }
  }

  // spotless:off
  private void createZoomButtons() {
    layeredPane = this.getLayeredPane();
    JPanel buttonHolder = new JPanel();
    //    buttonHolder.setPreferredSize(layeredPane.getPreferredSize());
    buttonHolder.setBackground(new Color(80, 180, 80));
    buttonHolder.setBorder(BorderFactory.createLineBorder(Color.BLUE, 3, true));
    String oldTheme = AppPreferences.getIconTheme();
    AppPreferences.setIconTheme(RessourceManager.ROD_TAKEHARA);
    JButton zInButton = new JButton(RessourceManager.getBigIcon(Icons.TOOLBAR_HIDE_OFF));
    JButton zOutButton = new JButton(RessourceManager.getBigIcon(Icons.TOOLBAR_HIDE_ON));
    JButton zResetButton =
        new JButton(RessourceManager.getBigIcon(Icons.TOOLBAR_TOPOLOGY_OVAL_HOLLOW));
    AppPreferences.setIconTheme(oldTheme);
    Dimension buttonSize = new Dimension(36, 36);
    Color bg = new Color(0, 0, 0, 0);
    zInButton.setBackground(bg);
    zOutButton.setBackground(bg);
    zResetButton.setBackground(bg);
    zInButton.setPreferredSize(buttonSize);
    zInButton.setPreferredSize(buttonSize);
    zOutButton.setPreferredSize(buttonSize);
    zResetButton.setPreferredSize(buttonSize);
    zInButton.addActionListener(e -> editor.zoomIn());
    zOutButton.addActionListener(e -> editor.zoomOut());
    zResetButton.addActionListener(e -> editor.zoomReset());

    zInButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    zOutButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    zResetButton.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    buttonHolder.setLayout(new BoxLayout(buttonHolder, BoxLayout.Y_AXIS));
    buttonHolder.add(zInButton);
    buttonHolder.add(zResetButton);
    buttonHolder.add(zOutButton);
    buttonHolder.validate();
    buttonHolder.setBounds(8, 70, 40, 120);
    layeredPane.add(buttonHolder, Integer.valueOf(450));
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
    editor.requestFocusInWindow();
  }

  @Override
  public void dispose() {
    AppState.setShowGrid(oldShowGrid);
    super.dispose();
  }
}
