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
package net.rptools.maptool.client.ui.zone.vbl;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.GeometryFactory;

public class AreaTreeInspector extends JPanel {
  private final AreaTree tree;
  private final Color[] colors = {Color.gray, Color.blue, Color.yellow, Color.orange, Color.cyan};

  private Point point;

  public AreaTreeInspector() {
    Area area = new Area();

    area.add(new Area(new Rectangle(100, 100, 300, 300)));
    area.subtract(new Area(new Rectangle(150, 200, 100, 100)));
    area.subtract(new Area(new Rectangle(300, 200, 75, 100)));
    area.add(new Area(new Rectangle(175, 225, 50, 50)));
    area.subtract(new Area(new Rectangle(180, 230, 20, 20)));

    area.add(new Area(new Rectangle(450, 100, 300, 300)));
    area.subtract(new Area(new Rectangle(500, 200, 100, 100)));
    area.subtract(new Area(new Rectangle(650, 200, 75, 100)));
    area.add(new Area(new Rectangle(525, 225, 50, 50)));

    tree = new AreaTree(area, false);

    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            point = e.getPoint();
            repaint();
          }
        });
  }

  @Override
  protected void paintComponent(Graphics g) {
    final var geometryFactory = new GeometryFactory();
    final var shapeWriter = new ShapeWriter();

    Dimension size = getSize();
    g.setColor(Color.white);
    g.fillRect(0, 0, size.width, size.height);

    // paintOcean((Graphics2D)g, ocean, 0);

    if (point != null) {
      Graphics2D g2d = (Graphics2D) g.create(0, 0, size.width, size.height);

      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
      g2d.setColor(Color.blue);

      var container = tree.getContainerAt(point);
      if (container != null && container.getBounds() != null) {
        g2d.fill(container.getBounds());
      }
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .25f));
      g2d.setColor(Color.red);

      if (container != null) {
        for (VisibleAreaSegment segment :
            container.getVisibleBoundarySegements(geometryFactory, point, false)) {
          var shadow = segment.castShadow(Integer.MAX_VALUE / 2);
          Area area = new Area(shapeWriter.toShape(shadow));
          if (area != null) {
            g2d.fill(area);
          }
        }
      }
      g2d.dispose();
    }
  }

  private void paintOcean(Graphics2D g, AreaOcean ocean, int depth) {
    if (ocean.getBounds() != null) {
      g.setColor(Color.white);
      g.fill(ocean.getBounds());

      g.setColor(colors[depth % colors.length]);
      g.draw(ocean.getBounds());
    }
    for (AreaIsland island : ocean.getIslands()) {
      paintIsland(g, island, depth + 1);
    }
  }

  private void paintIsland(Graphics2D g, AreaIsland island, int depth) {
    g.setColor(Color.gray);
    g.fill(island.getBounds());

    g.setColor(colors[depth % colors.length]);
    g.draw(island.getBounds());

    for (AreaOcean ocean : island.getOceans()) {
      paintOcean(g, ocean, depth + 1);
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setBounds(100, 100, 800, 500);
    f.add(new AreaTreeInspector());
    f.setVisible(true);
  }
}
