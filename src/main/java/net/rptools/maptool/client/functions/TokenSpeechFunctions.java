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

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.util.List;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenSpeechFunctions extends AbstractFunction {

  private static final TokenSpeechFunctions instance = new TokenSpeechFunctions();

  private TokenSpeechFunctions() {
    super(0, 2, "getSpeech", "setSpeech", "getSpeechNames");
  }

  public static TokenSpeechFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    final Token token =
        ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
    if (token == null) {
      throw new ParserException(
          I18N.getText("macro.function.general.noImpersonated", functionName));
    }

    if (functionName.equals("getSpeech")) {
      if (parameters.size() < 1) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
      }
      String speech = token.getSpeech(parameters.get(0).toString());
      return speech == null ? "" : speech;
    }

    if (functionName.equals("setSpeech")) {
      if (parameters.size() < 2) {
        throw new ParserException(
            I18N.getText(
                "macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
      }
      token.setSpeech(parameters.get(0).toString(), parameters.get(1).toString());
      return "";
    }

    if (functionName.equals("getSpeechNames")) {
      String[] speech = new String[token.getSpeechNames().size()];
      String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
      if ("json".equals(delim)) {
        JsonArray jarr = new JsonArray();
        token.getSpeechNames().forEach(s -> jarr.add(new JsonPrimitive(s)));
        return jarr.toString();
      } else {
        return StringFunctions.getInstance().join(token.getSpeechNames().toArray(speech), delim);
      }
    }
    return null;
  }
}
