package net.rptools.maptool.client.ui.drawpanel;

import java.awt.event.ActionEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawnElement;

public class DrawPanelPopupMenu extends JPopupMenu {
	
	private static final long serialVersionUID = 8889082158114727461L;
	private final ZoneRenderer renderer;
	private final DrawnElement elementUnderMouse;
	int x, y;
	Set<GUID> selectedDrawSet;
	
	public DrawPanelPopupMenu(Set<GUID> selectedDrawSet, int x, int y, ZoneRenderer renderer, DrawnElement elementUnderMouse) {
		super();
		this.renderer = renderer;
		this.elementUnderMouse = elementUnderMouse;
		this.x = x;
		this.y = y;

		addGMItem(createChangeToMenu(Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND));
		addGMItem(createArrangeMenu());
		add(new JSeparator());
		add(new DeleteDrawingAction());
	}
	
	public class DeleteDrawingAction extends AbstractAction {
		public DeleteDrawingAction() {
			super("Delete");
		}
		public void actionPerformed(ActionEvent e) {
			
		}
	}

	protected void addGMItem(JMenu menu) {
		if (menu == null) {
			return;
		}
		if (MapTool.getPlayer().isGM()) {
			add(menu);
		}
	}

	protected JMenu createChangeToMenu(Zone.Layer... types) {
		JMenu changeTypeMenu = new JMenu("Change to");
		for (Zone.Layer layer : types) {
			changeTypeMenu.add(new JMenuItem(new ChangeTypeAction(layer)));
		}
		return changeTypeMenu;
	}

	public class ChangeTypeAction extends AbstractAction {
		private final Zone.Layer layer;

		public ChangeTypeAction(Zone.Layer layer) {
			putValue(Action.NAME, layer.toString());
			this.layer = layer;
		}

		public void actionPerformed(ActionEvent e) {
		}
	}

	protected JMenu createArrangeMenu() {
		JMenu arrangeMenu = new JMenu("Arrange");
		JMenuItem bringToFrontMenuItem = new JMenuItem("Bring to Front");
		bringToFrontMenuItem.addActionListener(new BringToFrontAction());

		JMenuItem sendToBackMenuItem = new JMenuItem("Send to Back");
		sendToBackMenuItem.addActionListener(new SendToBackAction());

		arrangeMenu.add(bringToFrontMenuItem);
		arrangeMenu.add(sendToBackMenuItem);

		return arrangeMenu;
	}

	public class BringToFrontAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			//MapTool.serverCommand().bringTokensToFront(renderer.getZone().getId(), selectedTokenSet);
			//MapTool.getFrame().refresh();
		}
	}

	public class SendToBackAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			//MapTool.serverCommand().sendTokensToBack(renderer.getZone().getId(), selectedTokenSet);
			//MapTool.getFrame().refresh();
		}
	}

	public void showPopup(JComponent component) {
		show(component, x, y);
	}
}
