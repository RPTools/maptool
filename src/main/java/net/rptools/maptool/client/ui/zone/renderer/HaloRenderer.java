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
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.zones.GridChanged;
import org.apache.commons.lang3.ArrayUtils;

public class HaloRenderer {
  private final RenderHelper renderHelper;
  private final Zone zone;

  // region These fields need to be recalculated whenever the grid changes.
  private final Map<Token.HaloShape, Shape> haloShapeMap = new HashMap<>();
  private final Map<Key, Shape> haloScaledShapeMultiKeyMap = new HashMap<>();
  private final Map<Token.HaloStyle, Stroke> haloStyleStrokeMap = new HashMap<>();
  private final Map<Integer, Stroke> haloNubStrokeMap = new HashMap<>();
  private final Map<GUID, Area> haloTopologyAreaMap = new HashMap<>();
  // endregion

  private Integer haloLineWidth = AppPreferences.haloLineWidth.get();

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

    haloShapeMap.clear();
    haloScaledShapeMultiKeyMap.clear();
    haloStyleStrokeMap.clear();
    haloNubStrokeMap.clear();
    haloTopologyAreaMap.clear();
  }

  /**
   * Determine the Shape of the halo for a token (if not a topology based halo).
   *
   * @param grid The grid.
   * @param tokenHaloShape The token's halo shape attribute.
   * @param tokenShape The token's shape attribute.
   * @return The halo shape.
   */
  private Shape getHaloShape(
      Grid grid, Token.HaloShape tokenHaloShape, Token.TokenShape tokenShape) {

    Token.HaloShape workingTokenHaloShape = tokenHaloShape;

    // Override the token halo shape attribute in certain cases
    if (tokenHaloShape.equals(Token.HaloShape.TOKEN)
        && tokenShape.equals(Token.TokenShape.CIRCLE)) {
      workingTokenHaloShape = Token.HaloShape.CIRCLE;
    } else if (tokenHaloShape.equals(Token.HaloShape.TOKEN)
        && tokenShape.equals(Token.TokenShape.SQUARE)) {
      workingTokenHaloShape = Token.HaloShape.SQUARE;
    }

    // Get/store the final halo shape
    final Token.HaloShape finalTokenHaloShape = workingTokenHaloShape;

    // Use cache so we do not have to establish the Shape every time
    return haloShapeMap.computeIfAbsent(
        finalTokenHaloShape,
        shape2 -> {
          if (finalTokenHaloShape.equals(Token.HaloShape.CIRCLE)) {
            double r = grid.getSize() / 2d;
            // Rotate circle shapes so any subsequent halo style nubs start at 12 o'clock.
            return AffineTransform.getRotateInstance(Math.PI / -2)
                .createTransformedShape(new Ellipse2D.Double(-r, -r, 2 * r, 2 * r));
          } else if (finalTokenHaloShape.equals(Token.HaloShape.SQUARE)) {
            double x = grid.getSize() / 2d;
            return new Rectangle2D.Double(-x, -x, 2 * x, 2 * x);
          } else if (finalTokenHaloShape.equals(Token.HaloShape.DIAMOND)) {
            double x = grid.getSize() / 2d;
            return AffineTransform.getRotateInstance(Math.PI / 4)
                .createTransformedShape(new Rectangle2D.Double(-x, -x, 2 * x, 2 * x));
          } else {
            if (GridFactory.getGridType(grid).equals(GridFactory.NONE)) {
              double r = grid.getSize() / 2d;
              return new Ellipse2D.Double(-r, -r, 2 * r, 2 * r);
            } else {
              Shape gridCellShape = grid.getCellShape();
              gridCellShape =
                  AffineTransform.getTranslateInstance(
                          -gridCellShape.getBounds2D().getCenterX(),
                          -gridCellShape.getBounds2D().getCenterY())
                      .createTransformedShape(gridCellShape);
              return gridCellShape;
            }
          }
        });
  }

  /**
   * Renders the halo for a token.
   *
   * <p>If a topology-based halo shape (including *BL) the {@link Area} and position are obtained
   * via {@link TokenVBL}, otherwise determine the halo shape and position.
   *
   * @param g2d Where to paint.
   * @param token Which token needs the halo.
   * @param position The token's position.
   */
  public void renderHalo(Graphics2D g2d, Token token, ZoneViewModel.TokenPosition position) {
    if (token.getHaloColor() == null) {
      return;
    }

    var grid = zone.getGrid();
    if (grid == null) {
      return;
    }

    Token.HaloShape tokenHaloShape = token.getHaloShape();
    Token.TokenShape tokenShape = token.getShape();
    Shape positionedPaintShape;

    // generate the positioned halo shape
    if (tokenHaloShape.equals(Token.HaloShape.TOPOLOGY)
        || (tokenHaloShape.equals(Token.HaloShape.TOKEN)
            && tokenShape.equals(Token.TokenShape.TOP_DOWN))) {

      // use cache for the halo shape and position based on the token's image topology area
      Area tokenTopology =
          haloTopologyAreaMap.computeIfAbsent(
              token.getId(),
              id2 -> {
                return TokenVBL.createOptimizedTopologyArea(
                    token,
                    10,
                    true,
                    new Color(0, 0, 0, 0),
                    2,
                    TokenVBL.JTS_SimplifyMethodType.DOUGLAS_PEUCKER_SIMPLIFIER.name());
              });
      positionedPaintShape = token.getTransformedMaskTopology(zone, tokenTopology);

    } else {

      // otherwise establish the halo shape
      Shape haloShape = getHaloShape(grid, tokenHaloShape, tokenShape);
      TokenFootprint fp = token.getFootprint(grid);

      // use cache so we don't have to resize halo shapes every time
      Key key = new Key(fp, tokenHaloShape, tokenShape);
      Shape paintShape =
          haloScaledShapeMultiKeyMap.computeIfAbsent(
              key,
              newKey -> {
                Shape scaledShape;
                double scaleX =
                    position.footprintBounds().getWidth() / haloShape.getBounds2D().getWidth();
                double scaleY =
                    position.footprintBounds().getHeight() / haloShape.getBounds2D().getHeight();

                if (tokenHaloShape.equals(Token.HaloShape.DIAMOND)
                    && !GridFactory.getGridType(grid).equals(GridFactory.ISOMETRIC)) {
                  // Enlarge diamond halos on non-isometric grids
                  double SQRT2 = Math.sqrt(2.0d);
                  scaleX = scaleX * SQRT2;
                  scaleY = scaleY * SQRT2;
                }

                boolean isStandardShape =
                    ((List.of(
                                Token.HaloShape.DIAMOND,
                                Token.HaloShape.SQUARE,
                                Token.HaloShape.CIRCLE)
                            .contains(tokenHaloShape))
                        || (Token.HaloShape.TOKEN.equals(tokenHaloShape)
                            && List.of(Token.TokenShape.SQUARE, Token.TokenShape.CIRCLE)
                                .contains(tokenShape)));

                if (isStandardShape) {
                  if (GridFactory.getGridType(grid).equals(GridFactory.HEX_VERT)) {
                    // Stretch the width on vertical hex grids
                    scaleX = scaleX * grid.getSecondDimension() / grid.getCellWidth();
                  } else if (GridFactory.getGridType(grid).equals(GridFactory.HEX_HORI)) {
                    // Stretch the height on horizontal hex grids
                    scaleY = scaleY * grid.getSecondDimension() / grid.getCellHeight();
                  }
                }

                return AffineTransform.getScaleInstance(scaleX, scaleY)
                    .createTransformedShape(haloShape);
              });

      // position the shape we are painting
      positionedPaintShape =
          AffineTransform.getTranslateInstance(
                  position.transformedBounds().getBounds2D().getCenterX(),
                  position.transformedBounds().getBounds2D().getCenterY())
              .createTransformedShape(paintShape);
    }

    // this will eventually hold forks for painting different types of halo
    renderHelper.render(
        g2d,
        worldG -> {
          paintLineHalo(worldG, token, grid, positionedPaintShape);
        });
  }

  /**
   * Paint the halo using a solid line (the default) or a styled line {@link Token.HaloStyle}.
   *
   * @param g2d Where to paint.
   * @param token Which token needs the halo.
   * @param grid Which grid.
   * @param paintShape The shape to paint.
   */
  private void paintLineHalo(Graphics2D g2d, Token token, Grid grid, Shape paintShape) {

    // If the haloLineWidth preference has changed, store its new value and clear the stroke cache
    if (!haloLineWidth.equals(AppPreferences.haloLineWidth.get())) {
      haloLineWidth = AppPreferences.haloLineWidth.get();
      haloStyleStrokeMap.clear();
    }

    // double width thickness because we will clip the inside half
    float thickness =
        (float) (2f * Math.min(1f, token.getFootprint(grid).getScale()) * haloLineWidth);

    // only paint if the line width says so, and use a cache for the strokes
    if (haloLineWidth > 0) {
      Token.HaloStyle haloStyle = token.getHaloStyle();
      Stroke haloStyleStroke =
          haloStyleStrokeMap.computeIfAbsent(
              haloStyle,
              haloStyle2 -> {
                Stroke newHaloStyleStroke;

                // dashing style strokes
                int basicStrokeCap = BasicStroke.CAP_BUTT;
                float[] pattern = null;
                if (haloStyle.equals(Token.HaloStyle.DASHED)) {
                  pattern = new float[] {2.5f * thickness, thickness / 2f};
                } else if (haloStyle.equals(Token.HaloStyle.DOTTED)) {
                  pattern = new float[] {thickness / 2f, thickness / 2f};
                } else if (haloStyle.equals(Token.HaloStyle.DASHED_DOTTED)) {
                  pattern =
                      new float[] {
                        2.5f * thickness, thickness / 2f, thickness / 2f, thickness / 2f
                      };
                } else if (haloStyle.equals(Token.HaloStyle.SPIKED)) {
                  pattern = new float[] {1.5f, thickness / 2f};
                } else if (haloStyle.equals(Token.HaloStyle.SPOTTED)) {
                  pattern = new float[] {0f, 2f * thickness};
                  basicStrokeCap = BasicStroke.CAP_ROUND;
                }

                if (pattern == null) {
                  // i.e. halo styles which do not have a dashing stroke defined above
                  newHaloStyleStroke = new BasicStroke(thickness);
                } else {
                  // 'tis a dashing halo
                  newHaloStyleStroke =
                      new BasicStroke(
                          thickness, basicStrokeCap, BasicStroke.JOIN_MITER, 1f, pattern, 0f);
                }
                return newHaloStyleStroke;
              });

      g2d.setColor(token.getHaloColor());
      g2d.setStroke(haloStyleStroke);
      Shape oldClip = g2d.getClip();
      Area a = new Area(g2d.getClipBounds());
      a.subtract(new Area(paintShape));
      g2d.setClip(a);
      g2d.draw(paintShape);

      // add any halo nubs
      int haloNubCount =
          switch (haloStyle) {
            case Token.HaloStyle.NUBBED_1 -> 1;
            case Token.HaloStyle.NUBBED_2 -> 2;
            case Token.HaloStyle.NUBBED_3 -> 3;
            case Token.HaloStyle.NUBBED_4 -> 4;
            case Token.HaloStyle.NUBBED_5 -> 5;
            case Token.HaloStyle.NUBBED_6 -> 6;
            case Token.HaloStyle.NUBBED_7 -> 7;
            case Token.HaloStyle.NUBBED_8 -> 8;
            case Token.HaloStyle.NUBBED_9 -> 9;
            case Token.HaloStyle.NUBBED_10 -> 10;
            default -> 0;
          };

      if (haloNubCount > 0) {
        Stroke haloNubStroke =
            haloNubStrokeMap.computeIfAbsent(
                haloNubCount,
                haloNubCount2 -> {
                  float[] haloNubPattern = null;
                  int haloBigNubCount = Math.floorDiv(haloNubCount, 5);
                  int haloSmallNubCount = Math.floorMod(haloNubCount, 5);
                  for (int i = 0; i < haloBigNubCount; i++) {
                    haloNubPattern =
                        ArrayUtils.addAll(
                            haloNubPattern, new float[] {4.5f * thickness, thickness / 2});
                  }
                  for (int i = 0; i < haloSmallNubCount; i++) {
                    haloNubPattern =
                        ArrayUtils.addAll(
                            haloNubPattern, new float[] {thickness / 2, thickness / 2});
                  }
                  // add a long dash gap afterwards
                  haloNubPattern =
                      ArrayUtils.addAll(haloNubPattern, new float[] {0f, 500f * thickness});

                  return new BasicStroke(
                      3f * thickness,
                      BasicStroke.CAP_BUTT,
                      BasicStroke.JOIN_MITER,
                      50f,
                      haloNubPattern,
                      0f);
                });

        g2d.setStroke(haloNubStroke);
        g2d.draw(paintShape);
      }

      g2d.setClip(oldClip);
    }
  }

  /**
   * Keys for the scaled halo shape multikey map cache.
   *
   * @param key1 the token's footprint
   * @param key2 the token's halo shape
   * @param key3 the token's token shape
   */
  private record Key(TokenFootprint key1, Token.HaloShape key2, Token.TokenShape key3) {

    @Override
    public boolean equals(Object obj) {
      if (!(obj
          instanceof Key(TokenFootprint akey1, Token.HaloShape akey2, Token.TokenShape akey3))) {
        return false;
      }
      return this.key1.equals(akey1) && this.key2.equals(akey2) && this.key3.equals(akey3);
    }

    @Override
    public int hashCode() {
      return key1.hashCode() ^ key2.hashCode() ^ key3.hashCode();
    }
  }
}
