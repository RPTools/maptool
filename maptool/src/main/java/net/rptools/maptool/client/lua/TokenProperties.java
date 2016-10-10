/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.util.List;

import net.rptools.CaseInsensitiveHashMap;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TokenProperty;
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
public class TokenProperties extends LuaTable {
	private MapToolToken token;
	private List<TokenProperty> propmap;
	public TokenProperties(MapToolToken mapToolToken) {
		this.token = mapToolToken;
		propmap = MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(token.getToken().getPropertyType());
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setProperty"))); 
		}
		rawget(key).rawset("value", value);
	}
	public LuaValue remove(int pos) { return error("table is read-only"); }
	@Override
	public LuaValue rawget(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty"))); 
		}
		return new MapToolTokenProperty(token, key.checkjstring(), propmap);
	}
	
	@Override
	public int length() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperties"))); 
		}
		return token.getToken().getPropertyMap().size();
	}
	
	@Override
	public Varargs next(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperties"))); 
		}
		CaseInsensitiveHashMap<Object> propertyMap = token.getToken().getPropertyMap();
		boolean found = false;
		String name=null;
		if (key.isnil()) {
			found = true;
		}
		else {
			name = key.checkjstring();
		}
		for (String prop: propertyMap.keySetRaw()) {
			if (found) {
				return varargsOf(valueOf(prop), LuaConverters.fromObj(propertyMap.get(prop)));
			}
			if (!found && prop != null && prop.equals(name)) {
				found = true;
			}
		}
		return NIL;
	}
	
	public String tojstring() {
		return "Properties for " + token.toString();
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
