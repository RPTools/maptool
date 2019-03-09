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
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import net.rptools.maptool.model.Token;

/**
 * Place a cross over a token.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
public class CrossTokenOverlay extends XTokenOverlay {

  /** Default constructor needed for XML encoding/decoding */
  public CrossTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.RED, 5);
  }

  /**
   * Create a Cross token overlay with the given name.
   *
   * @param aName Name of this token overlay.
   * @param aColor The color of this token overlay.
   * @param aWidth The width of the lines in this token overlay.
   */
  public CrossTokenOverlay(String aName, Color aColor, int aWidth) {
    super(aName, aColor, aWidth);
  }

  /** @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone() */
  @Override
  public Object clone() {
    BooleanTokenOverlay overlay = new CrossTokenOverlay(getName(), getColor(), getWidth());
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
   * @see net.rptools.maptool.client.ui.token.XTokenOverlay#paintOverlay(java.awt.Graphics2D,
   *     net.rptools.maptool.model.Token, java.awt.Rectangle)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token aToken, Rectangle bounds) {
    Color tempColor = g.getColor();
    g.setColor(getColor());
    Stroke tempStroke = g.getStroke();
    g.setStroke(getStroke());
    Composite tempComposite = g.getComposite();
    if (getOpacity() != 100)
      g.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
    g.draw(
        new Line2D.Double(0, (double) bounds.height / 2, bounds.width, (double) bounds.height / 2));
    g.draw(
        new Line2D.Double((double) bounds.width / 2, 0, (double) bounds.width / 2, bounds.height));
    g.setColor(tempColor);
    g.setStroke(tempStroke);
    g.setComposite(tempComposite);
  }
}
