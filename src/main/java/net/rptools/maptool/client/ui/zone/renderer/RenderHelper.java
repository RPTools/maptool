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
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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
  private final ZoneRenderer renderer;
  private final BufferedImagePool tempBufferPool;

  public RenderHelper(ZoneRenderer renderer, BufferedImagePool tempBufferPool) {
    this.renderer = renderer;
    this.tempBufferPool = tempBufferPool;
  }

  public ImageObserver getImageObserver() {
    return renderer;
  }

  private void doRender(Graphics2D g, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();

    timer.start("RenderHelper-useAA");
    SwingUtil.useAntiAliasing(g);
    timer.stop("RenderHelper-useAA");

    timer.start("RenderHelper-setTransform");
    Scale scale = renderer.getZoneScale();
    AffineTransform af = new AffineTransform();
    af.translate(scale.getOffsetX(), scale.getOffsetY());
    af.scale(scale.getScale(), scale.getScale());
    g.setTransform(af);
    timer.stop("RenderHelper-setTransform");

    timer.start("RenderHelper-render");
    render.accept(g);
    timer.stop("RenderHelper-render");
  }

  public void render(Graphics2D g, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();
    timer.start("RenderHelper-createContext");
    g = (Graphics2D) g.create();
    timer.stop("RenderHelper-createContext");
    try {
      timer.start("RenderHelper-doRender");
      doRender(g, render);
    } finally {
      timer.stop("RenderHelper-doRender");
      timer.start("RenderHelper-disposeContext");
      g.dispose();
      timer.stop("RenderHelper-disposeContext");
    }
  }

  public void bufferedRender(Graphics2D g, Composite blitComposite, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();
    g = (Graphics2D) g.create();
    timer.start("RenderHelper-acquireBuffer");
    try (final var entry = tempBufferPool.acquire()) {
      final var buffer = entry.get();
      timer.stop("RenderHelper-acquireBuffer");

      Graphics2D buffG = buffer.createGraphics();
      try {
        buffG.setClip(new Area(new Rectangle(0, 0, buffer.getWidth(), buffer.getHeight())));
        doRender(buffG, render);
      } finally {
        buffG.dispose();
      }

      timer.start("RenderHelper-blit");
      g.setComposite(blitComposite);
      g.drawImage(buffer, 0, 0, renderer);
      timer.stop("RenderHelper-blit");
    } finally {
      g.dispose();
    }
  }
}
