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

import com.google.protobuf.Int32Value;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.server.proto.HaloPartDto;

/** Represents the part of a {@link Halo} which has many display options. */
public final class HaloPart implements Serializable {
  private final @Nonnull DrawablePaint paint;
  private final @Nullable HaloShapeType haloShapeType; // otherwise use default
  private final @Nullable Integer width; // otherwise use default from AppPreferences
  private final @Nullable ArrayList<Float> dashedPattern; // otherwise a solid line
  private final boolean fill; // whether to fill a non-mini-shape
  private final boolean flipHorizontal; // flip over horizontally
  private final boolean flipVertical; // flip over vertically
  private final double angle; // the inner angle of an arc, pie, or chord shape in degrees
  private final double offset; //  a distance offset from the token center
  private final double rotate; // rotate the halo shape a number of degrees
  private final double scaleX; // scale in the horizontal axis
  private final double scaleY; // scale in the vertical axis
  private final int vertices; // number of points on a polygon / star shape
  private final int mini; // how many mini-shapes to revolve around the token
  private final int miniStart; // limit how many mini-shapes
  private final int miniStop; // limit how many mini-shapes
  private final double miniRotate; // rotate the halo mini-shape a number of degrees
  private final double miniSpin; // spin factor as mini shape revolves

  /** The default halo shape used in {@code HaloRenderer} and {@code HaloSyntax} */
  public static final HaloShapeType DEFAULT_HALO_SHAPE_TYPE = HaloShapeType.GRID;

  /** A list of supported halo part shape types */
  public enum HaloShapeType {
    /*
     * geometric halo shapes
     */
    CIRCLE(), // a normal circle shape
    ARC(), // an arc of a circle
    PIE(), // a pie of a circle
    CHORD(), // a chord of a circle
    TRIANGLE(), // an equilateral triangle
    SQUARE(), // a regular quadrilateral
    POLYGON(), // a regular polygon with n > 2 sides, with vertices on the circumcircle
    STAR(), // a star polygon with n > 4 points
    /*
     * derived halo shapes
     */
    FOOTPRINT(), //  derived from the token's footprint
    GRID(), // derived from the grid's shape
    TOKEN(), // derived from the token's shape
    OUTLINE(), // derived from the token's image
    MBL(), // derived from the token's MBL mask
    VBLCOVER(), // derived from the token's Cover VBL mask
    VBLHILL(), // derived from the token's Hill VBL mask
    VBLPIT(), // derived from the token's Pit VBL mask
    VBLWALL(), // derived from the token's Wall VBL mask
    ;

    public boolean isAngleBased() {
      return switch (this) {
        case ARC, CHORD, PIE -> true;
        default -> false;
      };
    }

    public boolean isPolygonal() {
      return switch (this) {
        case POLYGON, STAR -> true;
        default -> false;
      };
    }

    public boolean isGeometric() {
      return switch (this) {
        case CIRCLE, ARC, PIE, CHORD, TRIANGLE, SQUARE, POLYGON, STAR -> true;
        default -> false;
      };
    }

    public boolean isTransformable() {
      return switch (this) {
        case OUTLINE, MBL, VBLCOVER, VBLHILL, VBLPIT, VBLWALL, FOOTPRINT -> false;
        default -> true;
      };
    }
  }

  public HaloPart(
      @Nonnull DrawablePaint paint,
      @Nullable HaloShapeType haloShapeType,
      @Nullable Integer width,
      @Nullable ArrayList<Float> dashedPattern,
      boolean fill,
      boolean flipHorizontal,
      boolean flipVertical,
      double angle,
      double offset,
      double rotate,
      double scaleX,
      double scaleY,
      int vertices,
      int mini,
      int miniStart,
      int miniStop,
      double miniRotate,
      double miniSpin) {
    this.paint = paint;
    this.haloShapeType = haloShapeType;
    this.width = width;
    this.dashedPattern = dashedPattern;
    this.fill = fill;
    this.flipHorizontal = flipHorizontal;
    this.flipVertical = flipVertical;
    this.angle = (angle == 0) ? 90 : angle % 360d;
    this.offset = offset;
    this.rotate = rotate % 360d;
    this.scaleX = scaleX;
    this.scaleY = scaleY;
    this.vertices =
        haloShapeType != null && haloShapeType.isPolygonal() ? Math.max(3, vertices) : 0;
    this.mini = haloShapeType != null && haloShapeType.isGeometric() ? mini : 0;
    this.miniStart = miniStart > 0 && miniStart != mini ? Math.min(miniStart, mini) : 0;
    this.miniStop = miniStop > 0 && miniStop != mini ? Math.min(miniStop, mini) : 0;
    this.miniRotate = miniRotate % 360d;
    this.miniSpin = miniSpin;
  }

