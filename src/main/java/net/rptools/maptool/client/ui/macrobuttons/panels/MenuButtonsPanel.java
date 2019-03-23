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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;

public class MenuButtonsPanel extends JPanel {

  public MenuButtonsPanel() {
    // TODO: refactoring reminder
    setLayout(new FlowLayout(FlowLayout.LEFT));

    addSelectAllButton();
    addDeselectAllButton();
    addSpacer();
    addSelectPreviousButton();
    addSelectNextButton();
    addSpacer();
    addRevertToPreviousButton();
  }

  private void addSelectAllButton() {
    ImageIcon i = new ImageIcon(AppStyle.arrowOut);
    JButton label =
        new JButton(i) {
          public Insets getInsets() {
            return new Insets(2, 2, 2, 2);
          }
        };
    label.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
            renderer.selectTokens(
                new Rectangle(
                    renderer.getX(), renderer.getY(), renderer.getWidth(), renderer.getHeight()));
          }
        });
    label.setToolTipText(I18N.getText("panel.Selected.tooltip.selectAll"));
    label.setBackground(null);
    add(label);
  }

  private void addDeselectAllButton() {
    ImageIcon i3 = new ImageIcon(AppStyle.arrowIn);
    JButton label3 =
        new JButton(i3) {
          public Insets getInsets() {
            return new Insets(2, 2, 2, 2);
          }
        };
    label3.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
            renderer.clearSelectedTokens();
          }
        });
    label3.setToolTipText(I18N.getText("panel.Selected.tooltip.deslectAll"));
    label3.setBackground(null);
    add(label3);
  }

  private void addRevertToPreviousButton() {
    ImageIcon i1 = new ImageIcon(AppStyle.arrowRotateClockwise);
    JButton label1 =
        new JButton(i1) {
          public Insets getInsets() {
            return new Insets(2, 2, 2, 2);
          }
        };
    label1.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            MapTool.getFrame().getCurrentZoneRenderer().undoSelectToken();
          }
        });
    label1.setToolTipText(I18N.getText("panel.Selected.tooltip.revertToPrevious"));
    label1.setBackground(null);
    add(label1);
  }

  private void addSpacer() {
    JPanel panel =
        new JPanel() {
          public Dimension getPreferredSize() {
            return new Dimension(10, 10);
          }

          public Insets getInsets() {
            return new Insets(0, 0, 0, 0);
          }
        };
    add(panel);
  }

  private void addSelectNextButton() {
    ImageIcon i1 = new ImageIcon(AppStyle.arrowRight);
    JButton label1 =
        new JButton(i1) {
          public Insets getInsets() {
            return new Insets(2, 2, 2, 2);
          }
        };
    label1.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            MapTool.getFrame().getCurrentZoneRenderer().cycleSelectedToken(1);
          }
        });
    label1.setToolTipText(I18N.getText("panel.Selected.tooltip.next"));
    label1.setBackground(null);
    add(label1);
  }

  private void addSelectPreviousButton() {
    ImageIcon i1 = new ImageIcon(AppStyle.arrowLeft);
    JButton label1 =
        new JButton(i1) {
          public Insets getInsets() {
            return new Insets(2, 2, 2, 2);
          }
        };
    label1.addMouseListener(
        new MouseAdapter() {
          public void mouseReleased(MouseEvent event) {
            MapTool.getFrame().getCurrentZoneRenderer().cycleSelectedToken(-1);
          }
        });
    label1.setToolTipText(I18N.getText("panel.Selected.tooltip.previous"));
    label1.setBackground(null);
    add(label1);
  }

  @Override
  public Dimension getPreferredSize() {

    Dimension size = getParent().getSize();

    FlowLayout layout = (FlowLayout) getLayout();
    Insets insets = getInsets();

    // This isn't exact, but hopefully it's close enough
    int x = layout.getHgap() + insets.left;
    int y = layout.getVgap();
    int rowHeight = 0;
    for (Component c : getComponents()) {

      Dimension cSize = c.getPreferredSize();
      if (x + cSize.width + layout.getHgap() > size.width - insets.right && x > 0) {
        x = 0;
        y += rowHeight + layout.getVgap();
        rowHeight = 0;
      }

      x += cSize.width + layout.getHgap();
      rowHeight = Math.max(cSize.height, rowHeight);
    }

    y += rowHeight + layout.getVgap();

    y += getInsets().top;
    y += getInsets().bottom;

    Dimension prefSize = new Dimension(size.width, y);
    return prefSize;
  }
}
