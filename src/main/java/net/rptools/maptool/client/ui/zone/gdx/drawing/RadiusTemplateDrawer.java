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
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.RadiusTemplate;

public class RadiusTemplateDrawer extends AbstractTemplateDrawer {
  public RadiusTemplateDrawer(AreaRenderer renderer) {
    super(renderer);
  }

  @Override
  protected void paintArea(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance) {
    var radiusTemplate = (RadiusTemplate) template;
    // Only squares w/in the radius
    if (distance <= radiusTemplate.getRadius()) {
      // Paint the squares
      for (AbstractTemplate.Quadrant q : AbstractTemplate.Quadrant.values()) {
        paintArea(batch, pen, template, xOff, yOff, gridSize, q);
      }
    }
  }

  @Override
  protected void paintBorder(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance) {
    var radiusTemplate = (RadiusTemplate) template;
    paintBorderAtRadius(
        batch, pen, template, x, y, xOff, yOff, gridSize, distance, radiusTemplate.getRadius());
  }

  private void paintBorderAtRadius(
      PolygonSpriteBatch batch,
      Pen pen,
      AbstractTemplate template,
      int x,
      int y,
      int xOff,
      int yOff,
      int gridSize,
      int distance,
      int radius) {
    // At the border?
    if (distance == radius) {
      // Paint lines between vertical boundaries if needed
      if (template.getDistance(x + 1, y) > radius) {
        for (AbstractTemplate.Quadrant q : AbstractTemplate.Quadrant.values()) {
          paintFarVerticalBorder(batch, pen, template, xOff, yOff, gridSize, q);
        }
      }

      // Paint lines between horizontal boundaries if needed
      if (template.getDistance(x, y + 1) > radius) {
        for (AbstractTemplate.Quadrant q : AbstractTemplate.Quadrant.values()) {
          paintFarHorizontalBorder(batch, pen, template, xOff, yOff, gridSize, q);
        }
      }
    }
  }
}
