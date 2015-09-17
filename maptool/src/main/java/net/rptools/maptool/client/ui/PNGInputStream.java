/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

/**
 * An ImageStream created from a Component. 
 * The Component may be very large in terms of XY size: it will
 * be rendered piecemeal by modifying its bounds().
 * @author mcl
 */
/*public class PNGInputStream extends InputStream {

	Component largeComponent;
	Rectangle origBounds;
	Dimension origSize;

	ImageWriter pngWriter = null; 

	*//**
		* @param largeComponent the Component to be turned into a PNG input stream
		*//*
		public PNGInputStream(Component c) {
		largeComponent = c;
		
		origBounds = largeComponent.getBounds();
		origSize   = largeComponent.getSize();
		}
		
		@Override
		public int read() throws IOException {
		if (pngWriter != null) {
		}
		else {
		pngWriter = (ImageWriter)ImageIO.getImageWritersByFormatName("png").next();
		pngWriter.setOutput(output);
		IIOImage image = new IIOImage(cachedZoneImage, null, null);
		pngWriter.write(null, image, iwp);
		}
		return 0;
		}
		}
		*/
