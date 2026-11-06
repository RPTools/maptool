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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public enum GridType {
  Square,
  Isometric,
  HexVertical,
  HexHorizontal,
  None;

  public boolean isHex() {
    return this == HexVertical || this == HexHorizontal;
  }

  public boolean isIsometric() {
    return this == Isometric;
  }

  public boolean isNone() {
    return this == None;
  }

  public Shape createGridShape(double size) {
    return switch (this) {
      case Square -> {
        var path = new Path2D.Double();
        path.moveTo(size / 2, size / 2);
        path.lineTo(-size / 2, size / 2);
        path.lineTo(-size / 2, -size / 2);
        path.lineTo(size / 2, -size / 2);
        path.closePath();
        yield path;
      }
      case Isometric -> {
        var path = new Path2D.Double();
        path.moveTo(size, 0.);
        path.lineTo(0., size / 2);
        path.lineTo(-size, 0.);
        path.lineTo(0, -size / 2);
        path.closePath();
        yield path;
      }
      case HexHorizontal -> {
        final double l = size / Math.sqrt(3.);
        var path = new Path2D.Double();
        path.moveTo(size / 2, l / 2);
        path.lineTo(0, l);
        path.lineTo(-size / 2, l / 2);
        path.lineTo(-size / 2, -l / 2);
        path.lineTo(0, -l);
        path.lineTo(size / 2, -l / 2);
        path.closePath();
        yield path;
      }
      case HexVertical -> {
        final double l = size / Math.sqrt(3.);
        var path = new Path2D.Double();
        path.moveTo(l, 0);
        path.lineTo(l / 2, size / 2);
        path.lineTo(-l / 2, size / 2);
        path.lineTo(-l, 0);
        path.lineTo(-l / 2, -size / 2);
        path.lineTo(l / 2, -size / 2);
        path.closePath();
        yield path;
      }
      case None -> new Ellipse2D.Double(-size / 2, -size / 2, size, size);
    };
  }

  public String toString() {
    return switch (this) {
      case Square -> "Square";
      case Isometric -> "Isometric";
      case HexVertical -> "Vertical Hex";
      case HexHorizontal -> "Horizontal Hex";
      case None -> "None";
    };
  }

  public static GridType fromString(String string) {
    return switch (string) {
      case "Square" -> Square;
      case "Isometric" -> Isometric;
      case "Vertical Hex" -> HexVertical;
      case "Horizontal Hex" -> HexHorizontal;
      case "None" -> None;
      default ->
          throw new IllegalArgumentException(
              String.format("\"%s\" is not a valid grid type", string));
    };
  }
}
