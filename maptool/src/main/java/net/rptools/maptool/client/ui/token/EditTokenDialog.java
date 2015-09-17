/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui.token;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractButton;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.functions.AbstractTokenAccessorFunction;
import net.rptools.maptool.client.functions.TokenBarFunction;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.GenericDialog;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Association;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.ObservableList;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.TokenFootprint;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.util.ImageManager;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jidesoft.grid.AbstractPropertyTableModel;
import com.jidesoft.grid.Property;
import com.jidesoft.grid.PropertyPane;
import com.jidesoft.grid.PropertyTable;
import com.jidesoft.swing.CheckBoxListWithSelectable;
import com.jidesoft.swing.DefaultSelectable;
import com.jidesoft.swing.Selectable;

/**
 * This dialog is used to display all of the token states and notes to the user.
 */
public class EditTokenDialog extends AbeillePanel<Token> {
	private static final long serialVersionUID = 1295729281890170792L;

	private boolean tokenSaved;
	private GenericDialog dialog;
	private ImageAssetPanel imagePanel;
	// private CharSheetController controller;

	/**
	 * The size used to constrain the icon.
	 */
	public static final int SIZE = 64;

	/**
	 * Create a new token notes dialog.
	 * 
	 * @param token
	 *            The token being displayed.
	 */
	public EditTokenDialog() {
		super("net/rptools/maptool/client/ui/forms/tokenPropertiesDialog.xml");
		panelInit();
	}

	public void initPlayerNotesTextArea() {
		getNotesTextArea().addMouseListener(new MouseHandler(getNotesTextArea()));
	}

	public void initGMNotesTextArea() {
		getGMNotesTextArea().addMouseListener(new MouseHandler(getGMNotesTextArea()));
		getComponent("@GMNotes").setEnabled(MapTool.getPlayer().isGM());
	}

	public void showDialog(Token token) {
		dialog = new GenericDialog(I18N.getString("EditTokenDialog.msg.title"), MapTool.getFrame(), this) {
			private static final long serialVersionUID = 5439449816096482201L;

			@Override
			public void closeDialog() {
				// TODO: I don't like this. There should really be a AbeilleDialog class that does this
				unbind();
				super.closeDialog();
			}
		};
		bind(token);
		getRootPane().setDefaultButton(getOKButton());
		dialog.showDialog();
	}

