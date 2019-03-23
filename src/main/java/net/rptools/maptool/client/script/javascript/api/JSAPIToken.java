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
package net.rptools.maptool.client.script.javascript.api;

import net.rptools.maptool.model.Token;

public class JSAPIToken {
  private final Token token;

  public JSAPIToken(Token token) {
    this.token = token;
  }

  public String getName() {
    return token.getName();
  }

  public void setName(String name) {
    token.setName(name);
  }

  public boolean hasSight() {
    return token.getHasSight();
  }

  public void setSight(boolean sight) {
    token.setHasSight(sight);
  }

  public String toString() {
    return "Token(id=" + token.getId() + ")";
  }
}
