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

import java.util.Stack;
import javax.annotation.Nullable;
import net.rptools.maptool.model.GUID;

/**
 * Who this client is currently speaking and acting as.
 *
 * <p>There are two layers. The <em>global</em> identity is the one the player chose, with {@code
 * /im} or the Impersonate panel, and it lasts until it is changed. On top of it sits a stack of
 * <em>context</em> identities: {@code /im token: macro} pushes one for the duration of that macro
 * and pops it afterwards, so that a macro can speak as a token without disturbing the player's own
 * choice. The current identity is the top of the stack, or the global identity when the stack is
 * empty.
 *
 * <p><b>Why this exists.</b> This state used to be fields of {@code CommandPanel}. Everything that
 * needed to know who was speaking -- chat formatting, several macro functions, and {@code
 * MacroManager} when it parses a command typed into chat -- had to reach through the frame to a
 * Swing component to ask, and so could not work without a UI. It now belongs to {@link
 * MapToolClient}. Each connection creates a new client, which is also when the panel used to clear
 * this state, so the lifetime is the same as before.
 *
 * <p><b>What is announced, and what is not.</b> A change to the <em>global</em> identity is passed
 * to a {@link GlobalIdentityListener}, which in a client with a UI is {@code CommandPanel}: it
 * updates the Impersonate panel, the avatar beside the chat box, and the HTML frames' {@code
 * onChangeImpersonated} callbacks. Entering, leaving or replacing a <em>context</em> identity is
 * never announced. That has always been so, and must stay so: if it were announced, every {@code
 * /im token: macro} -- which includes every click of a token's macro button -- would fire {@code
 * onChangeImpersonated} in every open HTML frame.
 *
 * <p><b>Threading.</b> This is not thread safe. Like the chat panel that used to hold it, it is
 * meant to be used from the thread that runs macros and chat commands, which in a client is the
 * Swing event dispatch thread.
 */
public class ImpersonationState {

  /** The identity the player chose, which lasts until it is changed. */
  private TokenIdentity globalIdentity = new TokenIdentity();

  /** Temporary identities pushed by macros. The most recent is at the top of the stack. */
  private final Stack<TokenIdentity> identityStack = new Stack<>();

  /** Told when the global identity changes. */
  @FunctionalInterface
  public interface GlobalIdentityListener {
    /**
     * Called after the global identity has been replaced.
     *
     * @param identity the new global identity, which is already in effect.
     */
    void globalIdentityChanged(TokenIdentity identity);
  }

  /** Whatever displays this client's identity, if anything does. */
  private @Nullable GlobalIdentityListener globalIdentityListener;

  /**
   * Sets what is told when the global identity changes, replacing any previous listener.
   *
   * <p>This is a single direct call rather than an event on {@code MapToolEventBus}, and that is
   * deliberate. The event bus delivers an event that is posted while another event is being
   * dispatched only after that outer dispatch has finished, and the global identity does change
   * from inside dispatches: {@code CommandPanel} re-sets it when the impersonated token is edited,
   * and framework event macros such as {@code onChangeMap} and {@code onMouseOverEnter} run inside
   * dispatches and may call {@code impersonate()}. Through the bus, the display would lag behind
   * the state, and a macro that impersonated a token and then asked {@code getImpersonated(1)} --
   * which reads the Impersonate panel -- would get the previous answer. Calling the listener
   * directly keeps the display exactly in step with the state, as it was when this state lived in
   * {@code CommandPanel}.
   *
   * @param listener the listener, or {@code null} for none, as on a server with no UI.
   */
  public void setGlobalIdentityListener(@Nullable GlobalIdentityListener listener) {
    this.globalIdentityListener = listener;
  }

  /**
   * @return the identity the player chose, ignoring any context identity a macro has pushed.
   */
  public TokenIdentity getGlobalIdentity() {
    return globalIdentity;
  }

  /**
   * Replaces the global identity, then tells the listener if there is one.
   *
   * <p>The state changes <em>before</em> the listener is called, so the listener, and anything it
   * runs -- including the HTML frames' {@code onChangeImpersonated} macros -- sees the new
   * identity. The listener is told on every call, even if the identity is the same as before; the
   * chat panel relies on that to refresh after the impersonated token is edited. If the listener
   * throws, the identity has still changed.
   *
   * @param identity the identity the player is now using.
   */
  public void setGlobalIdentity(TokenIdentity identity) {
    this.globalIdentity = identity;
    if (globalIdentityListener != null) {
      globalIdentityListener.globalIdentityChanged(identity);
    }
  }

  /**
   * @return the identity currently in effect: the most recent context identity, or the global
   *     identity if no macro has pushed one.
   */
  public TokenIdentity getCurrentIdentity() {
    return identityStack.isEmpty() ? globalIdentity : identityStack.peek();
  }

  /**
   * @return whether the current identity has a name, meaning something is being impersonated. This
   *     is true for an arbitrary name as well as for a token.
   */
  public boolean isImpersonating() {
    return getCurrentIdentity().hasName();
  }

  /**
   * @return the text to show for the current identity. When nothing is impersonated, this is the
   *     player's name.
   * @see TokenIdentity#getIdentity()
   */
  public String getIdentity() {
    return getCurrentIdentity().getIdentity();
  }

  /**
   * @return the GUID of the current identity, or {@code null} if it was set by name rather than by
   *     token. Callers that get {@code null} typically fall back to resolving {@link
   *     #getIdentity()} as a token name.
   */
  public GUID getIdentityGUID() {
    return getCurrentIdentity().getIdentityGUID();
  }

  /**
   * @return whether the current identity is a token that can still be found on the current map.
   */
  public boolean isImpersonatingToken() {
    return getCurrentIdentity().validToken();
  }

  /**
   * @return whether the global identity is a token that can still be found on the current map,
   *     ignoring any context identity.
   */
  public boolean isGlobalImpersonatingToken() {
    return globalIdentity.validToken();
  }

  /**
   * @return whether a macro has pushed a context identity that has not yet been popped.
   */
  public boolean hasContextIdentity() {
    return !identityStack.isEmpty();
  }

  /**
   * Adopts an identity temporarily, until the matching {@link #leaveContextIdentity()}.
   *
   * @param identity the identity to use for the duration of the context.
   */
  public void enterContextIdentity(TokenIdentity identity) {
    identityStack.push(identity);
  }

  /**
   * Returns to the identity that was in effect before the most recent {@link
   * #enterContextIdentity(TokenIdentity)}.
   *
   * @throws java.util.EmptyStackException if there is no context identity to leave. That can happen
   *     if {@link #clearContextIdentities()} ran while a macro was inside a context.
   */
  public void leaveContextIdentity() {
    identityStack.pop();
  }

  /**
   * Replaces the most recent context identity, leaving the depth of the stack unchanged.
   *
   * @param identity the identity to use for the rest of the current context.
   * @throws java.util.EmptyStackException if there is no context identity to replace.
   */
  public void replaceContextIdentity(TokenIdentity identity) {
    identityStack.pop();
    identityStack.push(identity);
  }

  /** Discards every context identity, leaving only the global identity. */
  public void clearContextIdentities() {
    identityStack.clear();
  }
}
