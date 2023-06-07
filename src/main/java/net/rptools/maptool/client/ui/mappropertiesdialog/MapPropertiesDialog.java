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
package net.rptools.maptool.client.ui.mappropertiesdialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.PaintChooser;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.PreviewPanelFileChooser;
import net.rptools.maptool.client.ui.TextureChooserPanel;
import net.rptools.maptool.client.ui.assetpanel.AssetDirectory;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
import net.rptools.maptool.client.ui.assetpanel.AssetPanelModel;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.GridlessGrid;
import net.rptools.maptool.model.HexGridHorizontal;
import net.rptools.maptool.model.HexGridVertical;
import net.rptools.maptool.model.IsometricGrid;
import net.rptools.maptool.model.SquareGrid;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.StringUtil;

public class MapPropertiesDialog extends JDialog {
  private static final int AUTO_REPEAT_THRESHOLD = 200;

  public enum Status {
    OK,
    CANCEL
  }

  private static File lastFilePath;

  private Status status;

  private PreviewPanelFileChooser imageFileChooser;
  private MapPreviewPanel imagePreviewPanel;
  private MapSelectorDialog mapSelectorDialog;
  private AbeillePanel formPanel;

  private DrawablePaint backgroundPaint;
  private Asset mapAsset;
  private DrawablePaint fogPaint;

  private Zone zone;
  private PaintChooser paintChooser;

  // As a new grid is created from scratch, need to hold on to these values.
  private int gridOffsetX = 0;
  private int gridOffsetY = 0;

  public static MapPropertiesDialog createMapPropertiesDialog(JFrame owner) {
    return new MapPropertiesDialog(I18N.getText("dialog.mapProperties.title"), owner);
  }

  public static MapPropertiesDialog createMapPropertiesImportDialog(JFrame owner) {
    return new MapPropertiesDialog(I18N.getText("dialog.importedMapProperties.title"), owner);
  }

  private MapPropertiesDialog(String title, JFrame owner) {
    super(owner, title, true);
    initialize();
    pack();
  }

  public Status getStatus() {
    return status;
  }

  /**
   * Set the pixels per cell value and stop user from editing.
   *
   * @param pixels the pixels per cell to set.
   */
  public void forcePixelsPerCell(int pixels) {
    getPixelsPerCellTextField().setText(Integer.toString(pixels));
    getPixelsPerCellTextField().setEditable(false);
  }

  public void forceMap(Asset asset) {
    setMapAsset(asset);
    getMapButton().setEnabled(false);
  }

  public void forceGridType(String gridType) {
    if (GridFactory.isHexVertical(gridType)) {
      getHexVerticalRadio().setSelected(true);
    } else if (GridFactory.isHexHorizontal(gridType)) {
      getHexHorizontalRadio().setSelected(true);
    } else if (GridFactory.isIsometric(gridType)) {
      getIsometricRadio().setSelected(true);
    } else if (GridFactory.isSquare(gridType)) {
      getSquareRadio().setSelected(true);
    } else {
      getNoGridRadio().setSelected(true);
    }
    getHexVerticalRadio().setEnabled(false);
    getHexHorizontalRadio().setEnabled(false);
    getIsometricRadio().setEnabled(false);
    getSquareRadio().setEnabled(false);
    getNoGridRadio().setEnabled(false);
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
  }

  private void initialize() {
    setLayout(new GridLayout());
    formPanel = new AbeillePanel(new MapPropertiesDialogView().getRootComponent());

    initDistanceTextField();

    initOKButton();
    initCancelButton();

    initBackgroundButton();
    initFogButton();
    initMapButton();

    initMapPreviewPanel();

    initDistanceTextField();
    initPlayerAliasTextField();
    initPixelsPerCellTextField();
    initDefaultVisionTextField();
    initVisionTypeCombo();
    initLightingStyleCombo();
    initAStarRoundingOptionsComboBox();

    initIsometricRadio();
    initHexHoriRadio();
    initHexVertRadio();
    initSquareRadio();
    initNoGridRadio();

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
    // Color picker
    paintChooser = new PaintChooser();
    AssetPanelModel model = new AssetPanelModel();
    Set<File> assetRootList = AppPreferences.getAssetRoots();
    for (File file : assetRootList) {
      model.addRootGroup(new AssetDirectory(file, AppConstants.IMAGE_FILE_FILTER));
    }

    TextureChooserPanel textureChooserPanel =
        new TextureChooserPanel(paintChooser, model, "mapPropertiesTextureChooser");
    paintChooser.addPaintChooser(textureChooserPanel);
    mapSelectorDialog = new MapSelectorDialog();
    getRootPane().setDefaultButton(getOKButton());
  }

