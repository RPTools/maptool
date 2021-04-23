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
package net.rptools.maptool.client.ui.macrobuttons.buttons;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.jetbrains.annotations.NotNull;

public class TransferableMacroButton implements Transferable {

  public static final DataFlavor macroButtonFlavor =
      new DataFlavor(MacroButton.class, "Macro Button");

  // private TokenMacroButton button;
  private TransferData transferData;

  public TransferableMacroButton(
      MacroButton button, int transferGestureModifiers, int panelHashcode) {
    // this.button = button;
    transferData = new TransferData(button, transferGestureModifiers, panelHashcode);
  }

  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {macroButtonFlavor};
  }

  public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
    return dataFlavor.equals(macroButtonFlavor);
  }

  public @NotNull Object getTransferData(DataFlavor dataFlavor) {
    if (dataFlavor.equals(macroButtonFlavor)) {
      return transferData;
    }

    return null;
  }
}
