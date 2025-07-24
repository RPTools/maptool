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
package net.rptools.lib.image;

import com.twelvemonkeys.image.ResampleOp;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public enum RenderQuality {
  LOW_SCALING,
  PIXEL_ART_SCALING,
  MEDIUM_SCALING,
  HIGH_SCALING;

  public void setRenderingHints(Graphics2D g) {
    switch (this) {
      case LOW_SCALING, PIXEL_ART_SCALING -> {
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      }
      case MEDIUM_SCALING -> {
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
      }
      case HIGH_SCALING -> {
        g.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }
    }
  }

  public void setShrinkRenderingHints(Graphics2D d) {
    switch (this) {
      case LOW_SCALING, PIXEL_ART_SCALING -> {
        d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      }
      case MEDIUM_SCALING -> {
        d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT);
      }
      case HIGH_SCALING -> {
        d.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      }
    }
  }

  public int getResampleOpFilter() {
    return switch (this) {
      case LOW_SCALING, PIXEL_ART_SCALING -> ResampleOp.FILTER_POINT;
      case MEDIUM_SCALING -> ResampleOp.FILTER_TRIANGLE;
      case HIGH_SCALING -> ResampleOp.FILTER_QUADRATIC;
    };
  }
}
