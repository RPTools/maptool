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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.drawing.AbstractDrawing;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.LineSegment;
import net.rptools.maptool.model.drawing.Pen;
import net.rptools.maptool.model.drawing.ShapeDrawable;

public class DrawPanelTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final long serialVersionUID = 499441097273543074L;
  private int row;
  private int rowWidth;

  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {

    setBorder(null);
    setBackgroundNonSelectionColor(Color.white);

    String text = "";
    this.row = row;
    if (value instanceof DrawnElement) {
      String key = "panel.DrawExplorer.Unknown.Shape";
      DrawnElement de = (DrawnElement) value;
      text = de.getDrawable().toString();
      if (de.getDrawable() instanceof DrawablesGroup) {
        text = "Group";
      } else if (de.getDrawable() instanceof ShapeDrawable) {
        ShapeDrawable sd = (ShapeDrawable) de.getDrawable();
        key =
            String.format(
                "panel.DrawExplorer.%s.%s",
                sd.getClass().getSimpleName(), sd.getShape().getClass().getSimpleName());
        text = I18N.getText(key, sd.getBounds().width, sd.getBounds().height);
        setLeafIcon(setDrawPanelIcon(key, de.getPen().isEraser()));
      } else if (de.getDrawable() instanceof LineSegment) {
        LineSegment ls = (LineSegment) de.getDrawable();
        key = String.format("panel.DrawExplorer.%s.Line", ls.getClass().getSimpleName());
        text = I18N.getText(key, ls.getPoints().size(), de.getPen().getThickness());
        setLeafIcon(setDrawPanelIcon(key, de.getPen().isEraser()));
      } else if (de.getDrawable() instanceof AbstractTemplate) {
        AbstractTemplate at = (AbstractTemplate) de.getDrawable();
        key = String.format("panel.DrawExplorer.Template.%s", at.getClass().getSimpleName());
        text = I18N.getText(key, at.getRadius());
        setLeafIcon(setDrawPanelIcon(key, de.getPen().isEraser()));
      }
      text = addText(de.getPen(), text, de.getDrawable());
    } else if (value instanceof DrawPanelTreeModel.View) {
      DrawPanelTreeModel.View view = (DrawPanelTreeModel.View) value;
      text = view.getLayer().toString();
    } else {
      // setLeafIcon(null);
    }

    super.getTreeCellRendererComponent(tree, text, sel, expanded, leaf, row, hasFocus);
    Icon icon = getIcon();
    rowWidth =
        (icon != null ? icon.getIconWidth() + 2 : 0)
            + SwingUtilities.computeStringWidth(getFontMetrics(getFont()), text);

    return this;
  }

  private String addText(Pen pen, String text, Drawable drawing) {
    if (pen == null) return text;
    String result = text;
    if (pen.isEraser()) result = "CUT: " + result;
    if (pen.getOpacity() < 1) {
      int perc = (int) (pen.getOpacity() * 100);
      result = result + String.format(" opacity %s%%", perc);
    }
    if (drawing instanceof AbstractDrawing) {
      String dName = ((AbstractDrawing) drawing).getName();
      if (dName != null && !"".equals(dName)) result = dName + ": " + result;
    }
    return result;
  }

  private Icon setDrawPanelIcon(String key, boolean eraser) {
    try {
      switch (key) {
        case "panel.DrawExplorer.ShapeDrawable.Area":
          if (eraser)
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-poly-erase.png")));
          else
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-poly.png")));
        case "panel.DrawExplorer.ShapeDrawable.Polygon":
          if (eraser)
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-poly-erase.png")));
          else
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-poly.png")));
        case "panel.DrawExplorer.ShapeDrawable.Float":
          if (eraser)
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-ellipse-erase.png")));
          else
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-ellipse.png")));
        case "panel.DrawExplorer.ShapeDrawable.Rectangle":
          if (eraser)
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-rectangle-erase.png")));
          else
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-rectangle.png")));
        case "panel.DrawExplorer.LineSegment.Line":
          if (eraser)
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-line-erase.png")));
          else
            return new ImageIcon(
                ImageIO.read(
                    getClass()
                        .getClassLoader()
                        .getResourceAsStream(
                            "net/rptools/maptool/client/image/tool/drawpanel-line.png")));
        case "panel.DrawExplorer.Template.RadiusTemplate":
          return new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/drawpanel-temp-blue.png")));
        case "panel.DrawExplorer.Template.ConeTemplate":
          return new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/drawpanel-temp-blue-cone.png")));
        case "panel.DrawExplorer.Template.LineTemplate":
          return new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/drawpanel-temp-blue-line.png")));
        case "panel.DrawExplorer.Template.BurstTemplate":
          return new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/drawpanel-temp-blue-burst.png")));
        case "panel.DrawExplorer.Template.BlastTemplate":
          return new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/drawpanel-temp-blue-square.png")));
        case "panel.DrawExplorer.Template.WallTemplate":
          return new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/drawpanel-temp-blue-wall.png")));
      }
    } catch (IOException e) {
      return null;
    }
    return null;
  }

  @Override
  public Dimension getPreferredSize() {
    // hides the unnecessary root row
    // not sure why this method is used rather than tree.setRootVisible(false)
    // but keep for consistency with other panels
    int height = row > 0 ? getFontMetrics(getFont()).getHeight() + 4 : 0;
    return new Dimension(super.getPreferredSize().width, height);
  }
}
