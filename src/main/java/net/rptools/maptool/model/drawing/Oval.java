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

import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.io.Serial;
import java.io.Serializable;
import net.rptools.maptool.model.GUID;

/**
 * An oval.
 *
 * @deprecated This is a legacy class not currently in use. It is kept here in case it has been
 *     serialized in any existing campaigns. It used to extend {@link Rectangle} and {@link
 *     AbstractDrawing} but is now just a holder for data and will replace itself with a {@link
 *     ShapeDrawable}.
 */
@Deprecated
public final class Oval implements Serializable {
  private GUID id;
  private String layer;
  private String name;
  private Point startPoint;
  private Point endPoint;

  @Serial
  private Object readResolve() {
    var ellipse =
        new Ellipse2D.Double(
            startPoint.x, startPoint.y, endPoint.x - startPoint.x, endPoint.y - startPoint.y);
    return new ShapeDrawable(id, ellipse, true);
  }
}
