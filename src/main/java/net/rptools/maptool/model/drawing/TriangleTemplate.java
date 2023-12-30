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
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.Comparator;
import java.util.PriorityQueue;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.*;
import net.rptools.maptool.server.proto.drawing.DrawableDto;
import net.rptools.maptool.server.proto.drawing.TriangleTemplateDto;

public class TriangleTemplate extends AbstractTemplate {
  // The definition of the cone is it is as wide as it is
  // long, so the cone angle is tan inverse of 1/2, since
  // if the length of the cone is 1, there is half it's
  // length from the midpoint to the left and right edge
  // of the base of the cone respectively...
  public static double CONE_ANGLE = Math.atan2(0.5, 1.0);

  // This is the ratio of the cone's side length to the
  // length from the point of the cone to the midpoint of the
  // base of the cone...
  public static double CONE_SIDE_LENGTH_RATIO = 1 / Math.cos(CONE_ANGLE);

  public TriangleTemplate() {
    this.showAOEOverlay = true; // While "building" it should show the overlay.
  }

  public TriangleTemplate(GUID id) {
    super(id);
    this.showAOEOverlay = false;
  }

  // The angle from the center of the cone to the horizontal.
  // This angle lets you adjust where the cone is pointed.
  private double theta = 0.0;

  public double getTheta() {
    return theta;
  }

  public void setTheta(double v) {
    theta = v;
  }

  // How much of a grid cell must be covered by the "stencil"
  // for the grid cell to be considered part of the AoE.
  private double sensitivity = 0.0;

  public double getSensitivity() {
    return sensitivity;
  }

  public void setSensitivity(double sensitivity) {
    this.sensitivity = sensitivity;
  }

  private boolean showAOEOverlay = false;

