package net.rptools.maptool.api.maptool;

import javax.xml.bind.annotation.XmlRootElement;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.webendpoint.WebEndPoint;

@XmlRootElement
public record MapToolInfo(
    String mapToolVersion,
    String webEndpointVersion,
    boolean developmentVersion
) implements ApiData {

  public MapToolInfo() {
    this(MapTool.getVersion(), WebEndPoint.getWebEndPointVersion(), MapTool.isDevelopment());
  }
}
