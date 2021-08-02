package net.rptools.maptool.webendpoint.servlet.player;

import java.net.HttpURLConnection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import net.rptools.maptool.api.player.PlayerApi;
import net.rptools.maptool.api.player.PlayerDatabaseInfo;
import net.rptools.maptool.api.player.PlayerInfo;
import net.rptools.maptool.api.util.ApiResult;
import net.rptools.maptool.api.util.ApiResultStatus;
import net.rptools.maptool.webendpoint.ApiHttpStatusMapping;

@Path("/playerdb")
public class PlayerDatabaseServlet {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PlayerDatabaseInfo get() {
    try {
      ApiResult<PlayerDatabaseInfo> playerdb = new PlayerApi().getDatabaseCapabilities().get();
      if (playerdb.getStatus() == ApiResultStatus.OK) {
        return playerdb.getData();
      } else {
        throw new WebApplicationException(new ApiHttpStatusMapping().getHttpStatus(playerdb.getStatus()));
      }
    } catch (WebApplicationException we) {
        throw we;
    } catch (Exception e) {
      // TODO: CDW: Log this
      throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

}
