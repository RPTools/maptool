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
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.threads.ThreadExecutionHelper;

/** Class that represents Lib:Token libraries. */
class LibraryToken implements Library {

  /** Prefix for library tokens. */
  private static final String LIBRARY_PREFIX = "lib://";

  /** The name of the property that holds the library version. */
  private static final String LIB_VERSION_PROPERTY_NAME = "libversion";

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
  static boolean handles(String path) {
    return path.toLowerCase().startsWith(LIBRARY_PREFIX);
  }

  /**
   * Returns the {@link Library} representing the lib:token.
   *
   * @param name the name of the lib:token.
   * @return the {@link Library} representing the lib:token.
   */
  static CompletableFuture<Optional<Library>> getLibrary(String name) {
    if (!name.toLowerCase().startsWith(LIBRARY_PREFIX)) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return new ThreadExecutionHelper<Optional<Library>>()
        .runOnSwingThread(() -> Optional.ofNullable(findLibrary(name)));
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
              String version = findLibrary(id).getProperty(LIB_VERSION_PROPERTY_NAME).toString();
              return version != null && version.length() > 0 ? version : LIB_VERSION_UNKNOWN;
            });
  }

  @Override
  public CompletableFuture<Boolean> locationExists(String location) throws IOException {
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
  public CompletableFuture<String> readAsString(String location) throws IOException {
    final var loc = Location.getLocation(location);
    return new ThreadExecutionHelper<String>()
        .runOnSwingThread(
            () -> {
              String text = null;
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
  public CompletableFuture<InputStream> read(String location) throws IOException {
    final var loc = Location.getLocation(location);
    return new ThreadExecutionHelper<InputStream>()
        .runOnSwingThread(
            () -> {
              String text = null;
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

  /**
   * Finds the library token with the specific name.
   *
   * @param name the name of the token to find.
   * @return the library token or {@code null} if it can not be found.
   */
  private static Library findLibrary(String name) {
    for (var zone : MapTool.getCampaign().getZones()) {
      List<Token> tokensFiltered = zone.getTokensFiltered(t -> name.equals(t.getName()));
      if (tokensFiltered.size() > 0) {
        return new LibraryToken(tokensFiltered.get(0).getId());
      }
    }
    return null;
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

    throw new LibraryNotValidException();
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

    var prop = token.getMacro(name, true);
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

    if (!token.getPropertyNames().contains(name)) {
      return null;
    } else {
      return token.getProperty(name).toString();
    }
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
    static Location getLocation(String location) throws IOException {

      String[] vals = location.replaceFirst("/*", "").split("/", 2);
      if (vals.length < 2) {
        throw new IOException("Invalid Location");
      }

      LocationType locType;
      try {
        locType = LocationType.valueOf(vals[0].toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new IOException("Invalid Location");
      }

      return new Location(locType, vals[1]);
    }
  }
}
