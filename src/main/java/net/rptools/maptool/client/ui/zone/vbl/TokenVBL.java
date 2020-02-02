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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

/**
 * A utility class that creates and returns an Area based on image pixels. A few convenience methods
 * to handle other Token VBL functions.
 *
 * @author Jamz
 */
public class TokenVBL {

  /**
   * A passed token will have it's image asset rendered into an Area based on pixels that have an
   * Alpha transparency level greater than or equal to the alphaSensitivity parameter.
   *
   * @author Jamz
   * @since 1.4.1.6
   * @param token the token
   * @param alphaSensitivity the alpha sensitivity of the VBL area
   * @return Area
   */
  public static Area createVblArea(Token token, int alphaSensitivity) {
    BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());

    // Future enhancement to create solid token VBL vs VBL with holes, further UI options...
    // int detail = 5;
    // int angle = 5;
    // return new Area(makePolyFromImage(image, detail, angle, alphaSensitivity));

    return createVblArea(image, alphaSensitivity);
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
    if (renderer == null) return area;

    if (erase) {
      renderer.getZone().removeTopology(area);
      MapTool.serverCommand().removeTopology(renderer.getZone().getId(), area);
    } else {
      renderer.getZone().addTopology(area);
      MapTool.serverCommand().addTopology(renderer.getZone().getId(), area);
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
   * @return the area.
   */
  private static Area createVblArea(BufferedImage image, int alphaSensitivity) {
    // Assumes all colors form the VBL Area, eg everything except transparent pixels with alpha
    // >=
    // alphaSensitivity
    if (image == null) return null;

    Area vblArea = new Area();
    Rectangle vblRectangle;
    int y1, y2;

    for (int x = 0; x < image.getWidth(); x++) {
      y1 = 99;
      y2 = -1;
      for (int y = 0; y < image.getHeight(); y++) {
        Color pixelColor = new Color(image.getRGB(x, y), true);
        if (pixelColor.getAlpha() >= alphaSensitivity) {
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

    if (vblArea.isEmpty()) return null;
    else return vblArea;
  }

  private static Polygon makePolyFromImage(
      BufferedImage image, int detail, int angle, int alphaSensitivity) {

    // creates an outline of a transparent image, points are stored in an array
    // arg0 - BufferedImage source image
    // arg1 - Int detail (lower = better)
    // arg2 - Int angle threshold in degrees (will remove points with angle differences below
    // this
    // level; 15 is a good value)
    // making this larger will make the body faster but less accurate;

    int w = image.getWidth(null);
    int h = image.getHeight(null);

    // increase array size from 255 if needed
    int[] vertex_x = new int[2555], vertex_y = new int[2555], vertex_k = new int[2555];

    int numPoints = 0, tx = 0, ty = 0, fy = -1, lx = 0, ly = 0;
    vertex_x[0] = 0;
    vertex_y[0] = 0;
    vertex_k[0] = 1;

    for (tx = 0; tx < w; tx += detail)
      for (ty = 0; ty < h; ty += 1) {
        // if ((image.getRGB(tx, ty) >> 24) != 0x00) {
        Color pixelColor = new Color(image.getRGB(tx, ty), true);
        if (pixelColor.getAlpha() >= alphaSensitivity) {
          vertex_x[numPoints] = tx;
          vertex_y[numPoints] = h - ty;
          vertex_k[numPoints] = 1;
          numPoints++;
          if (fy < 0) fy = ty;
          lx = tx;
          ly = ty;
          break;
        }
      }

    for (ty = 0; ty < h; ty += detail)
      for (tx = w - 1; tx >= 0; tx -= 1) {
        Color pixelColor = new Color(image.getRGB(tx, ty), true);
        // if ((image.getRGB(tx, ty) >> 24) != 0x00 && ty > ly) {
        if (pixelColor.getAlpha() >= alphaSensitivity && ty > ly) {
          vertex_x[numPoints] = tx;
          vertex_y[numPoints] = h - ty;
          vertex_k[numPoints] = 1;
          numPoints++;
          lx = tx;
          ly = ty;
          break;
        }
      }

    for (tx = w - 1; tx >= 0; tx -= detail)
      for (ty = h - 1; ty >= 0; ty -= 1) {
        Color pixelColor = new Color(image.getRGB(tx, ty), true);
        if (pixelColor.getAlpha() >= alphaSensitivity && tx < lx) {
          vertex_x[numPoints] = tx;
          vertex_y[numPoints] = h - ty;
          vertex_k[numPoints] = 1;
          numPoints++;
          lx = tx;
          ly = ty;
          break;
        }
      }

    for (ty = h - 1; ty >= 0; ty -= detail)
      for (tx = 0; tx < w; tx += 1) {
        Color pixelColor = new Color(image.getRGB(tx, ty), true);
        // if ((image.getRGB(tx, ty) >> 24) != 0x00 && ty < ly && ty > fy) {
        if (pixelColor.getAlpha() >= alphaSensitivity && ty < ly && ty > fy) {
          vertex_x[numPoints] = tx;
          vertex_y[numPoints] = h - ty;
          vertex_k[numPoints] = 1;
          numPoints++;
          lx = tx;
          ly = ty;
          break;
        }
      }

    double ang1, ang2;

    for (int i = 0; i < numPoints - 2; i++) {
      ang1 = PointDirection(vertex_x[i], vertex_y[i], vertex_x[i + 1], vertex_y[i + 1]);
      ang2 = PointDirection(vertex_x[i + 1], vertex_y[i + 1], vertex_x[i + 2], vertex_y[i + 2]);
      if (Math.abs(ang1 - ang2) <= angle) vertex_k[i + 1] = 0;
    }

    ang1 =
        PointDirection(
            vertex_x[numPoints - 2],
            vertex_y[numPoints - 2],
            vertex_x[numPoints - 1],
            vertex_y[numPoints - 1]);
    ang2 =
        PointDirection(vertex_x[numPoints - 1], vertex_y[numPoints - 1], vertex_x[0], vertex_y[0]);

    if (Math.abs(ang1 - ang2) <= angle) vertex_k[numPoints - 1] = 0;

    ang1 =
        PointDirection(vertex_x[numPoints - 1], vertex_y[numPoints - 1], vertex_x[0], vertex_y[0]);
    ang2 = PointDirection(vertex_x[0], vertex_y[0], vertex_x[1], vertex_y[1]);

    if (Math.abs(ang1 - ang2) <= angle) vertex_k[0] = 0;

    int n = 0;
    for (int i = 0; i < numPoints; i++) if (vertex_k[i] == 1) n++;

    Polygon poly = new Polygon();

    for (int i = 0; i < numPoints; i++)
      if (vertex_k[i] == 1) {
        poly.addPoint(vertex_x[i], h - vertex_y[i]);
        n++;
      }

    return poly;
  }

  private static double PointDirection(double xfrom, double yfrom, double xto, double yto) {
    return Math.atan2(yto - yfrom, xto - xfrom) * 180 / Math.PI;
  }
}
