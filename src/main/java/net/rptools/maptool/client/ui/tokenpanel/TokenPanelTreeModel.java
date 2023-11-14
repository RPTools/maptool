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

import com.google.common.eventbus.Subscribe;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.zones.TokenEdited;
import net.rptools.maptool.model.zones.TokensAdded;
import net.rptools.maptool.model.zones.TokensChanged;
import net.rptools.maptool.model.zones.TokensRemoved;
import net.rptools.maptool.server.ServerPolicy;

public class TokenPanelTreeModel implements TreeModel {

  public static final class View {
    private final String displayName;

    private View(String name) {
      this.displayName = I18N.getText("panel.MapExplorer.View." + name);
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  /** The list of token filters. */
  private final List<TokenFilter> filterList = new ArrayList<TokenFilter>();

  private static final String root = "Views";
  private Zone zone;
  private final JTree tree;
  /** Is an updateInternal pending? */
  private volatile boolean updatePending = false;

  public TokenPanelTreeModel(JTree tree) {
    this.tree = tree;
    update();

    // It would be useful to have this list be static, but it's really not that big of a memory
    // footprint
    filterList.add(new NPCTokenFilter());
    filterList.add(new PlayerTokenFilter());

    for (final var layer : Zone.Layer.values()) {
      if (layer != Zone.Layer.TOKEN) {
        filterList.add(new AdminLayerFilter(layer));
      }
    }
    filterList.add(new LightSourceFilter());

    new MapToolEventBus().getMainEventBus().register(this);
  }

  private final List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();
  private final Map<View, List<Token>> viewMap = new HashMap<View, List<Token>>();
  private final List<View> currentViewList = new ArrayList<View>();

  public Object getRoot() {
    return root;
  }

  /**
   * Set the zone, updating the listeners and the internals.
   *
   * @param zone the Zone to set.
   */
  public void setZone(Zone zone) {
    this.zone = zone;
    update();
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
      return getViewList((View) parent).indexOf(child);
    }
    return -1;
  }

  public void addTreeModelListener(TreeModelListener l) {
    listenerList.add(l);
  }

  public void removeTreeModelListener(TreeModelListener l) {
    listenerList.remove(l);
  }

  /**
   * Run updateInternal on the EventQueue. This clears currentViewList and viewMap, and adds views
   * in the filterList to currentViewList.
   */
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

