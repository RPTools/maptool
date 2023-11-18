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

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;

/**
 * This is a read-only implementation of BufferedImage intended to be used with ImageWriters to
 * create very large graphics files. Since this is read-only, it gets its pixel data by rasterizing
 * a Zone piece-meal as the data is requested by getData(rectangle) calls.
 *
 * <p>A very simple 4 MB cache is implemented to reduce zoneRender calls.
 */
public class ZoneImageGenerator extends BufferedImage {

  // final JComponent largeComponent;
  final ZoneRenderer renderer;
  final PlayerView view;
  final Rectangle origBounds;

  final int maxCacheSize = 1024 * 1024; // probably 4MB for most systems
  Raster cachedRaster;
  Rectangle cachedRect;
  Rectangle prevCacheRect;
  int numMisses = 0; // total number of cache misses, ever
  int recentHits = 0; // hits since last cache miss

  public ZoneImageGenerator(ZoneRenderer renderer, PlayerView view) {
    // The BufferedImage raster made by super() is just a dummy.
    // Making it something reasonable to avoid edge-case errors.
    super(32, 32, Transparency.OPAQUE);

    this.renderer = renderer;
    this.view = view;
    origBounds = new Rectangle(renderer.getBounds());
  }

  private boolean cacheMiss(Rectangle rect) {
    boolean miss;
    miss = (cachedRect == null) || !cachedRect.contains(rect);
    return miss;
  }

  @Override
  public Raster getData(Rectangle rect) {
    if (cacheMiss(rect)) {
      // Figure out what slice of the Zone to cache
      int sizeX = rect.width;
      int sizeY = rect.height;
      // Let's first try making the cache as wide as the whole zone
      sizeX = Math.max(sizeX, renderer.getBounds().width);
      sizeY = Math.max(sizeY, (maxCacheSize / sizeX));
      if ((sizeX * sizeY) > maxCacheSize) {
        // That didn't work, so let's try just making it as wide as requested
        // and as tall as possible
        sizeX = rect.width;
        sizeY = (maxCacheSize / sizeX);
      }
      // And let's make sure not to overdo things: no need for the cache to
      // be larger than the zone itself!
      sizeX = Math.min(sizeX, origBounds.width);
      sizeY = Math.min(sizeY, origBounds.height);
      prevCacheRect = cachedRect;
      cachedRect = new Rectangle(rect.x, rect.y, sizeX, sizeY);
      if (cacheMiss(rect)) {
        assert false
            : "Ooops! Cache doesn't contain requested data: wanted "
                + rect
                + " but have: "
                + cachedRect;
      }
      fillCache();
      numMisses++;
      recentHits = 0;
    }
    recentHits++;
    return cachedRaster.createChild(
        rect.x,
        rect.y, // source upper-left X, Y
        rect.width,
        rect.height, // size
        rect.x,
        rect.y, // child upper-left X, Y (this is 'translated', not actual pixel coords.
        // The returned raster is only as big as width * height
        null);
  }

  private void fillCache() {
    Rectangle rect = cachedRect;

    if (recentHits == 0 && numMisses > 0) {
      assert false : "Cache is being thrashed: " + prevCacheRect + cachedRect;
    }

    // preserve settings
    Scale origScale = new Scale(renderer.getZoneScale());
    Rectangle origBounds = new Rectangle(renderer.getBounds());

    // set new temp vars
    Scale s = new Scale(origScale);
    s.setOffset(origScale.getOffsetX() - rect.x, origScale.getOffsetY() - rect.y);
    renderer.setZoneScale(s);
    renderer.setBounds(rect);

    // make a tiny buffered image for this (hopefully) small rectangle request
    BufferedImage image = new BufferedImage(rect.width, rect.height, super.getType());
    Graphics2D g = image.createGraphics();
    g.setClip(0, 0, rect.width, rect.height);
    renderer.renderZone(g, view);
    // dispose is probably not needed. According to javadocs g's are disposed automatically when
    // used in paint()
    g.dispose();
    // makes a copy of the raster...
    Raster raster = image.getData();
    // ...so we can nudge it back to the original coordinate system
    cachedRaster = raster.createTranslatedChild(rect.x, rect.y);
    image = null;
    raster = null;

    renderer.setBounds(origBounds);
    renderer.setZoneScale(origScale);
  }

