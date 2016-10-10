/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.token.ResetProperty;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

/**
 * @author Maluku
 *
 */

public class MapToolTokenProperty extends LuaTable {
	private MapToolToken token;
	private String property;
	public MapToolTokenProperty(MapToolToken token, String property, List<TokenProperty> defaultprops) {
		if (defaultprops != null) {
			for (TokenProperty propy : defaultprops) {
				if (property.equalsIgnoreCase(propy.getName()) || property.equalsIgnoreCase(propy.getShortName())) {
					super.rawset(LuaValue.valueOf("default"), LuaConverters.fromObj(propy.getDefaultValue()));
					super.rawset(LuaValue.valueOf("name"), LuaValue.valueOf(propy.getName()));
					property = propy.getName();
					break;
				}
			}
		}
		super.rawset(LuaValue.valueOf("raw"), LuaValue.valueOf(""));
		super.rawset(LuaValue.valueOf("value"), LuaValue.valueOf(""));
		super.rawset(LuaValue.valueOf("reset"), new ResetProperty(token, property));
		this.token = token;
		this.property = property;
	}
	public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
	public void set(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(int key, LuaValue value) { error("table is read-only"); }
	public void rawset(LuaValue key, LuaValue value) { 
		if (key.isstring()) {
			if (key.checkjstring().equals("value")) {
				if (!token.isSelfOrTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setProperty"))); 
				}
				Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
				token.getToken().setProperty(property, LuaConverters.toObj(value));
				MapTool.serverCommand().putToken(zone.getId(), token.getToken());
				zone.putToken(token.getToken());
				return;
			}				
		}
		error("table is read-only, except for value"); 
	}
	public LuaValue remove(int pos) { return error("table is read-only"); }
	
	@Override
	public LuaValue rawget(LuaValue key) {
		if (key.isstring()) {
			switch (key.tojstring()) {
			case "raw":
				if (!token.isSelfOrTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getPropertyRaw"))); 
				}
				return LuaConverters.fromObj(token.getToken().getProperty(property));
			case "value":
				if (!token.isSelfOrTrusted()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty"))); 
				}
				return LuaConverters.fromObj(token.getToken().getEvaluatedProperty(property));
			}
		}
		return super.rawget(key);
	}
	
	public String tojstring() {
		return "Property : "+ property +" for " + token.toString();
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
