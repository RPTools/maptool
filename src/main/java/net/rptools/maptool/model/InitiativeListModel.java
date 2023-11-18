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

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.AbstractListModel;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token.Type;

/**
 * This implements a list model for the for the panel. It removes all of the tokens that aren't
 * visible to players if needed.
 *
 * @author Jay
 */
public class InitiativeListModel extends AbstractListModel<TokenInitiative>
    implements PropertyChangeListener {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** All of the tokens for this initiative list. */
  private InitiativeList list;

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the token with the current initiative. Handle GM vs. Player
   *
   * @return The current token displayed to the user or <code>null</code> if there is no current
   *     token. May be different for GM and Player
   */
  public TokenInitiative getCurrentTokenInitiative() {
    if (list.getCurrent() < 0) return null;
    if (MapTool.getFrame().getInitiativePanel().hasGMPermission())
      return list.getTokenInitiative(list.getCurrent());
    TokenInitiative visible = null;
    for (int i = 0; i <= list.getCurrent(); i++) {
      TokenInitiative ti = list.getTokenInitiative(i);
      Token token = ti.getToken();
      if (token != null && isTokenVisible(ti.getToken(), list.isHideNPC())) visible = ti;
    } // endfor
    return visible;
  }

  /**
   * Get the display index for the token at the passed list index
   *
   * @param index The list index of a token;
   * @return The index in the display model or -1 if the item is not displayed.
   */
  public int getDisplayIndex(int index) {
    if (index < 0 || MapTool.getFrame().getInitiativePanel().hasGMPermission()) return index;
    if (!isTokenVisible(list.getToken(index), list.isHideNPC())) return -1;
    int found = -1;
    for (int i = 0; i <= index; i++)
      if (isTokenVisible(list.getToken(i), list.isHideNPC())) found += 1;
    return found;
  }

  /** Called when the underlying tokens have been changed. */
  public void updateModel() {
    fireContentsChanged(this, 0, getSize() - 1);
  }

  /**
   * @return Getter for list
   */
  public InitiativeList getList() {
    return list;
  }

  /**
   * @param theList Setter for the list to set
   */
  public void setList(InitiativeList theList) {

    // Remove the old list
    int oldCount = 0;
    if (list != null) {
      list.removePropertyChangeListener(this);
      oldCount = getSize();
    } // endif

    // Add the new one
    list = theList;
    int newCount = 0;
    if (list != null) {
      list.addPropertyChangeListener(this);
      newCount = getSize();
    } // endif

    // Fire events
    if (oldCount > 0 || newCount > 0) {
      if (oldCount > newCount) {
        fireIntervalRemoved(this, newCount, oldCount - 1);
      } else if (oldCount < newCount) {
        fireIntervalAdded(this, oldCount, newCount - 1);
      } // endif
      fireContentsChanged(this, 0, Math.min(newCount, oldCount) - 1);
    } // endif
  }

  /**
   * Is the passed token displayed in the list?
   *
   * @param token Token being displayed
   * @param hideNPC Flag indicating that NPC's are hidden.
   * @return The value <code>true</code> if this token is shown to the user.
   */
  public static boolean isTokenVisible(Token token, boolean hideNPC) {
    if (token == null) return false;
    if (MapTool.getFrame().getInitiativePanel().hasGMPermission()) return true;
    if (!token.isVisible() || !token.getLayer().isVisibleToPlayers()) return false;
    if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) return false;
    if (hideNPC && token.getType() == Type.NPC) return false;
    return true;
  }

  /*---------------------------------------------------------------------------------------------
   * PropertyChangeEvent Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @SuppressWarnings("unchecked")
  public void propertyChange(PropertyChangeEvent evt) {

    // Handle by property name
    if (evt.getPropertyName().equals(InitiativeList.CURRENT_PROP)) {

      // Change the two old and new token with initiative
      int oldIndex = getDisplayIndex((Integer) evt.getOldValue());
      int newIndex = getDisplayIndex((Integer) evt.getNewValue());
      if (oldIndex != newIndex) {
        if (oldIndex != -1) fireContentsChanged(InitiativeListModel.this, oldIndex, oldIndex);
        if (newIndex != -1) fireContentsChanged(InitiativeListModel.this, newIndex, newIndex);
      } // endif
    } else if (evt.getPropertyName().equals(InitiativeList.TOKENS_PROP)) {
      if (evt instanceof IndexedPropertyChangeEvent) {
        int index = ((IndexedPropertyChangeEvent) evt).getIndex();
        if (evt.getOldValue() == null && evt.getNewValue() instanceof TokenInitiative) {

          // Inserted a token
          if (isTokenVisible(list.getToken(index), list.isHideNPC())) {
            int displayIndex = getDisplayIndex(index);
            fireIntervalAdded(InitiativeListModel.this, displayIndex, displayIndex);
          } // endif
        } else if (evt.getNewValue() == null & evt.getOldValue() instanceof TokenInitiative) {

          // Removed a token
          if (isTokenVisible(((TokenInitiative) evt.getOldValue()).getToken(), list.isHideNPC())) {
            fireIntervalRemoved(InitiativeListModel.this, list.getSize(), list.getSize());
            fireContentsChanged(InitiativeListModel.this, 0, list.getSize() - 1);
          }
        } else {

          // Update a token
          if (isTokenVisible(list.getToken(index), list.isHideNPC())) {
            int displayIndex = getDisplayIndex(index);
            fireContentsChanged(InitiativeListModel.this, displayIndex, displayIndex);
          }
        } // endif
      } else if (evt.getPropertyName().equals(InitiativeList.HIDE_NPCS_PROP)) {

        // Changed visibility of NPC tokens.
        List<TokenInitiative> tokens = list.getTokens();
        int oldSize = getSize(tokens, (Boolean) evt.getOldValue());
        int newSize = getSize(tokens, (Boolean) evt.getNewValue());
        if (oldSize > newSize) {
          fireIntervalRemoved(InitiativeListModel.this, newSize, oldSize - 1);
        } else if (newSize > oldSize) {
          fireIntervalAdded(InitiativeListModel.this, oldSize, newSize - 1);
        } // endif
        fireContentsChanged(InitiativeListModel.this, 0, Math.min(oldSize, newSize));
      } else {

        if (evt.getOldValue() instanceof List
            && evt.getNewValue() instanceof List
            && ((List) evt.getNewValue()).isEmpty()) {

          // Did a clear, delete everything
          List<TokenInitiative> tokens = (List<TokenInitiative>) evt.getOldValue();
          fireIntervalRemoved(InitiativeListModel.this, 0, getSize(tokens, list.isHideNPC()));
        } else if (evt.getOldValue() == null && evt.getNewValue() instanceof List) {

          // Just sorted, update everything
          List<TokenInitiative> tokens = (List<TokenInitiative>) evt.getNewValue();
          fireContentsChanged(InitiativeListModel.this, 0, getSize(tokens, list.isHideNPC()));
        } // endif
      } // endif
    }
  }

  /**
   * Get the number of visible tokens in a list;
   *
   * @param tokens Search for visible tokens in this list.
   * @param hideNPC Should the NPC's be hidden?
   * @return The number of visible tokens.
   */
  private int getSize(List<TokenInitiative> tokens, boolean hideNPC) {
    if (tokens == null || tokens.isEmpty()) return 0;
    int size = 0;
    if (MapTool.getFrame().getInitiativePanel().hasGMPermission()) {
      size = tokens.size();
    } else {
      for (TokenInitiative ti : tokens) if (isTokenVisible(ti.getToken(), hideNPC)) size += 1;
    }
    return size;
  }

  /*---------------------------------------------------------------------------------------------
   * ListModel Interface Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the token initiative at the passed display index. Handle GM vs. Player
   *
   * @see javax.swing.ListModel#getElementAt(int)
   */
  @Override
  public TokenInitiative getElementAt(int index) {
    if (MapTool.getFrame().getInitiativePanel().hasGMPermission())
      return list.getTokenInitiative(index);
    int found = index;
    for (int i = 0; i < list.getSize(); i++) {
      TokenInitiative ti = list.getTokenInitiative(i);
      if (isTokenVisible(ti.getToken(), list.isHideNPC())) {
        found -= 1;
        if (found == -1) return ti;
      }
    }
    return null;
  }

  /**
   * Get the size of the list model, handle GM vs. Player.
   *
   * @see javax.swing.ListModel#getSize()
   */
  @Override
  public int getSize() {
    if (list == null) {
      return 0;
    }

    if (MapTool.getFrame() == null || MapTool.getFrame().getInitiativePanel().hasGMPermission())
      return list.getSize();
    int size = 0;
    for (int i = 0; i < list.getSize(); i++) {
      TokenInitiative ti = list.getTokenInitiative(i);
      if (isTokenVisible(ti.getToken(), list.isHideNPC())) size += 1;
    } // endfor
    return size;
  }
}
