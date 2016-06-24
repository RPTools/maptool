package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.Color;
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
}