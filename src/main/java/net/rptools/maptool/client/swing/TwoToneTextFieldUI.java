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

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.FieldView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.text.View;

/**
 * Special UI to override the method that creates the view.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class TwoToneTextFieldUI extends BasicTextFieldUI {

  /** @see javax.swing.plaf.basic.BasicTextFieldUI#create(javax.swing.text.Element) */
  public View create(Element aElem) {
    return new TwoToneTextFieldView(aElem);
  }

  /**
   * Creates a UI for a TwoToneTextField.
   *
   * @param c the text field
   * @return the UI
   */
  public static ComponentUI createUI(JComponent c) {
    return new TwoToneTextFieldUI();
  }

  /*---------------------------------------------------------------------------------------------
   * TwoToneTextFieldView Inner Class
   *-------------------------------------------------------------------------------------------*/

  /**
   * Extension of {@linkplain javax.swing.text.FieldView} to allow for two tone text painting.
   *
   * @author jgorrell
   * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
   *     azhrei_fje $
   */
  public static class TwoToneTextFieldView extends FieldView {

    /**
     * Create a new TwoToneTextFieldView
     *
     * @param aElem The element this view paints
     */
    public TwoToneTextFieldView(Element aElem) {
      super(aElem);
    }

    /*---------------------------------------------------------------------------------------------
     * Class Variables
     *-------------------------------------------------------------------------------------------*/

    /** The amount that the text is offset horizontally */
    private static final int HORIZONTAL_OFFSET = 1;

    /** The amount that the text is offset vertially */
    private static final int VERTICAL_OFFSET = 0;

    /*---------------------------------------------------------------------------------------------
     * Overridden FieldView Methods
     *-------------------------------------------------------------------------------------------*/

    /** @see javax.swing.text.FieldView#getPreferredSpan(int) */
    public float getPreferredSpan(int axis) {
      // Do not use the HORIZONTAL_OFFSET here as it will interfere with highlighting
      return super.getPreferredSpan(axis) + (axis == View.X_AXIS ? 0 : VERTICAL_OFFSET);
    }

    /** @see javax.swing.text.PlainView#drawUnselectedText(java.awt.Graphics, int, int, int, int) */
    protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1)
        throws BadLocationException {

      // Find the text segment
      Document doc = getDocument();
      Segment s = new Segment();
      doc.getText(p0, p1 - p0, s);

      // Calculate the background highlight, it gets painted first.
      TwoToneTextField host = (TwoToneTextField) getContainer();
      Color bg = host.getTwoToneColor();
      Color fg = g.getColor();
      if (bg == null) { // No color set, guess black or white
        float[] hsb = Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), null);
        bg = hsb[2] > 0.7 ? Color.BLACK : Color.WHITE;
      } // endif
      g.setColor(bg);
      Utilities.drawTabbedText(s, x + HORIZONTAL_OFFSET, y + VERTICAL_OFFSET, g, this, p0);

      // Draw the foreground
      g.setColor(fg);
      return Utilities.drawTabbedText(s, x, y, g, this, p0);
    }
  }
}
