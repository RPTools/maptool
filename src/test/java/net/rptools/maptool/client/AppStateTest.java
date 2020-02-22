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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import org.junit.jupiter.api.Test;

public class AppStateTest {

  @Test
  public void getCampaignName_nullIsDefault() {
    AppState.setCampaignFile(null);

    assertEquals("Default", AppState.getCampaignName());
  }

  @Test
  public void getCampaignName_shortName_properExtension() {
    AppState.setCampaignFile(new File("c1.cmpgn"));

    assertEquals("c1", AppState.getCampaignName());
  }
}
