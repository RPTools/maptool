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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Set;

import javax.swing.SwingUtilities;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.Tool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.TokenUtil;

/**
 */
public abstract class DefaultTool extends Tool implements MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 3258411729238372921L;

	private boolean isDraggingMap;
	private int dragStartX;
	private int dragStartY;

	protected int mouseX;
	protected int mouseY;

	// This is to manage overflowing of map move events (keep things snappy)
	private long lastMoveRedraw;
	private int mapDX, mapDY;
	private static final int REDRAW_DELAY = 25; // millis

	protected ZoneRenderer renderer;

	@Override
	protected void attachTo(ZoneRenderer renderer) {
		super.attachTo(renderer);
		this.renderer = renderer;
	}

	@Override
	protected void detachFrom(ZoneRenderer renderer) {
		this.renderer = null;
		super.detachFrom(renderer);
	}

	public boolean isDraggingMap() {
		return isDraggingMap;
	}

	protected void repaintZone() {
		renderer.repaint();
	}

	protected Zone getZone() {
		return renderer.getZone();
	}

	////
	// Mouse
	public void mousePressed(MouseEvent e) {
		// Potential map dragging
		if (SwingUtilities.isRightMouseButton(e)) {
			dragStartX = e.getX();
			dragStartY = e.getY();
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (isDraggingMap && SwingUtilities.isRightMouseButton(e)) {
			renderer.maybeForcePlayersView();
		}
		// Cleanup
		isDraggingMap = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e) {
	}

	////
	// MouseMotion
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e) {
		if (renderer == null) {
			return;
		}
		mouseX = e.getX();
		mouseY = e.getY();

		CellPoint cp = getZone().getGrid().convert(new ScreenPoint(mouseX, mouseY).convertToZone(renderer));
		if (cp != null) {
			MapTool.getFrame().getCoordinateStatusBar().update(cp.x, cp.y);
		} else {
			MapTool.getFrame().getCoordinateStatusBar().clear();
		}
	}

	public void mouseDragged(MouseEvent e) {
		int mX = e.getX();
		int mY = e.getY();
		CellPoint cellUnderMouse = renderer.getCellAt(new ScreenPoint(mX, mY));
		if (cellUnderMouse != null) {
			MapTool.getFrame().getCoordinateStatusBar().update(cellUnderMouse.x, cellUnderMouse.y);
		} else {
			MapTool.getFrame().getCoordinateStatusBar().clear();
		}
		// MAP MOVEMENT
		if (SwingUtilities.isRightMouseButton(e)) {
			isDraggingMap = true;

			mapDX += mX - dragStartX;
			mapDY += mY - dragStartY;

			dragStartX = mX;
			dragStartY = mY;

			long now = System.currentTimeMillis();
			if (now - lastMoveRedraw > REDRAW_DELAY) {
				// TODO: does it matter to capture the last map move in the series ?
				// TODO: This should probably be genericized and put into ZoneRenderer to prevent over zealous repainting
				renderer.moveViewBy(mapDX, mapDY);
				mapDX = 0;
				mapDY = 0;
				lastMoveRedraw = now;
			}
		}
	}

	////
	// Mouse Wheel
	public void mouseWheelMoved(MouseWheelEvent e) {
		// Fix for High Resolution Mouse Wheels
		if (e.getWheelRotation() == 0) {
			return;
		}

		// QUICK ROTATE
		if (SwingUtil.isShiftDown(e)) {
			Set<GUID> tokenGUIDSet = renderer.getSelectedTokenSet();
			if (tokenGUIDSet.isEmpty()) {
				return;
			}
			for (GUID tokenGUID : tokenGUIDSet) {
				Token token = getZone().getToken(tokenGUID);
				if (token == null) {
					continue;
				}
				if (!AppUtil.playerOwns(token)) {
					continue;
				}
				Integer facing = token.getFacing();
				if (facing == null) {
					facing = -90; // natural alignment
				}
				if (SwingUtil.isControlDown(e)) {
					facing += e.getWheelRotation() > 0 ? 5 : -5;
				} else {
					int[] facingArray = getZone().getGrid().getFacingAngles();
					int facingIndex = TokenUtil.getIndexNearestTo(facingArray, facing);

					facingIndex += e.getWheelRotation() > 0 ? 1 : -1;
					if (facingIndex < 0) {
						facingIndex = facingArray.length - 1;
					}
					if (facingIndex == facingArray.length) {
						facingIndex = 0;
					}
					facing = facingArray[facingIndex];
				}
				token.setFacing(facing);

				renderer.flush(token);
				MapTool.serverCommand().putToken(getZone().getId(), token);
			}
			repaintZone();
			return;
		}
		// ZOOM
		if (!AppState.isZoomLocked()) {
			boolean direction = e.getWheelRotation() > 0;
			direction = isKeyDown('z') ? !direction : direction;
			if (direction) {
				renderer.zoomOut(e.getX(), e.getY());
			} else {
				renderer.zoomIn(e.getX(), e.getY());
			}
			renderer.maybeForcePlayersView();
		}
	}

	@Override
	protected void resetTool() {
		MapTool.getFrame().getToolbox().setSelectedTool(PointerTool.class);
	}
}
