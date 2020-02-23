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
package net.rptools.maptool.client.ui.zone.vbl;

import com.google.common.base.Stopwatch;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.locationtech.jts.simplify.VWSimplifier;

/**
 * A utility class that creates and returns an Area based on image pixels. A few convenience methods
 * to handle other Token VBL functions.
 *
 * @author Jamz
 */
public class TokenVBL {

  private static final Logger log = LogManager.getLogger();
  private static int sliceSize = 100;

  /**
   * TODO: Used by macro function only, update it.
   *
   * <p>A passed token will have it's image asset rendered into an Area based on pixels that have an
   * Alpha transparency level greater than or equal to the alphaSensitivity parameter.
   *
   * @param token the token
   * @param alphaSensitivity the alpha sensitivity of the VBL area
   * @return Area
   * @author Jamz
   * @since 1.6.0
   */
  public static Area createOptimizedVblArea(Token token, int alphaSensitivity) {
    final Area vblArea = createVblAreaFromToken(token, alphaSensitivity, false);
    return simplifyArea(vblArea, JTS_SimplifyMethodType.getDefault(), 10);
  }

  public static Area createVblAreaFromToken(Token token, int alphaSensitivity, boolean inverseVbl) {
    BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());

    final int w = image.getWidth();
    final int h = image.getHeight();

    //    log.info("Image w,h {},{}", w, h);

    // By slicing an image up, the Area math is greatly simplified downstream
    // Performance increase is up to 50x faster...
    if (w > sliceSize || h > sliceSize) {
      Stopwatch stopwatch = Stopwatch.createStarted();
      Area combinedArea = new Area();

      for (int i = 0; i < w; i += sliceSize) {
        int width = sliceSize;
        if (i + sliceSize > w) {
          width = w - i;
        }

        for (int j = 0; j < h; j += sliceSize) {
          int height = sliceSize;
          if (j + sliceSize > h) {
            height = h - j;
          }

          Area slice = createVblAreaSlice(i, j, width, height, image, alphaSensitivity, inverseVbl);
          combinedArea.add(slice);
        }
      }

      log.info(
          "Total time to render image to Area: {}ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

      return combinedArea;
    } else {
      Stopwatch stopwatch = Stopwatch.createStarted();
      final Area vblArea = createVblArea(image, alphaSensitivity, inverseVbl);
      log.info("Time to complete full image: {}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
      return vblArea;
    }
  }

  private static Area createVblAreaSlice(
      int x, int y, int w, int h, BufferedImage image, int alphaSensitivity, boolean inverseVbl) {

    //    log.info("x,y,w,h: {},{},{},{}", x, y, w, h);
    Stopwatch stopwatch = Stopwatch.createStarted();
    AffineTransform at = new AffineTransform();

    at.translate(x, y);
    Area slice =
        createVblArea(image.getSubimage(x, y, w, h), alphaSensitivity, inverseVbl)
            .createTransformedArea(at);

    //    log.info("Time to complete quadrant: {}", stopwatch.elapsed(TimeUnit.MILLISECONDS));

    return slice;
  }

  public static Area simplifyArea(
      Area vblArea, JTS_SimplifyMethodType simplifyMethod, double distanceTolerance) {

    if (simplifyMethod.equals(JTS_SimplifyMethodType.NONE)) {
      return vblArea;
    }

    final GeometryFactory geometryFactory = new GeometryFactory();
    ShapeReader shapeReader = new ShapeReader(geometryFactory);
    Geometry vblGeometry = null;

    if (!vblArea.isEmpty()) {
      try {
        vblGeometry = shapeReader.read(vblArea.getPathIterator(null));
        // .buffer(1); // helps creating valid geometry and prevent self-intersecting polygons
        if (!vblGeometry.isValid()) {
          log.debug(
              "vblGeometry is invalid! May cause issues. Check for self-intersecting polygons.");
        }
      } catch (Exception e) {
        log.error("There is a problem reading vblGeometry: ", e);
      }
    } else {
      return vblArea;
    }

    ShapeWriter sw = new ShapeWriter();
    Geometry simplifiedGeometry;

    switch (simplifyMethod) {
      case DOUGLAS_PEUCKER_SIMPLIFIER:
        DouglasPeuckerSimplifier dps = new DouglasPeuckerSimplifier(vblGeometry);
        dps.setDistanceTolerance(distanceTolerance);
        dps.setEnsureValid(false);
        simplifiedGeometry = dps.getResultGeometry();
        break;
      case TOPOLOGY_PRESERVING_SIMPLIFIER:
        TopologyPreservingSimplifier tss = new TopologyPreservingSimplifier(vblGeometry);
        tss.setDistanceTolerance(distanceTolerance);
        simplifiedGeometry = tss.getResultGeometry();
        break;
      case VW_SIMPLIFIER:
        VWSimplifier vws = new VWSimplifier(vblGeometry);
        vws.setDistanceTolerance(distanceTolerance);
        vws.setEnsureValid(false);
        simplifiedGeometry = vws.getResultGeometry();
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + simplifyMethod);
    }

    final Area simplifiedArea = new Area(sw.toShape(simplifiedGeometry));

    if (!simplifiedGeometry.isValid()) {
      log.debug(
          "simplifiedGeometry is invalid! May cause issues. Check for self-intersecting polygons.");
    }

    return simplifiedArea;
  }

