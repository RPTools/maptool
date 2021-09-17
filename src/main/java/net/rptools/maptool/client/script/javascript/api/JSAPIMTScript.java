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
package net.rptools.maptool.client.script.javascript.api;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.functions.MacroJavaScriptBridge;
import net.rptools.maptool.client.functions.exceptions.*;
import net.rptools.maptool.client.script.javascript.*;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.*;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.graalvm.polyglot.*;

@MapToolJSAPIDefinition(javaScriptVariableName = "MTScript")
/** Class used to provide an API to interact with MapTool custom scripting language. */
public class JSAPIMTScript implements MapToolJSAPIInterface {
  @Override
  public String serializeToString() {
    return "MTScript";
  }

  @HostAccess.Export
  public void registerMacro(String macroName, Function callable) throws ParserException {
    boolean trusted = JSScriptEngine.inTrustedContext();
    if (macroName.equals("eval")
        || macroName.equals("evalNS")
        || macroName.equals("evalURI")
        || macroName.equals("removeNS")
        || macroName.equals("createNS")) {
      throw new ParserException(I18N.getText("macro.function.general.reservedJS", macroName));
    }
    if (trusted) {
      new JSAPIRegisteredMacro(macroName, callable);
    }
  }

  @HostAccess.Export
  public Object getVariable(String name) throws ParserException {
    return MacroJavaScriptBridge.getInstance().getMTScriptVariable(name);
  }

  @HostAccess.Export
  public void setVariable(String name, Value value) throws ParserException {
    MacroJavaScriptBridge.getInstance().setMTScriptVariable(name, value);
  }

  @HostAccess.Export
  public void raiseError(String msg) throws ParserException {
    throw new JavascriptFunctionException(msg);
  }

  @HostAccess.Export
  public void abort() throws ParserException {
    throw new AbortFunctionException(
        I18N.getText("macro.function.abortFunction.message", "MTScript.abort()"));
  }

  @HostAccess.Export
  public void mtsAssert(boolean check, String message) throws AssertFunctionException {
    mtsAssert(check, message, true);
  }

  @HostAccess.Export
  public void mtsAssert(boolean check, String message, boolean padError)
      throws AssertFunctionException {
    if (!check) {
      if (padError) {
        throw new AssertFunctionException(I18N.getText("macro.function.assert.message", message));
      }
      throw new AssertFunctionException(message);
    }
  }

  @HostAccess.Export
  public Object execMacro(String macro) throws ParserException {
    Token tokenInContext = MacroJavaScriptBridge.getInstance().getTokenInContext();
    MapToolVariableResolver res = new MapToolVariableResolver(tokenInContext);
    return _evalMacro(res, tokenInContext, macro);
  }

  @HostAccess.Export
  public Object evalMacro(String macro) throws ParserException {
    Token tokenInContext = MacroJavaScriptBridge.getInstance().getTokenInContext();
    MapToolVariableResolver res = MacroJavaScriptBridge.getInstance().getVariableResolver();
    return _evalMacro(res, tokenInContext, macro);
  }

  private Object _evalMacro(MapToolVariableResolver res, Token tokenInContext, String line)
      throws ParserException {
    MapToolMacroContext context =
        new MapToolMacroContext(
            "<javascript>",
            MapTool.getParser().getContext().getSource(),
            JSScriptEngine.inTrustedContext());
    String ret = MapTool.getParser().parseLine(res, tokenInContext, line, context);
    try {
      return new BigDecimal(ret);
    } catch (Exception e) {
      return ret;
    }
  }

  @HostAccess.Export
  public List<Object> getMTScriptCallingArgs() {
    return MacroJavaScriptBridge.getInstance().getCallingArgs();
  }
}
