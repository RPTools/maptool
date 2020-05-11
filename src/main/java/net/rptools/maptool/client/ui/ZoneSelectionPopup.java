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
package net.rptools.maptool.client.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.*;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.PlayerView;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

public class ZoneSelectionPopup extends JScrollPopupMenu {

  private static final int PADDING = 5;

  public ZoneSelectionPopup() {}

  @Override
  public void show(Component invoker, int x, int y) {
    createEntries();
    super.show(invoker, x, y);
  }

  private void createEntries() {
    removeAll();

    List<ZoneRenderer> rendererList =
        new LinkedList<ZoneRenderer>(MapTool.getFrame().getZoneRenderers());
    if (!MapTool.getPlayer().isGM()) {
      for (ListIterator<ZoneRenderer> iter = rendererList.listIterator(); iter.hasNext(); ) {
        ZoneRenderer renderer = iter.next();
        if (!renderer.getZone().isVisible()) {
          iter.remove();
        }
      }
    }

    Collections.sort(
        rendererList,
        new Comparator<ZoneRenderer>() {
          public int compare(ZoneRenderer o1, ZoneRenderer o2) {

            String name1 = o1.getZone().getName();
            String name2 = o2.getZone().getName();

            return String.CASE_INSENSITIVE_ORDER.compare(name1, name2);
          }
        });

    for (ZoneRenderer renderer : rendererList) {

      BufferedImage thumb =
          MapTool.takeMapScreenShot(new PlayerView(MapTool.getPlayer().getRole()));

      add(new JCheckBoxMenuItem(new SelectAction(renderer)))
          .setSelected(renderer == MapTool.getFrame().getCurrentZoneRenderer());
    }
  }

  private class SelectAction extends AbstractAction {

    private ZoneRenderer renderer;

    public SelectAction(ZoneRenderer renderer) {
      this.renderer = renderer;
      super.putValue(Action.NAME, renderer.getZone().getName());
    }

    public void actionPerformed(ActionEvent e) {

      if (MapTool.getFrame().getCurrentZoneRenderer() != renderer) {
        MapTool.getFrame().setCurrentZoneRenderer(renderer);
        MapTool.getFrame().refresh();

        if (AppState.isPlayerViewLinked() && MapTool.getPlayer().isGM()) {
          MapTool.serverCommand().enforceZone(renderer.getZone().getId());
          renderer.forcePlayersView();
        }
      }

      ZoneSelectionPopup.this.setVisible(false);
    }
  }
}
