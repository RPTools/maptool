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
package net.rptools.maptool.client.ui.assetpanel;

import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.PersistenceUtil;

public class PdfAsDirectory extends Directory {

  public static final String PROPERTY_IMAGE_LOADED = "imageLoaded";

  private final Map<File, FutureTask<Image>> imageMap = new HashMap<File, FutureTask<Image>>();

  private static final Image INVALID_IMAGE = new BufferedImage(1, 1, Transparency.OPAQUE);

  private static ExecutorService largeImageLoaderService = Executors.newFixedThreadPool(1);
  private static ExecutorService smallImageLoaderService = Executors.newFixedThreadPool(2);

  private AtomicBoolean continueProcessing = new AtomicBoolean(true);

  public PdfAsDirectory(File directory, FilenameFilter fileFilter) {
    super(directory, fileFilter);
  }

  @Override
  public String toString() {
    return getPath().getName();
  }

  @Override
  public void refresh() {
    imageMap.clear();

    // Tell any in-progress processing to stop
    AtomicBoolean oldBool = continueProcessing;
    continueProcessing = new AtomicBoolean(true);
    oldBool.set(false);

    super.refresh();
  }

  /**
   * Returns the asset associated with this file, or null if the file has not yet been loaded as an
   * asset
   *
   * @param imageFile
   * @return
   */
  public Image getImageFor(File imageFile) {
    FutureTask<Image> future = imageMap.get(imageFile);
    if (future != null) {
      if (future.isDone()) {
        try {
          return future.get() != INVALID_IMAGE ? future.get() : null;
        } catch (InterruptedException e) {
          // TODO: need to indicate a broken image
          return null;
        } catch (ExecutionException e) {
          // TODO: need to indicate a broken image
          return null;
        }
      }
      // Not done loading yet, don't block
      return null;
    }
    // load the asset in the background
    future =
        new FutureTask<Image>(new ImageLoader(imageFile)) {
          @Override
          protected void done() {
            firePropertyChangeEvent(
                new PropertyChangeEvent(PdfAsDirectory.this, PROPERTY_IMAGE_LOADED, false, true));
          }
        };
    if (imageFile.length() < 30 * 1024) {
      smallImageLoaderService.execute(future);
    } else {
      largeImageLoaderService.execute(future);
    }
    imageMap.put(imageFile, future);
    return null;
  }

  @Override
  protected Directory newDirectory(File directory, FilenameFilter fileFilter) {
    return new PdfAsDirectory(directory, fileFilter);
  }

  private class ImageLoader implements Callable<Image> {
    private final File imageFile;

    public ImageLoader(File imageFile) {
      this.imageFile = imageFile;
    }

    public Image call() throws Exception {
      // Have we been orphaned ?
      if (!continueProcessing.get()) {
        return null;
      }
      // Load it up
      Image thumbnail = null;
      try {
        if (imageFile.getName().toLowerCase().endsWith(Token.FILE_EXTENSION)) {
          thumbnail = PersistenceUtil.getTokenThumbnail(imageFile);
        } else if (imageFile.getName().toLowerCase().endsWith(".pdf")) {
          // Jamz: Added to mark all PDF assets with proper image, TODO: Move image asset to proper
          // location
          System.out.println("PDF Thumb: " + imageFile.getAbsolutePath());
          thumbnail = MapTool.getThumbnailManager().getThumbnail(imageFile);
        } else {
          thumbnail = MapTool.getThumbnailManager().getThumbnail(imageFile);
        }
      } catch (Throwable t) {
        t.printStackTrace();
        thumbnail = INVALID_IMAGE;
      }
      return thumbnail;
    }
  }
}
