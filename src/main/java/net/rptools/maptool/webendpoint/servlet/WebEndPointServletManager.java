package net.rptools.maptool.webendpoint.servlet;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ServletInfo;
import java.util.Arrays;
import java.util.Collection;
import net.rptools.maptool.webendpoint.servlet.general.MapToolVersionServlet;
import net.rptools.maptool.webendpoint.servlet.player.PlayerServlet;

public class WebEndPointServletManager {


  private final ServletInfo[] servlets = new ServletInfo[] {
      Servlets.servlet(MapToolVersionServlet.getEndPointServletName(), MapToolVersionServlet.class)
          .addMapping("/version"),
      Servlets.servlet(PlayerServlet.getEndPointServletName(), PlayerServlet.class).addMapping(
          "/player")
  };

  public Collection<ServletInfo> getServlets() {
    return Arrays.asList(servlets);
  }
}
