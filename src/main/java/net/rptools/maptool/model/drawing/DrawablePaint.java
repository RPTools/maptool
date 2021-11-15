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
package net.rptools.maptool.model.drawing;

import java.awt.Color;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.image.ImageObserver;
import java.io.Serializable;

import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.ui.AssetPaint;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.server.Mapper;
import net.rptools.maptool.server.proto.drawing.DrawableColorPaintDto;
import net.rptools.maptool.server.proto.drawing.DrawablePaintDto;
import net.rptools.maptool.server.proto.drawing.DrawableTexturePaintDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DrawablePaint implements Serializable {
  private static final Logger log = LogManager.getLogger(DrawablePaint.class);

  public abstract Paint getPaint(ImageObserver... observers);

  public abstract Paint getPaint(
      int offsetX, int offsetY, double scale, ImageObserver... observers);

  public static DrawablePaint convertPaint(Paint paint) {
    if (paint == null) {
      return null;
    }
    if (paint instanceof Color) {
      // Colors from swatch/HSV/RGB pickers
      return new DrawableColorPaint((Color) paint);
    }
    if (paint instanceof AssetPaint) {
      // Texture Picker
      Asset asset = ((AssetPaint) paint).getAsset();
      return new DrawableTexturePaint(asset);
    }
    if (paint instanceof TexturePaint) {
      //  This only happens if you select the top-left White swatch and only
      //  the first click.  After that it always returns a Color.
      return new DrawableColorPaint(Color.WHITE);
    }
    throw new IllegalArgumentException("Invalid type of paint: " + paint.getClass().getName());
  }

  public static DrawablePaint fromDto(DrawablePaintDto dto) {
    switch (dto.getPaintTypeCase()) {
      case COLOR_PAINT -> {
        var paint = new DrawableColorPaint(dto.getColorPaint().getColor());
        return paint;
      }
      case TEXTURE_PAINT -> {
        var texturePaintDto = dto.getTexturePaint();
        return new DrawableTexturePaint(
            new MD5Key(texturePaintDto.getAssetId()), texturePaintDto.getScale());
      }
      default -> {
        log.warn("unknown DrawablePaintDto type: " + dto.getPaintTypeCase());
        return null;
      }
    }
  }

  public abstract DrawablePaintDto toDto();
}
