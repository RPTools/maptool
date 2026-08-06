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
package net.rptools.maptool.client.ui.zone.renderer.tokenRender;

import java.awt.*;
import java.awt.image.BufferedImage;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.ui.zone.ZoneViewModel.TokenPosition;
import net.rptools.maptool.client.ui.zone.renderer.RenderHelper;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.TokenUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TokenRenderer {
  private static final Logger log = LogManager.getLogger(TokenRenderer.class);

  private final RenderHelper renderHelper;
  private final Zone zone;

  public TokenRenderer(RenderHelper renderHelper, Zone zone) {
    this.renderHelper = renderHelper.withTimerPrefix("TokenRenderer");
    this.zone = zone;
  }

  public void renderToken(Token token, TokenPosition position, Graphics2D g2d, float extraOpacity) {
    var timer = CodeTimer.get();
    timer.increment("TokenRenderer-renderToken");
    timer.start("TokenRenderer-renderToken");
    renderHelper.render(
        g2d, worldG -> paintTokenImage(worldG, position, extraOpacity * token.getTokenOpacity()));
    timer.stop("TokenRenderer-renderToken");
  }

  /**
   * Checks to see if token has an image table and references that if the token has a facing
   * otherwise uses basic image
   *
   * @param token the token to get the image from.
   * @return The token's current image based on its facing.
   */
  private BufferedImage getRenderImage(Token token) {
    // get token image, using image table if present
    MD5Key tokenImageId = token.getTokenImageAssetId();
    return ImageManager.getImage(tokenImageId, renderHelper.getImageObserver());
  }

  private void paintTokenImage(Graphics2D g2d, TokenPosition position, float opacity) {
    var token = position.token();
    var renderImage = getRenderImage(token);

    var imageTransform =
        TokenUtil.getRenderTransform(
            zone,
            token,
            new Dimension(renderImage.getWidth(), renderImage.getHeight()),
            position.footprintBounds());

    if (opacity < 1.0f) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
    }

    g2d.drawImage(renderImage, imageTransform, renderHelper.getImageObserver());
    g2d.setStroke(new BasicStroke(1f));
  }
}
