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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;

public class NoteFrame extends JFrame {
  private JTextComponent noteArea;
  private JButton clearButton;
  private JButton closeButton;

  public NoteFrame() {
    setPreferredSize(new Dimension(300, 300));
    initUI();
  }

  private void initUI() {
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new JScrollPane(getNoteArea()));
    add(BorderLayout.SOUTH, createButtonBar());
  }

  private JTextComponent getNoteArea() {
    if (noteArea == null) {
      noteArea = new JTextArea();
      noteArea.setBorder(BorderFactory.createLineBorder(Color.black));
    }
    return noteArea;
  }

  private JPanel createButtonBar() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    panel.add(BorderLayout.WEST, getClearButton());
    panel.add(BorderLayout.EAST, getCloseButton());
    return panel;
  }

  private JButton getClearButton() {
    if (clearButton == null) {
      clearButton = new JButton("Clear");
      clearButton.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              getNoteArea().setText("");
            }
          });
    }
    return clearButton;
  }

  private JButton getCloseButton() {
    if (closeButton == null) {
      closeButton = new JButton("Close");
      closeButton.addActionListener(
          new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              processWindowEvent(new WindowEvent(NoteFrame.this, WindowEvent.WINDOW_CLOSING));
            }
          });
    }
    return closeButton;
  }

  public synchronized void addText(String text) {
    getNoteArea().setText(getNoteArea().getText() + text + "\n");
    getNoteArea().setCaretPosition(getNoteArea().getText().length());
  }
}
