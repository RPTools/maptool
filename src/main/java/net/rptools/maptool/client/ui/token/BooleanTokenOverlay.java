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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.BooleanTokenOverlayDto;
import net.rptools.maptool.util.FunctionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An overlay that may be applied to a token to show state.
 *
 * @author jgorrell
 */
public abstract sealed class BooleanTokenOverlay extends AbstractTokenOverlay
    permits AbstractShapeTokenOverlay,
        AbstractFlowShapeTokenOverlay,
        ColorDotTokenOverlay,
        ImageTokenOverlay,
        CornerImageTokenOverlay,
        FlowImageTokenOverlay,
        ShadedTokenOverlay {
  private static final Logger log = LogManager.getLogger(BooleanTokenOverlay.class);

  /**
   * Create an overlay with the passed name.
   *
   * @param aName Name of the new overlay.
   */
  protected BooleanTokenOverlay(String aName) {
    super(aName);
  }

  protected BooleanTokenOverlay(BooleanTokenOverlay other) {
    super(other);
  }

  @Override
  public abstract BooleanTokenOverlay clone();

  @Override
  public final void paintOverlay(Graphics2D g, Token token, Rectangle bounds, Object value) {
    if (!FunctionUtil.getBooleanValue(value)) {
      return;
    }

    g = (Graphics2D) g.create();
    try {
      // Apply Alpha Transparency
      float opacity = token.getTokenOpacity() * getOpacity() / 100.f;
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

      paintOverlay(g, token, bounds);
    } finally {
      g.dispose();
    }
  }

  /**
   * Paint the overlay for the passed token.
   *
   * @param g Graphics used to paint. It is already translated so that 0,0 is the upper left corner
   *     of the token. It is also clipped so that the overlay can not draw out of the token's
   *     bounding box.
   * @param token The token being painted.
   * @param bounds The bounds of the actual token. This will be different than the clip since the
   *     clip also has to take into account the edge of the window. If you draw based on the clip it
   *     will be off for partial token painting.
   */
  public abstract void paintOverlay(Graphics2D g, Token token, Rectangle bounds);

  public static BooleanTokenOverlay fromDto(BooleanTokenOverlayDto dto) {
    BooleanTokenOverlay overlay =
        switch (dto.getChildTypeCase()) {
          case SHAPE -> AbstractShapeTokenOverlay.fromDto(dto.getShape());
          case FLOW_SHAPE -> AbstractFlowShapeTokenOverlay.fromDto(dto.getFlowShape());
          case COLOR_DOT -> ColorDotTokenOverlay.fromDto(dto.getColorDot());
          case IMAGE -> ImageTokenOverlay.fromDto(dto.getImage());
          case CORNER_IMAGE -> CornerImageTokenOverlay.fromDto(dto.getCornerImage());
          case FLOW_IMAGE -> FlowImageTokenOverlay.fromDto(dto.getFlowImage());
          case SHADED -> ShadedTokenOverlay.fromDto(dto.getShaded());
          case CHILDTYPE_NOT_SET -> {
            log.error("Unrecognized BooleanTokenOverlay type");
            yield null;
          }
        };
    if (overlay != null) {
      overlay.fillFrom(dto.getCommon());
    }
    return overlay;
  }

  public final BooleanTokenOverlayDto toDto() {
    var message = BooleanTokenOverlayDto.newBuilder();
    message.setCommon(getCommonDto());
    switch (this) {
      case AbstractShapeTokenOverlay shapeOverlay -> message.setShape(shapeOverlay.toShapeDto());
      case AbstractFlowShapeTokenOverlay flowShapeOverlay ->
          message.setFlowShape(flowShapeOverlay.toFlowShapeDto());
      case ColorDotTokenOverlay colorDotTokenOverlay ->
          message.setColorDot(colorDotTokenOverlay.toColorDotDto());
      case ImageTokenOverlay imageTokenOverlay -> message.setImage(imageTokenOverlay.toImageDto());
      case CornerImageTokenOverlay cornerImageTokenOverlay ->
          message.setCornerImage(cornerImageTokenOverlay.toCornerImageDto());
      case FlowImageTokenOverlay flowImageTokenOverlay ->
          message.setFlowImage(flowImageTokenOverlay.toFlowImageDto());
      case ShadedTokenOverlay shadedTokenOverlay ->
          message.setShaded(shadedTokenOverlay.toShadedDto());
    }
    return message.build();
  }
}
