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
package net.rptools.maptool.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.model.Player;
import net.rptools.maptool.model.Token;

/** Utility class to facilitate macro events like onTokenMove and onInitiativeChange. */
public class EventMacroUtil {

  /**
   * Scans all maps to find the first Lib:Token containing a macro that matches the given "callback"
   * string. If more than one token has such a macro, the first one encountered is returned -
   * because this order is unpredictable, this is very much not encouraged.
   *
   * @param macroCallback the macro name to find
   * @return the first Lib:token found that contains the requested macro, or null if none
   */
  public static Token getEventMacroToken(final String macroCallback) {
    List<ZoneRenderer> zrenderers = MapTool.getFrame().getZoneRenderers();
    for (ZoneRenderer zr : zrenderers) {
      List<Token> tokenList =
          zr.getZone().getTokensFiltered(t -> t.getName().toLowerCase().startsWith("lib:"));
      for (Token token : tokenList) {
        // If the token is not owned by everyone and all owners are GMs
        // then we are in
        // its a trusted Lib:token so we can run the macro
        if (token != null) {
          if (token.isOwnedByAll()) {
            continue;
          } else {
            Set<String> gmPlayers = new HashSet<String>();
            for (Object o : MapTool.getPlayerList()) {
              Player p = (Player) o;
              if (p.isGM()) {
                gmPlayers.add(p.getName());
              }
            }
            for (String owner : token.getOwners()) {
              if (!gmPlayers.contains(owner)) {
                continue;
              }
            }
          }
        }
        if (token.getMacro(macroCallback, false) != null) {
          return token;
        }
      }
    }
    return null;
  }
}
