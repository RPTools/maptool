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
package net.rptools.maptool.client.ui.misc;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.LookAndFeel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Class that implements a multi line header renderer for tables. The header text is wrapped and
 * centered.
 */
public class MultiLineTableHeaderRenderer extends JTextPane implements TableCellRenderer {

  /** Creates a new instance of the renderer. */
  public MultiLineTableHeaderRenderer() {
    setOpaque(false);
    var doc = getStyledDocument();
    var center = new SimpleAttributeSet();
    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
    doc.setParagraphAttributes(0, doc.getLength(), center, false);
    setEditable(false);
    setFocusable(false);
    LookAndFeel.installBorder(this, "TableHeader.cellBorder");
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    int width = table.getColumnModel().getColumn(column).getWidth();
    setText((String) value);
    setSize(width, getPreferredSize().height);
    return this;
  }
}
