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
package net.rptools.maptool.client.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import javax.swing.JTextField;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.StringUtil;

/**
 * Manages the zoom level in the main MapTool window's status bar at the bottom of the window. This
 * means displaying the current zoom level as a percentage as well as allowing a value to be entered
 * and changing the zoom level to that amount.
 */
public class ZoomStatusBar extends JTextField implements ActionListener {
  public ZoomStatusBar() {
    super("", 9);
    setHorizontalAlignment(RIGHT);
    setToolTipText(I18N.getString("ZoomStatusBar.tooltip"));
    addActionListener(this);
  }

  @Override
  public boolean isEnabled() {
    return !AppState.isZoomLocked() && super.isEnabled();
  }

  public void actionPerformed(ActionEvent e) {
    JTextField target = (JTextField) e.getSource();
    if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
      double zoom;
      ZoneRenderer renderer;
      try {
        zoom = StringUtil.parseDecimal(target.getText());
        renderer = MapTool.getFrame().getCurrentZoneRenderer();
        renderer.setScale(zoom / 100);
        renderer.maybeForcePlayersView();
        update();
      } catch (ParseException ex) {
        // If the number is invalid, ignore it.
      }
    }
  }

  public void clear() {
    setText("");
  }

  public void update() {
    String zoom = "";
    if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
      double scale = MapTool.getFrame().getCurrentZoneRenderer().getZoneScale().getScale();
      scale *= 100;

      if (scale < 10) {
        zoom = String.format("%.4f%%", scale);
      } else if (scale < 100) {
        zoom = String.format("%.3f%%", scale);
      } else if (scale < 1000) {
        zoom = String.format("%.2f%%", scale);
      } else if (scale < 10000) {
        zoom = String.format("%.1f%%", scale);
      } else {
        zoom = String.format("%.0f%%", scale);
      }
    }
    setText(zoom);
  }
}
