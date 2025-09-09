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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Pools;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRendererConstants;
import net.rptools.maptool.model.*;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class GridRenderer {
  private ZoneCache zoneCache;
  private final AreaRenderer areaRenderer;
  private final ShapeDrawer drawer;
  private final Batch batch;
  private final Camera hudCam;

  public GridRenderer(AreaRenderer areaRenderer, Camera hudCam) {
    this.areaRenderer = areaRenderer;
    this.drawer = areaRenderer.getShapeDrawer();
    batch = drawer.getBatch();
    this.hudCam = hudCam;
  }

  public void setZoneCache(ZoneCache zoneCache) {
    this.zoneCache = zoneCache;
  }

  public void render() {
    var grid = zoneCache.getZone().getGrid();
    var scale = (float) zoneCache.getZoneRenderer().getScale();
    int gridSize = (int) (grid.getSize() * scale);

    if (!AppState.isShowGrid() || gridSize < ZoneRendererConstants.MIN_GRID_SIZE) {
      return;
    }

    // Do nothing for GridlessGrid
    if (grid instanceof HexGrid hexGrid) {
      renderGrid(hexGrid);
    } else if (grid instanceof SquareGrid squareGrid) {
      renderGrid(squareGrid);
    } else if (grid instanceof IsometricGrid isometricGrid) {
      renderGrid(isometricGrid);
    }
  }

  private void renderGrid(HexGrid grid) {
    var renderer = zoneCache.getZoneRenderer();
    var scale = renderer.getScale();
    var scaledMinorRadius = grid.getMinorRadius() * scale;
    var scaledEdgeLength = grid.getEdgeLength() * scale;
    var scaledEdgeProjection = grid.getEdgeProjection() * scale;
    var scaledHex = grid.createHalfShape(scaledMinorRadius, scaledEdgeProjection, scaledEdgeLength);

    int offU = grid.getOffU(renderer);
    int offV = grid.getOffV(renderer);
    int count = 0;

    var tmpColor = Pools.obtain(Color.class);
    Color.argb8888ToColor(tmpColor, zoneCache.getZone().getGridColor());
    tmpColor.premultiplyAlpha();
    drawer.setColor(tmpColor);
    var floats = areaRenderer.pathToFloatArray(scaledHex.getPathIterator(null));
    var lineWidth = AppState.getGridLineWeight();

    for (double v = offV % (scaledMinorRadius * 2) - (scaledMinorRadius * 2);
        v < grid.getRendererSizeV(renderer);
        v += scaledMinorRadius) {
      double offsetU = (int) ((count & 1) == 0 ? 0 : -(scaledEdgeProjection + scaledEdgeLength));
      count++;
      double start =
          offU % (2 * scaledEdgeLength + 2 * scaledEdgeProjection)
              - (2 * scaledEdgeLength + 2 * scaledEdgeProjection);
      double end =
          grid.getRendererSizeU(renderer) + 2 * scaledEdgeLength + 2 * scaledEdgeProjection;
      double incr = 2 * scaledEdgeLength + 2 * scaledEdgeProjection;
      for (double u = start; u < end; u += incr) {
        float transX;
        float transY;
        if (grid instanceof HexGridVertical) {
          transX = (float) (u + offsetU);
          transY = hudCam.viewportHeight - (float) v;
        } else {
          transX = (float) v;
          transY = (float) (-u - offsetU) + hudCam.viewportHeight;
        }

        var tmpMatrix = Pools.obtain(Matrix4.class);
        tmpMatrix.translate(transX, transY, 0);
        batch.setTransformMatrix(tmpMatrix);
        drawer.update();

        drawer.path(floats, lineWidth, JoinType.SMOOTH, true);
        tmpMatrix.idt();
        batch.setTransformMatrix(tmpMatrix);
        Pools.free(tmpMatrix);
        drawer.update();
      }
    }
    Pools.free(tmpColor);
  }

  private void renderGrid(IsometricGrid grid) {
    var scale = (float) zoneCache.getZoneRenderer().getScale();
    int gridSize = (int) (grid.getSize() * scale);

    var tmpColor = Pools.obtain(Color.class);
    Color.argb8888ToColor(tmpColor, zoneCache.getZone().getGridColor());
    tmpColor.premultiplyAlpha();

    drawer.setColor(tmpColor);

    var x = hudCam.position.x - hudCam.viewportWidth / 2;
    var y = hudCam.position.y - hudCam.viewportHeight / 2;
    var w = hudCam.viewportWidth;
    var h = hudCam.viewportHeight;

    double isoHeight = grid.getSize() * scale;
    double isoWidth = grid.getSize() * 2 * scale;

    int offX =
        (int) (zoneCache.getZoneRenderer().getViewOffsetX() % isoWidth + grid.getOffsetX() * scale)
            + 1;
    int offY =
        (int) (zoneCache.getZoneRenderer().getViewOffsetY() % gridSize + grid.getOffsetY() * scale)
            + 1;

    int startCol = (int) ((int) (x / isoWidth) * isoWidth);
    int startRow = (int) (y / gridSize) * gridSize;

    for (double row = startRow; row < y + h + gridSize; row += gridSize) {
      for (double col = startCol; col < x + w + isoWidth; col += isoWidth) {
        drawHatch(grid, (int) (col + offX), h - (int) (row + offY));
      }
    }

    for (double row = startRow - (isoHeight / 2); row < y + h + gridSize; row += gridSize) {
      for (double col = startCol - (isoWidth / 2); col < x + w + isoWidth; col += isoWidth) {
        drawHatch(grid, (int) (col + offX), h - (int) (row + offY));
      }
    }
    Pools.free(tmpColor);
  }

  private void drawHatch(IsometricGrid grid, float x, float y) {
    double isoWidth = grid.getSize() * zoneCache.getZoneRenderer().getScale();
    int hatchSize = isoWidth > 10 ? (int) isoWidth / 8 : 2;

    var lineWidth = AppState.getGridLineWeight();

    drawer.line(x - (hatchSize * 2), y - hatchSize, x + (hatchSize * 2), y + hatchSize, lineWidth);
    drawer.line(x - (hatchSize * 2), y + hatchSize, x + (hatchSize * 2), y - hatchSize, lineWidth);
  }

  private void renderGrid(SquareGrid grid) {
    var scale = (float) zoneCache.getZoneRenderer().getScale();
    float gridSize = (grid.getSize() * scale);
    var tmpColor = Pools.obtain(Color.class);
    Color.argb8888ToColor(tmpColor, zoneCache.getZone().getGridColor());
    tmpColor.premultiplyAlpha();

    drawer.setColor(tmpColor);

    var x = hudCam.position.x - hudCam.viewportWidth / 2;
    var y = hudCam.position.y - hudCam.viewportHeight / 2;
    var w = hudCam.viewportWidth;
    var h = hudCam.viewportHeight;

    var offX =
        Math.round(
            zoneCache.getZoneRenderer().getViewOffsetX() % gridSize + grid.getOffsetX() * scale);
    var offY =
        Math.round(
            zoneCache.getZoneRenderer().getViewOffsetY() % gridSize + grid.getOffsetY() * scale);

    var startCol = ((int) (x / gridSize) * gridSize);
    var startRow = ((int) (y / gridSize) * gridSize);

    var lineWidth = AppState.getGridLineWeight();

    for (float row = startRow; row < y + h + gridSize; row += gridSize) {
      var rounded = Math.round(h - (row + offY));
      drawer.line(x, rounded, x + w, rounded, lineWidth);
    }

    for (float col = startCol; col < x + w + gridSize; col += gridSize) {
      var rounded = Math.round(col + offX);
      drawer.line(rounded, y, rounded, y + h, lineWidth);
    }
    Pools.free(tmpColor);
  }
}