  /**
   * This is a convenience method to send the VBL Area to be rendered to the server
   *
   * @param renderer Reference to the ZoneRenderer
   * @param area A valid Area containing VBL polygons
   * @param erase Set to true to erase the VBL, otherwise draw it
   * @return the untouched area if the renderer is null, and null otherwise
   */
  public static Area renderVBL(ZoneRenderer renderer, Area area, boolean erase) {
    if (renderer == null) {
      return area;
    }

    if (erase) {
      renderer.getZone().removeTopology(area);
      MapTool.serverCommand()
          .removeTopology(renderer.getZone().getId(), area, renderer.getZone().getTopologyMode());
    } else {
      renderer.getZone().addTopology(area);
      MapTool.serverCommand()
          .addTopology(renderer.getZone().getId(), area, renderer.getZone().getTopologyMode());
    }

    MapTool.getFrame().getCurrentZoneRenderer().getZone().tokenTopologyChanged();
    renderer.repaint();
    return null;
  }

  public static Area getMapVBL_transformed(ZoneRenderer renderer, Token token) {
    Rectangle footprintBounds = token.getBounds(renderer.getZone());
    Area newTokenVBL = new Area(footprintBounds);
    Dimension imgSize = new Dimension(token.getWidth(), token.getHeight());
    SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);
    AffineTransform atArea = new AffineTransform();

    double tx, ty, sx, sy;

    // Prepare to reverse all the current token transformations so we can store a
    // raw untransformed version on the Token
    if (token.isSnapToScale()) {
      tx =
          -newTokenVBL.getBounds().getX()
              - (int) ((footprintBounds.getWidth() - imgSize.getWidth()) / 2);
      ty =
          -newTokenVBL.getBounds().getY()
              - (int) ((footprintBounds.getHeight() - imgSize.getHeight()) / 2);
      sx = 1 / (imgSize.getWidth() / token.getWidth());
      sy = 1 / (imgSize.getHeight() / token.getHeight());

      atArea.concatenate(AffineTransform.getScaleInstance(sx, sy));
    } else {
      tx = -newTokenVBL.getBounds().getX();
      ty = -newTokenVBL.getBounds().getY();
      sx = 1 / token.getScaleX();
      sy = 1 / token.getScaleY();

      atArea.concatenate(AffineTransform.getScaleInstance(sx, sy));
    }

    if (token.getShape() == Token.TokenShape.TOP_DOWN
        && Math.toRadians(token.getFacingInDegrees()) != 0.0) {
      // Get the center of the token bounds
      double rx = newTokenVBL.getBounds2D().getCenterX();
      double ry = newTokenVBL.getBounds2D().getCenterY();

      // Rotate the area to match the token facing
      AffineTransform captureArea =
          AffineTransform.getRotateInstance(Math.toRadians(token.getFacingInDegrees()), rx, ry);
      newTokenVBL = new Area(captureArea.createTransformedShape(newTokenVBL));

      // Capture the VBL via intersection
      newTokenVBL.intersect(renderer.getZone().getTopology());

      // Rotate the area back to prep to store on Token
      captureArea =
          AffineTransform.getRotateInstance(-Math.toRadians(token.getFacingInDegrees()), rx, ry);
      newTokenVBL = new Area(captureArea.createTransformedShape(newTokenVBL));
    } else {
      // Token will not be rotated so lets just capture the VBL
      newTokenVBL.intersect(renderer.getZone().getTopology());
    }

    // Translate the capture to zero out the x,y to store on the Token
    atArea.concatenate(AffineTransform.getTranslateInstance(tx, ty));
    newTokenVBL = new Area(atArea.createTransformedShape(newTokenVBL));

    // Lets account for flipped images...
    atArea = new AffineTransform();
    if (token.isFlippedX()) {
      atArea.concatenate(AffineTransform.getScaleInstance(-1.0, 1.0));
      atArea.concatenate(AffineTransform.getTranslateInstance(-token.getWidth(), 0));
    }

    if (token.isFlippedY()) {
      atArea.concatenate(AffineTransform.getScaleInstance(1.0, -1.0));
      atArea.concatenate(AffineTransform.getTranslateInstance(0, -token.getHeight()));
    }

