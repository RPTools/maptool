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
package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.ModelChangeEvent;
import net.rptools.maptool.model.ModelChangeListener;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.server.ServerPolicy;

public class TokenPanelTreeModel implements TreeModel, ModelChangeListener {
  private static final String _TOKENS = "panel.MapExplorer.View.TOKENS";
  private static final String _PLAYERS = "panel.MapExplorer.View.PLAYERS";
  private static final String _GROUPS = "panel.MapExplorer.View.GROUPS";
  private static final String _GM = "panel.MapExplorer.View.GM";
  private static final String _OBJECTS = "panel.MapExplorer.View.OBJECTS";
  private static final String _BACKGROUND = "panel.MapExplorer.View.BACKGROUND";
  private static final String _CLIPBOARD = "panel.MapExplorer.View.CLIPBOARD";
  private static final String _LIGHT_SOURCES = "panel.MapExplorer.View.LIGHT_SOURCES";

  public enum View {
    // @formatter:off
    // I18N key						Zone.Layer					Req'd?	isAdmin?
    TOKENS(_TOKENS, Zone.Layer.TOKEN, false, false),
    PLAYERS(_PLAYERS, Zone.Layer.TOKEN, false, false),
    GROUPS(_GROUPS, Zone.Layer.TOKEN, false, false),
    GM(_GM, Zone.Layer.GM, false, true),
    OBJECTS(_OBJECTS, Zone.Layer.OBJECT, false, true),
    BACKGROUND(_BACKGROUND, Zone.Layer.BACKGROUND, false, true),
    CLIPBOARD(_CLIPBOARD, Zone.Layer.TOKEN, false, true),
    LIGHT_SOURCES(_LIGHT_SOURCES, null, false, false);
    // @formatter:on

    String displayName;
    String description;
    boolean required;
    Zone.Layer layer;
    boolean isAdmin;

    private View(String key, Zone.Layer layer, boolean required, boolean isAdmin) {
      this.displayName = I18N.getText(key);
      this.description = null; // I18N.getDescription(key); // TODO Tooltip -- not currently used
      this.required = required;
      this.layer = layer;
      this.isAdmin = isAdmin;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getDescription() {
      return description;
    }

    public Zone.Layer getLayer() {
      return layer;
    }

    public boolean isRequired() {
      return required;
    }
  }

  private final List<TokenFilter> filterList = new ArrayList<TokenFilter>();
  private final String root = "Views";
  private Zone zone;
  private final JTree tree;
  private volatile boolean updatePending = false;

  public TokenPanelTreeModel(JTree tree) {
    this.tree = tree;
    update();

    // It would be useful to have this list be static, but it's really not that big of a memory
    // footprint
    // TODO: refactor to more tightly couple the View enum and the corresponding filter
    filterList.add(new TokenTokenFilter());
    filterList.add(new PlayerTokenFilter());
    filterList.add(new GMFilter());
    filterList.add(new ObjectFilter());
    filterList.add(new BackgroundFilter());
    filterList.add(new LightSourceFilter());
  }

  private final List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();
  private final Map<View, List<Token>> viewMap = new HashMap<View, List<Token>>();
  private final List<View> currentViewList = new ArrayList<View>();

  public Object getRoot() {
    return root;
  }

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

  public Object getChild(Object parent, int index) {
    if (parent == root) {
      return currentViewList.get(index);
    }
    if (parent instanceof View) {
      return getViewList((View) parent).get(index);
    }
    return null;
  }

  public int getChildCount(Object parent) {
    if (parent == root) {
      return currentViewList.size();
    }
    if (parent instanceof View) {
      return getViewList((View) parent).size();
    }
    return 0;
  }

  private List<Token> getViewList(View view) {
    List<Token> list = viewMap.get(view);
    if (list == null) {
      return Collections.emptyList();
    }
    return list;
  }

  public boolean isLeaf(Object node) {
    return node instanceof Token;
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    // Nothing to do
  }

  public int getIndexOfChild(Object parent, Object child) {
    if (parent == root) {
      return currentViewList.indexOf(child);
    }
    if (parent instanceof View) {
      getViewList((View) parent).indexOf(child);
    }
    return -1;
  }

  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(l);
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

    // Plan to show all of the views in order to keep the order
    for (TokenFilter filter : filterList) {
      if (filter.view.isAdmin && !MapTool.getPlayer().isGM()) {
        continue;
      }
      currentViewList.add(filter.view);
    }

    // Add in the appropriate views
    List<Token> tokenList = new ArrayList<Token>();
    if (zone != null) {
      tokenList = zone.getAllTokens();
    }
    for (Token token : tokenList) {
      for (TokenFilter filter : filterList) {
        filter.filter(token);
      }
    }

    // Clear out any view without any tokens
    for (ListIterator<View> viewIter = currentViewList.listIterator(); viewIter.hasNext(); ) {
      View view = viewIter.next();
      if (!view.isRequired() && (viewMap.get(view) == null || viewMap.get(view).size() == 0)) {
        viewIter.remove();
      }
    }

    // Sort
    for (List<Token> tokens : viewMap.values()) {
      Collections.sort(tokens, NAME_AND_STATE_COMPARATOR);
    }

