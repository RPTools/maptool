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
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class HasImpersonated extends AbstractFunction {
  private static final HasImpersonated instance = new HasImpersonated();

  private HasImpersonated() {
    super(0, 0, "hasImpersonated");
  }

  public static HasImpersonated getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
    Token t;
    GUID guid = MapTool.getFrame().getCommandPanel().getIdentityGUID();
    if (guid != null) t = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
    else t = zone.resolveToken(MapTool.getFrame().getCommandPanel().getIdentity());
    return t == null ? BigDecimal.ZERO : BigDecimal.ONE;
  }
}
