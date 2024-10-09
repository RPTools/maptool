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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.ColorWell;
import net.rptools.maptool.client.ui.PreviewPanelFileChooser;
import net.rptools.maptool.client.ui.token.*;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;
import net.rptools.maptool.util.StringUtil;

/**
 * This controller will handle all of the components on the States panel of the {@link
 * CampaignPropertiesDialog}.
 *
 * @author Jay
 */
@SuppressWarnings("unchecked")
public class TokenStatesController
    implements ActionListener,
        DocumentListener,
        ListSelectionListener,
        ItemListener,
        ChangeListener {

  /** Panel containing the campaign properties form panel */
  private final AbeillePanel formPanel;

  /** The names of states currently in the list data model */
  private Set<String> names = new HashSet<String>();

  /** Image file chooser used to support the browse buttons */
  private PreviewPanelFileChooser imageFileChooser;

  /** Name of the text field containing the name {@link String} value */
  public static final String NAME = "tokenStatesName";

  /** Name of the text field containing the group {@link String} value */
  public static final String GROUP = "tokenStatesGroup";

  /**
   * Name of the combo box containing a {@link String} value that maps directly to a {@link
   * BooleanTokenOverlay} class
   */
  public static final String TYPE = "tokenStatesType";

  /**
   * Name of the color well containing a {@link Color} value for token state types that need colors
   */
  public static final String COLOR = "tokenStatesColor";

  /** Name of the check box containing the {@link Boolean} mouseover value */
  public static final String MOUSEOVER = "tokenStatesMouseover";

  /**
   * Name of the spinner containing an {@link Integer} value that is the width of lines for token
   * state types that need line width
   */
  public static final String WIDTH = "tokenStatesWidth";

  /**
   * Name of the combo box containing a {@link String} value that one of the four corners of the
   * token and is used in the {@link ColorDotTokenOverlay} and {@link CornerImageTokenOverlay}
   */
  public static final String CORNER = "tokenStatesCorner";

  /** Name of the button used to add a new token state */
  public static final String ADD = "tokenStatesAddState";

  /** Name of the button used to delete selected token states */
  public static final String DELETE = "tokenStatesDeleteState";

  /** Name of the list containing all of the currently defined token states */
  public static final String STATES = "tokenStatesStates";

  /**
   * Name of the spinner containing a {@link String} value that is converted into the grid size for
   * flow token states
   */
  public static final String FLOW_GRID = "tokenStatesFlowGrid";

  /** Name of the spinner containing an {@link Integer} value that is the opacity used in drawing */
  public static final String OPACITY = "tokenStatesOpacity";

  /**
   * Name of the spinner containing an {@link Integer} value that is the index position of the state
   */
  public static final String INDEX = "tokenStatesIndex";

  /**
   * Name of the text field containing a {@link File} name that is the image file used for image
   * token states
   */
  public static final String IMAGE = "tokenStatesImageFile";

  /** Name of the button used to browse for an image icon */
  public static final String BROWSE = "tokenStatesBrowseImage";

  /** Name of the button used to edit an existing token state. */
  public static final String UPDATE = "tokenStatesUpdateState";

  /** Name of the button used to move a state up one space */
  public static final String MOVE_UP = "tokenStatesMoveUp";

  /** Name of the button used to move a state down one space */
  public static final String MOVE_DOWN = "tokenStatesMoveDown";

  /** Name of the check box that shows the GM sees the state */
  public static final String SHOW_GM = "tokenStatesGM";

  /** Name of the check box that shows the GM sees the state */
  public static final String SHOW_OWNER = "tokenStatesOwner";

  /** Name of the check box that shows the GM sees the state */
  public static final String SHOW_OTHERS = "tokenStatesEverybody";

  /** The size of the ICON faked in the list renderer */
  public static final int ICON_SIZE = 50;

  /** Each of the data entry components that can be enabled/disabled by type of state */
  public static final String[] DATA_ENTRY_COMPONENTS = {
    COLOR, WIDTH, CORNER, FLOW_GRID, IMAGE, BROWSE
  };

  /** Each of the data entry components that can be enabled/disabled by type of state */
  public static final String[] DATA_ENTRY_COMPONENT_LABELS = {
    COLOR + "Label", WIDTH + "Label", CORNER + "Label", FLOW_GRID + "Label", IMAGE + "Label"
  };

  /**
   * Flags for each of the data entry components needed by each of the types. The order of the types
   * is the' same as the list in the combo box. The order of the flags is the same as that in {@link
   * #DATA_ENTRY_COMPONENTS}.
   */
  public static final boolean[][] NEEDED_COMPONENTS = {
    {false, false, false, false, true, true}, // Image
    {false, false, true, false, true, true}, // Corner Image
    {false, false, false, true, true, true}, // Flow Image
    {true, false, true, false, false, false}, // Dot
    {true, false, false, true, false, false}, // Flow Dot
    {true, true, false, false, false, false}, // Circle
    {true, false, false, false, false, false}, // Shaded
    {true, true, false, false, false, false}, // X
    {true, true, false, false, false, false}, // Cross
    {true, true, false, false, false, false}, // Diamond
    {true, false, false, true, false, false}, // Flow Diamond
    {true, true, false, false, false, false}, // Yield
    {true, false, false, true, false, false}, // Flow Yield
    {true, true, false, false, false, false}, // Triangle
    {true, false, false, true, false, false}, // Flow Triangle
    {true, false, false, true, false, false}, // Flow Square
  };

  private enum OverlayType {
    Image,
    CornerImage,
    GridImage,
    Dot,
    GridDot,
    Circle,
    Shaded,
    X,
    Cross,
    Diamond,
    GridDiamond,
    Yield,
    GridYield,
    Triangle,
    GridTriangle,
    GridSquare
  }

  // the order needs to match the OverlayType enum
  private static final List<String> types =
      List.of(
          "CampaignPropertiesDialog.combo.states.type.image",
          "CampaignPropertiesDialog.combo.states.type.cornerImage",
          "CampaignPropertiesDialog.combo.states.type.gridImage",
          "CampaignPropertiesDialog.combo.states.type.dot",
          "CampaignPropertiesDialog.combo.states.type.gridDot",
          "CampaignPropertiesDialog.combo.states.type.circle",
          "CampaignPropertiesDialog.combo.states.type.shaded",
          "CampaignPropertiesDialog.combo.states.type.x",
          "CampaignPropertiesDialog.combo.states.type.cross",
          "CampaignPropertiesDialog.combo.states.type.diamond",
          "CampaignPropertiesDialog.combo.states.type.gridDiamond",
          "CampaignPropertiesDialog.combo.states.type.yield",
          "CampaignPropertiesDialog.combo.states.type.gridYield",
          "CampaignPropertiesDialog.combo.states.type.triangle",
          "CampaignPropertiesDialog.combo.states.type.gridTriangle",
          "CampaignPropertiesDialog.combo.states.type.gridSquare");

  // the order needs to match the AbstractTemplate.Quadrant enum
  private static final List<String> corners =
      List.of(
          "position.corner.topRight",
          "position.corner.topLeft",
          "position.corner.bottomRight",
          "position.corner.bottomLeft");

  /**
   * Set up the button listeners, spinner models, list cell renderer and selection listeners
   *
   * @param panel The {@link CampaignProperties} form panel
   */
  public TokenStatesController(AbeillePanel panel) {
    formPanel = panel;
    panel.getButton(ADD).addActionListener(this);
    panel.getButton(DELETE).addActionListener(this);
    panel.getButton(BROWSE).addActionListener(this);
    panel.getButton(UPDATE).addActionListener(this);
    panel.getButton(MOVE_UP).addActionListener(this);
    panel.getButton(MOVE_DOWN).addActionListener(this);

    var typeComboBox = panel.getComboBox(TYPE);
    typeComboBox.setModel(new DefaultComboBoxModel());
    for (var type : types) {
      typeComboBox.addItem(I18N.getText(type));
    }
    typeComboBox.addActionListener(this);

    var cornerComboBox = panel.getComboBox(CORNER);
    cornerComboBox.setModel(new DefaultComboBoxModel());
    for (var corner : corners) {
      cornerComboBox.addItem(I18N.getText(corner));
    }

    panel.getSpinner(WIDTH).setModel(new SpinnerNumberModel(5, 1, 10, 1));
    panel
        .getSpinner(FLOW_GRID)
        .setModel(new SpinnerListModel(new String[] {"2x2", "3x3", "4x4", "5x5", "8x8"}));
    panel.getSpinner(OPACITY).setModel(new SpinnerNumberModel(100, 1, 100, 5));
    panel
        .getSpinner(INDEX)
        .setModel(new SpinnerNumberModel(0, 0, 10000, 1)); // FIXME set proper upper limit?
    panel.getSpinner(INDEX).addChangeListener(this);
    panel.getList(STATES).setCellRenderer(new StateListRenderer());
    panel.getList(STATES).addListSelectionListener(this);
    panel.getTextComponent(NAME).getDocument().addDocumentListener(this);
    panel.getTextComponent(IMAGE).getDocument().addDocumentListener(this);
    panel.getCheckBox(SHOW_GM).addItemListener(this);
    panel.getCheckBox(SHOW_OTHERS).addItemListener(this);
    panel.getCheckBox(SHOW_OWNER).addItemListener(this);
    enableDataComponents();
    changedUpdate(null);
  }

  /**
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    changedUpdate(null);
  }

  /**
   * Handle all of the buttons and state combo box
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    String name = ((JComponent) e.getSource()).getName();
    JList<Object> list = formPanel.getList(STATES);
    DefaultListModel<Object> model = (DefaultListModel<Object>) list.getModel();
    int selected = list.getSelectedIndex();

    // Add a new state
    if (ADD.equals(name)) {
      BooleanTokenOverlay overlay = createTokenOverlay(null);
      if (overlay != null) {
        if (selected == -1) {
          model.addElement(overlay);
          getNames().add(overlay.getName());
          selected = model.size() - 1;
          System.out.println("selected = " + selected);
        } else {
          // model.addElement(overlay);
          // Jamz: Lets insert the new state at the current index instead of at the bottom, we'll
          // push the current element down one
          Object oldElement = model.remove(selected);
          model.insertElementAt(overlay, selected);
          getNames().add(overlay.getName());
          model.insertElementAt(oldElement, selected + 1);
        }

        formPanel.getTextComponent(NAME).setText("");
        formPanel.getTextComponent(GROUP).setText("");
        formPanel.getCheckBox(MOUSEOVER).setSelected(false);
        formPanel.getSpinner(OPACITY).setValue(100);
        formPanel.getSpinner(INDEX).setValue(selected);
        formPanel.getCheckBox(SHOW_GM).setSelected(true);
        formPanel.getCheckBox(SHOW_OWNER).setSelected(true);
        formPanel.getCheckBox(SHOW_OTHERS).setSelected(true);

        list.ensureIndexIsVisible(selected);
        list.setSelectedIndex(selected);
      }

      // Delete selected state
    } else if (DELETE.equals(name)) {
      int[] selectedElements = list.getSelectedIndices();
      for (int j = selectedElements.length - 1; j >= 0; j--) {
        BooleanTokenOverlay overlay = (BooleanTokenOverlay) model.remove(selectedElements[j]);
        getNames().remove(overlay.getName());
      } // endfor
      changedUpdate(null);

      // Browse for an image for image token states
    } else if (BROWSE.equals(name)) {
      if (getImageFileChooser().showOpenDialog(formPanel) == JFileChooser.APPROVE_OPTION) {
        File imageFile = getImageFileChooser().getSelectedFile();
        if (imageFile == null
            || imageFile.isDirectory()
            || !imageFile.exists()
            || !imageFile.canRead()) return;
        formPanel.getTextComponent(IMAGE).setText(imageFile.getPath());
        AppPreferences.loadDirectory.set(imageFile.getParentFile());
      } // endif

      // Change the enabled data components.
    } else if (TYPE.equals(name)) {
      enableDataComponents();
      changedUpdate(null);

      // Update the selected overlay
    } else if (UPDATE.equals(name)) {
      BooleanTokenOverlay selectedOverlay =
          (BooleanTokenOverlay) formPanel.getList(STATES).getSelectedValue();
      BooleanTokenOverlay overlay = createTokenOverlay(selectedOverlay);
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

  @Override
  public void stateChanged(ChangeEvent e) {
    String name = ((JComponent) e.getSource()).getName();
    JList<Object> list = formPanel.getList(STATES);
    DefaultListModel<Object> model = (DefaultListModel<Object>) list.getModel();
    int selected = list.getSelectedIndex();

    if (INDEX.equals(name) && selected != -1) {
      int value = (int) ((JSpinner) e.getSource()).getValue();
      if (selected != value) {
        if (value < 0) value = 0;

        if (value >= model.getSize()) value = model.getSize() - 1;

        Object element = model.remove(selected);
        model.insertElementAt(element, value);
        list.ensureIndexIsVisible(value);
        list.setSelectedIndex(value);
      }
    }
  }

  /** Enable the data components needed by the selected type of overlay. Disable the rest. */
  private void enableDataComponents() {
    int selected = formPanel.getComboBox(TYPE).getSelectedIndex();
    for (int i = 0; i < DATA_ENTRY_COMPONENTS.length; i++) {
      formPanel.getComponent(DATA_ENTRY_COMPONENTS[i]).setEnabled(NEEDED_COMPONENTS[selected][i]);
      if (i < DATA_ENTRY_COMPONENT_LABELS.length)
        formPanel
            .getComponent(DATA_ENTRY_COMPONENT_LABELS[i])
            .setEnabled(NEEDED_COMPONENTS[selected][i]);
    } // endfor
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
  @Override
  public void changedUpdate(DocumentEvent e) {
    String text = formPanel.getTextComponent(IMAGE).getText();
    var overlayType = OverlayType.values()[formPanel.getComboBox(TYPE).getSelectedIndex()];
    boolean hasImage =
        !(overlayType == OverlayType.CornerImage
                || overlayType == OverlayType.GridImage
                || overlayType == OverlayType.Image)
            || text != null && (text = text.trim()).length() != 0;
    text = formPanel.getTextComponent(NAME).getText();
    boolean hasName = text != null && (text = text.trim()).length() != 0;
    boolean hasShow =
        formPanel.getCheckBox(SHOW_GM).isSelected()
            || formPanel.getCheckBox(SHOW_OWNER).isSelected()
            || formPanel.getCheckBox(SHOW_OTHERS).isSelected();
    BooleanTokenOverlay selectedState =
        (BooleanTokenOverlay) formPanel.getList(STATES).getSelectedValue();
    boolean hasUniqueUpdateName = false;
    if (selectedState != null)
      hasUniqueUpdateName = selectedState.getName().equals(text) || !getNames().contains(text);
    formPanel
        .getButton(ADD)
        .setEnabled(hasName && !getNames().contains(text) && hasImage && hasShow);
    formPanel
        .getButton(UPDATE)
        .setEnabled(hasName && hasUniqueUpdateName && selectedState != null && hasShow);
  }

  /**
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
  public void insertUpdate(DocumentEvent e) {
    changedUpdate(e);
  }

  /**
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  @Override
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
    int selected = formPanel.getList(STATES).getSelectedIndex();
    formPanel.getButton(DELETE).setEnabled(selected >= 0);
    changedUpdate(null); // Makes sure update is selected
    formPanel.getButton(MOVE_UP).setEnabled(selected >= 1);
    formPanel
        .getButton(MOVE_DOWN)
        .setEnabled(selected <= formPanel.getList(STATES).getModel().getSize() - 2);
    if (selected >= 0) {

      // Set name, and always clear image
      BooleanTokenOverlay s = (BooleanTokenOverlay) formPanel.getList(STATES).getSelectedValue();
      formPanel.getTextComponent(NAME).setText(s.getName());
      formPanel.getTextComponent(GROUP).setText(s.getGroup());
      formPanel.getTextComponent(IMAGE).setText("");
      formPanel.getCheckBox(MOUSEOVER).setSelected(s.isMouseover());
      formPanel.getSpinner(OPACITY).setValue(s.getOpacity());
      formPanel.getSpinner(INDEX).setValue(selected);
      formPanel.getCheckBox(SHOW_GM).setSelected(s.isShowGM());
      formPanel.getCheckBox(SHOW_OWNER).setSelected(s.isShowOwner());
      formPanel.getCheckBox(SHOW_OTHERS).setSelected(s.isShowOthers());

      // Get most of the colors and all of the widths from the XTokenOverlay
      OverlayType type = OverlayType.Image;
      if (s instanceof XTokenOverlay) {
        type = OverlayType.X;
        formPanel.getSpinner(WIDTH).setValue(((XTokenOverlay) s).getWidth());
        ((ColorWell) formPanel.getComponent(COLOR)).setColor(((XTokenOverlay) s).getColor());
      } // endif

      // Get the the flow grid for most components from FlowColorDotTokenOverlay
      if (s instanceof FlowColorDotTokenOverlay) {
        type = OverlayType.GridDot;
        int size = ((FlowColorDotTokenOverlay) s).getGrid();
        formPanel.getSpinner(FLOW_GRID).setValue(size + "x" + size);
      } // endif

      // Handle the
      if (s instanceof CornerImageTokenOverlay) {
        type = OverlayType.CornerImage;
        formPanel
            .getComboBox(CORNER)
            .setSelectedIndex(((CornerImageTokenOverlay) s).getCorner().ordinal());
      } else if (s instanceof FlowImageTokenOverlay) {
        type = OverlayType.GridImage;
        int size = ((FlowImageTokenOverlay) s).getGrid(); // Still need grid size
        formPanel.getSpinner(FLOW_GRID).setValue(size + "x" + size);
      } else if (s instanceof ImageTokenOverlay) {
        type = OverlayType.Image;
      } else if (s instanceof ColorDotTokenOverlay) {
        type = OverlayType.Dot;
        formPanel
            .getComboBox(CORNER)
            .setSelectedIndex(((ColorDotTokenOverlay) s).getCorner().ordinal());
      } else if (s instanceof OTokenOverlay) {
        type = OverlayType.Circle;
      } else if (s instanceof ShadedTokenOverlay) {
        type = OverlayType.Shaded;
        ((ColorWell) formPanel.getComponent(COLOR)).setColor(((ShadedTokenOverlay) s).getColor());
      } else if (s instanceof CrossTokenOverlay) {
        type = OverlayType.Cross;
      } else if (s instanceof DiamondTokenOverlay) {
        type = OverlayType.Diamond;
      } else if (s instanceof FlowDiamondTokenOverlay) {
        type = OverlayType.GridDiamond;
      } else if (s instanceof YieldTokenOverlay) {
        type = OverlayType.Yield;
      } else if (s instanceof FlowYieldTokenOverlay) {
        type = OverlayType.GridYield;
      } else if (s instanceof TriangleTokenOverlay) {
        type = OverlayType.Triangle;
      } else if (s instanceof FlowTriangleTokenOverlay) {
        type = OverlayType.GridTriangle;
      } else if (s instanceof FlowColorSquareTokenOverlay) {
        type = OverlayType.GridSquare;
      } // endif

      // Set the type and change components
      formPanel.getComboBox(TYPE).setSelectedIndex(type.ordinal());
      enableDataComponents();
      changedUpdate(null);
    }
  }

  /**
   * The {@link ListCellRenderer} that draws the state as an icon and a state name.
   *
   * @author Jay
   */
  public static class StateListRenderer extends DefaultListCellRenderer {

    /** Bounds sent to the token state */
    Rectangle bounds = new Rectangle(0, 0, ICON_SIZE, ICON_SIZE);

    /** Fake token sent to the token state */
    Token token = new Token("name", null);

    /** Value passed to the overlay painter. */
    Double value = 1d;

    /** Overlay being painted by the icon */
    AbstractTokenOverlay overlay;

    /**
     * Create an icon from the token state. The icon has a black rectangle and the actual state is
     * drawn inside of it.
     */
    Icon icon =
        new Icon() {
          @Override
          public int getIconHeight() {
            return ICON_SIZE + 2;
          }

          @Override
          public int getIconWidth() {
            return ICON_SIZE + 2;
          }

          @Override
          public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawRect(x, y, ICON_SIZE + 2, ICON_SIZE + 2);
            g.translate(x + 1, y + 1);
            Shape old = g.getClip();
            g.setClip(bounds.intersection(old.getBounds()));
            overlay.paintOverlay((Graphics2D) g, token, bounds, value);
            g.setClip(old);
            g.translate(-(x + 1), -(y + 1));
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
      overlay = (AbstractTokenOverlay) value;
      setText(overlay.getName());
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
    names.clear();
    DefaultListModel<Object> model = new DefaultListModel<Object>();
    List<BooleanTokenOverlay> overlays =
        new ArrayList<BooleanTokenOverlay>(campaign.getTokenStatesMap().values());
    overlays.sort(BooleanTokenOverlay.COMPARATOR);
    for (BooleanTokenOverlay overlay : overlays) {
      model.addElement(overlay);
      getNames().add(overlay.getName());
    }
    formPanel.getList(STATES).setModel(model);
  }

  /**
   * Copy the token states from the state tab and place it in the passed campaign.
   *
   * @param campaign Campaign containing the properties being updated
   */
  public void copyUIToCampaign(Campaign campaign) {
    ListModel<Object> model = formPanel.getList(STATES).getModel();
    Map<String, BooleanTokenOverlay> states = new LinkedHashMap<String, BooleanTokenOverlay>();
    for (int i = 0; i < model.getSize(); i++) {
      BooleanTokenOverlay overlay = (BooleanTokenOverlay) model.getElementAt(i);
      overlay.setOrder(i);
      states.put(overlay.getName(), overlay);
    }
    campaign.getTokenStatesMap().clear();
    campaign.getTokenStatesMap().putAll(states);
  }

  /**
   * Create a token state from the user's input
   *
   * @param updatedOverlay Overlay being modified.
   * @return The new token state.
   */
  public BooleanTokenOverlay createTokenOverlay(BooleanTokenOverlay updatedOverlay) {

    // Need the color group, and name for everything
    Color color = ((ColorWell) formPanel.getComponent(COLOR)).getColor();
    String name = formPanel.getTextComponent(NAME).getText();
    String group = formPanel.getTextComponent(GROUP).getText();
    boolean mouseover = formPanel.getCheckBox(MOUSEOVER).isSelected();
    var overlay = OverlayType.values()[formPanel.getComboBox(TYPE).getSelectedIndex()];
    int opacity = getSpinner(OPACITY, "opacity", formPanel);
    int index = getSpinner(INDEX, "index", formPanel);
    boolean showGM = formPanel.getCheckBox(SHOW_GM).isSelected();
    boolean showOwner = formPanel.getCheckBox(SHOW_OWNER).isSelected();
    boolean showOthers = formPanel.getCheckBox(SHOW_OTHERS).isSelected();

    // Check for overlays that don't use width
    BooleanTokenOverlay to = null;
    if (overlay == OverlayType.Dot) {
      to =
          new ColorDotTokenOverlay(
              name, color, Quadrant.values()[formPanel.getComboBox(CORNER).getSelectedIndex()]);
    } else if (overlay == OverlayType.Shaded) {
      to = new ShadedTokenOverlay(name, color);
    } // endif

    // Get flow information
    int grid = -1;
    if (to == null) {
      String sGrid = (String) formPanel.getSpinner(FLOW_GRID).getValue();
      grid = Integer.parseInt(sGrid.substring(0, 1));
      if (overlay == OverlayType.GridDot) {
        to = new FlowColorDotTokenOverlay(name, color, grid);
      }
      if (overlay == OverlayType.GridSquare) {
        to = new FlowColorSquareTokenOverlay(name, color, grid);
      }
      if (overlay == OverlayType.GridTriangle) {
        to = new FlowTriangleTokenOverlay(name, color, grid);
      }
      if (overlay == OverlayType.GridDiamond) {
        to = new FlowDiamondTokenOverlay(name, color, grid);
      }
      if (overlay == OverlayType.GridYield) {
        to = new FlowYieldTokenOverlay(name, color, grid);
      } // endif
    } // endif

    // Handle all of the overlays with width
    if (to == null) {
      int width = getSpinner(WIDTH, "width", formPanel);
      if (overlay == OverlayType.Circle) {
        to = new OTokenOverlay(name, color, width);
      } else if (overlay == OverlayType.X) {
        to = new XTokenOverlay(name, color, width);
      } else if (overlay == OverlayType.Cross) {
        to = new CrossTokenOverlay(name, color, width);
      } else if (overlay == OverlayType.Diamond) {
        to = new DiamondTokenOverlay(name, color, width);
      } else if (overlay == OverlayType.Yield) {
        to = new YieldTokenOverlay(name, color, width);
      } else if (overlay == OverlayType.Triangle) {
        to = new TriangleTokenOverlay(name, color, width);
      } // endif
    } // endif

    // If we get here it is an image overlay, grab the image as an asset
    if (to == null) {
      MD5Key assetId = null;
      String fName = formPanel.getTextComponent(IMAGE).getText().trim();
      fName = fName.length() == 0 ? null : fName;
      if (updatedOverlay == null || fName != null) {
        assetId = loadAsssetFile(fName, formPanel);
      } else {
        if (updatedOverlay instanceof ImageTokenOverlay)
          assetId = ((ImageTokenOverlay) updatedOverlay).getAssetId();
      } // endif

      // Create all of the image overlays
      if (assetId != null) {
        if (overlay == OverlayType.Image) {
          to = new ImageTokenOverlay(name, assetId);
        } else if (overlay == OverlayType.CornerImage) {
          to =
              new CornerImageTokenOverlay(
                  name,
                  assetId,
                  Quadrant.values()[formPanel.getComboBox(CORNER).getSelectedIndex()]);
        } else if (overlay == OverlayType.GridImage) {
          to = new FlowImageTokenOverlay(name, assetId, grid);
        } // endif
      } // endif
    } // endif

    // Set the common token stuff
    if (to != null) {
      to.setGroup(group);
      to.setMouseover(mouseover);
      to.setOpacity(opacity);
      to.setOrder(index);
      to.setShowGM(showGM);
      to.setShowOthers(showOthers);
      to.setShowOwner(showOwner);
    } // endif
    return to;
  }

  /**
   * Read an integer value from a spinner.
   *
   * @param name Name of the spinner.
   * @param displayName Name used in the message if there is an error.
   * @param formPanel The form panel containing the component.
   * @return The integer value selected.
   */
  public static int getSpinner(String name, String displayName, AbeillePanel formPanel) {
    int width = 0;
    JSpinner spinner = formPanel.getSpinner(name);
    try {
      spinner.commitEdit();
      width = (Integer) spinner.getValue();
    } catch (ParseException e) {
      JOptionPane.showMessageDialog(
          spinner,
          "There is an invalid "
              + displayName
              + " specified: "
              + ((JTextField) spinner.getEditor()).getText(),
          "Error!",
          JOptionPane.ERROR_MESSAGE);
      throw new IllegalStateException(e);
    } // endtry
    return width;
  }

  /**
   * @return Getter for names
   */
  public Set<String> getNames() {
    return names;
  }

  /**
   * @param names Setter for names
   */
  public void setNames(Set<String> names) {
    this.names = names;
  }

  /**
   * Load an asset file to get its key.
   *
   * @param fName Name of the asset file (returns <code>null</code> if <b>fName</b> is empty or
   *     <code>null</code>)
   * @param formPanel Panel to use as parent for displaying error messages
   * @return The asset id if found or <code>null</code> if it could not be found.
   */
  public static MD5Key loadAsssetFile(String fName, AbeillePanel formPanel) {
    if (StringUtil.isEmpty(fName)) return null;
    File file = new File(fName);
    String message = null;
    if (!file.exists()) {
      message = "The image file does not exist: ";
    } else if (!file.canRead()) {
      message = "The image file cannot be read: ";
    } else if (file.isDirectory()) {
      message = "The file specified is a directory: ";
    }
    if (message != null) {
      JOptionPane.showMessageDialog(
          formPanel, message + file.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
      return null;
    }
    Asset asset = null;
    try {
      asset = AssetManager.createAsset(file);
    } catch (IOException e) {
      JOptionPane.showMessageDialog(
          formPanel,
          "Error reading image file: " + file.getAbsolutePath(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return null;
    }
    AssetManager.putAsset(asset);
    return asset.getMD5Key();
  }
}
