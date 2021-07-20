package net.rptools.maptool.webendpoint.servlet.general;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rptools.maptool.api.maptool.MapToolInfo;
import net.rptools.maptool.client.MapTool;
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
  }

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {
    response.setContentType("application/json");

    MapToolInfo maptoolInfo = new MapToolInfo();

    PrintWriter writer = response.getWriter();
    Gson gson = new Gson();
    gson.toJson(maptoolInfo, writer);
    writer.close();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

}
