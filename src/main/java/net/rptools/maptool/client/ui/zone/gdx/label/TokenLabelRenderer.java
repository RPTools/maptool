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

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import javax.swing.*;
import net.rptools.lib.StringUtil;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.GraphicsUtil;

public class TokenLabelRenderer implements ItemRenderer {
  private final boolean isGMView;
  private Token token;
  private TextRenderer textRenderer;
  private Vector3 tmpWorldCoord = new Vector3();
  private Vector3 tmpScreenCoord = new Vector3();
  private Zone zone;

  public TokenLabelRenderer(Token token, Zone zone, boolean isGMView, TextRenderer textRenderer) {
    this.token = token;
    this.zone = zone;
    this.isGMView = isGMView;
    this.textRenderer = textRenderer;
  }

  @Override
  public void render(Camera camera, float zoom) {
    int offset = 3; // Keep it from tramping on the token border.
    TextRenderer.Background background;
    Color foreground;

    if (token.isVisible()) {
      if (token.getType() == Token.Type.NPC) {
        background = TextRenderer.Background.Blue;
        foreground = Color.WHITE;
      } else {
        background = TextRenderer.Background.Gray;
        foreground = Color.BLACK;
      }
    } else {
      background = TextRenderer.Background.DarkGray;
      foreground = Color.WHITE;
    }
    String name = token.getName();
    if (isGMView && token.getGMName() != null && !StringUtil.isEmpty(token.getGMName())) {
      name += " (" + token.getGMName() + ")";
    }

    // Calculate image dimensions

    float labelHeight = textRenderer.getFont().getLineHeight() + GraphicsUtil.BOX_PADDINGY * 2;

    java.awt.Rectangle r = token.getBounds(zone);
    tmpWorldCoord.x = r.x + r.width / 2;
    tmpWorldCoord.y = (r.y + r.height + offset + labelHeight * zoom / 2) * -1;
    tmpWorldCoord.z = 0;
    tmpScreenCoord = camera.project(tmpWorldCoord);

    textRenderer.drawBoxedString(
        name, tmpScreenCoord.x, tmpScreenCoord.y, SwingUtilities.CENTER, background, foreground);

    var label = token.getLabel();

    // Draw name and label to image
    if (label != null && label.trim().length() > 0) {
      textRenderer.drawBoxedString(
          label,
          tmpScreenCoord.x,
          tmpScreenCoord.y - labelHeight,
          SwingUtilities.CENTER,
          background,
          foreground);
    }
  }
}
