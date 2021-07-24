package net.rptools.maptool.api.util;

public enum ApiResultStatus {
    OK("ok", ""),
    ERROR("error", "Internal API Error"),
    NONE("none", "Not Found");

    private final String textValue;
    private final String defaultMessage;

    ApiResultStatus(String val, String defaultMsg) {
      textValue = val;
      defaultMessage = defaultMsg;
    }

    public String getTextValue() {
      return textValue;
    }

    public String getDefaultMessage() {
      return defaultMessage;
    }
}
