/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool_fx.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class MapExplorer_Controller {

	@FXML private TreeView<String> mapExplorerTreeView;

	private static final Logger log = LogManager.getLogger(MapExplorer_Controller.class);

	@FXML
	void initialize() {
		assert mapExplorerTreeView != null : "fx:id=\"mapExplorerTreeView\" was not injected: check your FXML file 'MapExplorer.fxml'.";

		addTokensToTree();
	}

	private void addTokensToTree() {
		TreeItem<String> rootItem = new TreeItem<String>("Inbox");
		rootItem.setExpanded(true);
		for (int i = 1; i < 6; i++) {
			TreeItem<String> item = new TreeItem<String>("Message" + i);
			rootItem.getChildren().add(item);
		}

		// https://docs.oracle.com/javafx/2/ui_controls/tree-view.htm
		mapExplorerTreeView.setRoot(rootItem);
	}

	/*
	 * private JComponent createTokenTreePanel() { final JTree tree = new JTree(); tree.setName(I18N.getString("panel.MapExplorer")); tokenPanelTreeModel = new TokenPanelTreeModel(tree);
	 * tree.setModel(tokenPanelTreeModel); tree.setCellRenderer(new TokenPanelTreeCellRenderer()); tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
	 * tree.addMouseListener(new MouseAdapter() { // TODO: Make this a handler class, not an aic
	 * 
	 * @Override public void mousePressed(MouseEvent e) { // tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY())); TreePath path = tree.getPathForLocation(e.getX(), e.getY()); if (path
	 * == null) { return; } Object row = path.getLastPathComponent(); int rowIndex = tree.getRowForLocation(e.getX(), e.getY()); if (SwingUtilities.isLeftMouseButton(e)) { if
	 * (!SwingUtil.isShiftDown(e) && !SwingUtil.isControlDown(e)) { tree.clearSelection(); } tree.addSelectionInterval(rowIndex, rowIndex);
	 * 
	 * if (row instanceof Token) { if (e.getClickCount() == 2) { Token token = (Token) row; getCurrentZoneRenderer().clearSelectedTokens(); // Pick an appropriate tool // Jamz: why not just call
	 * .centerOn(Token token), now we have one place to fix... getCurrentZoneRenderer().centerOn(token); } } } if (SwingUtilities.isRightMouseButton(e)) { if (!isRowSelected(tree.getSelectionRows(),
	 * rowIndex) && !SwingUtil.isShiftDown(e)) { tree.clearSelection(); tree.addSelectionInterval(rowIndex, rowIndex); } final int x = e.getX(); final int y = e.getY(); EventQueue.invokeLater(new
	 * Runnable() { public void run() { Token firstToken = null; Set<GUID> selectedTokenSet = new HashSet<GUID>(); for (TreePath path : tree.getSelectionPaths()) { if (path.getLastPathComponent()
	 * instanceof Token) { Token token = (Token) path.getLastPathComponent(); if (firstToken == null) { firstToken = token; } if (AppUtil.playerOwns(token)) { selectedTokenSet.add(token.getId()); } }
	 * } if (!selectedTokenSet.isEmpty()) { try { if (firstToken.isStamp()) { new StampPopupMenu(selectedTokenSet, x, y, getCurrentZoneRenderer(), firstToken).showPopup(tree); } else { new
	 * TokenPopupMenu(selectedTokenSet, x, y, getCurrentZoneRenderer(), firstToken).showPopup(tree); } } catch (IllegalComponentStateException icse) { log.info(tree.toString(), icse); } } } }); } }
	 * }); MapTool.getEventDispatcher().addListener(new AppEventListener() { public void handleAppEvent(AppEvent event) { tokenPanelTreeModel.setZone((Zone) event.getNewValue()); } },
	 * MapTool.ZoneEvent.Activated); return tree; }
	 * 
	 * public void clearTokenTree() { if (tokenPanelTreeModel != null) { tokenPanelTreeModel.setZone(null); } }
	 * 
	 * public void updateTokenTree() { if (tokenPanelTreeModel != null) { tokenPanelTreeModel.update(); } if (initiativePanel != null) { initiativePanel.update(); } }
	 * 
	 * private boolean isRowSelected(int[] selectedRows, int row) { if (selectedRows == null) { return false; } for (int selectedRow : selectedRows) { if (row == selectedRow) { return true; } } return
	 * false; }
	 * 
	 */
}
