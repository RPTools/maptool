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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EmptyStackException;
import org.junit.jupiter.api.Test;

/**
 * Pins down how the global identity and the stack of context identities interact. Frameworks rely
 * on these rules through {@code /im}, {@code /im token: macro}, {@code impersonate()} and {@code
 * getImpersonated()}, and until this state moved out of {@code CommandPanel} there was no way to
 * test them without a UI.
 *
 * <p>These use name-only identities throughout. That keeps the tests free of tokens and maps, and
 * avoids {@link TokenIdentity#getIdentity()} on an empty identity, which falls back to the local
 * player's name.
 */
public class ImpersonationStateTest {

  private static TokenIdentity named(String name) {
    return new TokenIdentity(name);
  }

  @Test
  void startsWithNothingImpersonated() {
    var state = new ImpersonationState();

    assertFalse(state.isImpersonating());
    assertFalse(state.hasContextIdentity());
    assertNull(state.getIdentityGUID());
    assertFalse(state.isImpersonatingToken());
  }

  @Test
  void globalIdentityIsCurrentWhenNoContextIsActive() {
    var state = new ImpersonationState();
    var alice = named("Alice");

    state.setGlobalIdentity(alice);

    assertSame(alice, state.getCurrentIdentity());
    assertTrue(state.isImpersonating());
    assertEquals("Alice", state.getIdentity());
  }

  @Test
  void contextIdentityShadowsGlobalUntilLeft() {
    var state = new ImpersonationState();
    state.setGlobalIdentity(named("Alice"));

    state.enterContextIdentity(named("Bob"));
    assertEquals("Bob", state.getIdentity());
    assertTrue(state.hasContextIdentity());

    state.leaveContextIdentity();
    assertEquals("Alice", state.getIdentity());
    assertFalse(state.hasContextIdentity());
  }

  @Test
  void nestedContextsUnwindInOrder() {
    var state = new ImpersonationState();
    state.setGlobalIdentity(named("Alice"));

    state.enterContextIdentity(named("Bob"));
    state.enterContextIdentity(named("Carol"));
    assertEquals("Carol", state.getIdentity());

    state.leaveContextIdentity();
    assertEquals("Bob", state.getIdentity());

    state.leaveContextIdentity();
    assertEquals("Alice", state.getIdentity());
  }

  @Test
  void replacingAContextKeepsTheDepth() {
    var state = new ImpersonationState();
    state.setGlobalIdentity(named("Alice"));
    state.enterContextIdentity(named("Bob"));

    state.replaceContextIdentity(named("Dave"));
    assertEquals("Dave", state.getIdentity());

    // one leave is enough to get back to the global identity, so the depth did not change
    state.leaveContextIdentity();
    assertEquals("Alice", state.getIdentity());
  }

  /**
   * {@code impersonate()} sets the global identity. Called from inside an {@code /im token: macro}
   * context it is therefore invisible until that context ends. That is existing behaviour, not
   * necessarily intended, and recorded here so that a change to it is deliberate.
   */
  @Test
  void changingGlobalInsideAContextIsHiddenUntilTheContextEnds() {
    var state = new ImpersonationState();
    state.setGlobalIdentity(named("Alice"));
    state.enterContextIdentity(named("Bob"));

    state.setGlobalIdentity(named("Erin"));
    assertEquals("Bob", state.getIdentity());

    state.leaveContextIdentity();
    assertEquals("Erin", state.getIdentity());
  }

  @Test
  void clearingContextsKeepsTheGlobalIdentity() {
    var state = new ImpersonationState();
    state.setGlobalIdentity(named("Alice"));
    state.enterContextIdentity(named("Bob"));
    state.enterContextIdentity(named("Carol"));

    state.clearContextIdentities();

    assertFalse(state.hasContextIdentity());
    assertEquals("Alice", state.getIdentity());
  }

  /**
   * Clearing the contexts while a macro is inside one leaves that macro's eventual leave with
   * nothing to pop. Existing behaviour: it throws, and the caller is expected to cope.
   */
  @Test
  void leavingWithNoContextThrows() {
    var state = new ImpersonationState();

    assertThrows(EmptyStackException.class, state::leaveContextIdentity);
  }

  /**
   * A name-only identity counts as impersonating but not as impersonating a token. Macro functions
   * expose both answers -- {@code hasImpersonated()} asks the second -- so the distinction matters.
   */
  @Test
  void aNameIsImpersonationButNotATokenImpersonation() {
    var state = new ImpersonationState();
    state.setGlobalIdentity(named("Narrator"));

    assertTrue(state.isImpersonating());
    assertFalse(state.isImpersonatingToken());
    assertFalse(state.isGlobalImpersonatingToken());
    assertNull(state.getIdentityGUID());
  }
}
