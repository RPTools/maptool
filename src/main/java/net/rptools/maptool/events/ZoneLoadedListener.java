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

import com.google.common.eventbus.Subscribe;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.ZoneLoaded;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.util.EventMacroUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ZoneLoadedListener {
  private static final Logger LOGGER = LogManager.getLogger(EventMacroUtil.class);
  public static final String ON_CHANGE_MAP_CALLBACK = "onChangeMap";

  public ZoneLoadedListener() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  public void OnChangedMap(ZoneLoaded event) {
    ZoneRenderer currentZR = MapTool.getFrame().getCurrentZoneRenderer();
    try {
      var libs = new LibraryManager().getLegacyEventTargets(ON_CHANGE_MAP_CALLBACK).get();
      if (libs.isEmpty()) {
        return;
      }
      for (Library handler : libs) {
        try {
          String libraryNamespace = handler.getNamespace().get();
          EventMacroUtil.callEventHandler(
              ON_CHANGE_MAP_CALLBACK,
              libraryNamespace,
              currentZR.getZone().getId().toString(),
              null,
              Collections.emptyMap());
        } catch (InterruptedException | ExecutionException e) {
          LOGGER.error(I18N.getText("library.error.notFound"), e);
          throw new AssertionError("Error retrieving library namespace");
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(I18N.getText("library.error.retrievingEventHandler"), e);
    }
  }
}
