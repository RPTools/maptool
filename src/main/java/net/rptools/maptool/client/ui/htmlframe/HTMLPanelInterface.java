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
   */
  void updateContents(final String html);

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
}
