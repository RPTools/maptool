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
package net.rptools.maptool.client.ui.zone;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;

public class LightSourceIconOverlay implements ZoneOverlay {
  private final BufferedImage lightSourceIcon = RessourceManager.getImage(Images.LIGHT_SOURCE);

  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    var viewModel = renderer.getViewModel();
    for (var point : viewModel.getLightPositions()) {
      var screenPoint = viewModel.getZoneScale().toScreenSpace(point);
      var at =
          AffineTransform.getTranslateInstance(
              screenPoint.x - lightSourceIcon.getWidth() / 2.,
              screenPoint.y - lightSourceIcon.getHeight() / 2.);
      g.drawImage(lightSourceIcon, at, null);
    }
  }
}
