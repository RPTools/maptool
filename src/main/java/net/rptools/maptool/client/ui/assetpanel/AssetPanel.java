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
package net.rptools.maptool.client.ui.assetpanel;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalSliderUI;
import net.rptools.lib.swing.ImagePanel;
import net.rptools.lib.swing.ImagePanel.SelectionMode;
import net.rptools.lib.swing.SelectionListener;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.swing.preference.SplitPanePreferences;
import net.rptools.lib.swing.preference.TreePreferences;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;

public class AssetPanel extends JComponent {
  private static final ImageIcon FILTER_IMAGE =
      new ImageIcon(
          AssetPanel.class
              .getClassLoader()
              .getResource("net/rptools/maptool/client/image/zoom.png"));

  private final AssetTree assetTree;
  private ImagePanel imagePanel;
  private JTextField filterTextField;
  private JCheckBox globalSearchField;
  private JSlider thumbnailPreviewSlider;
  private final AssetPanelModel assetPanelModel;
  private Timer updateFilterTimer;
  private JProgressBar imagePanelProgressBar;

  public AssetPanel(String controlName) {
    this(controlName, new AssetPanelModel());
  }

  public AssetPanel(String controlName, AssetPanelModel model) {
    this(controlName, model, JSplitPane.VERTICAL_SPLIT);
  }

  public AssetPanel(String controlName, AssetPanelModel model, int splitPaneDirection) {
    assetPanelModel = model;
    model.addImageUpdateObserver(this);

    assetTree = new AssetTree(this);
    createImagePanel();

    JSplitPane splitPane = new JSplitPane(splitPaneDirection);
    splitPane.setContinuousLayout(true);

    splitPane.setTopComponent(new JScrollPane(assetTree));
    splitPane.setBottomComponent(createSouthPanel());
    splitPane.setDividerLocation(100);

    new SplitPanePreferences(AppConstants.APP_NAME, controlName, splitPane);
    new TreePreferences(AppConstants.APP_NAME, controlName, assetTree);

    setLayout(new GridLayout());
    add(splitPane);
  }

