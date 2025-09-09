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

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import java.util.List;
import net.rptools.maptool.client.ui.zone.gdx.AreaRenderer;
import net.rptools.maptool.client.ui.zone.gdx.ZoneCache;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.*;

public class DrawnElementRenderer {
  private final LineTemplateDrawer lineTemplateDrawer;
  private final LineCellTemplateDrawer lineCellTemplateDrawer;
  private final RadiusTemplateDrawer radiusTemplateDrawer;
  private final BurstTemplateDrawer burstTemplateDrawer;
  private final ConeTemplateDrawer coneTemplateDrawer;
  private final BlastTemplateDrawer blastTemplateDrawer;
  private final RadiusCellTemplateDrawer radiusCellTemplateDrawer;
  private final ShapeDrawableDrawer shapeDrawableDrawer;

  public DrawnElementRenderer(AreaRenderer areaRenderer) {
    lineTemplateDrawer = new LineTemplateDrawer(areaRenderer);
    lineCellTemplateDrawer = new LineCellTemplateDrawer(areaRenderer);
    radiusTemplateDrawer = new RadiusTemplateDrawer(areaRenderer);
    burstTemplateDrawer = new BurstTemplateDrawer(areaRenderer);
    coneTemplateDrawer = new ConeTemplateDrawer(areaRenderer);
    blastTemplateDrawer = new BlastTemplateDrawer(areaRenderer);
    radiusCellTemplateDrawer = new RadiusCellTemplateDrawer(areaRenderer);
    shapeDrawableDrawer = new ShapeDrawableDrawer(areaRenderer);
  }

  public void render(PolygonSpriteBatch batch, Zone zone, List<DrawnElement> drawables) {
    for (var drawable : drawables) renderDrawable(batch, zone, drawable);
  }

  private void renderDrawable(PolygonSpriteBatch batch, Zone zone, DrawnElement element) {
    var pen = element.getPen();
    var drawable = element.getDrawable();

    if (drawable instanceof ShapeDrawable) shapeDrawableDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof DrawablesGroup)
      for (var groupElement : ((DrawablesGroup) drawable).getDrawableList())
        renderDrawable(batch, zone, groupElement);
    else if (drawable instanceof RadiusCellTemplate)
      radiusCellTemplateDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof LineCellTemplate)
      lineCellTemplateDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof BlastTemplate)
      blastTemplateDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof ConeTemplate) coneTemplateDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof BurstTemplate)
      burstTemplateDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof RadiusTemplate)
      radiusTemplateDrawer.draw(batch, zone, drawable, pen);
    else if (drawable instanceof LineTemplate) lineTemplateDrawer.draw(batch, zone, drawable, pen);
  }

  public void setZoneCache(ZoneCache zoneCache) {
    lineTemplateDrawer.setZoneCache(zoneCache);
    lineCellTemplateDrawer.setZoneCache(zoneCache);
    radiusTemplateDrawer.setZoneCache(zoneCache);
    burstTemplateDrawer.setZoneCache(zoneCache);
    coneTemplateDrawer.setZoneCache(zoneCache);
    blastTemplateDrawer.setZoneCache(zoneCache);
    radiusCellTemplateDrawer.setZoneCache(zoneCache);
    shapeDrawableDrawer.setZoneCache(zoneCache);
  }
}
