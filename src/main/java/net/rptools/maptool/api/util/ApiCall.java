package net.rptools.maptool.api.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;

public class ApiCall<T extends ApiData> {

  public CompletableFuture<ApiResult<T>> runOnSwingThread(Callable<T> callable) {
    try {
      if (SwingUtilities.isEventDispatchThread()) {
        return CompletableFuture.completedFuture(doCall(callable));
      } else {
        return CompletableFuture.supplyAsync(() -> doCall(callable));
      }
    } catch (Exception e) {
      // TODO: CDW: Log this error
      return CompletableFuture.completedFuture(
          new ApiResult<>(new ApiException("err.internal", e)));
    }
  }

  private ApiResult<T> doCall(Callable<T> callable) {
    try {
      return new ApiResult<T>(callable.call());
    } catch (Exception e) {
      // TODO: CDW: Log this error
      return new ApiResult<>(new ApiException("err.internal", e));
    }
  }

}
