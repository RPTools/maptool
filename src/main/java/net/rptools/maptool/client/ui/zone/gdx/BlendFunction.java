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

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;

public record BlendFunction(
    int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
  public static final BlendFunction PREMULTIPLIED_ALPHA_SRC_OVER =
      new BlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

  public static final BlendFunction ALPHA_SRC_OVER =
      new BlendFunction(
          GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);

  public static final BlendFunction SCREEN =
      new BlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_COLOR, GL20.GL_ONE, GL20.GL_NONE);

  public static final BlendFunction SRC_ONLY = new BlendFunction(GL20.GL_ONE, GL20.GL_NONE);

  public static BlendFunction readFromBatch(Batch batch) {
    return new BlendFunction(
        batch.getBlendSrcFunc(),
        batch.getBlendDstFunc(),
        batch.getBlendSrcFuncAlpha(),
        batch.getBlendDstFuncAlpha());
  }

  public BlendFunction(int srcFunc, int dstFunc) {
    this(srcFunc, dstFunc, srcFunc, dstFunc);
  }

  public void applyToBatch(Batch batch) {
    batch.setBlendFunctionSeparate(srcFuncColor, dstFuncColor, srcFuncAlpha, dstFuncAlpha);
  }
}
