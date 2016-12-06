/*
 * The MIT License
 * 
 * Copyright (c) 2005 David Rice, Trevor Croft
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.rptools.tokentool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public class TokenCompositor {

	public static BufferedImage composeToken(
			BufferedImage overlayImage,
			BufferedImage tokenImage,
			int offsetX,
			int offsetY,
			int overlayWidth,
			int overlayHeight,
			double scale,
			CompositionProperties props) {

		if (overlayImage == null) {
			throw new IllegalArgumentException("Must have both an overlay and a token");
		}

		//
		// CALCULATE TOKEN DIMENSIONS AND PLACE THEM IN width,height
		//

		// it's handy to have this precalculated.

		int scaledTokenImageWidth = (int) (tokenImage.getWidth() * scale);
		int scaledTokenImageHeight = (int) (tokenImage.getHeight() * scale);

		// assume we are composing a token with an nonbase overlay, 
		// if we are, the height and width of the token 
		// are very simple -- it is just the height and width of the overlay.
		int width = overlayWidth;
		int height = overlayHeight;

		// however, if we are composing a token with a base, we will override the default
		// values.  and we must be concerne with exactly how the token image and overlay 
		// overlap.

		if (props.isBase()) {
			// if the base extends beyond the sides of the image, we need to extend the width.
			// at the minimum, the image will be as big as the scaled token image.

			width = scaledTokenImageWidth;
			height = scaledTokenImageHeight;

			if (offsetX < 0) {
				// the base is to the left of the left image edge.  Add offsetX to the width.
				width -= offsetX;
			}
			if (offsetY < 0) {
				// the base is above the top image edge.  Add offsetY to the height
				height -= offsetY;
			}

			// calculate how many pixels of the base extend beyond the right and bottom
			// sides of the token image.

			int rightOverhang = overlayWidth + offsetX - scaledTokenImageWidth;
			int bottomOverhang = overlayHeight + offsetY - scaledTokenImageHeight;

			if (rightOverhang > 0) {
				width += rightOverhang;
			}
			if (bottomOverhang > 0) {
				height += bottomOverhang;
			}

		}

		BufferedImage composedImage = new BufferedImage(width, height, Transparency.TRANSLUCENT);

		Graphics2D g = composedImage.createGraphics();
		if (props.isSolidBackground()) {
			g.setColor(props.getBackgroundColor());
			g.fillRect(0, 0, width, height);
		}
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// if the isBase checkbox is selected, we draw the overlay first
		// so that it appears underneath the token art.  The ternaries in the x,y
		// coord parameters keep the base and the token properly oriented.

		if (props.isBase()) {
			g.drawImage(overlayImage,
					(offsetX < 0) ? 0 : offsetX,
					(offsetY < 0) ? 0 : offsetY,
					overlayWidth,
					overlayHeight,
					null);
			g.drawImage(tokenImage,
					(offsetX > 0) ? 0 : -offsetX,
					(offsetY > 0) ? 0 : -offsetY,
					scaledTokenImageWidth,
					scaledTokenImageHeight,
					null);
		}

		// if the isBase checkbox is not selected, we draw 
		// the overlay first so that it appears *above* the token art.

		else {
			if (tokenImage != null) {
				//            	if (props.isSmooth()) {
				//                	Image scaledImage = tokenImage.getScaledInstance((int)(tokenImage.getWidth()*scale), (int)(tokenImage.getHeight()*scale), Image.SCALE_SMOOTH);
				//                	g.drawImage(scaledImage, -offsetX, -offsetY, null);
				//            	} else {
				g.drawImage(tokenImage, -offsetX, -offsetY, scaledTokenImageWidth, scaledTokenImageHeight, null);
				//            	}
			}
			g.drawImage(translateOverlay(overlayImage, props.getFudgeFactor()), 0, 0, width, height, null);
		}

		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
		g.dispose();

		if (props.isBase()) {
			// Tokens with bases are unmasked.
			return composedImage;
		} else {
			// Tokens with overlays are masked.
			BufferedImage scaledMask = new BufferedImage(width, height, Transparency.OPAQUE);
			g = scaledMask.createGraphics();
			g.drawImage(getMagentaMask(overlayImage, props.getFudgeFactor()), 0, 0, width, height, null);
			return applyAlpha(applyMask(composedImage, scaledMask), props.getTranslucency());
		}
	}

	public static BufferedImage applyMask(BufferedImage tokenImage, BufferedImage mask) {

		for (int y = 0; y < mask.getHeight(); y++) {

			for (int x = 0; x < mask.getWidth(); x++) {

				tokenImage.setRGB(x, y, calculateColor(tokenImage.getRGB(x, y), mask.getRGB(x, y)));
			}
		}

		return tokenImage;
	}

	public static BufferedImage applyAlpha(BufferedImage image, double alpha) {

		for (int y = 0; y < image.getHeight(); y++) {

			for (int x = 0; x < image.getWidth(); x++) {

				int color = image.getRGB(x, y);

				// Apply requested alpha
				int alphaChannel = (color & 0xff000000) >>> 24;
				alphaChannel = (int) (alpha * alphaChannel);
				alphaChannel = alphaChannel << 24;

				color &= 0xffffff; // clear old alpha
				color = alphaChannel | color; // install new alpha

				image.setRGB(x, y, color);
			}
		}

		return image;
	}

	public static BufferedImage getMagentaMask(BufferedImage overlayImage, int fudgeAmount) {

		BufferedImage mask = new BufferedImage(overlayImage.getWidth(), overlayImage.getHeight(), Transparency.OPAQUE);

		int black = Color.black.getRGB();
		int white = Color.white.getRGB();

		for (int y = 0; y < mask.getHeight(); y++) {

			for (int x = 0; x < mask.getWidth(); x++) {

				int color = overlayImage.getRGB(x, y);

				mask.setRGB(x, y, isMagenta(color, fudgeAmount) ? black : white);
			}
		}

		return mask;
	}

	/**
	 * Get the overlay image with the mask applied
	 */
	public static BufferedImage translateOverlay(BufferedImage overlayImage, int fudgeAmount) {

		if (overlayImage == null) {
			return null;
		}

		// Needs to be translucent
		BufferedImage translatedOverlayImage = new BufferedImage(overlayImage.getWidth(), overlayImage.getHeight(), Transparency.TRANSLUCENT);
		BufferedImage mask = getMagentaMask(overlayImage, fudgeAmount);

		for (int y = 0; y < overlayImage.getHeight(); y++) {

			for (int x = 0; x < overlayImage.getWidth(); x++) {

				translatedOverlayImage.setRGB(x, y, calculateColor(overlayImage.getRGB(x, y), mask.getRGB(x, y)));
			}
		}

		return translatedOverlayImage;
	}

	private static int calculateColor(int sourcePixel, int maskPixel) {

		int alphaChannel = (sourcePixel & 0xff000000) >>> 24;
		alphaChannel = (int) (((maskPixel & 0xff) / 255.0) * alphaChannel);
		alphaChannel = alphaChannel << 24;

		sourcePixel &= 0xffffff; // clear old alpha
		sourcePixel = alphaChannel | sourcePixel; // install new alpha

		return sourcePixel;
	}

	private static boolean isMagenta(int color, int fudge) {

		Color sc = new Color(color);

		int r = sc.getRed();
		int g = sc.getGreen();
		int b = sc.getBlue();

		if (Math.abs(r - b) > fudge) {
			return false;
		}

		if (g > r - fudge || g > b - fudge) {
			return false;
		}

		return true;
	}

}
