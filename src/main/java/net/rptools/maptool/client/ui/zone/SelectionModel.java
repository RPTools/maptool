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
package net.rptools.maptool.client.ui.zone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;

/** Models the current and historical selections for a ZoneRenderer. */
public class SelectionModel {
  public record SelectionChanged(Zone zone) {}

  /** The zone in which the selections are made. */
  private final Zone zone;

  /** The current selection. */
  private final Set<GUID> currentSelection = new HashSet<>();

  /** A stack of previous selections that can be restored. */
  private final List<Set<GUID>> selectionHistory = new ArrayList<>();

  /** How long {@link #selectionHistory} can get before old entries are forgotten. */
  private int maxHistoryLength = 20;

  /**
   * @param zone The zone in which selections will be managed.
   */
  public SelectionModel(Zone zone) {
    this.zone = zone;
  }

  /**
   * Adds the selection into the history so that it can be restored in the future.
   *
   * <p>In order to avoid storing empty and redundant history, the current selection will only be
   * added to the history if it is not empty and if not equal to the previous item in the history.
   */
  private void pushCurrentSelectionIntoHistory() {
    // don't add empty selections to history
    if (currentSelection.size() == 0) {
      return;
    }
    // Don't add history if nothing has changed.
    if (!selectionHistory.isEmpty() && currentSelection.equals(selectionHistory.get(0))) {
      return;
    }

    Set<GUID> history = new HashSet<>(currentSelection);
    selectionHistory.add(0, history);

    // limit the history to a certain size
    if (selectionHistory.size() > maxHistoryLength) {
      selectionHistory.subList(maxHistoryLength, selectionHistory.size() - 1).clear();
    }
  }

  /** Fire a SelectionChanged event. */
  private void selectionChanged() {
    new MapToolEventBus().getMainEventBus().post(new SelectionChanged(zone));
  }

  /**
   * Get the IDs of the selected tokens.
   *
   * <p>Note: no validation is done to ensure that the token IDs are still valid. It is up to the
   * caller to do that as needed.
   *
   * @return The IDs of all tokens that are current selected.
   */
  public Set<GUID> getSelectedTokenIds() {
    return Collections.unmodifiableSet(currentSelection);
  }

  /**
   * Check if any tokens are selected.
   *
   * @return {@code true} if there is some selected token, otherwise {@code false}.
   */
  public boolean isAnyTokenSelected() {
    return !currentSelection.isEmpty();
  }

  /**
   * Check if a token is selected.
   *
   * @param tokenId The ID of the token to check.
   * @return {@code true} if the token is selected, otherwise {@code false}.
   */
  public boolean isSelected(GUID tokenId) {
    return currentSelection.contains(tokenId);
  }

  /**
   * Check whether a token is eligible to be selected.
   *
   * <p>A token is only eligible for selection if:
   *
   * <ol>
   *   <li>It exists.
   *   <li>The token belongs to the zone that this model is for.
   *   <li>The token is either visible or owned by the current player.
   * </ol>
   *
   * @param tokenGuid The ID of the token to check.
   * @return {@code true} if the token is allowed to be selected, otherwise {@code false}.
   */
  private boolean isSelectable(GUID tokenGuid) {
    if (tokenGuid == null) {
      return false; // doesn't exist
    }
    final var token = zone.getToken(tokenGuid);
    if (token == null) {
      return false; // doesn't exist
    }
    if (!zone.isTokenVisible(token)) {
      return AppUtil.playerOwns(token); // can't own or see
    }
    return true;
  }

  /**
   * Saves the current selection to the history, and replaces the selection.
   *
   * <p>Among {@code tokens}, only selectable tokens will be added.
   *
   * @param tokens The IDs of the tokens to make selected.
   */
  public void replaceSelection(Collection<GUID> tokens) {
    pushCurrentSelectionIntoHistory();

    currentSelection.clear();
    for (GUID tokenGUID : tokens) {
      if (!isSelectable(tokenGUID)) {
        continue;
      }
      currentSelection.add(tokenGUID);
    }

    selectionChanged();
  }

  /**
   * Saves the current selection to the history, and adds tokens to the selection.
   *
   * <p>Among {@code tokens}, only selectable tokens will be added.
   *
   * @param tokens The IDs of the tokens to add to the selection.
   */
  public void addTokensToSelection(Collection<GUID> tokens) {
    pushCurrentSelectionIntoHistory();

    boolean anyAdded = false;
    for (GUID tokenGUID : tokens) {
      if (!isSelectable(tokenGUID)) {
        continue;
      }
      currentSelection.add(tokenGUID);
      anyAdded = true;
    }
    if (anyAdded) {
      selectionChanged();
    }
  }

  /**
   * Saves the current selection to the history, and removes tokens from the selection.
   *
   * @param tokens The IDs of the tokens to remove from the selection.
   */
  public void removeTokensFromSelection(Collection<GUID> tokens) {
    pushCurrentSelectionIntoHistory();
    final var changed = currentSelection.removeAll(tokens);
    if (changed) {
      selectionChanged();
    }
  }

  /**
   * Discard the current selection and replace it with a previous one from the history.
   *
   * <p>The selection will be restored to the most recent non-empty item in the history. Emptiness
   * is considered after discarding any tokens that do not exist or are no longer on the active
   * layer.
   *
   * @param activeLayer The current layer. Tokens not on this layer will not be part of the restored
   *     selection.
   */
  public void undoSelection(Zone.Layer activeLayer) {
    currentSelection.clear();
    while (!selectionHistory.isEmpty()) {
      currentSelection.addAll(selectionHistory.remove(0));

      // The user may have deleted some of the tokens that are contained in the selection history.
      // There could also be tokens in another than the current layer which we don't want to go back
      // to. Find them and filter them otherwise the selection will have orphaned GUIDs.
      for (final var guid : currentSelection) {
        final var token = zone.getToken(guid);
        if (token == null || token.getLayer() != activeLayer) {
          currentSelection.remove(guid);
        }
      }

      // It may be that all tokens weren't available anymore. If so, go further back in the history
      // in the hopes of not landing on an empty result.
      if (!currentSelection.isEmpty()) {
        break;
      }
    }

    selectionChanged();
  }
}
