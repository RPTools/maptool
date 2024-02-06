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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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

  private void doRender(Graphics2D g, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();

    Dimension size = renderer.getSize();
    Scale scale = renderer.getZoneScale();
    g.setClip(new Area(new Rectangle(0, 0, size.width, size.height)));
    SwingUtil.useAntiAliasing(g);

    AffineTransform af = new AffineTransform();
    af.translate(scale.getOffsetX(), scale.getOffsetY());
    af.scale(scale.getScale(), scale.getScale());
    g.setTransform(af);

    timer.start("bufferRender-render");
    render.accept(g);
    timer.stop("bufferRender-render");
  }

  public void render(Graphics2D g, Consumer<Graphics2D> render) {
    g = (Graphics2D) g.create();
    try {
      doRender(g, render);
    } finally {
      g.dispose();
    }
  }

  public void bufferedRender(Graphics2D g, Composite blitComposite, Consumer<Graphics2D> render) {
    var timer = CodeTimer.get();
    g = (Graphics2D) g.create();
    timer.start("bufferRender-acquireBuffer");
    try (final var entry = tempBufferPool.acquire()) {
      final var buffer = entry.get();
      timer.stop("bufferRender-acquireBuffer");

      Graphics2D buffG = buffer.createGraphics();
      try {
        doRender(buffG, render);
      } finally {
        buffG.dispose();
      }

      timer.start("bufferRender-blit");
      g.setComposite(blitComposite);
      g.drawImage(buffer, 0, 0, renderer);
      timer.stop("bufferRender-blit");
    } finally {
      g.dispose();
    }
  }
}