  public void calculateTheta(MouseEvent e, ZoneRenderer renderer) {
    if (getRadius() == 0) return;

    // Copy logic for off-setting to match what is in
    // ScreenPoint.convertToZone...
    // Without this, our theta calculations stops working
    // when we zoom or pan the view.
    double scale = renderer.getScale();
    double zX = e.getX();
    double zY = e.getY();

    // Translate
    zX -= renderer.getViewOffsetX();
    zY -= renderer.getViewOffsetY();

    // Scale
    zX = Math.floor(zX / scale);
    zY = Math.floor(zY / scale);

    ZonePoint sp = getVertex();
    double adjacent = zX - sp.x;
    double opposite = zY - sp.y;
    setTheta(Math.atan2(opposite, adjacent));
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden AbstractTemplate Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see AbstractTemplate#paintBorder(Graphics2D, int, int, int, int, int, int)
   */
  @Override
  protected void paintBorder(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    // NO-OP, not used. I overrode AbstractTemplate#paint in this class and I don't call paintBorder
    // and paintArea separately anymore.
  }

  @Override
  protected void paintArea(
      Graphics2D g, int x, int y, int xOff, int yOff, int gridSize, int distance) {
    // NO-OP, not used. I overrode AbstractTemplate#paint in this class and I don't call paintBorder
    // and paintArea separately anymore.
  }

  private void splitAndQueueVertically(Rectangle r, PriorityQueue<Rectangle> q, int gridSize) {
    // Midpoint is actually "distance to midpoint from x".
    // split rectangle horizontally:
    int midpoint = r.height / 2;
    int midpointSnappedToGrid = midpoint - midpoint % gridSize;
    Rectangle top = new Rectangle(r.x, r.y, r.width, midpointSnappedToGrid);
    // The bottom split starts at midpoint and has a height of the original height - midpoint
    int bottomY = r.y + midpointSnappedToGrid;
    int bottomH = r.height - midpointSnappedToGrid;
    Rectangle bottom = new Rectangle(r.x, bottomY, r.width, bottomH);

    q.add(top);
    q.add(bottom);
  }

  private void splitAndQueueHorizontally(Rectangle r, PriorityQueue<Rectangle> q, int gridSize) {
    // Midpoint is actually "distance to midpoint from x".
    // split rectangle horizontally:
    int midpoint = r.width / 2;
    int midpointSnappedToGrid = midpoint - midpoint % gridSize;
    Rectangle left = new Rectangle(r.x, r.y, midpointSnappedToGrid, r.height);
    // The right split starts at midpoint and has a width of the original width - midpoint
    int rightX = r.x + midpointSnappedToGrid;
    int rightW = r.width - midpointSnappedToGrid;
    Rectangle right = new Rectangle(rightX, r.y, rightW, r.height);

    if (r.height > gridSize) {
      splitAndQueueVertically(left, q, gridSize);
      splitAndQueueVertically(right, q, gridSize);
    } else {
      q.add(left);
      q.add(right);
    }
  }

  @Override
  protected void paint(Graphics2D g, boolean border, boolean area) {
    boolean debug = true;
    Path2D.Double path = getConePath();
    if (debug && border) {
      Rectangle boundingBox = this.getBoundingBox(path);
      Grid grid = MapTool.getCampaign().getZone(getZoneId()).getGrid();
      int gridSize = grid.getSize();
      Rectangle gridSnappedBoundingBox = this.getGridSnappedBoundingBox(boundingBox, gridSize);
      Color normal = g.getColor();
      g.setColor(new Color(0, 0, 255));
      g.draw(boundingBox);
      g.setColor(new Color(255, 0, 0));
      g.draw(gridSnappedBoundingBox);
      g.setColor(normal);
    }

    Area aoe = this.getArea();

    // Paint what is needed.
    if (area) {
      g.fill(aoe);
    }
    if (border) {
      if (this.showAOEOverlay) { // While drawing, it's helpful to see the cone overlay.
        g.draw(path);
      }
      g.draw(aoe);
    }
    // endif
  }

  /*---------------------------------------------------------------------------------------------
   * Drawable Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see Drawable#getBounds()
   */
  public Rectangle getBounds() {
    if (getZoneId() == null) {
      // This avoids a NPE when loading up a campaign
      return new Rectangle();
    }
    Zone zone = MapTool.getCampaign().getZone(getZoneId());
    if (zone == null) {
      return new Rectangle();
    }
    int gridSize = zone.getGrid().getSize();
    int quadrantSize = getRadius() * gridSize + BOUNDS_PADDING;
    ZonePoint vertex = getVertex();
    return new Rectangle(
        vertex.x - quadrantSize, vertex.y - quadrantSize, quadrantSize * 2, quadrantSize * 2);
  }

  private Path2D.Double getConePath() {
    ZonePoint sp = getVertex();

    // Only paint if the start and endpoints are not equal and the
    // radius is non-zero.
    double radius = getRadius();
    if (getRadius() == 0) return new Path2D.Double();

    Grid grid = MapTool.getCampaign().getZone(getZoneId()).getGrid();
    int gridSize = grid.getSize();

    // Calculate the position of the vertices of the cone based on
    // the angle of the cone.
    double coneSideLength = CONE_SIDE_LENGTH_RATIO * gridSize * radius;
    double coneAimAngle = getTheta();
    double vertex1X = coneSideLength * Math.cos(coneAimAngle + CONE_ANGLE) + sp.x;
    double vertex1Y = coneSideLength * Math.sin(coneAimAngle + CONE_ANGLE) + sp.y;

    double vertex2X = coneSideLength * Math.cos(coneAimAngle - CONE_ANGLE) + sp.x;
    double vertex2Y = coneSideLength * Math.sin(coneAimAngle - CONE_ANGLE) + sp.y;

    Path2D.Double path = new Path2D.Double();
    path.moveTo(sp.x, sp.y);
    path.lineTo(vertex1X, vertex1Y);
    path.lineTo(vertex2X, vertex2Y);
    path.lineTo(sp.x, sp.y);
    return path;
  }

  private Rectangle getBoundingBox(Path2D.Double path) {
    return path.getBounds();
  }

  private Rectangle getGridSnappedBoundingBox(Rectangle boundingBox, int gridSize) {
    // bounding box is not scaled to the grid.
    // We want to take the gridSize and snap the bounding box onto the grid.
    // In the top left position, we want to snap to the top left corner,
    // in the bottom right position we want to snap to the bottom right corner.
    double xLeftUpperGridScale = (double) boundingBox.x / gridSize;
    double xLeftUpperGridSnapped = Math.floor(xLeftUpperGridScale);
    double xLeftUpperZoneScale = gridSize * xLeftUpperGridSnapped;
    int xLeftUpper = (int) xLeftUpperZoneScale;

    double yLeftUpperGridScale = (double) boundingBox.y / gridSize;
    double yLeftUpperGridSnapped = Math.floor(yLeftUpperGridScale);
    double yLeftUpperZoneScale = gridSize * yLeftUpperGridSnapped;
    int yLeftUpper = (int) yLeftUpperZoneScale;

    ZonePoint leftUpper = new ZonePoint(xLeftUpper, yLeftUpper);

    float bottomRightX = boundingBox.x + boundingBox.width;
    double xRightBottomGridScale = (double) bottomRightX / gridSize;
    double xRightBottomGridSnapped = Math.ceil(xRightBottomGridScale);
    double xRightBottomZoneScale = gridSize * xRightBottomGridSnapped;
    int xRightBottom = (int) xRightBottomZoneScale;

    float bottomRightY = boundingBox.y + boundingBox.height;
    double yRightBottomGridScale = (double) bottomRightY / gridSize;
    double yRightBottomGridSnapped = Math.ceil(yRightBottomGridScale);
    double yRightBottomZoneScale = gridSize * yRightBottomGridSnapped;
    int yRightBottom = (int) yRightBottomZoneScale;

    ZonePoint bottomRight = new ZonePoint(xRightBottom, yRightBottom);
    Rectangle gridSnappedBoundingBox =
        new Rectangle(
            leftUpper.x, leftUpper.y, bottomRight.x - leftUpper.x, bottomRight.y - leftUpper.y);

    return gridSnappedBoundingBox;
  }

  @Override
  public Area getArea() {
    if (MapTool.getCampaign().getZone(getZoneId()) == null) {
      return new Area();
    }
    Grid grid = MapTool.getCampaign().getZone(getZoneId()).getGrid();
    int gridSize = grid.getSize();

    Path2D.Double path = getConePath();

    //  boundingBox is the minimal bounding box of the cone.
    //  griddedBoundingBox is the bounding box of all game squares
    // that the boundingBox intersects.
    Rectangle boundingBox = getBoundingBox(path);
    Rectangle gridSnappedBoundingBox = getGridSnappedBoundingBox(boundingBox, gridSize);
    Area cone = new Area(path);
    Area aoe = new Area(); // Empty rectangle that we will update.
    /** */
    PriorityQueue<Rectangle> queue =
        new PriorityQueue<Rectangle>(
            new Comparator<Rectangle>() {
              @Override
              /** We prioritize smaller rectangles so the Queue stays smaller. */
              public int compare(Rectangle o1, Rectangle o2) {
                return o1.height * o1.width - o2.height * o2.width;
              }
            });
    queue.add(gridSnappedBoundingBox);
    while (queue.size() > 0) {
      // if fully contained, then add it to the shape
      // if empty,
      Rectangle candidateRectForAoe = queue.poll();
      // cast to Area to allow subtracting cone.
      Area candidateAreaForAoe = new Area(candidateRectForAoe);
      candidateAreaForAoe.subtract(cone);
      if (candidateAreaForAoe.isEmpty()) {
        // If the subtraction leaves the grid square empty, then
        // the grid square is fully enclosed by the aoe!
        // Add it to the aoe.
        aoe.add(new Area(candidateRectForAoe));
      }
      // If the gridArea is not equal to the candidateRectForAoe, then
      // it means there is some amount of intersection between
      // the grid square and the aoe...
      else if (!candidateAreaForAoe.equals(new Area(candidateRectForAoe))) {
        if (candidateRectForAoe.width > gridSize) {
          splitAndQueueHorizontally(candidateRectForAoe, queue, gridSize);
        } else if (candidateRectForAoe.height > gridSize) {
          splitAndQueueVertically(candidateRectForAoe, queue, gridSize);
        } else {
          // Otherwise, it's already a gridsquare in size, so let's check
          // whether the overlapping area is enough for it to be considered
          // in the aoe!
          int totalArea = gridSize * gridSize;
          // How much of the grid square needs to be covered to be part of aoe.
          double requiredPercent = getSensitivity();
          double thresholdArea = totalArea * (100 - requiredPercent) / 100;

          // Application of the Shoelace formula, using a "flattened"
          // path iterator... You can find other examples online and this
          // code will look very familiar... but basically
          // this is a way of calculating the area under any polygon in o(n)
          // where n is the number of sides...
          PathIterator it = candidateAreaForAoe.getPathIterator(null, 0.1);
          double a = 0.0;
          double startingX = 0.0;
          double startingY = 0.0;
          double previousX = 0.0;
          double previousY = 0.0;
          double[] coords = new double[6];
          while (!it.isDone()) {
            int segmentType = it.currentSegment(coords);
            if (segmentType == PathIterator.SEG_MOVETO) {
              // set the starting coordinates..
              startingX = coords[0];
              startingY = coords[1];
            } else if (segmentType == PathIterator.SEG_LINETO) {
              a += (coords[0] - previousX) * (coords[1] + previousY) / 2.0;
            } else if (segmentType == PathIterator.SEG_CLOSE) {
              a += (startingX - previousX) * (startingY + previousY) / 2.0;
            }
            previousX = coords[0];
            previousY = coords[1];
            it.next();
          }

          // Since a is the "subtraction of aoe" from the total area, we're
          // actually deciding if the remaining area is less than the threshold
          // area... This is all a bit counter-intuitive but could pretty easily
          // be reworked.
          if (a < thresholdArea) {
            aoe.add(new Area(candidateRectForAoe));
          }
        }
      } // else there is no overlap, so there is nothing to do.
    }

    return aoe;
  }

  @Override
  public DrawableDto toDto() {
    var dto = TriangleTemplateDto.newBuilder();
    dto.setId(getId().toString())
        .setLayer(getLayer().name())
        .setZoneId(getZoneId().toString())
        .setRadius(getRadius())
        .setVertex(getVertex().toDto())
        .setTheta(getTheta())
        .setSensitivity(getSensitivity());

    if (getName() != null) dto.setName(StringValue.of(getName()));

    return DrawableDto.newBuilder().setTriangleTemplate(dto).build();
  }
}
