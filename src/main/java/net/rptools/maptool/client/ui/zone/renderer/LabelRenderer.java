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
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.ImageLabel;
import net.rptools.maptool.client.ui.zone.ZoneViewModel;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.util.GraphicsUtil;

/** Represents a delayed label render */
public class LabelRenderer implements ItemRenderer {

  private static final Map<GUID, LabelRenderer> labelCache = new HashMap<>();
  private static final Map<GUID, BufferedImage> labelImageCache = new HashMap<>();

  public static Map<GUID, LabelRenderer> getLabelCache() {
    return labelCache;
  }

  public static Map<GUID, BufferedImage> getLabelImageCache() {
    return labelImageCache;
  }

  private final ZoneRenderer renderer;
  private final String text;
  private int x;
  private int y;
  private final int align;
  private final Color foreground;
  private final ImageLabel background;

  // Used for drawing from label cache.
  private final GUID tokenId;
  private int width, height;

  public LabelRenderer(ZoneRenderer renderer, String text, int x, int y) {
    this(renderer, text, x, y, null);
  }

  public LabelRenderer(ZoneRenderer renderer, String text, int x, int y, GUID tId) {
    this.renderer = renderer;
    this.text = text;
    this.x = x;
    this.y = y;

    // Defaults
    this.align = SwingUtilities.CENTER;
    this.background = GraphicsUtil.GREY_LABEL;
    this.foreground = Color.black;
    tokenId = tId;
    if (tokenId != null) {
      width = labelImageCache.get(tokenId).getWidth();
      height = labelImageCache.get(tokenId).getHeight();
    }
  }

  @SuppressWarnings("unused")
  public LabelRenderer(
      ZoneRenderer renderer,
      String text,
      int x,
      int y,
      int align,
      ImageLabel background,
      Color foreground) {
    this(renderer, text, x, y, align, background, foreground, null);
  }

  public LabelRenderer(
      ZoneRenderer renderer,
      String text,
      int x,
      int y,
      int align,
      ImageLabel background,
      Color foreground,
      GUID tId) {
    this.renderer = renderer;
    this.text = text;
    this.x = x;
    this.y = y;
    this.align = align;
    this.foreground = foreground;
    this.background = background;
    tokenId = tId;
    if (tokenId != null) {
      width = labelImageCache.get(tokenId).getWidth();
      height = labelImageCache.get(tokenId).getHeight();
    }
  }

  public static boolean isLabelVisible(
      ZoneViewModel.TokenPosition position, ZoneViewModel viewModel, boolean hover) {
    if (!(AppState.isShowTokenNames() || hover)) {
      return false;
    }
    // if policy does not auto-reveal FoW, check if fog covers the token (slow)
    return viewModel.getPlayerView().isGMView()
        || (viewModel.isUsingVision() && MapTool.getServerPolicy().isAutoRevealOnMovement())
        || viewModel.zone.isTokenVisible(position.token());
  }

  public void render(Graphics2D g) {
    if (tokenId != null) { // Use cached image.
      switch (align) {
        case SwingUtilities.CENTER:
          x = x - width / 2;
          break;
        case SwingUtilities.RIGHT:
          x = x - width;
          break;
        case SwingUtilities.LEFT:
          break;
      }
      BufferedImage img = labelImageCache.get(tokenId);
      if (img != null) {
        g.drawImage(img, x, y, width, height, null);
      } else { // Draw as normal
        GraphicsUtil.drawBoxedString(g, text, x, y, align, background, foreground);
      }
    } else { // Draw as normal.
      GraphicsUtil.drawBoxedString(g, text, x, y, align, background, foreground);
    }
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }
}
