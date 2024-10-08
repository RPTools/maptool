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
package net.rptools.maptool.client.ui.zone.renderer;

import java.awt.*;
import java.util.ArrayList;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

public class HaloRenderer {
  private Zone zone;
  private ZoneRenderer renderer;
  private boolean initialised = false;

  public boolean isInitialised() {
    return initialised;
  }

  HaloRenderer() {}

  public void setRenderer(ZoneRenderer zoneRenderer) {
    renderer = zoneRenderer;
    zone = renderer.getZone();
    initialised = true;
  }

  // Render Halos
  public void renderHalo(Graphics2D g2d, Token token, TokenLocation location) {
    if (token.hasHalo()) {
      g2d.setStroke(new BasicStroke(AppPreferences.haloLineWidth.get()));
      g2d.setColor(token.getHaloColor());
      g2d.draw(location.bounds);
    }
  }

  // Render halo batch
  public void renderHalos(
      Graphics2D g2d, ArrayList<Token> tokens, ArrayList<TokenLocation> locations) {
    for (Token token : tokens) {
      renderHalo(g2d, token, locations.get(tokens.indexOf(token)));
    }
  }
}
