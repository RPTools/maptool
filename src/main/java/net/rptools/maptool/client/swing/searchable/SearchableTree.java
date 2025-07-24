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
package net.rptools.maptool.client.swing.searchable;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.rptools.maptool.model.localisedObject.LocalObject;
import org.apache.logging.log4j.util.Strings;

/** An alternate version of com.jidesoft.swing.TreeSearchable */
public class SearchableTree extends SearchableEx
    implements TreeModelListener, PropertyChangeListener {
  protected final JTree _tree;
  private boolean _recursive = true;
  private transient java.util.List<TreePath> _treePaths;

  public SearchableTree(JTree tree) {
    super(tree);
    this._tree = tree;
    if (tree.getModel() != null) {
      tree.getModel().addTreeModelListener(this);
    }
    tree.addPropertyChangeListener("model", this);
  }

  public boolean isRecursive() {
    return this._recursive;
  }

  public void setRecursive(boolean recursive) {
    this._recursive = recursive;
    this.resetTreePaths();
  }

  public void uninstallListeners() {
    super.uninstallListeners();
    if (this._tree != null && this._tree.getModel() != null) {
      this._tree.getModel().removeTreeModelListener(this);
    }
    Objects.requireNonNull(this._tree).removePropertyChangeListener("model", this);
  }

  protected void setSelectedIndex(int index, boolean incremental) {
    this._tree.expandRow(index);
    if (!this.isRecursive()) {
      if (incremental) {
        this._tree.addSelectionInterval(index, index);
      } else {
        this._tree.setSelectionRow(index);
      }
      this._tree.scrollRowToVisible(index);
    } else {
      Object elementAt = this.getElementAt(index);
      if (elementAt instanceof TreePath path) {
        if (incremental) {
          this._tree.addSelectionPath(path);
        } else {
          this._tree.setSelectionPath(path);
        }
        this._tree.scrollPathToVisible(path);
      }
    }
  }

  protected int getSelectedIndex() {
    if (this.isRecursive()) {
      TreePath[] treePaths = this._tree.getSelectionPaths();
      return treePaths != null && treePaths.length > 0
          ? this.getTreePaths().indexOf(treePaths[0])
          : -1;
    } else {
      int[] ai = this._tree.getSelectionRows();
      return ai != null && ai.length != 0 ? ai[0] : -1;
    }
  }

  protected Object getElementAt(int index) {
    if (index == -1) {
      return null;
    } else {
      return !this.isRecursive() ? this._tree.getPathForRow(index) : this.getTreePaths().get(index);
    }
  }

  protected int getElementCount() {
    return !this.isRecursive() ? this._tree.getRowCount() : this.getTreePaths().size();
  }

  protected void populateTreePaths() {
    this._treePaths = new ArrayList<>();
    Object root = this._tree.getModel().getRoot();
    this.populateTreePaths0(root, new TreePath(root), this._tree.getModel());
  }

  private void populateTreePaths0(Object node, TreePath path, TreeModel model) {
    if (this._tree.isRootVisible()
        || path.getLastPathComponent() != this._tree.getModel().getRoot()) {
      this._treePaths.add(path);
    }

    for (int i = 0; i < model.getChildCount(node); ++i) {
      Object childNode = model.getChild(node, i);
      this.populateTreePaths0(childNode, path.pathByAddingChild(childNode), model);
    }
  }

  protected void resetTreePaths() {
    this._treePaths = null;
  }

  protected List<TreePath> getTreePaths() {
    if (this._treePaths == null) {
      this.populateTreePaths();
    }
    return this._treePaths;
  }

  protected String convertElementToString(Object object) {
    String textToSearch = "";
    if (object instanceof TreePath) {
      Object treeNode = ((TreePath) object).getLastPathComponent();
      if (treeNode instanceof DefaultMutableTreeNode dmtn) {
        Object userObject = dmtn.getUserObject();
        if (userObject instanceof SearchWords searchWords) {
          textToSearch = Strings.join(searchWords.getSearchWords(), ' ');
        } else if (userObject instanceof LocalObject localisedItem) {
          textToSearch =
              localisedItem
                  + " "
                  + localisedItem.getValue()
                  + localisedItem.getI18nKey().replace('.', ' ');
        }
      }
      if (!(this.getComponent() instanceof JTree tree)) {
        textToSearch += " " + treeNode.toString();
      } else {
        TreePath[] selectionPaths = tree.getSelectionPaths();
        boolean selected = false;
        if (selectionPaths != null) {
          for (TreePath selectedPath : selectionPaths) {
            if (selectedPath == object) {
              selected = true;
              break;
            }
          }
        }
        textToSearch +=
            " "
                + tree.convertValueToText(
                    treeNode,
                    selected,
                    tree.isExpanded((TreePath) object),
                    tree.getModel().isLeaf(treeNode),
                    tree.getRowForPath((TreePath) object),
                    tree.hasFocus() && tree.getLeadSelectionPath() == object);
      }
    } else {
      textToSearch += object != null ? " " + object : "";
    }
    return textToSearch;
  }

  public void treeNodesChanged(TreeModelEvent e) {
    if (this.isProcessModelChangeEvent()) {
      this.resetTreePaths();
    }
  }

  public void treeNodesInserted(TreeModelEvent e) {
    if (this.isProcessModelChangeEvent()) {
      this.resetTreePaths();
    }
  }

  public void treeNodesRemoved(TreeModelEvent e) {
    if (this.isProcessModelChangeEvent()) {
      this.resetTreePaths();
    }
  }

  public void treeStructureChanged(TreeModelEvent e) {
    if (this.isProcessModelChangeEvent()) {
      this.resetTreePaths();
    }
  }

  public void propertyChange(PropertyChangeEvent evt) {
    if ("model".equals(evt.getPropertyName())) {
      if (evt.getOldValue() instanceof TreeModel) {
        ((TreeModel) evt.getOldValue()).removeTreeModelListener(this);
      }
      if (evt.getNewValue() instanceof TreeModel) {
        ((TreeModel) evt.getNewValue()).addTreeModelListener(this);
      }
      this.resetTreePaths();
    }
  }
}
