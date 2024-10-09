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
package net.rptools.maptool.model.player;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.AppState;
import net.rptools.maptool.util.cipher.CipherUtil;

/** Represents the local player. Its methods can depend on AppState and other local properties. */
public class LocalPlayer extends Player {

  private final String plainTextPassword;
  private CipherUtil.Key password;

  public LocalPlayer() throws NoSuchAlgorithmException, InvalidKeySpecException {
    this(AppPreferences.defaultUserName.get(), Role.GM, "");
  }

  public LocalPlayer(String name, Role role, String plainTextPassword)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    super(name, role, null); // Superclass takes care of plainTextPassword info
    this.plainTextPassword = plainTextPassword;
    setPasswordSalt(CipherUtil.createSalt());
  }

  /**
   * @return the effective role of the local player, taking into account Show As Player.
   */
  public Role getEffectiveRole() {
    if (isGM() && AppState.isShowAsPlayer()) {
      return Role.PLAYER;
    } else {
      return getRole();
    }
  }

  public void setRole(Role role) {
    super.setRole(role);
  }

  public void setPasswordSalt(byte[] salt)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    if (password == null || Arrays.compare(password.salt(), salt) != 0) {
      password = CipherUtil.createKey(plainTextPassword, salt);
    }
  }

  public CipherUtil.Key getPassword() {
    return password;
  }

  @Override
  public Player getTransferablePlayer() {
    return new Player(getName(), getRole(), getPassword());
  }
}
