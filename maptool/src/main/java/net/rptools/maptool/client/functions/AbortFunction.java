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

import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractNumberFunction;

/**
 * Aborts the current parser evaluation.
 * 
 * @author knizia.fan
 */
public class AbortFunction extends AbstractNumberFunction {
	public AbortFunction() {
		super(1, 1, "abort");
	}

	/** The singleton instance. */
	private final static AbortFunction instance = new AbortFunction();

	/**
	 * Gets the Input instance.
	 * 
	 * @return the instance.
	 */
	public static AbortFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		BigDecimal value = (BigDecimal) parameters.get(0);
		if (value.intValue() == 0)
			throw new AbortFunctionException(I18N.getText("macro.function.abortFunction.message", "Abort()"));
		else
			return new BigDecimal(value.intValue());
	}

	/** Exception type thrown by abort() function. Semantics are to silently halt the current execution. */
	public class AbortFunctionException extends ParserException {
		public AbortFunctionException(Throwable cause) {
			super(cause);
		}

		public AbortFunctionException(String msg) {
			super(msg);
		}
	}
}
