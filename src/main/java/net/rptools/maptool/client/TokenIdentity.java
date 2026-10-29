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

import net.rptools.maptool.client.functions.FindTokenFunctions;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;

/**
 * An identity that can be impersonated: either a token, or any name at all.
 *
 * <p>Instances are immutable. Note however that {@link #getToken()} and {@link #validToken()} are
 * not plain getters: they look the token up by its GUID each time, on the current map, so an
 * identity that resolved to a token when it was created can stop resolving later, or resolve
 * differently depending on which map is being viewed.
 *
 * <p>This was previously nested inside {@code CommandPanel}. It moved out alongside {@link
 * ImpersonationState}, so that code needing to know who is speaking does not depend on a Swing
 * component to do it. The behaviour is unchanged.
 */
public class TokenIdentity {

  /** The name of the identity. If null, nothing is impersonated. */
  private final String identityName;

  /** The GUID of the identity. */
  private final GUID identityGUID;

  /** Whether the player is allowed to set the token in the Impersonate panel. */
  private final boolean canImpersonate;

  /** Creates an empty identity (nothing impersonated). */
  public TokenIdentity() {
    identityName = null;
    identityGUID = null;
    canImpersonate = false;
  }

  /**
   * Creates an identity from the token. If null, nothing is impersonated.
   *
   * <p>Equivalent to {@code TokenIdentity(token, null)}, which in turn allows the token to be shown
   * in the Impersonate panel. This is what the {@code impersonate()} macro function uses, so a
   * token impersonated that way does appear in the panel. This constructor was package-private
   * while the class was nested in {@code CommandPanel}, and is public only because that class is
   * now in a different package.
   *
   * @param token the token to impersonate
   */
  public TokenIdentity(Token token) {
    this(token, null);
  }

  /**
   * Creates an identity from a name. If null, nothing is impersonated.
   *
   * @param name the name to impersonate
   */
  TokenIdentity(String name) {
    this(null, name, false);
  }

  /**
   * Creates an identity from a token. If the token is null, the identity uses the specified backup
   * name.
   *
   * @param token the token to impersonate
   * @param backupName the backup name to impersonate if the token is null
   */
  public TokenIdentity(Token token, String backupName) {
    this(token, backupName, true);
  }

  /**
   * Creates an identity from a GUID. If there is no associated token, the identity uses the
   * specified backup name.
   *
   * @param tokenId the token GUID
   * @param backupName the backup name to impersonate if the token is null
   */
  public TokenIdentity(GUID tokenId, String backupName) {
    this(FindTokenFunctions.findToken(tokenId, null), backupName);
  }

  /**
   * Creates an identity from a token. If the token is null, the identity uses the specified backup
   * name. Impersonation through the Impersonate panel can be disabled.
   *
   * @param token the token to impersonate
   * @param backupName the backup name to impersonate if the token is null
   * @param canImpersonate whether the token can be impersonated in the Impersonate panel
   */
  public TokenIdentity(Token token, String backupName, boolean canImpersonate) {
    if (token != null) {
      this.identityGUID = token.getId();
      this.identityName = token.getName();
      this.canImpersonate = canImpersonate;
    } else {
      this.identityGUID = null;
      this.identityName = backupName;
      this.canImpersonate = false;
    }
  }

  /**
   * Returns the text to show for this identity.
   *
   * <p>When nothing is impersonated this is the <em>player's</em> name, not an empty string. Chat
   * formatting relies on that, and so does {@code MacroManager}, which resolves this text to a
   * token when no GUID is available.
   *
   * @return a string representing the identity.
   */
  public String getIdentity() {
    if (identityName == null) {
      if (identityGUID == null) {
        return MapTool.getPlayer().getName();
      } else {
        return identityGUID.toString();
      }
    }
    return identityName;
  }

  /**
   * @return a string for the character label of the identity.
   */
  public String getCharacterLabel() {
    return hasName() ? identityName : "";
  }

  /**
   * @return the GUID of the identity.
   */
  public GUID getIdentityGUID() {
    return identityGUID;
  }

  /**
   * @return whether this identity may be shown in the Impersonate panel.
   */
  public boolean canImpersonate() {
    return canImpersonate;
  }

  /**
   * @return the token of the identity, looked up on the current map.
   */
  public Token getToken() {
    return FindTokenFunctions.findToken(identityGUID, null);
  }

  /**
   * @return whether the identity has a name.
   */
  public boolean hasName() {
    return identityName != null;
  }

  /**
   * @return whether the token can still be found on the current map.
   */
  public boolean validToken() {
    if (identityGUID == null) {
      return false;
    } else {
      return FindTokenFunctions.findToken(identityGUID, null) != null;
    }
  }
}
