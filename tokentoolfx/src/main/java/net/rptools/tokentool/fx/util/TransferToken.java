package net.rptools.tokentool.fx.util;

import java.io.File;
import java.io.IOException;

import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.TokenTool;

public class TransferToken {
	public static File lastFile = null;

	public static File getTempFileAsToken(boolean asToken, boolean useNumbering, String tempFileName, TextField fileNameSuffix) throws IOException {
		final String _extension;

		if (asToken) {
			_extension = AppConstants.DEFAULT_TOKEN_EXTENSION;
		} else {
			_extension = AppConstants.DEFAULT_IMAGE_EXTENSION;
		}

		if (useNumbering) {
			int dragCounter;
			try {
				dragCounter = Integer.parseInt(fileNameSuffix.getText());
			} catch (NumberFormatException e) {
				dragCounter = 0;
			}
			fileNameSuffix.setText(String.format("%04d", dragCounter + 1));

			if (tempFileName.isEmpty())
				tempFileName = "token";

			tempFileName = String.format("%s_%04d" + _extension, tempFileName, dragCounter);

		} else {
			if (tempFileName.isEmpty())
				tempFileName = AppConstants.DEFAULT_TOKEN_DRAG_NAME + _extension;

			if (!tempFileName.endsWith(_extension))
				tempFileName += _extension;
		}

		return new File(System.getProperty("java.io.tmpdir") + tempFileName);
	}
}
