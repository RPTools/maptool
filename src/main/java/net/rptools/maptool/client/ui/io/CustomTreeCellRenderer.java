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
package net.rptools.maptool.client.ui.io;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

/** @author crash */
@SuppressWarnings("serial")
class CustomTreeCellRenderer extends JCheckBox implements TreeCellRenderer {
  DefaultMutableTreeNode node;
  MaptoolNode mtnode;

  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {

    node = (DefaultMutableTreeNode) value;
    mtnode = (MaptoolNode) node.getUserObject();
    setText(mtnode.toString());
    setBackground(tree.getBackground());
    setEnabled(tree.isEnabled());
    setComponentOrientation(tree.getComponentOrientation());
    return this;
  }

  protected boolean isFirstLevel() {
    return node.getParent() == node.getRoot();
  }
}
