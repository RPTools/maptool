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
import net.rptools.maptool.language.I18N;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class DefineMacroFunction extends AbstractFunction {
	private static final DefineMacroFunction instance = new DefineMacroFunction();

	private DefineMacroFunction() {
		super(0, UNLIMITED_PARAMETERS, "defineFunction", "isFunctionDefined", "oldFunction");
	}

	public static DefineMacroFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName,
			List<Object> parameters) throws ParserException {
		if (functionName.equals("defineFunction")) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
			}

			if (parameters.size() < 2) {
				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
			}

			String macro = parameters.get(1).toString();
			if (macro.toLowerCase().endsWith("@this")) {
				macro = macro.substring(0, macro.length() - 4) + MapTool.getParser().getMacroSource();
			}

			boolean ignoreOutput = false;
			if (parameters.size() > 2) {
				if (!(parameters.get(2) instanceof BigDecimal)) {
					throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", functionName, 3, parameters.get(2).toString()));
				}
				ignoreOutput = !BigDecimal.ZERO.equals(parameters.get(2));
			}
			boolean newVariableContext = true;
			if (parameters.size() > 3) {
				if (!(parameters.get(3) instanceof BigDecimal)) {
					throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", functionName, 4, parameters.get(3).toString()));
				}
				newVariableContext = !BigDecimal.ZERO.equals(parameters.get(3));
			}

			UserDefinedMacroFunctions.getInstance().defineFunction(parser, parameters.get(0).toString(), macro, ignoreOutput, newVariableContext);
			return I18N.getText("macro.function.defineFunction.functionDefined", parameters.get(0).toString());
		} else if (functionName.equals("oldFunction")) {
			return UserDefinedMacroFunctions.getInstance().executeOldFunction(parser, parameters);
		} else { // isFunctionDefined

			if (UserDefinedMacroFunctions.getInstance().isFunctionDefined(parameters.get(0).toString())) {
				return BigDecimal.ONE;
			}

			if (parser.getFunction(parameters.get(0).toString()) != null) {
				return BigDecimal.valueOf(2);
			}

			return BigDecimal.ZERO;
		}

	}

}
