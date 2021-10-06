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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

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
   * @return the inpute stream for the location
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
  CompletableFuture<String> getGitHubUrl();

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
}
