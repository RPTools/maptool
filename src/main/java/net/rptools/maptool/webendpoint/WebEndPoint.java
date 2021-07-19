package net.rptools.maptool.webendpoint;

import io.undertow.Undertow;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.webendpoint.servlet.WebEndPointServletServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebEndPoint {

  private static final Logger log = LogManager.getLogger(WebEndPoint.class);

  private static final String WEB_ENDPOINT_VERSION = "0.0.1";

  private final static WebEndPoint webEndPoint = new WebEndPoint();
  private Undertow server;
  private int port;


  private WebEndPoint() {
    server = null;
  }

  public static String getWebEndPointVersion() {
    return WEB_ENDPOINT_VERSION;
  }
  public static WebEndPoint getWebEndPoint() {
    synchronized(webEndPoint) {
      if (webEndPoint.server == null) {
        webEndPoint.initialize(AppPreferences.getWebEndPointPort());
      }
    }
    return webEndPoint;
  }

  private void initialize(int portNumber) {
    try {
      synchronized (this) {
        if (server != null) {
          log.info(I18N.getText("msg.info.stopWebEndWebPoint", port));
          server.stop();
        }

        port = portNumber;

        server = Undertow.builder()
            .addHttpListener(port, "localhost")
            .setHandler(new WebEndPointServletServer().getPathHandler())
            .build();
      }
      log.info(I18N.getText("msg.info.startWebEndWebPoint", port));
      server.start();
    } catch (Exception e) {
      e.printStackTrace();
      // TODO: CDW: Here
    }
  }

  public synchronized int getPort() {
    return port;
  }

  public void setPort(int portNumber) {
    synchronized(this) {
      if (port != portNumber)
      initialize(portNumber);
    }
  }

}
