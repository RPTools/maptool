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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;

public class ZoneSelectionPopup extends JPopupMenu {

  private static final int PADDING = 5;

  public ZoneSelectionPopup() {

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

      add(new SelectZoneButton(renderer));
    }
  }

  private class SelectZoneButton extends JMenuItem implements ActionListener {

    private ZoneRenderer renderer;
    private boolean isMouseOver;

    public SelectZoneButton(ZoneRenderer renderer) {
      this.renderer = renderer;
      addActionListener(this);
      addMouseListener(
          new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
              isMouseOver = true;
              repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
              isMouseOver = false;
              repaint();
            }
          });
    }

    @Override
    public Dimension getPreferredSize() {
      FontMetrics fm = getFontMetrics(getFont());

      int width =
          Math.max(
              SwingUtilities.computeStringWidth(fm, renderer.getZone().getName()) + PADDING * 2,
              100);
      int height = fm.getHeight() + PADDING * 2;

      return new Dimension(width, height);
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

    @Override
    protected void paintComponent(Graphics g) {

      Graphics2D g2d = (Graphics2D) g;
      FontMetrics fm = g.getFontMetrics();
      Object oldAA = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      boolean isSelected = renderer == MapTool.getFrame().getCurrentZoneRenderer();

      // Background
      Dimension size = getSize();
      if (isSelected) {
        g2d.setPaint(
            new GradientPaint(
                0, 0, Color.lightGray.brighter(), 0, size.height, Color.gray.brighter()));
        g.fillRect(0, 0, size.width, size.height);
        // g2d.setPaint(new GradientPaint(0, 0, Color.lightGray, size.width, 0,
        // Color.gray.brighter()));
        // g.drawLine(PADDING, (size.height-fm.getHeight())/2+fm.getAscent()+2,
        // size.width-1-PADDING, (size.height-fm.getHeight())/2+fm.getAscent()+2);
      } else {
        g.setColor(Color.white);
        g.fillRect(0, 0, size.width, size.height);
      }

      // Visibility
      if (!renderer.getZone().isVisible()) {
        Stroke oldStroke = g2d.getStroke();
        g.setColor(Color.lightGray);
        g2d.setStroke(new BasicStroke(2));

        for (int i = 0; i < size.width * 2 || i < size.height * 2; i += 10) {
          g.drawLine(i, 0, 0, i);
        }

        g2d.setStroke(oldStroke);
      }

      // Mouse over
      if (!isSelected && isMouseOver) {
        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(Color.yellow);
        g2d.setStroke(new BasicStroke(5));
        g2d.drawRoundRect(0, 0, size.width - 1, size.height - 1, 10, 10);
        g2d.setStroke(oldStroke);
      }

      // Label
      String name = renderer.getZone().getName();
      if (name == null || name.length() == 0) {
        name = "Map";
      }

      int nameWidth = SwingUtilities.computeStringWidth(g.getFontMetrics(), name);

      if (!renderer.getZone().isVisible()) {
        // Clear out the lines from the label
        g.setColor(isSelected ? Color.lightGray : Color.white);
        g.fillRect(PADDING - 3, (size.height - fm.getHeight()) / 2, nameWidth + 6, fm.getHeight());
      }

      g.setColor(Color.black);
      g.drawString(name, PADDING, (size.height - fm.getHeight()) / 2 + fm.getAscent());

      // Border
      if (isSelected) {
        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(Color.blue);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(0, 0, size.width - 1, size.height - 1, 10, 10);
        g2d.setStroke(oldStroke);
      }

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
      repaint();
      return super.imageUpdate(img, infoflags, x, y, w, h);
    }
  }
}
