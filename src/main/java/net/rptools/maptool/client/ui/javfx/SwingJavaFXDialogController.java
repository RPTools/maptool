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
package net.rptools.maptool.client.ui.javfx;

/**
 * This interface must be implemented by all JavaFX UI Controller classes that will be loaded by the
 * {@link FXMLLoaderUtil} class.
 */
public interface SwingJavaFXDialogController {

  /**
   * Registers an event handler that will be called when the defined events occur.
   *
   * @param handler the event handler to call.
   */
  void registerEventHandler(SwingJavaFXDialogEventHandler handler);

  /**
   * Registers an event handler that will be called when the defined events occur.
   *
   * @param handler the event handler to call.
   */
  void deregisterEventHandler(SwingJavaFXDialogEventHandler handler);

  /** Initialize the contents of the JavaFX UI. */
  void init();
}
