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
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.DrawablesGroupDto;

/**
 * @author Jagged
 *     <p>A grouping of DrawnElements to create a mini-layer like effect
 */
public class DrawablesGroup extends AbstractDrawing {
  private List<DrawnElement> drawableList;

  public DrawablesGroup(List<DrawnElement> drawableList) {
    this.drawableList = drawableList;
  }

  public DrawablesGroup(GUID id, List<DrawnElement> drawableList) {
    super(id);
    this.drawableList = drawableList;
  }

  public DrawablesGroup(DrawablesGroup other) {
    super(other);

    this.drawableList = new ArrayList<>(other.drawableList.size());
    for (final var element : other.drawableList) {
      this.drawableList.add(new DrawnElement(element));
    }
  }

  @Override
  public Drawable copy() {
    return new DrawablesGroup(this);
  }

  public List<DrawnElement> getDrawableList() {
    return drawableList;
  }

  @Override
  public Rectangle getBounds(Zone zone) {
    Rectangle bounds = null;
    for (DrawnElement element : drawableList) {
      Rectangle drawnBounds = new Rectangle(element.getDrawable().getBounds(zone));
      // Handle pen size
      Pen pen = element.getPen();
      int penSize = (int) (pen.getThickness() / 2 + 1);
      drawnBounds.setRect(
          drawnBounds.getX() - penSize,
          drawnBounds.getY() - penSize,
          drawnBounds.getWidth() + pen.getThickness(),
          drawnBounds.getHeight() + pen.getThickness());
      if (bounds == null) bounds = drawnBounds;
      else bounds.add(drawnBounds);
    }
    if (bounds != null) return bounds;
    return new Rectangle(0, 0, -1, -1);
  }

  @Override
  public @Nonnull Area getArea(Zone zone) {
    Area area = new Area();
    for (DrawnElement element : drawableList) {
      boolean isEraser = element.getPen().isEraser();

      if (isEraser) {
        // Optimization: erasing from nothing is a no-op.
        if (!area.isEmpty()) {
          area.subtract(element.getDrawable().getArea(zone));
        }
      } else {
        area.add(element.getDrawable().getArea(zone));
      }
    }
    return area;
  }

  @Override
  public DrawableDto toDto() {
    var dto = DrawablesGroupDto.newBuilder();
    dto.setId(getId().toString()).setLayer(getLayer().name());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    getDrawableList().forEach(d -> dto.addDrawnElements(d.toDto()));
    return DrawableDto.newBuilder().setDrawablesGroup(dto).build();
  }

  public static DrawablesGroup fromDto(DrawablesGroupDto dto) {
    var id = GUID.valueOf(dto.getId());
    var elements = new ArrayList<DrawnElement>();
    var elementDtos = dto.getDrawnElementsList();
    elementDtos.forEach(e -> elements.add(DrawnElement.fromDto(e)));
    var drawable = new DrawablesGroup(id, elements);
    if (dto.hasName()) {
      drawable.setName(dto.getName().getValue());
    }
    drawable.setLayer(Zone.Layer.valueOf(dto.getLayer()));
    return drawable;
  }

  @Override
  protected void draw(Zone zone, Graphics2D g) {
    // This should never be called
    for (DrawnElement element : drawableList) {
      element.getDrawable().draw(zone, g, element.getPen());
    }
  }

  @Override
  protected void drawBackground(Zone zone, Graphics2D g) {
    // This should never be called
  }
}
