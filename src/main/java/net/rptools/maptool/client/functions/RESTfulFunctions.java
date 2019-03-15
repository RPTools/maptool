/*
 * This software Copyright by the RPTools.net development team, and
 * licensed under the Affero GPL Version 3 or, at your option, any later
 * version.
 *
 * MapTool Source Code is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public
 * License * along with this source Code.  If not, please visit
 * <http://www.gnu.org/licenses/> and specifically the Affero license
 * text at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * New class extending AbstractFunction to create new "Macro Functions" REST.get & REST.post
 *
 * <p>REST.get(URL) :: Takes a URL as a string and sends a GET request, returning HTTP data.
 *
 * <p>REST.post(URL, Parameters) :: Takes a URL as a string and a JSON array of Parameters and sends
 * s POST request, returning HTTP data.
 */
public class RESTfulFunctions extends AbstractFunction {
  private static final Logger log = LogManager.getLogger();

  private static final RESTfulFunctions instance = new RESTfulFunctions();

  private RESTfulFunctions() {
    super(1, 4, "REST.get", "REST.post", "REST.put", "REST.patch", "REST.delete");
  }

  public static RESTfulFunctions getInstance() {
    return instance;
  }

  private final OkHttpClient client = new OkHttpClient();
  private final Gson gson = new Gson();

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    if (!MapTool.getParser().isMacroPathTrusted())
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

    if (!AppPreferences.getAllowExternalMacroAccess())
      throw new ParserException(I18N.getText("macro.function.general.accessDenied", functionName));

    // Check do we need this?
    checkParameters(functionName, parameters, 1, 5);

    // Send GET Request to URL
    if (functionName.equals("REST.get")) return restGet(functionName, parameters);

    if (functionName.equals("REST.post")
        || functionName.equals("REST.put")
        || functionName.equals("REST.patch")
        || functionName.equals("REST.delete"))
      return makeRestfulClientCall(functionName, parameters);
    else
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  /**
   * Performs a RESTful GET request using OkHttp
   *
   * @param functionName
   * @param parameters include baseURL and if full response is requested
   * @return HTTP response as JSON
   * @throws ParserException
   */
  private Object restGet(String functionName, List<Object> parameters) throws ParserException {
    checkParameters(functionName, parameters, 1, 3);

    String baseURL = parameters.get(0).toString();
    Map<String, List<String>> headerMap = new HashMap<String, List<String>>();
    boolean fullResponse = false;

    // Special case, syrinscape URI use java.awt.Desktop to launch URI
    if (baseURL.startsWith("syrinscape")) {
      checkParameters(functionName, parameters, 1, 1);
      return launchSyrinscape(baseURL);
    }

    if (parameters.size() == 2) {
      if (parameters.get(1) instanceof BigDecimal)
        fullResponse = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(1));
      else
        headerMap =
            gson.fromJson(
                (String) parameters.get(1),
                new TypeToken<Map<String, List<String>>>() {}.getType());
    }

