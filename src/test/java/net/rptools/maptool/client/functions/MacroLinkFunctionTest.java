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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

class MacroLinkFunctionTest {

  @ParameterizedTest(name = "{index} - {0} is properly parsed")
  @CsvFileSource(
      resources = "/net/rptools/maptool/client/functions/macroURLCases.csv",
      numLinesToSkip = 1)
  void testMacroLinkPattern(
      String caseName,
      String macroLink,
      String protocol,
      String macroName,
      String who,
      String target,
      String val) {
    Pattern macroLinkPattern = MacroLinkFunction.MACROLINK_PATTERN;

    Matcher matcher = macroLinkPattern.matcher(macroLink);

    assertTrue(matcher.matches());
    assertEquals(protocol, matcher.group(1));
    assertEquals(macroName, matcher.group(2));
    assertEquals(who, matcher.group(3));
    assertEquals(target, matcher.group(4));
    assertEquals(val, matcher.group(5));
  }

  @ParameterizedTest(name = "{index} - {0} is properly parsed")
  @CsvFileSource(
      resources = "/net/rptools/maptool/client/functions/macroURLCases.csv",
      numLinesToSkip = 1)
  void testAutoExecPattern(
      String caseName,
      String macroLink,
      String protocol,
      String macroName,
      String who,
      String target,
      String val) {
    Pattern autoexecPattern = MacroLinkFunction.AUTOEXEC_PATTERN;

    Matcher matcher = autoexecPattern.matcher(macroLink);

    assertTrue(matcher.matches());
    assertEquals(protocol, matcher.group(1));
    assertEquals(macroName, matcher.group(2));
    assertEquals(who, matcher.group(3));
    assertEquals(target, matcher.group(4));
    assertEquals(val, matcher.group(5));
  }

  @ParameterizedTest(name = "{index} - {0} is properly parsed")
  @CsvFileSource(
      resources = "/net/rptools/maptool/client/functions/macroURLCases.csv",
      numLinesToSkip = 1)
  void testTooltipPattern(
      String caseName,
      String macroLink,
      String protocol,
      String macroName,
      String who,
      String target,
      String val) {
    Pattern tooltipPattern = MacroLinkFunction.TOOLTIP_PATTERN;

    Matcher matcher = tooltipPattern.matcher(macroLink);

    assertTrue(matcher.matches());
    assertEquals(protocol, matcher.group(1));
    assertEquals(macroName, matcher.group(2));
    assertEquals(who, matcher.group(3));
    assertEquals(target, matcher.group(4));
    assertEquals(val, matcher.group(5));
  }

  @ParameterizedTest(name = "{index} - {0} is properly parsed")
  @CsvFileSource(
      resources = "/net/rptools/maptool/client/functions/linkDataCases.csv",
      numLinesToSkip = 1)
  void testLinkDataPattern(String caseName, String macro, String macroLink, String macroData) {
    Pattern linkDataPattern = MacroLinkFunction.LINK_DATA_PATTERN;

    Matcher matcher = linkDataPattern.matcher(macro);

    assertTrue(matcher.matches());
    assertEquals(macroLink, matcher.group(1));
    assertEquals(macroData, matcher.group(2));
  }
}
