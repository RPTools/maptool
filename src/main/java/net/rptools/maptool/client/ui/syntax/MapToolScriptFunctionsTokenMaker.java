/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.ui.syntax;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;

import net.rptools.maptool.client.MapTool;

public class MapToolScriptFunctionsTokenMaker extends MapToolScriptTokenMaker {
	private static final Logger log = LogManager.getLogger(MapToolScriptFunctionsTokenMaker.class);

	static TokenMap macroFunctionTokenMap;

	public MapToolScriptFunctionsTokenMaker() {
		// Get all the macro functions defined in the parser
		macroFunctionTokenMap = getMacroFunctionNames();

		// Additional special functions
		macroFunctionTokenMap.put("onCampaignLoad", Token.RESERVED_WORD_2);
		macroFunctionTokenMap.put("onChangeSelection", Token.RESERVED_WORD_2);
		macroFunctionTokenMap.put("onMouseOverEvent", Token.RESERVED_WORD_2);
		macroFunctionTokenMap.put("onMultipleTokensMove", Token.RESERVED_WORD_2);
		macroFunctionTokenMap.put("onTokenMove", Token.RESERVED_WORD_2);
	}

	@Override
	public void addToken(char[] array, int start, int end, int tokenType, int startOffset, boolean hyperlink) {
		// This assumes all of your extra tokens would normally be scanned as IDENTIFIER.
		if (tokenType == TokenTypes.IDENTIFIER) {
			int newType = macroFunctionTokenMap.get(array, start, end);
			if (newType > -1) {
				tokenType = newType;
			}
		}

		super.addToken(array, start, end, tokenType, startOffset, hyperlink);
	}

	// FIXME: Currently any functions with a . in them do not highlight :(
	private TokenMap getMacroFunctionNames() {
		if (macroFunctionTokenMap == null) {
			macroFunctionTokenMap = new TokenMap(true);

			List<String> macroList = MapTool.getParser().listAllMacroFunctions();

			for (String macro : macroList) {
				macroFunctionTokenMap.put(macro, Token.FUNCTION);
				log.debug("Adding \"" + macro + "\" macro function to syntax highlighting.");
			}
		}

		return macroFunctionTokenMap;
	}
}
