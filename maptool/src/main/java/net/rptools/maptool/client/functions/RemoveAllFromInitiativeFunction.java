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
import net.rptools.maptool.model.InitiativeList;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Token.Type;
import net.rptools.parser.Parser;
import net.rptools.parser.ParserException;
import net.rptools.parser.function.AbstractFunction;

/**
 * Remove a set of tokens to initiative
 * 
 * @author Jay
 */
public class RemoveAllFromInitiativeFunction extends AbstractFunction {

	/** Handle removing, all, all PCs or all NPC tokens. */
	private RemoveAllFromInitiativeFunction() {
		super(0, 0, "removeAllFromInitiative", "removeAllPCsFromInitiative", "removeAllNPCsFromInitiative");
	}

	/** singleton instance of this function */
	private final static RemoveAllFromInitiativeFunction instance = new RemoveAllFromInitiativeFunction();

	/** @return singleton instance */
	public static RemoveAllFromInitiativeFunction getInstance() {
		return instance;
	}

	/**
	 * @see net.rptools.parser.function.AbstractFunction#childEvaluate(net.rptools.parser.Parser, java.lang.String, java.util.List)
	 */
	@Override
	public Object childEvaluate(Parser parser, String functionName, List<Object> args) throws ParserException {
		InitiativeList list = MapTool.getFrame().getCurrentZoneRenderer().getZone().getInitiativeList();
		int count = 0;

		if (!MapTool.getParser().isMacroTrusted()) {
			throw new ParserException(I18N.getText("macro.function.general.noPerm", functionName));
		}
		if (functionName.equals("removeAllFromInitiative")) {
			count = list.getSize();
			list.clearModel();
		} else {
			list.startUnitOfWork();
			boolean pcs = functionName.equals("removeAllPCsFromInitiative");
			for (int i = list.getSize() - 1; i >= 0; i--) {
				Token token = list.getTokenInitiative(i).getToken();
				if (token.getType() == Type.PC && pcs || token.getType() == Type.NPC && !pcs) {
					list.removeToken(i);
					count++;
				} // endif
			} // endfor
			list.setRound(-1);
			list.setCurrent(-1);
			list.finishUnitOfWork();
		} // endif
		return new BigDecimal(count);
	}
}
