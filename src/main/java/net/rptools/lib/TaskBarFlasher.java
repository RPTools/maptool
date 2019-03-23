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
package net.rptools.lib;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class TaskBarFlasher {

  private static final int FLASH_DELAY = 500;

  private final BufferedImage flashImage;
  private final Image originalImage;
  private final Frame frame;

  private FlashThread flashThread;

  public TaskBarFlasher(Frame frame) {
    this.frame = frame;

    originalImage = frame.getIconImage();
    flashImage =
        new BufferedImage(
            originalImage.getWidth(null), originalImage.getHeight(null), BufferedImage.OPAQUE);
    Graphics g = flashImage.getGraphics();
    g.setColor(Color.blue);
    g.fillRect(0, 0, flashImage.getWidth(), flashImage.getHeight());
    g.drawImage(originalImage, 0, 0, null);
    g.dispose();
  }

  public synchronized void flash() {
    if (flashThread != null) {
      // Already flashing
      return;
    }

    flashThread = new FlashThread();
    flashThread.start();
  }

  private class FlashThread extends Thread {
    @Override
    public void run() {
      while (!frame.isFocused()) {
        try {
          Thread.sleep(FLASH_DELAY);
          frame.setIconImage(flashImage);
          Thread.sleep(FLASH_DELAY);
          frame.setIconImage(originalImage);
        } catch (InterruptedException ie) {
          // Just leave, whatever
          break;
        }
      }
      flashThread = null;
    }
  }
}
