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
package net.rptools.maptool.client.tool.drawing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.RadiusCellTemplate;

/**
 * Draw a template for an effect with a radius. Make the template show the squares that are
 * effected, not just draw a circle. Let the player choose the vertex with the mouse and use the
 * wheel to set the radius. This allows the user to move the entire template where it is to be used
 * before placing it which is very important when casting a spell.
 *
 * @author naciron
 */
public class RadiusCellTemplateTool extends AbstractTemplateTool implements MouseMotionListener {
  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /**
   * The vertex that the effect is drawn on. It is the upper left corner of a specific grid
   * location.
   */
  protected AbstractTemplate template = createBaseTemplate();

  /** This flag controls the painting of the template. */
  protected boolean painting;

  /**
   * Has the anchoring point been set? When false, the anchor point is being placed. When true, the
   * area of effect is being drawn on the display.
   */
  protected boolean anchorSet;

  /**
   * The offset used to move the vertex when the control key is pressed. If this value is <code>null
   * </code> then this would be the first time that the control key had been reported in the mouse
   * event.
   */
  protected ZonePoint controlOffset;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /**
   * The width of the cursor. Since the cursor is a cross, this is the width of the horizontal bar
   * and the height of the vertical bar. Always make it an odd number to keep it aligned on the grid
   * properly.
   */
  public static final int CURSOR_WIDTH = 25;

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  public RadiusCellTemplateTool() {}

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create the base template for the tool.
   *
   * @return The radius template that is to be drawn.
   */
  protected AbstractTemplate createBaseTemplate() {
    return new RadiusCellTemplate();
  }

  /**
   * Calculate the cell at the mouse point. If it is different from the current point, make it the
   * current point and repaint.
   *
   * @param e The event to be checked.
   * @param point The current point.
   * @return Flag indicating that the value changed.
   */
  protected boolean setCellAtMouse(MouseEvent e, ZonePoint point) {
    ZonePoint working = getCellAtMouse(e);
    if (!working.equals(point)) {
      point.x = working.x;
      point.y = working.y;
      renderer.repaint();
      return true;
    } // endif
    return false;
  }

  /**
   * Calculate the cell closest to a mouse point. Cell coordinates are the upper left corner of the
   * cell.
   *
   * @param e The event to be checked.
   * @return The cell at the mouse point in screen coordinates.
   */
  protected ZonePoint getCellAtMouse(MouseEvent e) {
    ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
    CellPoint cp = renderer.getZone().getGrid().convert(mouse);
    return renderer.getZone().getGrid().convert(cp);
  }

  /**
   * Calculate the radius between two cells based on a mouse event.
   *
   * @param e Mouse event being checked
   * @return The radius between the current mouse location and the vertex location.
   */
  protected int getRadiusAtMouse(MouseEvent e) {
    CellPoint workingCell = renderer.getZone().getGrid().convert(getCellAtMouse(e));
    CellPoint vertexCell = renderer.getZone().getGrid().convert(template.getVertex());
    int x = Math.abs(workingCell.x - vertexCell.x);
    int y = Math.abs(workingCell.y - vertexCell.y);
    return template.getDistance(x, y);
  }

  /**
   * Paint a cursor
   *
   * @param g Where to paint.
   * @param paint Data to draw the cursor
   * @param thickness The thickness of the cursor.
   * @param vertex The vertex holding the cursor.
   */
  protected void paintCursor(Graphics2D g, Paint paint, float thickness, ZonePoint vertex) {
    g.setPaint(paint);
    g.setStroke(new BasicStroke(thickness));
    int grid = renderer.getZone().getGrid().getSize();
    g.drawRect(vertex.x, vertex.y, grid, grid);

    if (1 == 1) return;
    int halfCursor = CURSOR_WIDTH / 2;
    g.setPaint(paint);
    g.setStroke(new BasicStroke(thickness));
    g.drawLine(vertex.x - halfCursor, vertex.y, vertex.x + halfCursor, vertex.y);
    g.drawLine(vertex.x, vertex.y - halfCursor, vertex.x, vertex.y + halfCursor);
  }

  /**
   * Get the pen set up to paint the overlay.
   *
   * @return The pen used to paint the overlay.
   */
  protected Pen getPenForOverlay() {
    // Get the pen and modify to only show a cursor and the boundary
    Pen pen = getPen(); // new copy of pen, OK to modify
    pen.setBackgroundMode(Pen.MODE_SOLID);
    pen.setForegroundMode(Pen.MODE_SOLID);
    pen.setThickness(3);
    if (pen.isEraser()) {
      pen.setEraser(false);
      pen.setPaint(new DrawableColorPaint(Color.WHITE));
    } // endif
    return pen;
  }

  /**
   * Paint the radius value in feet.
   *
   * @param g Where to paint.
   * @param p Vertex where radius is painted.
   */
  protected void paintRadius(Graphics2D g, ZonePoint p) {
    if (template.getRadius() > 0 && anchorSet) {
      ScreenPoint centerText = ScreenPoint.fromZonePoint(renderer, p);
      centerText.translate(CURSOR_WIDTH, -CURSOR_WIDTH);
      ToolHelper.drawMeasurement(
          g,
          template.getRadius() * renderer.getZone().getUnitsPerCell(),
          (int) centerText.x,
          (int) centerText.y);
    } // endif
  }

