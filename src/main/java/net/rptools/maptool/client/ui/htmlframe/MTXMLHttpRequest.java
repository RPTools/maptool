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
import javafx.scene.web.*;
import javax.swing.*;
import net.rptools.maptool.model.library.url.*;
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
  private HashMap<String, String> requestHeaders = null;

  // Private state for the response side
  private HashMap<String, String> responseHeaders = null;

  // Public methods

  public void open(String method, String uri, boolean async, String user, String psw) {
    if (async) {
      throw new JSException(
          //  "Synchronous XMLHttpRequest on the main thread is denied because of its detrimental
          // effects to the end user's experience. For more help, check
          // https://xhr.spec.whatwg.org/.");
          "Asynchronous XMLHttpRequests are not yet supported");
    }
    this.uri = uri;
    this.method = method;
    this.async = async;
    this.user = user;
    this.psw = psw;
    this.ctx.setMember("readyState", 1);
    this.readyStateChanged();
  }

  public void send(String body) {
    System.out.println("5");
    if ((int) this.ctx.getMember("readyState") != 1) {
      throw new JSException(
          "Failed to execute 'send' on 'XMLHttpRequest': The object's state must be OPENED");
    }
    this.ctx.setMember("readyState", 2);
    this.body = body;
    this.readyStateChanged();

    this.processRequest();
  }

  public void abort() {
    if ((int) this.ctx.getMember("readyState") > 1) {
      this.ctx.setMember("readyState", 0);
    }
  }

  public String getAllResponseHeaders() {
    if ((int) this.ctx.getMember("readyState") != 4) {
      return null;
    }

    ArrayList<String> lines = new ArrayList<>();
    for (Map.Entry<String, String> set : this.responseHeaders.entrySet()) {
      System.out.println("4");
      lines.add(set.getKey() + ": " + set.getValue());
    }
    return String.join("\n", lines);
  }

  public String getResponseHeader(String name) {
    if ((int) this.ctx.getMember("readyState") == 4) {
      if (this.responseHeaders.containsKey(name)) {
        System.out.println("3");
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
    System.out.println("141");
    this.ctx.call("onreadystatechange");
  }

  private void processRequest() {
    System.out.println("0");
    URI _uri;
    try {
      _uri = new URI(this.href).resolve(this.uri);
    } catch (Exception e) {
      return;
    }

    System.out.println("2");
    this.ctx.setMember(
        "response",
        RequestHandler.processRequest(method, _uri, body, requestHeaders, responseHeaders));

    System.out.println("1");
  }
}
