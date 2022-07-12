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

import java.awt.*;

public class ScreenScaleProvider {
  private float _dpiScale;
  private float _accumulatedTime;

  public ScreenScaleProvider() {
    updateDpiScale();
  }

  public float getDpiScale() {
    return _dpiScale;
  }

  public void triggerUpdate(float deltaTime) {
    _accumulatedTime += deltaTime;
    // we update the dpiScale every second
    if (_accumulatedTime < 1.0f) return;
  }

  private void updateDpiScale() {
    _dpiScale = Toolkit.getDefaultToolkit().getScreenResolution() / 96.0f;
  }
}
