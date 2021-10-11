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
package net.rptools.maptool.server;

import com.google.protobuf.StringValue;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.model.drawing.Rectangle;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.server.proto.drawing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mapper {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(Mapper.class);

  public static ServerPolicy map(ServerPolicyDto source) {
    var destination = new ServerPolicy();
    destination.setUseStrictTokenManagement(source.getUseStrictTokenManagement());
    destination.setIsMovementLocked(source.getIsMovementLocked());
    destination.setIsTokenEditorLocked(source.getIsTokenEditorLocked());
    destination.setPlayersCanRevealVision(source.getPlayersCanRevealVision());
    destination.setGmRevealsVisionForUnownedTokens(source.getGmRevealsVisionForUnownedTokens());
    destination.setUseIndividualViews(source.getUseIndividualViews());
    destination.setRestrictedImpersonation(source.getRestrictedImpersonation());
    destination.setPlayersReceiveCampaignMacros(source.getPlayersReceiveCampaignMacros());
    destination.setUseToolTipsForDefaultRollFormat(source.getUseToolTipsForDefaultRollFormat());
    destination.setUseIndividualFOW(source.getUseIndividualFOW());
    destination.setAutoRevealOnMovement(source.getIsAutoRevealOnMovement());
    destination.setIncludeOwnedNPCs(source.getIncludeOwnedNPCs());
    destination.setMovementMetric(WalkerMetric.valueOf(source.getMovementMetric().name()));
    destination.setUsingAstarPathfinding(source.getUsingAstarPathfinding());
    destination.setVblBlocksMove(source.getVblBlocksMove());
    return destination;
  }

  public static ServerPolicyDto map(ServerPolicy source) {
    var destination = ServerPolicyDto.newBuilder();
    destination.setUseStrictTokenManagement(source.useStrictTokenManagement());
    destination.setIsMovementLocked(source.isMovementLocked());
    destination.setIsTokenEditorLocked(source.isTokenEditorLocked());
    destination.setPlayersCanRevealVision(source.getPlayersCanRevealVision());
    destination.setGmRevealsVisionForUnownedTokens(source.getGmRevealsVisionForUnownedTokens());
    destination.setUseIndividualViews(source.isUseIndividualViews());
    destination.setRestrictedImpersonation(source.isRestrictedImpersonation());
    destination.setPlayersReceiveCampaignMacros(source.playersReceiveCampaignMacros());
    destination.setUseToolTipsForDefaultRollFormat(source.getUseToolTipsForDefaultRollFormat());
    destination.setUseIndividualFOW(source.isUseIndividualFOW());
    destination.setIsAutoRevealOnMovement(source.isAutoRevealOnMovement());
    destination.setIncludeOwnedNPCs(source.isIncludeOwnedNPCs());
    destination.setMovementMetric(WalkerMetricDto.valueOf(source.getMovementMetric().name()));
    destination.setUsingAstarPathfinding(source.isUsingAstarPathfinding());
    destination.setVblBlocksMove(source.getVblBlocksMove());
    return destination.build();
  }

  public static Area map(AreaDto areaDto) {
    var segmentIterator = areaDto.getSegmentsList().iterator();

    var it =
        new PathIterator() {
          private SegmentDto currentSegment = segmentIterator.next();

          @Override
          public int getWindingRule() {
            return areaDto.getWindingValue();
          }

          @Override
          public boolean isDone() {
            return !segmentIterator.hasNext();
          }

          @Override
          public void next() {
            currentSegment = segmentIterator.next();
          }

          @Override
          public int currentSegment(float[] coords) {
            switch (currentSegment.getSegmentTypeCase()) {
              case MOVE_TO -> {
                var segment = currentSegment.getMoveTo();
                var point0 = segment.getPoint0();
                coords[0] = (float) point0.getX();
                coords[1] = (float) point0.getY();
                return PathIterator.SEG_MOVETO;
              }
              case LINE_TO -> {
                var segment = currentSegment.getLineTo();
                var point0 = segment.getPoint0();
                coords[0] = (float) point0.getX();
                coords[1] = (float) point0.getY();
                return PathIterator.SEG_LINETO;
              }
              case QUAD_TO -> {
                var segment = currentSegment.getQuadTo();
                var point0 = segment.getPoint0();
                coords[0] = (float) point0.getX();
                coords[1] = (float) point0.getY();
                var point1 = segment.getPoint1();
                coords[2] = (float) point1.getX();
                coords[3] = (float) point1.getY();
                return PathIterator.SEG_QUADTO;
              }
              case CUBIC_TO -> {
                var segment = currentSegment.getCubicTo();
                var point0 = segment.getPoint0();
                coords[0] = (float) point0.getX();
                coords[1] = (float) point0.getY();
                var point1 = segment.getPoint1();
                coords[2] = (float) point1.getX();
                coords[3] = (float) point1.getY();
                var point2 = segment.getPoint2();
                coords[4] = (float) point2.getX();
                coords[5] = (float) point2.getY();
                return PathIterator.SEG_CUBICTO;
              }
            }
            return SEG_CLOSE;
          }

          @Override
          public int currentSegment(double[] coords) {
            switch (currentSegment.getSegmentTypeCase()) {
              case MOVE_TO -> {
                var segment = currentSegment.getMoveTo();
                var point0 = segment.getPoint0();
                coords[0] = point0.getX();
                coords[1] = point0.getY();
                return PathIterator.SEG_MOVETO;
              }
              case LINE_TO -> {
                var segment = currentSegment.getLineTo();
                var point0 = segment.getPoint0();
                coords[0] = point0.getX();
                coords[1] = point0.getY();
                return PathIterator.SEG_LINETO;
              }
              case QUAD_TO -> {
                var segment = currentSegment.getQuadTo();
                var point0 = segment.getPoint0();
                coords[0] = point0.getX();
                coords[1] = point0.getY();
                var point1 = segment.getPoint1();
                coords[2] = point1.getX();
                coords[3] = point1.getY();
                return PathIterator.SEG_QUADTO;
              }
              case CUBIC_TO -> {
                var segment = currentSegment.getCubicTo();
                var point0 = segment.getPoint0();
                coords[0] = point0.getX();
                coords[1] = point0.getY();
                var point1 = segment.getPoint1();
                coords[2] = point1.getX();
                coords[3] = point1.getY();
                var point2 = segment.getPoint2();
                coords[4] = point2.getX();
                coords[5] = point2.getY();
                return PathIterator.SEG_CUBICTO;
              }
            }
            return SEG_CLOSE;
          }
        };
    var path = new Path2D.Float();
    path.append(it, false);
    return new Area(path);
  }

  public static AreaDto map(Area area) {
    var builder = AreaDto.newBuilder();

    var it = area.getPathIterator(null);
    float[] floats = new float[6];
    builder.setWinding(AreaDto.WindingRule.forNumber(it.getWindingRule()));

    for (; !it.isDone(); it.next()) {
      var segmentBuilder = SegmentDto.newBuilder();
      switch (it.currentSegment(floats)) {
        case PathIterator.SEG_MOVETO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(floats[0]).setY(floats[1]);
          var moveTo = MoveToSegment.newBuilder().setPoint0(point0Builder);
          segmentBuilder.setMoveTo(moveTo);
        }
        case PathIterator.SEG_LINETO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(floats[0]).setY(floats[1]);
          var lineTo = LineToSegment.newBuilder().setPoint0(point0Builder);
          segmentBuilder.setLineTo(lineTo);
        }
        case PathIterator.SEG_QUADTO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(floats[0]).setY(floats[1]);
          var point1Builder = DoublePointDto.newBuilder().setX(floats[2]).setY(floats[3]);
          var quadTo = QuadToSegment.newBuilder().setPoint0(point0Builder).setPoint1(point1Builder);
          segmentBuilder.setQuadTo(quadTo);
        }
        case PathIterator.SEG_CUBICTO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(floats[0]).setY(floats[1]);
          var point1Builder = DoublePointDto.newBuilder().setX(floats[2]).setY(floats[3]);
          var point2Builder = DoublePointDto.newBuilder().setX(floats[4]).setY(floats[5]);
          var cubicTo =
              CubicToSegment.newBuilder()
                  .setPoint0(point0Builder)
                  .setPoint1(point1Builder)
                  .setPoint2(point2Builder);
          segmentBuilder.setCubicTo(cubicTo);
        }
        case PathIterator.SEG_CLOSE -> segmentBuilder.setClose(CloseSegment.newBuilder());
      }
      builder.addSegments(segmentBuilder);
    }

    return builder.build();
  }

  public static DrawablePaint map(DrawablePaintDto dto) {
    switch (dto.getPaintTypeCase()) {
      case COLOR_PAINT -> {
        var paint = new DrawableColorPaint(dto.getColorPaint().getColor());
        return paint;
      }
      case TEXTURE_PAINT -> {
        var texturePaintDto = dto.getTexturePaint();
        return new DrawableTexturePaint(
            new MD5Key(texturePaintDto.getAssetId()), texturePaintDto.getScale());
      }
      default -> {
        log.warn("unknown DrawablePaintDto type: " + dto.getPaintTypeCase());
        return null;
      }
    }
  }

  public static DrawablePaintDto map(DrawablePaint paint) {
    var dto = DrawablePaintDto.newBuilder();
    if (paint instanceof DrawableColorPaint colorPaint) {
      return dto.setColorPaint(DrawableColorPaintDto.newBuilder().setColor(colorPaint.getColor()))
          .build();
    } else if (paint instanceof DrawableTexturePaint texturePaint) {
      var textureDto =
          DrawableTexturePaintDto.newBuilder()
              .setAssetId(texturePaint.getAssetId().toString())
              .setScale(texturePaint.getScale());
      return dto.setTexturePaint(textureDto).build();
    }
    log.warn("unexpected type " + paint.getClass().getName());
    return null;
  }

  public static Pen map(PenDto penDto) {
    var pen = new Pen();
    pen.setEraser(penDto.getEraser());
    pen.setForegroundMode(penDto.getForegroundModeValue());
    pen.setBackgroundMode(penDto.getBackgroundModeValue());
    pen.setThickness(penDto.getThickness());
    pen.setOpacity(penDto.getOpacity());
    pen.setSquareCap(penDto.getSquareCap());
    pen.setPaint(map(penDto.getForegroundColor()));
    pen.setBackgroundPaint(map(penDto.getBackgroundColor()));
    return pen;
  }

  public static PenDto map(Pen pen) {
    return PenDto.newBuilder()
        .setEraser(pen.isEraser())
        .setForegroundMode(PenDto.mode.forNumber(pen.getForegroundMode()))
        .setBackgroundMode(PenDto.mode.forNumber(pen.getBackgroundMode()))
        .setThickness(pen.getThickness())
        .setOpacity(pen.getOpacity())
        .setSquareCap(pen.getSquareCap())
        .setForegroundColor(map(pen.getPaint()))
        .setBackgroundColor(map(pen.getBackgroundPaint()))
        .build();
  }

  public static Drawable map(DrawableDto drawableDto) {
    switch (drawableDto.getDrawableTypeCase()) {
      case SHAPE_DRAWABLE -> {
        var dto = drawableDto.getShapeDrawable();
        var shape = map(dto.getShape());
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
        var drawable = new DrawnLabel(id, dto.getText(), map(dto.getBounds()), dto.getFont());
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
        pointDtos.forEach(p -> points.add(map(p)));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case DRAWABLES_GROUP -> {
        var dto = drawableDto.getDrawablesGroup();
        var id = GUID.valueOf(dto.getId());
        var elements = new ArrayList<DrawnElement>();
        var elementDtos = dto.getDrawnElementsList();
        elementDtos.forEach(e -> elements.add(map(e)));
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
        drawable.setQuadrant(AbstractTemplate.Quadrant.valueOf(dto.getQuadrant()));
        drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
        var pathVertex = dto.getPathVertex();
        drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
        if (dto.hasName()) drawable.setName(dto.getName().getValue());
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
        drawable.setQuadrant(AbstractTemplate.Quadrant.valueOf(dto.getQuadrant()));
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
        log.warn("unknown DrawableDto type: " + drawableDto.getDrawableTypeCase());
        return null;
      }
    }
  }

  public static DrawableDto map(Drawable drawableToMap) {
    var drawableDto = DrawableDto.newBuilder();

    if (drawableToMap instanceof ShapeDrawable drawable) {
      var shape = map(drawable.getShape());
      var dto =
          ShapeDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setLayer(drawable.getLayer().name())
              .setShape(shape)
              .setUseAntiAliasing(drawable.getUseAntiAliasing());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setShapeDrawable(dto).build();
    } else if (drawableToMap instanceof Oval drawable) {
      var dto =
          OvalDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setStartPoint(map(drawable.getStartPoint()))
              .setEndPoint(map(drawable.getEndPoint()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setOvalDrawable(dto).build();
    } else if (drawableToMap instanceof Rectangle drawable) {
      var dto =
          RectangleDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setLayer(drawable.getLayer().name())
              .setStartPoint(map(drawable.getStartPoint()))
              .setEndPoint(map(drawable.getEndPoint()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setRectangleDrawable(dto).build();
    } else if (drawableToMap instanceof Cross drawable) {
      var dto =
          CrossDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setLayer(drawable.getLayer().name())
              .setStartPoint(map(drawable.getStartPoint()))
              .setEndPoint(map(drawable.getEndPoint()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setCrossDrawable(dto).build();
    } else if (drawableToMap instanceof DrawnLabel drawable) {
      var dto = DrawnLabelDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setBounds(map(drawable.getBounds()))
          .setText(drawable.getText())
          .setFont(drawable.getFont());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setDrawnLabel(dto).build();
    } else if (drawableToMap instanceof LineSegment drawable) {
      var dto = LineSegmentDrawableDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setWidth(drawable.getWidth())
          .setSquareCap(drawable.isSquareCap());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      drawable.getPoints().forEach(p -> dto.addPoints(map(p)));
      return drawableDto.setLineSegment(dto).build();
    } else if (drawableToMap instanceof DrawablesGroup drawable) {
      var dto = DrawablesGroupDto.newBuilder();
      dto.setId(drawable.getId().toString()).setLayer(drawable.getLayer().name());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      drawable.getDrawableList().forEach(d -> dto.addDrawnElements(map(d)));
      return drawableDto.setDrawablesGroup(dto).build();
    } else if (drawableToMap instanceof RadiusCellTemplate drawable) {
      var dto = RadiusCellTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setRadiusCellTemplate(dto).build();
    } else if (drawableToMap instanceof LineCellTemplate drawable) {
      var dto = LineCellTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setQuadrant(drawable.getQuadrant().name())
          .setMouseSlopeGreater(drawable.isMouseSlopeGreater())
          .setPathVertex(map(drawable.getPathVertex()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setLineCellTemplate(dto).build();
    } else if (drawableToMap instanceof BlastTemplate drawable) {
      var dto = BlastTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setDirection(drawable.getDirection().name())
          .setOffsetX(drawable.getOffsetX())
          .setOffsetY(drawable.getOffsetY());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setBlastTemplate(dto).build();
    } else if (drawableToMap instanceof BurstTemplate drawable) {
      var dto = BurstTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setBurstTemplate(dto).build();
    } else if (drawableToMap instanceof ConeTemplate drawable) {
      var dto = ConeTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setDirection(drawable.getDirection().name());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setConeTemplate(dto).build();
    } else if (drawableToMap instanceof RadiusTemplate drawable) {
      var dto = RadiusTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()));

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setRadiusTemplate(dto).build();
    } else if (drawableToMap instanceof WallTemplate drawable) {
      var dto = WallTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setMouseSlopeGreater(drawable.isMouseSlopeGreater())
          .setPathVertex(map(drawable.getPathVertex()))
          .setDoubleWide(drawable.isDoubleWide());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      for (var point : drawable.getPath()) dto.addPoints(map(point));

      return drawableDto.setWallTemplate(dto).build();
    } else if (drawableToMap instanceof LineTemplate drawable) {
      var dto = LineTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setQuadrant(drawable.getQuadrant().name())
          .setMouseSlopeGreater(drawable.isMouseSlopeGreater())
          .setPathVertex(map(drawable.getPathVertex()))
          .setDoubleWide(drawable.isDoubleWide());

      if (drawable.getName() != null) dto.setName(StringValue.of(drawable.getName()));

      return drawableDto.setLineTemplate(dto).build();
    } else {
      log.warn("mapping not implemented for Drawable type: " + drawableToMap.getClass());
      return null;
    }
  }

  private static IntPointDto map(CellPoint point) {
    return IntPointDto.newBuilder().setX(point.x).setY(point.y).build();
  }

  private static IntPointDto map(ZonePoint point) {
    return IntPointDto.newBuilder().setX(point.x).setY(point.y).build();
  }

  private static Point map(IntPointDto dto) {
    var point = new Point();
    point.x = dto.getX();
    point.y = dto.getY();
    return point;
  }

  private static IntPointDto map(Point point) {
    return IntPointDto.newBuilder().setX(point.x).setY(point.y).build();
  }

  private static java.awt.Rectangle map(RectangleDto dto) {
    return new java.awt.Rectangle(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
  }

  private static RectangleDto map(java.awt.Rectangle rect) {
    return RectangleDto.newBuilder()
        .setX(rect.x)
        .setY(rect.y)
        .setWidth(rect.width)
        .setHeight(rect.height)
        .build();
  }

  private static DrawnElement map(DrawnElementDto dto) {
    return new DrawnElement(map(dto.getDrawable()), map(dto.getPen()));
  }

  private static DrawnElementDto map(DrawnElement element) {
    return DrawnElementDto.newBuilder()
        .setDrawable(map(element.getDrawable()))
        .setPen(map(element.getPen()))
        .build();
  }

  private static Shape map(ShapeDto shapeDto) {
    switch (shapeDto.getShapeTypeCase()) {
      case RECTANGLE -> {
        var dto = shapeDto.getRectangle();
        return new java.awt.Rectangle(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
      }
      case AREA -> {
        return map(shapeDto.getArea());
      }
      case POLYGON -> {
        var dto = shapeDto.getPolygon();
        var polygon = new Polygon();
        dto.getPointsList().forEach(p -> polygon.addPoint(p.getX(), p.getY()));
        return polygon;
      }
      case ELLIPSE -> {
        var dto = shapeDto.getEllipse();
        return new Ellipse2D.Float(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
      }
      default -> {
        log.warn("unknown ShapeDto type: " + shapeDto.getShapeTypeCase());
        return null;
      }
    }
  }

  private static ShapeDto map(Shape shape) {
    var shapeDto = ShapeDto.newBuilder();
    if (shape instanceof java.awt.Rectangle rect) {
      var dto =
          RectangleDto.newBuilder()
              .setX(rect.x)
              .setY(rect.y)
              .setWidth(rect.width)
              .setHeight(rect.height);
      return shapeDto.setRectangle(dto).build();
    } else if (shape instanceof Area area) {
      return shapeDto.setArea(map(area)).build();
    } else if (shape instanceof Polygon polygon) {
      var dto = PolygonDto.newBuilder();
      for (int i = 0; i < polygon.npoints; i++) {
        var pointDto = IntPointDto.newBuilder();
        pointDto.setX(polygon.xpoints[i]);
        pointDto.setY(polygon.ypoints[i]);
        dto.addPoints(pointDto);
      }
      return shapeDto.setPolygon(dto).build();
    } else if (shape instanceof Ellipse2D.Float ellipse) {
      var dto =
          EllipseDto.newBuilder()
              .setX(ellipse.x)
              .setY(ellipse.y)
              .setWidth(ellipse.width)
              .setHeight(ellipse.height);
      return shapeDto.setEllipse(dto).build();
    } else {
      log.warn("mapping not implemented for Shape type: " + shape.getClass());
      return null;
    }
  }

  public static Token map(TokenDto dto) {
    var token = new Token();
    token.setId(GUID.valueOf(dto.getId()));
    token.setBeingImpersonated(dto.getBeingImpersonated());
    token.setExposedAreaGUID(GUID.valueOf(dto.getExposedAreaGuid()));

    var assetMap = dto.getImageAssetMapMap();
    for(var key: assetMap.keySet())
      token.setImageAsset(key, new MD5Key(assetMap.get(key)));

    token.setImageAsset(dto.getCurrentImageAsset());
    token.setX(dto.getLastX());
    token.setX(dto.getX());
    token.setY(dto.getLastY());
    token.setY(dto.getY());
    token.setZOrder(dto.getZ());
    token.setAnchor(dto.getAnchorX(), dto.getAnchorY());
    token.setSizeScale(dto.getSizeScale());
    token.setLastPath(map(dto.getLastPath()));
    token.setSnapToScale(dto.getSnapToScale());
    token.setFlippedIso(false);
    token.setWidth(dto.getWidth());
    token.setHeight(dto.getHeight());
    token.setFlippedIso(true);
    token.setWidth(dto.getIsoWidth());
    token.setHeight(dto.getIsoHeight());
    token.setScaleX(dto.getScaleX());
    token.setScaleY(dto.getScaleY());

    var tokenSizeMap = token.getSizeMap();
    var dtoSizeMap = dto.getSizeMapMap();
    for(var key: dtoSizeMap.keySet()) {
      switch (key) {
        case 0 -> {
          tokenSizeMap.put(SquareGrid.class, GUID.valueOf(dtoSizeMap.get(key)));
        }
        case 1 -> {
          tokenSizeMap.put(GridlessGrid.class, GUID.valueOf(dtoSizeMap.get(key)));
        }
        case 2 -> {
          tokenSizeMap.put(HexGridVertical.class, GUID.valueOf(dtoSizeMap.get(key)));
        }
        case 3 -> {
          tokenSizeMap.put(HexGridHorizontal.class, GUID.valueOf(dtoSizeMap.get(key)));
        }
        case 4 -> {
          tokenSizeMap.put(IsometricGrid.class, GUID.valueOf(dtoSizeMap.get(key)));
        }
        default -> {
          log.warn("unknown grid in tokensizemap: " + key);
        }
      }
    }
    token.setSnapToGrid(dto.getSnapToGrid());
    token.setVisible(dto.getIsVisible());
    token.setVisibleOnlyToOwner(dto.getVisibleOnlyToOwner());
    token.setColorSensitivity(dto.getVblColorSensitivity());
    token.setAlwaysVisibleTolerance(dto.getAlwaysVisibleTolerance());
    token.setIsAlwaysVisible(dto.getIsAlwaysVisible());
    token.setVBL(map(dto.getVbl()));
    token.setName(dto.getName());
    dto.getOwnerListList().forEach(owner -> token.addOwner(owner));
    token.setOwnerType(dto.getOwnerType());
    token.setShape(Token.TokenShape.valueOf(dto.getTokenShape()));
    token.setType(Token.Type.valueOf(dto.getTokenShape()));
    token.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    token.setPropertyType(dto.getPropertyType());
    token.setFacing(dto.getFacing());
    token.setHaloColor(new Color(dto.getHaloColor(), true));
    token.setVisionOverlayColor(new Color(dto.getVisionOverlayColor(), true));
    token.setTokenOpacity(dto.getTokenOpacity());
    token.setSpeechName(dto.getSpeechname());
    token.setTerrainModifier(dto.getTerrainModifier());
    token.setTerrainModifierOperation(Token.TerrainModifierOperation.valueOf(dto.getTerrainModifierOperation().name()));

    var ignoredSet = new HashSet<Token.TerrainModifierOperation>();
    for(var value: dto.getTerrainModifiersIgnoredList())
      ignoredSet.add(Token.TerrainModifierOperation.valueOf(value.name()));
    token.setTerrainModifiersIgnored(ignoredSet);

    token.setFlippedX(dto.getIsFlippedX());
    token.setFlippedY(dto.getIsFlippedY());
    token.setFlippedIso(dto.getIsFlippedIso());
    token.setCharsheetImage(new MD5Key(dto.getCharsheetImage()));
    token.setPortraitImage(new MD5Key(dto.getPortraitImage()));

    var lightSources = token.getLightSourcesModifiable();
    for(var light: dto.getLightSourcesList())
      lightSources.add(map(light));

    token.setSightType(dto.getSightType());
    token.setHasSight(dto.getHasSight());
    token.setHasImageTable(dto.getHasImageTable());
    token.setImageTableName(dto.getImageTableName());
    token.setLabel(dto.getLabel());
    token.setNotes(dto.getNotes());
    token.setGMNotes(dto.getGmNotes());
    token.setGMName(dto.getGmName());

    var dtoStateMap = dto.getStateMap();
    for(var key: dtoStateMap.keySet()) {
      var stateDto = dtoStateMap.get(key);
      switch (stateDto.getStateTypeCase()) {
        case BOOL_VALUE -> {
          var value = stateDto.getBoolValue();
          token.setState(key, value ? Boolean.TRUE : null);
        }
        case DOUBLE_VALUE -> {
          token.setState(key, new BigDecimal(stateDto.getDoubleValue()));
        }
        default -> {
          log.warn("unknown state type:" + stateDto.getStateTypeCase());
        }
      }
    }

    var dtoProperties = dto.getPropertiesMap();
    for(var key: dtoProperties.keySet())
      token.setProperty(key, dtoProperties.get(key));

    var dtoMacros =  dto.getMacroPropertiesMap();
    var tokenMacros = token.getMacroPropertiesMap(false);
    for(var key: dtoMacros.keySet())
      tokenMacros.put(key, map(dtoMacros.get(key)));

    token.setSpeechMap(dto.getSpeechMap());
    token.setHeroLabData(map(dto.getHeroLabData()));
    token.setAllowURIAccess(dto.getAllowUriAccess());
    return token;
  }

  public static TokenDto map(Token token) {
    return null;
  }

  private static HeroLabData map(HeroLabDataDto heroLabData) {
    return null;
  }

  private static MacroButtonProperties map(MacroButtonPropertiesDto macroButtonPropertiesDto) {
    return null;
  }

  private static AttachedLightSource map(AttachedLightSourceDto light) {
    return null;
  }

  private static Path<? extends AbstractPoint> map(PathDto lastPath) {
    return null;
  }
}
