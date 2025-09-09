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

import com.google.protobuf.StringValue;
import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.*;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.ShapeDrawableDto;
import org.apache.batik.ext.awt.geom.ExtendedGeneralPath;
import org.apache.batik.ext.awt.geom.Polygon2D;

/** An rectangle */
public class ShapeDrawable extends AbstractDrawing {
  private final Shape shape;
  private final boolean useAntiAliasing;

  public ShapeDrawable(GUID id, Shape shape, boolean useAntiAliasing) {
    super(id);
    this.shape = shape;
    this.useAntiAliasing = useAntiAliasing;
  }

  public ShapeDrawable(Shape shape, boolean useAntiAliasing) {
    this.shape = shape;
    this.useAntiAliasing = useAntiAliasing;
  }

  public ShapeDrawable(ShapeDrawable other) {
    super(other);
    this.useAntiAliasing = other.useAntiAliasing;
    this.shape =
        switch (other.shape) {
          case Area a -> new Area(a);
          case CubicCurve2D cc ->
              new CubicCurve2D.Double(
                  cc.getX1(),
                  cc.getY1(),
                  cc.getCtrlX1(),
                  cc.getCtrlY1(),
                  cc.getCtrlX2(),
                  cc.getCtrlY2(),
                  cc.getX2(),
                  cc.getY2());
          case ExtendedGeneralPath egp -> new ExtendedGeneralPath_Double(egp);
          case Line2D ln -> new Line2D.Double(ln.getX1(), ln.getY1(), ln.getX2(), ln.getY2());
          case Path2D path -> new ExtendedGeneralPath_Double(path);
          case Polygon p -> new Polygon(p.xpoints, p.ypoints, p.npoints);
          case Polygon2D p -> new Polygon2D(p.xpoints, p.ypoints, p.npoints);
          case RectangularShape r ->
              (Shape) r.clone(); // Arc2D, Ellipse2D, Rectangle2D, RoundRectangle2D
          case QuadCurve2D qc ->
              new QuadCurve2D.Double(
                  qc.getX1(), qc.getY1(), qc.getCtrlX(), qc.getCtrlY(), qc.getX2(), qc.getY2());
          default -> other.shape; // Assume anything else cannot be copied but is also okay.
        };
  }

  @Override
  public Drawable copy() {
    return new ShapeDrawable(this);
  }

  /**
   * Get a descriptive name for the type of shape wrapped by this {@code ShapeDrawable}.
   *
   * <p>Note: do not use this method for type checks. It is only useful for producing human-readable
   * text, including by building translation keys. If you need type checking, using {@link
   * #getShape()} along with `instanceof`!
   *
   * @return The type of shape contained in this drawable.
   */
  public String getShapeTypeName() {
    return switch (shape) {
      case Arc2D ignored -> "Arc";
      case CubicCurve2D ignored -> "CubicCurve";
      case Ellipse2D ignored -> "Oval";
      case QuadCurve2D ignored -> "QuadCurve";
      case Rectangle ignored -> "Rectangle";
      case Rectangle2D ignored -> "Rectangle";
      case RoundRectangle2D ignored -> "RoundRectangle";
      case Line2D ignored -> "Line";
      case Path2D ignored -> "Path";
      case ExtendedGeneralPath ignored -> "Path";
      case Polygon ignored -> "Polygon";
      case Polygon2D ignored -> "Polygon";
      case Area ignored -> "Area";
      default -> "Unknown";
    };
  }

  public boolean getUseAntiAliasing() {
    return useAntiAliasing;
  }

  public java.awt.Rectangle getBounds() {
    return shape.getBounds();
  }

  @Override
  public java.awt.Rectangle getBounds(Zone zone) {
    return getBounds();
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    return new Area(shape);
  }

  @Override
  public DrawableDto toDto() {
    var shape = Mapper.map(getShape());
    var dto =
        ShapeDrawableDto.newBuilder()
            .setId(getId().toString())
            .setLayer(getLayer().name())
            .setShape(shape)
            .setUseAntiAliasing(getUseAntiAliasing());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setShapeDrawable(dto).build();
  }

  public static ShapeDrawable fromDto(ShapeDrawableDto dto) {
    var shape = Mapper.map(dto.getShape());
    var id = GUID.valueOf(dto.getId());
    var drawable = new ShapeDrawable(id, shape, dto.getUseAntiAliasing());
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }

  @Override
  protected void draw(Zone zone, Graphics2D g) {
    Object oldAA = applyAA(g);
    g.draw(shape);
    restoreAA(g, oldAA);
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {
    Object oldAA = applyAA(g);
    g.fill(shape);
    restoreAA(g, oldAA);
  }

  public Shape getShape() {
    return shape;
  }

  private Object applyAA(Graphics2D g) {
    Object oldAA = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(
        RenderingHints.KEY_ANTIALIASING,
        useAntiAliasing ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    return oldAA;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append("antiAliasing=").append(getUseAntiAliasing()).append(";");
    sb.append("shapeType=").append(getShapeTypeName()).append(";");
    sb.append("bounds=\"");
    sb.append("x=").append(getBounds().x).append(";");
    sb.append("y=").append(getBounds().y).append(";");
    sb.append("width=").append(getBounds().width).append(";");
    sb.append("height=").append(getBounds().height).append("\";");
    return sb.toString();
  }

  private void restoreAA(Graphics2D g, Object oldAA) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
  }
}
