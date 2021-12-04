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
package net.rptools.maptool.client.ui.htmlframe;

import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import javafx.scene.web.*;
import javax.swing.*;
import net.rptools.maptool.model.library.url.*;
import net.rptools.maptool.util.threads.ThreadExecutionHelper;
import netscape.javascript.*;
import org.w3c.dom.*;
import org.w3c.dom.html.*;

public class MTXMLHttpRequest {
  // The javascript counterpart to this request.
  private JSObject ctx = null;

  // Used for resolving relative resources.
  private String href;

  // Private state for the request side
  private String uri = null;
  private String method = null;
  private String body = null;
  private boolean async = false;
  private String user = null;
  private String psw = null;
  private String responseType = "text";
  private int readyState = 0;
  private String status = null;
  private HashMap<String, String> requestHeaders = null;

  // Private state for the response side
  private HashMap<String, String> responseHeaders = null;

  private static boolean warned = false;

  // Public methods

  public void _getResponseHeaders(JSObject jheaders) {

    responseHeaders.forEach(
        (key, value) -> {
          if (!key.equals(":Status")) {
            jheaders.setMember(key, value);
          }
        });
  }

  public int getReadyState() {
    return readyState;
  }

  public void setResponseType(String typ) {
    if (readyState > 1) {
      throw new JSException(
          "Failed to set the 'responseType' property on 'XMLHttpRequest': The response type cannot be set if the object's state is LOADING or DONE");
    }
    this.responseType = typ;
  }

  public String getResponseType() {
    return this.responseType;
  }

  public void open(String method, String uri, boolean async, String user, String psw) {
    this.uri = uri;
    this.method = method;
    this.async = async;
    this.user = user;
    this.psw = psw;
    readyState = 1;
    this.readyStateChanged();
    if (!async && !warned) {
      this.ctx.call("_warnAsync");
      warned = true;
    }
  }

  public void send(String body) {
    if (readyState != 1) {
      throw new JSException(
          "Failed to execute 'send' on 'XMLHttpRequest': The object's state must be OPENED");
    }
    readyState = 2;
    this.body = body;
    this.readyStateChanged();

    CompletableFuture<String> d = this.processRequest();
    if (!this.async) {
      try {
        this.recv(d.get());
      } catch (Exception e) {
        this.recv(e.getMessage());
      }
    } else {
      d.thenApply(
          (String result) -> {
            new ThreadExecutionHelper<Void>()
                .runOnJFXThread(
                    () -> {
                      this.recv(result);
                      return null;
                    });
            return result;
          });
    }
  }

  public String getStatus() {
    return this.status;
  }

  public void recv(String respBody) {
    this.status = this.responseHeaders.remove(":Status");
    switch (responseType) {
      case "blob":
        this.ctx.call("_makeBlob", respBody);
        break;
      case "arraybuffer":
        this.ctx.call("_makeArrayBuffer", respBody);
        break;
      case "document":
        this.ctx.call("_makeDocument", respBody);
        break;
      case "json":
        this.ctx.call("_makeJson", respBody);
        break;
      case "":
      case "text":
      default:
        this.ctx.call("_makeText", respBody);
        break;
    }
    readyState = 4;
    this.readyStateChanged();
  }

  public void abort() {
    if (readyState > 1) {
      readyState = 0;
    }
  }

  public String getAllResponseHeaders() {
    if (readyState != 4) {
      return null;
    }

    ArrayList<String> lines = new ArrayList<>();
    for (Map.Entry<String, String> set : this.responseHeaders.entrySet()) {
      lines.add(set.getKey() + ": " + set.getValue());
    }
    return String.join("\n", lines);
  }

  public String getResponseHeader(String name) {
    if (readyState == 4) {
      if (this.responseHeaders.containsKey(name)) {
        return this.responseHeaders.get(name);
      }
    }
    return null;
  }

  public void setRequestHeader(String name, String value) {
    this.requestHeaders.put(name, value);
  }

  // Methods
  // setRequestHeader

  public MTXMLHttpRequest(JSObject ctx, String href) {
    this.ctx = ctx;
    this.href = href;
    this.requestHeaders = new HashMap<>();
    this.responseHeaders = new HashMap<>();
  }

  private void readyStateChanged() {
    this.ctx.call("onreadystatechange");
  }

  private CompletableFuture<String> processRequest() {
    URI _uri;
    try {
      _uri = new URI(this.href).resolve(this.uri);
    } catch (Exception e) {
      readyState = 0;
      CompletableFuture c = new CompletableFuture<String>();
      c.complete(e.getMessage());
      return c;
    }

    return RequestHandler.processRequest(method, _uri, body, requestHeaders, responseHeaders);
  }
}
