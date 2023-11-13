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

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.zones.DrawableAdded;
import net.rptools.maptool.model.zones.DrawableRemoved;
import net.rptools.maptool.util.CollectionUtil;

public class DrawPanelTreeModel implements TreeModel {

  private static final String root = "Views";
  private Zone zone;
  private final JTree tree;
  private final Map<View, List<DrawnElement>> viewMap = new HashMap<View, List<DrawnElement>>();
  private final List<View> currentViewList = new ArrayList<View>();
  private volatile boolean updatePending = false;
  private final List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();

  public static final class View {
    private static final Map<Zone.Layer, View> byLayer =
        CollectionUtil.newFilledEnumMap(Zone.Layer.class, View::new);

    public static View getByLayer(Zone.Layer layer) {
      return byLayer.get(layer);
    }

    private final String displayName;
    private final Zone.Layer layer;

    private View(Zone.Layer layer) {
      this.displayName = I18N.getText("panel.DrawExplorer.View." + layer.name());
      this.layer = layer;
    }

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
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Subscribe
  private void onDrawableAdded(DrawableAdded event) {
    if (event.zone() != this.zone) {
      return;
    }
    update();
  }

  @Subscribe
  private void onDrawableRemoved(DrawableRemoved event) {
    if (event.zone() != this.zone) {
      return;
    }
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
    return node instanceof DrawnElement
        && !(((DrawnElement) node).getDrawable() instanceof DrawablesGroup);
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
      return getViewList((View) parent).indexOf(child);
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
    this.zone = zone;
    update();
  }

  public void update() {
    // better solution would be to use a timeout to invoke the internal update to give more
    // token events the chance to arrive, but in this case EventQueue overload will
    // manage to delay it quite nicely
    if (!updatePending) {
      updatePending = true;
      EventQueue.invokeLater(
          () -> {
            updatePending = false;
            updateInternal();
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
        for (var layer : Zone.Layer.values()) {
          View v = View.getByLayer(layer);
          drawableList = zone.getDrawnElements(layer);
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
          reverseList.removeIf(de -> !(de.getDrawable() instanceof AbstractTemplate));
          Collections.reverse(reverseList);
          View view = View.getByLayer(Zone.Layer.TOKEN);
          viewMap.put(view, reverseList);
          currentViewList.add(view);
        }
      }
    }

    Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(root));

    fireStructureChangedEvent(new TreeModelEvent(this, new Object[] {getRoot()}));
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
