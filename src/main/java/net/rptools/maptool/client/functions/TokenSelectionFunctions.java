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
import java.util.Collections;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.json.JSONMacroFunctions;
import net.rptools.maptool.client.ui.zone.SelectionModel;
import net.rptools.maptool.client.ui.zone.renderer.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.maptool.util.StringUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
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
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    if (!MapTool.getParser().isMacroTrusted()) {
      throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
    }
    if (functionName.equalsIgnoreCase("selectTokens")) {
      selectTokens(parameters);
    } else if (functionName.equalsIgnoreCase("deselectTokens")) {
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
    SelectionModel selectionModel = zr.getSelectionModel();

    if (parameters == null || parameters.isEmpty()) {
      // Deselect all currently selected tokens
      selectionModel.replaceSelection(Collections.emptyList());
    } else if (parameters.size() == 1) {
      // Single token to deselect
      String paramStr = parameters.get(0).toString().trim();
      Token t = zone.resolveToken(paramStr);

      if (t != null) {
        selectionModel.removeTokensFromSelection(Collections.singletonList(t.getId()));
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", "deselectTokens", paramStr));
      }
    } else if (parameters.size() == 2) {
      // Either a JSON Array or a String List
      String paramStr = parameters.get(0).toString();
      String delim = parameters.get(1).toString();
      final var deselectGUIDs = new ArrayList<GUID>();

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
        String[] strList = StringUtil.split(paramStr, delim);
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
      selectionModel.removeTokensFromSelection(deselectGUIDs);
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", "deselectTokens", 2, parameters.size()));
    }
  }

  private void selectTokens(List<Object> parameters) throws ParserException {
    ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
    Zone zone = zr.getZone();
    SelectionModel selectionModel = zr.getSelectionModel();

    final var newSelection = new ArrayList<GUID>();
    final boolean replaceSelection;

    if (parameters == null || parameters.isEmpty()) {
      replaceSelection = true;
      // Select all tokens
      List<Token> allTokens = zone.getTokensOnLayer(zr.getActiveLayer());
      if (allTokens != null) {
        for (Token t : allTokens) {
          GUID tid = t.getId();
          newSelection.add(tid);
        }
      }
    } else if (parameters.size() <= 2) {
      // One token ID provided, with optional replacement flag.
      String paramStr = parameters.get(0).toString();
      replaceSelection =
          parameters.size() < 2 || !FunctionUtil.getBooleanValue(parameters.get(1).toString());
      Token t = zone.resolveToken(paramStr);
      if (t != null) {
        newSelection.add(t.getId());
      } else {
        throw new ParserException(
            I18N.getText("macro.function.general.unknownToken", "selectTokens", paramStr));
      }
    } else if (parameters.size() == 3) {
      // Either a JSON Array or a String List
      String paramStr = parameters.get(0).toString();
      replaceSelection = !FunctionUtil.getBooleanValue(parameters.get(1).toString());
      String delim = parameters.get(2).toString();

      if (delim.equalsIgnoreCase("json")) {
        // A JSON Array was supplied
        JsonElement json = JSONMacroFunctions.getInstance().asJsonElement(paramStr);
        if (json.isJsonArray()) {
          for (JsonElement ele : json.getAsJsonArray()) {
            String identifier = JSONMacroFunctions.getInstance().jsonToScriptString(ele);
            Token t = zone.resolveToken(identifier);
            if (t != null) {
              newSelection.add(t.getId());
            } else {
              throw new ParserException(
                  I18N.getText("macro.function.general.unknownToken", "selectTokens", identifier));
            }
          }
        }
      } else {
        // String List
        String[] strList = StringUtil.split(paramStr, delim);
        for (String s : strList) {
          Token t = zone.resolveToken(s.trim());
          if (t != null) {
            newSelection.add(t.getId());
          } else {
            throw new ParserException(
                I18N.getText("macro.function.general.unknownToken", "selectTokens", s));
          }
        }
      }
    } else {
      throw new ParserException(
          I18N.getText(
              "macro.function.general.tooManyParam", "selectTokens", 3, parameters.size()));
    }

    if (replaceSelection) {
      selectionModel.replaceSelection(newSelection);
    } else {
      selectionModel.addTokensToSelection(newSelection);
    }
  }
}
