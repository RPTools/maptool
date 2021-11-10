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

/**
 * Exception that is thrown if the library is not valid Some of the reasons that a library may not
 * be valid are:
 *
 * <ul>
 *   <li>It has been removed.
 *   <li>It does not have the permission required.
 * </ul>
 */
public class LibraryNotValidException extends RuntimeException {
  public enum Reason {
    MISSING_LIBRARY,
    MISSING_PERMISSIONS,
    BAD_CONVERSION,
    BAD_LOCATION
  };

  private final Reason reason;

  public LibraryNotValidException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  public Reason getReason() {
    return reason;
  }
}