  private void cancel() {
    status = Status.CANCEL;
    setVisible(false);
  }

  private void accept() {
    // Push the assets to the server
    MapToolUtil.uploadTexture(backgroundPaint);
    MapToolUtil.uploadTexture(fogPaint);
    if (mapAsset != null) {
      AssetManager.putAsset(mapAsset);
      if (!MapTool.isHostingServer()) {
        MapTool.serverCommand().putAsset(mapAsset);
      }
    }
    copyUIToZone();
    status = Status.OK;
    setVisible(false);
  }

  public JCheckBox getRepeatCheckBox() {
    return formPanel.getCheckBox("repeat");
  }

  public JRadioButton getHexHorizontalRadio() {
    return formPanel.getRadioButton("hexHoriRadio");
  }

  public JLabel getHexHorizontalIcon() {
    return (JLabel) formPanel.getComponent("hexHoriIcon");
  }

  public JRadioButton getHexVerticalRadio() {
    return formPanel.getRadioButton("hexVertRadio");
  }

  public JLabel getHexVerticalIcon() {
    return (JLabel) formPanel.getComponent("hexVertIcon");
  }

  public JRadioButton getSquareRadio() {
    return formPanel.getRadioButton("squareRadio");
  }

  public JLabel getSquareIcon() {
    return (JLabel) formPanel.getComponent("squareIcon");
  }

  public JRadioButton getNoGridRadio() {
    return formPanel.getRadioButton("noGridRadio");
  }

  public JLabel getNoGridIcon() {
    return (JLabel) formPanel.getComponent("noGridIcon");
  }

  public JRadioButton getIsometricRadio() {
    return formPanel.getRadioButton("isoRadio");
  }

  public JLabel getIsometricIcon() {
    return (JLabel) formPanel.getComponent("isoIcon");
  }

  public JRadioButton getIsometricHexRadio() {
    return formPanel.getRadioButton("isoHexRadio");
  }

  public JComboBox getVisionTypeCombo() {
    return formPanel.getComboBox("visionType");
  }

  public JComboBox<Zone.LightingStyle> getLightingStyleCombo() {
    return formPanel.getComboBox("lightingStyle");
  }

  public JComboBox getAStarRoundingOptionsComboBox() {
    return formPanel.getComboBox("aStarRoundingOptionsComboBox");
  }

  public void setZone(Zone zone) {
    this.zone = zone;
    copyZoneToUI();
  }

  private void copyZoneToUI() {
    getNameTextField().setText(zone.getName());
    getPlayerAliasTextField().setText(zone.getPlayerAlias());
    // Localizes units per cell, using the proper separator. Fixes #507.
    getDistanceTextField().setText(StringUtil.formatDecimal(zone.getUnitsPerCell(), 1));
    getPixelsPerCellTextField().setText(Integer.toString(zone.getGrid().getSize()));
    getDefaultVisionTextField().setText(Integer.toString(zone.getTokenVisionDistance()));
    getHexVerticalRadio().setSelected(zone.getGrid() instanceof HexGridVertical);
    getIsometricRadio().setSelected(zone.getGrid() instanceof IsometricGrid);
    getHexHorizontalRadio().setSelected(zone.getGrid() instanceof HexGridHorizontal);
    getSquareRadio().setSelected(zone.getGrid() instanceof SquareGrid);
    getNoGridRadio().setSelected(zone.getGrid() instanceof GridlessGrid);
    getVisionTypeCombo().setSelectedItem(zone.getVisionType());
    getLightingStyleCombo().setSelectedItem(zone.getLightingStyle());
    getAStarRoundingOptionsComboBox().setSelectedItem(zone.getAStarRounding());

    gridOffsetX = zone.getGrid().getOffsetX();
    gridOffsetY = zone.getGrid().getOffsetY();

    fogPaint = zone.getFogPaint();
    backgroundPaint = zone.getBackgroundPaint();
    mapAsset = AssetManager.getAsset(zone.getMapAssetId());
  }

