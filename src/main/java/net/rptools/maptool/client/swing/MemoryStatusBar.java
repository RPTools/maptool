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
package net.rptools.maptool.client.swing;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.text.DecimalFormat;
import javax.swing.JProgressBar;
import net.rptools.maptool.util.FileUtil;

/** */
public class MemoryStatusBar extends JProgressBar {
  private static final long serialVersionUID = 1L;

  private static Dimension minSize;
  private static final DecimalFormat format = new DecimalFormat("#,##0.#");
  private static long largestMemoryUsed = -1;
  private static MemoryStatusBar msb = null;

  private static FontMetrics fm = null;

  public static MemoryStatusBar getInstance() {
    if (msb == null) msb = new MemoryStatusBar();
    return msb;
  }

  private MemoryStatusBar() {
    setMinimum(0);
    setStringPainted(true);

    // Adjust the minimum size to be big enought for the font used
    // plus a bit extra padding.
    fm = getFontMetrics(getFont());
    int w = 26 + fm.stringWidth("9.99 MB/9.99 MB");
    int h = 4 + fm.getHeight();
    minSize = new Dimension(w, h);

    new Thread() {
      @Override
      public void run() {
        while (true) {
          update();
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ie) {
            break;
          }
        }
      }
    }.start();
    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(java.awt.event.MouseEvent e) {
            System.gc();
            update();
          }
        });
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getMinimumSize()
   */
  @Override
  public Dimension getMinimumSize() {
    return minSize;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getPreferredSize()
   */
  @Override
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  /**
   * Returns the largest memory amount that has been used during running of the program.
   *
   * @return Memory size as a long
   */
  public long getLargestMemoryUsed() {
    return largestMemoryUsed;
  }

  private void update() {
    long totalMemory = Runtime.getRuntime().totalMemory();
    long freeMemory = Runtime.getRuntime().freeMemory();
    long maxMemory = Runtime.getRuntime().maxMemory();

    if (totalMemory > largestMemoryUsed) largestMemoryUsed = totalMemory;

    setMaximum((int) (totalMemory / (1024 * 1024)));
    setValue((int) ((totalMemory - freeMemory) / (1024 * 1024)));

    setString(
        FileUtil.byteCountToDisplaySize((totalMemory - freeMemory))
            + "/"
            + FileUtil.byteCountToDisplaySize(totalMemory));

    setToolTipText(
        "Used Memory: "
            + format.format((totalMemory - freeMemory) / (1024 * 1024))
            + "M, Total Memory: "
            + format.format(totalMemory / (1024 * 1024))
            + "M, Maximum Memory: "
            + format.format(maxMemory / (1024 * 1024))
            + "M");
  }
}
