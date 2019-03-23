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
package net.rptools.maptool.model;

/** @author trevor */
public class Player {
  public enum Role {
    PLAYER,
    GM
  }

  private String name; // Primary Key
  private String role;
  private String password;

  private transient Role actualRole;

  public Player() {
    // For serialization
  }

  public Player(String name, Role role, String password) {
    this.name = name;
    this.role = role.name();
    this.password = password;
    actualRole = role;
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

  /** @return Returns the name. */
  public String getName() {
    return name;
  }

  /** @param name The name to set. */
  public void setName(String name) {
    this.name = name;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPassword() {
    return password;
  }

  /** @return Returns the role. */
  public Role getRole() {
    if (actualRole == null) {
      actualRole = Role.valueOf(role);
    }
    return actualRole;
  }

  @Override
  public String toString() {
    return name + " " + (getRole() == Role.PLAYER ? "(Player)" : "(GM)");
  }
}
