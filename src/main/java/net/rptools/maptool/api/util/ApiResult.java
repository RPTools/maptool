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
package net.rptools.maptool.api.util;

import com.google.gson.JsonObject;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;

public class ApiResult<T extends ApiData> {

  private final T data;
  private final ApiResultStatus status;
  private final ApiException exception;

  public static ApiResult<NoData> NOT_FOUND = new ApiResult<>(new NoData());

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
