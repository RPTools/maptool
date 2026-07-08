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
package net.rptools.maptool.client.ui.token;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.rptools.lib.AwtUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.server.proto.ImageTokenOverlayDto;
import net.rptools.maptool.util.ImageManager;

/**
 * This is a token overlay that shows an image over the entire token
 *
 * @author Jay
 */
public final class ImageTokenOverlay extends BooleanTokenOverlay {

  /** ID of the image displayed in the overlay. */
  private MD5Key assetId;

  /**
   * Create the complete image overlay.
   *
   * @param name Name of the new token overlay
   * @param assetId ID of the image displayed in the new token overlay.
   */
  public ImageTokenOverlay(String name, MD5Key assetId) {
    super(name);
    this.assetId = assetId;
  }

  public ImageTokenOverlay(ImageTokenOverlay other) {
    super(other);
    this.assetId = other.assetId;
  }

  @Override
  public ImageTokenOverlay clone() {
    return new ImageTokenOverlay(this);
  }

  /**
   * @return Getter for assetId
   */
  public MD5Key getAssetId() {
    return assetId;
  }

  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds) {
    BufferedImage image = ImageManager.getImageAndWait(assetId);

    var imageBounds = new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight());
    AwtUtil.fitInto(imageBounds, bounds);

    // Paint it at the right location
    int width = (int) imageBounds.getWidth();
    int height = (int) imageBounds.getHeight();
    int x = (int) imageBounds.getMinX();
    int y = (int) imageBounds.getMinY();

    g.drawImage(image, x, y, width, height, null);
  }

  public ImageTokenOverlayDto toImageDto() {
    var dto = ImageTokenOverlayDto.newBuilder();
    dto.setAssetId(assetId.toString());
    return dto.build();
  }

  public static ImageTokenOverlay fromDto(ImageTokenOverlayDto dto) {
    var assetId = new MD5Key(dto.getAssetId());
    return new ImageTokenOverlay(DEFAULT_STATE_NAME, assetId);
  }
}
