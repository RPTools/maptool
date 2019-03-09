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
package net.rptools.maptool.client.ui.drawpanel;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;

public class DrawPanelTreeModel implements TreeModel, ModelChangeListener {

  private final String root = "Views";
  private Zone zone;
  private final JTree tree;
  private final Map<View, List<DrawnElement>> viewMap = new HashMap<View, List<DrawnElement>>();
  private final List<View> currentViewList = new ArrayList<View>();
  private volatile boolean updatePending = false;
  private final List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();

  public enum View {
    TOKEN_DRAWINGS("panel.DrawExplorer.View.TOKEN", Zone.Layer.TOKEN),
    GM_DRAWINGS("panel.DrawExplorer.View.GM", Zone.Layer.GM),
    OBJECT_DRAWINGS("panel.DrawExplorer.View.OBJECT", Zone.Layer.OBJECT),
    BACKGROUND_DRAWINGS("panel.DrawExplorer.View.BACKGROUND", Zone.Layer.BACKGROUND);

    private View(String key, Zone.Layer layer) {
      this.displayName = I18N.getText(key);
      this.layer = layer;
    }

    String displayName;
    Zone.Layer layer;

    public String getDisplayName() {
      return displayName;
    }

    public Zone.Layer getLayer() {
      return layer;
    }
  }

  public DrawPanelTreeModel(JTree tree) {
    this.tree = tree;
    update();
  }

  @Override
  public void modelChanged(ModelChangeEvent event) {
    update();
  }

  @Override
  public Object getRoot() {
    return root;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent == root) {
      return currentViewList.get(index);
    }
    if (parent instanceof View) {
      return getViewList((View) parent).get(index);
    }
    if (isDrawnElementGroup(parent)) {
      DrawablesGroup dg = (DrawablesGroup) ((DrawnElement) parent).getDrawable();
      return dg.getDrawableList().get(dg.getDrawableList().size() - index - 1);
    }
    return null;
  }

  private boolean isDrawnElementGroup(Object object) {
    if (object instanceof DrawnElement)
      return ((DrawnElement) object).getDrawable() instanceof DrawablesGroup;
    return false;
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent == root) {
      return currentViewList.size();
    }
    if (parent instanceof View) {
      return getViewList((View) parent).size();
    }
    if (isDrawnElementGroup(parent)) {
      DrawablesGroup dg = (DrawablesGroup) ((DrawnElement) parent).getDrawable();
      return dg.getDrawableList().size();
    }
    return 0;
  }

  private List<DrawnElement> getViewList(View view) {
    List<DrawnElement> list = viewMap.get(view);
    if (list == null) {
      return Collections.emptyList();
    }
    return list;
  }

  @Override
  public boolean isLeaf(Object node) {
    if (node instanceof DrawnElement) {
      if (((DrawnElement) node).getDrawable() instanceof DrawablesGroup) return false;
      else return true;
    }
    return false;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // Nothing to do
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent == root) {
      return currentViewList.indexOf(child);
    }
    if (parent instanceof View) {
      getViewList((View) parent).indexOf(child);
    }
    if (parent instanceof DrawnElement) {
      if (((DrawnElement) parent).getDrawable() instanceof DrawablesGroup) {
        DrawablesGroup dg = (DrawablesGroup) ((DrawnElement) parent).getDrawable();
        return dg.getDrawableList().size() - dg.getDrawableList().indexOf(child) - 1;
      }
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(l);
  }

  // Called by zone change event
  public void setZone(Zone zone) {
    if (zone != null) {
      zone.removeModelChangeListener(this);
    }
    this.zone = zone;
    update();

    if (zone != null) {
      zone.addModelChangeListener(this);
    }
  }

  public void update() {
    // better solution would be to use a timeout to invoke the internal update to give more
    // token events the chance to arrive, but in this case EventQueue overload will
    // manage to delay it quite nicely
    if (!updatePending) {
      updatePending = true;
      EventQueue.invokeLater(
          new Runnable() {
            public void run() {
              updatePending = false;
              updateInternal();
            }
          });
    }
  }

  private void updateInternal() {
    currentViewList.clear();
    viewMap.clear();
    List<DrawnElement> drawableList = new ArrayList<DrawnElement>();
    if (zone != null) {
      if (MapTool.getPlayer().isGM()) {
        // GM Sees all drawings
        for (View v : View.values()) {
          drawableList = zone.getDrawnElements(v.getLayer());
          if (drawableList.size() > 0) {
            // Reverse the list so that the element drawn last, is shown at the top of the tree
            // Be careful to clone the list so you don't damage the map
            List<DrawnElement> reverseList = new ArrayList<DrawnElement>(drawableList);
            Collections.reverse(reverseList);
            viewMap.put(v, reverseList);
            currentViewList.add(v);
          }
        }
      } else {
        // Players can only see templates on the token layer
        drawableList = zone.getDrawnElements(Zone.Layer.TOKEN);
        if (drawableList.size() > 0) {
          // Reverse the list so that the element drawn last, is shown at the top of the tree
          // Be careful to clone the list so you don't damage the map
          List<DrawnElement> reverseList = new ArrayList<DrawnElement>(drawableList);
          Iterator<DrawnElement> iter = reverseList.iterator();
          while (iter.hasNext()) {
            DrawnElement de = iter.next();
            if (!(de.getDrawable() instanceof AbstractTemplate)) iter.remove();
          }
          Collections.reverse(reverseList);
          viewMap.put(View.TOKEN_DRAWINGS, reverseList);
          currentViewList.add(View.TOKEN_DRAWINGS);
        }
      }
    }

    Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(root));

    fireStructureChangedEvent(
        new TreeModelEvent(
            this,
            new Object[] {getRoot()},
            new int[] {currentViewList.size() - 1},
            new Object[] {View.BACKGROUND_DRAWINGS}));
    while (expandedPaths != null && expandedPaths.hasMoreElements()) {
      tree.expandPath(expandedPaths.nextElement());
    }
  }

  private void fireStructureChangedEvent(TreeModelEvent e) {
    TreeModelListener[] listeners =
        listenerList.toArray(new TreeModelListener[listenerList.size()]);
    for (TreeModelListener listener : listeners) {
      listener.treeStructureChanged(e);
    }
  }
}
