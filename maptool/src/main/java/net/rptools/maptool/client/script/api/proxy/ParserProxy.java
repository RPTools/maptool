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

package net.rptools.maptool.client.script.api.proxy;

import java.math.BigDecimal;

import net.rptools.parser.Expression;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;

public class ParserProxy {
	private final Parser parser;

	public ParserProxy(Parser parser) {
		this.parser = parser;
	}

	public void setVariable(String name, Object value) throws ParserException {
		if (value instanceof Number)
			parser.setVariable(name, new BigDecimal(((Number) value).doubleValue()));
		else
			parser.setVariable(name, value);
	}

	public Object getVariable(String variableName) throws ParserException {
		return parser.getVariable(variableName);
	}

	public Object evaluate(String expression) throws ParserException {
		Expression xp = parser.parseExpression(expression);
		return xp.evaluate();
	}
}
