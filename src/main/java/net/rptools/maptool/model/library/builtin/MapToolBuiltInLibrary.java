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
package net.rptools.maptool.model.library.builtin;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.MTScriptMacroInfo;
import net.rptools.maptool.model.library.builtin.themecss.ThemeCssContext;
import net.rptools.maptool.model.library.data.LibraryData;
import net.rptools.maptool.util.HandlebarsUtil;

public class MapToolBuiltInLibrary implements BuiltInLibrary {

  private record ResourceDetails(
      String name, String path, Function<ResourceDetails, String> supplier, boolean cache) {
    public ResourceDetails(String name, String path) {
      this(name, path, null, false);
    }
  }

  private final Map<String, ResourceDetails> resourcesMap =
      Map.of(
          "css/mt-theme.css",
          new ResourceDetails(
              "MT Theme",
              "/net/rptools/maptool/library/builtin/mt-theme-css.hbs",
              rd -> processHandlebarsTemplate(rd),
              true),
          "css/mt-stat-sheet.css",
          new ResourceDetails(
              "MT Theme",
              "/net/rptools/maptool/library/builtin/mt-stat-sheet-css.hbs",
              rd -> processHandlebarsTemplate(rd),
              true));

  private String processHandlebarsTemplate(ResourceDetails rd) {
    try (var cssTemplateIs = MapToolBuiltInLibrary.class.getResourceAsStream(rd.path())) {
      var cssTemplate = new String(cssTemplateIs.readAllBytes());
      var css = new HandlebarsUtil<>(cssTemplate).apply(new ThemeCssContext());
      System.out.println(css);
      return css;
    } catch (IOException e) {
      MapTool.showError(I18N.getText("msg.error.parsing.handlebars", rd.path()), e);
      return "";
    }
  }

  private final Map<String, String> cache = new ConcurrentHashMap<>();

  private final String name = "MapTool Built-In Library";
  private final String namespace = "net.rptools.maptool";
  private final String version = "1.0.0";

  private final String website = "https://www.rptools.net";

  private final String gitUrl = "https://github.com/RPTools/maptool";

  private final String[] authors = new String[] {"RPTools Team"};

  private final String license = "AGPLv3";

  private final String description = "MapTool Built-In Library";

  private final String shortDescription = "MapTool Built-In Library";

  private final boolean allowsUriAccess = true;

  private final String readMeFile = "";

  private final String licenseFile = "";

  private final String[] tags = new String[] {};

  @Override
  public CompletableFuture<String> getVersion() {
    return CompletableFuture.completedFuture(version);
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    var key = location.getPath().replaceFirst("^/", "");
    return CompletableFuture.completedFuture(resourcesMap.containsKey(key));
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    var path = location.getPath().replaceFirst("^/", "");
    if (resourcesMap.containsKey(path)) {
      return CompletableFuture.completedFuture(readAsString(resourcesMap.get(path)));
    } else {
      throw new IOException("Resource not found: " + location);
    }
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    var path = location.getPath().replaceFirst("^/", "");
    if (resourcesMap.containsKey(path)) {
      return CompletableFuture.completedFuture(read(resourcesMap.get(path)));
    } else {
      throw new IOException("Resource not found: " + location);
    }
  }

  private String readAsString(ResourceDetails details) throws IOException {
    if (details.supplier != null) {
      if (details.cache) {
        return cache.computeIfAbsent(details.path, k -> details.supplier.apply(details));
      } else {
        return details.supplier.apply(details);
      }
    } else {
      return new String(read(details).readAllBytes());
    }
  }

  public InputStream read(ResourceDetails details) throws IOException {
    if (details.supplier != null) {
      return new ByteArrayInputStream(readAsString(details).getBytes());
    } else {
      return MapToolBuiltInLibrary.class.getResourceAsStream(details.path());
    }
  }

  @Override
  public CompletableFuture<String> getWebsite() {
    return CompletableFuture.completedFuture(namespace);
  }

  @Override
  public CompletableFuture<String> getGitUrl() {
    return CompletableFuture.completedFuture(gitUrl);
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return CompletableFuture.completedFuture(authors);
  }

  @Override
  public CompletableFuture<String> getLicense() {
    return CompletableFuture.completedFuture(license);
  }

  @Override
  public CompletableFuture<String> getNamespace() {
    return CompletableFuture.completedFuture(namespace);
  }

  @Override
  public CompletableFuture<String> getName() {
    return CompletableFuture.completedFuture(name);
  }

  @Override
  public CompletableFuture<String> getDescription() {
    return CompletableFuture.completedFuture(description);
  }

  @Override
  public CompletableFuture<String> getShortDescription() {
    return CompletableFuture.completedFuture(shortDescription);
  }

  @Override
  public CompletableFuture<Boolean> allowsUriAccess() {
    return CompletableFuture.completedFuture(allowsUriAccess);
  }

  @Override
  public CompletableFuture<LibraryInfo> getLibraryInfo() {
    return CompletableFuture.completedFuture(
        new LibraryInfo(
            name,
            namespace,
            version,
            website,
            gitUrl,
            authors,
            license,
            description,
            shortDescription,
            allowsUriAccess,
            readMeFile.isEmpty() ? null : readMeFile,
            licenseFile.isEmpty() ? null : licenseFile));
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName) {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<List<String>> getAllFiles() {
    return CompletableFuture.completedFuture(resourcesMap.keySet().stream().toList());
  }

  @Override
  public CompletableFuture<LibraryData> getLibraryData() {
    return CompletableFuture.completedFuture(new BuiltInLibraryData(this));
  }

  @Override
  public CompletableFuture<Optional<String>> getLegacyEventHandlerName(String eventName) {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Optional<Token>> getAssociatedToken() {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public boolean canMTScriptAccessPrivate(MapToolMacroContext context) {
    return false;
  }

  @Override
  public CompletableFuture<Optional<Asset>> getReadMeAsset() {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Optional<Asset>> getLicenseAsset() {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public void cleanup() {}
}
