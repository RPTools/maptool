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
package net.rptools.maptool.client.ui.token.dialog.edit;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.*;
import net.rptools.lib.MathUtil;
import net.rptools.maptool.client.AppStyle;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Support class used by the token editor dialog on the "Properties" tab to allow a token's image to
 * be moved around within a one-cell grid area. Scaling is supported using the mousewheel and
 * position is supported using left-drag. We should add rotation ability using Shift-mousewheel as
 * well.
 *
 * @author trevor
 */
public class TokenLayoutRenderPanel extends JPanel {
  private static final Logger log = LogManager.getLogger(TokenLayoutRenderPanel.class);

  public TokenLayoutRenderPanel() {
    evtTarget = MouseTarget.NONE;
    addRenderPaneListeners();
    setFocusable(true);
    addComponentListener(
        new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            size = getSize();
            requestFocus();
          }

          @Override
          public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            size = getSize();
            zoomFactor = -1;
            calcZoomFactor();
          }
        });
    log.debug("New TokenLayoutPanel");
  }

  private TokenLayoutPanelHelper helper;

  void setHelper(TokenLayoutPanelHelper h) {
    helper = h;
  }

  TokenLayoutPanelHelper getHelper() {
    return helper;
  }

  private enum MouseTarget {
    NONE,
    IMAGE_OFFSET,
    SCALE,
    VIEW_OFFSET,
    ZOOM
  }

  private MouseTarget evtTarget;
  private Token token;
  private int viewOffsetX = 0, viewOffsetY = 0, dragStartX, dragStartY;
  private int maxXoff = 100, maxYoff = 100;

  public void setMaxXoff(int maxX) {
    maxXoff = maxX;
  }

  public void setMaxYoff(int maxY) {
    maxYoff = maxY;
  }

  private double zoomFactor = -1.0;

  public Supplier<Number> zoom = () -> zoomFactor;
  public Consumer<Number> zoomSet = d -> zoomFactor = MathUtil.doublePrecision(d.doubleValue(), 4);

  Supplier<Number> getZoomSupplier() {
    return zoom;
  }

  Consumer<Number> getZoomConsumer() {
    return zoomSet;
  }

  public double getZoomFactor() {
    return zoom.get().doubleValue();
  }

  private void setZoomFactor(double d) {
    d = MathUtil.doublePrecision(d, 4);
    if (d == zoomFactor) {
      return;
    }
    if (helper.zoomPair.getPairSpinnerValue() != zoomFactor) {
      helper.zoomPair.setPairValue(d);
    }
  }

  Dimension size = getSize();

  protected Point2D.Double getViewOffset() {
    return new Point2D.Double(viewOffsetX, viewOffsetY);
  }

  public void setToken(Token token) {
    log.debug("Setting token");
    zoomFactor = -1d;
    this.token = token;
    helper.setToken(token, false);
    calcZoomFactor();
  }

  public void reset(Token token) {
    setToken(token);
  }

  /** Work out a zoom factor to fit the token on screen with a half cell border */
  protected void calcZoomFactor() {
    Rectangle2D fpBounds =
        new Rectangle2D.Double(
            helper.footprintBounds.getX(),
            helper.footprintBounds.getY(),
            helper.footprintBounds.getWidth(),
            helper.footprintBounds.getHeight());
    if (token == null || getSize().height == 0 || size == null) {
      return;
    }
    fpBounds.setRect(
        fpBounds.getWidth() / 2d,
        fpBounds.getHeight() / 2d,
        fpBounds.getWidth(),
        fpBounds.getHeight());
    if (helper.getTokenAnchorX() != 0 || helper.getTokenAnchorY() != 0) {
      fpBounds.add(
          new Rectangle2D.Double(
              fpBounds.getX() + helper.getTokenAnchorX(),
              fpBounds.getY() + helper.getTokenAnchorY(),
              fpBounds.getWidth() + helper.getTokenAnchorX(),
              fpBounds.getHeight() + helper.getTokenAnchorX()));
    }
    if (getHelper().isIsoFigure) {
      double th = token.getHeight() * fpBounds.getWidth() / token.getWidth();
      double iso_ho = fpBounds.getHeight() - th;
      fpBounds.add(
          new Rectangle2D.Double(
              fpBounds.getX(), fpBounds.getY() - iso_ho, fpBounds.getWidth(), th));
    }
    double fitWidth = fpBounds.getWidth() + helper.grid.getCellWidth() / 2;
    double fitHeight = fpBounds.getHeight() + helper.grid.getCellHeight() / 2;
    // which axis has the least space to grow
    boolean scaleToWidth = size.getWidth() - fitWidth < size.getHeight() - fitHeight;
    // set the zoom-factor
    double newZoom =
        Math.clamp(
            scaleToWidth ? size.getWidth() / fitWidth : size.getHeight() / fitHeight,
            TokenLayoutPanelHelper.MIN_ZOOM,
            TokenLayoutPanelHelper.MAX_ZOOM);

    setZoomFactor(newZoom);
    log.debug(
        "calculated ZoomFactor: "
            + zoomFactor
            + "\nSize: "
            + size
            + "\nFootprint bounds: "
            + fpBounds);
  }

  private void addRenderPaneListeners() {
    log.debug("addRenderPaneListeners");
    addMouseListener(
        new MouseAdapter() {
          String old;

          @Override
          public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);
            dragStartX = -1;
            dragStartY = -1;
            evtTarget = MouseTarget.NONE;
            helper.flagAsDirty();
          }

          @Override
          public void mousePressed(MouseEvent e) {
            super.mousePressed(e);
            dragStartX = e.getX();
            dragStartY = e.getY();
            if (SwingUtilities.isLeftMouseButton(e)) {
              // start token drag
              evtTarget = MouseTarget.IMAGE_OFFSET;
            } else if (SwingUtilities.isRightMouseButton(e)) {
              // start view drag
              evtTarget = MouseTarget.VIEW_OFFSET;
            }
          }

          @Override
          public void mouseEntered(MouseEvent e) {
            old = MapTool.getFrame().getStatusMessage();
            MapTool.getFrame()
                .setStatusMessage(I18N.getString("EditTokenDialog.status.layout.instructions"));
          }

          @Override
          public void mouseExited(MouseEvent e) {
            if (old != null) MapTool.getFrame().setStatusMessage(old);
            evtTarget = MouseTarget.NONE;
            helper.flagAsDirty();
          }

          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
              viewOffsetX = 0;
              viewOffsetY = 0;
              maxXoff = 100;
              maxYoff = 100;
              helper.resetPanel();
              evtTarget = MouseTarget.NONE;
            }
            if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 2) {
              viewOffsetX = 0;
              viewOffsetY = 0;
              maxXoff = 100;
              maxYoff = 100;
              helper.resetPanelToDefault();
              evtTarget = MouseTarget.NONE;
            }
          }
        });
    addMouseWheelListener(
        e -> {
          double delta = e.getPreciseWheelRotation();
          if (delta == 0) {
            return;
          }
          evtTarget = SwingUtil.isControlDown(e) ? MouseTarget.ZOOM : MouseTarget.SCALE;

          switch (evtTarget) {
            case MouseTarget.ZOOM -> {
              helper.zoomPair.incrementPairValue(delta);
              setZoomFactor(helper.zoomPair.getPairSpinnerValue());
            }
            case MouseTarget.SCALE -> {
              // Only for NOT snap-to-scale
              if (!token.isSnapToScale()) {
                return;
              }
              helper.scalePair.incrementPairValue(delta / 8d);
            }
            default -> log.debug("Defaulting - invalid mouse event target.");
          }
          evtTarget = MouseTarget.NONE;
          helper.flagAsDirty();
        });
    addMouseMotionListener(
        new MouseMotionAdapter() {
          @Override
          public void mouseDragged(MouseEvent e) {
            int dx = e.getX() - dragStartX;
            int dy = e.getY() - dragStartY;

            if (evtTarget == MouseTarget.IMAGE_OFFSET) {
              // avoid rounding to zero
              dx = dx == -1 || dx == 1 ? dx : (int) (dx / zoomFactor);
              dy = dy == -1 || dy == 1 ? dy : (int) (dy / zoomFactor);
              // limit to bounds
              int offX = Math.clamp(helper.getTokenAnchorX() + dx, -maxXoff, maxXoff);
              int offY = Math.clamp(helper.getTokenAnchorY() + dy, -maxYoff, maxYoff);
              helper.anchorXPair.setPairValue(offX);
              helper.anchorYPair.setPairValue(offY);

            } else if (evtTarget == MouseTarget.VIEW_OFFSET) {
              // drag view
              viewOffsetX += dx;
              viewOffsetY += dy;
              repaint();
            }
            dragStartX = e.getX();
            dragStartY = e.getY();
          }
        });
  }

  @Override
  protected void paintBorder(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    AppStyle.shadowBorder.paintWithin(g2d, 0, 0, getSize().width, getSize().height);
    super.paintBorder(g);
  }

  @Override
  protected void paintComponent(Graphics g) {
    helper.renderBits.init();
    if (helper.renderBits.viewBounds == null) {
      return;
    }
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHints(TokenLayoutPanelHelper.RenderBits.RENDERING_HINTS);
    // Cleanup
    g2d.clearRect(0, 0, size.width, size.height);
    // Background
    g2d.setPaint(TokenLayoutPanelHelper.RenderBits.backgroundTexture);
    g2d.fillRect(0, 0, size.width, size.height);

    if (helper.getTokenImage() == null || size.width == 0) {
      return;
    }
    if (zoomFactor == -1) {
      calcZoomFactor();
      return;
    }
    // Footprint
    helper.renderBits.paintFootprint(g, zoomFactor);
    // Add horizontal and vertical lines to help with centering
    helper.renderBits.paintCentreLines(g, true, false);
    // Token
    helper.renderBits.paintToken(g, evtTarget.equals(MouseTarget.IMAGE_OFFSET));
    helper.renderBits.paintCentreMark(g);
    g2d.dispose();
  }
}
