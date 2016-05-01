package net.rptools.maptool.client.functions;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolLineParser;
import net.rptools.maptool.client.MapToolMacroContext;
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */
public class ParserPropertyFunctions extends AbstractFunction {

	private static ParserPropertyFunctions instance = new ParserPropertyFunctions();

	public static ParserPropertyFunctions getInstance() {
		return instance;
	}

	public ParserPropertyFunctions() {
		super(0, 1, "getMaxRecursionDepth", "setMaxRecursionDepth", "getMaxLoopIterations", "setMaxLoopIterations",
				"getRecursionDepth", "getMacroContext");
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> args) throws ParserException {
		MapToolLineParser mtlParser = MapTool.getParser();

		int argVal = 0;

		if (functionName.startsWith("set")) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
			}

			if (args.size() < 1) {
				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, args.size()));
			}

			argVal = ((BigDecimal) (args.get(0))).intValue();
		}

		int returnVal = 0;

		if (functionName.equals("getMaxRecursionDepth")) {
			returnVal = mtlParser.getMaxRecursionDepth();
		} else if (functionName.equalsIgnoreCase("getMaxLoopIterations")) {
			returnVal = mtlParser.getMaxLoopIterations();
		} else if (functionName.equals("getRecursionDepth")) {
			returnVal = mtlParser.getRecursionDepth();
		} else if (functionName.equals("getMacroContext")) {
			Map<String, Object> mco = new HashMap<String, Object>();
			MapToolMacroContext mc = mtlParser.getContext();
			mco.put("stackSize", mtlParser.getContextStackSize());
			mco.put("name", mc.getName());
			mco.put("source", mc.getSource());
			mco.put("trusted", mc.isTrusted());
			if (mc.getMacroButtonIndex() >= 0) {
				mco.put("buttonIndex", mc.getMacroButtonIndex());
			}
			return JSONObject.fromObject(mco);
		} else if (functionName.equals("setMaxRecursionDepth")) {
			mtlParser.setMaxRecursionDepth(argVal);
			returnVal = mtlParser.getMaxRecursionDepth();
		} else if (functionName.equals("setMaxLoopIterations")) {
			mtlParser.setMaxLoopIterations(argVal);
			returnVal = mtlParser.getMaxLoopIterations();
		}

		return BigDecimal.valueOf(returnVal);
	}
}
