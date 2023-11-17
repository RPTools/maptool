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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.drawing.*;
import net.rptools.maptool.server.proto.*;
import net.rptools.maptool.server.proto.drawing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mapper {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(Mapper.class);

  public static Area map(AreaDto areaDto) {
    var segmentIterator = areaDto.getSegmentsList().iterator();
    if (!segmentIterator.hasNext()) return new Area();

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
    if (area == null) return null;

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

  public static Point map(IntPointDto dto) {
    var point = new Point();
    point.x = dto.getX();
    point.y = dto.getY();
    return point;
  }

  public static IntPointDto map(Point point) {
    return IntPointDto.newBuilder().setX(point.x).setY(point.y).build();
  }

  public static java.awt.Rectangle map(RectangleDto dto) {
    return new java.awt.Rectangle(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
  }

  public static RectangleDto map(java.awt.Rectangle rect) {
    return RectangleDto.newBuilder()
        .setX(rect.x)
        .setY(rect.y)
        .setWidth(rect.width)
        .setHeight(rect.height)
        .build();
  }

  public static Shape map(ShapeDto shapeDto) {
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

  public static ShapeDto map(Shape shape) {
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

  public static List<Object> map(List<ScriptTypeDto> argumentList) {
    return argumentList.stream().map(Mapper::map).collect(Collectors.toList());
  }

  public static Object map(ScriptTypeDto dto) {
    switch (dto.getTypeCase()) {
      case STRING_VAL -> {
        return dto.getStringVal();
      }
      case DOUBLE_VAL -> {
        final var stripped = BigDecimal.valueOf(dto.getDoubleVal()).stripTrailingZeros();
        return stripped.setScale(Math.max(0, stripped.scale()));
      }
      case JSON_VAL -> {
        return JsonParser.parseString(dto.getJsonVal());
      }
      default -> {
        log.warn("Unexpected type case:" + dto.getTypeCase());
        return "";
      }
    }
  }

  public static List<ScriptTypeDto> mapToScriptTypeDto(List<Object> args) {
    return args.stream().map(Mapper::mapToScriptTypeDto).collect(Collectors.toList());
  }

  public static ScriptTypeDto mapToScriptTypeDto(Object o) {
    var dto = ScriptTypeDto.newBuilder();
    if (o instanceof String stringValue) {
      dto.setStringVal(stringValue);
    } else if (o instanceof BigDecimal decimalValue) {
      dto.setDoubleVal(decimalValue.doubleValue());
    } else if (o instanceof JsonElement json) {
      dto.setJsonVal(json.toString());
    } else {
      log.warn("Unexpected type to convert to ScriptTypeDto: " + o.getClass());
    }
    return dto.build();
  }

  public static IntPointDto map(Dimension d) {
    return IntPointDto.newBuilder().setX(d.width).setY(d.height).build();
  }

  public static BasicStroke map(StrokeDto dto) {
    return new BasicStroke(dto.getWidth(), dto.getCapValue(), dto.getJoinValue());
  }

  public static StrokeDto map(BasicStroke stroke) {
    return StrokeDto.newBuilder()
        .setWidth(stroke.getLineWidth())
        .setCap(StrokeDto.CapDto.forNumber(stroke.getEndCap()))
        .setJoin(StrokeDto.JoinDto.forNumber(stroke.getLineJoin()))
        .build();
  }
}
