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

import java.awt.*;
import java.awt.event.MouseListener;
import net.rptools.maptool.client.*;
import net.rptools.maptool.client.ui.zone.ZoneOverlay;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.drawing.*;

/**
 * Tool for manipulating templates.
 *
 * @see DrawingPointerTool
 */
public class TemplatePointerTool extends DrawingPointerTool implements ZoneOverlay, MouseListener {

  public TemplatePointerTool() {
    new MapToolEventBus().getMainEventBus().register(this);
  }

  @Override
  public String getTooltip() {
    return "tool.templatepointer.tooltip";
  }

  @Override
  public String getInstructions() {
    return "tool.templatepointer.instructions";
  }
}
