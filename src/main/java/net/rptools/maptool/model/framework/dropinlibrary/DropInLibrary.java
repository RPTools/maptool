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
package net.rptools.maptool.model.framework.dropinlibrary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Asset.Type;
import net.rptools.maptool.model.AssetManager;
import net.rptools.maptool.model.framework.Library;
import net.rptools.maptool.model.framework.LibraryInfo;
import net.rptools.maptool.model.framework.LibraryNotValidException;
import net.rptools.maptool.model.framework.LibraryNotValidException.Reason;
import net.rptools.maptool.model.framework.MTScriptMacroInfo;
import net.rptools.maptool.model.framework.proto.DropInLibraryDto;
import net.rptools.maptool.model.framework.proto.MTScriptPropertiesDto;
import org.javatuples.Pair;

/** Class that implements drop in libraries. */
public class DropInLibrary implements Library {

  /** Record used to store information about the MacrScript functions for this library. */
  private record MTScript(String path, boolean autoExecute, String description, MD5Key md5Key) {}
  ;

  /** The directory where the files exposed URI are stored. */
  private static final String URL_PUBLIC_DIR = "public/";

  /** The directory where MT MacroScripts are stored. */
  private static final String MTSCRIPT_DIR = "mtscript/";

  /** The directory where public MT MacroScripts are stored. */
  private static final String MTSCRIPT_PUBLIC_DIR = "public/";

  /** The name of the drop in library. */
  private final String name;

  /** The version of the drop in library. */
  private final String version;

  /** The website for the drop in library. */
  private final String website;

  /** The authors for the drop in library. */
  private final String[] authors;

  /** The github url of the drop in library. */
  private final String gitHubUrl;

  /** The license for the drop in library. */
  private final String license;

  /** The namespace for the drop in library. */
  private final String namespace;

  /** The description for the drop in library. */
  private final String description;

  /** The short description for the drop in library. */
  private final String shortDescription;

  /** if the drop in library allows URI access or not. */
  private final boolean allowsUriAccess;

  /** The mapping between paths and asset information. */
  private final Map<String, Pair<MD5Key, Type>> pathAssetMap;

  /** The mapping between url paths and asset information. */
  private final Map<String, Pair<MD5Key, Type>> urlPathAssetMap;

  /** The mapping between MTScript function paths and asset information. */
  private final Map<String, MTScript> mtsFunctionAssetMap;

  /**
   * Class used to represent Drop In Libraries.
   *
   * @param dto The Drop In Libraries Data Transfer Object.
   * @param mtsDto The MTScript Properties Data Transfer Object.
   * @param pathAssetMap mapping of paths in the library to {@link MD5Key}s and {@link Asset.Type}s.
   */
  private DropInLibrary(
      DropInLibraryDto dto,
      MTScriptPropertiesDto mtsDto,
      Map<String, Pair<MD5Key, Asset.Type>> pathAssetMap) {
    Objects.requireNonNull(dto, I18N.getText("library.error.invalidDefinition"));
    name = Objects.requireNonNull(dto.getName(), I18N.getText("library.error.emptyName"));
    version =
        Objects.requireNonNull(dto.getVersion(), I18N.getText("library.error.emptyVersion", name));
    website = Objects.requireNonNullElse(dto.getWebsite(), "");
    authors = dto.getAuthorsList().toArray(String[]::new);
    gitHubUrl = dto.getGithubUrl();
    license = dto.getLicense();
    namespace = dto.getNamespace();
    description = dto.getDescription();
    shortDescription = dto.getShortDescription();
    this.pathAssetMap = Map.copyOf(pathAssetMap);
    allowsUriAccess = dto.getAllowsUriAccess();

    var urlsMap = new HashMap<String, Pair<MD5Key, Type>>();
    var mtsMap = new HashMap<String, MTScript>();

    var autoExecSet = new HashSet<String>();
    var descriptionMap = new HashMap<String, String>();

    for (var properties : mtsDto.getPropertiesList()) {
      var path = MTSCRIPT_DIR + properties.getFilename();
      if (properties.getAutoExecute()) {
        autoExecSet.add(path);
      }

      descriptionMap.put(path, properties.getDescription());
    }

    for (var entry : this.pathAssetMap.entrySet()) {
      String path = entry.getKey();
      if (path.startsWith(URL_PUBLIC_DIR)) {
        urlsMap.put(path.substring(URL_PUBLIC_DIR.length()), entry.getValue());
      } else if (path.startsWith(MTSCRIPT_DIR)) {
        if (path.toLowerCase().endsWith(".mts")) {
          String name = path.substring(MTSCRIPT_DIR.length(), path.length() - 4);
          mtsMap.put(
              name,
              new MTScript(
                  name,
                  autoExecSet.contains(path),
                  descriptionMap.getOrDefault(path, ""),
                  entry.getValue().getValue0()));
        }
      }
    }

    urlPathAssetMap = Collections.unmodifiableMap(urlsMap);
    mtsFunctionAssetMap = Collections.unmodifiableMap(mtsMap);
  }

