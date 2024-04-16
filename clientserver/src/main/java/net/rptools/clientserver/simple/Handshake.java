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
package net.rptools.clientserver.simple;

import java.util.concurrent.ExecutionException;
import net.rptools.clientserver.simple.connection.Connection;

public interface Handshake {

  /**
   * Returns if the handshake has been successful or not.
   *
   * @return {@code true} if the handshake has been successful, {code false} if it has failed or is
   *     still in progress.
   */
  boolean isSuccessful();

  /**
   * Returns the message for the error -- if any -- that occurred during the handshake.
   *
   * @return the message for the error that occurred during handshake.
   */
  String getErrorMessage();

  /**
   * Returns the connection for this {@code ServerHandshake}.
   *
   * @return the connection for this {@code ServerHandshake}.
   */
  Connection getConnection();

  /**
   * Returns the exception -- if any -- that occurred during processing of the handshake.
   *
   * @return the exception that occurred during the processing of the handshake.
   */
  Exception getException();

  /**
   * Adds an observer to the handshake process.
   *
   * @param observer the observer of the handshake process.
   */
  void addObserver(HandshakeObserver observer);

  /**
   * Removes an observer from the handshake process.
   *
   * @param observer the observer of the handshake process.
   */
  void removeObserver(HandshakeObserver observer);

  /**
   * Starts the handshake process.
   *
   * @throws ExecutionException when there is an exception in the background task.
   * @throws InterruptedException when the background task is interrupted.
   */
  void startHandshake() throws ExecutionException, InterruptedException;
}
