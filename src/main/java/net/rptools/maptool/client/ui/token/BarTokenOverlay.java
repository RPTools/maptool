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
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An overlay that paints a percentage as a bar. The bar can be smooth or it can be cut into fix
 * sized pieces.
 *
 * @author Jay
 */
public abstract class BarTokenOverlay extends AbstractTokenOverlay {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The number of increments painted for the bar. */
  private int increments;

  /** The side of the token where the bar is painted. */
  private Side side = Side.TOP;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Logger instance for this class. */
  private static final Logger LOGGER = LogManager.getLogger(BarTokenOverlay.class);

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /** @param name Name of the new bar. */
  public BarTokenOverlay(String name) {
    super(name);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /** @return Getter for increments */
  public int getIncrements() {
    return increments;
  }

  /** @param increments Setter for increments */
  public void setIncrements(int increments) {
    this.increments = increments;
  }

  /**
   * Calculate the bar size in pixels handling increments if any.
   *
   * @param size The maximum size of the bar
   * @param value The percentage of the bar that is painted.
   * @return The size of the bar to be painted.
   */
  public int calcBarSize(int size, double value) {
    if (value == 0) return 0;
    if (value == 1) return size;
    if (increments == 0) return (int) Math.ceil(size * value);
    double iSize = 1.0 / (increments - 1);
    return (int) (size * findIncrement(value) * iSize);
  }

  /**
   * Find the increment for the passed value
   *
   * @param value Find the increment for this value.
   * @return The increment for the value or -1 if no increments defined.
   */
  public int findIncrement(double value) {
    if (increments == 0) return -1;
    if (value == 0) return 0;
    if (value == 1) return increments - 1;
    return (int) Math.ceil(value * (increments - 1));
  }

  /** @return Getter for side */
  public Side getSide() {
    return side;
  }

  /** @param side Setter for side */
  public void setSide(Side side) {
    this.side = side;
  }

  /*---------------------------------------------------------------------------------------------
   * AbstractTokenOverlay Method implementation
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.client.ui.token.AbstractTokenOverlay#paintOverlay(java.awt.Graphics2D,
   *     net.rptools.maptool.model.Token, java.awt.Rectangle, java.lang.Object)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds, Object value) {
    if (value == null) return;
    double val = 0;
    if (value instanceof Number) {
      val = ((Number) value).doubleValue();
    } else {
      try {
        val = Double.parseDouble(value.toString());
      } catch (NumberFormatException e) {
        return; // Bad value so don't paint.
      } // endtry
    } // endif
    if (val < 0) val = 0;
    if (val > 1) val = 1;
    paintOverlay(g, token, bounds, val);
  }

  /*---------------------------------------------------------------------------------------------
   * Abstract Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Paint the overlay for the passed token.
   *
   * @param g Graphics used to paint. It is already translated so that 0,0 is the upper left corner
   *     of the token. It is also clipped so that the overlay can not draw out of the token's
   *     bounding box.
   * @param token The token being painted.
   * @param bounds The bounds of the actual token. This will be different than the clip since the
   *     clip also has to take into account the edge of the window. If you draw based on the clip it
   *     will be off for partial token painting.
   * @param value A value between 0 and 1 inclusive used to paint the bar.
   */
  public abstract void paintOverlay(Graphics2D g, Token token, Rectangle bounds, double value);

  /*---------------------------------------------------------------------------------------------
   * Side enumeration
   *-------------------------------------------------------------------------------------------*/

  /**
   * The side where the bar is painted.
   *
   * @author Jay
   */
  public enum Side {
    /** Draw the bar at the top of the token */
    TOP,

    /** Draw the bar at the bottom of the token */
    BOTTOM,

    /** Draw the bar on the left side of the token */
    LEFT,

    /** Draw the bar on the right side of the token */
    RIGHT;
  }

  // /*---------------------------------------------------------------------------------------------
  // * Class Methods
  // *-------------------------------------------------------------------------------------------*/
  //
  // /**
  // * Scale an asset.
  // *
  // * @param assetId Scale this asset
  // * @param d To fit here.
  // * @return The scaled asset.
  // */
  // public static BufferedImage getScaledImage(MD5Key assetId, Dimension d) {
  // Asset asset = AssetManager.getAsset(assetId);
  // if (asset == null) {
  // LOGGER.warning("Unable to locate and asset with ID: " + assetId);
  // return null;
  // } // endif
  // BufferedImage image = ImageManager.getImageAndWait(asset);
  // Dimension size = new Dimension(image.getWidth(), image.getHeight());
  // SwingUtil.constrainTo(size, d.width, d.height);
  // image = ImageUtil.createCompatibleImage(image, size.width, size.height, null);
  // return image;
  // }
}
