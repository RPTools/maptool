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
package net.rptools.maptool.box2d;

import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.ui.MapToolFrame;

public class DesktopLauncher extends JFrame {

  private static final long serialVersionUID = 2536172952937398744L;
  private LwjglAWTCanvas canvas;
  private LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
  private MapToolFrame mapToolFrame;

  public DesktopLauncher(MapToolFrame clientFrame) {
    cfg.title = MapToolGame.TITLE;
    cfg.width = MapToolGame.SCREEN_WIDTH;
    cfg.height = MapToolGame.SCREEN_HEIGHT;
    mapToolFrame = clientFrame;

    // setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    canvas = new LwjglAWTCanvas(new MapToolGame(mapToolFrame, this), cfg);
    canvas.getCanvas().setSize(MapToolGame.SCREEN_WIDTH, MapToolGame.SCREEN_HEIGHT);
    add(canvas.getCanvas());

    pack();
    setVisible(true);
  }

  public static void main(String[] arg) {
    SwingUtilities.invokeLater(
        new Runnable() {
          public void run() {
            new DesktopLauncher(null);
          }
        });
  }

  @Override
  public void dispose() {
    canvas.stop();

    super.dispose();
  }
}
