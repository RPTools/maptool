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
package net.rptools.maptool.util.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.rptools.maptool.client.AppSetup;
import net.rptools.maptool.client.AppStatePersisted;
import net.rptools.maptool.client.RemoteFileDownloader;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LibraryUtils {
  private static final Logger log = LogManager.getLogger(LibraryUtils.class);

  private LibraryUtils() {}

  private static final URI LIBRARY_BASE_URI = URI.create("https://library.rptools.net/1.3/");
  private static final URI LIBRARY_LIST_URI = LIBRARY_BASE_URI.resolve("listArtPacks");

  public static CompletionStage<String> downloadLibraryList() {
    CompletionStage<URL> urlStage;
    try {
      urlStage = CompletableFuture.completedStage(LIBRARY_LIST_URI.toURL());
    } catch (MalformedURLException e) {
      log.error("Library list URL is not valid: {}", LIBRARY_LIST_URI, e);
      urlStage = CompletableFuture.failedStage(e);
    }
    return urlStage
        .thenComposeAsync(
            url -> {
              URLConnection conn;
              try {
                conn = url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                // Send the request.
                conn.connect();
                return CompletableFuture.completedStage(conn);
              } catch (IOException e) {
                log.error("Unable to connect to {}", url, e);
                return CompletableFuture.failedStage(e);
              }
            })
        .thenComposeAsync(
            conn -> {
              try (InputStream in = conn.getInputStream()) {
                var list = IOUtils.toString(in, StandardCharsets.UTF_8);
                return CompletableFuture.completedStage(list);
              } catch (IOException e) {
                log.error("Unable to download data from {}", conn.getURL(), e);
                return CompletableFuture.failedStage(e);
              }
            });
  }

  public static List<Library> parseLibraryList(String listAsString) {
    var libraries = new ArrayList<Library>();

    var scanner = new Scanner(listAsString);
    while (scanner.hasNextLine()) {
      var line = scanner.nextLine().trim();
      if (line.isEmpty()) {
        // Nothing of value.
        continue;
      }

      // Negative limit to get trailing blank strings if present.
      String[] data = line.split(Pattern.quote("|"), -1);
      final var requiredParts = 3;
      if (data.length < requiredParts) {
        log.warn(
            "Skipping library with too few components. Expected {} parts, but only found {}",
            requiredParts,
            data.length);
        continue;
      }

      var name = data[0].trim();
      if (name.isEmpty()) {
        log.warn("Skipping library that is missing a name");
        continue;
      }

      URL url;
      var path = data[1].trim();
      if (path.isEmpty()) {
        log.warn("Skipping library with empty path");
        continue;
      }
      try {
        url = LIBRARY_BASE_URI.resolve(data[1].trim()).toURL();
      } catch (IllegalArgumentException | MalformedURLException e) {
        log.warn("Skipping library with invalid URL", e);
        continue;
      }

      long size;
      try {
        size = Long.parseLong(data[2]);
      } catch (NumberFormatException e) {
        log.warn("Skipping library with invalid size", e);
        continue;
      }

      // The author is optional for backwards compatibility.
      @Nullable String author;
      if (data.length < 4) {
        author = null;
      } else {
        author = data[3].trim();
        if (author.isEmpty()) {
          author = null;
        }
      }

      libraries.add(new Library(name, url, size, author));
    }

    return libraries;
  }

  public static CompletionStage<DownloadResult> downloadAndInstall(List<Library> libraries) {
    return CompletableFuture.completedStage(libraries)
        .thenComposeAsync(
            libraries2 -> {
              var failures = new ArrayList<Library>();
              var successes = new ArrayList<Library>();

              var assetRoots =
                  AppStatePersisted.getAssetRoots().stream().map(File::getName).toList();

              var failed = false;
              for (var library : libraries2) {
                if (assetRoots.contains(library.name())) {
                  // Library already exists. Skip it to avoid duplicates.
                  log.info("Skipping already-installed library: {}", library.name());
                  continue;
                }

                try {
                  RemoteFileDownloader downloader = new RemoteFileDownloader(library.location());
                  File tmpFile = downloader.read();
                  AppSetup.installLibrary(library.name(), tmpFile.toURI().toURL());
                  tmpFile.delete();
                } catch (IOException e) {
                  log.error("Error downloading library: {}", e, e);
                  failed = true;
                }

                (failed ? failures : successes).add(library);
              }

              return CompletableFuture.completedStage(new DownloadResult(successes, failures));
            });
  }

  public record DownloadResult(List<Library> successes, List<Library> failures) {}
}
