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

package net.rptools.maptool.client.ui.macrobuttons.panels;

public enum Tab {
	GLOBAL(0, "Global"), CAMPAIGN(1, "Campaign"), SELECTED(2, "Selection"), IMPERSONATED(3, "Impersonated");

	public final int index;
	public final String title;

	Tab(int index, String title) {
		this.index = index;
		this.title = title;
	}
}
