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
package net.rptools.maptool.client.swing.table;

import com.formdev.flatlaf.FlatIconColors;
import com.formdev.flatlaf.util.ColorFunctions;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class MultiLineTableHeaderRenderer implements TableCellRenderer {
  private static final Color HEADER_BACKGROUND =
      UIManager.getDefaults().getColor("TableHeader.background");
  private static final Color HEADER_FOREGROUND =
      UIManager.getDefaults().getColor("TableHeader.foreground");
  private static final Color HEADER_ALTERNATE_BACKGROUND;
  private static final Color HEADER_ALTERNATE_FOREGROUND;

  static {
    // this is so tests can be passed as they do not install the LaF
    Color c1, c2;
    try {
      Color mixWith = UIManager.getColor(FlatIconColors.OBJECTS_BLACK_TEXT.key);
      c1 = ColorFunctions.mix(HEADER_BACKGROUND, mixWith, 0.94f);
      c2 = ColorFunctions.mix(HEADER_FOREGROUND, mixWith, 0.31f);
    } catch (Exception e) {
      c1 = Color.WHITE;
      c2 = Color.BLACK;
    }
    HEADER_ALTERNATE_BACKGROUND = c1;
    HEADER_ALTERNATE_FOREGROUND = c2;
  }

  public MultiLineTableHeaderRenderer() {}

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JPanel panel = new JPanel();
    Color foreground = column % 2 == 0 ? HEADER_ALTERNATE_FOREGROUND : HEADER_FOREGROUND;
    Color background = column % 2 == 0 ? HEADER_ALTERNATE_BACKGROUND : HEADER_BACKGROUND;
    panel.setBackground(background);
    panel.setForeground(foreground);

    LookAndFeel.installBorder(panel, "TableHeader.cellBorder");

    BoxLayout box = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
    panel.setLayout(box);

    String[] heading = ((String) value).split(" ");
    for (String word : heading) {
      JLabel label = new JLabel(word, null, SwingConstants.CENTER);
      label.setBackground(background);
      label.setForeground(foreground);
      label.setOpaque(true);
      label.setAlignmentX(0.5f);
      panel.add(label);
    }
    panel.invalidate();

    return panel;
  }
}
