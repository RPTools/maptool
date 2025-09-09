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
import com.badlogic.gdx.math.Vector3;
import javax.swing.*;

public class LabelRenderer implements ItemRenderer {
  private final TextRenderer renderer;
  private float x;
  private float y;
  private String text;
  private Vector3 tmpWorldCoord = new Vector3();
  private Vector3 tmpScreenCoord = new Vector3();

  public LabelRenderer(String text, float x, float y, TextRenderer renderer) {
    this.x = x;
    this.y = y;
    this.text = text;
    this.renderer = renderer;
  }

  @Override
  public void render(Camera camera, float zoom) {
    tmpWorldCoord.x = x;
    tmpWorldCoord.y = y;
    tmpWorldCoord.z = 0;
    tmpScreenCoord = camera.project(tmpWorldCoord);

    renderer.drawBoxedString(text, tmpScreenCoord.x, tmpScreenCoord.y, SwingUtilities.CENTER);
  }
}
