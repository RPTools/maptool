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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import net.rptools.maptool.model.Token;

/**
 * Paint a square so that it doesn't overlay any other states being displayed in the same grid.
 *
 * @author Jay
 */
public class FlowYieldTokenOverlay extends FlowColorDotTokenOverlay {

  /** Default constructor needed for XML encoding/decoding */
  public FlowYieldTokenOverlay() {
    this(BooleanTokenOverlay.DEFAULT_STATE_NAME, Color.RED, -1);
  }

  /**
   * Create a new dot token overlay
   *
   * @param aName Name of the token overlay
   * @param aColor Color of the dot
   * @param aGrid Size of the overlay grid for this state. All states with the same grid size share
   *     the same overlay.
   */
  public FlowYieldTokenOverlay(String aName, Color aColor, int aGrid) {
    super(aName, aColor, aGrid);
  }

  /** @see net.rptools.maptool.client.ui.token.BooleanTokenOverlay#clone() */
  @Override
  public Object clone() {
    BooleanTokenOverlay overlay = new FlowYieldTokenOverlay(getName(), getColor(), getGrid());
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
   * @see net.rptools.maptool.client.ui.token.FlowColorDotTokenOverlay#getShape(java.awt.Rectangle,
   *     net.rptools.maptool.model.Token)
   */
  @Override
  protected Shape getShape(Rectangle bounds, Token token) {
    Rectangle2D r = getFlow().getStateBounds2D(bounds, token, getName());
    GeneralPath p = new GeneralPath();
    p.moveTo((float) r.getX(), (float) r.getY());
    p.lineTo((float) r.getCenterX(), (float) r.getMaxY());
    p.lineTo((float) r.getMaxX(), (float) r.getY());
    p.lineTo((float) r.getX(), (float) r.getY());
    p.closePath();
    return p;
  }
}
