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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.DeveloperOptions;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** */
public class PartitionedDrawableRenderer implements DrawableRenderer {
  private static Logger log = LogManager.getLogger(PartitionedDrawableRenderer.class);
  private static boolean messageLogged = false;

  private static final int CHUNK_SIZE = 256;
  private static List<BufferedImage> unusedChunkList = new LinkedList<BufferedImage>();

  private final Zone zone;
  private final Set<String> noImageSet = new HashSet<String>();
  private final List<Tuple> chunkList = new LinkedList<Tuple>();
  private int maxChunks;

  private double lastScale;
  private Rectangle lastViewport;

  private int horizontalChunkCount;
  private int verticalChunkCount;

  private boolean dirty = false;

  public PartitionedDrawableRenderer(Zone zone) {
    this.zone = zone;
  }

  public void flush() {
    int unusedSize = unusedChunkList.size();
    for (Tuple tuple : chunkList) {
      // Reuse the images
      if (unusedSize < maxChunks && tuple != null) {
        unusedChunkList.add(tuple.image);
        unusedSize++;
      }
    }
    chunkList.clear();
    noImageSet.clear();
    dirty = false;
  }

  public void setDirty() {
    dirty = true;
  }

  public void renderDrawables(
      Graphics g, List<DrawnElement> drawableList, Rectangle viewport, double scale) {
    CodeTimer.using(
        "Renderer",
        timer -> {
          timer.setThreshold(10);
          timer.setEnabled(false);

          // NOTHING TO DO
          if (drawableList == null || drawableList.isEmpty()) {
            if (dirty) flush();
            return;
          }
          // View changed ?
          if (dirty || lastScale != scale) {
            flush();
          }
          if (lastViewport == null
              || viewport.width != lastViewport.width
              || viewport.height != lastViewport.height) {
            horizontalChunkCount = (int) Math.ceil(viewport.width / (double) CHUNK_SIZE) + 1;
            verticalChunkCount = (int) Math.ceil(viewport.height / (double) CHUNK_SIZE) + 1;

            maxChunks = (horizontalChunkCount * verticalChunkCount * 2);
          }
          // Compute grid
          int gridx = (int) Math.floor(-viewport.x / (double) CHUNK_SIZE);
          int gridy = (int) Math.floor(-viewport.y / (double) CHUNK_SIZE);

          // OK, weirdest hack ever. Basically, when the viewport.x is exactly divisible by the
          // chunk size, the gridx decrements too early, creating a visual jump in the drawables. I
          // don't know the exact cause, but this seems to account for it
          // note that it only happens in the negative space. Weird.
          gridx += (viewport.x > CHUNK_SIZE && (viewport.x % CHUNK_SIZE == 0) ? -1 : 0);
          gridy += (viewport.y > CHUNK_SIZE && (viewport.y % CHUNK_SIZE == 0) ? -1 : 0);

          for (int row = 0; row < verticalChunkCount; row++) {
            for (int col = 0; col < horizontalChunkCount; col++) {
              int cellX = gridx + col;
              int cellY = gridy + row;

              String key = getKey(cellX, cellY);
              if (noImageSet.contains(key)) {
                continue;
              }
              Tuple chunk = findChunk(chunkList, key);
              if (chunk == null) {
                chunk = new Tuple(key, createChunk(drawableList, cellX, cellY, scale));

                if (chunk.image == null) {
                  noImageSet.add(key);
                  continue;
                }
              }
              // Most recently used is at the front
              chunkList.add(0, chunk);

              // Trim to the right size
              if (chunkList.size() > maxChunks) {
                int chunkSize = chunkList.size();
                // chunkList.subList(maxChunks, chunkSize).clear();
                while (chunkSize > maxChunks) {
                  chunkList.remove(--chunkSize);
                }
              }
              int x =
                  col * CHUNK_SIZE
                      - ((CHUNK_SIZE - viewport.x)) % CHUNK_SIZE
                      - (gridx < -1 ? CHUNK_SIZE : 0);
              int y =
                  row * CHUNK_SIZE
                      - ((CHUNK_SIZE - viewport.y)) % CHUNK_SIZE
                      - (gridy < -1 ? CHUNK_SIZE : 0);

              timer.start("render:DrawImage");
              g.drawImage(chunk.image, x, y, null);
              timer.stop("render:DrawImage");

              // DEBUG: Show partition boundaries
              if (DeveloperOptions.Toggle.ShowPartitionDrawableBoundaries.isEnabled()) {
                if (!messageLogged) {
                  messageLogged = true;
                  log.debug(
                      "DEBUG logging of "
                          + this.getClass().getSimpleName()
                          + " causes colored rectangles and message strings.");
                }
                if (col % 2 == 0) {
                  if (row % 2 == 0) {
                    g.setColor(Color.white);
                  } else {
                    g.setColor(Color.green);
                  }
                } else {
                  if (row % 2 == 0) {
                    g.setColor(Color.green);
                  } else {
                    g.setColor(Color.white);
                  }
                }
                g.drawRect(x, y, CHUNK_SIZE - 1, CHUNK_SIZE - 1);
                g.drawString(key, x + CHUNK_SIZE / 2, y + CHUNK_SIZE / 2);
              }
            }
          }
          // REMEMBER
          lastViewport = viewport;
          lastScale = scale;
        });
  }

