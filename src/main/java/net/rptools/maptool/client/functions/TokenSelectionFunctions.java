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

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TokenSelectionFunctions extends AbstractFunction {

  private static final TokenSelectionFunctions instance = new TokenSelectionFunctions();

  private TokenSelectionFunctions() {
    super(0, 3, "selectTokens", "deselectTokens");
  }

  public static TokenSelectionFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
    if (functionName.equals("selectTokens")) {
      selectTokens(parameters);
    } else if (functionName.equals("deselectTokens")) {
      deselectTokens(parameters);
    } else {
      throw new ParserException(
          I18N.getText("macro.function.general.unknownFunction", functionName));
    }
    return BigDecimal.ONE;
  }

  private void deselectTokens(List<Object> parameters) throws ParserException {
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zr.getZone();

    // If no tokens are selected, don't bother with the rest
    if (zr.getSelectedTokenSet() == null || zr.getSelectedTokenSet().isEmpty()) {
      return;
    }
    Collection<GUID> deselectGUIDs = new ArrayList<GUID>();

    if (parameters == null || parameters.isEmpty()) {
      // Deselect all currently selected tokens
      deselectGUIDs.addAll(zr.getSelectedTokenSet());
    } else if (parameters.size() == 1) {
      // If no tokens are selected, don't do anything
      String paramStr = parameters.get(0).toString().trim();
      Token t = zone.resolveToken(paramStr);

      if (t != null) {
        deselectGUIDs.add(t.getId());
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", "deselectTokens", paramStr));
      }
    } else if (parameters.size() == 2) {
      // Either a JSON Array or a String List
      String paramStr = parameters.get(0).toString();
      String delim = parameters.get(1).toString();

      if (delim.equalsIgnoreCase("json")) {
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(paramStr);
        // A JSON Array was supplied
        if (json.isJsonArray()) {
          for (JsonElement ele : json.getAsJsonArray()) {
            String identifier = JSONMacroFunctions.getInstance().jsonToScriptString(ele);
            Token t = zone.resolveToken(identifier.trim());
            if (t != null) {
              deselectGUIDs.add(t.getId());
            } else {
              throw new ParserException(
                  I18N.getText(
                      "macro.function.general.unknownToken", "deselectTokens", identifier));
            }
          }
        }
      } else {
        // String List
        String[] strList = paramStr.split(delim);
        for (String s : strList) {
          Token t = zone.resolveToken(s.trim());
          if (t != null) {
            deselectGUIDs.add(t.getId());
          } else {
            throw new ParserException(
                I18N.getText("macro.function.general.unknownToken", "deselectTokens", s));
          }
        }
      }
    } else if (parameters.size() > 2) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", "deselectTokens", 2, parameters.size()));
    }
    // Finally, loop through the deselect guids and deselect each token in turn
    for (GUID deselectGUID : deselectGUIDs) {
      zr.deselectToken(deselectGUID);
    }
  }

  private void selectTokens(List<Object> parameters) throws ParserException {
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zr.getZone();
    Collection<GUID> allGUIDs = new ArrayList<GUID>();

    if (parameters == null || parameters.isEmpty()) {
      // Select all tokens
      List<Token> allTokens = zone.getTokens();

      if (allTokens != null) {
        for (Token t : allTokens) {
          GUID tid = t.getId();
          allGUIDs.add(tid);
        }
      }
    } else if (parameters.size() == 1) {
      String paramStr = parameters.get(0).toString();
      zr.clearSelectedTokens();
      Token t = zone.resolveToken(paramStr);
      if (t != null) {
        allGUIDs.add(t.getId());
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", "selectTokens", paramStr));
      }
    } else if (parameters.size() == 2) {
      String paramStr = parameters.get(0).toString();
      String addOrReplace = parameters.get(1).toString();
      boolean add = FunctionUtil.getBooleanValue(addOrReplace);

      if (add) {
        allGUIDs = zr.getSelectedTokenSet();
      } else {
        zr.clearSelectedTokens();
      }
      Token t = zone.resolveToken(paramStr);

      if (t != null) {
        allGUIDs.add(t.getId());
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", "selectTokens", paramStr));
      }
    } else if (parameters.size() == 3) {
      // Either a JSON Array or a String List
      String paramStr = parameters.get(0).toString();
      String addOrReplace = parameters.get(1).toString();
      String delim = parameters.get(2).toString();
      boolean add = FunctionUtil.getBooleanValue(addOrReplace);

      if (add) {
        allGUIDs = zr.getSelectedTokenSet();
      } else {
        zr.clearSelectedTokens();
      }
      if (delim.equalsIgnoreCase("json")) {
        // A JSON Array was supplied
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(paramStr);
        if (json.isJsonArray()) {
          for (JsonElement ele : json.getAsJsonArray()) {
            String identifier = JSONMacroFunctions.getInstance().jsonToScriptString(ele);
            Token t = zone.resolveToken(identifier);
            if (t != null) {
              allGUIDs.add(t.getId());
            } else {
              throw new ParserException(
                  I18N.getText("macro.function.general.unknownToken", "selectTokens", identifier));
            }
          }
        }
      } else {
        // String List
        String[] strList = paramStr.split(delim);
        for (String s : strList) {
          Token t = zone.resolveToken(s.trim());
          if (t != null) {
            allGUIDs.add(t.getId());
          } else {
            throw new ParserException(
                I18N.getText("macro.function.general.unknownToken", "selectTokens", s));
          }
        }
      }
    } else if (parameters.size() > 3) {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", "selectTokens", 3, parameters.size()));
    }
    zr.selectTokens(allGUIDs);
  }
}
