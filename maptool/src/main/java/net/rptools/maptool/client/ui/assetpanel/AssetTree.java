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

import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;

import net.rptools.lib.swing.PopupListener;
import net.rptools.maptool.client.AppActions;

/**
 */
public class AssetTree extends JTree implements TreeSelectionListener {

	private Directory selectedDirectory;
	private AssetPanel assetPanel;

	public AssetTree(AssetPanel assetPanel) {
		super(assetPanel.getModel().getImageFileTreeModel());

		this.assetPanel = assetPanel;

		setCellRenderer(new AssetTreeCellRenderer());

		addMouseListener(createPopupListener());
		addTreeSelectionListener(this);

		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		getSelectionModel().addTreeSelectionListener(this);
	}

	@Override
	public int getRowHeight() {
		return -1;
	}

	public Directory getSelectedAssetGroup() {
		return selectedDirectory;
	}

	private MouseListener createPopupListener() {

		PopupListener listener = new PopupListener(createPopupMenu());

		return listener;
	}

	private JPopupMenu createPopupMenu() {

		JPopupMenu menu = new JPopupMenu();
		menu.add(new JMenuItem(AppActions.REMOVE_ASSET_ROOT));

		return menu;
	}

	public void refresh() {
		((ImageFileTreeModel) getModel()).refresh();
	}

	////
	// Tree Selection Listener
	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) {

		// Keep memory tight
		// TODO: make this an option
		if (selectedDirectory != null) {
			selectedDirectory.refresh();
		}

		selectedDirectory = null;

		Object node = e.getPath().getLastPathComponent();

		if (node instanceof Directory) {

			selectedDirectory = ((Directory) node);
			assetPanel.setDirectory((Directory) node);
		}
	}
}
