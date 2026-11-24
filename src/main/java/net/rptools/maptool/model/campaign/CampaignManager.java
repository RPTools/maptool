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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.model.campaign;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.script.javascript.JSScriptEngine;
import net.rptools.maptool.model.Campaign;
import net.rptools.maptool.model.gamedata.DataStoreManager;
import net.rptools.maptool.model.library.LibraryManager;

/**
 * Class for managing Campaigns. Its mostly empty at the moment, but campaign related functionality
 * will be added here as new features are added.
 */
public class CampaignManager {

  /**
   * Clears campaign data that is not stored in the {@link Campaign} object, and tells the server to
   * remove the campaign's add-on libraries.
   *
   * <p>Use this when the campaign in a running game is being replaced, such as by opening a
   * campaign or starting a new one. The new campaign publishes the add-on libraries it brings, but
   * without this the server and the other players would keep the old campaign's.
   */
  public void replaceCampaignData() {
    clearCampaignData();
    new LibraryManager().deregisterAddOnLibraries();
  }

  /**
   * Clears campaign data that is not stored in the {@link Campaign} object, in this client only.
   * This is called when a campaign is sent from the server, and when leaving a server.
   *
   * @see #replaceCampaignData()
   */
  public void clearCampaignData() {
    // Campaign state is cleared before the UI that displays it. If a panel throws, the
    // libraries, script contexts and data stores of the outgoing campaign have already been
    // released rather than leaking into the campaign about to be loaded.
    JSScriptEngine.resetContexts();
    new LibraryManager().deregisterAllLibraries();
    new DataStoreManager().getDefaultDataStoreForRemoteUpdate().clear();
    UserDefinedMacroFunctions.getInstance().clearUserDefinedFunctions();
    MacroManager.removeCampaignAliases();

    MapTool.getFrame().getOverlayPanel().removeAllOverlays();
    MapTool.getFrame().getLookupTablePanel().reset();
  }
}
