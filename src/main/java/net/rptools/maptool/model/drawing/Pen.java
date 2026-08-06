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
import java.io.Serial;
import java.io.Serializable;
import javax.annotation.Nullable;
import net.rptools.maptool.server.proto.drawing.PenDto;

/**
 * The color and thickness to draw a {@link Drawable} with. Also used to erase by drawing {@link
 * Drawable}s with a Pen whose {@link #isEraser()} is true.
 */
public class Pen implements Serializable {
  private static final int MODE_SOLID = 0;
  private static final int MODE_TRANSPARENT = 1;

  private @Nullable DrawablePaint paint;
  private @Nullable DrawablePaint backgroundPaint;

  private float thickness;
  private boolean eraser;
  private boolean squareCap;
  private float opacity = 1;

  // ***** Legacy support, these supports drawables from 1.1
  @Deprecated private int foregroundMode = MODE_SOLID;
  @Deprecated private int backgroundMode = MODE_SOLID;
  @Deprecated private int color;
  @Deprecated private int backgroundColor;

  /** "Primary" pen constructor that sets all fields */
  public Pen(
      @Nullable DrawablePaint paint,
      @Nullable DrawablePaint backgroundPaint,
      float thickness,
      boolean eraser,
      boolean squareCap,
      float opacity) {
    this.paint = paint;
    this.backgroundPaint = backgroundPaint;
    this.thickness = thickness;
    this.eraser = eraser;
    this.squareCap = squareCap;
    this.opacity = opacity;
  }

  public Pen() {
    this(
        new DrawableColorPaint(Color.black),
        new DrawableColorPaint(Color.black),
        3.0f,
        false,
        true,
        1.f);
  }

  public Pen(Pen copy) {
    this(
        copy.paint,
        copy.backgroundPaint,
        copy.thickness,
        copy.eraser,
        copy.squareCap,
        copy.opacity);
  }

  @Serial
  private Object readResolve() {
    // Legacy paints had used the `int` color fields along with the modes to determine what to draw,
    // and did not have the `DrawablePaint` fields set.
    // Modern paints will always have the mode set to MODE_TRANSPARENT when the paint is null.
    boolean isLegacyPaint = false;
    if (foregroundMode == MODE_SOLID && paint == null) {
      paint = new DrawableColorPaint(color);
      isLegacyPaint = true;
    }
    if (backgroundMode == MODE_SOLID && backgroundPaint == null) {
      backgroundPaint = new DrawableColorPaint(backgroundColor);
      isLegacyPaint = true;
    }
    if (isLegacyPaint && opacity <= 0) {
      opacity = 1;
    }

    return this;
  }

  @Serial
  private Object writeReplace() {
    // Set the legacy fields for best compatibility when downgrading campaigns, and to make sure
    // that `readResolve()` can properly interpret the saved pen.

    this.foregroundMode = paint == null ? MODE_TRANSPARENT : MODE_SOLID;
    this.backgroundMode = backgroundPaint == null ? MODE_TRANSPARENT : MODE_SOLID;

    this.color = paint instanceof DrawableColorPaint dcp ? dcp.getColor() : 0;
    this.backgroundColor = backgroundPaint instanceof DrawableColorPaint dcp ? dcp.getColor() : 0;

    return this;
  }

  public BasicStroke getStroke() {
    var cap = squareCap ? BasicStroke.CAP_SQUARE : BasicStroke.CAP_ROUND;
    var join = squareCap ? BasicStroke.JOIN_MITER : BasicStroke.JOIN_ROUND;
    return new BasicStroke(thickness, cap, join);
  }

  public @Nullable DrawablePaint getPaint() {
    return paint;
  }

  public void setPaint(@Nullable DrawablePaint paint) {
    this.paint = paint;
  }

  public @Nullable DrawablePaint getBackgroundPaint() {
    return backgroundPaint;
  }

  public void setBackgroundPaint(@Nullable DrawablePaint paint) {
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

  public static Pen fromDto(PenDto dto) {
    return new Pen(
        dto.hasForegroundPaint() ? DrawablePaint.fromDto(dto.getForegroundPaint()) : null,
        dto.hasBackgroundPaint() ? DrawablePaint.fromDto(dto.getBackgroundPaint()) : null,
        dto.getThickness(),
        dto.getEraser(),
        dto.getSquareCap(),
        dto.getOpacity());
  }

  public PenDto toDto() {
    var dto =
        PenDto.newBuilder()
            .setEraser(eraser)
            .setThickness(thickness)
            .setOpacity(opacity)
            .setSquareCap(squareCap);
    if (paint != null) {
      dto.setForegroundPaint(paint.toDto());
    }
    if (backgroundPaint != null) {
      dto.setBackgroundPaint(backgroundPaint.toDto());
    }
    return dto.build();
  }
}
