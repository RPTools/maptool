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

import com.formdev.flatlaf.FlatIconColors;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.UIManager;
import net.rptools.maptool.client.MapTool;

/**
 * This class is a MouseAdapter that will open a browser to the URL specified in the JLabel's text
 */
public class JLabelHyperLinkListener extends MouseAdapter {
  /** The JLabel that this listener is attached to */
  private final JLabel linkLabel;

  /**
   * Constructor that takes the JLabel that this listener is attached to as a parameter
   *
   * @param jlabel The JLabel that this listener is attached to
   */
  public JLabelHyperLinkListener(JLabel jlabel) {
    linkLabel = jlabel;
    linkLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    linkLabel.setForeground(UIManager.getColor(FlatIconColors.ACTIONS_BLUE_DARK.key));
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    MapTool.showDocument(linkLabel.getText());
  }
}
