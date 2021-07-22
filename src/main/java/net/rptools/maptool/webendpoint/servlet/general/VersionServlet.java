package net.rptools.maptool.webendpoint.servlet.general;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import net.rptools.maptool.api.maptool.MapToolApi;
import net.rptools.maptool.api.maptool.MapToolInfo;

@Path("/version1")
public class VersionServlet {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public MapToolInfo get() {
    return new MapToolInfo();
  }
}
