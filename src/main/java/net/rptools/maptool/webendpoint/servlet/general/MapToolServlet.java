package net.rptools.maptool.webendpoint.servlet.general;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.rptools.maptool.api.maptool.MapToolApi;
import net.rptools.maptool.api.util.ApiResult;

public class MapToolServlet extends HttpServlet {


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

    PrintWriter writer = response.getWriter();
    Gson gson = new Gson();
    try {
      JsonObject version = new MapToolApi().getVersion().get().asJsonObject();
      gson.toJson(version, writer);
    } catch (InterruptedException | ExecutionException e) {
      gson.toJson(ApiResult.INTERNAL_ERROR_RESULT, writer);
    }
    writer.close();
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

}
