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

import com.google.gson.JsonObject;
import com.google.protobuf.StringValue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.swing.Icon;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.server.proto.InitiativeListDto;
import net.rptools.maptool.server.proto.TokenInitiativeDto;
import net.rptools.maptool.util.EventMacroUtil;
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

  private static final Logger LOGGER = LogManager.getLogger(EventMacroUtil.class);

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

  /** "callback" name used for the onInitiativeChangeRequest event */
  public static final String ON_INITIATIVE_CHANGE_VETOABLE_MACRO_CALLBACK =
      "onInitiativeChangeRequest";

  /** variable to test for initiative change denial */
  public static final String ON_INITIATIVE_CHANGE_DENY_VARIABLE = "init.denyChange";

  /** "callback" name used for the onInitiativeChange (non-vetoable) event */
  public static final String ON_INITIATIVE_CHANGE_COMMIT_MACRO_CALLBACK = "onInitiativeChange";

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  private InitiativeList() {}

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
    if (index < tokens.size() && index >= 0) {
      return tokens.get(index);
    } else {
      LOGGER.error("getTokenInitiative: index = " + index + ", size = " + tokens.size());
      return null;
    }
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

  public Token getCurrentToken() {
    int currentIndex = getCurrent();
    return currentIndex < 0 ? null : getToken(currentIndex);
  }

  public TokenInitiative getCurrentTokenInitiative() {
    int currentIndex = getCurrent();
    return currentIndex < 0 ? null : getTokenInitiative(currentIndex);
  }

  /**
   * @return Getter for current
   */
  public int getCurrent() {
    return current;
  }

  /**
   * @param aCurrent Setter for the current to set
   */
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
    // if nothing has changed, there's no need to fire the macro events
    boolean fireEvents = (newRound != round || newCurrent != current);
    // confirm via onInitiativeChangeRequest
    boolean changeIsVetoed =
        fireEvents
            && callForInitiativeChangeVetoes(
                current, newCurrent, round, newRound, InitiativeChangeDirection.NEXT);
    if (!changeIsVetoed) {
      if (fireEvents) {
        // notify via onInitiativeChange
        handleInitiativeChangeCommitMacroEvent(
            current, newCurrent, round, newRound, InitiativeChangeDirection.NEXT);
      }
      setCurrent(newCurrent);
      setRound(newRound);
    }
    finishUnitOfWork();
  }

  /** Go to the previous token in initiative order. */
  public void prevInitiative() {
    if (tokens.isEmpty()) return;
    startUnitOfWork();
    int newRound = (round < 2) ? 1 : (current - 1 < 0) ? round - 1 : round;
    int newCurrent = (current < 1) ? (round < 2 ? 0 : tokens.size() - 1) : current - 1;
    // if nothing has changed, there's no need to fire the macro events
    boolean fireEvents = (newRound != round || newCurrent != current);
    // confirm via onInitiativeChangeRequest
    boolean changeIsVetoed =
        fireEvents
            && callForInitiativeChangeVetoes(
                current, newCurrent, round, newRound, InitiativeChangeDirection.PREVIOUS);
    if (!changeIsVetoed) {
      if (fireEvents) {
        // notify via onInitiativeChange
        handleInitiativeChangeCommitMacroEvent(
            current, newCurrent, round, newRound, InitiativeChangeDirection.PREVIOUS);
      }
      setCurrent(newCurrent);
      setRound(newRound);
    }
    finishUnitOfWork();
  }

  /**
   * @return Getter for round
   */
  public int getRound() {
    return round;
  }

  /**
   * @param aRound Setter for the round to set
   */
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
    // update the server if any actual change was made, otherwise just decrement holdUpdate
    if (updateNeeded) {
      finishUnitOfWork();
    } else {
      holdUpdate -= 1; // Do no updates.
      LOGGER.debug("finishUnitOfWork() - no update");
    } // endif
  }

  /**
   * Sort the tokens by their initiative state according to the default, descending order. See
   * {@link #sort(boolean)} for more details on handling of strings and nulls.
   */
  public void sort() {
    this.sort(false);
  }

  /**
   * Sort the tokens by their initiative state, in either ascending or descending order. If the
   * initiative state string can be converted into a {@link Double} that is done first. All values
   * converted to {@link Double}s are always considered bigger than the {@link String} values. The
   * {@link String} values are considered bigger than any <code>null</code> values.
   */
  public void sort(boolean ascendingOrder) {
    startUnitOfWork();
    final int DIRECTION = ascendingOrder ? -1 : 1;
    TokenInitiative currentInitiative =
        getTokenInitiative(getCurrent()); // Save the currently selected initiative
    tokens.sort(
        (o1, o2) -> {

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
          if (Objects.equals(one, two)) return 0;
          if (one == null) return 1 * DIRECTION; // Null is always the smallest value
          if (two == null) return -1 * DIRECTION;
          if (one instanceof Double & two instanceof Double)
            return ((Double) two).compareTo((Double) one) * DIRECTION;
          if (one instanceof String & two instanceof String)
            return ((String) two).compareTo((String) one) * DIRECTION;
          if (one instanceof Double) return -1 * DIRECTION; // Integers are bigger than strings
          return 1 * DIRECTION;
        });
    getPCS().firePropertyChange(TOKENS_PROP, null, tokens);
    setCurrent(indexOf(currentInitiative)); // Restore current initiative
    finishUnitOfWork();
  }

  /**
   * @return Getter for zone
   */
  public Zone getZone() {
    if (zone == null && zoneId != null) zone = MapTool.getCampaign().getZone(zoneId);
    return zone;
  }

  /**
   * @return Getter for pcs
   */
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

  /**
   * @param aZone Setter for the zone
   */
  public void setZone(Zone aZone) {
    zone = aZone;
    if (aZone != null) {
      zoneId = aZone.getId();
    } else {
      zoneId = null;
    } // endif
  }

  /**
   * @return Getter for hideNPC
   */
  public boolean isHideNPC() {
    return hideNPC;
  }

  /**
   * @param hide Setter for hideNPC
   */
  public void setHideNPC(boolean hide) {
    if (hide == hideNPC) return;
    startUnitOfWork();
    boolean old = hideNPC;
    hideNPC = hide;
    getPCS().firePropertyChange(HIDE_NPCS_PROP, old, hide);
    finishUnitOfWork();
  }

  /**
   * @return Getter for tokens
   */
  public List<TokenInitiative> getTokens() {
    return Collections.unmodifiableList(tokens);
  }

  /**
   * Handle the {@value #ON_INITIATIVE_CHANGE_VETOABLE_MACRO_CALLBACK} macro event, if any handlers
   * are present. Passes in some relevant info to each qualifying lib:token macro identified, and
   * checks to see whether ANY of the consulted handlers have vetoed the change.
   *
   * <p>Note: This is designed NOT to short-circuit. All handlers will be invoked, even if earlier
   * handlers have already vetoed.
   *
   * <p>If errors are encountered while executing any particular handler, that handler will be
   * considered to have allowed the change to proceed.
   *
   * @param oldOffset the offset prior to the pending change
   * @param newOffset the offset desired
   * @param oldRound the round prior to the pending change
   * @param newRound the round desired
   * @param direction whether the change was via next, previous, etc.
   * @return true if the change should be prevented, false otherwise
   */
  public boolean callForInitiativeChangeVetoes(
      int oldOffset,
      int newOffset,
      int oldRound,
      int newRound,
      InitiativeChangeDirection direction) {
    try {
      var libs =
          new LibraryManager()
              .getLegacyEventTargets(ON_INITIATIVE_CHANGE_VETOABLE_MACRO_CALLBACK)
              .get();
      boolean isVetoed = false;
      if (!libs.isEmpty()) {
        JsonObject args = new JsonObject();
        args.add("old", getInfoForOffset(oldOffset, oldRound));
        args.add("new", getInfoForOffset(newOffset, newRound));
        args.addProperty("direction", direction.toString());
        String argStr = args.toString();
        for (Library handler : libs) {
          try {
            String libraryNamespace = handler.getNamespace().get();
            boolean thisVote =
                EventMacroUtil.pollEventHandlerForVeto(
                    ON_INITIATIVE_CHANGE_VETOABLE_MACRO_CALLBACK,
                    libraryNamespace,
                    argStr,
                    null,
                    ON_INITIATIVE_CHANGE_DENY_VARIABLE,
                    Collections.emptyMap());
            isVetoed = isVetoed || thisVote;
          } catch (InterruptedException | ExecutionException e) {
            // Should not be possible
            throw new AssertionError("Error retrieving library namespace");
          }
        }
      }
      return isVetoed;
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(I18N.getText("library.error.retrievingEventHandler"), e);
      return false; // if we completely fail we should never prevent the change of initiative.
    }
  }

  /**
   * Handle the {@value #ON_INITIATIVE_CHANGE_COMMIT_MACRO_CALLBACK} macro event, if any handlers
   * are present. Passes in some relevant info to each qualifying lib:token macro identified.
   *
   * @param oldOffset the offset prior to the approved change
   * @param newOffset the resulting offset
   * @param oldRound the round prior to the approved change
   * @param newRound the resulting round
   * @param direction whether the change was via next, previous, etc.
   */
  public void handleInitiativeChangeCommitMacroEvent(
      int oldOffset,
      int newOffset,
      int oldRound,
      int newRound,
      InitiativeChangeDirection direction) {
    try {
      var libs =
          new LibraryManager()
              .getLegacyEventTargets(ON_INITIATIVE_CHANGE_COMMIT_MACRO_CALLBACK)
              .get();
      if (!libs.isEmpty()) {
        JsonObject args = new JsonObject();
        args.add("old", getInfoForOffset(oldOffset, oldRound));
        args.add("new", getInfoForOffset(newOffset, newRound));
        args.addProperty("direction", direction.toString());
        String argStr = args.toString();
        String prefix = ON_INITIATIVE_CHANGE_COMMIT_MACRO_CALLBACK + "@";
        for (Library handler : libs) {
          try {
            String libraryNamespace = handler.getNamespace().get();
            EventMacroUtil.callEventHandler(
                ON_INITIATIVE_CHANGE_COMMIT_MACRO_CALLBACK,
                libraryNamespace,
                argStr,
                null,
                Collections.emptyMap());
          } catch (InterruptedException | ExecutionException e) {
            // Should not be possible
            throw new AssertionError("Error retrieving library namespace");
          }
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(I18N.getText("library.error.retrievingEventHandler"), e);
    }
  }

  /**
   * Get a JsonObject describing the give initiative slot - includes offset, state, and token
   * information.
   *
   * <p>Because this output is intended for use with initiative event macros, nulls (for tokens,
   * initiative values, etc) are avoided in favor of empty strings.
   *
   * @param offset the offset from which to derive information
   * @param round the round number to include in the output
   * @return a JsonObject containing information about the the given initiative slot
   */
  private JsonObject getInfoForOffset(int offset, int round) {
    JsonObject info = new JsonObject();
    info.addProperty("round", round);
    info.addProperty("offset", offset);
    TokenInitiative theTI = getTokenInitiative(offset);
    String stateStr = (theTI != null) ? theTI.getState() : "";
    if (stateStr == null) stateStr = "";
    info.addProperty("initiative", stateStr);
    boolean isHolding = (theTI != null) ? theTI.isHolding() : false;
    info.addProperty("holding", isHolding ? 1 : 0);
    info.addProperty("token", offset != -1 ? getToken(offset).getId().toString() : "");
    return info;
  }

  public static InitiativeList fromDto(InitiativeListDto dto) {
    var initiativeList = new InitiativeList();
    initiativeList.current = dto.getCurrent();
    initiativeList.round = dto.getRound();
    initiativeList.zoneId = GUID.valueOf(dto.getZoneId());
    initiativeList.hideNPC = dto.getHideNpc();
    initiativeList.tokens =
        dto.getTokensList().stream()
            .map(t -> initiativeList.fromDto(t))
            .collect(Collectors.toList());
    return initiativeList;
  }

  public InitiativeListDto toDto() {
    var dto = InitiativeListDto.newBuilder();
    dto.setCurrent(current);
    dto.setRound(round);
    dto.setZoneId(zoneId.toString());
    dto.setHideNpc(hideNPC);
    dto.addAllTokens(tokens.stream().map(t -> t.toDto()).collect(Collectors.toList()));
    return dto.build();
  }

  public TokenInitiative fromDto(TokenInitiativeDto dto) {
    var initiative = new TokenInitiative();
    initiative.holding = dto.getHolding();
    initiative.id = GUID.valueOf(dto.getTokenId());
    initiative.state = dto.hasState() ? dto.getState().getValue() : null;
    return initiative;
  }

  /** Types of initiative changes - intended for use with initiative event macros. */
  public enum InitiativeChangeDirection {
    /** The initiative is advancing normally from the current initiative to the next */
    NEXT,
    /** The initiative is reverting back to the slot/token that last had initiative. */
    PREVIOUS,
    /** Initiative has been directly set to a specific slot without regard for sequence. */
    ARBITRARY;
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

    /**
     * Need to remember whether the displayIcon was shaded (to indicate a hidden token) so we can
     * update when needed
     */
    private transient boolean tokenVisibleWhenIconUpdated = false;

    /*---------------------------------------------------------------------------------------------
     * Constructors
     *-------------------------------------------------------------------------------------------*/

    private TokenInitiative() {}

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

    /**
     * @return Getter for token
     */
    public Token getToken() {
      return getZone().getToken(id);
    }

    /**
     * @return Getter for id
     */
    public GUID getId() {
      return id;
    }

    /**
     * @param id Setter for the id to set
     */
    public void setId(GUID id) {
      this.id = id;
    }

    /**
     * @return Getter for holding
     */
    public boolean isHolding() {
      return holding;
    }

    /**
     * Set the hold of an initiative, and update the server.
     *
     * @param isHolding the holding to set
     */
    public void setHolding(boolean isHolding) {
      if (holding == isHolding) return;
      startUnitOfWork();
      boolean old = holding;
      holding = isHolding;
      getPCS().fireIndexedPropertyChange(TOKENS_PROP, tokens.indexOf(this), old, isHolding);
      finishUnitOfWork(this);
    }

    /**
     * @return Getter for state
     */
    public String getState() {
      return state;
    }

    /**
     * Set the state of an initiative, and update the server.
     *
     * @param aState state to set
     */
    public void setState(String aState) {
      if (Objects.equals(state, aState)) return;
      startUnitOfWork();
      String old = state;
      state = aState;
      getPCS().fireIndexedPropertyChange(TOKENS_PROP, tokens.indexOf(this), old, aState);
      finishUnitOfWork(this);
    }

    /**
     * @return Getter for displayIcon
     */
    public Icon getDisplayIcon() {
      return displayIcon;
    }

    /**
     * NOTE: Be sure to also call {@link#setTokenVisibleWhenIconUpdated(boolean)}
     *
     * @param displayIcon Setter for the displayIcon to set.
     */
    public void setDisplayIcon(Icon displayIcon) {
      this.displayIcon = displayIcon;
    }

    /**
     * Checks whether the cached icon for this {@link TokenInitiative} was generated with the alpha
     * shading - indicating whether the token was visible when the icon was last refreshed.
     *
     * @return true if the token was visible (and the icon was therefore opaque), false otherwise
     */
    public boolean wasTokenVisibleWhenIconUpdated() {
      return tokenVisibleWhenIconUpdated;
    }

    /**
     * Remember whether the generated icon was shaded (to indicate a non-visible token), so it can
     * be refreshed if needed.
     *
     * @param tokenVisibleWhenIconUpdated true to indicate that the token was visible, false
     *     otherwise
     */
    public void setTokenVisibleWhenIconUpdated(boolean tokenVisibleWhenIconUpdated) {
      this.tokenVisibleWhenIconUpdated = tokenVisibleWhenIconUpdated;
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

    public TokenInitiativeDto toDto() {
      var dto = TokenInitiativeDto.newBuilder();
      dto.setHolding(holding);
      dto.setTokenId(id.toString());
      if (state != null) {
        dto.setState(StringValue.of(state));
      }
      return dto.build();
    }
  }
}
