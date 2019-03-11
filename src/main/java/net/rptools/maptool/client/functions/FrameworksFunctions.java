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
package net.rptools.maptool.client.functions;

import com.twelvemonkeys.lang.StringUtil;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.macro.Macro;
import net.rptools.maptool.client.macro.MacroDefinition;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.client.ui.syntax.MapToolScriptSyntax;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.Function;
import net.rptools.parser.function.ParameterException;

/** @author oliver.szymanski */
public class FrameworksFunctions implements Function {
  private static final FrameworksFunctions instance = new FrameworksFunctions();
  private static final String IMPORT_FUNCTION_NAME = "importFramework";
  private static final String INIT_FUNCTION_NAME = "initFrameworks";

  private final int minParameters;
  private final int maxParameters;
  private final boolean deterministic;

  private FrameworksFunctions() {
    this.minParameters = 2;
    this.maxParameters = 2;
    this.deterministic = true;
    init();
  }

  private void init() {
    frameworkFunctions.clear();
    frameworkFunctionsAliasMap.clear();
    frameworkAliasPrefixMap.clear();

    frameworkFunctions.add(this);
    frameworkFunctionsAliasMap.put(IMPORT_FUNCTION_NAME, this);
    frameworkAliasPrefixMap.put(IMPORT_FUNCTION_NAME, IMPORT_FUNCTION_NAME);
    frameworkFunctionsAliasMap.put(INIT_FUNCTION_NAME, this);
    frameworkAliasPrefixMap.put(INIT_FUNCTION_NAME, INIT_FUNCTION_NAME);
  }

  private final List<Function> frameworkFunctions = new LinkedList<>();
  private final Map<String, Function> frameworkFunctionsAliasMap = new HashMap<>();
  private final Map<String, String> frameworkAliasPrefixMap = new HashMap<>();

  public static FrameworksFunctions getInstance() {
    return instance;
  }

  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }

    if (functionName.equals(IMPORT_FUNCTION_NAME)) {
      return importFunction(functionName, parameters);
    } else if (functionName.equals(INIT_FUNCTION_NAME)) {
      init();
      return BigDecimal.ONE;
    } else {
      return executeFunction(parser, functionName, parameters);
    }
  }

  private Object importFunction(String functionName, List<Object> parameters)
      throws ParameterException, ParserException {
    this.checkParameters(parameters);

    if (parameters.size() < 2) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
    }

    List<String> newFunctionNames = new LinkedList<String>();
    List<String> newChatMacros = new LinkedList<String>();

    String prefix = FunctionCaller.getParam(parameters, 0);
    String frameworkName = FunctionCaller.getParam(parameters, 1);

    if (!StringUtil.isEmpty(prefix)) {
      prefix = prefix + "_";
    }

    try {
      Framework framework =
          (Framework)
              Class.forName(frameworkName.toString()).getDeclaredConstructor().newInstance();
      Collection<? extends Function> functions = framework.getFunctions();
      Collection<? extends Macro> chatMacros = framework.getChatMacros();

      for (Function function : functions) {
        if (frameworkFunctions.contains(function)) {
          // if overridden remove and add again
          frameworkFunctions.remove(function);
        }
        frameworkFunctions.add(function);

        for (String alias : function.getAliases()) {
          String aliasWithPrefix = prefix + alias;
          frameworkAliasPrefixMap.put(aliasWithPrefix, alias);
          frameworkFunctionsAliasMap.put(aliasWithPrefix, function);
          newFunctionNames.add(aliasWithPrefix);
        }
      }

      for (Macro chatMacro : chatMacros) {
        MacroDefinition macroDefinition = chatMacro.getClass().getAnnotation(MacroDefinition.class);
        if (macroDefinition == null) continue;

        MacroManager.registerMacro(chatMacro);
        newChatMacros.add(macroDefinition.name());
      }
    } catch (Exception e) {
      throw new ParserException(e);
    }

    MapToolScriptSyntax.resetScriptSyntax();

    return "<br>"
        + (newFunctionNames.stream().collect(Collectors.joining(", ")))
        + " framework functions defined.";
  }

  private Object executeFunction(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    String alias = frameworkAliasPrefixMap.get(functionName);
    Function function = frameworkFunctionsAliasMap.get(functionName);

    if (function != null) {
      if (function instanceof PrefixAware) {
        String prefix = functionName.substring(0, functionName.length() - alias.length());
        ((PrefixAware) function).setPrefix(prefix);
      }

      Object result = function.evaluate(parser, alias, parameters);

      if (function instanceof PrefixAware) {
        ((PrefixAware) function).setPrefix(null);
      }

      return result;
    }

    return BigDecimal.ZERO;
  }

  public String[] getAliases() {
    String[] aliases = new String[frameworkFunctionsAliasMap.keySet().size()];

    aliases = frameworkFunctionsAliasMap.keySet().toArray(aliases);
    if (aliases == null) {
      return new String[0];
    } else {
      return aliases;
    }
  }

  public int getMinimumParameterCount() {
    return this.minParameters;
  }

  public int getMaximumParameterCount() {
    return this.maxParameters;
  }

  public boolean isDeterministic() {
    return this.deterministic;
  }

  public void checkParameters(List<Object> parameters) throws ParameterException {
    int pCount = parameters == null ? 0 : parameters.size();
    if (pCount < this.minParameters
        || this.maxParameters != -1 && parameters.size() > this.maxParameters) {
      throw new ParameterException(
          String.format(
              "Invalid number of parameters %d, expected %s",
              pCount, this.formatExpectedParameterString()));
    }
  }

  public final Object evaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    return this.childEvaluate(parser, functionName, parameters);
  }

  private String formatExpectedParameterString() {
    if (this.minParameters == this.maxParameters) {
      return String.format("exactly %d parameter(s)", this.maxParameters);
    }
    if (this.maxParameters == -1) {
      return String.format("at least %d parameters", this.minParameters);
    }
    return String.format("between %d and %d parameters", this.minParameters, this.maxParameters);
  }

  public static interface PrefixAware {

    void setPrefix(String prefix);
  }

  public static interface Framework {

    Collection<? extends Function> getFunctions();

    Collection<? extends Macro> getChatMacros();
  }

  public static class FunctionCaller {

    public static Object callFunction(
        String functionName, Function f, Parser parser, Object... parameters)
        throws ParserException {
      return f.evaluate(parser, functionName, Arrays.asList(parameters));
    }

    public static Object callFunction(String functionName, Parser parser, Object... parameters)
        throws ParserException {
      Function f = parser.getFunction(functionName);
      return f.evaluate(parser, functionName, Arrays.asList(parameters));
    }

    public static List<Object> toObjectList(Object... parameters) {
      return Arrays.asList(parameters);
    }

    public static <T> T getParam(List<Object> parameters, int i) {
      return getParam(parameters, i, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getParam(List<Object> parameters, int i, T defaultValue) {
      if (parameters != null && parameters.size() > i) {
        return (T) parameters.get(i);
      } else {
        return defaultValue;
      }
    }

    public static boolean toBoolean(Object val) {
      if (val instanceof BigDecimal) {
        return BigDecimal.ZERO.equals(val) ? false : true;
      } else if (val instanceof Boolean) {
        return (Boolean) val;
      }

      return false;
    }
  }
}
