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
package net.rptools.maptool.client.ui.zone.gdx.drawing;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.utils.FloatArray;
import java.awt.geom.Area;
import net.rptools.maptool.client.ui.zone.gdx.AreaRenderer;
import net.rptools.maptool.client.ui.zone.gdx.ZoneCache;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.*;

public abstract class AbstractDrawingDrawer {

  protected Float alpha = null;
  protected AreaRenderer areaRenderer;

  protected ZoneCache zoneCache;

  public void setZoneCache(ZoneCache zoneCache) {
    this.zoneCache = zoneCache;
  }

  public AbstractDrawingDrawer(AreaRenderer areaRenderer) {
    this.areaRenderer = areaRenderer;
  }

  public void draw(PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen) {
    applyColor(pen.getBackgroundPaint(), true);
    drawBackground(batch, zone, element, pen);

    applyColor(pen.getPaint(), false);
    drawBorder(batch, zone, element, pen);
  }

  protected void applyColor(DrawablePaint paint, boolean applyAlpha) {
    var gdxPaint = zoneCache.getPaint(paint);
    var color = gdxPaint.color();
    var c2 = new Color().set(color);
    if (alpha != null && applyAlpha) {

      c2.set(color.r, color.g, color.b, alpha);
    }

    areaRenderer.setColor(c2);
    // areaRenderer.setColor(gdxPaint.color());
    if (gdxPaint.textureRegion() != null) {
      areaRenderer.setTextureRegion(gdxPaint.textureRegion());
    }
  }

  protected void line(PolygonSpriteBatch batch, Pen pen, float x1, float y1, float x2, float y2) {
    var floats = new FloatArray();
    // negate y values because we are y-up
    floats.add(x1, -y1, x2, -y2);
    var polygon =
        areaRenderer.drawPathWithJoin(
            floats,
            pen.getThickness(),
            pen.getSquareCap() ? AreaRenderer.JoinType.Pointy : AreaRenderer.JoinType.Round,
            false);
    applyColor(pen.getPaint(), false);
    areaRenderer.paintPolygon(batch, polygon);
  }

  protected void fillArea(PolygonSpriteBatch batch, Area area, Pen pen) {
    alpha = pen.getOpacity();
    applyColor(pen.getBackgroundPaint(), true);
    areaRenderer.fillArea(batch, area);
  }

  protected void drawArea(PolygonSpriteBatch batch, Area area, Pen pen) {
    alpha = pen.getOpacity();
    applyColor(pen.getPaint(), true);
    areaRenderer.drawArea(batch, area, !pen.getSquareCap(), pen.getThickness());
  }

  protected abstract void drawBackground(
      PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen);

  protected abstract void drawBorder(
      PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen);
}
