package net.rptools.maptool.webendpoint.servlet;

import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.ServletInfo;
import java.util.Arrays;
import java.util.Collection;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.webendpoint.WebEndPoint;
import net.rptools.maptool.webendpoint.servlet.general.MapToolServlet;

public class WebEndPointServletManager {

  private static final String CONTEXT_PATH = "/";
  private static final String DEPLOYMENT_NAME = "maptool";

  public static final String PARAM_MAPTOOL_VERSION = "maptoolVersion";
  public static final String PARAM_WEB_APP_VERSION = "webAppVersion";

  private final ServletInfo[] servlets = new ServletInfo[] {
      Servlets.servlet(MapToolServlet.getEndPointServletName(), MapToolServlet.class)
          .addInitParam(PARAM_MAPTOOL_VERSION, MapTool.getVersion())
          .addInitParam(PARAM_WEB_APP_VERSION, WebEndPoint.getWebEndPointVersion())
          .addMapping("/maptool")
  };

  public Collection<ServletInfo> getServlets() {
    return Arrays.asList(servlets);
  }
}
