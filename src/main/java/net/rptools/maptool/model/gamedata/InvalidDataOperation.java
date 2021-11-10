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
package net.rptools.maptool.model.gamedata;

import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.gamedata.data.DataType;

/** Exception thrown when an operation is attempted on a GameData object that is not valid. */
public class InvalidDataOperation extends RuntimeException {

  /** The propertyType of error that occurred. */
  public enum Type {
    INVALID_CONVERSION,
    ALREADY_EXISTS,
    UNDEFINED,
    NAMESPACE_DOES_NOT_EXIST
  }

  /** the propertyType of error that occurred */
  private final Type errorType;

  /**
   * Create a new InvalidDataOperation.
   *
   * @param message the message for the exception.
   * @param errorType the propertyType of error that occurred.
   */
  private InvalidDataOperation(String message, Type errorType) {
    super(message);
    this.errorType = errorType;
  }

  /**
   * Return the propertyType of error that occurred.
   *
   * @return the propertyType of error that occurred.
   */
  public Type getErrorType() {
    return errorType;
  }

  /**
   * Creates an InvalidDataOperation for invalid data conversion.
   *
   * @param from the propertyType of data that was being converted from.
   * @param to the propertyType of data that was being converted to.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createInvalidConversion(DataType from, DataType to) {
    return new InvalidDataOperation(
        I18N.getText("data.error.cantConvertTo", from.name(), to.name()), Type.INVALID_CONVERSION);
  }

  /**
   * Creates an InvalidDataOperation for invalid data conversion.
   *
   * @param from the string representation of the propertyType of data that was being converted
   *     from.
   * @param to the propertyType of data that was being converted to.
   * @return
   */
  public static InvalidDataOperation createInvalidConversion(String from, DataType to) {
    return new InvalidDataOperation(
        I18N.getText("data.error.cantConvertTo", from, to.name()), Type.INVALID_CONVERSION);
  }

  /**
   * Creates an InvalidDataOperation for when a data key already exists.
   *
   * @param name the name of the data key.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createAlreadyExists(String name) {
    return new InvalidDataOperation(
        I18N.getText("data.error.alreadyExists", name), Type.ALREADY_EXISTS);
  }

  /**
   * Creates an InvalidDataOperation for when a data key does not exist.
   *
   * @param name the name of the data key.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createUndefined(String name) {
    return new InvalidDataOperation(I18N.getText("data.error.undefined", name), Type.UNDEFINED);
  }

  /**
   * Create an InvalidDataOperation for when a namespace does not exist for a propertyType.
   *
   * @param propertyType the propertyType for the namespace.
   * @param namespace The namespace that does not exist.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createNamespaceDoesNotExist(
      String propertyType, String namespace) {
    return new InvalidDataOperation(
        I18N.getText("data.error.namespaceDoesNotExist", namespace, propertyType),
        Type.NAMESPACE_DOES_NOT_EXIST);
  }

  /**
   * Create an InvalidDataOperation for when a namespace already exists for a property type.
   *
   * @param propertyType the propertyType for the namespace.
   * @param namespace The namespace that already exists.
   * @return
   */
  public static InvalidDataOperation createNamespaceAlreadyExists(
      String propertyType, String namespace) {
    return new InvalidDataOperation(
        I18N.getText("data.error.namespaceAlreadyExists", namespace, propertyType),
        Type.ALREADY_EXISTS);
  }
}
