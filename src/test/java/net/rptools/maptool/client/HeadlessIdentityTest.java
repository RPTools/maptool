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
package net.rptools.maptool.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.client.macro.MacroManager;
import net.rptools.maptool.model.GUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The impersonation identity must be usable where there is no window, as on a server run without a
 * UI.
 *
 * <p>Tests run without creating a {@code MapToolFrame}, so {@link MapTool#getFrame()} is {@code
 * null} here, exactly as it is on a server run without a UI.
 */
public class HeadlessIdentityTest {

  private ImpersonationState identity;

  @BeforeEach
  void setUp() {
    assertNull(MapTool.getFrame(), "these tests assume there is no window");
    identity = MapTool.getClient().getImpersonation();
    identity.clearAllIdentities();
  }

  @AfterEach
  void tearDown() {
    identity.clearAllIdentities();
  }

  @Test
  void thereIsNoCurrentMapWithoutAWindow() {
    assertNull(MapTool.getClient().getCurrentZone());
  }

  @Test
  void findingATokenOnTheCurrentMapFindsNothingRatherThanFailing() {
    assertNull(FindTokenFunctions.findToken(new GUID(), null));
    assertNull(FindTokenFunctions.findToken("Anyone", null));
  }

  /** {@code hasImpersonated()} asks this, and it looks the token up on the current map. */
  @Test
  void aTokenIdentityIsNotATokenWhenThereIsNoMap() {
    identity.setGlobalIdentity(new TokenIdentity(new GUID(), "Somebody"));

    assertTrue(identity.isImpersonating());
    assertFalse(assertDoesNotThrow(identity::isImpersonatingToken));
    assertFalse(assertDoesNotThrow(identity::isGlobalImpersonatingToken));
  }

  /** With no map to find a token on, {@code /im} falls back to impersonating by name. */
  @Test
  void imByNameWorksWithoutAWindow() {
    assertDoesNotThrow(() -> MacroManager.executeMacro("/im Somebody"));

    assertEquals("Somebody", identity.getIdentity());
    assertNull(identity.getIdentityGUID());
    assertFalse(identity.isImpersonatingToken());

    assertDoesNotThrow(() -> MacroManager.executeMacro("/im"));
    assertFalse(identity.isImpersonating());
  }
}
