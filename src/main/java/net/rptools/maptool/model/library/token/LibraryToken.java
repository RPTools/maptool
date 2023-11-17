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
package net.rptools.maptool.model.library.token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroManager.MacroDetails;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryInfo;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.LibraryNotValidException;
import net.rptools.maptool.model.library.LibraryNotValidException.Reason;
import net.rptools.maptool.model.library.MTScriptMacroInfo;
import net.rptools.maptool.model.library.data.LibraryData;
import net.rptools.maptool.util.threads.ThreadExecutionHelper;

/** Class that represents Lib:Token libraries. */
class LibraryToken implements Library {

  /** Name of macro to divert calls to unknown macros on a lib macro to. */
  private static final String UNKNOWN_LIB_MACRO = "!!unknown-macro!!";

  /** "Protocol" for library tokens. */
  private static final String LIBRARY_PROTOCOL = "lib";

  /** The name of the property that holds the library version. */
  private static final String LIB_VERSION_PROPERTY_NAME = "libversion";

  /** The name of the property for the authors. */
  private static final String LIB_AUTHORS_PROPERTY_NAME = "libauthors";

  /** The name of the property for the website. */
  private static final String LIB_WEBSITE_PROPERTY_NAME = "libwebsite";

  /** The name of the property for the git url. */
  private static final String LIB_GITURL_PROPERTY_NAME = "libgiturl";

  /** The name of the property for the license information. */
  private static final String LIB_LICENSE_PROPERTY_NAME = "liblicense";

  /** The name of the property for the description of the library. */
  private static final String LIB_DESCRIPTION_PROPERTY_NAME = "libdescription";

  /** The name of the property for the short description of the library. */
  private static final String LIB_SHORT_DESCRIPTION_PROPERTY_NAME = "libshortdescription";

  /** The version number to return if the lin:token version is unknown. */
  private static final String LIB_VERSION_UNKNOWN = "unknown";

  /** The id of the library token. */
  private final GUID id;

  private final String name;

  private final String namespace;

  private final String version;

  private final List<String> authors;

  private final String website;

  private final String gitUrl;

  private final String license;

  private final String description;

  private final String shortDescription;

  private final boolean allowsUriAccess;

  private final Set<String> propertyNames;

  private final Set<String> macroNames;

  /**
   * Does LibraryToken handle libraries with this path. This does not check that the library exists
   * instead performs a less expensive check to see if its a path it would manage.
   *
   * @param path the path for the library (can be full path or just part of path).
   * @return if the library at the path is handled by the LibraryToken class.
   */
  static boolean handles(URL path) {
    if (path.getProtocol().toLowerCase().startsWith(LIBRARY_PROTOCOL)) {
      return !new LibraryManager().usesReservedPrefix(path.getHost());
    } else {
      return false;
    }
  }

  /**
   * Returns the namespace for a lib token.
   *
   * @param name the name of the lib token.
   * @return the namespace for the lib token.
   */
  static String namespaceForName(String name) {
    return name.substring(4);
  }

  /**
   * Creates a new {@code LibraryToken} for the lib:token id.
   *
   * @note this must be run on the Swing Thread.
   * @param token the token for the lib:token.
   */
  LibraryToken(Token token) {
    id = token.getId();
    name = token.getName();
    namespace = namespaceForName(token.getName());
    version =
        Objects.requireNonNullElse(
                token.getProperty(LIB_VERSION_PROPERTY_NAME), LIB_VERSION_UNKNOWN)
            .toString();
    website =
        Objects.requireNonNullElse(token.getProperty(LIB_WEBSITE_PROPERTY_NAME), "").toString();
    gitUrl = Objects.requireNonNullElse(token.getProperty(LIB_GITURL_PROPERTY_NAME), "").toString();
    license =
        Objects.requireNonNullElse(token.getProperty(LIB_LICENSE_PROPERTY_NAME), "").toString();
    description =
        Objects.requireNonNullElse(token.getProperty(LIB_DESCRIPTION_PROPERTY_NAME), "").toString();
    shortDescription =
        Objects.requireNonNullElse(token.getProperty(LIB_SHORT_DESCRIPTION_PROPERTY_NAME), "")
            .toString();
    authors =
        Arrays.stream(getProperty(LIB_AUTHORS_PROPERTY_NAME, "").split(","))
            .map(String::trim)
            .toList();
    allowsUriAccess = token.getAllowURIAccess();

    propertyNames = new HashSet<>(token.getPropertyNames());
    macroNames = new HashSet<>(token.getMacroNames(false));
  }