	@Override
	public void bind(final Token token) {
		// ICON
		getTokenIconPanel().setImageId(token.getImageAssetId());

		// PROPERTIES
		updatePropertyTypeCombo();
		updatePropertiesTable(token.getPropertyType());

		// SIGHT
		updateSightTypeCombo();

		// Image Tables
		updateImageTableCombo();

		// STATES
		Component barPanel = null;
		updateStatesPanel();
		Component[] statePanels = getStatesPanel().getComponents();
		for (int j = 0; j < statePanels.length; j++) {
			if ("bar".equals(statePanels[j].getName())) {
				barPanel = statePanels[j];
				continue;
			}
			Component[] states = ((Container) statePanels[j]).getComponents();
			for (int i = 0; i < states.length; i++) {
				JCheckBox state = (JCheckBox) states[i];
				state.setSelected(AbstractTokenAccessorFunction.getBooleanValue(token.getState(state.getText())));
			}
		}

		// BARS
		if (barPanel != null) {
			Component[] bars = ((Container) barPanel).getComponents();
			for (int i = 0; i < bars.length; i += 2) {
				JCheckBox cb = (JCheckBox) ((Container) bars[i]).getComponent(1);
				JSlider bar = (JSlider) bars[i + 1];
				if (token.getState(bar.getName()) == null) {
					cb.setSelected(true);
					bar.setEnabled(false);
					bar.setValue(100);
				} else {
					cb.setSelected(false);
					bar.setEnabled(true);
					bar.setValue((int) (TokenBarFunction.getBigDecimalValue(token.getState(bar.getName())).doubleValue() * 100));
				}
			}
		}

		// OWNER LIST
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				getOwnerList().setModel(new OwnerListModel());
			}
		});

		// SPEECH TABLE
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				getSpeechTable().setModel(new SpeechTableModel(token));
			}
		});

		//		Player player = MapTool.getPlayer();
		//		boolean editable = player.isGM() || !MapTool.getServerPolicy().useStrictTokenManagement() || token.isOwner(player.getName());
		//		getAllPlayersCheckBox().setSelected(token.isOwnedByAll());

		// OTHER
		getShapeCombo().setSelectedItem(token.getShape());
		setSizeCombo(token);

		getPropertyTypeCombo().setSelectedItem(token.getPropertyType());
		getSightTypeCombo().setSelectedItem(token.getSightType() != null ? token.getSightType() : MapTool.getCampaign().getCampaignProperties().getDefaultSightType());
		getCharSheetPanel().setImageId(token.getCharsheetImage());
		getPortraitPanel().setImageId(token.getPortraitImage());
		getTokenLayoutPanel().setToken(token);
		getImageTableCombo().setSelectedItem(token.getImageTableName());

		// we will disable the Owner only visible check box if the token is not
		// visible to players to signify the relationship
		ActionListener tokenVisibleActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				boolean selected = abstractButton.getModel().isSelected();
				getVisibleOnlyToOwnerCheckBox().setEnabled(selected);
				getVisibleOnlyToOwnerLabel().setEnabled(selected);
			}
		};
		getVisibleCheckBox().addActionListener(tokenVisibleActionListener);

		// Character Sheets
		//		controller = null;
		//		String form = MapTool.getCampaign().getCharacterSheets().get(token.getPropertyType());
		//		if (form == null)
		//			return;
		//		URL formUrl = getClass().getClassLoader().getResource(form);
		//		if (formUrl == null)
		//			return;
		//		controller = new CharSheetController(formUrl, null);
		//		HashMap<String, Object> properties = new HashMap<String, Object>();
		//		for (String prop : token.getPropertyNames())
		//			properties.put(prop, token.getProperty(prop));
		//		controller.setData(properties);
		//		controller.getPanel().setName("characterSheet");
		//		replaceComponent("sheetPanel", "characterSheet", controller.getPanel());

		super.bind(token);
	}

	public JTabbedPane getTabbedPane() {
		return (JTabbedPane) getComponent("tabs");
	}

	public JTextArea getNotesTextArea() {
		return (JTextArea) getComponent("@notes");
	}

	public JTextArea getGMNotesTextArea() {
		return (JTextArea) getComponent("@GMNotes");
	}

	// private JLabel getGMNameLabel() {
	// return (JLabel) getComponent("tokenGMNameLabel");
	// }
	//
	// public JTextField getNameTextField() {
	// return (JTextField) getComponent("tokenName");
	// }
	//
	// public JTextField getGMNameTextField() {
	// return (JTextField) getComponent("tokenGMName");
	// }

	public void initTypeCombo() {
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(Token.Type.NPC);
		model.addElement(Token.Type.PC);
		// getTypeCombo().setModel(model);
	}

	public JComboBox getTypeCombo() {
		return (JComboBox) getComponent("@type");
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

	public void setSizeCombo(Token token) {
		JComboBox size = getSizeCombo();
		Grid grid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
		DefaultComboBoxModel model = new DefaultComboBoxModel(grid.getFootprints().toArray());
		model.insertElementAt(token.getLayer() == Layer.TOKEN ? "Native Size" : "Free Size", 0);
		size.setModel(model);
		if (token.isSnapToScale()) {
			size.setSelectedItem(token.getFootprint(grid));
		} else {
			size.setSelectedIndex(0);
		}
	}

	public void initPropertyTypeCombo() {
		updatePropertyTypeCombo();
	}

	private void updatePropertyTypeCombo() {
		List<String> typeList = new ArrayList<String>(MapTool.getCampaign().getTokenTypes());
		Collections.sort(typeList);
		DefaultComboBoxModel model = new DefaultComboBoxModel(typeList.toArray());
		getPropertyTypeCombo().setModel(model);
		getPropertyTypeCombo().addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updatePropertiesTable((String) getPropertyTypeCombo().getSelectedItem());
			}
		});
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

	private void updatePropertiesTable(final String propertyType) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				PropertyTable pp = getPropertyTable();
				pp.setModel(new TokenPropertyTableModel());
				pp.expandAll();
			}
		});
	}

	public JComboBox getSizeCombo() {
		return (JComboBox) getComponent("size");
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

	public void initOKButton() {
		getOKButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (commit()) {
					unbind();
					dialog.closeDialog();
				}
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
		if (!super.commit()) {
			return false;
		}
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

		// Get the states
		Component[] stateComponents = getStatesPanel().getComponents();
		Component barPanel = null;
		for (int j = 0; j < stateComponents.length; j++) {
			if ("bar".equals(stateComponents[j].getName())) {
				barPanel = stateComponents[j];
				continue;
			}
			Component[] components = ((Container) stateComponents[j]).getComponents();
			for (int i = 0; i < components.length; i++) {
				JCheckBox cb = (JCheckBox) components[i];
				String state = cb.getText();
				token.setState(state, cb.isSelected() ? Boolean.TRUE : Boolean.FALSE);
			}
		} // endfor

		// BARS
		if (barPanel != null) {
			Component[] bars = ((Container) barPanel).getComponents();
			for (int i = 0; i < bars.length; i += 2) {
				JCheckBox cb = (JCheckBox) ((Container) bars[i]).getComponent(1);
				JSlider bar = (JSlider) bars[i + 1];
				BigDecimal value = cb.isSelected() ? null : new BigDecimal(bar.getValue()).divide(new BigDecimal(100));
				token.setState(bar.getName(), value);
				bar.setValue((int) (TokenBarFunction.getBigDecimalValue(token.getState(bar.getName())).doubleValue() * 100));
			}
		}
		// Ownership
		// If the token is owned by all and we are a player don't alter the ownership list.
		if (MapTool.getPlayer().isGM() || !token.isOwnedByAll()) {
			token.clearAllOwners();

			for (int i = 0; i < getOwnerList().getModel().getSize(); i++) {
				DefaultSelectable selectable = (DefaultSelectable) getOwnerList().getModel().getElementAt(i);
				if (selectable.isSelected()) {
					token.addOwner((String) selectable.getObject());
				}
			}
			// If we are not a GM and the only non GM owner make sure we can't
			// take our selves off of the owners list
			if (!MapTool.getPlayer().isGM()) {
				boolean hasPlayer = false;
				Set<String> owners = token.getOwners();
				if (owners != null) {
					Iterator<Player> playerIter = MapTool.getPlayerList().iterator();
					while (playerIter.hasNext()) {
						Player pl = playerIter.next();
						if (!pl.isGM() && owners.contains(pl.getName())) {
							hasPlayer = true;
						}
					}
				}
				if (!hasPlayer) {
					token.addOwner(MapTool.getPlayer().getName());
				}
			}
		}
		// SHAPE
		token.setShape((Token.TokenShape) getShapeCombo().getSelectedItem());

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
			BufferedImage image = ImageManager.getImageAndWait(getTokenIconPanel().getImageId());
			MapToolUtil.uploadAsset(AssetManager.getAsset(getTokenIconPanel().getImageId()));
			token.setImageAsset(null, getTokenIconPanel().getImageId()); // Default image for now
			token.setWidth(image.getWidth(null));
			token.setHeight(image.getHeight(null));
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

		// OTHER
		tokenSaved = true;

		// Character Sheet
		//		Map<String, Object> properties = controller.getData();
		//		for (String prop : token.getPropertyNames())
		//			token.setProperty(prop, properties.get(prop));

		// Update UI
		MapTool.getFrame().updateTokenTree();
		MapTool.getFrame().resetTokenPanels();

		return true;
	}

	public JButton getOKButton() {
		return (JButton) getComponent("okButton");
	}

	public void initCancelButton() {
		getCancelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unbind();
				dialog.closeDialog();
			}
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
		List<BooleanTokenOverlay> overlays = new ArrayList<BooleanTokenOverlay>(MapTool.getCampaign().getTokenStatesMap().values());
		Map<String, JPanel> groups = new TreeMap<String, JPanel>();
		groups.put("", new JPanel(new FormLayout("0px:grow 2px 0px:grow 2px 0px:grow 2px 0px:grow")));
		for (BooleanTokenOverlay overlay : overlays) {
			String group = overlay.getGroup();
			if (group != null && (group = group.trim()).length() != 0) {
				JPanel panel = groups.get(group);
				if (panel == null) {
					panel = new JPanel(new FormLayout("0px:grow 2px 0px:grow 2px 0px:grow 2px 0px:grow"));
					panel.setBorder(BorderFactory.createTitledBorder(group));
					groups.put(group, panel);
				}
			}
		}
		// Add the group panels and bar panel to the states panel
		JPanel panel = getStatesPanel();
		panel.removeAll();
		FormLayout layout = new FormLayout("0px:grow");
		panel.setLayout(layout);
		int row = 1;
		for (JPanel gPanel : groups.values()) {
			layout.appendRow(new RowSpec("pref"));
			layout.appendRow(new RowSpec("2px"));
			panel.add(gPanel, new CellConstraints(1, row));
			row += 2;
		}
		layout.appendRow(new RowSpec("pref"));
		layout.appendRow(new RowSpec("2px"));
		JPanel barPanel = new JPanel(new FormLayout("right:pref 2px pref 5px right:pref 2px pref"));
		panel.add(barPanel, new CellConstraints(1, row));

		// Add the individual check boxes.
		for (BooleanTokenOverlay state : overlays) {
			String group = state.getGroup();
			panel = groups.get("");
			if (group != null && (group = group.trim()).length() != 0)
				panel = groups.get(group);
			int x = panel.getComponentCount() % 4;
			int y = panel.getComponentCount() / 4;
			if (x == 0) {
				layout = (FormLayout) panel.getLayout();
				if (y != 0)
					layout.appendRow(new RowSpec("2px"));
				layout.appendRow(new RowSpec("pref"));
			}
			panel.add(new JCheckBox(state.getName()), new CellConstraints(x * 2 + 1, y * 2 + 1));
		}
		// Add sliders to the bar panel
		if (MapTool.getCampaign().getTokenBarsMap().size() > 0) {
			layout = (FormLayout) barPanel.getLayout();
			barPanel.setName("bar");
			barPanel.setBorder(BorderFactory.createTitledBorder("Bars"));
			int count = 0;
			row = 0;
			for (BarTokenOverlay bar : MapTool.getCampaign().getTokenBarsMap().values()) {
				int working = count % 2;
				if (working == 0) { // slider row
					layout.appendRow(new RowSpec("pref"));
					row += 1;
				}
				JPanel labelPanel = new JPanel(new FormLayout("pref", "pref 2px:grow pref"));
				barPanel.add(labelPanel, new CellConstraints(1 + working * 4, row));
				labelPanel.add(new JLabel(bar.getName() + ":"), new CellConstraints(1, 1, CellConstraints.RIGHT, CellConstraints.TOP));
				JSlider slider = new JSlider(0, 100);
				JCheckBox hide = new JCheckBox("Hide");
				hide.putClientProperty("JSlider", slider);
				hide.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						JSlider js = (JSlider) ((JCheckBox) e.getSource()).getClientProperty("JSlider");
						js.setEnabled(!((JCheckBox) e.getSource()).isSelected());
					}
				});
				labelPanel.add(hide, new CellConstraints(1, 3, CellConstraints.RIGHT, CellConstraints.TOP));
				slider.setName(bar.getName());
				slider.setPaintLabels(true);
				slider.setPaintTicks(true);
				slider.setMajorTickSpacing(20);
				slider.createStandardLabels(20);
				slider.setMajorTickSpacing(10);
				barPanel.add(slider, new CellConstraints(3 + working * 4, row));
				if (working != 0) { // spacer row
					layout.appendRow(new RowSpec("2px"));
					row += 1;
				}
				count += 1;
			}
		}
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

	private JPanel getGMNotesPanel() {
		return (JPanel) getComponent("gmNotesPanel");
	}

	private JTextField getNameField() {
		return (JTextField) getComponent("@name");
	}

	public CheckBoxListWithSelectable getOwnerList() {
		return (CheckBoxListWithSelectable) getComponent("ownerList");
	}

	public void initSpeechPanel() {
		getSpeechClearAllButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!MapTool.confirm("EditTokenDialog.confirm.clearSpeech")) {
					return;
				}
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						getSpeechTable().setModel(new SpeechTableModel());
					}
				});
			}
		});
	}

	public void initOwnershipPanel() {
		CheckBoxListWithSelectable list = new CheckBoxListWithSelectable();
		list.setName("ownerList");
		replaceComponent("ownershipPanel", "ownershipList", new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
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

	public void initPropertiesPanel() {
		PropertyTable propertyTable = new PropertyTable();
		propertyTable.setFillsViewportHeight(true); // XXX This is Java6-only -- need Java5 solution
		propertyTable.setName("propertiesTable");

		PropertyPane pane = new PropertyPane(propertyTable);
		//		pane.setPreferredSize(new Dimension(100, 300));

		replaceComponent("propertiesPanel", "propertiesTable", pane);
	}

	//	/**
	//	 * Set the currently displayed token.
	//	 * 
	//	 * @param aToken
	//	 *            The token to be displayed
	//	 */
	//	public void setToken(Token aToken) {
	//		if (aToken == token)
	//			return;
	//		if (token != null) {
	//			token.removeModelChangeListener(this);
	//		}
	//		token = aToken;
	//
	//		if (token != null) {
	//			token.addModelChangeListener(this);
	//
	//			List<String> typeList = new ArrayList<String>();
	//			typeList.addAll(MapTool.getCampaign().getTokenTypes());
	//			Collections.sort(typeList);
	//			getPropertyTypeCombo().setModel(new DefaultComboBoxModel(typeList.toArray()));
	//
	//			setFields();
	//			updateView();
	//		}
	//		getTabbedPane().setSelectedIndex(0);
	//	}

	/** @return Getter for tokenSaved */
	public boolean isTokenSaved() {
		return tokenSaved;
	}

	// //
	// HANDLER
	public class MouseHandler extends MouseAdapter {
		JTextArea source;

		public MouseHandler(JTextArea source) {
			this.source = source;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				JPopupMenu menu = new JPopupMenu();
				JMenuItem sendToChatItem = new JMenuItem("Send to Chat");
				sendToChatItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String selectedText = source.getSelectedText();
						if (selectedText == null) {
							selectedText = source.getText();
						}
						// TODO: Combine this with the code in MacroButton
						JTextComponent commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();

						commandArea.setText(commandArea.getText() + selectedText);
						commandArea.requestFocusInWindow();
					}
				});
				menu.add(sendToChatItem);

				JMenuItem sendAsEmoteItem = new JMenuItem("Send as Emit");
				sendAsEmoteItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String selectedText = source.getSelectedText();
						if (selectedText == null) {
							selectedText = source.getText();
						}
						// TODO: Combine this with the code in MacroButton
						JTextComponent commandArea = MapTool.getFrame().getCommandPanel().getCommandTextArea();

						commandArea.setText("/emit " + selectedText);
						commandArea.requestFocusInWindow();
						MapTool.getFrame().getCommandPanel().commitCommand();
					}
				});
				menu.add(sendAsEmoteItem);
				menu.show((JComponent) e.getSource(), e.getX(), e.getY());
			}
		}
	}

	// //
	// MODELS
	private class TokenPropertyTableModel extends AbstractPropertyTableModel<net.rptools.maptool.client.ui.token.EditTokenDialog.TokenPropertyTableModel.EditTokenProperty> {
		private static final long serialVersionUID = 2822797264738675580L;

		private Map<String, String> propertyMap;
		private List<net.rptools.maptool.model.TokenProperty> propertyList;

		private Map<String, String> getPropertyMap() {
			Token token = getModel();

			if (propertyMap == null) {
				propertyMap = new HashMap<String, String>();

				List<net.rptools.maptool.model.TokenProperty> propertyList = getPropertyList();
				for (net.rptools.maptool.model.TokenProperty property : propertyList) {
					String value = (String) token.getProperty(property.getName());
					if (value == null) {
						value = property.getDefaultValue();
					}
					propertyMap.put(property.getName(), value);
				}
			}
			return propertyMap;
		}

		private List<net.rptools.maptool.model.TokenProperty> getPropertyList() {
			if (propertyList == null) {
				propertyList = MapTool.getCampaign().getTokenPropertyList((String) getPropertyTypeCombo().getSelectedItem());
			}
			return propertyList;
		}

		public void applyTo(Token token) {
			for (net.rptools.maptool.model.TokenProperty property : getPropertyList()) {
				String value = getPropertyMap().get(property.getName());
				if (property.getDefaultValue() != null && property.getDefaultValue().equals(value)) {
					token.setProperty(property.getName(), null); // Clear original value
					continue;
				}
				token.setProperty(property.getName(), value);
			}
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
				this.key = key;
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
			List<String> list = new ArrayList<String>();
			Set<String> ownerSet = getModel().getOwners();
			list.addAll(ownerSet);

			ObservableList<Player> playerList = MapTool.getPlayerList();
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

	private static class SpeechTableModel extends KeyValueTableModel {
		private static final long serialVersionUID = 1601750325218502846L;

		public SpeechTableModel(Token token) {
			List<Association<String, String>> rowList = new ArrayList<Association<String, String>>();
			for (String speechName : token.getSpeechNames()) {
				rowList.add(new Association<String, String>(speechName, token.getSpeech(speechName)));
			}
			Collections.sort(rowList, new Comparator<Association<String, String>>() {
				public int compare(Association<String, String> o1, Association<String, String> o2) {
					return o1.getLeft().compareToIgnoreCase(o2.getLeft());
				}
			});
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
}
