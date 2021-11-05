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
package net.rptools.maptool.api.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import net.rptools.maptool.api.ApiData;
import net.rptools.maptool.api.ApiException;
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
