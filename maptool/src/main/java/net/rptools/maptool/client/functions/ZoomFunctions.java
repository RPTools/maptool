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
		super(0, 5, "getZoom", "setZoom", "setViewArea");
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
	 * Given a grid cell of top left (x1, y1) and bottom right (x2, y2)
	 * this function centres the screen over this area.
	 * @param args should contain int x1, int y1, int x2, int y2
	 * @return
	 * @throws ParserException
	 */
	private String setViewArea(List<Object> args) throws ParserException {
		if (args.size() < 4) {
			throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", "setViewArea", 4, args.size()));
		}
		int x1=0;
		int y1=0;
		int x2=0;
		int y2=0;
		boolean enforce = false;
		try {
			x1 = Integer.valueOf(args.get(0).toString());
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyType", "setViewArea", 1, args.get(0).toString()));
		}
		try {
			y1 = Integer.valueOf(args.get(1).toString());
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyType", "setViewArea", 2, args.get(1).toString()));
		}
		try {
			x2 = Integer.valueOf(args.get(2).toString());
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyType", "setViewArea", 3, args.get(2).toString()));
		}
		try {
			y2 = Integer.valueOf(args.get(3).toString());
		} catch (NumberFormatException ne) {
			throw new ParserException(I18N.getText("macro.function.general.argumentKeyType", "setViewArea", 4, args.get(3).toString()));
		}
		if (args.size() == 5) {
			try {
				enforce = AbstractTokenAccessorFunction.getBooleanValue(args.get(4));
			} catch (NumberFormatException ne) {
				// do nothing
			}
		}
		Grid mapGrid = MapTool.getFrame().getCurrentZoneRenderer().getZone().getGrid();
		Rectangle fromBounds = mapGrid.getBounds(new CellPoint(x1,y1));
		Rectangle toBounds = mapGrid.getBounds(new CellPoint(x2,y2));
		int width = (toBounds.x + toBounds.width) - fromBounds.x;
		int height = (toBounds.y + toBounds.height) - fromBounds.y;
		int centreX = fromBounds.x + (width / 2);
		int centreY = fromBounds.y + (height / 2);
		MapTool.getFrame().getCurrentZoneRenderer().enforceView(centreX, centreY, 1, width, height);
		if (enforce  && MapTool.getParser().isMacroTrusted())
			MapTool.getFrame().getCurrentZoneRenderer().forcePlayersView();
		return "";
	}

}
