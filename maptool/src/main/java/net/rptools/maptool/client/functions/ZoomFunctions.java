package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class ZoomFunctions extends AbstractFunction {
	/** Singleton for class **/
	private static final ZoomFunctions instance = new ZoomFunctions();
	
	private ZoomFunctions() {
		super(0, 1, "getZoom", "setZoom");
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
		
		if (!(args.get(0) instanceof BigDecimal)) {
			throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", "moveToken", 1, args.get(0).toString()));
		}
		double zoom = ((BigDecimal) args.get(0)).doubleValue();
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

}
