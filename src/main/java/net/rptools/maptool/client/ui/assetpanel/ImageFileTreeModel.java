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
package net.rptools.maptool.client.ui.assetpanel;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.rptools.maptool.client.MapTool;

/** */
public class ImageFileTreeModel implements TreeModel {
  private final List<Directory> rootDirectories = new ArrayList<Directory>();
  private final Object root = new String("");
  private final List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();

  public ImageFileTreeModel() {}

  public boolean isRootGroup(Directory dir) {
    return rootDirectories.contains(dir);
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return root;
  }

  public boolean containsRootGroup(Directory dir) {
    for (Directory directory : rootDirectories) {
      if (directory.getPath().equals(dir.getPath())) {
        return true;
      }
    }
    return false;
  }

  public void addRootGroup(Directory directory) {
    rootDirectories.add(directory);
    Collections.sort(rootDirectories, Directory.COMPARATOR);
    fireStructureChangedEvent(
        new TreeModelEvent(
            this,
            new Object[] {getRoot()},
            new int[] {rootDirectories.size() - 1},
            new Object[] {directory}));
  }

  public void removeRootGroup(Directory directory) {
    rootDirectories.remove(directory);
    fireStructureChangedEvent(new TreeModelEvent(this, new Object[] {getRoot()}));
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  public Object getChild(Object parent, int index) {
    if (parent == root) {
      return rootDirectories.get(index);
    }
    Directory dir = (Directory) parent;
    try {
      return dir.getSubDirs().get(index);
    } catch (FileNotFoundException fnf) {
      MapTool.showError(fnf.getLocalizedMessage(), fnf);
      // Returning 'null' should be okay, since getChildCount will always return 0 for this
      // exception
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    if (parent == root) {
      return rootDirectories.size();
    }
    Directory dir = (Directory) parent;
    try {
      return dir.getSubDirs().size();
    } catch (FileNotFoundException fnf) {
      MapTool.showError(fnf.getLocalizedMessage(), fnf);
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    // No leaves here
    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {
    // Nothing to do right now
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    if (parent == root) {
      return rootDirectories.indexOf(child);
    }
    Directory dir = (Directory) parent;
    try {
      return dir.getSubDirs().indexOf(child);
    } catch (FileNotFoundException fnf) {
      MapTool.showError(fnf.getLocalizedMessage(), fnf);
      // Returning '0' should be okay, since getChildCount will always return 0 for this exception
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event. TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(l);
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event. TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(l);
  }

  public void refresh() {}

  private void fireStructureChangedEvent(TreeModelEvent e) {
    TreeModelListener[] listeners =
        listenerList.toArray(new TreeModelListener[listenerList.size()]);
    for (TreeModelListener listener : listeners) {
      listener.treeStructureChanged(e);
    }
  }

  private void fireNodesInsertedEvent(TreeModelEvent e) {
    TreeModelListener[] listeners =
        listenerList.toArray(new TreeModelListener[listenerList.size()]);
    for (TreeModelListener listener : listeners) {
      listener.treeNodesInserted(e);
    }
  }
}
