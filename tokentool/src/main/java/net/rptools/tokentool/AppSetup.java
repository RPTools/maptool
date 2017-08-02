/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.tokentool;

import java.io.File;
import java.io.IOException;

import net.rptools.lib.AppUtil;
import net.rptools.lib.FileUtil;

/**
 * Executes only the first time the application is run.
 */
public class AppSetup {

	private static final String DEFAULT_TOKEN_ZIP = "net/rptools/tokentool/image/overlay/overlays.zip";

	public static void install() {

		AppUtil.init("tokentool");

		File appDir = AppUtil.getAppHome();

		// Only init once
		if (appDir.listFiles().length > 0) {
			return;
		}

		try {

			installDefaultOverlays();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void installDefaultOverlays() throws IOException {

		// Create the overlay directory
		File overlayDir = AppConstants.OVERLAY_DIR;
		overlayDir.mkdirs();

		// Put in a couple samples
		FileUtil.unzip(DEFAULT_TOKEN_ZIP, overlayDir);
	}
}
