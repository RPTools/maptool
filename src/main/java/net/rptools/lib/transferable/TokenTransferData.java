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
package net.rptools.lib.transferable;

import java.awt.Point;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import net.rptools.lib.MD5Key;

/**
 * Class used to transfer token information between applications. Used in Drag & Drop. Some
 * properties are shared between applications, and some are specific. Those specific properties are
 * stored in the map with a key that indicates what app owns that data.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
public class TokenTransferData extends HashMap<String, Object> implements Serializable {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Name of the token. */
  private String name;

  /** The image used to display the token. An image icon is used because it is serializable */
  private ImageIcon token;

  /** The players that own this token. When <code>null</code> there are no owners */
  private Set<String> players;

  /** Flag indicating if this token is visible to players */
  private boolean isVisible;

  /** Location of the token on the map. These may be cell coordinates or map coordinates */
  private Point location;

  /** The facing of the token on the map. A <code>null</code> value indicates no facing */
  private Integer facing;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Prefix for all values that are used by map tool */
  public static final String MAPTOOL = "maptool:";

  /**
   * Maptool's token id key. The value is an <code>String</code> that can be used to create a <code>
   * GUID</code>
   */
  public static final String ID = MAPTOOL + "id";

  /** Maptool's Z-order key. The value is an {@link MD5Key} used to identify an asset. */
  public static final String ASSET_ID = MAPTOOL + "assetId";

  /** Maptool's Z-order key. The value is an <code>Integer</code>. */
  public static final String Z = MAPTOOL + "z";

  /** Maptool's snap to scale key. The value is a <code>Boolean</code>. */
  public static final String SNAP_TO_SCALE = MAPTOOL + "snapToScale";

  /** Maptool's token width key. The value is an <code>Integer</code>. */
  public static final String WIDTH = MAPTOOL + "width";

  /** Maptool's token height key. The value is an <code>Integer</code>. */
  public static final String HEIGHT = MAPTOOL + "height";

  /**
   * Maptool's snap to grid key. Tells if x,y are cell or zone coordinates. The value is a <code>
   * Boolean</code>.
   */
  public static final String SNAP_TO_GRID = MAPTOOL + "snapToGrid";

  /**
   * Maptool's owned by all or just by list key. The value is an <code>Integer</code>. The value 0
   * means that the token is owned by all, the value 1 indicates that the owners are specified in
   * the <code>OWNER_LIST</code> property.
   */
  public static final String OWNER_TYPE = MAPTOOL + "ownerType";

  /** The setting of the token's VisibleOnlyToOwner flag. */
  public static final String VISIBLE_OWNER_ONLY = MAPTOOL + "visibleOnlyToOwner";

  /**
   * Maptool's type of token used by facing or stamping key. The value is a <code>String</code>
   * containing the name of a <code>Type</code> enumeration value.
   */
  public static final String TOKEN_TYPE = MAPTOOL + "tokenType";

  /** Maptool's notes for all key. The value is a <code>String</code>. */
  public static final String NOTES = MAPTOOL + "notes";

  /** Maptool's notes for GM key. The value is a <code>String</code>. */
  public static final String GM_NOTES = MAPTOOL + "gmNotes";

  /** Maptool's name for GM key. The value is a <code>String</code>. */
  public static final String GM_NAME = MAPTOOL + "gmName";

  /** Maptool's name for the portrait. The value is an {@link ImageIcon}. */
  public static final String PORTRAIT = MAPTOOL + "portrait";

  /** Maptool's name for the portrait. The value is an {@link Map}<code><String, String></code>. */
  public static final String MACROS = MAPTOOL + "macros";

  /** Serial version id to hide changes during transfer */
  private static final long serialVersionUID = -1838917777325573062L;

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /** @return Getter for isVisible */
  public boolean isVisible() {
    return isVisible;
  }

  /** @param aIsVisible Setter for isVisible */
  public void setVisible(boolean aIsVisible) {
    isVisible = aIsVisible;
  }

  /** @return Getter for name */
  public String getName() {
    return name;
  }

  /** @param aName Setter for name */
  public void setName(String aName) {
    name = aName;
  }

  /** @return Getter for players */
  public Set<String> getPlayers() {
    return players;
  }

  /** @param aPlayers Setter for players */
  public void setPlayers(Set<String> aPlayers) {
    players = aPlayers;
  }

  /** @return Getter for token */
  public ImageIcon getToken() {
    return token;
  }

  /** @param aToken Setter for token */
  public void setToken(ImageIcon aToken) {
    token = aToken;
  }

  /** @return Getter for facing */
  public Integer getFacing() {
    return facing;
  }

  /** @param aFacing Setter for facing */
  public void setFacing(Integer aFacing) {
    facing = aFacing;
  }

  /** @return Getter for location */
  public Point getLocation() {
    return location;
  }

  /** @param aLocation Setter for location */
  public void setLocation(Point aLocation) {
    location = aLocation;
  }
}
