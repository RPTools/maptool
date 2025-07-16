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

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.zones.GridChanged;

public class HaloRenderer {
  private final RenderHelper renderHelper;
  private final Zone zone;

  // region These fields need to be recalculated whenever the grid changes.

  private final Map<TokenFootprint, Shape> shapeMap = new HashMap<>();
  private Shape cachedHaloShape;

  // endregion

  public HaloRenderer(RenderHelper renderHelper, Zone zone) {
    this.renderHelper = renderHelper;
    this.zone = zone;

    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  private void gridChanged(GridChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    shapeMap.clear();
    cachedHaloShape = null;
  }

  private Shape getHaloShape(Grid grid) {
    if (cachedHaloShape == null) {
      if (GridFactory.getGridType(grid).equals(GridFactory.NONE)) {
        double r = grid.getSize() / 2d;
        cachedHaloShape = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
      } else {
        cachedHaloShape = grid.getCellShape();
        cachedHaloShape =
            AffineTransform.getTranslateInstance(
                    -cachedHaloShape.getBounds2D().getCenterX(),
                    -cachedHaloShape.getBounds2D().getCenterY())
                .createTransformedShape(cachedHaloShape);
      }
    }

    return cachedHaloShape;
  }

  // Render Halos
  public void renderHalo(Graphics2D g2d, Token token, ZoneViewModel.TokenPosition position) {
    if (token.getHaloColor() == null) {
      return;
    }

    var grid = zone.getGrid();
    if (grid == null) {
      return;
    }

    var haloShape = getHaloShape(grid);
    TokenFootprint fp = token.getFootprint(grid);

    // use cache so we don't have to resize halos every time
    Shape paintShape =
        shapeMap.computeIfAbsent(
            fp,
            fp2 -> {
              double maxD =
                  Math.max(
                      position.footprintBounds().getWidth() / haloShape.getBounds2D().getWidth(),
                      position.footprintBounds().getHeight() / haloShape.getBounds2D().getHeight());
              return AffineTransform.getScaleInstance(maxD, maxD).createTransformedShape(haloShape);
            });

    // position the shape we are painting
    var positionedPaintShape =
        AffineTransform.getTranslateInstance(
                position.transformedBounds().getBounds2D().getCenterX(),
                position.transformedBounds().getBounds2D().getCenterY())
            .createTransformedShape(paintShape);

    // this will eventually hold forks for painting different types of halo
    renderHelper.render(
        g2d,
        worldG -> {
          paintLineHalo(worldG, token, grid, positionedPaintShape);
        });
  }

  private void paintLineHalo(Graphics2D g2d, Token token, Grid grid, Shape paintShape) {
    // double width because we will clip the inside half
    g2d.setStroke(
        new BasicStroke(
            (float)
                (2f
                    * Math.min(1f, token.getFootprint(grid).getScale())
                    * AppPreferences.haloLineWidth.get())));
    g2d.setColor(token.getHaloColor());
    Shape oldClip = g2d.getClip();
    Area a = new Area(g2d.getClipBounds());
    a.subtract(new Area(paintShape));
    g2d.setClip(a);
    g2d.draw(paintShape);
    g2d.setClip(oldClip);
  }
}
