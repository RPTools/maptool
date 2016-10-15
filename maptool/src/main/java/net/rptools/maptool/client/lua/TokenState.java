/**
 * 
 */
package net.rptools.maptool.client.lua;

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

/**
 * @author Maluku
 *
 */
public class TokenState extends LuaTable {
	private MapToolToken token;

	public TokenState(MapToolToken mapToolToken) {
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
		boolean val = true;
		if (value.isboolean() && !value.toboolean() || value.isnumber() && value.tonumber().toint() == 0) {
			val = false;
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
		return LuaValue.valueOf(Boolean.TRUE.equals(token.getToken().getState(key.checkjstring())));
	}

	@Override
	public int length() {
		if (!token.isSelfOrTrusted()) {
			throw new LuaError(new ParserException(I18N.getText("macro.function.general.noPerm", "token.getState")));
		}
		List<String> states = new LinkedList<String>(MapTool.getCampaign().getCampaignProperties().getTokenStatesMap().keySet());
		Iterator<String> it = states.iterator();
		Token t = token.getToken();
		while (it.hasNext()) {
			if (!Boolean.TRUE.equals(t.getState(it.next()))) {
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
		for (String state : MapTool.getCampaign().getCampaignProperties().getTokenStatesMap().keySet()) {
			if (found && Boolean.TRUE.equals(t.getState(state))) {
				return varargsOf(valueOf(state), valueOf(true));
			}
			if (!found && name.equals(state)) {
				found = true;
			}
		}
		return NIL;
	}

	public String tojstring() {
		return "states for " + token.toString();
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
