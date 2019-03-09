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
import javax.swing.JProgressBar;

/** */
public class ProgressStatusBar extends JProgressBar {

  private static final Dimension minSize = new Dimension(75, 10);

  int indeterminateCount = 0;
  int determinateCount = 0;
  int totalWork = 0;
  int currentWork = 0;

  public ProgressStatusBar() {
    setMinimum(0);
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getMinimumSize()
   */
  public Dimension getMinimumSize() {
    return minSize;
  }

  /*
   * (non-Javadoc)
   *
   * @see javax.swing.JComponent#getPreferredSize()
   */
  public Dimension getPreferredSize() {
    return getMinimumSize();
  }

  public void startIndeterminate() {
    indeterminateCount++;
    setIndeterminate(true);
  }

  public void endIndeterminate() {
    indeterminateCount--;
    if (indeterminateCount < 1) {
      setIndeterminate(false);

      indeterminateCount = 0;
    }
  }

  public void startDeterminate(int totalWork) {
    determinateCount++;
    this.totalWork += totalWork;

    setMaximum(this.totalWork);
  }

  public void updateDeterminateProgress(int additionalWorkCompleted) {
    currentWork += additionalWorkCompleted;
    setValue(currentWork);
  }

  public void endDeterminate() {
    determinateCount--;
    if (determinateCount == 0) {
      totalWork = 0;
      currentWork = 0;

      setMaximum(0);
      setValue(0);
    }
  }
}
