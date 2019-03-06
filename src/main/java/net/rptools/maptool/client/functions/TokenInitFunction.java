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
package net.rptools.maptool.client.functions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList.TokenInitiative;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

/**
 * Set the token initiative
 *
 * @author Jay
 */
public class TokenInitFunction extends AbstractTokenAccessorFunction {

  /** Getter has 0 or 1, setter has 1 or 2 */
  private TokenInitFunction() {
    super(0, 2, "setInitiative", "getInitiative");
  }

  /** singleton instance of this function */
  private static final TokenInitFunction singletonInstance = new TokenInitFunction();

  /** @return singleton instance */
  public static TokenInitFunction getInstance() {
    return singletonInstance;
  };

  /**
   * @see
   *     net.rptools.maptool.client.functions.AbstractTokenAccessorFunction#getValue(net.rptools.maptool.model.Token)
   */
  @Override
  protected Object getValue(Token token) throws ParserException {
    String ret = "";
    List<TokenInitiative> tis = getTokenInitiatives(token);
    if (tis.isEmpty()) return I18N.getText("macro.function.TokenInit.notOnList");
    for (TokenInitiative ti : tis) {
      if (ret.length() > 0) ret += ", ";
      ret += ti.getState();
    } // endif
    return ret;
  }

  /**
   * @see
   *     net.rptools.maptool.client.functions.AbstractTokenAccessorFunction#setValue(net.rptools.maptool.model.Token,
   *     java.lang.Object)
   */
  @Override
  protected Object setValue(Token token, Object value) throws ParserException {
    String sValue = null;
    if (value != null) sValue = value.toString();
    List<TokenInitiative> tis = getTokenInitiatives(token);
    if (tis.isEmpty()) return I18N.getText("macro.function.TokenInit.notOnListSet");
    for (TokenInitiative ti : tis) ti.setState(sValue);
    return value;
  }

  /**
   * Get the first token initiative
   *
   * @param token Get it for this token
   * @return The first token initiative value for the passed token
   * @throws ParserException Token isn't in initiative.
   */
  public static TokenInitiative getTokenInitiative(Token token) throws ParserException {
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    List<Integer> list = zone.getInitiativeList().indexOf(token);
    if (list.isEmpty()) return null;
    return zone.getInitiativeList().getTokenInitiative(list.get(0).intValue());
  }

  /**
   * Get the first token initiative
   *
   * @param token Get it for this token
   * @return The first token initiative value for the passed token
   * @throws ParserException Token isn't in initiative.
   */
  @SuppressWarnings("unchecked")
  public static List<TokenInitiative> getTokenInitiatives(Token token) throws ParserException {
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    List<Integer> list = zone.getInitiativeList().indexOf(token);
    if (list.isEmpty()) return Collections.EMPTY_LIST;
    List<TokenInitiative> ret = new ArrayList<TokenInitiative>(list.size());
    for (Integer index : list)
      ret.add(zone.getInitiativeList().getTokenInitiative(index.intValue()));
    return ret;
  }
}
