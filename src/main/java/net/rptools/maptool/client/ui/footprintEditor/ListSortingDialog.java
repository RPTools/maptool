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
package net.rptools.maptool.client.ui.footprintEditor;

import com.jidesoft.list.ListTransferHandler;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.*;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.swing.AbeillePanel;
import net.rptools.maptool.client.swing.SwingUtil;
import net.rptools.maptool.language.I18N;

public class ListSortingDialog extends JDialog {
  AbeillePanel<JDialog> formPanel = new AbeillePanel<>(new ListSorterView().getRootComponent());
  private List<Object> sortList = new LinkedList<>();

  public ListSortingDialog(JFrame owner, List<Object> listToSort) {
    super(owner, I18N.getText("initPanel.sort"), true);
    sortList.addAll(listToSort);
    initialise();
    pack();
  }

  JButton okButton;
  JButton cancelButton;
  JButton upButton;
  JButton downButton;
  JList<Object> sortingList;
  JPanel listHolder;
  DefaultListModel<Object> listModel;

  private void initialise() {
    setLayout(new GridLayout());
    formPanel = new AbeillePanel<>(new ListSorterView().getRootComponent());
    getRootPane().setDefaultButton(okButton);

    setOkButton((JButton) formPanel.getButton("okButton"));
    setCancelButton((JButton) formPanel.getButton("cancelButton"));
    setUpButton((JButton) formPanel.getButton("upButton"));
    setDownButton((JButton) formPanel.getButton("downButton"));
    setListHolder((JPanel) formPanel.getComponent("listHolder"));
    setSortingList();

    add(formPanel);
    this.pack();
  }

  public List<Object> showDialog() {
    setVisible(true);
    Object[] tmp = new Object[listModel.getSize()];
    listModel.copyInto(tmp);
    return Arrays.stream(tmp).toList();
  }

  void cancel() {
    sortList = null;
    closeDialog();
  }

  public void setSortingList() {
    listModel = new DefaultListModel<>();
    sortingList = new JList<>(listModel);
    listModel.addAll(sortList);
    sortingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    sortingList.setTransferHandler(new ListTransferHandler(null));
    sortingList.setDragEnabled(true);
    sortingList.setVisibleRowCount(sortList.size());
    listHolder.add(new JScrollPane(this.sortingList), BorderLayout.CENTER);
    listHolder.invalidate();
  }

  public void setListHolder(JPanel listHolder) {
    this.listHolder = listHolder;
  }

  void moveItem(boolean up) {
    int selectedIndex = sortingList.getSelectedIndex();
    if ((up && selectedIndex == 0)
        || (!up && selectedIndex == sortList.size() - 1)
        || selectedIndex == -1) {
      return;
    }
    int targetIndex = up ? selectedIndex - 1 : selectedIndex + 1;

    Object selectedElement = listModel.getElementAt(selectedIndex);
    listModel.removeElementAt(selectedIndex);
    listModel.insertElementAt(selectedElement, targetIndex);
    sortingList.setSelectedIndex(targetIndex);
  }

  private final ActionListener buttonListener =
      e -> {
        switch (e.getActionCommand()) {
          case "up" -> moveItem(true);
          case "down" -> moveItem(false);
          case "ok" -> closeDialog();
          case "cancel" -> cancel();
        }
      };

  private void setDownButton(JButton downButton) {
    this.downButton = downButton;
    this.downButton.setActionCommand("down");
    this.downButton.addActionListener(buttonListener);
  }

  private void setUpButton(JButton upButton) {
    this.upButton = upButton;
    this.upButton.setActionCommand("up");
    this.upButton.addActionListener(buttonListener);
  }

  private void setOkButton(JButton okButton) {
    this.okButton = okButton;
    this.okButton.setActionCommand("ok");
    this.okButton.addActionListener(buttonListener);
  }

  private void setCancelButton(JButton cancelButton) {
    this.cancelButton = cancelButton;
    this.cancelButton.setActionCommand("cancel");
    this.cancelButton.addActionListener(buttonListener);
  }

  void closeDialog() {
    setVisible(false);
    dispose();
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      SwingUtil.centerOver(this, MapTool.getFrame());
    }
    super.setVisible(b);
  }
}