  /**
   * Given a List and a String key, find the element in the list that matches the key.
   *
   * @param list
   * @param key
   * @return
   */
  private Tuple findChunk(List<Tuple> list, String key) {
    ListIterator<Tuple> iter = list.listIterator();
    while (iter.hasNext()) {
      Tuple tuple = iter.next();
      if (tuple.key.equals(key)) {
        iter.remove();
        return tuple;
      }
    }
    return null;
  }

  private BufferedImage createChunk(
      List<DrawnElement> drawableList, int gridx, int gridy, double scale) {
    final var timer = CodeTimer.get();

    int x = gridx * CHUNK_SIZE;
    int y = gridy * CHUNK_SIZE;

    BufferedImage image = null;
    Composite oldComposite = null;
    Graphics2D g = null;

    for (DrawnElement element : drawableList) {
      timer.start("createChunk:calculate");
      Drawable drawable = element.getDrawable();
      Rectangle drawableBounds = drawable.getBounds(zone);
      if (drawableBounds == null) {
        timer.stop("createChunk:calculate");
        continue;
      }

      Rectangle2D drawnBounds = new Rectangle(drawableBounds);
      Rectangle2D chunkBounds =
          new Rectangle(
              (int) (gridx * (CHUNK_SIZE / scale)),
              (int) (gridy * (CHUNK_SIZE / scale)),
              (int) (CHUNK_SIZE / scale),
              (int) (CHUNK_SIZE / scale));

      // Handle pen size
      Pen pen = element.getPen();
      int penSize = (int) (pen.getThickness() / 2 + 1);
      drawnBounds.setRect(
          drawnBounds.getX() - penSize,
          drawnBounds.getY() - penSize,
          drawnBounds.getWidth() + pen.getThickness(),
          drawnBounds.getHeight() + pen.getThickness());
      timer.stop("createChunk:calculate");

      timer.start("createChunk:BoundsCheck");
      if (!drawnBounds.intersects(chunkBounds)) {
        timer.stop("createChunk:BoundsCheck");
        continue;
      }
      timer.stop("createChunk:BoundsCheck");

      timer.start("createChunk:CreateChunk");
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
      timer.stop("createChunk:CreateChunk");

      if (pen.getOpacity() != 1 && pen.getOpacity() != 0 /*
																 * handle legacy pens, besides, it doesn't make sense to have a non visible pen
																 */) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pen.getOpacity()));
      }

      // g.setColor(Color.red);
      // g.draw(drawnBounds);

      timer.start("createChunk:Draw");
      if (drawable instanceof DrawablesGroup) {
        DrawablesGroup dg = (DrawablesGroup) drawable;
        BufferedImage groupImage = createChunk(dg.getDrawableList(), gridx, gridy, scale);
        Graphics2D g2 = image.createGraphics();
        g2.drawImage(groupImage, 0, 0, CHUNK_SIZE, CHUNK_SIZE, null);
        g2.dispose();
      } else drawable.draw(zone, g, pen);
      g.setComposite(oldComposite);
      timer.stop("createChunk:Draw");
    }
    if (g != null) {
      g.dispose();
    }
    return image;
  }

  private BufferedImage getNewChunk() {
    BufferedImage image = null;
    if (unusedChunkList.size() > 0) {
      image = unusedChunkList.remove(0);
      ImageUtil.clearImage(image);
    } else {
      image = new BufferedImage(CHUNK_SIZE, CHUNK_SIZE, Transparency.BITMASK);
    }
    image.setAccelerationPriority(1);
    return image;
  }

  private String getKey(int col, int row) {
    return col + "." + row;
  }

  private static class Tuple {
    String key;
    BufferedImage image;

    public Tuple(String key, BufferedImage image) {
      this.key = key;
      this.image = image;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Tuple tuple = (Tuple) o;
      return Objects.equals(key, tuple.key);
    }

    @Override
    public int hashCode() {
      return Objects.hash(key);
    }
  }
}
