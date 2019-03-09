package de.jadebringer.maptool.framework;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.Function;

public class FunctionCaller {

	public static Object callFunction(String functionName, Function f, Parser parser, Object...parameters) throws ParserException {
		return f.evaluate(parser, functionName,	Arrays.asList(parameters));
	}
	
	public static Object callFunction(String functionName, Parser parser, Object...parameters) throws ParserException {
		Function f = parser.getFunction(functionName);
		return f.evaluate(parser, functionName,	Arrays.asList(parameters));
	}
	
	public static List<Object> toObjectList(Object...parameters) {
		return Arrays.asList(parameters);
	}
	
	public static <T> T getParam(List<Object> parameters, int i) {
		return getParam(parameters, i, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getParam(List<Object> parameters, int i, T defaultValue) {
		if (parameters != null && parameters.size() > i) {
			return (T)parameters.get(i);
		} else {
			return defaultValue;
		}
	}

	public static boolean toBoolean(Object val) {
		if (val instanceof BigDecimal) {
			return BigDecimal.ZERO.equals(val) ? false : true;
		} else if (val instanceof Boolean) {
			return (Boolean)val;
		}
		
		return false;
	}
}
