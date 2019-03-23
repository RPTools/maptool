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
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * Support for accessing getters and setters on tokens using a function
 *
 * @author Jay
 */
public abstract class AbstractTokenAccessorFunction extends AbstractFunction {

  /**
   * @param minParameters Maximum number of parameters allowed on a function call
   * @param maxParameters Minimum number of parameters allowed on a function call
   * @param aliases All function names handled by this instance.
   */
  public AbstractTokenAccessorFunction(int minParameters, int maxParameters, String... aliases) {
    super(minParameters, maxParameters, aliases);
  }

  /**
   * External call to get the token value.
   *
   * @param token Get the value from this token
   * @return Get the value from the token.
   * @throws ParserException Error setting value
   */
  public Object getTokenValue(Token token) throws ParserException {
    return getValue(token);
  }

  /**
   * External call to set the token value
   *
   * @param token Set this token
   * @param value To this value.
   * @return The new value of the token variable.
   * @throws ParserException Error setting value
   */
  public Object setTokenValue(Token token, Object value) throws ParserException {
    Object ret = setValue(token, value);
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
    return ret;
  }

  /**
   * Get the token that is being modified.
   *
   * @param parser Parser being evaluated.
   * @param args parameters to the function.
   * @param count Number of parameters expected if the token GUID was passed.
   * @return The token being modified.
   * @throws ParserException Unable to get a token.
   */
  public static Token getTarget(Parser parser, List<Object> args, int count)
      throws ParserException {
    Token token = null;
    if ((args.size() == count || !args.isEmpty() && count < 0) && args.get(0) instanceof GUID) {
      GUID guid = (GUID) args.get(0);
      args.remove(0);
      token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getToken(guid);
      if (token == null)
        throw new ParserException(
            I18N.getText(
                "macro.function.initiative.unknownToken",
                guid,
                MapTool.getFrame().getCurrentZoneRenderer().getZone().getName()));
    } else {
      token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
      if (token == null)
        throw new ParserException(I18N.getText("macro.function.initiative.noImpersonated"));
    } // endif
    return token;
  }

  /**
   * @see net.rptools.parser.function.AbstractFunction#childEvaluate(net.rptools.parser.Parser,
   *     java.lang.String, java.util.List)
   */
  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> parameters)
      throws ParserException {
    Token token = getTarget(parser, parameters, functionName.startsWith("set") ? 2 : 1);
    if (functionName.startsWith("set")) {
      return setTokenValue(token, parameters.get(0));
    } // endif
    return getTokenValue(token);
  }

  /**
   * @param token Get the value from this token
   * @return Get the value from the token.
   * @throws ParserException Error getting value
   */
  protected abstract Object getValue(Token token) throws ParserException;

  /**
   * @param token Set this token
   * @param value To this value.
   * @return The value that was set
   * @throws ParserException Error setting value
   */
  protected abstract Object setValue(Token token, Object value) throws ParserException;

  /**
   * Convert an object into a boolean value.
   *
   * @param value Convert this object. Must be {@link Boolean}, {@link BigDecimal}, or a can have
   *     its string value be converted to one of those types.
   * @return The boo
   */
  public static boolean getBooleanValue(Object value) {
    boolean set = false;
    if (value instanceof Boolean) {
      set = ((Boolean) value).booleanValue();
    } else if (value instanceof Number) {
      set = ((Number) value).doubleValue() != 0;
    } else if (value == null) {
      set = false;
    } else {
      try {
        set = !new BigDecimal(value.toString()).equals(BigDecimal.ZERO);
      } catch (NumberFormatException e) {
        set = Boolean.parseBoolean(value.toString());
      } // endif
    } // endif
    return set;
  }
}
