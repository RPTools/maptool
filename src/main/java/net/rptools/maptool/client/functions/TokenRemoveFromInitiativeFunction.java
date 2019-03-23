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

import java.math.BigDecimal;
import java.util.List;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * Remove a token from initiative
 *
 * @author Jay
 */
public class TokenRemoveFromInitiativeFunction extends AbstractFunction {

  /** Handle adding one, all, all PCs or all NPC tokens. */
  private TokenRemoveFromInitiativeFunction() {
    super(0, 1, "removeFromInitiative");
  }

  /** singleton instance of this function */
  private static final TokenRemoveFromInitiativeFunction instance =
      new TokenRemoveFromInitiativeFunction();

  /** @return singleton instance */
  public static TokenRemoveFromInitiativeFunction getInstance() {
    return instance;
  }

  /**
   * @see net.rptools.parser.function.AbstractFunction#childEvaluate(net.rptools.parser.Parser,
   *     java.lang.String, java.util.List)
   */
  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
    Token token = AbstractTokenAccessorFunction.getTarget(parser, args, 1);
    if (!MapTool.getParser().isMacroTrusted()) {
      if (!MapTool.getFrame().getInitiativePanel().hasOwnerPermission(token)) {
        String message = I18N.getText("macro.function.initiative.gmOnly", functionName);
        if (MapTool.getFrame().getInitiativePanel().isOwnerPermissions())
          message = I18N.getText("macro.function.initiative.gmOrOwner", functionName);
        throw new ParserException(message);
      } // endif
    }
    List<Integer> tokens = list.indexOf(token);
    list.startUnitOfWork();
    for (int i = tokens.size() - 1; i >= 0; i--) list.removeToken(tokens.get(i).intValue());
    list.finishUnitOfWork();
    return new BigDecimal(tokens.size());
  }
}
