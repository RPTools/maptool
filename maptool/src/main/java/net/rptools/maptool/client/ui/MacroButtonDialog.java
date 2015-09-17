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
package net.rptools.maptool.client.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.lib.swing.preference.WindowPreferences;
import net.rptools.maptool.client.AppConstants;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolUtil;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.GridView;

public class MacroButtonDialog extends JDialog {

	FormPanel panel;
	MacroButton button;
	MacroButtonProperties properties;
	boolean isTokenMacro = false;
	int oldHashCode = 0;
	Boolean startingCompareGroup;
	Boolean startingCompareSortPrefix;
	Boolean startingCompareCommand;
	Boolean startingCompareIncludeLabel;
	Boolean startingCompareAutoExecute;
	Boolean startingCompareApplyToSelectedTokens;
	Boolean startingAllowPlayerEdits;

	public MacroButtonDialog() {

		super(MapTool.getFrame(), "", true);
		panel = new FormPanel("net/rptools/maptool/client/ui/forms/macroButtonDialog.xml");
		setContentPane(panel);
		setSize(700, 400);
		SwingUtil.centerOver(this, MapTool.getFrame());

		installOKButton();
		installCancelButton();
		installHotKeyCombo();
		installColorCombo();
		installFontColorCombo();
		installFontSizeCombo();

		initCommandTextArea();

		panel.getCheckBox("applyToTokensCheckBox").setEnabled(!isTokenMacro);
		panel.getComboBox("hotKey").setEnabled(!isTokenMacro);
		panel.getTextField("maxWidth").setEnabled(false); // can't get max-width to work, so temporarily disabling it.
		panel.getCheckBox("allowPlayerEditsCheckBox").setEnabled(MapTool.getPlayer().isGM());

		new WindowPreferences(AppConstants.APP_NAME, "editMacroDialog", this);
	}

	private void installHotKeyCombo() {
		String[] hotkeys = MacroButtonHotKeyManager.HOTKEYS;
		JComboBox combo = panel.getComboBox("hotKey");
		for (int i = 0; i < hotkeys.length; i++)
			combo.insertItemAt(hotkeys[i], i);
	}

	private void installColorCombo() {
		JComboBox combo = panel.getComboBox("colorComboBox");
		combo.setModel(new DefaultComboBoxModel(MapToolUtil.getColorNames().toArray()));
		combo.insertItemAt("default", 0);
		combo.setSelectedItem("default");
		combo.setRenderer(new ColorComboBoxRenderer());
	}

	private void installFontColorCombo() {
		JComboBox combo = panel.getComboBox("fontColorComboBox");
		combo.setModel(new DefaultComboBoxModel(MacroButtonProperties.getFontColors()));
		//		combo.insertItemAt("default", 0);
		combo.setSelectedItem("black");
		combo.setRenderer(new ColorComboBoxRenderer());
	}

	private void installFontSizeCombo() {
		String[] fontSizes = { "0.75em", "0.80em", "0.85em", "0.90em", "0.95em", "1.00em", "1.05em", "1.10em", "1.15em", "1.20em", "1.25em" };
		//		String[] fontSizes = { "6pt", "7pt", "8pt", "9pt", "10pt", "11pt", "12pt", "13pt", "14pt", "15pt", "16pt" };
		JComboBox combo = panel.getComboBox("fontSizeComboBox");
		combo.setModel(new DefaultComboBoxModel(fontSizes));
	}

