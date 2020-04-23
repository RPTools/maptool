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
package net.rptools.lib.swing;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * Combo box showing the available pen widths and a preview of each.
 *
 * @author Jay
 * @version $Revision: 1307 $ $Date: 2005-10-18 19:51:22 -0500 (Tue, 18 Oct 2005)$
 */
@SuppressWarnings("serial")
public class PenWidthChooser extends JComboBox {

  /** The renderer for this chooser. */
  private PenListRenderer renderer = new PenListRenderer();

  /** Supported Pen Widths */
  public static final int[] WIDTHS = {1, 3, 5, 7, 11, 15, 21};

  /** The width that the Icon is painted. */
  public static final int ICON_WIDTH = 25;

  /** The maximum number of eleemnts in the list before it scrolls */
  public static final int MAX_ROW_COUNT = 10;

  /** Create the renderer and model for the combo box */
  public PenWidthChooser(int defaultThickness) {
    setRenderer(renderer);
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    int selected = -1;
    for (int i = 0; i < WIDTHS.length; i += 1) {
      model.addElement(WIDTHS[i]);
      renderer.icon.strokes.put(
          WIDTHS[i], new BasicStroke(WIDTHS[i], BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
      if (WIDTHS[i] == defaultThickness) selected = i;
    } // endfor

    // Set up the component
    setModel(model);
    setMaximumSize(getPreferredSize());
    setMaximumRowCount(MAX_ROW_COUNT);
    setSelectedIndex(Math.max(selected, WIDTHS[0]));
  }

  /**
   * Renderer for the items in the combo box
   *
   * @author jgorrell
   * @version $Revision: 1307 $ $Date: 2005-10-18 19:51:22 -0500 (Tue, 18 Oct 2005) $ $Author:
   *     tcroft $
   */
  private class PenListRenderer extends DefaultListCellRenderer {

    /** Icon used to draw the line sample */
    PenIcon icon = new PenIcon();

    /**
     * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     *     java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(
        JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

      // Sets the text, clears the icon
      super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      // Set the icon
      icon.width = (Integer) value;
      setIcon(icon);
      return this;
    }
  }

  /**
   * Icon for the renderer
   *
   * @author jgorrell
   * @version $Revision: 1307 $ $Date: 2005-10-18 19:51:22 -0500 (Tue, 18 Oct 2005) $ $Author:
   *     tcroft $
   */
  private class PenIcon implements Icon {

    /** Pen used in drawing. */
    private int width = 0;

    /** Strokes for this icon */
    private Map<Integer, Stroke> strokes = new HashMap<Integer, Stroke>();

    /** @see javax.swing.Icon#getIconHeight() */
    public int getIconHeight() {
      return getHeight();
    }

    /** @see javax.swing.Icon#getIconWidth() */
    public int getIconWidth() {
      return ICON_WIDTH;
    }

    /** @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int) */
    public void paintIcon(Component c, Graphics g, int x, int y) {

      // Fill the background
      Graphics2D g2d = (Graphics2D) g;
      g2d.setColor(c.getBackground());
      g2d.fillRect(x, y, getIconWidth(), getIconHeight());

      // Draw a line centered in the foreground
      g2d.setColor(c.getForeground());
      g2d.setStroke(strokes.get(width));
      int yCentered = y + getIconHeight() / 2;
      g2d.drawLine(x, yCentered, x + getIconWidth(), yCentered);
    }
  }
}
