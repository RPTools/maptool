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
 * Place a Triangle (triangle point down) over a token.
 *
 * @author pwright
 * @version $Revision$ $Date$ $Author$
 */
public class TriangleTokenOverlay extends XTokenOverlay {

  /** Default constructor needed for XML encoding/decoding */
  public TriangleTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.MAGENTA, 5);
  }

  /**
   * Create a Triangle token overlay with the given name.
   *
   * @param aName Name of this token overlay.
   * @param aColor The color of this token overlay.
   * @param aWidth The width of the lines in this token overlay.
   */
  public TriangleTokenOverlay(String aName, Color aColor, int aWidth) {
    super(aName, aColor, aWidth);
  }

  /** @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone() */
  @Override
  public Object clone() {
    BooleanTokenOverlay overlay = new TriangleTokenOverlay(getName(), getColor(), getWidth());
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
    Double hc = (double) bounds.width / 2;
    Double vc = bounds.height * 0.866;
    Color tempColor = g.getColor();
    g.setColor(getColor());
    Stroke tempStroke = g.getStroke();
    g.setStroke(getStroke());
    Composite tempComposite = g.getComposite();
    if (getOpacity() != 100)
      g.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
    g.draw(new Line2D.Double(0, vc, bounds.width, vc));
    g.draw(new Line2D.Double(bounds.width, vc, hc, 0));
    g.draw(new Line2D.Double(hc, 0, 0, vc));
    g.setColor(tempColor);
    g.setStroke(tempStroke);
    g.setComposite(tempComposite);
  }
}
