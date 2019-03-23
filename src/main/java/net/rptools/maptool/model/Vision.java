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
package net.rptools.maptool.model;

import java.awt.geom.Area;

public abstract class Vision {
  public enum Anchor {
    CORNER,
    CENTER
  }

  protected String name;
  protected int distance;
  private int angle; // degrees
  private boolean enabled = true;
  private int lastGridSize;
  private transient Area area;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /** The Vision's name */
  public void setName(String name) {
    this.name = name;
  }

  /** The Vision's name */
  public String getName() {
    return name;
  }

  /** The Vision's menu label */
  public String getLabel() {
    return "<html>"
        + (name != null ? name : "")
        + " <font size=-2>("
        + this
        + "-"
        + Integer.toString(distance)
        + ")</font></html>";
  }

  /** Angle in degrees */
  public int getAngle() {
    return angle;
  }

  /** Angle in degrees */
  public void setAngle(int angle) {
    this.angle = angle;
    flush();
  }

  /** Distance in zone points */
  public int getDistance() {
    return distance;
  }

  /**
   * Distance in zone points
   *
   * @param distance
   */
  public void setDistance(int distance) {
    this.distance = distance;
    flush();
  }

  /** Get the area shape that this vision represents, in zone points, centered on the origin x,y */
  public Area getArea(Zone zone, Token token) {
    if (area == null || lastGridSize != zone.getGrid().getSize()) {
      area = createArea(zone, token);
    }
    lastGridSize = zone.getGrid().getSize();
    return area;
  }

  /** Specific vision types must be able to create the shape they represent */
  protected abstract Area createArea(Zone zone, Token token);

  // This won't be abstract when anchor points are fleshed out, but rather a field on this class
  public abstract Anchor getAnchor();

  /** Flush the cached vision information */
  protected void flush() {
    area = null;
  }

  protected int getZonePointsPerCell(Zone zone) {
    return (int) (zone.getGrid().getSize() / zone.getUnitsPerCell());
  }
}
