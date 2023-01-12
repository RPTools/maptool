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

//import com.badlogic.gdx.graphics.g2d.RepeatablePolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Bezier;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;

import net.rptools.lib.gdx.RepeatablePolygonSprite;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AreaRenderer {

  private FloatArray tmpFloat = new FloatArray();
  private ShapeDrawer drawer;
  private float[] floatsFromArea = new float[6];
  private Vector2 tmpVector = new Vector2();
  private Vector2 tmpVector0 = new Vector2();
  private Vector2 tmpVector1 = new Vector2();
  private Vector2 tmpVector2 = new Vector2();
  private Vector2 tmpVector3 = new Vector2();
  private Vector2 tmpVectorOut = new Vector2();

 // private RepeatablePolygonSprite polygonSprite = new RepeatablePolygonSprite();
  private TextureRegion textureRegion = null;

  public AreaRenderer(ShapeDrawer drawer) {
    this.drawer = drawer;
  }

  public TextureRegion getTextureRegion() {
    return textureRegion;
  }

  public void setTextureRegion(TextureRegion textureRegion) {
    this.textureRegion = textureRegion;
  }

  public void fillArea(Area area) {
    if (area == null || area.isEmpty()) return;

    paintArea(area, true);
  }

  public void drawArea(Area area) {
    if (area == null || area.isEmpty()) return;

    paintArea(area, false);
  }

  protected void paintArea(Area area, boolean fill) {
    pathToFloatArray(area.getPathIterator(null));

    if (fill) {
      var lastX = tmpFloat.get(tmpFloat.size - 2);
      var lastY = tmpFloat.get(tmpFloat.size - 1);
      if (lastX != tmpFloat.get(0) && lastY != tmpFloat.get(1))
        tmpFloat.add(tmpFloat.get(0), tmpFloat.get(1));

      if(textureRegion == null) {
        drawer.filledPolygon(tmpFloat.toArray());
      } else {
        var sprite = new RepeatablePolygonSprite();
       // sprite.setPolygon(textureRegion, tmpFloat.toArray());
        sprite.setTextureRegion(textureRegion);
        sprite.setVertices(tmpFloat.toArray());
        sprite.draw((PolygonSpriteBatch) drawer.getBatch());
      }
    } else {
      if (tmpFloat.get(0) == tmpFloat.get(tmpFloat.size - 2)
          && tmpFloat.get(1) == tmpFloat.get(tmpFloat.size - 1)) {
        tmpFloat.pop();
        tmpFloat.pop();
      }

      drawer.path(tmpFloat.toArray(), drawer.getDefaultLineWidth(), JoinType.SMOOTH, false);
    }
  }

  public FloatArray pathToFloatArray(PathIterator it) {
    tmpFloat.clear();

    for (; !it.isDone(); it.next()) {
      int type = it.currentSegment(floatsFromArea);

      switch (type) {
        case PathIterator.SEG_MOVETO:
          //                   System.out.println("Move to: ( " + floatsFromArea[0] + ", " +
          // floatsFromArea[1] + ")");
          tmpFloat.add(floatsFromArea[0], -floatsFromArea[1]);

          break;
        case PathIterator.SEG_CLOSE:
          //                   System.out.println("Close");

          return tmpFloat;
        case PathIterator.SEG_LINETO:
          //                  System.out.println("Line to: ( " + floatsFromArea[0] + ", " +
          // floatsFromArea[1] + ")");
          tmpFloat.add(floatsFromArea[0], -floatsFromArea[1]);
          break;
        case PathIterator.SEG_QUADTO:
          //                  System.out.println("quadratic bezier with: ( " + floatsFromArea[0] +
          // ", " + floatsFromArea[1] +
          //                          "), (" + floatsFromArea[2] + ", " + floatsFromArea[3] + ")");

          tmpVector0.set(tmpFloat.get(tmpFloat.size - 2), tmpFloat.get(tmpFloat.size - 1));
          tmpVector1.set(floatsFromArea[0], -floatsFromArea[1]);
          tmpVector2.set(floatsFromArea[2], -floatsFromArea[3]);
          for (var i = 1; i <= GdxRenderer.POINTS_PER_BEZIER; i++) {
            Bezier.quadratic(
                tmpVectorOut,
                i / GdxRenderer.POINTS_PER_BEZIER,
                tmpVector0,
                tmpVector1,
                tmpVector2,
                tmpVector);
            tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
          }
          break;
        case PathIterator.SEG_CUBICTO:
          //                    System.out.println("cubic bezier with: ( " + floatsFromArea[0] + ",
          // " + floatsFromArea[1] +
          //                            "), (" + floatsFromArea[2] + ", " + floatsFromArea[3] +
          //                            "), (" + floatsFromArea[4] + ", " + floatsFromArea[5] +
          // ")");

          tmpVector0.set(tmpFloat.get(tmpFloat.size - 2), tmpFloat.get(tmpFloat.size - 1));
          tmpVector1.set(floatsFromArea[0], -floatsFromArea[1]);
          tmpVector2.set(floatsFromArea[2], -floatsFromArea[3]);
          tmpVector3.set(floatsFromArea[4], -floatsFromArea[5]);
          for (var i = 1; i <= GdxRenderer.POINTS_PER_BEZIER; i++) {
            Bezier.cubic(
                tmpVectorOut,
                i / GdxRenderer.POINTS_PER_BEZIER,
                tmpVector0,
                tmpVector1,
                tmpVector2,
                tmpVector3,
                tmpVector);
            tmpFloat.add(tmpVectorOut.x, tmpVectorOut.y);
          }
          break;
        default:
          System.out.println("Type: " + type);
      }
    }
    return tmpFloat;
  }
}
