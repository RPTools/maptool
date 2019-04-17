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
package net.rptools.maptool.client.ui.syntax;

import java.util.Map;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.DefinesSpecialVariables;
import net.rptools.maptool.client.functions.UserDefinedMacroFunctions;
import net.rptools.parser.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

public class MapToolScriptSyntax extends MapToolScriptTokenMaker {
  private static final Logger log = LogManager.getLogger(MapToolScriptSyntax.class);

  static volatile TokenMap macroFunctionTokenMap;

  static String[] DATA_TYPES = {
    "bar.name",
    "macro.args",
    "roll.count",
    "roll.result",
    "state.name",
    "token.gm_name",
    "token.halo",
    "token.init",
    "token.initHold",
    "token.label",
    "token.name",
    "token.visible",
    "tokens.denyMove",
    "tokens.moveCount"
  };

  static String[] RESERVED_WORDS = {
    "c",
    "code",
    "count",
    "dialog",
    "e",
    "expanded",
    "for",
    "foreach",
    "frame",
    "g",
    "gm",
    "gmtt",
    "gt",
    "h",
    "hidden",
    "hide",
    "if",
    "macro",
    "r",
    "result",
    "s",
    "self",
    "selftt",
    "st",
    "switch",
    "t",
    "token",
    "tooltip",
    "u",
    "unformatted",
    "w",
    "while",
    "whisper"
  };

  static String[] RESERVED_WORDS_2 = {
    "onCampaignLoad", "onChangeSelection", "onMouseOverEvent", "onMultipleTokensMove", "onTokenMove"
  };

  static String[] OPERATORS = {
    "!", "&&", "*", "+", ",", "-", "/", ":", ";", "<", "<=", "=", "==", ">", ">=", "||"
  };

  public MapToolScriptSyntax() {
    // Get all the macro functions defined in the parser
    macroFunctionTokenMap = getMacroFunctionNames();

    // Add "Special Variables" as Data Type
    for (String dataType : DATA_TYPES) macroFunctionTokenMap.put(dataType, Token.DATA_TYPE);

    // Add "Roll Options" as Reserved word
    for (String reservedWord : RESERVED_WORDS)
      macroFunctionTokenMap.put(reservedWord, Token.RESERVED_WORD);

    // Add "Events" as Reserved Word 2
    for (String reservedWord2 : RESERVED_WORDS_2)
      macroFunctionTokenMap.put(reservedWord2, Token.RESERVED_WORD_2);

    // Add "Operators" as OPERATOR
    for (String operators : OPERATORS) macroFunctionTokenMap.put(operators, Token.OPERATOR);

    // Add "highlights defined by functions like Special Variables" as Data Type
    for (Function function : MapTool.getParser().getMacroFunctions()) {
      if (function instanceof DefinesSpecialVariables) {
        for (String specialVariable : ((DefinesSpecialVariables) function).getSpecialVariables()) {
          macroFunctionTokenMap.put(specialVariable, Token.DATA_TYPE);
        }
      }
    }
  }

  @Override
  public void addToken(
      char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
    // This assumes all of your extra tokens would normally be scanned as IDENTIFIER.
    if (tokenType == TokenTypes.IDENTIFIER) {
      int newType = getMacroFunctionNames().get(array, start, end);
      if (newType > -1) {
        tokenType = newType;
      }
    }

    super.addToken(array, start, end, tokenType, startOffset, hyperlink);
  }

  public static void resetScriptSyntax() {
    macroFunctionTokenMap = null;
  }

  private TokenMap getMacroFunctionNames() {
    if (macroFunctionTokenMap == null) {
      synchronized (MapToolScriptSyntax.class) {
        macroFunctionTokenMap = new TokenMap(true);

        Map<String, String> macroMap = MapTool.getParser().listAllMacroFunctions();

        for (String macro : macroMap.keySet()) {
          if (macroMap.get(macro).equals(UserDefinedMacroFunctions.class.getName()))
            macroFunctionTokenMap.put(macro, Token.ANNOTATION);
          else macroFunctionTokenMap.put(macro, Token.FUNCTION);

          log.debug("Adding \"" + macro + "\" macro function to syntax highlighting.");
        }
      }
    }

    return macroFunctionTokenMap;
  }
}
