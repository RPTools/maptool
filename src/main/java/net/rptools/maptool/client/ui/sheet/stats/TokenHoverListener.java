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
package net.rptools.maptool.client.ui.sheet.stats;

import com.google.common.eventbus.Subscribe;
import net.rptools.maptool.client.events.StartHoverOverToken;
import net.rptools.maptool.client.events.StopHoverOverToken;

public class TokenHoverListener {

  @Subscribe
  public void onTokenHover(StartHoverOverToken event) {
    System.out.println("TokenHoverListener.onTokenHover");
  }

  @Subscribe
  public void onTokenHover(StopHoverOverToken event) {
    System.out.println("TokenHoverListener.onTokenHover");
  }
}
