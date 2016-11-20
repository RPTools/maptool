package net.rptools.maptool.client.lua.token;

import java.util.List;
import java.util.Map;

import net.rptools.maptool.client.functions.TokenMoveFunctions;
import net.rptools.maptool.client.lua.LuaConverters;
import net.rptools.maptool.client.lua.MapToolToken;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Path;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;

public class LastPath extends OneArgFunction {
	
	MapToolToken token;
	
	public LastPath(MapToolToken token) {
		super();
		this.token = token;
	}
	@Override
	public LuaValue call(LuaValue arg) {
		boolean units = arg.isboolean() ? arg.toboolean() : true;
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getLastPath")));
		}
		Path<?> path = token.getToken().getLastPath();

		List<Map<String, Integer>> pathPoints = TokenMoveFunctions.getLastPathList(path, units);
		return LuaConverters.fromJson(TokenMoveFunctions.pathPointsToJSONArray(pathPoints));
	}

}
