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

import net.rptools.maptool.server.proto.PointerDto;
import net.rptools.maptool.server.proto.drawing.IntPointDto;

/** Represents a player pointer on the screen */
public class Pointer {

  public enum Type {
    ARROW,
    SPEECH_BUBBLE,
    THOUGHT_BUBBLE,
    LOOK_HERE
  }

  private GUID zoneGUID;
  private int x;
  private int y;
  private double direction; //
  private String type;

  private Pointer() {}

  public Pointer(Zone zone, int x, int y, double direction, Type type) {
    this.zoneGUID = zone.getId();
    this.x = x;
    this.y = y;
    this.direction = direction;
    this.type = type.name();
  }

  public String toString() {
    return x + "." + y + "-" + direction;
  }

  public Type getType() {
    return type != null ? Type.valueOf(type) : Type.ARROW;
  }

  public GUID getZoneGUID() {
    return zoneGUID;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  public double getDirection() {
    return direction;
  }

  public static Pointer fromDto(PointerDto dto) {
    var pointer = new Pointer();
    pointer.direction = dto.getDirection();
    pointer.x = dto.getPoint().getX();
    pointer.y = dto.getPoint().getY();
    pointer.zoneGUID = GUID.valueOf(dto.getZoneGuid());
    pointer.type = dto.getType();
    return pointer;
  }

  public PointerDto toDto() {
    var dto = PointerDto.newBuilder();
    dto.setDirection(direction);
    dto.setPoint(IntPointDto.newBuilder().setX(x).setY(y).build());
    dto.setZoneGuid(zoneGUID.toString());
    dto.setType(type);
    return dto.build();
  }
}
