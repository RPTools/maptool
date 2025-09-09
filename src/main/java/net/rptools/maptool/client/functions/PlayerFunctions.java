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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.player.PlayerInfo;
import net.rptools.maptool.model.player.Players;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.VariableResolver;
import net.rptools.parser.function.AbstractFunction;

/** Class that implements player macro functions. */
public class PlayerFunctions extends AbstractFunction {

  /** Creates a new {@code PlayerFunctions} object. */
  public PlayerFunctions() {
    super(
        0,
        1,
        "player.getName",
        "player.getInfo",
        "player.getConnectedPlayers",
        "player.getPlayers");
  }

  @Override
  public Object childEvaluate(
      Parser parser, VariableResolver resolver, String functionName, List<Object> parameters)
      throws ParserException {

    try {
      Players players = new Players(MapTool.getClient().getPlayerDatabase());
      return switch (functionName) {
        case "player.getName" -> players.getPlayer().get().name();
        case "player.getInfo" -> {
          if (parameters.size() > 0) {
            FunctionUtil.blockUntrustedMacro(functionName);
            var pinfo = players.getPlayer(parameters.get(0).toString()).get();
            if (pinfo == null) {
              throw new ParserException(
                  I18N.getText("msg.error.playerDB.noSuchPlayer", parameters.get(0)));
            }
            yield playerAsJson(pinfo);
          } else {
            yield players.getPlayer().thenApply(this::playerAsJson).get();
          }
        }
        case "player.getConnectedPlayers" -> {
          FunctionUtil.blockUntrustedMacro(functionName);
          yield players.getConnectedPlayers().thenApply(this::playersAsJson).get();
        }
        case "player.getPlayers" -> {
          FunctionUtil.blockUntrustedMacro(functionName);
          yield players.getDatabasePlayers().thenApply(this::playersAsJson).get();
        }
        default ->
            throw new ParserException(
                I18N.getText("macro.function.general.unknownFunction", functionName));
      };
    } catch (InterruptedException | ExecutionException e) {
      throw new ParserException(e);
    }
  }

  /**
   * Returns a collection of {@link PlayerInfo} mapped to a Json Array.
   *
   * @param playerInfos the PlayerInfo objects to map
   * @return a collection of {@link PlayerInfo} mapped to a Json Array.
   */
  private JsonArray playersAsJson(Collection<PlayerInfo> playerInfos) {
    JsonArray players = new JsonArray();

    playerInfos.stream().map(this::playerAsJson).forEach(players::add);

    return players;
  }

  /**
   * Returns a {@link PlayerInfo} mapped to a Json object.
   *
   * @param playerInfo the PlayerInfo to map.
   * @return a {@link PlayerInfo} mapped to a Json object.
   */
  private JsonObject playerAsJson(PlayerInfo playerInfo) {
    JsonObject jobj = new JsonObject();
    jobj.addProperty("name", playerInfo.name());
    jobj.addProperty("role", playerInfo.role().name());
    jobj.addProperty("connected", playerInfo.connected());

    return jobj;
  }
}
