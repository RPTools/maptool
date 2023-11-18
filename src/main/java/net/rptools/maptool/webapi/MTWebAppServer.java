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
package net.rptools.maptool.webapi;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class MTWebAppServer {
  public static final String WEBAPP_CONTEXT_PATH = "webapi";

  /** The port to listen on. */
  private int port = 8000;

  /** The embedded Jetty server we will use. */
  private Server server = null;

  /** Has the server been started. */
  private boolean started = false;

  /** Extra resource directories. */
  private Map<String, String> resourceDirs = new HashMap<>();

  public synchronized void addResourceDir(String contextPath, String dirname) {
    if (started) {
      System.err.println("Can not add a resource directory to a running Webapp server.");
      throw new IllegalArgumentException(
          "Can not add a resource directory to a running Webapp server.");
    }
    resourceDirs.put(contextPath, dirname);
  }

  /**
   * Sets the port
   *
   * @param port the port for the server
   */
  public synchronized void setPort(int port) {
    if (started) {
      System.err.println("Can not change port of running Webapp server.");
      throw new IllegalArgumentException("Can not change port of running Webapp server.");
    }
    this.port = port;
  }

  /** Starts the server at the specified port. */
  public synchronized void startServer() {
    if (started) {
      return; // Do nothing as its already running.
    }

    System.out.println("DEBUG: Starting server on port " + this.port + "...");
    server = new org.eclipse.jetty.server.Server(this.port);

    // set up the web socket handler
    ContextHandler contextHandler = new ContextHandler();
    contextHandler.setContextPath("/ws");
    contextHandler.setHandler(
        new WebSocketHandler() {

          @Override
          public void configure(WebSocketServletFactory factory) {
            factory.setCreator(
                (req, resp) -> {
                  String query = req.getRequestURI().toString();
                  if ((query == null) || (query.length() <= 0)) {
                    try {
                      resp.sendForbidden("DEBUG: Unspecified query");
                    } catch (IOException e) {

                    }

                    return null;
                  }

                  return new MTWebSocket();
                });
          }
        });

    WebAppContext webAppContext = new WebAppContext();
    webAppContext.setResourceBase(
        this.getClass().getResource("/net/rptools/maptool/webapp").toExternalForm());
    webAppContext.setContextPath("/" + WEBAPP_CONTEXT_PATH);
    webAppContext.setLogger(new StdErrLog());

    HandlerList handlers = new HandlerList();

    handlers.addHandler(contextHandler);
    handlers.addHandler(webAppContext);

    for (Map.Entry<String, String> res : resourceDirs.entrySet()) {
      ContextHandler context = new ContextHandler();
      context.setWelcomeFiles(new String[] {"index.html"});
      context.setContextPath(res.getKey());

      ResourceHandler resourceHandler = new ResourceHandler();
      resourceHandler.setDirectoriesListed(true);
      resourceHandler.setWelcomeFiles(new String[] {"index.html"});
      resourceHandler.setResourceBase(res.getValue());

      context.setHandler(resourceHandler);
      handlers.addHandler(context);

      System.err.println("DEBUG: name=" + res.getKey() + ": value=" + res.getValue());
    }

    ContextHandler tokenImageContext = new ContextHandler();
    tokenImageContext.setContextPath("/token");
    tokenImageContext.setLogger(new StdErrLog());
    tokenImageContext.setHandler(new TokenImageHandler());

    handlers.addHandler(tokenImageContext);

    server.setHandler(handlers);

    try {
      server.start();
    } catch (Exception e) {

    }
    System.out.println("DEBUG: Started.");
    started = true;

    // Set up the heartbeat
    ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    ses.scheduleAtFixedRate(
        () -> {
          JsonObject data = new JsonObject();
          MTWebClientManager.getInstance().sendToAllSessions("keepalive", data);
        },
        1,
        1,
        TimeUnit.MINUTES);

    try {
      String address = InetAddress.getLocalHost().getHostAddress();
      String portString = Integer.toString(port);
      MapTool.addLocalMessage(
          I18N.getText("webapp.serverStarted", address, portString, WEBAPP_CONTEXT_PATH));
    } catch (UnknownHostException e) {
      e.printStackTrace();
      // FIXME: log this error
    }
  }

  public boolean hasStarted() {
    return started;
  }
}
