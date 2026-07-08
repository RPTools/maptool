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
import java.awt.geom.*;
import java.util.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.zones.GridChanged;

public class HaloRenderer {
  private final RenderHelper renderHelper;
  private final Campaign campaign;
  private final Zone zone;

  private final Map<CompositeUnitShapeKey, Shape> haloUnitShapeMap = new HashMap<>();

  // region These fields need to be recalculated whenever the grid changes.
  private Shape haloGridShapeCache;
  private final Map<CompositeHaloMiniShapeKey, Area> haloMiniShapeMap = new HashMap<>();
  private final Map<MD5Key, Area> haloOutlineAreaMap = new HashMap<>();

  // endregion

  public HaloRenderer(RenderHelper renderHelper, Campaign campaign, Zone zone) {
    this.renderHelper = renderHelper.withTimerPrefix("HaloRenderer");
    this.campaign = campaign;
    this.zone = zone;

    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  private void gridChanged(GridChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    haloOutlineAreaMap.clear();
    haloMiniShapeMap.clear();
    haloGridShapeCache = null;
  }

  /**
   * Loop through each {@link Halo} on the token.
   *
   * @param g2d where to paint
   * @param token the token which may have halos
   * @param position the token's position
   * @param view used for GMs viewing as a player
   */
  public void renderHalos(
      Graphics2D g2d, Token token, ZoneViewModel.TokenPosition position, PlayerView view) {

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-renderHalos");

    var tokenHalos = token.getHalos();
    if (token.getHaloColor() == null && tokenHalos.isEmpty()) {
      timer.stop("HaloRenderer-renderHalos");
      return;
    }

    var grid = zone.getGrid();
    if (grid == null) {
      timer.stop("HaloRenderer-renderHalos");
      return;
    }

    if (MapTool.getPlayer().isGM()) {
      // as we have not actually determined halo visibility yet, only provide this for GMs!
      timer.increment("HaloRenderer-renderHalos:tokensWithHalos");
    }

    // used to help determine concentric offsets for multiple halos
    ArrayList<Integer> renderableHaloMaxWidths = new ArrayList<>();
    int renderableInnerHaloMaxWidth = 0;

    CategorizedHalos categorizedHalos = campaign.getCategorizedHalos();

    var haloLineWidthPreference =
        AppPreferences.haloLineWidth.get() < 0 ? 0 : AppPreferences.haloLineWidth.get();

    timer.start("HaloRenderer-renderHalos:prepareToRender");
    // Loop through the token's halos and first determine whether they actually need to be rendered.
    // If so, get the halo's associated halo parts and establish where concentrically they need to
    // be
    // rendered and store them for later as we will render these halos in reverse order and their
    // respective halo parts in order.
    var renderableHalos = new ArrayList<ArrayList<RenderablePart>>();
    if (!tokenHalos.isEmpty()) {
      // loop through the halos attached to the token
      for (GUID id : tokenHalos) {
        Halo halo = categorizedHalos.getHalo(id);

        if (halo != null) {
          final int haloFacingAngle = halo.isFacingWithToken() ? token.getFacingInDegrees() : 0;

          int maxHaloPartWidth = 0;

          // should we render the halo?
          if ((MapTool.getPlayer().isGM() && view.isGMView())
              || (!halo.isGMOnly() && MapTool.getPlayer().isGM() && !view.isGMView())
              || (halo.isOwnerOnly() && AppUtil.playerOwns(token))
              || (!halo.isOwnerOnly() && !halo.isGMOnly())) {

            timer.increment("HaloRenderer-renderHalos:renderableHalos");

            double haloScaleFactor;
            if (halo.isScaleWithToken()) {
              haloScaleFactor =
                  Math.min(
                          position.footprintBounds().getBounds().getHeight(),
                          position.footprintBounds().getBounds().getWidth())
                      / grid.getSize();
            } else {
              haloScaleFactor = 1d;
            }

            // loop through the individual halo parts in each halo
            var haloParts = halo.getHaloParts();
            var renderableHaloParts = new ArrayList<RenderablePart>();
            for (HaloPart hp : haloParts) {

              /*
              Concentrically offset each halo part around the token based on:
              1. whether the halo is an 'inner' i.e. innermost concentrically
              2. the supplied halo part offset scaled to the token
              3. the maximum widths rendered halos thus far
              4. the number of gaps between rendered halos
               */
              double concentricOffset;
              if (halo.isInner()) {
                concentricOffset = 2d * (hp.getOffset() * haloScaleFactor);
              } else {
                concentricOffset =
                    2d
                        * (hp.getOffset() * haloScaleFactor
                            + renderableHaloMaxWidths.stream().reduce(0, Integer::sum)
                            + renderableHaloMaxWidths.size() * haloLineWidthPreference);
              }

              var width = hp.getWidth() == null ? haloLineWidthPreference : hp.getWidth();

              var shapeType =
                  Objects.requireNonNullElse(
                      hp.getHaloShapeType(), HaloPart.DEFAULT_HALO_SHAPE_TYPE);
              if (shapeType == HaloPart.HaloShapeType.TOKEN) {
                shapeType =
                    switch (token.getShape()) {
                      case CIRCLE, TOP_DOWN -> HaloPart.HaloShapeType.CIRCLE;
                      case SQUARE, FIGURE -> HaloPart.HaloShapeType.GRID;
                    };
              }

              renderableHaloParts.add(
                  new RenderablePart(
                      halo,
                      hp,
                      shapeType,
                      haloFacingAngle,
                      width,
                      concentricOffset,
                      haloScaleFactor));

              // determine the maximum width we have rendered thus far for this halo
              maxHaloPartWidth =
                  Math.max(
                      maxHaloPartWidth,
                      (int) Math.ceil((width * hp.getScaleY() + hp.getOffset()) * haloScaleFactor));
            }
            renderableHalos.add(renderableHaloParts);

            // add to the relevant max width accumulators
            if (maxHaloPartWidth != 0) {
              if (halo.isInner()) {
                renderableInnerHaloMaxWidth =
                    Math.max(maxHaloPartWidth, renderableInnerHaloMaxWidth);
              } else {
                renderableHaloMaxWidths.add(maxHaloPartWidth);
              }
            }
          }
        }
      }
    }
    timer.stop("HaloRenderer-renderHalos:prepareToRender");

    // Render the halos in reverse order but their respective halo parts in order.  This is so
    // any filled outer-concentric halos do not graffiti over inner-concentric halos and also
    // halo parts are rendered in the order in which they are written in the halo syntax.
    timer.start("HaloRenderer-renderHalos:orderedRendering");
    for (var renderableHaloParts : renderableHalos.reversed()) {
      for (var renderableHaloPart : renderableHaloParts) {
        var halo = renderableHaloPart.halo();

        int offsetConcentricByInner;
        if (halo.isInner() || renderableInnerHaloMaxWidth <= 0) {
          offsetConcentricByInner = 0;
        } else {
          offsetConcentricByInner = 2 * (renderableInnerHaloMaxWidth + haloLineWidthPreference);
        }

        renderHaloPart(
            g2d,
            token,
            position,
            grid,
            renderableHaloPart.part(),
            renderableHaloPart.shapeType(),
            renderableHaloPart.lineWidth(),
            renderableHaloPart.facingAngle(),
            renderableHaloPart.scaleFactor(),
            renderableHaloPart.concentricOffset() + offsetConcentricByInner,
            halo.isFlipWithToken());
      }
    }
    timer.stop("HaloRenderer-renderHalos:orderedRendering");

    // finally, render any legacy halo in the outermost position
    if (token.getHaloColor() != null) {
      timer.start("HaloRenderer-renderHalos:renderSimpleHalo");
      DrawableColorPaint dcp = new DrawableColorPaint(token.getHaloColor());
      HaloPart simpleHaloPart =
          new HaloPart(
              dcp,
              HaloPart.HaloShapeType.GRID,
              haloLineWidthPreference,
              null,
              false,
              false,
              false,
              0,
              0,
              0,
              1d,
              1d,
              0,
              0,
              0,
              0,
              0,
              0);
      double concentricOffset =
          2d
              * (renderableHaloMaxWidths.stream().reduce(0, Integer::sum)
                  + renderableHaloMaxWidths.size() * haloLineWidthPreference);
      int offsetConcentricByInner =
          renderableInnerHaloMaxWidth > 0
              ? 2 * (renderableInnerHaloMaxWidth + haloLineWidthPreference)
              : 0;
      var haloFacingAngle = 0;
      var scaleFactor = 1d;
      renderHaloPart(
          g2d,
          token,
          position,
          grid,
          simpleHaloPart,
          HaloPart.HaloShapeType.GRID,
          haloLineWidthPreference,
          haloFacingAngle,
          scaleFactor,
          concentricOffset + offsetConcentricByInner,
          false);
      timer.stop("HaloRenderer-renderHalos:renderSimpleHalo");
    }

    timer.stop("HaloRenderer-renderHalos");
  }

  /**
   * Render the individual {@code haloPart}.
   *
   * @param g2d where to paint
   * @param token the token which has this haloPart
   * @param position the token's position
   * @param grid the map's grid
   * @param haloPart the haloPart itself
   * @param haloShapeType the halo shape type
   * @param lineWidth the halo line width
   * @param haloFacingAngle the halo facing angle
   * @param haloScaleFactor the halo scale factor
   * @param concentricOffset the distance to offset the next haloPart
   * @param flipWithToken {@code true} if the halo should be flipped as the token is flipped.
   */
  private void renderHaloPart(
      Graphics2D g2d,
      Token token,
      ZoneViewModel.TokenPosition position,
      Grid grid,
      HaloPart haloPart,
      HaloPart.HaloShapeType haloShapeType,
      int lineWidth,
      int haloFacingAngle,
      double haloScaleFactor,
      double concentricOffset,
      boolean flipWithToken) {

    var timer = CodeTimer.get();
    timer.increment("HaloRenderer-renderHaloPart");
    timer.start("HaloRenderer-renderHaloPart");

    // get the unit shape, polygon, and star shapes have vertices; angle base shapes have an angle.

    // count how many of each type of halo part we are rendering
    timer.increment(
        String.format("HaloRenderer-renderHaloPart:%s", haloShapeType.name().toLowerCase()));
    // get the shape of the halo part
    Shape maskShape = null;
    Shape haloPartShape =
        switch (haloShapeType) {
          /* TOKEN case will never be hit as we should have already overwritten that to GRID or
           * CIRCLE previously. */
          case TOKEN -> null;
          case HaloPart.HaloShapeType.FOOTPRINT -> // tokens on gridless maps do not have footprints
              grid.getType().isNone() ? null : getHaloFootprintShape(token, grid, concentricOffset);
          case HaloPart.HaloShapeType.OUTLINE -> getHaloOutlineShape(token);
          case HaloPart.HaloShapeType.MBL ->
              token.getTransformedMaskTopology(zone, Zone.TopologyType.MBL);
          case HaloPart.HaloShapeType.VBLCOVER ->
              token.getTransformedMaskTopology(zone, Zone.TopologyType.COVER_VBL);
          case HaloPart.HaloShapeType.VBLHILL ->
              token.getTransformedMaskTopology(zone, Zone.TopologyType.HILL_VBL);
          case HaloPart.HaloShapeType.VBLPIT ->
              token.getTransformedMaskTopology(zone, Zone.TopologyType.PIT_VBL);
          case HaloPart.HaloShapeType.VBLWALL ->
              token.getTransformedMaskTopology(zone, Zone.TopologyType.WALL_VBL);
          case HaloPart.HaloShapeType.GRID ->
              getHaloGridShape(position, grid, haloPart, haloFacingAngle, concentricOffset);
          case CIRCLE, ARC, PIE, CHORD, TRIANGLE, SQUARE, POLYGON, STAR -> {
            timer.start("HaloRenderer-renderHaloPart:unitShapePrep");
            Shape unitShape =
                getUnitShape(haloShapeType, haloPart.getVertices(), haloPart.getAngle());
            timer.stop("HaloRenderer-renderHaloPart:unitShapePrep");

            if (haloPart.getMini() > 0) {
              yield getHaloMiniShape(
                  token,
                  position,
                  grid,
                  haloPart,
                  unitShape,
                  haloFacingAngle,
                  lineWidth,
                  haloScaleFactor,
                  concentricOffset,
                  flipWithToken);
            } else {
              var geometricTransform =
                  getHaloGeometricTransform(
                      position, grid, haloPart, haloFacingAngle, concentricOffset, flipWithToken);
              var shape = geometricTransform.createTransformedShape(unitShape);

              if (haloShapeType == HaloPart.HaloShapeType.ARC) {
                maskShape =
                    geometricTransform.createTransformedShape(
                        getUnitShape(HaloPart.HaloShapeType.CIRCLE, 0, 0));
              }

              yield shape;
            }
          }
        };

    if (haloPartShape != null) {
      final Area mask = maskShape == null ? new Area(haloPartShape) : new Area(maskShape);

      renderHelper.render(
          g2d,
          worldG -> {
            paintHaloPart(
                worldG, haloPartShape, mask, haloPart, haloShapeType, lineWidth, haloScaleFactor);
          });
    }

    timer.stop("HaloRenderer-renderHaloPart");
  }

  private AffineTransform getHaloGeometricTransform(
      ZoneViewModel.TokenPosition position,
      Grid grid,
      HaloPart haloPart,
      int haloFacingAngle,
      double concentricAdjustment,
      boolean flipWithToken) {

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getHaloTransformation");

    // transform the shape
    double scaleX;
    double scaleY;
    double footprintScaleX = position.footprintBounds().getWidth() + concentricAdjustment;
    double footprintScaleY = position.footprintBounds().getHeight() + concentricAdjustment;
    if (grid.getType().isIsometric() || grid.getType().isHex()) {
      scaleX = haloPart.getScaleX() * Math.min(footprintScaleX, footprintScaleY);
      scaleY = haloPart.getScaleY() * Math.min(footprintScaleX, footprintScaleY);
    } else {
      scaleX = haloPart.getScaleX() * footprintScaleX;
      scaleY = haloPart.getScaleY() * footprintScaleY;
    }
    double translateX = position.transformedBounds().getBounds2D().getCenterX();
    double translateY = position.transformedBounds().getBounds2D().getCenterY();

    boolean flipH = (flipWithToken && position.token().isFlippedX()) ^ haloPart.getFlipHorizontal();
    boolean flipV = (flipWithToken && position.token().isFlippedY()) ^ haloPart.getFlipVertical();

    double rotate;
    boolean rotateBeforeScale = false;
    if (flipWithToken && position.token().getIsFlippedIso()) {
      rotate =
          position.token().getShape().equals(Token.TokenShape.TOP_DOWN)
              ? haloPart.getRotate() + 45d
              : haloPart.getRotate() - 45d;
      scaleX = scaleX * Math.sqrt(2);
      scaleY = scaleY * Math.sqrt(2) / 2;
      rotateBeforeScale = !position.token().getShape().equals(Token.TokenShape.TOP_DOWN);
    } else {
      rotate = haloPart.getRotate();
    }

    var at =
        buildTransform(
            scaleX,
            scaleY,
            flipH,
            flipV,
            rotate + haloFacingAngle,
            translateX,
            translateY,
            rotateBeforeScale);

    timer.stop("HaloRenderer-getHaloTransformation");
    return at;
  }

  /**
   * Build and apply all the transforms we need for basic shapes
   *
   * @param scaleX scale in the x dimension
   * @param scaleY scale in the y dimension
   * @param flipH flip horizontally
   * @param flipV flip vertically
   * @param rotate rotate
   * @param translateX translate in the x dimension
   * @param translateY translate in the x dimension
   * @param rotateBeforeScale controls sequencing of scale and rotation (for isometric)
   * @return the combined transforms
   */
  private AffineTransform buildTransform(
      double scaleX,
      double scaleY,
      boolean flipH,
      boolean flipV,
      double rotate,
      double translateX,
      double translateY,
      boolean rotateBeforeScale) {
    // (*) note that transforms are applied in the reverse order to which they are added below
    AffineTransform at = new AffineTransform();
    // (*) the transform below this comment will be applied last
    if (translateX != 0 || translateY != 0) {
      at.translate(translateX, translateY);
    }
    // for isometric rotate before (*) scale , otherwise scale before (*) rotate
    if (rotateBeforeScale) {
      if (scaleX != 0 || scaleY != 0) {
        at.scale(scaleX, scaleY);
      }
      if (rotate % 360 != 0) {
        at.rotate(rotate * Math.PI / 180d);
      }
    } else {
      if (rotate % 360 != 0) {
        at.rotate(rotate * Math.PI / 180d);
      }
      if (scaleX != 0 || scaleY != 0) {
        at.scale(scaleX, scaleY);
      }
    }
    if (flipH) {
      at.scale(1, -1);
    }
    if (flipV) {
      at.scale(-1, 1);
    }
    // (*) the transform above this comment will be applied first
    return at;
  }

  /**
   * Generate a shape, with a number of vertices lying on a circumscribed circle (circumcircle) for
   * a given polygon inscribed circle (incircle) of radius (inradius), which can be scaled and
   * positioned later as needed.
   *
   * <ul>
   *   <li>regular polygon
   *   <li>star polygon with a polygon density of 2
   *
   * @param isStar {@code true} if the polygon should be a star polygon.
   * @param vertices the number of polygon vertices or star outer-vertices
   * @return the unit polygon shape
   */
  private Shape getUnitPolygonShape(boolean isStar, Integer vertices) {
    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getUnitPolygonShape");

    timer.increment("HaloRenderer-getUnitPolygonShape");
    double inradius = 1 / 2d;
    // a circumscribed circle passes through all polygon outer-vertices
    double circumradius = inradius / Math.cos(Math.PI / vertices);
    double radius = circumradius;

    double orientationAngle = 0d;
    // orientate regular polygons to have a flat top, otherwise a pointy top for stars
    if (!isStar) {
      orientationAngle = Math.PI / vertices;
    }

    // create the shape by iterating around the number of points
    Path2D.Double shapePath = new Path2D.Double();
    for (int i = 0; i < vertices; i++) {
      double theta; // the angle required move round to the next point
      if (isStar) {
        // with polygon density of 2 the next point will be the one after next, but...
        // ...even numbered stars need to be rotated when halfway to avoid repetition
        if (vertices % 2 == 0 && i == vertices / 2) {
          orientationAngle = orientationAngle + 2 * Math.PI / vertices;
        }
        theta = (4 * Math.PI * i / vertices) - orientationAngle;
      } else {
        // for regular polygons the next point is adjacent clockwise
        theta = (2 * Math.PI * i / vertices) - orientationAngle;
      }
      double x = radius * Math.sin(theta);
      double y = radius * -Math.cos(theta);

      if (i == 0) {
        shapePath.moveTo(x, y);
      } else if (isStar && (vertices % 2 == 0 && i == vertices / 2)) {
        shapePath.closePath();
        shapePath.moveTo(x, y);
      } else {
        shapePath.lineTo(x, y);
      }
    }
    shapePath.closePath();

    timer.stop("HaloRenderer-getUnitPolygonShape");
    return shapePath;
  }

  /**
   * Get a scaled and positioned haloPart mini-shape, composed of a number of miniature shapes
   * rotated and spread equidistant around a circle.
   *
   * @return the polygon shape
   * @see HaloPart.HaloShapeType
   */
  private Shape getHaloMiniShape(
      Token token,
      ZoneViewModel.TokenPosition position,
      Grid grid,
      HaloPart haloPart,
      Shape unitShape,
      int haloFacingAngle,
      int lineWidth,
      double haloScaleFactor,
      double concentricAdjustment,
      boolean flipWithToken) {

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getHaloMiniShape");

    timer.start("HaloRenderer-getHaloMiniShape:unitShapePrep");
    // scale unit shape to the required miniature size
    double scaleUnitX = lineWidth * haloScaleFactor * haloPart.getScaleX();
    double scaleUnitY = lineWidth * haloScaleFactor * haloPart.getScaleY();
    Shape scaledUnitShape =
        AffineTransform.getScaleInstance(scaleUnitX, scaleUnitY).createTransformedShape(unitShape);

    // get some other attributes we need
    int mini = haloPart.getMini(); // the number of miniature shapes
    int miniStart = haloPart.getMiniStart() == 0 ? 0 : haloPart.getMiniStart() - 1; // when to start
    int miniStop = haloPart.getMiniStop() == 0 ? mini : haloPart.getMiniStop(); // when to stop
    double miniRotate = haloPart.getMiniRotate();
    double miniSpin =
        haloPart
            .getMiniSpin(); // rotation multiplier as mini it revolves (0 is static, 1 is default)
    TokenFootprint footprint = token.getFootprint(grid);
    double radius = grid.getSize() / 2d;

    double scaleX;
    double scaleY;
    double footprintScaleX =
        (position.footprintBounds().getWidth() + concentricAdjustment) / grid.getSize();
    double footprintScaleY =
        (position.footprintBounds().getHeight() + concentricAdjustment) / grid.getSize();
    if (grid.getType().isIsometric() || grid.getType().isHex()) {
      scaleX = Math.min(footprintScaleX, footprintScaleY);
      scaleY = Math.min(footprintScaleX, footprintScaleY);
    } else {
      scaleX = footprintScaleX;
      scaleY = footprintScaleY;
    }
    timer.stop("HaloRenderer-getHaloMiniShape:unitShapePrep");

    timer.start("HaloRenderer-getHaloMiniShape:generateKey");
    CompositeHaloMiniShapeKey key =
        new CompositeHaloMiniShapeKey(
            footprint, haloPart, scaleUnitX, scaleUnitY, radius, concentricAdjustment);
    timer.stop("HaloRenderer-getHaloMiniShape:generateKey");

    timer.start("HaloRenderer-getHaloMiniShape:compositeArea");
    // revolve the mini shape around the radius, rotate, and then and combine
    Area compositeArea =
        haloMiniShapeMap.computeIfAbsent(
            key,
            key2 -> {
              timer.increment("HaloRenderer-getHaloMiniShape:haloMiniShapeMap");

              Area area = new Area();
              // iterate through the mini halo shapes
              for (int i = miniStart; i < miniStop; i++) {
                double theta = (2 * Math.PI * i / mini);
                double x = radius * Math.sin(theta);
                double y = radius * -Math.cos(theta);
                double x1 = x * scaleX;
                double y1 = y * scaleY;

                Shape revolvedShape =
                    AffineTransform.getTranslateInstance(x1, y1)
                        .createTransformedShape(scaledUnitShape);
                Shape rotatedShape =
                    AffineTransform.getRotateInstance(
                            theta * miniSpin + miniRotate * Math.PI / 180, x1, y1)
                        .createTransformedShape(revolvedShape);
                Area singleArea = new Area(rotatedShape);
                area.add(singleArea);
              }
              return area;
            });
    timer.stop("HaloRenderer-getHaloMiniShape:compositeArea");

    // position the composite area to the token, and transform as required
    timer.start("HaloRenderer-getHaloMiniShape:transform");
    double translateX = position.transformedBounds().getBounds2D().getCenterX();
    double translateY = position.transformedBounds().getBounds2D().getCenterY();
    boolean flipH = flipWithToken && token.isFlippedX() ^ haloPart.getFlipHorizontal();
    boolean flipV = flipWithToken && token.isFlippedY() ^ haloPart.getFlipVertical();

    double rotate;
    double transformScaleX;
    double transformScaleY;

    boolean rotateBeforeScale = false;
    if (flipWithToken && token.getIsFlippedIso()) {
      rotate =
          token.getShape().equals(Token.TokenShape.TOP_DOWN)
              ? haloPart.getRotate()
              : haloPart.getRotate() - 45d;
      transformScaleX = Math.sqrt(2);
      transformScaleY = Math.sqrt(2) / 2;
      rotateBeforeScale = !token.getShape().equals(Token.TokenShape.TOP_DOWN);
    } else {
      rotate = haloPart.getRotate();
      transformScaleX = 1d;
      transformScaleY = 1d;
    }

    var at =
        buildTransform(
            transformScaleX,
            transformScaleY,
            flipH,
            flipV,
            haloFacingAngle + rotate,
            translateX,
            translateY,
            rotateBeforeScale);
    Shape finalHaloMiniShape = at.createTransformedShape(compositeArea);

    timer.stop("HaloRenderer-getHaloMiniShape:transform");
    timer.stop("HaloRenderer-getHaloMiniShape");

    return finalHaloMiniShape;
  }

  /**
   * Generate a shape of unit size, which can be scaled and positioned later as needed.
   *
   * @param haloShapeType the halo shape type
   * @param vertices the number of vertices (if applicable)
   * @param angle the angle (if applicable)
   * @return the unit shape
   */
  private Shape getUnitShape(HaloPart.HaloShapeType haloShapeType, int vertices, double angle) {

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getUnitShape");

    CompositeUnitShapeKey key = new CompositeUnitShapeKey(haloShapeType, vertices, angle);

    Shape returnUnitShape =
        haloUnitShapeMap.computeIfAbsent(
            key,
            key2 -> {
              double r = 1d;
              return switch (haloShapeType) {
                case HaloPart.HaloShapeType.CIRCLE ->
                    // rotated so the circle path starts at the 12 o'clock position
                    AffineTransform.getRotateInstance(-Math.PI / 2, 0, 0)
                        .createTransformedShape(new Ellipse2D.Double(-r / 2, -r / 2, r, r));

                // start angles = 90 so path starts from 12 o'clock position
                // extent angles is negative to force clockwise direction
                case HaloPart.HaloShapeType.ARC ->
                    new Arc2D.Double(-r / 2, -r / 2, r, r, 90, -angle, Arc2D.OPEN);
                case HaloPart.HaloShapeType.CHORD ->
                    new Arc2D.Double(-r / 2, -r / 2, r, r, 90, -angle, Arc2D.CHORD);
                case HaloPart.HaloShapeType.PIE ->
                    new Arc2D.Double(-r / 2, -r / 2, r, r, 90, -angle, Arc2D.PIE);

                case HaloPart.HaloShapeType.TRIANGLE ->
                    AffineTransform.getRotateInstance(Math.PI, 0, 0)
                        .createTransformedShape(getUnitPolygonShape(false, 3));

                case HaloPart.HaloShapeType.SQUARE ->
                    // this could also have been done as getUnitPolygonShape with 4 vertices
                    new Rectangle2D.Double(-r / 2, -r / 2, r, r);

                case HaloPart.HaloShapeType.POLYGON -> getUnitPolygonShape(false, vertices);

                case HaloPart.HaloShapeType.STAR -> getUnitPolygonShape(true, vertices);

                // None of the following are a geometric type, so this method shouldn't even be
                // used for them.
                case FOOTPRINT, GRID, TOKEN, OUTLINE, MBL, VBLCOVER, VBLHILL, VBLPIT, VBLWALL ->
                    new Area();
              };
            });

    timer.stop("HaloRenderer-getUnitShape");
    return returnUnitShape;
  }

  /**
   * Get a scaled and positioned halo shape based on the token footprint's occupied cells.
   *
   * @param token the token that needs the halo
   * @param grid the grid
   * @param concentricAdjustment offset from the centre
   * @return the halo shape
   */
  private Shape getHaloFootprintShape(Token token, Grid grid, double concentricAdjustment) {

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getHaloFootprintShape");

    timer.start("HaloRenderer-getHaloFootprintShape:gridShape");
    Shape gridShape = grid.getCellShape();
    Area compositeArea = new Area();
    double scaleX = 1d + concentricAdjustment / grid.getCellWidth();
    double scaleY = 1d + concentricAdjustment / grid.getCellHeight();
    Shape scaledShape =
        AffineTransform.getScaleInstance(scaleX, scaleY).createTransformedShape(gridShape);
    timer.stop("HaloRenderer-getHaloFootprintShape:gridShape");

    timer.start("HaloRenderer-getHaloFootprintShape:compositeArea");
    Set<CellPoint> cps = token.getOccupiedCells(grid);
    for (CellPoint cp : cps) {
      ZonePoint zp = grid.convert(cp);
      Shape translatedShape =
          AffineTransform.getTranslateInstance(zp.x, zp.y).createTransformedShape(scaledShape);
      Area areaCell = new Area(translatedShape);
      compositeArea.add(areaCell);
    }
    timer.stop("HaloRenderer-getHaloFootprintShape:compositeArea");

    timer.start("HaloRenderer-getHaloFootprintShape:transform");
    double gridCellOffsetX = grid.getCellOffset().getWidth();
    double gridCellOffsetY = grid.getCellOffset().getHeight();
    Shape translatedCompositeArea =
        AffineTransform.getTranslateInstance(
                gridCellOffsetX - concentricAdjustment / 2d,
                gridCellOffsetY - concentricAdjustment / 2d)
            .createTransformedShape(compositeArea);
    timer.stop("HaloRenderer-getHaloFootprintShape:transform");

    timer.stop("HaloRenderer-getHaloFootprintShape");

    return translatedCompositeArea;
  }

  /**
   * Get a scaled and positioned <code>haloPart</code> shape based on the grid.
   *
   * @param position the token's position
   * @param grid the grid
   * @param haloPart the haloPart
   * @param concentricAdjustment offset from the centre
   * @param haloFacingAngle the halo facing angle
   * @return the grid shape, or a circle for gridless
   */
  private Shape getHaloGridShape(
      ZoneViewModel.TokenPosition position,
      Grid grid,
      HaloPart haloPart,
      int haloFacingAngle,
      double concentricAdjustment) {

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getHaloGridShape");

    if (haloGridShapeCache == null) {
      if (grid.getType().isNone()) {
        double r = grid.getSize() / 2d;
        haloGridShapeCache = new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
      } else {
        haloGridShapeCache = grid.getCellShape();
        haloGridShapeCache =
            AffineTransform.getTranslateInstance(
                    -haloGridShapeCache.getBounds2D().getCenterX(),
                    -haloGridShapeCache.getBounds2D().getCenterY())
                .createTransformedShape(haloGridShapeCache);
      }
    }

    double scaleX = (position.footprintBounds().getWidth() + concentricAdjustment) / grid.getSize();
    double scaleY =
        (position.footprintBounds().getHeight() + concentricAdjustment) / grid.getSize();
    double scale;
    if (grid.getType().isIsometric() || grid.getType().isHex()) {
      scale = Math.min(scaleX, scaleY);
    } else {
      scale = Math.max(scaleX, scaleY);
    }
    double translateX = position.transformedBounds().getBounds2D().getCenterX();
    double translateY = position.transformedBounds().getBounds2D().getCenterY();

    var at =
        buildTransform(
            scale,
            scale,
            haloPart.getFlipHorizontal(),
            haloPart.getFlipVertical(),
            haloFacingAngle + haloPart.getRotate(),
            translateX,
            translateY,
            false);

    Shape transformedHaloShape = at.createTransformedShape(haloGridShapeCache);

    timer.stop("HaloRenderer-getHaloGridShape");
    return transformedHaloShape;
  }

  /**
   * Get a halo shape based on the outline of the token's image asset.
   *
   * @param token the token which needs a halo
   * @return Area as a Shape
   */
  private Shape getHaloOutlineShape(Token token) {
    // no concentricAdjustment for HaloPart Outline shapes currently...

    var timer = CodeTimer.get();
    timer.start("HaloRenderer-getHaloOutlineShape");

    Area tokenOutline =
        haloOutlineAreaMap.computeIfAbsent(
            token.getImageAssetId(),
            id2 -> {
              return TokenVBL.createOptimizedTopologyArea(
                  token,
                  10,
                  true,
                  new Color(0, 0, 0, 0),
                  2,
                  TokenVBL.JTS_SimplifyMethodType.DOUGLAS_PEUCKER_SIMPLIFIER.name());
            });

    timer.stop("HaloRenderer-getHaloOutlineShape");
    return token.getTransformedMaskTopology(zone, tokenOutline);
  }

  /**
   * Paint the <code>haloPart</code> using the width, color, and dashed pattern where supplied.
   *
   * @param g2d where to paint
   * @param paintShape the shape to paint which has been scaled and positioned
   * @param mask the shape to mask out parts of {@code paintShape}, e.g., the interior of the halo.
   * @param haloPart the haloPart itself
   */
  private void paintHaloPart(
      Graphics2D g2d,
      Shape paintShape,
      Area mask,
      HaloPart haloPart,
      HaloPart.HaloShapeType haloShapeType,
      int lineWidth,
      double haloScaleFactor) {
    var timer = CodeTimer.get();
    timer.start("HaloRenderer-paintHaloPart");

    // region 1. determine the haloPart colors including alpha
    timer.start("HaloRenderer-paintHaloPart:color");
    Color color;
    if (haloPart.getPaint() instanceof DrawableColorPaint dcp) {
      boolean hasAlpha = (dcp.getColor() & 0xFF000000) != 0xFF000000;
      color = new Color(dcp.getColor(), hasAlpha);
    } else {
      color = new Color(255, 255, 255, 125);
    }
    Color bgColor = color;
    timer.stop("HaloRenderer-paintHaloPart:color");
    // endregion

    boolean doStroke = true;
    boolean doFill = haloPart.getFill();
    boolean doClip = true;

    if (haloPart.getMini() > 0) {
      doStroke = false;
      doFill = true;
      lineWidth = 1;
    }

    if (doFill) {
      // no need for clippin' if we are a fillin'
      doClip = false;
    }

    Stroke haloStroke;
    if (doStroke) {
      // region 2. determine the haloPart stroke
      timer.start("HaloRenderer-paintHaloPart:basicStroke");

      // double width stroke thickness because we will clip the inside half
      float strokeWidth = (float) (2f * lineWidth * Math.max(1d, haloScaleFactor));

      haloStroke = new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
      timer.stop("HaloRenderer-paintHaloPart:basicStroke");

      timer.start("HaloRenderer-paintHaloPart:dashedPattern");
      ArrayList<Float> dashedPattern = haloPart.getDashedPattern();
      if (dashedPattern != null) {
        // Could be dashing
        if (!dashedPattern.isEmpty()) {
          // Definitely dashing
          int arraySize = dashedPattern.size();
          float dashPhase = 0f;

          // The dash array takes pairs of float values, but if an odd number provided
          // shrink the array size and use the last number for the dash phase
          if (Math.floorMod(arraySize, 2) == 1) {
            arraySize = arraySize - 1;
            dashPhase = (float) (dashedPattern.get(arraySize) * haloScaleFactor);
          }
          float[] dash = new float[arraySize];
          for (int i = 0; i < arraySize; i++) {
            dash[i] = (float) (dashedPattern.get(i) * haloScaleFactor);
          }
          haloStroke =
              new BasicStroke(
                  strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dash, dashPhase);
        }
      }
      timer.stop("HaloRenderer-paintHaloPart:dashedPattern");
      // endregion

      Shape originalClip = null;
      // region 3. clipping and drawing
      if (doClip) {
        timer.start("HaloRenderer-paintHaloPart:clip-%s", haloShapeType.name());
        originalClip = g2d.getClip();
        Area bounds = new Area(g2d.getClipBounds());
        bounds.subtract(mask);
        g2d.setClip(bounds);
        timer.stop("HaloRenderer-paintHaloPart:clip-%s", haloShapeType.name());
      }

      timer.start("HaloRenderer-paintHaloPart:draw-%s", haloShapeType.name());
      g2d.setColor(color);
      g2d.setStroke(haloStroke);
      g2d.draw(paintShape);
      timer.stop("HaloRenderer-paintHaloPart:draw-%s", haloShapeType.name());

      if (doClip) {
        timer.start("HaloRenderer-paintHaloPart:resetClip-%s", haloShapeType.name());
        g2d.setClip(originalClip);
        timer.stop("HaloRenderer-paintHaloPart:resetClip-%s", haloShapeType.name());
      }
      // endregion
    }

    // region 4. filling
    if (doFill) {
      timer.start("HaloRenderer-paintHaloPart:fill-%s", haloShapeType.name());
      g2d.setColor(bgColor);
      g2d.fill(paintShape);
      timer.stop("HaloRenderer-paintHaloPart:fill-%s", haloShapeType.name());
    }
    // endregion

    timer.stop("HaloRenderer-paintHaloPart");
  }

  /**
   * Keys for the mini halo shape multikey map cache.
   *
   * @param key1 the token footprint
   * @param key2 the halo part
   * @param key3 the scaleUnitX
   * @param key4 the scaleUnitY
   * @param key5 the radius
   * @param key6 the concentric offset adjustment
   */
  private record CompositeHaloMiniShapeKey(
      TokenFootprint key1, HaloPart key2, Double key3, Double key4, Double key5, Double key6) {}

  /**
   * Keys for the unit shape multikey map cache.
   *
   * @param key1 the halo shape type
   * @param key2 the number of halo vertices (will be 0 for non-polygonal shapes)
   * @param key3 the angle (will be 0 for non-angle based shapes)
   */
  private record CompositeUnitShapeKey(HaloPart.HaloShapeType key1, Integer key2, Double key3) {}

  private record RenderablePart(
      Halo halo,
      HaloPart part,
      HaloPart.HaloShapeType shapeType,
      int facingAngle,
      int lineWidth,
      double concentricOffset,
      double scaleFactor) {}
}
