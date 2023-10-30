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
package net.rptools.maptool.model.grid;

import java.awt.*;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;

public class GridRenderStyle {
  public GridDrawLineStyle lineStyle = GridDrawLineStyle.INTERSECTION;
  public GridDrawBlendComposite blendComposite = GridDrawBlendComposite.NONE;
  public float opacity = 0.8f;
  public double lineOffset = 0;
  public double lineWeight = AppState.getGridLineWeight();
  public Color firstColour = AppPreferences.getDefaultGridColor();
  public Color secondColour = new Color(245, 245, 0, 200);
  public boolean exposedOnly = true;
  public boolean overFog = false;
  public boolean softEdge = true;
  public boolean twoColour = true;
}
