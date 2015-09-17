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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Player;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

public class IsTrustedFunction extends AbstractFunction {
	private static final IsTrustedFunction instance = new IsTrustedFunction();

	private IsTrustedFunction() {
		super(0, 1, "isTrusted", "isGM");
	}

	public static IsTrustedFunction getInstance() {
		return instance;
	}

	@Override
	public Object childEvaluate(Parser parser, String functionName,
			List<Object> parameters) throws ParserException {
		if (functionName.equals("isTrusted")) {
			return MapTool.getParser().isMacroTrusted() ? BigDecimal.ONE : BigDecimal.ZERO;
		} else {
			// functionName is isGM
			if (parameters.isEmpty())
				return MapTool.getPlayer().isGM() ? BigDecimal.ONE : BigDecimal.ZERO;
			else {

				return getGMs().contains(parameters.get(0)) ? BigDecimal.ONE : BigDecimal.ZERO;
			}
		}
	}

	/**
	 * retrieves a list of GMs
	 * @return
	 * 
	 * copied from MacroLinkFunctions since its private there
	 */
	private List<String> getGMs() {
		List<String> gms = new ArrayList<String>();

		Iterator<Player> pliter = MapTool.getPlayerList().iterator();
		while (pliter.hasNext()) {
			Player plr = pliter.next();
			if (plr.isGM()) {
				gms.add(plr.getName());
			}
		}
		return gms;
	}

}
