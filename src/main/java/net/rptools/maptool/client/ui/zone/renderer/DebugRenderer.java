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
package net.rptools.maptool.client.ui.zone.renderer;

import com.google.common.collect.Iterators;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public class DebugRenderer {
  private final RenderHelper renderHelper;
  private final Color[] palette;

  public DebugRenderer(RenderHelper renderHelper) {
    this.renderHelper = renderHelper;
    palette =
        new Color[] {Color.red, Color.green, Color.blue, Color.magenta, Color.orange, Color.yellow};
  }

  public void renderShapes(Graphics2D g2d, Iterable<Shape> shapes) {
    renderHelper.render(g2d, worldG -> renderWorld(worldG, shapes));
  }

  private void renderWorld(Graphics2D worldG, Iterable<Shape> shapes) {
    worldG.setComposite(AlphaComposite.SrcOver);
    // Keep the line a consistent thickness
    worldG.setStroke(new BasicStroke(1 / (float) worldG.getTransform().getScaleX()));

    var paletteIterator = Iterators.cycle(palette);
    for (final var shape : shapes) {
      final var color = paletteIterator.next();

      if (shape == null) {
        continue;
      }

      var fillColor = color.darker();
      fillColor =
          new Color(
              fillColor.getRed(),
              fillColor.getGreen(),
              fillColor.getBlue(),
              fillColor.getAlpha() / 3);
      worldG.setColor(fillColor);
      worldG.fill(shape);

      worldG.setColor(color);
      worldG.draw(shape);
    }
  }
}