    if (parameters.size() == 3) {
      headerMap =
          gson.fromJson(
              (String) parameters.get(1), new TypeToken<Map<String, List<String>>>() {}.getType());
      fullResponse = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(2));
    }

    Request request;

    if (headerMap.isEmpty()) {
      request = new Request.Builder().url(baseURL).build();
    } else {
      request = new Request.Builder().build();
      Headers.Builder headersBuilder = request.headers().newBuilder();
     
      for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
        String name = entry.getKey();
        List<String> values = entry.getValue();
        for (String value : values) headersBuilder.add(name, value);
      }

      request = new Request.Builder().url(baseURL).headers(headersBuilder.build()).build();
    }

    // Execute the call and check the response...
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() && !fullResponse)
        throw new ParserException(
            I18N.getText("macro.function.rest.error.response", functionName, response.code()));

      if (fullResponse) {
        return gson.toJson(new RestResponseObj(response));
      } else {
        return response.body().string();
      }
    } catch (IllegalArgumentException | IOException e) {
      throw new ParserException(I18N.getText("macro.function.rest.error", functionName));
    }
  }

  /**
   * Performs a RESTful POST, PATCH, PUT, or DELETE request using OkHttp
   *
   * @param functionName
   * @param parameters include baseURL and if full response is requested
   * @return HTTP response as JSON
   * @throws ParserException
   */
  private Object makeRestfulClientCall(String functionName, List<Object> parameters)
      throws ParserException {
    checkParameters(functionName, parameters, 3, 4);

    Request request;
    String baseURL = parameters.get(0).toString();
    String payload = parameters.get(1).toString();
    MediaType mediaType = MediaType.parse(parameters.get(2).toString());

    // Full Response requested?
    boolean fullResponse = false;
    if (parameters.size() == 4)
      fullResponse = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(3));

    if (functionName.equals("REST.post"))
      request =
          new Request.Builder().url(baseURL).post(RequestBody.create(mediaType, payload)).build();
    else if (functionName.equals("REST.put"))
      request =
          new Request.Builder().url(baseURL).put(RequestBody.create(mediaType, payload)).build();
    else if (functionName.equals("REST.patch"))
      request =
          new Request.Builder().url(baseURL).patch(RequestBody.create(mediaType, payload)).build();
    else if (functionName.equals("REST.delete") && payload.isEmpty())
      request = new Request.Builder().url(baseURL).delete().build();
    else if (functionName.equals("REST.delete") && !payload.isEmpty())
      request =
          new Request.Builder().url(baseURL).delete(RequestBody.create(mediaType, payload)).build();
    else
      throw new ParserException(
          I18N.getText("macro.function.rest.error.unknown", functionName, parameters.size()));

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() && !fullResponse)
        throw new ParserException(
            I18N.getText("macro.function.rest.error.response", functionName, response.code()));

      if (fullResponse) return gson.toJson(new RestResponseObj(response));
      else return response.body().string();

    } catch (IllegalArgumentException | IOException e) {
      throw new ParserException(I18N.getText("macro.function.rest.error", functionName));
    }
  }

  /*
   * @param parameters include baseURL and if full response is requested
   * @return HTTP response as JSON
   * @throws ParserException
   */
  private BigDecimal launchSyrinscape(String baseURL) throws ParserException {
    if (!AppPreferences.getSyrinscapeActive()) return BigDecimal.ZERO;

    URI uri;

    if (Desktop.isDesktopSupported()) {
      try {
        uri = new URI(baseURL);
        Desktop.getDesktop().browse(uri);
      } catch (IOException | URISyntaxException e) {
        throw new ParserException(I18N.getText("macro.function.rest.syrinscape.error"));
      }
    }

    return BigDecimal.ONE;
  }

  /**
   * @param function's name
   * @param parameters passed into the function call
   * @param min number of parameters required
   * @param max number of parameters required
   * @throws ParserException
   */
  private void checkParameters(String functionName, List<Object> parameters, int min, int max)
      throws ParserException {

    if (min == max) {
      if (parameters.size() != max)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, max, parameters.size()));

    } else {
      if (parameters.size() < min)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, min, parameters.size()));

      if (parameters.size() > max)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, max, parameters.size()));
    }
  }

  /*
   * A POJO to hold an HTTP Response object to marshal as a JSON object
   */
  private final class RestResponseObj {
    private final int status;
    private final String message;
    private final Map<String, List<String>> headers;
    private final JsonElement body;

    public RestResponseObj(Response response) throws IOException {
      this.status = response.code();
      this.headers = response.headers().toMultimap();

      if (!response.message().isEmpty()) this.message = response.message();
      else this.message = null;

      String responseBody = response.body().string();
      if (isValidJSON(responseBody)) body = gson.fromJson(responseBody, JsonElement.class);
      else this.body = new Gson().toJsonTree(responseBody);
    }

    private boolean isValidJSON(String jsonInString) {
      try {
        gson.fromJson(jsonInString, JsonElement.class);
        return true;
      } catch (com.google.gson.JsonSyntaxException ex) {
        return false;
      }
    }
  }
}