  /**
   * New instance of the template, at the passed vertex
   *
   * @param vertex The starting vertex for the new template or <code>null</code> if we should use
   *     the current template's vertex.
   */
  protected void resetTool(ZonePoint vertex) {
    anchorSet = false;
    if (vertex == null) {
      vertex = template.getVertex();
      vertex = new ZonePoint(vertex.x, vertex.y); // Must create copy!
    } // endif
    template = createBaseTemplate();
    template.setVertex(vertex);
    controlOffset = null;
    renderer.repaint();
  }

  /**
   * Handles setting the vertex when the control key is pressed during mouse movement. A change in
   * the passed vertex causes the template to repaint the zone.
   *
   * @param e The mouse movement event.
   * @param vertex The vertex being modified.
   */
  protected void handleControlOffset(MouseEvent e, ZonePoint vertex) {
    ZonePoint working = getCellAtMouse(e);
    if (controlOffset == null) {
      controlOffset = working;
      controlOffset.x = working.x - vertex.x;
      controlOffset.y = working.y - vertex.y;
    } else {
      working.x = working.x - controlOffset.x;
      working.y = working.y - controlOffset.y;
      if (!working.equals(vertex)) {
        if (vertex == template.getVertex()) {
          template.setVertex(working);
        } else {
          vertex.x = working.x;
          vertex.y = working.y;
        } // endif
        renderer.repaint();
      } // endif
    } // endif
  }

  /**
   * Set the radius on a mouse move after the anchor has been set.
   *
   * @param e Current mouse locations
   */
  protected void setRadiusFromAnchor(MouseEvent e) {
    template.setRadius(getRadiusAtMouse(e));
  }

  /**
   * Handle mouse movement. Done here so that subclasses can still benefit from the code in
   * DefaultTool w/o rewriting it.
   *
   * @param e Current mouse location
   */
  protected void handleMouseMovement(MouseEvent e) {
    // Set the anchor
    ZonePoint vertex = template.getVertex();
    if (!anchorSet) {
      setCellAtMouse(e, vertex);
      controlOffset = null;

      // Move the anchor if control pressed.
    } else if (SwingUtil.isControlDown(e)) {
      handleControlOffset(e, vertex);

      // Set the radius and repaint
    } else {
      setRadiusFromAnchor(e);
      renderer.repaint();
      controlOffset = null;
    } // endif
  }

  /*---------------------------------------------------------------------------------------------
   * DefaultTool Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.client.tool.DefaultTool#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    handleMouseMovement(e);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractDrawingTool Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.client.ui.zone.ZoneOverlay#paintOverlay(ZoneRenderer,
   *     java.awt.Graphics2D)
   */
  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (painting && renderer != null) {
      Pen pen = getPenForOverlay();
      AffineTransform old = g.getTransform();
      AffineTransform newTransform = g.getTransform();
      newTransform.concatenate(getPaintTransform(renderer));
      g.setTransform(newTransform);
      template.draw(renderer.getZone(), g, pen);
      Paint paint = pen.getPaint() != null ? pen.getPaint().getPaint() : null;
      paintCursor(g, paint, pen.getThickness(), template.getVertex());
      g.setTransform(old);
      paintRadius(g, template.getVertex());
    } // endif
  }

  /**
   * New instance of the template, at the current vertex
   *
   * @see Tool#resetTool()
   */
  @Override
  protected void resetTool() {
    if (!anchorSet) {
      super.resetTool();
      return;
    }
    resetTool(null);
  }

  /**
   * It is OK to modify the pen returned by this method
   *
   * @see AbstractTemplateTool#getPen()
   */
  @Override
  protected Pen getPen() {
    // Just paint the foreground
    Pen pen = super.getPen();
    pen.setBackgroundMode(Pen.MODE_SOLID);
    return pen;
  }

  /**
   * @see Tool#detachFrom(ZoneRenderer)
   */
  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    super.detachFrom(renderer);
    renderer.repaint();
  }

  /**
   * @see Tool#attachTo(ZoneRenderer)
   */
  @Override
  protected void attachTo(ZoneRenderer renderer) {
    renderer.repaint();
    super.attachTo(renderer);
  }

  /**
   * @see Tool#getTooltip()
   */
  @Override
  public String getTooltip() {
    return "tool.radiusCellTemplate.tooltip";
  }

  /**
   * @see Tool#getInstructions()
   */
  @Override
  public String getInstructions() {
    return "tool.radiustemplate.instructions";
  }

  /*---------------------------------------------------------------------------------------------
   * MouseListener Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);
    if (!painting) return;

    if (SwingUtilities.isLeftMouseButton(e)) {
      // Need to set the anchor?
      controlOffset = null;
      if (!anchorSet) {
        anchorSet = true;
        return;
      } // endif

      // Need to finish the radius?
      if (template.getRadius() < AbstractTemplate.MIN_RADIUS) return;

      // Set the eraser, set the drawable, reset the tool.
      setIsEraser(isEraser(e));
      template.setRadius(getRadiusAtMouse(e));
      ZonePoint vertex = template.getVertex();
      ZonePoint newPoint = new ZonePoint(vertex.x, vertex.y);
      completeDrawable(getPen(), template);
      setIsEraser(false);
      resetTool(newPoint);
    }
  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    super.mouseEntered(e);
    painting = true;
    renderer.repaint();
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
    super.mouseExited(e);
    painting = false;
    renderer.repaint();
  }
}
