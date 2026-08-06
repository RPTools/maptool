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
package net.rptools.maptool.tools;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import net.rptools.maptool.client.swing.PositionalLayout;
import net.rptools.maptool.client.swing.PositionalPanel;

public class PositionalLayoutDemo {
  public static void main(String[] args) {

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel panel = new PositionalPanel();

    panel.add(createButton("NW"), PositionalLayout.Position.NW);
    panel.add(createButton("N"), PositionalLayout.Position.N);
    panel.add(createButton("NE"), PositionalLayout.Position.NE);
    panel.add(createButton("W"), PositionalLayout.Position.W);
    panel.add(createButton("E"), PositionalLayout.Position.E);
    panel.add(createButton("SW"), PositionalLayout.Position.SW);
    panel.add(createButton("S"), PositionalLayout.Position.S);
    panel.add(createButton("SE"), PositionalLayout.Position.SE);
    panel.add(createButton("CENTER"), PositionalLayout.Position.CENTER);

    frame.setContentPane(panel);

    frame.setSize(200, 200);
    frame.setVisible(true);
  }

  private static JButton createButton(String label) {
    JButton button = new JButton(label);
    button.setSize(button.getMinimumSize());

    return button;
  }
}
