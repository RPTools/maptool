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
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/** Tool for drawing freehand lines. */
public class FreehandTool extends AbstractLineTool implements MouseMotionListener {
  private static final long serialVersionUID = 3904963036442998837L;

  public FreehandTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/draw-blue-freehndlines.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    // Don't ever show measurement drawing with freehand tool
    drawMeasurementDisabled = true;
  }

  @Override
  public String getTooltip() {
    return "tool.freehand.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.freehand.instructions";
  }

  ////
  // MOUSE LISTENER
  @Override
  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      startLine(e);
      setIsEraser(isEraser(e));
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      stopLine(e);
    }
    super.mouseReleased(e);
  }

  ////
  // MOUSE MOTION LISTENER
  @Override
  public void mouseDragged(java.awt.event.MouseEvent e) {
    addPoint(e);
    super.mouseDragged(e);
  }
}
