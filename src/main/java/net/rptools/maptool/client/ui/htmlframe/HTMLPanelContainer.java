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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import net.rptools.maptool.client.functions.MacroLinkFunction;
import net.rptools.maptool.model.Token;

/** Interface for the container of an HTML panel. */
public interface HTMLPanelContainer extends ActionListener {

  /** @return the visibility of the container. */
  boolean isVisible();

  /** @return the macroCallBacks of the container. */
  Map<String, String> macroCallbacks();

  /**
   * Set the visibility of the container.
   *
   * @param visible should the container be visible.
   */
  void setVisible(boolean visible);

  /** @return the temporary status of the container. */
  boolean getTemporary();

  /**
   * Set the temporary status of the container.
   *
   * @param temp should the container be temporary/
   */
  void setTemporary(boolean temp);

  /** @return the value stored into the container. */
  Object getValue();

  /**
   * Set the value of the container.
   *
   * @param value the value to store.
   */
  void setValue(Object value);

  /**
   * Add a component to the container.
   *
   * @param component the component to add.
   * @return the added component.
   */
  Component add(Component component);

  /**
   * Remove a component from the container.
   *
   * @param component the component to remove.
   */
  void remove(Component component);

  /**
   * Act when an action is performed.
   *
   * @param e the ActionEvent.
   */
  void actionPerformed(ActionEvent e);

  /** Request to close the container. */
  void closeRequest();

  //////////// STATIC METHODS  //////////////

  /**
   * Run the callback macro for "onChangeSelection".
   *
   * @param macroCallbacks the call back macros.
   */
  static void selectedChanged(Map<String, String> macroCallbacks) {
    if (macroCallbacks.get("onChangeSelection") != null) {
      EventQueue.invokeLater(
          () -> MacroLinkFunction.runMacroLink(macroCallbacks.get("onChangeSelection")));
    }
  }

  /**
   * Run the callback macro for "onChangeImpersonated".
   *
   * @param macroCallbacks the call back macros.
   */
  static void impersonatedChanged(Map<String, String> macroCallbacks) {
    if (macroCallbacks.get("onChangeImpersonated") != null) {
      EventQueue.invokeLater(
          () -> MacroLinkFunction.runMacroLink(macroCallbacks.get("onChangeImpersonated")));
    }
  }

  /**
   * Run the callback macro for "onChangeToken".
   *
   * @param token the token that changed.
   * @param macroCallbacks the call back macros.
   */
  static void tokenChanged(final Token token, Map<String, String> macroCallbacks) {
    if (macroCallbacks.get("onChangeToken") != null) {
      EventQueue.invokeLater(
          () ->
              MacroLinkFunction.runMacroLink(
                  macroCallbacks.get("onChangeToken") + token.getId().toString()));
    }
  }
}
