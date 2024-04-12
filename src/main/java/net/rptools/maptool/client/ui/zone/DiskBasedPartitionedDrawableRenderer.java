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
package net.rptools.maptool.client.ui.zone;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import net.rptools.lib.FileUtil;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;

/** */
public class DiskBasedPartitionedDrawableRenderer implements DrawableRenderer {
  private static final File CACHE_DIR = AppUtil.getAppHome("dbpdrcache");

  private static final BufferedImage NO_IMAGE = new BufferedImage(1, 1, Transparency.OPAQUE);
  private static final int CHUNK_SIZE = 256;

  private final Zone zone;
  private final Map<String, BufferedImage> chunkMap = new HashMap<String, BufferedImage>();

  private double lastScale;
  private Rectangle lastViewport;

  private int horizontalChunkCount;
  private int verticalChunkCount;

  private boolean dirty = false;

  static {
    try {
      // Clear out the cache. Boot up is as good of a time as any
      if (CACHE_DIR.exists()) {
        FileUtil.delete(CACHE_DIR);
      }
      CACHE_DIR.mkdirs();
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
  }

  public DiskBasedPartitionedDrawableRenderer(Zone zone) {
    this.zone = zone;
    flush();
  }

  public void flush() {
    File chunkDir = getChunkDir();
    try {
      if (chunkDir.exists()) {
        FileUtil.delete(chunkDir);
      }
      chunkDir.mkdirs();
    } catch (Exception ioe) {
      ioe.printStackTrace();
    }
    chunkMap.clear();
    dirty = false;
  }

  public void setDirty() {
    dirty = true;
  }

  public void renderDrawables(
      Graphics g, List<DrawnElement> drawableList, Rectangle viewport, double scale) {
    // NOTHING TO DO
    if (drawableList == null || drawableList.size() == 0) {
      if (dirty) flush();
      return;
    }
    if (dirty || lastScale != scale) {
      flush();
    }
    if (lastViewport == null
        || viewport.width != lastViewport.width
        || viewport.height != lastViewport.height) {
      horizontalChunkCount = (int) Math.ceil(viewport.width / (double) CHUNK_SIZE) + 1;
      verticalChunkCount = (int) Math.ceil(viewport.height / (double) CHUNK_SIZE) + 1;
    }
    int gridx = (int) Math.floor(-viewport.x / (double) CHUNK_SIZE);
    int gridy = (int) Math.floor(-viewport.y / (double) CHUNK_SIZE);

    Set<String> chunkCache = new HashSet<String>(chunkMap.keySet());
    int count = 0;
    for (int row = 0; row < verticalChunkCount; row++) {
      for (int col = 0; col < horizontalChunkCount; col++) {
        int x =
            col * CHUNK_SIZE
                - ((CHUNK_SIZE - viewport.x)) % CHUNK_SIZE
                - (gridx < -1 ? CHUNK_SIZE : 0);
        int y =
            row * CHUNK_SIZE
                - ((CHUNK_SIZE - viewport.y)) % CHUNK_SIZE
                - (gridy < -1 ? CHUNK_SIZE : 0);

        int cellX = gridx + col;
        int cellY = gridy + row;

        String key = getKey(cellX, cellY);
        BufferedImage chunk = chunkMap.get(key);
        if (chunk == null) {
          chunk = createChunk(drawableList, cellX, cellY, scale);
          chunkMap.put(key, chunk);
        }
        if (chunk != null && chunk != NO_IMAGE) {
          // System.out.println("Drawing: " + key);
          count++;
          g.drawImage(chunk, x, y, null);
        }
        chunkCache.remove(key);

        // if (col%2 == 0) {
        // if (row%2 == 0) {
        // g.setColor(Color.white);
        // } else {
        // g.setColor(Color.green);
        // }
        // } else {
        // if (row%2 == 0) {
        // g.setColor(Color.green);
        // } else {
        // g.setColor(Color.white);
        // }
        // }
        // g.drawRect(x, y, CHUNK_SIZE-1, CHUNK_SIZE-1);
        // g.drawString(key, x + CHUNK_SIZE/2, y + CHUNK_SIZE/2);
      }
    }
    for (String key : chunkCache) {
      // Keep NO_IMAGEs in memory since they take virtually no space, and reduces the amount of work
      // to do
      if (chunkMap.get(key) != NO_IMAGE) {
        chunkMap.remove(key);
      }
    }
    // System.out.println("Chunks: " + count);

    // REMEMBER
    lastViewport = viewport;
    lastScale = scale;
  }

  private BufferedImage createChunk(
      List<DrawnElement> drawableList, int gridx, int gridy, double scale) {
    // Have we already cached it ?
    File chunkFile = getChunkFile(gridx, gridy);
    if (chunkFile.exists()) {
      try {
        BufferedImage image = ImageIO.read(chunkFile);
        // System.out.println("Using cache: " + gridx + ", " + gridy);
        return image;
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    // System.out.println("Creating " + gridx + ", " + gridy);

    int x = gridx * CHUNK_SIZE;
    int y = gridy * CHUNK_SIZE;

    BufferedImage image = null;
    Composite oldComposite = null;
    Graphics2D g = null;

    for (DrawnElement element : drawableList) {
      Drawable drawable = element.getDrawable();

      Rectangle2D drawnBounds = drawable.getBounds(zone);
      Rectangle2D chunkBounds =
          new Rectangle(
              (int) (gridx * (CHUNK_SIZE / scale)),
              (int) (gridy * (CHUNK_SIZE / scale)),
              (int) (CHUNK_SIZE / scale),
              (int) (CHUNK_SIZE / scale));
      // if (gridx == 0 && gridy == 1) {
      // System.out.println(drawnBounds.intersects(chunkBounds));
      // System.out.println(drawnBounds + " - " + chunkBounds);
      // }

      // TODO: handle pen size
      if (!drawnBounds.intersects(chunkBounds)) {
        continue;
      }
      if (image == null) {
        image = getNewChunk();
        g = image.createGraphics();
        g.setClip(0, 0, CHUNK_SIZE, CHUNK_SIZE);
        oldComposite = g.getComposite();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform af = new AffineTransform();
        af.translate(-x, -y);
        af.scale(scale, scale);
        g.setTransform(af);
      }
      Pen pen = element.getPen();
      // Handle legacy pens; besides, it doesn't make sense to have a non-visible pen.
      if (pen.getOpacity() != 1 && pen.getOpacity() != 0) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pen.getOpacity()));
      }
      // if (gridx == 0 && gridy == 1) {
      // System.out.println("draw");
      // }
      drawable.draw(zone, g, pen);
      g.setComposite(oldComposite);
    }

    if (g != null) {
      g.dispose();
    }
    // if (image != null && isEmpty(image)) {
    // releaseChunk(image);
    // image = null;
    // }
    if (image == null) {
      image = NO_IMAGE;
    } else {
      try {
        ImageIO.write(image, "png", chunkFile);
      } catch (IOException ioe) {
        ioe.printStackTrace();
      }
    }
    return image;
  }

  private File getChunkDir() {
    return new File(CACHE_DIR.getAbsolutePath() + "/" + hashCode());
  }

  private File getChunkFile(int x, int y) {
    return new File(getChunkDir().getAbsolutePath() + "/" + x + "-" + y);
  }

  private BufferedImage getNewChunk() {
    return new BufferedImage(CHUNK_SIZE, CHUNK_SIZE, Transparency.BITMASK);
  }

  private String getKey(int col, int row) {
    return col + "." + row;
  }
}
