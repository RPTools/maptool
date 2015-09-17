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

package net.rptools.maptool.client.ui.macrobuttons.buttons;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class TransferableMacroButton implements Transferable {

	public static final DataFlavor macroButtonFlavor = new DataFlavor(MacroButton.class, "Macro Button");

	//private TokenMacroButton button;
	private TransferData transferData;

	public TransferableMacroButton(MacroButton button) {
		//this.button = button;
		transferData = new TransferData(button);
	}

	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { macroButtonFlavor };
	}

	public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
		return dataFlavor.equals(macroButtonFlavor);
	}

	public Object getTransferData(DataFlavor dataFlavor) throws UnsupportedFlavorException, IOException {
		if (dataFlavor.equals(macroButtonFlavor)) {
			return transferData;
		}

		return null;
	}
}
