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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.Library;
import net.rptools.maptool.model.library.LibraryManager;
import net.rptools.maptool.model.library.LibraryNotValidException;
import net.rptools.maptool.model.library.LibraryNotValidException.Reason;
import net.rptools.maptool.util.threads.ThreadExecutionHelper;

/** Class that represents Lib:Token libraries. */
public class LibraryTokenManager {

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

  /**
   * Does LibraryToken handle libraries with this path. This does not check that the library exists
   * instead performs a less expensive check to see if its a path it would manage.
   *
   * @param path the path for the library (can be full path or just part of path).
   * @return if the library at the path is handled by the LibraryToken class.
   */
  public boolean handles(URL path) {
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
  public CompletableFuture<List<Library>> getLibraries() {
    return getMatchingLibraryList(null, null);
  }

  /**
   * Returns a list of the library tokens filtered by those that have a given property and macro
   * name. if either the property or macro name is null then it will match all tokens for that
   * filter.
   *
   * @param property the name of the property to match or null to ignore
   * @param macro the name of the macro to match or null to ignore
   * @return list of library tokens that match the filters.
   */
  private CompletableFuture<List<Library>> getMatchingLibraryList(String property, String macro) {
    return new ThreadExecutionHelper<List<Library>>()
        .runOnSwingThread(
            () -> {
              List<Library> tokenList = new ArrayList<>();
              for (var zone : MapTool.getCampaign().getZones()) {
                tokenList.addAll(
                    zone
                        .getTokensFiltered(t -> t.getName().toLowerCase().startsWith("lib:"))
                        .stream()
                        .filter(t -> property == null || t.getProperty(property) != null)
                        .filter(t -> macro == null || t.getMacro(macro, false) != null)
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
  public CompletableFuture<Library> getLibrary(String namespace) {
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
  public CompletableFuture<Optional<Library>> getLibrary(URL path) {
    if (!handles(path)) {
      return CompletableFuture.completedFuture(Optional.empty());
    }

    return new ThreadExecutionHelper<Optional<Library>>()
        .runOnSwingThread(() -> Optional.ofNullable(findLibrary(path)));
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
   * Returns the list of tokens that have handlers for the specified legacy token events.
   *
   * @param eventName the name of the event to match.
   * @return the list of tokens that have handlers for the specified legacy token events.
   */
  public CompletableFuture<List<Library>> getLegacyEventTargets(String eventName) {
    return getMatchingLibraryList(null, eventName);
  }
}
