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
package net.rptools.lib.image;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.swing.SwingUtil;
import org.apache.commons.io.FileUtils;

public class ThumbnailManager {
  private final File thumbnailLocation;
  private final Dimension thumbnailSize;

  public ThumbnailManager(File thumbnailLocation, Dimension thumbnailSize) {
    this.thumbnailLocation = thumbnailLocation;
    this.thumbnailSize = thumbnailSize;
  }

  public File getThumbnailLocation() {
    return thumbnailLocation;
  }

  public Dimension getThumbnailSize() {
    return thumbnailSize;
  }

  public Image getThumbnail(File file) throws IOException {
    // Cache
    BufferedImage thumbnail = getCachedThumbnail(file);
    if (thumbnail != null) {
      return thumbnail;
    }
    // Create
    return createThumbnail(file);
  }

  private Image createThumbnail(File file) throws IOException {
    // Gather info
    File thumbnailFile = getThumbnailFile(file);
    if (thumbnailFile.exists()) {
      return ImageUtil.getImage(thumbnailFile);
    }

    Image image = ImageUtil.getImage(file);
    Dimension imgSize = new Dimension(image.getWidth(null), image.getHeight(null));

    // Test if we Should we bother making a thumbnail ?
    // Jamz: New size 100k (was 30k) and put in check so we're not creating thumbnails LARGER than
    // the original...
    if (file.length() < 102400
        || (imgSize.width <= thumbnailSize.width && imgSize.height <= thumbnailSize.height)) {
      return image;
    }
    // Transform the image
    SwingUtil.constrainTo(
        imgSize,
        Math.min(image.getWidth(null), thumbnailSize.width),
        Math.min(image.getHeight(null), thumbnailSize.height));
    BufferedImage thumbnailImage =
        new BufferedImage(imgSize.width, imgSize.height, ImageUtil.pickBestTransparency(image));

    Graphics2D g = thumbnailImage.createGraphics();
    AppPreferences.renderQuality.get().setShrinkRenderingHints(g);
    g.drawImage(image, 0, 0, imgSize.width, imgSize.height, null);
    g.dispose();

    // Use png to preserve transparency
    FileUtils.writeByteArrayToFile(thumbnailFile, ImageUtil.imageToBytes(thumbnailImage, "png"));

    return thumbnailImage;
  }

  public void clearImageThumbCache() {
    try {
      if (thumbnailLocation != null) {
        FileUtils.cleanDirectory(thumbnailLocation);
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private BufferedImage getCachedThumbnail(File file) {
    File thumbnailFile = getThumbnailFile(file);

    if (!thumbnailFile.exists()) {
      return null;
    }
    try {
      // Check that it hasn't changed on disk
      if (file.lastModified() > thumbnailFile.lastModified()) {
        return null;
      }
      // Get the thumbnail
      BufferedImage thumbnail = ImageIO.read(thumbnailFile);

      // Check that we have the size we want
      if (thumbnail.getWidth() != thumbnailSize.width
          && thumbnail.getHeight() != thumbnailSize.height) {
        return null;
      }
      return thumbnail;
    } catch (IOException ioe) {
      return null;
    }
  }

  private File getThumbnailFile(File file) {
    MD5Key key = new MD5Key(file.getAbsolutePath().getBytes());
    return new File(thumbnailLocation.getPath(), key.toString());
  }
}
