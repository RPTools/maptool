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

import java.awt.geom.Rectangle2D;
import java.util.*;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;

/**
 * The Zone Compositor is responsible for providing the Zone Renderer with what needs to be
 * rendered. Within a given map region what objects exist that need to be drawn. Basically "What's
 * on screen?"
 */
public class ZoneCompositor {
  Zone zone;
  ZoneRenderer renderer;
  private Map<Token, Set<Token>> objectCache; // placeholder
  private boolean initialised;

  ZoneCompositor() {
    initialised = false;
  }

  public boolean isInitialised() {
    return initialised;
  }

  public void setRenderer(ZoneRenderer zoneRenderer) {
    renderer = zoneRenderer;
    zone = renderer.getZone();
    initialised = true;
  }

  protected Map<Token, Set<Token>> drawWhat(Rectangle2D bounds) {
    // Some logic goes here
    return objectCache;
  }
}
