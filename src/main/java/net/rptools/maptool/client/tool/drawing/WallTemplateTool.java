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
package net.rptools.maptool.client.tool.drawing;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.LineTemplate;
import net.rptools.maptool.model.drawing.WallTemplate;

/**
 * A tool to draw a wall template for 4e D&D
 *
 * @author Jay
 */
public class WallTemplateTool extends BurstTemplateTool {

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /** Set the icon for the base tool. */
  public WallTemplateTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/temp-blue-wall.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } // endtry
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden RadiusTemplateTool methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#createBaseTemplate() */
  @Override
  protected AbstractTemplate createBaseTemplate() {
    return new WallTemplate();
  }

  /** @see net.rptools.maptool.client.ui.Tool#getTooltip() */
  @Override
  public String getTooltip() {
    return "tool.walltemplate.tooltip";
  }

  /** @see net.rptools.maptool.client.ui.Tool#getInstructions() */
  @Override
  public String getInstructions() {
    return "tool.walltemplate.instructions";
  }

  /**
   * @see
   *     net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    if (!painting) return;

    // Set up the path when the anchor is pressed.
    if (SwingUtilities.isLeftMouseButton(e) && !anchorSet) {
      LineTemplate lt = ((LineTemplate) template);
      lt.clearPath();
      ArrayList<CellPoint> path = new ArrayList<CellPoint>();
      path.add(lt.getPointFromPool(0, 0));
      lt.setPath(path);
    } // endif
    super.mousePressed(e);
  }

  /**
   * @see
   *     net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#handleMouseMovement(java.awt.event.MouseEvent)
   */
  @Override
  protected void handleMouseMovement(MouseEvent e) {
    // Set the anchor
    ZonePoint vertex = template.getVertex();
    if (!anchorSet) {
      setCellAtMouse(e, vertex);
      controlOffset = null;

      // Move the anchor if control pressed.
    } else if (SwingUtil.isControlDown(e)) {
      handleControlOffset(e, vertex);

      // Add or delete a new cell
    } else {

      // Get mouse point as an offset from the vertex
      LineTemplate lt = ((LineTemplate) template);
      ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
      CellPoint mousePoint = renderer.getZone().getGrid().convert(mouse);
      CellPoint vertexPoint = renderer.getZone().getGrid().convert(lt.getVertex());
      mousePoint.x = mousePoint.x - vertexPoint.x;
      mousePoint.y = mousePoint.y - vertexPoint.y;

      // Compare to the second to last point, if == delete last point
      List<CellPoint> path = lt.getPath();
      CellPoint lastPoint = path.get(path.size() - 1);
      int dx = mousePoint.x - lastPoint.x;
      int dy = mousePoint.y - lastPoint.y;
      if (dx != 0 && dy == 0 || dy != 0 && dx == 0) {
        int count = Math.max(Math.abs(dy), Math.abs(dx));
        dx = dx == 0 ? 0 : dx / Math.abs(dx);
        dy = dy == 0 ? 0 : dy / Math.abs(dy);
        for (int i = 1; i <= count; i++) {
          CellPoint current = lt.getPointFromPool(lastPoint.x + dx * i, lastPoint.y + dy * i);
          if (path.size() > 1 && path.get(path.size() - 2).equals(current)) {
            lt.addPointToPool(path.remove(path.size() - 1));
            lt.addPointToPool(current);
          } else {
            path.add(current);
          } // endif
        } // endfor
      } // endif
      renderer.repaint();
      controlOffset = null;
    } // endif
  }

  /** @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#resetTool() */
  @Override
  protected void resetTool() {
    super.resetTool();
    ((WallTemplate) template).clearPath();
  }
}
