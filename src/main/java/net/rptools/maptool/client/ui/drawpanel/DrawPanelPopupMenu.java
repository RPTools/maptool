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
package net.rptools.maptool.client.ui.drawpanel;

import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import net.rptools.lib.swing.ColorPicker;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.macro.MacroContext;
import net.rptools.maptool.client.ui.AssetPaint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.TextMessage;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablePaint;
import net.rptools.maptool.model.drawing.DrawableTexturePaint;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DrawPanelPopupMenu extends JPopupMenu {

  private static final long serialVersionUID = 8889082158114727461L;
  private final DrawnElement elementUnderMouse;
  private final ZoneRenderer renderer;
  private final boolean topLevelOnly;
  Set<GUID> selectedDrawSet;
  int x, y;

  public DrawPanelPopupMenu(
      Set<GUID> selectedDrawSet,
      int x,
      int y,
      ZoneRenderer renderer,
      DrawnElement elementUnderMouse,
      boolean topLevelOnly) {
    super();
    this.selectedDrawSet = selectedDrawSet;
    this.x = x;
    this.y = y;
    this.renderer = renderer;
    this.elementUnderMouse = elementUnderMouse;
    this.topLevelOnly = topLevelOnly;

    addGMItem(
        createChangeToMenu(
            Zone.Layer.TOKEN, Zone.Layer.GM, Zone.Layer.OBJECT, Zone.Layer.BACKGROUND));
    addGMItem(createArrangeMenu());
    if (isDrawnElementGroup(elementUnderMouse)) {
      add(new UngroupDrawingsAction());
    } else add(new GroupDrawingsAction());
    add(new MergeDrawingsAction());
    addGMItem(new JSeparator());
    add(new DeleteDrawingAction());
    // add(new JSeparator());
    add(new GetPropertiesAction());
    add(new SetPropertiesAction());
    add(new SetDrawingName());
    add(new GetDrawingId());
    addGMItem(new JSeparator());
    add(createPathVblMenu());
    add(createShapeVblMenu());
  }

  private class BringToFrontAction extends AbstractAction {
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

  private class ChangeTypeAction extends AbstractAction {
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
        if (de.getDrawable().getLayer() != this.layer
            && selectedDrawSet.contains(de.getDrawable().getId())) {
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

  private class DeleteDrawingAction extends AbstractAction {
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

  private class GetDrawingId extends AbstractAction {
    public GetDrawingId() {
      super("Get Drawing Id");
    }

    public void actionPerformed(ActionEvent e) {
      String id = elementUnderMouse.getDrawable().getId().toString();
      MacroContext context = new MacroContext();
      MapTool.addMessage(TextMessage.say(context.getTransformationHistory(), id));
    }
  }

  private class GetPropertiesAction extends AbstractAction {
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
        cp.setSquareCapSelected(p.getSquareCap());
      }
    }
  }

  private class GroupDrawingsAction extends AbstractAction {
    public GroupDrawingsAction() {
      super("Group Drawings");
      enabled = selectedDrawSet.size() > 1;
      if (enabled) {
        List<DrawnElement> zoneList =
            renderer.getZone().getDrawnElements(elementUnderMouse.getDrawable().getLayer());
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

  private class MergeDrawingsAction extends AbstractAction {
    public MergeDrawingsAction() {
      super("Merge Drawings");
      enabled = selectedDrawSet.size() > 1;
      if (enabled) {
        List<DrawnElement> zoneList =
            renderer.getZone().getDrawnElements(elementUnderMouse.getDrawable().getLayer());
        for (GUID id : selectedDrawSet) {
          DrawnElement de = renderer.getZone().getDrawnElement(id);
          if (!zoneList.contains(de) || isDrawnElementGroup(de)) {
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
        Iterator<DrawnElement> iter = drawableList.iterator();
        Area a = elementUnderMouse.getDrawable().getArea();
        while (iter.hasNext()) {
          DrawnElement de = iter.next();
          if (selectedDrawSet.contains(de.getDrawable().getId())) {
            renderer.getZone().removeDrawable(de.getDrawable().getId());
            MapTool.serverCommand().undoDraw(renderer.getZone().getId(), de.getDrawable().getId());
            de.getDrawable().setLayer(elementUnderMouse.getDrawable().getLayer());
            if (!de.equals(elementUnderMouse)) a.add(de.getDrawable().getArea());
          }
        }
        Shape s = (Shape) a;
        Pen newPen = new Pen(elementUnderMouse.getPen());
        if (elementUnderMouse.getDrawable() instanceof LineSegment) newPen = invertPen(newPen);
        DrawnElement de = new DrawnElement(new ShapeDrawable(s), newPen);
        de.getDrawable().setLayer(elementUnderMouse.getDrawable().getLayer());
        MapTool.serverCommand().draw(renderer.getZone().getId(), newPen, de.getDrawable());
        MapTool.getFrame().updateDrawTree();
        MapTool.getFrame().refresh();
      }
    }
  }

  private Pen invertPen(Pen pen) {
    Pen newPen = new Pen(pen);
    newPen.setBackgroundPaint(pen.getPaint());
    newPen.setBackgroundMode(pen.getForegroundMode());
    newPen.setPaint(pen.getBackgroundPaint());
    newPen.setForegroundMode(pen.getBackgroundMode());
    return newPen;
  }

  private class SendToBackAction extends AbstractAction {
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

  private class SetDrawingName extends AbstractAction {
    public SetDrawingName() {
      super("Set Name");
    }

    public void actionPerformed(ActionEvent e) {
      String drawType = "Drawing";
      if (elementUnderMouse.getDrawable() instanceof DrawnElement) drawType = "Group";
      AbstractDrawing group = (AbstractDrawing) elementUnderMouse.getDrawable();
      String groupName =
          (String)
              JOptionPane.showInputDialog(
                  MapTool.getFrame(),
                  "Enter a name for the " + drawType,
                  drawType + " Name",
                  JOptionPane.QUESTION_MESSAGE,
                  null,
                  null,
                  group.getName());
      group.setName(groupName == "" ? null : groupName);
      MapTool.getFrame().updateDrawTree();
    }
  }

  private class SetPropertiesAction extends AbstractAction {
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
      p.setSquareCap(cp.isSquareCapSelected());
      MapTool.getFrame().updateDrawTree();
      MapTool.serverCommand().updateDrawing(renderer.getZone().getId(), p, elementUnderMouse);
    }
  }

  private class UngroupDrawingsAction extends AbstractAction {
    public UngroupDrawingsAction() {
      super("Ungroup");
      enabled = selectedDrawSet.size() == 1 && isDrawnElementGroup(elementUnderMouse);
    }

    public void actionPerformed(ActionEvent e) {
      MapTool.serverCommand()
          .undoDraw(renderer.getZone().getId(), elementUnderMouse.getDrawable().getId());
      DrawablesGroup dg = (DrawablesGroup) ((DrawnElement) elementUnderMouse).getDrawable();
      for (DrawnElement de : dg.getDrawableList()) {
        MapTool.serverCommand().draw(renderer.getZone().getId(), de.getPen(), de.getDrawable());
      }
    }
  }

  /**
   * Menu items for VBL section. Calls VblTool
   *
   * @param pathOnly - boolean, just path if true, otherwise fill shape.
   * @param isEraser - boolean, erase VBL if true.
   */
  private class VblAction extends AbstractAction {
    private final boolean isEraser;
    private final boolean pathOnly;

    public VblAction(boolean pathOnly, boolean isEraser) {
      super((isEraser ? "Remove from " : "Add to ") + "VBL");
      enabled = hasPath(elementUnderMouse);
      this.isEraser = isEraser;
      this.pathOnly = pathOnly;
    }

    public void actionPerformed(ActionEvent e) {
      List<DrawnElement> drawableList = renderer.getZone().getAllDrawnElements();
      for (GUID guid : selectedDrawSet) {
        DrawnElement de = findDrawnElement(drawableList, guid);
        if (de != null) VblTool(de.getDrawable(), pathOnly, isEraser);
      }
    }
  }

  private DrawnElement findDrawnElement(List<DrawnElement> drawableList, GUID guid) {
    Iterator<DrawnElement> iter = drawableList.iterator();
    while (iter.hasNext()) {
      DrawnElement de = iter.next();
      if (de.getDrawable().getId().equals(guid)) {
        return de;
      }
      if (de.getDrawable() instanceof DrawablesGroup) {
        DrawnElement result =
            findDrawnElement(((DrawablesGroup) de.getDrawable()).getDrawableList(), guid);
        if (result != null) return result;
      }
    }
    return null;
  }

  private void addGMItem(JMenu menu) {
    if (menu == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) {
      add(menu);
    }
  }

  private void addGMItem(JSeparator separator) {
    if (separator == null) {
      return;
    }
    if (MapTool.getPlayer().isGM()) add(separator);
  }

  private JMenu createArrangeMenu() {
    JMenu arrangeMenu = new JMenu("Arrange");
    arrangeMenu.setEnabled(topLevelOnly);
    JMenuItem bringToFrontMenuItem = new JMenuItem("Bring to Front");
    bringToFrontMenuItem.addActionListener(new BringToFrontAction());

    JMenuItem sendToBackMenuItem = new JMenuItem("Send to Back");
    sendToBackMenuItem.addActionListener(new SendToBackAction());

    arrangeMenu.add(bringToFrontMenuItem);
    arrangeMenu.add(sendToBackMenuItem);

    return arrangeMenu;
  }

  private JMenu createChangeToMenu(Zone.Layer... types) {
    JMenu changeTypeMenu = new JMenu("Change to");
    changeTypeMenu.setEnabled(topLevelOnly);
    for (Zone.Layer layer : types) {
      changeTypeMenu.add(new JMenuItem(new ChangeTypeAction(layer)));
    }
    return changeTypeMenu;
  }

  private JMenu createPathVblMenu() {
    JMenu pathVblMenu = new JMenu("Path to VBL");
    pathVblMenu.setEnabled(hasPath(elementUnderMouse));
    pathVblMenu.add(new JMenuItem(new VblAction(true, false)));
    pathVblMenu.add(new JMenuItem(new VblAction(true, true)));
    return pathVblMenu;
  }

  private JMenu createShapeVblMenu() {
    JMenu shapeVblMenu = new JMenu("Shape to VBL");
    shapeVblMenu.setEnabled(hasPath(elementUnderMouse));
    shapeVblMenu.add(new JMenuItem(new VblAction(false, false)));
    shapeVblMenu.add(new JMenuItem(new VblAction(false, true)));
    return shapeVblMenu;
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

  /**
   * Tests to see if the selected object has a drawn path
   *
   * @param drawnElement
   * @return boolean
   */
  private boolean hasPath(DrawnElement drawnElement) {
    if (drawnElement.getDrawable() instanceof LineSegment) return true;
    if (drawnElement.getDrawable() instanceof ShapeDrawable) {
      ShapeDrawable sd = (ShapeDrawable) drawnElement.getDrawable();
      if ("Float".equalsIgnoreCase(sd.getShape().getClass().getSimpleName())) return false;
      return true;
    }
    return false;
  }

  private boolean isDrawnElementGroup(Object object) {
    if (object instanceof DrawnElement)
      return ((DrawnElement) object).getDrawable() instanceof DrawablesGroup;
    return false;
  }

  public void showPopup(JComponent component) {
    show(component, x, y);
  }

  /**
   * Takes a drawable and adds or removes its shape or path from the VBL
   *
   * @param drawable
   * @param pathOnly - boolean, just path if true, otherwise fill shape.
   * @param isEraser - boolean, erase VBL if true.
   */
  private void VblTool(Drawable drawable, boolean pathOnly, boolean isEraser) {
    Area area = new Area();

    if (drawable instanceof LineSegment) {
      Path2D path = getPath(drawable);
      BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
      area.add(new Area(stroke.createStrokedShape(path)));
    } else {
      if (pathOnly) {
        BasicStroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        area.add(new Area(stroke.createStrokedShape(((ShapeDrawable) drawable).getShape())));
      } else {
        area = new Area(((ShapeDrawable) drawable).getShape());
      }
    }
    if (isEraser) {
      renderer.getZone().removeTopology(area);
      MapTool.serverCommand().removeTopology(renderer.getZone().getId(), area);
    } else {
      renderer.getZone().addTopology(area);
      MapTool.serverCommand().addTopology(renderer.getZone().getId(), area);
    }
    renderer.repaint();
  }

  private Path2D getPath(Drawable drawable) {
    if (drawable instanceof LineSegment) {
      LineSegment line = (LineSegment) drawable;
      Path2D path = new Path2D.Double();
      Point lastPoint = null;

      for (Point point : line.getPoints()) {
        if (path.getCurrentPoint() == null) {
          path.moveTo(point.x, point.y);
        } else if (!point.equals(lastPoint)) {
          path.lineTo(point.x, point.y);
          lastPoint = point;
        }
      }
      return path;
    }
    return null;
  }
}
