package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.Grid;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.TokenShape;
import net.rptools.maptool.util.ImageManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class TokenVBL {
	Area tokenVBL;
	Token token;

	public TokenVBL(Token token1) {
		tokenVBL = null;
		token = token1;
	}

	public void doWork(Area a, ZoneRenderer renderer) {
		tokenVBL = a;
		doWork(renderer);
	}

	public Area buildVBL() {
		BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());
		return getArea_FastHack(image);
	}

	public void doWork(ZoneRenderer renderer) {
		boolean isVisionBlocker = !token.isVisionBlocker();

		token.setVisionBlocker(isVisionBlocker);
		//token.setTokenVBL(null);
		/*
				if (token.hasTokenVBL()) {
					tokenVBL = token.getTokenVBL();
					System.out.println("current tokenVBL bounds: " + tokenVBL.getBounds());
				} else {
					BufferedImage image = ImageManager.getImageAndWait(token.getImageAssetId());
		
					tokenVBL = getArea_FastHack(image);
					token.setTokenVBL(tokenVBL);
					System.out.println("new tokenVBL bounds: " + tokenVBL.getBounds());
				}
		*/
		System.out.println("#now tokenVBL bounds: " + tokenVBL.getBounds());

		Grid grid = renderer.getZone().getGrid();

		System.out.println("##Token scale: " + token.getSizeScale());
		System.out.println("##Token scaleX: " + token.getScaleX());
		System.out.println("##Token scaleY: " + token.getScaleY());
		System.out.println("##Token footprint: " + token.getFootprint(grid));
		System.out.println("##Token snaptoscale (native size): " + token.isSnapToScale());
		System.out.println("##Token footprint scale: " + token.getFootprint(grid).getScale());

		//System.out.println("##BlockVisionAction: image asset" + token.getImageAssetId());
		//System.out.println("##BlockVisionAction: Area" + newVBL.toString());
		AffineTransform atArea = new AffineTransform();

		double tx = token.getX();
		double ty = token.getY();
		double rx = tokenVBL.getBounds().x + (tokenVBL.getBounds().width / 2);
		double ry = tokenVBL.getBounds().y + (tokenVBL.getBounds().height / 2);

		if (token.isFlippedX())
			tx += (token.getWidth() * token.getScaleX());
		if (token.isFlippedY())
			ty += (token.getHeight() * token.getScaleY());

		if (token.isFlippedX())
			rx -= (token.getWidth() * token.getScaleX());
		if (token.isFlippedY())
			ry -= (token.getHeight() * token.getScaleY());

		atArea.translate(tx, ty);

		if (token.getShape().equals(TokenShape.TOP_DOWN) && token.getFacing() != null) {
			//System.out.println("facing: " + token.getFacing());

			int facing = token.getFacing();

			//int tw = token.getWidth();
			//int th = token.getHeight();

			double tr = -(facing + 90);

			//System.out.println("tx: " + tx);
			//System.out.println("ty: " + ty);
			//System.out.println("rx: " + rx);
			//System.out.println("ry: " + ry);
			//System.out.println("tr: " + tr);

			atArea.rotate(Math.toRadians(tr), rx, ry);
		}

		double sx = token.getScaleX();
		double sy = token.getScaleY();
		if (token.isFlippedX())
			sx = sx * -1;
		if (token.isFlippedY())
			sy = sy * -1;

		atArea.scale(sx, sy);

		//System.out.println("token.getScaleX(): " + token.getScaleX());

		tokenVBL.transform(atArea);

		// Finally add the area to VBL
		renderVBL(renderer, tokenVBL, isVisionBlocker);
	}

	/**
	 * Creates an Area with PixelPerfect precision
	 * 
	 * @param color
	 *            The color that is draws the Custom Shape
	 * @param tolerance
	 *            The color tolerance
	 * @return Area
	 */
	private Area getArea(BufferedImage image, Color color, int tolerance) {
		if (image == null)
			return null;
		Area area = new Area();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color pixel = new Color(image.getRGB(x, y));
				if (isIncluded(color, pixel, tolerance)) {
					Rectangle r = new Rectangle(x, y, 1, 1);
					area.add(new Area(r));
				}
			}
		}

		return area;
	}

	private Area getArea_FastHack(BufferedImage image) {
		//Assumes all colors form the VBL Area, eg everything except transparent pixels
		if (image == null)
			return null;

		System.out.println("Creating VBL for token...");

		Area area = new Area();
		Rectangle r;
		int y1, y2;

		for (int x = 0; x < image.getWidth(); x++) {
			y1 = 99;
			y2 = -1;
			for (int y = 0; y < image.getHeight(); y++) {
				//Color pixel = new Color(image.getRGB(x, y));
				int pixel = image.getRGB(x, y);
				// 24 is full transparency, 28 seems a little more forgiving for shadows
				// but more jagged VBL...
				if ((pixel >> 28) != 0x00) {
					if (y1 == 99) {
						y1 = y;
						y2 = y;
					}
					if (y > (y2 + 1)) {
						r = new Rectangle(x, y1, 1, y2 - y1);
						area.add(new Area(r));
						y1 = y;
						y2 = y;
					}
					y2 = y;
				}
			}
			if ((y2 - y1) >= 0) {
				r = new Rectangle(x, y1, 1, y2 - y1);
				area.add(new Area(r));
			}
		}

		return area;
	}

	private boolean isIncluded(Color target, Color pixel, int tolerance) {
		int rT = target.getRed();
		int gT = target.getGreen();
		int bT = target.getBlue();
		int rP = pixel.getRed();
		int gP = pixel.getGreen();
		int bP = pixel.getBlue();
		return ((rP - tolerance <= rT) && (rT <= rP + tolerance) &&
				(gP - tolerance <= gT) && (gT <= gP + tolerance) &&
				(bP - tolerance <= bT) && (bT <= bP + tolerance));
	}

	private void renderVBL(ZoneRenderer renderer, Area area, boolean draw) {
		if (draw) {
			renderer.getZone().addTopology(area);
			MapTool.serverCommand().addTopology(renderer.getZone().getId(),
					area);
		} else {
			renderer.getZone().removeTopology(area);
			MapTool.serverCommand().removeTopology(renderer.getZone().getId(),
					area);
		}
		renderer.repaint();
	}

	private BufferedImage createImageFromBytes(byte[] imageData) {
		ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
		try {
			return ImageIO.read(bais);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}