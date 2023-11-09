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
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.Images;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.model.AttachedLightSource;
import net.rptools.maptool.model.LightSource;
import net.rptools.maptool.model.Token;

public class LightSourceIconOverlay implements ZoneOverlay {
  private BufferedImage lightSourceIcon = RessourceManager.getImage(Images.LIGHT_SOURCE);

  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {

    for (Token token : renderer.getZone().getAllTokens()) {

      if (token.hasLightSources()) {
        boolean foundNormalLight = false;
        for (AttachedLightSource attachedLightSource : token.getLightSources()) {
          LightSource lightSource = attachedLightSource.resolve(token, MapTool.getCampaign());
          if (lightSource != null && lightSource.getType() == LightSource.Type.NORMAL) {
            foundNormalLight = true;
            break;
          }
        }
        if (!foundNormalLight) {
          continue;
        }

        Area area = renderer.getTokenBounds(token);
        if (area == null) {
          continue;
        }

        int x = area.getBounds().x + (area.getBounds().width - lightSourceIcon.getWidth()) / 2;
        int y = area.getBounds().y + (area.getBounds().height - lightSourceIcon.getHeight()) / 2;
        g.drawImage(lightSourceIcon, x, y, null);
      }
    }
  }
}
