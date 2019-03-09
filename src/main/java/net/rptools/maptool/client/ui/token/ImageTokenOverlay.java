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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.ImageManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is a token overlay that shows an image over the entire token
 *
 * @author Jay
 */
public class ImageTokenOverlay extends BooleanTokenOverlay {

  /** If of the image displayed in the overlay. */
  private MD5Key assetId;

  /** Logger instance for this class. */
  private static final Logger LOGGER = LogManager.getLogger(ImageTokenOverlay.class);

  /** Needed for serialization */
  public ImageTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, null);
  }

  /**
   * Create the complete image overlay.
   *
   * @param name Name of the new token overlay
   * @param anAssetId Id of the image displayed in the new token overlay.
   */
  public ImageTokenOverlay(String name, MD5Key anAssetId) {
    super(name);
    assetId = anAssetId;
  }

  /** @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone() */
  @Override
  public Object clone() {
    BooleanTokenOverlay overlay = new ImageTokenOverlay(getName(), assetId);
    overlay.setOrder(getOrder());
    overlay.setGroup(getGroup());
    overlay.setMouseover(isMouseover());
    overlay.setOpacity(getOpacity());
    overlay.setShowGM(isShowGM());
    overlay.setShowOwner(isShowOwner());
    overlay.setShowOthers(isShowOthers());
    return overlay;
  }

  /**
   * @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#paintOverlay(java.awt.Graphics2D,
   *     net.rptools.maptool.model.Token, java.awt.Rectangle)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds) {

    // Get the image
    Rectangle iBounds = getImageBounds(bounds, token);
    Dimension d = iBounds.getSize();

    BufferedImage image = ImageManager.getImageAndWait(assetId);
    Dimension size = new Dimension(image.getWidth(), image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    // Paint it at the right location
    int width = size.width;
    int height = size.height;
    int x = iBounds.x + (d.width - width) / 2;
    int y = iBounds.y + (d.height - height) / 2;
    Composite tempComposite = g.getComposite();
    if (getOpacity() != 100)
      g.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
    g.drawImage(image, x, y, size.width, size.height, null);
    g.setComposite(tempComposite);
  }

  /** @return Getter for assetId */
  public MD5Key getAssetId() {
    return assetId;
  }

  /**
   * Calculate the image bounds from the token bounds
   *
   * @param bounds Bounds of the token passed to the overlay.
   * @param token Token being decorated.
   * @return The bounds w/in the token where the image is painted.
   */
  protected Rectangle getImageBounds(Rectangle bounds, Token token) {
    return bounds;
  }
}
