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
package net.rptools.maptool.client.ui.token;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.rptools.lib.MD5Key;
import net.rptools.lib.swing.SwingUtil;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.TokenShape;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.ImageManager;

/**
 * Support class used by the token editor dialog on the "Properties" tab to allow a token's image to
 * be moved around within a one-cell grid area. Scaling is supported using the mousewheel and
 * position is supported using left-drag. We should add rotation ability using Shift-mousewheel as
 * well.
 *
 * @author trevor
 */
public class TokenLayoutPanel extends JPanel {
  private Token token;
  private int dragOffsetX;
  private int dragOffsetY;
  private MD5Key tokenImage;

  public TokenLayoutPanel() {
    addMouseWheelListener(
        new MouseWheelListener() {
          @Override
          public void mouseWheelMoved(MouseWheelEvent e) {
            // Not for snap-to-scale
            if (!token.isSnapToScale()) {
              return;
            }
            double delta = e.getWheelRotation() > 0 ? -.1 : .1;
            if (SwingUtil.isShiftDown(e)) {
              // Nothing yet, as changing the facing isn't the right way to handle it --
              // the image itself really should be rotated. And it's probably better to
              // not simply store a Transform but to create a new image. We could
              // store an AffineTransform until the dialog is closed and then create
              // the new image. But the amount of rotation needs to be saved so
              // that future adjustments can return back to the original image (as
              // a way of reducing round off error from multiple rotations).
            }
            double scale = token.getSizeScale() + delta;

            // Range
            scale = Math.max(.1, scale);
            scale = Math.min(3, scale);
            token.setSizeScale(scale);
            repaint();
          }
        });
    addMouseListener(
        new MouseAdapter() {
          String old;

          @Override
          public void mousePressed(MouseEvent e) {
            dragOffsetX = e.getX();
            dragOffsetY = e.getY();
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            old = MapTool.getFrame().getStatusMessage();
            MapTool.getFrame()
                .setStatusMessage("Mouse Wheel to zoom; double-LClick to reset position and zoom");
          }

          @Override
          public void mouseExited(MouseEvent e) {
            if (old != null) MapTool.getFrame().setStatusMessage(old);
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
              dragOffsetX = 0;
              dragOffsetY = 0;
              token.setAnchor(0, 0);
              token.setSizeScale(1.0);
              repaint();
            }
          }
        });
    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            int dx = e.getX() - dragOffsetX;
            int dy = e.getY() - dragOffsetY;

            Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

            int gridSize = zone.getGrid().getSize();
            int halfGridSize = gridSize / 2;
            int maxXoff = Math.max(halfGridSize, token.getBounds(zone).width - gridSize);
            int maxYoff = Math.max(halfGridSize, token.getBounds(zone).height - gridSize);

            int offX = Math.min(maxXoff, Math.max(token.getAnchor().x + dx, -maxXoff));
            int offY = Math.min(maxYoff, Math.max(token.getAnchor().y + dy, -maxYoff));

            token.setAnchor(offX, offY);
            dragOffsetX = e.getX();
            dragOffsetY = e.getY();
            repaint();
          }
        });
  }

  public double getSizeScale() {
    return token.getSizeScale();
  }

  public int getAnchorX() {
    return token.getAnchor().x;
  }

  public int getAnchorY() {
    return token.getAnchor().y;
  }

  public void setToken(Token token) {
    this.token = new Token(token);
    setTokenImage(token.getImageAssetId());
  }

  public MD5Key getTokenImage() {
    return tokenImage;
  }

  public void setTokenImage(MD5Key tokenImage) {
    this.tokenImage = tokenImage;
  }

  @Override
  protected void paintComponent(Graphics g) {
    Dimension size = getSize();
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();

    // Gather info
    BufferedImage image = ImageManager.getImage(getTokenImage());

    Rectangle tokenSize = token.getBounds(zone);
    Dimension imgSize = new Dimension(image.getWidth(), image.getHeight());

    // If figure we need to calculate an additional offset for the token height
    double iso_ho = 0;
    if (token.getShape() == TokenShape.FIGURE) {
      double th = token.getHeight() * Double.valueOf(tokenSize.width) / token.getWidth();
      iso_ho = tokenSize.height - th;
      tokenSize = new Rectangle(tokenSize.x, tokenSize.y - (int) iso_ho, tokenSize.width, (int) th);
    }

    SwingUtil.constrainTo(imgSize, tokenSize.width, tokenSize.height);

    Point centerPoint = new Point(size.width / 2, size.height / 2);
    Graphics2D g2d = (Graphics2D) g;

    // Background
    ((Graphics2D) g)
        .setPaint(
            new TexturePaint(
                AppStyle.panelTexture,
                new Rectangle(
                    0, 0, AppStyle.panelTexture.getWidth(), AppStyle.panelTexture.getHeight())));
    g2d.fillRect(0, 0, size.width, size.height);
    AppStyle.shadowBorder.paintWithin((Graphics2D) g, 0, 0, size.width, size.height);

    // Grid
    if (zone.getGrid().getCapabilities().isSnapToGridSupported()) {
      Area gridShape = zone.getGrid().getCellShape();
      int offsetX = (size.width - gridShape.getBounds().width) / 2;
      int offsetY = (size.height - gridShape.getBounds().height) / 2;
      g2d.setColor(Color.black);

      // Add horizontal and vertical lines to help with centering
      g2d.drawLine(
          0, (size.height - (int) iso_ho) / 2, size.width, (size.height - (int) iso_ho) / 2);
      g2d.drawLine(size.width / 2, 0, size.width / 2, (size.height - (int) iso_ho));

      offsetY = offsetY - (int) (iso_ho / 2);
      g2d.translate(offsetX, offsetY);
      g2d.draw(gridShape);
      g2d.translate(-offsetX, -offsetY);
    }
    // Token
    g2d.drawImage(
        image,
        centerPoint.x - imgSize.width / 2 + token.getAnchor().x,
        centerPoint.y - imgSize.height / 2 + token.getAnchor().y,
        imgSize.width,
        imgSize.height,
        this);
  }
}
