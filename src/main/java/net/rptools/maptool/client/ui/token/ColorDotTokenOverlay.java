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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.drawing.AbstractTemplate.Quadrant;

/**
 * Token overlay that draws a colored dot in one of the corners.
 *
 * @author giliath
 * @version $Revision$ $Date$ $Author$
 */
public class ColorDotTokenOverlay extends XTokenOverlay {

  /** The corner where the dot is placed */
  private Quadrant corner = Quadrant.SOUTH_EAST;

  /** Default constructor needed for XML encoding/decoding */
  public ColorDotTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.RED, Quadrant.SOUTH_EAST);
  }

  /**
   * Create a new dot token overlay
   *
   * @param aName Name of the token overlay
   * @param aColor Color of the dot
   * @param aCorner Corner containing the dot
   */
  public ColorDotTokenOverlay(String aName, Color aColor, Quadrant aCorner) {
    super(aName, aColor, 0);
    if (aCorner != null) corner = aCorner;
  }

  /** @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone() */
  @Override
  public Object clone() {
    BooleanTokenOverlay overlay = new ColorDotTokenOverlay(getName(), getColor(), getCorner());
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
   *     net.rptools.maptool.model.Token, Rectangle)
   */
  @Override
  public void paintOverlay(Graphics2D g, Token aToken, Rectangle bounds) {
    Color tempColor = g.getColor();
    Stroke tempStroke = g.getStroke();
    Composite tempComposite = g.getComposite();
    try {
      g.setColor(getColor());
      g.setStroke(getStroke());

      double size = bounds.width * 0.2;
      double offset = bounds.width * 0.8;
      double x = 0;
      double y = 0;
      switch (corner) {
        case SOUTH_EAST:
          x = y = offset;
          break;
        case SOUTH_WEST:
          y = offset;
          break;
        case NORTH_EAST:
          x = offset;
          break;
        case NORTH_WEST:
          break;
      } // endswitch
      Shape s = new Ellipse2D.Double(x, y, size, size);
      if (getOpacity() != 100)
        g.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) getOpacity() / 100));
      g.fill(s);
    } finally {
      g.setColor(tempColor);
      g.setStroke(tempStroke);
      g.setComposite(tempComposite);
    }
  }

  /** @return Getter for corner */
  public Quadrant getCorner() {
    return corner;
  }
}
