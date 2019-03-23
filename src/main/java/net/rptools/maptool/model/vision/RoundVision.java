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
package net.rptools.maptool.model.vision;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Vision;
import net.rptools.maptool.model.Zone;

public class RoundVision extends Vision {
  public RoundVision() {}

  public RoundVision(int distance) {
    setDistance(distance);
  }

  @Override
  public Anchor getAnchor() {
    return Vision.Anchor.CENTER;
  }

  @Override
  protected Area createArea(Zone zone, Token token) {
    int size = getDistance() * getZonePointsPerCell(zone) * 2;
    int half = size / 2;
    Area area = new Area(new Ellipse2D.Double(-half, -half, size, size));

    return area;
  }

  @Override
  public String toString() {
    return "Round";
  }
}
