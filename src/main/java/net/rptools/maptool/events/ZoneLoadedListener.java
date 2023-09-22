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
package net.rptools.maptool.events;

import static net.rptools.maptool.client.functions.MapFunctions.ON_CHANGE_MAP_CALLBACK;

import com.google.common.eventbus.Subscribe;
import java.util.Collections;
import net.rptools.maptool.client.events.ZoneLoaded;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.EventMacroUtil;

public class ZoneLoadedListener {

  public ZoneLoadedListener() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  public void OnChangedMap(ZoneLoaded event) {
    var libTokens = EventMacroUtil.getEventMacroTokens(ON_CHANGE_MAP_CALLBACK);
    String prefix = ON_CHANGE_MAP_CALLBACK + "@";

    for (Token handler : libTokens) {
      EventMacroUtil.callEventHandlerOld(
          prefix + handler.getName(), "", handler, Collections.emptyMap(), true);
    }
  }
}
