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
package net.rptools.maptool.client.ui.drawpanel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.Drawable;
import net.rptools.maptool.model.drawing.DrawableColorPaint;
import net.rptools.maptool.model.drawing.DrawablesGroup;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool.model.drawing.Pen;

public class DrawablesPanel extends JComponent {
  private static final long serialVersionUID = 441600187734634440L;
  private static final int MAX_PANEL_SIZE = 250;
  private final List<GUID> selectedIDList = new ArrayList<GUID>();

  public List<Object> getSelectedIds() {
    List<Object> list = new ArrayList<Object>(selectedIDList);
    return list;
  }

  public void setSelectedIds(List<GUID> ids) {
    this.selectedIDList.clear();
    this.selectedIDList.addAll(ids);
    repaint();
  }

  public void addSelectedId(GUID id) {
    this.selectedIDList.add(id);
    repaint();
  }

  public void clearSelectedIds() {
    this.selectedIDList.clear();
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g) {
    if (selectedIDList.size() > 0) {
      if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
        Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
        if (zone != null) {
          List<DrawnElement> drawableList = new ArrayList<DrawnElement>();
          boolean onlyCuts = true;
          for (GUID id : selectedIDList) {
            DrawnElement de = zone.getDrawnElement(id);
            if (de != null) {
              drawableList.add(de);
              if (!de.getPen().isEraser()) onlyCuts = false;
            }
          }
          if (drawableList.size() > 0) {
            Collections.reverse(drawableList);
            Rectangle bounds = getBounds(zone, drawableList);
            double scale =
                (double) Math.min(MAX_PANEL_SIZE, getSize().width) / (double) bounds.width;
            if ((bounds.height * scale) > MAX_PANEL_SIZE)
              scale = (double) Math.min(MAX_PANEL_SIZE, getSize().height) / (double) bounds.height;
            g.drawImage(drawDrawables(zone, drawableList, bounds, scale, onlyCuts), 0, 0, null);
          }
        }
      }
    }
  }

  private BufferedImage drawDrawables(
      Zone zone,
      List<DrawnElement> drawableList,
      Rectangle viewport,
      double scale,
      boolean showEraser) {
    BufferedImage backBuffer =
        new BufferedImage(
            (int) (viewport.width * scale),
            (int) (viewport.height * scale),
            Transparency.TRANSLUCENT);
    Graphics2D g = backBuffer.createGraphics();
    g.setClip(0, 0, backBuffer.getWidth(), backBuffer.getHeight());
    Composite oldComposite = g.getComposite();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    AffineTransform tf = new AffineTransform();
    tf.translate(-(viewport.x * scale), -(viewport.y * scale));
    tf.scale(scale, scale);
    g.transform(tf);
    for (DrawnElement element : drawableList) {
      Drawable drawable = element.getDrawable();
      Pen pen = element.getPen();
      if (pen.getOpacity() != 1
          && pen.getOpacity()
              != 0 /* handle legacy pens, besides, it doesn't make sense to have a non visible pen */) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pen.getOpacity()));
      }
      // If we are only drawing cuts, make the pen visible
      if (showEraser && pen.isEraser()) {
        pen = new Pen(pen);
        pen.setEraser(false);
        pen.setPaint(new DrawableColorPaint(Color.red));
        pen.setBackgroundPaint(new DrawableColorPaint(Color.red));
      }
      if (drawable instanceof DrawablesGroup) {
        g.drawImage(
            drawDrawables(
                zone,
                ((DrawablesGroup) drawable).getDrawableList(),
                new Rectangle(viewport),
                1,
                false),
            viewport.x,
            viewport.y,
            null);
      } else {
        drawable.draw(zone, g, pen);
      }
      g.setComposite(oldComposite);
    }
    g.dispose();
    return backBuffer;
  }

  private Rectangle getBounds(Zone zone, List<DrawnElement> drawableList) {
    Rectangle bounds = null;
    for (DrawnElement element : drawableList) {
      // Empty drawables are created by right clicking during the draw process
      // and need to be skipped.
      Rectangle drawnBounds = element.getDrawable().getBounds(zone);
      if (drawnBounds == null) {
        continue;
      }
      drawnBounds = new Rectangle(drawnBounds);
      // Handle pen size
      Pen pen = element.getPen();
      int penSize = pen.getForegroundMode() == Pen.MODE_TRANSPARENT ? 0 : (int) pen.getThickness();
      drawnBounds.setRect(
          drawnBounds.getX() - penSize,
          drawnBounds.getY() - penSize,
          drawnBounds.getWidth() + (penSize * 2),
          drawnBounds.getHeight() + (penSize * 2));
      if (bounds == null) bounds = drawnBounds;
      else bounds.add(drawnBounds);
    }
    // Fix for Sentry MAPTOOL-20
    if (bounds != null && bounds.getWidth() > 0 && bounds.getHeight() > 0) {
      return bounds;
    }
    return new Rectangle(0, 0, -1, -1);
  }
}
