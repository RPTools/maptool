package net.rptools.maptool.webendpoint.servlet;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import javax.servlet.ServletException;
import net.rptools.maptool.model.Path;

public class WebEndPointServletServer  {

  private static String CONTEXT_PATH = "/";
  private static String DEPLOYMENT_NAME = "maptool";


  private final PathHandler pathHandler;

  public WebEndPointServletServer() throws ServletException {
    DeploymentInfo deploymentInfo =
        Servlets.deployment().setClassLoader(WebEndPointServletServer.class.getClassLoader())
            .setContextPath(CONTEXT_PATH)
            .setDeploymentName(DEPLOYMENT_NAME);
    new WebEndPointServletManager().getServlets().forEach(deploymentInfo::addServlet);

    DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(deploymentInfo);
    deploymentManager.deploy();

    HttpHandler servletHandler = deploymentManager.start();

    pathHandler = Handlers.path(Handlers.redirect(DEPLOYMENT_NAME)).addPrefixPath(DEPLOYMENT_NAME
        , servletHandler);
  }

  public PathHandler getPathHandler() {
    return pathHandler;
  }
}
