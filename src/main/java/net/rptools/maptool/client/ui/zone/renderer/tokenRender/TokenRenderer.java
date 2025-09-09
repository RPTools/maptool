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
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import net.rptools.lib.CodeTimer;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneViewModel.TokenPosition;
import net.rptools.maptool.client.ui.zone.renderer.RenderHelper;
import net.rptools.maptool.model.*;
import net.rptools.maptool.util.ImageManager;
import net.rptools.maptool.util.ImageSupport;
import net.rptools.maptool.util.TokenUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class TokenRenderer {
  private static final Logger log = LogManager.getLogger(TokenRenderer.class);
  private static final Map<String, Map<Integer, BufferedImage>> imageTableMap =
      Collections.synchronizedMap(new HashMap<>());

  private final RenderHelper renderHelper;
  private final Zone zone;

  public TokenRenderer(RenderHelper renderHelper, Zone zone) {
    this.renderHelper = renderHelper;
    this.zone = zone;
  }

  public void renderToken(Token token, TokenPosition position, Graphics2D g2d, float opacity) {
    var timer = CodeTimer.get();
    timer.increment("TokenRenderer-renderToken");
    timer.start("TokenRenderer-renderToken");

    timer.start("TokenRenderer-loadImageTable");
    if (token.getHasImageTable() && !imageTableMap.containsKey(token.getImageTableName())) {
      (new CacheTableImagesWorker(token.getImageTableName())).execute();
    }
    timer.stop("TokenRenderer-loadImageTable");

    timer.start("TokenRenderer-paintTokenImage");
    renderHelper.render(
        g2d, worldG -> paintTokenImage(worldG, position, opacity * token.getTokenOpacity()));
    timer.stop("TokenRenderer-paintTokenImage");
    timer.stop("TokenRenderer-renderToken");
  }

  private BufferedImage getRenderImage(Token token) {
    var timer = CodeTimer.get();
    timer.start("TokenRenderer-getRenderImage");
    BufferedImage bi = ImageManager.BROKEN_IMAGE;
    if (token.getHasImageTable() && imageTableMap.containsKey(token.getImageTableName())) {
      Map<Integer, BufferedImage> imageTable = imageTableMap.get(token.getImageTableName());
      int max = imageTable.keySet().stream().max(Integer::compareTo).orElse(Integer.MAX_VALUE);
      if (max != Integer.MAX_VALUE) {
        int useValue = (360 + token.getFacingInDegrees()) % max;
        bi =
            imageTable.get(
                imageTable.keySet().stream()
                    .sorted()
                    .filter(integer -> integer >= useValue)
                    .toList()
                    .getFirst());
      }
    } else {
      bi = ImageSupport.getTokenImage(token, renderHelper.getImageObserver());
    }
    timer.stop("TokenRenderer-getRenderImage");
    return bi;
  }

  private void paintTokenImage(Graphics2D g2d, TokenPosition position, float opacity) {
    var token = position.token();
    var renderImage = getRenderImage(token);
    if (renderImage == null) {
      return;
    }

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

  private static Map<Integer, BufferedImage> cacheImageTable(String tableName) {
    LookupTable lookupTable = MapTool.getCampaign().getLookupTableMap().get(tableName);
    if (lookupTable != null) {
      BufferedImage broken = ImageManager.BROKEN_IMAGE;
      Map<Integer, BufferedImage> tmp = new HashMap<>();
      List<LookupTable.LookupEntry> entries = lookupTable.getEntryList();
      for (LookupTable.LookupEntry entry : entries) {
        MD5Key asset = entry.getImageId();
        if (asset != null) {
          BufferedImage bi = ImageManager.getImageAndWait(asset);
          if (!bi.equals(broken)) {
            tmp.put(entry.getMax(), bi);
          }
        }
      }
      if (!tmp.isEmpty()) {
        return tmp;
      }
    }
    return null;
  }

  private static class CacheTableImagesWorker
      extends SwingWorker<Map<Integer, BufferedImage>, String> {
    String tableName;

    public CacheTableImagesWorker(String tableName) {
      this.tableName = tableName;
    }

    @Override
    public Map<Integer, BufferedImage> doInBackground() {
      return cacheImageTable(tableName);
    }

    @Override
    protected void done() {
      try {
        imageTableMap.put(tableName, get());
      } catch (Exception ignore) {
      }
    }
  }
}