  @SuppressWarnings("ConstantConditions")
  @Serial
  private @Nonnull Object readResolve() {
    // Rather than modifying the current object, we'll create a replacement that is definitely
    // initialized properly.
    return new HaloPart(
        paint,
        haloShapeType,
        width,
        dashedPattern,
        fill,
        flipHorizontal,
        flipVertical,
        angle,
        offset,
        rotate,
        scaleX,
        scaleY,
        vertices,
        mini,
        miniStart,
        miniStop,
        miniRotate,
        miniSpin);
  }

  public @Nullable HaloShapeType getHaloShapeType() {
    return haloShapeType;
  }

  public boolean getFill() {
    return fill;
  }

  public boolean getFlipHorizontal() {
    return flipHorizontal;
  }

  public boolean getFlipVertical() {
    return flipVertical;
  }

  public double getAngle() {
    return angle;
  }

  public double getOffset() {
    return offset;
  }

  public double getRotate() {
    return rotate;
  }

  public double getScaleX() {
    return scaleX;
  }

  public double getScaleY() {
    return scaleY;
  }

  public int getVertices() {
    return vertices;
  }

  public int getMini() {
    return mini;
  }

  public int getMiniStart() {
    return miniStart;
  }

  public int getMiniStop() {
    return miniStop;
  }

  public double getMiniRotate() {
    return miniRotate;
  }

  public double getMiniSpin() {
    return miniSpin;
  }

  public @Nullable Integer getWidth() {
    return width;
  }

  public @Nonnull DrawablePaint getPaint() {
    return paint;
  }

  public @Nullable ArrayList<Float> getDashedPattern() {
    return dashedPattern;
  }

  public static @Nonnull HaloPart fromDto(@Nonnull HaloPartDto dto) {
    return new HaloPart(
        // dto.hasPaint() ? DrawablePaint.fromDto(dto.getPaint()) : null,
        Objects.requireNonNull(DrawablePaint.fromDto(dto.getPaint())),
        HaloShapeType.valueOf(dto.getHaloShapeType().name()),
        dto.hasWidth() ? dto.getWidth().getValue() : null,
        new ArrayList<>(dto.getDashedPatternList()),
        dto.getFill(),
        dto.getFlipHorizontal(),
        dto.getFlipVertical(),
        dto.getAngle(),
        dto.getOffset(),
        dto.getRotate(),
        dto.getScaleX(),
        dto.getScaleY(),
        dto.getVertices(),
        dto.getMini(),
        dto.getMiniStart(),
        dto.getMiniStop(),
        dto.getMiniRotate(),
        dto.getMiniSpin());
  }

  public @Nonnull HaloPartDto toDto() {
    var dto = HaloPartDto.newBuilder();
    dto.setPaint(paint.toDto());
    if (haloShapeType == null) {
      dto.setHaloShapeType(HaloPartDto.HaloShapeTypeDto.valueOf(DEFAULT_HALO_SHAPE_TYPE.name()));
    } else {
      dto.setHaloShapeType(HaloPartDto.HaloShapeTypeDto.valueOf(haloShapeType.name()));
    }
    if (width != null) {
      dto.setWidth(Int32Value.of(width));
    }
    if (dashedPattern != null) {
      dto.addAllDashedPattern(dashedPattern);
    }
    dto.setFill(fill);
    dto.setFlipHorizontal(flipHorizontal);
    dto.setFlipVertical(flipVertical);
    dto.setAngle(angle);
    dto.setOffset(offset);
    dto.setRotate(rotate);
    dto.setScaleX(scaleX);
    dto.setScaleY(scaleY);
    dto.setVertices(vertices);
    dto.setMini(mini);
    dto.setMiniStart(miniStart);
    dto.setMiniStop(miniStop);
    dto.setMiniRotate(miniRotate);
    dto.setMiniSpin(miniSpin);
    return dto.build();
  }
}
