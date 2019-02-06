/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
public class DrawingGetterFunctions extends DrawingFunctions {
	private static final DrawingGetterFunctions instance = new DrawingGetterFunctions();

	public static DrawingGetterFunctions getInstance() {
		return instance;
	}

	private DrawingGetterFunctions() {
		super(2, 2, "getDrawingLayer", "getDrawingOpacity", "getDrawingProperties", "getPenColor", "getFillColor", "getDrawingEraser", "getPenWidth", "getLineCap");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		checkTrusted(functionName);
		checkNumberOfParameters(functionName, parameters, 2, 2);
		String mapName = parameters.get(0).toString();
		String id = parameters.get(1).toString();
		Zone map = getNamedMap(functionName, mapName).getZone();
		GUID guid = getGUID(functionName, id);
		if ("getDrawingLayer".equalsIgnoreCase(functionName)) {
			return getDrawable(functionName, map, guid).getLayer();
		} else if ("getDrawingOpacity".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).getOpacity();
		} else if ("getDrawingProperties".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid);
		} else if ("getPenColor".equalsIgnoreCase(functionName)) {
			String result = paintToString(getPen(functionName, map, guid).getPaint());
			return result;
		} else if ("getFillColor".equalsIgnoreCase(functionName)) {
			String result = paintToString(getPen(functionName, map, guid).getBackgroundPaint());
			return result;
		} else if ("getDrawingEraser".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).isEraser() ? BigDecimal.ONE : BigDecimal.ZERO;
		} else if ("getPenWidth".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).getThickness();
		} else if ("getLineCap".equalsIgnoreCase(functionName)) {
			return getPen(functionName, map, guid).getSquareCap() ? BigDecimal.ONE : BigDecimal.ZERO;
		}
		return null;
	}
}
