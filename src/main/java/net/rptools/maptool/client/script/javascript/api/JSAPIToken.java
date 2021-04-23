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

import java.util.Iterator;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import org.graalvm.polyglot.HostAccess;

public class JSAPIToken {

  private final Token token;
  private Set<String> names;
  private Iterator<String> names_iter;

  public JSAPIToken(Token token) {
    this.token = token;
  }

  public JSAPIToken(String tid) {
    this(MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(new GUID(tid)));
  }

  @HostAccess.Export
  public String getNotes() {
    return token.getNotes();
  }

  @HostAccess.Export
  public void setNotes(String notes) {
    token.setNotes(notes);
  }

  @HostAccess.Export
  public String getName() {
    return token.getName();
  }

  @HostAccess.Export
  public void setName(String name) {
    token.setName(name);
  }

  @HostAccess.Export
  public boolean hasSight() {
    return token.getHasSight();
  }

  @HostAccess.Export
  public void setSight(boolean sight) {
    token.setHasSight(sight);
  }

  @HostAccess.Export
  public String toString() {
    return "Token(id=" + token.getId() + ")";
  }

  @HostAccess.Export
  public String getId() {
    return "" + token.getId();
  }

  @HostAccess.Export
  public String getProperty(String name) {
    return "" + this.token.getProperty(name);
  }

  @HostAccess.Export
  public void setProperty(String name, String value) {
    this.token.setProperty(name, value);
  }

  @HostAccess.Export
  public int getX() {
    return this.token.getX();
  }

  @HostAccess.Export
  public int getY() {
    return this.token.getY();
  }

  @HostAccess.Export
  public void setX(int x) {
    this.token.setX(x);
  }

  @HostAccess.Export
  public void setY(int y) {
    this.token.setY(y);
  }
}
