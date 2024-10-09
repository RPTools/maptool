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
package net.rptools.maptool.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * @author tylere
 */
public class MRUCampaignManager {
  // To increase max mru's need to update mnemonics code
  private static final int DEFAULT_MAX_MRU = 9;
  private final JMenu mruMenu;
  private List<File> mruCampaigns;

  public MRUCampaignManager(JMenu menu) {
    mruMenu = menu;
    mruCampaigns = new ArrayList<File>(DEFAULT_MAX_MRU + 1);
    loadMruCampaignList();
  }

  /**
   * Returns the MRU Campaigns menu item and sub-menu.
   *
   * @return the MRU Campaigns menu item and sub-menu.
   */
  public JMenu getMRUMenu() {
    return mruMenu;
  }

  /**
   * Adds a new Campaign to the MRU list, then resort the list and update the menu
   *
   * @param newCampaign the campaign to add to the most recently used list.
   */
  public void addMRUCampaign(File newCampaign) {
    // FIXME (this coupling is too tight; change the calling function to avoid this call entirely)
    // don't add the autosave recovery file until it is resaved
    if (newCampaign == AutoSaveManager.AUTOSAVE_FILE) return;

    if (mruCampaigns.isEmpty()) {
      mruCampaigns.add(newCampaign);
    } else {
      // This code would be much simpler, but too late in the 1.3 cycle for this change.
      // LinkedList<File> newMruList = new LinkedList<File>(mruCampaigns);
      // newMruList.removeFirstOccurrence(newCampaign);
      // newMruList.addFirst(newCampaign);
      // while (newMruList.size() > DEFAULT_MAX_MRU)
      // newMruList.removeLast();
      ArrayList<File> newMruList = new ArrayList<File>(DEFAULT_MAX_MRU + 1);
      newMruList.add(newCampaign);
      for (File next : mruCampaigns) {
        if (newMruList.size() == DEFAULT_MAX_MRU) break;
        else if (next.equals(newCampaign)) continue;
        else newMruList.add(next);
      }
      mruCampaigns = newMruList;
    }
    resetMruMenu();
  }

  private void resetMruMenu() {
    mruMenu.removeAll();
    addMRUsToMenu();
    saveMruCampaignList();
  }

  private void addMRUsToMenu() {
    if (mruCampaigns.isEmpty()) {
      mruMenu.add(new JMenuItem("[empty]"));
    } else {
      int i = 1;
      for (ListIterator<File> iter = mruCampaigns.listIterator(); iter.hasNext(); ) {
        if (i > DEFAULT_MAX_MRU) {
          break;
        }
        File nextFile = iter.next();
        // Check to see if the file has been deleted
        if (nextFile.exists()) {
          Action action = new AppActions.OpenMRUCampaign(nextFile, i++);
          mruMenu.add(action);
        } else {
          iter.remove();
        }
      }
    }
  }

  private void saveMruCampaignList() {
    AppStatePersisted.setMruCampaigns(mruCampaigns);
  }

  private void loadMruCampaignList() {
    mruCampaigns = AppStatePersisted.getMruCampaigns();
    addMRUsToMenu();
  }
}
