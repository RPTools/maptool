package net.rptools.maptool.client.ui.drawpanel;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DrawPanelTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 499441097273543074L;
	private int row;

    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf, int row,
                                                  boolean hasFocus) {

		setBorder(null);
		setBackgroundNonSelectionColor(Color.white);

		String text = "";
		this.row = row;
		if (value instanceof DrawnElement) {
			DrawnElement de = (DrawnElement) value;
			text = de.getDrawable().toString();
			if (de.getDrawable() instanceof ShapeDrawable) {
				text = ((ShapeDrawable)de.getDrawable()).getShape().toString();
			} else if (de.getDrawable() instanceof LineSegment) {
				
			}
		}
		if (value instanceof DrawPanelTreeModel.View) {
			DrawPanelTreeModel.View view = (DrawPanelTreeModel.View) value;
			text = view.getLayer().name();
		}

		super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);
		
		return this;
    }
}
