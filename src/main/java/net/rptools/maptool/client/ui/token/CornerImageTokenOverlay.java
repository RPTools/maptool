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

import java.awt.Rectangle;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;

/**
 * Place an image in a given corner.
 *
 * @author Jay
 */
public class CornerImageTokenOverlay extends ImageTokenOverlay {

  /** The corner where the image is placed */
  private Quadrant corner = Quadrant.SOUTH_EAST;

  /** Needed for serialization */
  public CornerImageTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, null, Quadrant.SOUTH_EAST);
  }

  /**
   * Create the complete image overlay.
   *
   * @param name Name of the new token overlay
   * @param anAssetId Id of the image displayed in the new token overlay.
   * @param aCorner Corner that contains the image.
   */
  public CornerImageTokenOverlay(String name, MD5Key anAssetId, Quadrant aCorner) {
    super(name, anAssetId);
    corner = aCorner;
  }

  /** @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone() */
  @Override
  public Object clone() {
    BooleanTokenOverlay overlay = new CornerImageTokenOverlay(getName(), getAssetId(), corner);
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
   * @see net.rptools.maptool.client.ui.token.ImageTokenOverlay#getImageBounds(java.awt.Rectangle,
   *     Token)
   */
  @Override
  protected Rectangle getImageBounds(Rectangle bounds, Token token) {
    int x = (bounds.width + 1) / 2;
    int y = (bounds.height + 1) / 2;
    switch (corner) {
      case SOUTH_EAST:
        break;
      case SOUTH_WEST:
        x = 0;
        break;
      case NORTH_EAST:
        y = 0;
        break;
      case NORTH_WEST:
        x = y = 0;
        break;
    } // endswitch
    return new Rectangle(x, y, bounds.width / 2, bounds.height / 2);
  }

  /** @return Getter for corner */
  public Quadrant getCorner() {
    return corner;
  }
}
