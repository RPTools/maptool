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
 * <https://www.gnu.org/licenses/> and specifically the Affero license
 * text at <https://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.rptools.parser.ParserException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** How errors from JavaScript reach the macro that ran it. */
class MacroJavaScriptBridgeErrorTest {
  /** Java code a script can call, standing in for MapTool's JavaScript API. */
  public static class Host {
    static final ParserException PARSER_ERROR = new ParserException("from MTScript");

    @HostAccess.Export
    public void throwParserException() throws ParserException {
      throw PARSER_ERROR;
    }

    @HostAccess.Export
    public void throwOther() {
      throw new IllegalStateException("from Java");
    }
  }

  private Context context;

  @BeforeEach
  void setUp() {
    context = Context.newBuilder("js").allowHostAccess(HostAccess.EXPLICIT).build();
    context.getBindings("js").putMember("host", new Host());
  }

  @AfterEach
  void tearDown() {
    context.close();
  }

  private PolyglotException run(String script) {
    return assertThrows(PolyglotException.class, () -> context.eval("js", script));
  }

  @Test
  void anErrorInTheScriptKeepsItsMessageAndLine() {
    // The case that used to be reported only as "PolyglotEngineException".
    ParserException pe =
        MacroJavaScriptBridge.toParserException(run("let a = 1;\nfind_pc_libs();"));
    assertEquals("ReferenceError: find_pc_libs is not defined (line 2)", pe.getMessage());
  }

  @Test
  void aThrowInTheScriptKeepsItsMessage() {
    ParserException pe =
        MacroJavaScriptBridge.toParserException(run("throw new Error('PZ2E: no token');"));
    assertTrue(pe.getMessage().startsWith("Error: PZ2E: no token"), pe.getMessage());
  }

  @Test
  void aParserExceptionFromJavaIsReportedAsItself() {
    assertSame(
        Host.PARSER_ERROR,
        MacroJavaScriptBridge.toParserException(run("host.throwParserException();")));
  }

  @Test
  void anyOtherExceptionFromJavaIsWrappedRatherThanFailingTheCast() {
    // This used to fail with a ClassCastException, which hid the original error.
    ParserException pe = MacroJavaScriptBridge.toParserException(run("host.throwOther();"));
    assertTrue(pe.getCause() instanceof IllegalStateException, String.valueOf(pe.getCause()));
    assertEquals("from Java", pe.getCause().getMessage());
  }
}
