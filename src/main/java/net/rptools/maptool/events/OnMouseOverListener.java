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
import net.rptools.maptool.client.events.TokenHoverEnter;
import net.rptools.maptool.client.events.TokenHoverExit;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.util.EventMacroUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OnMouseOverListener {
  public static final String ON_MOUSE_OVER_CALLBACK = "onMouseOver";

  public OnMouseOverListener(TokenHoverEnter event) {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  private static final Logger LOGGER = LogManager.getLogger(EventMacroUtil.class);

  public OnMouseOverListener() {}

  @Subscribe
  public void onMouseOverEnter(TokenHoverEnter event) {
    var token = event.token();
    var tokX = event.token().getX();
    var tokY = event.token().getY();
    var shiftKey = event.shiftDown();
    var ctrlKey = event.controlDown();
    try {
      var libs = new LibraryManager().getLegacyEventTargets(ON_MOUSE_OVER_CALLBACK).get();
      if (!libs.isEmpty()) {
        for (Library handler : libs) {
          try {
            String libraryNamespace = handler.getNamespace().get();
            EventMacroUtil.callEventHandler(
                ON_MOUSE_OVER_CALLBACK,
                libraryNamespace,
                token.getId().toString() + "," + tokX + "," + tokY + "," + shiftKey + "," + ctrlKey,
                null,
                Collections.emptyMap(),
                false);
          } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("Error retrieving library namespace");
          }
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(I18N.getText("library.error.retrievingEventHandler"), e);
    }
  }

  @Subscribe
  public void onMouseOverExit(TokenHoverExit event) {
    var token = event.token();
    try {
      var libs = new LibraryManager().getLegacyEventTargets(ON_MOUSE_OVER_CALLBACK).get();
      if (!libs.isEmpty()) {
        for (Library handler : libs) {
          try {
            String libraryNamespace = handler.getNamespace().get();
            EventMacroUtil.callEventHandler(
                ON_MOUSE_OVER_CALLBACK,
                libraryNamespace,
                token.getId().toString() + ",exit",
                null,
                Collections.emptyMap(),
                false);
          } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError("Error retrieving library namespace");
          }
        }
      }
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error(I18N.getText("library.error.retrievingEventHandler"), e);
    }
  }
}
