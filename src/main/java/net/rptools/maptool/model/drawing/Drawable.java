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
import java.util.ArrayList;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import org.apache.logging.log4j.LogManager;

/**
 * @author drice
 */
public interface Drawable {

  void draw(Graphics2D g, Pen pen);

  java.awt.Rectangle getBounds();

  Area getArea();

  GUID getId();

  Zone.Layer getLayer();

  void setLayer(Zone.Layer layer);

  DrawableDto toDto();

  static Drawable fromDto(DrawableDto drawableDto) {
    switch (drawableDto.getDrawableTypeCase()) {
      case SHAPE_DRAWABLE -> {
        var dto = drawableDto.getShapeDrawable();
        var shape = Mapper.map(dto.getShape());
        var id = GUID.valueOf(dto.getId());
        var drawable = new ShapeDrawable(id, shape, dto.getUseAntiAliasing());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case RECTANGLE_DRAWABLE -> {
        var dto = drawableDto.getRectangleDrawable();
        var id = GUID.valueOf(dto.getId());
        var startPoint = dto.getStartPoint();
        var endPoint = dto.getEndPoint();
        var drawable =
            new Rectangle(
                id, startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case OVAL_DRAWABLE -> {
        var dto = drawableDto.getOvalDrawable();
        var id = GUID.valueOf(dto.getId());
        var startPoint = dto.getStartPoint();
        var endPoint = dto.getEndPoint();
        var drawable =
            new Oval(id, startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case CROSS_DRAWABLE -> {
        var dto = drawableDto.getCrossDrawable();
        var id = GUID.valueOf(dto.getId());
        var startPoint = dto.getStartPoint();
        var endPoint = dto.getEndPoint();
        var drawable =
            new Cross(id, startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case DRAWN_LABEL -> {
        var dto = drawableDto.getDrawnLabel();
        var id = GUID.valueOf(dto.getId());
        var bounds = dto.getBounds();
        var drawable =
            new DrawnLabel(id, dto.getText(), Mapper.map(dto.getBounds()), dto.getFont());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case LINE_SEGMENT -> {
        var dto = drawableDto.getLineSegment();
        var id = GUID.valueOf(dto.getId());
        var drawable = new LineSegment(id, dto.getWidth(), dto.getSquareCap());
        var points = drawable.getPoints();
        var pointDtos = dto.getPointsList();
        pointDtos.forEach(p -> points.add(Mapper.map(p)));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case DRAWABLES_GROUP -> {
        var dto = drawableDto.getDrawablesGroup();
        var id = GUID.valueOf(dto.getId());
        var elements = new ArrayList<DrawnElement>();
        var elementDtos = dto.getDrawnElementsList();
        elementDtos.forEach(e -> elements.add(DrawnElement.fromDto(e)));
        var drawable = new DrawablesGroup(id, elements);
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case RADIUS_CELL_TEMPLATE -> {
        var dto = drawableDto.getRadiusCellTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new RadiusCellTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case LINE_CELL_TEMPLATE -> {
        var dto = drawableDto.getLineCellTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new LineCellTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        if (!dto.getQuadrant().isEmpty()) {
          drawable.setQuadrant(AbstractTemplate.Quadrant.valueOf(dto.getQuadrant()));
        }
        drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
        var pathVertex = dto.getPathVertex();
        drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
        if (dto.hasName()) {
          drawable.setName(dto.getName().getValue());
        }
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case RADIUS_TEMPLATE -> {
        var dto = drawableDto.getRadiusTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new RadiusTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case BURST_TEMPLATE -> {
        var dto = drawableDto.getBurstTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new BurstTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case CONE_TEMPLATE -> {
        var dto = drawableDto.getConeTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new ConeTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        drawable.setDirection(AbstractTemplate.Direction.valueOf(dto.getDirection()));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case BLAST_TEMPLATE -> {
        var dto = drawableDto.getBlastTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new BlastTemplate(id, dto.getOffsetX(), dto.getOffsetY());
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        drawable.setDirection(AbstractTemplate.Direction.valueOf(dto.getDirection()));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case LINE_TEMPLATE -> {
        var dto = drawableDto.getLineTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new LineTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        if (!dto.getQuadrant().isEmpty()) {
          drawable.setQuadrant(AbstractTemplate.Quadrant.valueOf(dto.getQuadrant()));
        }
        drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
        var pathVertex = dto.getPathVertex();
        drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
        drawable.setDoubleWide(dto.getDoubleWide());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case WALL_TEMPLATE -> {
        var dto = drawableDto.getWallTemplate();
        var id = GUID.valueOf(dto.getId());
        var drawable = new WallTemplate(id);
        drawable.setZoneId(GUID.valueOf(dto.getZoneId()));
        drawable.setRadius(dto.getRadius());
        var vertex = dto.getVertex();
        drawable.setVertex(new ZonePoint(vertex.getX(), vertex.getY()));
        drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
        var pathVertex = dto.getPathVertex();
        drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
        drawable.setDoubleWide(dto.getDoubleWide());
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));

        var cellpoints = new ArrayList<CellPoint>();
        for (var point : dto.getPointsList())
          cellpoints.add(new CellPoint(point.getX(), point.getY()));
        drawable.setPath(cellpoints);

        return drawable;
      }
      default -> {
        LogManager.getLogger(Drawable.class)
            .warn("unknown DrawableDto type: " + drawableDto.getDrawableTypeCase());
        return null;
      }
    }
  }
}
