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
   * Clears campaign data that is not stored in the {@link Campaign} object. This is called when a
   * campaign is about to be loaded, sent from the server, or a new campaign is created.
   */
  public void clearCampaignData() {
    MapTool.getFrame().getOverlayPanel().removeAllOverlays();
    JSScriptEngine.resetContexts();
    new LibraryManager().deregisterAllLibraries();
    new DataStoreManager().getDefaultDataStoreForRemoteUpdate().clear();
    UserDefinedMacroFunctions.getInstance().clearUserDefinedFunctions();
    MacroManager.clearCampaignAliases();
  }
}
