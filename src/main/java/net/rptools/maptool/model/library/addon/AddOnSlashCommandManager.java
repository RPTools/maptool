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
package net.rptools.maptool.model.library.addon;

import com.google.common.eventbus.Subscribe;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.model.library.AddOnsAddedEvent;
import net.rptools.maptool.model.library.AddOnsRemovedEvent;
import net.rptools.maptool.model.library.LibraryManager;

/** Listens for add on events and adds/removes slash commands from the macro manager. */
public class AddOnSlashCommandManager {
  @Subscribe
  void addOnAdded(AddOnsAddedEvent event) {
    var libMan = new LibraryManager();
    var addOns =
        event.addOns().stream()
            .map(a -> libMan.getLibrary(a.namespace()).get())
            .filter(a -> !a.getSlashCommands().isEmpty())
            .toList();
    SwingUtilities.invokeLater(
        () -> {
          for (var addOn : addOns) {
            for (var slash : addOn.getSlashCommands()) {
              MacroManager.setAlias(slash);
            }
          }
        });
  }

  @Subscribe
  void removedAddOn(AddOnsRemovedEvent event) {
    SwingUtilities.invokeLater(
        () -> {
          for (var addOn : event.addOns()) {
            MacroManager.removeAddOnAliases(addOn.namespace());
          }
        });
  }
}
