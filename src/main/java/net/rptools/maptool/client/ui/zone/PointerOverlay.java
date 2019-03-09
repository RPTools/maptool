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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.ScreenPoint;
import net.rptools.maptool.model.Pointer;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.ZonePoint;
import net.rptools.maptool.util.GraphicsUtil;

/**
 * Draws the various "pointer" shapes that users can call up using
 * Spacebar/Ctrl+Spacebar/Shift+Spacebar. The paintOverlay() method is called by
 * ZoneRenderer.renderTokens() and no one else.
 */
public class PointerOverlay implements ZoneOverlay {
  private final List<PointerPair> pointerList = new ArrayList<PointerPair>();
  private static BufferedImage POINTER_IMAGE;
  private static BufferedImage SPEECH_IMAGE;
  private static BufferedImage THOUGHT_IMAGE;
  private static BufferedImage LOOK_HERE_IMAGE;

  static {
    try {
      POINTER_IMAGE = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/arrow.png");
      SPEECH_IMAGE = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/speech.png");
      THOUGHT_IMAGE = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/thought.png");
      LOOK_HERE_IMAGE =
          ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/look_here.png");

    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public void paintOverlay(ZoneRenderer renderer, Graphics2D g) {
    Zone zone = renderer.getZone();

    for (int i = 0; i < pointerList.size(); i++) {
      PointerPair p = pointerList.get(i);
      if (p.pointer.getZoneGUID().equals(zone.getId())) {
        ZonePoint zPoint = new ZonePoint(p.pointer.getX(), p.pointer.getY());
        ScreenPoint sPoint = ScreenPoint.fromZonePointRnd(renderer, zPoint.x, zPoint.y);

        int offX = 0;
        int offY = 0;
        int centX = 0;
        int centY = 0;
        BufferedImage image = null;
        switch (p.pointer.getType()) {
          case ARROW:
            offX = 2;
            offY = -36;
            image = POINTER_IMAGE;
            break;
          case SPEECH_BUBBLE:
            offX = -19;
            offY = -61;
            centX = 36;
            centY = 23;
            image = SPEECH_IMAGE;
            break;
          case THOUGHT_BUBBLE:
            offX = -13;
            offY = -65;
            centX = 36;
            centY = 23;
            image = THOUGHT_IMAGE;
            break;
          case LOOK_HERE:
            offX = 0;
            offY = -52;
            image = LOOK_HERE_IMAGE;
            break;
        }
        g.drawImage(image, (int) sPoint.x + offX, (int) sPoint.y + offY, null);

        switch (p.pointer.getType()) {
          case ARROW:
            GraphicsUtil.drawBoxedString(
                g,
                p.player,
                (int) sPoint.x + POINTER_IMAGE.getWidth() - 10,
                (int) sPoint.y - POINTER_IMAGE.getHeight() + 15,
                SwingUtilities.LEFT);
            break;
          case THOUGHT_BUBBLE:
          case SPEECH_BUBBLE:
            FontMetrics fm = renderer.getFontMetrics(renderer.getFont());
            String name = p.player;
            int len = SwingUtilities.computeStringWidth(fm, name);

            g.setColor(Color.black);
            int x = (int) sPoint.x + centX + offX + 5;
            int y = (int) sPoint.y + offY + centY + fm.getHeight() / 2;
            g.drawString(name, x - len / 2, y);
            break;
          case LOOK_HERE:
            GraphicsUtil.drawBoxedString(
                g,
                p.player,
                (int) sPoint.x + LOOK_HERE_IMAGE.getWidth() - 22,
                (int) sPoint.y + 2,
                SwingUtilities.LEFT);
            break;
          default:
            break;
        }
      }
    }
  }

  public void addPointer(String player, Pointer pointer) {
    pointerList.add(new PointerPair(player, pointer));
  }

  public void removePointer(String player) {
    for (int i = 0; i < pointerList.size(); i++) {
      if (pointerList.get(i).player.equals(player)) {
        pointerList.remove(i);
      }
    }
  }

  public Pointer getPointer(String player) {
    for (int i = 0; i < pointerList.size(); i++) {
      if (pointerList.get(i).player.equals(player)) {
        return pointerList.get(i).pointer;
      }
    }
    return null;
  }

  private class PointerPair {
    Pointer pointer;
    String player;

    PointerPair(String player, Pointer pointer) {
      this.pointer = pointer;
      this.player = player;
    }
  }
}
