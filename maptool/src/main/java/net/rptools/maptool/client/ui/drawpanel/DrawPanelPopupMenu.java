/*
 * This software Copyright by the RPTools.net development team, and licensed
 * under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this source Code. If not, see <http://www.gnu.org/licenses/>
 */

package net.rptools.maptool.client.ui.drawpanel;

import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import net.rptools.lib.swing.ColorPicker;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.AssetPaint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DrawPanelPopupMenu extends JPopupMenu {

	private static final long serialVersionUID = 8889082158114727461L;
	private final ZoneRenderer renderer;
	private final DrawnElement elementUnderMouse;
	int x, y;
	Set<GUID> selectedDrawSet;

	public DrawPanelPopupMenu(Set<GUID> selectedDrawSet, int x, int y, ZoneRenderer renderer, DrawnElement elementUnderMouse) {
		super();
		this.selectedDrawSet = selectedDrawSet;
		this.x = x;
		this.y = y;
		this.renderer = renderer;
		this.elementUnderMouse = elementUnderMouse;

		addGMItem(createChangeToMenu(Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND));
		addGMItem(createArrangeMenu());
		if (isDrawnElementGroup(elementUnderMouse))
			add(new UngroupDrawingsAction());
		else
			add(new GroupDrawingsAction());
		add(new MergeDrawingsAction());
		addGMItem(new JSeparator());
		add(new DeleteDrawingAction());
		// TODO add properties action as stage two
		//add(new JSeparator());
		add(new GetPropertiesAction());
		add(new SetPropertiesAction());
	}

	private boolean isDrawnElementGroup(Object object) {
		if (object instanceof DrawnElement)
			return ((DrawnElement) object).getDrawable() instanceof DrawablesGroup;
		return false;
	}

	public class GetPropertiesAction extends AbstractAction {
		public GetPropertiesAction() {
			super("Get Properties");
		}

		public void actionPerformed(ActionEvent e) {
			ColorPicker cp = MapTool.getFrame().getColorPicker();
			Pen p = elementUnderMouse.getPen();
			Drawable d = elementUnderMouse.getDrawable();

			if (d instanceof AbstractDrawing) {
				AbstractDrawing ad = (AbstractDrawing) d;
				cp.setForegroundPaint(getPaint(p.getPaint(), ad));
				cp.setBackgroundPaint(getPaint(p.getBackgroundPaint(), ad));
				cp.setPenWidth((int) p.getThickness());
				cp.setTranslucency((int) (p.getOpacity() * 100));
				cp.setEraseSelected(p.isEraser());
			}
		}
	}

	private Paint getPaint(DrawablePaint paint, AbstractDrawing ad) {
		if (paint instanceof DrawableColorPaint) {
			return paint.getPaint(ad);
		}
		if (paint instanceof DrawableTexturePaint) {
			DrawableTexturePaint dtp = (DrawableTexturePaint) paint;
			return new AssetPaint(dtp.getAsset());
		}
		return null;
	}

	public class SetPropertiesAction extends AbstractAction {
		public SetPropertiesAction() {
			super("Set Properties");
		}

		public void actionPerformed(ActionEvent e) {
			ColorPicker cp = MapTool.getFrame().getColorPicker();
			Pen p = elementUnderMouse.getPen();
			if (cp.getForegroundPaint() != null) {
				p.setPaint(DrawablePaint.convertPaint(cp.getForegroundPaint()));
				p.setForegroundMode(0);
			} else {
				p.setPaint(null);
				p.setForegroundMode(1);
			}
			if (cp.getBackgroundPaint() != null) {
				p.setBackgroundPaint(DrawablePaint.convertPaint(cp.getBackgroundPaint()));
				p.setBackgroundMode(0);
			} else {
				p.setBackgroundPaint(null);
				p.setBackgroundMode(1);
			}
			p.setThickness(cp.getStrokeWidth());
			p.setOpacity(cp.getOpacity());
			p.setThickness(cp.getStrokeWidth());
			p.setEraser(cp.isEraseSelected());
			MapTool.getFrame().updateDrawTree();
			MapTool.serverCommand().updateDrawing(renderer.getZone().getId(), p, elementUnderMouse);
		}
	}

	public class UngroupDrawingsAction extends AbstractAction {
		public UngroupDrawingsAction() {
			super("Ungroup");
			enabled = selectedDrawSet.size() == 1 && isDrawnElementGroup(elementUnderMouse);
		}

		public void actionPerformed(ActionEvent e) {
			MapTool.serverCommand().undoDraw(renderer.getZone().getId(), elementUnderMouse.getDrawable().getId());
			DrawablesGroup dg = (DrawablesGroup) ((DrawnElement) elementUnderMouse).getDrawable();
			for (DrawnElement de : dg.getDrawableList()) {
				MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
			}
		}
	}

	public class GroupDrawingsAction extends AbstractAction {
		public GroupDrawingsAction() {
			super("Group Drawings");
			enabled = selectedDrawSet.size() > 1;
			if (enabled) {
				List<DrawnElement> zoneList = renderer.getZone().getDrawnElements(elementUnderMouse.getDrawable().getLayer());
				for (GUID id : selectedDrawSet) {
					DrawnElement de = renderer.getZone().getDrawnElement(id);
					if (!zoneList.contains(de)) {
						enabled = false;
						break;
					}
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (selectedDrawSet.size() > 1 && elementUnderMouse != null) {
				// only bother doing stuff if more than one selected
				List<DrawnElement> drawableList = renderer.getZone().getAllDrawnElements();
				List<DrawnElement> groupList = new ArrayList<DrawnElement>();
				Iterator<DrawnElement> iter = drawableList.iterator();
				Pen pen = new Pen(elementUnderMouse.getPen());
				pen.setEraser(false);
				pen.setOpacity(1);
				while (iter.hasNext()) {
					DrawnElement de = iter.next();
					if (selectedDrawSet.contains(de.getDrawable().getId())) {
						renderer.getZone().removeDrawable(de.getDrawable().getId());
						MapTool.serverCommand().undoDraw(renderer.getZone().getId(), de.getDrawable().getId());
						de.getDrawable().setLayer(elementUnderMouse.getDrawable().getLayer());
						groupList.add(de);
						if (de.getPen().getThickness() > pen.getThickness()) {
							pen = new Pen(de.getPen());
							pen.setEraser(false);
							pen.setOpacity(1);
						}
					}
				}
				DrawnElement de = new DrawnElement(new DrawablesGroup(groupList), pen);
				de.getDrawable().setLayer(elementUnderMouse.getDrawable().getLayer());
				MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
				MapTool.getFrame().updateDrawTree();
				MapTool.getFrame().refresh();
			}
		}
	}

	public class MergeDrawingsAction extends AbstractAction {
		public MergeDrawingsAction() {
			super("Merge Drawings");
			enabled = selectedDrawSet.size() > 1;
			if (enabled) {
				List<DrawnElement> zoneList = renderer.getZone().getDrawnElements(elementUnderMouse.getDrawable().getLayer());
				for (GUID id : selectedDrawSet) {
					DrawnElement de = renderer.getZone().getDrawnElement(id);
					if (!zoneList.contains(de)) {
						enabled = false;
						break;
					}
				}
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (selectedDrawSet.size() > 1 && elementUnderMouse != null) {
				// only bother doing stuff if more than one selected
				List<DrawnElement> drawableList = renderer.getZone().getAllDrawnElements();
				List<DrawnElement> groupList = new ArrayList<DrawnElement>();
				Iterator<DrawnElement> iter = drawableList.iterator();
				Area a = elementUnderMouse.getDrawable().getArea();
				while (iter.hasNext()) {
					DrawnElement de = iter.next();
					if (selectedDrawSet.contains(de.getDrawable().getId())) {
						renderer.getZone().removeDrawable(de.getDrawable().getId());
						MapTool.serverCommand().undoDraw(renderer.getZone().getId(), de.getDrawable().getId());
						de.getDrawable().setLayer(elementUnderMouse.getDrawable().getLayer());
						if (!de.equals(elementUnderMouse))
							a.add(de.getDrawable().getArea());
					}
				}
				Shape s = (Shape) a;
				DrawnElement de = new DrawnElement(new ShapeDrawable(s), elementUnderMouse.getPen());
				de.getDrawable().setLayer(elementUnderMouse.getDrawable().getLayer());
				MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
				MapTool.getFrame().updateDrawTree();
				MapTool.getFrame().refresh();
			}
		}
	}

	public class DeleteDrawingAction extends AbstractAction {
		public DeleteDrawingAction() {
			super("Delete");
		}

		public void actionPerformed(ActionEvent e) {
			// check to see if this is the required action
			if (!MapTool.confirmDrawDelete()) {
				return;
			}
			for (GUID id : selectedDrawSet) {
				MapTool.serverCommand().undoDraw(renderer.getZone().getId(), id);
			}
			renderer.repaint();
			MapTool.getFrame().updateDrawTree();
			MapTool.getFrame().refresh();
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

	protected void addGMItem(JSeparator separator) {
		if (separator == null) {
			return;
		}
		if (MapTool.getPlayer().isGM())
			add(separator);
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
			List<DrawnElement> drawableList = renderer.getZone().getAllDrawnElements();
			Iterator<DrawnElement> iter = drawableList.iterator();
			while (iter.hasNext()) {
				DrawnElement de = iter.next();
				if (de.getDrawable().getLayer() != this.layer && selectedDrawSet.contains(de.getDrawable().getId())) {
					renderer.getZone().removeDrawable(de.getDrawable().getId());
					MapTool.serverCommand().undoDraw(renderer.getZone().getId(), de.getDrawable().getId());
					de.getDrawable().setLayer(this.layer);
					renderer.getZone().addDrawable(de);
					MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
				}
			}
			MapTool.getFrame().updateDrawTree();
			MapTool.getFrame().refresh();
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
			List<DrawnElement> drawableList = renderer.getZone().getAllDrawnElements();
			Iterator<DrawnElement> iter = drawableList.iterator();
			while (iter.hasNext()) {
				DrawnElement de = iter.next();
				if (selectedDrawSet.contains(de.getDrawable().getId())) {
					renderer.getZone().removeDrawable(de.getDrawable().getId());
					MapTool.serverCommand().undoDraw(renderer.getZone().getId(), de.getDrawable().getId());
					renderer.getZone().addDrawable(new DrawnElement(de.getDrawable(), de.getPen()));
					MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
				}
			}
			MapTool.getFrame().updateDrawTree();
			MapTool.getFrame().refresh();
		}
	}

	public class SendToBackAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			List<DrawnElement> drawableList = renderer.getZone().getAllDrawnElements();
			Iterator<DrawnElement> iter = drawableList.iterator();
			while (iter.hasNext()) {
				DrawnElement de = iter.next();
				if (selectedDrawSet.contains(de.getDrawable().getId())) {
					renderer.getZone().removeDrawable(de.getDrawable().getId());
					renderer.getZone().addDrawableRear(de);
				}
			}
			// horrid kludge needed to redraw zone :(
			for (DrawnElement de : renderer.getZone().getAllDrawnElements()) {
				MapTool.serverCommand().undoDraw(renderer.getZone().getId(), de.getDrawable().getId());
				MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
			}
			MapTool.getFrame().updateDrawTree();
			MapTool.getFrame().refresh();
		}
	}

	public void showPopup(JComponent component) {
		show(component, x, y);
	}
}
