package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;

/**
 * A utility class that creates and returns and Area based on image pixels
 * 
 * @author Jamz
 *
 */
public class TokenVBL {

	/**
	 * A passed token will have it's image asset rendered into an Area
	 * based on pixels that have an Alpha transparency level greater than
	 * or equal to the alphaSensitivity parameter.
	 * 
	 * @author Jamz
	 * @since 1.4.1.6
	 * 
	 * @param token
	 * @param alphaSensitivity
	 * @return Area
	 */
	public static Area createVblArea(Token token, int alphaSensitivity) {
		BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());

		// Future enhancement to create solid token VBL vs VBL with holes, further UI options...
		// Area area = new Area();
		// Polygon poly = makePolyFromImage(image, 6, 15, alphaSensitivity);
		// area = new Area(poly);

		return createVblArea(image, alphaSensitivity);
	}

	private static Area createVblArea(BufferedImage image, int alphaSensitivity) {
		//Assumes all colors form the VBL Area, eg everything except transparent pixels with alpha >= alphaSensitivity
		if (image == null)
			return null;

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
				vblRectangle = new Rectangle(x, y1, 1, y2 - y1);
				vblArea.add(new Area(vblRectangle));
			}
		}

		if (vblArea.isEmpty())
			return null;
		else
			return vblArea;
	}

	private static Polygon makePolyFromImage(BufferedImage image, int detail, int angle, int alphaSensitivity) {

		//creates an outline of a transparent image, points are stored in an array
		//arg0 - BufferedImage source image 
		//arg1 - Int detail (lower = better)
		//arg2 - Int angle threshold in degrees (will remove points with angle differences below this level; 15 is a good value)
		//making this larger will make the body faster but less accurate;

		int w = image.getWidth(null);
		int h = image.getHeight(null);

		// increase array size from 255 if needed
		int[] vertex_x = new int[255], vertex_y = new int[255], vertex_k = new int[255];

		int numPoints = 0, tx = 0, ty = 0, fy = -1, lx = 0, ly = 0;
		vertex_x[0] = 0;
		vertex_y[0] = 0;
		vertex_k[0] = 1;

		for (tx = 0; tx < w; tx += detail)
			for (ty = 0; ty < h; ty += 1)
				if ((image.getRGB(tx, ty) >> 24) != 0x00) {
					vertex_x[numPoints] = tx;
					vertex_y[numPoints] = h - ty;
					vertex_k[numPoints] = 1;
					numPoints++;
					if (fy < 0)
						fy = ty;
					lx = tx;
					ly = ty;
					break;
				}

		for (ty = 0; ty < h; ty += detail)
			for (tx = w - 1; tx >= 0; tx -= 1)
				if ((image.getRGB(tx, ty) >> 24) != 0x00 && ty > ly) {
					vertex_x[numPoints] = tx;
					vertex_y[numPoints] = h - ty;
					vertex_k[numPoints] = 1;
					numPoints++;
					lx = tx;
					ly = ty;
					break;
				}

		for (tx = w - 1; tx >= 0; tx -= detail)
			for (ty = h - 1; ty >= 0; ty -= 1)
				if ((image.getRGB(tx, ty) >> 24) != 0x00 && tx < lx) {
					vertex_x[numPoints] = tx;
					vertex_y[numPoints] = h - ty;
					vertex_k[numPoints] = 1;
					numPoints++;
					lx = tx;
					ly = ty;
					break;
				}

		for (ty = h - 1; ty >= 0; ty -= detail)
			for (tx = 0; tx < w; tx += 1)
				if ((image.getRGB(tx, ty) >> 24) != 0x00 && ty < ly && ty > fy) {
					vertex_x[numPoints] = tx;
					vertex_y[numPoints] = h - ty;
					vertex_k[numPoints] = 1;
					numPoints++;
					lx = tx;
					ly = ty;
					break;
				}

		double ang1, ang2;

		for (int i = 0; i < numPoints - 2; i++) {
			ang1 = PointDirection(vertex_x[i], vertex_y[i], vertex_x[i + 1], vertex_y[i + 1]);
			ang2 = PointDirection(vertex_x[i + 1], vertex_y[i + 1], vertex_x[i + 2], vertex_y[i + 2]);
			if (Math.abs(ang1 - ang2) <= angle)
				vertex_k[i + 1] = 0;
		}

		ang1 = PointDirection(vertex_x[numPoints - 2], vertex_y[numPoints - 2], vertex_x[numPoints - 1], vertex_y[numPoints - 1]);
		ang2 = PointDirection(vertex_x[numPoints - 1], vertex_y[numPoints - 1], vertex_x[0], vertex_y[0]);

		if (Math.abs(ang1 - ang2) <= angle)
			vertex_k[numPoints - 1] = 0;

		ang1 = PointDirection(vertex_x[numPoints - 1], vertex_y[numPoints - 1], vertex_x[0], vertex_y[0]);
		ang2 = PointDirection(vertex_x[0], vertex_y[0], vertex_x[1], vertex_y[1]);

		if (Math.abs(ang1 - ang2) <= angle)
			vertex_k[0] = 0;

		int n = 0;
		for (int i = 0; i < numPoints; i++)
			if (vertex_k[i] == 1)
				n++;

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