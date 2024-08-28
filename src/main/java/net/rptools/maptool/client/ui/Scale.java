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

import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serial;
import java.io.Serializable;

public class Scale implements Serializable {
  public static final String PROPERTY_SCALE = "scale";
  public static final String PROPERTY_OFFSET = "offset";
  private static final int MIN_ZOOM_LEVEL = -175;
  private static final int MAX_ZOOM_LEVEL = 175;

  private final double oneToOneScale = 1; // Let this be configurable at some point
  private double scale = oneToOneScale;
  private final double scaleIncrement = .075;

  private int zoomLevel = 0;
  private int offsetX;
  private int offsetY;
  private transient PropertyChangeSupport propertyChangeSupport;

  public Scale() {
    this.offsetX = 0;
    this.offsetY = 0;
  }

  public Scale(Scale copy) {
    this.offsetX = copy.offsetX;
    this.offsetY = copy.offsetY;
    setScale(copy.scale);
  }

  @Serial
  private Object readResolve() {
    // Make sure the zoom level is correct.
    setScale(this.scale);
    return this;
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    getPropertyChangeSupport().addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    getPropertyChangeSupport().removePropertyChangeListener(listener);
  }

  public int getOffsetX() {
    return offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  public void setOffset(int x, int y) {

    int oldX = offsetX;
    int oldY = offsetY;

    offsetX = x;
    offsetY = y;

    getPropertyChangeSupport()
        .firePropertyChange(PROPERTY_OFFSET, new Point(oldX, oldY), new Point(offsetX, offsetY));
  }

  public double getScale() {
    return scale;
  }

  public void setScale(double scale) {
    if (scale <= 0.0) {
      return;
    }

    // Determine zoomLevel appropriate for given scale
    var zoomLevel =
        (int) Math.round(Math.log(scale / oneToOneScale) / Math.log(1 + scaleIncrement));
    // Check that we haven't gone out of bounds with our zooming.
    if (zoomLevel < MIN_ZOOM_LEVEL) {
      setZoomLevel(MIN_ZOOM_LEVEL);
    } else if (zoomLevel > MAX_ZOOM_LEVEL) {
      setZoomLevel(MAX_ZOOM_LEVEL);
    } else {
      // Acceptable scale. Use it.
      var oldScale = this.scale;
      this.scale = scale;
      this.zoomLevel = zoomLevel;
      getPropertyChangeSupport().firePropertyChange(PROPERTY_SCALE, oldScale, this.scale);
    }
  }

  private void setScaleFromZoomLevel() {
    double oldScale = this.scale;

    // Check for zero just to avoid any possible imprecision.
    this.scale =
        zoomLevel == 0 ? oneToOneScale : oneToOneScale * Math.pow(1 + scaleIncrement, zoomLevel);

    getPropertyChangeSupport().firePropertyChange(PROPERTY_SCALE, oldScale, scale);
  }

  public double getOneToOneScale() {
    return oneToOneScale;
  }

  public void reset() {
    setZoomLevel(0);
  }

  private void setZoomLevel(int zoomLevel) {
    this.zoomLevel = Math.clamp(zoomLevel, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL);
    setScaleFromZoomLevel();
  }

  private void scaleUp() {
    setZoomLevel(zoomLevel + 1);
  }

  private void scaleDown() {
    setZoomLevel(zoomLevel - 1);
  }

  public void zoomReset(int x, int y) {
    var oldScale = scale;
    reset();
    zoomTo(x, y, oldScale);
  }

  public void zoomIn(int x, int y) {
    double oldScale = scale;
    scaleUp();
    zoomTo(x, y, oldScale);
  }

  public void zoomOut(int x, int y) {
    double oldScale = scale;
    scaleDown();
    zoomTo(x, y, oldScale);
  }

  public void zoomScale(int x, int y, double scale) {
    double oldScale = this.scale;
    setScale(scale);
    zoomTo(x, y, oldScale);
  }

  private PropertyChangeSupport getPropertyChangeSupport() {
    if (propertyChangeSupport == null) {
      propertyChangeSupport = new PropertyChangeSupport(this);
    }
    return propertyChangeSupport;
  }

  private void zoomTo(int x, int y, double oldScale) {

    // Keep the current pixel centered
    x -= offsetX;
    y -= offsetY;

    // Rounding reduces drift in offset from repeated zooming
    int newX = (int) Math.round((x * scale) / oldScale);
    int newY = (int) Math.round((y * scale) / oldScale);

    offsetX = offsetX - (newX - x);
    offsetY = offsetY - (newY - y);
  }
}