  private void copyUIToZone() {
    zone.setName(getNameTextField().getText().trim());
    zone.setUnitsPerCell(
        StringUtil.parseDecimal(getDistanceTextField().getText(), zone.getUnitsPerCell()));
    zone.setPlayerAlias(getPlayerAliasTextField().getText().trim());
    zone.setGrid(createZoneGrid());
    zone.setTokenVisionDistance(
        StringUtil.parseInteger(
            getDefaultVisionTextField().getText(), zone.getTokenVisionDistance()));

    zone.setVisionType((Zone.VisionType) getVisionTypeCombo().getSelectedItem());
    zone.setLightingStyle((Zone.LightingStyle) getLightingStyleCombo().getSelectedItem());
    zone.setAStarRounding(
        (Zone.AStarRoundingOptions) getAStarRoundingOptionsComboBox().getSelectedItem());

    zone.setFogPaint(fogPaint);
    zone.setBackgroundPaint(backgroundPaint);
    zone.setMapAsset(mapAsset != null ? mapAsset.getMD5Key() : null);
    // TODO: Handle grid type changes
  }

  private void initIsometricRadio() {
    getIsometricRadio().setSelected(GridFactory.isIsometric(AppPreferences.getDefaultGridType()));
    getIsometricIcon().setIcon(RessourceManager.getSmallIcon(Icons.GRID_ISOMETRIC));
  }

  private void initHexHoriRadio() {
    getHexHorizontalRadio()
        .setSelected(GridFactory.isHexHorizontal(AppPreferences.getDefaultGridType()));
    getHexHorizontalIcon().setIcon(RessourceManager.getSmallIcon(Icons.GRID_HEX_HORIZONTAL));
  }

  private void initHexVertRadio() {
    getHexVerticalRadio()
        .setSelected(GridFactory.isHexVertical(AppPreferences.getDefaultGridType()));
    getHexVerticalIcon().setIcon(RessourceManager.getSmallIcon(Icons.GRID_HEX_VERTICAL));
  }

  private void initSquareRadio() {
    getSquareRadio().setSelected(GridFactory.isSquare(AppPreferences.getDefaultGridType()));
    getSquareIcon().setIcon(RessourceManager.getSmallIcon(Icons.GRID_SQUARE));
  }

  private void initNoGridRadio() {
    getNoGridRadio().setSelected(GridFactory.isNone(AppPreferences.getDefaultGridType()));
    getNoGridIcon().setIcon(RessourceManager.getSmallIcon(Icons.GRID_NONE));
  }

  public JTextField getDistanceTextField() {
    return formPanel.getTextField("distance");
  }

  private void initDistanceTextField() {
    getDistanceTextField().setText("5");
  }

  public JTextField getPlayerAliasTextField() {
    return formPanel.getTextField("playerMapAlias");
  }

  private void initPlayerAliasTextField() {
    getPlayerAliasTextField().setText("NewMap0000");
  }

  private void initMapPreviewPanel() {
    JPanel previewPanel = new JPanel(new GridLayout());
    previewPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
    previewPanel.add(getMapPreviewPanel());
    formPanel.replaceComponent("previewPanel", "mapPreviewPanel", previewPanel);
  }

  public JTextField getNameTextField() {
    return formPanel.getTextField("name");
  }

  public JButton getOKButton() {
    return (JButton) formPanel.getButton("okButton");
  }

