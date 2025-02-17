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

import com.formdev.flatlaf.ui.FlatUIUtils;
import com.formdev.flatlaf.util.UIScale;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import javax.accessibility.AccessibleContext;
import javax.swing.*;

public class GenericDialog extends JDialog {
  private static final long serialVersionUID = 6739665491287916519L;
  private final JPanel panel;
  private boolean hasPositionedItself;

  public GenericDialog(String title, Frame parent, JPanel panel) {
    this(title, parent, panel, true);
  }

  public GenericDialog(String title, Frame parent, JPanel panel, boolean modal) {
    super(parent, title, modal);
    setResizable(true);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    this.panel = panel;
    setLayout(new GridLayout());
    JScrollPane scrollPane = new JScrollPane(this.panel);
    add(scrollPane);

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            closeDialog();
          }
        });
    addComponentListener(
        new ComponentAdapter() {
          private void placeButtons() {
            if (getSize().width == 0) {
              return;
            }
            if (maximiseBtn == null || restoreBtn == null) {
              addResizeButtons();
            }
            positionResizeButtons();
          }

          @Override
          public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            placeButtons();
          }

          @Override
          public void componentShown(ComponentEvent e) {
            super.componentShown(e);
            dBounds = getBounds();
            placeButtons();
          }
        });

    // ESCAPE cancels the window without committing
    this.panel
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    this.panel
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

  // Resize Button code
  JButton maximiseBtn, restoreBtn;
  JLayeredPane layeredPane = getLayeredPane();
  Dimension buttonSize = FlatUIUtils.getSubUIDimension("TitlePane.buttonSize", null);
  Window w = SwingUtilities.getWindowAncestor(SwingUtilities.getRoot(layeredPane));
  String defaultWindowStyle =
      (w != null && w.getType() == Window.Type.UTILITY) ? WINDOW_STYLE_SMALL : null;
  String windowStyle =
      clientProperty(layeredPane.getRootPane(), WINDOW_STYLE, defaultWindowStyle, String.class);
  int buttonMinimumWidth = FlatUIUtils.getSubUIInt("TitlePane.buttonMinimumWidth", windowStyle, 30);
  Rectangle dBounds = new Rectangle();
  Rectangle screenBounds =
      GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
  ActionListener resizeListener =
      e -> {
        if (e.getActionCommand().equals("maximise")) {
          setBounds(screenBounds);
          swapResizeButtons();
        } else if (e.getActionCommand().equals("restore")) {
          setBounds(dBounds);
          swapResizeButtons();
        }
      };

  private void addResizeButtons() {
    maximiseBtn = createButtons("TitlePane.maximizeIcon", "maximise");
    restoreBtn = createButtons("TitlePane.restoreIcon", "restore");
    layeredPane.add(maximiseBtn, Integer.valueOf(900), 1);
  }

  protected JButton createButtons(String iconKey, String accessibleName) {
    JButton button =
        new JButton(FlatUIUtils.getSubUIIcon(iconKey, windowStyle)) {
          @Override
          public Dimension getMinimumSize() {
            // allow the button to shrink if space is rare
            return new Dimension(UIScale.scale(buttonMinimumWidth), super.getMinimumSize().height);
          }
        };
    button.setSize(UIScale.scale(buttonSize.width), UIScale.scale(buttonSize.height));
    button.setFocusable(false);
    button.setContentAreaFilled(false);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, accessibleName);
    button.setActionCommand(accessibleName);
    button.addActionListener(resizeListener);
    return button;
  }

  private void swapResizeButtons() {
    if (Arrays.stream(layeredPane.getComponents()).toList().contains(maximiseBtn)) {
      layeredPane.remove(maximiseBtn);
      layeredPane.add(restoreBtn, Integer.valueOf(900), 1);
    } else {
      layeredPane.remove(restoreBtn);
      layeredPane.add(maximiseBtn, Integer.valueOf(900), 1);
    }
    positionResizeButtons();
    Rectangle currentBounds = layeredPane.getBounds();
    RepaintManager.currentManager(layeredPane.getRootPane())
        .addDirtyRegion(
            this, currentBounds.x, currentBounds.y, currentBounds.width, buttonSize.height + 2);
  }

  private void positionResizeButtons() {
    int x = layeredPane.getBounds().width - maximiseBtn.getWidth() * 2;
    int y = 0;
    maximiseBtn.setLocation(x, y);
    restoreBtn.setLocation(x, y);
  }
}
