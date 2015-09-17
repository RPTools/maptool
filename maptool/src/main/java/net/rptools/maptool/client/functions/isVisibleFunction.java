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

import java.awt.geom.Area;
import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.client.AppUtil;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class isVisibleFunction extends AbstractFunction {

	/** The singleton instance. */
	private static final isVisibleFunction instance = new isVisibleFunction();

	private isVisibleFunction() {
		super(2, 3, "isVisible");
	}

	/**
	 * Gets the instance of isVisibleFunction.
	 * 
	 * @return the instance.
	 */
	public static isVisibleFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> param) throws ParserException {
		ZoneRenderer zr = MapTool.getFrame().getCurrentZoneRenderer();
		Token token = null;

		// If there is more than two parameters (x,y) then the third parameter is the token
		// to test visibility for, so perform all the usual checks for trusted and fetch
		// the token from the zone.
		if (param.size() > 2) {
			if (!MapTool.getParser().isMacroTrusted()) {
				throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
			}
			token = zr.getZone().resolveToken(param.get(2).toString());
			if (token == null) {
				throw new ParserException(I18N.getText("macro.function.general.unknownToken", functionName, param.get(2).toString()));
			}
		} else {
			MapToolVariableResolver mvr = (MapToolVariableResolver) parser.getVariableResolver();
			if (mvr.getTokenInContext() != null) {
				token = mvr.getTokenInContext();
			}
			if (token == null) {
				throw new ParserException(I18N.getText("macro.function.initiative.noImpersonated", functionName));
			}
		}

		if (!(param.get(0) instanceof BigDecimal)) {
			throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", functionName, 1, param.get(0).toString()));
		}

		if (!(param.get(1) instanceof BigDecimal)) {
			throw new ParserException(I18N.getText("macro.function.general.argumentTypeN", functionName, 2, param.get(1).toString()));
		}
		if (token.isVisibleOnlyToOwner() && !AppUtil.playerOwns(token)) {
			return BigDecimal.ZERO;
		}
		int x = ((BigDecimal) param.get(0)).intValue();
		int y = ((BigDecimal) param.get(1)).intValue();

		Area visArea = zr.getZoneView().getVisibleArea(token);
		if (visArea == null) {
			return BigDecimal.ZERO;
		}

		return visArea.contains(x, y) ? BigDecimal.ONE : BigDecimal.ZERO;
	}

}
