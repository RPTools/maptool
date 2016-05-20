/*
 * This software Copyright by the RPTools.net development team, and licensed
 * under the GPL Version 3 or, at your option, any later version.
 *
 * MapTool 2 Source Code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this source Code. If not, see <http://www.gnu.org/licenses/>
 */
package net.rptools.maptool.client.script.javascript;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class JSScriptEngine {

	private static JSScriptEngine jsScriptEngine = new JSScriptEngine();
	private static final Logger log = Logger.getLogger(JSScriptEngine.class);

	private ScriptEngine engine;

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

	private JSScriptEngine() {
		engine = new NashornScriptEngineFactory().getScriptEngine(new JSClassFilter());
		try {
			engine.eval("var MTScript = {}");
			engine.eval("MTScript = Java.type('net.rptools.maptool.client.script.javascript.api.MTScript')");
		} catch (ScriptException e) {
			log.error("Could not initialize JavaScript Engine.", e);
		}
	}

	public static JSScriptEngine getJSScriptEngine() {
		return jsScriptEngine;
	}

	public Object evalAnonymous(String script) throws ScriptException {

		StringBuilder wrapped = new StringBuilder();
		wrapped.append("(function() {").append(script).append("})();");
		return engine.eval(wrapped.toString());
	}
}
