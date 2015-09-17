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

import java.util.List;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;
import net.sf.json.JSONArray;

public class TokenSpeechFunctions extends AbstractFunction {

	private static final TokenSpeechFunctions instance = new TokenSpeechFunctions();

	private TokenSpeechFunctions() {
		super(0, 2, "getSpeech", "setSpeech", "getSpeechNames");
	}

	public static TokenSpeechFunctions getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName,
			List<Object> parameters) throws ParserException {
		final Token token = ((MapToolVariableResolver) parser.getVariableResolver()).getTokenInContext();
		if (token == null) {
			throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
		}

		if (functionName.equals("getSpeech")) {
			if (parameters.size() < 1) {
				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
			}
			String speech = token.getSpeech(parameters.get(0).toString());
			return speech == null ? "" : speech;
		}

		if (functionName.equals("setSpeech")) {
			if (parameters.size() < 2) {
				throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 2, parameters.size()));
			}
			token.setSpeech(parameters.get(0).toString(), parameters.get(1).toString());
			return "";
		}

		if (functionName.equals("getSpeechNames")) {
			String[] speech = new String[token.getSpeechNames().size()];
			String delim = parameters.size() > 0 ? parameters.get(0).toString() : ",";
			if ("json".equals(delim)) {
				return JSONArray.fromObject(token.getSpeechNames()).toString();
			} else {
				return StringFunctions.getInstance().join(token.getSpeechNames().toArray(speech), delim);
			}
		}
		return null;
	}

}
