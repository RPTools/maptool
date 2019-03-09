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

import net.rptools.maptool.client.MapTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

public class MapToolScriptSyntax extends MapToolScriptTokenMaker {
  private static final Logger log = LogManager.getLogger(MapToolScriptSyntax.class);

  static TokenMap macroFunctionTokenMap;

  static String[] DATA_TYPES = {
    "bar.name",
    "macro.args",
    "macro.return",
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
    for (String reservedWord : RESERVED_WORDS_2)
      macroFunctionTokenMap.put(reservedWord, Token.RESERVED_WORD_2);

    // Add "Events" as OPERATOR
    for (String reservedWord : OPERATORS) macroFunctionTokenMap.put(reservedWord, Token.OPERATOR);
  }

  @Override
  public void addToken(
      char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
    // This assumes all of your extra tokens would normally be scanned as IDENTIFIER.
    if (tokenType == TokenTypes.IDENTIFIER) {
      int newType = macroFunctionTokenMap.get(array, start, end);
      if (newType > -1) {
        tokenType = newType;
      }
    }

    super.addToken(array, start, end, tokenType, startOffset, hyperlink);
  }

  private TokenMap getMacroFunctionNames() {
    if (macroFunctionTokenMap == null) {
      macroFunctionTokenMap = new TokenMap(true);

      for (String macro : MapTool.getParser().listAllMacroFunctions()) {
        macroFunctionTokenMap.put(macro, Token.FUNCTION);
        log.debug("Adding \"" + macro + "\" macro function to syntax highlighting.");
      }
    }

    return macroFunctionTokenMap;
  }
}
