/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.drawing;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

/** An oval. */
public class Oval extends Rectangle {
  /**
   * @param x
   * @param y
   */
  public Oval(int x, int y, int width, int height) {
    super(x, y, width, height);
  }

  @Override
  protected void draw(Graphics2D g) {
    int minX = Math.min(startPoint.x, endPoint.x);
    int minY = Math.min(startPoint.y, endPoint.y);

    int width = Math.abs(startPoint.x - endPoint.x);
    int height = Math.abs(startPoint.y - endPoint.y);

    g.drawOval(minX, minY, width, height);
  }

  @Override
  protected void drawBackground(Graphics2D g) {
    int minX = Math.min(startPoint.x, endPoint.x);
    int minY = Math.min(startPoint.y, endPoint.y);

    int width = Math.abs(startPoint.x - endPoint.x);
    int height = Math.abs(startPoint.y - endPoint.y);

    g.fillOval(minX, minY, width, height);
  }

  @Override
  public Area getArea() {
    java.awt.Rectangle r = getBounds();
    return new Area(new Ellipse2D.Double(r.x, r.y, r.width, r.height));
  }
}
