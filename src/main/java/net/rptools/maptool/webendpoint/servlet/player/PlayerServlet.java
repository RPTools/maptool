package net.rptools.maptool.webendpoint.servlet.player;

import java.net.HttpURLConnection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import net.rptools.maptool.api.player.PlayerApi;
import net.rptools.maptool.api.player.PlayerInfo;
import net.rptools.maptool.api.util.ApiResult;
import net.rptools.maptool.api.util.ApiResultStatus;
import net.rptools.maptool.webendpoint.ApiHttpStatusMapping;

@Path("/player")
public class PlayerServlet {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public PlayerInfo get() {
    try {
      ApiResult<PlayerInfo> player = new PlayerApi().getPlayer().get();
      if (player.getStatus() == ApiResultStatus.OK) {
        return player.getData();
      } else {
        throw new WebApplicationException(new ApiHttpStatusMapping().getHttpStatus(player.getStatus()));
      }
    } catch (WebApplicationException we) {
        throw we;
    } catch (Exception e) {
      // TODO: CDW: Log this
      throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{name}")
  public PlayerInfo get(@PathParam("name") String name) {
    try {
      ApiResult<PlayerInfo> player = new PlayerApi().getPlayer(name).get();
      if (player.getStatus() == ApiResultStatus.OK) {
        return player.getData();
      } else {
        throw new WebApplicationException(new ApiHttpStatusMapping().getHttpStatus(player.getStatus()));
      }
    } catch (WebApplicationException we) {
      throw we;
    } catch (Exception e) {
      // TODO: CDW: Log this
      throw new WebApplicationException(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }
}
