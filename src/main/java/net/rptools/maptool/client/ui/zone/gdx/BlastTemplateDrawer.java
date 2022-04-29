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
package net.rptools.maptool.client.ui.zone.gdx;

import net.rptools.maptool.model.drawing.AbstractTemplate;
import net.rptools.maptool.model.drawing.BlastTemplate;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.Pen;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BlastTemplateDrawer extends AbstractDrawingDrawer {

  public BlastTemplateDrawer(ShapeDrawer drawer) {
    super(drawer);
  }

  @Override
  protected void drawBackground(Drawable element, Pen pen) {
    var template = (BlastTemplate) element;
    tmpColor.set(tmpColor.r, tmpColor.g, tmpColor.b, AbstractTemplate.DEFAULT_BG_ALPHA);
    drawer.setColor(tmpColor);
    fillArea(template.getArea());
  }

  @Override
  protected void drawBorder(Drawable element, Pen pen) {
    var template = (BlastTemplate) element;
    drawArea(template.getArea());
  }
}
