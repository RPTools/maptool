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

/**
 * Token overlay for bar meters.
 *
 * @author Jay
 */
public class MultipleImageBarTokenOverlay extends BarTokenOverlay {

  /** ID of the base image displayed in the overlay. */
  private MD5Key[] assetIds;

  /** Needed for serialization */
  public MultipleImageBarTokenOverlay() {
    this(AbstractTokenOverlay.DEFAULT_STATE_NAME, null);
  }

  /**
   * Create the complete image overlay.
   *
   * @param name Name of the new token overlay
   * @param theAssetIds Id of the base image.
   */
  public MultipleImageBarTokenOverlay(String name, MD5Key[] theAssetIds) {
    super(name);
    assetIds = theAssetIds;
  }

  /** @see net.rptools.maptool.client.ui.token.AbstractTokenOverlay#clone() */
  @Override
  public Object clone() {
    BarTokenOverlay overlay = new MultipleImageBarTokenOverlay(getName(), assetIds);
    overlay.setOrder(getOrder());
    overlay.setGroup(getGroup());
    overlay.setMouseover(isMouseover());
    overlay.setOpacity(getOpacity());
    overlay.setIncrements(getIncrements());
    overlay.setSide(getSide());
    overlay.setShowGM(isShowGM());
    overlay.setShowOwner(isShowOwner());
    overlay.setShowOthers(isShowOthers());
    return overlay;
  }

  /**
   * @see net.rptools.maptool.client.ui.token.BarTokenOverlay#paintOverlay(java.awt.Graphics2D,
   *     net.rptools.maptool.model.Token, java.awt.Rectangle, double)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds, double value) {
    int incr = findIncrement(value);

    // Get the images
    BufferedImage image = ImageManager.getImageAndWait(assetIds[incr]);

    Dimension d = bounds.getSize();
    Dimension size = new Dimension(image.getWidth(), image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    // Find the position of the image according to the size and side where they are placed
    int x = 0;
    int y = 0;
    switch (getSide()) {
      case RIGHT:
        x = d.width - size.width;
        break;
      case BOTTOM:
        y = d.height - size.height;
    }

    Composite tempComposite = g.getComposite();
    if (getOpacity() != 100) {
      g.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
    }
    g.drawImage(image, x, y, size.width, size.height, null);
    g.setComposite(tempComposite);
  }

  /** @return Getter for bottomAssetId */
  public MD5Key[] getAssetIds() {
    return assetIds;
  }

  /** @param theAssetIds Setter for bottomAssetId */
  public void setAssetIds(MD5Key[] theAssetIds) {
    this.assetIds = theAssetIds;
  }
}
