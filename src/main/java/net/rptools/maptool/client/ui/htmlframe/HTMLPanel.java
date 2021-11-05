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
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

/** Represents the JPanel holding the HTML pane. */
public class HTMLPanel extends JPanel implements HTMLPanelInterface {
  private static final long serialVersionUID = -2574631956909778786L;

  /** The HTMLPane holding the HTML content. */
  private final HTMLPane pane = new HTMLPane();

  /**
   * Creates a new HTMLPanel.
   *
   * @param container The container that will hold the HTML panel
   * @param scrollBar whether panel have scroll bars or not
   */
  HTMLPanel(final HTMLPanelContainer container, boolean scrollBar) {
    setLayout(new BorderLayout());

    if (scrollBar) {
      add(new JScrollPane(pane), BorderLayout.CENTER);
    } else {
      add(pane, BorderLayout.CENTER);
    }
    updateContents("", false);

    // ESCAPE closes the window
    pane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
    pane.getActionMap()
        .put(
            "cancel",
            new AbstractAction() {
              public void actionPerformed(ActionEvent e) {
                container.closeRequest();
              }
            });
  }

  @Override
  public void updateContents(final String html, boolean scrollReset) {
    pane.updateContents(html, scrollReset);
  }

  /** Flushes any caching for the panel. */
  @Override
  public void flush() {
    pane.flush();
  }

  /**
   * Add the object to a HTMLPanelContainer.
   *
   * @param container the container
   */
  @Override
  public void addToContainer(HTMLPanelContainer container) {
    container.add(this);
  }

  @Override
  public void removeFromContainer(HTMLPanelContainer container) {
    container.remove(this);
  }

  @Override
  public void addActionListener(ActionListener container) {
    pane.addActionListener(container);
  }

  /**
   * Returns false.
   *
   * @param script the script that cannot be ran
   * @return false
   */
  @Override
  public boolean runJavascript(String script) {
    return false;
  }
}
