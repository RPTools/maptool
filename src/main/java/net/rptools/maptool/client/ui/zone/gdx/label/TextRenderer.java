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
package net.rptools.maptool.client.ui.zone.gdx.label;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import javax.swing.*;

public class TextRenderer {
  public enum Background {
    Gray,
    Blue,
    DarkGray
  }

  private GlyphLayout glyphLayout = new GlyphLayout();
  private NinePatch blueLabel;
  private NinePatch grayLabel;
  private NinePatch darkGrayLabel;
  private Batch batch;
  private BitmapFont font;

  private boolean scaling;

  public TextRenderer(TextureAtlas atlas, Batch batch, BitmapFont font) {
    this(atlas, batch, font, true);
  }

  public TextRenderer(TextureAtlas atlas, Batch batch, BitmapFont font, boolean scaling) {
    this.font = font;
    this.batch = batch;
    blueLabel = atlas.createPatch("blueLabelbox");
    grayLabel = atlas.createPatch("grayLabelbox");
    darkGrayLabel = atlas.createPatch("darkGreyLabelbox");
    this.scaling = scaling;
  }

  public BitmapFont getFont() {
    return font;
  }

  public void drawString(String text, float centerX, float centerY, Color foreground) {
    drawBoxedString(text, centerX, centerY, SwingUtilities.CENTER, null, foreground);
  }

  public void drawString(String text, float centerX, float centerY) {
    drawBoxedString(text, centerX, centerY, SwingUtilities.CENTER, null, Color.WHITE);
  }

  public void drawBoxedString(String text, float centerX, float centerY) {
    drawBoxedString(text, centerX, centerY, SwingUtilities.CENTER);
  }

  public void drawBoxedString(String text, float x, float y, int justification) {
    drawBoxedString(text, x, y, justification, Background.Gray, Color.BLACK);
  }

  public void drawBoxedString(
      String text, float x, float y, int justification, Background background, Color foreground) {
    NinePatch backgroundPatch = null;
    if (background != null) {
      switch (background) {
        case Gray -> {
          backgroundPatch = grayLabel;
        }
        case Blue -> {
          backgroundPatch = blueLabel;
        }
        case DarkGray -> {
          backgroundPatch = darkGrayLabel;
        }
      }
    }

    var BOX_PADDINGX = 10;
    var BOX_PADDINGY = 2;

    if (text == null) {
      text = "";
    }

    // the font size was already scaled. So don't scale it here.
    glyphLayout.setText(font, text);
    var strWidth = glyphLayout.width;
    var fontHeight = font.getLineHeight();

    var width = strWidth + BOX_PADDINGX * 2;
    var height = fontHeight + BOX_PADDINGY * 2;

    y = y - fontHeight / 2 - BOX_PADDINGY;

    switch (justification) {
      case SwingUtilities.CENTER:
        x = x - strWidth / 2 - BOX_PADDINGX;
        break;
      case SwingUtilities.RIGHT:
        x = x - strWidth - BOX_PADDINGX;
        break;
      case SwingUtilities.LEFT:
        break;
    }

    // Box
    if (backgroundPatch != null) {
      backgroundPatch.draw(batch, x, y, width, height);
    }

    // Renderer message

    var textX = x + BOX_PADDINGX;
    var textY = y + height - BOX_PADDINGY - font.getAscent();
    font.setColor(foreground);
    font.draw(batch, text, textX, textY);
  }
}
