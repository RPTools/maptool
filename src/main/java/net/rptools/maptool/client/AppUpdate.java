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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.jar.*;
import javax.swing.*;
import net.rptools.lib.FileUtil;
import net.rptools.lib.ModelVersionManager;
import net.rptools.lib.OsDetection;
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

  private static final String GIT_HUB_LATEST_RELEASE = "github.api.releases.latest";

  public record ReleaseInfo(String id, String version, URL assetUrl, long assetSize) {}

  public static CompletionStage<Optional<ReleaseInfo>> getLatestRelease() {
    return getLatestReleaseJson()
        .thenApply(
            latestRelease -> {
              if (!latestRelease.has("id") || !latestRelease.has("tag_name")) {
                throw new RuntimeException(
                    "Github payload missing required fields. Aborting update check.");
              }

              String latestReleaseId = latestRelease.get("id").getAsString();
              String latestReleaseVersion = latestRelease.get("tag_name").getAsString();
              if (StringUtils.isBlank(latestReleaseVersion)) {
                throw new RuntimeException(
                    "Unable to detect latest version from GitHub payload. Aborting comparison.");
              }

              // Look for the default extension for each platform.
              // Ideally, we could even detect the current installation method and find a matching
              // asset, but for now we just hardcode one option for each platform.
              String downloadExtension;
              if (OsDetection.WINDOWS) {
                downloadExtension = ".exe";
              } else if (OsDetection.MAC_OS_X) {
                downloadExtension = ".pkg";
              } else {
                downloadExtension = ".deb";
              }

              JsonArray releaseAssets = latestRelease.get("assets").getAsJsonArray();
              for (JsonElement elem : releaseAssets) {
                JsonObject asset = elem.getAsJsonObject();
                if (!asset.has("name")) {
                  continue;
                }

                String assetName = asset.get("name").getAsString();
                log.info("Asset: {}", assetName);

                if (!assetName.toLowerCase().endsWith(downloadExtension)) {
                  continue;
                }

                JsonElement assetDownloadElem = asset.get("browser_download_url");
                JsonElement assetSizeElem = asset.get("size");
                if (assetDownloadElem == null || assetSizeElem == null) {
                  continue;
                }

                String assetDownloadURL = assetDownloadElem.getAsString();
                final long assetDownloadSize = assetSizeElem.getAsLong();
                log.info("Download URL: {}", assetDownloadURL);

                URL url;
                try {
                  url = new URI(assetDownloadURL).toURL();
                } catch (URISyntaxException | MalformedURLException e) {
                  log.warn("Invalid asset download URL.", e);
                  continue;
                }

                return Optional.of(
                    new ReleaseInfo(latestReleaseId, latestReleaseVersion, url, assetDownloadSize));
              }

              return Optional.empty();
            });
  }

  /**
   * Look for a newer version of MapTool. If a newer release is found and the AppPreferences tell us
   * the update should not be ignored, return the information for the release.
   *
   * @return The release information for the latest release, if it is newer. If there is no newer
   *     release, {@code Optional.empty()}.
   */
  public static CompletionStage<Optional<ReleaseInfo>> autoUpdateCheck() {
    if (MapTool.isDevelopment()) {
      log.info("Development build. Skipping update check");
      return CompletableFuture.completedStage(Optional.empty());
    }
    if (AppPreferences.skipAutoUpdate.get()) {
      log.info("Automatic updates have been disabled via preferences. Skipping update check");
      return CompletableFuture.completedStage(Optional.empty());
    }

    String runningVersion = getImplementationVersion();
    if (StringUtils.isBlank(runningVersion)) {
      log.info("Blank implementation version detected, not checking for updates.");
      return CompletableFuture.completedStage(Optional.empty());
    }

    return getLatestRelease()
        .thenApply(
            latestReleaseOptional -> {
              if (latestReleaseOptional.isEmpty()) {
                log.info("Did not find a latest release asset");
                return Optional.empty();
              }

              var latestRelease = latestReleaseOptional.get();
              if (AppPreferences.skipAutoUpdateRelease.get().equals(latestRelease.id())) {
                log.info("Release {} skipped by user request", latestRelease.version());
                return Optional.empty();
              }
              if (!ModelVersionManager.isBefore(runningVersion, latestRelease.version())) {
                log.info(
                    "Skipping release {} as it is not newer than the current version {}",
                    latestRelease.version(),
                    runningVersion);
                return Optional.empty();
              }

              return latestReleaseOptional;
            });
  }

  /**
   * Get the release info from the GitHub {@code /releases/latest} endpoint. The GitHub API docs
   * define "latest" as:
   *
   * <blockquote>
   *
   * … the most recent non-prerelease, non-draft release, sorted by the {@code created_at}
   * attribute. The {@code created_at} attribute is the date of the commit used for the release, and
   * not the date when the release was drafted or published.
   *
   * </blockquote>
   *
   * @return a JsonObject representing the latest release, if one could be retrieved
   */
  private static CompletionStage<JsonObject> getLatestReleaseJson() {
    var future = new CompletableFuture<JsonObject>();
    ForkJoinPool.commonPool()
        .submit(
            () -> {
              String strURL = getProperty(GIT_HUB_LATEST_RELEASE);
              try {
                Request request = new Request.Builder().url(strURL).build();
                Response response = new OkHttpClient().newCall(request).execute();
                String bodyStr = response.body().string();
                future.complete(JsonParser.parseString(bodyStr).getAsJsonObject());
              } catch (IOException e) {
                log.error("Unable to reach {}", strURL, e);
                future.completeExceptionally(e);
              } catch (IllegalStateException e) {
                log.error("Error parsing JSON response from releases/latest.", e);
                future.completeExceptionally(e);
              } catch (Throwable e) {
                log.error("Unexpected error while getting release info", e);
                future.completeExceptionally(e);
              }
            });
    return future.orTimeout(5, TimeUnit.SECONDS);
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

  public static boolean confirmUpdate(ReleaseInfo releaseInfo, boolean showSkipOptions) {
    String releaseId = releaseInfo.id();
    String tagName = releaseInfo.version();

    String title = I18N.getText("Update.title");
    String msg1 = I18N.getText("Update.msg1");
    String msg2 = I18N.getText("Update.msg2", tagName);

    JCheckBox dontAskCheckbox = new JCheckBox(I18N.getText("Update.chkbox"));

    Object[] msgContent, options;
    if (showSkipOptions) {
      msgContent = new Object[] {msg1, msg2, "", dontAskCheckbox};
      options =
          new Object[] {
            I18N.getText("Button.yes"), I18N.getText("Button.no"), I18N.getText("Update.button")
          };
    } else {
      msgContent = new Object[] {msg1, msg2, ""};
      options = new Object[] {I18N.getText("Button.yes"), I18N.getText("Button.no")};
    }

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

    if (showSkipOptions) {
      if (dontAsk) {
        AppPreferences.skipAutoUpdate.set(true);
      }
      if (result == JOptionPane.CANCEL_OPTION) {
        AppPreferences.skipAutoUpdateRelease.set(releaseId);
      }
    }

    return (result == JOptionPane.YES_OPTION);
  }

  public static void downloadFile(URL assetDownloadURL, long assetDownloadSize) {
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
    final File saveLocation = chosenLocation;

    log.info("URL: {}", assetDownloadURL);
    log.info("assetDownloadSize: {}", assetDownloadSize);

    Runnable updatethread =
        () -> {
          try (InputStream stream = assetDownloadURL.openStream()) {
            ProgressMonitorInputStream pmis =
                new ProgressMonitorInputStream(
                    MapTool.getFrame(), I18N.getText("Update.downloading"), stream);
            UIManager.put("ProgressMonitor.progressText", I18N.getText("Update.downloadingTitle"));

            ProgressMonitor pm = pmis.getProgressMonitor();
            pm.setMillisToDecideToPopup(500);
            pm.setMillisToPopup(500);
            pm.setNote(assetDownloadURL.toString());
            pm.setMinimum(0);
            pm.setMaximum((int) assetDownloadSize);

            FileUtils.copyInputStreamToFile(pmis, saveLocation);
          } catch (InterruptedIOException e) {
            // Progress monitor was canceled. This is not an error, unlike other IOExceptions, so
            // ignore it.
            // Don't leave partial files around.
            FileUtil.delete(saveLocation);
          } catch (IOException ioe) {
            // Don't leave potential broken files around..
            FileUtil.delete(saveLocation);
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
