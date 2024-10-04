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
package net.rptools.maptool.client.ui.macrobuttons;

import java.util.HashMap;
import java.util.Map;
import javax.swing.KeyStroke;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.macrobuttons.buttons.MacroButton;

/**
 * @author tylere
 */
public class MacroButtonHotKeyManager {

  // Changing and adding more hotkeys should work smoothly, however hotkeys[0]
  // should be kept as the "no hotkey" option, regardless of the actual String used for it.
  // HOTKEYS strings must follow the syntax required by getKeyStroke(String s)
  public static final String[] HOTKEYS = {
    "None",
    "F2",
    "F3",
    "F4",
    "F5",
    "F6",
    "F7",
    "F8",
    "F9",
    "F10",
    "F11",
    "F12",
    "alt F1",
    "alt F2",
    "alt F3",
    "alt F5",
    "alt F6",
    "alt F7",
    "alt F8",
    "alt F9",
    "alt F10",
    "alt F11",
    "alt F12",
    "ctrl F1",
    "ctrl F2",
    "ctrl F3",
    "ctrl F4",
    "ctrl F5",
    "ctrl F6",
    "ctrl F7",
    "ctrl F8",
    "ctrl F9",
    "ctrl F10",
    "ctrl F11",
    "ctrl F12",
    "shift F1",
    "shift F2",
    "shift F3",
    "shift F4",
    "shift F5",
    "shift F6",
    "shift F7",
    "shift F8",
    "shift F9",
    "shift F10",
    "shift F11",
    "shift F12"
  };

  // our own map is required to allow us to search which button has an associated keystroke
  private static Map<KeyStroke, MacroButton> buttonsByKeyStroke =
      new HashMap<KeyStroke, MacroButton>();
  private MacroButton macroButton;

  public static boolean isHotkeyAssigned(String hotkey) {
    return buttonsByKeyStroke.containsKey(hotkey);
  }

  public MacroButtonHotKeyManager(MacroButton macroButton) {
    this.macroButton = macroButton;
  }

  public void assignKeyStroke(String hotKey) {

    // remove the old keystroke from our map
    KeyStroke oldKeystroke = KeyStroke.getKeyStroke(macroButton.getProperties().getHotKey());
    buttonsByKeyStroke.remove(oldKeystroke);
    // assign the new hotKey
    macroButton.getProperties().setHotKey(hotKey);

    // HOTKEYS[0] is no hotkey.
    if (!hotKey.equals(HOTKEYS[0])) {

      KeyStroke keystroke = KeyStroke.getKeyStroke(hotKey);

      // Check what button the hotkey is already assigned to
      MacroButton oldButton = buttonsByKeyStroke.get(keystroke);

      // if it is already assigned, then update the old mapped button
      if (oldButton != macroButton && oldButton != null) {

        // tell the old button properties it no longer has a hotkey
        oldButton.getProperties().setHotKey(HOTKEYS[0]);
        // remove the hot key reference from the button's text
        oldButton.setText(oldButton.getButtonText());
        // remove from our map
        buttonsByKeyStroke.remove(keystroke);
        // need to save settings
        oldButton.getProperties().save();
      }

      // Add the new button and keystroke to our map
      buttonsByKeyStroke.put(keystroke, macroButton);

      // keep macrotabbedpane's keystrokes in sync
      if (MapTool.getFrame() != null) {
        // MapTool.getFrame().getMacroTabbedPane().updateKeyStrokes();
        // TODO: change this later to use the hub
        MapTool.getFrame().updateKeyStrokes();
      }
    }
  }

  public static Map<KeyStroke, MacroButton> getKeyStrokeMap() {
    return buttonsByKeyStroke;
  }

  // when the user loads a campaign the saved macro keystrokes should be restored
  // and the possible conflicts with the global macro keystrokes should be resolved.
  public static void clearKeyStrokes() {
    buttonsByKeyStroke.clear();
  }
}
