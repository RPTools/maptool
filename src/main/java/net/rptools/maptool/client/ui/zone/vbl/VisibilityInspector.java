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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.ui.zone.FogUtil;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.GraphicsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VisibilityInspector extends JPanel {
  private static final Logger log = LogManager.getLogger(VisibilityInspector.class);
  private static final double VISION_RANGE_CHANGE_RATE = 15.;

  private Map<Zone.TopologyType, Color> palette = new EnumMap<>(Zone.TopologyType.class);
  private Map<Zone.TopologyType, Area> toplogyAreas = new EnumMap<>(Zone.TopologyType.class);
  private Map<Zone.TopologyType, AreaTree> toplogyTrees = new EnumMap<>(Zone.TopologyType.class);
  private AffineTransform affineTransform;
  private Point2D point;
  private double visionRange;

  {
    palette.put(Zone.TopologyType.WALL_VBL, Color.blue);
    palette.put(Zone.TopologyType.HILL_VBL, Color.cyan);
    palette.put(Zone.TopologyType.PIT_VBL, Color.green);
    palette.put(Zone.TopologyType.COVER_VBL, Color.red);
  }

  public VisibilityInspector() {
    affineTransform = new AffineTransform();
    point = new Point(0, 0);
    visionRange = 200;

    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseMoved(MouseEvent e) {
            point = e.getPoint();
            try {
              point =
                  affineTransform.inverseTransform(
                      new Point2D.Double(point.getX(), point.getY()), null);
            } catch (NoninvertibleTransformException ex) {
              // Should never happen since we are careful with building our transformations.
              throw new RuntimeException(ex);
            }
            repaint();
          }
        });
    addMouseWheelListener(
        new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(MouseWheelEvent e) {
            final var amount = e.getUnitsToScroll();
            visionRange = Math.max(1.0, visionRange - amount * VISION_RANGE_CHANGE_RATE);
            repaint();
          }
        });
  }

  public void setTopology(Area wallVbl, Area hillVbl, Area pitVbl, Area coverVbl) {
    this.toplogyAreas.clear();
    this.toplogyAreas.put(Zone.TopologyType.WALL_VBL, wallVbl);
    this.toplogyAreas.put(Zone.TopologyType.HILL_VBL, hillVbl);
    this.toplogyAreas.put(Zone.TopologyType.PIT_VBL, pitVbl);
    this.toplogyAreas.put(Zone.TopologyType.COVER_VBL, coverVbl);

    this.toplogyTrees.clear();
    var bounds = new Rectangle();
    for (final var entry : this.toplogyAreas.entrySet()) {
      var type = entry.getKey();
      var area = entry.getValue();

      var tree = new AreaTree(area);
      this.toplogyTrees.put(type, tree);
      bounds.add(area.getBounds());
    }

    final var dimensions = getSize();
    final var scaleX = dimensions.getWidth() / bounds.getWidth();
    final var scaleY = dimensions.getHeight() / bounds.getHeight();
    var scale = Math.min(scaleX, scaleY);
    if (scale <= 0) {
      scale = 1.;
    }
    affineTransform = AffineTransform.getTranslateInstance(-bounds.getX(), -bounds.getY());
    affineTransform.scale(scale, scale);
  }

  @Override
  protected void paintComponent(Graphics g) {
    final var size = getSize();
    final var g2d = (Graphics2D) g.create(0, 0, size.width, size.height);

    g2d.setColor(Color.white);
    g2d.fillRect(0, 0, size.width, size.height);

    g2d.transform(affineTransform);
    g2d.setStroke(new BasicStroke(1));

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
    for (final var entry : this.toplogyAreas.entrySet()) {
      g2d.setColor(palette.get(entry.getKey()).brighter());
      g2d.fill(entry.getValue());
    }

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.f));
    for (final var entry : this.toplogyAreas.entrySet()) {
      g2d.setColor(palette.get(entry.getKey()));
      g2d.draw(entry.getValue());
    }

    final var CIRCLE_SEGMENTS = 60;
    final var unobstructedVision =
        GraphicsUtil.createLineSegmentEllipse(
            -visionRange, -visionRange, visionRange, visionRange, CIRCLE_SEGMENTS);
    unobstructedVision.transform(AffineTransform.getTranslateInstance(point.getX(), point.getY()));
    final var visionBounds = new Area(unobstructedVision.getBounds());

    Area vision;
    vision =
        FogUtil.calculateVisibility(
            new Point((int) point.getX(), (int) point.getY()),
            unobstructedVision,
            toplogyTrees.get(Zone.TopologyType.WALL_VBL),
            toplogyTrees.get(Zone.TopologyType.HILL_VBL),
            toplogyTrees.get(Zone.TopologyType.PIT_VBL),
            toplogyTrees.get(Zone.TopologyType.COVER_VBL));

    final var obstructedVision = new Area(unobstructedVision);

    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f));
    {
      obstructedVision.subtract(vision);
      g2d.setColor(Color.red);
      g2d.fill(vision);
      g2d.setColor(Color.black);
      g2d.draw(vision);
    }
    {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .2f));
      g2d.setStroke(new BasicStroke(1));
      g2d.setColor(Color.red.brighter());
      g2d.fill(obstructedVision);
    }
    {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
      g2d.setColor(Color.black);
      g2d.draw(visionBounds);
    }

    {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
      g2d.setStroke(new BasicStroke(3));
      g2d.setColor(Color.black);
      g2d.drawLine((int) point.getX(), (int) point.getY(), (int) point.getX(), (int) point.getY());
    }
  }

  public static void main(String[] args) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setBounds(100, 100, 800, 800);

    final var panel = new VisibilityInspector();

    f.add(panel);
    f.setVisible(true);

    SwingUtilities.invokeLater(() -> buildRepeatedSquaredTopology(panel));
  }

  private static void buildRepeatedSquaredTopology(VisibilityInspector visibilityInspector) {
    Area wallArea = new Area();
    Area hillArea = new Area();
    Area pitArea = new Area();
    Area coverArea = new Area();
    wallArea.add(new Area(new Rectangle(0, 0, 750, 750)));
    wallArea.subtract(new Area(new Rectangle(50, 50, 650, 650)));
    for (int x = 1; x < 7; ++x) {
      for (int y = 1; y < 7; ++y) {
        final var pillar = new Area(new Rectangle(100 * x, 100 * y, 50, 50));
        final int index = (x + y) % 4;

        switch (index) {
          case 0 -> wallArea.add(pillar);
          case 1 -> hillArea.add(pillar);
          case 2 -> pitArea.add(pillar);
          case 3 -> {
            hillArea.add(pillar);
            pitArea.add(pillar);
          }
        }
      }
    }
    visibilityInspector.setTopology(wallArea, hillArea, pitArea, coverArea);
  }

  private static void buildTripleIntersectionTopology(VisibilityInspector visibilityInspector) {
    Area wallArea = new Area();
    Area hillArea = new Area();
    Area pitArea = new Area();
    Area coverArea = new Area();
    wallArea.add(new Area(new Rectangle(0, 0, 750, 750)));
    wallArea.subtract(new Area(new Rectangle(50, 50, 650, 650)));

    wallArea.add(new Area(new Polygon(new int[] {250, 450, 450}, new int[] {250, 450, 250}, 3)));
    hillArea.add(new Area(new Polygon(new int[] {250, 450, 450}, new int[] {450, 450, 250}, 3)));
    pitArea.add(new Area(new Polygon(new int[] {275, 325, 325}, new int[] {350, 150, 550}, 3)));

    visibilityInspector.setTopology(wallArea, hillArea, pitArea, coverArea);
  }

  private static void buildSinglePillarTopology(VisibilityInspector visibilityInspector) {
    Area wallArea = new Area();
    Area hillArea = new Area();
    Area pitArea = new Area();
    Area coverArea = new Area();
    wallArea.add(new Area(new Rectangle(0, 0, 750, 750)));
    wallArea.subtract(new Area(new Rectangle(50, 50, 650, 650)));

    final var pillar = new Area(new Rectangle(300, 300, 50, 50));
    wallArea.add(pillar);

    visibilityInspector.setTopology(wallArea, hillArea, pitArea, coverArea);
  }
}
