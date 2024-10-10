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
package net.rptools.maptool.client;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.*;
import javax.swing.*;
import net.rptools.lib.ModelVersionManager;
import net.rptools.maptool.language.I18N;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppUpdate {
  private static final Logger log = LogManager.getLogger(AppUpdate.class);

  private static final String GIT_HUB_RELEASES = "github.api.releases";
  private static final String GIT_HUB_LATEST_RELEASE = "github.api.releases.latest";
  private static final String GIT_HUB_OAUTH_TOKEN =
      "github.api.oauth.token"; // Grants read-only access to public information

  /**
   * Look for a newer version of MapTool. If a newer release is found and the AppPreferences tell us
   * the update should not be ignored, give a prompt to update.
   *
   * @return has an update been made
   */
  public static boolean gitHubReleases() {
    // AppPreferences.setSkipAutoUpdate(false); // For testing only
    if (AppPreferences.skipAutoUpdate.get()) {
      return false;
    }

    // Default for Linux?
    String DOWNLOAD_EXTENSION = ".deb";

    if (AppUtil.WINDOWS) DOWNLOAD_EXTENSION = ".exe";
    else if (AppUtil.MAC_OS_X) DOWNLOAD_EXTENSION = ".pkg"; // Better default than .dmg?

    String runningVersion = getImplementationVersion();
    if (StringUtils.isBlank(runningVersion)) {
      log.info("Blank implementation version detected, not checking for updates.");
      return false;
    }

    Optional<JsonObject> latestReleaseOpt = getLatestReleaseInfo();
    if (latestReleaseOpt.isEmpty()) return false;
    JsonObject latestRelease = latestReleaseOpt.get();
    if (!latestRelease.has("id") || !latestRelease.has("tag_name")) {
      log.info("Github payload missing required fields??? Aborting update check.");
      return false;
    }
    String latestReleaseId = latestRelease.get("id").getAsString();
    String latestReleaseVersion = latestRelease.get("tag_name").getAsString();
    if (StringUtils.isBlank(latestReleaseVersion)) {
      log.info("Unable to detect latest version from GitHub payload, aborting comparison.");
      return false;
    }

    if (!AppPreferences.skipAutoUpdateRelease.get().equals(latestReleaseId)
        && ModelVersionManager.isBefore(runningVersion, latestReleaseVersion)) {
      JsonArray releaseAssets = latestRelease.get("assets").getAsJsonArray();

      for (JsonElement elem : releaseAssets) {
        JsonObject asset = elem.getAsJsonObject();
        String assetName = asset.get("name").getAsString();
        log.info("Asset: {}", assetName);

        if (assetName.toLowerCase().endsWith(DOWNLOAD_EXTENSION)) {
          JsonElement assetDownloadElem = asset.get("browser_download_url");
          JsonElement assetSizeElem = asset.get("size");
          if (assetDownloadElem != null && assetSizeElem != null) {
            String assetDownloadURL = assetDownloadElem.getAsString();
            final long assetDownloadSize = assetSizeElem.getAsLong();
            log.info("Download URL: {}", assetDownloadURL);
            try {
              URL url = new URL(assetDownloadURL);
              SwingUtilities.invokeLater(
                  () -> {
                    if (showMessage(latestReleaseId, latestReleaseVersion))
                      downloadFile(url, assetDownloadSize);
                  });
            } catch (MalformedURLException e) {
              log.error("Error with download URL.", e);
            }
            return true;
          }
        }
      }
    }

    return false;
  }

  /**
   * Get the String containing the list of the releases and pre-releases from github.
   *
   * @return the String with the list of releases, or null if IOException
   */
  private static String getReleases() {
    String strURL = getProperty(GIT_HUB_RELEASES);
    String strRequest = strURL + getProperty(GIT_HUB_OAUTH_TOKEN);
    try {
      Request request = new Request.Builder().url(strRequest).build();
      Response response = new OkHttpClient().newCall(request).execute();
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

      String responseBody = response.body().string();
      log.debug("GitHub API Response: " + responseBody);
      return responseBody;
    } catch (IOException e) {
      log.error("Unable to reach " + strURL, e.getLocalizedMessage());
      return null;
    }
  }

  /**
   * Get the release info from the GitHub /releases/latest endpoint. The GitHub API docs define
   * "latest" as:
   *
   * <blockquote>
   *
   * ...the most recent non-prerelease, no-draft release, sorted by the `created_at` attribute. The
   * `created_at` attribute is the date of the commit used for the release, and not the date when
   * the release was drafted or published.
   *
   * </blockquote>
   *
   * @return a JsonObject representing the latest release, if one could be retrieved
   */
  private static Optional<JsonObject> getLatestReleaseInfo() {
    String strURL = getProperty(GIT_HUB_LATEST_RELEASE);
    try {
      Request request = new Request.Builder().url(strURL).build();
      Response response = new OkHttpClient().newCall(request).execute();
      String bodyStr = response.body().string();
      return Optional.of(JsonParser.parseString(bodyStr).getAsJsonObject());
    } catch (IOException e) {
      log.error("Unable to reach {}: {}", strURL, e.getLocalizedMessage());
      return Optional.empty();
    } catch (IllegalStateException e1) {
      log.error("Error parsing JSON response from releases/latest.", e1);
      return Optional.empty();
    }
  }

  /**
   * Get the latest commit SHA from MANIFEST.MF
   *
   * @return the String of the commit SHA, or null if IOException
   */
  @Deprecated
  public static String getCommitSHA() {
    String jarCommit = "";

    ClassLoader cl = MapTool.class.getClassLoader();

    try {
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

  /**
   * Inspects the Manifest to get the Implementation Version of the currently running MapTool
   * application. This Manifest property is set by gradle to the current git tag at build time, so
   * it makes a good value for comparison in release version checks.
   *
   * @return the version of MT currently running (as determined from the Manifest)
   */
  public static String getImplementationVersion() {
    String version;
    ClassLoader cl = MapTool.class.getClassLoader();
    try {
      URL url = cl.getResource("META-INF/MANIFEST.MF");
      Manifest manifest = new Manifest(url.openStream());

      Attributes attr = manifest.getMainAttributes();
      version = attr.getValue("Implementation-Version");
      log.info("Implementation-Version from Manifest: " + version);
      version = ModelVersionManager.cleanVersionNumber(version);
      log.info("Cleaned version: " + version);
    } catch (IOException e) {
      log.error(
          "No Implementation-Version attribute found in MANIFEST.MF, skip looking for updates...",
          e);
      return null;
    }
    return version;
  }

  private static boolean showMessage(String releaseId, String tagName) {
    JCheckBox dontAskCheckbox = new JCheckBox(I18N.getText("Update.chkbox"));

    String title = I18N.getText("Update.title");
    String msg1 = I18N.getText("Update.msg1");
    String msg2 = I18N.getText("Update.msg2", tagName);
    String blankLine = " ";

    Object[] msgContent = {msg1, msg2, blankLine, dontAskCheckbox};
    Object[] options = {
      I18N.getText("Button.yes"), I18N.getText("Button.no"), I18N.getText("Update.button")
    };
    int result =
        JOptionPane.showOptionDialog(
            MapTool.getFrame(),
            msgContent,
            title,
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1]);
    boolean dontAsk = dontAskCheckbox.isSelected();

    if (dontAsk) {
      AppPreferences.skipAutoUpdate.set(true);
    }

    if (result == JOptionPane.CANCEL_OPTION) AppPreferences.skipAutoUpdateRelease.set(releaseId);

    return (result == JOptionPane.YES_OPTION);
  }

  private static void downloadFile(URL assetDownloadURL, long assetDownloadSize) {
    final JFileChooser chooser = MapTool.getFrame().getSaveFileChooser();
    chooser.setSelectedFile(new File(assetDownloadURL.getFile()));

    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    File chosenLocation = null;
    while (chosenLocation == null) {
      // Last chance to "cancel" but canceling out of JFileChooser
      if (chooser.showSaveDialog(MapTool.getFrame()) != JFileChooser.APPROVE_OPTION) {
        return;
      }

      chosenLocation = chooser.getSelectedFile();
      try {
        boolean newFile = chosenLocation.createNewFile();
        if (!newFile) {
          MapTool.showError(I18N.getText("msg.error.fileAlreadyExists", chosenLocation));
          chosenLocation = null;
        }
      } catch (IOException ioe) {
        MapTool.showError(I18N.getText("msg.error.directoryNotWriteable", chosenLocation));
        chosenLocation = null;
      }
    }
    final File saveLocation = chooser.getSelectedFile();

    log.info("URL: " + assetDownloadURL.toString());
    log.info("assetDownloadSize: " + assetDownloadSize);

    Runnable updatethread =
        () -> {
          try (InputStream stream = assetDownloadURL.openStream()) {
            ProgressMonitorInputStream pmis =
                new ProgressMonitorInputStream(MapTool.getFrame(), "Downloading...\n", stream);
            UIManager.put("ProgressMonitor.progressText", "New Update");

            ProgressMonitor pm = pmis.getProgressMonitor();
            pm.setMillisToDecideToPopup(500);
            pm.setMillisToPopup(500);
            pm.setNote(assetDownloadURL.toString());
            pm.setMinimum(0);
            pm.setMaximum((int) assetDownloadSize);

            FileUtils.copyInputStreamToFile(pmis, saveLocation);
          } catch (IOException ioe) {
            MapTool.showError("msg.error.failedSavingNewVersion", ioe);
          }
        };

    new Thread(updatethread).start();
  }

  private static String getProperty(String propertyName) {
    Properties prop = new Properties();

    try {
      prop.load(AppUpdate.class.getClassLoader().getResourceAsStream("github.properties"));

      return prop.getProperty(propertyName);
    } catch (IOException ioe) {
      log.error("Unable to load github.properties.", ioe);
    }

    return "";
  }
}
