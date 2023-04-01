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
package net.rptools.maptool.client.ui.zone;

import java.awt.geom.Area;
import net.rptools.maptool.model.drawing.DrawablePaint;

public class DrawableLight {

  private DrawablePaint paint;
  private Area area;
  private int lumens;

  public DrawableLight(DrawablePaint paint, Area area, int lumens) {
    super();
    this.paint = paint;
    this.area = area;
    this.lumens = lumens;
  }

  public DrawablePaint getPaint() {
    return paint;
  }

  public Area getArea() {
    return area;
  }

  public int getLumens() {
    return lumens;
  }

  @Override
  public String toString() {
    return "DrawableLight[" + area.getBounds() + ", " + paint.getClass().getName() + "]";
  }
}
