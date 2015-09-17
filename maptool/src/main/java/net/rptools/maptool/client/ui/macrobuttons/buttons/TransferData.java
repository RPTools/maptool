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

package net.rptools.maptool.client.ui.macrobuttons.buttons;

import java.io.Serializable;

import net.rptools.maptool.model.MacroButtonProperties;

public class TransferData implements Serializable {

	public int index = 0;
	public String command = "";
	public String colorKey = "";
	public String hotKey = "";
	public String label = "";
	public String group = "";
	public String sortby = "";
	public boolean autoExecute = true;
	public boolean includeLabel = false;
	public boolean applyToTokens = true;
	public String fontColorKey = "";
	public String fontSize = "";
	public String minWidth = "";
	public String maxWidth = "";
	public String panelClass = "";
	public String toolTip = "";

	public TransferData(MacroButton button) {
		MacroButtonProperties prop = button.getProperties();
		this.index = prop.getIndex();
		this.label = prop.getLabel();
		this.command = prop.getCommand();
		this.colorKey = prop.getColorKey();
		this.hotKey = prop.getHotKey();
		this.group = prop.getGroup();
		this.sortby = prop.getSortby();
		this.autoExecute = prop.getAutoExecute();
		this.includeLabel = prop.getIncludeLabel();
		this.applyToTokens = prop.getApplyToTokens();
		this.panelClass = button.getPanelClass();
		this.fontColorKey = prop.getFontColorKey();
		this.fontSize = prop.getFontSize();
		this.minWidth = prop.getMinWidth();
		this.maxWidth = prop.getMaxWidth();
		this.toolTip = prop.getToolTip();
	}

}
