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
package net.rptools.maptool.model.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.framework.LibraryNotValidException.Reason;
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

  /** The name of the property for the github url. */
  private static final String LIB_GITHUBURL_PROPERTY_NAME = "libgithuburl";

  /** The name of the property for the license information. */
  private static final String LIB_LICENSE_PROPERTY_NAME = "liblicense";

  /** The name of the property for the description of the l;ibrary. */
  private static final String LIB_DESCRIPTION_PROPERTY_NAME = "libdescription";

  /** The name of the property for the short description of the l;ibrary. */
  private static final String LIB_SHORT_DESCRIPTION_PROPERTY_NAME = "libshortdescription";

  /** The version number to return if the lin:token version is unknown. */
  private static final String LIB_VERSION_UNKNOWN = "unknown";

  /** The id of the library token. */
  private final GUID id;

  /**
   * Does LibraryToken handles libraries with this path. This does not check that the library exists
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
   * Returns a list of the library tokens.
   *
   * @return list of library tokens
   */
  static CompletableFuture<List<Library>> getLibraries() {
    return new ThreadExecutionHelper<List<Library>>()
        .runOnSwingThread(
            () -> {
              List<Library> tokenList = new ArrayList<>();
              for (var zone : MapTool.getCampaign().getZones()) {
                tokenList.addAll(
                    zone
                        .getTokensFiltered(t -> t.getName().toLowerCase().startsWith("lib:"))
                        .stream()
                        .map(t -> new LibraryToken(t.getId()))
                        .toList());
              }
              return tokenList;
            });
  }

  /**
   * Returns the library for a given namespace.
   *
   * @param namespace the namespace to return the library for.
   * @return the library for the namespace.
   */
  static CompletableFuture<Library> getLibrary(String namespace) {
    return new ThreadExecutionHelper<Library>()
        .runOnSwingThread(
            () -> {
              var tokenList = getTokensWithName("lib:" + namespace);
              if (tokenList.isEmpty()) {
                return null;
              } else {
                return new LibraryToken(tokenList.get(0).getId());
              }
            });
  }

  /**
   * Returns the {@link Library} representing the lib:token.
   *
   * @param path the path of the lib:token.
   * @return the {@link Library} representing the lib:token.
   */
  static CompletableFuture<Optional<Library>> getLibrary(URL path) {
    if (!handles(path)) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return new ThreadExecutionHelper<Optional<Library>>()
        .runOnSwingThread(() -> Optional.ofNullable(findLibrary(path)));
  }

  /**
   * Creates a new {@code LibraryToken} for the lib:token id.
   *
   * @param id the id of the lib:token.
   */
  private LibraryToken(GUID id) {
    this.id = id;
  }

  @Override
  public CompletableFuture<String> getVersion() {
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(
            () -> {
              String version = getProperty(LIB_VERSION_PROPERTY_NAME, LIB_VERSION_UNKNOWN);
              return version.isEmpty() ? LIB_VERSION_UNKNOWN : version;
            });
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
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(() -> getProperty(LIB_WEBSITE_PROPERTY_NAME, ""));
  }

  @Override
  public CompletableFuture<String> getGitHubUrl() {
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(() -> getProperty(LIB_GITHUBURL_PROPERTY_NAME, ""));
  }

  @Override
  public CompletableFuture<String[]> getAuthors() {
    return new ThreadExecutionHelper<String[]>()
        .runOnSwingThread(
            () ->
                Arrays.stream(getProperty(LIB_AUTHORS_PROPERTY_NAME, "").split(","))
                    .map(String::trim)
                    .toArray(String[]::new));
  }

  @Override
  public CompletableFuture<String> getLicense() {
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(() -> getProperty(LIB_LICENSE_PROPERTY_NAME, ""));
  }

  @Override
  public CompletableFuture<String> getNamespace() {
    // For LibTokens the namespace is just the name without the lib:
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(() -> findLibrary(id).getName().substring(4));
  }

  @Override
  public CompletableFuture<String> getName() {
    return new ThreadExecutionHelper<String>().runOnSwingThread(() -> findLibrary(id).getName());
  }

  @Override
  public CompletableFuture<String> getDescription() {
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(() -> getProperty(LIB_DESCRIPTION_PROPERTY_NAME, ""));
  }

  @Override
  public CompletableFuture<String> getShortDescription() {
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(() -> getProperty(LIB_SHORT_DESCRIPTION_PROPERTY_NAME, ""));
  }

  @Override
  public CompletableFuture<Boolean> allowsUriAccess() {
    return new ThreadExecutionHelper<Boolean>()
        .runOnSwingThread((() -> findLibrary(id).getAllowURIAccess()));
  }

  @Override
  public CompletableFuture<LibraryInfo> getLibraryInfo() {
    return new ThreadExecutionHelper<LibraryInfo>()
        .runOnSwingThread(
            () -> {
              Token library = findLibrary(id);
              var authors =
                  Arrays.stream(getProperty(LIB_AUTHORS_PROPERTY_NAME, "").split(","))
                      .map(String::trim)
                      .toArray(String[]::new);
              return new LibraryInfo(
                  library.getName(),
                  library.getName(),
                  getProperty(LIB_VERSION_PROPERTY_NAME),
                  getProperty(LIB_WEBSITE_PROPERTY_NAME),
                  getProperty(LIB_GITHUBURL_PROPERTY_NAME),
                  authors,
                  getProperty(LIB_LICENSE_PROPERTY_NAME),
                  getProperty(LIB_DESCRIPTION_PROPERTY_NAME),
                  getProperty(LIB_SHORT_DESCRIPTION_PROPERTY_NAME),
                  library.getAllowURIAccess());
            });
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
                      macroName, buttonProps.getCommand(), buttonProps.getAllowPlayerEdits()));
            });
  }

  @Override
  public CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName) {
    // There are no private macros in a token library.
    return CompletableFuture.completedFuture(Optional.empty());
  }

  @Override
  public CompletableFuture<List<String>> getAllFiles() {
    return new ThreadExecutionHelper<List<String>>()
        .runOnSwingThread(
            () -> {
              Token library = findLibrary(id);
              List<String> files =
                  new ArrayList<>(
                      library.getMacroList(false).stream()
                          .map(p -> "macro/" + p.getLabel())
                          .toList());
              files.addAll(library.getPropertyNames().stream().map(p -> "property/" + p).toList());

              return files;
            });
  }

  /**
   * Finds the library token with the specific path.
   *
   * @param path the path of the token to find.
   * @return the library token or {@code null} if it can not be found.
   */
  private static Library findLibrary(URL path) {
    String name = "lib:" + path.getHost();
    List<Token> tokenList = getTokensWithName(name);
    if (tokenList.size() > 0) {
      Optional<Token> token = tokenList.stream().filter(Token::getAllowURIAccess).findFirst();
      if (token.isPresent()) {
        return new LibraryToken(token.get().getId());
      } else { // There are some tokens but none with "Allow URI Access"
        throw new LibraryNotValidException(
            Reason.MISSING_PERMISSIONS, I18N.getText("library.error.libtoken.no.access", name));
      }
    }
    return null;
  }

  /**
   * Returns a list of all tokens that match the specified name (case-insensitive)
   *
   * @param name the name to match.
   * @return list of tokens.
   */
  private static List<Token> getTokensWithName(String name) {
    List<Token> tokenList = new ArrayList<Token>();
    for (var zone : MapTool.getCampaign().getZones()) {
      tokenList.addAll(zone.getTokensFiltered(t -> name.equalsIgnoreCase(t.getName())));
    }

    return tokenList;
  }

  /**
   * Returns the {@link Token} for the library.
   *
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
   * exist.
   *
   * @param name the name of the property
   * @return the property value for the specified name.
   */
  private String getProperty(String name) {
    var token = findLibrary(id);

    return token.getProperty(name).toString();
  }

  /**
   * Returns the property value for the specified name, Will return null if the property does not
   * exist.
   *
   * @param name the name of the property.
   * @param defaultValue the default value to return if the property is null.
   * @return the property value for the specified name.
   */
  private String getProperty(String name, String defaultValue) {
    return Objects.requireNonNullElse(getProperty(name), defaultValue);
  }

  /** Enumeration for location types. */
  enum LocationType {
    MACRO,
    PROPERTY
  }

  /** The location type and location. */
  private record Location(LocationType locationType, String location) {

    /**
     * Splits the location string into location type and location.
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
}
