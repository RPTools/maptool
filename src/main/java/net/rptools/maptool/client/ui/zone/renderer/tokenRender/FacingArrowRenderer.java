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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.zone.ZoneViewModel.TokenPosition;
import net.rptools.maptool.client.ui.zone.renderer.RenderHelper;
import net.rptools.maptool.model.Token.TokenShape;
import net.rptools.maptool.model.Zone;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class FacingArrowRenderer {
  private static final Logger log = LogManager.getLogger(FacingArrowRenderer.class);
  private static final double tailX = -0.25;
  private static final double dovetailX = -0.15;
  private static final double tailY = .35;

  /** An arrow facing horizontally to the positive x-axis, with its point at (0, 0). */
  public static final Path2D UNIT_ARROW;

  static {
    UNIT_ARROW = new Path2D.Double();
    UNIT_ARROW.moveTo(0, 0);
    UNIT_ARROW.lineTo(tailX, -tailY);
    UNIT_ARROW.lineTo(dovetailX, 0);
    UNIT_ARROW.lineTo(tailX, tailY);
    UNIT_ARROW.closePath();
  }

  private final RenderHelper renderHelper;
  private final Zone zone;

  private final ArrayList<Color> figureFillColours = new ArrayList<>();
  private Color fillColour = AppPreferences.facingArrowBGColour.get();
  private Color borderColour = AppPreferences.facingArrowBorderColour.get();

  {
    AppPreferences.facingArrowBGColour.onChange(color -> fillColour = color);
    AppPreferences.facingArrowBorderColour.onChange(color -> borderColour = color);
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
  }

  public void paintArrow(Graphics2D tokenG, TokenPosition position) {
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
    renderHelper.render(
        tokenG,
        worldG ->
            paintArrowWorld(worldG, token.getFacing(), tokenShape, position.footprintBounds()));
    timer.stop("FacingArrowRenderer-render");
  }

  private void paintArrowWorld(
      Graphics2D tokenG, int facing, TokenShape tokenShape, Rectangle2D footprintBounds) {
    var timer = CodeTimer.get();
    timer.start("FacingArrowRenderer-paintArrow");
    try {
      final var isIsometric = zone.getGrid().isIsometric();

      timer.start("FacingArrowRenderer-calculateTransform");
      int angle = Math.floorMod(facing + (isIsometric ? 45 : 0), 360);
      AffineTransform transform =
          buildArrowTransform(tokenShape, footprintBounds, angle, isIsometric);
      timer.stop("FacingArrowRenderer-calculateTransform");

      timer.start("FacingArrowRenderer-transformArrow");
      Shape facingArrow = transform.createTransformedShape(UNIT_ARROW);
      timer.stop("FacingArrowRenderer-transformArrow");

      timer.start("FacingArrowRenderer-fill");
      if (TokenShape.FIGURE.equals(tokenShape) && angle <= 180) {
        tokenG.setColor(figureFillColours.get(angle));
      } else {
        tokenG.setColor(fillColour);
      }
      tokenG.fill(facingArrow);
      timer.stop("FacingArrowRenderer-fill");

      timer.start("FacingArrowRenderer-draw");
      tokenG.setRenderingHints(ImageUtil.getRenderingHintsQuality());
      tokenG.setColor(borderColour);
      tokenG.setStroke(new BasicStroke(0.85f));
      tokenG.draw(facingArrow);
      timer.stop("FacingArrowRenderer-draw");
    } catch (Exception e) {
      log.error("Failed to paint facing arrow.", e);
    } finally {
      timer.stop("FacingArrowRenderer-paintArrow");
    }
  }

  private static AffineTransform buildArrowTransform(
      TokenShape shape, Rectangle2D footprintBounds, int angle, boolean isIsometric) {
    double radFacing = Math.toRadians(angle);

    AffineTransform transform = new AffineTransform();
    transform.translate(footprintBounds.getCenterX(), footprintBounds.getCenterY());
    if (isIsometric) {
      transform.scale(1.0, 0.5);
    }
    transform.rotate(-radFacing);

    double distanceToPoint = footprintBounds.getWidth() / 2;
    if (TokenShape.SQUARE.equals(shape) && !isIsometric) {
      if (angle >= 45 && angle <= 135 || angle >= 225 && angle <= 315) { // Top or bottom face.
        distanceToPoint = footprintBounds.getHeight() / 2 / Math.abs(Math.sin(radFacing));
      } else { // Left or right face
        distanceToPoint = footprintBounds.getWidth() / 2 / Math.abs(Math.cos(radFacing));
      }
    }
    transform.translate(distanceToPoint, 0);

    var size = footprintBounds.getWidth() / 2d;
    transform.scale(size, size);
    return transform;
  }
}
