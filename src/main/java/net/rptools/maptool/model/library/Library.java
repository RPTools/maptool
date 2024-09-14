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
package net.rptools.maptool.model.library;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.rptools.lib.MD5Key;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.macro.MacroManager.MacroDetails;
import net.rptools.maptool.model.Asset;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.library.data.LibraryData;

/** Interface for classes that represents a framework library. */
public interface Library {

  /**
   * Returns the version of the library
   *
   * @return the version of the library
   */
  CompletableFuture<String> getVersion();

  /**
   * Checks if the specified location exists.
   *
   * @param location the location to check.
   * @return {@code true} if the location exists, otherwise {@code false}.
   * @throws IOException if there is an io error reading the location.
   */
  CompletableFuture<Boolean> locationExists(URL location) throws IOException;

  /**
   * Checks to see if the specified location is an Asset.
   *
   * @param location the location to check.
   * @return {@code true} if the location is an asset, otherwise {@code false}.
   */
  CompletableFuture<Boolean> isAsset(URL location);

  /**
   * Returns the asset at the specified location. This will only return a value if {@link
   * #isAsset(URL)} returns {@code true}.
   *
   * @param location the location to get the asset for.
   * @return the asset at the specified location.
   */
  CompletableFuture<Optional<MD5Key>> getAssetKey(URL location);

  /**
   * Reads the location as a string.
   *
   * @param location the location to read.
   * @return the contents of the location as a string.
   * @throws IOException if there is an io error reading the location.
   */
  CompletableFuture<String> readAsString(URL location) throws IOException;

  /**
   * Returns an {@link InputStream} for the location specified.
   *
   * @param location the location to return the input stream for.
   * @return the input stream for the location
   * @throws IOException if there is an io error reading the location.
   */
  CompletableFuture<InputStream> read(URL location) throws IOException;

  /**
   * Returns the Website for the library.
   *
   * @return the Website for the library.
   */
  CompletableFuture<String> getWebsite();

  /**
   * Returns the GitHub URL for the library.
   *
   * @return the GitHub URL for the library.
   */
  CompletableFuture<String> getGitUrl();

  /**
   * Returns the Authors of the library.
   *
   * @return the Authors of the library.
   */
  CompletableFuture<String[]> getAuthors();

  /**
   * Returns the license of the library.
   *
   * @return the license of the library.
   */
  CompletableFuture<String> getLicense();

  /**
   * Returns the namespace for the library.
   *
   * @return the namespace for the library.
   */
  CompletableFuture<String> getNamespace();

  /**
   * Returns the name of the library.
   *
   * @return the name of the library.
   */
  CompletableFuture<String> getName();

  /**
   * Returns the description of the library.
   *
   * @return the description of the library.
   */
  CompletableFuture<String> getDescription();

  /**
   * Returns the short description of the library.
   *
   * @return the short description of the library.
   */
  CompletableFuture<String> getShortDescription();

  /**
   * Returns if this library allows lib:// URI access or not.
   *
   * @return {@code true} if URI access is allowed.
   */
  CompletableFuture<Boolean> allowsUriAccess();

  /**
   * Returns a {@link LibraryInfo} record representing this library, in a lot of cases where you
   * need to deal with several bits of information about the library this is going to be more
   * convenient than calling several methods that all return completable futures.
   *
   * @return information about the library.
   */
  CompletableFuture<LibraryInfo> getLibraryInfo();

  /**
   * Returns the information about MapTool Macro Script on this library.
   *
   * @param macroName The name of the macro.
   * @return the information about the MapTool Macro Script.
   */
  CompletableFuture<Optional<MTScriptMacroInfo>> getMTScriptMacroInfo(String macroName);

  /**
   * Returns the information about "non-public" MapTool Macro Script on this library. The non-public
   * macro script should only be available im a limited set of circumstances, not to general
   * [macro(): ] calls.
   *
   * @param macroName The name of the macro.
   * @return the information about the MapTool Macro Script.
   */
  CompletableFuture<Optional<MTScriptMacroInfo>> getPrivateMacroInfo(String macroName);

  /**
   * Returns a list of the "files" within the library.
   *
   * @return a list of the "files" within the library.
   */
  CompletableFuture<List<String>> getAllFiles();

  /**
   * Returns the data for the library.
   *
   * @return the data for the library.
   */
  CompletableFuture<LibraryData> getLibraryData();

  /**
   * Returns the name of the MT script to execute for the legacy event.
   *
   * @param eventName The name of the event.
   * @return the name of the MT script to execute for the legacy event.
   */
  CompletableFuture<Optional<String>> getLegacyEventHandlerName(String eventName);

  /**
   * Returns the token associated with this library. This will only return a token for legacy
   * libraries.
   *
   * @return the token associated with this library.
   */
  CompletableFuture<Optional<Token>> getAssociatedToken();

  /**
   * Returns if the context is able to access the private values in the library.
   *
   * @param context the current MTScript context to check.
   * @return if the context is able to access the private values in the library.
   */
  boolean canMTScriptAccessPrivate(MapToolMacroContext context);

  /**
   * Returns the {@link Asset} for the library readme file if it has one.
   *
   * @return the {@link Asset} for the library readme file if it has one.
   */
  CompletableFuture<Optional<Asset>> getReadMeAsset();

  /**
   * Returns the {@link Asset} for the library license file if it has one.
   *
   * @return the {@link Asset} for the library license file if it has one.
   */
  CompletableFuture<Optional<Asset>> getLicenseAsset();

  /**
   * Clean up any resources used by the library, This should be called when the library is no longer
   * needed.
   */
  void cleanup();

  /**
   * Returns the slash commands defined by the library.
   *
   * @return the slash commands defined by for the library.
   */
  Set<MacroDetails> getSlashCommands();
}
