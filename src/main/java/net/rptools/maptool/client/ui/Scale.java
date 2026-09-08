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
package net.rptools.maptool.client.ui;

import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import net.rptools.maptool.client.ScreenPoint;

public class Scale implements Serializable {
  private static final int MIN_ZOOM_LEVEL = -175;
  private static final int MAX_ZOOM_LEVEL = 175;

  // mouse wheels tend to have 24 steps per wheel rotation.
  private static final int FINE_ZOOM_MULTIPLIER = 24;
  private static final int ULTRAFINE_ZOOM_MULTIPLIER = FINE_ZOOM_MULTIPLIER * 24;

  private final double oneToOneScale = 1; // Let this be configurable at some point
  private final double scaleIncrement = .075;

  private final double logScaleIncrement = Math.log(1 + scaleIncrement);
  private final double logFineScaleIncrement = logScaleIncrement / ULTRAFINE_ZOOM_MULTIPLIER;

  /** Calculated from {@link #scale} */
  transient int fineZoomLevel; // package private for unit testing

  double scale; // package private for unit testing
  private int offsetX;
  private int offsetY;

  public Scale(double scale, int offsetX, int offsetY) {
    var fineZoomLevel = fineZoomLevelForScale(scale);
    if (fineZoomLevel < MIN_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER) {
      fineZoomLevel = MIN_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER;
      scale = scaleForFineZoomLevel(fineZoomLevel);
    } else if (fineZoomLevel > MAX_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER) {
      fineZoomLevel = MAX_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER;
      scale = scaleForFineZoomLevel(fineZoomLevel);
    }

    this.scale = scale;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.fineZoomLevel = fineZoomLevel;
  }

  public Scale(int zoomLevel, int offsetX, int offsetY) {
    zoomLevel = Math.clamp(zoomLevel, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL);

    this.scale = scaleForFineZoomLevel(zoomLevel * ULTRAFINE_ZOOM_MULTIPLIER);
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.fineZoomLevel = zoomLevel * ULTRAFINE_ZOOM_MULTIPLIER;
  }

  public Scale() {
    this(0, 0, 0);
  }

  public Scale(Scale copy) {
    this(copy.scale, copy.offsetX, copy.offsetY);
  }

