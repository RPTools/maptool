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
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the transfer handler for the list in the {@link InitiativePanel}.
 *
 * @author Jay
 */
public class InitiativeTransferHandler extends TransferHandler {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Model containing all of the tokens in this initiative. */
  private InitiativePanel panel;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Logger instance for this class. */
  static final Logger LOGGER = LogManager.getLogger(InitiativeTransferHandler.class);

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /**
   * Create a handler for the passed panel
   *
   * @param aPanel The panel supported by this handler.
   */
  public InitiativeTransferHandler(InitiativePanel aPanel) {
    panel = aPanel;
  }

  /*---------------------------------------------------------------------------------------------
   * Overridden TransferHandler methods
   *-------------------------------------------------------------------------------------------*/

  /**
   * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
   *     java.awt.datatransfer.DataFlavor[])
   */
  @Override
  public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    for (int i = 0; i < transferFlavors.length; i++)
      if (InitiativeTransferable.INIT_TOKEN_FLAVOR.equals(transferFlavors[i])) return true;
    return false;
  }

  /**
   * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
   *     java.awt.datatransfer.Transferable)
   */
  @Override
  public boolean importData(JComponent comp, Transferable t) {
    try {
      if (!t.isDataFlavorSupported(InitiativeTransferable.INIT_TOKEN_FLAVOR)) return false;

      // Get the token and it's current position
      InitiativeList list = panel.getList();
      InitiativeTransferable data =
          (InitiativeTransferable) t.getTransferData(InitiativeTransferable.INIT_TOKEN_FLAVOR);
      JList displayList = (JList) comp;
      int newIndex = displayList.getSelectedIndex();
      if (newIndex == -1) newIndex = list.getSize() - 1;
      if (newIndex > data.getInititiave()) newIndex++;
      list.moveToken(data.getInititiave(), newIndex);
      return true;
    } catch (UnsupportedFlavorException e) {
      LOGGER.warn("Should not happen, I've already checked to make sure it is valid", e);
    } catch (IOException e) {
      LOGGER.warn("Rat bastards changed valid types after I started reading data", e);
    } // entry
    return false;
  }

  /** @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent) */
  @Override
  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  /** @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent) */
  @Override
  protected Transferable createTransferable(JComponent c) {
    JList displayList = (JList) c;
    TokenInitiative ti = (TokenInitiative) displayList.getSelectedValue();
    if (ti == null || ti.getId() == null) return null;
    return new InitiativeTransferable(ti.getId(), panel.getList().indexOf(ti));
  }
}
