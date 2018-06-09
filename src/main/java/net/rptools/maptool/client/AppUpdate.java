/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client;

import java.io.*;
import java.net.*;
import java.util.jar.*;
import javax.swing.*;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.rptools.maptool.util.HTTPUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class AppUpdate {
	private static final Logger log = LogManager.getLogger(AppUpdate.class);

	// TODO: Move this to a .properties config
	static final String GIT_HUB_API_URL = "https://api.github.com/repos/JamzTheMan/MapTool/releases/latest?access_token=";
	static final String GIT_HUB_OAUTH_TOKEN = "60a5595a0b90510f8d0fbe686e21be6ed1c0b787"; // Grants read-only access to public information

	public static boolean gitHubReleases() {
		// AppPreferences.setSkipAutoUpdate(false); // For testing only
		if (AppPreferences.getSkipAutoUpdate())
			return false;

		String response = null;
		String jarCommit = null;
		String latestGitHubReleaseCommit = "";
		String latestGitHubReleaseTagName = "";

		// Default for Linux?
		String DOWNLOAD_EXTENSION = ".deb";

		if (AppUtil.WINDOWS)
			DOWNLOAD_EXTENSION = ".exe";
		else if (AppUtil.MAC_OS_X)
			DOWNLOAD_EXTENSION = ".pkg"; // Better default than .dmg?

		// Get current commit from JAR Manifest
		jarCommit = getCommitSHA();

		// If we don't have a commit attribute from JAR, we're done!
		if (jarCommit == null)
			return false;

		try {
			response = HTTPUtil.getJsonPaylod(GIT_HUB_API_URL + GIT_HUB_OAUTH_TOKEN);
			log.debug("Response: " + response);
		} catch (IOException e) {
			log.error("Unable to reach " + GIT_HUB_API_URL, e.getLocalizedMessage());
			return false;
		}

		JSONObject releases = new JSONObject();
		try {
			releases = JSONObject.fromObject(response);
			latestGitHubReleaseCommit = releases.get("target_commitish").toString();
			log.info("target_commitish from GitHub: " + latestGitHubReleaseCommit);
			latestGitHubReleaseTagName = releases.get("tag_name").toString();
			log.info("tag_name from GitHub: " + latestGitHubReleaseTagName);
		} catch (Exception e) {
			log.error("Unable to parse JSON payload from GitHub...", e);
			return false;
		}

		// If the commits are the same or we were told to skip this update, we're done!
		if (jarCommit.equals(latestGitHubReleaseCommit) || AppPreferences.getSkipAutoUpdateCommit().equals(latestGitHubReleaseCommit))
			return false;

		JSONArray releaseAssets = releases.getJSONArray("assets");
		String assetDownloadURL = null;
		JSONObject asset;

		for (int i = 0; i < releaseAssets.size(); ++i) {
			asset = releaseAssets.getJSONObject(i);

			log.info("Asset: " + asset.getString("name"));

			if (asset.getString("name").toLowerCase().endsWith(DOWNLOAD_EXTENSION)) {
				assetDownloadURL = asset.getString("browser_download_url");
				final long assetDownloadSize = asset.getLong("size");

				if (assetDownloadURL != null) {
					log.info("Download: " + assetDownloadURL);

					try {
						URL url = new URL(assetDownloadURL);
						String commit = latestGitHubReleaseCommit;
						String tagName = latestGitHubReleaseTagName;
						SwingUtilities.invokeLater(() -> {
							if (showMessage("New Update Found", commit, tagName))
								downloadFile(url, assetDownloadSize);
						});
					} catch (MalformedURLException e) {
						log.error("Error with URL " + assetDownloadURL, e);
					}

					return true;
				}
			}
		}

		return false;
	}

	public static String getCommitSHA() {
		String jarCommit = "";

		// Attempt to get current commit out of JAR Manifest, if null is return, most likely ran from IDE/non-JAR version so skip
		// URLClassLoader cl = (URLClassLoader) MapTool.class.getClassLoader();
		ClassLoader cl = MapTool.class.getClassLoader();

		try {
			// URL url = cl.findResource("META-INF/MANIFEST.MF");
			URL url = cl.getResource("META-INF/MANIFEST.MF");
			Manifest manifest = new Manifest(url.openStream());

			Attributes attr = manifest.getMainAttributes();
			jarCommit = attr.getValue("Git-Commit-SHA");
			log.info("Git-Commit-SHA from Manifest: " + jarCommit);
		} catch (IOException e) {
			log.error("No Git-Commit-SHA attribute found in MANIFEST.MF, skip looking for updates...", e);
			return null;
		}

		return jarCommit;
	}

	private static boolean showMessage(String aTitle, String commit, String tagName) {
		JCheckBox dontAskCheckbox = new JCheckBox("Never check for updates again!");

		String title = "Update Available";
		String msg1 = "A new version of MapTool infused with Nerps is available!";
		String msg2 = "Would you like to download " + tagName + "?";
		String blankLine = " ";

		Object[] msgContent = { msg1, msg2, blankLine, dontAskCheckbox };
		Object[] options = { "Yes", "No", "Skip this Version" };
		int result = JOptionPane.showOptionDialog(MapTool.getFrame(), msgContent, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		boolean dontAsk = dontAskCheckbox.isSelected();

		if (dontAsk)
			AppPreferences.setSkipAutoUpdate(true);

		if (result == JOptionPane.CANCEL_OPTION)
			AppPreferences.setSkipAutoUpdateCommit(commit);

		return (result == JOptionPane.YES_OPTION);
	}

	private static void downloadFile(URL assetDownloadURL, long assetDownloadSize) {
		final JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
		chooser.setSelectedFile(new File(assetDownloadURL.getFile()));

		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Last chance to "cancel" but canceling out of JFileChooser
		if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File saveLocation = chooser.getSelectedFile();

		log.info("URL: " + assetDownloadURL.toString());
		log.info("assetDownloadSize: " + assetDownloadSize);

		Runnable updatethread = new Runnable() {
			public void run() {
				try (InputStream stream = assetDownloadURL.openStream()) {
					ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(MapTool.getFrame(), "Downloading...\n", stream);
					UIManager.put("ProgressMonitor.progressText", "New Update");

					ProgressMonitor pm = pmis.getProgressMonitor();
					pm.setMillisToDecideToPopup(500);
					pm.setMillisToPopup(500);
					pm.setNote(assetDownloadURL.toString());
					pm.setMinimum(0);
					pm.setMaximum((int) assetDownloadSize);

					FileUtils.copyInputStreamToFile(pmis, saveLocation);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};

		new Thread(updatethread).start();
	}
}