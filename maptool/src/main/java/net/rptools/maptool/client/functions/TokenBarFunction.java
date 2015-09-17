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

package net.rptools.maptool.client.functions;

import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 *
 * @author Jay
 */
public class TokenBarFunction extends AbstractFunction {

	/** Support get and set bar on tokens */
	private TokenBarFunction() {
		super(1, 3, "getBar", "setBar", "isBarVisible", "setBarVisible");
	}

	/** singleton instance of this function */
	private final static TokenBarFunction instance = new TokenBarFunction();

	/** @return singleton instance */
	public static TokenBarFunction getInstance() {
		return instance;
	}

	/**
	 * @see net.rptools.parser.function.AbstractFunction#childEvaluate(net.rptools.parser.Parser, java.lang.String, java.util.List)
	 */
	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		Token token = AbstractTokenAccessorFunction.getTarget(parser, parameters, -1);
		String bar = (String) parameters.get(0);
		if (functionName.equals("getBar")) {
			return getValue(token, bar);
		} else if (functionName.equals("setBar")) {
			return setValue(token, bar, parameters.get(1));
		} else if (functionName.equals("isBarVisible")) {
			return isVisible(token, bar);
		} else {
			return setVisible(token, bar, AbstractTokenAccessorFunction.getBooleanValue(parameters.get(1)));
		}
	}

	/**
	 * Get the value for the bar.
	 * 
	 * @param token Get the value from this token
	 * @param bar For this bar
	 * @return A {@link BigDecimal} value.
	 * @throws ParserException
	 */
	public Object getValue(Token token, String bar) throws ParserException {
		return token.getState(bar);
	}

	/**
	 * @param token Set the value in this token
	 * @param bar For this bar
	 * @param value New value for the bar. Will be converted into a {@link BigDecimal} before setting
	 * @return The {@link BigDecimal} value that was actually set.
	 * @throws ParserException
	 */
	public Object setValue(Token token, String bar, Object value) throws ParserException {
		BigDecimal val = getBigDecimalValue(value);
		token.setState(bar, val);
		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
		return val;
	}

	public BigDecimal isVisible(Token token, String bar) {
		return token.getState(bar) == null ? BigDecimal.ZERO : BigDecimal.ONE;
	}

	public BigDecimal setVisible(Token token, String bar, boolean show) {
		token.setState(bar, show ? BigDecimal.ONE : null);
		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token);
		return show ? BigDecimal.ONE : BigDecimal.ZERO;
	}

	/**
	 * Convert the passed object into a big decimal value.
	 * 
	 * @param value The value to be converted
	 * @return The {@link BigDecimal} version of the value. The <code>null</code> value and strings
	 * that can't be converted to numbers return {@link BigDecimal#ZERO}
	 */
	public static BigDecimal getBigDecimalValue(Object value) {
		BigDecimal val = null;
		if (value instanceof BigDecimal) {
			val = (BigDecimal) value;
		} else if (value == null) {
			val = BigDecimal.ZERO;
		} else {
			try {
				val = new BigDecimal(value.toString());
			} catch (NumberFormatException e) {
				val = BigDecimal.ZERO;
			} // endtry
		} // endif
		return val;
	}

}
