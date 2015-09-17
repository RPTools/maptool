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
import net.rptools.maptool.model.Zone;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class SwitchTokenFunction extends AbstractFunction {
	private static final SwitchTokenFunction instance = new SwitchTokenFunction();

	private SwitchTokenFunction() {
		super(1, 1, "switchToken");
	}

	public static SwitchTokenFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> parameters) throws ParserException {
		if (!MapTool.getParser().isMacroTrusted()) {
			throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
		}
		if (parameters.size() < 1) {
			throw new ParserException(I18N.getText("macro.function.general.notEnoughParam", functionName, 1, parameters.size()));
		}
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		Token token = zone.resolveToken(parameters.get(0).toString());
		if (token == null) {
			throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, parameters.get(0).toString()));
		}
		((MapToolVariableResolver) parser.getVariableResolver()).setTokenIncontext(token);
		return "";
	}
}
