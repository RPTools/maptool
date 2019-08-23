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

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AutoResizeStampDialog extends JDialog {
  private static final Logger log = LogManager.getLogger(AutoResizeStampDialog.class);

  private static final long serialVersionUID = 3443156494702113624L;
  private final JPanel contentPanel = new JPanel();

  private int cellWidthSelected = 0;
  private int cellHeightSelected = 0;
  private int pixelWidthSelected = 0;
  private int pixelHeightSelected = 0;

  /**
   * Create the dialog.
   *
   * @param selectedWidth
   * @param selectedHeight
   * @param stampWidth
   * @param stampHeight
   * @param anchorX
   * @param anchorY
   */
  public AutoResizeStampDialog(
      int selectedWidth,
      int selectedHeight,
      int stampWidth,
      int stampHeight,
      int anchorX,
      int anchorY) {
    final JSpinner spinnerCellWidthSelected = new JSpinner();
    final JSpinner spinnerCellHeightSelected = new JSpinner();
    final JSpinner spinnerPixelWidthSelected = new JSpinner();
    final JSpinner spinnerPixelHeightSelected = new JSpinner();
    final JSpinner spinnerPixelWidthAnchor = new JSpinner();
    final JSpinner spinnerPixelHeightAnchor = new JSpinner();
    final JCheckBox chckbxAdjustHorizontalAnchor =
        new JCheckBox(I18N.getText("dialog.resizeStamp.checkbox.horizontal.anchor"));
    final JCheckBox chckbxAdjustVerticalAnchor =
        new JCheckBox(I18N.getText("dialog.resizeStamp.checkbox.vertical.anchor"));

    setModal(true);
    setResizable(false);
    setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    setAlwaysOnTop(true);
    setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
    setTitle("Automatically Resize Stamp");
    //    setBounds(100, 100, 450, 200);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    getContentPane().add(contentPanel, BorderLayout.CENTER);

    JLabel lblCellWidthSelected =
        new JLabel(I18N.getText("dialog.resizeStamp.label.width.selected"));
    JLabel lblCellHeightSelected =
        new JLabel(I18N.getText("dialog.resizeStamp.label.height.selected"));

    spinnerCellWidthSelected.setModel(new SpinnerNumberModel(1, 1, 100, 1));
    spinnerCellHeightSelected.setModel(new SpinnerNumberModel(1, 1, 100, 1));

    JLabel lblPixelWidthSelected = new JLabel(I18N.getText("dialog.resizeStamp.label.width"));
    JLabel lblPixelHeightSelected = new JLabel(I18N.getText("dialog.resizeStamp.label.height"));

    spinnerPixelWidthSelected.setModel(new SpinnerNumberModel(selectedWidth, 1, 5000, 1));
    spinnerPixelHeightSelected.setModel(new SpinnerNumberModel(selectedHeight, 1, 5000, 1));

    JLabel lblPx = new JLabel(I18N.getText("dialog.resizeStamp.label.px"));
    JLabel lblPx2 = new JLabel(I18N.getText("dialog.resizeStamp.label.px"));

    chckbxAdjustHorizontalAnchor.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            spinnerPixelWidthAnchor.setEnabled(chckbxAdjustHorizontalAnchor.isSelected());
          }
        });
    chckbxAdjustHorizontalAnchor.setSelected(true);

    chckbxAdjustVerticalAnchor.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            spinnerPixelHeightAnchor.setEnabled(chckbxAdjustVerticalAnchor.isSelected());
          }
        });
    chckbxAdjustVerticalAnchor.setSelected(true);

    spinnerPixelWidthAnchor.setModel(new SpinnerNumberModel(anchorX, -500, 500, 1));
    spinnerPixelWidthAnchor.setToolTipText(I18N.getText("dialog.resizeStamp.toolTip"));

    spinnerPixelHeightAnchor.setModel(new SpinnerNumberModel(anchorY, -500, 500, 1));
    spinnerPixelHeightAnchor.setToolTipText(I18N.getText("dialog.resizeStamp.toolTip"));

    JLabel lblPx3 = new JLabel(I18N.getText("dialog.resizeStamp.label.px"));
    JLabel lblPx4 = new JLabel(I18N.getText("dialog.resizeStamp.label.px"));
    JLabel lblStampDimensions =
        new JLabel(I18N.getText("dialog.resizeStamp.label.stampDimensions"));

    JLabel lblX = new JLabel(stampWidth + " x " + stampHeight);
    lblX.setHorizontalAlignment(SwingConstants.CENTER);
    GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
    gl_contentPanel.setHorizontalGroup(
        gl_contentPanel
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                gl_contentPanel
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        gl_contentPanel
                            .createParallelGroup(Alignment.LEADING)
                            .addComponent(lblCellWidthSelected)
                            .addComponent(lblCellHeightSelected)
                            .addComponent(chckbxAdjustHorizontalAnchor)
                            .addComponent(
                                chckbxAdjustVerticalAnchor,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE))
                    .addGap(18)
                    .addGroup(
                        gl_contentPanel
                            .createParallelGroup(Alignment.LEADING)
                            .addGroup(
                                gl_contentPanel
                                    .createSequentialGroup()
                                    .addGroup(
                                        gl_contentPanel
                                            .createParallelGroup(Alignment.LEADING, false)
                                            .addComponent(
                                                spinnerCellWidthSelected,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                            .addComponent(
                                                spinnerCellHeightSelected,
                                                GroupLayout.PREFERRED_SIZE,
                                                42,
                                                Short.MAX_VALUE))
                                    .addGap(18)
                                    .addGroup(
                                        gl_contentPanel
                                            .createParallelGroup(Alignment.LEADING, false)
                                            .addGroup(
                                                gl_contentPanel
                                                    .createSequentialGroup()
                                                    .addComponent(lblPixelWidthSelected)
                                                    .addPreferredGap(
                                                        ComponentPlacement.RELATED,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        Short.MAX_VALUE)
                                                    .addComponent(
                                                        spinnerPixelWidthSelected,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE))
                                            .addGroup(
                                                gl_contentPanel
                                                    .createSequentialGroup()
                                                    .addComponent(lblPixelHeightSelected)
                                                    .addGap(18)
                                                    .addComponent(
                                                        spinnerPixelHeightSelected,
                                                        GroupLayout.PREFERRED_SIZE,
                                                        GroupLayout.DEFAULT_SIZE,
                                                        GroupLayout.PREFERRED_SIZE)))
                                    .addGap(4)
                                    .addGroup(
                                        gl_contentPanel
                                            .createParallelGroup(Alignment.LEADING)
                                            .addComponent(lblPx)
                                            .addComponent(
                                                lblPx2,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                24)))
                            .addGroup(
                                gl_contentPanel
                                    .createParallelGroup(Alignment.LEADING)
                                    .addGroup(
                                        gl_contentPanel
                                            .createSequentialGroup()
                                            .addComponent(
                                                spinnerPixelWidthAnchor,
                                                GroupLayout.PREFERRED_SIZE,
                                                84,
                                                GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addComponent(
                                                lblPx3,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                24))
                                    .addGroup(
                                        gl_contentPanel
                                            .createSequentialGroup()
                                            .addComponent(
                                                spinnerPixelHeightAnchor,
                                                GroupLayout.PREFERRED_SIZE,
                                                84,
                                                GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(ComponentPlacement.RELATED)
                                            .addComponent(
                                                lblPx4,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                24))))
                    .addContainerGap(69, Short.MAX_VALUE))
            .addGroup(
                Alignment.TRAILING,
                gl_contentPanel
                    .createSequentialGroup()
                    .addContainerGap(338, Short.MAX_VALUE)
                    .addGroup(
                        gl_contentPanel
                            .createParallelGroup(Alignment.LEADING, false)
                            .addComponent(
                                lblX, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblStampDimensions))
                    .addContainerGap()));
    gl_contentPanel.setVerticalGroup(
        gl_contentPanel
            .createParallelGroup(Alignment.LEADING)
            .addGroup(
                gl_contentPanel
                    .createSequentialGroup()
                    .addContainerGap()
                    .addGroup(
                        gl_contentPanel
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(lblCellWidthSelected)
                            .addComponent(
                                spinnerCellWidthSelected,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPixelWidthSelected)
                            .addComponent(
                                spinnerPixelWidthSelected,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPx))
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addGroup(
                        gl_contentPanel
                            .createParallelGroup(Alignment.BASELINE)
                            .addComponent(lblCellHeightSelected)
                            .addComponent(
                                spinnerCellHeightSelected,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPixelHeightSelected)
                            .addComponent(
                                spinnerPixelHeightSelected,
                                GroupLayout.PREFERRED_SIZE,
                                GroupLayout.DEFAULT_SIZE,
                                GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblPx2))
                    .addGroup(
                        gl_contentPanel
                            .createParallelGroup(Alignment.LEADING)
                            .addGroup(
                                gl_contentPanel
                                    .createSequentialGroup()
                                    .addGap(18)
                                    .addGroup(
                                        gl_contentPanel
                                            .createParallelGroup(Alignment.BASELINE)
                                            .addComponent(chckbxAdjustHorizontalAnchor)
                                            .addComponent(
                                                spinnerPixelWidthAnchor,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblPx3))
                                    .addPreferredGap(ComponentPlacement.UNRELATED)
                                    .addGroup(
                                        gl_contentPanel
                                            .createParallelGroup(Alignment.BASELINE)
                                            .addComponent(chckbxAdjustVerticalAnchor)
                                            .addComponent(
                                                spinnerPixelHeightAnchor,
                                                GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                            .addComponent(lblPx4)))
                            .addGroup(
                                gl_contentPanel
                                    .createSequentialGroup()
                                    .addGap(27)
                                    .addComponent(lblStampDimensions)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(
                                        lblX,
                                        GroupLayout.DEFAULT_SIZE,
                                        GroupLayout.DEFAULT_SIZE,
                                        Short.MAX_VALUE)
                                    .addGap(11)))
                    .addGap(19)));
    contentPanel.setLayout(gl_contentPanel);
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton okButton = new JButton(I18N.getText("msg.title.messageDialog.ok"));
        okButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                cellWidthSelected =
                    Integer.parseInt(spinnerCellWidthSelected.getValue().toString());
                cellHeightSelected =
                    Integer.parseInt(spinnerCellHeightSelected.getValue().toString());
                pixelWidthSelected =
                    Integer.parseInt(spinnerPixelWidthSelected.getValue().toString());
                pixelHeightSelected =
                    Integer.parseInt(spinnerPixelHeightSelected.getValue().toString());

                setVisible(false);
                dispose();
              }
            });
        okButton.setActionCommand(I18N.getText("msg.title.messageDialog.ok"));
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
      }
      {
        JButton cancelButton = new JButton(I18N.getText("msg.title.messageDialog.cancel"));
        cancelButton.addActionListener(
            new ActionListener() {
              public void actionPerformed(ActionEvent arg0) {
                setVisible(false);
                dispose();
              }
            });
        cancelButton.setActionCommand(I18N.getText("msg.title.messageDialog.cancel"));
        buttonPane.add(cancelButton);
      }
    }
    pack();
  }

  public int getCellWidthSelected() {
    return cellWidthSelected;
  }

  public int getCellHeightSelected() {
    return cellHeightSelected;
  }

  public int getPixelWidthSelected() {
    return pixelWidthSelected;
  }

  public void setPixelWidthSelected(int pixelWidthSelected) {
    this.pixelWidthSelected = pixelWidthSelected;
  }

  public int getPixelHeightSelected() {
    return pixelHeightSelected;
  }

  public void setPixelHeightSelected(int pixelHeightSelected) {
    this.pixelHeightSelected = pixelHeightSelected;
  }
}
