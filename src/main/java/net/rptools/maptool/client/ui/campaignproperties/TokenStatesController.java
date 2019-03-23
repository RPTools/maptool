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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SpinnerListModel;
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
import net.rptools.maptool.client.ui.token.AbstractTokenOverlay;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.client.ui.token.ColorDotTokenOverlay;
import net.rptools.maptool.client.ui.token.CornerImageTokenOverlay;
import net.rptools.maptool.client.ui.token.CrossTokenOverlay;
import net.rptools.maptool.client.ui.token.DiamondTokenOverlay;
import net.rptools.maptool.client.ui.token.FlowColorDotTokenOverlay;
import net.rptools.maptool.client.ui.token.FlowColorSquareTokenOverlay;
import net.rptools.maptool.client.ui.token.FlowDiamondTokenOverlay;
import net.rptools.maptool.client.ui.token.FlowImageTokenOverlay;
import net.rptools.maptool.client.ui.token.FlowTriangleTokenOverlay;
import net.rptools.maptool.client.ui.token.FlowYieldTokenOverlay;
import net.rptools.maptool.client.ui.token.ImageTokenOverlay;
import net.rptools.maptool.client.ui.token.OTokenOverlay;
import net.rptools.maptool.client.ui.token.ShadedTokenOverlay;
import net.rptools.maptool.client.ui.token.TriangleTokenOverlay;
import net.rptools.maptool.client.ui.token.XTokenOverlay;
import net.rptools.maptool.client.ui.token.YieldTokenOverlay;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.CampaignProperties;
import net.rptools.maptool.model.Token;
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
  private final FormPanel formPanel;

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
   * token and is used in the {@link ColorDotTokenOverlay} & {@link CornerImageTokenOverlay}
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

  /**
   * Set up the button listeners, spinner models, list cell renderer and selection listeners
   *
   * @param panel The {@link CampaignProperties} form panel
   */
  public TokenStatesController(FormPanel panel) {
    formPanel = panel;
    panel.getButton(ADD).addActionListener(this);
    panel.getButton(DELETE).addActionListener(this);
    panel.getButton(BROWSE).addActionListener(this);
    panel.getButton(UPDATE).addActionListener(this);
    panel.getButton(MOVE_UP).addActionListener(this);
    panel.getButton(MOVE_DOWN).addActionListener(this);
    panel.getComboBox(TYPE).addActionListener(this);
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

  /** @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent) */
  public void itemStateChanged(ItemEvent e) {
    changedUpdate(null);
  }

  /**
   * Handle all of the buttons & state combo box
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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

        formPanel.setText(NAME, "");
        formPanel.setText(GROUP, "");
        formPanel.setSelected(MOUSEOVER, false);
        formPanel.getSpinner(OPACITY).setValue(new Integer(100));
        formPanel.getSpinner(INDEX).setValue(selected);
        formPanel.setSelected(SHOW_GM, true);
        formPanel.setSelected(SHOW_OWNER, true);
        formPanel.setSelected(SHOW_OTHERS, true);

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
        formPanel.setText(IMAGE, imageFile.getPath());
        AppPreferences.setLoadDir(imageFile.getParentFile());
      } // endif

      // Change the enabled data components.
    } else if (TYPE.equals(name)) {
      enableDataComponents();

      // Update the selected overlay
    } else if (UPDATE.equals(name)) {
      BooleanTokenOverlay selectedOverlay = (BooleanTokenOverlay) formPanel.getSelectedItem(STATES);
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
      formPanel
          .getComponentByName(DATA_ENTRY_COMPONENTS[i])
          .setEnabled(NEEDED_COMPONENTS[selected][i]);
      if (i < DATA_ENTRY_COMPONENT_LABELS.length)
        formPanel
            .getComponentByName(DATA_ENTRY_COMPONENT_LABELS[i])
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
  public void changedUpdate(DocumentEvent e) {
    String text = formPanel.getText(IMAGE);
    boolean hasImage =
        !((ListItemProperty) formPanel.getSelectedItem(TYPE)).getLabel().contains("Image")
            || text != null && (text = text.trim()).length() != 0;
    text = formPanel.getText(NAME);
    boolean hasName = text != null && (text = text.trim()).length() != 0;
    boolean hasShow =
        formPanel.isSelected(SHOW_GM)
            || formPanel.isSelected(SHOW_OWNER)
            || formPanel.isSelected(SHOW_OTHERS);
    formPanel
        .getButton(ADD)
        .setEnabled(hasName && !getNames().contains(text) && hasImage && hasShow);
    formPanel
        .getButton(UPDATE)
        .setEnabled(hasName && formPanel.getSelectedItem(STATES) != null && hasShow);
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
      formPanel.setText(NAME, s.getName());
      formPanel.setText(GROUP, s.getGroup());
      formPanel.setText(IMAGE, "");
      formPanel.setSelected(MOUSEOVER, s.isMouseover());
      formPanel.getSpinner(OPACITY).setValue(new Integer(s.getOpacity()));
      formPanel.getSpinner(INDEX).setValue(selected);
      formPanel.setSelected(SHOW_GM, s.isShowGM());
      formPanel.setSelected(SHOW_OWNER, s.isShowOwner());
      formPanel.setSelected(SHOW_OTHERS, s.isShowOthers());

      // Get most of the colors and all of the widths from the XTokenOverlay
      int type = -1;
      if (s instanceof XTokenOverlay) {
        type = 7;
        formPanel.getSpinner(WIDTH).setValue(Integer.valueOf(((XTokenOverlay) s).getWidth()));
        ((JETAColorWell) formPanel.getComponentByName(COLOR))
            .setColor(((XTokenOverlay) s).getColor());
      } // endif

      // Get the the flow grid for most components from FlowColorDotTokenOverlay
      if (s instanceof FlowColorDotTokenOverlay) {
        type = 4;
        int size = ((FlowColorDotTokenOverlay) s).getGrid();
        formPanel.getSpinner(FLOW_GRID).setValue(size + "x" + size);
      } // endif

      // Handle the
      if (s instanceof CornerImageTokenOverlay) {
        type = 1;
        formPanel
            .getComboBox(CORNER)
            .setSelectedIndex(((CornerImageTokenOverlay) s).getCorner().ordinal());
      } else if (s instanceof FlowImageTokenOverlay) {
        type = 2;
        int size = ((FlowImageTokenOverlay) s).getGrid(); // Still need grid size
        formPanel.getSpinner(FLOW_GRID).setValue(size + "x" + size);
      } else if (s instanceof ImageTokenOverlay) {
        type = 0;
      } else if (s instanceof ColorDotTokenOverlay) {
        type = 3;
        formPanel
            .getComboBox(CORNER)
            .setSelectedIndex(((ColorDotTokenOverlay) s).getCorner().ordinal());
      } else if (s instanceof OTokenOverlay) {
        type = 5;
      } else if (s instanceof ShadedTokenOverlay) {
        type = 6;
        ((JETAColorWell) formPanel.getComponentByName(COLOR))
            .setColor(((ShadedTokenOverlay) s).getColor());
      } else if (s instanceof CrossTokenOverlay) {
        type = 8;
      } else if (s instanceof DiamondTokenOverlay) {
        type = 9;
      } else if (s instanceof FlowDiamondTokenOverlay) {
        type = 10;
      } else if (s instanceof YieldTokenOverlay) {
        type = 11;
      } else if (s instanceof FlowYieldTokenOverlay) {
        type = 12;
      } else if (s instanceof TriangleTokenOverlay) {
        type = 13;
      } else if (s instanceof FlowTriangleTokenOverlay) {
        type = 14;
      } else if (s instanceof FlowColorSquareTokenOverlay) {
        type = 15;
      } // endif

      // Set the type and change components
      formPanel.getComboBox(TYPE).setSelectedIndex(type);
      enableDataComponents();
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
    Double value = Double.valueOf(1);

    /** Overlay being painted by the icon */
    AbstractTokenOverlay overlay;

    /**
     * Create an icon from the token state. The icon has a black rectangle and the actual state is
     * drawn inside of it.
     */
    Icon icon =
        new Icon() {
          public int getIconHeight() {
            return ICON_SIZE + 2;
          }

          public int getIconWidth() {
            return ICON_SIZE + 2;
          }

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
    Collections.sort(overlays, BooleanTokenOverlay.COMPARATOR);
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
    Color color = ((JETAColorWell) formPanel.getComponentByName(COLOR)).getColor();
    String name = formPanel.getText(NAME);
    String group = formPanel.getText(GROUP);
    boolean mouseover = formPanel.isSelected(MOUSEOVER);
    String overlay = ((ListItemProperty) formPanel.getSelectedItem(TYPE)).getLabel();
    int opacity = getSpinner(OPACITY, "opacity", formPanel);
    int index = getSpinner(INDEX, "index", formPanel);
    boolean showGM = formPanel.isSelected(SHOW_GM);
    boolean showOwner = formPanel.isSelected(SHOW_OWNER);
    boolean showOthers = formPanel.isSelected(SHOW_OTHERS);

    // Check for overlays that don't use width
    BooleanTokenOverlay to = null;
    if (overlay.equals("Dot")) {
      String cornerName =
          formPanel.getSelectedItem(CORNER).toString().toUpperCase().replace(' ', '_');
      to = new ColorDotTokenOverlay(name, color, Quadrant.valueOf(cornerName));
    } else if (overlay.equals("Shaded")) {
      to = new ShadedTokenOverlay(name, color);
    } // endif

    // Get flow information
    int grid = -1;
    if (to == null) {
      String sGrid = (String) formPanel.getSpinner(FLOW_GRID).getValue();
      grid = Integer.parseInt(sGrid.substring(0, 1));
      if (overlay.equals("Grid Dot")) {
        to = new FlowColorDotTokenOverlay(name, color, grid);
      }
      if (overlay.equals("Grid Square")) {
        to = new FlowColorSquareTokenOverlay(name, color, grid);
      }
      if (overlay.equals("Grid Triangle")) {
        to = new FlowTriangleTokenOverlay(name, color, grid);
      }
      if (overlay.equals("Grid Diamond")) {
        to = new FlowDiamondTokenOverlay(name, color, grid);
      }
      if (overlay.equals("Grid Yield")) {
        to = new FlowYieldTokenOverlay(name, color, grid);
      } // endif
    } // endif

    // Handle all of the overlays with width
    if (to == null) {
      int width = getSpinner(WIDTH, "width", formPanel);
      if (overlay.equals("Circle")) {
        to = new OTokenOverlay(name, color, width);
      } else if (overlay.equals("X")) {
        to = new XTokenOverlay(name, color, width);
      } else if (overlay.equals("Cross")) {
        to = new CrossTokenOverlay(name, color, width);
      } else if (overlay.equals("Diamond")) {
        to = new DiamondTokenOverlay(name, color, width);
      } else if (overlay.equals("Yield")) {
        to = new YieldTokenOverlay(name, color, width);
      } else if (overlay.equals("Triangle")) {
        to = new TriangleTokenOverlay(name, color, width);
      } // endif
    } // endif

    // If we get here it is an image overlay, grab the image as an asset
    if (to == null) {
      MD5Key assetId = null;
      String fName = formPanel.getText(IMAGE).trim();
      fName = fName.length() == 0 ? null : fName;
      if (updatedOverlay == null || fName != null) {
        assetId = loadAsssetFile(fName, formPanel);
      } else {
        if (updatedOverlay instanceof ImageTokenOverlay)
          assetId = ((ImageTokenOverlay) updatedOverlay).getAssetId();
      } // endif

      // Create all of the image overlays
      if (assetId != null) {
        if (overlay.equals("Image")) {
          to = new ImageTokenOverlay(name, assetId);
        } else if (overlay.equals("Corner Image")) {
          String cornerName =
              formPanel.getSelectedItem(CORNER).toString().toUpperCase().replace(' ', '_');
          to = new CornerImageTokenOverlay(name, assetId, Quadrant.valueOf(cornerName));
        } else if (overlay.equals("Grid Image")) {
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
  public static int getSpinner(String name, String displayName, FormPanel formPanel) {
    int width = 0;
    JSpinner spinner = formPanel.getSpinner(name);
    try {
      spinner.commitEdit();
      width = ((Integer) spinner.getValue()).intValue();
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

  /** @return Getter for names */
  public Set<String> getNames() {
    return names;
  }

  /** @param names Setter for names */
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
  public static MD5Key loadAsssetFile(String fName, FormPanel formPanel) {
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
    return asset.getId();
  }
}
