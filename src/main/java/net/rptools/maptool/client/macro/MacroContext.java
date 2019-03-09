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
package net.rptools.maptool.client.macro;

import java.util.LinkedList;
import java.util.List;

public class MacroContext {

  private List<String> transformHistory;

  public void addTransform(String transform) {
    if (transformHistory == null) {
      transformHistory = new LinkedList<String>();
    }
    transformHistory.add(transform);
  }

  public List<String> getTransformationHistory() {
    return transformHistory;
  }
}
