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
import net.rptools.maptool.client.tool.Tool;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.BlastTemplate;

/**
 * Draws a square blast template next to a base cell.
 *
 * @author Jay
 */
public class BlastTemplateTool extends BurstTemplateTool {

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  public BlastTemplateTool() {}

  /*---------------------------------------------------------------------------------------------
   * Overridden RadiusTemplateTool methods
   *-------------------------------------------------------------------------------------------*/

  /** @see net.rptools.maptool.client.tool.drawing.BurstTemplateTool#createBaseTemplate() */
  @Override
  protected AbstractTemplate createBaseTemplate() {
    return new BlastTemplate();
  }

  /** @see Tool#getTooltip() */
  @Override
  public String getTooltip() {
    return "tool.blasttemplate.tooltip";
  }

  /** @see Tool#getInstructions() */
  @Override
  public String getInstructions() {
    return "tool.blasttemplate.instructions";
  }

  /**
   * @see
   *     net.rptools.maptool.client.tool.drawing.RadiusTemplateTool#setRadiusFromAnchor(java.awt.event.MouseEvent)
   */
  @Override
  protected void setRadiusFromAnchor(MouseEvent e) {
    // Determine mouse cell position relative to base cell and then pass to blast template
    CellPoint workingCell = renderer.getZone().getGrid().convert(getCellAtMouse(e));
    CellPoint vertexCell = renderer.getZone().getGrid().convert(template.getVertex());
    ((BlastTemplate) template)
        .setControlCellRelative(workingCell.x - vertexCell.x, workingCell.y - vertexCell.y);
  }
}
