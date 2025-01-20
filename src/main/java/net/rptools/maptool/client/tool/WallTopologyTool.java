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
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nullable;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.swing.SwingUtil;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.math.Vector2D;

public class WallTopologyTool extends DefaultTool implements ZoneOverlay {
  private static final Logger log = LogManager.getLogger(WallTopologyTool.class);
  private Point2D currentPosition =
      new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

  /** The current tool behaviour. Each operation enters a distinct mode so we don't cross-talk. */
  private ToolMode mode = new NilToolMode();

  private final TopologyTool.MaskOverlay maskOverlay = new TopologyTool.MaskOverlay();

  private double getHandleRadius() {
    var radius = 4.;

    var scale = renderer.getScale();
    if (scale < 1) {
      radius /= scale;
    }

    return radius;
  }

  private double getWallHalfWidth() {
    var width = 1.5;

    var scale = renderer.getScale();
    if (scale < 1) {
      width /= scale;
    }

    return width;
  }

  private double getHandleSelectDistance() {
    // Include a bit of leniency for the user.
    return getHandleRadius() * 1.125;
  }

  private double getWallSelectDistance() {
    return getWallHalfWidth() * 1.5;
  }

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
  protected void attachTo(ZoneRenderer renderer) {
    super.attachTo(renderer);
    currentPosition = new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    var rig = new WallTopologyRig(this::getHandleSelectDistance, this::getWallSelectDistance);
    rig.setWalls(getZone().getWalls());
    changeToolMode(new BasicToolMode(this, rig));

    MapTool.getFrame()
        .showControlPanel(
            MapTool.getFrame().getWallConfigurationController().getView().getRootComponent());

    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Override
  protected void detachFrom(ZoneRenderer renderer) {
    new MapToolEventBus().getMainEventBus().unregister(this);

    MapTool.getFrame().getWallConfigurationController().unbind();
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
    if (mode.shouldAllowMapDrag(e)) {
      super.mouseDragged(e);
    }
    mode.mouseMoved(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  @Override
  public void mousePressed(MouseEvent e) {
    super.mousePressed(e);
    mode.mousePressed(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    super.mouseReleased(e);
    mode.mouseReleased(updateCurrentPosition(e), getSnapMode(e), e);
    renderer.repaint();
  }

  private void setWallPropertiesFromConfigPanel(Wall wall) {
    var current = MapTool.getFrame().getWallConfigurationController().getModel();
    wall.copyDataFrom(current);
  }

  private void setSelectedWall(@Nullable Wall wall) {
    var configPanel = MapTool.getFrame().getWallConfigurationController();
    if (wall == null) {
      configPanel.unbind();
    } else {
      configPanel.bind(getZone(), wall);
    }
  }

  private boolean isSelectedWall(Wall wall) {
    var selected = getSelectedWall();
    return wall.equals(selected);
  }

  private Wall getSelectedWall() {
    return MapTool.getFrame().getWallConfigurationController().getModel();
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

  @Subscribe
  private void onTopologyChanged(WallTopologyChanged event) {
    var selected = getSelectedWall();

    var rig = new WallTopologyRig(this::getHandleSelectDistance, this::getWallSelectDistance);
    rig.setWalls(getZone().getWalls());
    changeToolMode(new BasicToolMode(this, rig));

    // If the selected wall still exists, rebind it.
    var optionalWall = getZone().getWalls().getWall(selected.from(), selected.to());
    optionalWall.ifPresentOrElse(wall -> setSelectedWall(wall), () -> setSelectedWall(null));
  }

  private interface ToolMode {
    void activate();

    void deactivate();

    /**
     * Cancels the current tool mode.
     *
     * @return {@code true} if the tool mode has its own cancel behaviour; {@code false} if the
     *     regular behaviour (revert to pointer tool) should apply.
     */
    boolean cancel();

    boolean shouldAllowMapDrag(MouseEvent e);

    void mouseMoved(Point2D point, Snap snapMode, MouseEvent event);

    void mousePressed(Point2D point, Snap snapMode, MouseEvent event);

    void mouseReleased(Point2D point, Snap snapMode, MouseEvent event);

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
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mouseReleased(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public boolean shouldAllowMapDrag(MouseEvent e) {
      return true;
    }

    @Override
    public void paint(Graphics2D g2) {}
  }

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
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public void mouseReleased(Point2D point, Snap snapMode, MouseEvent event) {}

    @Override
    public boolean shouldAllowMapDrag(MouseEvent e) {
      return true;
    }

    protected Paint getWallStrokePaint(WallTopologyRig.MovableWall wall) {
      if (tool.isSelectedWall(wall.getSource())) {
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

      //      result.moveTo(-halfHeight, -halfHeight);
      //      result.lineTo(0, 0);
      //      result.lineTo(-halfHeight, halfHeight);
      //      result.lineTo(-0.75 * halfHeight, 0);
      //      result.closePath();
      return result;
    }
  }

  private static final class BasicToolMode extends ToolModeBase {
    // The hovered handle. This is the candidate for any pending mouse event. E.g., a mouse pressed
    // can start a drag operation on it.
    private @Nullable WallTopologyRig.Element<?> currentElement;
    private @Nullable Point2D potentialSplitPoint;

    public BasicToolMode(WallTopologyTool tool, WallTopologyRig rig) {
      super(tool, rig);
    }

    @Override
    public void activate() {
      currentElement = rig.getNearbyElement(tool.getCurrentPosition()).orElse(null);
    }

    @Override
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {
      currentElement = rig.getNearbyElement(point).orElse(null);

      if (event.isAltDown() && currentElement instanceof WallTopologyRig.MovableWall movableWall) {
        potentialSplitPoint = rig.getSplitPoint(movableWall, point);
      } else {
        potentialSplitPoint = null;
      }
    }

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        if (currentElement == null) {
          // Grabbed blank space. Start a new wall, unless the delete modifier is held.
          if (!SwingUtil.isShiftDown(event)) {
            var newWall = rig.addDegenerateWall(snapMode.snap(point));
            // TODO Why do we pass the snapped position rather than the original mouse point?
            tool.setWallPropertiesFromConfigPanel(newWall.getSource());
            tool.setSelectedWall(newWall.getSource());
            tool.changeToolMode(
                new DragVertexToolMode(
                    tool, rig, newWall.getTo(), newWall.getTo().getPosition(), true));
          }
        } else if (SwingUtil.isShiftDown(event)) {
          currentElement.delete();
          MapTool.serverCommand().replaceWalls(tool.getZone(), rig.commit());
          currentElement = rig.getNearbyElement(tool.getCurrentPosition()).orElse(null);
        } else if (event.isAltDown()) {
          // Start drawing a new wall using the current handle or wall.
          var fromVertex =
              switch (currentElement) {
                case WallTopologyRig.MovableVertex movableVertex -> movableVertex;
                case WallTopologyRig.MovableWall movableWall -> rig.splitAt(movableWall, point);
              };
          var newWall = rig.addConnectedWall(fromVertex.getSource(), fromVertex.getPosition());
          tool.setWallPropertiesFromConfigPanel(newWall.getSource());
          tool.setSelectedWall(newWall.getSource());
          tool.changeToolMode(new DragVertexToolMode(tool, rig, newWall.getTo(), point, false));
        } else {
          // No special modifiers. Grab the handle, i.e., start a drag.
          switch (currentElement) {
            case WallTopologyRig.MovableVertex movableVertex -> {
              tool.setSelectedWall(null);
              tool.changeToolMode(new DragVertexToolMode(tool, rig, movableVertex, point, false));
            }
            case WallTopologyRig.MovableWall movableWall ->
                tool.changeToolMode(new DragWallToolMode(tool, rig, movableWall, point));
          }
        }
      }
    }

    @Override
    public Paint getHandleFill(Handle<Vertex> handle) {
      // TODO Blue if alt is down.
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

    @Override
    public void paint(Graphics2D g2) {
      super.paint(g2);

      if (potentialSplitPoint != null) {
        paintHandle(g2, potentialSplitPoint, Color.blue);
      }
    }
  }

  private abstract static class DragToolMode<T extends Movable<?>> extends ToolModeBase {
    // Flag used to avoid adding degenerate walls if the user randomly clicks nowhere.
    protected boolean nonTrivialChange;
    protected final Point2D originalMousePoint;
    protected T movable;

    protected DragToolMode(
        WallTopologyTool tool, WallTopologyRig rig, T movable, Point2D originalMousePoint) {
      super(tool, rig);
      this.nonTrivialChange = false;
      this.movable = movable;
      this.originalMousePoint = originalMousePoint;
    }

    protected void setCurrentHandle(T newHandle, Point2D mousePoint) {
      this.originalMousePoint.setLocation(mousePoint);
      this.movable = newHandle;
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
        complete();
      }
    }

    @Override
    public void mouseMoved(Point2D point, Snap snapMode, MouseEvent event) {
      nonTrivialChange = true;
      movable.displace(
          point.getX() - originalMousePoint.getX(),
          point.getY() - originalMousePoint.getY(),
          snapMode);
      afterMove(event);
    }

    @Override
    public Paint getHandleFill(Handle<Vertex> handle) {
      if (this.movable.isForSameElement(handle)) {
        return Color.green;
      }
      return super.getHandleFill(handle);
    }

    protected final void complete() {
      if (nonTrivialChange) {
        movable.applyMove();
        beforeCommit();
        MapTool.serverCommand().replaceWalls(tool.getZone(), rig.commit());
      } else {
        // Revert so no new pointless walls are added to the zone.
        rig.setWalls(tool.getZone().getWalls());
      }

      tool.changeToolMode(new BasicToolMode(tool, rig));
    }

    protected abstract void beforeCommit();

    protected abstract void afterMove(MouseEvent event);
  }

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
      var wall = this.movable.getSource();
      this.rig.bringToFront(wall);
      this.rig.bringToFront(wall);
      tool.setSelectedWall(wall);
    }

    @Override
    public boolean shouldAllowMapDrag(MouseEvent e) {
      return true;
    }

    @Override
    protected void afterMove(MouseEvent event) {
      // Nothing to do.
    }

    @Override
    protected void beforeCommit() {}

    @Override
    protected Paint getWallFill(Movable<Wall> wall) {
      if (wall.isForSameElement(this.movable)) {
        return AppStyle.highlightedWallTopologyColor;
      }
      return super.getWallFill(wall);
    }
  }

  private static final class DragVertexToolMode
      extends DragToolMode<WallTopologyRig.MovableVertex> {
    private @Nullable WallTopologyRig.Element<?> connectTo;

    /**
     * The constant displacement from the mouse to the vertex. Will remain constant even as we add
     * vertices or toggle snap-to-grid on or off.
     */
    private final double offsetX, offsetY;

    public DragVertexToolMode(
        WallTopologyTool tool,
        WallTopologyRig rig,
        WallTopologyRig.MovableVertex handle,
        Point2D originalMousePoint,
        boolean cancelIfTrivial) {
      super(tool, rig, handle, originalMousePoint);

      this.offsetX = handle.getPosition().getX() - originalMousePoint.getX();
      this.offsetY = handle.getPosition().getY() - originalMousePoint.getY();

      this.nonTrivialChange = !cancelIfTrivial;
    }

    @Override
    public void activate() {
      this.rig.bringToFront(this.movable.getSource());
    }

    private void findConnectToHandle(MouseEvent event) {
      connectTo = null;

      if (event.isAltDown()) {
        // Add some leniency so the snapping feels good.
        var extraSpace = 4.f;
        connectTo =
            rig.getNearbyElement(
                    tool.getCurrentPosition(),
                    extraSpace,
                    (WallTopologyRig.Element<?> other) -> {
                      switch (other) {
                        case WallTopologyRig.MovableVertex movableVertex -> {
                          return !movable.isForSameElement(movableVertex);
                        }
                        case WallTopologyRig.MovableWall movableWall -> {
                          return !movable.isForSameElement(movableWall.getFrom())
                              && !movable.isForSameElement(movableWall.getTo());
                        }
                      }
                    })
                .orElse(null);

        switch (connectTo) {
          case null -> {
            /* Nothing to do */
          }
          case WallTopologyRig.MovableVertex movableVertex -> {
            // Snap the handle to the vertex it would connect to.
            movable.moveTo(movableVertex.getPosition());
          }
          case WallTopologyRig.MovableWall movableWall -> {
            // Snap the handle to where we would split the wall.
            movable.moveTo(rig.getSplitPoint(movableWall, tool.getCurrentPosition()));
          }
        }
      }
    }

    @Override
    public boolean shouldAllowMapDrag(MouseEvent e) {
      // Map drag conflicts with our extend action.
      return false;
    }

    @Override
    public void mousePressed(Point2D point, Snap snapMode, MouseEvent event) {
      if (SwingUtilities.isRightMouseButton(event)) {
        nonTrivialChange = true;
        var snapped = snapMode.snap(point);
        tryMerge();

        var newWall = this.rig.addConnectedWall(movable.getSource(), snapped);
        var newHandle = newWall.getTo();

        // Maintain the original offset regardless of what the actual cursor position is now.
        var fakeMousePosition =
            new Point2D.Double(
                newHandle.getPosition().getX() - offsetX, newHandle.getPosition().getY() - offsetY);

        tool.setWallPropertiesFromConfigPanel(newWall.getSource());
        tool.setSelectedWall(newWall.getSource());
        setCurrentHandle(newHandle, fakeMousePosition);
        findConnectToHandle(event);
      }
    }

    @Override
    protected void afterMove(MouseEvent event) {
      findConnectToHandle(event);
    }

    @Override
    protected void beforeCommit() {
      tryMerge();
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
      return super.getHandleFill(handle);
    }

    private void tryMerge() {
      movable.applyMove();

      switch (connectTo) {
        case null -> {
          /* Do nothing */
        }
        case WallTopologyRig.MovableVertex movableVertex -> {
          var newHandle = rig.mergeVertices(movableVertex, movable);
          // Current handle's vertex may have just been eliminated. Use the returned one instead.
          this.setCurrentHandle(newHandle, newHandle.getPosition());
        }
        case WallTopologyRig.MovableWall movableWall -> {
          // Split the wall, then merge with the new vertex.
          var splitVertex = rig.splitAt(movableWall, movable.getPosition());
          var newVertex = rig.mergeVertices(splitVertex, movable);
          // Vertex may have just been eliminated. Use the returned one instead.
          this.setCurrentHandle(newVertex, newVertex.getPosition());
        }
      }
    }
  }
}
