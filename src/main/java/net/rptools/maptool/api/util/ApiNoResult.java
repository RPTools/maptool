package net.rptools.maptool.api.util;

import com.google.gson.JsonObject;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;

public class ApiNoResult<T extends ApiData> {

  private final ApiResultStatus status;
  private final ApiException exception;


  public static ApiNoResult<NoData> NOT_FOUND = new ApiNoResult<>(new NoData());

  public ApiNoResult() {
    this.exception = null;
  }

  public ApiNoResult(ApiException e) {
    this.data = null;
    this.status = ApiResultStatus.ERROR;
    this.exception = e;
  }

  public T getData() {
    return data;
  }

  public ApiResultStatus getStatus() {
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
    JsonObject json = new JsonObject();
    if (data != null) {
      json.add("data", data.asJsonObject());
    }
    json.addProperty("status", status.getTextValue());
    String msg = getStatusMessage();
    if (msg != null && msg.length() > 0) {
      json.addProperty("message", msg);
    }

    return json;
  }


}
