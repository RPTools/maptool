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
import net.rptools.maptool.model.Token;

/**
 * Construct a TokenLocation object for caching where images are stored and what their size is.
 * Speeds up subsequent rendering passes.
 */
class TokenLocation {
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
   * @param bounds Footprint Area
   * @param origBounds Bounding rectangle (unused)
   * @param token Token
   * @param x Token screen point x
   * @param y Token screen point y
   * @param width token.getBounds(zone).width (unused)
   * @param height token.getBounds(zone).height (unused)
   * @param scaledWidth zoneScale.getScale() * width
   * @param scaledHeight zoneScale.getScale() * height
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
    this.bounds = bounds;
    this.token = token;
    this.scaledWidth = scaledWidth;
    this.scaledHeight = scaledHeight;
    this.x = x;
    this.y = y;
    this.offsetX = renderer.getViewOffsetX();
    this.offsetY = renderer.getViewOffsetY();
    boundsCache = bounds.getBounds();
  }

  public boolean maybeOnscreen(ZoneRenderer renderer, Rectangle viewport) {
    int deltaX = renderer.getViewOffsetX() - offsetX;
    int deltaY = renderer.getViewOffsetY() - offsetY;

    boundsCache.x += deltaX;
    boundsCache.y += deltaY;

    offsetX = renderer.getViewOffsetX();
    offsetY = renderer.getViewOffsetY();

    return boundsCache.intersects(viewport);
  }
}
