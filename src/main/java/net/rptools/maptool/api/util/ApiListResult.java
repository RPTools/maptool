package net.rptools.maptool.api.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;

public class ApiListResult<T extends ApiData> {

  private final List<T> data;
  private final ApiResultStatus status;
  private final ApiException exception;

  public static ApiListResult<NoData> INTERNAL_ERROR_RESULT =
      new ApiListResult<>(List.of(new NoData()));

  public ApiListResult(List<T> data) {
    this.data = List.copyOf(data);
    this.exception = null;
    this.status = data != null ? ApiResultStatus.OK : ApiResultStatus.NONE;

  }


  public ApiListResult(ApiException e) {
    this.data = null;
    this.status = ApiResultStatus.ERROR;
    this.exception = e;
  }


    public List<T> getData() {
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
      JsonArray objList = data.stream().map(ApiData::asJsonObject)
          .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
      json.add("data", objList);
    }
    json.addProperty("status", status.getTextValue());
    String msg = getStatusMessage();
    if (msg != null && msg.length() > 0) {
      json.addProperty("message", msg);
    }

    return json;
  }


}
