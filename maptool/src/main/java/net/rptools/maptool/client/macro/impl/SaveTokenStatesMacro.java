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

package net.rptools.maptool.client.macro.impl;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.ui.token.BooleanTokenOverlay;
import net.rptools.maptool.language.I18N;

/**
 * Save the current list of token states for use later.
 * 
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
@MacroDefinition(
		name = "savetokenstates",
		aliases = { "tss" },
		description = "savetokenstates.description")
public class SaveTokenStatesMacro implements Macro {
	/**
	 * @see net.rptools.maptool.client.macro.Macro#execute(java.lang.String)
	 */
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		// Read the file from the command line
		File aliasFile = null;
		if (macro.length() > 0) {
			aliasFile = new File(macro);
		} else {
			// Not on the command line, ask the user for a file.
			JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
			chooser.setDialogTitle(I18N.getText("savetokenstates.dialogTitle"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION)
				return;
			aliasFile = chooser.getSelectedFile();
		} // endif

		// Make it an XML file if type isn't set, check for overwrite
		if (aliasFile.getName().indexOf(".") < 0)
			aliasFile = new File(aliasFile.getAbsolutePath() + "-tokenStates.xml");
		if (aliasFile.exists() && !MapTool.confirm(I18N.getText("msg.confirm.fileExists")))
			return;

		// Save the file using a decoder
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(aliasFile)));
			List<BooleanTokenOverlay> overlays = new ArrayList<BooleanTokenOverlay>();
			for (String overlay : MapTool.getCampaign().getTokenStatesMap().keySet()) {
				overlays.add(MapTool.getCampaign().getTokenStatesMap().get(overlay));
			} // endfor
			encoder.writeObject(overlays);
			encoder.close();
			MapTool.addLocalMessage(I18N.getText("savetokenstates.saved", overlays.size()));
		} catch (FileNotFoundException fnfe) {
			MapTool.addLocalMessage(I18N.getText("savetokenstates.couldNotSave", I18N.getText("msg.error.fileNotFound")));
		} // endif
	}
}
