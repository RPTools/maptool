package net.rptools.maptool.webendpoint.servlet.player;

import java.net.HttpURLConnection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import net.rptools.maptool.api.maptool.MapToolApi;
import net.rptools.maptool.api.maptool.MapToolInfo;
import net.rptools.maptool.api.player.PlayerApi;
import net.rptools.maptool.api.player.PlayerInfo;
import net.rptools.maptool.api.util.ApiResult;
import net.rptools.maptool.api.util.ApiResultStatus;

@Path("/version")
public class PlayerServlet {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PlayerInfo get() {
    try {
      ApiResult<PlayerInfo> player = new PlayerApi().getPlayer().get();
      switch (player.getStatus()) {
        case ApiResultStatus.OK:
          return mapToolInfoApiResult.getData();
        throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
      }
    } catch (Exception e) {
      // TODO: CDW: Log this
      throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }
}
