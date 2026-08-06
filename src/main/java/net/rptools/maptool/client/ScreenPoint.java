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
package net.rptools.maptool.client;

import java.awt.geom.Point2D;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.model.ZonePoint;

public class ScreenPoint extends Point2D.Double {
  public ScreenPoint(double x, double y) {
    super(x, y);
  }

  /**
   * Translate the point from screen x,y to zone x,y.
   *
   * @param zoneScale the "screen view" of the {@link net.rptools.maptool.model.Zone}.
   * @param x the x screen co-ordinate.
   * @param y the y screen co-ordinate.
   * @return the {@link ZonePoint} representing the screen point.
   */
  private static ZonePoint convertToZone(Scale zoneScale, double x, double y) {
    var doublePrecision = zoneScale.toWorldSpace(x, y);
    return new ZonePoint(
        (int) Math.floor(doublePrecision.getX()), (int) Math.floor(doublePrecision.getY()));
  }

  /**
   * Translate the point from screen x,y to zone x,y.
   *
   * @param scale the "screen view" of the {@link net.rptools.maptool.model.Zone}
   * @return the {@link ZonePoint} representing the screen point.
   */
  public ZonePoint convertToZone(Scale scale) {
    return convertToZone(scale, this.x, this.y);
  }

  /**
   * Translate the point from screen x,y to the nearest top left corner of a zone x,y for when the
   * zone point required is on the "zone point grid" as opposed to the area of zone space designated
   * by the zone point.
   *
   * @param zoneScale the "screen view" of the {@link net.rptools.maptool.model.Zone}.
   * @return the {@link ZonePoint} representing the screen point.
   */
  public ZonePoint convertToZoneRnd(Scale zoneScale) {
    double rndAdj = 0.5 * zoneScale.getScale();
    return convertToZone(zoneScale, this.x + rndAdj, this.y + rndAdj);
  }

  /**
   * Converts a ZonePoint to a screen coordinate (ScreenPoint) and rounding both axis values to
   * longs.
   *
   * @param zoneScale the "screen view" of the {@link net.rptools.maptool.model.Zone}.
   * @param x X axis coordinate
   * @param y Y axis coordinate
   * @return new ScreenPoint
   */
  public static ScreenPoint fromZonePointRnd(Scale zoneScale, double x, double y) {
    ScreenPoint sp = zoneScale.toScreenSpace(x, y);
    sp.x = Math.round(sp.x);
    sp.y = Math.round(sp.y);
    return sp;
  }

  @Override
  public String toString() {
    return "ScreenPoint" + super.toString();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object pt) {
    if (!(pt instanceof ScreenPoint)) return false;
    ScreenPoint spt = (ScreenPoint) pt;
    return spt.x == x && spt.y == y;
  }

  public void translate(int dx, int dy) {
    x += dx;
    y += dy;
  }
}
