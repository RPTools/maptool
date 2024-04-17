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
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.RectangularShape;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.ShapeDrawableDto;

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

  public ShapeDrawable(Shape shape) {
    this(shape, true);
  }

  public ShapeDrawable(ShapeDrawable other) {
    super(other);
    this.useAntiAliasing = other.useAntiAliasing;
    this.shape =
        switch (other.shape) {
            // Covers Rectangle, Ellipse2D, etc.
          case RectangularShape r -> (Shape) r.clone();
          case Polygon p -> new Polygon(p.xpoints, p.ypoints, p.npoints);
          case Area a -> new Area(a);
          default -> other.shape; // Assume anything else cannot be copied but is also okay.
        };
  }

  @Override
  public Drawable copy() {
    return new ShapeDrawable(this);
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

  private void restoreAA(Graphics2D g, Object oldAA) {
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
  }
}
