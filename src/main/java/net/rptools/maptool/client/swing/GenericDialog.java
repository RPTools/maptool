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
import net.rptools.maptool.client.ui.theme.RessourceManager;
import net.rptools.maptool.language.I18N;

public class GenericDialog extends JDialog {
  public static final String AFFIRM = ButtonPanel.AFFIRMATIVE_BUTTON;
  public static final String DENY = ButtonPanel.CANCEL_BUTTON;
  private boolean hasPositionedItself;
  private String _dialogResult = ButtonPanel.CANCEL_BUTTON;
  private final JComponent _contentPane = new JPanel();
  private JComponent _mainContent = new JPanel();
  private JComponent _header = new JPanel();
  private JComponent _navPane = new JPanel();
  private JComponent _sideBarPane = new JPanel();
  private JComponent _toolbar = new JToolBar();
  private final JScrollPane _scrollPane = new JScrollPane();
  private ButtonPanel _buttonPanel;
  private final List<AbeillePanel<?>> abeillePanelList = new ArrayList<>();
  private ActionListener _onCloseAction;
  private ActionListener _onShowAction;

  public static GenericDialogFactory getFactory() {
    return new GenericDialogFactory();
  }

  /** Whilst this works. You should use the factory method instead. */
  public GenericDialog() {
    super(MapTool.getFrame());
    super.setContentPane(_contentPane);
    Resizable _resizable =
        new Resizable(getRootPane()) {
          public void resizing(int resizeDir, int newX, int newY, int newW, int newH) {
            Container container = GenericDialog.this.getContentPane();
            PortingUtils.setPreferredSize(container, new Dimension(newW, newH));
            if (GenericDialog.this.isUndecorated()) {
              GenericDialog.this.setBounds(newX, newY, newW, newH);
            }
          }
        };
    _resizable.setResizeCornerSize(18);
    _resizable.setResizableCorners(Resizable.LOWER_LEFT | Resizable.LOWER_RIGHT);
    super.setResizable(true);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    getToolbar().setVisible(false);
    getNavPane().setVisible(false);
    getSideBarPane().setVisible(false);
    getHeader().setVisible(false);

    addWindowListener(
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
    setDialogTitle(title);
    setModal(modal);
    setContent(panel);
  }

  protected void layoutComponents() {
    getScrollPane().setViewportView(getContentPanel());
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(getToolbar(), BorderLayout.BEFORE_FIRST_LINE);
    getContentPane().add(getHeader(), BorderLayout.NORTH);
    getContentPane().add(getNavPane(), BorderLayout.WEST);
    getContentPane().add(getScrollPane(), BorderLayout.CENTER);
    getContentPane().add(getSideBarPane(), BorderLayout.EAST);
    getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
  }

  @Override
  public JComponent getContentPane() {
    return this._contentPane;
  }

  public JComponent getToolbar() {
    return this._toolbar;
  }

  public JComponent getHeader() {
    return this._header;
  }

  public JComponent getContentPanel() {
    return this._mainContent;
  }

  public JComponent getSideBarPane() {
    return this._sideBarPane;
  }

  public JComponent getNavPane() {
    return this._navPane;
  }

  public JScrollPane getScrollPane() {
    return this._scrollPane;
  }

  @SuppressWarnings("UnusedReturnValue")
  public ButtonPanel getButtonPanel() {
    if (this._buttonPanel == null) {
      this._buttonPanel = new ScrollableButtonPanel();
      this._buttonPanel.setSizeConstraint(ButtonPanel.NO_LESS_THAN);
      this._buttonPanel.setBorder(
          BorderFactory.createCompoundBorder(
              new PartialLineBorder(
                  UIManager.getDefaults().getColor("windowBorder"), 1, PartialLineBorder.NORTH),
              BorderFactory.createEmptyBorder(4, 6, 6, 6)));
    }
    return this._buttonPanel;
  }

  @SuppressWarnings("UnusedReturnValue")
  public void setDefaultButton(ButtonKind buttonKind) {
    JButton button = (JButton) getButtonPanel().getButtonByName(buttonKind.name);
    if (button == null) {
      addButton(buttonKind);
      button = (JButton) getButtonPanel().getButtonByName(buttonKind.name);
    }
    getRootPane().setDefaultButton(button);
  }

  public void setButtonOrder(String buttonOrder) {
    getButtonPanel().setButtonOrder(buttonOrder);
  }

  public void setOppositeButtonOrder(String buttonOrder) {
    getButtonPanel().setOppositeButtonOrder(buttonOrder);
  }

  public void addNonButton(Component c, Object constraints, int index) {
    getButtonPanel().add(c, constraints, index);
  }

  public void addButton(AbstractButton button, Object constraints, int index) {
    getButtonPanel().addButton(button, constraints, index);
  }

  public void addButton(ButtonKind buttonKind) {
    addButton(buttonKind, null, null);
  }

  public void addButton(ButtonKind buttonKind, Action action) {
    addButton(buttonKind, action, null);
  }

  public void addButton(ButtonKind buttonKind, ActionListener listener) {
    addButton(buttonKind, null, listener);
  }

  public void addButton(ButtonKind buttonKind, Action action, ActionListener listener) {
    // check button exists
    AbstractButton b = (AbstractButton) getButtonPanel().getButtonByName(buttonKind.name);
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
      this.getButtonPanel().addButton(b, buttonKind.buttonPanelButtonType);
    }
  }

