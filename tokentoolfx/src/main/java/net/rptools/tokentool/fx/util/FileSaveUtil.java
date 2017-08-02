/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool.fx.util;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import net.rptools.lib.FileUtil;
import net.rptools.tokentool.AppConstants;
import net.rptools.tokentool.TokenTool;

public class FileSaveUtil {
	private File lastFile = null;

	public File getTempFileName(boolean asToken, boolean useNumbering, String tempFileName,
			TextField fileNameSuffix) throws IOException {

		return new File(System.getProperty("java.io.tmpdir"), getFileName(asToken, useNumbering, tempFileName, fileNameSuffix).getName());
	}

	public File getFileName(boolean asToken, boolean useNumbering, String tempFileName, TextField fileNameSuffix)
			throws IOException {
		final String _extension;

		System.out.println("lastFile = " + lastFile);

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
				tempFileName = AppConstants.DEFAULT_TOKEN_NAME;

			if (lastFile != null) {
				return new File(lastFile.getParent(), String.format("%s_%04d" + _extension, tempFileName, dragCounter));
			} else {
				return new File(String.format("%s_%04d" + _extension, tempFileName, dragCounter));
			}
		} else {
			if (lastFile != null)
				// tempFileName = FileUtil.getNameWithoutExtension(lastFile);

				if (tempFileName.isEmpty())
					tempFileName = AppConstants.DEFAULT_TOKEN_NAME + _extension;

			if (!tempFileName.endsWith(_extension))
				tempFileName += _extension;

			if (lastFile != null)
				lastFile = new File(lastFile.getParent(), tempFileName);
			else
				lastFile = new File(tempFileName);

			return lastFile;
		}
	}

	public File getLastFile() {
		return lastFile;
	}

	public void setLastFile(File lastFile) {
		this.lastFile = lastFile;
	}
}
