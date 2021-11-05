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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;

public class AppConstantsTest {

  private File dir = new File("Somedir");

  @Test
  public void propertyFilterMatch() {
    assertTrue(AppConstants.CAMPAIGN_PROPERTIES_FILE_FILTER.accept(dir, "a.mtprops"));
  }

  @Test
  public void propertyFilterNoMatch() {
    assertFalse(AppConstants.CAMPAIGN_PROPERTIES_FILE_FILTER.accept(dir, "a.something"));
  }
}
