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

import java.awt.BasicStroke;
import java.awt.Color;

/**
 * The color and thickness to draw a {@link Drawable}with. Also used to erase by drawing {@link
 * Drawable}s with a Pen whose {@link #setEraser}is true.
 */
public class Pen {
  public static final int MODE_SOLID = 0;
  public static final int MODE_TRANSPARENT = 1;

  public static final Pen DEFAULT = new Pen(new DrawableColorPaint(Color.black), 3.0f);

  private int foregroundMode = MODE_SOLID;
  private DrawablePaint paint;

  private int backgroundMode = MODE_SOLID;
  private DrawablePaint backgroundPaint;

  private float thickness;
  private boolean eraser;
  private boolean squareCap;
  private float opacity = 1;

  // ***** Legacy support, these supports drawables from 1.1
  private int color;
  private int backgroundColor;

  public Pen() {}

  public Pen(DrawablePaint paint, float thickness) {
    this(paint, thickness, false, true);
  }

  public Pen(DrawablePaint paint, float thickness, boolean eraser, boolean squareCap) {
    this.paint = paint;
    this.thickness = thickness;
    this.eraser = eraser;
    this.squareCap = squareCap;
  }

  public Pen(Pen copy) {
    this.paint = copy.paint;
    this.foregroundMode = copy.foregroundMode;
    this.backgroundPaint = copy.backgroundPaint;
    this.backgroundMode = copy.backgroundMode;
    this.thickness = copy.thickness;
    this.eraser = copy.eraser;
    this.squareCap = copy.squareCap;
    this.opacity = copy.opacity;
  }

  public DrawablePaint getPaint() {
    return paint;
  }

  public void setPaint(DrawablePaint paint) {
    this.paint = paint;
  }

  public DrawablePaint getBackgroundPaint() {
    return backgroundPaint;
  }

  public void setBackgroundPaint(DrawablePaint paint) {
    this.backgroundPaint = paint;
  }

  public boolean isEraser() {
    return eraser;
  }

  public void setEraser(boolean eraser) {
    this.eraser = eraser;
  }

  public float getThickness() {
    return thickness;
  }

  public void setThickness(float thickness) {
    this.thickness = thickness;
  }

  public int getBackgroundMode() {
    return backgroundMode;
  }

  public void setBackgroundMode(int backgroundMode) {
    this.backgroundMode = backgroundMode;
  }

  public int getForegroundMode() {
    return foregroundMode;
  }

  public void setForegroundMode(int foregroundMode) {
    this.foregroundMode = foregroundMode;
  }

  public float getOpacity() {
    return opacity;
  }

  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }

  public boolean getSquareCap() {
    return squareCap;
  }

  public void setSquareCap(boolean squareCap) {
    this.squareCap = squareCap;
  }

  public int getStrokeCap() {
    if (squareCap) return BasicStroke.CAP_SQUARE;
    else return BasicStroke.CAP_ROUND;
  }

  public int getStrokeJoin() {
    if (squareCap) return BasicStroke.JOIN_MITER;
    else return BasicStroke.JOIN_ROUND;
  }

  // ***** Legacy support, these supports drawables from 1.1
  // Note the lack of mutators
  public int getColor() {
    return color;
  }

  public int getBackgroundColor() {
    return backgroundColor;
  }
}