  public void createOkCancelButtons() {
    addButton(ButtonKind.OK);
    addButton(ButtonKind.CANCEL);
  }

  public void onBeforeShow(ActionListener listener) {
    _onShowAction = listener;
  }

  public void onBeforeClose(ActionListener listener) {
    _onCloseAction = listener;
  }

  public void setDialogTitle(String title) {
    super.setTitle(title);
  }

  public void setToolbar(JComponent toolbarContent) {
    if (toolbarContent instanceof JToolBar toolbar) {
      this._toolbar = toolbar;
    } else {
      this._toolbar.add(toolbarContent);
    }
    this._toolbar.setVisible(true);
    this._toolbar.setMinimumSize(
        new Dimension(this.getPreferredSize().width, RessourceManager.smallIconSize));
    if (toolbarContent instanceof AbeillePanel<?> panel) {
      abeillePanelList.add(panel);
    }
    this.getContentPane()
        .setBorder(
            new PartialLineBorder(
                UIManager.getDefaults().getColor("windowBorder"), 1, PartialLineBorder.NORTH));
  }

  public void setHeader(JComponent headerContent) {
    this._header = headerContent;
    this._header.setVisible(true);
    if (headerContent instanceof AbeillePanel<?> panel) {
      abeillePanelList.add(panel);
    }
  }

  @SuppressWarnings("unused")
  public void setNavPane(JComponent navContent) {
    this._navPane = navContent;
    this._navPane.setVisible(true);
    if (navContent instanceof AbeillePanel<?> panel) {
      abeillePanelList.add(panel);
    }
  }

  @SuppressWarnings("unused")
  public void setSideBarPane(JComponent sideBarContent) {
    this._sideBarPane = sideBarContent;
    this._sideBarPane.setVisible(true);
    if (sideBarContent instanceof AbeillePanel<?> panel) {
      abeillePanelList.add(panel);
    }
  }

  public void setContent(JComponent mainContent) {
    this._mainContent = mainContent;
    if (mainContent instanceof AbeillePanel<?> panel) {
      abeillePanelList.add(panel);
    }
    this._scrollPane.setViewportView(mainContent);

    // ESCAPE cancels the window without committing
    mainContent
        .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
    mainContent
        .getActionMap()
        .put(
            "close",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                closeDialog();
              }
            });
  }

  public AbstractButton getOKButton() {
    return getButton(ButtonKind.OK);
  }

  public AbstractButton getCancelButton() {
    return getButton(ButtonKind.CANCEL);
  }

  public void closeDialog() {
    if (_onCloseAction != null) {
      _onCloseAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "close"));
    }
    if (!abeillePanelList.isEmpty()) {
      for (AbeillePanel<?> panel : abeillePanelList) {
        if (panel.getModel() != null && getDialogResult().equals(AFFIRM)) {
          panel.commit();
        }
        if (getDefaultCloseOperation() == DISPOSE_ON_CLOSE) {
          panel.unbind();
        }
      }
    }
    super.setVisible(false);
  }

  public AbstractButton getButton(ButtonKind buttonKind) {
    return (AbstractButton) getButtonPanel().getButtonByName(buttonKind.name);
  }

  protected void setDialogResult(String result) {
    _dialogResult = result;
  }

  public String getDialogResult() {
    return _dialogResult;
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
    int scrollBarSize = UIManager.getDefaults().getInt("ScrollBar.width");
    Dimension superPref = super.getPreferredSize();
    superPref =
        new Dimension(superPref.width + 2 * scrollBarSize, superPref.height + scrollBarSize);
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

  public String showDialogWithReturnValue() {
    if (!isModal()) {
      setModal(true);
    }
    setVisible(true);
    return this.getDialogResult();
  }

  public void showDialog() {
    setVisible(true);
    _scrollPane.requestFocus();
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      layoutComponents();
      getRootPane().invalidate();
      // We want to center over our parent, but only the first time.
      // If this dialog is reused, we want it to show up where it was last.
      pack();
      if (!hasPositionedItself) {
        positionInitialView();
        hasPositionedItself = true;
      }
      if (_onShowAction != null) {
        _onShowAction.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "show"));
        revalidate();
      }
      if (getRootPane().getDefaultButton() == null
          && getButtonPanel().getComponents().length == 1) {
        getRootPane().setDefaultButton((JButton) getButtonPanel().getComponents()[0]);
      }
      super.setVisible(true);
    } else {
      super.setVisible(false);
    }
  }

  protected void positionInitialView() {
    SwingUtil.centerOver(this, getOwner());
  }
}
