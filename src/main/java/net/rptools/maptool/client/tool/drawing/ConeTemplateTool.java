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
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.ConeTemplate;
import net.rptools.maptool.model.drawing.RadiusTemplate;

/**
 * Draw a template for an effect with a cone area. Make the template show the squares that are
 * effected, not just draw a circle. Let the player choose the vertex with the mouse and use the
 * wheel to set the radius. Use control and mouse position to direct the cone. This allows the user
 * to move the entire template where it is to be used before placing it which is very important when
 * casting a spell.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class ConeTemplateTool extends RadiusTemplateTool {
  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  /** Add the icon to the toggle button. */
  public ConeTemplateTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/temp-blue-cone.png"))));
    } catch (IOException ioe) {
      MapTool.showError("Cannot read image 'temp-blue-cone.png'", ioe);
    } // endtry
  }

  /*---------------------------------------------------------------------------------------------
   * Overidden RadiusTemplateTool Methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#createBaseTemplate() */
  @Override
  protected AbstractTemplate createBaseTemplate() {
    return new ConeTemplate();
  }

  /** @see net.rptools.maptool.client.ui.Tool#getTooltip() */
  @Override
  public String getTooltip() {
    return "tool.cone.tooltip";
  }

  /** @see net.rptools.maptool.client.ui.Tool#getInstructions() */
  @Override
  public String getInstructions() {
    return "tool.cone.instructions";
  }

  /**
   * @see
   *     net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#setRadiusFromAnchor(java.awt.event.MouseEvent)
   */
  @Override
  protected void setRadiusFromAnchor(MouseEvent e) {
    super.setRadiusFromAnchor(e);

    // Set the direction based on the mouse location too
    ZonePoint vertex = template.getVertex();
    ZonePoint mouse = new ScreenPoint(e.getX(), e.getY()).convertToZone(renderer);
    ((ConeTemplate) template)
        .setDirection(RadiusTemplate.Direction.findDirection(mouse.x, mouse.y, vertex.x, vertex.y));
  }
}
