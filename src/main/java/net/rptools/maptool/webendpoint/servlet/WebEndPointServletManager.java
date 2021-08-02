package net.rptools.maptool.webendpoint.servlet;

import io.undertow.servlet.api.ServletInfo;
import java.util.Arrays;
import java.util.Collection;

public class WebEndPointServletManager {


  private final ServletInfo[] servlets = new ServletInfo[] {
      /*Servlets.servlet(MapToolVersionServlet.getEndPointServletName(), MapToolVersionServlet
      .class)
          .addMapping("/version"),
      Servlets.servlet(PlayerServlet.getEndPointServletName(), PlayerServlet.class).addMapping(
          "/player")*/
  };

  public Collection<ServletInfo> getServlets() {
    return Arrays.asList(servlets);
  }
}
