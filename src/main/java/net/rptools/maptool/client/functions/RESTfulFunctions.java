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
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

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

    if (parameters.size() == 0)
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));

    // Send GET Request to URL
    String baseURL = parameters.get(0).toString();
    String responseString = "";

    /**
     * REST.get does a RESTful GET request using OkHttp
     *
     * <p>String(url) :: Sets the URL target of this request.
     *
     * <p>boolean(fullResponse) :: If true, will return full response data as JSON
     */
    if (functionName.equals("REST.get")) {
      if (parameters.size() > 2)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

      // Special case, syrinscape URI use java.awt.Desktop to launch URI
      if (baseURL.startsWith("syrinscape")) {
        if (parameters.size() != 1)
          throw new ParserException(
              I18N.getText(
                  "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

        launchSyrinscape(baseURL);
      }

      // Full Response requested?
      boolean fullResponse = false;
      if (parameters.size() == 2)
        fullResponse = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(1));

      // Make the RESTful Get Request using OkHttp
      Request request = new Request.Builder().url(baseURL).build();

      // Check the response...
      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful() && !fullResponse)
          throw new ParserException(
              I18N.getText("macro.function.rest.error.response", functionName, response.code()));

        if (fullResponse) {
          responseString = gson.toJson(new RestResponseObj(response));
        } else {
          responseString = response.body().string();
        }
      } catch (IllegalArgumentException | IOException e) {
        throw new ParserException(I18N.getText("macro.function.rest.error", functionName));
      }

      return responseString;
    }

    /**
     * REST.post does a RESTful POST request using OkHttp
     *
     * <p>String(url) :: Sets the URL target of this request.
     *
     * <p>String(payload) :: Sets payload of the request.
     *
     * <p>String(content-type) :: Sets content-type of the request.
     *
     * <p>boolean(fullResponse) :: If true, will return full response data as JSON
     */
    if (functionName.equals("REST.post")
        || functionName.equals("REST.put")
        || functionName.equals("REST.patch")
        || functionName.equals("REST.delete")) {
      if (parameters.size() < 3 || parameters.size() > 4)
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));

      // Get the Payload to POST
      String payload = parameters.get(1).toString();

      MediaType mediaType = MediaType.parse(parameters.get(2).toString());
      // Full Response requested?
      boolean fullResponse = false;
      if (parameters.size() == 4)
        fullResponse = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(3));

      Request request;

      if (functionName.equals("REST.post"))
        request =
            new Request.Builder().url(baseURL).post(RequestBody.create(mediaType, payload)).build();
      else if (functionName.equals("REST.put"))
        request =
            new Request.Builder().url(baseURL).put(RequestBody.create(mediaType, payload)).build();
      else if (functionName.equals("REST.patch"))
        request =
            new Request.Builder()
                .url(baseURL)
                .patch(RequestBody.create(mediaType, payload))
                .build();
      else if (functionName.equals("REST.delete"))
        request =
            new Request.Builder()
                .url(baseURL)
                .delete(RequestBody.create(mediaType, payload))
                .build();
      else
        throw new ParserException(
            I18N.getText("macro.function.rest.error.unknown", functionName, 1, parameters.size()));

      try (Response response = client.newCall(request).execute()) {
        if (!response.isSuccessful() && !fullResponse)
          throw new ParserException(
              I18N.getText("macro.function.rest.error.response", functionName, response.code()));

        if (fullResponse) {
          responseString = gson.toJson(new RestResponseObj(response));
        } else {
          responseString = response.body().string();
        }

      } catch (IllegalArgumentException | IOException e) {
        throw new ParserException(I18N.getText("macro.function.rest.error", functionName));
      }

      return responseString;
    }

    //    /**
    //     * REST.put does a RESTful put request using OkHttp
    //     *
    //     * <p>String(url) :: Sets the URL target of this request.
    //     *
    //     * <p>String(payload) :: Sets payload of the request.
    //     *
    //     * <p>String(mediaType) :: Sets payload of the request.
    //     *
    //     * <p>boolean(fullResponse) :: If true, will return full response data as JSON
    //     */
    //    if (functionName.equals("REST.put")) {
    //      if (parameters.size() < 3 || parameters.size() > 4)
    //        throw new ParserException(
    //            I18N.getText(
    //                "macro.function.general.wrongNumParam", functionName, 1, parameters.size()));
    //
    //      // Get the Payload to POST
    //      String payload = parameters.get(1).toString();
    //      Map<String, String> params = new HashMap<String, String>();
    //
    //      MediaType mediaType = MediaType.parse(parameters.get(1).toString());
    //      // Full Response requested?
    //      boolean fullResponse = false;
    //      if (parameters.size() == 4)
    //        fullResponse = AbstractTokenAccessorFunction.getBooleanValue(parameters.get(1));
    //
    //      //  Make an RESTful POST Request using OkHttp
    //      Request request =
    //          new Request.Builder().url(baseURL).put(RequestBody.create(mediaType,
    // payload)).build();
    //
    //      try (Response response = client.newCall(request).execute()) {
    //        if (!response.isSuccessful() && !fullResponse)
    //          throw new ParserException(
    //              I18N.getText("macro.function.rest.error.response", functionName,
    // response.code()));
    //
    //        if (fullResponse) {
    //          responseString = gson.toJson(new RestResponseObj(response));
    //        } else {
    //          responseString = response.body().string();
    //        }
    //
    //      } catch (IllegalArgumentException | IOException e) {
    //        log.error("GSON: ", e);
    //        //        throw new ParserException(I18N.getText("macro.function.rest.error",
    //        // functionName));
    //      }
    //
    //      return responseString;
    //    }

    return "No Response";
  }

  private BigDecimal launchSyrinscape(String URI) {
    if (!AppPreferences.getSyrinscapeActive()) return BigDecimal.ZERO;

    URI uri = null;

    if (Desktop.isDesktopSupported()) {
      try {
        uri = new URI(URI);
        Desktop.getDesktop().browse(uri);
      } catch (IOException | URISyntaxException e) {
        e.printStackTrace();
      }
    }

    return BigDecimal.ONE;
  }

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
