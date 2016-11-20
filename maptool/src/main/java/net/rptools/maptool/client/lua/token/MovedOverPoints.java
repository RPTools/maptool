package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenMoveFunctions;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class MovedOverPoints extends TwoArgFunction {
	
	MapToolToken token;
	
	public MovedOverPoints(MapToolToken token) {
		super();
		this.token = token;
	}
	@Override
	public LuaValue call(LuaValue pnts, LuaValue p) {
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.movedOverPoints")));
		}
		String points = pnts.checkjstring();
		if (p.isnil()) {
			Path<?> path = token.getToken().getLastPath();
			return LuaConverters.fromJson(TokenMoveFunctions.pathPointsToJSONArray(TokenMoveFunctions.crossedPoints(zone, token.getToken(), points, TokenMoveFunctions.getLastPathList(path, true))));
		} else {
			String jsonPath = ObjectUtils.toString(LuaConverters.toJson(p));
			return LuaConverters.fromJson(TokenMoveFunctions.pathPointsToJSONArray(TokenMoveFunctions.crossedPoints(zone, token.getToken(), points, jsonPath)));
		}
	}

}
