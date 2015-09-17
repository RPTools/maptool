/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.client.ui.tokenpanel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.rptools.maptool.model.GUID;

/**
 * Transferable for token identifiers.
 * 
 * @author Jay
 */
public class InitiativeTransferable implements Transferable {

	/*---------------------------------------------------------------------------------------------
	 * Instance Variables 
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Transferred id.
	 */
	private GUID id;

	/**
	 * The initiative order of the transferred token. Needed for moving duplicate tokens
	 */
	private int inititiave;

	/*---------------------------------------------------------------------------------------------
	 * Class Variables 
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * Pass GUIDs around when dragging.
	 */
	public static final DataFlavor INIT_TOKEN_FLAVOR;

	/**
	 * Logger instance for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(InitiativeTransferable.class.getName());

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

	/**
	 * Build the flavors and handle exceptions.
	 */
	static {
		DataFlavor guid = null;
		try {
			guid = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=net.rptools.maptool.client.ui.tokenpanel.InitiativeTransferable");
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.WARNING, "Should never happen since the GUID is a valid class when the classpath is correct.");
		} // endtry
		INIT_TOKEN_FLAVOR = guid;
	}

	/*---------------------------------------------------------------------------------------------
	 * Transferable Method Implementations
	 *-------------------------------------------------------------------------------------------*/

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (INIT_TOKEN_FLAVOR.equals(flavor)) {
			return this;
		}
		InitiativeTransferHandler.LOGGER.warning("Can't support flavor: " + flavor.getHumanPresentableName());
		throw new UnsupportedFlavorException(flavor);
	}

	/**
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { INIT_TOKEN_FLAVOR };
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
