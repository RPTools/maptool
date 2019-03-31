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
import com.jeta.forms.gui.form.FormAccessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import net.rptools.lib.swing.PaintChooser;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.assetpanel.AssetPanel;
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
  private FormPanel formPanel;

  private DrawablePaint backgroundPaint;
  private Asset mapAsset;
  private DrawablePaint fogPaint;

  private Zone zone;
  private PaintChooser paintChooser;

  public MapPropertiesDialog(JFrame owner) {
    super(owner, "Map Properties", true);
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
    }
    super.setVisible(b);
  }

  private void initialize() {
    setLayout(new GridLayout());
    formPanel = new FormPanel("net/rptools/maptool/client/ui/forms/mapPropertiesDialog.xml");

    initDistanceTextField();

    initOKButton();
    initCancelButton();

    initBackgroundButton();
    initFogButton();
    initMapButton();

    initMapPreviewPanel();

    initDistanceTextField();
    initPixelsPerCellTextField();
    initDefaultVisionTextField();

    initIsometricRadio();
    initHexHoriRadio();
    initHexVertRadio();
    initSquareRadio();

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
    TextureChooserPanel textureChooserPanel =
        new TextureChooserPanel(
            paintChooser,
            MapTool.getFrame().getAssetPanel().getModel(),
            "mapPropertiesTextureChooser");
    paintChooser.addPaintChooser(textureChooserPanel);
    paintChooser.setPreferredSize(new Dimension(450, 400));
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

  public JRadioButton getHexVerticalRadio() {
    return formPanel.getRadioButton("hexVertRadio");
  }

  public JRadioButton getSquareRadio() {
    return formPanel.getRadioButton("squareRadio");
  }

  public JRadioButton getNoGridRadio() {
    return formPanel.getRadioButton("noGridRadio");
  }

  public JRadioButton getIsometricRadio() {
    return formPanel.getRadioButton("isoRadio");
  }

  public JRadioButton getIsometricHexRadio() {
    return formPanel.getRadioButton("isoHexRadio");
  }

  public void setZone(Zone zone) {
    this.zone = zone;
    copyZoneToUI();
  }

  private void copyZoneToUI() {
    getNameTextField().setText(zone.getName());
    getDistanceTextField().setText(Double.toString(zone.getUnitsPerCell()));
    getPixelsPerCellTextField().setText(Integer.toString(zone.getGrid().getSize()));
    getDefaultVisionTextField().setText(Integer.toString(zone.getTokenVisionDistance()));
    getHexVerticalRadio().setSelected(zone.getGrid() instanceof HexGridVertical);
    getIsometricRadio().setSelected(zone.getGrid() instanceof IsometricGrid);
    getHexHorizontalRadio().setSelected(zone.getGrid() instanceof HexGridHorizontal);
    getSquareRadio().setSelected(zone.getGrid() instanceof SquareGrid);
    getNoGridRadio().setSelected(zone.getGrid() instanceof GridlessGrid);

    fogPaint = zone.getFogPaint();
    backgroundPaint = zone.getBackgroundPaint();
    mapAsset = AssetManager.getAsset(zone.getMapAssetId());
  }

  private void copyUIToZone() {
    zone.setName(getNameTextField().getText().trim());
    zone.setUnitsPerCell(
        StringUtil.parseDecimal(getDistanceTextField().getText(), zone.getUnitsPerCell()));
    zone.setGrid(createZoneGrid());
    zone.setTokenVisionDistance(
        StringUtil.parseInteger(
            getDefaultVisionTextField().getText(), zone.getTokenVisionDistance()));

    zone.setFogPaint(fogPaint);
    zone.setBackgroundPaint(backgroundPaint);
    zone.setMapAsset(mapAsset != null ? mapAsset.getId() : null);
    // TODO: Handle grid type changes
  }

  private void initIsometricRadio() {
    getIsometricRadio().setSelected(GridFactory.isIsometric(AppPreferences.getDefaultGridType()));
  }

  private void initHexHoriRadio() {
    getHexHorizontalRadio()
        .setSelected(GridFactory.isHexHorizontal(AppPreferences.getDefaultGridType()));
  }

  private void initHexVertRadio() {
    getHexVerticalRadio()
        .setSelected(GridFactory.isHexVertical(AppPreferences.getDefaultGridType()));
  }

  private void initSquareRadio() {
    getSquareRadio().setSelected(GridFactory.isSquare(AppPreferences.getDefaultGridType()));
  }

  private void initNoGridRadio() {
    getNoGridRadio().setSelected(GridFactory.isNone(AppPreferences.getDefaultGridType()));
  }

  public JTextField getDistanceTextField() {
    return formPanel.getTextField("distance");
  }

  private void initDistanceTextField() {
    getDistanceTextField().setText("5");
  }

  private void initMapPreviewPanel() {
    FormAccessor accessor = formPanel.getFormAccessor("previewPanel");
    JPanel previewPanel = new JPanel(new GridLayout());
    previewPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
    previewPanel.add(getMapPreviewPanel());
    accessor.replaceBean("mapPreviewPanel", previewPanel);
  }

  public JTextField getNameTextField() {
    return formPanel.getTextField("name");
  }

  public JButton getOKButton() {
    return (JButton) formPanel.getButton("okButton");
  }

  private void initOKButton() {
    getOKButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                accept();
              }
            });
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
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Paint paint =
                    paintChooser.choosePaint(
                        MapTool.getFrame(),
                        backgroundPaint != null ? backgroundPaint.getPaint() : null,
                        "Choose Background");
                if (paint != null) {
                  backgroundPaint = DrawablePaint.convertPaint(paint);
                }
                updatePreview();
              }
            });
  }

  private void initMapButton() {
    getMapButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Asset asset = mapSelectorDialog.chooseAsset();
                if (asset == null) {
                  return;
                }
                mapAsset = asset;
                getNameTextField().setText(asset.getName());
                updatePreview();
              }
            });
  }

  private void initFogButton() {
    getFogButton()
        .addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                Paint paint =
                    paintChooser.choosePaint(
                        MapTool.getFrame(),
                        fogPaint != null ? fogPaint.getPaint() : null,
                        "Choose Fog");
                if (paint != null) {
                  fogPaint = DrawablePaint.convertPaint(paint);
                }
                updatePreview();
              }
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
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                status = Status.CANCEL;
                setVisible(false);
              }
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
      this.setTitle("Select Map Image");
      setSize(500, 400);
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
      // leftPanel.add(createClearButton());

      JPanel rightPanel = new JPanel();
      rightPanel.add(createOKButton());
      rightPanel.add(createCancelButton());

      panel.add(BorderLayout.WEST, leftPanel);
      panel.add(BorderLayout.EAST, rightPanel);

      return panel;
    }

    private JButton createFilesystemButton() {
      JButton button = new JButton("Filesystem ...");
      button.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
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
            }
          });
      return button;
    }

    // private JButton createClearButton() {
    // JButton button = new JButton("Clear");
    //
    // return button;
    // }

    private JButton createOKButton() {
      JButton button = new JButton("OK");
      button.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              setVisible(false);
            }
          });
      return button;
    }

    private JButton createCancelButton() {
      JButton button = new JButton("Cancel");
      button.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              selectedAsset = null;
              setVisible(false);
            }
          });
      return button;
    }

    public Asset chooseAsset() {
      setVisible(true);
      return selectedAsset;
    }

    private JComponent createImageExplorerPanel() {
      final AssetPanel assetPanel =
          new AssetPanel(
              "mapPropertiesImageExplorer",
              MapTool.getFrame().getAssetPanel().getModel(),
              JSplitPane.HORIZONTAL_SPLIT);
      assetPanel.addImageSelectionListener(
          new SelectionListener() {
            public void selectionPerformed(List<Object> selectedList) {
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
            }
          });
      return assetPanel;
    }
  }

  private class MapPreviewPanel extends JComponent {
    private static final long serialVersionUID = 3761329103161077644L;

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
        BufferedImage image = ImageManager.getImageAndWait(mapAsset.getId());
        Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());
        SwingUtil.constrainTo(imgSize, size.width - 10 * 4, size.height - 10 * 4);

        int x = (size.width - imgSize.width) / 2;
        int y = (size.height - imgSize.height) / 2;

        g.drawImage(image, x, y, imgSize.width, imgSize.height, this);
      }
    }
  }

  private final ImageObserver drawableObserver =
      new ImageObserver() {
        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
          MapPropertiesDialog.this.imagePreviewPanel.repaint();
          return true;
        }
      };
}
