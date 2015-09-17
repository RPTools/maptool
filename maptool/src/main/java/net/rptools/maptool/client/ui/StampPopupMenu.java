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

import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

public class StampPopupMenu extends AbstractTokenPopupMenu {
	private static final long serialVersionUID = -1355308966041002520L;

	public StampPopupMenu(Set<GUID> selectedTokenSet, int x, int y, ZoneRenderer renderer, Token tokenUnderMouse) {
		super(selectedTokenSet, x, y, renderer, tokenUnderMouse);

		add(new SetFacingAction());
		add(new ClearFacingAction());
		add(new StartMoveAction());
		add(createFlipMenu());
		add(createSizeMenu());
		add(createArrangeMenu());
		add(createChangeToMenu(Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND));
		if (getTokenUnderMouse().getCharsheetImage() != null && AppUtil.playerOwns(getTokenUnderMouse())) {
			add(new ShowHandoutAction());
		}
		add(new JSeparator());
		addOwnedItem(createLightSourceMenu());
		add(new JSeparator());

		addToggledItem(new SnapToGridAction(tokenUnderMouse.isSnapToGrid(), renderer), tokenUnderMouse.isSnapToGrid());
		addToggledGMItem(new VisibilityAction(), tokenUnderMouse.isVisible());

		add(new JSeparator());

		add(new JMenuItem(new AutoResizeAction()));
		add(new JSeparator());

		add(new JMenuItem(new CutAction()));
		add(new JMenuItem(new CopyAction()));
		add(new JMenuItem(new DeleteAction()));

		add(new JSeparator());

		add(new ShowPropertiesDialogAction());
		add(new SaveAction());
	}
}
