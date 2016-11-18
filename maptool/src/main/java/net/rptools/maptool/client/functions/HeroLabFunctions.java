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
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * @author Jamz
 *
 * Functions to support reading new Hero Lab data from a token.
 * 
 */
public class HeroLabFunctions extends AbstractFunction {

	private static final HeroLabFunctions instance = new HeroLabFunctions();

	private HeroLabFunctions() {
		super(0, 2, "herolab.getInfo", "herolab.getStatBlock", "herolab.hasChanged", "herolab.XPath", "xpath.getText", "xpath.getNode");
	}

	public static HeroLabFunctions getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		String responseString = "";

		if (functionName.equals("herolab.getInfo")) {
			Token token;

			if (!MapTool.getParser().isMacroPathTrusted())
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

			if (parameters.size() == 1) {
				token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
				}
			} else if (parameters.size() == 0) {
				MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
				token = res.getTokenInContext();
				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
				}
			} else {
				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
			}

			if (token.getHeroLabData() == null)
				throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

			// Jamz: TODO actually return character info as a json...
			responseString = token.getHeroLabData().getSummary();

			return responseString;
		} else if (functionName.equals("herolab.getStatBlock")) {
			if (!MapTool.getParser().isMacroPathTrusted())
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

			if (parameters.size() > 2)
				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));

			if (parameters.isEmpty())
				throw new ParserException(I18N.getText("macro.function.general.notenoughparms", functionName, 1, parameters.size()));

			Token token = null;

			if (parameters.size() == 2) {
				token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
				}
			} else if (parameters.size() == 1) {
				MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
				token = res.getTokenInContext();

				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
				}
			}

			if (token.getHeroLabData() == null)
				throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

			String statBlockType = parameters.get(0).toString();

			return token.getHeroLabData().getStatBlock_data(statBlockType);
		} else if (functionName.equals("herolab.hasChanged")) {
			Token token;

			if (!MapTool.getParser().isMacroPathTrusted())
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

			if (parameters.size() == 1) {
				token = FindTokenFunctions.findToken(parameters.get(0).toString(), null);
				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
				}
			} else if (parameters.size() == 0) {
				MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
				token = res.getTokenInContext();
				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
				}
			} else {
				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));
			}

			if (token.getHeroLabData() == null)
				throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

			return token.getHeroLabData().isDirty();
		} else if (functionName.equals("herolab.XPath")) {
			if (!MapTool.getParser().isMacroPathTrusted())
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));

			if (parameters.size() > 2)
				throw new ParserException(I18N.getText("macro.function.general.tooManyParam", functionName, 1, parameters.size()));

			if (parameters.isEmpty())
				throw new ParserException(I18N.getText("macro.function.general.notenoughparms", functionName, 1, parameters.size()));

			Token token = null;

			if (parameters.size() == 2) {
				token = FindTokenFunctions.findToken(parameters.get(1).toString(), null);

				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
				}
			} else if (parameters.size() == 1) {
				MapToolVariableResolver res = (MapToolVariableResolver) parser.getVariableResolver();
				token = res.getTokenInContext();

				if (token == null) {
					throw new ParserException(I18N.getText("macro.function.general.noImpersonated", functionName));
				}
			}

			if (token.getHeroLabData() == null)
				throw new ParserException(I18N.getText("macro.function.herolab.null", functionName));

			String xPathExpression = parameters.get(0).toString();

			return token.getHeroLabData().parseXML(xPathExpression);
		}

		return "<ERROR>";
	}
}
