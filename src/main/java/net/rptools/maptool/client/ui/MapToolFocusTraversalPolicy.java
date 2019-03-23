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
package net.rptools.maptool.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import net.rptools.maptool.client.MapTool;

public class MapToolFocusTraversalPolicy extends FocusTraversalPolicy {

  @Override
  public Component getComponentAfter(Container aContainer, Component aComponent) {
    return MapTool.getFrame().getCurrentZoneRenderer();
  }

  @Override
  public Component getComponentBefore(Container aContainer, Component aComponent) {
    return MapTool.getFrame().getCurrentZoneRenderer();
  }

  @Override
  public Component getFirstComponent(Container aContainer) {
    return MapTool.getFrame().getCurrentZoneRenderer();
  }

  @Override
  public Component getLastComponent(Container aContainer) {
    return MapTool.getFrame().getCurrentZoneRenderer();
  }

  @Override
  public Component getDefaultComponent(Container aContainer) {
    return MapTool.getFrame().getCurrentZoneRenderer();
  }
}