    // Do any final transformations for flipped images
    newTokenVBL = new Area(atArea.createTransformedShape(newTokenVBL));

    return newTokenVBL;
  }

  public static Area getVBL_underToken(ZoneRenderer renderer, Token token) {
    Rectangle footprintBounds = token.getBounds(renderer.getZone());
    Area newTokenVBL = new Area(footprintBounds);
    Dimension imgSize = new Dimension(token.getWidth(), token.getHeight());
    SwingUtil.constrainTo(imgSize, footprintBounds.width, footprintBounds.height);
    AffineTransform atArea = new AffineTransform();

    double sx, sy;

    if (token.isSnapToScale()) {
      sx = 1 / (imgSize.getWidth() / token.getWidth());
      sy = 1 / (imgSize.getHeight() / token.getHeight());

      atArea.concatenate(AffineTransform.getScaleInstance(sx, sy));
    } else {
      sx = 1 / token.getScaleX();
      sy = 1 / token.getScaleY();

      atArea.concatenate(AffineTransform.getScaleInstance(sx, sy));
    }

    if (token.getShape() == Token.TokenShape.TOP_DOWN
        && Math.toRadians(token.getFacingInDegrees()) != 0.0) {
      // Get the center of the token bounds
      double rx = newTokenVBL.getBounds2D().getCenterX();
      double ry = newTokenVBL.getBounds2D().getCenterY();

      // Rotate the area to match the token facing
      AffineTransform captureArea =
          AffineTransform.getRotateInstance(Math.toRadians(token.getFacingInDegrees()), rx, ry);
      newTokenVBL = new Area(captureArea.createTransformedShape(newTokenVBL));

      // Capture the VBL via intersection
      newTokenVBL.intersect(renderer.getZone().getTopology());
    } else {
      // Token will not be rotated so lets just capture the VBL
      newTokenVBL.intersect(renderer.getZone().getTopology());
    }

    return newTokenVBL;
  }

  /**
   * Create a VBL area from a bufferedImage and alphaSensitity. The area is created by combining
   * vblRectangle where the alpha of the image is greater or equal to the sensitivity.
   *
   * @param image the buffered image.
   * @param alphaSensitivity the alphaSensitivity.
   * @param inverseVbl
   * @return the area.
   */
  private static Area createVblArea(BufferedImage image, int alphaSensitivity, boolean inverseVbl) {
    // Assumes all colors form the VBL Area, eg everything except transparent pixels with alpha
    // >=
    // alphaSensitivity
    if (image == null) {
      return new Area();
    }

    Area vblArea = new Area();
    Rectangle vblRectangle;
    int y1, y2;
    boolean addArea = false;

    for (int x = 0; x < image.getWidth(); x++) {
      y1 = 99;
      y2 = -1;
      for (int y = 0; y < image.getHeight(); y++) {
        Color pixelColor = new Color(image.getRGB(x, y), true);
        addArea = false;

        if (!inverseVbl && pixelColor.getAlpha() >= alphaSensitivity) {
          addArea = true;
        }

        if (inverseVbl && pixelColor.getAlpha() <= alphaSensitivity) {
          addArea = true;
        }

        if (addArea) {
          if (y1 == 99) {
            y1 = y;
            y2 = y;
          }
          if (y > (y2 + 1)) {
            vblRectangle = new Rectangle(x, y1, 1, y2 - y1);
            vblArea.add(new Area(vblRectangle));
            y1 = y;
            y2 = y;
          }
          y2 = y;
        }
      }
      if ((y2 - y1) >= 0) {
        vblRectangle = new Rectangle(x, y1, 1, y2 - y1 + 1);
        vblArea.add(new Area(vblRectangle));
      }
    }

    if (vblArea.isEmpty()) {
      return new Area();
    } else {
      return vblArea;
    }
  }

  public enum JTS_SimplifyMethodType {
    DOUGLAS_PEUCKER_SIMPLIFIER("Douglas Peucker"),
    TOPOLOGY_PRESERVING_SIMPLIFIER("Topology Preserving"),
    VW_SIMPLIFIER("VW Simplifier"),
    NONE("No Optimization");

    private final String label;

    JTS_SimplifyMethodType(String label) {
      this.label = label;
    }

    public static JTS_SimplifyMethodType getDefault() {
      return DOUGLAS_PEUCKER_SIMPLIFIER;
    }

    public static JTS_SimplifyMethodType fromString(String label) {
      final JTS_SimplifyMethodType jts_simplifyMethod =
          Stream.of(JTS_SimplifyMethodType.values())
              .filter(e -> e.label.equalsIgnoreCase(label))
              .findAny()
              .orElse(DOUGLAS_PEUCKER_SIMPLIFIER);

      return jts_simplifyMethod;
    }
  }
}