  private void initOKButton() {
    getOKButton().addActionListener(e -> accept());
  }

  public JButton getBackgroundButton() {
    return (JButton) formPanel.getButton("backgroundButton");
  }

  public JButton getMapButton() {
    return (JButton) formPanel.getButton("mapButton");
  }

  public JButton getFogButton() {
    return (JButton) formPanel.getButton("fogButton");
  }

  private void initBackgroundButton() {
    getBackgroundButton()
        .addActionListener(
            e -> {
              Paint paint =
                  paintChooser.choosePaint(
                      MapTool.getFrame(),
                      backgroundPaint != null ? backgroundPaint.getPaint() : null,
                      I18N.getText("MapPropertiesDialog.label.background"));
              if (paint != null) {
                backgroundPaint = DrawablePaint.convertPaint(paint);
              }
              updatePreview();
            });
  }

  private void setMapAsset(Asset asset) {
    mapAsset = asset;
    if (asset != null) {
      getNameTextField().setText(asset.getName());
    }
    updatePreview();
  }

  private void initMapButton() {
    getMapButton()
        .addActionListener(
            e -> {
              mapSelectorDialog.pack();
              Asset asset = mapSelectorDialog.chooseAsset();
              if (asset == null) {
                return;
              }
              setMapAsset(asset);
            });
  }

  private void initFogButton() {
    getFogButton()
        .addActionListener(
            e -> {
              Paint paint =
                  paintChooser.choosePaint(
                      MapTool.getFrame(),
                      fogPaint != null ? fogPaint.getPaint() : null,
                      I18N.getText("MapPropertiesDialog.label.fog"));
              if (paint != null) {
                fogPaint = DrawablePaint.convertPaint(paint);
              }
              updatePreview();
            });
  }

  private void updatePreview() {
    getMapPreviewPanel().repaint();
  }

  public JButton getCancelButton() {
    return (JButton) formPanel.getButton("cancelButton");
  }

  private void initCancelButton() {
    getCancelButton()
        .addActionListener(
            e -> {
              status = Status.CANCEL;
              setVisible(false);
            });
  }

  private PreviewPanelFileChooser getImageFileChooser() {
    if (imageFileChooser == null) {
      imageFileChooser = new PreviewPanelFileChooser();
      imageFileChooser.setFileFilter(
          new FileFilter() {
            @Override
            public boolean accept(File f) {
              return f.isDirectory()
                  || AppConstants.IMAGE_FILE_FILTER.accept(f.getAbsoluteFile(), f.getName());
            }

            @Override
            public String getDescription() {
              return "Images only";
            }
          });
      if (lastFilePath != null) {
        imageFileChooser.setCurrentDirectory(lastFilePath);
      }
    }
    return imageFileChooser;
  }

  private MapPreviewPanel getMapPreviewPanel() {
    if (imagePreviewPanel == null) {
      imagePreviewPanel = new MapPreviewPanel();
      imagePreviewPanel.setPreferredSize(new Dimension(150, 150));
      imagePreviewPanel.setMinimumSize(new Dimension(150, 150));
    }
    return imagePreviewPanel;
  }

  public JTextField getPixelsPerCellTextField() {
    return formPanel.getTextField("pixelsPerCell");
  }

  private void initPixelsPerCellTextField() {
    getPixelsPerCellTextField().setText(Integer.toString(AppPreferences.getDefaultGridSize()));
  }

  public JTextField getDefaultVisionTextField() {
    return formPanel.getTextField("defaultVision");
  }

  private void initDefaultVisionTextField() {
    this.getDefaultVisionTextField()
        .setText(Integer.toString(AppPreferences.getDefaultVisionDistance()));
  }

  private void initVisionTypeCombo() {
    DefaultComboBoxModel<Zone.VisionType> model = new DefaultComboBoxModel<>();
    for (Zone.VisionType vt : Zone.VisionType.values()) {
      model.addElement(vt);
    }
    model.setSelectedItem(AppPreferences.getDefaultVisionType());
    getVisionTypeCombo().setModel(model);
  }

