package net.rptools.maptool.model.gamedata;


import net.rptools.maptool.language.I18N;

/**
 * Exception thrown when an operation is attempted on a GameData object that is not valid.
 */
public class InvalidDataOperation extends RuntimeException{

  /**
   * The type of error that occurred.
   */
  public enum  Type {
    INVALID_CONVERSION,
    ALREADY_EXISTS,
    UNDEFINED
  }

  /** the type of error that occurred */
  private final Type errorType;

  /**
   * Create a new InvalidDataOperation.
   * @param message the message for the exception.
   * @param errorType the type of error that occurred.
   */
  private InvalidDataOperation(String message, Type errorType) {
    super(message);
    this.errorType = errorType;
  }

  /**
   * Return the type of error that occurred.
   * @return the type of error that occurred.
   */
  public Type getErrType() {
    return errorType;
  }

  /**
   * Creates an InvalidDataOperation for invalid data conversion.
   * @param from the type of data that was being converted from.
   * @param to the type of data that was being converted to.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createInvalidConversion(DataType from, DataType to) {
    return new InvalidDataOperation(I18N.getText("data.error.cantConvertTo", from.name(), to.name()),
        Type.INVALID_CONVERSION);
  }

  /**
   * Creates an InvalidDataOperation for invalid data conversion.
   * @param from the string representation of the type of data that was being converted from.
   * @param to the type of data that was being converted to.
   * @return
   */
  public static InvalidDataOperation createInvalidConversion(String from, DataType to) {
    return new InvalidDataOperation(I18N.getText("data.error.cantConvertTo", from, to.name()),
        Type.INVALID_CONVERSION);
  }

  /**
   * Creates an InvalidDataOperation for when a data key already exists.
   * @param name the name of the data key.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createAlreadyExists(String name) {
    return new InvalidDataOperation(I18N.getText("data.error.alreadyExists", name),
        Type.ALREADY_EXISTS);
  }

  /**
   * Creates an InvalidDataOperation for when a data key does not exist.
   * @param name the name of the data key.
   * @return the InvalidDataOperation.
   */
  public static InvalidDataOperation createUndefined(String name) {
    return new InvalidDataOperation(I18N.getText("data.error.undefined", name), Type.UNDEFINED);
  }
}
