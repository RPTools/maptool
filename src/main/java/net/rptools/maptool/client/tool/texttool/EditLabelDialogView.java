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
package net.rptools.maptool.client.tool.texttool;

import java.awt.*;
import javax.swing.*;
import net.rptools.maptool.client.swing.ColorWell;

/**
 * The EditLabelDialogView class represents a dialog view for editing a label. It contains
 * components for specifying label properties such as border color, border width, and border arc.
 * The dialog view can be accessed by calling the getRootComponent() method, which returns the root
 * component of the view.
 */
public class EditLabelDialogView {
  /** The main panel for the dialog. */
  private JPanel mainPanel;

  /** Checkbox to determine whether to show the border. */
  private JCheckBox showBorderCheckBox;

  private ColorWell borderColor;

  /** Spinner for specifying the border width. */
  private JSpinner borderWidth;

  /** Spinner for specifying the border arc. */
  private JSpinner borderArc;

  /**
   * The EditLabelDialogView class represents a dialog view for editing a label. It contains
   * components for specifying label properties such as border color, border width, and border arc.
   * The dialog view can be accessed by calling the getRootComponent() method, which returns the
   * root component of the view.
   */
  public EditLabelDialogView() {
    showBorderCheckBox.addActionListener(
        e -> {
          if (showBorderCheckBox.isSelected()) {
            borderColor.setVisible(true); // disabling a ColorWell does nothing.
            borderWidth.setEnabled(true);
            borderArc.setEnabled(true);
          } else {
            borderColor.setVisible(false); // disabling a ColorWell does nothing.
            borderWidth.setEnabled(false);
            borderArc.setEnabled(false);
          }
        });
  }

  /**
   * Retrieves the root component of the EditLabelDialogView class.
   *
   * @return The root component of the EditLabelDialogView class.
   */
  public JComponent getRootComponent() {
    return mainPanel;
  }
}
