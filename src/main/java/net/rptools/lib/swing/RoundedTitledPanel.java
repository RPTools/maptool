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
package net.rptools.lib.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class RoundedTitledPanel extends JPanel {

  public RoundedTitledPanel() {
    super.setLayout(new RoundedTitlePanelLayout());
  }

  @Override
  public void setLayout(LayoutManager mgr) {
    throw new IllegalAccessError("Can't change the layout");
  }

  private class RoundedTitlePanelLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component comp) {}

    public void layoutContainer(Container parent) {}

    public Dimension minimumLayoutSize(Container parent) {
      return null;
    }

    public Dimension preferredLayoutSize(Container parent) {
      return null;
    }

    public void removeLayoutComponent(Component comp) {}
  }
}
