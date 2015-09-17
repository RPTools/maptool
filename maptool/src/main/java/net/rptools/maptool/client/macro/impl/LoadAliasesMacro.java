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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;

import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.language.I18N;

@MacroDefinition(
		name = "loadaliases",
		aliases = {},
		description = "loadaliases.description")
public class LoadAliasesMacro implements Macro {
	public void execute(MacroContext context, String macro, MapToolMacroContext executionContext) {
		File aliasFile = null;
		if (macro.length() > 0) {
			aliasFile = new File(macro);
		} else {
			JFileChooser chooser = MapTool.getFrame().getLoadFileChooser();
			chooser.setDialogTitle(I18N.getText("loadaliases.dialogTitle"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			if (chooser.showOpenDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			aliasFile = chooser.getSelectedFile();
		}
		if (aliasFile.getName().indexOf(".") < 0) {
			aliasFile = new File(aliasFile.getAbsolutePath() + ".alias");
		}
		if (!aliasFile.exists()) {
			MapTool.addLocalMessage(I18N.getText("loadaliases.cantFindFile", aliasFile));
			return;
		}

		try {
			MapTool.addLocalMessage(I18N.getText("loadalises.loading"));
			List<String> lineList = FileUtil.getLines(aliasFile);

			for (String line : lineList) {
				line = line.trim();
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				// Split into components
				String name = line;
				String value = null;
				int split = line.indexOf(":");
				if (split > 0) {
					name = line.substring(0, split);
					value = line.substring(split + 1).trim();
				}
				if (value != null) {
					MapTool.addLocalMessage("&nbsp;&nbsp;&nbsp;'" + name + "'");
					MacroManager.setAlias(name, value);
				} else {
					MapTool.addLocalMessage("&nbsp;&nbsp;&nbsp;" + I18N.getText("loadaliases.ignoring", name));
				}
			}
		} catch (FileNotFoundException fnfe) {
			MapTool.addLocalMessage(I18N.getText("loadaliases.couldNotLoad", I18N.getText("msg.error.fileNotFound")));
		} catch (IOException ioe) {
			MapTool.addLocalMessage("loadaliases.couldNotLoad" + ioe);
		}
	}
}
