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
package net.rptools.maptool.client.ui.zone;

import com.google.common.eventbus.Subscribe;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.events.ZoneActivated;
import net.rptools.maptool.client.swing.ImageBorder;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.events.MapToolEventBus;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.zones.FogChanged;
import net.rptools.maptool.util.ImageManager;

/** */
public class ZoneMiniMapPanel extends JPanel {

  private static final int SIZE_WIDTH = 125;
  private static final int SIZE_HEIGHT = 100;

  private Rectangle bounds;
  private BufferedImage backBuffer;

  private Zone zone;

  public ZoneMiniMapPanel() {

    addMouseListener(new MouseHandler());

    new MapToolEventBus().getMainEventBus().register(this);
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
   */
  @Override
  protected void paintComponent(Graphics g) {

    Dimension mySize = getSize();
    g.setColor(Color.black);
    g.fillRect(0, 0, mySize.width, mySize.height);

    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    if (renderer == null) {
      return;
    }

    if (backBuffer == null
        || backBuffer.getWidth() != mySize.width
        || backBuffer.getHeight() != mySize.height) {

      backBuffer = new BufferedImage(mySize.width, mySize.height, Transparency.OPAQUE);

      // TODO: This is a naive solution. In the future, actually render the zone
      BufferedImage img = renderer.getMiniImage(SIZE_WIDTH);
      if (img == null || img == ImageManager.TRANSFERING_IMAGE) {
        img = ImageManager.TRANSFERING_IMAGE;

        // Let's wake up when the image arrives
        // ImageManager.addObservers(renderer.getZone().getBackgroundAssetId(), this);
      }

      ImageBorder border = AppStyle.miniMapBorder;

      Dimension size = new Dimension(img.getWidth(), img.getHeight());
      SwingUtil.constrainTo(
          size,
          mySize.width - border.getLeftMargin() - border.getRightMargin(),
          mySize.height - border.getTopMargin() - border.getBottomMargin());

      int x =
          border.getLeftMargin()
              + (mySize.width - size.width - border.getLeftMargin() - border.getRightMargin()) / 2;
      int y =
          border.getTopMargin()
              + (mySize.height - size.height - border.getTopMargin() - border.getBottomMargin())
                  / 2;
      int w = size.width;
      int h = size.height;

      Graphics2D g2d = backBuffer.createGraphics();
      g2d.setClip(0, 0, backBuffer.getWidth(), backBuffer.getHeight());

      bounds = new Rectangle(x, y, w, h);

      g2d.drawImage(img, x, y, w, h, this);

      border.paintWithin(g2d, 0, 0, mySize.width, mySize.height);

      g2d.dispose();
    }

    g.drawImage(backBuffer, 0, 0, this);
  }

  @Override
  public Dimension getPreferredSize() {
    if (MapTool.getFrame() == null) {
      return new Dimension(0, 0);
    }

    ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
    if (renderer == null) {
      return new Dimension(0, 0);
    }
    BufferedImage img = renderer.getMiniImage(SIZE_WIDTH);
    if (img == null || img == ImageManager.TRANSFERING_IMAGE) {
      img = ImageManager.TRANSFERING_IMAGE;

      // Let's wake up when the image arrives
      // ImageManager.addObservers(renderer.getZone().getBackgroundAssetId(), this);
    }

    ImageBorder border = AppStyle.miniMapBorder;

    Dimension size = new Dimension(img.getWidth(), img.getHeight());
    SwingUtil.constrainTo(size, SIZE_WIDTH, SIZE_HEIGHT);
    size.width += border.getLeftMargin() + border.getRightMargin();
    size.height += border.getTopMargin() + border.getBottomMargin();

    return size;
  }

  public void flush() {
    backBuffer = null;
  }

  public void resize() {

    setSize(getPreferredSize());
  }

  @Subscribe
  private void onZoneActivated(ZoneActivated event) {
    this.zone = event.zone();

    flush();
    resize();

    // getParent().doLayout();
    repaint();
  }

  @Subscribe
  private void onFogChanged(FogChanged event) {
    if (event.zone() != this.zone) {
      return;
    }

    flush();
    repaint();
  }

  ////
  // IMAGE OBSERVER
  @Override
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
    if (infoflags == ImageObserver.ALLBITS) {
      flush();
      resize();
      getParent().doLayout();
      repaint();
    }
    return super.imageUpdate(img, infoflags, x, y, w, h);
  }

  ////
  // MOUSE HANDLER
  private static class MouseHandler extends MouseAdapter {
    @Override
    public void mouseClicked(MouseEvent e) {

      if (SwingUtilities.isLeftMouseButton(e)) {

        // Minimap interaction
        // TODO: Make this work for unbounded
        // int miniX = e.getX() - bounds.x;
        // int miniY = e.getY() - bounds.y;
        //
        // int mapX = (int)(renderer.getZone().getWidth() * (miniX / (double)bounds.width));
        // int mapY = (int)(renderer.getZone().getHeight() * (miniY / (double)bounds.height));
        //
        // renderer.centerOn(new ZonePoint(mapX, mapY));
      }
    }
  }
}
