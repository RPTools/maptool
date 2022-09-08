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
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.server.proto.drawing.BurstTemplateDto;
import net.rptools.maptool.server.proto.drawing.DrawableDto;

/**
 * Create and paint a donut burst
 *
 * @author Jay
 */
public class BurstTemplate extends RadiusTemplate {
  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Renderer for the blast. The {@link Shape} is just a rectangle. */
  private final ShapeDrawable renderer = new ShapeDrawable(new Rectangle());

  /** Renderer for the blast. The {@link Shape} is just a rectangle. */
  private final ShapeDrawable vertexRenderer = new ShapeDrawable(new Rectangle());

  public BurstTemplate() {}

  public BurstTemplate(GUID id) {
    super(id);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * This methods adjusts the rectangle in the renderer to match the new radius, vertex, or
   * direction. Due to the fact that it is impossible to draw to the cardinal directions evenly when
   * the radius is an even number and still stay in the squares, that case isn't allowed.
   */
  private void adjustShape() {
    if (getZoneId() == null) return;
    Zone zone;
    if (MapTool.isHostingServer()) {
      zone = MapTool.getServer().getCampaign().getZone(getZoneId());
    } else {
      zone = MapTool.getCampaign().getZone(getZoneId());
    }
    if (zone == null) return;

    int gridSize = zone.getGrid().getSize();
    Rectangle r = (Rectangle) vertexRenderer.getShape();
    r.setBounds(getVertex().x, getVertex().y, gridSize, gridSize);
    r = (Rectangle) renderer.getShape();
    r.setBounds(getVertex().x, getVertex().y, gridSize, gridSize);
    r.x -= getRadius() * gridSize;
    r.y -= getRadius() * gridSize;
    r.width = r.height = (getRadius() * 2 + 1) * gridSize;
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden *Template Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.model.drawing.AbstractTemplate#setRadius(int) */
  @Override
  public void setRadius(int squares) {
    super.setRadius(squares);
    adjustShape();
  }

  /**
   * @see
   *     net.rptools.maptool.model.drawing.AbstractTemplate#setVertex(net.rptools.maptool.model.ZonePoint)
   */
  @Override
  public void setVertex(ZonePoint vertex) {
    super.setVertex(vertex);
    adjustShape();
  }

  /** @see net.rptools.maptool.model.drawing.AbstractTemplate#getDistance(int, int) */
  @Override
  public int getDistance(int x, int y) {
    return Math.max(x, y);
  }

  @Override
  public Rectangle getBounds() {
    Rectangle r = new Rectangle(renderer.getShape().getBounds());
    // We don't know pen width, so add some padding to account for it
    r.x -= 5;
    r.y -= 5;
    r.width += 10;
    r.height += 10;

    return r;
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractDrawing Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.model.drawing.AbstractDrawing#draw(java.awt.Graphics2D) */
  @Override
  protected void draw(Graphics2D g) {
    renderer.draw(g);
    vertexRenderer.draw(g);
  }

  /** @see net.rptools.maptool.model.drawing.AbstractDrawing#drawBackground(java.awt.Graphics2D) */
  @Override
  protected void drawBackground(Graphics2D g) {
    Composite old = g.getComposite();
    if (old != AlphaComposite.Clear)
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, DEFAULT_BG_ALPHA));
    renderer.drawBackground(g);
    g.setComposite(old);
  }

  @Override
  public Area getArea() {
    return renderer.getArea();
  }

  @Override
  public DrawableDto toDto() {
    var dto = BurstTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setZoneId(getZoneId().toString())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setBurstTemplate(dto).build();
  }
}
