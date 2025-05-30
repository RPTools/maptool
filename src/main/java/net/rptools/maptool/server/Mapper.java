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

import com.google.common.primitives.Floats;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.awt.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import net.rptools.maptool.model.drawing.ExtendedGeneralPath_Double;
import net.rptools.maptool.server.proto.ScriptTypeDto;
import net.rptools.maptool.server.proto.StrokeDto;
import net.rptools.maptool.server.proto.drawing.*;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.ExtendedPathIterator;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Mapper {
  /** Instance used for log messages. */
  private static final Logger log = LogManager.getLogger(Mapper.class);

  private static PathShapeDto map(PathIterator it) {
    var builder = PathShapeDto.newBuilder();

    double[] coords = new double[6];
    builder.setWinding(PathShapeDto.WindingRule.forNumber(it.getWindingRule()));

    for (; !it.isDone(); it.next()) {
      var segmentBuilder = SegmentDto.newBuilder();
      switch (it.currentSegment(coords)) {
        case ExtendedPathIterator.SEG_ARCTO -> {
          var arcTo =
              ArcToSegment.newBuilder()
                  .setRX(coords[0])
                  .setRY(coords[1])
                  .setAngle(coords[2])
                  .setLargeArcFlag(coords[3] != 0)
                  .setSweepFlag(coords[4] != 0)
                  .setX(coords[5])
                  .setY(coords[6]);
          segmentBuilder.setArcTo(arcTo);
        }
        case PathIterator.SEG_MOVETO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(coords[0]).setY(coords[1]);
          var moveTo = MoveToSegment.newBuilder().setPoint0(point0Builder);
          segmentBuilder.setMoveTo(moveTo);
        }
        case PathIterator.SEG_LINETO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(coords[0]).setY(coords[1]);
          var lineTo = LineToSegment.newBuilder().setPoint0(point0Builder);
          segmentBuilder.setLineTo(lineTo);
        }
        case PathIterator.SEG_QUADTO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(coords[0]).setY(coords[1]);
          var point1Builder = DoublePointDto.newBuilder().setX(coords[2]).setY(coords[3]);
          var quadTo = QuadToSegment.newBuilder().setPoint0(point0Builder).setPoint1(point1Builder);
          segmentBuilder.setQuadTo(quadTo);
        }
        case PathIterator.SEG_CUBICTO -> {
          var point0Builder = DoublePointDto.newBuilder().setX(coords[0]).setY(coords[1]);
          var point1Builder = DoublePointDto.newBuilder().setX(coords[2]).setY(coords[3]);
          var point2Builder = DoublePointDto.newBuilder().setX(coords[4]).setY(coords[5]);
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

  public static Path2D map(PathShapeDto pathShapeDto) {
    final var segments = pathShapeDto.getSegmentsList();
    final var path =
        new ExtendedGeneralPath_Double(pathShapeDto.getWindingValue(), segments.size());

    for (final SegmentDto currentSegment : pathShapeDto.getSegmentsList()) {
      switch (currentSegment.getSegmentTypeCase()) {
        case ARC_TO -> {
          final var segment = currentSegment.getArcTo();
          path.arcTo(
              segment.getRX(),
              segment.getRY(),
              segment.getAngle(),
              segment.getLargeArcFlag(),
              segment.getSweepFlag(),
              segment.getX(),
              segment.getY());
        }
        case MOVE_TO -> {
          final var segment = currentSegment.getMoveTo();
          var point = segment.getPoint0();
          path.moveTo(point.getX(), point.getY());
        }
        case LINE_TO -> {
          final var segment = currentSegment.getLineTo();
          var point = segment.getPoint0();
          path.lineTo(point.getX(), point.getY());
        }
        case QUAD_TO -> {
          final var segment = currentSegment.getQuadTo();
          var point0 = segment.getPoint0();
          var point1 = segment.getPoint1();
          path.quadTo(point0.getX(), point0.getY(), point1.getX(), point1.getY());
        }
        case CUBIC_TO -> {
          final var segment = currentSegment.getCubicTo();
          var point0 = segment.getPoint0();
          var point1 = segment.getPoint1();
          var point2 = segment.getPoint2();
          path.curveTo(
              point0.getX(),
              point0.getY(),
              point1.getX(),
              point1.getY(),
              point2.getX(),
              point2.getY());
        }
        case CLOSE -> {
          path.closePath();
        }
      }
    }
    return new Path2D.Double(path);
  }

  public static PathShapeDto map(Path2D path) {
    return map(path.getPathIterator(null));
  }

  public static Area map(AreaDto areaDto) {
    return new Area(map(areaDto.getPath()));
  }

  public static AreaDto map(Area area) {
    if (area == null) return null;

    var builder = AreaDto.newBuilder();
    var pathDto = map(area.getPathIterator(null));
    builder.setPath(pathDto);
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
      case ARC -> {
        var dto = shapeDto.getArc();
        return new Arc2D.Double(
            dto.getX(),
            dto.getY(),
            dto.getW(),
            dto.getY(),
            dto.getStart(),
            dto.getExtent(),
            switch (dto.getType()) {
              case 1 -> Arc2D.PIE;
              case 2 -> Arc2D.CHORD;
              default -> Arc2D.OPEN;
            });
      }
      case CUBICCURVE -> {
        var dto = shapeDto.getCubicCurve();
        return new CubicCurve2D.Double(
            dto.getX1(),
            dto.getY1(),
            dto.getCtrlX1(),
            dto.getCtrlY1(),
            dto.getCtrlX2(),
            dto.getCtrlY2(),
            dto.getX2(),
            dto.getY2());
      }
      case LINE -> {
        var dto = shapeDto.getLine();
        return new Line2D.Double(dto.getX1(), dto.getY1(), dto.getX2(), dto.getY2());
      }
      case POLYGON2D -> {
        var dto = shapeDto.getPolygon2D();
        return new Polygon2D(
            Floats.toArray(dto.getXPointsList()),
            Floats.toArray(dto.getYPointsList()),
            dto.getNumPoints());
      }
      case QUADCURVE -> {
        var dto = shapeDto.getQuadCurve();
        return new QuadCurve2D.Double(
            dto.getX1(), dto.getY1(), dto.getCtrlX(), dto.getCtrlY(), dto.getX2(), dto.getY2());
      }
      case RECTANGLE -> {
        var dto = shapeDto.getRectangle();
        return new java.awt.Rectangle(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
      }
      case RECTANGLE2D -> {
        var dto = shapeDto.getRectangle2D();
        return new Rectangle2D.Double(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
      }
      case ROUNDRECTANGLE -> {
        var dto = shapeDto.getRoundRectangle();
        return new RoundRectangle2D.Double(
            dto.getX(), dto.getY(), dto.getW(), dto.getH(), dto.getArcW(), dto.getArcH());
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
        return new Ellipse2D.Double(dto.getX(), dto.getY(), dto.getWidth(), dto.getHeight());
      }
      case PATH -> {
        return map(shapeDto.getPath());
      }
      default -> {
        log.warn("unknown ShapeDto type: " + shapeDto.getShapeTypeCase());
        return null;
      }
    }
  }

  public static ShapeDto map(Shape shape) {
    var shapeDto = ShapeDto.newBuilder();
    if (shape instanceof Arc2D arc2D) {
      var dto =
          ArcDto.newBuilder()
              .setX(arc2D.getX())
              .setY(arc2D.getY())
              .setW(arc2D.getWidth())
              .setH(arc2D.getHeight())
              .setStart(arc2D.getAngleStart())
              .setExtent(arc2D.getAngleExtent())
              .setType(arc2D.getArcType());
      return shapeDto.setArc(dto).build();
    } else if (shape instanceof CubicCurve2D cc) {
      var dto =
          CubicCurveDto.newBuilder()
              .setX1(cc.getX1())
              .setY1(cc.getY1())
              .setCtrlX1(cc.getCtrlX1())
              .setCtrlY1(cc.getCtrlY1())
              .setCtrlX2(cc.getCtrlX2())
              .setCtrlY2(cc.getCtrlY2())
              .setX2(cc.getX2())
              .setY2(cc.getY2());
      return shapeDto.setCubicCurve(dto).build();
    } else if (shape instanceof ExtendedGeneralPath path) {
      return shapeDto.setPath(map(path.getPathIterator(null))).build();
    } else if (shape instanceof Line2D ln) {
      var dto =
          LineDto.newBuilder()
              .setX1(ln.getX1())
              .setY1(ln.getY1())
              .setX2(ln.getX2())
              .setY2(ln.getY2());
      return shapeDto.setLine(dto).build();
    } else if (shape instanceof Polygon2D poly) {
      var dto =
          PolygonShapeDto.newBuilder()
              .addAllXPoints(Floats.asList(poly.xpoints))
              .addAllYPoints(Floats.asList(poly.ypoints))
              .setNumPoints(poly.npoints);
      return shapeDto.setPolygon2D(dto).build();
    } else if (shape instanceof QuadCurve2D qc) {
      var dto =
          QuadCurveDto.newBuilder()
              .setX1(qc.getX1())
              .setY1(qc.getY1())
              .setCtrlX(qc.getCtrlX())
              .setCtrlY(qc.getCtrlY())
              .setX2(qc.getX2())
              .setY2(qc.getY2());
      return shapeDto.setQuadCurve(dto).build();
    } else if (shape instanceof RoundRectangle2D rr) {
      var dto =
          RoundRectangleDto.newBuilder()
              .setX(rr.getX())
              .setY(rr.getY())
              .setW(rr.getWidth())
              .setH(rr.getHeight())
              .setArcW(rr.getArcWidth())
              .setArcH(rr.getArcHeight());
      return shapeDto.setRoundRectangle(dto).build();
    } else if (shape instanceof java.awt.Rectangle rect) {
      var dto =
          RectangleDto.newBuilder()
              .setX(rect.x)
              .setY(rect.y)
              .setWidth(rect.width)
              .setHeight(rect.height);
      return shapeDto.setRectangle(dto).build();
    } else if (shape instanceof Rectangle2D rr) {
      var dto =
          Rectangle2DDto.newBuilder()
              .setX(rr.getX())
              .setY(rr.getY())
              .setWidth(rr.getWidth())
              .setHeight(rr.getHeight());
      return shapeDto.setRectangle2D(dto).build();
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
    } else if (shape instanceof Ellipse2D ellipse) {
      var dto =
          EllipseDto.newBuilder()
              .setX(ellipse.getX())
              .setY(ellipse.getY())
              .setWidth(ellipse.getWidth())
              .setHeight(ellipse.getHeight());
      return shapeDto.setEllipse(dto).build();
    } else if (shape instanceof Path2D path) {
      return shapeDto.setPath(map(path)).build();
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