    // Keep the expanded branches consistent
    Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(root));
    // @formatter:off
    fireStructureChangedEvent(
        new TreeModelEvent(
            this,
            new Object[] {getRoot()},
            new int[] {currentViewList.size() - 1},
            new Object[] {View.TOKENS}));
    // @formatter:on
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

  private void fireNodesInsertedEvent(TreeModelEvent e) {
    TreeModelListener[] listeners =
        listenerList.toArray(new TreeModelListener[listenerList.size()]);
    for (TreeModelListener listener : listeners) {
      listener.treeNodesInserted(e);
    }
  }

  private abstract class TokenFilter {
    private final View view;

    public TokenFilter(View view) {
      this.view = view;
    }

    private void filter(Token token) {
      if (accept(token)) {
        List<Token> tokenList = viewMap.get(view);
        if (tokenList == null) {
          tokenList = new ArrayList<Token>();
          viewMap.put(view, tokenList);
        }
        tokenList.add(token);
      }
    }

    protected abstract boolean accept(Token token);
  }

  /**
   * Accepts only tokens that are PCs and are owned by the current player (takes
   * useStrictTokenManagement() into account).
   */
  private class PlayerTokenFilter extends TokenFilter {
    public PlayerTokenFilter() {
      super(View.PLAYERS);
    }

    @Override
    protected boolean accept(Token token) {
      if (token.getType() != Token.Type.PC) return false;
      if (MapTool.getServerPolicy().isUseIndividualViews()) {
        if (AppUtil.playerOwns(token)) {
          return true;
        } else return false;
      }
      return token.isVisible();
    }
  }

  /** Accepts only tokens on the Object layer and only for the GM. */
  private class ObjectFilter extends TokenFilter {
    /** Accepts only tokens on the Object layer and only for the GM. */
    public ObjectFilter() {
      super(View.OBJECTS);
    }

    @Override
    protected boolean accept(Token token) {
      return MapTool.getPlayer().isGM() && token.isObjectStamp();
    }
  }

  /** Accepts only tokens on the GM (aka Hidden) layer and only for a GM. */
  private class GMFilter extends TokenFilter {
    /** Accepts only tokens on the GM (aka Hidden) layer and only for a GM. */
    public GMFilter() {
      super(View.GM);
    }

    @Override
    protected boolean accept(Token token) {
      return MapTool.getPlayer().isGM() && token.isGMStamp();
    }
  }

  /** Accepts only tokens on the Background layer and only for a GM. */
  private class BackgroundFilter extends TokenFilter {
    /** Accepts only tokens on the Background layer and only for a GM. */
    public BackgroundFilter() {
      super(View.BACKGROUND);
    }

    @Override
    protected boolean accept(Token token) {
      return MapTool.getPlayer().isGM() && token.isBackgroundStamp();
    }
  }

  /** Accepts only tokens with an attached light source and only when owned by the user. */
  private class LightSourceFilter extends TokenFilter {
    /** Accepts only tokens with an attached light source and only when owned by the user. */
    public LightSourceFilter() {
      super(View.LIGHT_SOURCES);
    }

    @Override
    protected boolean accept(Token token) {
      return token.getLightSources().isEmpty() ? false : AppUtil.playerOwns(token);
    }
  }

  /**
   * Accepts only NPC tokens (for GM) or tokens owned by the current player (takes {@link
   * ServerPolicy#useStrictTokenManagement()} into account). Here's the selection process:
   *
   * <ol>
   *   <li>If the token is not on the Token layer, return <code>false</code>.
   *   <li>If the token has type PC, return <code>false</code>.
   *   <li>If the current player is the GM, return <code>true</code>.
   *   <li>If the token is owned by the current player, return <code>true</code>. (Takes into
   *       account StrictTokenManagement and the AllPlayers ownership flag).
   *   <li>If the token is visible only to the owner, return <code>false</code>. (It's already been
   *       determined that we're not an owner.)
   *   <li>Otherwise, return true.
   * </ol>
   */
  private class TokenTokenFilter extends TokenFilter {
    public TokenTokenFilter() {
      super(View.TOKENS);
    }

    @Override
    protected boolean accept(Token token) {
      ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
      if (renderer == null) {
        return false;
      }
      if (token.isStamp() || token.getType() == Token.Type.PC) {
        return false;
      }
      if (MapTool.getPlayer().isGM()) return true;
      if (AppUtil.playerOwns(token)) { // returns true if useStrictTokenManagement()==false
        return true;
      }
      // if (token.isVisibleOnlyToOwner())
      // return false;
      return false;
    }
  }

  ////
  // MODEL CHANGE LISTENER
  public void modelChanged(ModelChangeEvent event) {
    update();
  }

  ////
  // SORTING
  private static final Comparator<Token> NAME_AND_STATE_COMPARATOR =
      new Comparator<Token>() {
        public int compare(Token o1, Token o2) {
          if (o1.isVisible() != o2.isVisible()) {
            return o1.isVisible() ? -1 : 1;
          }

          return o1.getName().compareToIgnoreCase(o2.getName());
        }
      };
}