  /** Clear currentViewList and viewMap, and add views in the filterList to currentViewList */
  private void updateInternal() {
    currentViewList.clear();
    viewMap.clear();

    // Add in the appropriate views
    List<Token> tokenList = new ArrayList<Token>();
    if (zone != null) {
      tokenList = zone.getAllTokens();
    }
    for (TokenFilter filter : filterList) {
      if (!filter.checkPlayer()) {
        continue;
      }

      var viewTokens = new ArrayList<Token>();
      for (Token token : tokenList) {
        if (filter.accept(token)) {
          viewTokens.add(token);
        }
      }
      // Don't keep views that don't have any tokens.
      if (!viewTokens.isEmpty()) {
        viewTokens.sort(NAME_AND_STATE_COMPARATOR);
        viewMap.put(filter.view, Collections.unmodifiableList(viewTokens));
        currentViewList.add(filter.view);
      }
    }

    // Keep the expanded branches consistent
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

  private static class TokenFilter {
    public final View view;

    public TokenFilter(View view) {
      this.view = view;
    }

    /**
     * Checks whether the current player is allowed to see tokens allowed by this filter.
     *
     * <p>In other words, check whether the filter could ever be displayed for the current player.
     *
     * @return {@code true} if the filter is able to be displayed for the current player.
     */
    public boolean checkPlayer() {
      return true;
    }

    /**
     * Checks whether the filter matches the token.
     *
     * @param token The token to check.
     * @return {@code true} if the filter matches {@code token}, allowing it to be displayed.
     */
    public boolean accept(Token token) {
      return true;
    }
  }

  /** Accepts only tokens on a specified layer, and only if the player is a GM. */
  private static class AdminLayerFilter extends TokenFilter {
    public final Zone.Layer layer;

    public AdminLayerFilter(Zone.Layer layer) {
      super(new View(layer.name()));
      this.layer = layer;
    }

    @Override
    public boolean checkPlayer() {
      return MapTool.getPlayer().isGM();
    }

    @Override
    public boolean accept(Token token) {
      return this.layer == token.getLayer();
    }
  }

  /**
   * Accepts only PC tokens (for GM) or PC tokens owned by the current player (takes {@link
   * ServerPolicy#useStrictTokenManagement()} into account). Here's the selection process:
   *
   * <ol>
   *   <li>If the token is not a PC, return <code>false</code>.
   *   <li>If the current player is the GM, return <code>true</code>.
   *   <li>If individualized view is on or the token is visible only to owner, and the token is not
   *       owned by the current player, return <code>false</code>. (Takes into account
   *       StrictTokenManagement and the AllPlayers ownership flag).
   *   <li>If the token is not visible to players or is on the hidden layer, returns false.
   *   <li>Otherwise, return true.
   * </ol>
   */
  private static class PlayerTokenFilter extends TokenFilter {
    /** Accepts only PCs tokens owned by the current player. */
    public PlayerTokenFilter() {
      super(new View("PLAYERS"));
    }

    @Override
    public boolean accept(Token token) {
      if (token.getType() != Token.Type.PC) {
        return false;
      }
      if (MapTool.getPlayer().isGM()) {
        return true;
      }
      if (!AppUtil.playerOwns(token)
          && (MapTool.getServerPolicy().isUseIndividualViews() || token.isVisibleOnlyToOwner())) {
        return false;
      }
      return token.isVisible() && token.getLayer() != Zone.Layer.GM;
    }
  }

  /** Accepts only tokens with an attached light source and only when owned by the user. */
  private static class LightSourceFilter extends TokenFilter {
    /** Accepts only tokens with an attached light source and only when owned by the user. */
    public LightSourceFilter() {
      super(new View("LIGHT_SOURCES"));
    }

    @Override
    public boolean accept(Token token) {
      return !token.getLightSources().isEmpty() && AppUtil.playerOwns(token);
    }
  }

  /**
   * Accepts only NPC tokens (for GM) or NPC tokens owned by the current player (takes {@link
   * ServerPolicy#useStrictTokenManagement()} into account). Here's the selection process:
   *
   * <ol>
   *   <li>If the token is not on the Token layer, return <code>false</code>.
   *   <li>If the token is not a NPC, return <code>false</code>.
   *   <li>If the current player is the GM, return <code>true</code>.
   *   <li>If individualized view is on or the token is visible only to owner, and the token is not
   *       owned by the current player, return <code>false</code>. (Takes into account
   *       StrictTokenManagement and the AllPlayers ownership flag).
   *   <li>If the token is not visible to players, returns false.
   *   <li>Otherwise, return true.
   * </ol>
   */
  private static class NPCTokenFilter extends TokenFilter {
    public NPCTokenFilter() {
      super(new View("NPCS"));
    }

    @Override
    public boolean accept(Token token) {
      if (!token.getLayer().isTokenLayer() || token.getType() != Token.Type.NPC) {
        return false;
      }
      if (MapTool.getPlayer().isGM()) {
        return true;
      }
      if (!AppUtil.playerOwns(token)) {
        return false;
      }
      return token.isVisible();
    }
  }

  @Subscribe
  private void onTokensAdded(TokensAdded event) {
    update();
  }

  @Subscribe
  private void onTokensRemoved(TokensRemoved event) {
    update();
  }

  @Subscribe
  private void onTokensChanged(TokensChanged event) {
    update();
  }

  @Subscribe
  private void onTokensEdited(TokenEdited event) {
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
