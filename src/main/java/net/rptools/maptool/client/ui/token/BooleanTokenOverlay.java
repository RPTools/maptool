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
import java.awt.Graphics2D;
import java.awt.Rectangle;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;

/**
 * An overlay that may be applied to a token to show state.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public abstract class BooleanTokenOverlay extends AbstractTokenOverlay {

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create an overlay with the passed name.
   *
   * @param aName Name of the new overlay.
   */
  protected BooleanTokenOverlay(String aName) {
    super(aName);
  }

  /*---------------------------------------------------------------------------------------------
   * AbstractTokenOverlay Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see net.rptools.maptool.client.ui.token.AbstractTokenOverlay#paintOverlay(java.awt.Graphics2D,
   *     net.rptools.maptool.model.Token, java.awt.Rectangle, java.lang.Object)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token token, Rectangle bounds, Object value) {
    if (FunctionUtil.getBooleanValue(value)) {
      // Apply Alpha Transparency
      float opacity = token.getTokenOpacity();
      if (opacity < 1.0f)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));

      paintOverlay(g, token, bounds);
    }
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
   */
  public abstract void paintOverlay(Graphics2D g, Token token, Rectangle bounds);
}