  ///////////////////////////////////////////////////////////////////////
  // All of the methods after this are various forms of no-op designed
  // to ensure the application fails in a predictable way
  // if it tries to write to this object.
  // As noted above, this is a read-only object!
  ///////////////////////////////////////////////////////////////////////

  /** This object cannot be written to. Method does nothing. */
  @Override
  public WritableRaster getRaster() {
    return null;
  }

  /** This object cannot be written to. Method does nothing. */
  @Override
  public synchronized void setRGB(int x, int y, int rgb) {
    // Do nothing, since this object is read-only.
    // I would like to throw an exception, but I can't since BufferedImage doesn't
  }

  /** This object cannot be written to. Method does nothing. */
  @Override
  public void setRGB(
      int startX, int startY, int w, int h, int[] rgbArray, int offset, int scansize) {
    // Do nothing, since this object is read-only.
    // I would like to throw an exception, but I can't since BufferedImage doesn't
  }

  /** To the outside world, we represent that this image is as large as the Component. */
  @Override
  public int getWidth() {
    return origBounds.width;
  }

  /** To the outside world, we represent that this image is as large as the Component. */
  @Override
  public int getHeight() {
    return origBounds.height;
  }

  /** To the outside world, we represent that this image is as large as the Component. */
  @Override
  public int getWidth(ImageObserver observer) {
    return this.getWidth();
  }

  /** To the outside world, we represent that this image is as large as the Component. */
  @Override
  public int getHeight(ImageObserver observer) {
    return this.getHeight();
  }

  /** This object cannot be written to. Returns null. */
  @Override
  public Graphics getGraphics() {
    return null;
  }

  /** This object cannot be written to. Returns null. */
  @Override
  public Graphics2D createGraphics() {
    return null;
  }

  @Override
  public Raster getData() {
    assert false : "Can not get a raster for the whole CachedComponentImage!";
    return null;
  }

  /** This should not be needed... hopefully all ImageWriters use getData instead. */
  @Override
  public WritableRaster copyData(WritableRaster outRaster) {
    if (outRaster == null) {
      assert false : "Someone tried to get a copy of the whole Raster in CachedComponentImage";
    } else {
      assert false
          : "Class CachedComponentImage.copyData() called. This should probably be implemented. ";
    }
    return null;
  }

  /** This object cannot be written to. Method does nothing. */
  @Override
  public void setData(Raster r) {
    // Do nothing, since this object is read-only.
    // I would like to throw an exception, but I can't since BufferedImage doesn't
  }

  // These methods are
  //
  //
  @Override
  public void removeTileObserver(TileObserver to) {
    super.removeTileObserver(to);
    // Ha! In BufferedImage image this is a no-op!
  }

  /** This object cannot be written to. Returns false. */
  @Override
  public boolean isTileWritable(int tileX, int tileY) {
    return false;
  }

  /** This object cannot be written to. Returns null. */
  @Override
  public Point[] getWritableTileIndices() {
    return null;
  }

  /** This object cannot be written to. Returns false. */
  @Override
  public boolean hasTileWriters() {
    return false;
  }

  /** This object cannot be written to. Returns null. */
  @Override
  public WritableRaster getWritableTile(int tileX, int tileY) {
    return null;
  }

  /** This object cannot be written to. Method does nothing. */
  @Override
  public void releaseWritableTile(int tileX, int tileY) {
    // Do nothing, since this object is read-only.
    // I would like to throw an exception, but I can't since BufferedImage doesn't
  }
}
