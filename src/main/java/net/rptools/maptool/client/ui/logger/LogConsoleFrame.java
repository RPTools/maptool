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
package net.rptools.maptool.client.ui.logger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;
import net.rptools.lib.image.ImageUtil;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.language.I18N;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class LogConsoleFrame extends JFrame {
  private static final Logger log = LogManager.getLogger(LogConsoleFrame.class);

  // private RSyntaxTextArea jLoggingConsole; // Doesn't look like RSyntaxTextArea can keep up with
  // large amounts of logging
  private JTextArea jLoggingConsole;

  private static final String LOGGER_IMAGE = "net/rptools/maptool/client/image/log4j_icon.png";
  private static final Font LOGGER_FONT = new Font("Lucida Console", Font.PLAIN, 12);

  private JButton clearButton;
  private JButton closeButton;

  public LogConsoleFrame() {
    setTitle(I18N.getString("action.openLogConsole.title"));
    setSize(800, 600);
    try {
      setIconImage(ImageUtil.getImage(LOGGER_IMAGE));
    } catch (IOException ioe) {
      String msg = I18N.getText("msg.error.loadingIconImage");
      log.error(msg, ioe);
      System.err.println(msg);
    }

    initUI();
  }

  public LogConsoleFrame(String title, Dimension dimension) {
    setPreferredSize(dimension);
    setTitle(title);
    initUI();
  }

  private void initUI() {
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setLayout(new BorderLayout());

    JScrollPane jConsoleScroll = new JScrollPane(getNoteArea());
    jConsoleScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

    add(BorderLayout.CENTER, jConsoleScroll);
    add(BorderLayout.SOUTH, createButtonBar());

    addWindowListener(
        new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            AppState.setLoggingToConsole(false);
            setVisible(false);
          }
        });
  }

  private JTextComponent getNoteArea() {
    if (jLoggingConsole == null) {
      // jLoggingConsole = new RSyntaxTextArea();
      jLoggingConsole = new JTextArea();
      jLoggingConsole.setBorder(BorderFactory.createLineBorder(Color.black));

      jLoggingConsole.setLineWrap(true);
      jLoggingConsole.setWrapStyleWord(true);
      jLoggingConsole.setEditable(false);
      jLoggingConsole.setFont(LOGGER_FONT);

      // Subscribe the text area to JTextAreaAppender
      JTextAreaAppender.addTextArea(jLoggingConsole);
    }

    return jLoggingConsole;
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
              processWindowEvent(new WindowEvent(LogConsoleFrame.this, WindowEvent.WINDOW_CLOSING));
            }
          });
    }
    return closeButton;
  }

  public synchronized void addText(String text) {
    jLoggingConsole.append(text);
  }
}
