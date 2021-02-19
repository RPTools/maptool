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

import java.awt.event.ActionListener;

/** Interface for the HTML Panel holding the HTML Pane. */
interface HTMLPanelInterface {

  /**
   * Update the HTML content and the close button.
   *
   * @param html the html to load.
   * @param scrollreset whether the scroll bar should be reset.
   */
  void updateContents(final String html, boolean scrollreset);

  /** Flush the Panel. */
  void flush();

  /**
   * Add the Panel to a HTMLPanelContainer.
   *
   * @param container the container.
   */
  void addToContainer(final HTMLPanelContainer container);

  /**
   * Remove the Panel from an HTMLPanelContainer.
   *
   * @param container the container.
   */
  void removeFromContainer(final HTMLPanelContainer container);

  /**
   * Add an ActionListener for the container to the panel.
   *
   * @param container the container.
   */
  void addActionListener(final ActionListener container);

  /**
   * Modify the rolls and hyperlinks in the HTML.
   *
   * @param html the HTML to modify
   * @return the modified HTML
   */
  static String fixHTML(String html) {
    // We use ASCII control characters to mark off the rolls so that there's no limitation on what
    // (printable) characters the output can include
    // Note: options gm, self, and whisper are currently ignored
    // Tooltip rolls
    html =
        html.replaceAll(
            "\036(\001\002)?([^\036\037]*)\037([^\036]*)\036",
            "<span class='roll' title='&#171; $2 &#187;'>$3</span>");
    // Unformatted rolls
    html = html.replaceAll("\036\01u\02([^\036]*)\036", "&#171; $1 &#187;");
    // Inline rolls
    html =
        html.replaceAll(
            "\036(\001\002)?([^\036]*)\036",
            "&#171;<span class='roll' style='color:blue'>&nbsp;$2&nbsp;</span>&#187;");
    // Auto inline expansion
    html = html.replaceAll("(^|\\s)(https?://[\\w.%-/~?&+#=]+)", "$1<a href='$2'>$2</a>");
    return html;
  }

  /**
   * Runs a javascript, if the panel supports it.
   *
   * @param script the script to run.
   * @return true if the script can be run, false otherwise.
   */
  boolean runJavascript(String script);
}
