package net.rptools.maptool.webendpoint.servlet.general;

import java.net.HttpURLConnection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import net.rptools.maptool.api.maptool.MapToolApi;
import net.rptools.maptool.api.maptool.MapToolInfo;
import net.rptools.maptool.api.util.ApiResult;
import net.rptools.maptool.api.util.ApiResultStatus;

@Path("/version")
public class VersionServlet {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public MapToolInfo get() {
    try {
      ApiResult<MapToolInfo> mapToolInfoApiResult = new MapToolApi().getVersion().get();
      if (mapToolInfoApiResult.getStatus() == ApiResultStatus.OK) {
        return mapToolInfoApiResult.getData();
      } else {
        throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    } catch (Exception e) {
      // TODO: CDW: Log this
      throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }
}
