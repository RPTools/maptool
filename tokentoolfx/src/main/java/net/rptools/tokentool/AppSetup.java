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
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import javafx.scene.control.TreeItem;
import net.rptools.lib.AppUtil;
import net.rptools.lib.FileUtil;
import net.rptools.lib.image.ImageUtil;

/**
 * Executes only the first time the application is run.
 */
public class AppSetup {
	// private static final String DEFAULT_TOKEN_ZIP = "net/rptools/tokentool/zip/overlays.zip";
	private static final String DEFAULT_OVERLAYS = "net/rptools/tokentool/overlays";

	// https://dzone.com/articles/get-all-classes-within-package

	public static void install(String versionString) {
		AppUtil.init("tokentoolfx");

		File appDir = AppUtil.getAppHome();
		File overlayDir = AppConstants.OVERLAY_DIR;
		File[] overLays = overlayDir.listFiles(ImageUtil.SUPPORTED_IMAGE_FILE_FILTER); // TODO Does this search subdirs? Prob not
		File overlayVer = new File(appDir.getAbsolutePath() + "/version.txt");

		// Only init once or if version.text is missing
		// After 1.4.0.1 we can install only newer overlays based on version if needed
		// overLays.length is 0 because of sub dirs, FIXME
		try {
			// if (overlayVer.exists() && overLays.length > 0) {
			if (overlayVer.exists()) {
				// return;
			} else if (!overlayVer.exists()) {
				// overlayVer.createNewFile();
				FileUtils.writeStringToFile(overlayVer, versionString);
			}

			if (overLays.length > 0) {
				File backupDir = new File(overlayDir.getAbsolutePath() + "/Backup");
				System.out.println("Log: Backing up existing overlays to " + backupDir.getAbsolutePath());
				for (File file : overLays)
					FileUtils.moveFileToDirectory(file, backupDir, true);
			}

			// Put in a default samples
			// FileUtil.unzip(DEFAULT_TOKEN_ZIP, overlayDir);

			installDefaultOverlays();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public static void installDefaultOverlays() throws IOException {
		// Create the overlay directory
		File overlayDir = AppConstants.OVERLAY_DIR;
		overlayDir.mkdirs();

		// Put in a default samples
		// FileUtil.unzip(DEFAULT_TOKEN_ZIP, overlayDir);

		// Copy default overlays from resources
		Reflections reflections = new Reflections(DEFAULT_OVERLAYS, new ResourcesScanner());
		Set<String> resourcePathSet = reflections.getResources(Pattern.compile(".*"));

		for (String resourcePath : resourcePathSet) {
			URL inputUrl = AppSetup.class.getClassLoader().getResource(resourcePath);
			String resourceName = resourcePath.substring(DEFAULT_OVERLAYS.length());

			// System.out.println("resource: " + resource);
			// System.out.println("URL: " + inputUrl);
			// System.out.println("Filename: " + resourceName);
			try {
				FileUtils.copyURLToFile(inputUrl, new File(overlayDir, resourceName));
			} catch (IOException e) {
				System.out.println("ERROR writing " + inputUrl);
				e.printStackTrace();
			}
		}
	}
}