	private void installOKButton() {
		JButton button = (JButton) panel.getButton("okButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		getRootPane().setDefaultButton(button);
	}

	private void installCancelButton() {
		JButton button = (JButton) panel.getButton("cancelButton");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
	}

	public void show(MacroButton button) {
		initI18NSupport();
		this.button = button;
		this.isTokenMacro = button.getToken() == null ? false : true;
		this.properties = button.getProperties();
		oldHashCode = properties.hashCodeForComparison();
		if (properties != null) {
			Boolean playerCanEdit = !MapTool.getPlayer().isGM() && properties.getAllowPlayerEdits();
			Boolean onGlobalPanel = properties.getSaveLocation().equals("Global");
			Boolean allowEdits = onGlobalPanel || MapTool.getPlayer().isGM() || playerCanEdit;
			Boolean isCommonMacro = button.getPanelClass().equals("SelectionPanel") && MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(properties);
			if (allowEdits) {
				this.setTitle(I18N.getText("component.dialogTitle.macro.macroID") + ": " + Integer.toString(this.properties.hashCodeForComparison()));

				getColorComboBox().setSelectedItem(properties.getColorKey());
				getHotKeyCombo().setSelectedItem(properties.getHotKey());
				getLabelTextField().setText(properties.getLabel());
				getGroupTextField().setText(properties.getGroup());
				getSortbyTextField().setText(properties.getSortby());
				getCommandTextArea().setText(properties.getCommand());
				getCommandTextArea().setCaretPosition(0);

				getAutoExecuteCheckBox().setSelected(properties.getAutoExecute());
				getIncludeLabelCheckBox().setSelected(properties.getIncludeLabel());
				getApplyToTokensCheckBox().setSelected(properties.getApplyToTokens());
				getFontColorComboBox().setSelectedItem(properties.getFontColorKey());
				getFontSizeComboBox().setSelectedItem(properties.getFontSize());
				getMinWidthTextField().setText(properties.getMinWidth());
				getMaxWidthTextField().setText(properties.getMaxWidth());
				getCompareGroupCheckBox().setSelected(properties.getCompareGroup());
				getCompareSortPrefixCheckBox().setSelected(properties.getCompareSortPrefix());
				getCompareCommandCheckBox().setSelected(properties.getCompareCommand());
				getCompareIncludeLabelCheckBox().setSelected(properties.getCompareIncludeLabel());
				getCompareAutoExecuteCheckBox().setSelected(properties.getCompareAutoExecute());
				getCompareApplyToSelectedTokensCheckBox().setSelected(properties.getCompareApplyToSelectedTokens());
				getAllowPlayerEditsCheckBox().setSelected(properties.getAllowPlayerEdits());
				getToolTipTextField().setText(properties.getToolTip());

				if (isCommonMacro) {
					getColorComboBox().setEnabled(false);
					getHotKeyCombo().setEnabled(false);
					getGroupTextField().setEnabled(properties.getCompareGroup());
					getSortbyTextField().setEnabled(properties.getCompareSortPrefix());
					getCommandTextArea().setEnabled(properties.getCompareCommand());
					getAutoExecuteCheckBox().setEnabled(properties.getCompareAutoExecute());
					getIncludeLabelCheckBox().setEnabled(properties.getCompareIncludeLabel());
					getApplyToTokensCheckBox().setEnabled(properties.getCompareApplyToSelectedTokens());
					getFontColorComboBox().setEnabled(false);
					getFontSizeComboBox().setEnabled(false);
					getMinWidthTextField().setEnabled(false);
					getMaxWidthTextField().setEnabled(false);
				}
				startingCompareGroup = properties.getCompareGroup();
				startingCompareSortPrefix = properties.getCompareSortPrefix();
				startingCompareCommand = properties.getCompareCommand();
				startingCompareAutoExecute = properties.getCompareAutoExecute();
				startingCompareIncludeLabel = properties.getCompareIncludeLabel();
				startingCompareApplyToSelectedTokens = properties.getCompareApplyToSelectedTokens();
				startingAllowPlayerEdits = properties.getAllowPlayerEdits();

				setVisible(true);
			} else {
				MapTool.showWarning(I18N.getText("msg.warning.macro.playerChangesNotAllowed"));
			}
		} else {
			MapTool.showError(I18N.getText("msg.error.macro.buttonPropsAreNull"));
		}
	}

	private void initCommandTextArea() {
		// Need to get rid of the tooltip, but abeille can't set it back to null, so we'll do it manually
		getCommandTextArea().setToolTipText(null);
	}

	private void save() {
		String hotKey = getHotKeyCombo().getSelectedItem().toString();
		button.getHotKeyManager().assignKeyStroke(hotKey);
		button.setColor(getColorComboBox().getSelectedItem().toString());
		button.setText(this.button.getButtonText());
		properties.setHotKey(hotKey);
		properties.setColorKey(getColorComboBox().getSelectedItem().toString());
		properties.setLabel(getLabelTextField().getText());
		properties.setGroup(getGroupTextField().getText());
		properties.setSortby(getSortbyTextField().getText());
		properties.setCommand(getCommandTextArea().getText());
		properties.setAutoExecute(getAutoExecuteCheckBox().isSelected());
		properties.setIncludeLabel(getIncludeLabelCheckBox().isSelected());
		properties.setApplyToTokens(getApplyToTokensCheckBox().isSelected());
		properties.setFontColorKey(getFontColorComboBox().getSelectedItem().toString());
		properties.setFontSize(getFontSizeComboBox().getSelectedItem().toString());
		properties.setMinWidth(getMinWidthTextField().getText());
		properties.setMaxWidth(getMaxWidthTextField().getText());
		properties.setCompareGroup(getCompareGroupCheckBox().isSelected());
		properties.setCompareSortPrefix(getCompareSortPrefixCheckBox().isSelected());
		properties.setCompareCommand(getCompareCommandCheckBox().isSelected());
		properties.setCompareIncludeLabel(getCompareIncludeLabelCheckBox().isSelected());
		properties.setCompareAutoExecute(getCompareAutoExecuteCheckBox().isSelected());
		properties.setCompareApplyToSelectedTokens(getCompareApplyToSelectedTokensCheckBox().isSelected());
		properties.setAllowPlayerEdits(getAllowPlayerEditsCheckBox().isSelected());
		properties.setToolTip(getToolTipTextField().getText());

		properties.save();

		if (button.getPanelClass().equals("SelectionPanel")) {
			if (MapTool.getFrame().getSelectionPanel().getCommonMacros().contains(button.getProperties())) {
				Boolean changeAllowPlayerEdits = false;
				Boolean endingAllowPlayerEdits = false;
				if (startingAllowPlayerEdits) {
					if (!properties.getAllowPlayerEdits()) {
						Boolean confirmDisallowPlayerEdits = MapTool.confirm(I18N.getText("confirm.macro.disallowPlayerEdits"));
						if (confirmDisallowPlayerEdits) {
							changeAllowPlayerEdits = true;
							endingAllowPlayerEdits = false;
						} else {
							properties.setAllowPlayerEdits(true);
						}
					}
				} else {
					if (properties.getAllowPlayerEdits()) {
						Boolean confirmAllowPlayerEdits = MapTool.confirm(I18N.getText("confirm.macro.allowPlayerEdits"));
						if (confirmAllowPlayerEdits) {
							changeAllowPlayerEdits = true;
							endingAllowPlayerEdits = true;
						} else {
							properties.setAllowPlayerEdits(false);
						}
					}
				}
				Boolean trusted = true;
				for (Token nextToken : MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokensList()) {
					if (AppUtil.playerOwns(nextToken)) {
						trusted = true;
					} else {
						trusted = false;
					}
					boolean isGM = MapTool.getPlayer().isGM();
					for (MacroButtonProperties nextMacro : nextToken.getMacroList(trusted)) {
						if (isGM || (!isGM && nextMacro.getApplyToTokens())) {
							if (nextMacro.hashCodeForComparison() == oldHashCode) {
								nextMacro.setLabel(properties.getLabel());
								if (properties.getCompareGroup() && startingCompareGroup) {
									nextMacro.setGroup(properties.getGroup());
								}
								if (properties.getCompareSortPrefix() && startingCompareSortPrefix) {
									nextMacro.setSortby(properties.getSortby());
								}
								if (properties.getCompareCommand() && startingCompareCommand) {
									nextMacro.setCommand(properties.getCommand());
								}
								if (properties.getCompareAutoExecute() && startingCompareAutoExecute) {
									nextMacro.setAutoExecute(properties.getAutoExecute());
								}
								if (properties.getCompareIncludeLabel() && startingCompareIncludeLabel) {
									nextMacro.setIncludeLabel(properties.getIncludeLabel());
								}
								if (properties.getCompareApplyToSelectedTokens() && startingCompareApplyToSelectedTokens) {
									nextMacro.setApplyToTokens(properties.getApplyToTokens());
								}
								if (changeAllowPlayerEdits) {
									nextMacro.setAllowPlayerEdits(endingAllowPlayerEdits);
								}
								nextMacro.setCompareGroup(properties.getCompareGroup());
								nextMacro.setCompareSortPrefix(properties.getCompareSortPrefix());
								nextMacro.setCompareCommand(properties.getCompareCommand());
								nextMacro.setCompareAutoExecute(properties.getCompareAutoExecute());
								nextMacro.setCompareIncludeLabel(properties.getCompareIncludeLabel());
								nextMacro.setCompareApplyToSelectedTokens(properties.getCompareApplyToSelectedTokens());
								nextMacro.save();
							}
						}
					}
				}
			}
			MapTool.getFrame().getSelectionPanel().reset();
		}
		if (button.getPanelClass().equals("CampaignPanel")) {
			MapTool.serverCommand().updateCampaignMacros(MapTool.getCampaign().getMacroButtonPropertiesArray());
			MapTool.getFrame().getCampaignPanel().reset();
		}
		setVisible(false);
		//		dispose();
	}

	private void cancel() {
		setVisible(false);
		//		dispose();
	}

	private JCheckBox getAutoExecuteCheckBox() {
		return panel.getCheckBox("autoExecuteCheckBox");
	}

	private JCheckBox getIncludeLabelCheckBox() {
		return panel.getCheckBox("includeLabelCheckBox");
	}

	private JCheckBox getApplyToTokensCheckBox() {
		return panel.getCheckBox("applyToTokensCheckBox");
	}

	private JComboBox getHotKeyCombo() {
		return panel.getComboBox("hotKey");
	}

	private JComboBox getColorComboBox() {
		return panel.getComboBox("colorComboBox");
	}

	private JTextField getLabelTextField() {
		return panel.getTextField("label");
	}

	private JTextField getGroupTextField() {
		return panel.getTextField("group");
	}

	private JTextField getSortbyTextField() {
		return panel.getTextField("sortby");
	}

	private JTextArea getCommandTextArea() {
		return (JTextArea) panel.getTextComponent("command");
	}

	private JComboBox getFontColorComboBox() {
		return panel.getComboBox("fontColorComboBox");
	}

	private JComboBox getFontSizeComboBox() {
		return panel.getComboBox("fontSizeComboBox");
	}

	private JTextField getMinWidthTextField() {
		return panel.getTextField("minWidth");
	}

	private JTextField getMaxWidthTextField() {
		return panel.getTextField("maxWidth");
	}

	private JCheckBox getAllowPlayerEditsCheckBox() {
		return panel.getCheckBox("allowPlayerEditsCheckBox");
	}

	private JTextField getToolTipTextField() {
		return panel.getTextField("toolTip");
	}

	// Begin comparison customization

	private JCheckBox getCompareIncludeLabelCheckBox() {
		return panel.getCheckBox("commonUseIncludeLabel");
	}

	private JCheckBox getCompareAutoExecuteCheckBox() {
		return panel.getCheckBox("commonUseAutoExecute");
	}

	private JCheckBox getCompareApplyToSelectedTokensCheckBox() {
		return panel.getCheckBox("commonUseApplyToSelectedTokens");
	}

	private JCheckBox getCompareGroupCheckBox() {
		return panel.getCheckBox("commonUseGroup");
	}

	private JCheckBox getCompareSortPrefixCheckBox() {
		return panel.getCheckBox("commonUseSortPrefix");
	}

	private JCheckBox getCompareCommandCheckBox() {
		return panel.getCheckBox("commonUseCommand");
	}

	// End comparison customization

	private void initI18NSupport() {
		panel.getTabbedPane("macroTabs").setTitleAt(0, I18N.getText("component.tab.macro.details"));
		panel.getTabbedPane("macroTabs").setTitleAt(1, I18N.getText("component.tab.macro.options"));
		panel.getLabel("macroLabelLabel").setText(I18N.getText("component.label.macro.label") + ":");
		getLabelTextField().setToolTipText(I18N.getText("component.tooltip.macro.label"));
		panel.getLabel("macroGroupLabel").setText(I18N.getText("component.label.macro.group") + ":");
		getGroupTextField().setToolTipText(I18N.getText("component.tooltip.macro.group"));
		panel.getLabel("macroSortPrefixLabel").setText(I18N.getText("component.label.macro.sortPrefix") + ":");
		getSortbyTextField().setToolTipText(I18N.getText("component.tooltip.macro.sortPrefix"));
		panel.getLabel("macroHotKeyLabel").setText(I18N.getText("component.label.macro.hotKey") + ":");
		getHotKeyCombo().setToolTipText(I18N.getText("component.tooltip.macro.hotKey"));
		panel.getLabel("macroCommandLabel").setText(I18N.getText("component.label.macro.command"));
		panel.getLabel("macroButtonColorLabel").setText(I18N.getText("component.label.macro.buttonColor") + ":");
		getColorComboBox().setToolTipText(I18N.getText("component.tooltip.macro.buttonColor"));
		panel.getLabel("macroFontColorLabel").setText(I18N.getText("component.label.macro.fontColor") + ":");
		getFontColorComboBox().setToolTipText(I18N.getText("component.tooltip.macro.fontColor"));
		panel.getLabel("macroFontSizeLabel").setText(I18N.getText("component.label.macro.fontSize") + ":");
		getFontSizeComboBox().setToolTipText(I18N.getText("component.tooltip.macro.fontSize"));
		panel.getLabel("macroMinWidthLabel").setText(I18N.getText("component.label.macro.minWidth") + ":");
		getMinWidthTextField().setToolTipText(I18N.getText("component.tooltip.macro.minWidth"));
		panel.getLabel("macroMaxWidthLabel").setText(I18N.getText("component.label.macro.maxWidth") + ":");
		getMaxWidthTextField().setToolTipText(I18N.getText("component.tooltip.macro.maxWidth"));
		panel.getLabel("macroToolTipLabel").setText(I18N.getText("component.label.macro.toolTip") + ":");
		getToolTipTextField().setToolTipText(I18N.getText("component.tooltip.macro.tooltip"));
		getIncludeLabelCheckBox().setText(I18N.getText("component.label.macro.includeLabel"));
		getIncludeLabelCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.includeLabel"));
		getAutoExecuteCheckBox().setText(I18N.getText("component.label.macro.autoExecute"));
		getAutoExecuteCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.autoExecute"));
		getApplyToTokensCheckBox().setText(I18N.getText("component.label.macro.applyToSelected"));
		getApplyToTokensCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.applyToSelected"));
		getAllowPlayerEditsCheckBox().setText(I18N.getText("component.label.macro.allowPlayerEdits"));
		getAllowPlayerEditsCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.allowPlayerEdits"));
		((TitledBorder) ((GridView) panel.getComponentByName("macroComparisonGridView")).getBorder()).setTitle(I18N.getText("component.label.macro.macroCommonality"));
		getCompareIncludeLabelCheckBox().setText(I18N.getText("component.label.macro.compareUseIncludeLabel"));
		getCompareIncludeLabelCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseIncludeLabel"));
		getCompareAutoExecuteCheckBox().setText(I18N.getText("component.label.macro.compareUseAutoExecute"));
		getCompareAutoExecuteCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseAutoExecute"));
		getCompareApplyToSelectedTokensCheckBox().setText(I18N.getText("component.label.macro.compareApplyToSelected"));
		getCompareApplyToSelectedTokensCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseApplyToSelected"));
		getCompareGroupCheckBox().setText(I18N.getText("component.label.macro.compareUseGroup"));
		getCompareGroupCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseGroup"));
		getCompareSortPrefixCheckBox().setText(I18N.getText("component.label.macro.compareUseSortPrefix"));
		getCompareSortPrefixCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseSortPrefix"));
		getCompareCommandCheckBox().setText(I18N.getText("component.label.macro.compareUseCommand"));
		getCompareCommandCheckBox().setToolTipText(I18N.getText("component.tooltip.macro.compareUseCommand"));
	}
}
