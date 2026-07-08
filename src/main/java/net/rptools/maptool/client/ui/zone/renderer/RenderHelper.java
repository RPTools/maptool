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
package net.rptools.maptool.client.ui.zone.renderer;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.function.Consumer;
import net.rptools.lib.CodeTimer;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.client.ui.Scale;
import net.rptools.maptool.client.ui.zone.BufferedImagePool;

/**
 * Transform graphics objects into world space to enable more convenient rendering for some layers.
 *
 * <p>Also optionally renders onto an intermediate buffer.
 */
public class RenderHelper {
  private final String timerPrefix;
  private final ZoneRenderer renderer;
  private final BufferedImagePool tempBufferPool;

  private RenderHelper(
      ZoneRenderer renderer, BufferedImagePool tempBufferPool, String timerPrefix) {
    this.renderer = renderer;
    this.tempBufferPool = tempBufferPool;
    this.timerPrefix = timerPrefix;
  }

  public RenderHelper(ZoneRenderer renderer, BufferedImagePool tempBufferPool) {
    this(renderer, tempBufferPool, "RenderHelper");
  }

  public ImageObserver getImageObserver() {
    return renderer;
  }

  public RenderHelper withTimerPrefix(String timerPrefix) {
    return new RenderHelper(renderer, tempBufferPool, timerPrefix);
  }

  private void doRender(Graphics2D g, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();

    timer.start("%s-useAA", timerPrefix);
    SwingUtil.useAntiAliasing(g);
    timer.stop("%s-useAA", timerPrefix);

    timer.start("%s-setTransform", timerPrefix);
    Scale scale = renderer.getViewModel().getZoneScale();
    AffineTransform af = new AffineTransform();
    af.translate(scale.getOffsetX(), scale.getOffsetY());
    af.scale(scale.getScale(), scale.getScale());
    g.setTransform(af);
    timer.stop("%s-setTransform", timerPrefix);

    timer.start("%s-render", timerPrefix);
    render.accept(g);
    timer.stop("%s-render", timerPrefix);
  }

  public void render(Graphics2D g, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();
    timer.start("%s-createContext", timerPrefix);
    g = (Graphics2D) g.create();
    timer.stop("%s-createContext", timerPrefix);
    try {
      timer.start("%s-doRender", timerPrefix);
      doRender(g, render);
    } finally {
      timer.stop("%s-doRender", timerPrefix);
      timer.start("%s-disposeContext", timerPrefix);
      g.dispose();
      timer.stop("%s-disposeContext", timerPrefix);
    }
  }

  public void bufferedRender(Graphics2D g, Composite blitComposite, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();

    if (tempBufferPool.getWidth() == renderer.getWidth()
        && tempBufferPool.getHeight() == renderer.getHeight()) {
      // This case only holds during regular rendering. Other rendering, such as screenshots, may
      // have different dimensions.
      timer.start("%s-acquireBuffer", timerPrefix);
      try (final var entry = tempBufferPool.acquire()) {
        var buffer = entry.get();
        timer.stop("%s-acquireBuffer", timerPrefix);

        bufferedRender(buffer, g, blitComposite, render);
      }
    } else {
      timer.start("%s-acquireBuffer", timerPrefix);
      var buffer =
          GraphicsEnvironment.getLocalGraphicsEnvironment()
              .getDefaultScreenDevice()
              .getDefaultConfiguration()
              .createCompatibleImage(
                  renderer.getWidth(), renderer.getHeight(), Transparency.TRANSLUCENT);
      timer.stop("%s-acquireBuffer", timerPrefix);

      bufferedRender(buffer, g, blitComposite, render);
    }
  }

  private void bufferedRender(
      BufferedImage buffer, Graphics2D g, Composite blitComposite, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();

    Graphics2D buffG = buffer.createGraphics();
    try {
      buffG.setClip(new Rectangle(0, 0, buffer.getWidth(), buffer.getHeight()));
      doRender(buffG, render);
    } finally {
      buffG.dispose();
    }

    timer.start("%s-blit", timerPrefix);
    g = (Graphics2D) g.create();
    try {
      g.setComposite(blitComposite);
      g.drawImage(buffer, 0, 0, renderer);
    } finally {
      g.dispose();
    }
    timer.stop("%s-blit", timerPrefix);
  }
}
