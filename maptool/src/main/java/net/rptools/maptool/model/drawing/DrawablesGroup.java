/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model.drawing;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.List;

/**
 * 
 * @author Jagged
 * 
 * A grouping of DrawnElements to create a mini-layer like effect 
 *
 */
public class DrawablesGroup extends AbstractDrawing {
	private List<DrawnElement> drawableList;

	public DrawablesGroup(List<DrawnElement> drawableList) {
		this.drawableList = drawableList;
	}
	
	@Override
	public Rectangle getBounds() {
		Rectangle bounds = null;
		for (DrawnElement element : drawableList) {
			if (bounds==null)
				bounds = element.getDrawable().getBounds();
			else
				bounds.add(element.getDrawable().getBounds());
		}
		return bounds;
	}

	@Override
	public Area getArea() {
		Area area = null;
		for (DrawnElement element : drawableList) {
			if (area==null)
				area = element.getDrawable().getArea();
			else
				area.add(element.getDrawable().getArea());;
		}
		return area;
	}

	@Override
	protected void draw(Graphics2D g) {
		Graphics2D newG = (Graphics2D) g.create();
		for (DrawnElement element : drawableList) {
			element.getDrawable().draw(newG, element.getPen());
		}
		newG.dispose();
	}

	@Override
	protected void drawBackground(Graphics2D g) {
		// Do nothing
	}

}
