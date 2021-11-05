package net.rptools.maptool.api.maptool;

import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.client.MapTool;

//@XmlRootElement
public record MapToolInfo(
    String mapToolVersion,
    String webEndpointVersion,
    boolean developmentVersion
) implements ApiData {

  public MapToolInfo() {
    this(MapTool.getVersion(), "unavailable", MapTool.isDevelopment());
  }
}
