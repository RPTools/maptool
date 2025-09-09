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

import com.jidesoft.dialog.*;
import com.jidesoft.swing.*;
import com.jidesoft.utils.PortingUtils;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;

public class GenericDialog extends JDialog {
  public static final String AFFIRM = ButtonPanel.AFFIRMATIVE_BUTTON;
  public static final String DENY = ButtonPanel.CANCEL_BUTTON;
  private Dimension _preferredSize = null;
  private boolean hasBeenShown;
  private String _dialogResult = ButtonPanel.CANCEL_BUTTON;
  protected final Resizable _resizable;
  private final JComponent _contentPane = new JPanel();
  private JComponent _content = new JPanel();
  private final JScrollPane _scrollPane = new JScrollPane();
  private ScrollableButtonPanel _buttonPanel;
  private boolean _usingAbeillePanel = false;
  private ActionListener _onCloseAction;
  private ActionListener _onShowAction;

  public static GenericDialogFactory getFactory() {
    return new GenericDialogFactory();
  }

  /** Whilst this works. You should use the factory method instead. */
  public GenericDialog() {
    super(MapTool.getFrame());
    super.setContentPane(_contentPane);
    initComponents();

    this._resizable =
        new Resizable(getRootPane()) {
          public void resizing(int resizeDir, int newX, int newY, int newW, int newH) {
            Container container = GenericDialog.this.getContentPane();
            PortingUtils.setPreferredSize(container, new Dimension(newW, newH));
            if (GenericDialog.this.isUndecorated()) {
              GenericDialog.this.setBounds(newX, newY, newW, newH);
            }
          }
        };
    this._resizable.setResizeCornerSize(18);
    this._resizable.setResizableCorners(Resizable.LOWER_LEFT | Resizable.LOWER_RIGHT);
    super.setResizable(true);

    this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    this.addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            closeDialog();
          }
        });
  }

  public GenericDialog(String title, JPanel panel) {
    this(title, panel, false);
  }

  public GenericDialog(String title, JComponent panel, boolean modal) {
    this();
    this.setDialogTitle(title);
    this.setModal(modal);
    this.setContent(panel);
  }

  protected void initComponents() {
    JideBoxLayout layout = new JideBoxLayout(this.getContentPane(), JideBoxLayout.PAGE_AXIS);
    this.getContentPane().setLayout(layout);
    this._scrollPane.setViewportView(this.getContent());
    this.getContentPane().add(this._scrollPane, JideBoxLayout.VARY);
    this.getContentPane().add(this.getButtonPanel(), JideBoxLayout.FIX);
  }

  @Override
  public JComponent getContentPane() {
    return _contentPane;
  }

  public JComponent getContent() {
    return _content;
  }

  @SuppressWarnings("UnusedReturnValue")
  public ButtonPanel getButtonPanel() {
    if (this._buttonPanel == null) {
      this._buttonPanel = new ScrollableButtonPanel() {};
      this._buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN);
      this._buttonPanel.setBorder(UIManager.getDefaults().getBorder("DesktopIcon.border"));
    }
    return this._buttonPanel;
  }

  @SuppressWarnings("UnusedReturnValue")
  public void setDefaultButton(ButtonKind buttonKind) {
    JButton button = (JButton) this._buttonPanel.getButtonByName(buttonKind.name);
    if (button == null) {
      this.addButton(buttonKind);
      button = (JButton) this._buttonPanel.getButtonByName(buttonKind.name);
    }
    this.getRootPane().setDefaultButton(button);
  }

  public void addButton(AbstractButton button, Object constraints, int index) {
    this._buttonPanel.addButton(button, constraints, index);
  }

  public void addButton(AbstractButton button, Object constraints) {
    this.addButton(button, constraints, -1);
  }

  public void addButton(AbstractButton button, ActionListener l) {
    this.addButton(button, l, ButtonPanel.AFFIRMATIVE_BUTTON);
  }

  public void addButton(AbstractButton button, ActionListener l, Object constraints) {
    button.addActionListener(l);
    this.addButton(button, constraints, -1);
  }

  public void addButton(AbstractButton button) {
    this.addButton(button, ButtonPanel.AFFIRMATIVE_BUTTON);
  }

  public void addButton(ButtonKind buttonKind) {
    this.addButton(buttonKind, null, null);
  }

  public void addButton(ButtonKind buttonKind, Action action) {
    this.addButton(buttonKind, action, null);
  }

  public void addButton(ButtonKind buttonKind, ActionListener listener) {
    this.addButton(buttonKind, null, listener);
  }

  public void addButton(ButtonKind buttonKind, Action action, ActionListener listener) {
    // check button exists
    AbstractButton b = (AbstractButton) _buttonPanel.getButtonByName(buttonKind.name);
    boolean needNewButton = b == null;
    if (needNewButton) {
      b = new JButton(buttonKind.i18nText);
      b.setName(buttonKind.name);
      b.setMnemonic(buttonKind.i18nMnemonicKeyCode);
    }
    if (action != null) {
      b.setAction(action);
    } else {
      if (buttonKind.buttonPanelButtonType.equals(ButtonPanel.AFFIRMATIVE_BUTTON)) {
        b.setAction(
            new AbstractAction(I18N.getText(buttonKind.i18nKey)) {
              @Override
              public void actionPerformed(ActionEvent e) {
                setDialogResult(AFFIRM);
                closeDialog();
              }
            });
      } else if (buttonKind.buttonPanelButtonType.equals(ButtonPanel.CANCEL_BUTTON)) {
        b.setAction(
            new AbstractAction(I18N.getText(buttonKind.i18nKey)) {
              @Override
              public void actionPerformed(ActionEvent e) {
                setDialogResult(DENY);
                closeDialog();
              }
            });
      }
    }
    if (listener != null) {
      b.addActionListener(listener);
    }
    if (needNewButton) {
      this._buttonPanel.addButton(b, buttonKind.buttonPanelButtonType);
    }
  }

  public void addNonButton(Component c, Object constraints, int index) {
    this._buttonPanel.add(c, constraints, index);
  }

  public void createOkCancelButtons() {
    this.addButton(ButtonKind.OK);
    this.addButton(ButtonKind.CANCEL);
  }

  public void setButtonOrder(String buttonOrder) {
    this._buttonPanel.setButtonOrder(buttonOrder);
  }

  public void setOppositeButtonOrder(String buttonOrder) {
    this._buttonPanel.setOppositeButtonOrder(buttonOrder);
  }

  public void onBeforeShow(ActionListener listener) {
    this._onShowAction = listener;
  }

  public void onBeforeClose(ActionListener listener) {
    this._onCloseAction = listener;
  }

  public void setDialogTitle(String title) {
    super.setTitle(title);
  }

  public void setContent(JComponent content) {
    this._content = content;
    this._usingAbeillePanel = content instanceof AbeillePanel;
    this._scrollPane.setViewportView(content);
  }

  public AbstractButton getOKButton() {
    return this.getButton(ButtonKind.OK);
  }

  public AbstractButton getCancelButton() {
    return this.getButton(ButtonKind.CANCEL);
  }

  public AbstractButton getButton(ButtonKind buttonKind) {
    return (AbstractButton) this._buttonPanel.getButtonByName(buttonKind.name);
  }

  protected void setDialogResult(String result) {
    this._dialogResult = result;
  }

  public String getDialogResult() {
    return this._dialogResult;
  }

  private Dimension getMaxScreenSize() {
    GraphicsConfiguration gc = getOwner().getGraphicsConfiguration();
    Insets insets = getOwner().getToolkit().getScreenInsets(gc);
    Rectangle bounds = gc.getDevice().getDefaultConfiguration().getBounds();
    return new Dimension(
        bounds.width - insets.left - insets.right, bounds.height - insets.top - insets.bottom);
  }

  private List<Component> getAllComponents(Container c) {
    List<Component> listOut = new ArrayList<>();
    Component[] components = c.getComponents();
    for (Component _c : components) {
      listOut.add(_c);
      if (_c instanceof Container c_) {
        listOut.addAll(getAllComponents(c_));
      }
    }
    return listOut;
  }

  @Override
  public Dimension getPreferredSize() {
    //    for (Component c : getAllComponents(this)) {
    //      System.out.println(c.getName() + c.getPreferredSize());
    //    }

    int scrollBarSize = UIManager.getDefaults().getInt("ScrollBar.width");
    Dimension frameSize = MapTool.getFrame().getSize();
    Dimension superPref = super.getPreferredSize();
    superPref =
        new Dimension(superPref.width + 2 * scrollBarSize, superPref.height + scrollBarSize);
    Dimension screenMax = getMaxScreenSize();
    return new Dimension(
        Math.min(Math.min(superPref.width, screenMax.width), frameSize.width),
        Math.min(Math.min(superPref.height, screenMax.height), frameSize.height));
  }

  @Override
  public Dimension getMaximumSize() {
    Dimension superMax = super.getMaximumSize();
    Dimension screenMax = getMaxScreenSize();
    Dimension frameSize = MapTool.getFrame().getSize();
    return new Dimension(
        Math.min(Math.min(superMax.width, screenMax.width), frameSize.width),
        Math.min(Math.min(superMax.height, screenMax.height), frameSize.height));
  }

  @Override
  public void setMaximumSize(Dimension maximumSize) {
    Dimension screenMax = getMaxScreenSize();
    // limit size to screen size
    super.setMaximumSize(
        new Dimension(
            Math.min(maximumSize.width, screenMax.width),
            Math.min(maximumSize.height, screenMax.height)));
  }

  public String showDialogWithReturnValue() {
    // have to be modal to return a result
    if (!isModal()) {
      this.setModal(true);
    }
    showDialog();
    return this.getDialogResult();
  }

  public void showDialog() {
    // things to do when first displayed
    if (!this.hasBeenShown) {
      this.invalidate();
      this.pack();
      // tie escape key to dialogue close
      this._content
          .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
      this._content
          .getActionMap()
          .put(
              "cancel",
              new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                  setDialogResult(GenericDialog.DENY);
                  closeDialog();
                }
              });
      // if not set, and there is only one button, make it the default.
      if (this.getRootPane().getDefaultButton() == null
          && _buttonPanel.getComponents().length == 1) {
        this.getRootPane().setDefaultButton((JButton) _buttonPanel.getComponents()[0]);
      }
      // Center the dialogue over its parent
      SwingUtil.centerOver(this, this.getOwner());
      this.hasBeenShown = true;
    }
    // set off the onShowAction if present
    if (this._onShowAction != null) {
      this._onShowAction.actionPerformed(
          new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "show"));
    }
    // let it be seen
    super.setVisible(true);
  }

  public void closeDialog() {
    if (this._usingAbeillePanel && ((AbeillePanel<?>) this._content).getModel() != null) {
      // wrap up any AbeillePanel commits and unbinding
      if (this.getDialogResult().equals(AFFIRM)) {
        ((AbeillePanel<?>) _content).commit();
      }
      ((AbeillePanel<?>) _content).unbind();
    }
    // set off the onCloseAction if present
    if (this._onCloseAction != null) {
      this._onCloseAction.actionPerformed(
          new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "close"));
    }
    // hide or dispose as appropriate
    switch (getDefaultCloseOperation()) {
      case DISPOSE_ON_CLOSE, EXIT_ON_CLOSE -> this.dispose();
      case HIDE_ON_CLOSE -> super.setVisible(false);
    }
  }
}
