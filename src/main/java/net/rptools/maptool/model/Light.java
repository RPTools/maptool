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
package net.rptools.maptool.model;

import java.awt.geom.Area;
import net.rptools.maptool.model.drawing.DrawablePaint;

public class Light {
  private DrawablePaint paint;
  private double facingOffset;
  private double radius;
  private double arcAngle;
  private ShapeType shape;
  private boolean isGM;
  private boolean ownerOnly;

  public Light() {
    // For serialization
  }

  public Light(
      ShapeType shape, double facingOffset, double radius, double arcAngle, DrawablePaint paint) {
    this.facingOffset = facingOffset;
    this.shape = shape;
    this.radius = radius;
    this.arcAngle = arcAngle;
    this.paint = paint;
    this.isGM = false;
    this.ownerOnly = false;

    if (arcAngle == 0) {
      this.arcAngle = 90;
    }
  }

  public Light(
      ShapeType shape,
      double facingOffset,
      double radius,
      double arcAngle,
      DrawablePaint paint,
      boolean isGM,
      boolean owner) {
    this.facingOffset = facingOffset;
    this.shape = shape;
    this.radius = radius;
    this.arcAngle = arcAngle;
    this.paint = paint;
    this.isGM = isGM;
    this.ownerOnly = owner;
    if (arcAngle == 0) {
      this.arcAngle = 90;
    }
  }

  public DrawablePaint getPaint() {
    return paint;
  }

  public void setPaint(DrawablePaint paint) {
    this.paint = paint;
  }

  public double getFacingOffset() {
    return facingOffset;
  }

  public void setFacingOffset(double facingOffset) {
    this.facingOffset = facingOffset;
  }

  public double getRadius() {
    return radius;
  }

  public void setRadius(double radius) {
    this.radius = radius;
  }

  public double getArcAngle() {
    return arcAngle;
  }

  public void setArcAngle(double arcAngle) {
    this.arcAngle = arcAngle;
  }

  public ShapeType getShape() {
    return shape;
  }

  public void setShape(ShapeType shape) {
    this.shape = shape;
  }

  public Area getArea(Token token, Zone zone, boolean scaleWithToken) {
    return zone.getGrid()
        .getShapedArea(
            getShape(), token, getRadius(), getArcAngle(), (int) getFacingOffset(), scaleWithToken);
  }

  public void setGM(boolean b) {
    isGM = b;
  }

  public boolean isGM() {
    return isGM;
  }

  public boolean isOwnerOnly() {
    return ownerOnly;
  }

  public void setOwnerOnly(boolean owner) {
    ownerOnly = owner;
  }
}
