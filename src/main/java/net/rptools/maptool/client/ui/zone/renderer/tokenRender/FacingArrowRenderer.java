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
package net.rptools.maptool.client.ui.zone.renderer.tokenRender;

import com.google.common.eventbus.Subscribe;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.GeometryUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.zone.ZoneViewModel.TokenPosition;
import net.rptools.maptool.client.ui.zone.renderer.RenderHelper;
import net.rptools.maptool.model.GridFactory;
import net.rptools.maptool.model.Token.TokenShape;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.zones.GridChanged;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FacingArrowRenderer {
  private static final Logger log = LogManager.getLogger(FacingArrowRenderer.class);

  /** An arrow facing horizontally to the positive x-axis, with its point at (0, 0). */
  private static final Path2D UNIT_ARROW;

  static {
    final double tailX = -0.25;
    final double tailY = .35;
    UNIT_ARROW = new Path2D.Double();
    UNIT_ARROW.moveTo(0, 0);
    UNIT_ARROW.lineTo(tailX, -tailY);
    UNIT_ARROW.lineTo(tailX, tailY);
    UNIT_ARROW.closePath();
  }

  private final RenderHelper renderHelper;
  private Zone zone;

  private final ArrayList<Color> figureFillColours = new ArrayList<>();

  private final Color fillColour = Color.YELLOW;
  private final Color borderColour = Color.DARK_GRAY;
  private boolean isIsometric;
  private boolean isSquare;

  @SuppressWarnings("unused")
  @Subscribe
  private void onGridChanged(GridChanged event) {
    if (event.zone() != null) {
      this.zone = event.zone();
      if (zone.getGrid() == null) {
        isIsometric = false;
        isSquare = false;
      } else {
        isIsometric = this.zone.getGrid().isIsometric();
        isSquare = GridFactory.getGridType(this.zone.getGrid()).equals(GridFactory.SQUARE);
      }
    }
  }

  public FacingArrowRenderer(RenderHelper renderHelper, Zone zone) {
    this.renderHelper = renderHelper;
    this.zone = zone;
    for (int i = 0; i <= 90; i++) {
      figureFillColours.add(new Color(1 - 0.5f / 90f * i, 1 - 0.5f / 90f * i, 0));
    }
    for (int i = 89; i >= 0; i--) {
      figureFillColours.add(figureFillColours.get(i));
    }
    if (zone.getGrid() == null) {
      isIsometric = false;
      isSquare = false;
    } else {
      isIsometric = this.zone.getGrid().isIsometric();
      isSquare = GridFactory.getGridType(this.zone.getGrid()).equals(GridFactory.SQUARE);
    }
  }

  public void paintArrow(Graphics2D g2d, TokenPosition position) {
    var timer = CodeTimer.get();
    var token = position.token();
    var tokenShape = token.getShape();

    timer.start("FacingArrowRenderer-preCheck");
    if (!token.hasFacing()) {
      return;
    }
    final var forceFacing = AppPreferences.forceFacingArrow.get();
    if (!forceFacing) {
      if (TokenShape.TOP_DOWN.equals(tokenShape)) {
        return;
      }
      if (TokenShape.FIGURE.equals(tokenShape) && token.getHasImageTable()) {
        return;
      }
    }
    timer.stop("FacingArrowRenderer-preCheck");

    timer.start("FacingArrowRenderer-render");
    // set the stroke to shrink for tiny tokens to prevent it crowding out the fill
    g2d.setStroke(new BasicStroke((float) (0.85f * position.token().getSizeScale())));
    renderHelper.render(
        g2d,
        worldG ->
            paintArrowWorld(worldG, token.getFacing(), tokenShape, position.footprintBounds()));
    timer.stop("FacingArrowRenderer-render");
  }

  private void paintArrowWorld(
      Graphics2D tokenG, int facing, TokenShape tokenShape, Rectangle2D footprintBounds) {
    var timer = CodeTimer.get();
    timer.start("FacingArrowRenderer-paintArrow");
    try {
      timer.start("FacingArrowRenderer-calculateTransform");
      int angle = Math.floorMod(facing + (isIsometric ? 45 : 0), 360);
      AffineTransform transform =
          buildArrowTransform(tokenShape, footprintBounds, angle, isIsometric);
      timer.stop("FacingArrowRenderer-calculateTransform");

      timer.start("FacingArrowRenderer-transformArrow");
      Shape facingArrow = transform.createTransformedShape(UNIT_ARROW);
      timer.stop("FacingArrowRenderer-transformArrow");

      // draw first so that fill is always visible
      tokenG.setColor(borderColour);
      tokenG.draw(facingArrow);

      if (TokenShape.FIGURE.equals(tokenShape) && angle <= 180) {
        tokenG.setColor(figureFillColours.get(angle));
      } else {
        tokenG.setColor(fillColour);
      }
      tokenG.fill(facingArrow);
    } catch (Exception e) {
      log.error("Failed to paint facing arrow.", e);
    }
    timer.stop("FacingArrowRenderer-paintArrow");
  }

  private AffineTransform buildArrowTransform(
      TokenShape shape, Rectangle2D footprintBounds, int angle, boolean isIsometric) {
    double radFacing = Math.toRadians(angle);

    AffineTransform transform = new AffineTransform();
    // move to footprint centre
    transform.translate(footprintBounds.getCenterX(), footprintBounds.getCenterY());
    if (isIsometric) {
      transform.scale(1.0, 0.5);
    }
    // spin to face correct direction. Not linear for isometric
    transform.rotate(-radFacing);

    // calculate distance to edge
    double distanceToPoint;
    Shape cellShape = this.zone.getGrid().getCellShape();
    if (cellShape != null) {
      Point2D centre = new Point2D.Double(0, 0);
      // centre the cell shape
      cellShape =
          AffineTransform.getTranslateInstance(
                  -cellShape.getBounds2D().getCenterX(), -cellShape.getBounds2D().getCenterY())
              .createTransformedShape(cellShape);
      double scale = footprintBounds.getWidth() / cellShape.getBounds2D().getWidth();
      // size the cell shape to the footprint - compensate for previous isometric scaling
      cellShape =
          AffineTransform.getScaleInstance(scale, isIsometric ? 2 * scale : scale)
              .createTransformedShape(cellShape);
      // create a line from the centre with token facing angle
      Point2D farPoint = GraphicsUtil.getPointAtVector(centre, angle, 300 * scale);
      Line2D.Double ray = new Line2D.Double(centre, farPoint);
      // obtain the point the line intersects the cell shape
      Point2D[] point2D = GeometryUtil.lineSegmentShapeIntersection(ray, cellShape);
      distanceToPoint = Math.hypot(point2D[0].getX(), point2D[0].getY());
    } else {
      // fallback for gridless, just use radius based on size
      distanceToPoint = footprintBounds.getWidth() / 2;
    }
    // move out to edge
    transform.translate(distanceToPoint, 0);

    var sizeW = footprintBounds.getWidth() / 2d;
    var sizeH = footprintBounds.getHeight() / 2d;
    // make it look big
    transform.scale(sizeW, sizeH);
    return transform;
  }
}
