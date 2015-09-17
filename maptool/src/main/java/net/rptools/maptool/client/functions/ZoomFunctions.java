package net.rptools.maptool.client.functions;

import java.awt.Rectangle;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.CellPoint;
import net.rptools.maptool.model.Grid;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class ZoomFunctions extends AbstractFunction {
	/** Singleton for class **/
	private static final ZoomFunctions instance = new ZoomFunctions();

	private ZoomFunctions() {
		super(0, 6, "getZoom", "setZoom", "setViewArea");
	}

	public static ZoomFunctions getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> args) throws ParserException {
		if ("getZoom".equals(functionName)) {
			return getZ();
		}
		if ("setZoom".equals(functionName)) {
			return setZ(args);
		}
		if ("setViewArea".equals(functionName)) {
			return setViewArea(args);
		}
		return null;
	}

	/**
	 * Sets the scale or zoom distance on the current zone
	 * where a value of 0.5 = 50%, 1 = 100% and 2 = 200%
	 * @param args
	 * @return String
	 * @throws ParserException
	 */
	private String setZ(List<Object> args) throws ParserException {
		if (args.size() != 1) {
			throw new ParserException(I18N.getText("macro.function.general.wrongNumParam", "setZoom", 1, args.size()));
		}
		double zoom = 1;
		try {
			zoom = Double.valueOf(args.get(0).toString());
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", "moveToken", 1, args.get(0).toString()));
		}
		MapTool.getFrame().getCurrentZoneRenderer().setScale(zoom);

		return "";
	}

	/**
	 * Returns the scale or zoom distance on the current zone
	 * where a value of 0.5 = 50%, 1 = 100% and 2 = 200%
	 * @return String
	 * @throws ParserException
	 */
	private String getZ() throws ParserException {
		return Double.valueOf(MapTool.getFrame().getCurrentZoneRenderer().getScale()).toString();
	}

	/**
	 * Given a grid pixels or cell coordinates of top left (x1, y1) and bottom right (x2, y2)
	 * this function centres the screen over this area.
	 * @param args should contain int x1, int y1, int x2, int y2, boolean pixels, boolean enforceView
	 * @return
	 * @throws ParserException
	 */
	private String setViewArea(List<Object> args) throws ParserException {
		if (args.size() < 4) {
			throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", "setViewArea", 4, args.size()));
		}
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = 0;
		boolean pixels = true;
		boolean enforce = false;
		x1 = parseInteger(args, 0);
		y1 = parseInteger(args, 1);
		x2 = parseInteger(args, 2);
		y2 = parseInteger(args, 3);
		if (args.size() >= 5)
			pixels = parseBoolean(args, 4);
		if (args.size() >= 6)
			enforce = parseBoolean(args, 5);
		// If x & y not in pixels, use grid cell coordinates and convert to pixels
		if (!pixels) {
			Grid mapGrid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
			Rectangle fromBounds = mapGrid.getBounds(new CellPoint(x1, y1));
			x1 = fromBounds.x;
			y1 = fromBounds.y;
			Rectangle toBounds = mapGrid.getBounds(new CellPoint(x2, y2));
			x2 = toBounds.x + toBounds.width;
			y2 = toBounds.y + toBounds.height;
		}
		// enforceView command uses point at centre of screen
		int width = x2 - x1;
		int height = y2 - y1;
		int centreX = x1 + (width / 2);
		int centreY = y1 + (height / 2);
		MapTool.getFrame().getCurrentZoneRenderer().enforceView(centreX, centreY, 1, width, height);
		// if requested, set all players to map and match view
		if (enforce && MapTool.getParser().isMacroTrusted()) {
			MapTool.serverCommand().enforceZone(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId());
			MapTool.getFrame().getCurrentZoneRenderer().forcePlayersView();
		}
		return "";
	}

	private int parseInteger(List<Object> args, int param) throws ParserException {
		try {
			return Integer.valueOf(args.get(param).toString());
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyType", "setViewArea", param, args.get(param).toString()));
		}
	}

	private boolean parseBoolean(List<Object> args, int param) throws ParserException {
		try {
			return AbstractTokenAccessorFunction.getBooleanValue(args.get(param));
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentTypeInvalid", "setViewArea", param, args.get(param).toString()));
		}
	}

}
