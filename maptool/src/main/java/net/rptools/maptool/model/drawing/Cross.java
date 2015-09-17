/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.rptools.maptool.model.drawing;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Area;

/**
 * An Cross
 */
public class Cross extends AbstractDrawing {
	protected Point startPoint;
	protected Point endPoint;
	private transient java.awt.Rectangle bounds;

	public Cross(int startX, int startY, int endX, int endY) {

		startPoint = new Point(startX, startY);
		endPoint = new Point(endX, endY);
	}

	public Area getArea() {
		return new Area(getBounds());
	}

	/* (non-Javadoc)
	 * @see net.rptools.maptool.model.drawing.Drawable#getBounds()
	 */
	public java.awt.Rectangle getBounds() {

		if (bounds == null) {
			int x = Math.min(startPoint.x, endPoint.x);
			int y = Math.min(startPoint.y, endPoint.y);
			int width = Math.abs(endPoint.x - startPoint.x);
			int height = Math.abs(endPoint.y - startPoint.y);

			bounds = new java.awt.Rectangle(x, y, width, height);
		}

		return bounds;
	}

	public Point getStartPoint() {
		return startPoint;
	}

	public Point getEndPoint() {
		return endPoint;
	}

	protected void draw(Graphics2D g) {

		int minX = Math.min(startPoint.x, endPoint.x);
		int minY = Math.min(startPoint.y, endPoint.y);

		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		//g.drawRect(minX, minY, width, height);

		g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		g.drawLine(startPoint.x, endPoint.y, endPoint.x, startPoint.y);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
	}

	protected void drawBackground(Graphics2D g) {
		int minX = Math.min(startPoint.x, endPoint.x);
		int minY = Math.min(startPoint.y, endPoint.y);

		int width = Math.abs(startPoint.x - endPoint.x);
		int height = Math.abs(startPoint.y - endPoint.y);

		Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g.fillRect(minX, minY, width, height);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
	}
}
