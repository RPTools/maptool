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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;

import net.rptools.maptool.model.drawing.*;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class AbstractDrawingDrawer {
  protected Color tmpColor = new Color();
  protected Vector2 tmpVector = new Vector2();
  protected ShapeDrawer drawer;
  protected AreaRenderer areaRenderer;

  public AbstractDrawingDrawer(ShapeDrawer drawer) {
    this.drawer = drawer;
    this.areaRenderer = new AreaRenderer(drawer);
  }

  public void draw(Drawable element, Pen pen) {
    if (pen.getBackgroundPaint() instanceof DrawableColorPaint) {
      var colorPaint = (DrawableColorPaint) pen.getBackgroundPaint();
      Color.argb8888ToColor(tmpColor, colorPaint.getColor());
      drawer.setColor(tmpColor);
      areaRenderer.setTextureRegion(null);
    } else if(pen.getBackgroundPaint() instanceof DrawableTexturePaint texturePaint) {
      var image = texturePaint.getAsset().getData();
      var pix = new Pixmap(image, 0, image.length);

      //FIXME properly dispose
      var region = new TextureRegion(new Texture(pix));
      region.flip(false, true);
      areaRenderer.setTextureRegion(region);
      pix.dispose();
    }
    drawBackground(element, pen);

    if (pen.getPaint() instanceof DrawableColorPaint) {
      var colorPaint = (DrawableColorPaint) pen.getPaint();
      Color.argb8888ToColor(tmpColor, colorPaint.getColor());
      drawer.setColor(tmpColor);
    }
    var lineWidth = drawer.getDefaultLineWidth();
    drawer.setDefaultLineWidth(pen.getThickness());
    drawBorder(element, pen);
    drawer.setDefaultLineWidth(lineWidth);
  }

  protected void line(Pen pen, float x1, float y1, float x2, float y2) {
    var halfLineWidth = pen.getThickness() / 2f;
    if (!pen.getSquareCap()) {
      drawer.filledCircle(x1, -y1, halfLineWidth);
      drawer.filledCircle(x2, -y2, halfLineWidth);
      drawer.line(x1, -y1, x2, -y2);
    } else {
      tmpVector.set(x1 - x2, y1 - y2).nor();
      var tx = tmpVector.x * halfLineWidth;
      var ty = tmpVector.y * halfLineWidth;
      drawer.line(x1 + tx, y1 + ty, x2 - tx, y2 - ty);
    }
  }

  protected FloatArray pathToFloatArray(PathIterator pathIterator) {
    return areaRenderer.pathToFloatArray(pathIterator);
  }

  protected void fillArea(Area area) {
    areaRenderer.fillArea(area);
  }

  protected void drawArea(Area area) {
    areaRenderer.drawArea(area);
  }

  protected abstract void drawBackground(Drawable element, Pen pen);

  protected abstract void drawBorder(Drawable element, Pen pen);
}
