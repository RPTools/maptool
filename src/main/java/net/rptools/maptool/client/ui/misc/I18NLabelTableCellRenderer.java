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
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import net.rptools.maptool.util.I18nName;

/**
 * Class that implements a TreeCell renderer. If the object being rendered implements {@link
 * I18nName} then the i18n name is used, otherwise <code>toString()</code> is used for the label.
 */
public class I18NLabelTableCellRenderer implements TableCellRenderer {

  /** Enum that defines the horizontal alignment of the label. */
  public enum HorizontalAlignment {
    LEFT(JLabel.LEFT),
    CENTER(JLabel.CENTER),
    RIGHT(JLabel.RIGHT);

    private final int alignment;

    HorizontalAlignment(int align) {
      this.alignment = align;
    }

    public int getAlignment() {
      return alignment;
    }
  }

  /** The alignment of the label. */
  private HorizontalAlignment alignment = HorizontalAlignment.LEFT;

  /** Creates a new instance of the renderer for the given object. */
  public I18NLabelTableCellRenderer() {}

  /**
   * Creates a new instance of the renderer for the given object.
   *
   * @param alignment The alignment of the label.
   */
  public I18NLabelTableCellRenderer(HorizontalAlignment alignment) {
    this.alignment = alignment;
  }

  @Override
  public Component getTableCellRendererComponent(
      JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    var label = new JLabel();
    label.setOpaque(true);
    if (isSelected) {
      label.setBackground(table.getSelectionBackground());
      label.setForeground(table.getSelectionForeground());
    } else {
      label.setBackground(table.getBackground());
      label.setForeground(table.getForeground());
    }
    if (value != null) {
      if (value instanceof I18nName i18name) {
        label.setText(i18name.i18nName());
      } else {
        label.setText(value.toString());
      }
    }
    label.setHorizontalAlignment(alignment.getAlignment());
    return label;
  }
}
