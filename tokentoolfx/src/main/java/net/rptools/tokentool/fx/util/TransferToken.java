package net.rptools.tokentool.fx.util;

import java.io.File;
import java.io.IOException;

import javafx.scene.control.TitledPane;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.TokenTool;

public class TransferToken {
	public static File lastFile = null;

	public static File getTempFileAsToken(boolean asToken, TitledPane saveOptionsPane, String tempFileName) throws IOException {
		final String _extension;

		if (asToken) {
			_extension = AppConstants.DEFAULT_TOKEN_EXTENSION;
		} else {
			_extension = AppConstants.DEFAULT_IMAGE_EXTENSION;
		}

		if (saveOptionsPane.getContent() != null) {
			int dragCounter = TokenTool.getFrame().getControlPanel().getFileNumber();
			String namePrefix = TokenTool.getFrame().getControlPanel().getNamePrefix();
			if (namePrefix == null)
				namePrefix = "token";
			tempFileName = String.format("%s_%04d" + _extension, namePrefix, dragCounter);

		} else {
			//tempTileName = AppConstants.DEFAULT_TOKEN_DRAG_NAME + _extension;

			//			tempFileName = TokenTool.getFrame().getControlPanel().getNamePrefix();
			if (tempFileName == null || tempFileName.isEmpty())
				tempFileName = AppConstants.DEFAULT_TOKEN_DRAG_NAME + _extension;
			tempFileName += _extension;
		}

		tempFileName = System.getProperty("java.io.tmpdir") + tempFileName;

		return new File(tempFileName);
	}
}
