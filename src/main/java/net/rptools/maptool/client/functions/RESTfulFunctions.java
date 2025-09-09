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

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Inflater;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.GzipSource;
import okio.InflaterSource;
import okio.Okio;

/**
 * RESTful based functions REST.get, REST.post, REST.put, REST.patch, REST.delete
 *
 * <p>Functions take 1 to 5 parameters depending on function called. A minimum of a URL is always
 * required.
 *
 * <p>A body (payload) and Media Type are always required for post, put, patch and optional for
 * delete.
 *
 * <p>Headers are optional on all functions passed in as a JSON as a String: String[] array of
 * Strings
 *
 * <p>A boolean at the end is always option to request a full response. A BigDecimal of 1 will
 * return HTTP status code, headers, message (if present), and body of response. Otherwise just the
 * body of the response is returned in what ever form the call returns.
 */
public class RESTfulFunctions extends AbstractFunction {

  private static final RESTfulFunctions instance = new RESTfulFunctions();

  private RESTfulFunctions() {
    super(1, 5, "REST.get", "REST.post", "REST.put", "REST.patch", "REST.delete");
  }

  public static RESTfulFunctions getInstance() {
    return instance;
  }

  private final OkHttpClient client = buildClient();
  private final Gson gson = new Gson();

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    if (!AppPreferences.allowExternalMacroAccess.get()) {
      throw new ParserException(I18N.getText("macro.function.general.accessDenied", functionName));
    }

    // Check do we need this?
    checkParameters(functionName, parameters, 1, 5);

    if (functionName.equalsIgnoreCase("REST.get")) {
      return buildGetRequest(functionName, parameters);
    }

    if (functionName.equalsIgnoreCase("REST.post")
        || functionName.equalsIgnoreCase("REST.put")
        || functionName.equalsIgnoreCase("REST.patch")
        || functionName.equalsIgnoreCase("REST.delete")) {
      return buildRequest(functionName, parameters);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
  }

  /**
   * Performs a RESTful GET request using OkHttp
   *
   * @param functionName
   * @param parameters include URL, headers (optional) and if full response is requested (optional)
   * @return HTTP response as JSON (if full response) or server response, usually JSON but can be
   *     XML, HTML, or other formats.
   * @throws ParserException
   */
  private Object buildGetRequest(String functionName, List<Object> parameters)
      throws ParserException {
    checkParameters(functionName, parameters, 1, 3);

    Request request;
    String baseURL = parameters.get(0).toString();
    Map<String, List<String>> headerMap = getHeaderMap(parameters, 1);
    Headers headers = buildHeaders(headerMap);

    // Special case, syrinscape URI use java.awt.Desktop to launch URI
    if (baseURL.startsWith("syrinscape")) {
      checkParameters(functionName, parameters, 1, 1);
      return launchSyrinscape(baseURL);
    }

    if (functionName.equalsIgnoreCase("REST.get")) {
      request = new Request.Builder().url(baseURL).build();
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }

    // If we need to add headers then rebuild a new request with new headers
    if (!headerMap.isEmpty()) {
      if (functionName.equalsIgnoreCase("REST.get")) {
        request = new Request.Builder().url(baseURL).headers(headers).build();
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    }

    return executeClientCall(functionName, request, isFullResponseRequested(parameters));
  }

  /**
   * Performs a RESTful POST, PATCH, PUT or DELETE request using OkHttp Minimum requirements are
   * URL, Payload, & MediaType Optional parameters are Headers & Full Response
   *
   * @param functionName
   * @param parameters include URL, payload, media type of payload, headers (optional) and if full
   *     response is requested (optional)
   * @return HTTP response as JSON (if full response) or server response, usually JSON but can be
   *     XML, HTML, or other formats.
   * @throws ParserException
   */
  private Object buildRequest(String functionName, List<Object> parameters) throws ParserException {
    checkParameters(functionName, parameters, 3, 5);

    Request request = null;
    String baseURL = parameters.get(0).toString();
    String payload = parameters.get(1).toString();
    MediaType mediaType = MediaType.parse(parameters.get(2).toString());
    Map<String, List<String>> headerMap = getHeaderMap(parameters, 3);
    Headers headers = buildHeaders(headerMap);

    // Build out the request body
    RequestBody requestBody = RequestBody.create(mediaType, payload);

    // If we need to add headers then build a new request with new headers

    if (headerMap.isEmpty()) {
      // Build without any headers...
      if (functionName.equalsIgnoreCase("REST.post")) {
        request = new Request.Builder().url(baseURL).post(requestBody).build();
      } else if (functionName.equalsIgnoreCase("REST.put")) {
        request = new Request.Builder().url(baseURL).put(requestBody).build();
      } else if (functionName.equalsIgnoreCase("REST.patch")) {
        request = new Request.Builder().url(baseURL).patch(requestBody).build();
      } else if (functionName.equalsIgnoreCase("REST.delete")) {
        request = new Request.Builder().url(baseURL).delete(requestBody).build();
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    } else {
      // Now build request with headers
      if (functionName.equalsIgnoreCase("REST.post")) {
        request = new Request.Builder().url(baseURL).headers(headers).post(requestBody).build();
      } else if (functionName.equalsIgnoreCase("REST.put")) {
        request = new Request.Builder().url(baseURL).headers(headers).put(requestBody).build();
      } else if (functionName.equalsIgnoreCase("REST.patch")) {
        request = new Request.Builder().url(baseURL).headers(headers).patch(requestBody).build();
      } else if (functionName.equalsIgnoreCase("REST.delete")) {
        request = new Request.Builder().url(baseURL).headers(headers).delete(requestBody).build();
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownFunction", functionName));
      }
    }

    return executeClientCall(functionName, request, isFullResponseRequested(parameters));
  }

  /**
   * Execute RESTful Client Call for the request and return appropriate response body
   *
   * @param functionName
   * @param request
   * @param fullResponse
   * @return
   * @throws ParserException
   */
  private Object executeClientCall(String functionName, Request request, boolean fullResponse)
      throws ParserException {

    // Execute the call and check the response...
    JsonObject errjson = new JsonObject();
    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful() && !fullResponse) {
        errjson.addProperty("error", response.code());
        return errjson;
      }

      if (fullResponse) {
        return gson.toJson(new RestResponseObj(response));
      } else {
        return response.body().string();
      }

    } catch (IllegalArgumentException | IOException e) {
      errjson.addProperty("error", e.toString());
      return errjson;
    }
  }

