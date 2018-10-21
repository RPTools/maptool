/*
 * This software Copyright by the RPTools.net development team, and licensed under the Affero GPL Version 3 or, at your option, any later version.
 *
 * MapTool Source Code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * You should have received a copy of the GNU Affero General Public License * along with this source Code. If not, please visit <http://www.gnu.org/licenses/> and specifically the Affero license text
 * at <http://www.gnu.org/licenses/agpl.html>.
 */
package net.rptools.maptool.client.functions;

import java.util.Iterator;
import java.util.List;

import net.rptools.maptool.model.GUID;
import net.rptools.maptool.model.Zone.Layer;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.model.drawing.DrawnElement;
import net.rptools.maptool_fx.MapTool;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;;

public class DrawingFunctions extends AbstractFunction {
	private static final DrawingFunctions instance = new DrawingFunctions();

	private DrawingFunctions() {
		super(2, 3, "getDrawingLayer", "setDrawingLayer", "bringDrawingToFront", "sendDrawingToBack");
	}

	public static DrawingFunctions getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		checkTrusted(functionName);
		if ("getDrawingLayer".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			List<DrawnElement> drawableList = map.getAllDrawnElements();
			Iterator<DrawnElement> iter = drawableList.iterator();
			while (iter.hasNext()) {
				DrawnElement de = iter.next();
				if (de.getDrawable().getId().equals(guid)) {
					System.out.println(de.getDrawable().getLayer());
					return de.getDrawable().getLayer();
				}
			}
		} else if ("setDrawingLayer".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 3, 3);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Layer layer = getLayer(parameters.get(2).toString());
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			return changeLayer(map, layer, guid);
		} else if ("bringDrawingToFront".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			BringToFront(map, guid);
			return "";
		} else if ("sendDrawingToBack".equalsIgnoreCase(functionName)) {
			checkNumberOfParameters(functionName, parameters, 2, 2);
			String mapName = parameters.get(0).toString();
			String id = parameters.get(1).toString();
			Zone map = getNamedMap(functionName, mapName).getZone();
			GUID guid = getGUID(functionName, id);
			SendToBack(map, guid);
			return "";
		}
		return null;
	}

	public void BringToFront(Zone map, GUID guid) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				map.removeDrawable(de.getDrawable().getId());
				MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
				map.addDrawable(new DrawnElement(de.getDrawable(), de.getPen()));
				MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
			}
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
	}

	public void SendToBack(Zone map, GUID guid) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getId().equals(guid)) {
				map.removeDrawable(de.getDrawable().getId());
				map.addDrawableRear(de);
			}
		}
		// horrid kludge needed to redraw zone :(
		for (DrawnElement de : map.getAllDrawnElements()) {
			MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
			MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
	}

	private Layer changeLayer(Zone map, Layer layer, GUID guid) {
		List<DrawnElement> drawableList = map.getAllDrawnElements();
		Iterator<DrawnElement> iter = drawableList.iterator();
		while (iter.hasNext()) {
			DrawnElement de = iter.next();
			if (de.getDrawable().getLayer() != layer && de.getDrawable().getId().equals(guid)) {
				map.removeDrawable(de.getDrawable().getId());
				MapTool.serverCommand().undoDraw(map.getId(), de.getDrawable().getId());
				de.getDrawable().setLayer(layer);
				map.addDrawable(de);
				MapTool.serverCommand().draw(map.getId(), de.getPen(), de.getDrawable());
			}
		}
		MapTool.getFrame().updateDrawTree();
		MapTool.getFrame().refresh();
		return layer;
	}

	/**
	 * Checks that the number of objects in the list <code>parameters</code> is within given bounds (inclusive). Throws a <code>ParserException</code> if the check fails.
	 *
	 * @param functionName
	 *            this is used in the exception message
	 * @param parameters
	 *            a list of parameters
	 * @param min
	 *            the minimum amount of parameters (inclusive)
	 * @param max
	 *            the maximum amount of parameters (inclusive)
	 * @throws ParserException
	 *             if there were more or less parameters than allowed
	 */
	private void checkNumberOfParameters(String functionName, List<Object> parameters, int min, int max) throws ParserException {
		int numberOfParameters = parameters.size();
		if (numberOfParameters < min) {
			throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, min, numberOfParameters));
		} else if (numberOfParameters > max) {
			throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, max, numberOfParameters));
		}
	}

	/**
	 * Checks whether or not the function is trusted
	 * 
	 * @param functionName
	 *            Name of the macro function
	 * @throws ParserException
	 *             Returns trust error message and function name
	 */
	private void checkTrusted(String functionName) throws ParserException {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
		}
	}

	/**
	 * Find the map/zone for a given map name
	 * 
	 * @param functionName
	 *            String Name of the calling function.
	 * @param mapName
	 *            String Name of the searched for map.
	 * @return ZoneRenderer The map/zone.
	 * @throws ParserException
	 *             if the map is not found
	 */
	private ZoneRenderer getNamedMap(String functionName, String mapName) throws ParserException {
		for (ZoneRenderer zr : MapTool.getFrame().getZoneRenderers()) {
			if (mapName.equals(zr.getZone().getName())) {
				return zr;
			}
		}
		throw new ParserException(I18N.getText("macro.function.moveTokenMap.unknownMap", functionName, mapName));
	}

	/**
	 * Take a string and return a layer
	 * 
	 * @param layer
	 *            String naming the layer
	 * @return Layer
	 */
	private Layer getLayer(String layer) {
		if ("GM".equalsIgnoreCase(layer))
			return Layer.GM;
		else if ("OBJECT".equalsIgnoreCase(layer))
			return Layer.OBJECT;
		else if ("BACKGROUND".equalsIgnoreCase(layer))
			return Layer.BACKGROUND;
		return Layer.TOKEN;
	}

	/**
	 * Validates the GUID
	 * 
	 * @param id
	 *            String value of GUID
	 * @return GUID
	 * @throws ParserException
	 *             thrown on invalid GUID
	 */
	private GUID getGUID(String functionName, String id) throws ParserException {
		try {
			return GUID.valueOf(id);
		} catch (Exception e) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyTypeG", functionName, id));
		}
	}
}
