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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import java.awt.*;
import java.awt.geom.Area;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.token.*;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TokenOverlayRenderer {
  private final Color tmpColor = Color.WHITE.cpy();
  private ZoneCache zoneCache;
  private final AreaRenderer areaRenderer;
  private final ShapeDrawer drawer;
  private final PolygonSpriteBatch batch;

  public TokenOverlayRenderer(AreaRenderer areaRenderer) {
    this.areaRenderer = areaRenderer;
    drawer = areaRenderer.getShapeDrawer();
    batch = (PolygonSpriteBatch) drawer.getBatch();
  }

  public void setZoneCache(ZoneCache zoneCache) {
    this.zoneCache = zoneCache;
  }

  public void render(float stateTime, AbstractTokenOverlay overlay, Token token, Object value) {
    if (overlay instanceof BarTokenOverlay barTokenOverlay)
      renderBarTokenOverlay(stateTime, barTokenOverlay, token, value);
    else if (overlay instanceof BooleanTokenOverlay booleanTokenOverlay)
      renderTokenOverlay(stateTime, booleanTokenOverlay, token, value);
  }

  private void renderBarTokenOverlay(
      float stateTime, BarTokenOverlay overlay, Token token, Object value) {
    if (value == null) return;
    double val;
    if (value instanceof Number) {
      val = ((Number) value).doubleValue();
    } else {
      try {
        val = Double.parseDouble(value.toString());
      } catch (NumberFormatException e) {
        return; // Bad value so don't paint.
      }
    } // endif
    if (val < 0) val = 0;
    if (val > 1) val = 1;

    if (overlay instanceof MultipleImageBarTokenOverlay actualOverlay)
      renderTokenOverlay(stateTime, actualOverlay, token, val);
    else if (overlay instanceof SingleImageBarTokenOverlay actualOverlay)
      renderTokenOverlay(stateTime, actualOverlay, token, val);
    else if (overlay instanceof TwoToneBarTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token, val);
    else if (overlay instanceof DrawnBarTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token, val);
    else if (overlay instanceof TwoImageBarTokenOverlay actualOverlay)
      renderTokenOverlay(stateTime, actualOverlay, token, val);
  }

  private void renderTokenOverlay(
      float stateTime, MultipleImageBarTokenOverlay overlay, Token token, double barValue) {
    int increment = overlay.findIncrement(barValue);

    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;

    // Get the images
    var image = zoneCache.getSprite(overlay.getAssetIds()[increment], stateTime);

    Dimension d = bounds.getSize();
    Dimension size = new Dimension((int) image.getWidth(), (int) image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    // Find the position of the image according to the size and side where they are placed
    switch (overlay.getSide()) {
      case LEFT:
      case TOP:
        y += d.height - size.height;
        break;
      case RIGHT:
        x += d.width - size.width;
        y += d.height - size.height;
        break;
    }

    image.setPosition(x, y);
    image.setSize(size.width, size.height);
    image.draw(batch, overlay.getOpacity() / 100f);
  }

  private void renderTokenOverlay(
      float stateTime, SingleImageBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;

    // Get the images
    var image = zoneCache.getSprite(overlay.getAssetId(), stateTime);

    Dimension d = bounds.getSize();
    Dimension size = new Dimension((int) image.getWidth(), (int) image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    var side = overlay.getSide();
    // Find the position of the images according to the size and side where they are placed
    switch (side) {
      case LEFT:
      case TOP:
        y += d.height - size.height;
        break;
      case RIGHT:
        x += d.width - size.width;
        y += d.height - size.height;
        break;
    }

    int screenWidth =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM)
            ? overlay.calcBarSize(size.width, barValue)
            : size.width;
    int screenHeight =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT)
            ? overlay.calcBarSize(size.height, barValue)
            : size.height;

    image.setPosition(x + size.width - screenWidth, y + size.height - screenHeight);
    image.setSize(screenWidth, screenHeight);

    var u = image.getU();
    var v = image.getV();
    var u2 = image.getU2();
    var v2 = image.getV2();

    var uFactor = screenWidth * 1.0f / size.width;
    var uDiff = (u2 - u) * uFactor;
    image.setU(u2 - uDiff);

    var vFactor = screenHeight * 1.0f / size.height;
    var vDiff = (v2 - v) * vFactor;
    image.setV(v2 - vDiff);

    image.draw(batch, overlay.getOpacity() / 100f);

    image.setU(u);
    image.setV(v);
  }

  private void renderTokenOverlay(DrawnBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var side = overlay.getSide();
    var thickness = overlay.getThickness();

    int width =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) ? w : thickness;
    int height =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT) ? h : thickness;

    switch (side) {
      case LEFT:
      case TOP:
        y += h - height;
        break;
      case RIGHT:
        x += w - width;
        break;
    }

    if (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) {
      width = overlay.calcBarSize(width, barValue);
    } else {
      height = overlay.calcBarSize(height, barValue);
      y += bounds.height - height;
    }

    var barColor = overlay.getBarColor();
    tmpColor.set(
        barColor.getRed() / 255f,
        barColor.getGreen() / 255f,
        barColor.getBlue() / 255f,
        barColor.getAlpha() / 255f);
    drawer.filledRectangle(x, y, width, height, tmpColor);
  }

  private void renderTokenOverlay(TwoToneBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var side = overlay.getSide();
    var thickness = overlay.getThickness();

    int width =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) ? w : thickness;
    int height =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT) ? h : thickness;

    switch (side) {
      case LEFT:
      case TOP:
        y += h - height;
        break;
      case RIGHT:
        x += w - width;
        break;
    }

    var color = overlay.getBgColor();
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        color.getAlpha() / 255f);
    drawer.filledRectangle(x, y, width, height, tmpColor);

    // Draw the bar
    int borderSize = thickness > 5 ? 2 : 1;
    x += borderSize;
    y += borderSize;
    width -= borderSize * 2;
    height -= borderSize * 2;
    if (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM) {
      width = overlay.calcBarSize(width, barValue);
    } else {
      height = overlay.calcBarSize(height, barValue);
    }

    color = overlay.getBarColor();
    tmpColor.set(
        color.getRed() / 255f,
        color.getGreen() / 255f,
        color.getBlue() / 255f,
        color.getAlpha() / 255f);
    drawer.filledRectangle(x, y, width, height, tmpColor);
  }

  private void renderTokenOverlay(
      float stateTime, TwoImageBarTokenOverlay overlay, Token token, double barValue) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;

    // Get the images
    var topImage = zoneCache.getSprite(overlay.getTopAssetId(), stateTime);
    var bottomImage = zoneCache.getSprite(overlay.getBottomAssetId(), stateTime);

    Dimension d = bounds.getSize();
    Dimension size = new Dimension((int) topImage.getWidth(), (int) topImage.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    var side = overlay.getSide();
    // Find the position of the images according to the size and side where they are placed
    switch (side) {
      case LEFT:
      case TOP:
        y += d.height - size.height;
        break;
      case RIGHT:
        x += d.width - size.width;
        y += d.height - size.height;
        break;
    }

    var screenWidth =
        (side == BarTokenOverlay.Side.TOP || side == BarTokenOverlay.Side.BOTTOM)
            ? overlay.calcBarSize(size.width, barValue)
            : size.width;
    var screenHeight =
        (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT)
            ? overlay.calcBarSize(size.height, barValue)
            : size.height;

    bottomImage.setPosition(x, y);
    bottomImage.setSize(size.width, size.height);
    bottomImage.draw(batch, overlay.getOpacity() / 100f);

    var u = topImage.getU();
    var v = topImage.getV();
    var u2 = topImage.getU2();
    var v2 = topImage.getV2();

    var wFactor = screenWidth * 1.0f / size.width;
    var uDiff = (u2 - u) * wFactor;

    var vFactor = screenHeight * 1.0f / size.height;
    var vDiff = (v2 - v) * vFactor;

    topImage.setPosition(x, y);
    topImage.setSize(screenWidth, screenHeight);

    if (side == BarTokenOverlay.Side.LEFT || side == BarTokenOverlay.Side.RIGHT) {
      topImage.setU(u2 - uDiff);
      topImage.setV(v2 - vDiff);
    } else {

      topImage.setU2(u + uDiff);
      topImage.setV2(v + vDiff);
    }
    topImage.draw(batch, overlay.getOpacity() / 100f);

    topImage.setU(u);
    topImage.setV(v);
    topImage.setU2(u2);
    topImage.setV2(v2);
  }

  private void renderTokenOverlay(
      float stateTime, BooleanTokenOverlay overlay, Token token, Object value) {
    if (!FunctionUtil.getBooleanValue(value)) return;

    if (overlay instanceof ImageTokenOverlay actualOverlay)
      renderTokenOverlay(stateTime, actualOverlay, token);
    else if (overlay instanceof FlowColorDotTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof YieldTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof OTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof ColorDotTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof DiamondTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof TriangleTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof CrossTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof XTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
    else if (overlay instanceof ShadedTokenOverlay actualOverlay)
      renderTokenOverlay(actualOverlay, token);
  }

  private void renderTokenOverlay(ShadedTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    tmpColor.set(1, 1, 1, overlay.getOpacity() / 100f);
    drawer.setColor(tmpColor);
    drawer.filledRectangle(x, y, w, h);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(float stateTime, ImageTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y;

    // Get the image
    java.awt.Rectangle iBounds = overlay.getImageBounds(bounds, token);
    Dimension d = iBounds.getSize();

    var image = zoneCache.getSprite(overlay.getAssetId(), stateTime);

    Dimension size = new Dimension((int) image.getWidth(), (int) image.getHeight());
    SwingUtil.constrainTo(size, d.width, d.height);

    // Paint it at the right location
    int width = size.width;
    int height = size.height;

    if (overlay instanceof CornerImageTokenOverlay) {
      x += iBounds.x + (d.width - width) / 2;
      y -= iBounds.y + (d.height - height) / 2 + iBounds.height;
    } else {
      x = iBounds.x + (d.width - width) / 2;
      y = -(iBounds.y + (d.height - height) / 2) - iBounds.height;
    }

    image.setPosition(x, y);
    image.setSize(size.width, size.height);
    image.draw(batch, overlay.getOpacity() / 100f);
  }

  private void renderTokenOverlay(XTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    var stroke = overlay.getStroke();

    drawer.setColor(tmpColor);
    drawer.line(x, y, x + w, y + h, stroke.getLineWidth());
    drawer.line(x, y + h, x + w, y, stroke.getLineWidth());
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(FlowColorDotTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    drawer.setColor(tmpColor);
    Shape s = overlay.getShape(bounds, token);
    areaRenderer.fillArea(batch, new Area(s));
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(YieldTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    var stroke = overlay.getStroke();
    var hc = w / 2f;
    var vc = h * (1 - 0.134f);

    var floats =
        new float[] {
          x, y + vc, x + w, y + vc, x + hc, y,
        };

    drawer.setColor(tmpColor);
    drawer.path(floats, stroke.getLineWidth(), JoinType.POINTY, false);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(OTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    tmpColor
        .set(
            color.getRed() / 255f,
            color.getGreen() / 255f,
            color.getBlue() / 255f,
            overlay.getOpacity() / 100f)
        .premultiplyAlpha();

    var stroke = overlay.getStroke();
    var lineWidth = stroke.getLineWidth();

    var centerX = x + w / 2f;
    var centerY = y + h / 2f;
    var radiusX = w / 2f - lineWidth / 2f;
    var radiusY = h / 2f - lineWidth / 2f;

    drawer.setColor(tmpColor);
    drawer.ellipse(centerX, centerY, radiusX, radiusY, 0, lineWidth);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(ColorDotTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    var size = w * 0.1f;
    var offset = w * 0.8f;

    var posX = x + size;
    var posY = y + size;

    switch (overlay.getCorner()) {
      case SOUTH_EAST:
        posX += offset;
        break;
      case SOUTH_WEST:
        break;
      case NORTH_EAST:
        posX += offset;
        posY += offset;
        break;
      case NORTH_WEST:
        posY += offset;
        break;
    }

    drawer.setColor(tmpColor);
    drawer.filledEllipse(posX, posY, size, size);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(DiamondTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    var stroke = overlay.getStroke();

    var hc = w / 2f;
    var vc = h / 2f;

    var floats =
        new float[] {
          x, y + vc, x + hc, y, x + w, y + vc, x + hc, y + h,
        };

    drawer.setColor(tmpColor);
    drawer.path(floats, stroke.getLineWidth(), JoinType.POINTY, false);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(TriangleTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    var stroke = overlay.getStroke();

    var hc = w / 2f;
    var vc = h * (1 - 0.866f);

    var floats =
        new float[] {
          x, y + vc, x + w, y + vc, x + hc, y + h,
        };

    drawer.setColor(tmpColor);
    drawer.path(floats, stroke.getLineWidth(), JoinType.POINTY, false);
    drawer.setColor(Color.WHITE);
  }

  private void renderTokenOverlay(CrossTokenOverlay overlay, Token token) {
    var bounds = token.getBounds(zoneCache.getZone());
    var x = bounds.x;
    var y = -bounds.y - bounds.height;
    var w = bounds.width;
    var h = bounds.height;

    var color = overlay.getColor();
    Color.argb8888ToColor(tmpColor, color.getRGB());
    tmpColor.a = overlay.getOpacity() / 100f;
    tmpColor.premultiplyAlpha();

    var stroke = overlay.getStroke();

    drawer.setColor(tmpColor);
    drawer.line(x, y + h / 2f, x + w, y + h / 2f, stroke.getLineWidth());
    drawer.line(x + w / 2f, y, x + w / 2f, y + h, stroke.getLineWidth());
    drawer.setColor(Color.WHITE);
  }
}