  private void initAStarRoundingOptionsComboBox() {
    getAStarRoundingOptionsComboBox()
        .setModel(new DefaultComboBoxModel<>(Zone.AStarRoundingOptions.values()));
  }

  private void initLightingStyleCombo() {
    DefaultComboBoxModel<Zone.LightingStyle> model = new DefaultComboBoxModel<>();
    for (Zone.LightingStyle vt : Zone.LightingStyle.values()) {
      model.addElement(vt);
    }
    model.setSelectedItem(Zone.LightingStyle.OVERTOP);
    getLightingStyleCombo().setModel(model);
  }

  public String getZoneName() {
    return getNameTextField().getText();
  }

  public int getZoneDistancePerCell() {
    try {
      // TODO: Handle this in validation
      return Integer.parseInt(getDistanceTextField().getText());
    } catch (NumberFormatException nfe) {
      return 0;
    }
  }

  private Grid createZoneGrid() {
    Grid grid = null;
    if (getHexHorizontalRadio().isSelected()) {
      grid =
          GridFactory.createGrid(
              GridFactory.HEX_HORI, AppPreferences.getFaceEdge(), AppPreferences.getFaceVertex());
    }
    if (getHexVerticalRadio().isSelected()) {
      grid =
          GridFactory.createGrid(
              GridFactory.HEX_VERT, AppPreferences.getFaceEdge(), AppPreferences.getFaceVertex());
    }
    if (getSquareRadio().isSelected()) {
      grid =
          GridFactory.createGrid(
              GridFactory.SQUARE, AppPreferences.getFaceEdge(), AppPreferences.getFaceVertex());
    }
    if (getIsometricRadio().isSelected()) {
      grid =
          GridFactory.createGrid(
              GridFactory.ISOMETRIC, AppPreferences.getFaceEdge(), AppPreferences.getFaceVertex());
    }
    if (getNoGridRadio().isSelected()) {
      grid = GridFactory.createGrid(GridFactory.NONE);
    }
    grid.setSize(StringUtil.parseInteger(getPixelsPerCellTextField().getText(), grid.getSize()));

    grid.setOffset(gridOffsetX, gridOffsetY);

    return grid;
  }

  private class MapSelectorDialog extends JDialog {
    private static final long serialVersionUID = -854043369053089633L;

    private Asset selectedAsset;

    public MapSelectorDialog() {
      super(MapTool.getFrame(), true);
      setLayout(new BorderLayout());

      add(BorderLayout.CENTER, createImageExplorerPanel());
      add(BorderLayout.SOUTH, createButtonBar());
      this.setTitle(I18N.getText("MapPropertiesDialog.label.image"));
    }

    @Override
    public void setVisible(boolean b) {
      if (b) {
        SwingUtil.centerOver(this, MapTool.getFrame());
      }
      super.setVisible(b);
    }

    private JPanel createButtonBar() {
      JPanel panel = new JPanel(new BorderLayout());

      JPanel leftPanel = new JPanel();
      leftPanel.add(createFilesystemButton());

      JPanel rightPanel = new JPanel();
      rightPanel.add(createOKButton());
      rightPanel.add(createCancelButton());

      panel.add(BorderLayout.WEST, leftPanel);
      panel.add(BorderLayout.EAST, rightPanel);

      return panel;
    }

    private JButton createFilesystemButton() {
      JButton button = new JButton(I18N.getText("Label.filesystem"));
      button.addActionListener(
          e -> {
            setVisible(false);
            if (getImageFileChooser().showOpenDialog(MapPropertiesDialog.this)
                == JFileChooser.APPROVE_OPTION) {
              File imageFile = getImageFileChooser().getSelectedFile();
              if (imageFile == null || imageFile.isDirectory()) {
                return;
              }
              lastFilePath = new File(imageFile.getParentFile() + "/.");
              try {
                selectedAsset = AssetManager.createAsset(imageFile);

                // Store for later use
                AssetManager.putAsset(selectedAsset);
                updatePreview();
                // setBackgroundAsset(asset, getImageFileChooser().getSelectedThumbnailImage());
              } catch (IOException ioe) {
                MapTool.showError("Could not load that map: " + ioe);
                selectedAsset = null;
              }
            }
          });
      return button;
    }

