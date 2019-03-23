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
import net.rptools.maptool.model.GUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Transferable for token identifiers.
 *
 * @author Jay
 */
public class InitiativeTransferable implements Transferable {

  /*---------------------------------------------------------------------------------------------
   * Instance Variables
   *-------------------------------------------------------------------------------------------*/

  /** Transferred id. */
  private GUID id;

  /** The initiative order of the transferred token. Needed for moving duplicate tokens */
  private int inititiave;

  /*---------------------------------------------------------------------------------------------
   * Class Variables
   *-------------------------------------------------------------------------------------------*/

  /** Pass GUIDs around when dragging. */
  public static final DataFlavor INIT_TOKEN_FLAVOR;

  /** Logger instance for this class. */
  private static final Logger LOGGER = LogManager.getLogger(InitiativeTransferable.class);

  /*---------------------------------------------------------------------------------------------
   * Constructors
   *-------------------------------------------------------------------------------------------*/

  /**
   * Build the transferable.
   *
   * @param anId The id of the token being transferred.
   * @param init The index of the token being transferred.
   */
  public InitiativeTransferable(GUID anId, int init) {
    id = anId;
    inititiave = init;
  }

  /** Build the flavors and handle exceptions. */
  static {
    DataFlavor guid = null;
    try {
      guid =
          new DataFlavor(
              DataFlavor.javaJVMLocalObjectMimeType
                  + ";class=net.rptools.maptool.client.ui.tokenpanel.InitiativeTransferable");
    } catch (ClassNotFoundException e) {
      LOGGER.warn(
          "Should never happen since the GUID is a valid class when the classpath is correct.");
    } // endtry
    INIT_TOKEN_FLAVOR = guid;
  }

  /*---------------------------------------------------------------------------------------------
   * Transferable Method Implementations
   *-------------------------------------------------------------------------------------------*/

  /** @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor) */
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    if (INIT_TOKEN_FLAVOR.equals(flavor)) {
      return this;
    }
    InitiativeTransferHandler.LOGGER.warn(
        "Can't support flavor: " + flavor.getHumanPresentableName());
    throw new UnsupportedFlavorException(flavor);
  }

  /** @see java.awt.datatransfer.Transferable#getTransferDataFlavors() */
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] {INIT_TOKEN_FLAVOR};
  }

  /**
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return INIT_TOKEN_FLAVOR.equals(flavor);
  }

  /*---------------------------------------------------------------------------------------------
   * Instance Methods
   *-------------------------------------------------------------------------------------------*/

  /** @return Getter for id */
  public GUID getId() {
    return id;
  }

  /** @return Getter for initiative */
  public int getInititiave() {
    return inititiave;
  }
}
