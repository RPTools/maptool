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

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.walker.WalkerMetric;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.model.drawing.Rectangle;
import net.rptools.maptool.server.proto.ServerPolicyDto;
import net.rptools.maptool.server.proto.WalkerMetricDto;
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
        return drawable;
      }
      case DRAWN_LABEL -> {
        var dto = drawableDto.getDrawnLabel();
        var id = GUID.valueOf(dto.getId());
        var bounds = dto.getBounds();
        var drawable = new DrawnLabel(id, dto.getText(), map(dto.getBounds()), dto.getFont());
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setName(dto.getName());
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
        drawable.setQuadrant(AbstractTemplate.Quadrant.valueOf(dto.getQuadrant()));
        drawable.setMouseSlopeGreater(dto.getMouseSlopeGreater());
        var pathVertex = dto.getPathVertex();
        drawable.setPathVertex(new ZonePoint(pathVertex.getX(), pathVertex.getY()));
        drawable.setDoubleWide(dto.getDoubleWide());
        drawable.setName(dto.getName());
        drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
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
              .setName(drawable.getName())
              .setLayer(drawable.getLayer().name())
              .setShape(shape)
              .setUseAntiAliasing(drawable.getUseAntiAliasing());
      return drawableDto.setShapeDrawable(dto).build();
    } else if (drawableToMap instanceof Rectangle drawable) {
      var dto =
          RectangleDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setName(drawable.getName())
              .setLayer(drawable.getLayer().name())
              .setStartPoint(map(drawable.getStartPoint()))
              .setEndPoint(map(drawable.getEndPoint()));
      return drawableDto.setRectangleDrawable(dto).build();
    } else if (drawableToMap instanceof Oval drawable) {
      var dto =
          OvalDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setName(drawable.getName())
              .setLayer(drawable.getLayer().name())
              .setStartPoint(map(drawable.getStartPoint()))
              .setEndPoint(map(drawable.getEndPoint()));
      return drawableDto.setOvalDrawable(dto).build();
    } else if (drawableToMap instanceof Cross drawable) {
      var dto =
          CrossDrawableDto.newBuilder()
              .setId(drawable.getId().toString())
              .setName(drawable.getName())
              .setLayer(drawable.getLayer().name())
              .setStartPoint(map(drawable.getStartPoint()))
              .setEndPoint(map(drawable.getEndPoint()));
      return drawableDto.setCrossDrawable(dto).build();
    } else if (drawableToMap instanceof DrawnLabel drawable) {
      var dto = DrawnLabelDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setBounds(map(drawable.getBounds()))
          .setText(drawable.getText())
          .setFont(drawable.getFont());
      return drawableDto.setDrawnLabel(dto).build();
    } else if (drawableToMap instanceof LineSegment drawable) {
      var dto = LineSegmentDrawableDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setWidth(drawable.getWidth())
          .setSquareCap(drawable.isSquareCap());
      drawable.getPoints().forEach(p -> dto.addPoints(map(p)));
      return drawableDto.setLineSegment(dto).build();
    } else if (drawableToMap instanceof DrawablesGroup drawable) {
      var dto = DrawablesGroupDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name());
      drawable.getDrawableList().forEach(d -> dto.addDrawnElements(map(d)));
      return drawableDto.setDrawablesGroup(dto).build();
    } else if (drawableToMap instanceof RadiusCellTemplate drawable) {
      var dto = RadiusCellTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()));
      return drawableDto.setRadiusCellTemplate(dto).build();
    } else if (drawableToMap instanceof LineCellTemplate drawable) {
      var dto = LineCellTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setQuadrant(drawable.getQuadrant().name())
          .setMouseSlopeGreater(drawable.isMouseSlopeGreater())
          .setPathVertex(map(drawable.getPathVertex()));
      return drawableDto.setLineCellTemplate(dto).build();
    } else if (drawableToMap instanceof RadiusTemplate drawable) {
      var dto = RadiusTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()));
      return drawableDto.setRadiusTemplate(dto).build();
    } else if (drawableToMap instanceof BurstTemplate drawable) {
      var dto = BurstTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()));
      return drawableDto.setBurstTemplate(dto).build();
    } else if (drawableToMap instanceof ConeTemplate drawable) {
      var dto = ConeTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setDirection(drawable.getDirection().name());
      return drawableDto.setConeTemplate(dto).build();
    } else if (drawableToMap instanceof BlastTemplate drawable) {
      var dto = BlastTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setDirection(drawable.getDirection().name());
      return drawableDto.setBlastTemplate(dto).build();
    } else if (drawableToMap instanceof LineTemplate drawable) {
      var dto = LineTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setQuadrant(drawable.getQuadrant().name())
          .setMouseSlopeGreater(drawable.isMouseSlopeGreater())
          .setPathVertex(map(drawable.getPathVertex()))
          .setDoubleWide(drawable.isDoubleWide());
      return drawableDto.setLineTemplate(dto).build();
    } else if (drawableToMap instanceof WallTemplate drawable) {
      var dto = WallTemplateDto.newBuilder();
      dto.setId(drawable.getId().toString())
          .setName(drawable.getName())
          .setLayer(drawable.getLayer().name())
          .setZoneId(drawable.getZoneId().toString())
          .setRadius(drawable.getRadius())
          .setVertex(map(drawable.getVertex()))
          .setQuadrant(drawable.getQuadrant().name())
          .setMouseSlopeGreater(drawable.isMouseSlopeGreater())
          .setPathVertex(map(drawable.getPathVertex()))
          .setDoubleWide(drawable.isDoubleWide());
      return drawableDto.setWallTemplate(dto).build();
    } else {
      log.warn("mapping not implemented for Drawable type: " + drawableToMap.getClass());
      return null;
    }
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

  private static Shape map(ShapeDto shape) {
    return null;
  }

  private static ShapeDto map(Shape shape) {
    return null;
  }
}