    private JButton createOKButton() {
      JButton button = new JButton(I18N.getText("Button.ok"));
      button.addActionListener(e -> setVisible(false));
      return button;
    }

    private JButton createCancelButton() {
      JButton button = new JButton(I18N.getText("Button.cancel"));
      button.addActionListener(
          e -> {
            selectedAsset = null;
            setVisible(false);
          });
      return button;
    }

    public Asset chooseAsset() {
      setVisible(true);
      return selectedAsset;
    }

    private JComponent createImageExplorerPanel() {
      AssetPanelModel model = new AssetPanelModel();
      Set<File> assetRootList = AppPreferences.getAssetRoots();
      for (File file : assetRootList) {
        model.addRootGroup(new AssetDirectory(file, AppConstants.IMAGE_FILE_FILTER));
      }
      final AssetPanel assetPanel =
          new AssetPanel("mapPropertiesImageExplorer", model, JSplitPane.HORIZONTAL_SPLIT);

      assetPanel.addImageSelectionListener(
          selectedList -> {
            // There should be exactly one
            if (selectedList.size() != 1) {
              return;
            }
            Integer imageIndex = (Integer) selectedList.get(0);

            // if (getBackgroundAsset() != null) {
            // // Tighten memory usage
            // ImageManager.flushImage(getBackgroundAsset());
            // }
            selectedAsset = assetPanel.getAsset(imageIndex);

            // Store for later use
            if (selectedAsset != null) {
              AssetManager.putAsset(selectedAsset);
            }
          });
      return assetPanel;
    }
  }

  private class MapPreviewPanel extends JComponent {
    private static final long serialVersionUID = 3761329103161077644L;

    private JButton cancelButton;

    private MapPreviewPanel() {
      setLayout(new BorderLayout());
      JPanel northPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      northPanel.setOpaque(false);
      northPanel.add(getCancelButton());
      add(BorderLayout.NORTH, northPanel);
    }

    private JButton getCancelButton() {
      if (cancelButton == null) {
        cancelButton = new JButton(RessourceManager.getSmallIcon(Icons.ACTION_CANCEL));
        cancelButton.setContentAreaFilled(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setFocusable(false);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));

        cancelButton.addActionListener(e -> setMapAsset(null));
      }
      return cancelButton;
    }

    @Override
    protected void paintComponent(Graphics g) {
      Dimension size = getSize();
      Graphics2D g2d = (Graphics2D) g;

      g.setColor(Color.gray);
      g.fillRect(0, 0, size.width, size.height);

      // Tile
      if (backgroundPaint != null) {
        g2d.setPaint(backgroundPaint.getPaint(drawableObserver));
        g.fillRect(0, 0, size.width, size.height);
      }
      // Fog
      if (fogPaint != null) {
        g2d.setPaint(fogPaint.getPaint());
        g.fillRect(0, 0, size.width, 10);
        g.fillRect(0, 0, 10, size.height);
        g.fillRect(0, size.height - 10, size.width, 10);
        g.fillRect(size.width - 10, 0, 10, size.height);
      }
      // Map
      if (mapAsset != null) {
        BufferedImage image = ImageManager.getImageAndWait(mapAsset.getMD5Key());
        Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
        SwingUtil.constrainTo(imgSize, size.width - 10 * 4, size.height - 10 * 4);

        int x = (size.width - imgSize.width) / 2;
        int y = (size.height - imgSize.height) / 2;

        g.drawImage(image, x, y, imgSize.width, imgSize.height, this);
      }

      getCancelButton().setVisible(mapAsset != null);
    }
  }

  private final ImageObserver drawableObserver =
      (img, infoflags, x, y, width, height) -> {
        MapPropertiesDialog.this.imagePreviewPanel.repaint();
        return true;
      };
}
