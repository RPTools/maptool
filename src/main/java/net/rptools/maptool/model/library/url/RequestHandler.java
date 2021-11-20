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

import java.io.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.rptools.maptool.client.*;
import net.rptools.maptool.util.threads.*;

public class RequestHandler {
  public static String processRequest(
      String method,
      URI uri,
      String _body,
      HashMap<String, String> requestHeaders,
      HashMap<String, String> responseHeaders) {
    String body = (_body != null) ? _body : "";
    String scheme = uri.getScheme();

    // macro: URIs can only use POST requests
    if (scheme.equalsIgnoreCase("macro")) {
      if (!("post".equalsIgnoreCase(method))) {
        responseHeaders.put("Status", "0");
        return "";
      }
      System.out.println("39");
      try {
        String result =
            new ThreadExecutionHelper<String>()
                .runOnSwingThread(
                    () -> {
                      String macroName = uri.getSchemeSpecificPart();

                      MapToolVariableResolver resolver = new MapToolVariableResolver(null);
                      System.out.println("macroName: " + macroName);
                      String line = MapTool.getParser().runMacro(resolver, null, macroName, body);
                      System.out.println("line: " + line);
                      return line;
                    })
                .get();
        responseHeaders.put("Status", "200");
        System.out.println("line: " + result);
        return result;
      } catch (Exception e) {
        responseHeaders.put("Status", "500");
        return e.getMessage();
      }
    }

    InputStream stream = null;
    if (scheme.equalsIgnoreCase("lib")) {
      if (!("get".equalsIgnoreCase(method))) {
        responseHeaders.put("Status", "0");
        return "";
      }

      try {
        stream = new LibraryURLConnection(uri.toURL()).getInputStream();
      } catch (IOException ioe) {
        responseHeaders.put("Status", "404");
        return ioe.getMessage();
      }
      responseHeaders.put("Status", "200");
      try {
        return new String(stream.readAllBytes(), StandardCharsets.UTF_16);
      } catch (IOException e) {
        responseHeaders.put("Status", "500");
        return e.getMessage();
      }
    }

    if (scheme.equalsIgnoreCase("asset")) {
      if (!("get".equalsIgnoreCase(method))) {
        responseHeaders.put("Status", "0");
        return "";
      }
      try {
        stream = new AssetURLStreamHandler.AssetURLConnection(uri.toURL()).getInputStream();
      } catch (IOException ioe) {
        responseHeaders.put("Status", "404");
        return ioe.getMessage();
      }
      try {
        byte[] bytes = stream.readAllBytes();
        byte[] outBytes = new byte[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
          outBytes[i * 2] = 0;
          outBytes[i * 2 + 1] = bytes[i];
        }
        responseHeaders.put("Status", "200");
        return new String(outBytes, StandardCharsets.UTF_16);
      } catch (IOException e) {
        responseHeaders.put("Status", "500");
        return e.getMessage();
      }
    }

    return null;
  }
}
