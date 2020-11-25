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

import net.rptools.maptool.client.AppState;

/** Represents the local player. Its methods can depend on AppState and other local properties. */
public class LocalPlayer extends Player {
  public LocalPlayer(String name, Role role, String password) {
    super(name, role, password);
  }

  /** @return the effective role of the local player, taking into account Show As Player. */
  public Role getEffectiveRole() {
    if (isGM() && AppState.isShowAsPlayer()) {
      return Role.PLAYER;
    } else {
      return getRole();
    }
  }

  /** @return whether the player is a GM using GM view. */
  public boolean isEffectiveGM() {
    return isGM() && !AppState.isShowAsPlayer();
  }
}
