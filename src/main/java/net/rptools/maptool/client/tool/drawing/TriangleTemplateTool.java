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

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.client.tool.ToolHelper;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.*;

/**
 * Draw a template for a right angle cone with a flat end. This currently only implements cones
 * where the base width = height.
 *
 * <p>Based on implementation of RadiusTemplateTool, but with different features...
 *
 * <p>The template shows the squares that are effected. The player chooses the starting vertex which
 * will be the point of the cone, then moves the mouse to set the ending vertex which becomes the
 * midpoint of the base of the cone. [Actually implemented in abstract drawing] Holding CTRL while
 * moving the mouse allows the user to move the entire template after the initial vertex has been
 * set but before the radius has been set. This allows users to move the AOE around to know where
 * they are casting a spell.
 *
 * <p>This class primarily handles the state machine of:
 *
 * <p>ToolSelectedAndInactive PlacingInitialVertex MovingVertexUsingCtrl AdjustingRadiusUsingMouse
 * -> Might change this? SendingDrawableToServerForRender...
 */
public class TriangleTemplateTool extends AbstractDrawingTool implements MouseMotionListener {
  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

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

  public TriangleTemplateTool() {}

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create the base template for the tool.
   *
   * @return The right angle cone template that is to be drawn.
   */
  protected AbstractTemplate createBaseTemplate() {
    return new TriangleTemplate();
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
   * Calculate the cell closest to a mouse point.
   *
   * @param e The event to be checked.
   * @return The cell at the mouse point in screen coordinates.
   */
  protected ZonePoint getCellAtMouse(MouseEvent e) {
    // Find the cell that the mouse is in.
    ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);

    // Typically here we would snap this to the grid...
    // For this template, since we are reducing the shape
    // to an aoe, there is no need to start from a specific
    // cell, so this will not be snapped to the grid.
    // This also helps this template specifically, because
    // users will want to make minor adjustments to this template
    // and won't want to be stuck on a given corner as a starting
    // point.
    return mouse;
  }

  /**
   * Calculate the radius between two cells based on a mouse event.
   *
   * @param e Mouse event being checked
   * @return The radius between the current mouse location and the vertex location.
   */
  protected int getRadiusAtMouse(MouseEvent e) {
    // To keep the re-sizing a bit smoother, this takes the distance between
    // ZonePoints instead of CellPoints. Basically we look at the starting
    // mouse and ending mouse point and calculate the distance based on a
    // "snapped" distance instead of snapping the starting and ending point
    // to the grid and calculating the distance from that. The end result
    // should be a much smoother and more predictable template sizing.
    ZonePoint workingZonePoint = getCellAtMouse(e);
    ZonePoint currentVertex = template.getVertex();

    int x = Math.abs(workingZonePoint.x - currentVertex.x);
    int y = Math.abs(workingZonePoint.y - currentVertex.y);

    int distance = template.getDistance(x, y);
    Grid grid = MapTool.getCampaign().getZone(template.getZoneId()).getGrid();
    float gridScaledDistance = (float) distance / (grid.getSize());
    int radius = Math.round(gridScaledDistance);
    return radius;
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
    template.setZoneId(renderer.getZone().getId());
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
      ZonePoint ep = getCellAtMouse(e);
      TriangleTemplate t = (TriangleTemplate) template;
      t.calculateTheta(e, renderer);
      renderer.repaint();
      controlOffset = null;
    } // endif
  }

  /*---------------------------------------------------------------------------------------------
   * DefaultTool Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.client.tool.DefaultTool#mouseMoved(MouseEvent)
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
   * @see net.rptools.maptool.client.ui.zone.ZoneOverlay#paintOverlay(ZoneRenderer, Graphics2D)
   */
  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    if (painting && renderer != null) {
      Pen pen = getPenForOverlay();
      AffineTransform old = g.getTransform();
      AffineTransform newTransform = g.getTransform();
      newTransform.concatenate(getPaintTransform(renderer));
      g.setTransform(newTransform);
      template.draw(g, pen);
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
   * @see AbstractDrawingTool#getPen()
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
    template.setZoneId(null);
    renderer.repaint();
  }

  /**
   * @see Tool#attachTo(ZoneRenderer)
   */
  @Override
  protected void attachTo(ZoneRenderer renderer) {
    template.setZoneId(renderer.getZone().getId());
    renderer.repaint();
    super.attachTo(renderer);
  }

  /**
   * @see Tool#getTooltip()
   */
  @Override
  public String getTooltip() {
    return "tool.radiustemplate.tooltip";
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
   * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);
    if (!painting) return;

    if (SwingUtilities.isLeftMouseButton(e)) {
      controlOffset = null;
      if (!anchorSet) {
        anchorSet = true;
        return;
      } // endif

      if (template.getRadius() < AbstractTemplate.MIN_RADIUS) return;
      TriangleTemplate t = (TriangleTemplate) template;

      setIsEraser(isEraser(e));
      template.setRadius(getRadiusAtMouse(e));

      ZonePoint vertex = template.getVertex();
      ZonePoint newPoint = new ZonePoint(vertex.x, vertex.y);
      completeDrawable(renderer.getZone().getId(), getPen(), template);
      setIsEraser(false);
      resetTool(newPoint);
    }
  }

  /**
   * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    super.mouseEntered(e);
    painting = true;
    renderer.repaint();
  }

  /**
   * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
    super.mouseExited(e);
    painting = false;
    renderer.repaint();
  }
}
