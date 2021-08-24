package net.rptools.maptool.api.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;
import net.rptools.maptool.model.player.PasswordFilePlayerDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiCall<T extends ApiData> {

  private static final Logger log = LogManager.getLogger(ApiCall.class);

  public CompletableFuture<ApiResult<T>> runOnSwingThread(Callable<T> callable) {
    try {
      if (SwingUtilities.isEventDispatchThread()) {
        return CompletableFuture.completedFuture(doCall(callable));
      } else {
        return CompletableFuture.supplyAsync(() -> doCall(callable));
      }
    } catch (Exception e) {
      log.error(e);
      return CompletableFuture.completedFuture(
          new ApiResult<>(new ApiException("err.internal", e)));
    }
  }

  private ApiResult<T> doCall(Callable<T> callable) {
    try {
      return new ApiResult<T>(callable.call());
    } catch (Exception e) {
      log.error(e);
      return new ApiResult<>(new ApiException("err.internal", e));
    }
  }

}
