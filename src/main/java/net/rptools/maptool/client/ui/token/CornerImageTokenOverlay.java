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

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import net.rptools.lib.AwtUtil;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;
import net.rptools.maptool.server.proto.CornerImageTokenOverlayDto;
import net.rptools.maptool.server.proto.QuadrantDto;
import net.rptools.maptool.util.ImageManager;

/**
 * Place an image in a given corner.
 *
 * @author Jay
 */
public final class CornerImageTokenOverlay extends BooleanTokenOverlay {

  /** ID of the image displayed in the overlay. */
  private MD5Key assetId;

  /** The corner where the image is placed */
  private Quadrant corner;

  /**
   * Create the complete image overlay.
   *
   * @param name Name of the new token overlay
   * @param assetId Id of the image displayed in the new token overlay.
   * @param corner Corner that contains the image.
   */
  public CornerImageTokenOverlay(String name, MD5Key assetId, Quadrant corner) {
    super(name);
    this.assetId = assetId;
    this.corner = corner;
  }

  public CornerImageTokenOverlay(CornerImageTokenOverlay other) {
    super(other);
    this.assetId = other.assetId;
    this.corner = other.corner;
  }

  @Override
  public CornerImageTokenOverlay clone() {
    return new CornerImageTokenOverlay(this);
  }

  public MD5Key getAssetId() {
    return assetId;
  }

  /**
   * @return Getter for corner
   */
  public Quadrant getCorner() {
    return corner;
  }

  public Rectangle2D getBounds(Rectangle2D bounds) {
    var result = new Rectangle2D.Double();
    result.setRect(bounds);

    // Push the image into one of the corners.
    result.width /= 2;
    result.height /= 2;
    switch (corner) {
      case NORTH_WEST -> {
        // Already at (0, 0)
      }
      case NORTH_EAST -> {
        result.x += result.width;
      }
      case SOUTH_EAST -> {
        result.x += result.width;
        result.y += result.height;
      }
      case SOUTH_WEST -> {
        result.y += result.height;
      }
    }

    return result;
  }

  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds) {
    BufferedImage image = ImageManager.getImageAndWait(assetId);
    Rectangle2D imageBounds = new Rectangle2D.Double(0, 0, image.getWidth(), image.getHeight());
    AwtUtil.fitInto(imageBounds, bounds);
    imageBounds = getBounds(imageBounds);

    // Paint it at the right location
    int width = (int) imageBounds.getWidth();
    int height = (int) imageBounds.getHeight();
    int x = (int) imageBounds.getMinX();
    int y = (int) imageBounds.getMinY();

    g.drawImage(image, x, y, width, height, null);
  }

  public CornerImageTokenOverlayDto toCornerImageDto() {
    var dto = CornerImageTokenOverlayDto.newBuilder();
    dto.setAssetId(assetId.toString());
    dto.setQuadrant(QuadrantDto.valueOf(corner.name()));
    return dto.build();
  }

  public static CornerImageTokenOverlay fromDto(CornerImageTokenOverlayDto dto) {
    var assetId = new MD5Key(dto.getAssetId());
    var quadrant = Quadrant.valueOf(dto.getQuadrant().name());
    return new CornerImageTokenOverlay(DEFAULT_STATE_NAME, assetId, quadrant);
  }
}