  /**
   * Creates a new Drop In Library from the given {@link DropInLibraryDto}, {@link
   * MTScriptPropertiesDto}, and file path assets map.
   *
   * @param dto The Drop In Libraries Data Transfer Object.
   * @param mtsDto The MTScript Properties Data Transfer Object.
   * @param pathAssetMap mapping of paths in the library to {@link MD5Key}s and {@link Asset.Type}s.
   * @return
   */
  public static DropInLibrary fromDto(
      DropInLibraryDto dto,
      MTScriptPropertiesDto mtsDto,
      Map<String, Pair<MD5Key, Asset.Type>> pathAssetMap) {
    return new DropInLibrary(dto, mtsDto, pathAssetMap);
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

  /**
   * Returns a list of the library tokens.
   *
   * @return list of library tokens
   */
  @Override
  public CompletableFuture<LibraryInfo> getLibraryInfo() {
    return CompletableFuture.completedFuture(
        new LibraryInfo(
            name,
            namespace,
            version,
            website,
            gitHubUrl,
            authors,
            license,
            description,
            shortDescription,
            allowsUriAccess));
  }

  /**
   * Return a {@link MTScriptMacroInfo} for the macro.
   *
   * @param macroName The name of the macro.
   * @param macro The macro details.
   * @return The {@link MTScriptMacroInfo} details.
   */
  private CompletableFuture<Optional<MTScriptMacroInfo>> getMacroInfo(
      String macroName, MTScript macro) {
    return CompletableFuture.supplyAsync(
        () -> {
          Asset asset = AssetManager.getAsset(macro.md5Key());
          String command = asset.getDataAsString();
          // Drop In Library Functions are always trusted as only GM can add and no one can edit.
          return Optional.of(
              new MTScriptMacroInfo(
                  macroName,
                  command,
                  true, // Drop In Library Functions are always trusted
                  macro.autoExecute(),
                  macro.description()));
        });
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName) {
    var macro = mtsFunctionAssetMap.get(MTSCRIPT_PUBLIC_DIR + macroName);
    if (macro == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return getMacroInfo(macroName, macro);
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
    var macro = mtsFunctionAssetMap.get(macroName);
    if (macro == null) {
      return CompletableFuture.completedFuture(Optional.empty());
    }
    return getMacroInfo(macroName, macro);
  }

  @Override
  public CompletableFuture<List<String>> getAllFiles() {
    return CompletableFuture.completedFuture(new ArrayList<>(pathAssetMap.keySet()));
  }

  @Override
  public CompletableFuture<String> getVersion() {
    return CompletableFuture.completedFuture(version);
  }

  /**
   * Returns the asset information for the specified location, this will take care of the mapping to
   * public/
   *
   * @param location the URI location passed in.
   * @return the asset information for the path, if there is no asset information at that path then
   *     null is returned.
   */
  private Pair<MD5Key, Asset.Type> getURILocation(URL location) {
    return urlPathAssetMap.get(location.getPath().replaceFirst("^/", ""));
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    if (allowsUriAccess) {
      return CompletableFuture.completedFuture(getURILocation(location) != null);
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.dropin.no.access", name));
    }
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    if (allowsUriAccess) {
      var values = getURILocation(location);
      if (values == null) {
        throw new IOException("Invalid Location");
      }
      if (!values.getValue1().isStringType()) {
        throw new LibraryNotValidException(
            Reason.BAD_CONVERSION,
            I18N.getText("library.error.dropin.notText", values.getValue1().name()));
      }
      return CompletableFuture.supplyAsync(
          () -> {
            Asset asset = AssetManager.getAsset(values.getValue0());
            return asset.getDataAsString();
          });
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.dropin.no.access", name));
    }
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    if (allowsUriAccess) {
      var values = getURILocation(location);
      if (values == null) {
        throw new IOException("Invalid Location");
      }
      return CompletableFuture.supplyAsync(
          () -> {
            Asset asset = AssetManager.getAsset(values.getValue0());
            return new ByteArrayInputStream(asset.getData());
          });
    } else {
      throw new LibraryNotValidException(
          Reason.MISSING_PERMISSIONS, I18N.getText("library.error.dropin.no.access", name));
    }
  }

  @Override
  public CompletableFuture<String> getWebsite() {
    return CompletableFuture.completedFuture(website);
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return CompletableFuture.completedFuture(authors);
  }

  @Override
  public CompletableFuture<String> getGitHubUrl() {
    return CompletableFuture.completedFuture(gitHubUrl);
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
}
