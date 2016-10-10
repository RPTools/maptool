/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.math.BigDecimal;
import java.util.Collection;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.functions.TokenLightFunctions;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.LightSource;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */
public class TokenLight extends LuaTable {
	private MapToolToken token;
	private String category;
	public TokenLight(MapToolToken mapToolToken, String cat) {
		this.token = mapToolToken;
		this.category = cat;
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getLights"))); 
		}
		String name = key.checkjstring();
		BigDecimal val = BigDecimal.ONE;
		if (value.isboolean() && !value.toboolean() || value.isnumber() && value.tonumber().toint() == 0) {
			val = BigDecimal.ZERO;
		}
		try {
			TokenLightFunctions.setLight(token.getToken(), category, name, val);
		} catch (ParserException e) {
			throw new LuaError(e);
		}
	}
	public LuaValue remove(int pos) { return error("table is read-only"); }
	@Override
	public LuaValue rawget(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getLights"))); 
		}
		String name = key.checkjstring();
		for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(category).values()) {
			if (ls.getName().equals(name)) {
				return LuaValue.valueOf(token.getToken().hasLightSource(ls));
			}
		}
		return LuaValue.NIL;
	}
	
	@Override
	public int length() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getLights"))); 
		}
		int count = 0;
		for (LightSource ls : MapTool.getCampaign().getLightSourcesMap().get(category).values()) {
			if (token.getToken().hasLightSource(ls)) count++;
		}
		return count;
	}
	
	@Override
	public Varargs next(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getLights"))); 
		}
		Collection<LightSource> values = MapTool.getCampaign().getLightSourcesMap().get(category).values();
		boolean found = false;
		String name=null;
		if (key.isnil()) {
			found = true;
		}
		else {
			name = key.checkjstring();
		}
		for (LightSource source: values) {
			if (found && token.getToken().hasLightSource(source)) {
				return varargsOf(valueOf(source.getName()), valueOf(token.getToken().hasLightSource(source)));
			}
			if (!found && source.getName().equals(name)) {
				found = true;
			}
		}
		return NIL;
	}
	
	public String tojstring() {
		return category + " lights for " + token.toString();
	}
	
	@Override
	public LuaValue tostring() {
		return LuaValue.valueOf(tojstring());
	}
	@Override
	public LuaString checkstring() {
		return LuaValue.valueOf(tojstring());
	}
	@Override
	public String toString() {
		return tojstring();
	}
}
