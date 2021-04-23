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
package net.rptools.maptool.client;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.rptools.common.expression.ExpressionParser;
import net.rptools.maptool.client.functions.*;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.Function;

public class MapToolExpressionParser extends ExpressionParser {

  /** MapTool functions to add to the parser. */
  private static final List<Function> mapToolParserFunctions =
      Stream.of(
              AbortFunction.getInstance(),
              AssertFunction.getInstance(),
              AddAllToInitiativeFunction.getInstance(),
              ChatFunction.getInstance(),
              CurrentInitiativeFunction.getInstance(),
              DefineMacroFunction.getInstance(),
              EvalMacroFunctions.getInstance(),
              ExecFunction.getInstance(),
              FindTokenFunctions.getInstance(),
              HasImpersonated.getInstance(),
              InitiativeRoundFunction.getInstance(),
              InputFunction.getInstance(),
              IsTrustedFunction.getInstance(),
              JSONMacroFunctions.getInstance(),
              LookupTableFunction.getInstance(),
              MacroArgsFunctions.getInstance(),
              MacroDialogFunctions.getInstance(),
              MacroFunctions.getInstance(),
              MacroLinkFunction.getInstance(),
              MapFunctions.getInstance(),
              MiscInitiativeFunction.getInstance(),
              PlayerFunctions.getInstance(),
              RemoveAllFromInitiativeFunction.getInstance(),
              ReturnFunction.getInstance(),
              SoundFunctions.getInstance(),
              StateImageFunction.getInstance(),
              BarImageFunction.getInstance(),
              StringFunctions.getInstance(),
              StrListFunctions.getInstance(),
              StrPropFunctions.getInstance(),
              SwitchTokenFunction.getInstance(),
              TokenBarFunction.getInstance(),
              TokenCopyDeleteFunctions.getInstance(),
              TokenGMNameFunction.getInstance(),
              TokenHaloFunction.getInstance(),
              TokenImage.getInstance(),
              TokenInitFunction.getInstance(),
              TokenInitHoldFunction.getInstance(),
              TokenLabelFunction.getInstance(),
              TokenLightFunctions.getInstance(),
              TokenLocationFunctions.getInstance(),
              TokenNameFunction.getInstance(),
              TokenPropertyFunctions.getInstance(),
              TokenRemoveFromInitiativeFunction.getInstance(),
              TokenSelectionFunctions.getInstance(),
              TokenSightFunctions.getInstance(),
              TokenSpeechFunctions.getInstance(),
              TokenStateFunction.getInstance(),
              TokenVisibleFunction.getInstance(),
              isVisibleFunction.getInstance(),
              getInfoFunction.getInstance(),
              TokenMoveFunctions.getInstance(),
              FogOfWarFunctions.getInstance(),
              Topology_Functions.getInstance(),
              ZoomFunctions.getInstance(),
              ParserPropertyFunctions.getInstance(),
              MathFunctions.getInstance(),
              MacroJavaScriptBridge.getInstance(),
              DrawingGetterFunctions.getInstance(),
              DrawingSetterFunctions.getInstance(),
              DrawingMiscFunctions.getInstance(),
              ExportDataFunctions.getInstance(),
              RESTfulFunctions.getInstance(),
              HeroLabFunctions.getInstance(),
              LogFunctions.getInstance(),
              LastRolledFunction.getInstance(),
              Base64Functions.getInstance(),
              TokenTerrainModifierFunctions.getInstance(),
              TestFunctions.getInstance(),
              TextLabelFunctions.getInstance(),
              new MarkDownFunctions())
          .collect(Collectors.toList());

  public MapToolExpressionParser() {
    super.getParser().addFunctions(mapToolParserFunctions);
  }

  public static List<Function> getMacroFunctions() {
    return mapToolParserFunctions;
  }

  /**
   * Override dicelib's parser creation to inject our expression caching parser
   *
   * @return instance of parser
   */
  @Override
  protected Parser createParser() {
    return new ExpressionCachingParser();
  }

  /** Parser implementation that caches expressions in a soft value cache */
  private static class ExpressionCachingParser extends Parser {

    private final Cache<String, Expression> expressionCache =
        CacheBuilder.newBuilder().softValues().build();

    @Override
    public Expression parseExpression(String expression) throws ParserException {
      // Expression exp = super.parseExpression(expression);
      Expression exp = expressionCache.getIfPresent(expression);
      if (exp == null) {
        exp = super.parseExpression(expression);
        expressionCache.put(expression, exp);
      }
      return exp;
    }

    /**
     * Functions are only passed to the parser once, on initial create User defined functions are
     * injected here if defined in UserDefinedMacroFunctions
     *
     * @param functionName the name of the function
     * @return Either user defined function or function known to parser
     */
    @Override
    public Function getFunction(String functionName) {

      // check user defined functions first
      UserDefinedMacroFunctions userFunctions = UserDefinedMacroFunctions.getInstance();
      if (userFunctions.isFunctionDefined(functionName)) return userFunctions;

      // let parser do its thing
      return super.getFunction(functionName);
    }
  }
}
