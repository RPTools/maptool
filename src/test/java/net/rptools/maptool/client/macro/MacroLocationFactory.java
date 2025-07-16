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
import java.net.URISyntaxException;
import net.rptools.maptool.model.Token;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class MacroLocationFactoryTest {

  private final MacroLocationFactory factory = MacroLocationFactory.getInstance();

  @Test
  void testCreateUnknownLocation() {
    MacroLocation location = factory.createUnknownLocation("unknownMacro");
    assertEquals("unknownMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.unknown, location.getSource());
    assertEquals("", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateGlobalLocation() {
    MacroLocation location = factory.createGlobalLocation("globalMacro");
    assertEquals("globalMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.global, location.getSource());
    assertEquals("global", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateCampaignLocation() {
    MacroLocation location = factory.createCampaignLocation("campaignMacro");
    assertEquals("campaignMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.campaign, location.getSource());
    assertEquals("campaign", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateTokenLocation() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation location = factory.createTokenLocation("tokenMacro", mockToken);
    assertEquals("tokenMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.token, location.getSource());
    assertEquals("mockToken", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateLibTokenLocation() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("lib:libToken");
    MacroLocation location = factory.createLibTokenLocation("libMacro", mockToken);
    assertEquals("libMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.library, location.getSource());
    assertEquals("libToken", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateGmLocation() {
    MacroLocation location = factory.createGmLocation("gmMacro");
    assertEquals("gmMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.gm, location.getSource());
    assertEquals("gm", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateUriLocation() throws URISyntaxException {
    MacroLocation location =
        factory.createUriLocation("lib://test/macro/m2", new URI("lib://lib-test.net/macros/m1"));
    assertEquals("/macro/m2", location.getName());
    assertEquals(MacroLocation.MacroSource.uri, location.getSource());
    assertEquals("test", location.getLocation());
    assertNotNull(location.getUri());
    assertEquals(URI.create("lib://test/macro/m2"), location.getUri());
  }

  @Test
  void testCreateUriLocationInvalid() throws URISyntaxException {
    MacroLocation location =
        factory.createUriLocation("$$::invalidUriMacro", new URI("lib://test-lib.net/macros/m1"));
    assertEquals("$$::invalidUriMacro", location.getName());
    assertEquals(MacroLocation.MacroSource.unknown, location.getSource());
    assertEquals("", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateRelativeUriLocationRelative() throws URISyntaxException {
    MacroLocation location =
        factory.createUriLocation("relative/path", new URI("lib://test-lib.net/macros/m1"));
    assertEquals("/macros/relative/path", location.getName());
    assertEquals(MacroLocation.MacroSource.uri, location.getSource());
    assertEquals("test-lib.net", location.getLocation());
    assertNotNull(location.getUri());
    assertEquals(URI.create("lib://test-lib.net/macros/relative/path"), location.getUri());
  }

  @Test
  void testCreateChatLocation() {
    MacroLocation location = factory.createChatLocation();
    assertEquals("chat", location.getName());
    assertEquals(MacroLocation.MacroSource.chat, location.getSource());
    assertEquals("chat", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateToolTipLocationWithToken() {
    Token mockToken = Mockito.mock(Token.class);
    Mockito.when(mockToken.getName()).thenReturn("mockToken");

    MacroLocation location = factory.createToolTipLocation(mockToken);
    assertEquals("tooltip", location.getName());
    assertEquals(MacroLocation.MacroSource.tooltip, location.getSource());
    assertEquals("mockToken", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateToolTipLocationWithoutToken() {
    MacroLocation location = factory.createToolTipLocation(null);
    assertEquals("tooltip", location.getName());
    assertEquals(MacroLocation.MacroSource.tooltip, location.getSource());
    assertEquals("", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateExecFunctionLocation() {
    MacroLocation location = factory.createExecFunctionLocation("execFunction");
    assertEquals("execFunction", location.getName());
    assertEquals(MacroLocation.MacroSource.execFunction, location.getSource());
    assertEquals("execFunction", location.getLocation());
    assertNull(location.getUri());
  }

  @Test
  void testCreateSentryIoLoggingLocation() {
    MacroLocation location = factory.createSentryIoLoggingLocation();
    assertEquals("sentryIoLogging", location.getName());
    assertEquals(MacroLocation.MacroSource.sentryIoLogging, location.getSource());
    assertEquals("sentryIoLogging", location.getLocation());
    assertNull(location.getUri());
  }
}
