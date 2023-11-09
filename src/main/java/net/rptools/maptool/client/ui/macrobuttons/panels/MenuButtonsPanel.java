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
package net.rptools.maptool.client.ui.macrobuttons.panels;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.theme.Icons;
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;

public class MenuButtonsPanel extends JToolBar {

  public MenuButtonsPanel() {
    setFloatable(false);

    addSelectAllButton();
    addDeselectAllButton();
    add(Box.createHorizontalStrut(8));
    addSelectPreviousButton();
    addSelectNextButton();
    add(Box.createHorizontalStrut(8));
    addRevertToPreviousButton();
  }

  private void addSelectAllButton() {
    ImageIcon i = RessourceManager.getSmallIcon(Icons.ACTION_SELECT_ALL_TOKENS);
    JButton label = new JButton(i);
    label.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            // Check if there is a map. Fix #1605
            if (zr != null) {
              final var tokens =
                  zr.getTokenIdsInBounds(
                      new Rectangle(zr.getX(), zr.getY(), zr.getWidth(), zr.getHeight()));
              zr.getSelectionModel().replaceSelection(tokens);
            }
          }
        });
    label.setToolTipText(I18N.getText("panel.Selected.tooltip.selectAll"));
    label.setBackground(null);
    add(label);
  }

  private void addDeselectAllButton() {
    ImageIcon i3 = RessourceManager.getSmallIcon(Icons.ACTION_SELECT_NO_TOKENS);
    JButton label3 = new JButton(i3);
    label3.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
            if (renderer != null) {
              renderer.getSelectionModel().replaceSelection(Collections.emptyList());
            }
          }
        });
    label3.setToolTipText(I18N.getText("panel.Selected.tooltip.deslectAll"));
    label3.setBackground(null);
    add(label3);
  }

  private void addRevertToPreviousButton() {
    ImageIcon i1 = RessourceManager.getSmallIcon(Icons.ACTION_RESET_TOKEN_SELECTION);
    JButton label1 = new JButton(i1);
    label1.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            if (zr != null) {
              zr.getSelectionModel().undoSelection(zr.getActiveLayer());
            }
          }
        });
    label1.setToolTipText(I18N.getText("panel.Selected.tooltip.revertToPrevious"));
    label1.setBackground(null);
    add(label1);
  }

  private void addSelectNextButton() {
    ImageIcon i1 = RessourceManager.getSmallIcon(Icons.ACTION_NEXT_TOKEN);
    JButton label1 = new JButton(i1);
    label1.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            if (zr != null) {
              zr.cycleSelectedToken(1);
            }
          }
        });
    label1.setToolTipText(I18N.getText("panel.Selected.tooltip.next"));
    label1.setBackground(null);
    add(label1);
  }

  private void addSelectPreviousButton() {
    ImageIcon i1 = RessourceManager.getSmallIcon(Icons.ACTION_PREVIOUS_TOKEN);
    JButton label1 = new JButton(i1);
    label1.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
            if (zr != null) {
              zr.cycleSelectedToken(-1);
            }
          }
        });
    label1.setToolTipText(I18N.getText("panel.Selected.tooltip.previous"));
    label1.setBackground(null);
    add(label1);
  }
}
