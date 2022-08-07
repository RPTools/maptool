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
package net.rptools.maptool.util;

import java.util.concurrent.CompletableFuture;
import netscape.javascript.JSObject;

public class PromiseUtil {
  public static JSObject convertToPromise(JSObject window, CompletableFuture<Object> future) {
    JSObject deferred =
        (JSObject)
            window.call(
                "eval",
                "(()=>{let p={}; p.promise = new Promise((r, f)=>{p.resolve=r; p.reject=f;}); return p;})();");
    JSObject promise = (JSObject) deferred.getMember("promise");

    future.thenApply(
        (Object o) -> {
          deferred.call("resolve", o);
          return o;
        });
    future.exceptionally(
        (Throwable t) -> {
          deferred.call("reject", t);
          return t;
        });
    return promise;
  }
}
