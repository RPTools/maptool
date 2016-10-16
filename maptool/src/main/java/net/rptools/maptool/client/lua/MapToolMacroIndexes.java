/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.util.ArrayList;

import net.rptools.maptool.client.MapToolVariableResolver;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.MacroButtonProperties;
import net.rptools.parser.ParserException;

import org.apache.commons.lang.ObjectUtils;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * @author Maluku
 *
 */

public class MapToolMacroIndexes extends LuaTable {
	private final MapToolToken token;
	private MapToolVariableResolver resolver;

	public MapToolMacroIndexes(MapToolToken token, MapToolVariableResolver resolver) {
		this.token = token;
		this.resolver = resolver;
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
		if (value.isnil()) {
			if (token.isSelfOrTrusted()) {
				MacroButtonProperties mbp = token.getToken().getMacro(value.checkint(), false);
				if (mbp == null) {
					throw new LuaError(new ParserException("removeMacro(): No button at index " + value.checkint() + " for " + token.getToken().getName()));
				}
				String label = mbp.getLabel();
				token.getToken().deleteMacroButtonProperty(mbp);
				StringBuilder sb = new StringBuilder();
				sb.append("Removed macro button ").append(label).append("(index = ").append(value.checkint());
				sb.append(") from ").append(token.getToken().getName());
				return;
			}
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.removeMacro")));
		} else {
			if (token.isSelfOrTrusted()) {
				LuaTable vl = rawget(key).checktable();
				if (vl instanceof Macro) { //Reduce the amount of updates
					Macro m = (Macro) vl;
					for (LuaValue prop : LuaConverters.keyIterate(value.checktable())) {
						if (!prop.checkjstring().equals("index")) {
							m.setProp(prop, value.get(prop));
						}
					}
					m.save();
					return;
				} else {
					for (LuaValue prop : LuaConverters.keyIterate(value.checktable())) {
						if (!prop.checkjstring().equals("index")) {
							vl.rawset(prop, value.get(prop));
						}
					}
					return;
				}
			}
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setMacroProps")));
		}
	}

	public LuaValue remove(int pos) {
		if (token.isSelfOrTrusted()) {
			ArrayList<Integer> indizes = new ArrayList<Integer>(token.getToken().getMacroPropertiesMap(false).keySet());
			if (pos < indizes.size()) {
				MacroButtonProperties mbp = token.getToken().getMacro(indizes.get(pos), false);
				if (mbp == null) {
					throw new LuaError(new ParserException("removeMacro(): No button at index " + indizes.get(pos) + " for " + token.getToken().getName()));
				}
				String label = mbp.getLabel();
				token.getToken().deleteMacroButtonProperty(mbp);
				StringBuilder sb = new StringBuilder();
				sb.append("Removed macro button ").append(label).append("(index = ").append(indizes.get(pos));
				sb.append(") from ").append(token.getToken().getName());
				return valueOf(sb.toString());
			} else {
				return NONE;
			}
		}
		throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.removeMacro")));
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		if (token.isSelfOrTrusted()) {
			if (key.isint()) {
				if (token.getToken().getMacroPropertiesMap(true).containsKey(key.toint())) {
					return new Macro(token, key.toint(), resolver);
				}
			}
		}
		return NIL;
	}

	public String tojstring() {
		return "Tables";
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

	@Override
	public int length() {
		if (token.isSelfOrTrusted()) {
			return token.getToken().getMacroPropertiesMap(false).size();
		}
		return 0;
	}

	@Override
	public Varargs next(LuaValue key) {
		if (token.isSelfOrTrusted()) {
			boolean found = false;
			Integer name = null;
			if (key.isnil()) {
				found = true;
			} else {
				name = key.checkint();
			}
			for (Integer i : token.getToken().getMacroPropertiesMap(false).keySet()) {
				if (found && !ObjectUtils.equals(name, i)) {
					return varargsOf(valueOf(i), new Macro(token, i, resolver));
				}
				if (!found && name.equals(i)) {
					found = true;
				}
			}
		}
		return NIL;
	}
}