  private void createImagePanel() {
    imagePanel = new ImagePanel();
    /*
     * {
     *
     * @Override public void dragGestureRecognized(DragGestureEvent dge) { super.dragGestureRecognized(dge);
     *
     * MapTool.getFrame().getDragImageGlassPane().setImage(ImageManager. getImageAndWait( assetBeingTransferred)); }
     *
     * @Override public void dragMouseMoved(DragSourceDragEvent dsde) { super.dragMouseMoved(dsde);
     *
     * Point p = new Point(dsde.getLocation()); SwingUtilities.convertPointFromScreen(p, MapTool.getFrame().getDragImageGlassPane());
     *
     * MapTool.getFrame().getDragImageGlassPane().setImagePosition(p); }
     *
     * @Override public void dragDropEnd(DragSourceDropEvent dsde) { super.dragDropEnd(dsde);
     *
     * MapTool.getFrame().getDragImageGlassPane().setImage(null); }
     *
     * @Override protected Cursor getDragCursor() { return Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1, 1, Transparency.BITMASK), new Point (0,0), ""); } };
     */
    imagePanel.setShowCaptions(true);
    imagePanel.setSelectionMode(SelectionMode.SINGLE);
    imagePanel.setFont(new Font("Helvetica", 0, 10)); // XXX Overrides TinyLAF?

    imagePanel.addMouseWheelListener(
        new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(MouseWheelEvent e) {
            if (SwingUtil.isControlDown(e) || e.isMetaDown()) { // XXX Why either one?
              e.consume();
              int steps = e.getWheelRotation();
              imagePanel.setGridSize(imagePanel.getGridSize() + steps);
              thumbnailPreviewSlider.setValue(imagePanel.getGridSize());
            } else {
              imagePanel.getParent().dispatchEvent(e);
            }
          }
        });
  }

  public void setThumbSize(int size) {
    imagePanel.setGridSize(size);
  }

  public void setImagePanelProgress(int progress) {
    imagePanelProgressBar.setValue(progress);
  }

  public void setImagePanelProgressMax(int max) {
    imagePanelProgressBar.setMaximum(max);
  }

  public void showImagePanelProgress(boolean isVisble) {
    imagePanelProgressBar.setVisible(isVisble);
  }

  private JPanel createSouthPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    imagePanelProgressBar = new JProgressBar(0, 100);
    imagePanelProgressBar.setValue(0);
    imagePanelProgressBar.setStringPainted(true);
    imagePanelProgressBar.setIndeterminate(false);
    imagePanelProgressBar.setVisible(false);

    panel.add(BorderLayout.NORTH, createFilterPanel());
    panel.add(BorderLayout.NORTH, createFilterPanel());
    panel.add(BorderLayout.WEST, getThumbnailPreviewSlider());
    panel.add(BorderLayout.CENTER, new JScrollPane(imagePanel));
    panel.add(BorderLayout.SOUTH, imagePanelProgressBar);

    return panel;
  }

  /**
   * Creates the GUI for the bottom half of the splitpane that allows for finding assets within any
   * of the repository locations (such as local directories).
   *
   * @return
   */
  private JPanel createFilterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

    JPanel top = new JPanel(new BorderLayout());
    top.add(BorderLayout.WEST, new JLabel("", FILTER_IMAGE, JLabel.LEFT));
    top.add(BorderLayout.CENTER, getFilterTextField());

    panel.add(BorderLayout.NORTH, top);
    panel.add(BorderLayout.CENTER, getGlobalSearchField());

    return panel;
  }

  public void addImageSelectionListener(SelectionListener listener) {
    imagePanel.addSelectionListener(listener);
  }

  public void removeImageSelectionListener(SelectionListener listener) {
    imagePanel.removeSelectionListener(listener);
  }

  public List<Object> getSelectedIds() {
    return imagePanel.getSelectedIds();
  }

  public void showImagePanelPopup(JPopupMenu menu, int x, int y) {
    menu.show(imagePanel, x, y);
  }

  public JTextField getFilterTextField() {
    if (filterTextField == null) {
      filterTextField = new JTextField();
      filterTextField
          .getDocument()
          .addDocumentListener(
              new DocumentListener() {
                @Override
                public void changedUpdate(DocumentEvent e) {
                  // no op
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                  updateFilter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                  updateFilter();
                }
              });
    }
    return filterTextField;
  }

  /**
   * Returns a checkbox that indicates whether the filter field applies to <i>all</i> images in all
   * libraries or just the currently selected image directory.
   *
   * @return the checkbox component
   */
  private JCheckBox getGlobalSearchField() {
    if (globalSearchField == null) {
      globalSearchField =
          new JCheckBox(I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir1"), false);
      globalSearchField.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ev) {
              updateFilter();
            }
          });
    }
    return globalSearchField;
  }

  public void updateGlobalSearchLabel(int listSize) {
    if (getGlobalSearchField().isSelected()) {
      globalSearchField.setText(
          I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir1")
              + " ("
              + listSize
              + "/"
              + AppConstants.ASSET_SEARCH_LIMIT
              + " "
              + I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir2")
              + ")");
    } else {
      globalSearchField.setText(I18N.getText("panel.Asset.ImageModel.checkbox.searchSubDir1"));
    }

    imagePanel.revalidate();
    imagePanel.repaint();
  }

  /**
   * Returns a slider that controls the thumbnail size of image previews
   *
   * @return the JSlider component
   */
  private JSlider getThumbnailPreviewSlider() {
    if (thumbnailPreviewSlider == null) {
      thumbnailPreviewSlider = new JSlider(25, 500, imagePanel.getGridSize());
      thumbnailPreviewSlider.setInverted(true);
      thumbnailPreviewSlider.setName("Icon Size");
      thumbnailPreviewSlider.setMinorTickSpacing(25);
      thumbnailPreviewSlider.setMajorTickSpacing(100);
      thumbnailPreviewSlider.setOrientation(SwingConstants.VERTICAL);
      thumbnailPreviewSlider.setToolTipText(
          I18N.getString("panel.Asset.ImageModel.slider.toolTip"));

      thumbnailPreviewSlider.setUI(
          new MetalSliderUI() {
            @Override
            protected void scrollDueToClickInTrack(int direction) {
              int value = thumbnailPreviewSlider.getValue();

              if (thumbnailPreviewSlider.getOrientation() == JSlider.HORIZONTAL) {
                value = this.valueForXPosition(thumbnailPreviewSlider.getMousePosition().x);
              } else if (slider.getOrientation() == JSlider.VERTICAL) {
                value = this.valueForYPosition(thumbnailPreviewSlider.getMousePosition().y);
              }
              thumbnailPreviewSlider.setValue(value);
            }
          });

      thumbnailPreviewSlider.addChangeListener(
          new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
              setThumbSize(thumbnailPreviewSlider.getValue());
            }
          });
    }

    return thumbnailPreviewSlider;
  }

  public void rescanImagePanelDir(Directory dir) {
    ((ImageFileImagePanelModel) imagePanel.getModel()).rescan(dir);
    updateImagePanel();
  }

  public void updateImagePanel() {
    imagePanel.revalidate();
    imagePanel.repaint();
  }

  private synchronized void updateFilter() {
    if (updateFilterTimer == null) {
      updateFilterTimer =
          new Timer(
              500,
              new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                  ImageFileImagePanelModel model = (ImageFileImagePanelModel) imagePanel.getModel();
                  if (model == null) {
                    return;
                  }
                  model.setGlobalSearch(getGlobalSearchField().isSelected());
                  model.setFilter(getFilterTextField().getText());
                  // TODO: This should be event based
                  imagePanel.revalidate();
                  imagePanel.repaint();

                  updateFilterTimer.stop();
                  updateFilterTimer = null;
                }
              });
      updateFilterTimer.start();
    } else {
      updateFilterTimer.restart();
    }
  }

  // TODO: Find a way around this, it's ugly
  public Asset getAsset(int index) {
    return ((ImageFileImagePanelModel) imagePanel.getModel()).getAsset(index);
  }

  public void addImagePanelMouseListener(MouseListener listener) {
    imagePanel.addMouseListener(listener);
  }

  public void removeImagePanelMouseListener(MouseListener listener) {
    imagePanel.removeMouseListener(listener);
  }

  public AssetPanelModel getModel() {
    return assetPanelModel;
  }

  public boolean isAssetRoot(Directory dir) {
    return ((ImageFileTreeModel) assetTree.getModel()).isRootGroup(dir);
  }

  public void removeAssetRoot(Directory dir) {
    assetPanelModel.removeRootGroup(dir);
  }

  public Directory getSelectedAssetRoot() {
    return assetTree.getSelectedAssetGroup();
  }

  public void addAssetRoot(Directory dir) {
    assetPanelModel.addRootGroup(dir);
  }

  public void setDirectory(Directory dir) {
    imagePanel.setModel(
        new ImageFileImagePanelModel(dir) {
          @Override
          public Transferable getTransferable(int index) {
            // TransferableAsset t = (TransferableAsset) super.getTransferable(index);
            // assetBeingTransferred = t.getAsset();
            // return t;
            return super.getTransferable(index);
          }
        });
    updateFilter();
  }

  public AssetTree getAssetTree() {
    return assetTree;
  }
}
