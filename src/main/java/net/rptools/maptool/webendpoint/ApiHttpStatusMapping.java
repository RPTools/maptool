package net.rptools.maptool.webendpoint;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import net.rptools.maptool.api.util.ApiResultStatus;

public class ApiHttpStatusMapping {
  private static final Map<ApiResultStatus, Integer> apiStatusMap = new HashMap<>();

  static {
    apiStatusMap.put(ApiResultStatus.OK, HttpURLConnection.HTTP_OK);
    apiStatusMap.put(ApiResultStatus.ERROR, HttpURLConnection.HTTP_INTERNAL_ERROR);
    apiStatusMap.put(ApiResultStatus.NONE, HttpURLConnection.HTTP_NOT_FOUND);
  }


  public int getHttpStatus(ApiResultStatus status)  {
    return apiStatusMap.get(status);
  }

}
