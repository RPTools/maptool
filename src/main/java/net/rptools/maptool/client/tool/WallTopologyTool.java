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
package net.rptools.maptool.client.tool;

import com.google.common.eventbus.Subscribe;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.swing.walls.WallConfigurationController;
import net.rptools.maptool.client.tool.drawing.TopologyTool;
import net.rptools.maptool.client.tool.rig.Handle;
import net.rptools.maptool.client.tool.rig.Movable;
import net.rptools.maptool.client.tool.rig.Snap;
import net.rptools.maptool.client.tool.rig.WallTopologyRig;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.topology.Vertex;
import net.rptools.maptool.model.topology.Wall;
import net.rptools.maptool.model.zones.WallTopologyChanged;
import org.locationtech.jts.math.Vector2D;

public class WallTopologyTool extends DefaultTool implements ZoneOverlay {
  private Point2D currentPosition =
      new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

  private final WallConfigurationController controlPanel = new WallConfigurationController();
  private @Nullable WallTopologyRig.MovableWall selectedWall;

  /** The current tool behaviour. Each operation enters a distinct mode so we don't cross-talk. */
  private ToolMode mode = new NilToolMode();

  private final TopologyTool.MaskOverlay maskOverlay = new TopologyTool.MaskOverlay();

  @Override
  public String getTooltip() {
    return "tool.walltopology.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.walltopology.instructions";
  }

  @Override
  public boolean isAvailable() {
    return MapTool.getPlayer().isGM();
  }

  @Override
  protected void installKeystrokes(Map<KeyStroke, Action> actionMap) {
    super.installKeystrokes(actionMap);

    actionMap.put(
        KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
        new AbstractAction() {
          @Override
          public void actionPerformed(ActionEvent e) {
            mode.delete();
            renderer.repaint();
          }
        });
  }

  @Override
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    currentPosition = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    var rig = new WallTopologyRig(this::getHandleSelectDistance, this::getWallSelectDistance);
    rig.setWalls(getZone().getWalls());
    changeToolMode(new BasicToolMode(this, rig));

    MapTool.getFrame().showControlPanel(controlPanel.getView().getRootComponent());

    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    new MapToolEventBus().getMainEventBus().unregister(this);

    controlPanel.unbind();
    MapTool.getFrame().removeControlPanel();