  private OkHttpClient buildClient() {
    return new OkHttpClient.Builder()
        .addInterceptor(
            (Interceptor.Chain chain) -> {
              var oldRequest = chain.request();

              // If the macro has passed its own Accept-Encoding
              // it's indicating it expects to somehow handle it itself.
              if (oldRequest.header("Accept-Encoding") != null) {
                return chain.proceed(oldRequest);
              }

              // Augment request saying we accept multiple content encodings
              var newHeaders =
                  oldRequest
                      .headers()
                      .newBuilder()
                      .add("Accept-Encoding", "deflate")
                      .add("Accept-Encoding", "gzip")
                      .build();

              var newRequest = oldRequest.newBuilder().headers(newHeaders).build();

              var oldResponse = chain.proceed(newRequest);

              // Replace the response's request with the original one
              var responseBuilder = oldResponse.newBuilder().request(oldRequest);

              // We might not have a body to decompress
              var body = oldResponse.body();
              if (body != null) {
                BufferedSource source = body.source();
                // The body may have been wrapped in an arbitrary encoding sequence
                // and the server returns them in the order it encoded them
                // so we wrap them with decoders in reverse order.
                var encodings = oldResponse.headers().values("Content-Encoding");
                Collections.reverse(encodings);
                for (var encoding : encodings) {
                  if ("deflate".equalsIgnoreCase(encoding)) {
                    var inflater = new Inflater(true);
                    source = Okio.buffer(new InflaterSource(source, inflater));
                  } else if ("gzip".equalsIgnoreCase(encoding)) {
                    source = Okio.buffer(new GzipSource(source));
                  }
                }

                // Strip encoding and length headers as we've already handled them
                var strippedHeaders =
                    oldResponse
                        .headers()
                        .newBuilder()
                        .removeAll("Content-Encoding")
                        .removeAll("Content-Length")
                        .build();
                responseBuilder.headers(strippedHeaders);
                var contentType = MediaType.parse(oldResponse.header("Content-Type"));
                // Construct a new body with an inferred Content-Length
                var newBody = ResponseBody.create(contentType, -1L, source);
                responseBuilder.body(newBody);
              }

              return responseBuilder.build();
            })
        .build();
  }

  private Headers buildHeaders(Map<String, List<String>> headerMap) {
    Headers.Builder headerBuilder = new Headers.Builder();

    for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
      String name = entry.getKey();
      List<String> values = entry.getValue();
      for (String value : values) {
        headerBuilder.add(name, value);
      }
    }

    return headerBuilder.build();
  }

  private boolean isLastParamBoolean(List<Object> parameters) {
    return (parameters.get(parameters.size() - 1) instanceof BigDecimal);
  }

  private boolean isFullResponseRequested(List<Object> parameters) {
    if (isLastParamBoolean(parameters)) {
      return FunctionUtil.getBooleanValue(parameters.get(parameters.size() - 1));
    } else {
      return false;
    }
  }

  private Map<String, List<String>> getHeaderMap(List<Object> parameters, int headerIndex) {
    // If the parameters only have enough room for either header or full response
    // and the last parameter is a boolean then headers were not passed in.
    // of if parameter size is not large enough to hold either value...
    if (headerIndex >= parameters.size()
        || (headerIndex == parameters.size() - 1 && isLastParamBoolean(parameters))) {
      return new HashMap<String, List<String>>();
    } else {
      String headerString = parameters.get(headerIndex).toString();

      return gson.fromJson(headerString, new TypeToken<Map<String, List<String>>>() {}.getType());
    }
  }

  /*
   * @param parameters include URI
   * @return BigDecimal.ONE if successful, BigDecimal.ZERO if Syrinscape is not activated in preferences
   * @throws ParserException
   */
  private BigDecimal launchSyrinscape(String baseURL) throws ParserException {
    if (!AppPreferences.syrinscapeActive.get()) {
      return BigDecimal.ZERO;
    }

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
   * @param functionName the name of the function
   * @param parameters passed into the function call
   * @param min number of parameters required
   * @param max number of parameters required
   * @throws ParserException
   */
  private void checkParameters(String functionName, List<Object> parameters, int min, int max)
      throws ParserException {

    if (min == max) {
      if (parameters.size() != max) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.wrongNumParam", functionName, max, parameters.size()));
      }

    } else {
      if (parameters.size() < min) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, min, parameters.size()));
      }

      if (parameters.size() > max) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.tooManyParam", functionName, max, parameters.size()));
      }
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

      if (!response.message().isEmpty()) {
        this.message = response.message();
      } else {
        this.message = null;
      }

      String responseBody = response.body().string();
      if (isValidJSON(responseBody)) {
        body = gson.fromJson(responseBody, JsonElement.class);
      } else {
        this.body = new Gson().toJsonTree(responseBody);
      }
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
