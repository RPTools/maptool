package net.rptools.maptool.api.util;

import com.google.gson.JsonObject;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;

public class ApiResult<T extends ApiData> {


  private final T data;
  private final ApiResultStatus status;
  private final ApiException exception;


  public static ApiResult<NoData> INTERNAL_ERROR_RESULT = new ApiResult<>(new NoData());

  public ApiResult(T data) {
    this.data = data;
    this.exception = null;
    this.status = data != null ? ApiResultStatus.OK : ApiResultStatus.NONE;
  }

  public ApiResult(ApiException e) {
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
