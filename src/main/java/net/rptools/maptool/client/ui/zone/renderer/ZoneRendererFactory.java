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
package net.rptools.maptool.client.ui.zone.renderer;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Zone;

public class ZoneRendererFactory {
  /**
   * Create a new ZoneRenderer from a zone.
   *
   * @param zone the Zone.
   * @return the new {@link ZoneRenderer}
   */
  public static ZoneRenderer newRenderer(Zone zone) {
    ZoneRenderer renderer = new ZoneRenderer(zone);
    if (MapTool.getFrame() != null) {
      renderer.addOverlay(MapTool.getFrame().getPointerOverlay());
    }
    return renderer;
  }
}
