package net.rptools.maptool.api.maptool;

import java.util.concurrent.CompletableFuture;
import net.rptools.maptool.api.util.ApiResult;

public class MapToolApi {

  public CompletableFuture<ApiResult<MapToolInfo>> getVersion() {
    return CompletableFuture.completedFuture(new ApiResult<MapToolInfo>(new MapToolInfo()));
  }

}
