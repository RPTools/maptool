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
package net.rptools.maptool.client.script_deprecated.api.proxy;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenBarFunction;
import net.rptools.maptool.client.functions.TokenGMNameFunction;
import net.rptools.maptool.client.functions.TokenHaloFunction;
import net.rptools.maptool.client.functions.TokenLabelFunction;
import net.rptools.maptool.client.functions.TokenNameFunction;
import net.rptools.maptool.client.functions.TokenStateFunction;
import net.rptools.maptool.client.functions.TokenVisibleFunction;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

public class TokenProxy {
  private final Token token;

  public TokenProxy(Token token) {
    this.token = token;
  }

  // States
  public boolean getState(String state) throws ParserException {
    return TokenStateFunction.getInstance().getBooleanTokenState(token, state);
  }

  public Object setState(String state, Object value) {
    return token.setState(state, value);
  }

  // Bars
  public Object getBar(String bar) throws ParserException {
    return TokenBarFunction.getInstance().getValue(token, bar);
  }

  public void setBar(String bar, Object value) throws ParserException {
    TokenBarFunction.getInstance().setValue(token, bar, value);
  }

  // Halos
  public String getHalo() {
    return TokenHaloFunction.getInstance().getHalo(token).toString();
  }

  public void setHalo(String color) throws ParserException {
    TokenHaloFunction.getInstance().setHalo(token, color);
  }

  // Names
  public String getName() {
    return TokenNameFunction.getInstance().getName(token);
  }

  public void setName(Object value) throws ParserException {
    TokenNameFunction.setName(token, value.toString());
  }

  // GM Name
  public String getGMName() throws ParserException {
    return TokenGMNameFunction.getInstance().getGMName(token);
  }

  public void setGMName(Object value) throws ParserException {
    TokenGMNameFunction.getInstance().setGMName(token, value.toString());
  }

  // Label
  public String getLabel() {
    return TokenLabelFunction.getInstance().getLabel(token);
  }

  public void setLabel(Object value) {
    TokenLabelFunction.getInstance().setLabel(token, value.toString());
  }

  // Visible
  public boolean getVisible() throws ParserException {
    return TokenVisibleFunction.getInstance().getBooleanVisible(token);
  }

  public void setVisible(Object value) throws ParserException {
    TokenVisibleFunction.getInstance().setVisible(token, value.toString());
  }

  // Properties
  public Object getProperty(String name) {
    return token.getEvaluatedProperty(name);
  }

  public void setProperty(String name, Object value) {
    token.setProperty(name, value.toString());
    MapTool.serverCommand()
        .putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
  }
}
