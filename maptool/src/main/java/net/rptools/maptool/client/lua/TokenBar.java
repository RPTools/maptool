/**
 * 
 */
package net.rptools.maptool.client.lua;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.language.I18N;
import net.rptools.maptool.model.Token;
import net.rptools.parser.ParserException;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import static net.rptools.maptool.client.functions.TokenBarFunction.getBigDecimalValue;

/**
 * @author Maluku
 *
 */
public class TokenBar extends LuaTable {
	private MapToolToken token;

	public TokenBar(MapToolToken mapToolToken) {
		this.token = mapToolToken;
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
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.setState")));
		}
		String name = key.checkjstring();
		BigDecimal val = null;
		if (value.isnumber()) {
			val = BigDecimal.valueOf(value.checkdouble());
		}
		token.getToken().setState(name, val);
		MapTool.getFrame().getCurrentZoneRenderer().getZone().putToken(token.getToken());
		MapTool.serverCommand().putToken(MapTool.getFrame().getCurrentZoneRenderer().getZone().getId(), token.getToken());
	}

	public LuaValue remove(int pos) {
		return error("table is read-only");
	}

	@Override
	public LuaValue rawget(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getState")));
		}
		Object o = token.getToken().getState(key.checkjstring());
		if (o == null) {
			return NIL;
		}
		return LuaValue.valueOf(getBigDecimalValue(o).doubleValue());
	}

	@Override
	public int length() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getState")));
		}
		List<String> states = new LinkedList<String>(MapTool.getCampaign().getCampaignProperties().getTokenBarsMap().keySet());
		Iterator<String> it = states.iterator();
		Token t = token.getToken();
		while (it.hasNext()) {
			if (t.getState(it.next()) == null) {
				it.remove();
			}
		}
		return states.size();
	}

	@Override
	public Varargs next(LuaValue key) {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getState")));
		}
		boolean found = false;
		String name = null;
		if (key.isnil()) {
			found = true;
		} else {
			name = key.checkjstring();
		}
		Token t = token.getToken();
		for (String state : MapTool.getCampaign().getCampaignProperties().getTokenBarsMap().keySet()) {
			Object o = t.getState(state);
			if (found && o != null) {
				return varargsOf(valueOf(state), valueOf(getBigDecimalValue(o).doubleValue()));
			}
			if (!found && name.equals(state)) {
				found = true;
			}
		}
		return NIL;
	}

	public String tojstring() {
		return "bars for " + token.toString();
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
