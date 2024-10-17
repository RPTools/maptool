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
import java.awt.geom.Path2D;
import java.io.Serial;
import java.io.Serializable;
import net.rptools.maptool.model.GUID;

/**
 * An X-shaped cross
 *
 * @deprecated This is a legacy class not currently in use. It is kept here in case it has been
 *     serialized in any existing campaigns. It used to extend {@link AbstractDrawing} but is now *
 *     just a holder for data and will replace itself with a {@link ShapeDrawable}.
 */
@Deprecated
public final class Cross implements Serializable {
  private GUID id;
  private String layer;
  private String name;
  private Point startPoint;
  private Point endPoint;

  @Serial
  private Object readResolve() {
    var path = new Path2D.Double();
    path.moveTo(startPoint.x, startPoint.y);
    path.lineTo(endPoint.x, endPoint.y);
    path.moveTo(startPoint.x, endPoint.y);
    path.lineTo(endPoint.x, startPoint.y);

    return new ShapeDrawable(id, path, false);
  }
}
