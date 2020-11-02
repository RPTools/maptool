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
package net.rptools.maptool.client.tool.drawing;

import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL;
import net.rptools.maptool.client.ui.zone.vbl.TokenVBL.JTS_SimplifyMethodType;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.model.drawing.Rectangle;

public class AutoTopologyTool extends HollowRectangleTopologyTool {
	  private JTS_SimplifyMethodType jtsMethod = JTS_SimplifyMethodType.getDefault();
	  private double jtsDistanceTolerance;
  private static final Logger log = LogManager.getLogger(AutoTopologyTool.class);
  private class AutoTopologyDialog extends JDialog {
	private static final long serialVersionUID = -2175128142690426855L;
	private BufferedImage thresholdImg;
	  private BufferedImage sourceImg;
	  private JSlider slider;
	  private JLabel lbl;
	  private int curVal;
    public AutoTopologyDialog(int x1, int x2, int y1, int y2) {
      super(MapTool.getFrame(), I18N.getText("tool.autotopology.dialog.title"), true);
      log.debug("x1={},x2={},y1={},y2={}", x1, x2, y1, y2);
      setLayout(new FlowLayout());
      JButton ok = new JButton("Ok");
      ok.addActionListener( e -> dispose() );
      add(ok);
      ZoneRenderer currentRenderer = MapTool.getFrame().getCurrentZoneRenderer();
      int w = currentRenderer.getWidth();
      int h = currentRenderer.getHeight();
      BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
      Graphics2D g = img.createGraphics();
      currentRenderer.paintComponent(g);
      g.dispose();
      ScreenPoint sp1 = ScreenPoint.fromZonePoint(currentRenderer, x1, y1);
      ScreenPoint sp2 = ScreenPoint.fromZonePoint(currentRenderer, x2, y2);
      log.debug("sp1={},sp2={},w={},h={}", sp1, sp2, w, h);
      sourceImg = img.getSubimage((int)sp1.getX(), (int)sp1.getY(), (int)(sp2.getX() - sp1.getX()), (int)(sp2.getY() - sp1.getY()));
      thresholdImg = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
      log.info("w={},h={}", sourceImg.getWidth(), sourceImg.getHeight());
      slider = new JSlider(JSlider.VERTICAL, 0, 255, 100);
      slider.addChangeListener(e -> eval());
      lbl = new JLabel();
      eval();
      add(slider);
      add(lbl);
      pack();
    }
    private void eval() {
    	curVal = slider.getValue();
        log.debug("curVal={}", curVal);
	    for (int i = 0; i < sourceImg.getWidth(); i++)
	        for (int j = 0; j < sourceImg.getHeight(); j++)
	      	  thresholdImg.setRGB(i, j, (sourceImg.getRGB(i, j) & 0xff) > curVal ? 0xffffff : 0);
        ImageIcon icon = new ImageIcon(thresholdImg);
        lbl.setIcon(icon);
    }
  }

  private static final long serialVersionUID = 5547527015512994019L;

  public AutoTopologyTool() {
    try {
      setIcon(
          new ImageIcon(
              ImageIO.read(
                  getClass()
                      .getClassLoader()
                      .getResourceAsStream(
                          "net/rptools/maptool/client/image/tool/top-blue-hdiamond.png"))));
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  @Override
  public String getInstructions() {
    return "tool.autotopology.instructions";
  }

  @Override
  public String getTooltip() {
    return "tool.autotopologyhollow.tooltip";
  }

  @Override
  public void mousePressed(MouseEvent e) {
    ZonePoint zp = getPoint(e);

    if (SwingUtilities.isLeftMouseButton(e)) {
      if (rectangle == null) {
        rectangle = new Rectangle(zp.x, zp.y, zp.x, zp.y);
      } else {
        rectangle.getEndPoint().x = zp.x;
        rectangle.getEndPoint().y = zp.y;

        int x1 = Math.min(rectangle.getStartPoint().x, rectangle.getEndPoint().x);
        int x2 = Math.max(rectangle.getStartPoint().x, rectangle.getEndPoint().x);
        int y1 = Math.min(rectangle.getStartPoint().y, rectangle.getEndPoint().y);
        int y2 = Math.max(rectangle.getStartPoint().y, rectangle.getEndPoint().y);
        renderer.repaint();
        // TODO: send this to the server
        rectangle = null;
        
        AutoTopologyDialog dialog = new AutoTopologyDialog(x1,x2,y1,y2);
        SwingUtil.centerOver(dialog, MapTool.getFrame());
        dialog.setVisible(true);

        Thread t = new Thread() {
        	public void run() {
        		calculateAndPublishArea(e, x1, x2, y1, y2, dialog);
        	}
        };
        t.start();
      }
      setIsEraser(isEraser(e));
    }
    // Potential map dragging
    if (SwingUtilities.isRightMouseButton(e)) {
      setDragStart(e.getX(), e.getY());
    }
  }

  private void calculateAndPublishArea(MouseEvent e, int x1, int x2, int y1, int y2, AutoTopologyDialog dialog) {
	  Area area = new Area();

	  MapTool.getFrame().startDeterminateAction(dialog.thresholdImg.getWidth());
	  for (int i = 0; i < dialog.thresholdImg.getWidth(); i++) {
		  MapTool.getFrame().updateDeterminateActionProgress(1);		  
		  for (int j = 0; j < dialog.thresholdImg.getHeight(); j++) {
			  if ((dialog.thresholdImg.getRGB(i, j) & 0xff) <= dialog.curVal) {
				  area.add(new Area(new Rectangle2D.Double(i, j, 1, 1)));
			  }
		  }
	  }
	  MapTool.getFrame().endDeterminateAction();

/*
    for (int xx=0; xx<dialog.thresholdImg.getWidth(); xx++) {
    	log.info("xx={}", xx);
        int firsty = 0;
        for (int yy=0; yy<dialog.thresholdImg.getHeight(); yy++) {
            if ((dialog.thresholdImg.getRGB(xx, yy) & 0xff) <= dialog.curVal) {
            } else {
            	if (firsty != yy) {
        			area.add(new Area(new Rectangle2D.Double(xx, firsty, w, yy - firsty)));
            	}
            	firsty = yy;
            }
        }

    	if (firsty != dialog.thresholdImg.getHeight()) {
			area.add(new Area(new Rectangle2D.Double(xx, firsty, w, dialog.thresholdImg.getHeight() - firsty)));
    	}
	}
*/

	AffineTransform trafo = AffineTransform.getTranslateInstance(x1, y1);
	trafo.concatenate(AffineTransform.getScaleInstance((x2 - x1) / (double) dialog.thresholdImg.getWidth(), (y2 - y1) / (double) dialog.thresholdImg.getHeight()));
	area.transform(trafo);
	log.info("area.x={}, area.y={}, area.width={}, are.height={}", area.getBounds().x, area.getBounds().y, area.getBounds().width, area.getBounds().height);
	if (isEraser(e)) {
	  getZone().removeTopology(area);
	  MapTool.serverCommand()
	      .removeTopology(getZone().getId(), area, getZone().getTopologyMode());
	} else {
	  getZone().addTopology(area);
	  MapTool.serverCommand().addTopology(getZone().getId(), area, getZone().getTopologyMode());
	}
  }
}
