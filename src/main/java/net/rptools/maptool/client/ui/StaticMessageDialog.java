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
package net.rptools.maptool.client.ui;

public class StaticMessageDialog extends MessageDialog {
  private static final long serialVersionUID = 3101164410637883204L;

  private String status;

  public StaticMessageDialog(String status) {
    this.status = status;
  }

  @Override
  protected String getStatus() {
    return status;
  }

  /**
   * Doesn't work right as it forces a repaint of the GlassPane object which takes a snapshot of the
   * RootPane and then adds the 'status' message as an overlay. The problem is that the RootPane
   * snapshot includes the previous image that might have been displayed previously.
   *
   * @param s
   */
  public void setStatus(String s) {
    this.status = s;
    revalidate();
    repaint();
  }
}
