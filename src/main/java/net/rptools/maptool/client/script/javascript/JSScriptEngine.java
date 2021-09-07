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
package net.rptools.maptool.client.script.javascript;

import com.oracle.truffle.js.scriptengine.*;
import java.util.*;
import javax.script.*;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIDefinition;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.HostAccess.*;
import org.reflections.Reflections;

public class JSScriptEngine {

  private static final JSScriptEngine jsScriptEngine = new JSScriptEngine();
  private static final Logger log = LogManager.getLogger(JSScriptEngine.class);

  private static Context context;
  private static Value bindings;

  private void registerAPIObject(Value bindings, MapToolJSAPIInterface apiObj) {
    MapToolJSAPIDefinition def = apiObj.getClass().getAnnotation(MapToolJSAPIDefinition.class);
    bindings.putMember(def.javaScriptVariableName(), apiObj);
  }

  private JSScriptEngine() {
    HostAccess.Builder habuilder = HostAccess.newBuilder();
    habuilder.allowAccessAnnotatedBy(HostAccess.Export.class);
    habuilder.allowArrayAccess(true);
    habuilder.allowListAccess(true);
    habuilder.targetTypeMapping(
        Value.class, Object.class, (v) -> v.hasArrayElements(), (v) -> v.as(List.class));

    HostAccess access = habuilder.build();

    Context.Builder cbuilder = Context.newBuilder("js");
    cbuilder.allowHostAccess(access);
    cbuilder.option("js.ecmascript-version", "2021");

    context = cbuilder.build();
    bindings = context.getBindings("js");

    Reflections reflections = new Reflections("net.rptools.maptool.client.script.javascript.api");
    Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(MapToolJSAPIDefinition.class);

    for (Class<?> apiClass : annotated) {
      try {
        if (MapToolJSAPIInterface.class.isAssignableFrom(apiClass)) {
          registerAPIObject(bindings, (MapToolJSAPIInterface) apiClass.newInstance());
        } else {
          log.error("Could not add API object " + apiClass.getName() + " (missing interface)");
        }
      } catch (Exception e) {
        log.error("Could not add API object " + apiClass.getName(), e);
      }
    }
  }

  public static JSScriptEngine getJSScriptEngine() {
    return jsScriptEngine;
  }

  public Value evalAnonymous(String script) throws ScriptException {

    StringBuilder wrapped = new StringBuilder();
    wrapped
        .append("(function() { var args = MTScript.getMTScriptCallingArgs(); ")
        .append(script)
        .append("})();");
    return context.eval("js", wrapped.toString());
  }
}
