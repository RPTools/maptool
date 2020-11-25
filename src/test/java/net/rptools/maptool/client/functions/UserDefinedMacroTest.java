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

import static org.junit.jupiter.api.Assertions.*;

import net.rptools.maptool.client.*;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;
import org.junit.jupiter.api.Test;

public class UserDefinedMacroTest {

  @Test
  public void testUserDefinedFunction() throws ParserException {

    // create a new macro of name mymacro that returns some text and argument passed in
    MacroButtonProperties macro = new MacroButtonProperties(0);
    macro.setLabel("mymacro");
    macro.setCommand("[r:'got parameter ' + arg(0)]");

    // create a token w/macro
    Token token = new Token();
    token.saveMacro(macro);

    // setup evaluation, trusted, on token
    MapToolVariableResolver resolver = new MapToolVariableResolver(token);
    MapTool.getParser().enterContext(new MapToolMacroContext("test", "test", true));

    // define myfunction
    MapToolExpressionParser parser = new MapToolExpressionParser();
    parser.evaluate("defineFunction('myfunction', 'mymacro@TOKEN')", resolver);

    // evaluate myfunction that should call mymacro on token
    assertEquals(
        "got parameter Hello", parser.evaluate("myfunction('Hello')", resolver).getValue());
  }
}
