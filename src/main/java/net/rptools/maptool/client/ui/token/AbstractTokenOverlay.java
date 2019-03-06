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
import java.util.Comparator;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Token;

/**
 * An overlay that may be applied to a token to show state.
 *
 * @author jgorrell
 * @version $Revision: 4531 $ $Date: 2008-08-20 14:15:46 -0500 (Wed, 20 Aug 2008) $ $Author:
 *     coloneldork $
 */
public abstract class AbstractTokenOverlay implements Cloneable {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The name of this overlay. Normally this is the name of a state. */
  protected String name;

  /** Order of the states as displayed on the states menu. */
  private int order;

  /** The group that this token overlay belongs to. It may be <code>null</code>. */
  private String group;

  /** Flag indicating that this token overlay is only displayed on mouseover */
  private boolean mouseover;

  /** The opacity of the painting. Must be a value between 0 & 100 */
  private int opacity = 100;

  /** Flag indicating that this token overlay is displayed to the user. */
  private boolean showGM;

  /** Flag indicating that this token overlay is displayed to the owner. */
  private boolean showOwner;

  /** Flag indicating that this token overlay is displayed to the everybody else. */
  private boolean showOthers;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** A default state name used in default constructors. */
  public static final String DEFAULT_STATE_NAME = "defaultStateName";

  /** This comparator is used to order the states. */
  public static final Comparator<AbstractTokenOverlay> COMPARATOR =
      new Comparator<AbstractTokenOverlay>() {
        public int compare(AbstractTokenOverlay o1, AbstractTokenOverlay o2) {
          return o1.getOrder() - o2.getOrder();
        }
      };

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create an overlay with the passed name.
   *
   * @param aName Name of the new overlay.
   */
  protected AbstractTokenOverlay(String aName) {
    assert aName != null : "A name is required but null was passed.";
    name = aName;
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the name for this AbstractTokenOverlay.
   *
   * @return Returns the current value of name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the value of name for this AbstractTokenOverlay.
   *
   * @param aName The name to set.
   */
  public void setName(String aName) {
    name = aName;
  }

  /** @return Getter for order */
  public int getOrder() {
    return order;
  }

  /** @param order Setter for the order to set */
  public void setOrder(int order) {
    this.order = order;
  }

  /** @return Getter for group */
  public String getGroup() {
    return group;
  }

  /** @param group Setter for group */
  public void setGroup(String group) {
    this.group = group;
  }

  /** @return Getter for mouseover */
  public boolean isMouseover() {
    return mouseover;
  }

  /** @param mouseover Setter for mouseover */
  public void setMouseover(boolean mouseover) {
    this.mouseover = mouseover;
  }

  /** @return Getter for opacity */
  public int getOpacity() {
    if (opacity == 0) opacity = 100;
    return opacity;
  }

  /** @param opacity Setter for opacity */
  public void setOpacity(int opacity) {
    this.opacity = opacity;
  }

  /** @return Getter for showGM */
  public boolean isShowGM() {
    if (!showGM && !showOwner && !showOthers) showGM = showOwner = showOthers = true;
    return showGM;
  }

  /** @param showGM Setter for showGM */
  public void setShowGM(boolean showGM) {
    this.showGM = showGM;
  }

  /** @return Getter for showOwner */
  public boolean isShowOwner() {
    if (!showGM && !showOwner && !showOthers) showGM = showOwner = showOthers = true;
    return showOwner;
  }

  /** @param showOwner Setter for showOwner */
  public void setShowOwner(boolean showOwner) {
    this.showOwner = showOwner;
  }

  /** @return Getter for showOthers */
  public boolean isShowOthers() {
    if (!showGM && !showOwner && !showOthers) showGM = showOwner = showOthers = true;
    return showOthers;
  }

  /** @param showOthers Setter for showOthers */
  public void setShowOthers(boolean showOthers) {
    this.showOthers = showOthers;
  }

  /**
   * Determine if the current overly should be displayed to a player for a given token
   *
   * @param token Check owner of this token
   * @param player Check to see if this player can see this overlay.
   * @return The value <code>true</code> if the passed player can see this overlay on the token.
   */
  public boolean showPlayer(Token token, Player player) {
    if (isShowGM() && player.isGM()) return true;
    boolean owner = token.isOwner(player.getName());
    if (isShowOwner() && owner) return true;
    if (isShowOthers() && !player.isGM() && !owner) return true;
    return false;
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
   * @param value The value for the token state.
   */
  public abstract void paintOverlay(Graphics2D g, Token token, Rectangle bounds, Object value);

  /** @see java.lang.Object#clone() */
  public abstract Object clone();
}
