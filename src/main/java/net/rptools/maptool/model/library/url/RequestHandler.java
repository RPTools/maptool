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
package net.rptools.maptool.model.library.url;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.client.*;
import net.rptools.maptool.util.threads.*;

public class RequestHandler {
  private static final Gson gson = new Gson();

  public static CompletableFuture<String> processRequest(
      String method,
      URI uri,
      String _body,
      HashMap<String, String> requestHeaders,
      HashMap<String, String> responseHeaders) {
    CompletableFuture<String> c = new CompletableFuture<String>();
    String body = (_body != null) ? _body : "";
    String scheme = uri.getScheme();

    // macro: URIs can only use POST requests
    if (scheme.equalsIgnoreCase("macro")) {
      if (!("post".equalsIgnoreCase(method))) {
        responseHeaders.put(":Status", "0");
        c.complete("Only POST method can call macros");
        return c;
      }
      try {
        Instant instant = Instant.now();
        String formattedTime =
            DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneOffset.UTC).format(instant);
        responseHeaders.put("Server", "Maptool Macro Server");
        responseHeaders.put("Date", formattedTime);
        responseHeaders.put("Content-Type", "text/html");
        responseHeaders.put(":Status", "200 OK");

        final MapToolVariableResolver resolver = new MapToolVariableResolver(null);
        c =
            new ThreadExecutionHelper<String>()
                .runOnSwingThread(
                    () -> {
                      String macroName = uri.getSchemeSpecificPart();

                      resolver.setVariable("macro.requestHeaders", gson.toJsonTree(requestHeaders));
                      resolver.setVariable(
                          "macro.responseHeaders", gson.toJsonTree(responseHeaders));
                      String line = MapTool.getParser().runMacro(resolver, null, macroName, body);
                      return line;
                    });
        c.thenApply(
            (String r) -> {
              try {
                HashMap<String, String> returnedHeaders;
                Object headerObj = resolver.getVariable("macro.responseHeaders");

                if (headerObj instanceof JsonObject headerJson) {
                  returnedHeaders =
                      gson.fromJson(
                          headerJson, new TypeToken<HashMap<String, String>>() {}.getType());
                } else {
                  String headerString = headerObj.toString();
                  returnedHeaders =
                      gson.fromJson(
                          headerString, new TypeToken<HashMap<String, String>>() {}.getType());
                }

                responseHeaders.putAll(returnedHeaders);
              } catch (Exception pe) {
                responseHeaders.put(":Status", "500 Internal Exception (bad response header)");
                return pe.toString();
              }
              return r;
            });

        return c;
      } catch (Exception e) {
        responseHeaders.put(":Status", "500 Internal Exception");
        c.complete(e.getMessage());
        return c;
      }
    }

    InputStream stream = null;
    if (scheme.equalsIgnoreCase("lib")) {
      if (!("get".equalsIgnoreCase(method))) {
        responseHeaders.put(":Status", "0");
        c.complete("Only GET method can retrieve resources");
        return c;
      }

      try {
        stream = new LibraryURLConnection(uri.toURL()).getInputStream();
      } catch (IOException ioe) {
        responseHeaders.put(":Status", "404 Not Found");
        c.complete(ioe.getMessage());
        return c;
      }
      responseHeaders.put(":Status", "200");
      try {
        c.complete(new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        return c;
      } catch (IOException e) {
        responseHeaders.put(":Status", "500 Internal Exception");
        c.complete(e.getMessage());
        return c;
      }
    }

    if (scheme.equalsIgnoreCase("asset")) {
      if (!("get".equalsIgnoreCase(method))) {
        responseHeaders.put(":Status", "0");
        c.complete("Only GET method can retrieve assets");
        return c;
      }
      try {
        stream = new AssetURLStreamHandler.AssetURLConnection(uri.toURL()).getInputStream();
      } catch (IOException ioe) {
        responseHeaders.put(":Status", "404 Not Found");
        c.complete(ioe.getMessage());
        return c;
      }
      try {
        byte[] bytes = stream.readAllBytes();
        byte[] outBytes = new byte[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
          outBytes[i * 2] = 0;
          outBytes[i * 2 + 1] = bytes[i];
        }
        responseHeaders.put(":Status", "200 OK");
        c.complete(new String(outBytes, StandardCharsets.UTF_16));
        return c;
      } catch (IOException e) {
        responseHeaders.put(":Status", "500 Internal Exception");
        c.complete(e.getMessage());
        return c;
      }
    }
    c.complete(null);
    return c;
  }
}
