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

import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.server.proto.PlayerDto;
import net.rptools.maptool.util.cipher.CipherUtil;

/**
 * @author trevor
 */
public class Player {

  public enum Role {
    PLAYER(),
    GM();

    private final String displayName;

    Role() {
      if (name().equals("GM")) {
        displayName = I18N.getString("userTerm.GM");
      } else {
        displayName = I18N.getString("userTerm.Player");
      }
    }

    @Override
    public String toString() {
      return displayName;
    }
  }

  private String name; // Primary Key
  private String role;
  private GUID zoneId;
  private boolean loaded;

  private transient CipherUtil.Key password;
  private transient Role actualRole;

  public Player() {
    // For serialization
  }

  Player(String name, Role role, CipherUtil.Key password) {
    this.name = name;
    this.role = role.name();
    this.password = password;
    this.zoneId = null;
    this.loaded = true;
  }

  protected void setRole(Role role) {
    this.role = role.name();
    actualRole = role;
  }

  public GUID getZoneId() {
    return zoneId;
  }

  public void setZoneId(GUID zoneId) {
    this.zoneId = zoneId;
  }

  public boolean getLoaded() {
    return loaded;
  }

  public void setLoaded(boolean loaded) {
    this.loaded = loaded;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Player)) {
      return false;
    }
    return name.equals(((Player) obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  public boolean isGM() {
    return getRole() == Role.GM;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The name to set.
   */
  public void setName(String name) {
    this.name = name;
  }

  public CipherUtil.Key getPassword() {
    return password;
  }

  /**
   * @return Returns the role.
   */
  public Role getRole() {
    if (actualRole == null) {
      actualRole = Role.valueOf(role);
    }
    return actualRole;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", name, getRole().toString());
  }

  public Player getTransferablePlayer() {
    return this;
  }

  public static Player fromDto(PlayerDto dto) {
    var player = new Player();
    player.name = dto.getName();
    player.role = dto.getRole();
    player.zoneId = dto.getZoneGuid().equals("") ? null : GUID.valueOf(dto.getZoneGuid());
    player.loaded = dto.getLoaded();

    return player;
  }

  public PlayerDto toDto() {
    var builder = PlayerDto.newBuilder().setName(name).setRole(role).setLoaded(loaded);

    if (zoneId != null) {
      builder.setZoneGuid(zoneId.toString());
    }

    return builder.build();
  }
}
