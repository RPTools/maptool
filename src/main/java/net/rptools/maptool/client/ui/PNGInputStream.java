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
package net.rptools.maptool.client.ui;

import java.io.InputStream;

/**
 * An ImageStream created from a Component. The Component may be very large in terms of XY size: it
 * will be rendered piecemeal by modifying its bounds().
 *
 * @author mcl
 */
public abstract class PNGInputStream extends InputStream {
  /**
   * Component largeComponent; Rectangle origBounds; Dimension origSize;
   *
   * <p>ImageWriter pngWriter = null;
   *
   * @param largeComponent the Component to be turned into a PNG input stream
   */
  /*
   * public PNGInputStream(Component c) { largeComponent = c;
   *
   * origBounds = largeComponent.getBounds(); origSize = largeComponent.getSize(); }
   *
   * @Override public int read() throws IOException { if (pngWriter != null) { } else { pngWriter = (ImageWriter)ImageIO.getImageWritersByFormatName("png").next(); pngWriter.setOutput(output);
   * IIOImage image = new IIOImage(cachedZoneImage, null, null); pngWriter.write(null, image, iwp); } return 0; } }
   */
}
