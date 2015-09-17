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

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;

import net.rptools.maptool.client.MapTool;

public class MapToolFocusTraversalPolicy extends FocusTraversalPolicy {

	@Override
	public Component getComponentAfter(Container aContainer,
			Component aComponent) {
		return MapTool.getFrame().getCurrentZoneRenderer();
	}

	@Override
	public Component getComponentBefore(Container aContainer,
			Component aComponent) {
		return MapTool.getFrame().getCurrentZoneRenderer();
	}

	@Override
	public Component getFirstComponent(Container aContainer) {
		return MapTool.getFrame().getCurrentZoneRenderer();
	}

	@Override
	public Component getLastComponent(Container aContainer) {
		return MapTool.getFrame().getCurrentZoneRenderer();
	}

	@Override
	public Component getDefaultComponent(Container aContainer) {
		return MapTool.getFrame().getCurrentZoneRenderer();
	}

}
