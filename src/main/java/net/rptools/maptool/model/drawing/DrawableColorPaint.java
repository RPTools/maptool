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
import net.rptools.maptool.server.proto.drawing.DrawableColorPaintDto;
import net.rptools.maptool.server.proto.drawing.DrawablePaintDto;

public class DrawableColorPaint extends DrawablePaint implements Serializable {
  private int color;
  private transient Color colorCache;

  public DrawableColorPaint() {
    // For deserialization
  }

  public DrawableColorPaint(Color color) {
    this.color = color.getRGB();
  }

  public DrawableColorPaint(int color) {
    this.color = color;
  }

  public int getColor() {
    return color;
  }

  public void setColor(int value) {
    color = value;
  }

  @Override
  public Paint getPaint(ImageObserver... observers) {
    if (colorCache == null) {
      colorCache = new Color(color);
    }
    return colorCache;
  }

  @Override
  public Paint getPaint(int offsetX, int offsetY, double scale, ImageObserver... observer) {
    return getPaint();
  }

  @Override
  public DrawablePaintDto toDto() {
    {
      var dto = DrawablePaintDto.newBuilder();
      return dto.setColorPaint(DrawableColorPaintDto.newBuilder().setColor(getColor())).build();
    }
  }
}
