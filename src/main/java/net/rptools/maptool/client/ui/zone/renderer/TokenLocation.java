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
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.model.Token;

class TokenLocation {

  private final ZoneRenderer renderer;
  public Area bounds;
  public Token token;
  public Rectangle boundsCache;
  public double scaledHeight;
  public double scaledWidth;
  public double x;
  public double y;
  public int offsetX;
  public int offsetY;

  /**
   * Construct a TokenLocation object that caches where images are stored and what their size is so
   * that the next rendering pass can use that information to optimize the drawing.
   *
   * @param bounds
   * @param origBounds (unused)
   * @param token
   * @param x
   * @param y
   * @param width (unused)
   * @param height (unused)
   * @param scaledWidth
   * @param scaledHeight
   */
  public TokenLocation(
      ZoneRenderer renderer,
      Area bounds,
      Rectangle2D origBounds,
      Token token,
      double x,
      double y,
      int width,
      int height,
      double scaledWidth,
      double scaledHeight) {
    this.renderer = renderer;
    this.bounds = bounds;
    this.token = token;
    this.scaledWidth = scaledWidth;
    this.scaledHeight = scaledHeight;
    this.x = x;
    this.y = y;

    offsetX = renderer.getViewOffsetX();
    offsetY = renderer.getViewOffsetY();

    boundsCache = bounds.getBounds();
  }

  public boolean maybeOnscreen(Rectangle viewport) {
    int deltaX = renderer.getViewOffsetX() - offsetX;
    int deltaY = renderer.getViewOffsetY() - offsetY;

    boundsCache.x += deltaX;
    boundsCache.y += deltaY;

    offsetX = renderer.getViewOffsetX();
    offsetY = renderer.getViewOffsetY();

    final var timer = CodeTimer.get();
    timer.start("maybeOnsceen");
    try {
      return boundsCache.intersects(viewport);
    } finally {
      timer.stop("maybeOnsceen");
    }
  }
}
