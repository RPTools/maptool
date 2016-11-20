package net.rptools.maptool.client.lua.token;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenMoveFunctions;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Path;
import net.rptools.maptool.model.Token;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;

public class MovedOverToken extends TwoArgFunction {
	
	MapToolToken token;
	
	public MovedOverToken(MapToolToken token) {
		super();
		this.token = token;
	}
	@Override
	public LuaValue call(LuaValue tok, LuaValue p) {
		Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.movedOverToken")));
		}
		Token target;
		if (tok.isstring()) {
			target = zone.resolveToken(tok.checkjstring());
		} else if (tok instanceof MapToolToken) {
			target = ((MapToolToken) tok).getToken();
		} else {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.unknownToken", "token.movedOverToken", tok.toString())));
		}
		if (p.isnil()) {
			Path<?> path = token.getToken().getLastPath();
			return LuaConverters.fromJson(TokenMoveFunctions.pathPointsToJSONArray(TokenMoveFunctions.crossedToken(zone, token.getToken(), target, TokenMoveFunctions.getLastPathList(path, true))));
		} else {
			String jsonPath = ObjectUtils.toString(LuaConverters.toJson(p));
			return LuaConverters.fromJson(TokenMoveFunctions.pathPointsToJSONArray(TokenMoveFunctions.crossedToken(zone, token.getToken(), target, jsonPath)));
		}
	}

}