    changeToolMode(new NilToolMode());
    super.detachFrom(renderer);
  }

  @Override
  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    // Paint legacy masks. This isn't strictly necessary, but I want to do it so that users can
    // trace walls over masks if converting by hand.
    maskOverlay.paintOverlay(renderer, g);

    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(renderer.getViewOffsetX(), renderer.getViewOffsetY());
    g2.scale(renderer.getScale(), renderer.getScale());
    SwingUtil.useAntiAliasing(g2);
    g2.setComposite(AlphaComposite.SrcOver);

    mode.paint(g2);
  }

  @Override
  protected void resetTool() {
    if (!mode.cancel()) {
      super.resetTool();
    }
    renderer.repaint();
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    super.mouseMoved(e);
    mode.mouseMoved(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    var skipDefault = mode.mouseDragged(updateCurrentPosition(e), getSnapMode(e), e);
    if (!skipDefault) {
      super.mouseDragged(e);
    }
    renderer.repaint();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);
    mode.mousePressed(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    super.mouseClicked(e);
    mode.mouseClicked(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    super.mouseReleased(e);
    mode.mouseReleased(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  private double getHandleRadius() {
    return 4. / Math.min(1., renderer.getScale());
  }

  private double getWallHalfWidth() {
    return 1.5 / Math.min(1, renderer.getScale());
  }

  private double getHandleSelectDistance() {
    // Include a bit of leniency for the user.
    return getHandleRadius() * 1.125;
  }

  private double getWallSelectDistance() {
    return getWallHalfWidth() * 1.5;
  }

  private void setWallPropertiesFromConfigPanel(Wall wall) {
    var current = controlPanel.getModel();
    wall.copyDataFrom(current);
  }

  private void setSelectedWall(@Nullable WallTopologyRig.MovableWall wall) {
    selectedWall = wall;
    if (selectedWall == null) {
      controlPanel.unbind();
    } else {
      controlPanel.bind(getZone(), selectedWall.getSource());
    }
  }

  private boolean isSelectedWall(WallTopologyRig.MovableWall wall) {
    var selected = getSelectedWall();
    return selected.isPresent() && wall.isForSameElement(selected.get());
  }

  private Optional<WallTopologyRig.MovableWall> getSelectedWall() {
    return Optional.ofNullable(selectedWall);
  }

  private void changeToolMode(ToolMode newMode) {
    mode.deactivate();
    mode = newMode;
    mode.activate();
  }

  private Point2D getCurrentPosition() {
    return currentPosition;
  }

  private Point2D updateCurrentPosition(MouseEvent e) {
    return currentPosition = ScreenPoint.convertToZone2d(renderer, e.getX(), e.getY());
  }

  private Snap getSnapMode(MouseEvent e) {
    if (SwingUtil.isControlDown(e)) {
      return Snap.fine(getZone().getGrid());
    }
    return Snap.none();
  }

  private WallTopologyRig.Element<?> findNearbyElement(
      Point2D point, WallTopologyRig rig, WallTopologyRig.MovableVertex ignoreVertex) {
    var extraSpace = 2.;
    return rig.getNearbyElement(
            point,
            extraSpace,
            (WallTopologyRig.Element<?> other) -> {
              switch (other) {
                case WallTopologyRig.MovableVertex movableVertex -> {
                  return !ignoreVertex.isForSameElement(movableVertex);
                }
                case WallTopologyRig.MovableWall movableWall -> {
                  if (ignoreVertex.isForSameElement(movableWall.getFrom())
                      || ignoreVertex.isForSameElement(movableWall.getTo())) {
                    return false;
                  }

                  return true;
                }
              }
            })
        .orElse(null);
  }

  @Subscribe
  private void onTopologyChanged(WallTopologyChanged event) {
    var selected = getSelectedWall();

    var rig = new WallTopologyRig(this::getHandleSelectDistance, this::getWallSelectDistance);
    rig.setWalls(getZone().getWalls());
    changeToolMode(new BasicToolMode(this, rig));

    // If the selected wall still exists, rebind it.
    var newSelectedWall =
        selected.flatMap(s -> rig.getWall(s.getSource().from(), s.getSource().to())).orElse(null);
    setSelectedWall(newSelectedWall);
  }

  /**
   * Represents the behaviour of the tool at a point in time.
   *
   * <p>By encapsulating this state into separate modes, it avoids the possibility of mixing up
   * which actions we are expecting.
   *
   * <p>The various mouse event handler include a {@link Snap} parameter. This is a strategy for
   * snapping arbitrary zone points to the grid based on the keys the user is pressing. If a tool
   * doesn't support snapping, it is free to ignore this parameter and use the raw position. If
   * snapaping is supported, the {@link Snap#snap(Point2D)} method should be used to snap to grid.
   */
  private interface ToolMode {
    /** Called when the mode becomes the current mode. */
    void activate();

    /**
     * Called when the mode stops being the current mode.
     *
     * <p>When switching modes, the {@code deactivate()} of the old mode will be called before the
     * {@link #activate()} of the new mode.
     */
    void deactivate();

    /**
     * Cancels the current tool mode.
     *
     * <p>Typoically this requires restoring zone state to what it was prior to the last commit.
     *
     * @return {@code true} if the tool mode has its own cancel behaviour; {@code false} if the
     *     regular behaviour (revert to pointer tool) should apply.
     */
    boolean cancel();

    /**
     * Called when the delete key is pressed.
     *
     * <p>Implementations should use this to remove the current selection.
     */
    void delete();

    /**
     * Handles a mouse move.
     *
     * <p><strong>Note:</strong> if the mouse is dragged, {@link #mouseDragged(Point2D, Snap,
     * MouseEvent)} will be called instead of this method..
     *
     * @param point The location of the mouse event in zone units.
     * @param snapMode The strategy for snapping {@code point} to the grid.
     * @param event The original AWT mouse event.
     */
    void mouseMoved(Point2D point, Snap snapMode, MouseEvent event);

    /**
     * Handles a mouse drag.
     *
     * @param point The location of the mouse event in zone units.
     * @param snapMode The strategy for snapping {@code point} to the grid.
     * @param event The original AWT mouse event.
     * @return {@code true} if the default mouse drag behaviour should forbidden.
     */
    boolean mouseDragged(Point2D point, Snap snapMode, MouseEvent event);

    /**
     * Handles a mouse press.
     *
     * @param point The location of the mouse event in zone units.
     * @param snapMode The strategy for snapping {@code point} to the grid.
     * @param event The original AWT mouse event.
     */
    void mousePressed(Point2D point, Snap snapMode, MouseEvent event);

    /**
     * Handles a mouse click.
     *
     * @param point The location of the mouse event in zone units.
     * @param snapMode The strategy for snapping {@code point} to the grid.
     * @param event The original AWT mouse event.
     */
    void mouseClicked(Point2D point, Snap snapMode, MouseEvent event);

    /**
     * Handles a mouse release.
     *
     * @param point The location of the mouse event in zone units.
     * @param snapMode The strategy for snapping {@code point} to the grid.
     * @param event The original AWT mouse event.
     */
    void mouseReleased(Point2D point, Snap snapMode, MouseEvent event);

    /**
     * Draws any custom visuals required by the tool mode.
     *
     * @param g2 The graphics context for drawing.
     */
    void paint(Graphics2D g2);
  }

  /**
   * The mode that does nothing.
   *
   * <p>Convenient for when the tool is unattached.
   */
  private static final class NilToolMode implements ToolMode {
    @Override
    public void activate() {}

    @Override
    public void deactivate() {}

    @Override
    public boolean cancel() {
      return false;
    }

    @Override
    public void delete() {}

    @Override
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public boolean mouseDragged(Point2D point, Snap snapMode, MouseEvent event) {
      return false;
    }

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mouseReleased(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mouseClicked(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void paint(Graphics2D g2) {}
  }

  /**
   * Base class for fully functional tools.
   *
   * <p>This keeps track of the tool itself along with the rig that the tool mode can use.
   */
  private abstract static class ToolModeBase implements ToolMode {
    protected final WallTopologyTool tool;
    protected final WallTopologyRig rig;

    protected ToolModeBase(WallTopologyTool tool, WallTopologyRig rig) {
      this.tool = tool;
      this.rig = rig;
    }

    @Override
    public void activate() {}

    @Override
    public void deactivate() {}

    @Override
    public boolean cancel() {
      return false;
    }

    @Override
    public void delete() {}

    @Override
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public boolean mouseDragged(Point2D point, Snap snapMode, MouseEvent event) {
      return false;
    }

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mouseClicked(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mouseReleased(Point2D point, Snap snapMode, MouseEvent event) {}

    protected Paint getWallStrokePaint(WallTopologyRig.MovableWall wall) {
      if (tool.isSelectedWall(wall)) {
        return AppStyle.selectedWallOutlineColor;
      }
      return AppStyle.wallTopologyOutlineColor;
    }

    /**
     * Get a special paint for the handle if one is applicable.
     *
     * @param handle The handle to get the fill paint for.
     * @return The paint for the handle.
     */
    protected Paint getHandleFill(Handle<Vertex> handle) {
      return Color.white;
    }

    protected Paint getWallFill(Movable<Wall> wall) {
      return AppStyle.wallTopologyColor;
    }

    protected void paintHandle(Graphics2D g2, Point2D point, Paint fill) {
      var handleRadius = tool.getHandleRadius();
      var handleOutlineStroke =
          new BasicStroke(
              (float) (handleRadius / 4.), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      var handleOutlineColor = AppStyle.wallTopologyOutlineColor;

      var shape =
          new Ellipse2D.Double(
              point.getX() - handleRadius,
              point.getY() - handleRadius,
              2 * handleRadius,
              2 * handleRadius);

      g2.setPaint(fill);
      g2.fill(shape);

      g2.setStroke(handleOutlineStroke);
      g2.setPaint(handleOutlineColor);
      g2.draw(shape);
    }

    @Override
    public void paint(Graphics2D g2) {
      var handleRadius = tool.getHandleRadius();

      Rectangle2D bounds = g2.getClipBounds().getBounds2D();
      // Pad the bounds by a bit so handles whose center is just outside will still show up.
      var padding = handleRadius;
      bounds.setRect(
          bounds.getX() - padding,
          bounds.getY() - padding,
          bounds.getWidth() + 2 * padding,
          bounds.getHeight() + 2 * padding);

      // region Wall decorations.
      // These are mere prototypes that sit at (0, 0). They will be instanced wherever they are
      // needed during painting.
      var directionalArrow = buildDirectionalArrowDecoration();
      var sourceDecoration = buildWallSourceDecoration();
      var targetDecoration = buildWallTargetDecoration();
      // endregion

      var wallStroke =
          new BasicStroke(
              (float) (2 * tool.getWallHalfWidth()), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      var wallOutlineColor = AppStyle.wallTopologyOutlineColor;
      var wallOutlineStroke =
          new BasicStroke(
              (float) (wallStroke.getLineWidth() * 1.5),
              wallStroke.getEndCap(),
              wallStroke.getLineJoin());
      var decorationStroke =
          new BasicStroke(1.5f, wallStroke.getEndCap(), wallStroke.getLineJoin());
      var walls = rig.getWallsWithin(bounds);
      for (var wall : walls) {
        var asSegment = wall.asLineSegment();
        var asVector = Vector2D.create(asSegment.p0, asSegment.p1);

        var lengthSquared = asVector.lengthSquared();
        if (lengthSquared <= 4 * handleRadius * handleRadius) {
          // The wall is so small it isn't worth drawing the wall or its decorations.
          continue;
        }

        var angle = asVector.angle();
        var normVector = asVector.normalize();

        {
          // Draw the wall itself.
          var shape = new Path2D.Double();
          shape.moveTo(asSegment.p0.getX(), asSegment.p0.getY());
          shape.lineTo(asSegment.p1.getX(), asSegment.p1.getY());

          // Draw it twice to get a black border effects without having to stroke the path.
          g2.setStroke(wallOutlineStroke);
          g2.setPaint(getWallStrokePaint(wall));
          g2.draw(shape);

          g2.setStroke(wallStroke);
          g2.setPaint(getWallFill(wall));
          g2.draw(shape);
        }

        // Next up: decorations
        g2.setStroke(decorationStroke);
        g2.setPaint(wallOutlineColor);
        {
          // Draw a tiny arrow head to indicate the target end of the wall.
          var preTransform = g2.getTransform();

          var point = normVector.multiply(-0.75 * handleRadius).translate(asSegment.p1);
          g2.translate(point.getX(), point.getY());
          g2.rotate(angle);

          g2.draw(targetDecoration);
          g2.fill(targetDecoration);

          g2.setTransform(preTransform);
        }
        if (wall.getSource().direction() != Wall.Direction.Both) {
          // Draw an arrow through the midpoint of the wall to indicate its direction.
          var preTransform = g2.getTransform();
          var wallMidpoint = asSegment.midPoint();
          g2.translate(wallMidpoint.getX(), wallMidpoint.getY());
          g2.rotate(angle);
          if (wall.getSource().direction() == Wall.Direction.Left) {
            g2.scale(-1, -1);
          }

          g2.draw(directionalArrow);

          g2.setTransform(preTransform);
        }

        if (lengthSquared > 12 * handleRadius * handleRadius) {
          // Draw a bar to indicate the source end of the wall.
          // This is optional if the wall is on the small side.
          var preTransform = g2.getTransform();
          var barCenter = normVector.multiply(1.5 * handleRadius).translate(asSegment.p0);
          g2.translate(barCenter.getX(), barCenter.getY());
          g2.rotate(angle);

          g2.draw(sourceDecoration);

          g2.setTransform(preTransform);
        }
      }

      var vertices = rig.getHandlesWithin(bounds);
      for (var handle : vertices) {
        paintHandle(g2, handle.getPosition(), getHandleFill(handle));
      }
    }

    /**
     * Builds a shape indicating the directionality of a wall.
     *
     * @return An arrow through (0, 0) pointing toward the positive y-axis.
     */
    protected Shape buildDirectionalArrowDecoration() {
      var result = new Path2D.Double();
      var arrowLength = 8 * tool.getWallHalfWidth();
      var arrowTip = new Point2D.Double(0, arrowLength * 1.5);
      result.moveTo(arrowTip.getX(), arrowTip.getY());
      result.lineTo(0, -arrowLength * 0.5);
      result.moveTo(arrowTip.getX(), arrowTip.getY());
      result.lineTo(-arrowLength * 0.25, arrowTip.getY() - arrowLength * 0.5);
      result.moveTo(arrowTip.getX(), arrowTip.getY());
      result.lineTo(arrowLength * 0.25, arrowTip.getY() - arrowLength * 0.5);
      return result;
    }

    protected Shape buildWallSourceDecoration() {
      var result = new Path2D.Double();
      var halfHeight = 4 * tool.getWallHalfWidth();
      result.moveTo(0, -halfHeight);
      result.lineTo(0, halfHeight);
      return result;
    }

    protected Shape buildWallTargetDecoration() {
      var result = new Path2D.Double();
      var halfHeight = 4 * tool.getWallHalfWidth();
      result.moveTo(0, 0);
      result.lineTo(-halfHeight, halfHeight);
      result.quadTo(-0.75 * halfHeight, 0, -halfHeight, -halfHeight);
      result.closePath();

      return result;
    }
  }

  /**
   * Tool mode used when not manipulating walls.
   *
   * <p>This mode supports the following actions:
   *
   * <ol>
   *   <li>Canceling exits the {@code WallTopologyTool}, transitioning to {@link PointerTool}.
   *   <li>Delete key deletes the selected wall.
   *   <li>Left-clicking a wall selects that wall and unselects other walls.
   *   <li>Double-clicking a wall or vertex deletes that element.
   *   <li>Left-clicking empty space starts a new wall and transitions to {@link
   *       DrawingWallToolMode}.
   *   <li>Right-clicking a vertex starts a new wall connected to that vertex and transitions to
   *       {@link DrawingWallToolMode}.
   *   <li>Right-clicking a wall splits the wall with a new vertex, and starts a new wall connected
   *       to that vertex, transitioning to {@link DrawingWallToolMode}.
   *   <li>Left-dragging a vertex transitions to {@link DragVertexToolMode}.
   *   <li>Left-dragging a wall transitions to {@link DragWallToolMode}.
   * </ol>
   */
  private static final class BasicToolMode extends ToolModeBase {
    // The hovered handle. This is the candidate for any pending mouse event. E.g., a mouse pressed
    // can start a drag operation on it.
    private @Nullable WallTopologyRig.Element<?> currentElement;
    // If mouse is pressed on currentElement, it becomes a drag candidate if the mouse is then
    // dragged.
    private @Nullable WallTopologyRig.Element<?> potentialDragElement;
    private Point2D potentialDragPoint = new Point2D.Double();

    public BasicToolMode(WallTopologyTool tool, WallTopologyRig rig) {
      super(tool, rig);
    }

    @Override
    public void activate() {
      currentElement = rig.getNearbyElement(tool.getCurrentPosition()).orElse(null);
    }

    @Override
    public void delete() {
      tool.getSelectedWall().ifPresent(selected -> selected.delete());
    }

    @Override
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {
      currentElement = rig.getNearbyElement(point).orElse(null);
    }

    @Override
    public boolean mouseDragged(Point2D point, Snap snapMode, MouseEvent event) {
      boolean handled = false;

      if (SwingUtilities.isLeftMouseButton(event)) {
        switch (potentialDragElement) {
          case null -> {
            // Do nothing.
          }
          case WallTopologyRig.MovableVertex movableVertex -> {
            tool.changeToolMode(
                new DragVertexToolMode(tool, rig, movableVertex, potentialDragPoint));
            handled = true;
          }
          case WallTopologyRig.MovableWall movableWall -> {
            tool.changeToolMode(new DragWallToolMode(tool, rig, movableWall, potentialDragPoint));
            handled = true;
          }
        }
      }

      return handled;
    }

    @Override
    public void mouseClicked(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        switch (currentElement) {
          case null -> {
            // Hit blank space. Start a new wall.
            var newWall = rig.addDegenerateWall(snapMode.snap(point));
            tool.setWallPropertiesFromConfigPanel(newWall.getSource());
            tool.setSelectedWall(newWall);
            tool.changeToolMode(new DrawingWallToolMode(tool, rig, newWall));
          }
          case WallTopologyRig.MovableVertex movableVertex -> {
            if (event.getClickCount() == 2) {
              var selectionToBeDeleted =
                  tool.getSelectedWall()
                      .filter(
                          s ->
                              s.getFrom().isForSameElement(movableVertex)
                                  || s.getTo().isForSameElement(movableVertex))
                      .isPresent();
              if (selectionToBeDeleted) {
                tool.setSelectedWall(null);
              }

              movableVertex.delete();
              MapTool.serverCommand().replaceWalls(tool.getZone(), rig.commit());
            }
          }
          case WallTopologyRig.MovableWall movableWall -> {
            if (event.getClickCount() == 2) {
              var isSelected = tool.isSelectedWall(movableWall);
              if (isSelected) {
                tool.setSelectedWall(null);
              }

              movableWall.delete();
              MapTool.serverCommand().replaceWalls(tool.getZone(), rig.commit());
            } else {
              tool.setSelectedWall(movableWall);
            }
          }
        }
      }
      if (SwingUtilities.isRightMouseButton(event)) {
        // Connect to an existing element.
        rig.getOrCreateMergeCandidate(snapMode.snap(point), currentElement)
            .ifPresent(
                mergeCandidate -> {
                  var newWall = rig.addConnectedWall(mergeCandidate);
                  tool.setWallPropertiesFromConfigPanel(newWall.getSource());
                  tool.setSelectedWall(newWall);
                  tool.changeToolMode(new DrawingWallToolMode(tool, rig, newWall));
                });
      }
    }

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        potentialDragElement = currentElement;
        potentialDragPoint = point;
      }
    }

    @Override
    public void mouseReleased(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        potentialDragElement = null;
      }
    }

    @Override
    public Paint getHandleFill(Handle<Vertex> handle) {
      if (currentElement != null && currentElement.isForSameElement(handle)) {
        return Color.green;
      }
      return super.getHandleFill(handle);
    }

    @Override
    protected Paint getWallFill(Movable<Wall> wall) {
      if (currentElement != null && currentElement.isForSameElement(wall)) {
        return AppStyle.highlightedWallTopologyColor;
      }
      return super.getWallFill(wall);
    }
  }

  /**
   * Tool mode used for drawing walls.
   *
   * <p>This mode supports the following actions:
   *
   * <ol>
   *   <li>Canceling removes the wall being drawn and transitions to {@link BasicToolMode}.
   *   <li>Moving the mouse moves the "to" vertex of the wall being drawn.
   *   <li>Left-clicking drops the vertex as its current location and transitions to {@link
   *       BasicToolMode}. If dropped on another vertex, the two vertices will be merged. If dropped
   *       on a wall, the wall will be split and then merged at the new vertex. If the {@code SHIFT}
   *       key is held, the merging behaviour will be disabled.
   *   <li>Right-clicking is just like left-clicking, but a new wall will be started that is
   *       connected to the dropped vertex. In this case, the tool mode will remain as {@code
   *       DrawingWallToolMode}.
   * </ol>
   */
  private static final class DrawingWallToolMode extends ToolModeBase {
    private WallTopologyRig.MovableWall wall;
    private @Nullable WallTopologyRig.Element<?> connectTo;

    public DrawingWallToolMode(
        WallTopologyTool tool, WallTopologyRig rig, WallTopologyRig.MovableWall wall) {
      super(tool, rig);
      this.wall = wall;
    }

    private void findConnectToHandle(InputEvent event) {
      if (event.isShiftDown()) {
        connectTo = null;
        return;
      }

      connectTo = tool.findNearbyElement(wall.getTo().getPosition(), rig, wall.getTo());
    }

    @Override
    public boolean cancel() {
      // Revert to the original.
      rig.setWalls(tool.getZone().getWalls());
      tool.setSelectedWall(null);
      tool.changeToolMode(new BasicToolMode(tool, rig));
      return true;
    }

    @Override
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {
      wall.getTo().moveTo(snapMode.snap(point));
      findConnectToHandle(event);
    }

    @Override
    public void mouseClicked(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isRightMouseButton(event)) {
        // Lay down a vertex and keep drawing.
        var snapped = snapMode.snap(point);
        wall.getTo().moveTo(snapped);
        findConnectToHandle(event);

        var mergeCandidate = rig.getOrCreateMergeCandidate(snapped, connectTo);

        final WallTopologyRig.MovableWall newWall;
        if (mergeCandidate.isEmpty()) {
          newWall = rig.addConnectedWall(wall.getTo());
        } else {
          var newVertex = rig.mergeVertices(wall.getTo(), mergeCandidate.get());
          // It's possible we just deleted a wall by merging. If so, start a brand-new wall.
          if (newVertex.isEmpty()) {
            newWall = rig.addDegenerateWall(snapped);
          } else {
            newWall = rig.addConnectedWall(newVertex.get());
          }
        }
        wall = newWall;

        tool.setWallPropertiesFromConfigPanel(newWall.getSource());
        tool.setSelectedWall(newWall);
      }
      if (SwingUtilities.isLeftMouseButton(event)) {
        // Like a right-click, but we're done drawing.
        var snapped = snapMode.snap(point);
        wall.getTo().moveTo(snapped);
        findConnectToHandle(event);

        var mergeCandidate = rig.getOrCreateMergeCandidate(snapped, connectTo);

        final WallTopologyRig.MovableWall lastWall;
        if (mergeCandidate.isEmpty()) {
          lastWall = wall;
        } else {
          // Careful: it's possible we merged with the other vertex of `wall`, thus deleting `wall`.
          lastWall =
              rig.mergeVertices(wall.getTo(), mergeCandidate.get())
                  .flatMap(vertex -> rig.getWall(wall.getSource().from(), vertex.getSource().id()))
                  .orElse(null);
        }
        tool.setSelectedWall(lastWall);

        MapTool.serverCommand().replaceWalls(tool.getZone(), rig.commit());
        tool.changeToolMode(new BasicToolMode(tool, rig));
      }
    }

    @Override
    protected Paint getWallFill(Movable<Wall> wall) {
      if (connectTo != null && connectTo.isForSameElement(wall)) {
        return AppStyle.highlightedWallTopologyColor;
      }
      return super.getWallFill(wall);
    }

    @Override
    public Paint getHandleFill(Handle<Vertex> handle) {
      if (connectTo != null) {
        // Both the connecting handle and current handle should show as connecting, i.e., blue.
        if (wall.getTo().isForSameElement(handle) || connectTo.isForSameElement(handle)) {
          return Color.blue;
        }
      }
      return super.getHandleFill(handle);
    }
  }

  /**
   * Base class for tool modes that drag a {@link Movable} element.
   *
   * <p>These tools modes support the following actions:
   *
   * <ol>
   *   <li>Canceling restores the original position of the element and transitions to {@link
   *       BasicToolMode}.
   *   <li>Left-dragging the mouse will move the element accordingly.
   *   <li>Releasing the left mouse button will commit the element's new position and transition to
   *       {@link BasicToolMode}.
   * </ol>
   *
   * @param <T> The type of movable the tool mode supports dragging.
   */
  private abstract static class DragToolMode<T extends Movable<?>> extends ToolModeBase {
    protected final Point2D originalMousePoint;
    protected final T movable;

    protected DragToolMode(
        WallTopologyTool tool, WallTopologyRig rig, T movable, Point2D originalMousePoint) {
      super(tool, rig);
      this.movable = movable;
      this.originalMousePoint = originalMousePoint;
    }

    @Override
    public final boolean cancel() {
      // Revert to the original.
      rig.setWalls(tool.getZone().getWalls());
      tool.setSelectedWall(null);
      tool.changeToolMode(new BasicToolMode(tool, rig));
      return true;
    }

    @Override
    public void mouseReleased(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        movable.applyMove();
        beforeCommit(event);
        MapTool.serverCommand().replaceWalls(tool.getZone(), rig.commit());

        tool.changeToolMode(new BasicToolMode(tool, rig));
      }
    }

    @Override
    public boolean mouseDragged(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        movable.displace(
            point.getX() - originalMousePoint.getX(),
            point.getY() - originalMousePoint.getY(),
            snapMode);
        afterMove(event);
      }

      return false;
    }

    protected abstract void beforeCommit(InputEvent event);

    protected abstract void afterMove(InputEvent event);
  }

  /**
   * Tool mode for dragging a wall.
   *
   * <p>This mode supports the following actions:
   *
   * <ol>
   *   <li>Canceling restores the original position of the wall and transitions to {@link
   *       BasicToolMode}.
   *   <li>Left-dragging the mouse will move the wall accordingly.
   *   <li>Releasing the left mouse button will commit the wall's new position and transition to
   *       {@link BasicToolMode}.
   * </ol>
   */
  private static final class DragWallToolMode extends DragToolMode<WallTopologyRig.MovableWall> {
    public DragWallToolMode(
        WallTopologyTool tool,
        WallTopologyRig rig,
        WallTopologyRig.MovableWall wall,
        Point2D originalMousePoint) {
      super(tool, rig, wall, originalMousePoint);
    }

    @Override
    public void activate() {
      this.rig.bringToFront(this.movable);
      this.tool.setSelectedWall(this.movable);
    }

    @Override
    protected void afterMove(InputEvent event) {}

    @Override
    protected void beforeCommit(InputEvent event) {}

    @Override
    protected Paint getWallFill(Movable<Wall> wall) {
      if (wall.isForSameElement(this.movable)) {
        return AppStyle.highlightedWallTopologyColor;
      }
      return super.getWallFill(wall);
    }
  }

  /**
   * Tool mode for dragging a vertex.
   *
   * <p>This mode supports the following actions:
   *
   * <ol>
   *   <li>Canceling restores the original position of the vertex and transitions to {@link
   *       BasicToolMode}.
   *   <li>Left-dragging the mouse will vertex the wall accordingly.
   *   <li>Releasing the left mouse button will commit the vertex's new position and transition to
   *       {@link BasicToolMode}. If the vertex is dropped on another vertex, the two vertices will
   *       be merged. If dropped on a wall, the wall will be split and then merged at the new
   *       vertex. If the {@code SHIFT} key is held, the merging behaviour will be disabled.
   * </ol>
   */
  private static final class DragVertexToolMode
      extends DragToolMode<WallTopologyRig.MovableVertex> {
    private @Nullable WallTopologyRig.Element<?> connectTo;

    public DragVertexToolMode(
        WallTopologyTool tool,
        WallTopologyRig rig,
        WallTopologyRig.MovableVertex handle,
        Point2D originalMousePoint) {
      super(tool, rig, handle, originalMousePoint);
    }

    /**
     * Try to find an element to connect to.
     *
     * <p>The user can prevent connections by holding the shift key.
     *
     * @param event The input event that trigger the search for an element to connect to.
     */
    private void findConnectToHandle(InputEvent event) {
      if (event.isShiftDown()) {
        connectTo = null;
      } else {
        connectTo = tool.findNearbyElement(movable.getPosition(), rig, movable);
      }
    }

    @Override
    public void activate() {
      tool.setSelectedWall(null);
      this.rig.bringToFront(this.movable);
    }

    @Override
    protected void afterMove(InputEvent event) {
      findConnectToHandle(event);
    }

    @Override
    protected void beforeCommit(InputEvent event) {
      findConnectToHandle(event);

      // It is possible the movable was laid on top of an existing vertex or wall. If so,
      // merge those.
      rig.getOrCreateMergeCandidate(movable.getPosition(), connectTo)
          .ifPresent(
              mergeCandidate -> {
                rig.mergeVertices(movable, mergeCandidate);
              });
    }

    @Override
    protected Paint getWallFill(Movable<Wall> wall) {
      if (connectTo != null && connectTo.isForSameElement(wall)) {
        return AppStyle.highlightedWallTopologyColor;
      }
      return super.getWallFill(wall);
    }

    @Override
    public Paint getHandleFill(Handle<Vertex> handle) {
      if (connectTo != null) {
        // Both the connecting handle and current handle should show as connecting, i.e., blue.
        if (movable.isForSameElement(handle) || connectTo.isForSameElement(handle)) {
          return Color.blue;
        }
      }
      if (this.movable.isForSameElement(handle)) {
        return Color.green;
      }
      return super.getHandleFill(handle);
    }
  }
}
