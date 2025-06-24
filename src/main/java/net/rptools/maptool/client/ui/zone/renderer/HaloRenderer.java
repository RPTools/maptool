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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.Map;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HaloRenderer {
  private static final Logger log = LogManager.getLogger(HaloRenderer.class);
  private final RenderHelper renderHelper;
  private static final Map<Grid, Map<TokenFootprint, Shape>> GRID_SHAPE_MAP = new HashMap<>();
  private Map<TokenFootprint, Shape> shapeMap;
  private Grid grid;
  private Shape haloShape;
  private Shape paintShape;

  public HaloRenderer(RenderHelper renderHelper) {
    this.renderHelper = renderHelper;
  }

  public void setRenderer(ZoneRenderer zoneRenderer) {
    Zone zone = zoneRenderer.getZone();
    grid = zone.getGrid();
    if (grid == null) {
      return;
    }
    shapeMap = GRID_SHAPE_MAP.get(grid);
    if (shapeMap == null) {
      shapeMap = new HashMap<>();
    }
    if (GridFactory.getGridType(grid).equals(GridFactory.NONE)) {
      double r = grid.getSize() / 2d;
      haloShape = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
    } else {
      haloShape = grid.getCellShape();
      haloShape =
          AffineTransform.getTranslateInstance(
                  -haloShape.getBounds2D().getCenterX(), -haloShape.getBounds2D().getCenterY())
              .createTransformedShape(haloShape);
    }

    log.info("HaloRenderer - ZoneRenderer updated - Grid set.");
  }

  // Render Halos
  public void renderHalo(Graphics2D g2d, Token token, ZoneViewModel.TokenPosition position) {
    if (token.getHaloColor() == null || grid == null) {
      return;
    }
    // use cache so we don't have to resize halos every time
    TokenFootprint fp = token.getFootprint(grid);
    if (shapeMap.containsKey(fp)) {
      paintShape = shapeMap.get(fp);
    } else {
      double maxD =
          Math.max(
              position.footprintBounds().getBounds2D().getWidth()
                  / haloShape.getBounds2D().getWidth(),
              position.footprintBounds().getBounds2D().getHeight()
                  / haloShape.getBounds2D().getHeight());
      paintShape = AffineTransform.getScaleInstance(maxD, maxD).createTransformedShape(haloShape);

      shapeMap.put(fp, paintShape);
      GRID_SHAPE_MAP.put(grid, shapeMap);
    }

    // position the shape we are painting
    paintShape =
        AffineTransform.getTranslateInstance(
                position.transformedBounds().getBounds2D().getCenterX(),
                position.transformedBounds().getBounds2D().getCenterY())
            .createTransformedShape(paintShape);

    // this will eventually hold forks for painting different types of halo
    renderHelper.render(
        g2d,
        worldG -> {
          paintLineHalo(worldG, token);
        });
  }

  private void paintLineHalo(Graphics2D g2d, Token token) {
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

  public void gridChanged(ZoneRenderer zoneRenderer) {
    setRenderer(zoneRenderer);
  }
}
