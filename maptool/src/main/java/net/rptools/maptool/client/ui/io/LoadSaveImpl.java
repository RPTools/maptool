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

package net.rptools.maptool.client.ui.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneMiniMapPanel;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Zone;

/**
 * @author crash
 *
 */
public class LoadSaveImpl {
	private static final Logger log = Logger.getLogger(LoadSaveImpl.class);

	private Campaign cmpgn;
	private UIBuilder.TreeModel model;
	private static Map<String, DataTemplate> registry;

	static {
		registry = new HashMap<String, DataTemplate>();
	}

	/**
	 * <p>
	 * This method is used by other subsystems to register with us so we know they exist!
	 * This allows us to call them when the user wants to load/save their data.
	 * </p>
	 * 
	 * @param dt the {@code DataTemplate} object that represents the subsystem
	 */
	public static void addToRegistry(DataTemplate dt) {
		registry.put(dt.getSubsystemName(), dt);
	}

	public void saveApplication() {
		cmpgn = MapTool.getCampaign();

		// Create the load/save dialog form.
		// Populate it with maps, tables, macros, and so forth.
		// Display it to the user with either a LOAD or SAVE button.
		// When they're done, write the selected components out.
		UIBuilder form = new UIBuilder(MapTool.getFrame());
		model = form.getTreeModel();
		addToRegistry(new DataTemplate() {
			public String getSubsystemName() {
				return "built-in";
			}

			public void prepareForDisplay() {
				addDataObjects("Campaign/Properties/Token Properties", cmpgn.getTokenTypeMap());
				addDataObjects("Campaign/Properties/Repositories", cmpgn.getRemoteRepositoryList());
				addDataObjects("Campaign/Properties/Sights", cmpgn.getSightTypeMap());
				addDataObjects("Campaign/Properties/Lights", cmpgn.getLightSourcesMap());
				addDataObjects("Campaign/Properties/States", cmpgn.getTokenStatesMap());
				addDataObjects("Campaign/Properties/Bars", cmpgn.getTokenBarsMap());
				addDataObjects("Campaign/Properties/Tables", cmpgn.getLookupTableMap());
				addDataObjects("Campaign/CampaignMacros", cmpgn.getMacroButtonPropertiesArray());
				addDataObjects("Campaign/Maps", cmpgn.getZones());
			}
		});
		for (Iterator<String> iter = registry.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			DataTemplate dt = (DataTemplate) registry.get(name);
			dt.populateModel(model);
		}
		form.expandAndSelectAll(true);
		form.pack();
		form.setVisible(true);
		if (form.getStatus() == JOptionPane.OK_OPTION) {
			// Clicked OK to perform a load/save operation.
		}
	}

	private void addMacros(UIBuilder.TreeModel model) {
		List<MacroButtonProperties> macros = cmpgn.getMacroButtonPropertiesArray();
		Map<String, MacroButtonProperties> global = new HashMap<String, MacroButtonProperties>();
		Map<String, MacroButtonProperties> campaign = new HashMap<String, MacroButtonProperties>();
		Map<String, MacroButtonProperties> other = new HashMap<String, MacroButtonProperties>();

		for (MacroButtonProperties macro : macros) {
			String loc = macro.getSaveLocation();
			if (loc.equals("GlobalPanel"))
				global.put(macro.getLabel(), macro);
			else if (loc.equals("CampaignPanel"))
				campaign.put(macro.getLabel(), macro);
			else {
				log.debug("Ignoring " + loc + " macro button property");
				other.put(loc, macro);
			}
		}
		oneMacroCategory("Campaign/Properties/Macros/Global", global, model);
		oneMacroCategory("Campaign/Properties/Macros/Campaign", campaign, model);
		oneMacroCategory("Campaign/Properties/Macros/Other", other, model);
	}

	private void oneMacroCategory(String where, Map<String, MacroButtonProperties> map, UIBuilder.TreeModel model) {
		Set<String> set = map.keySet();
		String[] names = new String[set.size()];
		set.toArray(names);
		Arrays.sort(names);

		for (int index = 0; index < names.length; index++) {
			MacroButtonProperties macro = map.get(names[index]);
			model.addNode(where, new MaptoolNode(names[index], macro));
		}
	}
}
