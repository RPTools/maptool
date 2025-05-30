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
package net.rptools.maptool.client.macro;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import net.rptools.maptool.model.Token;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MacroLocationTest {

  @Test
  void testParseNameCampaign() {
    MacroLocation location = MacroLocation.parseMacroName("test@campaign", null, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.campaign, location.getSource());
    assertEquals("campaign", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseNameGM() {
    MacroLocation location = MacroLocation.parseMacroName("test@gm", null, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.gm, location.getSource());
    assertEquals("gm", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseNameToken() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation location = MacroLocation.parseMacroName("test@token", null, mockToken);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.token, location.getSource());
    assertEquals("mockToken", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseNameLibToken() {
    MacroLocation location = MacroLocation.parseMacroName("test@lib:libName", null, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.library, location.getSource());
    assertEquals("lib:libName", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseNameURI() {
    MacroLocation location = MacroLocation.parseMacroName("lib://host/path", null, null);
    assertEquals("path", location.getName());
    assertEquals(MacroLocation.MacroSource.uri, location.getSource());
    assertEquals("host", location.getLocation());
    assertNotNull(location.getUri());
    assertEquals(URI.create("lib://host/path"), location.getUri());
  }

  @Test
  void testParseInvalidNameUri() {
    MacroLocation location = MacroLocation.parseMacroName("$$::invalidUri", null, null);
    assertEquals("$$::invalidUri", location.getName());
    assertEquals(MacroLocation.MacroSource.unknown, location.getSource());
    assertEquals("", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseUnknown() {
    MacroLocation location = MacroLocation.parseMacroName("unknownMacro", null, null);
    assertEquals("unknownMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.unknown, location.getSource());
  }

  @Test
  void testParseTokenAtThis() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation location = MacroLocation.parseMacroName("test@this", null, mockToken);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.token, location.getSource());
    assertEquals("mockToken", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseTokenAtThisWithCaller() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation caller = MacroLocation.parseMacroName("testParent@token", null, mockToken);
    MacroLocation location = MacroLocation.parseMacroName("test@this", caller, mockToken);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.token, location.getSource());
    assertEquals("mockToken", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseCampaignAtThis() {
    MacroLocation caller = MacroLocation.parseMacroName("testParent@campaign", null, null);
    MacroLocation location = MacroLocation.parseMacroName("test@this", caller, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.campaign, location.getSource());
    assertEquals("campaign", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseGmAtThis() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation caller = MacroLocation.parseMacroName("testParent@gm", null, mockToken);
    MacroLocation location = MacroLocation.parseMacroName("test@this", caller, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.gm, location.getSource());
    assertEquals("gm", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseGlobalAtThis() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation caller = MacroLocation.parseMacroName("testParent@global", null, mockToken);
    MacroLocation location = MacroLocation.parseMacroName("test@this", caller, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.global, location.getSource());
    assertEquals("global", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseLibTokenAtThis() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation caller = MacroLocation.parseMacroName("testParent@lib:libName", null, mockToken);
    MacroLocation location = MacroLocation.parseMacroName("test@this", caller, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.library, location.getSource());
    assertEquals("lib:libName", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testParseUriAtThis() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation caller =
        MacroLocation.parseMacroName("lib://test.lib/macros/macro", null, mockToken);
    MacroLocation location = MacroLocation.parseMacroName("test@this", caller, null);
    assertEquals("test", location.getName());
    assertEquals(MacroLocation.MacroSource.uri, location.getSource());
    assertEquals("test.lib", location.getLocation());
  }

  @Test
  void testParseInvalidAtThis() {
    MacroLocation location = MacroLocation.parseMacroName("test@this", null, null);
    assertEquals("test@this", location.getName());
    assertEquals(MacroLocation.MacroSource.unknown, location.getSource());
    assertEquals("", location.getLocation());
    assertNull(location.getUri());
  }
}
