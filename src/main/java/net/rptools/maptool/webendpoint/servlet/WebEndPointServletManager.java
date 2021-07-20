package net.rptools.maptool.webendpoint.servlet;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ServletInfo;
import java.util.Arrays;
import java.util.Collection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.webendpoint.WebEndPoint;
import net.rptools.maptool.webendpoint.servlet.general.MapToolServlet;

public class WebEndPointServletManager {


  private final ServletInfo[] servlets = new ServletInfo[] {
      Servlets.servlet(MapToolServlet.getEndPointServletName(), MapToolServlet.class)
          .addMapping("/info")
  };

  public Collection<ServletInfo> getServlets() {
    return Arrays.asList(servlets);
  }
}
