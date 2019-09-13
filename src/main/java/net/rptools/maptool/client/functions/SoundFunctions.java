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
import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.util.FunctionUtil;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class SoundFunctions extends AbstractFunction {

  /** The singleton instance. */
  private static final SoundFunctions instance = new SoundFunctions();

  private SoundFunctions() {
    super(0, 5, "playStream", "stopStream", "editStream", "getStreamProperties");
  }

  /**
   * Gets the SoundFunctions instance.
   *
   * @return the instance.
   */
  public static SoundFunctions getInstance() {
    return instance;
  }

  @Override
  public Object childEvaluate(Parser parser, String functionName, List<Object> args)
      throws ParserException {
    int psize = args.size();
    if (functionName.equalsIgnoreCase("playStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 1, 5);
      if (!AppPreferences.getPlayStreams()) return -1; // do nothing if disabled in preferences
      String strUri = MediaPlayerAdapter.convertToURI(args.get(0));
      int cycleCount = psize > 1 ? FunctionUtil.paramAsInteger(functionName, args, 1, true) : 1;
      double volume = psize > 2 ? FunctionUtil.paramAsDouble(functionName, args, 2, true) : 1;
      double start = psize > 3 ? FunctionUtil.paramAsDouble(functionName, args, 3, true) * 1000 : 0;
      double stop = psize > 4 ? FunctionUtil.paramAsDouble(functionName, args, 4, true) * 1000 : -1;

      return MediaPlayerAdapter.playStream(strUri, cycleCount, volume, start, stop)
          ? BigDecimal.ONE
          : BigDecimal.ZERO;
    } else if (functionName.equalsIgnoreCase("editStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 2, 5);
      String strUri = MediaPlayerAdapter.convertToURI(args.get(0));

      Integer cycleCount = null;
      Double volume = null;
      Double start = null;
      Double stop = null;

      if (psize > 1 && !args.get(1).equals(""))
        cycleCount = FunctionUtil.paramAsInteger(functionName, args, 1, true);
      if (psize > 2 && !args.get(2).equals(""))
        volume = FunctionUtil.paramAsDouble(functionName, args, 2, true);
      if (psize > 3 && !args.get(3).equals(""))
        start = FunctionUtil.paramAsDouble(functionName, args, 3, true) * 1000;
      if (psize > 4 && !args.get(4).equals(""))
        stop = FunctionUtil.paramAsDouble(functionName, args, 4, true) * 1000;

      MediaPlayerAdapter.editStream(strUri, cycleCount, volume, start, stop);
      return "";
    } else if (functionName.equalsIgnoreCase("stopStream")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 3);
      String strUri = psize > 0 ? MediaPlayerAdapter.convertToURI(args.get(0)) : "*";
      boolean del = psize > 1 ? FunctionUtil.paramAsBoolean(functionName, args, 1, true) : true;
      double fade = psize > 2 ? FunctionUtil.paramAsDouble(functionName, args, 2, true) * 1000 : 0;
      MediaPlayerAdapter.stopStream(strUri, del, fade);
      return "";
    } else if (functionName.equalsIgnoreCase("getStreamProperties")) {
      FunctionUtil.checkNumberParam(functionName, args, 0, 1);
      String strUri = psize > 0 ? MediaPlayerAdapter.convertToURI(args.get(0)) : "*";
      return MediaPlayerAdapter.getStreamProperties(strUri);
    }
    return null;
  }
}
