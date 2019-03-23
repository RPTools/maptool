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
import java.awt.geom.Rectangle2D;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This supports token states flowing from one box to the next when multiple states are set on the
 * same token.
 *
 * @author Jay
 */
public class TokenOverlayFlow {

  private static final Logger log = LogManager.getLogger(TokenOverlayFlow.class);

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The number of cells in the X & Y directions on the token. */
  private final int gridSize;

  /**
   * Offsets for the placement of each state in percentage of the token size. They are calculated
   * from the grid size.
   */
  private final double[] offsets;

  /** The size of a cell in percentage of the token size. Calculated from the grid size. */
  private final double size;

  /**
   * This map contains the list of states for each token in the order they are drawn. It's done this
   * way so that the states don't jump around as they are added or removed.
   */
  private final Map<GUID, List<String>> savedStates = new HashMap<GUID, List<String>>();

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /**
   * Flows are shared by multiple token overlay types. This map contains all of the available flow
   * instances.
   */
  private static Map<Integer, TokenOverlayFlow> instances =
      new HashMap<Integer, TokenOverlayFlow>();

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  /**
   * Internal constructor to make sure only one of each grid size is created.
   *
   * @param aGrid The size of the grid placed over the token.
   */
  private TokenOverlayFlow(int aGrid) {
    gridSize = aGrid;
    size = 1.0D / gridSize;
    offsets = new double[gridSize];
    for (int i = 0; i < offsets.length; i++) offsets[i] = i * size;
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Calculate the bounds to paint the passed state. It takes into account states that have already
   * been set w/o changing the order. It also removes any unused states.
   *
   * @param bounds The token's bounds. All states are drawn inside this area.
   * @param token Rendering the states for this token.
   * @param state The state being rendered.
   * @return The bounds used to paint the state.
   */
  public Rectangle2D getStateBounds2D(Rectangle bounds, Token token, String state) {

    // Find the list of states already drawn on the token
    List<String> states = savedStates.get(token.getId());
    if (states == null) {
      states = new LinkedList<String>();
      savedStates.put(token.getId(), states);
    } // endif

    // Find the state in the list, make sure that all the states before it still exist.
    ListIterator<String> i = states.listIterator();
    boolean found = false;
    while (i.hasNext()) {
      String savedState = i.next();
      if (!found && savedState.equals(state)) {
        found = true;
      } else {
        Object stateValue = token.getState(savedState);
        if (stateValue == null) {
          i.remove();
        } else if (stateValue instanceof Boolean) {
          Boolean b = (Boolean) stateValue;
          if (b.booleanValue() == false) i.remove();
        } else if (stateValue instanceof BigDecimal) {
          BigDecimal bd = (BigDecimal) stateValue;
          if (bd.compareTo(BigDecimal.ZERO) == 0) i.remove();
        }
      } // endif
    } // endwhile

    // Find the index of the state, then convert it into row & column
    int index = states.size();
    if (found) {
      index = states.indexOf(state);
    } else {
      states.add(state);
    } // endif
    if (index >= gridSize * gridSize) {
      log.warn(
          "Overlapping states in grid size "
              + gridSize
              + " at "
              + index
              + " on token "
              + token.getName());
      index = index % (gridSize * gridSize);
    } // endif
    int row = gridSize - 1 - (index / gridSize); // Start at bottom
    int col = gridSize - 1 - (index % gridSize); // Start at right

    // Build the rectangle from the passed bounds
    return new Rectangle2D.Double(
        offsets[col] * bounds.width + bounds.x,
        offsets[row] * bounds.height + bounds.y,
        size * bounds.width,
        size * bounds.height);
  }

  /**
   * Calculate the bounds to paint the passed state. It takes into account states that have already
   * been set w/o changing the order. It also removes any unused states.
   *
   * @param bounds The token's bounds. All states are drawn inside this area.
   * @param token Rendering the states for this token.
   * @param state The state being rendered.
   * @return The bounds used to paint the state.
   */
  public Rectangle getStateBounds(Rectangle bounds, Token token, String state) {
    Rectangle2D r = getStateBounds2D(bounds, token, state);
    return new Rectangle(
        (int) Math.round(r.getX()),
        (int) Math.round(r.getY()),
        (int) Math.round(r.getWidth()),
        (int) Math.round(r.getHeight()));
  }

  /*---------------------------------------------------------------------------------------------
   * Class Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the one and only instance of an overlay flow for a particular grid size.
   *
   * @param grid The size of the grid placement for the placement of states.
   * @return The flow for the passed grid size
   */
  public static TokenOverlayFlow getInstance(int grid) {
    Integer key = Integer.valueOf(grid);
    TokenOverlayFlow instance = instances.get(key);
    if (instance == null) {
      instance = new TokenOverlayFlow(grid);
      instances.put(key, instance);
    } // endif
    return instance;
  }
}
