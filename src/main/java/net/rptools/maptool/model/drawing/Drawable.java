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
package net.rptools.maptool.model.drawing;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import org.apache.logging.log4j.LogManager;

/**
 * @author drice
 */
public interface Drawable {
  Drawable copy();

  void draw(Zone zone, Graphics2D g, Pen pen);

  java.awt.Rectangle getBounds(Zone zone);

  @Nonnull
  Area getArea(Zone zone);

  GUID getId();

  void setId(GUID guid);

  Zone.Layer getLayer();

  void setLayer(Zone.Layer layer);

  DrawableDto toDto();

  static Drawable fromDto(DrawableDto drawableDto) {
    return switch (drawableDto.getDrawableTypeCase()) {
      case SHAPE_DRAWABLE -> ShapeDrawable.fromDto(drawableDto.getShapeDrawable());
      case DRAWN_LABEL -> DrawnLabel.fromDto(drawableDto.getDrawnLabel());
      case LINE_SEGMENT -> LineSegment.fromDto(drawableDto.getLineSegment());
      case DRAWABLES_GROUP -> DrawablesGroup.fromDto(drawableDto.getDrawablesGroup());
      case RADIUS_CELL_TEMPLATE -> RadiusCellTemplate.fromDto(drawableDto.getRadiusCellTemplate());
      case LINE_CELL_TEMPLATE -> LineCellTemplate.fromDto(drawableDto.getLineCellTemplate());
      case RADIUS_TEMPLATE -> RadiusTemplate.fromDto(drawableDto.getRadiusTemplate());
      case BURST_TEMPLATE -> BurstTemplate.fromDto(drawableDto.getBurstTemplate());
      case CONE_TEMPLATE -> ConeTemplate.fromDto(drawableDto.getConeTemplate());
      case BLAST_TEMPLATE -> BlastTemplate.fromDto(drawableDto.getBlastTemplate());
      case LINE_TEMPLATE -> LineTemplate.fromDto(drawableDto.getLineTemplate());
      case WALL_TEMPLATE -> WallTemplate.fromDto(drawableDto.getWallTemplate());
      default -> {
        LogManager.getLogger(Drawable.class)
            .warn("unknown DrawableDto type: " + drawableDto.getDrawableTypeCase());
        yield null;
      }
    };
  }
}
