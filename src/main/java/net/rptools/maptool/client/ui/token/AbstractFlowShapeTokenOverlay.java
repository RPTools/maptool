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
package net.rptools.maptool.client.ui.token;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.FlowShapeTokenOverlayDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract sealed class AbstractFlowShapeTokenOverlay extends BooleanTokenOverlay
    permits FlowColorDotTokenOverlay,
        FlowDiamondTokenOverlay,
        FlowColorSquareTokenOverlay,
        FlowYieldTokenOverlay,
        FlowTriangleTokenOverlay {
  private static final Logger log = LogManager.getLogger(AbstractFlowShapeTokenOverlay.class);
  private static final Color DEFAULT_COLOR = Color.RED;
  private static final int DEFAULT_GRID_SIZE = 3;

  /** Color used when filling the overlay shape. */
  private Color color;

  /**
   * @deprecated This is a hold over from when FlowColorDotTokenOverlay (and by extension the other
   *     flow shape overlays) inherited from XTokenOverlay. It didn't do anything then, and does
   *     nothing now.
   */
  @Deprecated private BasicStroke stroke;

  /** Size of the grid used to place a token with this state. */
  private int grid;

  /** Flow used to define position of states */
  private transient TokenOverlayFlow flow;

  public AbstractFlowShapeTokenOverlay(String name, Color color, int gridSize) {
    super(name);

    if (color == null) {
      color = DEFAULT_COLOR;
    }
    this.color = color;

    if (gridSize <= 0) {
      gridSize = DEFAULT_GRID_SIZE;
    }
    this.grid = gridSize;
  }

  public AbstractFlowShapeTokenOverlay(AbstractFlowShapeTokenOverlay other) {
    super(other);
    this.color = other.color;
    this.grid = other.grid;
  }

  public final Color getColor() {
    return color;
  }

  public final void setColor(Color color) {
    this.color = color;
  }

  public final int getGrid() {
    return grid;
  }

  /**
   * Get the flow used to position the states.
   *
   * @return Flow used to position the states
   */
  protected final TokenOverlayFlow getFlow() {
    if (flow == null) {
      flow = TokenOverlayFlow.getInstance(grid);
    }
    return flow;
  }

  @Override
  public final void paintOverlay(Graphics2D g, Token token, Rectangle bounds) {
    g.setColor(getColor());
    Rectangle2D gridCellBounds = getFlow().getStateBounds2D(bounds, token, getName());
    Shape s = getShape(getFlow().getStateBounds2D(bounds, token, getName()));
    g.fill(s);
  }

  /**
   * Return the overlay's shape.
   *
   * @param bounds Bounds of the token
   * @param token Token being rendered.
   * @return The shape of to render for the overlay.
   */
  @Deprecated
  public final Shape getShape(Rectangle bounds, Token token) {
    Rectangle2D gridCellBounds = getFlow().getStateBounds2D(bounds, token, getName());
    return getShape(gridCellBounds);
  }

  public abstract Shape getShape(Rectangle2D bounds);

  public final FlowShapeTokenOverlayDto toFlowShapeDto() {
    var dto = FlowShapeTokenOverlayDto.newBuilder();
    dto.setColor(color.getRGB());
    dto.setGridSize(grid);
    dto.setType(
        switch (this) {
          case FlowColorDotTokenOverlay ignored -> FlowShapeTokenOverlayDto.TypeTag.DOT;
          case FlowColorSquareTokenOverlay ignored -> FlowShapeTokenOverlayDto.TypeTag.SQUARE;
          case FlowDiamondTokenOverlay ignored -> FlowShapeTokenOverlayDto.TypeTag.DIAMOND;
          case FlowTriangleTokenOverlay ignored -> FlowShapeTokenOverlayDto.TypeTag.TRIANGLE;
          case FlowYieldTokenOverlay ignored -> FlowShapeTokenOverlayDto.TypeTag.YIELD;
        });
    return dto.build();
  }

  public static AbstractFlowShapeTokenOverlay fromDto(FlowShapeTokenOverlayDto dto) {
    var color = new Color(dto.getColor(), true);
    var gridSize = dto.getGridSize();
    return switch (dto.getType()) {
      case DOT -> new FlowColorDotTokenOverlay(DEFAULT_STATE_NAME, color, gridSize);
      case SQUARE -> new FlowColorSquareTokenOverlay(DEFAULT_STATE_NAME, color, gridSize);
      case DIAMOND -> new FlowDiamondTokenOverlay(DEFAULT_STATE_NAME, color, gridSize);
      case TRIANGLE -> new FlowTriangleTokenOverlay(DEFAULT_STATE_NAME, color, gridSize);
      case YIELD -> new FlowYieldTokenOverlay(DEFAULT_STATE_NAME, color, gridSize);
      case UNRECOGNIZED -> {
        log.error("Unrecognized AbstractShapeOverlay type");
        yield null;
      }
    };
  }
}
