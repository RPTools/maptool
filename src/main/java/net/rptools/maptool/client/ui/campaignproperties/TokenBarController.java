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

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.ui.PreviewPanelFileChooser;
import net.rptools.maptool.client.ui.campaignproperties.TokenStatesController.StateListRenderer;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay.Side;
import net.rptools.maptool.client.ui.token.DrawnBarTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoToneBarTokenOverlay;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.util.ImageManager;

/**
 * This controller will handle all of the components on the Bar panel of the {@link
 * CampaignPropertiesDialog}.
 *
 * @author Jay
 */
public class TokenBarController
    implements ActionListener,
        DocumentListener,
        ListSelectionListener,
        ItemListener,
        ChangeListener {

  /** Panel containing the campaign properties form panel */
  private final AbeillePanel formPanel;

  /** Names of the bars */
  private Set<String> names;

  /** Image file chooser used to support the browse buttons */
  private PreviewPanelFileChooser imageFileChooser;

  /** Renderer used to paint the bars */
  private final StateListRenderer renderer = new StateListRenderer();

  /** Name of the text field containing the name {@link String} value */
  public static final String NAME = "tokenBarName";

  /**
   * Name of the combo box containing a {@link String} value that maps directly to a {@link
   * BarTokenOverlay} class
   */
  public static final String TYPE = "tokenBarType";

  /**
   * Name of the color well containing a {@link Color} value for token bar types that need colors
   */
  public static final String COLOR = "tokenBarColor";

  /**
   * Name of the color well containing a {@link Color} value for token bar types that need
   * background colors
   */
  public static final String BG_COLOR = "tokenBarBgColor";

  /** Name of the check box containing the {@link Boolean} mouseover value */
  public static final String MOUSEOVER = "tokenBarMouseover";

  /**
   * Name of the spinner containing an {@link Integer} value that is the thickness of bar types that
   * need bar width
   */
  public static final String THICKNESS = "tokenBarThickness";

  /**
   * Name of the spinner containing an {@link Integer} value that is the thickness of bar types that
   * need bar width
   */
  public static final String INCREMENTS = "tokenBarIncrements";

  /**
   * Name of the combo box containing a {@link String} value for one of the four sides of the token
   */
  public static final String SIDE = "tokenBarSide";

  /** Name of the button used to add a new token bar */
  public static final String ADD = "tokenBarAddBar";

  /** Name of the button used to delete selected token bars */
  public static final String DELETE = "tokenBarDeleteBar";

  /** Name of the button used to edit an existing token bar. */
  public static final String UPDATE = "tokenBarUpdateBar";

  /** Name of the button used to move a bar up one space */
  public static final String MOVE_UP = "tokenBarMoveUp";

  /** Name of the button used to move a bar down one space */
  public static final String MOVE_DOWN = "tokenBarMoveDown";

  /** Name of the list containing all of the currently defined token bars */
  public static final String BARS = "tokenBarBars";

  /** Name of the spinner containing an {@link Integer} value that is the opacity used in drawing */
  public static final String OPACITY = "tokenBarOpacity";

  /**
   * Name of the list field containing {@link File} names that is the image file used for image
   * token bars
   */
  public static final String IMAGES = "tokenBarImages";

  /** Name of the button used to add a new token bar image */
  public static final String IMAGE_ADD = "tokenBarAddImage";

  /** Name of the button used to delete selected token bar images */
  public static final String IMAGE_DELETE = "tokenBarDeleteImage";

  /** Name of the button used to edit an existing token bar image. */
  public static final String IMAGE_UPDATE = "tokenBarUpdateImage";

  /** Name of the button used to move a bar image up one space */
  public static final String IMAGE_MOVE_UP = "tokenBarMoveUpImage";

  /** Name of the button used to move a bar image down one space */
  public static final String IMAGE_MOVE_DOWN = "tokenBarMoveDownImage";

  /** Name of the check box that shows the GM sees the bar */
  public static final String SHOW_GM = "tokenBarGM";

  /** Name of the check box that shows the GM sees the bar */
  public static final String SHOW_OWNER = "tokenBarOwner";

  /** Name of the check box that shows the GM sees the bar */
  public static final String SHOW_OTHERS = "tokenBarEverybody";

  /** Name of the slider used to test the bars */
  public static final String TESTER = "tokenBarTest";

  /** The size of the ICON faked in the list renderer */
  public static final int ICON_SIZE = 50;

  /** Each of the data entry components that can be enabled/disabled by type of bar */
  public static final String[] DATA_ENTRY_COMPONENTS = {
    COLOR,
    BG_COLOR,
    THICKNESS,
    IMAGES,
    IMAGE_ADD,
    IMAGE_DELETE,
    IMAGE_MOVE_DOWN,
    IMAGE_MOVE_UP,
    IMAGE_UPDATE
  };

  /** Each of the data entry components that can be enabled/disabled by type of bar */
  public static final String[] DATA_ENTRY_COMPONENT_LABELS = {
    COLOR + "Label", BG_COLOR + "Label", THICKNESS + "Label", IMAGES + "Label"
  };

  /**
   * Flags for each of the data entry components needed by each of the types. The order of the types
   * is the' same as the list in the combo box. The order of the flags is the same as that in {@link
   * #DATA_ENTRY_COMPONENTS}.
   */
  public static final boolean[][] NEEDED_COMPONENTS = {
    {false, false, false, true, true, true, true, true, true}, // Two Image
    {false, false, false, true, true, true, true, true, true}, // One Image
    {false, false, false, true, true, true, true, true, true}, // Multiple Image
    {true, false, true, false, false, false, false, false, false}, // Solid
    {true, true, true, false, false, false, false, false, false}, // Two Tone
  };

  /** The number of images needed by each type of bar. The increments are added to this */
  public static final String[][] NEEDED_IMAGES = {
    {"Base", "Bar"},
    {"Bar"},
    null, // Multiple images
    new String[0], // Solid
    new String[0], // Two Tone
  };

  enum BarType {
    TWO_IMAGE("CampaignPropertiesDialog.combo.bars.type.twoImages"),
    ONE_IMAGE("CampaignPropertiesDialog.combo.bars.type.singleImage"),
    MULTIPLE_IMAGE("CampaignPropertiesDialog.combo.bars.type.multipleImages"),
    SOLID("CampaignPropertiesDialog.combo.bars.type.solid"),
    TWO_TONE("CampaignPropertiesDialog.combo.bars.type.twoTone");

    private final String i18nKey;

    BarType(String i18nKey) {
      this.i18nKey = i18nKey;
    }

    @Override
    public String toString() {
      return I18N.getText(i18nKey);
    }
  }

  enum BarSide {
    TOP("CampaignPropertiesDialog.combo.bars.side.top", Side.TOP),
    BOTTOM("CampaignPropertiesDialog.combo.bars.type.bottom", Side.BOTTOM),
    LEFT("CampaignPropertiesDialog.combo.bars.type.left", Side.LEFT),
    RIGHT("CampaignPropertiesDialog.combo.bars.type.right", Side.RIGHT);

    private final String i18nKey;
    private final Side side;

    BarSide(String i18nKey, Side side) {
      this.i18nKey = i18nKey;
      this.side = side;
    }

    public Side getSide() {
      return side;
    }

    @Override
    public String toString() {
      return I18N.getText(i18nKey);
    }
  }

  /**
   * Set up the button listeners, spinner models, list cell renderer and selection listeners
   *
   * @param panel The {@link CampaignProperties} form panel
   */
  public TokenBarController(AbeillePanel panel) {
    formPanel = panel;
    panel.getButton(ADD).addActionListener(this);
    panel.getButton(DELETE).addActionListener(this);
    panel.getButton(UPDATE).addActionListener(this);
    panel.getButton(MOVE_UP).addActionListener(this);
    panel.getButton(MOVE_DOWN).addActionListener(this);
    panel.getButton(IMAGE_ADD).addActionListener(this);
    panel.getButton(IMAGE_DELETE).addActionListener(this);
    panel.getButton(IMAGE_UPDATE).addActionListener(this);
    panel.getButton(IMAGE_MOVE_UP).addActionListener(this);
    panel.getButton(IMAGE_MOVE_DOWN).addActionListener(this);

    var typeComboBox = panel.getComboBox(TYPE);
    typeComboBox.setModel(new DefaultComboBoxModel<BarType>());
    Arrays.stream(BarType.values()).forEach(typeComboBox::addItem);
    typeComboBox.addActionListener(this);

    var sideComboBox = panel.getComboBox(SIDE);
    sideComboBox.setModel(new DefaultComboBoxModel<BarSide>());
    Arrays.stream(BarSide.values()).forEach(sideComboBox::addItem);

    panel.getSpinner(THICKNESS).setModel(new SpinnerNumberModel(5, 2, 10, 1));
    panel.getSpinner(INCREMENTS).setModel(new SpinnerNumberModel(0, 0, 100, 1));
    panel.getSpinner(INCREMENTS).addChangeListener(this);
    panel.getSpinner(OPACITY).setModel(new SpinnerNumberModel(100, 1, 100, 5));
    panel.getList(BARS).setCellRenderer(renderer);
    panel.getList(BARS).addListSelectionListener(this);
    panel.getList(IMAGES).setModel(new DefaultListModel<MD5Key>());
    panel.getList(IMAGES).setCellRenderer(new ImageListRenderer());
    panel.getList(IMAGES).addListSelectionListener(this);
    panel.getTextComponent(NAME).getDocument().addDocumentListener(this);
    panel.getCheckBox(SHOW_GM).addItemListener(this);
    panel.getCheckBox(SHOW_OTHERS).addItemListener(this);
    panel.getCheckBox(SHOW_OWNER).addItemListener(this);
    ((JSlider) panel.getComponent(TESTER)).addChangeListener(this);
    ((JSlider) panel.getComponent(TESTER)).setValue(100);
    enableDataComponents();
    changedUpdate(null);
    // Initially no bar is selected, so these start disabled
    panel.getButton(MOVE_UP).setEnabled(false);
    panel.getButton(MOVE_DOWN).setEnabled(false);
    panel.getButton(DELETE).setEnabled(false);
  }

  /**
   * Get the set of names for states and bars;
   *
   * @return The state/bar namespace
   */
  public Set<String> getNames() {
    return names;
  }

  /**
   * Handle all of the buttons and bar combo box
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    String name = ((JComponent) e.getSource()).getName();
    JList<BarTokenOverlay> list = formPanel.getList(BARS);
    DefaultListModel<BarTokenOverlay> model = (DefaultListModel<BarTokenOverlay>) list.getModel();
    int selected = list.getSelectedIndex();
    JList<MD5Key> imageList = formPanel.getList(IMAGES);
    DefaultListModel<MD5Key> imageModel = (DefaultListModel<MD5Key>) imageList.getModel();
    int imageSelected = imageList.getSelectedIndex();

    // Add a new bar
    if (ADD.equals(name)) {
      BarTokenOverlay overlay = createTokenOverlay(null);
      if (overlay != null) {
        model.addElement(overlay);
        getNames().add(overlay.getName());
        formPanel.getTextComponent(NAME).setText("");
        formPanel.getCheckBox(MOUSEOVER).setSelected(false);
        formPanel.getSpinner(OPACITY).setValue(100);
        formPanel.getCheckBox(SHOW_GM).setSelected(true);
        formPanel.getCheckBox(SHOW_OWNER).setSelected(true);
        formPanel.getCheckBox(SHOW_OTHERS).setSelected(true);
        formPanel.getList(IMAGES).setModel(new DefaultListModel<>());
        formPanel.getList(BARS).clearSelection();
      } // endif

      // Delete selected bar
    } else if (DELETE.equals(name)) {
      int[] selectedElements = list.getSelectedIndices();
      for (int j = selectedElements.length - 1; j >= 0; j--) {
        BarTokenOverlay overlay = model.remove(selectedElements[j]);
        getNames().remove(overlay.getName());
      } // endfor
      changedUpdate(null);

      // Add an image to the list
    } else if (IMAGE_ADD.equals(name)) {
      if (getImageFileChooser().showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
        File imageFile = getImageFileChooser().getSelectedFile();
        if (imageFile == null
            || imageFile.isDirectory()
            || !imageFile.exists()
            || !imageFile.canRead()) return;
        if (imageSelected >= 0) {
          imageModel.insertElementAt(
              TokenStatesController.loadAsssetFile(imageFile.getAbsolutePath(), formPanel),
              imageSelected);
        } else {
          imageModel.addElement(
              TokenStatesController.loadAsssetFile(imageFile.getAbsolutePath(), formPanel));
          imageSelected = imageModel.size() - 1;
        } // endif
        ((JScrollPane) formPanel.getComponent("tokenBarImagesScroll"))
            .scrollRectToVisible(imageList.getCellBounds(imageSelected, imageSelected));
        AppPreferences.loadDirectory.set(imageFile.getParentFile());
        changedUpdate(null);
      } // endif

      // Update an image in the list
    } else if (IMAGE_UPDATE.equals(name)) {
      if (imageSelected < 0) {
        return; // We really should disable the UPDATE button unless an image is selected...
      }
      if (getImageFileChooser().showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
        File imageFile = getImageFileChooser().getSelectedFile();
        if (imageFile == null
            || imageFile.isDirectory()
            || !imageFile.exists()
            || !imageFile.canRead()) return;
        imageModel.set(
            imageSelected,
            TokenStatesController.loadAsssetFile(imageFile.getAbsolutePath(), formPanel));
        AppPreferences.loadDirectory.set(imageFile.getParentFile());
      } // endif

      // Delete an image in the list
    } else if (IMAGE_DELETE.equals(name)) {
      if (imageSelected < 0) {
        return; // We really should disable the DELETE button unless an image is selected...
      }
      imageModel.remove(imageSelected);
      changedUpdate(null);

      // Move an image up one row
    } else if (IMAGE_MOVE_UP.equals(name)) {
      if (imageSelected < 0) {
        return; // We really should disable the MOVE_UP button unless an image is selected...
      }
      MD5Key element = imageModel.remove(imageSelected);
      imageModel.add(imageSelected - 1, element);
      imageList.setSelectedIndex(imageSelected - 1);
      imageList.scrollRectToVisible(imageList.getCellBounds(imageSelected - 1, imageSelected - 1));

      // Move an image down one row
    } else if (IMAGE_MOVE_DOWN.equals(name)) {
      if (imageSelected < 0) {
        return; // We really should disable the MOVE_DOWN button unless an image is selected...
      }
      MD5Key element = imageModel.remove(imageSelected);
      imageModel.add(imageSelected + 1, element);
      imageList.setSelectedIndex(imageSelected + 1);
      imageList.scrollRectToVisible(imageList.getCellBounds(imageSelected + 1, imageSelected + 1));

      // Change the enabled data components.
    } else if (TYPE.equals(name)) {
      enableDataComponents();

      // Update the selected overlay
    } else if (UPDATE.equals(name)) {
      BarTokenOverlay selectedOverlay =
          (BarTokenOverlay) formPanel.getList(BARS).getSelectedValue();
      BarTokenOverlay overlay = createTokenOverlay(selectedOverlay);
      if (overlay != null) model.set(selected, overlay);

      // Move an item up one row
    } else if (MOVE_UP.equals(name)) {
      BarTokenOverlay element = model.remove(selected);
      model.add(selected - 1, element);
      list.setSelectedIndex(selected - 1);
      list.scrollRectToVisible(list.getCellBounds(selected - 1, selected - 1));

      // Move an item down one row
    } else if (MOVE_DOWN.equals(name)) {
      BarTokenOverlay element = model.remove(selected);
      model.add(selected + 1, element);
      list.setSelectedIndex(selected + 1);
      list.scrollRectToVisible(list.getCellBounds(selected + 1, selected + 1));
    } // endif
  }

  /** Enable the data components needed by the selected type of overlay. Disable the rest. */
  private void enableDataComponents() {
    int selected = formPanel.getComboBox(TYPE).getSelectedIndex();
    int selectedImg = formPanel.getList(IMAGES).getSelectedIndex();
    int size = formPanel.getList(IMAGES).getModel().getSize();

    for (int i = 0; i < DATA_ENTRY_COMPONENTS.length; i++) {
      String name = DATA_ENTRY_COMPONENTS[i];
      boolean enabled = NEEDED_COMPONENTS[selected][i];
      if (enabled) {
        // These buttons can be disabled depending on which image is selected
        if (name.equals(IMAGE_DELETE) || name.equals(IMAGE_UPDATE)) {
          enabled = selectedImg >= 0;
        } else if (name.equals(IMAGE_MOVE_UP)) {
          enabled = selectedImg >= 1;
        } else if (name.equals(IMAGE_MOVE_DOWN)) {
          enabled = selectedImg >= 0 && selectedImg <= size - 2;
        }
      }

      formPanel.getComponent(DATA_ENTRY_COMPONENTS[i]).setEnabled(enabled);
      if (i < DATA_ENTRY_COMPONENT_LABELS.length) {
        formPanel.getComponent(DATA_ENTRY_COMPONENT_LABELS[i]).setEnabled(enabled);
      }
    } // endfor
    changedUpdate(null);
  }

  /**
   * Get the file chooser which allows an image to be selected.
   *
   * @return The image file chooser.
   */
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
    }
    return imageFileChooser;
  }

  /**
   * Enable/disable the ADD and UPDATE buttons as needed.
   *
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void changedUpdate(DocumentEvent e) {
    int type = formPanel.getComboBox(TYPE).getSelectedIndex();
    int size = NEEDED_IMAGES[type] == null ? -1 : NEEDED_IMAGES[type].length;
    int imageCount = formPanel.getList(IMAGES).getModel().getSize();
    int increments = TokenStatesController.getSpinner(INCREMENTS, "increments", formPanel);
    String text = formPanel.getTextComponent(NAME).getText();
    boolean hasName = text != null && (text = text.trim()).length() != 0;
    boolean hasShow =
        formPanel.getCheckBox(SHOW_GM).isSelected()
            || formPanel.getCheckBox(SHOW_OWNER).isSelected()
            || formPanel.getCheckBox(SHOW_OTHERS).isSelected();
    boolean hasImages = false;
    BarTokenOverlay selectedBar = (BarTokenOverlay) formPanel.getList(BARS).getSelectedValue();
    boolean hasUniqueUpdateName = false;
    if (selectedBar != null)
      hasUniqueUpdateName = selectedBar.getName().equals(text) || !getNames().contains(text);
    if (size > 0 && imageCount == size) {
      hasImages = true;
    } else if (size < 0 && imageCount == increments && increments > 0) {
      hasImages = true;
    } else if (size == 0) {
      hasImages = true;
    } // endif
    formPanel
        .getButton(ADD)
        .setEnabled(hasName && !getNames().contains(text) && hasImages && hasShow);
    formPanel
        .getButton(UPDATE)
        .setEnabled(hasName && hasUniqueUpdateName && selectedBar != null && hasShow && hasImages);
  }

  /**
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent e) {
    changedUpdate(e);
  }

  /**
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent e) {
    changedUpdate(e);
  }

  /**
   * Handle a change in the selected list item.
   *
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    if (e.getSource() == formPanel.getList(BARS)) {
      int selected = formPanel.getList(BARS).getSelectedIndex();
      int size = formPanel.getList(BARS).getModel().getSize();
      formPanel.getButton(DELETE).setEnabled(selected >= 0);
      changedUpdate(null); // Makes sure update is selected
      formPanel.getButton(MOVE_UP).setEnabled(selected >= 1);
      formPanel.getButton(MOVE_DOWN).setEnabled(selected >= 0 && selected <= size - 2);
      if (selected >= 0) {
        // Set common stuff
        BarTokenOverlay bar = (BarTokenOverlay) formPanel.getList(BARS).getSelectedValue();
        formPanel.getTextComponent(NAME).setText(bar.getName());
        formPanel.getCheckBox(MOUSEOVER).setSelected(bar.isMouseover());
        formPanel.getSpinner(OPACITY).setValue(bar.getOpacity());
        formPanel.getCheckBox(SHOW_GM).setSelected(bar.isShowGM());
        formPanel.getCheckBox(SHOW_OWNER).setSelected(bar.isShowOwner());
        formPanel.getCheckBox(SHOW_OTHERS).setSelected(bar.isShowOthers());
        formPanel.getSpinner(INCREMENTS).setValue(bar.getIncrements());
        formPanel.getComboBox(SIDE).setSelectedIndex(bar.getSide().ordinal());

        // Handle the drawn overlays
        int type = -1;
        if (bar instanceof DrawnBarTokenOverlay) {
          formPanel.getSpinner(THICKNESS).setValue(((DrawnBarTokenOverlay) bar).getThickness());
          ((ColorWell) formPanel.getComponent(COLOR))
              .setColor(((DrawnBarTokenOverlay) bar).getBarColor());
          type = 3;
        } // endif
        if (bar instanceof TwoToneBarTokenOverlay) {
          ((ColorWell) formPanel.getComponent(BG_COLOR))
              .setColor(((TwoToneBarTokenOverlay) bar).getBgColor());
          type = 4;
        } // endif

        // Handle images
        MD5Key[] assetIds = null;
        if (bar instanceof TwoImageBarTokenOverlay) {
          assetIds =
              new MD5Key[] {
                ((TwoImageBarTokenOverlay) bar).getBottomAssetId(),
                ((TwoImageBarTokenOverlay) bar).getTopAssetId()
              };
          type = 0;
        } else if (bar instanceof SingleImageBarTokenOverlay) {
          assetIds = new MD5Key[] {((SingleImageBarTokenOverlay) bar).getAssetId()};
          type = 1;
        } else if (bar instanceof MultipleImageBarTokenOverlay) {
          assetIds = ((MultipleImageBarTokenOverlay) bar).getAssetIds();
          type = 2;
        }
        DefaultListModel<MD5Key> model = new DefaultListModel<>();
        if (assetIds != null) for (MD5Key key : assetIds) model.addElement(key);
        formPanel.getList(IMAGES).setModel(model);
        formPanel.getList(IMAGES).repaint();

        // Set the type and change components
        formPanel.getComboBox(TYPE).setSelectedIndex(type);
        enableDataComponents();
      }
    } else {
      int selected = formPanel.getList(IMAGES).getSelectedIndex();
      int size = formPanel.getList(IMAGES).getModel().getSize();
      formPanel.getButton(IMAGE_DELETE).setEnabled(selected >= 0);
      formPanel.getButton(IMAGE_UPDATE).setEnabled(selected >= 0);
      formPanel.getButton(IMAGE_MOVE_UP).setEnabled(selected >= 1);
      formPanel.getButton(IMAGE_MOVE_DOWN).setEnabled(selected >= 0 && selected <= size - 2);
    } // endif
  }

  /**
   * The {@link ListCellRenderer} that draws the images selected by the user
   *
   * @author Jay
   */
  private class ImageListRenderer extends DefaultListCellRenderer {

    /** Bounds of the image on the line */
    Rectangle bounds = new Rectangle(0, 0, ICON_SIZE, ICON_SIZE);

    /** Image being rendered. */
    MD5Key key;

    /** Cached images */
    private transient Map<MD5Key, BufferedImage> imageCache = new HashMap<>();

    /** Create an icon from the token bar. */
    Icon icon =
        new Icon() {
          public int getIconHeight() {
            return ICON_SIZE;
          }

          public int getIconWidth() {
            return ICON_SIZE;
          }

          public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            Shape old = g.getClip();
            g.setClip(bounds.intersection(old.getBounds()));
            BufferedImage image = ImageManager.getImageAndWait(key);
            g.drawImage(image, x, y, bounds.width, bounds.height, null);
            g.setClip(old);
          }
        };

    /**
     * Set the icon and name in the renderer.
     *
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *     java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      key = (MD5Key) value;
      int type = formPanel.getComboBox(TYPE).getSelectedIndex();
      if (NEEDED_IMAGES[type] == null) {
        int increments = TokenStatesController.getSpinner(INCREMENTS, "increments", formPanel);
        if (increments >= index) {
          if (index == 0) {
            setText("Empty Bar");
          } else if (index == increments - 1) {
            setText("Full Bar");
          } else if (index < increments) {
            setText("Increment " + index);
          } else {
            setText("Unneeded");
          } // endif
        } else {
          setText("Unneeded");
        } // endif
      } else if (NEEDED_IMAGES[type].length > index) {
        setText(NEEDED_IMAGES[type][index]);
      } else {
        setText("Unneeded");
      } // endif
      setIcon(icon);
      return this;
    }
  }

  /**
   * Copy the token states from the campaign properties to the state tab.
   *
   * @param campaign Place the states in these properties in the form panel
   */
  public void copyCampaignToUI(CampaignProperties campaign) {
    DefaultListModel<BarTokenOverlay> model = new DefaultListModel<>();
    List<BarTokenOverlay> overlays = new ArrayList<>(campaign.getTokenBarsMap().values());
    overlays.sort(BarTokenOverlay.COMPARATOR);
    for (BarTokenOverlay overlay : overlays) {
      model.addElement(overlay);
      getNames().add(overlay.getName());
    }
    formPanel.getList(BARS).setModel(model);
  }

  /**
   * Copy the token states from the state tab and place it in the passed campaign.
   *
   * @param campaign Campaign containing the properties being updated
   */
  public void copyUIToCampaign(Campaign campaign) {
    ListModel<BarTokenOverlay> model = formPanel.getList(BARS).getModel();
    Map<String, BarTokenOverlay> states = new LinkedHashMap<>();
    for (int i = 0; i < model.getSize(); i++) {
      BarTokenOverlay overlay = model.getElementAt(i);
      overlay.setOrder(i);
      states.put(overlay.getName(), overlay);
    }
    campaign.getTokenBarsMap().clear();
    campaign.getTokenBarsMap().putAll(states);
  }

  /**
   * Create a token state from the user's input
   *
   * @param updatedOverlay Overlay being modified
   * @return The new token state.
   */
  public BarTokenOverlay createTokenOverlay(BarTokenOverlay updatedOverlay) {

    // Need the color and name for everything
    Color color = ((ColorWell) formPanel.getComponent(COLOR)).getColor();
    Color bgColor = ((ColorWell) formPanel.getComponent(BG_COLOR)).getColor();
    String name = formPanel.getTextComponent(NAME).getText();
    boolean mouseover = formPanel.getCheckBox(MOUSEOVER).isSelected();
    var overlay = ((BarType) formPanel.getComboBox(TYPE).getSelectedItem());
    int opacity = TokenStatesController.getSpinner(OPACITY, "opacity", formPanel);
    boolean showGM = formPanel.getCheckBox(SHOW_GM).isSelected();
    boolean showOwner = formPanel.getCheckBox(SHOW_OWNER).isSelected();
    boolean showOthers = formPanel.getCheckBox(SHOW_OTHERS).isSelected();
    int thickness = TokenStatesController.getSpinner(THICKNESS, "thickness", formPanel);
    int increments = TokenStatesController.getSpinner(INCREMENTS, "increments", formPanel);
    var side = (BarSide) formPanel.getComboBox(SIDE).getSelectedItem();

    if (overlay == null || side == null) {
      return null;
    }

    BarTokenOverlay to = null;
    if (overlay.equals(BarType.SOLID)) {
      to = new DrawnBarTokenOverlay(name, color, thickness);
    } else if (overlay.equals(BarType.TWO_TONE)) {
      to = new TwoToneBarTokenOverlay(name, color, bgColor, thickness);
    } else {

      // Get all of the assets
      DefaultListModel<MD5Key> model =
          (DefaultListModel<MD5Key>) formPanel.getList(IMAGES).getModel();
      MD5Key[] assetIds = new MD5Key[model.getSize()];
      model.copyInto(assetIds);

      // Create the bars
      if (overlay.equals(BarType.TWO_IMAGE)) {
        to = new TwoImageBarTokenOverlay(name, assetIds[1], assetIds[0]);
      } else if (overlay.equals(BarType.ONE_IMAGE)) {
        to = new SingleImageBarTokenOverlay(name, assetIds[0]);
      } else if (overlay.equals(BarType.MULTIPLE_IMAGE)) {
        to = new MultipleImageBarTokenOverlay(name, assetIds);
      } // endif
    } // endif

    // Set the common token stuff
    if (to != null) {
      to.setMouseover(mouseover);
      to.setOpacity(opacity);
      to.setShowGM(showGM);
      to.setShowOthers(showOthers);
      to.setShowOwner(showOwner);
      to.setIncrements(increments);
      to.setSide(side.getSide());
    } // endif
    return to;
  }

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  public void itemStateChanged(ItemEvent e) {
    changedUpdate(null);
  }

  /**
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == formPanel.getComponent(TESTER)) {
      renderer.value = ((JSlider) formPanel.getComponent(TESTER)).getValue() / 100.0;
      formPanel.getList(BARS).repaint();
    } else {
      changedUpdate(null);
    }
  }

  /**
   * @param names Setter for names
   */
  public void setNames(Set<String> names) {
    this.names = names;
  }
}
