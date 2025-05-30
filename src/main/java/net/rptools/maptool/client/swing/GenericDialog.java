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

import static com.formdev.flatlaf.FlatClientProperties.*;

import com.formdev.flatlaf.ui.FlatRootPaneUI;
import com.jidesoft.swing.JideBoxLayout;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GenericDialog extends JDialog {
  private static final long serialVersionUID = 6739665491287916519L;
  private boolean hasPositionedItself;

  public GenericDialog(String title, Frame parent, JPanel panel) {
    this(title, parent, panel, true);
  }

  public GenericDialog(String title, Frame parent, JPanel panel, boolean modal) {
    super(parent, title, modal);

    setResizable(true);
    getRootPane()
        .setBorder(
            BorderFactory.createCompoundBorder(
                new FlatRootPaneUI.FlatWindowBorder(),
                BorderFactory.createEmptyBorder(0, 4, 4, 3)));

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    JideBoxLayout layout = new JideBoxLayout(getContentPane(), JideBoxLayout.PAGE_AXIS);
    getContentPane().setLayout(layout);
    JScrollPane scrollPane = new JScrollPane(panel);
    getContentPane().add(scrollPane, JideBoxLayout.FLEXIBLE);

    getRootPane().validate();

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            closeDialog();
          }
        });

    // ESCAPE cancels the window without committing
    panel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    panel
        .getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                closeDialog();
              }
            });
  }

  public void closeDialog() {
    dispose();
  }

  private Dimension getMaxScreenSize() {
    GraphicsConfiguration gc = getOwner().getGraphicsConfiguration();
    Insets insets = getOwner().getToolkit().getScreenInsets(gc);
    Rectangle bounds = gc.getDevice().getDefaultConfiguration().getBounds();
    return new Dimension(
        bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
  }

  @Override
  public Dimension getPreferredSize() {
    Dimension superPref = super.getPreferredSize();
    Dimension screenMax = getMaxScreenSize();
    return new Dimension(
        Math.min(superPref.width, screenMax.width), Math.min(superPref.height, screenMax.height));
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension superMax = super.getMaximumSize();
    Dimension screenMax = getMaxScreenSize();
    return new Dimension(
        Math.min(superMax.width, screenMax.width), Math.min(superMax.height, screenMax.height));
  }

  @Override
  public void setMaximumSize(Dimension maximumSize) {
    Dimension screenMax = getMaxScreenSize();
    super.setMaximumSize(
        new Dimension(
            Math.min(maximumSize.width, screenMax.width),
            Math.min(maximumSize.height, screenMax.height)));
  }

  public void showDialog() {
    // We want to center over our parent, but only the first time.
    // If this dialog is reused, we want it to show up where it was last.
    if (!hasPositionedItself) {
      pack();
      positionInitialView();
      hasPositionedItself = true;
    }
    setVisible(true);
  }

  protected void positionInitialView() {
    SwingUtil.centerOver(this, getOwner());
  }
}
