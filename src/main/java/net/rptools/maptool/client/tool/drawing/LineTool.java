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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

/** Tool for drawing freehand lines. */
public class LineTool extends AbstractLineTool implements MouseMotionListener {
  private static final long serialVersionUID = 3258132466219627316L;
  private Point tempPoint;

  public LineTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/draw-blue-strtlines.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public String getTooltip() {
    return "tool.line.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.line.instructions";
  }

  ////
  // MOUSE LISTENER
  @Override
  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      if (getLine() == null) {
        startLine(e);
        setIsEraser(isEraser(e));
      } else {
        tempPoint = null;
        stopLine(e);
      }
    } else if (getLine() != null) {
      // Create a joint
      tempPoint = null;
      return;
    }
    super.mousePressed(e);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (getLine() == null) {
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if (getLine() != null) {
      if (tempPoint != null) {
        removePoint(tempPoint);
      }
      tempPoint = addPoint(e);
    }
    super.mouseMoved(e);
  }
}
