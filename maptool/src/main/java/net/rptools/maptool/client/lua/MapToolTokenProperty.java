/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.math.BigDecimal;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.lua.token.ResetProperty;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.TokenProperty;
import net.rptools.maptool.model.Zone;
import net.rptools.maptool.util.StringUtil;
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

	public MapToolTokenProperty(MapToolToken token, String property) {
//			, List<TokenProperty> defaultprops) {
//		if (defaultprops != null) {
//			for (TokenProperty propy : defaultprops) {
//				if (property.equalsIgnoreCase(propy.getName()) || property.equalsIgnoreCase(propy.getShortName())) {
//					super.rawset(LuaValue.valueOf("default"), LuaConverters.fromObj(propy.getDefaultValue()));
//					super.rawset(LuaValue.valueOf("name"), LuaValue.valueOf(propy.getName()));
//					property = propy.getName();
//					break;
//				}
//			}
//		}
		super.rawset(LuaValue.valueOf("raw"), LuaValue.valueOf(""));
		super.rawset(LuaValue.valueOf("value"), LuaValue.valueOf(""));
		super.rawset(LuaValue.valueOf("converted"), LuaValue.valueOf(""));
		super.rawset(LuaValue.valueOf("reset"), new ResetProperty(token, property));
		this.token = token;
		this.property = property;
	}

	public LuaValue setmetatable(LuaValue metatable) {
		return error("table is read-only");
	}

	public void set(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(int key, LuaValue value) {
		error("table is read-only");
	}

	public void rawset(LuaValue key, LuaValue value) {
		if (key.isstring()) {
			if (key.checkjstring().equals("value")) {
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setProperty")));
				}
				if (token.isLib()) {
					try {
						token.getToken().setProperty(property, LuaConverters.toJson(value).toString());
						Zone z;
						z = MapTool.getParser().getTokenMacroLibZone(token.getToken().getName());
						MapTool.serverCommand().putToken(z.getId(), token.getToken());
						z.putToken(token.getToken());
					} catch (ParserException e) {
						throw new LuaError(e);
					}
				} else {
					Zone zone = MapTool.getFrame().getCurrentZoneRenderer().getZone();
					token.getToken().setProperty(property, LuaConverters.toJson(value).toString());
					MapTool.serverCommand().putToken(zone.getId(), token.getToken());
					zone.putToken(token.getToken());
					return;
				}
			}
		}
		error("table is read-only, except for value");
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}
	
	private static Object maybeNumber(Object val) {
		if (val instanceof String) {
			// try to convert to a number
			try {
				return new BigDecimal(val.toString());
			} catch (Exception e) {
				return val;
			}
		} else {
			return val;
		}
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		if (key.isstring()) {
			switch (key.tojstring()) {
			case "default": {
				List<TokenProperty> tokenPropertyList = MapTool.getCampaign().getCampaignProperties().getTokenPropertyList(token.getToken().getPropertyType());
				if (tokenPropertyList == null) {
					return LuaValue.NIL;
				}
				for (TokenProperty propy : tokenPropertyList) {
					if (property.equalsIgnoreCase(propy.getName()) || property.equalsIgnoreCase(propy.getShortName())) {
						super.rawset(LuaValue.valueOf("default"), LuaConverters.fromObj(propy.getDefaultValue()));
						super.rawset(LuaValue.valueOf("name"), LuaValue.valueOf(propy.getName()));
						property = propy.getName();
						break;
					}
				}
			}
			case "raw":
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getPropertyRaw")));
				}
				return LuaConverters.fromObj(token.getToken().getProperty(property));
			case "value":
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty")));
				}
				return LuaConverters.fromObj(maybeNumber(token.getToken().getEvaluatedProperty(property)));
			case "converted":
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty")));
				}
				return LuaConverters.fromJson(maybeNumber(token.getToken().getEvaluatedProperty(property)));
			case "defined":
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty")));
				}
				return LuaValue.valueOf(token.getToken().getPropertyMap().containsKey(property));
			case "exists":
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty")));
				}
				Object o = token.getToken().getEvaluatedProperty(property);
				return LuaValue.valueOf(o != null && !StringUtil.isEmpty(o.toString()));
			case "empty":
				if (!token.isSelfOrTrustedOrLib()) {
					throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getProperty")));
				}
				return LuaValue.valueOf(!token.getToken().getPropertyMap().containsKey(property) || token.getToken().getPropertyMap().get(property) == null);
			}
		}
		return super.rawget(key);
	}

	public String tojstring() {
		return "Property : " + property + " for " + token.toString();
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
