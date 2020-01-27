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

import com.jayway.jsonpath.JsonPath;
import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Properties;
import java.util.jar.*;
import javax.swing.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppUpdate {
  private static final Logger log = LogManager.getLogger(AppUpdate.class);

  private static final String GIT_HUB_RELEASES = "github.api.releases";
  private static final String GIT_HUB_OAUTH_TOKEN =
      "github.api.oauth.token"; // Grants read-only access to public information

  /**
   * Look for a newer version of MapTool. If a newer release is found and the AppPreferences tell us
   * the update should not be ignored, give a prompt to update. If current version is a release,
   * update to the most recent release. If the current version is a pre-release, update to the most
   * recent version (pre-release or release).
   *
   * @return has an update been made
   */
  public static boolean gitHubReleases() {
    // AppPreferences.setSkipAutoUpdate(false); // For testing only
    if (AppPreferences.getSkipAutoUpdate()) return false;
    String strURL = getProperty(GIT_HUB_RELEASES);
    String strRequest = strURL + getProperty(GIT_HUB_OAUTH_TOKEN);

    String jarCommit;
    String latestGitHubReleaseCommit;
    String latestGitHubReleaseTagName;

    // Default for Linux?
    String DOWNLOAD_EXTENSION = ".deb";

    if (AppUtil.WINDOWS) DOWNLOAD_EXTENSION = ".exe";
    else if (AppUtil.MAC_OS_X) DOWNLOAD_EXTENSION = ".pkg"; // Better default than .dmg?

    // Get current commit from JAR Manifest
    jarCommit = getCommitSHA();

    // If we don't have a commit attribute from JAR, we're done!
    if (jarCommit == null) {
      log.info("No commit SHA (running in DEVELOPMENT mode?): " + strRequest);
      return false;
    }

    String strReleases = getReleases();
    // If can't access the list of releases, we're done
    if (strReleases == null) return false;

    JSONObject release;
    try {
      // Get pre-release information regarding MapTool version from github list
      String path = "$.[?(@.target_commitish == '" + jarCommit + "')].prerelease";
      List<Boolean> listMatches = JsonPath.parse(strReleases).read(path);
      boolean prerelease = listMatches.isEmpty() || listMatches.get(0);

      if (prerelease) {
        JSONArray releasesList = JSONArray.fromObject(strReleases);
        release = JSONObject.fromObject(releasesList.get(0)); // the latest is at top of list
      } else {
        path = "$.[?(@.prerelease == false)]";
        listMatches = JsonPath.parse(strReleases).read(path); // get sublist of releases
        release = JSONObject.fromObject(listMatches.get(0)); // the latest is at top of list
      }
      latestGitHubReleaseCommit = release.get("target_commitish").toString();
      log.info("target_commitish from GitHub: " + latestGitHubReleaseCommit);
      latestGitHubReleaseTagName = release.get("tag_name").toString();
      log.info("tag_name from GitHub: " + latestGitHubReleaseTagName);
    } catch (Exception e) {
      log.error("Unable to parse JSON payload from GitHub...", e);
      return false;
    }

    // If the commits are the same or we were told to skip this update, we're done!
    if (jarCommit.equals(latestGitHubReleaseCommit)
        || AppPreferences.getSkipAutoUpdateCommit().equals(latestGitHubReleaseCommit)) return false;

    JSONArray releaseAssets = release.getJSONArray("assets");
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
            SwingUtilities.invokeLater(
                () -> {
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
   * Get the latest commit SHA from MANIFEST.MF
   *
   * @return the String of the commit SHA, or null if IOException
   */
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

  private static boolean showMessage(String aTitle, String commit, String tagName) {
    JCheckBox dontAskCheckbox = new JCheckBox("Never check for updates again!");

    String title = "Update Available";
    String msg1 = "A new version of MapTool is available!";
    String msg2 = "Would you like to download " + tagName + "?";
    String blankLine = " ";

    Object[] msgContent = {msg1, msg2, blankLine, dontAskCheckbox};
    Object[] options = {"Yes", "No", "Skip this Version"};
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

    if (dontAsk) AppPreferences.setSkipAutoUpdate(true);

    if (result == JOptionPane.CANCEL_OPTION) AppPreferences.setSkipAutoUpdateCommit(commit);

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

    Runnable updatethread =
        new Runnable() {
          @Override
          public void run() {
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
            } catch (IOException e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
            }
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
