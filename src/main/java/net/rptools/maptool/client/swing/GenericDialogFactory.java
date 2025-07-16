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

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public class GenericDialogFactory {
  GenericDialog delegate;

  public GenericDialog getDialog() {
    return delegate;
  }

  public GenericDialogFactory() {
    delegate = new GenericDialog();
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setDefaultButton(ButtonKind buttonKind) {
    delegate.setDefaultButton(buttonKind);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addButton(
      AbstractButton b, ActionListener l, Object constraints, int index) {
    b.addActionListener(l);
    delegate.addButton(b, constraints, index);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addButton(AbstractButton b, ActionListener l, Object constraints) {
    return addButton(b, l, constraints, -1);
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addNonButton(Component c, Object constraints, int index) {
    delegate.addNonButton(c, constraints, index);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addNonButton(Component c, Object constraints) {
    return addNonButton(c, constraints, -1);
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addButton(ButtonKind buttonKind) {
    return addButton(buttonKind, null, null);
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addButton(ButtonKind buttonKind, Action action) {
    return addButton(buttonKind, action, null);
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addButton(ButtonKind buttonKind, ActionListener listener) {
    return addButton(buttonKind, null, listener);
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory addButton(
      ButtonKind buttonKind, Action action, ActionListener listener) {
    delegate.addButton(buttonKind, action, listener);
    return this;
  }

  public String getButtonOrder() {
    return delegate.getButtonPanel().getButtonOrder();
  }

  public String getOppositeButtonOrder() {
    return delegate.getButtonPanel().getOppositeButtonOrder();
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setButtonOrder(String buttonOrder) {
    delegate.setButtonOrder(buttonOrder);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setOppositeButtonOrder(String buttonOrder) {
    delegate.setOppositeButtonOrder(buttonOrder);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory makeModal(boolean modal) {
    delegate.setModal(modal);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setCloseOperation(int closeOperation) {
    delegate.setDefaultCloseOperation(closeOperation);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory createOkCancelButtons() {
    delegate.createOkCancelButtons();
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory onBeforeShow(ActionListener listener) {
    delegate.onBeforeShow(listener);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory onBeforeClose(ActionListener listener) {
    delegate.onBeforeClose(listener);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setDialogTitle(String title) {
    delegate.setTitle(title);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setContent(JComponent content) {
    delegate.setContent(content);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory setDialogResult(String string) {
    delegate.setDialogResult(string);
    return this;
  }

  @SuppressWarnings("UnusedReturnValue")
  public GenericDialogFactory display() {
    delegate.showDialog();
    return this;
  }

  public String displayWithReturnValue() {
    return delegate.showDialogWithReturnValue();
  }

  public AbstractButton getButton(ButtonKind buttonKind) {
    return delegate.getButton(buttonKind);
  }

  public AbstractButton getOKButton() {
    return getButton(ButtonKind.OK);
  }

  public AbstractButton getCancelButton() {
    return getButton(ButtonKind.CANCEL);
  }

  public String closeDialog() {
    String result = delegate.getDialogResult();
    delegate.closeDialog();
    return result;
  }

  public JButton getDefaultButton() {
    return delegate.getRootPane().getDefaultButton();
  }

  public String getResult() {
    return delegate.getDialogResult();
  }
}
