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

package net.rptools.maptool.client.ui.io;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

/**
 * @author crash
 * 
 */
@SuppressWarnings("serial")
class CustomTreeCellRenderer extends JCheckBox implements TreeCellRenderer {
	DefaultMutableTreeNode node;
	MaptoolNode mtnode;

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
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
