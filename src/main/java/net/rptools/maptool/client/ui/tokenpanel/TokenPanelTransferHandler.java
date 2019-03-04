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
package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import net.rptools.lib.transferable.MapToolTokenTransferData;
import net.rptools.maptool.model.Token;

/**
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
public class TokenPanelTransferHandler extends TransferHandler {

  /*---------------------------------------------------------------------------------------------
   * Constructor
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create the transfer handler for the passed display component.
   *
   * @param displayComponent Create the handler for this component.
   */
  public TokenPanelTransferHandler(JComponent displayComponent) {
    if (displayComponent instanceof JList) ((JList) displayComponent).setDragEnabled(true);
    displayComponent.setTransferHandler(this);
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden TransferHandler methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
   *     java.awt.datatransfer.DataFlavor[])
   */
  @Override
  public boolean canImport(JComponent aComp, DataFlavor[] aTransferFlavors) {
    return false;
  }

  /**
   * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
   *     java.awt.datatransfer.Transferable)
   */
  @Override
  public boolean importData(JComponent aComp, Transferable aT) {
    return false;
  }

  /** @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent) */
  @Override
  public int getSourceActions(JComponent aC) {
    return TransferHandler.COPY;
  }

  /** @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent) */
  @Override
  protected Transferable createTransferable(JComponent aC) {
    if (aC instanceof JList) {
      Object[] selectedValues = ((JList) aC).getSelectedValuesList().toArray();
      return new TokenPanelTransferable(selectedValues);
    } // endif
    return null;
  }
}

/**
 * Used to transfer the selected tokens from the token panel.
 *
 * @author jgorrell
 * @version $Revision$ $Date$ $Author$
 */
class TokenPanelTransferable implements Transferable {

  /** The array of tokens read from the token panel when the transferable was created */
  private Object[] tokens;

  /**
   * Create the transferable for the given tokens.
   *
   * @param theTokens Tokens being transfered. Uses object array since that is what is provided by
   *     the list.
   */
  TokenPanelTransferable(Object[] theTokens) {
    tokens = theTokens;
  }

  /** @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor) */
  public Object getTransferData(DataFlavor aFlavor) throws UnsupportedFlavorException, IOException {
    if (!isDataFlavorSupported(aFlavor)) throw new UnsupportedFlavorException(aFlavor);
    MapToolTokenTransferData tokenList = new MapToolTokenTransferData();
    for (int i = 0; i < tokens.length; i++) tokenList.add(((Token) tokens[i]).toTransferData());
    return tokenList;
  }

  /** @see java.awt.datatransfer.Transferable#getTransferDataFlavors() */
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {MapToolTokenTransferData.MAP_TOOL_TOKEN_LIST_FLAVOR};
  }

  /**
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported(DataFlavor aFlavor) {
    return MapToolTokenTransferData.MAP_TOOL_TOKEN_LIST_FLAVOR.equals(aFlavor);
  }
}
