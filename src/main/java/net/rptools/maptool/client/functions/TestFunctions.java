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
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.macrobuttons.panels.SelectionPanel;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class TestFunctions extends AbstractFunction {

  /** The singleton instance. */
  private static final TestFunctions instance = new TestFunctions();

  private static final String TEST_CONTEXT_NAME = "<test>";

  private int testDepth = 0;
  private int failures = 0;
  private List<String> messages = new ArrayList<String>();

  public static TestFunctions getInstance() {
    return instance;
  }

  private TestFunctions() {
    super(0, 3, "test.equal", "test.run");
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {

    switch (functionName) {
      case "test.equal":
        FunctionUtil.checkNumberParam(functionName, parameters, 3, 3);
        Object expected = parameters.get(0);
        Object actual = parameters.get(1);
        String name = parameters.get(2).toString();
        String msg;
        String formattedMsg;
        if (expected.equals(actual)) {
          msg = name + " Succeeded";
          formattedMsg =
              "<div style='background-color:green; color:white'><b>" + msg + "</b></div>";
        } else {
          msg = name + "Failed, expected " + expected + " got " + actual;
          formattedMsg = "<div style='background-color:red'><b>" + msg + "</b></div>";
          if (testDepth > 0) {
            failures++;
          }
        }
        JsonArray jsonArray = new JsonArray();
        if (testDepth == 0) {
          MapTool.addLocalMessage(formattedMsg);
        } else {
          messages.add(msg);
        }
        return "";
      case "test.run":
        testDepth++;
        String res = runTests();
        testDepth--;
        return res;
    }
    throw new ParserException(I18N.getText("macro.function.general.unknownFunction", functionName));
  }

  private String runTests() throws ParserException {
    Set<Token> tokens = new HashSet<>();
    Set<GUID> tokenIds = MapTool.getFrame().getCurrentZoneRenderer().getSelectedTokenSet();
    if (!tokenIds.isEmpty()) {
      for (GUID tokenId : tokenIds) {
        Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(tokenId);
        tokens.add(token);
      }
    } else {
      tokens.addAll(MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokens());
    }

    tokens =
        tokens.stream()
            .filter(t -> t.getName().toLowerCase().startsWith("test:"))
            .collect(Collectors.toSet());

    return runTests(tokens);
  }

  private String runTests(Set<Token> tokens) throws ParserException {
    int i = 0;
    for (Token token : tokens) {
      i++;
      MapTool.addLocalMessage(
          "<b>Running test " + i + " of " + tokens.size() + " (" + token.getName() + ")<b>");
      runTests(token);
    }
    return "";
  }

  private void runTests(Token token) throws ParserException {
    Map<Integer, Object> macroPropertiesMap =
        token.getMacroPropertiesMap(MapTool.getParser().isMacroTrusted());

    for (Object o : macroPropertiesMap.values()) {
      MacroButtonProperties mbp = (MacroButtonProperties) o;
      if (mbp.getLabel().toLowerCase().startsWith("test:")) {

        failures = 0;
        messages.clear();
        // Each call gets its own variable parser
        MapToolVariableResolver resolver = new MapToolVariableResolver(null);
        MapToolMacroContext context =
            new MapToolMacroContext(
                TEST_CONTEXT_NAME, MapTool.getParser().getContext().getSource(), true);
        try {
          String ret =
              MapTool.getParser()
                  .parseLine(resolver, resolver.getTokenInContext(), mbp.getCommand(), context);
        } catch (Exception e) {
          failures++;
          messages.add(e.getMessage());
        }

        JsonObject jsonObj = new JsonObject();
        if (failures > 0) {
          mbp.setColorKey("red");
          mbp.setFontColorKey("white");
          jsonObj.addProperty("status", "failure");
        } else {
          mbp.setColorKey("green");
          mbp.setFontColorKey("white");
          jsonObj.addProperty("status", "success");
        }
        JsonArray jsonArray = new JsonArray();
        for (String msg : messages) {
          jsonArray.add(msg);
        }
        jsonObj.add("messages", jsonArray);
        token.setProperty(mbp.getLabel(), jsonObj.toString());
      }
    }
    SelectionPanel selectionPanel = MapTool.getFrame().getSelectionPanel();
    if (selectionPanel != null && selectionPanel.isVisible()) {
      selectionPanel.reset();
    }
  }
}
