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
import java.awt.image.ImageObserver;
import java.io.Serializable;
import net.rptools.maptool.client.ui.AssetPaint;
import net.rptools.maptool.model.Asset;

public abstract class DrawablePaint implements Serializable {
  public abstract Paint getPaint(ImageObserver... observers);

  public abstract Paint getPaint(
      int offsetX, int offsetY, double scale, ImageObserver... observers);

  public static DrawablePaint convertPaint(Paint paint) {
    if (paint == null) {
      return null;
    }
    if (paint instanceof Color) {
      return new DrawableColorPaint((Color) paint);
    }
    if (paint instanceof AssetPaint) {
      Asset asset = ((AssetPaint) paint).getAsset();
      return new DrawableTexturePaint(asset);
    }
    throw new IllegalArgumentException("Invalid type of paint: " + paint.getClass().getName());
  }
}
