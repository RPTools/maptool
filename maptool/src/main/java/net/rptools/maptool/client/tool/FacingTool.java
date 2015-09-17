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

package net.rptools.maptool.client.tool;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.TokenUtil;

/**
 */
public class FacingTool extends DefaultTool {
	private static final long serialVersionUID = -2807604658989763950L;

	// TODO: This shouldn't be necessary, just get it from the renderer
	private Token tokenUnderMouse;
	private Set<GUID> selectedTokenSet;

	public FacingTool() {
		// Non tool-bar tool ... atm
	}

	public void init(Token keyToken, Set<GUID> selectedTokenSet) {
		tokenUnderMouse = keyToken;
		this.selectedTokenSet = selectedTokenSet;
	}

	@Override
	public String getTooltip() {
		return "tool.facing.tooltip";
	}

	@Override
	public String getInstructions() {
		return "tool.facing.instructions";
	}

	@Override
	protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
		super.installKeystrokes(actionMap);

		actionMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (MapTool.confirm(I18N.getText("msg.confirm.removeFacings"))) {
					for (GUID tokenGUID : renderer.getSelectedTokenSet()) {
						Token token = renderer.getZone().getToken(tokenGUID);
						if (token == null) {
							continue;
						}
						token.setFacing(null);
						renderer.flush(token);
					}
					// Go back to the pointer tool
					resetTool();
				}
			}
		});
	}

	////
	// MOUSE
	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);

		if (tokenUnderMouse == null || renderer.getTokenBounds(tokenUnderMouse) == null) {
			return;
		}
		Rectangle bounds = renderer.getTokenBounds(tokenUnderMouse).getBounds();

		int x = bounds.x + bounds.width / 2;
		int y = bounds.y + bounds.height / 2;

		double angle = Math.atan2(y - e.getY(), e.getX() - x);

		int degrees = (int) Math.toDegrees(angle);

		if (!SwingUtil.isControlDown(e)) {
			int[] facingAngles = renderer.getZone().getGrid().getFacingAngles();
			degrees = facingAngles[TokenUtil.getIndexNearestTo(facingAngles, degrees)];
		}
		Area visibleArea = null;
		Set<GUID> remoteSelected = new HashSet<GUID>();
		for (GUID tokenGUID : selectedTokenSet) {
			Token token = renderer.getZone().getToken(tokenGUID);
			if (token == null) {
				continue;
			}
			token.setFacing(degrees);
			// if has fog(required) 
			// and ((isGM with pref set) OR serverPolicy allows auto reveal by players)
			if (renderer.getZone().hasFog() && ((AppPreferences.getAutoRevealVisionOnGMMovement() && MapTool.getPlayer().isGM())) || MapTool.getServerPolicy().isAutoRevealOnMovement()) {
				visibleArea = renderer.getZoneView().getVisibleArea(token);
				remoteSelected.add(token.getId());
				renderer.getZone().exposeArea(visibleArea, token);
			}
			renderer.flushFog();
		}
		// XXX Instead of calling exposeFoW() when visibleArea is null, shouldn't we just skip it?
		MapTool.serverCommand().exposeFoW(renderer.getZone().getId(), visibleArea == null ? new Area() : visibleArea, remoteSelected);
		renderer.repaint(); // TODO: shrink this
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Commit
		for (GUID tokenGUID : selectedTokenSet) {
			Token token = renderer.getZone().getToken(tokenGUID);
			if (token == null) {
				continue;
			}
			renderer.flush(token);
			MapTool.serverCommand().putToken(renderer.getZone().getId(), token);
		}
		// Go back to the pointer tool
		resetTool();
	}

	@Override
	protected void resetTool() {
		if (tokenUnderMouse.isStamp()) {
			MapTool.getFrame().getToolbox().setSelectedTool(StampTool.class);
		} else {
			MapTool.getFrame().getToolbox().setSelectedTool(PointerTool.class);
		}
	}
}
