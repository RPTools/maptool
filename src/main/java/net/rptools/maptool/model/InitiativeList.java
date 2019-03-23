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
package net.rptools.maptool.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Icon;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * All of the tokens currently being shown in the initiative list. It includes a reference to all
 * the tokens in order, a reference to the current token, a displayable initiative value and a hold
 * state for each token.
 *
 * @author Jay
 */
public class InitiativeList implements Serializable {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The tokens and their order within the initiative */
  private List<TokenInitiative> tokens = new ArrayList<TokenInitiative>();

  /** The token in the list which currently has initiative. */
  private int current = -1;

  /** The current round for initiative. */
  private int round = -1;

  /** Used to add property change support to the round and current values. */
  private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);

  /** The zone that owns this initiative list. */
  private transient Zone zone;

  /** The id of the zone that owns this initiative list, used for persistence */
  private GUID zoneId;

  /**
   * Hold the update when this variable is greater than 0. Some methods need to call {@link
   * #updateServer()} when they are called, but they also get called by other methods that update
   * the server. This keeps it from happening multiple times.
   */
  private transient int holdUpdate;

  /** Flag indicating that a full update is needed. */
  private boolean fullUpdate;

  /** Hide all of the NPC's from the players. */
  private boolean hideNPC = AppPreferences.getInitHideNpcs();

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Name of the tokens property passed in {@link PropertyChangeEvent}s. */
  public static final String TOKENS_PROP = "tokens";

  /** Name of the round property passed in {@link PropertyChangeEvent}s. */
  public static final String ROUND_PROP = "round";

  /** Name of the current property passed in {@link PropertyChangeEvent}s. */
  public static final String CURRENT_PROP = "current";

  /** Name of the hide NPCs property passed in {@link PropertyChangeEvent}s. */
  public static final String HIDE_NPCS_PROP = "hideNPCs";

  /** Name of the owner permission property passed in {@link PropertyChangeEvent}s. */
  public static final String OWNER_PERMISSIONS_PROP = "ownerPermissions";

  /** Logger for this class */
  private static final Logger LOGGER = LogManager.getLogger(InitiativeList.class);

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create an initiative list for a zone.
   *
   * @param aZone The zone that owns this initiative list.
   */
  public InitiativeList(Zone aZone) {
    setZone(aZone);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the token initiative data at the passed index. Allows the other state to be set.
   *
   * @param index Index of the token initiative data needed.
   * @return The token initiative data for the passed index.
   */
  public TokenInitiative getTokenInitiative(int index) {
    return index < tokens.size() && index >= 0 ? tokens.get(index) : null;
  }

  /**
   * Get the number of tokens in this list.
   *
   * @return Number of tokens
   */
  public int getSize() {
    return tokens.size();
  }

  /**
   * Get the token at the passed index.
   *
   * @param index Index of the token needed.
   * @return The token for the passed index.
   */
  public Token getToken(int index) {
    return index >= 0 && index < tokens.size() ? tokens.get(index).getToken() : null;
  }

  /**
   * Insert a new token into the initiative.
   *
   * @param index Insert the token here.
   * @param token Insert this token.
   * @return The token initiative value that holds the token.
   */
  public TokenInitiative insertToken(int index, Token token) {
    startUnitOfWork();
    TokenInitiative currentInitiative =
        getTokenInitiative(getCurrent()); // Save the currently selected initiative
    if (index == -1) {
      index = tokens.size();
    }
    TokenInitiative ti = new TokenInitiative(token);
    tokens.add(index, ti);
    getPCS().fireIndexedPropertyChange(TOKENS_PROP, index, null, ti);
    setCurrent(indexOf(currentInitiative)); // Restore current initiative
    finishUnitOfWork();
    return ti;
  }

  /**
   * Insert a new token into the initiative.
   *
   * @param tokens Insert these tokens.
   */
  public void insertTokens(List<Token> tokens) {
    startUnitOfWork();
    for (Token token : tokens) insertToken(-1, token);
    finishUnitOfWork();
  }

  /**
   * Find the index of the passed token.
   *
   * @param token Search for this token.
   * @return A list of the indexes found for the listed token
   */
  public List<Integer> indexOf(Token token) {
    List<Integer> list = new ArrayList<Integer>();
    for (int i = 0; i < tokens.size(); i++) if (token.equals(tokens.get(i).getToken())) list.add(i);
    return list;
  }

  /**
   * Find the index of the passed token initiative.
   *
   * @param ti Search for this token initiative instance
   * @return The index of the token initiative that was found or -1 if the token initiative was not
   *     found;
   */
  public int indexOf(TokenInitiative ti) {
    for (int i = 0; i < tokens.size(); i++) if (tokens.get(i).equals(ti)) return i;
    return -1;
  }

  /**
   * Remove a token from the initiative.
   *
   * @param index Remove the token at this index.
   * @return The token that was removed.
   */
  public Token removeToken(int index) {

    // If we are deleting the token with initiative, drop back to the previous token, if we're at
    // the beginning,
    // clear current
    startUnitOfWork();
    TokenInitiative currentInitiative =
        getTokenInitiative(getCurrent()); // Save the currently selected initiative
    int currentInitIndex = indexOf(currentInitiative);
    if (currentInitIndex == index) {
      if (tokens.size() == 1) {
        currentInitiative = null;
      }
      if (index == 0) {
        currentInitiative = getTokenInitiative(1);
      } else {
        currentInitiative = getTokenInitiative(currentInitIndex - 1);
      } // endif
    } // endif

    TokenInitiative ti = tokens.remove(index);
    Token old = ti.getToken();
    getPCS().fireIndexedPropertyChange(TOKENS_PROP, index, ti, null);
    setCurrent(indexOf(currentInitiative)); // Restore current initiative
    finishUnitOfWork();
    return old;
  }

  /** @return Getter for current */
  public int getCurrent() {
    return current;
  }

  /** @param aCurrent Setter for the current to set */
  public void setCurrent(int aCurrent) {
    if (current == aCurrent) return;
    startUnitOfWork();
    if (aCurrent < 0 || aCurrent >= tokens.size()) aCurrent = -1; // Don't allow bad values
    int old = current;
    current = aCurrent;
    getPCS().firePropertyChange(CURRENT_PROP, old, current);
    finishUnitOfWork();
  }

  /** Go to the next token in initiative order. */
  public void nextInitiative() {
    if (tokens.isEmpty()) return;
    startUnitOfWork();
    int newRound = (round < 0) ? 1 : (current + 1 >= tokens.size()) ? round + 1 : round;
    int newCurrent = (current < 0 || current + 1 >= tokens.size()) ? 0 : current + 1;
    setCurrent(newCurrent);
    setRound(newRound);
    finishUnitOfWork();
  }

  /** Go to the previous token in initiative order. */
  public void prevInitiative() {
    if (tokens.isEmpty()) return;
    startUnitOfWork();
    int newRound = (round < 2) ? 1 : (current - 1 < 0) ? round - 1 : round;
    int newCurrent = (current < 1) ? (round < 2 ? 0 : tokens.size() - 1) : current - 1;
    setCurrent(newCurrent);
    setRound(newRound);
    finishUnitOfWork();
  }

  /** @return Getter for round */
  public int getRound() {
    return round;
  }

  /** @param aRound Setter for the round to set */
  public void setRound(int aRound) {
    if (round == aRound) return;
    startUnitOfWork();
    int old = round;
    round = aRound;
    getPCS().firePropertyChange(ROUND_PROP, old, aRound);
    finishUnitOfWork();
  }

  /**
   * Add a listener to any property change.
   *
   * @param listener The listener to be added.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    getPCS().addPropertyChangeListener(listener);
  }

  /**
   * Add a listener to the given property name
   *
   * @param propertyName Add the listener to this property name.
   * @param listener The listener to be added.
   */
  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    getPCS().addPropertyChangeListener(propertyName, listener);
  }

  /**
   * Remove a listener for all property changes.
   *
   * @param listener The listener to be removed.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    getPCS().removePropertyChangeListener(listener);
  }

  /**
   * Remove a listener from a given property name
   *
   * @param propertyName Remove the listener from this property name.
   * @param listener The listener to be removed.
   */
  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    getPCS().removePropertyChangeListener(propertyName, listener);
  }

  /** Start a new unit of work. */
  public void startUnitOfWork() {
    holdUpdate += 1;
    if (holdUpdate == 1) fullUpdate = false;
    LOGGER.debug("startUnitOfWork(): " + holdUpdate + " full: " + fullUpdate);
  }

  /** Finish the current unit of work and update the server. */
  public void finishUnitOfWork() {
    fullUpdate = true;
    finishUnitOfWork(null);
  }

  /**
   * Finish the current unit of work on a single initiative item and update the server.
   *
   * @param ti Only need to update this token initiative.
   */
  public void finishUnitOfWork(TokenInitiative ti) {
    assert holdUpdate > 0 : "Trying to close unit of work when one is not open.";
    holdUpdate -= 1;
    LOGGER.debug(
        "finishUnitOfWork("
            + (ti == null ? "" : ti.getId().toString())
            + "): = "
            + holdUpdate
            + " full: "
            + fullUpdate);
    if (holdUpdate == 0) {
      if (fullUpdate || ti == null) {
        updateServer();
      } else {
        updateServer(ti);
      } // endif
    } // endif
  }

  /** Remove all of the tokens from the model and clear round and current */
  public void clearModel() {
    if (current == -1 && round == -1 && tokens.isEmpty()) return;
    startUnitOfWork();
    setCurrent(-1);
    setRound(-1);
    if (!tokens.isEmpty()) {
      List<TokenInitiative> old = tokens;
      tokens = new ArrayList<TokenInitiative>();
      getPCS().firePropertyChange(TOKENS_PROP, old, tokens);
    } // endif
    finishUnitOfWork();
  }

  /** Updates occurred to the tokens. */
  public void update() {

    // No zone, no tokens
    if (getZone() == null) {
      clearModel();
      return;
    } // endif

    // Remove deleted tokens
    startUnitOfWork();
    boolean updateNeeded = false;
    ListIterator<TokenInitiative> i = tokens.listIterator();
    while (i.hasNext()) {
      TokenInitiative ti = i.next();
      if (getZone().getToken(ti.getId()) == null) {
        int index = tokens.indexOf(ti);
        if (index <= current) setCurrent(current - 1);
        i.remove();
        updateNeeded = true;
        getPCS().fireIndexedPropertyChange(TOKENS_PROP, index, ti, null);
      } // endif
    } // endwhile
    if (updateNeeded) {
      finishUnitOfWork();
    } else if (holdUpdate == 1) {
      holdUpdate -= 1; // Do no updates.
      LOGGER.debug("finishUnitOfWork() - no update");
    } // endif
  }

  /**
   * Sort the tokens by their initiative state from largest to smallest. If the initiative state
   * string can be converted into a {@link Double} that is done first. All values converted to
   * {@link Double}s are always considered bigger than the {@link String} values. The {@link String}
   * values are considered bigger than any <code>null</code> values.
   */
  public void sort() {
    startUnitOfWork();
    TokenInitiative currentInitiative =
        getTokenInitiative(getCurrent()); // Save the currently selected initiative
    Collections.sort(
        tokens,
        new Comparator<TokenInitiative>() {
          public int compare(TokenInitiative o1, TokenInitiative o2) {

            // Get a number, string, or null for first parameter
            Object one = null;
            if (o1.state != null) {
              one = o1.state;
              try {
                one = Double.valueOf(o1.state);
              } catch (NumberFormatException e) {
                // Not a number so ignore
              } // endtry
            } // endif

            // Repeat for second param
            Object two = null;
            if (o2.state != null) {
              two = o2.state;
              try {
                two = Double.valueOf(o2.state);
              } catch (NumberFormatException e) {
                // Not a number so ignore
              } // endtry
            } // endif

            // Do the comparison
            if (one == two || (one != null && one.equals(two))) return 0;
            if (one == null) return 1; // Null is always the smallest value
            if (two == null) return -1;
            if (one instanceof Double & two instanceof Double)
              return ((Double) two).compareTo((Double) one);
            if (one instanceof String & two instanceof String)
              return ((String) two).compareTo((String) one);
            if (one instanceof Double) return -1; // Integers are bigger than strings
            return 1;
          }
        });
    getPCS().firePropertyChange(TOKENS_PROP, null, tokens);
    setCurrent(indexOf(currentInitiative)); // Restore current initiative
    finishUnitOfWork();
  }

  /** @return Getter for zone */
  public Zone getZone() {
    if (zone == null && zoneId != null) zone = MapTool.getCampaign().getZone(zoneId);
    return zone;
  }

  /** @return Getter for pcs */
  private PropertyChangeSupport getPCS() {
    if (pcs == null) pcs = new PropertyChangeSupport(this);
    return pcs;
  }

  /**
   * Move a token from it's current position to the new one.
   *
   * @param oldIndex Move the token at this index
   * @param index To here.
   */
  public void moveToken(int oldIndex, int index) {

    // Bad index, same index, oldIndex->oldindex+1, or moving the last token to the end of the list
    // do nothing.
    if (oldIndex < 0
        || oldIndex == index
        || (oldIndex == tokens.size() - 1 && index == tokens.size())
        || oldIndex == (index - 1)) return;

    // Save the current position, the token moves but the initiative does not.
    TokenInitiative newInitiative = null;
    TokenInitiative currentInitiative =
        getTokenInitiative(getCurrent()); // Save the current initiative
    if (oldIndex == current) {
      newInitiative = getTokenInitiative(oldIndex != 0 ? oldIndex - 1 : 1);
      current = (oldIndex != 0 ? oldIndex - 1 : 1);
    }

    startUnitOfWork();
    current = -1;
    TokenInitiative ti = tokens.remove(oldIndex);
    getPCS().fireIndexedPropertyChange(TOKENS_PROP, oldIndex, ti, null);

    // Add it at it's new position
    index -= index > oldIndex ? 1 : 0;
    tokens.add(index, ti);
    getPCS().fireIndexedPropertyChange(TOKENS_PROP, index, null, ti);

    // Set/restore proper initiative
    if (newInitiative == null) current = indexOf(currentInitiative);
    else setCurrent(indexOf(newInitiative));
    finishUnitOfWork();
  }

  /** Update the server with the new list */
  public void updateServer() {
    if (zoneId == null) return;
    LOGGER.debug("Full update");
    // if (AppPreferences.getInitEnableServerSync())
    MapTool.serverCommand().updateInitiative(this, null);
  }

  /**
   * Update the server with the new Token Initiative
   *
   * @param ti Item to update
   */
  public void updateServer(TokenInitiative ti) {
    if (zoneId == null) return;
    LOGGER.debug("Token Init update: " + ti.getId());
    // if (AppPreferences.getInitEnableServerSync())
    MapTool.serverCommand()
        .updateTokenInitiative(zoneId, ti.getId(), ti.isHolding(), ti.getState(), indexOf(ti));
  }

  /** @param aZone Setter for the zone */
  public void setZone(Zone aZone) {
    zone = aZone;
    if (aZone != null) {
      zoneId = aZone.getId();
    } else {
      zoneId = null;
    } // endif
  }

  /** @return Getter for hideNPC */
  public boolean isHideNPC() {
    return hideNPC;
  }

  /** @param hide Setter for hideNPC */
  public void setHideNPC(boolean hide) {
    if (hide == hideNPC) return;
    startUnitOfWork();
    boolean old = hideNPC;
    hideNPC = hide;
    getPCS().firePropertyChange(HIDE_NPCS_PROP, old, hide);
    finishUnitOfWork();
  }

  /** @return Getter for tokens */
  public List<TokenInitiative> getTokens() {
    return Collections.unmodifiableList(tokens);
  }

  /*---------------------------------------------------------------------------------------------
   * TokenInitiative Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * This class holds all of the data to describe a token w/in initiative.
   *
   * @author Jay
   */
  public class TokenInitiative {

    /*---------------------------------------------------------------------------------------------
     * Instance Variables
     *-------------------------------------------------------------------------------------------*/

    /** The id of the token which is needed for persistence. It is immutable. */
    private GUID id;

    /** Flag indicating that the token is holding it's initiative. */
    private boolean holding;

    /** Optional state that can be displayed in the initiative panel. */
    private String state;

    /** Save off the icon so that it can be displayed as needed. */
    private transient Icon displayIcon;

    /*---------------------------------------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------------------------------------*/

    /**
     * Create the token initiative for the passed token.
     *
     * @param aToken Add this token to the initiative.
     */
    public TokenInitiative(Token aToken) {
      if (aToken != null) id = aToken.getId();
    }

    /*---------------------------------------------------------------------------------------------
     * Instance Methods
     *-------------------------------------------------------------------------------------------*/

    /** @return Getter for token */
    public Token getToken() {
      return getZone().getToken(id);
    }

    /** @return Getter for id */
    public GUID getId() {
      return id;
    }

    /** @param id Setter for the id to set */
    public void setId(GUID id) {
      this.id = id;
    }

    /** @return Getter for holding */
    public boolean isHolding() {
      return holding;
    }

    /** @param isHolding Setter for the holding to set */
    public void setHolding(boolean isHolding) {
      if (holding == isHolding) return;
      startUnitOfWork();
      boolean old = holding;
      holding = isHolding;
      getPCS().fireIndexedPropertyChange(TOKENS_PROP, tokens.indexOf(this), old, isHolding);
      finishUnitOfWork(this);
    }

    /** @return Getter for state */
    public String getState() {
      return state;
    }

    /** @param aState Setter for the state to set */
    public void setState(String aState) {
      if (state == aState || (state != null && state.equals(aState))) return;
      startUnitOfWork();
      String old = state;
      state = aState;
      getPCS().fireIndexedPropertyChange(TOKENS_PROP, tokens.indexOf(this), old, aState);
      finishUnitOfWork(this);
    }

    /** @return Getter for displayIcon */
    public Icon getDisplayIcon() {
      return displayIcon;
    }

    /** @param displayIcon Setter for the displayIcon to set */
    public void setDisplayIcon(Icon displayIcon) {
      this.displayIcon = displayIcon;
    }

    /**
     * Update the internal state w/o firing events. Needed for single token init updates.
     *
     * @param isHolding New holding state
     * @param aState New state
     */
    public void update(boolean isHolding, String aState) {
      boolean old = holding;
      holding = isHolding;
      String oldState = state;
      state = aState;
      getPCS().fireIndexedPropertyChange(TOKENS_PROP, tokens.indexOf(this), old, isHolding);
      getPCS().fireIndexedPropertyChange(TOKENS_PROP, tokens.indexOf(this), oldState, aState);
    }
  }
}
