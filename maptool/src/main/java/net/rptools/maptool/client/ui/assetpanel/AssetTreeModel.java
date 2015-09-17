/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui.assetpanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.rptools.maptool.model.AssetGroup;

/**
 */
public class AssetTreeModel implements TreeModel {
	private final List<AssetGroup> rootAssetGroups = new ArrayList<AssetGroup>();
	private final Object root = new String("Images");
	private final List<TreeModelListener> listenerList = new ArrayList<TreeModelListener>();

	public AssetTreeModel() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	public Object getRoot() {
		return root;
	}

	public void addRootGroup(AssetGroup group) {
		rootAssetGroups.add(group);
		Collections.sort(rootAssetGroups, AssetGroup.GROUP_COMPARATOR);
		fireNodesInsertedEvent(new TreeModelEvent(this, new Object[] { getRoot() }, new int[] { rootAssetGroups.size() - 1 }, new Object[] { group }));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	public Object getChild(Object parent, int index) {
		if (parent == root) {
			return rootAssetGroups.get(index);
		}
		AssetGroup group = (AssetGroup) parent;
		return group.getChildGroups().get(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	public int getChildCount(Object parent) {
		if (parent == root) {
			return rootAssetGroups.size();
		}
		AssetGroup group = (AssetGroup) parent;
		return group.getChildGroupCount();
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
			return rootAssetGroups.indexOf(child);
		}
		AssetGroup group = (AssetGroup) parent;
		return group.indexOf((AssetGroup) child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void addTreeModelListener(TreeModelListener l) {
		listenerList.add(l);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
	 */
	public void removeTreeModelListener(TreeModelListener l) {
		listenerList.remove(l);
	}

	public void refresh() {
		for (AssetGroup group : rootAssetGroups) {
			group.updateGroup();
			fireStructureChangedEvent(new TreeModelEvent(this, new Object[] { getRoot(), group }, new int[] { 0 }, new Object[] {}));
		} // endfor      
	}

	private void fireStructureChangedEvent(TreeModelEvent e) {
		TreeModelListener[] listeners = listenerList.toArray(new TreeModelListener[listenerList.size()]);
		for (TreeModelListener listener : listeners) {
			listener.treeStructureChanged(e);
		}
	}

	private void fireNodesInsertedEvent(TreeModelEvent e) {
		TreeModelListener[] listeners = listenerList.toArray(new TreeModelListener[listenerList.size()]);
		for (TreeModelListener listener : listeners) {
			listener.treeNodesInserted(e);
		}
	}
}
