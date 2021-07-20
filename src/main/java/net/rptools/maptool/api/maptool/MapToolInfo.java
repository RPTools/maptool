package net.rptools.maptool.api.maptool;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.webendpoint.WebEndPoint;

public record MapToolInfo(
    String mapToolVersion,
    String webEndpointVersion,
    boolean developmentVersion
) {

  public MapToolInfo() {
    this(MapTool.getVersion(), WebEndPoint.getWebEndPointVersion(), MapTool.isDevelopment());
  }
}