  @Override
  public CompletableFuture<String> getVersion() {
    return CompletableFuture.completedFuture(version);
  }

  @Override
  public CompletableFuture<Boolean> locationExists(URL location) throws IOException {
    final var loc = Location.getLocation(location);
    return new ThreadExecutionHelper<Boolean>()
        .runOnSwingThread(
            () -> {
              if (loc.locationType() == LocationType.MACRO) {
                return getMacroText(loc.location()) != null ? Boolean.TRUE : Boolean.FALSE;
              } else {
                return getProperty(loc.location()) != null ? Boolean.TRUE : Boolean.FALSE;
              }
            });
  }

  @Override
  public CompletableFuture<Boolean> isAsset(URL location) {
    return CompletableFuture.completedFuture(Boolean.FALSE);
  }

  @Override
  public CompletableFuture<Optional<MD5Key>> getAssetKey(URL location) {
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<String> readAsString(URL location) throws IOException {
    final var loc = Location.getLocation(location);
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(
            () -> {
              String text;
              if (loc.locationType() == LocationType.MACRO) {
                text = getMacroText(loc.location());
              } else {
                text = getProperty(loc.location());
              }

              if (text == null) {
                throw new IOException("Invalid Location");
              }

              return text;
            });
  }

  @Override
  public CompletableFuture<InputStream> read(URL location) throws IOException {
    final var loc = Location.getLocation(location);
    return new ThreadExecutionHelper<InputStream>()
        .runOnSwingThread(
            () -> {
              String text;
              if (loc.locationType() == LocationType.MACRO) {
                text = getMacroText(loc.location());
              } else {
                text = getProperty(loc.location());
              }

              if (text == null) {
                throw new IOException("Invalid Location");
              }

              return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
            });
  }

  @Override
  public CompletableFuture<String> getWebsite() {
    return CompletableFuture.completedFuture(website);
  }

  @Override
  public CompletableFuture<String> getGitUrl() {
    return CompletableFuture.completedFuture(gitUrl);
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return CompletableFuture.completedFuture(authors.toArray(new String[0]));
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
    String notSet = I18N.getText("library.property.value.notSpecified");
    return CompletableFuture.completedFuture(
        new LibraryInfo(
            name,
            namespace,
            version.isEmpty() ? notSet : version,
            website.isEmpty() ? notSet : website,
            gitUrl.isEmpty() ? notSet : gitUrl,
            authors.size() == 0 ? new String[] {notSet} : authors.toArray(new String[0]),
            license.isEmpty() ? notSet : license,
            description.isEmpty() ? notSet : description,
            shortDescription.isEmpty() ? notSet : shortDescription,
            allowsUriAccess,
            null,
            null));
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName) {
    return new ThreadExecutionHelper<Optional<MTScriptMacroInfo>>()
        .runOnSwingThread(
            () -> {
              Token library = findLibrary(id);
              MacroButtonProperties buttonProps = library.getMacro(macroName, false);
              if (buttonProps == null) {
                // Try the "unknown macro"
                buttonProps = library.getMacro(UNKNOWN_LIB_MACRO, false);
                if (buttonProps == null) {
                  return Optional.empty();
                }
              }

              return Optional.of(
                  new MTScriptMacroInfo(
                      macroName,
                      buttonProps.getCommand(),
                      library.isOwnedByNone() || !buttonProps.getAllowPlayerEdits(),
                      !buttonProps.getAllowPlayerEdits() && buttonProps.getAutoExecute(),
                      buttonProps.getEvaluatedToolTip()));
            });
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
    // There are no private macros in a token library.
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<List<String>> getAllFiles() {
    return CompletableFuture.supplyAsync(
        () -> {
          return Stream.concat(
                  macroNames.stream().map(name -> "macro/" + name),
                  propertyNames.stream().map(name -> "property/" + name))
              .toList();
        });
  }

  @Override
  public CompletableFuture<LibraryData> getLibraryData() {
    return CompletableFuture.completedFuture(new TokenLibraryData(this));
  }

  @Override
  public CompletableFuture<Optional<String>> getLegacyEventHandlerName(String eventName) {
    // For library tokens the legacy event handler name is the same as the event name.
    return CompletableFuture.completedFuture(Optional.of(eventName));
  }

  @Override
  public CompletableFuture<Optional<Token>> getAssociatedToken() {
    return getToken().thenApply(Optional::of);
  }

  @Override
  public boolean canMTScriptAccessPrivate(MapToolMacroContext context) {
    return false; // Library Tokens don't have private data
  }

  @Override
  public CompletableFuture<Optional<Asset>> getReadMeAsset() {
    // Lib:tokens don't support read me files.
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<Optional<Asset>> getLicenseAsset() {
    // Lib:tokens don't support license files.
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public void cleanup() {
    // No cleanup needed
  }

  @Override
  public Set<MacroDetails> getSlashCommands() {
    return Set.of();
  }

  /**
   * @param id the id of the token Lib:Token to get.
   * @return the Token for the library.
   */
  private Token findLibrary(GUID id) {
    for (var zone : MapTool.getCampaign().getZones()) {
      var token = zone.getToken(id);
      if (token != null) {
        return token;
      }
    }

    throw new LibraryNotValidException(
        Reason.MISSING_LIBRARY, I18N.getText("library.error.libtoken.missing"));
  }

  /**
   * Returns the {@link MacroButtonProperties} for the specified macro on the lib:token. Will return
   * null if the macro does not exist.
   *
   * @param name the name of the macro.
   * @return the {@code MacroButtonProperties} for the specified macro.
   */
  private String getMacroText(String name) {
    var token = findLibrary(id);

    var prop = token.getMacro(name, false);
    if (prop == null) {
      return null;
    } else {
      return prop.getCommand();
    }
  }

  /**
   * Returns the property value for the specified name, Will return null if the property does not
   * exist. This method must be run on the swing thread.
   *
   * @param name the name of the property
   * @return the property value for the specified name.
   */
  String getProperty(String name) {
    var token = findLibrary(id);

    Object prop = token.getProperty(name);
    if (prop == null) {
      return null;
    } else {
      return prop.toString();
    }
  }

  /**
   * Returns the property value for the specified name, Will return null if the property does not
   * exist. This method must be run on the swing thread.
   *
   * @param name the name of the property.
   * @param defaultValue the default value to return if the property is null.
   * @return the property value for the specified name.
   */
  String getProperty(String name, String defaultValue) {
    return Objects.requireNonNullElse(getProperty(name), defaultValue);
  }

  /** Enumeration for location types. */
  enum LocationType {
    MACRO,
    PROPERTY
  }

  /** The location propertyType and location. */
  private record Location(LocationType locationType, String location) {

    /**
     * Splits the location string into location propertyType and location.
     *
     * @param location the location string to split.
     * @return the location for the location string.
     * @throws IOException if the location is invalid.
     */
    static Location getLocation(URL location) throws IOException {

      String[] vals = location.getPath().replaceFirst("^/", "").split("/", 2);
      if (vals.length < 2) {
        throw new IOException("Invalid Location");
      }

      LocationType locType;
      try {
        locType = LocationType.valueOf(vals[0].toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IOException("Invalid Location");
      }

      return new Location(locType, URLDecoder.decode(vals[1], StandardCharsets.UTF_8));
    }
  }

  /**
   * Returns the token for the library.
   *
   * @return the token for the library.
   */
  CompletableFuture<Token> getToken() {
    return new ThreadExecutionHelper<Token>().runOnSwingThread(() -> findLibrary(id));
  }

  /**
   * Returns the id of the library token.
   *
   * @note This method is thread safe.
   * @return returns the id of the library token.
   */
  GUID getId() {
    return id;
  }

  /**
   * Checks to see if the library token contains the specified macro.
   *
   * @param name the name of the macro.
   * @return true if the library token contains the specified macro.
   */
  boolean hasMacro(String name) {
    return macroNames.contains(name);
  }
}
