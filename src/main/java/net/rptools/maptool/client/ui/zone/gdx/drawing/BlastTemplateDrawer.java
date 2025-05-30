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
import net.rptools.maptool.client.ui.zone.gdx.AreaRenderer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.BlastTemplate;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;

public class BlastTemplateDrawer extends AbstractDrawingDrawer {

  public BlastTemplateDrawer(AreaRenderer renderer) {
    super(renderer);
  }

  @Override
  protected void drawBackground(PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen) {
    var template = (BlastTemplate) element;
    alpha = AbstractTemplate.DEFAULT_BG_ALPHA;
    fillArea(batch, template.getArea(zone), pen);
  }

  @Override
  protected void drawBorder(PolygonSpriteBatch batch, Zone zone, Drawable element, Pen pen) {
    var template = (BlastTemplate) element;
    drawArea(batch, template.getArea(zone), pen);
  }
}
