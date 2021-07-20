package net.rptools.maptool.api.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;

public class ApiResult<T extends ApiData> {
  public enum Status {
    OK("ok", ""),
    ERROR("error", "Internal API Error");

    private final String textValue;
    private final String defaultMessage;

    Status(String val, String defaulMsg) {
      textValue = val;
      defaultMessage = defaulMsg;
    }

    public String getTextValue() {
      return textValue;
    }

    public String getDefaultMessage() {
      return defaultMessage;
    }
  }

  private final T data;
  private final Status status;
  private final ApiException exception;


  public static ApiResult<NoData> INTERNAL_ERROR_RESULT = new ApiResult<>(new NoData());

  public ApiResult(T data) {
    this.data = data;
    this.status = Status.OK;
    this.exception = null;
  }

  public T getData() {
    return data;
  }

  public Status getStatus() {
    return status;
  }

  public String getStatusMessage() {
    if (exception == null) {
      return "";
    } else {
      return exception.getMessage();
    }
  }

  public JsonObject asJsonObject() {
    Gson gson = new Gson();
    JsonObject json = new JsonObject();
    if (data != null) {
      json.addProperty("data", gson.toJson(data));
    }
    json.addProperty("status", status.getTextValue());
    String msg = getStatusMessage();
    if (msg != null && msg.length() > 0) {
      json.addProperty("message", msg);
    }

    return json;
  }


}
