package net.rptools.maptool.webendpoint.servlet.general;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rptools.maptool.webendpoint.servlet.WebEndPointServletManager;

public class MapToolServlet extends HttpServlet {
  private String maptoolVersion;
  private String webAppVersion;


  public static String getEndPointServletName() {
    return "MapToolServlet";
  }

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    maptoolVersion = config.getInitParameter(WebEndPointServletManager.PARAM_MAPTOOL_VERSION);
    webAppVersion = config.getInitParameter(WebEndPointServletManager.PARAM_WEB_APP_VERSION);
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");
    JsonObject responseObject = new JsonObject();
    responseObject.addProperty(WebEndPointServletManager.PARAM_MAPTOOL_VERSION, maptoolVersion);
    responseObject.addProperty(WebEndPointServletManager.PARAM_WEB_APP_VERSION, webAppVersion);

    PrintWriter writer = response.getWriter();
    writer.write(responseObject.toString());
    writer.close();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

}
