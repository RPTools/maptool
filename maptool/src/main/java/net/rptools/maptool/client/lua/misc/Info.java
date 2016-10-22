package net.rptools.maptool.client.lua.misc;

import net.rptools.maptool.client.functions.getInfoFunction;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class Info extends OneArgFunction {

	@Override
	public LuaValue call(LuaValue arg) {
		String infoType = arg.checkjstring();
		try {
			if (infoType.equalsIgnoreCase("map") || infoType.equalsIgnoreCase("zone")) {
				return LuaConverters.fromJson(getInfoFunction.getMapInfo());
			} else if (infoType.equalsIgnoreCase("client")) {
				return LuaConverters.fromJson(getInfoFunction.getClientInfo());
			} else if (infoType.equals("server")) {
				return LuaConverters.fromJson(getInfoFunction.getServerInfo());
			} else if (infoType.equals("campaign")) {
				return LuaConverters.fromJson(getInfoFunction.getCampaignInfo());
			} else if (infoType.equalsIgnoreCase("debug")) {
				return LuaConverters.fromJson(getInfoFunction.getDebugInfo());
			}
		} catch (ParserException e) {
			throw new LuaError(e);
		}
		
		return NIL;
	}

}
