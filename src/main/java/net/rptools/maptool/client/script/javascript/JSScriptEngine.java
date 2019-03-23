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

import java.util.Set;
import javax.script.*;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIDefinition;
import net.rptools.maptool.client.script.javascript.api.MapToolJSAPIInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

public class JSScriptEngine {

  private static JSScriptEngine jsScriptEngine = new JSScriptEngine();
  private static final Logger log = LogManager.getLogger(JSScriptEngine.class);

  private ScriptEngine engine;
  private ScriptContext anonymousContext;

  public class JSClassFilter implements ClassFilter {

    @Override
    public boolean exposeToScripts(String jclass) {
      if (jclass.equals("java.lang.Math")) {
        return true;
      }
      if (jclass.equals("java.lang.Boolean")) {
        return true;
      }
      if (jclass.equals("java.lang.Byte")) {
        return true;
      }
      if (jclass.equals("java.lang.Character")) {
        return true;
      }
      if (jclass.equals("java.lang.Double")) {
        return true;
      }
      if (jclass.equals("java.lang.Float")) {
        return true;
      }
      if (jclass.equals("java.lang.Long")) {
        return true;
      }
      if (jclass.equals("java.lang.Number")) {
        return true;
      }
      if (jclass.equals("java.lang.Short")) {
        return true;
      }
      if (jclass.equals("java.lang.StrictMath")) {
        return true;
      }
      if (jclass.equals("java.lang.String")) {
        return true;
      }
      if (jclass.startsWith("java.lang.")) {
        return false;
      }
      if (jclass.startsWith("java.util.Timer")) {
        return false;
      }
      if (jclass.startsWith("java.util.concurrent")) {
        return false;
      }
      if (jclass.startsWith("java.util.concurrent.atomic")) {
        return false;
      }
      if (jclass.startsWith("java.util.concurrent.locks")) {
        return false;
      }
      if (jclass.startsWith("java.util.function")) {
        return false;
      }
      if (jclass.startsWith("java.util.jar")) {
        return false;
      }
      if (jclass.startsWith("java.util.logging")) {
        return false;
      }
      if (jclass.startsWith("java.util.spi")) {
        return false;
      }
      if (jclass.startsWith("java.util.zip")) {
        return false;
      }
      if (jclass.startsWith("java.util.")) {
        return true;
      }
      if (jclass.startsWith("net.rptools.maptool.client.script.javascript.api.")) {
        return true;
      }

      return false;
    }
  }

  private void registerAPIObject(ScriptContext context, MapToolJSAPIInterface apiObj)
      throws ScriptException {
    MapToolJSAPIDefinition def = apiObj.getClass().getAnnotation(MapToolJSAPIDefinition.class);
    Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
    bindings.put(def.javaScriptVariableName(), apiObj);
  }

  private JSScriptEngine() {
    engine = new NashornScriptEngineFactory().getScriptEngine(new JSClassFilter());
    anonymousContext = new SimpleScriptContext();
    anonymousContext.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
    Reflections reflections = new Reflections("net.rptools.maptool.client.script.javascript.api");
    Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(MapToolJSAPIDefinition.class);

    for (Class<?> apiClass : annotated) {
      try {
        if (MapToolJSAPIInterface.class.isAssignableFrom(apiClass)) {
          registerAPIObject(anonymousContext, (MapToolJSAPIInterface) apiClass.newInstance());
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

  public Object evalAnonymous(String script) throws ScriptException {

    StringBuilder wrapped = new StringBuilder();
    wrapped
        .append("(function() { var args = MTScript.getMTScriptCallingArgs(); ")
        .append(script)
        .append("})();");
    return engine.eval(wrapped.toString(), anonymousContext);
  }
}
