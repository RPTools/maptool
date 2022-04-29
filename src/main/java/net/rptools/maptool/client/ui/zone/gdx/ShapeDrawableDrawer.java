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
package net.rptools.maptool.client.ui.zone.gdx;

import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ShapeDrawableDrawer extends AbstractDrawingDrawer {

  public ShapeDrawableDrawer(ShapeDrawer drawer) {
    super(drawer);
  }

  @Override
  protected void drawBackground(Drawable element, Pen pen) {
    var shape = (ShapeDrawable) element;
    fillArea(shape.getArea());
  }

  @Override
  protected void drawBorder(Drawable element, Pen pen) {
    var shape = (ShapeDrawable) element;
    var tmpFloat = pathToFloatArray(shape.getShape().getPathIterator(null));
    if (tmpFloat.get(0) == tmpFloat.get(tmpFloat.size - 2)
        && tmpFloat.get(1) == tmpFloat.get(tmpFloat.size - 1)) {
      tmpFloat.pop();
      tmpFloat.pop();
    }
    if (pen.getSquareCap())
      drawer.path(tmpFloat.toArray(), drawer.getDefaultLineWidth(), JoinType.POINTY, false);
    else {
      drawer.path(tmpFloat.toArray(), drawer.getDefaultLineWidth(), JoinType.NONE, false);
      for (int i = 0; i + 1 < tmpFloat.size; i += 2)
        drawer.filledCircle(tmpFloat.get(i), tmpFloat.get(i + 1), pen.getThickness() / 2f);
    }
  }
}
