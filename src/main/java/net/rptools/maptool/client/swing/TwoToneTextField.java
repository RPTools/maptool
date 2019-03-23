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
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.Document;

/**
 * A text field that supports two tone text.
 *
 * @author jgorrell
 * @version $Revision: 5945 $ $Date: 2013-06-03 04:35:50 +0930 (Mon, 03 Jun 2013) $ $Author:
 *     azhrei_fje $
 */
public class TwoToneTextField extends JTextField {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** The second color drawn behind the foreground color */
  private Color twoToneColor = Color.DARK_GRAY;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** The ui class id. */
  private static final String UI_CLASS_ID = "TwoToneTextFieldUI";

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /** Set the UI for the field. */
  static {
    UIManager.put(UI_CLASS_ID, TwoToneTextFieldUI.class.getName());
  }

  /** Default constructor */
  public TwoToneTextField() {
    super();
  }

  /**
   * Create the field with the passed text.
   *
   * @param aText The text for the new field.
   */
  public TwoToneTextField(String aText) {
    super(aText);
  }

  /**
   * Create the field with a set number of columns
   *
   * @param aColumns The number of columns for the new field.
   */
  public TwoToneTextField(int aColumns) {
    super(aColumns);
  }

  /**
   * Create a field with the passed text and number of columns
   *
   * @param aText The text for the new field.
   * @param aColumns The number of columns for the new field.
   */
  public TwoToneTextField(String aText, int aColumns) {
    super(aText, aColumns);
  }

  /**
   * Create a field with the given document, text, and column count.
   *
   * @param aDoc The document for the new field.
   * @param aText The text for the new field.
   * @param aColumns The number of columns for the new field.
   */
  public TwoToneTextField(Document aDoc, String aText, int aColumns) {
    super(aDoc, aText, aColumns);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * Get the twoToneColor for this TwoToneTextField.
   *
   * @return Returns the current value of twoToneColor.
   */
  public Color getTwoToneColor() {
    return twoToneColor;
  }

  /**
   * Set the value of twoToneColor for this TwoToneTextField.
   *
   * @param aTwoToneColor The twoToneColor to set.
   */
  public void setTwoToneColor(Color aTwoToneColor) {
    twoToneColor = aTwoToneColor;
  }

  /**
   * Gets the class ID for a UI.
   *
   * @return the string "TwoToneTextFieldUI"
   * @see JComponent#getUIClassID
   * @see UIDefaults#getUI
   */
  public String getUIClassID() {
    return UI_CLASS_ID;
  }
}
