package net.rptools.maptool.api;

public class ApiException extends Exception {

  public ApiException(String message) {
    super(message);
  }

  public ApiException(String message, Throwable cause) {
    super(message, cause);
  }

}
