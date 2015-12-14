package net.rptools.maptool.client.ui.drawpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DrawPanelTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = 499441097273543074L;
	private int row;
	private int rowWidth;

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
			String key = "panel.DrawExplorer.Unknown.Shape";
			DrawnElement de = (DrawnElement) value;
			text = de.getDrawable().toString();
			if (de.getDrawable() instanceof ShapeDrawable) {
				ShapeDrawable sd = (ShapeDrawable)de.getDrawable();
				//text = sd.getClass().getSimpleName() + " " + sd.getShape().getClass().getSimpleName();
				key = String.format("panel.DrawExplorer.%s.%s", sd.getClass().getSimpleName(), sd.getShape().getClass().getSimpleName());
				text = I18N.getText(key);
			} else if (de.getDrawable() instanceof LineSegment) {
				LineSegment ls = (LineSegment)de.getDrawable();
				key = String.format("panel.DrawExplorer.%s.Line", ls.getClass().getSimpleName());
				text = I18N.getText(key, ls.getPoints().size());
			} else if (de.getDrawable() instanceof AbstractTemplate) {
				key = String.format("panel.DrawExplorer.Template.%s", de.getDrawable().getClass().getSimpleName());
				text = I18N.getText(key);
			}
			if (de.getPen().isEraser())
				text = "CUT: "+text;
		} else if (value instanceof DrawPanelTreeModel.View) {
			DrawPanelTreeModel.View view = (DrawPanelTreeModel.View) value;
			text = view.getLayer().name();
		} else {
			//setLeafIcon(null);
		}

		super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);
		Icon icon = getIcon();
		rowWidth = (icon != null ? icon.getIconWidth() + 2 : 0) + SwingUtilities.computeStringWidth(getFontMetrics(getFont()), text);
		
		return this;
    }

	@Override
	public Dimension getPreferredSize() {
		// hides the unnecessary root row
		int height = row > 0 ? getFontMetrics(getFont()).getHeight() + 4 : 0;
		return new Dimension(super.getPreferredSize().width, height);
	}
}
