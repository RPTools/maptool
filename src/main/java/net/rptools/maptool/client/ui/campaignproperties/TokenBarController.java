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

import com.jeta.forms.components.colors.JETAColorWell;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.store.properties.ListItemProperty;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
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
import net.rptools.maptool.client.ui.PreviewPanelFileChooser;
import net.rptools.maptool.client.ui.campaignproperties.TokenStatesController.StateListRenderer;
import net.rptools.maptool.client.ui.token.BarTokenOverlay;
import net.rptools.maptool.client.ui.token.BarTokenOverlay.Side;
import net.rptools.maptool.client.ui.token.DrawnBarTokenOverlay;
import net.rptools.maptool.client.ui.token.MultipleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.SingleImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoImageBarTokenOverlay;
import net.rptools.maptool.client.ui.token.TwoToneBarTokenOverlay;
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
  private final FormPanel formPanel;

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

  /**
   * Set up the button listeners, spinner models, list cell renderer and selection listeners
   *
   * @param panel The {@link CampaignProperties} form panel
   */
  public TokenBarController(FormPanel panel) {
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
    panel.getComboBox(TYPE).addActionListener(this);
    panel.getSpinner(THICKNESS).setModel(new SpinnerNumberModel(5, 2, 10, 1));
    panel.getSpinner(INCREMENTS).setModel(new SpinnerNumberModel(0, 0, 100, 1));
    panel.getSpinner(INCREMENTS).addChangeListener(this);
    panel.getSpinner(OPACITY).setModel(new SpinnerNumberModel(100, 1, 100, 5));
    panel.getList(BARS).setCellRenderer(renderer);
    panel.getList(BARS).addListSelectionListener(this);
    panel.getList(IMAGES).setCellRenderer(new ImageListRenderer());
    panel.getList(IMAGES).addListSelectionListener(this);
    panel.getTextComponent(NAME).getDocument().addDocumentListener(this);
    panel.getCheckBox(SHOW_GM).addItemListener(this);
    panel.getCheckBox(SHOW_OTHERS).addItemListener(this);
    panel.getCheckBox(SHOW_OWNER).addItemListener(this);
    ((JSlider) panel.getComponentByName(TESTER)).addChangeListener(this);
    ((JSlider) panel.getComponentByName(TESTER)).setValue(100);
    enableDataComponents();
    changedUpdate(null);
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
   * Handle all of the buttons & bar combo box
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    String name = ((JComponent) e.getSource()).getName();
    JList list = formPanel.getList(BARS);
    DefaultListModel model = (DefaultListModel) list.getModel();
    int selected = list.getSelectedIndex();
    JList imageList = formPanel.getList(IMAGES);
    DefaultListModel imageModel = (DefaultListModel) imageList.getModel();
    int imageSelected = imageList.getSelectedIndex();

    // Add a new bar
    if (ADD.equals(name)) {
      BarTokenOverlay overlay = createTokenOverlay(null);
      if (overlay != null) {
        model.addElement(overlay);
        getNames().add(overlay.getName());
        formPanel.setText(NAME, "");
        formPanel.setSelected(MOUSEOVER, false);
        formPanel.getSpinner(OPACITY).setValue(new Integer(100));
        formPanel.setSelected(SHOW_GM, true);
        formPanel.setSelected(SHOW_OWNER, true);
        formPanel.setSelected(SHOW_OTHERS, true);
        formPanel.getList(IMAGES).setModel(new DefaultListModel());
        formPanel.getList(BARS).clearSelection();
      } // endif

      // Delete selected bar
    } else if (DELETE.equals(name)) {
      int[] selectedElements = list.getSelectedIndices();
      for (int j = selectedElements.length - 1; j >= 0; j--) {
        BarTokenOverlay overlay = (BarTokenOverlay) model.remove(selectedElements[j]);
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
        ((JScrollPane) formPanel.getComponentByName("tokenBarImagesScroll"))
            .scrollRectToVisible(imageList.getCellBounds(imageSelected, imageSelected));
        AppPreferences.setLoadDir(imageFile.getParentFile());
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
        AppPreferences.setLoadDir(imageFile.getParentFile());
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
      Object element = imageModel.remove(imageSelected);
      imageModel.add(imageSelected - 1, element);
      imageList.setSelectedIndex(imageSelected - 1);
      imageList.scrollRectToVisible(imageList.getCellBounds(imageSelected - 1, imageSelected - 1));

      // Move an image down one row
    } else if (IMAGE_MOVE_DOWN.equals(name)) {
      if (imageSelected < 0) {
        return; // We really should disable the MOVE_DOWN button unless an image is selected...
      }
      Object element = imageModel.remove(imageSelected);
      imageModel.add(imageSelected + 1, element);
      imageList.setSelectedIndex(imageSelected + 1);
      imageList.scrollRectToVisible(imageList.getCellBounds(imageSelected + 1, imageSelected + 1));

      // Change the enabled data components.
    } else if (TYPE.equals(name)) {
      enableDataComponents();

      // Update the selected overlay
    } else if (UPDATE.equals(name)) {
      BarTokenOverlay selectedOverlay = (BarTokenOverlay) formPanel.getSelectedItem(BARS);
      BarTokenOverlay overlay = createTokenOverlay(selectedOverlay);
      if (overlay != null) model.set(selected, overlay);

      // Move an item up one row
    } else if (MOVE_UP.equals(name)) {
      Object element = model.remove(selected);
      model.add(selected - 1, element);
      list.setSelectedIndex(selected - 1);
      list.scrollRectToVisible(list.getCellBounds(selected - 1, selected - 1));

      // Move an item down one row
    } else if (MOVE_DOWN.equals(name)) {
      Object element = model.remove(selected);
      model.add(selected + 1, element);
      list.setSelectedIndex(selected + 1);
      list.scrollRectToVisible(list.getCellBounds(selected + 1, selected + 1));
    } // endif
  }

  /** Enable the data components needed by the selected type of overlay. Disable the rest. */
  private void enableDataComponents() {
    int selected = formPanel.getComboBox(TYPE).getSelectedIndex();
    for (int i = 0; i < DATA_ENTRY_COMPONENTS.length; i++) {
      formPanel
          .getComponentByName(DATA_ENTRY_COMPONENTS[i])
          .setEnabled(NEEDED_COMPONENTS[selected][i]);
      if (i < DATA_ENTRY_COMPONENT_LABELS.length)
        formPanel
            .getComponentByName(DATA_ENTRY_COMPONENT_LABELS[i])
            .setEnabled(NEEDED_COMPONENTS[selected][i]);
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
   * Enable/disable the buttons as needed.
   *
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate(DocumentEvent e) {
    int type = formPanel.getComboBox(TYPE).getSelectedIndex();
    int size = NEEDED_IMAGES[type] == null ? -1 : NEEDED_IMAGES[type].length;
    int imageCount = formPanel.getList(IMAGES).getModel().getSize();
    int increments = TokenStatesController.getSpinner(INCREMENTS, "increments", formPanel);
    String text = formPanel.getText(NAME);
    boolean hasName = text != null && (text = text.trim()).length() != 0;
    boolean hasShow =
        formPanel.isSelected(SHOW_GM)
            || formPanel.isSelected(SHOW_OWNER)
            || formPanel.isSelected(SHOW_OTHERS);
    boolean hasImages = false;
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
        .setEnabled(hasName && formPanel.getSelectedItem(BARS) != null && hasShow && hasImages);
  }

  /** @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent) */
  public void insertUpdate(DocumentEvent e) {
    changedUpdate(e);
  }

  /** @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent) */
  public void removeUpdate(DocumentEvent e) {
    changedUpdate(e);
  }

  /**
   * Handle a change in the selected list item.
   *
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) return;
    if (e.getSource() == formPanel.getList(BARS)) {
      int selected = formPanel.getList(BARS).getSelectedIndex();
      formPanel.getButton(DELETE).setEnabled(selected >= 0);
      changedUpdate(null); // Makes sure update is selected
      formPanel.getButton(MOVE_UP).setEnabled(selected >= 1);
      formPanel
          .getButton(MOVE_DOWN)
          .setEnabled(selected <= formPanel.getList(BARS).getModel().getSize() - 2);
      if (selected >= 0) {

        // Set common stuff
        BarTokenOverlay bar = (BarTokenOverlay) formPanel.getList(BARS).getSelectedValue();
        formPanel.setText(NAME, bar.getName());
        formPanel.setSelected(MOUSEOVER, bar.isMouseover());
        formPanel.getSpinner(OPACITY).setValue(new Integer(bar.getOpacity()));
        formPanel.setSelected(SHOW_GM, bar.isShowGM());
        formPanel.setSelected(SHOW_OWNER, bar.isShowOwner());
        formPanel.setSelected(SHOW_OTHERS, bar.isShowOthers());
        formPanel.getSpinner(INCREMENTS).setValue(Integer.valueOf(bar.getIncrements()));
        formPanel.getComboBox(SIDE).setSelectedIndex(bar.getSide().ordinal());

        // Handle the drawn overlays
        int type = -1;
        if (bar instanceof DrawnBarTokenOverlay) {
          formPanel
              .getSpinner(THICKNESS)
              .setValue(Integer.valueOf(((DrawnBarTokenOverlay) bar).getThickness()));
          ((JETAColorWell) formPanel.getComponentByName(COLOR))
              .setColor(((DrawnBarTokenOverlay) bar).getBarColor());
          type = 3;
        } // endif
        if (bar instanceof TwoToneBarTokenOverlay) {
          ((JETAColorWell) formPanel.getComponentByName(BG_COLOR))
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
        DefaultListModel model = new DefaultListModel();
        if (assetIds != null) for (MD5Key key : assetIds) model.addElement(key);
        formPanel.getList(IMAGES).setModel(model);
        formPanel.getList(IMAGES).repaint();

        // Set the type and change components
        formPanel.getComboBox(TYPE).setSelectedIndex(type);
        enableDataComponents();
      }
    } else {
      int selected = formPanel.getList(IMAGES).getSelectedIndex();
      formPanel.getButton(IMAGE_DELETE).setEnabled(selected >= 0);
      formPanel.getButton(IMAGE_UPDATE).setEnabled(selected >= 0);
      formPanel.getButton(IMAGE_MOVE_UP).setEnabled(selected >= 1);
      formPanel
          .getButton(IMAGE_MOVE_DOWN)
          .setEnabled(selected <= formPanel.getList(IMAGES).getModel().getSize() - 2);
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
    private transient Map<MD5Key, BufferedImage> imageCache = new HashMap<MD5Key, BufferedImage>();

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
    DefaultListModel model = new DefaultListModel();
    List<BarTokenOverlay> overlays =
        new ArrayList<BarTokenOverlay>(campaign.getTokenBarsMap().values());
    Collections.sort(overlays, BarTokenOverlay.COMPARATOR);
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
    ListModel model = formPanel.getList(BARS).getModel();
    Map<String, BarTokenOverlay> states = new LinkedHashMap<String, BarTokenOverlay>();
    for (int i = 0; i < model.getSize(); i++) {
      BarTokenOverlay overlay = (BarTokenOverlay) model.getElementAt(i);
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
    Color color = ((JETAColorWell) formPanel.getComponentByName(COLOR)).getColor();
    Color bgColor = ((JETAColorWell) formPanel.getComponentByName(BG_COLOR)).getColor();
    String name = formPanel.getText(NAME);
    boolean mouseover = formPanel.isSelected(MOUSEOVER);
    String overlay = ((ListItemProperty) formPanel.getSelectedItem(TYPE)).getLabel();
    int opacity = TokenStatesController.getSpinner(OPACITY, "opacity", formPanel);
    boolean showGM = formPanel.isSelected(SHOW_GM);
    boolean showOwner = formPanel.isSelected(SHOW_OWNER);
    boolean showOthers = formPanel.isSelected(SHOW_OTHERS);
    int thickness = TokenStatesController.getSpinner(THICKNESS, "thickness", formPanel);
    int increments = TokenStatesController.getSpinner(INCREMENTS, "increments", formPanel);
    Side side = Side.valueOf(formPanel.getSelectedItem(SIDE).toString().toUpperCase());

    BarTokenOverlay to = null;
    if (overlay.equals("Solid")) {
      to = new DrawnBarTokenOverlay(name, color, thickness);
    } else if (overlay.equals("Two Tone")) {
      to = new TwoToneBarTokenOverlay(name, color, bgColor, thickness);
    } else {

      // Get all of the assets
      DefaultListModel model = (DefaultListModel) formPanel.getList(IMAGES).getModel();
      MD5Key[] assetIds = new MD5Key[model.getSize()];
      model.copyInto(assetIds);

      // Create the bars
      if (overlay.equals("Two Image")) {
        to = new TwoImageBarTokenOverlay(name, assetIds[1], assetIds[0]);
      } else if (overlay.equals("Single Image")) {
        to = new SingleImageBarTokenOverlay(name, assetIds[0]);
      } else if (overlay.equals("Multiple Images")) {
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
      to.setSide(side);
    } // endif
    return to;
  }

  /** @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent) */
  public void itemStateChanged(ItemEvent e) {
    changedUpdate(null);
  }

  /** @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent) */
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == formPanel.getComponentByName(TESTER)) {
      renderer.value =
          new Double(((JSlider) formPanel.getComponentByName(TESTER)).getValue() / 100.0);
      formPanel.getList(BARS).repaint();
    } else {
      changedUpdate(null);
    }
  }

  /** @param names Setter for names */
  public void setNames(Set<String> names) {
    this.names = names;
  }
}