  @Serial
  private Object readResolve() {
    // scale is authoritative, provided the associated zoom level lies within appropriate bounds.
    return new Scale(clampScale(this.scale), this.offsetX, this.offsetY);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Scale other)) {
      return false;
    }

    return other.scale == this.scale
        && other.offsetX == this.offsetX
        && other.offsetY == this.offsetY
        && other.oneToOneScale == this.oneToOneScale
        && other.scaleIncrement == this.scaleIncrement;
  }

  @Override
  public int hashCode() {
    return Objects.hash(scale, offsetX, offsetY, oneToOneScale, scaleIncrement);
  }

  private int fineZoomLevelForScale(double scale) {
    return (int) Math.round(Math.log(scale / oneToOneScale) / logFineScaleIncrement);
  }

  private double scaleForFineZoomLevel(int zoomLevel) {
    return zoomLevel == 0
        ? oneToOneScale
        : oneToOneScale * Math.exp(logFineScaleIncrement * zoomLevel);
  }

  private double clampScale(double newScale) {
    var newZoomLevel = fineZoomLevelForScale(newScale);
    if (newZoomLevel <= MIN_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER) {
      return scaleForFineZoomLevel(MIN_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER);
    }
    if (newZoomLevel >= MAX_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER) {
      return scaleForFineZoomLevel(MAX_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER);
    }
    return newScale;
  }

  public int getOffsetX() {
    return offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  public double getScale() {
    return scale;
  }

  public double getOneToOneScale() {
    return oneToOneScale;
  }

  public AffineTransform toWorldTransform() {
    var transform = new AffineTransform();
    transform.scale(1 / scale, 1 / scale);
    transform.translate(-offsetX, -offsetY);
    return transform;
  }

  public AffineTransform toScreenTransform() {
    var transform = new AffineTransform();
    transform.translate(offsetX, offsetY);
    transform.scale(scale, scale);
    return transform;
  }

  public Scale withOffset(int x, int y) {
    return new Scale(scale, x, y);
  }

  public Scale withScale(double newScale, int x, int y) {
    newScale = clampScale(newScale);

    x -= offsetX;
    y -= offsetY;

    // Rounding reduces drift in offset from repeated zooming
    int newX = (int) Math.round((x * newScale) / scale);
    int newY = (int) Math.round((y * newScale) / scale);

    var newOffsetX = offsetX + x - newX;
    var newOffsetY = offsetY + y - newY;

    return new Scale(newScale, newOffsetX, newOffsetY);
  }

  public Scale withCenteredScale(double newScale, Dimension size) {
    return withScale(newScale, size.width / 2, size.height / 2);
  }

  public Scale centeredOn(int x, int y, Dimension size) {
    return withOffset(
        size.width / 2 - (int) (x * scale) - 1, size.height / 2 - (int) (y * scale) - 1);
  }

  public Scale translated(int dx, int dy) {
    return new Scale(scale, offsetX + dx, offsetY + dy);
  }

  public Scale withZoomLevel(int newZoomLevel, int x, int y) {
    newZoomLevel = Math.clamp(newZoomLevel, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL);
    var newScale =
        newZoomLevel == 0
            ? oneToOneScale
            : oneToOneScale * Math.exp(logScaleIncrement * newZoomLevel);

    return withScale(newScale, x, y);
  }

  public Scale withFineZoomLevel(int newFineZoomLevel, int x, int y) {
    newFineZoomLevel =
        Math.clamp(
            newFineZoomLevel,
            MIN_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER,
            MAX_ZOOM_LEVEL * ULTRAFINE_ZOOM_MULTIPLIER);
    var newScale =
        newFineZoomLevel == 0
            ? oneToOneScale
            : oneToOneScale * Math.exp(logFineScaleIncrement * newFineZoomLevel);

    return withScale(newScale, x, y);
  }

  public Scale withResetZoomLevel() {
    return new Scale(0, offsetX, offsetY);
  }

  public Scale withZoomReset(int x, int y) {
    return withZoomLevel(0, x, y);
  }

  public Scale zoomedIn(int x, int y) {
    return withFineZoomLevel(fineZoomLevel + ULTRAFINE_ZOOM_MULTIPLIER, x, y);
  }

  public Scale zoomedOut(int x, int y) {
    return withFineZoomLevel(fineZoomLevel - ULTRAFINE_ZOOM_MULTIPLIER, x, y);
  }

  public Scale zoomedInFine(int x, int y) {
    return withFineZoomLevel(fineZoomLevel + FINE_ZOOM_MULTIPLIER, x, y);
  }

  public Scale zoomedOutFine(int x, int y) {
    return withFineZoomLevel(fineZoomLevel - FINE_ZOOM_MULTIPLIER, x, y);
  }

  public Scale zoomedInUltrafine(int x, int y) {
    return withFineZoomLevel(fineZoomLevel + 1, x, y);
  }

  public Scale zoomedOutUltrafine(int x, int y) {
    return withFineZoomLevel(fineZoomLevel - 1, x, y);
  }

  public Point2D toWorldSpace(ScreenPoint screenPoint) {
    return new Point2D.Double((screenPoint.x - offsetX) / scale, (screenPoint.y - offsetY) / scale);
  }

  public Point2D toWorldSpace(Point2D screenPoint) {
    return toWorldSpace(screenPoint.getX(), screenPoint.getY());
  }

  public Point2D toWorldSpace(double x, double y) {
    return new Point2D.Double((x - offsetX) / scale, (y - offsetY) / scale);
  }

  /**
   * Transforms a rectangle from screen space to world space.
   *
   * @param screenRect A rectangle in screen space.
   * @return The equivalent rectangle in world space.
   */
  public Rectangle2D toWorldSpace(Rectangle2D screenRect) {
    return new Rectangle2D.Double(
        (screenRect.getMinX() - offsetX) / scale,
        (screenRect.getMinY() - offsetY) / scale,
        screenRect.getWidth() / scale,
        screenRect.getHeight() / scale);
  }

  public Area toWorldSpace(Area area) {
    return area.createTransformedArea(toWorldTransform());
  }

  /**
   * Transforms a rectangle from world space to screen space.
   *
   * @param worldRect A rectangle in world space.
   * @return The equivalent recentangle in screen space.
   */
  public Rectangle2D toScreenSpace(Rectangle2D worldRect) {
    return new Rectangle2D.Double(
        worldRect.getMinX() * scale + offsetX,
        worldRect.getMinY() * scale + offsetY,
        worldRect.getWidth() * scale,
        worldRect.getHeight() * scale);
  }

  public ScreenPoint toScreenSpace(Point2D worldSpace) {
    return toScreenSpace(worldSpace.getX(), worldSpace.getY());
  }

  public ScreenPoint toScreenSpace(double x, double y) {
    return new ScreenPoint(x * scale + offsetX, y * scale + offsetY);
  }

  public Area toScreenSpace(Area area) {
    return area.createTransformedArea(toScreenTransform());
  }
}
